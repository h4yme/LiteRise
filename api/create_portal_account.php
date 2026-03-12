<?php
// ============================================================
// create_portal_account.php  POST  (admin only)
// Creates a new admin or teacher portal account.
// Body: { name, email, password, role, school_id? }
// Response: { success, message, id }
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
$name     = trim($body['name']      ?? '');
$email    = trim($body['email']     ?? '');
$password = $body['password']       ?? '';
$role     = trim($body['role']      ?? '');
$schoolId = isset($body['school_id']) && $body['school_id'] !== '' ? (int)$body['school_id'] : null;

if (!$name || !$email || !$password || !$role) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'name, email, password, and role are required.']);
    exit;
}

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Invalid email address.']);
    exit;
}

if (strlen($password) < 8) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Password must be at least 8 characters.']);
    exit;
}

$role = ucfirst(strtolower($role));
if (!in_array($role, ['Admin', 'Teacher'])) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'Role must be Admin or Teacher.']);
    exit;
}

try {
    $pdo = getConnection();

    // Check email uniqueness
    $check = $pdo->prepare("SELECT COUNT(*) FROM dbo.PortalUsers WHERE Email = ?");
    $check->execute([$email]);
    if ((int)$check->fetchColumn() > 0) {
        http_response_code(409);
        echo json_encode(['success' => false, 'message' => 'An account with this email already exists.']);
        exit;
    }

    $hash = password_hash($password, PASSWORD_BCRYPT);

    $stmt = $pdo->prepare("
        INSERT INTO dbo.PortalUsers (FullName, Email, PasswordHash, Role, SchoolID, IsActive, CreatedAt)
        OUTPUT INSERTED.UserID
        VALUES (?, ?, ?, ?, ?, 1, GETUTCDATE())
    ");
    $stmt->execute([$name, $email, $hash, $role, $schoolId]);
    $newId = (int)$stmt->fetchColumn();

    echo json_encode([
        'success' => true,
        'message' => 'Account created successfully.',
        'id'      => $newId,
    ]);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
