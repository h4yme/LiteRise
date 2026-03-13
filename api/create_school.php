<?php
/**
 * create_school.php
 * Creates a new school record.
 *
 * POST /api/create_school.php
 *
 * Requires: Bearer JWT (portal token)
 * Body: { school_name, district, address, city, province }
 */
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

requireAuth();

$body       = json_decode(file_get_contents('php://input'), true) ?? [];
$schoolName = trim($body['school_name'] ?? '');
$district   = trim($body['district']    ?? '');
$address    = trim($body['address']     ?? '');
$city       = trim($body['city']        ?? '');
$province   = trim($body['province']    ?? '');

if (!$schoolName) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'school_name is required.']);
    exit;
}

try {
    $stmt = $conn->prepare("
        INSERT INTO dbo.Schools (SchoolName, District, Address, City, Province, IsActive, DateCreated)
        OUTPUT INSERTED.SchoolID
        VALUES (?, ?, ?, ?, ?, 1, GETUTCDATE())
    ");
    $stmt->execute([$schoolName, $district, $address, $city, $province]);
    $newId = (int)$stmt->fetchColumn();

    echo json_encode([
        'success'     => true,
        'school_id'   => $newId,
        'school_name' => $schoolName,
        'district'    => $district,
        'city'        => $city,
        'province'    => $province,
    ], JSON_UNESCAPED_UNICODE);

} catch (\Throwable $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
    error_log('create_school error: ' . $e->getMessage());
}
