<?php
/**
 * LiteRise - Submit Assessment Responses API
 * Endpoint: POST /api/submit_responses.php
 * Processes responses, calculates new ability using IRT, updates database
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';
require_once 'irt.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit();
}

$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['StudentID']) || !isset($input['Responses'])) {
    http_response_code(400);
    echo json_encode(['error' => 'StudentID and Responses required']);
    exit();
}

$studentId = (int)$input['StudentID'];
$responses = $input['Responses'];

if (!is_array($responses) || empty($responses)) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid responses format']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Get or create session
    $sessionId = isset($input['SessionID']) ? (int)$input['SessionID'] : null;

    if (!$sessionId) {
        // Create new session
        $sql = "EXEC SP_CreateTestSession @StudentID = :studentId, @Type = 'PreAssessment'";
        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
        $stmt->execute();
        $session = $stmt->fetch();
        $sessionId = (int)$session['SessionID'];
        $currentTheta = (float)$session['InitialTheta'];
    } else {
        // Get existing session
        $sql = "SELECT InitialTheta, StudentID FROM TestSessions WHERE SessionID = :sessionId";
        $stmt = $conn->prepare($sql);
        $stmt->bindParam(':sessionId', $sessionId, PDO::PARAM_INT);
        $stmt->execute();
        $session = $stmt->fetch();
        $currentTheta = (float)$session['InitialTheta'];
    }

    // Get item details for IRT calculation
    $itemIds = array_column($responses, 'ItemID');
    $placeholders = implode(',', array_fill(0, count($itemIds), '?'));

    $sql = "SELECT ItemID, DifficultyParam as Difficulty,
                   DiscriminationParam as Discrimination,
                   GuessingParam as Guessing
            FROM Items
            WHERE ItemID IN ($placeholders)";

    $stmt = $conn->prepare($sql);
    $stmt->execute($itemIds);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Create lookup for items
    $itemLookup = [];
    foreach ($items as $item) {
        $itemLookup[$item['ItemID']] = [
            'Difficulty' => (float)$item['Difficulty'],
            'Discrimination' => (float)$item['Discrimination'],
            'Guessing' => (float)($item['Guessing'] ?? 0.25)
        ];
    }

    // Prepare responses with item parameters for IRT
    $irtResponses = [];
    $irtItems = [];

    foreach ($responses as $response) {
        $itemId = (int)$response['ItemID'];

        if (!isset($itemLookup[$itemId])) {
            continue;
        }

        $irtResponses[] = [
            'ItemID' => $itemId,
            'Correct' => (bool)($response['Correct'] ?? false)
        ];

        $irtItems[] = $itemLookup[$itemId];
    }

    // Calculate new ability using IRT
    $newTheta = IRTCalculator::updateAbility($currentTheta, $irtResponses, $irtItems);

    // Calculate standard error and reliability
    $standardError = IRTCalculator::calculateStandardError($newTheta, $irtItems);
    $reliability = IRTCalculator::calculateReliability($irtItems, $newTheta);

    // Begin transaction
    $conn->beginTransaction();

    try {
        // Insert responses into database
        $insertSql = "INSERT INTO Responses
                      (SessionID, ItemID, StudentResponse, IsCorrect, TimeSpent,
                       ThetaBeforeResponse, ThetaAfterResponse, Timestamp)
                      VALUES
                      (:sessionId, :itemId, :response, :correct, :timeSpent,
                       :thetaBefore, :thetaAfter, GETDATE())";

        $insertStmt = $conn->prepare($insertSql);

        $runningTheta = $currentTheta;

        foreach ($responses as $index => $response) {
            $itemId = (int)$response['ItemID'];
            $selectedOption = $response['SelectedOption'] ?? '';
            $isCorrect = (bool)($response['Correct'] ?? false);
            $timeSpent = (float)($response['TimeTakenSec'] ?? 0);

            // Calculate theta after this response (progressive update)
            if ($index < count($irtResponses) - 1) {
                $partialResponses = array_slice($irtResponses, 0, $index + 1);
                $partialItems = array_slice($irtItems, 0, $index + 1);
                $thetaAfter = IRTCalculator::updateAbility($currentTheta, $partialResponses, $partialItems);
            } else {
                $thetaAfter = $newTheta;
            }

            $insertStmt->bindParam(':sessionId', $sessionId, PDO::PARAM_INT);
            $insertStmt->bindParam(':itemId', $itemId, PDO::PARAM_INT);
            $insertStmt->bindParam(':response', $selectedOption, PDO::PARAM_STR);
            $insertStmt->bindParam(':correct', $isCorrect, PDO::PARAM_BOOL);
            $insertStmt->bindParam(':timeSpent', $timeSpent, PDO::PARAM_STR);
            $insertStmt->bindParam(':thetaBefore', $runningTheta, PDO::PARAM_STR);
            $insertStmt->bindParam(':thetaAfter', $thetaAfter, PDO::PARAM_STR);
            $insertStmt->execute();

            $runningTheta = $thetaAfter;
        }

        // Update session statistics
        $totalQuestions = count($responses);
        $correctAnswers = count(array_filter($responses, function($r) {
            return $r['Correct'] ?? false;
        }));
        $accuracy = ($totalQuestions > 0) ? ($correctAnswers / $totalQuestions) * 100 : 0;

        $updateSessionSql = "UPDATE TestSessions
                             SET FinalTheta = :finalTheta,
                                 EndTime = GETDATE(),
                                 TotalQuestions = :total,
                                 CorrectAnswers = :correct,
                                 AccuracyPercentage = :accuracy,
                                 IsCompleted = 1
                             WHERE SessionID = :sessionId";

        $updateStmt = $conn->prepare($updateSessionSql);
        $updateStmt->bindParam(':finalTheta', $newTheta, PDO::PARAM_STR);
        $updateStmt->bindParam(':total', $totalQuestions, PDO::PARAM_INT);
        $updateStmt->bindParam(':correct', $correctAnswers, PDO::PARAM_INT);
        $updateStmt->bindParam(':accuracy', $accuracy, PDO::PARAM_STR);
        $updateStmt->bindParam(':sessionId', $sessionId, PDO::PARAM_INT);
        $updateStmt->execute();

        // Update student's current ability
        $updateStudentSql = "UPDATE Students
                             SET CurrentAbility = :theta,
                                 InitialAbility = CASE
                                     WHEN InitialAbility = 0 THEN :theta
                                     ELSE InitialAbility
                                 END
                             WHERE StudentID = :studentId";

        $updateStudentStmt = $conn->prepare($updateStudentSql);
        $updateStudentStmt->bindParam(':theta', $newTheta, PDO::PARAM_STR);
        $updateStudentStmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
        $updateStudentStmt->execute();

        // Commit transaction
        $conn->commit();

        // Return success response with IRT statistics
        $responseData = [
            'success' => true,
            'SessionID' => $sessionId,
            'FinalTheta' => $newTheta,
            'InitialTheta' => $currentTheta,
            'ThetaChange' => round($newTheta - $currentTheta, 4),
            'TotalQuestions' => $totalQuestions,
            'CorrectAnswers' => $correctAnswers,
            'Accuracy' => round($accuracy, 2),
            'StandardError' => $standardError,
            'Reliability' => $reliability,
            'message' => 'Assessment completed successfully'
        ];

        http_response_code(200);
        echo json_encode($responseData);

    } catch (Exception $e) {
        $conn->rollBack();
        throw $e;
    }

} catch (Exception $e) {
    error_log("Submit responses error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to submit responses', 'details' => $e->getMessage()]);
}
