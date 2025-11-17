<?php
/**
 * LiteRise - Get Leaderboard API
 * Endpoint: GET /api/get_leaderboard.php?GradeLevel=4&Limit=10
 * Returns top students by XP, optionally filtered by grade level
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Get optional filters
$gradeLevel = isset($_GET['GradeLevel']) ? (int)$_GET['GradeLevel'] : 0;
$limit = isset($_GET['Limit']) ? (int)$_GET['Limit'] : 10;
$limit = min($limit, 100); // Cap at 100

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Build query with optional grade filter
    $sql = "SELECT TOP (:limit)
                StudentID, FirstName, LastName,
                GradeLevel, Section,
                TotalXP, CurrentStreak, LongestStreak,
                CurrentAbility,
                ROW_NUMBER() OVER (ORDER BY TotalXP DESC) as Rank
            FROM Students
            WHERE IsActive = 1";

    if ($gradeLevel > 0) {
        $sql .= " AND GradeLevel = :gradeLevel";
    }

    $sql .= " ORDER BY TotalXP DESC, CurrentStreak DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':limit', $limit, PDO::PARAM_INT);

    if ($gradeLevel > 0) {
        $stmt->bindParam(':gradeLevel', $gradeLevel, PDO::PARAM_INT);
    }

    $stmt->execute();
    $students = $stmt->fetchAll();

    $response = [];
    $rank = 1;
    foreach ($students as $student) {
        $response[] = [
            'Rank' => $rank++,
            'StudentID' => (int)$student['StudentID'],
            'FullName' => $student['FirstName'] . ' ' . $student['LastName'],
            'FirstName' => $student['FirstName'],
            'GradeLevel' => (int)$student['GradeLevel'],
            'Section' => $student['Section'],
            'TotalXP' => (int)$student['TotalXP'],
            'CurrentStreak' => (int)$student['CurrentStreak'],
            'LongestStreak' => (int)$student['LongestStreak'],
            'AbilityScore' => (float)$student['CurrentAbility']
        ];
    }

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'filter' => [
            'GradeLevel' => $gradeLevel > 0 ? $gradeLevel : 'All',
            'Limit' => $limit
        ],
        'count' => count($response),
        'leaderboard' => $response
    ]);

} catch (Exception $e) {
    error_log("Get leaderboard error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve leaderboard']);
}
