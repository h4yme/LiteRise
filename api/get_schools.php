<?php
/**
 * get_schools.php
 * Returns all active schools.
 *
 * GET /api/get_schools.php
 *
 * Requires: Bearer JWT (portal token)
 *
 * Response: JSON array of school objects
 */
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

requireAuth();

try {
    $stmt = $conn->query("
        SELECT
            SchoolID                                    AS school_id,
            SchoolName                                  AS school_name,
            ISNULL(District,  '')                       AS district,
            ISNULL(Address,   '')                       AS address,
            ISNULL(City,      '')                       AS city,
            ISNULL(Province,  '')                       AS province,
            CAST(ISNULL(IsActive, 1) AS BIT)            AS is_active,
            CONVERT(VARCHAR(19), DateCreated, 120)       AS date_created
        FROM dbo.Schools
        WHERE ISNULL(IsActive, 1) = 1
        ORDER BY SchoolName
    ");

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    foreach ($rows as &$row) {
        $row['school_id'] = (int)$row['school_id'];
        $row['is_active'] = (bool)$row['is_active'];
    }
    unset($row);

    echo json_encode($rows, JSON_UNESCAPED_UNICODE);

} catch (\Throwable $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
    error_log('get_schools error: ' . $e->getMessage());
}
