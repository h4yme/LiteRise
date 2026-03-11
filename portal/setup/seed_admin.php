<?php
/**
 * LiteRise Portal — First-run account seeder.
 *
 * Run this ONCE via browser or CLI to insert the default admin and
 * teacher accounts with properly bcrypt-hashed passwords.
 *
 *   Browser : http://yourserver/portal/setup/seed_admin.php
 *   CLI     : php portal/setup/seed_admin.php
 *
 * DELETE THIS FILE (or block access to /portal/setup/) after running.
 *
 * Default credentials:
 *   admin@literise.com   / LiteRise@2025   (Role: Admin)
 *   teacher@literise.com / LiteRise@2025   (Role: Teacher)
 */

// ── Safety guard ─────────────────────────────────────────────
// Set to true only when you want to seed. Flip back to false
// (or delete the file) immediately after.
define('SEEDING_ENABLED', true);

if (!SEEDING_ENABLED) {
    http_response_code(403);
    die("Seeding is disabled. Set SEEDING_ENABLED = true in this file to run it.");
}

header('Content-Type: text/plain; charset=utf-8');

require_once __DIR__ . '/../src/db_connect.php';

if (!$portalConn) {
    die("ERROR: Could not connect to the database. Check .env credentials.\n");
}

// ── Accounts to seed ─────────────────────────────────────────
$accounts = [
    [
        'first'    => 'System',
        'last'     => 'Admin',
        'email'    => 'admin@literise.com',
        'password' => 'LiteRise@2025',
        'role'     => 'Admin',
        'school'   => null,
    ],
    [
        'first'    => 'Demo',
        'last'     => 'Teacher',
        'email'    => 'teacher@literise.com',
        'password' => 'LiteRise@2025',
        'role'     => 'Teacher',
        'school'   => null,  // set a SchoolID integer if needed
    ],
];

echo "LiteRise Portal — Account Seeder\n";
echo str_repeat('=', 40) . "\n\n";

foreach ($accounts as $acc) {
    echo "Processing: {$acc['email']} ({$acc['role']}) … ";

    // Check if already exists
    $check = $portalConn->prepare("SELECT AdminID FROM Administrators WHERE Email = ?");
    $check->execute([$acc['email']]);
    if ($check->fetch()) {
        echo "SKIPPED (already exists)\n";
        continue;
    }

    $hash = password_hash($acc['password'], PASSWORD_BCRYPT, ['cost' => 12]);

    $stmt = $portalConn->prepare(
        "INSERT INTO Administrators (FirstName, LastName, Email, PasswordHash, Role, SchoolID)
         VALUES (?, ?, ?, ?, ?, ?)"
    );
    $stmt->execute([
        $acc['first'],
        $acc['last'],
        $acc['email'],
        $hash,
        $acc['role'],
        $acc['school'],
    ]);

    echo "OK\n";
}

echo "\nDone!\n\n";
echo "⚠️  IMPORTANT: Delete this file or restrict /portal/setup/ in your web server config.\n";
