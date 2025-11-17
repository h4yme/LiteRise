<?php
header('Content-Type: application/json');
require_once __DIR__ . '/src/db.php';

$data = json_decode(file_get_contents('php://input'), true);
$studentID = $data['student_id'] ?? 0;
$type = $data['type'] ?? 'PreAssessment';

if ($studentID == 0) {
    echo json_encode(["error" => "Missing student_id"]);
    exit;
}

try {
    $stmt = $conn->prepare("EXEC SP_CreateTestSession @StudentID = ?, @Type = ?");
    $stmt->execute([$studentID, $type]);
    $session = $stmt->fetch(PDO::FETCH_ASSOC);
    echo json_encode(["status" => "success", "session" => $session]);
} catch (PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>
