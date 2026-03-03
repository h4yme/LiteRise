<?php

/**
 * LiteRise Submit Quiz API
 * POST /api/submit_quiz.php
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "node_id": 5,
 *   "placement_level": 3,
 *   "answers": {"101": 1, "102": 3, "103": 2}
 *   // key = question_id (ItemID), value = selected option index (1=A,2=B,3=C,4=D)
 * }
 */

require_once __DIR__ . '/src/db.php';

$data = getJsonInput();

$studentID      = (int)($data['student_id'] ?? 0);
$nodeID         = (int)($data['node_id'] ?? 0);
$placementLevel = (int)($data['placement_level'] ?? 1);
$answers        = $data['answers'] ?? [];

if ($studentID <= 0 || $nodeID <= 0 || empty($answers)) {
    sendError("student_id, node_id, and answers are required", 400);
}

// Map option index to letter
$optionMap = [1 => 'A', 2 => 'B', 3 => 'C', 4 => 'D'];

try {
    // Get correct answers for submitted item IDs
    $itemIDs = array_keys($answers);
    $placeholders = implode(',', array_fill(0, count($itemIDs), '?'));

    $stmt = $conn->prepare(
        "SELECT ItemID, CorrectAnswer FROM AssessmentItems WHERE ItemID IN ($placeholders)"
    );
    $stmt->execute($itemIDs);
    $correctAnswers = [];
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        $correctAnswers[(int)$row['ItemID']] = strtoupper(trim($row['CorrectAnswer']));
    }

    // Grade the answers
    $totalQuestions = count($answers);
    $correctCount   = 0;

    foreach ($answers as $itemID => $selectedIndex) {
        $itemID       = (int)$itemID;
        $selectedLetter = strtoupper($optionMap[$selectedIndex] ?? '');
        $correctLetter  = $correctAnswers[$itemID] ?? '';

        if ($selectedLetter === $correctLetter) {
            $correctCount++;
        }
    }

    $scorePercent = $totalQuestions > 0
        ? round($correctCount / $totalQuestions * 100, 2)
        : 0;

    // Adaptive decision
    $adaptiveDecision = 'needs_review';
    if ($scorePercent >= 90) {
        $adaptiveDecision = 'advanced';
    } elseif ($scorePercent >= 70) {
        $adaptiveDecision = 'pass';
    }

    // XP award
    $xpAwarded = (int)($scorePercent * 0.5); // e.g. 100% = 50 XP

    // Update StudentProgress for this node (lesson)
    $existsStmt = $conn->prepare(
        "SELECT ProgressID FROM StudentProgress WHERE StudentID = ? AND LessonID = ?"
    );
    $existsStmt->execute([$studentID, $nodeID]);
    $existing = $existsStmt->fetch(PDO::FETCH_ASSOC);

    if ($existing) {
        $conn->prepare(
            "UPDATE StudentProgress
             SET CompletionStatus = 'quiz_completed', Score = ?, LastAttemptDate = GETDATE(), CompletionDate = GETDATE()
             WHERE StudentID = ? AND LessonID = ?"
        )->execute([$scorePercent, $studentID, $nodeID]);
    } else {
        $conn->prepare(
            "INSERT INTO StudentProgress (StudentID, LessonID, CompletionStatus, Score, AttemptsCount, LastAttemptDate, CompletionDate)
             VALUES (?, ?, 'quiz_completed', ?, 1, GETDATE(), GETDATE())"
        )->execute([$studentID, $nodeID, $scorePercent]);
    }

    // Award XP to student
    if ($xpAwarded > 0) {
        $conn->prepare(
            "UPDATE Students SET TotalXP = ISNULL(TotalXP, 0) + ? WHERE StudentID = ?"
        )->execute([$xpAwarded, $studentID]);
    }

    // Check for unlocked nodes (next lesson in sequence)
    $nextStmt = $conn->prepare(
        "SELECT TOP 1 LessonID, LessonTitle FROM Lessons
         WHERE LessonID > ? AND IsActive = 1
         ORDER BY LessonID ASC"
    );
    $nextStmt->execute([$nodeID]);
    $nextLesson = $nextStmt->fetch(PDO::FETCH_ASSOC);

    $unlockedNodes = [];
    if ($nextLesson && $adaptiveDecision !== 'needs_review') {
        $unlockedNodes[] = [
            'type'      => 'lesson',
            'node_id'   => (int)$nextLesson['LessonID'],
            'title'     => $nextLesson['LessonTitle'],
            'mandatory' => true,
        ];
    }

    sendResponse([
        'success' => true,
        'result'  => [
            'score_percent'     => $scorePercent,
            'correct_count'     => $correctCount,
            'total_questions'   => $totalQuestions,
            'adaptive_decision' => $adaptiveDecision,
            'xp_awarded'        => $xpAwarded,
            'unlocked_nodes'    => $unlockedNodes,
        ],
    ]);

} catch (PDOException $e) {
    error_log("submit_quiz error: " . $e->getMessage());
    sendError("Failed to submit quiz", 500, $e->getMessage());
}
?>
