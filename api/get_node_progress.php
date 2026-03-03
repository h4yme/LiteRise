<?php

/**
 * LiteRise Get Node Progress API
 * GET /api/get_node_progress.php?student_id=1&node_id=5
 */

require_once __DIR__ . '/src/db.php';

$studentID = (int)($_GET['student_id'] ?? 0);
$nodeID    = (int)($_GET['node_id'] ?? 0);

if ($studentID <= 0 || $nodeID <= 0) {
    sendError("student_id and node_id are required", 400);
}

try {
    $stmt = $conn->prepare(
        "SELECT CompletionStatus, Score, AttemptsCount, LastAttemptDate, CompletionDate
         FROM StudentProgress
         WHERE StudentID = ? AND LessonID = ?"
    );
    $stmt->execute([$studentID, $nodeID]);
    $progress = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$progress) {
        // No progress yet — return default
        sendResponse([
            'success'  => true,
            'progress' => [
                'lesson_completed'  => false,
                'game_completed'    => false,
                'quiz_completed'    => false,
                'quiz_score'        => 0.0,
                'adaptive_decision' => 'not_started',
                'completed_at'      => null,
            ],
        ]);
    }

    $status         = $progress['CompletionStatus'] ?? 'not_started';
    $score          = (float)($progress['Score'] ?? 0);

    $lessonCompleted = in_array($status, ['lesson_completed', 'game_completed', 'quiz_completed']);
    $gameCompleted   = in_array($status, ['game_completed', 'quiz_completed']);
    $quizCompleted   = $status === 'quiz_completed';

    $adaptiveDecision = 'not_started';
    if ($quizCompleted) {
        if ($score >= 90) {
            $adaptiveDecision = 'advanced';
        } elseif ($score >= 70) {
            $adaptiveDecision = 'pass';
        } else {
            $adaptiveDecision = 'needs_review';
        }
    } elseif ($lessonCompleted || $gameCompleted) {
        $adaptiveDecision = 'in_progress';
    }

    sendResponse([
        'success'  => true,
        'progress' => [
            'lesson_completed'  => $lessonCompleted,
            'game_completed'    => $gameCompleted,
            'quiz_completed'    => $quizCompleted,
            'quiz_score'        => $score,
            'adaptive_decision' => $adaptiveDecision,
            'completed_at'      => $progress['CompletionDate'] ?? null,
        ],
    ]);

} catch (PDOException $e) {
    error_log("get_node_progress error: " . $e->getMessage());
    sendError("Failed to get node progress", 500, $e->getMessage());
}
?>
