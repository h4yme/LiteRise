<?php

/**
 * LiteRise Evaluate Pronunciation API
 * POST /api/evaluate_pronunciation.php  (multipart/form-data)
 *
 * Form fields:
 *   student_id   - int
 *   item_id      - int
 *   response_id  - int
 *   session_id   - int
 *   target_word  - string
 *   audio_file   - binary file
 */

require_once __DIR__ . '/src/db.php';

$studentID   = (int)($_POST['student_id'] ?? 0);
$itemID      = (int)($_POST['item_id'] ?? 0);
$responseID  = (int)($_POST['response_id'] ?? 0);
$sessionID   = (int)($_POST['session_id'] ?? 0);
$targetWord  = trim($_POST['target_word'] ?? '');

if ($studentID <= 0 || empty($targetWord)) {
    sendError("student_id and target_word are required", 400);
}

// Check that an audio file was uploaded
if (!isset($_FILES['audio_file']) || $_FILES['audio_file']['error'] !== UPLOAD_ERR_OK) {
    sendError("Audio file is required", 400);
}

$audioFile = $_FILES['audio_file'];
$tmpPath   = $audioFile['tmp_name'];

// Placeholder: real implementation would call a speech recognition service.
// Here we use a simple heuristic to always return a reasonable score for testing.
// Replace this block with your STT API call (Google Speech-to-Text, Azure, etc.)
$recognizedText = $targetWord; // Simulated recognition
$accuracyScore  = 0.85;        // 85% accuracy (placeholder)
$isCorrect      = $accuracyScore >= 0.7;

try {
    // Record score via stored procedure
    $stmt = $conn->prepare(
        "EXEC SP_RecordPronunciationScore
            @ResponseID         = :rid,
            @StudentID          = :sid,
            @ItemID             = :iid,
            @RecognizedText     = :rtext,
            @Confidence         = :conf,
            @OverallAccuracy    = :acc,
            @PronunciationScore = :pscore"
    );
    $stmt->bindValue(':rid',    $responseID,      PDO::PARAM_INT);
    $stmt->bindValue(':sid',    $studentID,       PDO::PARAM_INT);
    $stmt->bindValue(':iid',    $itemID,          PDO::PARAM_INT);
    $stmt->bindValue(':rtext',  $recognizedText,  PDO::PARAM_STR);
    $stmt->bindValue(':conf',   $accuracyScore);
    $stmt->bindValue(':acc',    (int)($accuracyScore * 100), PDO::PARAM_INT);
    $stmt->bindValue(':pscore', $accuracyScore);
    $stmt->execute();

    $row = $stmt->fetch(PDO::FETCH_ASSOC);

    // Build JSON response (matches Android PronunciationResponse model)
    $responseBody = json_encode([
        'success'         => true,
        'recognized_text' => $recognizedText,
        'target_word'     => $targetWord,
        'accuracy_score'  => $accuracyScore,
        'is_correct'      => $isCorrect,
        'feedback'        => $isCorrect
            ? 'Great pronunciation!'
            : 'Keep practicing! Try again.',
        'score_id'        => (int)($row['ScoreID'] ?? 0),
    ]);

    header('Content-Type: application/json');
    echo $responseBody;
    exit;

} catch (PDOException $e) {
    error_log("evaluate_pronunciation error: " . $e->getMessage());
    sendError("Failed to evaluate pronunciation", 500, $e->getMessage());
}
?>
