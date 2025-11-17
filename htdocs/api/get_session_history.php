<?php
/**
 * LiteRise - Get Session History API
 * Endpoint: GET /api/get_session_history.php?StudentID=1&Limit=20
 * Returns all past test sessions for a student
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Get parameters
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $studentId = isset($_GET['StudentID']) ? (int)$_GET['StudentID'] : 0;
    $limit = isset($_GET['Limit']) ? (int)$_GET['Limit'] : 20;
} else {
    $input = json_decode(file_get_contents('php://input'), true);
    $studentId = isset($input['StudentID']) ? (int)$input['StudentID'] : 0;
    $limit = isset($input['Limit']) ? (int)$input['Limit'] : 20;
}

if ($studentId <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'Valid StudentID required']);
    exit();
}

$limit = min($limit, 100); // Cap at 100

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Get session history with statistics
    $sql = "SELECT TOP (:limit)
                SessionID, SessionType, StartTime, EndTime,
                InitialTheta, FinalTheta,
                TotalQuestions, CorrectAnswers, AccuracyPercentage,
                IsCompleted
            FROM TestSessions
            WHERE StudentID = :studentId
            ORDER BY StartTime DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);
    $stmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $stmt->execute();
    $sessions = $stmt->fetchAll();

    $response = [];
    foreach ($sessions as $session) {
        $thetaChange = (float)$session['FinalTheta'] - (float)$session['InitialTheta'];

        $response[] = [
            'SessionID' => (int)$session['SessionID'],
            'SessionType' => $session['SessionType'],
            'StartTime' => $session['StartTime'],
            'EndTime' => $session['EndTime'],
            'InitialTheta' => (float)$session['InitialTheta'],
            'FinalTheta' => (float)$session['FinalTheta'],
            'ThetaChange' => round($thetaChange, 4),
            'TotalQuestions' => (int)$session['TotalQuestions'],
            'CorrectAnswers' => (int)$session['CorrectAnswers'],
            'Accuracy' => (float)$session['AccuracyPercentage'],
            'IsCompleted' => (bool)$session['IsCompleted']
        ];
    }

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'StudentID' => $studentId,
        'count' => count($response),
        'sessions' => $response
    ]);

} catch (Exception $e) {
    error_log("Get session history error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve session history']);
}
