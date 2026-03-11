/* LiteRise — layout.js
   Handles: theme toggle, sidebar, dropdowns, logout modal */

(function () {
    'use strict';

    // ── Theme ──────────────────────────────────────────────────
    var themeToggle = document.getElementById('themeToggle');
    var savedTheme  = localStorage.getItem('lr-theme');

    if (savedTheme === 'dark') document.body.classList.add('dark-mode');

    if (themeToggle) {
        themeToggle.addEventListener('click', function () {
            document.body.classList.toggle('dark-mode');
            localStorage.setItem('lr-theme',
                document.body.classList.contains('dark-mode') ? 'dark' : 'light');
        });
    }

    // ── Sidebar toggle (desktop collapse) ─────────────────────
    var sidebar       = document.getElementById('sidebar');
    var mainContainer = document.querySelector('.Main-Container');
    var sidebarToggle = document.getElementById('sidebarToggle');
    var collapsed     = localStorage.getItem('lr-sidebar') === 'collapsed';

    function applySidebarState() {
        if (collapsed) {
            document.body.classList.add('sidebar-collapsed');
        } else {
            document.body.classList.remove('sidebar-collapsed');
        }
    }
    applySidebarState();

    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', function () {
            collapsed = !collapsed;
            localStorage.setItem('lr-sidebar', collapsed ? 'collapsed' : 'expanded');
            applySidebarState();
        });
    }

    // ── Mobile sidebar overlay ─────────────────────────────────
    var mobileToggle = document.getElementById('mobileSidebarToggle');
    var overlay      = document.createElement('div');
    overlay.className = 'sidebar-overlay';
    document.body.appendChild(overlay);

    function openMobileSidebar() {
        sidebar && sidebar.classList.add('mobile-open');
        overlay.classList.add('active');
    }
    function closeMobileSidebar() {
        sidebar && sidebar.classList.remove('mobile-open');
        overlay.classList.remove('active');
    }

    if (mobileToggle) {
        mobileToggle.addEventListener('click', openMobileSidebar);
    }
    overlay.addEventListener('click', closeMobileSidebar);

    // ── Dropdowns (notifications + settings) ──────────────────
    function setupDropdown(toggleId, panelId) {
        var btn   = document.getElementById(toggleId);
        var panel = document.getElementById(panelId);
        if (!btn || !panel) return;

        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            var isOpen = panel.classList.contains('show');
            closeAllDropdowns();
            if (!isOpen) {
                panel.classList.add('show');
                btn.setAttribute('aria-expanded', 'true');
            }
        });
    }

    function closeAllDropdowns() {
        ['notificationsPanel', 'settingsDropdown'].forEach(function (id) {
            var el = document.getElementById(id);
            if (el) el.classList.remove('show');
        });
        ['notificationsToggle', 'settingsToggle'].forEach(function (id) {
            var el = document.getElementById(id);
            if (el) el.setAttribute('aria-expanded', 'false');
        });
    }

    setupDropdown('notificationsToggle', 'notificationsPanel');
    setupDropdown('settingsToggle', 'settingsDropdown');

    document.addEventListener('click', function (e) {
        if (!e.target.closest('.dropdown-container')) {
            closeAllDropdowns();
        }
    });

    // ── Logout modal ───────────────────────────────────────────
    var logoutLink    = document.getElementById('logoutLink');
    var logoutModal   = document.getElementById('logoutModal');
    var modalClose    = document.getElementById('logoutModalClose');
    var btnCancel     = document.getElementById('btnLogoutCancel');
    var btnConfirm    = document.getElementById('btnLogoutConfirm');

    function openLogoutModal() {
        if (logoutModal) logoutModal.classList.add('show');
    }
    function closeLogoutModal() {
        if (logoutModal) logoutModal.classList.remove('show');
    }

    if (logoutLink)  logoutLink.addEventListener('click', openLogoutModal);
    if (modalClose)  modalClose.addEventListener('click', closeLogoutModal);
    if (btnCancel)   btnCancel.addEventListener('click', closeLogoutModal);
    if (btnConfirm)  btnConfirm.addEventListener('click', function () {
        var url = (typeof LOGOUT_URL !== 'undefined') ? LOGOUT_URL : '/Login/Logout';
        window.location.href = url;
    });

    if (logoutModal) {
        logoutModal.addEventListener('click', function (e) {
            if (e.target === logoutModal) closeLogoutModal();
        });
    }

})();
