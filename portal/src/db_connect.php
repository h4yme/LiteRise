<?php
/**
 * Portal DB connection helper.
 * Does NOT set JSON Content-Type headers — safe to include in HTML-rendering pages.
 */

if (defined('PORTAL_DB_LOADED')) return;
define('PORTAL_DB_LOADED', true);

function _portalLoadEnv(string $path): void {
    if (!file_exists($path)) return;
    foreach (file($path, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES) as $line) {
        $line = trim($line);
        if ($line === '' || str_starts_with($line, '#') || strpos($line, '=') === false) continue;
        [$key, $value] = explode('=', $line, 2);
        $key   = trim($key);
        $value = trim(trim($value), "\"'");
        $_ENV[$key] = $value;
        putenv("$key=$value");
    }
}

function _portalEnv(string $key, $default = null) {
    $v = getenv($key);
    if ($v !== false && $v !== '') return $v;
    if (isset($_ENV[$key])    && $_ENV[$key]    !== '') return $_ENV[$key];
    if (isset($_SERVER[$key]) && $_SERVER[$key] !== '') return $_SERVER[$key];
    return $default;
}

// Load .env from project root
$_envFile = realpath(__DIR__ . '/../../.env');
if ($_envFile) _portalLoadEnv($_envFile);
unset($_envFile);

// Build PDO connection
global $portalConn;
$portalConn = null;

try {
    $dsn = sprintf(
        'sqlsrv:Server=tcp:%s,1433;Database=%s;Encrypt=yes;TrustServerCertificate=no;LoginTimeout=30;',
        _portalEnv('DB_SERVER', 'literise.database.windows.net'),
        _portalEnv('DB_NAME',   'literisedb')
    );
    $portalConn = new PDO($dsn,
        _portalEnv('DB_USER',     'SAliterise'),
        _portalEnv('DB_PASSWORD', ''),
        [
            PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
            PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        ]
    );
} catch (PDOException $e) {
    error_log("Portal DB connection failed: " . $e->getMessage());
    $portalConn = null;
}
