<?php
header('Content-Type: application/json');
require_once __DIR__ . '/src/db.php';

$data = json_decode(file_get_contents('php://input'), true);
$studentID = $data['StudentID'] ?? 0;
$responses = $data['Responses'] ?? [];

if (!$studentID || empty($responses)) {
    echo json_encode(["error" => "Missing StudentID or Responses"]);
    exit;
}

try {
    // 1️⃣ Create Session
    $stmt = $conn->prepare("EXEC SP_CreateTestSession @StudentID = ?, @Type = ?");
    $stmt->execute([$studentID, 'PreAssessment']);
    $session = $stmt->fetch(PDO::FETCH_ASSOC);
    $sessionID = $session['SessionID'];

    // 2️⃣ Save Responses
    $jsonData = json_encode($responses);
    $stmt = $conn->prepare("EXEC SP_SaveResponses @SessionID = ?, @Responses = ?");
    $stmt->execute([$sessionID, $jsonData]);

    // 3️⃣ Update ability (placeholder)
    $stmt = $conn->prepare("EXEC SP_UpdateStudentAbility @StudentID = ?");
    $stmt->execute([$studentID]);

    echo json_encode(["status" => "success", "session_id" => $sessionID]);
} catch (PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>
