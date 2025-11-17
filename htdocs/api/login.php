<?php
/**
 * LiteRise - Student Login API
 * Endpoint: POST /api/login.php
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Only allow POST
if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit();
}

// Get JSON input
$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['email']) || !isset($input['password'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Email and password required']);
    exit();
}

$email = trim($input['email']);
$password = $input['password'];

// Validate input
if (empty($email) || empty($password)) {
    http_response_code(400);
    echo json_encode(['error' => 'Email and password cannot be empty']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Query student by email
    $sql = "SELECT StudentID, FirstName, LastName, Email, Password, GradeLevel,
                   Section, CurrentAbility, TotalXP, CurrentStreak, LongestStreak, IsActive
            FROM Students
            WHERE Email = :email AND IsActive = 1";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':email', $email);
    $stmt->execute();

    $student = $stmt->fetch();

    if (!$student) {
        http_response_code(401);
        echo json_encode(['error' => 'Invalid credentials']);
        exit();
    }

    // Verify password
    // Note: In production, use password_hash() when creating accounts
    // and password_verify() here
    if ($student['Password'] !== $password && !password_verify($password, $student['Password'])) {
        http_response_code(401);
        echo json_encode(['error' => 'Invalid credentials']);
        exit();
    }

    // Update last login
    $updateSql = "UPDATE Students SET LastLogin = GETDATE() WHERE StudentID = :id";
    $updateStmt = $conn->prepare($updateSql);
    $updateStmt->bindParam(':id', $student['StudentID']);
    $updateStmt->execute();

    // Return student data (exclude password)
    $response = [
        'StudentID' => (int)$student['StudentID'],
        'FullName' => $student['FirstName'] . ' ' . $student['LastName'],
        'FirstName' => $student['FirstName'],
        'LastName' => $student['LastName'],
        'email' => $student['Email'],
        'GradeLevel' => (int)$student['GradeLevel'],
        'Section' => $student['Section'],
        'AbilityScore' => (float)$student['CurrentAbility'],
        'XP' => (int)$student['TotalXP'],
        'CurrentStreak' => (int)$student['CurrentStreak'],
        'LongestStreak' => (int)$student['LongestStreak']
    ];

    http_response_code(200);
    echo json_encode($response);

} catch (Exception $e) {
    error_log("Login error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Server error occurred']);
}
