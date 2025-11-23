<?php
/**
 * LiteRise Database Connection
 * SQL Server connection via PDO with helper functions
 */

// Enable error reporting for development
error_reporting(E_ALL);
ini_set('display_errors', 0);
ini_set('log_errors', 1);

// Set JSON headers for all API responses
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

// Handle preflight requests
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

// Database configuration
$dbConfig = [
    'server' => getenv('DB_SERVER') ?: 'localhost',
    'database' => getenv('DB_DATABASE') ?: 'LiteRise',
    'username' => getenv('DB_USERNAME') ?: 'sa',
    'password' => getenv('DB_PASSWORD') ?: ''
];

try {
    $dsn = "sqlsrv:Server={$dbConfig['server']};Database={$dbConfig['database']}";

    $conn = new PDO($dsn, $dbConfig['username'], $dbConfig['password'], [
        PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
        PDO::SQLSRV_ATTR_ENCODING => PDO::SQLSRV_ENCODING_UTF8
    ]);
} catch (PDOException $e) {
    error_log("Database connection failed: " . $e->getMessage());
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'error' => 'Database connection failed'
    ]);
    exit;
}

/**
 * Get JSON input from request body
 * @return array Parsed JSON data
 */
function getJsonInput() {
    $input = file_get_contents('php://input');
    $data = json_decode($input, true);
    return $data ?? [];
}

/**
 * Send JSON response
 * @param mixed $data Response data
 * @param int $statusCode HTTP status code
 */
function sendResponse($data, $statusCode = 200) {
    http_response_code($statusCode);
    echo json_encode($data);
    exit;
}

/**
 * Send error response
 * @param string $message Error message
 * @param int $statusCode HTTP status code
 * @param string|null $details Additional error details (only in dev)
 */
function sendError($message, $statusCode = 400, $details = null) {
    http_response_code($statusCode);
    $response = [
        'success' => false,
        'error' => $message
    ];
    if ($details && getenv('APP_DEBUG') === 'true') {
        $response['details'] = $details;
    }
    echo json_encode($response);
    exit;
}

/**
 * Validate required fields in data array
 * @param array $data Input data
 * @param array $fields Required field names
 */
function validateRequired($data, $fields) {
    $missing = [];
    foreach ($fields as $field) {
        if (!isset($data[$field]) || $data[$field] === '') {
            $missing[] = $field;
        }
    }
    if (!empty($missing)) {
        sendError("Missing required fields: " . implode(', ', $missing), 400);
    }
}

/**
 * Log activity to ActivityLogs table
 * @param int $studentID Student ID
 * @param string $activityType Type of activity
 * @param string $description Activity description
 */
function logActivity($studentID, $activityType, $description) {
    global $conn;
    try {
        $stmt = $conn->prepare(
            "INSERT INTO ActivityLogs (StudentID, ActivityType, Description, Timestamp)
             VALUES (?, ?, ?, GETDATE())"
        );
        $stmt->execute([$studentID, $activityType, $description]);
    } catch (Exception $e) {
        error_log("Failed to log activity: " . $e->getMessage());
    }
}
?>
