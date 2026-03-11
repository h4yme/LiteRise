<?php
/**
 * seed_portal_users.php
 * ─────────────────────────────────────────────────────────────────────────────
 * ONE-TIME setup script — run from CLI or browser to insert a default
 * Admin and Teacher account into the LiteRiseDB database.
 *
 * Usage (CLI):
 *   php seed_portal_users.php
 *
 * Delete or restrict access to this file after running it.
 * ─────────────────────────────────────────────────────────────────────────────
 * Default credentials created:
 *
 *   Admin
 *     Email   : admin@literise.com
 *     Password: Admin@123
 *
 *   Teacher
 *     Email   : teacher@literise.com
 *     Password: Teacher@123
 * ─────────────────────────────────────────────────────────────────────────────
 */

// ── DB config ─────────────────────────────────────────────────────────────────
// Reads from environment variables — set DB_PASSWORD in your server environment.
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
    die("DB connection failed: " . $e->getMessage() . "\n");
}

// ── Seed Admin ─────────────────────────────────────────────────────────────────

$adminEmail    = 'admin@literise.com';
$adminPassword = 'Admin@123';
$adminUsername = 'superadmin';

// Check if already exists
$check = $pdo->prepare('SELECT AdminID FROM dbo.Admins WHERE Email = ?');
$check->execute([$adminEmail]);

if ($check->fetch()) {
    echo "Admin {$adminEmail} already exists — skipping.\n";
} else {
    $hash = password_hash($adminPassword, PASSWORD_BCRYPT);
    // Salt column is required by schema; bcrypt embeds the salt internally
    // so we store a placeholder string.
    $salt = 'bcrypt';

    $stmt = $pdo->prepare(
        "INSERT INTO dbo.Admins (Username, Email, PasswordHash, Salt, IsActive, FailedLoginAttempts)
         VALUES (?, ?, ?, ?, 1, 0)"
    );
    $stmt->execute([$adminUsername, $adminEmail, $hash, $salt]);
    echo "Admin created: {$adminEmail} / {$adminPassword}\n";
}

// ── Seed Teacher ───────────────────────────────────────────────────────────────

$teacherEmail     = 'teacher@literise.com';
$teacherPassword  = 'Teacher@123';
$teacherFirstName = 'Demo';
$teacherLastName  = 'Teacher';
$teacherDept      = 'English';

$check2 = $pdo->prepare('SELECT TeacherID FROM dbo.Teachers WHERE Email = ?');
$check2->execute([$teacherEmail]);

if ($check2->fetch()) {
    echo "Teacher {$teacherEmail} already exists — skipping.\n";
} else {
    $hash2 = password_hash($teacherPassword, PASSWORD_BCRYPT);

    $stmt2 = $pdo->prepare(
        "INSERT INTO dbo.Teachers (FirstName, LastName, Email, Password, Department, IsActive, DateCreated)
         VALUES (?, ?, ?, ?, ?, 1, GETDATE())"
    );
    $stmt2->execute([$teacherFirstName, $teacherLastName, $teacherEmail, $hash2, $teacherDept]);
    echo "Teacher created: {$teacherEmail} / {$teacherPassword}\n";
}

echo "\nDone. Delete this file from the server now.\n";
