<?php
/**
 * LiteRise - Update Student Ability API
 * Endpoint: POST /api/update_ability.php
 * Manually updates student ability (theta)
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

if (!isset($input['StudentID']) || !isset($input['AbilityScore'])) {
    http_response_code(400);
    echo json_encode(['error' => 'StudentID and AbilityScore required']);
    exit();
}

$studentId = (int)$input['StudentID'];
$newTheta = (float)$input['AbilityScore'];

// Validate theta range (-4 to 4 is typical)
if ($newTheta < -5 || $newTheta > 5) {
    http_response_code(400);
    echo json_encode(['error' => 'Ability score out of valid range']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Call stored procedure
    $sql = "EXEC SP_UpdateStudentAbility @StudentID = :studentId, @NewTheta = :theta";
    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $stmt->bindParam(':theta', $newTheta, PDO::PARAM_STR);
    $stmt->execute();

    $result = $stmt->fetch();

    http_response_code(200);
    echo json_encode([
        'success' => true,
        'StudentID' => $studentId,
        'CurrentAbility' => (float)$result['CurrentAbility']
    ]);

} catch (Exception $e) {
    error_log("Update ability error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to update ability']);
}
