<?php
/**
 * LiteRise Get Pre-Assessment Items API
 * POST /api/get_preassessment_items.php
 *
 * Request Body:
 * {
 *   "student_id": 1  // Optional: for adaptive selection
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "count": 20,
 *   "items": [
 *     {
 *       "ItemID": 1,
 *       "ItemText": "Choose the correct spelling:",
 *       "QuestionText": "Choose the correct spelling:",
 *       "PassageText": "",
 *       "ItemType": "Spelling",
 *       "DifficultyLevel": "Easy",
 *       "DifficultyParam": -0.5,
 *       "DiscriminationParam": 1.3,
 *       "GuessingParam": 0.25,
 *       "AnswerChoices": ["receive", "recieve", "recive"],
 *       "OptionA": "receive",
 *       "OptionB": "recieve",
 *       "OptionC": "recive",
 *       "OptionD": "",
 *       "CorrectOption": "A"
 *     }
 *   ]
 * }
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

// Require authentication
$authUser = requireAuth();

try {
    // Call stored procedure to get pre-assessment items
    $stmt = $conn->prepare("EXEC SP_GetPreAssessmentItems");
    $stmt->execute();

    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (empty($items)) {
        sendError("No assessment items available", 404);
    }

    // Format items to match Android app expectations
    $formattedItems = array_map(function($item) {
        // Parse AnswerChoices JSON if it exists
        $answerChoices = [];
        if (!empty($item['AnswerChoices'])) {
            $decoded = json_decode($item['AnswerChoices'], true);
            $answerChoices = $decoded ?? [];
        }

        // Map answer choices to options (Android expects OptionA, OptionB, etc.)
        $optionA = $answerChoices[0] ?? '';
        $optionB = $answerChoices[1] ?? '';
        $optionC = $answerChoices[2] ?? '';
        $optionD = $answerChoices[3] ?? '';

        // Determine correct option letter based on CorrectAnswer
        $correctOption = '';
        if (!empty($item['CorrectAnswer'])) {
            $correctAnswer = trim($item['CorrectAnswer']);
            if ($correctAnswer === $optionA) $correctOption = 'A';
            elseif ($correctAnswer === $optionB) $correctOption = 'B';
            elseif ($correctAnswer === $optionC) $correctOption = 'C';
            elseif ($correctAnswer === $optionD) $correctOption = 'D';
        }

        return [
            'ItemID' => (int)$item['ItemID'],
            'ItemText' => $item['ItemText'] ?? '',
            'QuestionText' => $item['ItemText'] ?? '', // Alias for Android
            'PassageText' => '', // Not used in current schema
            'ItemType' => $item['ItemType'] ?? '',
            'DifficultyLevel' => $item['DifficultyLevel'] ?? '',
            'Difficulty' => (float)($item['DifficultyParam'] ?? 0), // Alias
            'DifficultyParam' => (float)($item['DifficultyParam'] ?? 0),
            'Discrimination' => (float)($item['DiscriminationParam'] ?? 1.0), // Alias
            'DiscriminationParam' => (float)($item['DiscriminationParam'] ?? 1.0),
            'GuessingParam' => (float)($item['GuessingParam'] ?? 0.25),
            'AnswerChoices' => $answerChoices,
            'OptionA' => $optionA,
            'OptionB' => $optionB,
            'OptionC' => $optionC,
            'OptionD' => $optionD,
            'CorrectAnswer' => $item['CorrectAnswer'] ?? '',
            'CorrectOption' => $correctOption,
            'ImageURL' => $item['ImageURL'] ?? null,
            'AudioURL' => $item['AudioURL'] ?? null
        ];
    }, $items);

    $response = [
        'success' => true,
        'count' => count($formattedItems),
        'items' => $formattedItems
    ];

    sendResponse($response, 200);

} catch (PDOException $e) {
    error_log("Get pre-assessment items error: " . $e->getMessage());
    sendError("Failed to fetch assessment items", 500, $e->getMessage());
} catch (Exception $e) {
    error_log("Get pre-assessment items error: " . $e->getMessage());
    sendError("An error occurred", 500, $e->getMessage());
}
?>
