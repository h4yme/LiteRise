<?php
// ============================================================
// get_schools.php  GET
// Returns all active schools.
// Used by: ApiService.GetSchoolsAsync()
//
// Response: [
//   { school_id, school_name, district, address, city, province,
//     is_active, date_created }
// ]
// ============================================================
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');

require_once __DIR__ . '/../src/auth.php';
require_once __DIR__ . '/../src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

$auth = verifyToken();
if (!$auth) {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    exit;
}

try {
    $pdo = getConnection();

    $stmt = $pdo->query("
        SELECT
            sc.SchoolID                                        AS school_id,
            sc.SchoolName                                      AS school_name,
            ISNULL(sc.District,  '')                          AS district,
            ISNULL(sc.Address,   '')                          AS address,
            ISNULL(sc.City,      '')                          AS city,
            ISNULL(sc.Province,  '')                          AS province,
            CAST(ISNULL(sc.IsActive, 1) AS BIT)              AS is_active,
            CONVERT(VARCHAR(19), sc.DateCreated, 120)         AS date_created
        FROM  dbo.Schools sc
        WHERE sc.IsActive = 1
        ORDER BY sc.SchoolName
    ");

    if ($stmt === false) {
        throw new \RuntimeException('Query failed: ' . implode(', ', $pdo->errorInfo()));
    }

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
}
