<?php

/**
 * LiteRise Get Next Adaptive Question API
 * POST /api/get_next_question.php
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "session_id": 10,
 *   "current_theta": 0.5,
 *   "assessment_type": "PreAssessment",
 *   "category": "Vocabulary"  // optional
 * }
 */

require_once __DIR__ . '/src/db.php';

$data = getJsonInput();

$studentID      = (int)($data['student_id'] ?? 0);
$sessionID      = (int)($data['session_id'] ?? 0);
$currentTheta   = (float)($data['current_theta'] ?? 0.0);
$assessmentType = trim($data['assessment_type'] ?? 'PreAssessment');
$category       = $data['category'] ?? null;

if ($studentID <= 0 || $sessionID <= 0) {
    sendError("student_id and session_id are required", 400);
}

try {
    $stmt = $conn->prepare(
        "EXEC SP_GetNextAdaptiveQuestion
            @StudentID       = :sid,
            @SessionID       = :sess,
            @CurrentTheta    = :theta,
            @AssessmentType  = :atype,
            @CategoryFilter  = :cat"
    );
    $stmt->bindValue(':sid',   $studentID,      PDO::PARAM_INT);
    $stmt->bindValue(':sess',  $sessionID,      PDO::PARAM_INT);
    $stmt->bindValue(':theta', $currentTheta);
    $stmt->bindValue(':atype', $assessmentType, PDO::PARAM_STR);
    $stmt->bindValue(':cat',   $category,       PDO::PARAM_STR);
    $stmt->execute();

    $item = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$item) {
        sendResponse([
            'success' => false,
            'message' => 'No more questions available',
            'question' => null,
        ]);
    }

    // Count questions answered in this session
    $countStmt = $conn->prepare(
        "SELECT COUNT(*) AS answered,
                SUM(CASE WHEN IsCorrect = 1 THEN 1 ELSE 0 END) AS correct
         FROM StudentResponses WHERE StudentID = ? AND SessionID = ?"
    );
    $countStmt->execute([$studentID, $sessionID]);
    $progress = $countStmt->fetch(PDO::FETCH_ASSOC);

    $answered = (int)($progress['answered'] ?? 0);
    $correct  = (int)($progress['correct'] ?? 0);
    $accuracy = $answered > 0 ? round($correct / $answered * 100, 2) : 0;

    sendResponse([
        'success'  => true,
        'question' => [
            'item_id'          => (int)$item['ItemID'],
            'category'         => $item['Category'] ?? '',
            'subcategory'      => $item['Subcategory'] ?? '',
            'skill_area'       => $item['SkillArea'] ?? '',
            'question_text'    => $item['QuestionText'] ?? '',
            'question_type'    => $item['QuestionType'] ?? 'MultipleChoice',
            'reading_passage'  => $item['ReadingPassage'] ?? null,
            'option_a'         => $item['OptionA'] ?? '',
            'option_b'         => $item['OptionB'] ?? '',
            'option_c'         => $item['OptionC'] ?? '',
            'option_d'         => $item['OptionD'] ?? '',
            'difficulty'       => (float)($item['DifficultyParam'] ?? 0),
            'discrimination'   => (float)($item['DiscriminationParam'] ?? 1),
            'estimated_time'   => (int)($item['EstimatedTime'] ?? 30),
        ],
        'progress' => [
            'questions_answered' => $answered,
            'accuracy'           => $accuracy,
            'current_theta'      => $currentTheta,
        ],
    ]);

} catch (PDOException $e) {
    error_log("get_next_question error: " . $e->getMessage());
    sendError("Failed to get next question", 500, $e->getMessage());
}
?>
