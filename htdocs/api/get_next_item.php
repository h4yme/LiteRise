<?php
/**
 * LiteRise Adaptive Item Selection API
 * POST /api/get_next_item.php
 *
 * Uses IRT to select the next best item based on current ability estimate
 *
 * Request Body:
 * {
 *   "session_id": 123,
 *   "current_theta": 0.5,
 *   "items_answered": [1, 3, 5, 7]  // IDs of already answered items
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "item": { ... item details ... },
 *   "current_theta": 0.5,
 *   "items_remaining": 15,
 *   "assessment_complete": false
 * }
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';
require_once __DIR__ . '/irt.php';

// Require authentication
$authUser = requireAuth();

// Get JSON input
$data = getJsonInput();
$sessionID = $data['session_id'] ?? null;
$currentTheta = $data['current_theta'] ?? 0.0;
$itemsAnswered = $data['items_answered'] ?? [];

// Configuration
$maxItems = 20; // Maximum items in assessment
$targetSEM = 0.3; // Target precision (stop when SEM is this low)
$minItems = 10; // Minimum items before considering stopping

// Auto-create session if not provided (for Android app compatibility)
$autoCreatedSession = false;
if (!$sessionID || $sessionID == 0) {
    error_log("Auto-creating PreAssessment session for student " . $authUser['studentID']);
    try {
        $stmt = $conn->prepare("EXEC SP_CreateTestSession @StudentID = :studentID, @Type = :type");
        $stmt->bindValue(':studentID', $authUser['studentID'], PDO::PARAM_INT);
        $stmt->bindValue(':type', 'PreAssessment', PDO::PARAM_STR);
        $stmt->execute();

        $session = $stmt->fetch(PDO::FETCH_ASSOC);

        // Close cursor to prevent "other threads running in session" error
        $stmt->closeCursor();

        if ($session) {
            $sessionID = $session['SessionID'];
            $autoCreatedSession = true;
            error_log("Created session $sessionID for student " . $authUser['studentID']);
        } else {
            sendError("Failed to create session", 500);
        }
    } catch (Exception $e) {
        error_log("Failed to create session: " . $e->getMessage());
        sendError("Failed to create session", 500, $e->getMessage());
    }
}

try {
    // Verify session belongs to authenticated user
    $stmt = $conn->prepare("SELECT StudentID, SessionType FROM TestSessions WHERE SessionID = ?");
    $stmt->execute([$sessionID]);
    $session = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$session) {
        sendError("Session not found", 404);
    }

    if ($session['StudentID'] != $authUser['studentID']) {
        sendError("Unauthorized", 403);
    }

    // Get all available items (not yet answered)
    $placeholders = str_repeat('?,', count($itemsAnswered) - 1) . '?';
    $query = "SELECT
                ItemID,
                ItemText,
                ItemType,
                DifficultyLevel,
                AnswerChoices,
                CorrectAnswer,
                DifficultyParam,
                DiscriminationParam,
                GuessingParam,
                ImageURL,
                AudioURL,
                Phonetic,
                Definition
              FROM Items
              WHERE IsActive = 1";

    if (!empty($itemsAnswered)) {
        $query .= " AND ItemID NOT IN ($placeholders)";
        $stmt = $conn->prepare($query);
        $stmt->execute($itemsAnswered);
    } else {
        $stmt = $conn->prepare($query);
        $stmt->execute();
    }

    $availableItems = $stmt->fetchAll(PDO::FETCH_ASSOC);

    if (empty($availableItems)) {
        sendResponse([
            'success' => true,
            'session_id' => $sessionID,
            'assessment_complete' => true,
            'message' => 'No more items available',
            'final_theta' => $currentTheta
        ], 200);
        exit;
    }

    // Check if we should stop (enough items and good precision)
    $itemsCompleted = count($itemsAnswered);
    $shouldStop = false;

    if ($itemsCompleted >= $minItems) {
        // Calculate current SEM if we have response data
        $stmt = $conn->prepare(
            "SELECT
                r.IsCorrect,
                i.DiscriminationParam as a,
                i.DifficultyParam as b,
                i.GuessingParam as c
             FROM Responses r
             JOIN Items i ON r.ItemID = i.ItemID
             WHERE r.SessionID = ?"
        );
        $stmt->execute([$sessionID]);
        $responses = $stmt->fetchAll(PDO::FETCH_ASSOC);

        if (!empty($responses)) {
            $irt = new ItemResponseTheory();

            // Convert to IRT format
            $irtResponses = array_map(function($r) {
                return [
                    'isCorrect' => (bool)$r['IsCorrect'],
                    'a' => (float)$r['a'],
                    'b' => (float)$r['b'],
                    'c' => (float)$r['c']
                ];
            }, $responses);

            $sem = $irt->calculateSEM($currentTheta, $irtResponses);

            if ($sem <= $targetSEM || $itemsCompleted >= $maxItems) {
                $shouldStop = true;
            }
        }
    }

    if ($shouldStop || $itemsCompleted >= $maxItems) {
        sendResponse([
            'success' => true,
            'session_id' => $sessionID,
            'assessment_complete' => true,
            'message' => 'Assessment complete - sufficient precision achieved',
            'items_completed' => $itemsCompleted,
            'final_theta' => $currentTheta,
            'sem' => $sem ?? null
        ], 200);
        exit;
    }

    // Use IRT to select the next best item
    $irt = new ItemResponseTheory();

    // Prepare items for IRT selection
    $irtItems = array_map(function($item) {
        return [
            'itemID' => $item['ItemID'],
            'a' => (float)$item['DiscriminationParam'],
            'b' => (float)$item['DifficultyParam'],
            'c' => (float)$item['GuessingParam'],
            'raw' => $item // Keep full item data
        ];
    }, $availableItems);

    // Select best item using Maximum Information
    $selectedIRTItem = $irt->selectNextItem($currentTheta, $irtItems);

    if (!$selectedIRTItem) {
        sendError("Failed to select next item", 500);
    }

    // Get the full item data
    $selectedItem = $selectedIRTItem['raw'];
    $itemType = $selectedItem['ItemType'] ?? '';

    // Format item similar to get_preassessment_items.php
    $answerChoices = [];
    if (!empty($selectedItem['AnswerChoices'])) {
        $decoded = json_decode($selectedItem['AnswerChoices'], true);
        $answerChoices = $decoded ?? [];
    }

    $optionA = '';
    $optionB = '';
    $optionC = '';
    $optionD = '';
    $correctOption = '';
    $scrambledWords = [];

    if ($itemType === 'Syntax') {
        // Handle Syntax items (same logic as get_preassessment_items.php)
        $scrambledWords = array_map('trim', explode(' / ', $selectedItem['ItemText']));

        if (empty($answerChoices) && !empty($selectedItem['CorrectAnswer'])) {
            require_once 'get_preassessment_items.php'; // For generateIncorrectSentences

            $correctSentence = $selectedItem['CorrectAnswer'];
            $incorrectOptions = generateIncorrectSentences($scrambledWords, $correctSentence);
            $allOptions = array_merge([$correctSentence], $incorrectOptions);
            shuffle($allOptions);

            $optionA = $allOptions[0] ?? '';
            $optionB = $allOptions[1] ?? '';
            $optionC = $allOptions[2] ?? '';
            $optionD = $allOptions[3] ?? '';

            if ($correctSentence === $optionA) $correctOption = 'A';
            elseif ($correctSentence === $optionB) $correctOption = 'B';
            elseif ($correctSentence === $optionC) $correctOption = 'C';
            elseif ($correctSentence === $optionD) $correctOption = 'D';
        }
    } else {
        // Handle other item types
        $optionA = $answerChoices[0] ?? '';
        $optionB = $answerChoices[1] ?? '';
        $optionC = $answerChoices[2] ?? '';
        $optionD = $answerChoices[3] ?? '';

        if (!empty($selectedItem['CorrectAnswer'])) {
            $correctAnswer = trim($selectedItem['CorrectAnswer']);
            if ($correctAnswer === $optionA) $correctOption = 'A';
            elseif ($correctAnswer === $optionB) $correctOption = 'B';
            elseif ($correctAnswer === $optionC) $correctOption = 'C';
            elseif ($correctAnswer === $optionD) $correctOption = 'D';
        }
    }

    // Use Phonetic field for pronunciation items
    $passageText = '';
    if ($itemType === 'Pronunciation' && !empty($selectedItem['Phonetic'])) {
        $passageText = $selectedItem['Phonetic'];
    }

    $formattedItem = [
        'ItemID' => (int)$selectedItem['ItemID'],
        'ItemText' => $selectedItem['ItemText'] ?? '',
        'QuestionText' => $itemType === 'Syntax'
            ? 'Arrange the words to form a correct sentence:'
            : ($selectedItem['ItemText'] ?? ''),
        'PassageText' => $passageText,
        'ItemType' => $itemType,
        'DifficultyLevel' => $selectedItem['DifficultyLevel'] ?? '',
        'Difficulty' => (float)($selectedItem['DifficultyParam'] ?? 0),
        'DifficultyParam' => (float)($selectedItem['DifficultyParam'] ?? 0),
        'Discrimination' => (float)($selectedItem['DiscriminationParam'] ?? 1.0),
        'DiscriminationParam' => (float)($selectedItem['DiscriminationParam'] ?? 1.0),
        'GuessingParam' => (float)($selectedItem['GuessingParam'] ?? 0.25),
        'AnswerChoices' => $answerChoices,
        'ScrambledWords' => $scrambledWords,
        'OptionA' => $optionA,
        'OptionB' => $optionB,
        'OptionC' => $optionC,
        'OptionD' => $optionD,
        'CorrectAnswer' => $selectedItem['CorrectAnswer'] ?? '',
        'CorrectOption' => $correctOption,
        'ImageURL' => $selectedItem['ImageURL'] ?? null,
        'AudioURL' => $selectedItem['AudioURL'] ?? null,
        'Phonetic' => $selectedItem['Phonetic'] ?? null,
        'Definition' => $selectedItem['Definition'] ?? null
    ];

    $response = [
        'success' => true,
        'session_id' => $sessionID,
        'item' => $formattedItem,
        'current_theta' => round($currentTheta, 3),
        'items_completed' => $itemsCompleted,
        'items_remaining' => min($maxItems - $itemsCompleted, count($availableItems)),
        'assessment_complete' => false,
        'progress_percentage' => round(($itemsCompleted / $maxItems) * 100, 1)
    ];

    sendResponse($response, 200);

} catch (PDOException $e) {
    error_log("Get next item error: " . $e->getMessage());
    sendError("Failed to get next item", 500, $e->getMessage());
} catch (Exception $e) {
    error_log("Get next item error: " . $e->getMessage());
    sendError("An error occurred", 500, $e->getMessage());
}
?>
