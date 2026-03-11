<?php
/**
 * LiteRise Portal — Login
 * Handles both GET (render form) and POST (authenticate + redirect).
 * Role "Admin"   → admin/index.php
 * Role "Teacher" → teacher/index.php
 */

session_start();

// Already logged in? Skip the form.
if (!empty($_SESSION['portal_user'])) {
    $role = $_SESSION['portal_user']['role'];
    header('Location: ' . ($role === 'Admin' ? 'admin/index.php' : 'teacher/index.php'));
    exit;
}

$error        = '';
$prefillEmail = '';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $email    = trim($_POST['email']    ?? '');
    $password = trim($_POST['password'] ?? '');
    $prefillEmail = htmlspecialchars($email, ENT_QUOTES, 'UTF-8');

    if (!$email || !$password) {
        $error = 'Please enter your email and password.';
    } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        $error = 'Invalid email address.';
    } else {
        require_once __DIR__ . '/src/db_connect.php';

        if (!$portalConn) {
            $error = 'Database connection failed. Please try again later.';
        } else {
            try {
                $stmt = $portalConn->prepare("EXEC SP_AdminLogin @Email = ?");
                $stmt->execute([$email]);
                $admin = $stmt->fetch(PDO::FETCH_ASSOC);

                if ($admin && password_verify($password, $admin['PasswordHash'])) {
                    // Store session
                    $_SESSION['portal_user'] = [
                        'id'        => $admin['AdminID'],
                        'name'      => trim($admin['FirstName'] . ' ' . $admin['LastName']),
                        'initials'  => strtoupper(substr($admin['FirstName'], 0, 1) . substr($admin['LastName'], 0, 1)),
                        'email'     => $admin['Email'],
                        'role'      => $admin['Role'],
                        'school_id' => $admin['SchoolID'],
                    ];

                    // Update last login (non-fatal)
                    try {
                        $s2 = $portalConn->prepare("EXEC SP_UpdateAdminLastLogin @AdminID = ?");
                        $s2->execute([$admin['AdminID']]);
                    } catch (Exception $e) {
                        error_log("Portal last-login update failed: " . $e->getMessage());
                    }

                    header('Location: ' . ($admin['Role'] === 'Admin' ? 'admin/index.php' : 'teacher/index.php'));
                    exit;
                } else {
                    $error = 'Invalid email or password.';
                }
            } catch (Exception $e) {
                error_log("Portal login error: " . $e->getMessage());
                $error = 'Login failed. Please try again.';
            }
        }
    }
}
?><!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>LiteRise Portal — Sign In</title>
    <style>
        /* ── Fonts ──────────────────────────────────────────── */
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: #0F172A;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative;
            overflow: hidden;
        }

        /* Grid texture */
        body::before {
            content: '';
            position: fixed; inset: 0;
            background-image:
                linear-gradient(rgba(255,255,255,0.025) 1px, transparent 1px),
                linear-gradient(90deg, rgba(255,255,255,0.025) 1px, transparent 1px);
            background-size: 48px 48px;
            pointer-events: none;
        }

        /* ── Glow blobs ───────────────────────────────────── */
        .bg-blobs { position: fixed; inset: 0; overflow: hidden; z-index: 0; pointer-events: none; }
        .blob { position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.5; }
        .blob-1 { width:400px;height:400px;background:radial-gradient(circle,rgba(17,176,103,.25),transparent 70%);top:-100px;right:-80px; }
        .blob-2 { width:350px;height:350px;background:radial-gradient(circle,rgba(124,58,237,.18),transparent 70%);bottom:-80px;left:-60px; }
        .blob-3 { width:250px;height:250px;background:radial-gradient(circle,rgba(253,201,74,.15),transparent 70%);top:40%;left:10%; }
        .blob-4 { width:200px;height:200px;background:radial-gradient(circle,rgba(17,176,103,.15),transparent 70%);bottom:20%;right:15%; }

        /* ── Branding ─────────────────────────────────────── */
        .branding {
            position: fixed; top: 28px; left: 36px; z-index: 10;
            display: flex; align-items: center; gap: 10px;
        }
        .brand-icon {
            width: 36px; height: 36px; border-radius: 10px;
            background: linear-gradient(135deg, #0EA05A, #11B067);
            display: flex; align-items: center; justify-content: center;
            color: #fff; font-weight: 800; font-size: 16px;
            box-shadow: 0 4px 12px rgba(17,176,103,.4);
        }
        .brand-name {
            font-size: 1.15rem; font-weight: 800; color: #fff;
            letter-spacing: -0.3px; text-decoration: none;
        }

        /* ── Role badge ───────────────────────────────────── */
        .role-badge {
            position: fixed; top: 34px; right: 36px; z-index: 10;
            background: rgba(255,255,255,.08); border: 1px solid rgba(255,255,255,.15);
            border-radius: 999px; padding: 4px 14px;
            font-size: 0.78rem; font-weight: 600; color: rgba(255,255,255,.65);
        }

        /* ── Login container ──────────────────────────────── */
        .login-wrap {
            position: relative; z-index: 1;
            width: 100%; max-width: 420px; padding: 20px;
        }

        /* ── Card ─────────────────────────────────────────── */
        .card {
            background: rgba(255,255,255,.97);
            border-radius: 20px;
            padding: 40px 36px;
            box-shadow: 0 24px 64px rgba(0,0,0,.35), 0 0 0 1px rgba(255,255,255,.08);
        }

        /* ── Header ───────────────────────────────────────── */
        .card-header { text-align: center; margin-bottom: 28px; }
        .card-logo {
            width: 56px; height: 56px; border-radius: 16px;
            background: linear-gradient(135deg, #0EA05A, #11B067);
            display: flex; align-items: center; justify-content: center;
            margin: 0 auto 16px;
            font-size: 26px;
            box-shadow: 0 8px 24px rgba(17,176,103,.35);
        }
        .card-header h1 { font-size: 1.45rem; font-weight: 800; color: #0F172A; margin-bottom: 6px; }
        .card-header p  { font-size: 0.85rem; color: #64748B; font-weight: 500; }

        /* ── Role tabs ────────────────────────────────────── */
        .role-tabs {
            display: flex; gap: 6px;
            background: #F1F5F9; border-radius: 10px;
            padding: 4px; margin-bottom: 24px;
        }
        .role-tab {
            flex: 1; padding: 8px 12px; border: none; border-radius: 8px;
            font-size: 0.85rem; font-weight: 600; cursor: pointer;
            transition: all 0.2s; color: #64748B; background: transparent;
        }
        .role-tab.active {
            background: #fff; color: #11B067;
            box-shadow: 0 1px 4px rgba(0,0,0,.1);
        }

        /* ── Error ────────────────────────────────────────── */
        .error-msg {
            display: flex; align-items: center; gap: 10px;
            background: #FEE2E2; color: #DC2626;
            padding: 11px 14px; border-radius: 8px;
            margin-bottom: 18px; font-size: 0.84rem; font-weight: 600;
        }

        /* ── Form ─────────────────────────────────────────── */
        .form { display: flex; flex-direction: column; gap: 18px; }
        .form-group { display: flex; flex-direction: column; gap: 6px; }
        .form-group label { font-size: 0.84rem; font-weight: 700; color: #0F172A; }
        .input-wrap { position: relative; }
        .input-icon {
            position: absolute; left: 14px; top: 50%; transform: translateY(-50%);
            color: #94A3B8; pointer-events: none;
            display: flex; align-items: center;
        }
        .input-wrap input {
            width: 100%; padding: 12px 44px 12px 42px;
            border: 1.5px solid #E2E8F0; border-radius: 10px;
            font-size: 0.9rem; background: #F8FAFC; color: #0F172A;
            transition: all 0.2s; font-family: inherit;
        }
        .input-wrap input:focus {
            outline: none; border-color: #11B067; background: #fff;
            box-shadow: 0 0 0 3px rgba(17,176,103,.12);
        }
        .input-wrap input::placeholder { color: #94A3B8; }

        .pw-toggle {
            position: absolute; right: 12px; top: 50%; transform: translateY(-50%);
            background: none; border: none; cursor: pointer; color: #94A3B8;
            display: flex; align-items: center; padding: 4px;
            transition: color 0.2s;
        }
        .pw-toggle:hover { color: #11B067; }

        /* ── Options row ──────────────────────────────────── */
        .form-options {
            display: flex; justify-content: space-between; align-items: center;
            font-size: 0.84rem;
        }
        .remember {
            display: flex; align-items: center; gap: 7px;
            cursor: pointer; color: #64748B; font-weight: 500;
        }
        .remember input { accent-color: #11B067; width: 15px; height: 15px; cursor: pointer; }
        .forgot { color: #11B067; text-decoration: none; font-weight: 700; }
        .forgot:hover { color: #0EA05A; }

        /* ── Submit ───────────────────────────────────────── */
        .btn-submit {
            display: flex; align-items: center; justify-content: center; gap: 8px;
            background: linear-gradient(135deg, #0EA05A, #11B067, #22C55E);
            color: #fff; border: none; border-radius: 10px; padding: 13px;
            font-size: 0.97rem; font-weight: 700; font-family: inherit;
            cursor: pointer; transition: all 0.25s;
            box-shadow: 0 6px 20px rgba(17,176,103,.4);
        }
        .btn-submit:hover { transform: translateY(-2px); box-shadow: 0 10px 28px rgba(17,176,103,.55); }
        .btn-submit:active { transform: translateY(0); }
        .btn-submit.loading { opacity: 0.7; pointer-events: none; }

        /* ── Footer ───────────────────────────────────────── */
        .card-footer { text-align: center; margin-top: 20px; font-size: 0.82rem; color: #94A3B8; }
        .card-footer a { color: #11B067; text-decoration: none; font-weight: 600; }
        .card-footer a:hover { color: #0EA05A; }

        @media (max-width: 480px) {
            .card { padding: 28px 20px; }
            .branding { top: 18px; left: 20px; }
            .role-badge { display: none; }
        }
    </style>
</head>
<body>

    <!-- Background blobs -->
    <div class="bg-blobs">
        <div class="blob blob-1"></div>
        <div class="blob blob-2"></div>
        <div class="blob blob-3"></div>
        <div class="blob blob-4"></div>
    </div>

    <!-- Branding -->
    <div class="branding">
        <div class="brand-icon">L</div>
        <span class="brand-name">LiteRise</span>
    </div>
    <div class="role-badge">Teacher &amp; Admin Portal</div>

    <!-- Login card -->
    <div class="login-wrap">
        <div class="card">
            <div class="card-header">
                <div class="card-logo">📚</div>
                <h1>Portal Sign In</h1>
                <p>Enter your credentials to continue</p>
            </div>

            <!-- Role tabs (cosmetic — actual role comes from DB) -->
            <div class="role-tabs" id="roleTabs">
                <button type="button" class="role-tab active" data-role="admin" onclick="setRoleHint(this)">
                    🛡 Admin
                </button>
                <button type="button" class="role-tab" data-role="teacher" onclick="setRoleHint(this)">
                    📋 Teacher
                </button>
            </div>

            <?php if ($error): ?>
            <div class="error-msg">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="12" cy="12" r="10"></circle>
                    <line x1="12" y1="8" x2="12" y2="12"></line>
                    <line x1="12" y1="16" x2="12.01" y2="16"></line>
                </svg>
                <?= htmlspecialchars($error, ENT_QUOTES, 'UTF-8') ?>
            </div>
            <?php endif; ?>

            <form class="form" method="POST" action="" id="loginForm">
                <div class="form-group">
                    <label for="email">Email Address</label>
                    <div class="input-wrap">
                        <span class="input-icon">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                                <polyline points="22,6 12,13 2,6"></polyline>
                            </svg>
                        </span>
                        <input type="email" id="email" name="email"
                               placeholder="your@email.com"
                               value="<?= $prefillEmail ?>"
                               required autocomplete="email">
                    </div>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <div class="input-wrap">
                        <span class="input-icon">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                            </svg>
                        </span>
                        <input type="password" id="password" name="password"
                               placeholder="••••••••"
                               required autocomplete="current-password">
                        <button type="button" class="pw-toggle" id="pwToggle" aria-label="Show/hide password">
                            <svg id="eyeOpen" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                                <circle cx="12" cy="12" r="3"></circle>
                            </svg>
                            <svg id="eyeClosed" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="display:none">
                                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                                <line x1="1" y1="1" x2="23" y2="23"></line>
                            </svg>
                        </button>
                    </div>
                </div>

                <div class="form-options">
                    <label class="remember">
                        <input type="checkbox" name="remember"> Remember me
                    </label>
                    <a href="#" class="forgot">Forgot password?</a>
                </div>

                <button type="submit" class="btn-submit" id="submitBtn">
                    Sign In
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                        <polyline points="12 5 19 12 12 19"></polyline>
                    </svg>
                </button>
            </form>

            <div class="card-footer">
                Student app? <a href="/">Go to LiteRise home</a>
            </div>
        </div>
    </div>

    <script>
        // Password visibility toggle
        document.getElementById('pwToggle').addEventListener('click', function () {
            const input = document.getElementById('password');
            const isText = input.type === 'text';
            input.type = isText ? 'password' : 'text';
            document.getElementById('eyeOpen').style.display  = isText ? 'block' : 'none';
            document.getElementById('eyeClosed').style.display = isText ? 'none' : 'block';
        });

        // Role tabs (visual only — actual role resolved by DB)
        function setRoleHint(btn) {
            document.querySelectorAll('.role-tab').forEach(t => t.classList.remove('active'));
            btn.classList.add('active');
        }

        // Loading state on submit
        document.getElementById('loginForm').addEventListener('submit', function () {
            const btn = document.getElementById('submitBtn');
            btn.classList.add('loading');
            btn.textContent = 'Signing in…';
        });
    </script>
</body>
</html>
