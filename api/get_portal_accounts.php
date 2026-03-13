<?php
/**
 * get_portal_accounts.php
 * Returns all admin and teacher accounts.
 *
 * GET /api/get_portal_accounts.php
 *
 * Requires: Bearer JWT (portal token)
 *
 * Response: JSON array of account objects
 */
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Authorization, Content-Type');

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') { http_response_code(204); exit; }

requireAuth();

try {
    // ── Admins ────────────────────────────────────────────────────────────
    $stmtA = $conn->query("
        SELECT
            'admin_' + CAST(AdminID AS VARCHAR)              AS id,
            AdminID                                          AS raw_id,
            ISNULL(Username, Email)                          AS name,
            Email                                            AS email,
            'Admin'                                          AS role,
            CAST(IsActive AS BIT)                            AS isActive
        FROM dbo.Admins
    ");
    $admins = $stmtA->fetchAll(PDO::FETCH_ASSOC);

    // ── Teachers ──────────────────────────────────────────────────────────
    $stmtT = $conn->query("
        SELECT
            'teacher_' + CAST(TeacherID AS VARCHAR)                  AS id,
            TeacherID                                                 AS raw_id,
            RTRIM(ISNULL(FirstName,'') + ' ' + ISNULL(LastName,''))  AS name,
            Email                                                     AS email,
            'Teacher'                                                 AS role,
            CAST(IsActive AS BIT)                                     AS isActive
        FROM dbo.Teachers
    ");
    $teachers = $stmtT->fetchAll(PDO::FETCH_ASSOC);

    $all = array_merge($admins, $teachers);

    foreach ($all as &$row) {
        $row['raw_id']   = (int)$row['raw_id'];
        $row['isActive'] = (bool)$row['isActive'];
    }
    unset($row);

    usort($all, function ($a, $b) { return strcmp($a['name'], $b['name']); });

    echo json_encode($all, JSON_UNESCAPED_UNICODE);

} catch (\Throwable $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => $e->getMessage()]);
    error_log('get_portal_accounts error: ' . $e->getMessage());
}
