<?php
/**
 * LiteRise Portal Login API
 * POST /api/portal_login.php
 * Body : { "email": "...", "password": "..." }
 * Returns: {
 *   "success": true,
 *   "admin_id": 1,
 *   "name": "System Admin",
 *   "email": "admin@literise.com",
 *   "role": "Admin",          // "Admin" | "Teacher"
 *   "school_id": null,
 *   "token": "<jwt>"
 * }
 */

require_once 'src/db.php';
require_once 'src/auth.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    sendError("Method not allowed", 405);
}

$input    = getJsonInput();
$email    = trim($input['email']    ?? '');
$password = trim($input['password'] ?? '');

if (!$email || !$password) {
    sendError("Email and password are required", 400);
}

if (!isValidEmail($email)) {
    sendError("Invalid email format", 400);
}

// ── Look up the administrator ─────────────────────────────────
$stmt = $conn->prepare("EXEC SP_AdminLogin @Email = ?");
$stmt->execute([$email]);
$admin = $stmt->fetch(PDO::FETCH_ASSOC);

if (!$admin || !password_verify($password, $admin['PasswordHash'])) {
    sendError("Invalid email or password", 401);
}

// ── Stamp last login (non-fatal) ─────────────────────────────
try {
    $s2 = $conn->prepare("EXEC SP_UpdateAdminLastLogin @AdminID = ?");
    $s2->execute([$admin['AdminID']]);
} catch (Exception $e) {
    error_log("Portal: last-login update failed — " . $e->getMessage());
}

// ── Build JWT with role claim ─────────────────────────────────
$secret  = ($_ENV['JWT_SECRET'] ?? getenv('JWT_SECRET')) ?: 'default_secret_change_this';
$now     = time();
$expires = $now + (60 * 60 * 24 * 7); // 7 days

$header  = base64UrlEncode(json_encode(['typ' => 'JWT', 'alg' => 'HS256']));
$payload = base64UrlEncode(json_encode([
    'iat'     => $now,
    'exp'     => $expires,
    'adminID' => $admin['AdminID'],
    'email'   => $admin['Email'],
    'role'    => $admin['Role'],
]));
$sig   = base64UrlEncode(hash_hmac('sha256', "$header.$payload", $secret, true));
$token = "$header.$payload.$sig";

sendResponse([
    'admin_id'  => $admin['AdminID'],
    'name'      => trim($admin['FirstName'] . ' ' . $admin['LastName']),
    'email'     => $admin['Email'],
    'role'      => $admin['Role'],
    'school_id' => $admin['SchoolID'],
    'token'     => $token,
]);
