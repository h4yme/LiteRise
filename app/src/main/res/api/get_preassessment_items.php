<?php
header('Content-Type: application/json');
require_once __DIR__ . '/src/db.php';

try {
    $stmt = $conn->prepare("EXEC SP_GetPreAssessmentItems");
    $stmt->execute();

    $items = $stmt->fetchAll(PDO::FETCH_ASSOC);
    echo json_encode($items ?: []);
} catch (PDOException $e) {
    echo json_encode(["error" => $e->getMessage()]);
}
?>
