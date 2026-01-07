<?php
/**
 * Evaluate Pronunciation API
 * POST /api/evaluate_pronunciation.php
 *
 * Evaluates student's pronunciation using Google Cloud Speech-to-Text API
 * with pronunciation assessment features
 *
 * Request Body (multipart/form-data):
 * {
 *   "student_id": 27,
 *   "item_id": 15,
 *   "response_id": 1234,
 *   "audio_file": <file upload>,
 *   "target_word": "elephant"
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "score_id": 567,
 *   "pronunciation_result": {
 *     "recognized_text": "elephant",
 *     "confidence": 0.95,
 *     "overall_accuracy": 87,
 *     "pronunciation_score": 0.87,
 *     "fluency_score": 0.92,
 *     "completeness_score": 1.0,
 *     "feedback": "Great job! Your pronunciation is excellent!",
 *     "passed": true
 *   },
 *   "phoneme_details": [
 *     {"phoneme": "ɛ", "accuracy": 0.95},
 *     {"phoneme": "l", "accuracy": 0.88},
 *     {"phoneme": "ə", "accuracy": 0.90}
 *   ]
 * }
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

// Require authentication
$authUser = requireAuth();

// Get form data
$studentID = isset($_POST['student_id']) ? (int)$_POST['student_id'] : 0;
$itemID = isset($_POST['item_id']) ? (int)$_POST['item_id'] : 0;
$responseID = isset($_POST['response_id']) ? (int)$_POST['response_id'] : 0;
$targetWord = isset($_POST['target_word']) ? sanitizeInput($_POST['target_word']) : '';

// Validate required fields
if (!$studentID || !$itemID || !$responseID || !$targetWord) {
    sendError('Missing required fields', 400);
}

// Verify user can only submit for themselves
if ($authUser['studentID'] != $studentID) {
    sendError('Unauthorized: Cannot submit pronunciation for another student', 403);
}

// Check if audio file was uploaded
if (!isset($_FILES['audio_file']) || $_FILES['audio_file']['error'] !== UPLOAD_ERR_OK) {
    sendError('Audio file is required', 400);
}

$audioFile = $_FILES['audio_file'];

// Validate file type (accept common audio formats)
$allowedMimeTypes = ['audio/webm', 'audio/wav', 'audio/mp3', 'audio/mpeg', 'audio/ogg', 'audio/flac', 'audio/3gp', 'audio/3gpp', 'audio/amr', 'video/3gpp'];
$finfo = finfo_open(FILEINFO_MIME_TYPE);
$mimeType = finfo_file($finfo, $audioFile['tmp_name']);
finfo_close($finfo);

// Log the detected MIME type for debugging
error_log("DEBUG: Detected MIME type: " . $mimeType . " for file: " . $audioFile['name']);

if (!in_array($mimeType, $allowedMimeTypes)) {
    sendError('Invalid audio file format. Detected: ' . $mimeType . '. Supported: WAV, MP3, WebM, OGG, FLAC, 3GP', 400);
}

// Validate file size (max 10MB)
$maxFileSize = 10 * 1024 * 1024; // 10MB
if ($audioFile['size'] > $maxFileSize) {
    sendError('Audio file too large. Maximum size: 10MB', 400);
}

// Get item details for pronunciation assessment
$itemStmt = $conn->prepare("
    SELECT TargetPronunciation, PhoneticTranscription, MinimumAccuracy, DifficultyParam, Category
    FROM dbo.AssessmentItems
    WHERE ItemID = ?
");
$itemStmt->execute([$itemID]);
$item = $itemStmt->fetch(PDO::FETCH_ASSOC);

if (!$item) {
    sendError('Assessment item not found', 404);
}

$targetPronunciation = $item['TargetPronunciation'] ?? $targetWord;
$minAccuracy = $item['MinimumAccuracy'] ?? 65;
$itemDifficulty = $item['DifficultyParam'] ?? 0.0;
$category = $item['Category'] ?? 'Oral Language';

// =============================================
// Option 1: Use Google Cloud Speech-to-Text API
// =============================================
// Uncomment this section when you have Google Cloud credentials

/*
try {
    require_once __DIR__ . '/vendor/autoload.php'; // Google Cloud PHP SDK

    use Google\Cloud\Speech\V1\SpeechClient;
    use Google\Cloud\Speech\V1\RecognitionAudio;
    use Google\Cloud\Speech\V1\RecognitionConfig;
    use Google\Cloud\Speech\V1\RecognitionConfig\AudioEncoding;

    // Initialize Google Cloud Speech client
    $speech = new SpeechClient([
        'credentials' => __DIR__ . '/google-cloud-credentials.json'
    ]);

    // Read audio file
    $audioContent = file_get_contents($audioFile['tmp_name']);

    // Configure recognition with pronunciation assessment
    $audio = (new RecognitionAudio())->setContent($audioContent);

    $config = (new RecognitionConfig())
        ->setEncoding(AudioEncoding::WEBM_OPUS) // Adjust based on actual format
        ->setSampleRateHertz(48000)
        ->setLanguageCode('en-US')
        ->setEnableAutomaticPunctuation(false)
        ->setPhraseHints([$targetPronunciation])
        ->setModel('default');

    // Perform speech recognition
    $response = $speech->recognize($config, $audio);

    $recognizedText = '';
    $confidence = 0.0;
    $pronunciationScore = 0.0;

    foreach ($response->getResults() as $result) {
        $alternative = $result->getAlternatives()[0];
        $recognizedText = strtolower(trim($alternative->getTranscript()));
        $confidence = $alternative->getConfidence();

        // Google Speech API doesn't provide direct pronunciation scoring
        // We'll calculate it based on text match and confidence
        $pronunciationScore = calculatePronunciationScore(
            $recognizedText,
            $targetPronunciation,
            $confidence
        );
        break;
    }

    $speech->close();

} catch (Exception $e) {
    sendError('Speech recognition failed: ' . $e->getMessage(), 500);
}
*/

