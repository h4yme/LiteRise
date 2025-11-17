<?php
/**
 * LiteRise - Database Connection Test
 * Endpoint: GET /api/test_db.php
 * Tests database connectivity and basic queries
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');

require_once 'src/db.php';

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Failed to connect to database');
    }

    // Test basic query
    $stmt = $conn->query("SELECT COUNT(*) as StudentCount FROM Students");
    $result = $stmt->fetch();

    $itemStmt = $conn->query("SELECT COUNT(*) as ItemCount FROM Items");
    $itemResult = $itemStmt->fetch();

    $response = [
        'status' => 'success',
        'message' => 'Database connection successful',
        'database' => 'LiteRiseDB',
        'statistics' => [
            'TotalStudents' => (int)$result['StudentCount'],
            'TotalItems' => (int)$itemResult['ItemCount']
        ],
        'timestamp' => date('Y-m-d H:i:s')
    ];

    http_response_code(200);
    echo json_encode($response, JSON_PRETTY_PRINT);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => 'Database connection failed',
        'error' => $e->getMessage()
    ], JSON_PRETTY_PRINT);
}
