<?php
// ============================================================
// get_portal_accounts.php  GET  (admin only)
// Returns all portal admin/teacher accounts.
// Used by: AdministrationController.GetAdmins()
//
// Response: [
//   { id, name, email, role, school, school_id, lastLogin, isActive }
// ]
// ============================================================
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');

require_once __DIR__ . '/../src/auth.php';
require_once __DIR__ . '/../src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

$auth = verifyToken();
if (!$auth || strtolower($auth['role'] ?? '') !== 'admin') {
    http_response_code(401);
    echo json_encode(['success' => false, 'message' => 'Unauthorized']);
    exit;
}

try {
    $pdo = getConnection();

    $rows = $pdo->query("
        SELECT
            pu.UserID                                   AS id,
            pu.FullName                                 AS name,
            pu.Email                                    AS email,
            pu.Role                                     AS role,
            ISNULL(sc.SchoolName, '')                  AS school,
            pu.SchoolID                                AS school_id,
            CONVERT(VARCHAR(19), pu.LastLogin, 120)   AS lastLogin,
            CAST(pu.IsActive AS BIT)                   AS isActive
        FROM  dbo.PortalUsers pu
        LEFT  JOIN dbo.Schools sc ON sc.SchoolID = pu.SchoolID
        ORDER BY pu.FullName
    ")->fetchAll(PDO::FETCH_ASSOC);

    foreach ($rows as &$row) {
        $row['id']       = (int)$row['id'];
        $row['school_id']= $row['school_id'] !== null ? (int)$row['school_id'] : null;
        $row['isActive'] = (bool)$row['isActive'];
    }
    unset($row);

    echo json_encode($rows, JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
