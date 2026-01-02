<?php
/**
 * Check for PHP errors and warnings in API files
 */

// Turn on error reporting
error_reporting(E_ALL);
ini_set('display_errors', 1);

echo "<h2>Checking API Files for Errors</h2>";

// Test 1: Check if files can be included without errors
echo "<h3>1. Testing file includes:</h3><pre>";

// Start output buffering to catch any output
ob_start();

try {
    require_once __DIR__ . '/src/db.php';
    echo "✓ db.php loaded\n";
} catch (Exception $e) {
    echo "✗ db.php ERROR: " . $e->getMessage() . "\n";
}

// Check if db.php produced any output
$dbOutput = ob_get_clean();
if (!empty(trim($dbOutput))) {
    echo "⚠️ db.php produced output:\n$dbOutput\n";
} else {
    echo "✓ db.php - no output (good!)\n";
}

ob_start();
try {
    require_once __DIR__ . '/src/auth.php';
    echo "✓ auth.php loaded\n";
} catch (Exception $e) {
    echo "✗ auth.php ERROR: " . $e->getMessage() . "\n";
}

$authOutput = ob_get_clean();
if (!empty(trim($authOutput))) {
    echo "⚠️ auth.php produced output:\n$authOutput\n";
} else {
    echo "✓ auth.php - no output (good!)\n";
}

ob_start();
try {
    require_once __DIR__ . '/src/email.php';
    echo "✓ email.php loaded\n";
} catch (Exception $e) {
    echo "✗ email.php ERROR: " . $e->getMessage() . "\n";
}

$emailOutput = ob_get_clean();
if (!empty(trim($emailOutput))) {
    echo "⚠️ email.php produced output:\n$emailOutput\n";
} else {
    echo "✓ email.php - no output (good!)\n";
}

echo "</pre>";

// Test 2: Check register.php for syntax errors
echo "<h3>2. Checking register.php:</h3><pre>";

ob_start();
$registerContent = file_get_contents(__DIR__ . '/register.php');

// Check if register.php has any echo statements before JSON output
if (preg_match('/echo|print|var_dump|print_r/i', substr($registerContent, 0, 500))) {
    echo "⚠️ Warning: register.php may have output statements\n";
}

// Try to detect the first line that sends output
$lines = explode("\n", $registerContent);
foreach ($lines as $i => $line) {
    $line = trim($line);
    if (empty($line) || strpos($line, '//') === 0 || strpos($line, '/*') === 0 || strpos($line, '*') === 0) {
        continue;
    }

    if (preg_match('/^(echo|print|var_dump|print_r)\s/i', $line)) {
        echo "⚠️ Line " . ($i + 1) . ": $line\n";
    }
}

echo "✓ register.php syntax check complete\n";
echo "</pre>";

// Test 3: Test database connection
echo "<h3>3. Testing Database Connection:</h3><pre>";

if (isset($conn) && $conn instanceof PDO) {
    try {
        $stmt = $conn->query("SELECT 1 AS test");
        $result = $stmt->fetch();
        if ($result) {
            echo "✓ Database connection: WORKING\n";
        }
    } catch (Exception $e) {
        echo "✗ Database query error: " . $e->getMessage() . "\n";
    }
} else {
    echo "✗ Database connection: NOT ESTABLISHED\n";
    echo "Check your .env file and database credentials\n";
}

echo "</pre>";

// Test 4: Simulate a registration request
echo "<h3>4. Testing Registration Endpoint (Simulated):</h3><pre>";

$_SERVER['REQUEST_METHOD'] = 'POST';
$testData = [
    'nickname' => 'DiagTest',
    'first_name' => 'Diagnostic',
    'last_name' => 'Test',
    'email' => 'diagtest_' . time() . '@example.com',
    'password' => 'test123456',
    'grade_level' => '1'
];

echo "Test data:\n";
echo json_encode($testData, JSON_PRETTY_PRINT) . "\n\n";

echo "To test the actual endpoint, use:\n";
echo "URL: http://localhost/api/register.php\n";
echo "Method: POST\n";
echo "Content-Type: application/json\n";
echo "Body: " . json_encode($testData) . "\n";

echo "</pre>";

// Test 5: Check headers already sent
echo "<h3>5. Checking Headers:</h3><pre>";

if (headers_sent($file, $line)) {
    echo "⚠️ WARNING: Headers already sent at $file:$line\n";
    echo "This will prevent JSON responses from working!\n";
} else {
    echo "✓ Headers not sent yet (good!)\n";
}

echo "</pre>";

echo "<h3>6. Recommendations:</h3>";
echo "<ul>";
echo "<li>If you see any '⚠️' warnings above, fix those first</li>";
echo "<li>Make sure .env file exists with correct database credentials</li>";
echo "<li>Test the simple endpoint first: <a href='test_simple.php'>test_simple.php</a></li>";
echo "<li>Use the browser tester: <a href='test_browser.html'>test_browser.html</a></li>";
echo "</ul>";

?>
