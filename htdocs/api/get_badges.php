<?php
/**
 * LiteRise - Get All Badges API
 * Endpoint: GET /api/get_badges.php
 * Returns all available badges in the system
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

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Get all badges ordered by XP requirement
    $sql = "SELECT BadgeID, BadgeName, BadgeDescription, BadgeIcon,
                   XPRequirement, CreatedDate
            FROM Badges
            ORDER BY XPRequirement ASC";

    $stmt = $conn->query($sql);
    $badges = $stmt->fetchAll();

    $response = [];
    foreach ($badges as $badge) {
        $response[] = [
            'BadgeID' => (int)$badge['BadgeID'],
            'BadgeName' => $badge['BadgeName'],
            'Description' => $badge['BadgeDescription'],
            'Icon' => $badge['BadgeIcon'],
            'XPRequired' => (int)$badge['XPRequirement'],
            'CreatedDate' => $badge['CreatedDate']
        ];
    }

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'count' => count($response),
        'badges' => $response
    ]);

} catch (Exception $e) {
    error_log("Get badges error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve badges']);
}
