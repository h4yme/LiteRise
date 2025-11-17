<?php
/**
 * Debug endpoint to test if Students.CurrentAbility can be updated
 */

header("Content-Type: application/json");

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

try {
    // Require authentication
    $authUser = requireAuth();
    $studentID = $authUser['studentID'];

    // Get current ability
    $stmt = $conn->prepare("SELECT StudentID, FirstName, LastName, CurrentAbility FROM Students WHERE StudentID = ?");
    $stmt->execute([$studentID]);
    $beforeUpdate = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$beforeUpdate) {
        echo json_encode(['error' => 'Student not found']);
        exit;
    }

    // Try to update ability
    $testAbility = 1.25; // Test value
    $stmt = $conn->prepare("UPDATE Students SET CurrentAbility = ? WHERE StudentID = ?");
    $updateResult = $stmt->execute([$testAbility, $studentID]);
    $rowsAffected = $stmt->rowCount();

    // Get updated ability
    $stmt = $conn->prepare("SELECT StudentID, FirstName, LastName, CurrentAbility FROM Students WHERE StudentID = ?");
    $stmt->execute([$studentID]);
    $afterUpdate = $stmt->fetch(PDO::FETCH_ASSOC);

    echo json_encode([
        'test' => 'Students.CurrentAbility UPDATE test',
        'student_id' => $studentID,
        'before_update' => $beforeUpdate,
        'update_executed' => $updateResult ? 'true' : 'false',
        'rows_affected' => $rowsAffected,
        'after_update' => $afterUpdate,
        'success' => $afterUpdate['CurrentAbility'] == $testAbility
    ], JSON_PRETTY_PRINT);

} catch (Exception $e) {
    echo json_encode([
        'error' => $e->getMessage(),
        'trace' => $e->getTraceAsString()
    ], JSON_PRETTY_PRINT);
}
?>
