<?php
/**
 * Debug endpoint to check Students table schema
 */

header("Content-Type: application/json");

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

try {
    // Require authentication
    $authUser = requireAuth();

    // Get column information for Students table
    $stmt = $conn->prepare("
        SELECT
            COLUMN_NAME,
            DATA_TYPE,
            CHARACTER_MAXIMUM_LENGTH,
            IS_NULLABLE,
            COLUMN_DEFAULT
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_NAME = 'Students'
        ORDER BY ORDINAL_POSITION
    ");
    $stmt->execute();
    $columns = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Get a sample student record
    $stmt = $conn->prepare("SELECT TOP 1 * FROM Students WHERE StudentID = ?");
    $stmt->execute([$authUser['studentID']]);
    $sampleStudent = $stmt->fetch(PDO::FETCH_ASSOC);

    echo json_encode([
        'message' => 'Students table schema and sample data',
        'columns' => $columns,
        'sample_student' => $sampleStudent,
        'has_current_ability_column' => in_array('CurrentAbility', array_column($columns, 'COLUMN_NAME'))
    ], JSON_PRETTY_PRINT);

} catch (Exception $e) {
    echo json_encode([
        'error' => $e->getMessage(),
        'trace' => $e->getTraceAsString()
    ], JSON_PRETTY_PRINT);
}
?>
