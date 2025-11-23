<?php
/**
 * LiteRise Login API
 * POST /api/login.php
 *
 * Request Body:
 * {
 *   "email": "student@example.com",
 *   "password": "password123"
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "message": "Login successful",
 *   "token": "jwt_token_here",
 *   "student": { ... student data ... }
 * }
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

$data = getJsonInput();
$email = $data['email'] ?? '';
$password = $data['password'] ?? '';

validateRequired($data, ['email', 'password']);

try {
    // Find student by email
    $stmt = $conn->prepare(
        "SELECT StudentID, FirstName, LastName, Email, Password, CurrentAbility,
                TotalXP, CurrentStreak, LongestStreak
         FROM Students
         WHERE Email = ?"
    );
    $stmt->execute([$email]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        sendError("Invalid email or password", 401);
    }

    // Verify password
    if (!verifyPassword($password, $student['Password'])) {
        sendError("Invalid email or password", 401);
    }

    // Generate JWT token
    $token = generateToken([
        'studentID' => $student['StudentID'],
        'email' => $student['Email'],
        'firstName' => $student['FirstName']
    ]);

    // Remove password from response
    unset($student['Password']);

    // Log activity
    logActivity($student['StudentID'], 'Login', 'Student logged in successfully');

    sendResponse([
        'success' => true,
        'message' => 'Login successful',
        'token' => $token,
        'student' => $student
    ], 200);

} catch (PDOException $e) {
    error_log("Login error: " . $e->getMessage());
    sendError("Login failed", 500);
}
?>
