<?php
/**
 * LiteRise Registration API
 * POST /api/register.php
 *
 * Request Body:
 * {
 *   "email": "student@example.com",
 *   "password": "password123",
 *   "first_name": "John",
 *   "last_name": "Doe",
 *   "grade_level": 5
 * }
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

$data = getJsonInput();

validateRequired($data, ['email', 'password', 'first_name', 'last_name']);

$email = trim($data['email']);
$password = $data['password'];
$firstName = trim($data['first_name']);
$lastName = trim($data['last_name']);
$gradeLevel = $data['grade_level'] ?? null;

// Validate email format
if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    sendError("Invalid email format", 400);
}

// Validate password length
if (strlen($password) < 6) {
    sendError("Password must be at least 6 characters", 400);
}

try {
    // Check if email already exists
    $stmt = $conn->prepare("SELECT StudentID FROM Students WHERE Email = ?");
    $stmt->execute([$email]);
    if ($stmt->fetch()) {
        sendError("Email already registered", 409);
    }

    // Hash password
    $hashedPassword = hashPassword($password);

    // Insert new student
    $stmt = $conn->prepare(
        "INSERT INTO Students (Email, Password, FirstName, LastName, GradeLevel, CurrentAbility, TotalXP, CurrentStreak, LongestStreak, RegistrationDate)
         VALUES (?, ?, ?, ?, ?, 0.0, 0, 0, 0, GETDATE())"
    );
    $stmt->execute([$email, $hashedPassword, $firstName, $lastName, $gradeLevel]);

    // Get the new student ID
    $studentID = $conn->lastInsertId();

    // Generate token
    $token = generateToken([
        'studentID' => $studentID,
        'email' => $email,
        'firstName' => $firstName
    ]);

    // Log activity
    logActivity($studentID, 'Registration', 'New student registered');

    sendResponse([
        'success' => true,
        'message' => 'Registration successful',
        'token' => $token,
        'student' => [
            'StudentID' => (int)$studentID,
            'Email' => $email,
            'FirstName' => $firstName,
            'LastName' => $lastName,
            'CurrentAbility' => 0.0,
            'TotalXP' => 0
        ]
    ], 201);

} catch (PDOException $e) {
    error_log("Registration error: " . $e->getMessage());
    sendError("Registration failed", 500);
}
?>
