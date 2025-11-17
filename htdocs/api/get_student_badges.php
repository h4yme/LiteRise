<?php
/**
 * LiteRise - Get Student Badges API
 * Endpoint: GET /api/get_student_badges.php?StudentID=1
 * Returns all badges earned by a specific student
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

// Get StudentID from query string or POST body
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $studentId = isset($_GET['StudentID']) ? (int)$_GET['StudentID'] : 0;
} else {
    $input = json_decode(file_get_contents('php://input'), true);
    $studentId = isset($input['StudentID']) ? (int)$input['StudentID'] : 0;
}

if ($studentId <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'Valid StudentID required']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Get student's earned badges with badge details
    $sql = "SELECT sb.UnlockID, sb.BadgeID, sb.UnlockedDate,
                   b.BadgeName, b.BadgeDescription, b.BadgeIcon, b.XPRequirement
            FROM StudentBadges sb
            INNER JOIN Badges b ON sb.BadgeID = b.BadgeID
            WHERE sb.StudentID = :studentId
            ORDER BY sb.UnlockedDate DESC";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $stmt->execute();
    $badges = $stmt->fetchAll();

    $response = [];
    foreach ($badges as $badge) {
        $response[] = [
            'UnlockID' => (int)$badge['UnlockID'],
            'BadgeID' => (int)$badge['BadgeID'],
            'BadgeName' => $badge['BadgeName'],
            'Description' => $badge['BadgeDescription'],
            'Icon' => $badge['BadgeIcon'],
            'XPRequired' => (int)$badge['XPRequirement'],
            'UnlockedDate' => $badge['UnlockedDate']
        ];
    }

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'StudentID' => $studentId,
        'count' => count($response),
        'badges' => $response
    ]);

} catch (Exception $e) {
    error_log("Get student badges error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve student badges']);
}
