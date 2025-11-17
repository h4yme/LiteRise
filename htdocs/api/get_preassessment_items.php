<?php
/**
 * LiteRise - Get Pre-Assessment Items API
 * Endpoint: POST /api/get_preassessment_items.php
 * Returns 20 questions for placement test with proper formatting for Android
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Call stored procedure SP_GetPreAssessmentItems
    $sql = "EXEC SP_GetPreAssessmentItems";
    $stmt = $conn->prepare($sql);
    $stmt->execute();

    $items = $stmt->fetchAll();

    if (!$items) {
        http_response_code(404);
        echo json_encode(['error' => 'No items found']);
        exit();
    }

    // Transform database structure to Android model format
    $questions = [];

    foreach ($items as $item) {
        // Parse AnswerChoices JSON if it exists
        $choices = [];
        if (!empty($item['AnswerChoices'])) {
            $choicesJson = json_decode($item['AnswerChoices'], true);
            if (is_array($choicesJson)) {
                $choices = $choicesJson;
            }
        }

        // Determine passage text (for reading comprehension items)
        $passageText = null;
        if ($item['ItemType'] === 'Reading') {
            // First part of ItemText could be passage, separated by delimiter
            $parts = explode('|||', $item['ItemText']);
            if (count($parts) > 1) {
                $passageText = trim($parts[0]);
                $questionText = trim($parts[1]);
            } else {
                $questionText = $item['ItemText'];
            }
        } else {
            $questionText = $item['ItemText'];
        }

        // Build question object matching Android model
        $question = [
            'ItemID' => (int)$item['ItemID'],
            'PassageText' => $passageText,
            'QuestionText' => $questionText,
            'OptionA' => isset($choices[0]) ? $choices[0] : '',
            'OptionB' => isset($choices[1]) ? $choices[1] : '',
            'OptionC' => isset($choices[2]) ? $choices[2] : '',
            'OptionD' => isset($choices[3]) ? $choices[3] : '',
            'CorrectOption' => strtoupper($item['CorrectAnswer'] ?? ''),
            'Difficulty' => (float)$item['DifficultyParam'],
            'Discrimination' => (float)$item['DiscriminationParam'],
            'Guessing' => (float)($item['GuessingParam'] ?? 0.25),
            'ItemType' => $item['ItemType'],
            'DifficultyLevel' => $item['DifficultyLevel']
        ];

        // Special handling for Syntax items (Sentence Scramble)
        if ($item['ItemType'] === 'Syntax') {
            // ItemText contains scrambled words
            $question['QuestionText'] = "Arrange these words to form a correct sentence:";
            $question['ScrambledWords'] = $item['ItemText'];
            $question['CorrectSentence'] = $item['CorrectAnswer'];
        }

        // Special handling for Pronunciation items
        if ($item['ItemType'] === 'Pronunciation') {
            $question['QuestionText'] = "Read this word aloud:";
            $question['Word'] = $item['ItemText'];
            $question['ExpectedPronunciation'] = $item['CorrectAnswer'];
        }

        $questions[] = $question;
    }

    http_response_code(200);
    echo json_encode($questions);

} catch (Exception $e) {
    error_log("Get assessment items error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve assessment items']);
}
