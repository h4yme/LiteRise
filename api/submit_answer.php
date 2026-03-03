<?php

/**
 * LiteRise Submit Answer API
 * POST /api/submit_answer.php
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "item_id": 5,
 *   "session_id": 10,
 *   "assessment_type": "PreAssessment",
 *   "selected_answer": "A",
 *   "is_correct": true,
 *   "student_theta": 0.5,
 *   "response_time": 12,
 *   "question_number": 3,
 *   "device_info": "Android 12",
 *   "interaction_data": "{}"
 * }
 */

require_once __DIR__ . '/src/db.php';

$data = getJsonInput();

$studentID      = (int)($data['student_id'] ?? 0);
$itemID         = (int)($data['item_id'] ?? 0);
$sessionID      = (int)($data['session_id'] ?? 0);
$assessmentType = trim($data['assessment_type'] ?? 'PreAssessment');
$selectedAnswer = trim($data['selected_answer'] ?? '');
$isCorrect      = (bool)($data['is_correct'] ?? false);
$studentTheta   = (float)($data['student_theta'] ?? 0.0);
$responseTime   = isset($data['response_time']) ? (int)$data['response_time'] : null;
$questionNumber = (int)($data['question_number'] ?? 1);
$deviceInfo     = $data['device_info'] ?? null;
$interactionData= $data['interaction_data'] ?? null;

if ($studentID <= 0 || $itemID <= 0 || $sessionID <= 0) {
    sendError("student_id, item_id, and session_id are required", 400);
}

try {
    $stmt = $conn->prepare(
        "EXEC SP_RecordStudentResponse
            @StudentID           = :sid,
            @ItemID              = :iid,
            @SessionID           = :sess,
            @AssessmentType      = :atype,
            @SelectedAnswer      = :ans,
            @IsCorrect           = :correct,
            @StudentThetaAtTime  = :theta,
            @ResponseTime        = :rtime,
            @QuestionNumber      = :qnum,
            @DeviceInfo          = :device,
            @InteractionData     = :idata"
    );
    $stmt->bindValue(':sid',     $studentID,       PDO::PARAM_INT);
    $stmt->bindValue(':iid',     $itemID,          PDO::PARAM_INT);
    $stmt->bindValue(':sess',    $sessionID,       PDO::PARAM_INT);
    $stmt->bindValue(':atype',   $assessmentType,  PDO::PARAM_STR);
    $stmt->bindValue(':ans',     $selectedAnswer,  PDO::PARAM_STR);
    $stmt->bindValue(':correct', $isCorrect ? 1 : 0, PDO::PARAM_INT);
    $stmt->bindValue(':theta',   $studentTheta);
    $stmt->bindValue(':rtime',   $responseTime,    PDO::PARAM_INT);
    $stmt->bindValue(':qnum',    $questionNumber,  PDO::PARAM_INT);
    $stmt->bindValue(':device',  $deviceInfo,      PDO::PARAM_STR);
    $stmt->bindValue(':idata',   $interactionData, PDO::PARAM_STR);
    $stmt->execute();

    $row        = $stmt->fetch(PDO::FETCH_ASSOC);
    $responseID = (int)($row['ResponseID'] ?? 0);

    // Get item parameters for feedback
    $itemStmt = $conn->prepare(
        "SELECT DifficultyParam, DiscriminationParam, GuessingParam FROM AssessmentItems WHERE ItemID = ?"
    );
    $itemStmt->execute([$itemID]);
    $item = $itemStmt->fetch(PDO::FETCH_ASSOC);

    $expectedProb = 0.25;
    if ($item) {
        $b = (float)$item['DifficultyParam'];
        $a = (float)$item['DiscriminationParam'];
        $c = (float)($item['GuessingParam'] ?? 0.25);
        $expectedProb = $c + (1 - $c) / (1 + exp(-1.7 * $a * ($studentTheta - $b)));
    }

    sendResponse([
        'success'     => true,
        'response_id' => $responseID,
        'is_correct'  => $isCorrect,
        'feedback'    => [
            'message'               => $isCorrect ? 'Correct!' : 'Keep going!',
            'expected_probability'  => round($expectedProb, 4),
            'new_theta_estimate'    => $studentTheta,
        ],
    ]);

} catch (PDOException $e) {
    error_log("submit_answer error: " . $e->getMessage());
    sendError("Failed to submit answer", 500, $e->getMessage());
}
?>
