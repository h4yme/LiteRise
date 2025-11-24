<?php
/**
 * Word Hunt Game API Endpoint
 * Returns vocabulary words for the Word Hunt game
 * Filters by student's grade level
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
    $studentID = $data['student_id'] ?? $_GET['student_id'] ?? null;
    $gridSize = 10; // Default grid size

    $words = [];
    $gradeLevel = 4; // Default grade level

    // Get student's grade level if student_id is provided
    if ($studentID) {
        try {
            $gradeSql = "SELECT GradeLevel FROM Students WHERE StudentID = ?";
            $gradeStmt = $conn->prepare($gradeSql);
            $gradeStmt->execute([$studentID]);
            $studentData = $gradeStmt->fetch(PDO::FETCH_ASSOC);
            if ($studentData && isset($studentData['GradeLevel'])) {
                $gradeLevel = (int)$studentData['GradeLevel'];
            }
        } catch (Exception $e) {
            error_log("Failed to get student grade level: " . $e->getMessage());
        }
    }

    // Try to get words from VocabularyWords table filtered by grade level
    try {
        // Get words for student's grade level and one level below (for variety)
        $sql = "SELECT TOP (?)
                    WordID as word_id,
                    Word as word,
                    Definition as definition,
                    ExampleSentence as example_sentence,
                    Difficulty as difficulty,
                    Category as category,
                    GradeLevel as grade_level
                FROM VocabularyWords
                WHERE IsActive = 1
                  AND GradeLevel BETWEEN ? AND ?
                ORDER BY NEWID()";

        $minGrade = max(4, $gradeLevel - 1); // Don't go below grade 4
        $maxGrade = min(6, $gradeLevel);     // Don't exceed grade 6

        $stmt = $conn->prepare($sql);
        $stmt->execute([$count, $minGrade, $maxGrade]);
        $words = $stmt->fetchAll(PDO::FETCH_ASSOC);

        // If not enough words, expand the range
        if (count($words) < $count) {
            $sql = "SELECT TOP (?)
                        WordID as word_id,
                        Word as word,
                        Definition as definition,
                        ExampleSentence as example_sentence,
                        Difficulty as difficulty,
                        Category as category,
                        GradeLevel as grade_level
                    FROM VocabularyWords
                    WHERE IsActive = 1
                    ORDER BY ABS(GradeLevel - ?) ASC, NEWID()";

            $stmt = $conn->prepare($sql);
            $stmt->execute([$count, $gradeLevel]);
            $words = $stmt->fetchAll(PDO::FETCH_ASSOC);
        }
    } catch (Exception $e) {
        // Table might not exist, use fallback
        error_log("VocabularyWords query failed: " . $e->getMessage());
    }

    // If no words from database, use fallback filtered by grade
    if (empty($words)) {
        $words = getFallbackWords($count, $gradeLevel);
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
        'lesson_id' => $lessonID,
        'student_grade' => $gradeLevel
    ]);

} catch (PDOException $e) {
    error_log("Word Hunt DB error: " . $e->getMessage());
    sendError("Database error", 500);
} catch (Exception $e) {
    error_log("Word Hunt error: " . $e->getMessage());
    sendError("Error loading words", 500);
}

function getFallbackWords($count, $gradeLevel = 4) {
    $allWords = [
        // Grade 4 words
        ['word_id' => 1, 'word' => 'READ', 'definition' => 'To look at and understand written words', 'difficulty' => 0.3, 'grade_level' => 4],
        ['word_id' => 2, 'word' => 'BOOK', 'definition' => 'A written work that tells a story or gives information', 'difficulty' => 0.3, 'grade_level' => 4],
        ['word_id' => 3, 'word' => 'WORD', 'definition' => 'A unit of language that has meaning', 'difficulty' => 0.3, 'grade_level' => 4],
        ['word_id' => 4, 'word' => 'LEARN', 'definition' => 'To gain knowledge or skill through study or experience', 'difficulty' => 0.4, 'grade_level' => 4],
        ['word_id' => 5, 'word' => 'WRITE', 'definition' => 'To form letters or words on paper or screen', 'difficulty' => 0.4, 'grade_level' => 4],
        ['word_id' => 6, 'word' => 'STORY', 'definition' => 'An account of events, real or imaginary', 'difficulty' => 0.4, 'grade_level' => 4],
        ['word_id' => 7, 'word' => 'PLAY', 'definition' => 'To engage in activity for fun', 'difficulty' => 0.3, 'grade_level' => 4],
        ['word_id' => 8, 'word' => 'HELP', 'definition' => 'To assist or aid someone', 'difficulty' => 0.3, 'grade_level' => 4],

        // Grade 5 words
        ['word_id' => 9, 'word' => 'SPEAK', 'definition' => 'To say words aloud to communicate', 'difficulty' => 0.5, 'grade_level' => 5],
        ['word_id' => 10, 'word' => 'LISTEN', 'definition' => 'To pay attention to sounds or speech', 'difficulty' => 0.5, 'grade_level' => 5],
        ['word_id' => 11, 'word' => 'THINK', 'definition' => 'To use your mind to consider or reason', 'difficulty' => 0.5, 'grade_level' => 5],
        ['word_id' => 12, 'word' => 'STUDY', 'definition' => 'To give time and attention to learning', 'difficulty' => 0.5, 'grade_level' => 5],
        ['word_id' => 13, 'word' => 'ANSWER', 'definition' => 'A response to a question', 'difficulty' => 0.5, 'grade_level' => 5],
        ['word_id' => 14, 'word' => 'EXPLAIN', 'definition' => 'To make something clear', 'difficulty' => 0.6, 'grade_level' => 5],

        // Grade 6 words
        ['word_id' => 15, 'word' => 'ANALYZE', 'definition' => 'To examine in detail', 'difficulty' => 0.8, 'grade_level' => 6],
        ['word_id' => 16, 'word' => 'EVIDENCE', 'definition' => 'Facts that prove something', 'difficulty' => 0.8, 'grade_level' => 6],
        ['word_id' => 17, 'word' => 'SUMMARIZE', 'definition' => 'To give main points briefly', 'difficulty' => 0.9, 'grade_level' => 6],
        ['word_id' => 18, 'word' => 'CONTEXT', 'definition' => 'Surrounding circumstances', 'difficulty' => 0.9, 'grade_level' => 6],
    ];

    // Filter by grade level (include current grade and one below)
    $minGrade = max(4, $gradeLevel - 1);
    $maxGrade = min(6, $gradeLevel);

    $filtered = array_filter($allWords, function($word) use ($minGrade, $maxGrade) {
        return $word['grade_level'] >= $minGrade && $word['grade_level'] <= $maxGrade;
    });

    // If not enough words after filtering, use all words sorted by closest grade
    if (count($filtered) < $count) {
        usort($allWords, function($a, $b) use ($gradeLevel) {
            return abs($a['grade_level'] - $gradeLevel) - abs($b['grade_level'] - $gradeLevel);
        });
        $filtered = $allWords;
    }

    shuffle($filtered);
    return array_slice(array_values($filtered), 0, $count);
}
?>
