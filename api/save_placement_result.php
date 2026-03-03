<?php

/**
 * LiteRise Save Placement Result API
 * POST /api/save_placement_result.php
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "session_id": 10,
 *   "assessment_type": "PreAssessment",
 *   "final_theta": 0.75,
 *   "placement_level": 3,
 *   "level_name": "Proficient",
 *   "total_questions": 20,
 *   "correct_answers": 15,
 *   "accuracy_percentage": 75.0,
 *   "category_scores": {"Vocabulary":0.8,"Grammar":0.7},
 *   "category_theta":  {"Vocabulary":0.9,"Grammar":0.6},
 *   "time_spent_seconds": 600
 * }
 */

require_once __DIR__ . '/src/db.php';

$data = getJsonInput();

$studentID          = (int)($data['student_id'] ?? 0);
$sessionID          = (int)($data['session_id'] ?? 0);
$assessmentType     = trim($data['assessment_type'] ?? 'PreAssessment');
$finalTheta         = (float)($data['final_theta'] ?? 0);
$placementLevel     = (int)($data['placement_level'] ?? 1);
$levelName          = trim($data['level_name'] ?? 'Beginner');
$totalQuestions     = (int)($data['total_questions'] ?? 0);
$correctAnswers     = (int)($data['correct_answers'] ?? 0);
$accuracyPercentage = (float)($data['accuracy_percentage'] ?? 0);
$categoryScores     = $data['category_scores'] ?? [];
$categoryTheta      = $data['category_theta'] ?? [];
$timeSpent          = isset($data['time_spent_seconds']) ? (int)$data['time_spent_seconds'] : null;
$deviceInfo         = $data['device_info'] ?? null;
$appVersion         = $data['app_version'] ?? null;

if ($studentID <= 0) {
    sendError("student_id is required", 400);
}

// Extract named category scores (up to 4)
$cats   = array_values((array)$categoryScores);
$thetas = array_values((array)$categoryTheta);

try {
    $stmt = $conn->prepare(
        "EXEC SP_SavePlacementResult
            @StudentID            = :sid,
            @SessionID            = :sess,
            @AssessmentType       = :atype,
            @FinalTheta           = :theta,
            @PlacementLevel       = :level,
            @LevelName            = :lname,
            @TotalQuestions       = :total,
            @CorrectAnswers       = :correct,
            @AccuracyPercentage   = :acc,
            @Category1Score       = :c1s,
            @Category2Score       = :c2s,
            @Category3Score       = :c3s,
            @Category4Score       = :c4s,
            @Category1Theta       = :c1t,
            @Category2Theta       = :c2t,
            @Category3Theta       = :c3t,
            @Category4Theta       = :c4t,
            @TimeSpentSeconds     = :time,
            @DeviceInfo           = :device,
            @AppVersion           = :ver"
    );

    $stmt->bindValue(':sid',     $studentID,          PDO::PARAM_INT);
    $stmt->bindValue(':sess',    $sessionID,          PDO::PARAM_INT);
    $stmt->bindValue(':atype',   $assessmentType,     PDO::PARAM_STR);
    $stmt->bindValue(':theta',   $finalTheta);
    $stmt->bindValue(':level',   $placementLevel,     PDO::PARAM_INT);
    $stmt->bindValue(':lname',   $levelName,          PDO::PARAM_STR);
    $stmt->bindValue(':total',   $totalQuestions,     PDO::PARAM_INT);
    $stmt->bindValue(':correct', $correctAnswers,     PDO::PARAM_INT);
    $stmt->bindValue(':acc',     $accuracyPercentage);
    $stmt->bindValue(':c1s',     $cats[0] ?? null);
    $stmt->bindValue(':c2s',     $cats[1] ?? null);
    $stmt->bindValue(':c3s',     $cats[2] ?? null);
    $stmt->bindValue(':c4s',     $cats[3] ?? null);
    $stmt->bindValue(':c1t',     $thetas[0] ?? null);
    $stmt->bindValue(':c2t',     $thetas[1] ?? null);
    $stmt->bindValue(':c3t',     $thetas[2] ?? null);
    $stmt->bindValue(':c4t',     $thetas[3] ?? null);
    $stmt->bindValue(':time',    $timeSpent,          PDO::PARAM_INT);
    $stmt->bindValue(':device',  $deviceInfo,         PDO::PARAM_STR);
    $stmt->bindValue(':ver',     $appVersion,         PDO::PARAM_STR);
    $stmt->execute();

    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    // Update student pre/post assessment status
    $flag = ($assessmentType === 'PreAssessment') ? 'PreAssessmentCompleted' : 'PostAssessmentCompleted';
    $conn->prepare("UPDATE Students SET $flag = 1, AssessmentStatus = 'Completed', CurrentAbility = :theta WHERE StudentID = :sid")
         ->execute([':theta' => $finalTheta, ':sid' => $studentID]);

    sendResponse([
        'success' => true,
        'message' => 'Placement result saved',
        'result'  => [
            'ResultID'           => (int)($row['ResultID'] ?? 0),
            'StudentID'          => $studentID,
            'AssessmentType'     => $assessmentType,
            'PlacementLevel'     => $placementLevel,
            'LevelName'          => $levelName,
            'AccuracyPercentage' => $accuracyPercentage,
            'CompletedDate'      => date('Y-m-d H:i:s'),
        ],
    ]);

} catch (PDOException $e) {
    error_log("save_placement_result error: " . $e->getMessage());
    sendError("Failed to save placement result", 500, $e->getMessage());
}
?>
