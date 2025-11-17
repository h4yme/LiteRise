<?php
/**
 * LiteRise - Save Game Result API
 * Endpoint: POST /api/save_game_result.php
 * Saves game results and updates student XP and streaks
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit();
}

$input = json_decode(file_get_contents('php://input'), true);

$required = ['SessionID', 'StudentID', 'GameType', 'Score', 'AccuracyPercentage', 'TimeCompleted'];

foreach ($required as $field) {
    if (!isset($input[$field])) {
        http_response_code(400);
        echo json_encode(['error' => "$field is required"]);
        exit();
    }
}

$sessionId = (int)$input['SessionID'];
$studentId = (int)$input['StudentID'];
$gameType = trim($input['GameType']);
$score = (int)$input['Score'];
$accuracy = (float)$input['AccuracyPercentage'];
$timeCompleted = (int)$input['TimeCompleted'];
$xpEarned = isset($input['XPEarned']) ? (int)$input['XPEarned'] : 0;
$streakAchieved = isset($input['StreakAchieved']) ? (int)$input['StreakAchieved'] : 0;

// Validate game type
$validGameTypes = ['SentenceScramble', 'TimedTrail'];
if (!in_array($gameType, $validGameTypes)) {
    http_response_code(400);
    echo json_encode(['error' => 'Invalid game type']);
    exit();
}

// Calculate XP if not provided
if ($xpEarned === 0) {
    // Base XP calculation: score + accuracy bonus + time bonus
    $xpEarned = $score;

    // Accuracy bonus
    if ($accuracy >= 90) {
        $xpEarned += 50;
    } elseif ($accuracy >= 75) {
        $xpEarned += 25;
    }

    // Time bonus (faster = more XP)
    if ($timeCompleted < 30) {
        $xpEarned += 30;
    } elseif ($timeCompleted < 60) {
        $xpEarned += 15;
    }
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Call stored procedure
    $sql = "EXEC SP_SaveGameResult
            @SessionID = :sessionId,
            @StudentID = :studentId,
            @GameType = :gameType,
            @Score = :score,
            @AccuracyPercentage = :accuracy,
            @TimeCompleted = :timeCompleted,
            @XPEarned = :xpEarned,
            @StreakAchieved = :streakAchieved";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':sessionId', $sessionId, PDO::PARAM_INT);
    $stmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $stmt->bindParam(':gameType', $gameType, PDO::PARAM_STR);
    $stmt->bindParam(':score', $score, PDO::PARAM_INT);
    $stmt->bindParam(':accuracy', $accuracy, PDO::PARAM_STR);
    $stmt->bindParam(':timeCompleted', $timeCompleted, PDO::PARAM_INT);
    $stmt->bindParam(':xpEarned', $xpEarned, PDO::PARAM_INT);
    $stmt->bindParam(':streakAchieved', $streakAchieved, PDO::PARAM_INT);
    $stmt->execute();

    // Get updated student stats
    $statsSql = "SELECT TotalXP, CurrentStreak, LongestStreak
                 FROM Students
                 WHERE StudentID = :studentId";

    $statsStmt = $conn->prepare($statsSql);
    $statsStmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $statsStmt->execute();
    $stats = $statsStmt->fetch();

    // Check for badge unlocks
    $badgeSql = "EXEC SP_CheckBadgeUnlock @StudentID = :studentId";
    $badgeStmt = $conn->prepare($badgeSql);
    $badgeStmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $badgeStmt->execute();
    $newBadges = $badgeStmt->fetchAll();

    http_response_code(201);
    echo json_encode([
        'success' => true,
        'XPEarned' => $xpEarned,
        'TotalXP' => (int)$stats['TotalXP'],
        'CurrentStreak' => (int)$stats['CurrentStreak'],
        'LongestStreak' => (int)$stats['LongestStreak'],
        'NewBadges' => $newBadges,
        'message' => 'Game result saved successfully'
    ]);

} catch (Exception $e) {
    error_log("Save game result error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to save game result']);
}
