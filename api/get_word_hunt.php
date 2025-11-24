<?php
/**
 * Word Hunt Game API Endpoint
 * Returns vocabulary words for the Word Hunt game
 */

require_once __DIR__ . '/src/db.php';

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

    $words = [];

    // Try to get words from VocabularyWords table
    try {
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
    } catch (Exception $e) {
        // Table might not exist, use fallback
        error_log("VocabularyWords query failed: " . $e->getMessage());
    }

    // If no words from database, use fallback
    if (empty($words)) {
        $words = getFallbackWords($count);
    }

    // Process words
    foreach ($words as &$word) {
        // Ensure word is uppercase
        $word['word'] = strtoupper(trim($word['word']));

        // Adjust grid size if needed for longer words
        $wordLen = strlen($word['word']);
        if ($wordLen > $gridSize - 2) {
            $gridSize = $wordLen + 2;
        }
    }

    sendResponse([
        'success' => true,
        'words' => $words,
        'grid_size' => $gridSize,
        'lesson_id' => $lessonID
    ]);

} catch (PDOException $e) {
    error_log("Word Hunt DB error: " . $e->getMessage());
    sendError("Database error", 500);
} catch (Exception $e) {
    error_log("Word Hunt error: " . $e->getMessage());
    sendError("Error loading words", 500);
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
