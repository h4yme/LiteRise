<?php
// ============================================================
// create_school.php  POST  (admin only)
// Creates a new school record.
// Body: { school_name: string, barangay: string }
// Response: { success, school_id, school_code, school_name, barangay }
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

$body = json_decode(file_get_contents('php://input'), true) ?? [];
$schoolName = trim($body['school_name'] ?? '');
$barangay   = trim($body['barangay']    ?? '');

if (!$schoolName) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'school_name is required.']);
    exit;
}

try {
    $pdo = getConnection();

    // Generate next school code
    $maxId = (int)$pdo->query("SELECT ISNULL(MAX(SchoolID), 0) FROM dbo.Schools")->fetchColumn();
    $code  = 'SCH' . str_pad($maxId + 1, 3, '0', STR_PAD_LEFT);

    $stmt = $pdo->prepare("
        INSERT INTO dbo.Schools (SchoolCode, SchoolName, Barangay, IsActive)
        OUTPUT INSERTED.SchoolID
        VALUES (?, ?, ?, 1)
    ");
    $stmt->execute([$code, $schoolName, $barangay]);
    $newId = (int)$stmt->fetchColumn();

    echo json_encode([
        'success'     => true,
        'school_id'   => $newId,
        'school_code' => $code,
        'school_name' => $schoolName,
        'barangay'    => $barangay,
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
