<?php
/**
 * LiteRise Authentication Helper
 * JWT-based authentication for API endpoints
 */

require_once __DIR__ . '/db.php';

// JWT Secret Key - should be set via environment variable in production
$jwtSecret = getenv('JWT_SECRET') ?: 'literise_jwt_secret_key_change_in_production';

/**
 * Generate JWT token
 * @param array $payload Token payload
 * @return string JWT token
 */
function generateToken($payload) {
    global $jwtSecret;

    $header = base64url_encode(json_encode(['alg' => 'HS256', 'typ' => 'JWT']));

    $payload['iat'] = time();
    $payload['exp'] = time() + (7 * 24 * 60 * 60); // 7 days expiry
    $payloadEncoded = base64url_encode(json_encode($payload));

    $signature = base64url_encode(
        hash_hmac('sha256', "$header.$payloadEncoded", $jwtSecret, true)
    );

    return "$header.$payloadEncoded.$signature";
}

/**
 * Verify and decode JWT token
 * @param string $token JWT token
 * @return array|null Decoded payload or null if invalid
 */
function verifyToken($token) {
    global $jwtSecret;

    $parts = explode('.', $token);
    if (count($parts) !== 3) {
        return null;
    }

    list($header, $payload, $signature) = $parts;

    // Verify signature
    $expectedSignature = base64url_encode(
        hash_hmac('sha256', "$header.$payload", $jwtSecret, true)
    );

    if (!hash_equals($expectedSignature, $signature)) {
        return null;
    }

    $decoded = json_decode(base64url_decode($payload), true);

    // Check expiration
    if (isset($decoded['exp']) && $decoded['exp'] < time()) {
        return null;
    }

    return $decoded;
}

/**
 * URL-safe Base64 encode
 */
function base64url_encode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}

/**
 * URL-safe Base64 decode
 */
function base64url_decode($data) {
    return base64_decode(strtr($data, '-_', '+/'));
}

/**
 * Get authenticated user from request
 * @return array|null User data or null if not authenticated
 */
function getAuthUser() {
    $headers = getallheaders();
    $authHeader = $headers['Authorization'] ?? $headers['authorization'] ?? '';

    if (empty($authHeader)) {
        return null;
    }

    // Handle "Bearer <token>" format
    if (preg_match('/Bearer\s+(.*)$/i', $authHeader, $matches)) {
        $token = $matches[1];
    } else {
        $token = $authHeader;
    }

    return verifyToken($token);
}

/**
 * Require authentication - sends error if not authenticated
 * @return array Authenticated user data
 */
function requireAuth() {
    $user = getAuthUser();

    if (!$user) {
        sendError("Unauthorized - Invalid or missing token", 401);
    }

    return $user;
}

/**
 * Hash password using bcrypt
 * @param string $password Plain text password
 * @return string Hashed password
 */
function hashPassword($password) {
    return password_hash($password, PASSWORD_BCRYPT, ['cost' => 12]);
}

/**
 * Verify password against hash
 * @param string $password Plain text password
 * @param string $hash Password hash
 * @return bool True if password matches
 */
function verifyPassword($password, $hash) {
    return password_verify($password, $hash);
}
?>
