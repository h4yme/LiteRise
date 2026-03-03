<?php

/**
 * LiteRise Get Quiz Questions API
 * GET /api/get_quiz_questions.php?node_id=1&placement_level=3
 */

require_once __DIR__ . '/src/db.php';

$nodeID         = (int)($_GET['node_id'] ?? 0);
$placementLevel = (int)($_GET['placement_level'] ?? 1);

if ($nodeID <= 0) {
    sendError("node_id is required", 400);
}

try {
    // Get lesson info to determine grade level / category
    $lessonStmt = $conn->prepare(
        "SELECT GradeLevel, LessonType FROM Lessons WHERE LessonID = ? AND IsActive = 1"
    );
    $lessonStmt->execute([$nodeID]);
    $lesson = $lessonStmt->fetch(PDO::FETCH_ASSOC);

    if (!$lesson) {
        sendError("Lesson not found", 404);
    }

    $gradeLevel = (int)$lesson['GradeLevel'];

    // Fetch quiz questions from AssessmentItems for this grade level
    // Limit to 10 questions ordered by difficulty close to placement level mapping
    $targetDifficulty = ($placementLevel - 3) * 0.5; // Map level 1-5 to theta range
    $stmt = $conn->prepare(
        "SELECT TOP 10
            ItemID, QuestionText, OptionA, OptionB, OptionC, OptionD,
            DifficultyParam
         FROM AssessmentItems
         WHERE GradeLevel = ? AND IsActive = 1
         ORDER BY ABS(DifficultyParam - ?) ASC, NEWID()"
    );
    $stmt->execute([$gradeLevel, $targetDifficulty]);
    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (empty($items)) {
        // Fallback: get any active items
        $fallbackStmt = $conn->prepare(
            "SELECT TOP 10 ItemID, QuestionText, OptionA, OptionB, OptionC, OptionD, DifficultyParam
             FROM AssessmentItems WHERE IsActive = 1 ORDER BY NEWID()"
        );
        $fallbackStmt->execute();
        $items = $fallbackStmt->fetchAll(PDO::FETCH_ASSOC);
    }

    $questions = array_map(function($item) {
        return [
            'question_id'   => (int)$item['ItemID'],
            'question_text' => $item['QuestionText'],
            'option_a'      => $item['OptionA'] ?? '',
            'option_b'      => $item['OptionB'] ?? '',
            'option_c'      => $item['OptionC'] ?? '',
            'option_d'      => $item['OptionD'] ?? '',
        ];
    }, $items);

    sendResponse([
        'success' => true,
        'quiz'    => [
            'node_id'         => $nodeID,
            'total_questions' => count($questions),
            'questions'       => $questions,
        ],
    ]);

} catch (PDOException $e) {
    error_log("get_quiz_questions error: " . $e->getMessage());
    sendError("Failed to get quiz questions", 500, $e->getMessage());
}
?>
