<?php

/**
 * LiteRise Update Node Progress API
 * POST /api/update_node_progress.php
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "node_id": 5,
 *   "phase": "lesson_completed"  // lesson_completed | game_completed | quiz_completed
 * }
 */

require_once __DIR__ . '/src/db.php';

$data = getJsonInput();

$studentID = (int)($data['student_id'] ?? 0);
$nodeID    = (int)($data['node_id'] ?? 0);
$phase     = trim($data['phase'] ?? '');

$validPhases = ['lesson_completed', 'game_completed', 'quiz_completed'];

if ($studentID <= 0 || $nodeID <= 0 || !in_array($phase, $validPhases)) {
    sendError("student_id, node_id, and a valid phase are required", 400);
}

try {
    // Check if progress record exists
    $checkStmt = $conn->prepare(
        "SELECT ProgressID, CompletionStatus FROM StudentProgress
         WHERE StudentID = ? AND LessonID = ?"
    );
    $checkStmt->execute([$studentID, $nodeID]);
    $existing = $checkStmt->fetch(PDO::FETCH_ASSOC);

    if ($existing) {
        $conn->prepare(
            "UPDATE StudentProgress
             SET CompletionStatus = ?, LastAttemptDate = GETDATE()
             WHERE StudentID = ? AND LessonID = ?"
        )->execute([$phase, $studentID, $nodeID]);
    } else {
        $conn->prepare(
            "INSERT INTO StudentProgress (StudentID, LessonID, CompletionStatus, AttemptsCount, LastAttemptDate)
             VALUES (?, ?, ?, 1, GETDATE())"
        )->execute([$studentID, $nodeID, $phase]);
    }

    sendResponse([
        'success' => true,
        'message' => "Progress updated to '$phase'",
    ]);

} catch (PDOException $e) {
    error_log("update_node_progress error: " . $e->getMessage());
    sendError("Failed to update node progress", 500, $e->getMessage());
}
?>
