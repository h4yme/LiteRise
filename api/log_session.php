<?php

/**
 * LiteRise Log Session API
 * POST /api/log_session.php
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "session_type": "Login",
 *   "session_tag": "optional_tag",
 *   "device_info": "Android 12",
 *   "additional_data": {}
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "log_id": 123,
 *   "session_type": "Login",
 *   "logged_at": "2026-01-01 10:00:00"
 * }
 */

require_once __DIR__ . '/src/db.php';

$data = getJsonInput();

$studentID   = (int)($data['student_id'] ?? 0);
$sessionType = trim($data['session_type'] ?? '');
$sessionTag  = $data['session_tag'] ?? null;
$deviceInfo  = $data['device_info'] ?? null;
$additionalData = isset($data['additional_data'])
    ? json_encode($data['additional_data'])
    : null;

if ($studentID <= 0 || empty($sessionType)) {
    sendError("student_id and session_type are required", 400);
}

try {
    $stmt = $conn->prepare(
        "EXEC SP_LogStudentSession
            @StudentID      = :sid,
            @SessionType    = :type,
            @SessionTag     = :tag,
            @DeviceInfo     = :device,
            @IPAddress      = :ip,
            @AdditionalData = :extra"
    );
    $stmt->bindValue(':sid',    $studentID,     PDO::PARAM_INT);
    $stmt->bindValue(':type',   $sessionType,   PDO::PARAM_STR);
    $stmt->bindValue(':tag',    $sessionTag,    PDO::PARAM_STR);
    $stmt->bindValue(':device', $deviceInfo,    PDO::PARAM_STR);
    $stmt->bindValue(':ip',     $_SERVER['REMOTE_ADDR'] ?? null, PDO::PARAM_STR);
    $stmt->bindValue(':extra',  $additionalData, PDO::PARAM_STR);
    $stmt->execute();

    $row   = $stmt->fetch(PDO::FETCH_ASSOC);
    $logID = $row['LogID'] ?? 0;

    sendResponse([
        'success'      => true,
        'message'      => 'Session logged successfully',
        'log_id'       => (int)$logID,
        'session_type' => $sessionType,
        'logged_at'    => date('Y-m-d H:i:s'),
    ]);

} catch (PDOException $e) {
    error_log("log_session error: " . $e->getMessage());
    sendError("Failed to log session", 500, $e->getMessage());
}
?>
