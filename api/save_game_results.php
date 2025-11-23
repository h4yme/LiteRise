<?php

/**

 * LiteRise Save Game Result API

 * POST /api/save_game_result.php

 *

 * Saves game results and awards XP/streaks

 *

 * Request Body:

 * {

 *   "session_id": 123,

 *   "student_id": 1,

 *   "game_type": "SentenceScramble",

 *   "score": 850,

 *   "accuracy_percentage": 85.0,

 *   "time_completed": 120,

 *   "xp_earned": 100,

 *   "streak_achieved": 7

 * }

 *

 * Response:

 * {

 *   "success": true,

 *   "message": "Game result saved successfully",

 *   "game_result_id": 456,

 *   "student": {

 *     "TotalXP": 1300,

 *     "CurrentStreak": 7,

 *     "LongestStreak": 10

 *   },

 *   "badges_unlocked": []

 * }

 */

 

require_once __DIR__ . '/src/db.php';

require_once __DIR__ . '/src/auth.php';

 

// Require authentication

$authUser = requireAuth();

 

// Get JSON input

$data = getJsonInput();

$sessionID = $data['session_id'] ?? null;

$studentID = $data['student_id'] ?? null;

$gameType = $data['game_type'] ?? '';

$score = $data['score'] ?? 0;

$accuracyPercentage = $data['accuracy_percentage'] ?? 0.0;

$timeCompleted = $data['time_completed'] ?? 0;

$xpEarned = $data['xp_earned'] ?? 0;

$streakAchieved = $data['streak_achieved'] ?? 0;

 

// Validate required fields

validateRequired($data, ['session_id', 'student_id', 'game_type', 'score']);

 

// Verify authenticated user

if ($authUser['studentID'] != $studentID) {

    sendError("Unauthorized: Cannot save game result for another student", 403);

}

 

// Validate game type

$validGameTypes = ['SentenceScramble', 'TimedTrail'];

if (!in_array($gameType, $validGameTypes)) {

    sendError("Invalid game_type. Must be: " . implode(', ', $validGameTypes), 400);

}

 

try {

    // Start transaction

    $conn->beginTransaction();

 

    // Verify session exists and belongs to student

    $stmt = $conn->prepare(

        "SELECT StudentID, SessionType FROM TestSessions WHERE SessionID = ?"

    );

    $stmt->execute([$sessionID]);

    $session = $stmt->fetch(PDO::FETCH_ASSOC);

 

    if (!$session) {

        $conn->rollBack();

        sendError("Session not found", 404);

    }

 

    if ($session['StudentID'] != $studentID) {

        $conn->rollBack();

        sendError("Session does not belong to this student", 403);

    }

 

    // Call stored procedure to save game result

    $stmt = $conn->prepare(

        "EXEC SP_SaveGameResult

         @SessionID = ?,

         @StudentID = ?,

         @GameType = ?,

         @Score = ?,

         @AccuracyPercentage = ?,

         @TimeCompleted = ?,

         @XPEarned = ?,

         @StreakAchieved = ?"

    );

    $stmt->execute([

        $sessionID,

        $studentID,

        $gameType,

        $score,

        $accuracyPercentage,

        $timeCompleted,

        $xpEarned,

        $streakAchieved

    ]);

 

    // Get the game result ID

    $stmt = $conn->prepare(

        "SELECT TOP 1 GameResultID

         FROM GameResults

         WHERE StudentID = ? AND SessionID = ?

         ORDER BY DatePlayed DESC"

    );

    $stmt->execute([$studentID, $sessionID]);

    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    $gameResultID = $result['GameResultID'] ?? null;

 

    // Get updated student stats

    $stmt = $conn->prepare(

        "SELECT TotalXP, CurrentStreak, LongestStreak

         FROM Students

         WHERE StudentID = ?"

    );

    $stmt->execute([$studentID]);

    $studentStats = $stmt->fetch(PDO::FETCH_ASSOC);

 

    // Check for badge unlocks

    $stmt = $conn->prepare("EXEC SP_CheckBadgeUnlock @StudentID = ?");

    $stmt->execute([$studentID]);

    $unlockedBadges = $stmt->fetchAll(PDO::FETCH_ASSOC);

 

    // Mark session as completed

    $stmt = $conn->prepare(

        "UPDATE TestSessions

         SET IsCompleted = 1, EndTime = GETDATE()

         WHERE SessionID = ?"

    );

    $stmt->execute([$sessionID]);

 

    // Commit transaction

    $conn->commit();

 

    // Log activity

    logActivity(

        $studentID,

        'GameComplete',

        "Completed $gameType - Score: $score, Accuracy: $accuracyPercentage%, XP: +$xpEarned"

    );

 

    $response = [

        'success' => true,

        'message' => 'Game result saved successfully',

        'game_result_id' => (int)$gameResultID,

        'student' => [

            'TotalXP' => (int)$studentStats['TotalXP'],

            'CurrentStreak' => (int)$studentStats['CurrentStreak'],

            'LongestStreak' => (int)$studentStats['LongestStreak']

        ],

        'badges_unlocked' => $unlockedBadges

    ];

 

    sendResponse($response, 201);

 

} catch (PDOException $e) {

    if ($conn->inTransaction()) {

        $conn->rollBack();

    }

    error_log("Save game result error: " . $e->getMessage());

    sendError("Failed to save game result", 500, $e->getMessage());

} catch (Exception $e) {

    if ($conn->inTransaction()) {

        $conn->rollBack();

    }

    error_log("Save game result error: " . $e->getMessage());

    sendError("An error occurred", 500, $e->getMessage());

}

?>