<?php
/**
 * LiteRise - Get Student Progress API
 * Endpoint: GET/POST /api/get_student_progress.php
 * Returns comprehensive student statistics and progress
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

// Get StudentID from query parameter or JSON body
$studentId = null;

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $studentId = isset($_GET['StudentID']) ? (int)$_GET['StudentID'] : null;
} else {
    $input = json_decode(file_get_contents('php://input'), true);
    $studentId = isset($input['StudentID']) ? (int)$input['StudentID'] : null;
}

if (!$studentId) {
    http_response_code(400);
    echo json_encode(['error' => 'StudentID required']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Call stored procedure
    $sql = "EXEC SP_GetStudentProgress @StudentID = :studentId";
    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $stmt->execute();

    $progress = $stmt->fetch();

    if (!$progress) {
        http_response_code(404);
        echo json_encode(['error' => 'Student not found']);
        exit();
    }

    // Get recent activity log
    $activitySql = "SELECT TOP 10 ActivityType, ActivityDetails, Timestamp
                    FROM ActivityLog
                    WHERE StudentID = :studentId
                    ORDER BY Timestamp DESC";

    $activityStmt = $conn->prepare($activitySql);
    $activityStmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $activityStmt->execute();
    $activities = $activityStmt->fetchAll();

    // Get earned badges
    $badgesSql = "SELECT b.BadgeID, b.BadgeName, b.BadgeDescription,
                         b.BadgeIconURL, sb.DateEarned
                  FROM StudentBadges sb
                  JOIN Badges b ON sb.BadgeID = b.BadgeID
                  WHERE sb.StudentID = :studentId
                  ORDER BY sb.DateEarned DESC";

    $badgesStmt = $conn->prepare($badgesSql);
    $badgesStmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $badgesStmt->execute();
    $badges = $badgesStmt->fetchAll();

    // Get session history
    $sessionsSql = "SELECT SessionID, SessionType, InitialTheta, FinalTheta,
                           StartTime, EndTime, TotalQuestions, CorrectAnswers,
                           AccuracyPercentage
                    FROM TestSessions
                    WHERE StudentID = :studentId AND IsCompleted = 1
                    ORDER BY StartTime DESC";

    $sessionsStmt = $conn->prepare($sessionsSql);
    $sessionsStmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $sessionsStmt->execute();
    $sessions = $sessionsStmt->fetchAll();

    $response = [
        'StudentID' => $studentId,
        'FirstName' => $progress['FirstName'],
        'LastName' => $progress['LastName'],
        'FullName' => $progress['FirstName'] . ' ' . $progress['LastName'],
        'CurrentAbility' => (float)$progress['CurrentAbility'],
        'TotalXP' => (int)$progress['TotalXP'],
        'CurrentStreak' => (int)$progress['CurrentStreak'],
        'LongestStreak' => (int)$progress['LongestStreak'],
        'TotalSessions' => (int)$progress['TotalSessions'],
        'AverageAccuracy' => round((float)$progress['AverageAccuracy'], 2),
        'TotalBadges' => (int)$progress['TotalBadges'],
        'RecentActivities' => $activities,
        'EarnedBadges' => $badges,
        'SessionHistory' => $sessions
    ];

    http_response_code(200);
    echo json_encode($response);

} catch (Exception $e) {
    error_log("Get student progress error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve student progress']);
}
