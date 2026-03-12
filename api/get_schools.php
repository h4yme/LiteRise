<?php
// ============================================================
// get_schools.php  GET
// Returns all schools.
// Used by: ApiService.GetSchoolsAsync()
//
// Response: [
//   { school_id, school_code, school_name, barangay, student_count, is_active }
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
            sc.SchoolID                                         AS school_id,
            ISNULL(sc.SchoolCode, 'SCH' + RIGHT('000' + CAST(sc.SchoolID AS VARCHAR), 3)) AS school_code,
            sc.SchoolName                                       AS school_name,
            ISNULL(sc.Barangay, '')                            AS barangay,
            COUNT(s.StudentID)                                  AS student_count,
            CAST(ISNULL(sc.IsActive, 1) AS BIT)               AS is_active
        FROM  dbo.Schools sc
        LEFT  JOIN dbo.Students s ON s.SchoolID = sc.SchoolID
        GROUP BY sc.SchoolID, sc.SchoolCode, sc.SchoolName, sc.Barangay, sc.IsActive
        ORDER BY sc.SchoolName
    ")->fetchAll(PDO::FETCH_ASSOC);

    foreach ($rows as &$row) {
        $row['school_id']      = (int) $row['school_id'];
        $row['student_count']  = (int) $row['student_count'];
        $row['is_active']      = (bool)$row['is_active'];
    }
    unset($row);

    echo json_encode($rows, JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
