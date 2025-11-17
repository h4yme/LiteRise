<?php
/**
 * LiteRise - Teacher Login API
 * Endpoint: POST /api/teacher_login.php
 * Authenticates teachers for dashboard access
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode(['error' => 'Method not allowed']);
    exit();
}

$input = json_decode(file_get_contents('php://input'), true);

if (!isset($input['email']) || !isset($input['password'])) {
    http_response_code(400);
    echo json_encode(['error' => 'Email and password required']);
    exit();
}

$email = trim($input['email']);
$password = $input['password'];

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

    // Query teacher by email
    $sql = "SELECT TeacherID, FirstName, LastName, Email, Password,
                   Department, IsActive
            FROM Teachers
            WHERE Email = :email AND IsActive = 1";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':email', $email);
    $stmt->execute();

    $teacher = $stmt->fetch();

    if (!$teacher) {
        http_response_code(401);
        echo json_encode(['error' => 'Invalid credentials']);
        exit();
    }

    // Verify password
    if ($teacher['Password'] !== $password && !password_verify($password, $teacher['Password'])) {
        http_response_code(401);
        echo json_encode(['error' => 'Invalid credentials']);
        exit();
    }

    // Get teacher's student count
    $countSql = "SELECT COUNT(*) as StudentCount
                 FROM Students
                 WHERE TeacherID = :teacherId AND IsActive = 1";
    $countStmt = $conn->prepare($countSql);
    $countStmt->bindParam(':teacherId', $teacher['TeacherID']);
    $countStmt->execute();
    $countResult = $countStmt->fetch();

    // Return teacher data (exclude password)
    $response = [
        'TeacherID' => (int)$teacher['TeacherID'],
        'FullName' => $teacher['FirstName'] . ' ' . $teacher['LastName'],
        'FirstName' => $teacher['FirstName'],
        'LastName' => $teacher['LastName'],
        'email' => $teacher['Email'],
        'Department' => $teacher['Department'],
        'StudentCount' => (int)$countResult['StudentCount']
    ];

    http_response_code(200);
    echo json_encode($response);

} catch (Exception $e) {
    error_log("Teacher login error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Server error occurred']);
}
