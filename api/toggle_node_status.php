<?php
// ============================================================
// toggle_node_status.php  POST  application/json
// Body: { "node_id": int, "enabled": bool }
// Admin only — enables or disables a curriculum node.
// Used by: MasterfileController.ToggleNode()
//
// Response: { "success": true, "message": string, "node_id": int, "is_active": bool }
// ============================================================
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['success' => false, 'message' => 'Method not allowed']);
    exit;
}

requireAuth();

$body    = json_decode(file_get_contents('php://input'), true) ?? [];
$nodeId  = isset($body['node_id'])  ? (int)$body['node_id']      : null;
$enabled = isset($body['enabled'])  ? (bool)$body['enabled']     : null;

if (!$nodeId || $enabled === null) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'node_id and enabled are required']);
    exit;
}

try {
    // Verify node exists
    $stmt = $conn->prepare('SELECT NodeID FROM dbo.Nodes WHERE NodeID = ?');
    $stmt->execute([$nodeId]);
    if (!$stmt->fetch()) {
        http_response_code(404);
        echo json_encode(['success' => false, 'message' => 'Node not found']);
        exit;
    }

    $isActive = $enabled ? 1 : 0;
    $upd = $conn->prepare('UPDATE dbo.Nodes SET IsActive = ? WHERE NodeID = ?');
    $upd->execute([$isActive, $nodeId]);

    $action = $enabled ? 'enabled' : 'disabled';
    echo json_encode([
        'success'   => true,
        'message'   => "Node {$nodeId} {$action} successfully",
        'node_id'   => $nodeId,
        'is_active' => $enabled,
    ]);

} catch (\Throwable $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
