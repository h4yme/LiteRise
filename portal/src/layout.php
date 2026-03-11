<?php
/**
 * Shared portal layout helpers.
 * Call portalHeader($user, $pageTitle, $activeNav) to open the HTML shell,
 * then echo your page content,
 * then call portalFooter() to close it.
 *
 * $activeNav matches the "data-page" attribute on sidebar links.
 */

function portalHeader(array $user, string $pageTitle, string $activeNav = ''): void
{
    $name     = htmlspecialchars($user['name'],     ENT_QUOTES, 'UTF-8');
    $email    = htmlspecialchars($user['email'],    ENT_QUOTES, 'UTF-8');
    $role     = htmlspecialchars($user['role'],     ENT_QUOTES, 'UTF-8');
    $initials = htmlspecialchars($user['initials'], ENT_QUOTES, 'UTF-8');
    $title    = htmlspecialchars($pageTitle,        ENT_QUOTES, 'UTF-8');

    // Build nav items for the correct role
    $adminNav = [
        ['page' => 'Dashboard',  'href' => 'index.php',         'icon' => '⊞',  'label' => 'Dashboard'],
        ['page' => 'Students',   'href' => '../students/index.php', 'icon' => '👥', 'label' => 'Students'],
        ['page' => 'Schools',    'href' => '../schools/index.php',  'icon' => '🏫', 'label' => 'Schools'],
        ['page' => 'Analytics',  'href' => '../analytics/index.php','icon' => '📊', 'label' => 'Analytics'],
        ['page' => 'Masterfile', 'href' => '../masterfile/index.php','icon' => '📚', 'label' => 'Masterfile'],
        ['page' => 'Reports',    'href' => '../reports/index.php',  'icon' => '📋', 'label' => 'Reports'],
        ['page' => 'Notifications','href'=> '../notifications/index.php','icon'=>'🔔','label'=> 'Notifications'],
        ['page' => 'Settings',   'href' => '../settings/index.php', 'icon' => '⚙️', 'label' => 'Settings'],
    ];

    $teacherNav = [
        ['page' => 'Dashboard',  'href' => 'index.php',              'icon' => '⊞',  'label' => 'Dashboard'],
        ['page' => 'Students',   'href' => '../students/index.php',   'icon' => '👥', 'label' => 'My Students'],
        ['page' => 'Analytics',  'href' => '../analytics/index.php',  'icon' => '📊', 'label' => 'Class Analytics'],
        ['page' => 'Reports',    'href' => '../reports/index.php',    'icon' => '📋', 'label' => 'Progress Reports'],
        ['page' => 'Settings',   'href' => '../settings/index.php',   'icon' => '⚙️', 'label' => 'Settings'],
    ];

    $navItems = ($user['role'] === 'Admin') ? $adminNav : $teacherNav;
    $roleBadge = $user['role'] === 'Admin'
        ? '<span style="background:#7C3AED;color:#fff;font-size:.7rem;font-weight:700;border-radius:6px;padding:2px 8px;">ADMIN</span>'
        : '<span style="background:#11B067;color:#fff;font-size:.7rem;font-weight:700;border-radius:6px;padding:2px 8px;">TEACHER</span>';

    echo <<<HTML
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>{$title} — LiteRise Portal</title>
    <style>
        /* ── Reset & tokens ─────────────────────────────── */
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }
        :root {
            --green:       #11B067;
            --green-dark:  #0EA05A;
            --green-pale:  #E8F7EE;
            --dark:        #0F172A;
            --dark-card:   #1E293B;
            --text:        #0F172A;
            --text-sec:    #64748B;
            --text-muted:  #94A3B8;
            --bg:          #F8FAFC;
            --border:      #E2E8F0;
            --shadow-sm:   0 1px 3px rgba(15,23,42,.08);
            --shadow-md:   0 4px 16px rgba(15,23,42,.1);
            --shadow-lg:   0 20px 50px rgba(15,23,42,.14);
        }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
            background: var(--bg);
            color: var(--text);
            min-height: 100vh;
            -webkit-font-smoothing: antialiased;
        }

        /* ── Top bar ─────────────────────────────────────── */
        .topbar {
            position: fixed; top: 0; left: 0; right: 0; z-index: 1001;
            height: 60px; background: #fff;
            border-bottom: 1px solid var(--border);
            display: flex; align-items: center; justify-content: space-between;
            padding: 0 20px; box-shadow: var(--shadow-sm);
        }
        .topbar-left { display: flex; align-items: center; gap: 14px; }
        .sidebar-toggle {
            background: none; border: none; cursor: pointer; padding: 8px;
            border-radius: 8px; color: var(--text-sec); transition: all .2s;
            display: flex; align-items: center; justify-content: center;
            width: 36px; height: 36px;
        }
        .sidebar-toggle:hover { background: var(--green-pale); color: var(--green); }
        .topbar-brand { font-size: 1.1rem; font-weight: 800; color: var(--text); text-decoration: none; }
        .topbar-brand:hover { color: var(--green); }
        .topbar-right { display: flex; align-items: center; gap: 8px; }
        .topbar-user { display: flex; align-items: center; gap: 10px; cursor: pointer; position: relative; }
        .user-avatar {
            width: 34px; height: 34px; border-radius: 50%;
            background: linear-gradient(135deg, #0EA05A, #11B067);
            display: flex; align-items: center; justify-content: center;
            color: #fff; font-weight: 700; font-size: 13px;
            box-shadow: 0 2px 8px rgba(17,176,103,.35);
            flex-shrink: 0;
        }
        .user-info { display: flex; flex-direction: column; }
        .user-name  { font-size: .85rem; font-weight: 700; color: var(--text); }
        .user-email { font-size: .72rem; color: var(--text-muted); }

        /* User dropdown */
        .user-dropdown {
            position: absolute; top: calc(100% + 10px); right: 0;
            background: #fff; border: 1px solid var(--border);
            border-radius: 14px; box-shadow: var(--shadow-lg);
            min-width: 200px; padding: 6px 0;
            opacity: 0; visibility: hidden; transform: translateY(-6px);
            transition: all .2s ease; z-index: 2000;
        }
        .topbar-user:focus-within .user-dropdown,
        .topbar-user.open .user-dropdown {
            opacity: 1; visibility: visible; transform: translateY(0);
        }
        .user-dropdown a {
            display: flex; align-items: center; gap: 10px;
            padding: 10px 16px; text-decoration: none;
            color: var(--text-sec); font-size: .87rem; font-weight: 500;
            transition: all .15s;
        }
        .user-dropdown a:hover { background: var(--green-pale); color: var(--green); }
        .user-dropdown .divider { height: 1px; background: var(--border); margin: 4px 0; }
        .user-dropdown .logout  { color: #EF4444; }
        .user-dropdown .logout:hover { background: #FEE2E2; color: #DC2626; }

        /* ── Sidebar ─────────────────────────────────────── */
        .sidebar {
            position: fixed; top: 60px; left: 0; bottom: 0;
            width: 230px; background: var(--dark);
            display: flex; flex-direction: column;
            overflow: hidden; z-index: 1000;
            transition: width .3s ease;
            box-shadow: 2px 0 16px rgba(0,0,0,.2);
        }
        .sidebar.collapsed { width: 58px; }
        .sidebar-inner { flex: 1; overflow-y: auto; overflow-x: hidden; padding: 12px 0; }
        .sidebar-inner::-webkit-scrollbar { width: 4px; }
        .sidebar-inner::-webkit-scrollbar-thumb { background: rgba(255,255,255,.12); border-radius: 4px; }

        .nav-item {
            display: flex; align-items: center; gap: 12px;
            padding: 10px 12px; margin: 2px 8px; border-radius: 10px;
            color: rgba(255,255,255,.5); text-decoration: none;
            font-size: .88rem; font-weight: 500;
            transition: all .2s; min-height: 42px; white-space: nowrap;
        }
        .nav-item:hover { background: rgba(17,176,103,.12); color: #84C788; text-decoration: none; }
        .nav-item.active {
            background: rgba(17,176,103,.18); color: #fff; font-weight: 600;
            border-left: 3px solid var(--green); padding-left: 9px;
        }
        .nav-icon { flex-shrink: 0; width: 22px; text-align: center; font-size: 15px; }
        .nav-label { transition: opacity .25s; }
        .sidebar.collapsed .nav-label { opacity: 0; width: 0; overflow: hidden; }
        .sidebar.collapsed .nav-item { justify-content: center; padding: 12px 0; margin: 2px 0; border-radius: 0; border-left: none; }
        .sidebar.collapsed .nav-item.active { border-left: 3px solid var(--green); }

        /* Sidebar footer */
        .sidebar-foot {
            border-top: 1px solid rgba(255,255,255,.07);
            padding: 10px 8px;
        }
        .sidebar-foot-inner {
            display: flex; align-items: center; gap: 10px;
            padding: 8px; border-radius: 10px;
            color: rgba(255,255,255,.4); font-size: .8rem;
        }
        .sidebar.collapsed .sidebar-foot-inner { justify-content: center; }
        .sidebar.collapsed .sidebar-foot-text  { display: none; }
        .sidebar-foot-text { display: flex; flex-direction: column; min-width: 0; }
        .foot-name  { font-weight: 600; font-size: .82rem; color: rgba(255,255,255,.75); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .foot-role  { font-size: .7rem; color: rgba(255,255,255,.35); }

        /* ── Main content ────────────────────────────────── */
        .main {
            margin-top: 60px; margin-left: 230px;
            min-height: calc(100vh - 60px);
            transition: margin-left .3s ease;
        }
        .sidebar.collapsed ~ .main { margin-left: 58px; }
        .main-body { padding: 28px; }

        /* ── Page header ─────────────────────────────────── */
        .page-header { margin-bottom: 24px; }
        .page-header h1 { font-size: 1.5rem; font-weight: 800; color: var(--text); }
        .page-header p  { font-size: .88rem; color: var(--text-sec); margin-top: 4px; }
        .breadcrumb { font-size: .78rem; color: var(--text-muted); margin-bottom: 6px; }
        .breadcrumb span { color: var(--green); }

        /* ── Responsive ──────────────────────────────────── */
        @media (max-width: 768px) {
            .sidebar { transform: translateX(-100%); width: 260px; }
            .sidebar.mobile-open { transform: translateX(0); }
            .main { margin-left: 0 !important; }
            .user-info { display: none; }
        }
    </style>
</head>
<body>

<!-- ── Top bar ──────────────────────────────────────────────── -->
<div class="topbar">
    <div class="topbar-left">
        <button class="sidebar-toggle" id="sidebarToggle" aria-label="Toggle sidebar">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="12" x2="21" y2="12"></line>
                <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
        </button>
        <a href="index.php" class="topbar-brand">LiteRise</a>
        {$roleBadge}
    </div>
    <div class="topbar-right">
        <div class="topbar-user" id="userMenu" tabindex="0">
            <div class="user-avatar">{$initials}</div>
            <div class="user-info">
                <span class="user-name">{$name}</span>
                <span class="user-email">{$email}</span>
            </div>
            <div class="user-dropdown">
                <a href="../settings/index.php">⚙️ &nbsp;Settings</a>
                <div class="divider"></div>
                <a href="../logout.php" class="logout">🚪 &nbsp;Sign Out</a>
            </div>
        </div>
    </div>
</div>

<!-- ── Sidebar ───────────────────────────────────────────────── -->
<div class="sidebar" id="sidebar">
    <div class="sidebar-inner">
HTML;

    foreach ($navItems as $item) {
        $active = ($item['page'] === $activeNav) ? ' active' : '';
        $href   = htmlspecialchars($item['href'],  ENT_QUOTES, 'UTF-8');
        $label  = htmlspecialchars($item['label'], ENT_QUOTES, 'UTF-8');
        $icon   = $item['icon'];
        echo "        <a href=\"{$href}\" class=\"nav-item{$active}\" data-page=\"{$item['page']}\">\n";
        echo "            <span class=\"nav-icon\">{$icon}</span>\n";
        echo "            <span class=\"nav-label\">{$label}</span>\n";
        echo "        </a>\n";
    }

    echo <<<HTML
    </div>
    <div class="sidebar-foot">
        <div class="sidebar-foot-inner">
            <div class="user-avatar" style="width:28px;height:28px;font-size:11px;flex-shrink:0">{$initials}</div>
            <div class="sidebar-foot-text">
                <span class="foot-name">{$name}</span>
                <span class="foot-role">{$role}</span>
            </div>
        </div>
    </div>
</div>

<!-- ── Main content ──────────────────────────────────────────── -->
<div class="main" id="mainContent">
<div class="main-body">
HTML;
}

function portalFooter(): void
{
    echo <<<HTML
</div><!-- .main-body -->
</div><!-- .main -->

<script>
    // Sidebar toggle
    const sidebar      = document.getElementById('sidebar');
    const mainContent  = document.getElementById('mainContent');
    const toggle       = document.getElementById('sidebarToggle');

    toggle.addEventListener('click', () => {
        if (window.innerWidth <= 768) {
            sidebar.classList.toggle('mobile-open');
        } else {
            sidebar.classList.toggle('collapsed');
        }
    });

    // User dropdown toggle
    const userMenu = document.getElementById('userMenu');
    userMenu.addEventListener('click', () => userMenu.classList.toggle('open'));
    document.addEventListener('click', e => {
        if (!userMenu.contains(e.target)) userMenu.classList.remove('open');
    });

    // Active nav highlight
    const currentPage = window.location.pathname.split('/').pop();
    document.querySelectorAll('.nav-item').forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        }
    });
</script>
HTML;
}
