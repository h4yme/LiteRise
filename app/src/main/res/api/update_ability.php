<?php
require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/irt.php';

$data = json_decode(file_get_contents("php://input"), true);

$studentID = $data['student_id'] ?? null;
$sessionID = $data['session_id'] ?? null;

if (!$studentID) {
    sendError("Missing student_id", 400);
}

try {
    $irt = new ItemResponseTheory();
    
    // Get responses from this session (or all recent sessions)
    if ($sessionID) {
        $stmt = $conn->prepare(
            "SELECT r.IsCorrect, i.DiscriminationParam as a, i.DifficultyParam as b, i.GuessingParam as c
             FROM Responses r
             JOIN Items i ON r.ItemID = i.ItemID
             WHERE r.SessionID = ?"
        );
        $stmt->execute([$sessionID]);
    } else {
        // Get all responses from student
        $stmt = $conn->prepare(
            "SELECT r.IsCorrect, i.DiscriminationParam as a, i.DifficultyParam as b, i.GuessingParam as c
             FROM Responses r
             JOIN Items i ON r.ItemID = i.ItemID
             JOIN TestSessions ts ON r.SessionID = ts.SessionID
             WHERE ts.StudentID = ?
             ORDER BY r.Timestamp DESC
             LIMIT 20"
        );
        $stmt->execute([$studentID]);
    }
    
    $responses = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
    if (empty($responses)) {
        sendError("No responses found to calculate ability", 404);
    }
    
    // Format responses for IRT
    $irtResponses = array_map(function($r) {
        return [
            'isCorrect' => (bool)$r['IsCorrect'],
            'a' => (float)$r['a'],
            'b' => (float)$r['b'],
            'c' => (float)$r['c']
        ];
    }, $responses);
    
    // Get current ability as starting point
    $stmt = $conn->prepare("SELECT CurrentAbility FROM Students WHERE StudentID = ?");
    $stmt->execute([$studentID]);
    $currentAbility = $stmt->fetchColumn() ?? 0.0;
    
    // Estimate new ability
    $newTheta = $irt->estimateAbility($irtResponses, $currentAbility);
    
    // Calculate SEM (standard error of measurement)
    $sem = $irt->calculateSEM($newTheta, $irtResponses);
    
    // Classify ability
    $classification = $irt->classifyAbility($newTheta);
    
    // Update database
    $stmt = $conn->prepare("EXEC SP_UpdateStudentAbility @StudentID = ?, @NewTheta = ?");
    $stmt->execute([$studentID, $newTheta]);
    
    // Update session final theta if session provided
    if ($sessionID) {
        $stmt = $conn->prepare("UPDATE TestSessions SET FinalTheta = ?, IsCompleted = 1, EndTime = GETDATE() WHERE SessionID = ?");
        $stmt->execute([$newTheta, $sessionID]);
    }
    
    sendResponse([
        "status" => "success",
        "message" => "Ability updated successfully",
        "ability" => round($newTheta, 3),
        "classification" => $classification,
        "standardError" => round($sem, 3),
        "responsesAnalyzed" => count($responses)
    ]);
    
} catch (Exception $e) {
    sendError("Failed to update ability", 500, $e->getMessage());
}
?>