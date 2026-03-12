<?php
// ============================================================
// update_school.php  POST  (admin only)
// Updates an existing school record.
// Body: { school_id: int, school_name: string, barangay: string }
// Response: { success, message }
// ============================================================
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');

require_once __DIR__ . '/../src/auth.php';
require_once __DIR__ . '/../src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

$auth = verifyToken();
if (!$auth || strtolower($auth['role'] ?? '') !== 'admin') {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    exit;
}

$body       = json_decode(file_get_contents('php://input'), true) ?? [];
$schoolId   = (int)($body['school_id']   ?? 0);
$schoolName = trim($body['school_name']  ?? '');
$barangay   = trim($body['barangay']     ?? '');

if ($schoolId <= 0 || !$schoolName) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'school_id and school_name are required.']);
    exit;
}

try {
    $pdo  = getConnection();
    $stmt = $pdo->prepare("
        UPDATE dbo.Schools SET SchoolName = ?, Barangay = ? WHERE SchoolID = ?
    ");
    $stmt->execute([$schoolName, $barangay, $schoolId]);

    if ($stmt->rowCount() === 0) {
        echo json_encode(['success' => false, 'message' => 'School not found.']);
    } else {
        echo json_encode(['success' => true, 'message' => 'School updated.']);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