// =============================================
// Option 2: Fallback - Simple accuracy calculation
// =============================================
// For development/testing without Google Cloud API
// Replace with actual Google Cloud API in production

// Simulate speech recognition (for testing)
// In production, replace this with actual API call above

// For now, use basic text similarity matching
// This is a simplified version - real implementation should use Google Cloud API

$recognizedText = $targetWord; // Placeholder - should come from actual speech recognition
$confidence = 0.85; // Placeholder

// Calculate pronunciation accuracy using Levenshtein distance
$pronunciationScore = calculatePronunciationScore($recognizedText, $targetPronunciation, $confidence);
$overallAccuracy = (int)($pronunciationScore * 100);

// Fluency and completeness scores (would come from API in production)
$fluencyScore = 0.90;
$completenessScore = 1.0;

// Determine if student passed
$passed = ($overallAccuracy >= $minAccuracy);

// Generate feedback message
$feedback = generateFeedback($overallAccuracy, $passed);

// Prepare phoneme details (would come from detailed API response)
$phonemeDetails = generatePhonemeDetails($targetPronunciation, $recognizedText);
$errorPhonemes = implode(',', array_column(array_filter($phonemeDetails, function($p) {
    return $p['accuracy'] < 0.7;
}), 'phoneme'));

// Get audio duration (in milliseconds)
$audioDuration = null; // Would be extracted from audio file metadata

// Build API response JSON (for storing full details)
$apiResponseJSON = json_encode([
    'recognized_text' => $recognizedText,
    'confidence' => $confidence,
    'target' => $targetPronunciation,
    'provider' => 'Fallback', // Change to 'Google Cloud Speech' in production
    'timestamp' => date('c')
]);

