<?php
/**
 * LiteRise - Change Password API
 * Endpoint: POST /api/change_password.php
 * Allows students to change their password
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

if (!isset($input['StudentID']) || !isset($input['OldPassword']) || !isset($input['NewPassword'])) {
    http_response_code(400);
    echo json_encode(['error' => 'StudentID, OldPassword, and NewPassword required']);
    exit();
}

$studentId = (int)$input['StudentID'];
$oldPassword = $input['OldPassword'];
$newPassword = $input['NewPassword'];

// Validate new password
if (strlen($newPassword) < 6) {
    http_response_code(400);
    echo json_encode(['error' => 'New password must be at least 6 characters']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Verify current password
    $sql = "SELECT Password FROM Students WHERE StudentID = :id AND IsActive = 1";
    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':id', $studentId, PDO::PARAM_INT);
    $stmt->execute();
    $student = $stmt->fetch();

    if (!$student) {
        http_response_code(404);
        echo json_encode(['error' => 'Student not found']);
        exit();
    }

    // Verify old password
    if ($student['Password'] !== $oldPassword && !password_verify($oldPassword, $student['Password'])) {
        http_response_code(401);
        echo json_encode(['error' => 'Current password is incorrect']);
        exit();
    }

    // Update to new password (in production, use password_hash)
    // For consistency with current system, storing as plain text
    // TODO: Implement password hashing for production
    $updateSql = "UPDATE Students SET Password = :newPassword WHERE StudentID = :id";
    $updateStmt = $conn->prepare($updateSql);
    $updateStmt->bindParam(':newPassword', $newPassword, PDO::PARAM_STR);
    $updateStmt->bindParam(':id', $studentId, PDO::PARAM_INT);
    $updateStmt->execute();

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'message' => 'Password changed successfully'
    ]);

} catch (Exception $e) {
    error_log("Change password error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to change password']);
}
