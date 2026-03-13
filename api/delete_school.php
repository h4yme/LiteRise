<?php
/**
 * delete_school.php
 * Soft-deletes a school (sets IsActive = 0).
 *
 * POST /api/delete_school.php
 *
 * Requires: Bearer JWT (portal token)
 * Body: { school_id }
 */
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

requireAuth();

$body     = json_decode(file_get_contents('php://input'), true) ?? [];
$schoolId = (int)($body['school_id'] ?? $body['id'] ?? 0);

if ($schoolId <= 0) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'school_id is required.']);
    exit;
}

try {
    $del = $conn->prepare("UPDATE dbo.Schools SET IsActive = 0 WHERE SchoolID = ?");
    $del->execute([$schoolId]);

    echo json_encode(['success' => true, 'message' => 'School deleted.']);

} catch (\Throwable $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
    error_log('delete_school error: ' . $e->getMessage());
}
