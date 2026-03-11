<?php
/**
 * Portal authentication guard.
 * Include at the top of every protected portal page:
 *
 *   require_once __DIR__ . '/../auth_check.php';  // from admin/ or teacher/
 *   $user = requirePortalAuth('Admin');            // or 'Teacher' or '' for any role
 */

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

/**
 * Verify the current session has a logged-in portal user.
 * Optionally enforce a specific role.
 *
 * @param string $requiredRole  'Admin', 'Teacher', or '' (any role)
 * @return array                Session user data
 */
function requirePortalAuth(string $requiredRole = ''): array
{
    if (empty($_SESSION['portal_user'])) {
        // Not logged in — redirect to login page.
        // Works for pages one level deep (admin/ or teacher/).
        header('Location: ../login.php');
        exit;
    }

    $user = $_SESSION['portal_user'];

    // Wrong role? Send them to their own portal.
    if ($requiredRole !== '' && $user['role'] !== $requiredRole) {
        $dest = $user['role'] === 'Admin' ? '../admin/index.php' : '../teacher/index.php';
        header("Location: $dest");
        exit;
    }

    return $user;
}
