<?php
/**
 * LiteRise - Get Students by Teacher API
 * Endpoint: GET /api/get_students_by_teacher.php?TeacherID=1
 * Returns all students assigned to a specific teacher
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

    // Get all students for this teacher with their latest stats
    $sql = "SELECT s.StudentID, s.FirstName, s.LastName, s.Email,
                   s.GradeLevel, s.Section,
                   s.CurrentAbility, s.InitialAbility,
                   s.TotalXP, s.CurrentStreak, s.LongestStreak,
                   s.LastLogin, s.CreatedDate,
                   (SELECT COUNT(*) FROM TestSessions
                    WHERE StudentID = s.StudentID AND IsCompleted = 1) as CompletedSessions,
                   (SELECT COUNT(*) FROM StudentBadges
                    WHERE StudentID = s.StudentID) as BadgeCount
            FROM Students s
            WHERE s.TeacherID = :teacherId AND s.IsActive = 1
            ORDER BY s.LastName, s.FirstName";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':teacherId', $teacherId, PDO::PARAM_INT);
    $stmt->execute();
    $students = $stmt->fetchAll();

    $response = [];
    foreach ($students as $student) {
        $abilityGrowth = (float)$student['CurrentAbility'] - (float)$student['InitialAbility'];

        $response[] = [
            'StudentID' => (int)$student['StudentID'],
            'FullName' => $student['FirstName'] . ' ' . $student['LastName'],
            'FirstName' => $student['FirstName'],
            'LastName' => $student['LastName'],
            'Email' => $student['Email'],
            'GradeLevel' => (int)$student['GradeLevel'],
            'Section' => $student['Section'],
            'CurrentAbility' => (float)$student['CurrentAbility'],
            'InitialAbility' => (float)$student['InitialAbility'],
            'AbilityGrowth' => round($abilityGrowth, 4),
            'TotalXP' => (int)$student['TotalXP'],
            'CurrentStreak' => (int)$student['CurrentStreak'],
            'LongestStreak' => (int)$student['LongestStreak'],
            'CompletedSessions' => (int)$student['CompletedSessions'],
            'BadgeCount' => (int)$student['BadgeCount'],
            'LastLogin' => $student['LastLogin'],
            'MemberSince' => $student['CreatedDate']
        ];
    }

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'TeacherID' => $teacherId,
        'count' => count($response),
        'students' => $response
    ]);

} catch (Exception $e) {
    error_log("Get students by teacher error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve students']);
}
