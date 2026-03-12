<?php
// ============================================================
// get_schools.php  GET
// Returns all active schools.
// Used by: ApiService.GetSchoolsAsync()
//
// Response: [
//   { school_id, school_name, district, address, city, province,
//     student_count, is_active, date_created }
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

    $rows = $pdo->query("
        SELECT
            sc.SchoolID                                        AS school_id,
            sc.SchoolName                                      AS school_name,
            ISNULL(sc.District,  '')                          AS district,
            ISNULL(sc.Address,   '')                          AS address,
            ISNULL(sc.City,      '')                          AS city,
            ISNULL(sc.Province,  '')                          AS province,
            COUNT(s.StudentID)                                 AS student_count,
            CAST(ISNULL(sc.IsActive, 1) AS BIT)              AS is_active,
            CONVERT(VARCHAR(19), sc.DateCreated, 120)         AS date_created
        FROM  dbo.Schools sc
        LEFT  JOIN dbo.Students s ON s.SchoolID = sc.SchoolID
        WHERE sc.IsActive = 1
        GROUP BY sc.SchoolID, sc.SchoolName, sc.District, sc.Address,
                 sc.City, sc.Province, sc.IsActive, sc.DateCreated
        ORDER BY sc.SchoolName
    ")->fetchAll(PDO::FETCH_ASSOC);

    foreach ($rows as &$row) {
        $row['school_id']     = (int)$row['school_id'];
        $row['student_count'] = (int)$row['student_count'];
        $row['is_active']     = (bool)$row['is_active'];
    }
    unset($row);

    echo json_encode($rows, JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
