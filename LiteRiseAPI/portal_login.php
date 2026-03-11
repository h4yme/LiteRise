<?php
/**
 * portal_login.php
 * Authenticates Admins and Teachers for the LiteRise web portal.
 *
 * POST params:
 *   email    – user's email address
 *   password – plain-text password
 *   role     – "admin" | "teacher"
 *
 * Response JSON:
 *   { success: bool, user_id: int, name: string, email: string,
 *     role: string, message: string }
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'POST required.']);
    exit;
}

// ── Input ─────────────────────────────────────────────────────────────────────

$email    = trim($_POST['email']    ?? '');
$password = trim($_POST['password'] ?? '');
$role     = strtolower(trim($_POST['role'] ?? 'admin'));

if ($email === '' || $password === '') {
    echo json_encode(['success' => false, 'message' => 'Email and password are required.']);
    exit;
}

if (!in_array($role, ['admin', 'teacher'], true)) {
    echo json_encode(['success' => false, 'message' => 'Invalid role.']);
    exit;
}

// ── DB connection ──────────────────────────────────────────────────────────────

// Credentials are read from environment variables (set in your web server config
// or a .env loader).  Set DB_PASSWORD in your server environment — do NOT
// hard-code it here.
$dsn    = sprintf(
    'sqlsrv:Server=%s;Database=%s',
    getenv('DB_SERVER') ?: 'literise.database.windows.net',
    getenv('DB_NAME')   ?: 'literisedb'
);
$dbUser = getenv('DB_USER') ?: 'SAliterise';
$dbPass = getenv('DB_PASSWORD') ?: '';

try {
    $pdo = new PDO($dsn, $dbUser, $dbPass, [
        PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
        PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    ]);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Database connection failed.']);
    exit;
}

// ── Authenticate ───────────────────────────────────────────────────────────────

if ($role === 'admin') {
    $stmt = $pdo->prepare(
        'SELECT AdminID, Username, Email, PasswordHash, IsActive, FailedLoginAttempts, LockoutEnd
         FROM   dbo.Admins
         WHERE  Email = ?'
    );
    $stmt->execute([$email]);
    $row = $stmt->fetch();

    if (!$row) {
        echo json_encode(['success' => false, 'message' => 'Invalid email or password.']);
        exit;
    }

    // Account locked?
    if (!$row['IsActive']) {
        echo json_encode(['success' => false, 'message' => 'This account has been deactivated.']);
        exit;
    }
    if ($row['LockoutEnd'] !== null && strtotime($row['LockoutEnd']) > time()) {
        echo json_encode(['success' => false, 'message' => 'Account is temporarily locked. Try again later.']);
        exit;
    }

    if (!password_verify($password, $row['PasswordHash'])) {
        // Increment failed attempts
        $pdo->prepare('UPDATE dbo.Admins SET FailedLoginAttempts = FailedLoginAttempts + 1 WHERE AdminID = ?')
            ->execute([$row['AdminID']]);
        echo json_encode(['success' => false, 'message' => 'Invalid email or password.']);
        exit;
    }

    // Successful — reset counters, update last login
    $pdo->prepare('UPDATE dbo.Admins SET FailedLoginAttempts = 0, LockoutEnd = NULL, LastLoginDate = GETDATE() WHERE AdminID = ?')
        ->execute([$row['AdminID']]);

    echo json_encode([
        'success' => true,
        'user_id' => (int) $row['AdminID'],
        'name'    => $row['Username'],
        'email'   => $row['Email'],
        'role'    => 'admin',
        'message' => 'Login successful.',
    ]);

} else {
    // Teacher
    $stmt = $pdo->prepare(
        'SELECT TeacherID, FirstName, LastName, Email, Password, IsActive
         FROM   dbo.Teachers
         WHERE  Email = ?'
    );
    $stmt->execute([$email]);
    $row = $stmt->fetch();

    if (!$row) {
        echo json_encode(['success' => false, 'message' => 'Invalid email or password.']);
        exit;
    }

    if (!(bool) $row['IsActive']) {
        echo json_encode(['success' => false, 'message' => 'This account has been deactivated.']);
        exit;
    }

    if (!password_verify($password, $row['Password'])) {
        echo json_encode(['success' => false, 'message' => 'Invalid email or password.']);
        exit;
    }

    echo json_encode([
        'success' => true,
        'user_id' => (int) $row['TeacherID'],
        'name'    => $row['FirstName'] . ' ' . $row['LastName'],
        'email'   => $row['Email'],
        'role'    => 'teacher',
        'message' => 'Login successful.',
    ]);
}
