<?php
/**
 * LiteRise - Get Game Data API
 * Endpoint: GET/POST /api/get_game_data.php
 * Returns game questions for Sentence Scramble or Timed Trail
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$gameType = null;
$gradeLevel = null;
$count = 10;

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $gameType = isset($_GET['GameType']) ? $_GET['GameType'] : null;
    $gradeLevel = isset($_GET['GradeLevel']) ? (int)$_GET['GradeLevel'] : null;
    $count = isset($_GET['Count']) ? (int)$_GET['Count'] : 10;
} else {
    $input = json_decode(file_get_contents('php://input'), true);
    $gameType = isset($input['GameType']) ? $input['GameType'] : null;
    $gradeLevel = isset($input['GradeLevel']) ? (int)$input['GradeLevel'] : null;
    $count = isset($input['Count']) ? (int)$input['Count'] : 10;
}

if (!$gameType || !$gradeLevel) {
    http_response_code(400);
    echo json_encode(['error' => 'GameType and GradeLevel required']);
    exit();
}

// Validate game type
$validGameTypes = ['SentenceScramble', 'TimedTrail'];
if (!in_array($gameType, $validGameTypes)) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid game type']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    if ($gameType === 'SentenceScramble') {
        // Call stored procedure for Sentence Scramble
        $sql = "EXEC SP_GetSentenceScrambleData @GradeLevel = :gradeLevel, @Count = :count";
    } else {
        // Call stored procedure for Timed Trail
        $sql = "EXEC SP_GetTimedTrailData @GradeLevel = :gradeLevel, @Count = :count";
    }

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':gradeLevel', $gradeLevel, PDO::PARAM_INT);
    $stmt->bindParam(':count', $count, PDO::PARAM_INT);
    $stmt->execute();

    $items = $stmt->fetchAll();

    // Format data for specific game type
    $gameData = [];

    foreach ($items as $item) {
        if ($gameType === 'SentenceScramble') {
            // Parse scrambled words
            $words = explode(' / ', $item['ItemText']);

            $gameData[] = [
                'ItemID' => (int)$item['ItemID'],
                'ScrambledWords' => $words,
                'CorrectSentence' => $item['CorrectAnswer'],
                'DifficultyLevel' => $item['DifficultyLevel'],
                'Hint' => count($words) . ' words'
            ];
        } else {
            // Timed Trail - multiple types of questions
            $choices = [];
            if (!empty($item['AnswerChoices'])) {
                $choicesJson = json_decode($item['AnswerChoices'], true);
                if (is_array($choicesJson)) {
                    $choices = $choicesJson;
                }
            }

            $gameData[] = [
                'ItemID' => (int)$item['ItemID'],
                'QuestionText' => $item['ItemText'],
                'ItemType' => $item['ItemType'],
                'Choices' => $choices,
                'CorrectAnswer' => $item['CorrectAnswer'],
                'DifficultyLevel' => $item['DifficultyLevel']
            ];
        }
    }

    http_response_code(200);
    echo json_encode([
        'GameType' => $gameType,
        'GradeLevel' => $gradeLevel,
        'ItemCount' => count($gameData),
        'Items' => $gameData
    ]);

} catch (Exception $e) {
    error_log("Get game data error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve game data']);
}
