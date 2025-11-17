<?php
/**
 * LiteRise - Get Class Statistics API
 * Endpoint: GET /api/get_class_statistics.php?TeacherID=1
 * Returns comprehensive analytics for a teacher's class
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

// Get TeacherID
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $teacherId = isset($_GET['TeacherID']) ? (int)$_GET['TeacherID'] : 0;
} else {
    $input = json_decode(file_get_contents('php://input'), true);
    $teacherId = isset($input['TeacherID']) ? (int)$input['TeacherID'] : 0;
}

if ($teacherId <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'Valid TeacherID required']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Overall class statistics
    $overallSql = "SELECT
                    COUNT(DISTINCT s.StudentID) as TotalStudents,
                    AVG(s.CurrentAbility) as AverageAbility,
                    SUM(s.TotalXP) as TotalClassXP,
                    AVG(s.TotalXP) as AverageXP,
                    MAX(s.CurrentStreak) as LongestStreak,
                    (SELECT COUNT(*) FROM TestSessions ts
                     INNER JOIN Students st ON ts.StudentID = st.StudentID
                     WHERE st.TeacherID = :teacherId AND ts.IsCompleted = 1) as TotalCompletedSessions
                FROM Students s
                WHERE s.TeacherID = :teacherId AND s.IsActive = 1";

    $stmt = $conn->prepare($overallSql);
    $stmt->bindParam(':teacherId', $teacherId, PDO::PARAM_INT);
    $stmt->execute();
    $overall = $stmt->fetch();

    // Ability distribution
    $distributionSql = "SELECT
                        SUM(CASE WHEN CurrentAbility < -1.0 THEN 1 ELSE 0 END) as Beginner,
                        SUM(CASE WHEN CurrentAbility >= -1.0 AND CurrentAbility < 0.5 THEN 1 ELSE 0 END) as Developing,
                        SUM(CASE WHEN CurrentAbility >= 0.5 AND CurrentAbility < 1.5 THEN 1 ELSE 0 END) as Intermediate,
                        SUM(CASE WHEN CurrentAbility >= 1.5 AND CurrentAbility < 2.5 THEN 1 ELSE 0 END) as Advanced,
                        SUM(CASE WHEN CurrentAbility >= 2.5 THEN 1 ELSE 0 END) as Expert
                    FROM Students
                    WHERE TeacherID = :teacherId AND IsActive = 1";

    $stmt = $conn->prepare($distributionSql);
    $stmt->bindParam(':teacherId', $teacherId, PDO::PARAM_INT);
    $stmt->execute();
    $distribution = $stmt->fetch();

    // Recent activity (last 7 days)
    $activitySql = "SELECT COUNT(DISTINCT ts.SessionID) as RecentSessions,
                           COUNT(DISTINCT ts.StudentID) as ActiveStudents
                    FROM TestSessions ts
                    INNER JOIN Students s ON ts.StudentID = s.StudentID
                    WHERE s.TeacherID = :teacherId
                      AND ts.StartTime >= DATEADD(day, -7, GETDATE())";

    $stmt = $conn->prepare($activitySql);
    $stmt->bindParam(':teacherId', $teacherId, PDO::PARAM_INT);
    $stmt->execute();
    $activity = $stmt->fetch();

    // Top 5 students by XP
    $topStudentsSql = "SELECT TOP 5
                        StudentID, FirstName, LastName, TotalXP, CurrentAbility
                    FROM Students
                    WHERE TeacherID = :teacherId AND IsActive = 1
                    ORDER BY TotalXP DESC";

    $stmt = $conn->prepare($topStudentsSql);
    $stmt->bindParam(':teacherId', $teacherId, PDO::PARAM_INT);
    $stmt->execute();
    $topStudents = $stmt->fetchAll();

    $topStudentsArray = [];
    foreach ($topStudents as $student) {
        $topStudentsArray[] = [
            'StudentID' => (int)$student['StudentID'],
            'FullName' => $student['FirstName'] . ' ' . $student['LastName'],
            'TotalXP' => (int)$student['TotalXP'],
            'CurrentAbility' => (float)$student['CurrentAbility']
        ];
    }

    // Grade level breakdown
    $gradeSql = "SELECT GradeLevel, COUNT(*) as StudentCount,
                        AVG(CurrentAbility) as AverageAbility,
                        AVG(TotalXP) as AverageXP
                 FROM Students
                 WHERE TeacherID = :teacherId AND IsActive = 1
                 GROUP BY GradeLevel
                 ORDER BY GradeLevel";

    $stmt = $conn->prepare($gradeSql);
    $stmt->bindParam(':teacherId', $teacherId, PDO::PARAM_INT);
    $stmt->execute();
    $gradeBreakdown = $stmt->fetchAll();

    $gradeArray = [];
    foreach ($gradeBreakdown as $grade) {
        $gradeArray[] = [
            'GradeLevel' => (int)$grade['GradeLevel'],
            'StudentCount' => (int)$grade['StudentCount'],
            'AverageAbility' => round((float)$grade['AverageAbility'], 4),
            'AverageXP' => (int)$grade['AverageXP']
        ];
    }

    $response = [
        'success' => true,
        'TeacherID' => $teacherId,
        'overall' => [
            'TotalStudents' => (int)$overall['TotalStudents'],
            'AverageAbility' => round((float)$overall['AverageAbility'], 4),
            'TotalClassXP' => (int)$overall['TotalClassXP'],
            'AverageXP' => (int)$overall['AverageXP'],
            'LongestStreak' => (int)$overall['LongestStreak'],
            'TotalCompletedSessions' => (int)$overall['TotalCompletedSessions']
        ],
        'abilityDistribution' => [
            'Beginner' => (int)$distribution['Beginner'],
            'Developing' => (int)$distribution['Developing'],
            'Intermediate' => (int)$distribution['Intermediate'],
            'Advanced' => (int)$distribution['Advanced'],
            'Expert' => (int)$distribution['Expert']
        ],
        'recentActivity' => [
            'RecentSessions' => (int)$activity['RecentSessions'],
            'ActiveStudents' => (int)$activity['ActiveStudents'],
            'Period' => 'Last 7 days'
        ],
        'topStudents' => $topStudentsArray,
        'gradeBreakdown' => $gradeArray
    ];

    http_response_code(200);
    echo json_encode($response);

} catch (Exception $e) {
    error_log("Get class statistics error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve class statistics']);
}
