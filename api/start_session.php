<?php
/**
 * LiteRise Start Test Session API
 * POST /api/start_session.php
 *
 * Creates a new test session (PreAssessment, PostAssessment, Practice, Game)
 *
 * Request Body:
 * {
 *   "session_type": "PreAssessment"
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "session_id": 123,
 *   "session_type": "PreAssessment",
 *   "initial_theta": 0.0
 * }
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

$authUser = requireAuth();

$data = getJsonInput();
$sessionType = $data['session_type'] ?? 'Practice';

// Validate session type
$validTypes = ['PreAssessment', 'PostAssessment', 'Practice', 'Game'];
if (!in_array($sessionType, $validTypes)) {
    sendError("Invalid session_type. Must be: " . implode(', ', $validTypes), 400);
}

try {
    $studentID = $authUser['studentID'];

    // Get student's current ability for initial theta
    $stmt = $conn->prepare("SELECT CurrentAbility FROM Students WHERE StudentID = ?");
    $stmt->execute([$studentID]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);
    $initialTheta = (float)($student['CurrentAbility'] ?? 0.0);

    // For PreAssessment, check if one already exists
    if ($sessionType === 'PreAssessment') {
        $stmt = $conn->prepare(
            "SELECT SessionID FROM TestSessions
             WHERE StudentID = ? AND SessionType = 'PreAssessment' AND IsCompleted = 1"
        );
        $stmt->execute([$studentID]);
        if ($stmt->fetch()) {
            // Already completed a pre-assessment
            // Could return error or allow retake based on requirements
        }
    }

    // Create new session
    $stmt = $conn->prepare(
        "INSERT INTO TestSessions (StudentID, SessionType, InitialTheta, StartTime, IsCompleted)
         VALUES (?, ?, ?, GETDATE(), 0)"
    );
    $stmt->execute([$studentID, $sessionType, $initialTheta]);

    $sessionID = $conn->lastInsertId();

    // Log activity
    logActivity($studentID, 'SessionStart', "Started $sessionType session");

    sendResponse([
        'success' => true,
        'session_id' => (int)$sessionID,
        'session_type' => $sessionType,
        'initial_theta' => $initialTheta,
        'message' => "$sessionType session started"
    ], 201);

} catch (PDOException $e) {
    error_log("Start session error: " . $e->getMessage());
    sendError("Failed to start session", 500, $e->getMessage());
}
?>
