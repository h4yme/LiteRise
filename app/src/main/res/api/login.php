<?php
require_once __DIR__ . '/src/db.php';

// Get JSON input
$data = json_decode(file_get_contents("php://input"), true);
$email = trim($data["email"] ?? '');
$password = trim($data["password"] ?? '');

if (empty($email) || empty($password)) {
    sendError("Missing email or password", 400);
}

try {
    // Call the stored procedure
    $stmt = $conn->prepare("EXEC SP_StudentLogin @Email = :email, @Password = :password");
    $stmt->bindValue(":email", $email);
    $stmt->bindValue(":password", $password);
    $stmt->execute();

    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if ($student) {
       
        $hashedPassword = $student['Password'];
        unset($student['Password']);
        
       
        
        // Generate JWT token
        $token = generateJWT($student['StudentID'], $email);
        
        // Add token to response
        $student['token'] = $token;
        
        sendResponse($student, 200);
    } else {
        sendError("Invalid credentials", 401);
    }
} catch (Exception $e) {
    sendError("Server error", 500, $e->getMessage());
}

/**
 * Generate JWT token
 */
function generateJWT($studentID, $email) {
    $secret = $_ENV['JWT_SECRET'] ?? 'default_secret_change_this';
    $issuedAt = time();
    $expire = $issuedAt + (60 * 60 * 24 * 7); // Valid for 7 days
    
    $header = json_encode(['typ' => 'JWT', 'alg' => 'HS256']);
    $payload = json_encode([
        'iat' => $issuedAt,
        'exp' => $expire,
        'studentID' => $studentID,
        'email' => $email
    ]);
    
    $base64UrlHeader = base64UrlEncode($header);
    $base64UrlPayload = base64UrlEncode($payload);
    
    $signature = hash_hmac('sha256', $base64UrlHeader . "." . $base64UrlPayload, $secret, true);
    $base64UrlSignature = base64UrlEncode($signature);
    
    $jwt = $base64UrlHeader . "." . $base64UrlPayload . "." . $base64UrlSignature;
    
    return $jwt;
}

function base64UrlEncode($data) {
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}
?>