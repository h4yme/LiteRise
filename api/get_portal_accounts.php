<?php
// ============================================================
// get_portal_accounts.php  GET  (admin only)
// UNIONs dbo.Admins and dbo.Teachers into one list.
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

    // ── Admins ────────────────────────────────────────────────────────────
    $admins = $pdo->query("
        SELECT
            'admin_' + CAST(AdminID AS VARCHAR)              AS id,
            AdminID                                          AS raw_id,
            ISNULL(Username, Email)                          AS name,
            Email                                            AS email,
            'Admin'                                          AS role,
            ''                                               AS school,
            NULL                                             AS school_id,
            CONVERT(VARCHAR(19), LastLoginDate, 120)         AS lastLogin,
            CAST(IsActive AS BIT)                            AS isActive
        FROM dbo.Admins
    ")->fetchAll(PDO::FETCH_ASSOC);

    // ── Teachers ──────────────────────────────────────────────────────────
    $teachers = $pdo->query("
        SELECT
            'teacher_' + CAST(t.TeacherID AS VARCHAR)        AS id,
            t.TeacherID                                       AS raw_id,
            RTRIM(ISNULL(t.FirstName,'') + ' ' + ISNULL(t.LastName,'')) AS name,
            t.Email                                           AS email,
            'Teacher'                                         AS role,
            ISNULL(sc.SchoolName, '')                        AS school,
            t.SchoolID                                       AS school_id,
            CONVERT(VARCHAR(19), t.LastLoginDate, 120)       AS lastLogin,
            CAST(t.IsActive AS BIT)                          AS isActive
        FROM dbo.Teachers t
        LEFT JOIN dbo.Schools sc ON sc.SchoolID = t.SchoolID
    ")->fetchAll(PDO::FETCH_ASSOC);

    $all = array_merge($admins, $teachers);

    foreach ($all as &$row) {
        $row['raw_id']   = (int)$row['raw_id'];
        $row['school_id']= $row['school_id'] !== null ? (int)$row['school_id'] : null;
        $row['isActive'] = (bool)$row['isActive'];
    }
    unset($row);

    usort($all, fn($a, $b) => strcmp($a['name'], $b['name']));

    echo json_encode($all, JSON_UNESCAPED_UNICODE);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
}
