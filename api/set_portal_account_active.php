<?php
// ============================================================
// set_portal_account_active.php  POST  (admin only)
// Activates or deactivates a portal account.
// Body: { id, is_active: bool }
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

$body     = json_decode(file_get_contents('php://input'), true) ?? [];
$id       = (int)($body['id']        ?? 0);
$isActive = isset($body['is_active']) ? (bool)$body['is_active'] : null;

if ($id <= 0 || $isActive === null) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'id and is_active are required.']);
    exit;
}

try {
    $pdo  = getConnection();
    $stmt = $pdo->prepare("UPDATE dbo.PortalUsers SET IsActive = ? WHERE UserID = ?");
    $stmt->execute([$isActive ? 1 : 0, $id]);

    if ($stmt->rowCount() === 0) {
        echo json_encode(['success' => false, 'message' => 'Account not found.']);
    } else {
        $msg = $isActive ? 'Account reactivated.' : 'Account deactivated.';
        echo json_encode(['success' => true, 'message' => $msg]);
    }

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
