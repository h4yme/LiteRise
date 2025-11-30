<?php
header("Content-Type: application/json");
header("Access-Control-Allow-Origin: *");
header("Access-Control-Allow-Methods: POST");
header("Access-Control-Allow-Headers: Content-Type");

require_once 'src/db.php';

// Get JSON input
$json = file_get_contents('php://input');
$data = json_decode($json, true);

// Validate input
if (!isset($data['StudentID']) || !isset($data['Nickname'])) {
    echo json_encode([
        'success' => false,
        'message' => 'StudentID and Nickname are required'
    ]);
    exit;
}

$studentId = (int)$data['StudentID'];
$nickname = trim($data['Nickname']);

// Validate nickname
if (empty($nickname)) {
    echo json_encode([
        'success' => false,
        'message' => 'Nickname cannot be empty'
    ]);
    exit;
}

if (strlen($nickname) > 20) {
    echo json_encode([
        'success' => false,
        'message' => 'Nickname must be 20 characters or less'
    ]);
    exit;
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    // Update nickname in Students table
    $sql = "UPDATE Students SET Nickname = ? WHERE StudentID = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("si", $nickname, $studentId);

    if ($stmt->execute()) {
        if ($stmt->affected_rows > 0) {
            echo json_encode([
                'success' => true,
                'message' => 'Nickname saved successfully',
                'nickname' => $nickname
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'message' => 'Student not found or nickname unchanged'
            ]);
        }
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Failed to save nickname: ' . $stmt->error
        ]);
    }

    $stmt->close();
    $conn->close();

} catch (Exception $e) {
    echo json_encode([
        'success' => false,
        'message' => 'Error: ' . $e->getMessage()
    ]);
}
?>
