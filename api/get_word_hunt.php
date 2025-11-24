<?php
/**
 * Word Hunt Game API Endpoint
 * Returns vocabulary words for the Word Hunt game
 */

header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: GET, POST, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type, Authorization");
header("Content-Type: application/json; charset=UTF-8");

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db_config.php';

try {
    // Get parameters from query string or JSON body
    $data = [];
    $rawInput = file_get_contents("php://input");
    if (!empty($rawInput)) {
        $data = json_decode($rawInput, true) ?? [];
    }

    $count = $data['count'] ?? $_GET['count'] ?? 8;
    $lessonID = $data['lesson_id'] ?? $_GET['lesson_id'] ?? null;
    $gridSize = 10; // Default grid size

    $conn = getDBConnection();
    $words = [];

    if ($lessonID) {
        // Get words from LessonGameContent for specific lesson
        $sql = "SELECT TOP (?)
                    ContentID as word_id,
                    ContentText as word,
                    ContentData as definition,
                    Difficulty as difficulty,
                    Category as category
                FROM LessonGameContent
                WHERE LessonID = ?
                  AND GameType = 'WordHunt'
                  AND IsActive = 1
                ORDER BY NEWID()";

        $stmt = $conn->prepare($sql);
        $stmt->execute([$count, $lessonID]);
        $words = $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    // If no lesson-specific content, get general vocabulary
    if (empty($words)) {
        // Check if VocabularyWords table exists
        $tableCheck = $conn->query("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'VocabularyWords'");
        $tableExists = $tableCheck->fetchColumn() > 0;

        if ($tableExists) {
            $sql = "SELECT TOP (?)
                        WordID as word_id,
                        Word as word,
                        Definition as definition,
                        ExampleSentence as example_sentence,
                        Difficulty as difficulty,
                        Category as category
                    FROM VocabularyWords
                    WHERE IsActive = 1
                    ORDER BY NEWID()";

            $stmt = $conn->prepare($sql);
            $stmt->execute([$count]);
            $words = $stmt->fetchAll(PDO::FETCH_ASSOC);
        }
    }

    // If still no words, use fallback
    if (empty($words)) {
        $words = getFallbackWords($count);
    }

    // Process words - parse definition from ContentData if needed
    foreach ($words as &$word) {
        // If definition looks like JSON, parse it
        if (isset($word['definition']) && $word['definition'] && $word['definition'][0] === '{') {
            $jsonData = json_decode($word['definition'], true);
            if ($jsonData && isset($jsonData['definition'])) {
                $word['definition'] = $jsonData['definition'];
                if (isset($jsonData['example'])) {
                    $word['example_sentence'] = $jsonData['example'];
                }
            }
        }

        // Ensure word is uppercase and reasonable length for grid
        $word['word'] = strtoupper(trim($word['word']));

        // Adjust grid size if needed for longer words
        $wordLen = strlen($word['word']);
        if ($wordLen > $gridSize - 2) {
            $gridSize = $wordLen + 2;
        }
    }

    echo json_encode([
        'success' => true,
        'words' => $words,
        'grid_size' => $gridSize,
        'lesson_id' => $lessonID
    ]);

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Database error: ' . $e->getMessage()
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}

function getFallbackWords($count) {
    $allWords = [
        ['word_id' => 1, 'word' => 'READ', 'definition' => 'To look at and understand written words', 'difficulty' => 0.4],
        ['word_id' => 2, 'word' => 'BOOK', 'definition' => 'A written work that tells a story or gives information', 'difficulty' => 0.3],
        ['word_id' => 3, 'word' => 'WORD', 'definition' => 'A unit of language that has meaning', 'difficulty' => 0.3],
        ['word_id' => 4, 'word' => 'LEARN', 'definition' => 'To gain knowledge or skill through study or experience', 'difficulty' => 0.5],
        ['word_id' => 5, 'word' => 'WRITE', 'definition' => 'To form letters or words on paper or screen', 'difficulty' => 0.4],
        ['word_id' => 6, 'word' => 'STORY', 'definition' => 'An account of events, real or imaginary', 'difficulty' => 0.5],
        ['word_id' => 7, 'word' => 'SPELL', 'definition' => 'To name or write the letters of a word in order', 'difficulty' => 0.6],
        ['word_id' => 8, 'word' => 'THINK', 'definition' => 'To use your mind to consider or reason', 'difficulty' => 0.5],
        ['word_id' => 9, 'word' => 'STUDY', 'definition' => 'To give time and attention to learning', 'difficulty' => 0.5],
        ['word_id' => 10, 'word' => 'SPEAK', 'definition' => 'To say words aloud to communicate', 'difficulty' => 0.4],
        ['word_id' => 11, 'word' => 'LISTEN', 'definition' => 'To pay attention to sounds or speech', 'difficulty' => 0.5],
        ['word_id' => 12, 'word' => 'ANSWER', 'definition' => 'A response to a question', 'difficulty' => 0.5],
    ];

    shuffle($allWords);
    return array_slice($allWords, 0, $count);
}
?>
