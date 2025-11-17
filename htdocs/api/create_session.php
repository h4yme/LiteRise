<?php
/**
 * LiteRise - Create Test Session API
 * Endpoint: POST /api/create_session.php
 * Calls: SP_CreateTestSession
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

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

if (!isset($input['StudentID']) || !isset($input['SessionType'])) {
    http_response_code(400);
    echo json_encode(['error' => 'StudentID and SessionType required']);
    exit();
}

$studentId = (int)$input['StudentID'];
$sessionType = trim($input['SessionType']);

// Validate session type
$validTypes = ['PreAssessment', 'Lesson', 'PostAssessment', 'Game'];
if (!in_array($sessionType, $validTypes)) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid session type']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Call stored procedure SP_CreateTestSession
    $sql = "EXEC SP_CreateTestSession @StudentID = :studentId, @Type = :type";
    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $stmt->bindParam(':type', $sessionType, PDO::PARAM_STR);
    $stmt->execute();

    $session = $stmt->fetch();

    if (!$session) {
        throw new Exception('Failed to create session');
    }

    $response = [
        'SessionID' => (int)$session['SessionID'],
        'StudentID' => (int)$session['StudentID'],
        'SessionType' => $session['SessionType'],
        'InitialTheta' => (float)$session['InitialTheta'],
        'StartTime' => $session['StartTime']
    ];

    http_response_code(201);
    echo json_encode($response);

} catch (Exception $e) {
    error_log("Create session error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to create session']);
}
