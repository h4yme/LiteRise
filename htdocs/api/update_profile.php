<?php
/**
 * LiteRise - Update Student Profile API
 * Endpoint: POST /api/update_profile.php
 * Allows students to update their profile information
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

if (!isset($input['StudentID'])) {
    http_response_code(400);
    echo json_encode(['error' => 'StudentID required']);
    exit();
}

$studentId = (int)$input['StudentID'];

// Allowed fields to update
$allowedFields = ['FirstName', 'LastName', 'Email', 'Section'];
$updates = [];
$params = [':studentId' => $studentId];

foreach ($allowedFields as $field) {
    if (isset($input[$field]) && !empty($input[$field])) {
        $updates[] = "$field = :$field";
        $params[":$field"] = trim($input[$field]);
    }
}

if (empty($updates)) {
    http_response_code(400);
    echo json_encode(['error' => 'No valid fields to update']);
    exit();
}

// Email validation
if (isset($input['Email'])) {
    if (!filter_var($input['Email'], FILTER_VALIDATE_EMAIL)) {
        http_response_code(400);
        echo json_encode(['error' => 'Invalid email format']);
        exit();
    }
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Check if student exists
    $checkSql = "SELECT StudentID FROM Students WHERE StudentID = :id AND IsActive = 1";
    $checkStmt = $conn->prepare($checkSql);
    $checkStmt->bindParam(':id', $studentId, PDO::PARAM_INT);
    $checkStmt->execute();

    if (!$checkStmt->fetch()) {
        http_response_code(404);
        echo json_encode(['error' => 'Student not found']);
        exit();
    }

    // If email is being updated, check if it's already in use
    if (isset($input['Email'])) {
        $emailCheckSql = "SELECT StudentID FROM Students
                          WHERE Email = :email AND StudentID != :id";
        $emailStmt = $conn->prepare($emailCheckSql);
        $emailStmt->bindParam(':email', $input['Email']);
        $emailStmt->bindParam(':id', $studentId, PDO::PARAM_INT);
        $emailStmt->execute();

        if ($emailStmt->fetch()) {
            http_response_code(409);
            echo json_encode(['error' => 'Email already in use']);
            exit();
        }
    }

    // Build and execute update query
    $updateSql = "UPDATE Students SET " . implode(', ', $updates) .
                 " WHERE StudentID = :studentId";

    $stmt = $conn->prepare($updateSql);
    $stmt->execute($params);

    // Get updated student data
    $selectSql = "SELECT StudentID, FirstName, LastName, Email,
                         GradeLevel, Section, CurrentAbility, TotalXP
                  FROM Students WHERE StudentID = :id";
    $selectStmt = $conn->prepare($selectSql);
    $selectStmt->bindParam(':id', $studentId, PDO::PARAM_INT);
    $selectStmt->execute();
    $student = $selectStmt->fetch();

    $response = [
        'success' => true,
        'message' => 'Profile updated successfully',
        'student' => [
            'StudentID' => (int)$student['StudentID'],
            'FullName' => $student['FirstName'] . ' ' . $student['LastName'],
            'FirstName' => $student['FirstName'],
            'LastName' => $student['LastName'],
            'Email' => $student['Email'],
            'GradeLevel' => (int)$student['GradeLevel'],
            'Section' => $student['Section'],
            'AbilityScore' => (float)$student['CurrentAbility'],
            'XP' => (int)$student['TotalXP']
        ]
    ];

    http_response_code(200);
    echo json_encode($response);

} catch (Exception $e) {
    error_log("Update profile error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to update profile']);
}
