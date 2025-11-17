<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// Load environment variables
require_once __DIR__ . '/../vendor/autoload.php'; // If using composer
// OR manually load .env
$envFile = __DIR__ . '/../.env';
if (file_exists($envFile)) {
    $lines = file($envFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        if (strpos(trim($line), '#') === 0) continue; // Skip comments
        list($key, $value) = explode('=', $line, 2);
        $_ENV[trim($key)] = trim($value);
    }
}

// Database configuration from environment variables
$serverName = $_ENV['DB_SERVER'] ?? 'DESKTOP-PEM6F9E\\SQLEXPRESS';
$database = $_ENV['DB_NAME'] ?? 'LiteRiseDB';
$username = $_ENV['DB_USER'] ?? 'sa';
$password = $_ENV['DB_PASSWORD'] ?? 'p@ssw0rd';

try {
    $conn = new PDO("sqlsrv:Server=$serverName;Database=$database", $username, $password);
    $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    
    // Only show success message if DEBUG_MODE is true
    if (isset($_ENV['DEBUG_MODE']) && $_ENV['DEBUG_MODE'] === 'true') {
        // Connection successful - can log this
        error_log("Database connection successful");
    }
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        "error" => "Database connection failed",
        "message" => ($_ENV['DEBUG_MODE'] ?? 'false') === 'true' ? $e->getMessage() : "Internal server error"
    ]);
    exit;
}

// Helper function for JSON responses
function sendResponse($data, $statusCode = 200) {
    http_response_code($statusCode);
    echo json_encode($data);
    exit;
}

// Helper function for error responses
function sendError($message, $statusCode = 400, $details = null) {
    http_response_code($statusCode);
    $response = ["error" => $message];
    if ($details && ($_ENV['DEBUG_MODE'] ?? 'false') === 'true') {
        $response['details'] = $details;
    }
    echo json_encode($response);
    exit;
}
?>