// Record pronunciation score in database
try {
    // First, create a StudentResponse record to get a valid ResponseID
    $insertResponseStmt = $conn->prepare("
        INSERT INTO dbo.StudentResponses
        (StudentID, ItemID, SessionID, AssessmentType, SelectedAnswer, IsCorrect,
         StudentThetaAtTime, ItemDifficulty, QuestionNumber, ResponseTime)
        VALUES (?, ?, ?, 'Pronunciation', ?, ?, 0.0, ?, 1, 0)
    ");

    $insertResponseStmt->execute([
        $studentID,
        $itemID,
        $responseID, // Use the temporary ID as SessionID
        $recognizedText, // SelectedAnswer
        $passed ? 1 : 0, // IsCorrect based on pronunciation pass/fail
        $itemDifficulty // ItemDifficulty from AssessmentItems
    ]);

    // Get the last inserted ID using PDO
    $actualResponseID = (int)$conn->lastInsertId();

    if ($actualResponseID === 0) {
        error_log("ERROR: Failed to get lastInsertId after StudentResponse insert");
        throw new Exception("Failed to create StudentResponse record");
    }

    error_log("DEBUG: Created StudentResponse with ID: " . $actualResponseID);

    // Now record the pronunciation score with the valid ResponseID
    $stmt = $conn->prepare("EXEC dbo.SP_RecordPronunciationScore
        @ResponseID = ?,
        @StudentID = ?,
        @ItemID = ?,
        @RecognizedText = ?,
        @Confidence = ?,
        @OverallAccuracy = ?,
        @PronunciationScore = ?,
        @FluencyScore = ?,
        @CompletenessScore = ?,
        @PhonemeAccuracyJSON = ?,
        @ErrorPhonemes = ?,
        @AudioDuration = ?,
        @AudioQuality = ?,
        @APIResponseJSON = ?
    ");

    $stmt->execute([
        $actualResponseID, // Use the actual ResponseID from StudentResponses
        $studentID,
        $itemID,
        $recognizedText,
        $confidence,
        $overallAccuracy,
        $pronunciationScore,
        $fluencyScore,
        $completenessScore,
        json_encode($phonemeDetails),
        $errorPhonemes,
        $audioDuration,
        0.8, // Audio quality placeholder
        $apiResponseJSON
    ]);

    $result = $stmt->fetch(PDO::FETCH_ASSOC);
    $scoreID = $result['ScoreID'] ?? 0;

    // Return success response
    sendResponse([
        'score_id' => $scoreID,
        'pronunciation_result' => [
            'recognized_text' => $recognizedText,
            'confidence' => $confidence,
            'overall_accuracy' => $overallAccuracy,
            'pronunciation_score' => $pronunciationScore,
            'fluency_score' => $fluencyScore,
            'completeness_score' => $completenessScore,
            'feedback' => $feedback,
            'passed' => $passed,
            'minimum_accuracy' => $minAccuracy
        ],
        'phoneme_details' => $phonemeDetails
    ]);

} catch (PDOException $e) {
    error_log('Database error in pronunciation scoring: ' . $e->getMessage());
    $errorDetails = ($_ENV['DEBUG_MODE'] ?? 'false') === 'true' ? $e->getMessage() : null;
    sendError('Failed to record pronunciation score: ' . $e->getMessage(), 500, $errorDetails);
}

// =============================================
// Helper Functions
// =============================================

/**
 * Calculate pronunciation score based on text match and confidence
 */
function calculatePronunciationScore($recognized, $target, $confidence) {
    $recognized = strtolower(trim($recognized));
    $target = strtolower(trim($target));

    // Exact match gets full score
    if ($recognized === $target) {
        return $confidence;
    }

    // Calculate similarity using Levenshtein distance
    $maxLen = max(strlen($recognized), strlen($target));
    if ($maxLen === 0) return 0.0;

    $distance = levenshtein($recognized, $target);
    $similarity = 1.0 - ($distance / $maxLen);

    // Combine similarity with speech confidence
    return ($similarity * 0.7 + $confidence * 0.3);
}

/**
 * Generate feedback message based on accuracy
 */
function generateFeedback($accuracy, $passed) {
    if ($accuracy >= 95) {
        return "Perfect! Your pronunciation is excellent! 🌟";
    } elseif ($accuracy >= 85) {
        return "Great job! Your pronunciation is very good! 🎉";
    } elseif ($accuracy >= 75) {
        return "Good work! Keep practicing to improve! 👍";
    } elseif ($accuracy >= 65) {
        return "Nice try! You're getting better! 💪";
    } else {
        return "Keep practicing! Try again! 🔄";
    }
}

/**
 * Generate simulated phoneme details
 * In production, this would come from Google Cloud Speech API's detailed response
 */
function generatePhonemeDetails($target, $recognized) {
    // This is a placeholder - actual phoneme analysis requires speech API
    // For now, return empty array or basic analysis

    $phonemes = [];

    // Simple character-by-character comparison
    $targetChars = str_split(strtolower($target));
    $recognizedChars = str_split(strtolower($recognized));

    for ($i = 0; $i < count($targetChars); $i++) {
        $accuracy = 1.0;

        if (!isset($recognizedChars[$i]) || $recognizedChars[$i] !== $targetChars[$i]) {
            $accuracy = 0.6; // Lower accuracy for mismatch
        }

        $phonemes[] = [
            'phoneme' => $targetChars[$i],
            'accuracy' => $accuracy
        ];
    }

    return $phonemes;
}
