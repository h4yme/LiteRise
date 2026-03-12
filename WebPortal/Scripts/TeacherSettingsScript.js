/*!
 * TeacherSettingsScript.js
 * LiteRise Web Portal — Teacher Settings Section
 * Handles: Profile, Security, My Students tabs
 */
(function () {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════════════
    var _students         = [];
    var _pendingRequestId = null;
    var _requestModal     = null;

    // ═══════════════════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', function () {
        initSidebarNav();
        initDarkMode();
        loadProfile();

        var reqModalEl = document.getElementById('requestModal');
        if (reqModalEl && window.bootstrap) {
            _requestModal = new window.bootstrap.Modal(reqModalEl);
        }
    });

    // ═══════════════════════════════════════════════════════════════════════
    // SIDEBAR NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════
    function initSidebarNav() {
        var links = document.querySelectorAll('.ts-nav-link, .nav-link[data-section]');

        links.forEach(function (link) {
            link.addEventListener('click', function (e) {
                e.preventDefault();
                var sectionId = this.getAttribute('data-section');
                if (!sectionId) return;

                // Update active link
                links.forEach(function (l) { l.classList.remove('active'); });
                this.classList.add('active');

                // Show section
                var sections = document.querySelectorAll('.ts-section, .settings-section');
                sections.forEach(function (s) { s.classList.remove('active'); });

                var target = document.getElementById('section-' + sectionId);
                if (target) {
                    target.classList.add('active');
                    // Lazy load students tab
                    if (sectionId === 'ts-students' && _students.length === 0) {
                        loadStudents();
                    }
                }
            });
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PROFILE
    // ═══════════════════════════════════════════════════════════════════════
    function loadProfile() {
        fetch('/TeacherSettings/GetProfile')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) {
                    showToast(data.message || 'Failed to load profile.', 'danger');
                    return;
                }
                var u = data.user;

                // Fill form fields
                setVal('tsProfileName',      u.name       || '');
                setVal('tsProfileEmail',     u.email      || '');
                setVal('tsProfileSchool',    u.school     || '');
                setVal('tsProfileLastLogin', u.lastLogin  || '—');

                // Update sidebar avatar
                var avatarEl = document.getElementById('tsAvatar');
                var nameEl   = document.getElementById('tsSidebarName');
                var headerEl = document.getElementById('tsHeaderUserName');

                if (avatarEl && u.name) avatarEl.textContent = initials(u.name);
                if (nameEl   && u.name) nameEl.textContent   = u.name;
                if (headerEl && u.name) headerEl.textContent = u.name;
            })
            .catch(function () {
                showToast('Could not reach the server.', 'danger');
            });
    }

    window.tsSaveProfile = function () {
        var name  = getVal('tsProfileName').trim();
        var email = getVal('tsProfileEmail').trim();

        if (!name)  { showToast('Full name is required.', 'danger');      return; }
        if (!email) { showToast('Email address is required.', 'danger');  return; }
        if (!isValidEmail(email)) { showToast('Enter a valid email address.', 'danger'); return; }

        var btn = document.querySelector('[onclick="tsSaveProfile()"]');
        setLoading(btn, true, 'Saving…');

        fetch('/TeacherSettings/UpdateProfile', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ name: name, email: email })
        })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            setLoading(btn, false, '<i class="fas fa-save me-1"></i> Save Profile');
            if (data.success) {
                showToast(data.message || 'Profile saved.', 'success');
                // Update sidebar name
                var sidebarName = document.getElementById('tsSidebarName');
                var headerName  = document.getElementById('tsHeaderUserName');
                var avatar      = document.getElementById('tsAvatar');
                if (sidebarName) sidebarName.textContent = name;
                if (headerName)  headerName.textContent  = name;
                if (avatar)      avatar.textContent       = initials(name);
            } else {
                showToast(data.message || 'Failed to save profile.', 'danger');
            }
        })
        .catch(function () {
            setLoading(btn, false, '<i class="fas fa-save me-1"></i> Save Profile');
            showToast('Server error. Please try again.', 'danger');
        });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // SECURITY — CHANGE PASSWORD
    // ═══════════════════════════════════════════════════════════════════════
    window.tsChangePassword = function () {
        var current = getVal('tsCurrentPassword');
        var next    = getVal('tsNewPassword');
        var confirm = getVal('tsConfirmPassword');

        if (!current) { showToast('Current password is required.', 'danger');  return; }
        if (!next)    { showToast('New password is required.', 'danger');       return; }
        if (next.length < 8) { showToast('New password must be at least 8 characters.', 'danger'); return; }
        if (next !== confirm) { showToast('Passwords do not match.', 'danger'); return; }
        if (current === next)  { showToast('New password must differ from current.', 'danger'); return; }

        var btn = document.querySelector('[onclick="tsChangePassword()"]');
        setLoading(btn, true, 'Saving…');

        fetch('/TeacherSettings/ChangePassword', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ currentPassword: current, newPassword: next, confirmPassword: confirm })
        })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            setLoading(btn, false, '<i class="fas fa-key me-1"></i> Change Password');
            if (data.success) {
                showToast(data.message || 'Password changed.', 'success');
                setVal('tsCurrentPassword', '');
                setVal('tsNewPassword',     '');
                setVal('tsConfirmPassword', '');
            } else {
                showToast(data.message || 'Failed to change password.', 'danger');
            }
        })
        .catch(function () {
            setLoading(btn, false, '<i class="fas fa-key me-1"></i> Change Password');
            showToast('Server error. Please try again.', 'danger');
        });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // MY STUDENTS
    // ═══════════════════════════════════════════════════════════════════════
    function loadStudents() {
        var tbody = document.getElementById('tsStudentsTableBody');
        if (!tbody) return;

        tbody.innerHTML = '<tr><td colspan="4" class="text-center py-4 text-muted">' +
            '<div class="spinner-border spinner-border-sm me-2" role="status"></div>Loading students…</td></tr>';

        fetch('/TeacherSettings/GetAssignedStudents')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) {
                    tbody.innerHTML = '<tr><td colspan="4" class="text-center text-danger py-3">Failed to load students.</td></tr>';
                    return;
                }
                _students = data.students || [];
                renderStudents(_students);
            })
            .catch(function () {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-danger py-3">Could not reach server.</td></tr>';
            });
    }

    function renderStudents(list) {
        var tbody = document.getElementById('tsStudentsTableBody');
        if (!tbody) return;

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-4">No students assigned.</td></tr>';
            return;
        }

        var html = '';
        list.forEach(function (s) {
            var levelClass = (s.level || '').toLowerCase();
            var levelBadge = s.level
                ? '<span class="level-badge ' + levelClass + '">' + capitalize(s.level) + '</span>'
                : '<span class="text-muted">—</span>';

            var lastActive = formatDate(s.lastActive || s.last_active);

            html += '<tr>' +
                '<td>' + escHtml(s.name || '—') + '</td>' +
                '<td>' + escHtml(s.grade || '—') + '</td>' +
                '<td>' + levelBadge + '</td>' +
                '<td>' + lastActive + '</td>' +
                '</tr>';
        });

        tbody.innerHTML = html;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STUDENT CHANGE REQUEST
    // ═══════════════════════════════════════════════════════════════════════
    window.tsSubmitRequest = function () {
        var studentName = getVal('reqStudentName').trim();
        var actionRadio = document.querySelector('input[name="reqAction"]:checked');
        var action      = actionRadio ? actionRadio.value : 'Add';
        var reason      = getVal('reqReason').trim();

        if (!studentName) { showToast('Student name is required.', 'danger'); return; }
        if (!reason)       { showToast('Please provide a reason.', 'danger'); return; }

        var btn = document.querySelector('[onclick="tsSubmitRequest()"]');
        setLoading(btn, true, 'Submitting…');

        fetch('/TeacherSettings/RequestStudentChange', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ studentName: studentName, action: action, reason: reason })
        })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            setLoading(btn, false, '<i class="fas fa-paper-plane me-1"></i> Submit Request');
            if (data.success) {
                showToast(data.message || 'Request submitted.', 'success');
                setVal('reqStudentName', '');
                setVal('reqReason', '');
                if (_requestModal) _requestModal.hide();
            } else {
                showToast(data.message || 'Failed to submit request.', 'danger');
            }
        })
        .catch(function () {
            setLoading(btn, false, '<i class="fas fa-paper-plane me-1"></i> Submit Request');
            showToast('Server error. Please try again.', 'danger');
        });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // DARK MODE
    // ═══════════════════════════════════════════════════════════════════════
    function initDarkMode() {
        var toggleBtn = document.getElementById('tsToggleDarkMode');
        if (!toggleBtn) return;

        var isDark = localStorage.getItem('literise-dark') === 'true';
        if (isDark) document.body.classList.add('dark-mode');
        updateDarkBtn(toggleBtn, isDark);

        toggleBtn.addEventListener('click', function () {
            var nowDark = document.body.classList.toggle('dark-mode');
            localStorage.setItem('literise-dark', nowDark);
            updateDarkBtn(this, nowDark);
        });
    }

    function updateDarkBtn(btn, isDark) {
        if (!btn) return;
        btn.innerHTML = isDark
            ? '<i class="fas fa-sun"></i> Light Mode'
            : '<i class="fas fa-moon"></i> Dark Mode';
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TOAST
    // ═══════════════════════════════════════════════════════════════════════
    window.showToast = function (message, type) {
        var container = document.getElementById('tsToastContainer');
        if (!container) return;

        type = type || 'success';
        var icon = type === 'success'
            ? '<i class="fas fa-check-circle me-2"></i>'
            : '<i class="fas fa-exclamation-circle me-2"></i>';

        var id    = 'toast-' + Date.now();
        var toast = document.createElement('div');
        toast.id        = id;
        toast.className = 'toast align-items-center text-white bg-' + type + ' border-0 show mb-2';
        toast.setAttribute('role', 'alert');
        toast.setAttribute('aria-live', 'assertive');
        toast.innerHTML =
            '<div class="d-flex">' +
                '<div class="toast-body">' + icon + escHtml(message) + '</div>' +
                '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>' +
            '</div>';

        container.appendChild(toast);

        setTimeout(function () {
            toast.classList.remove('show');
            setTimeout(function () { toast.parentNode && toast.parentNode.removeChild(toast); }, 300);
        }, 3500);
    };

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════════════════════
    function getVal(id) {
        var el = document.getElementById(id);
        return el ? el.value : '';
    }

    function setVal(id, val) {
        var el = document.getElementById(id);
        if (el) el.value = val;
    }

    function setLoading(btn, loading, label) {
        if (!btn) return;
        btn.disabled  = loading;
        btn.innerHTML = loading
            ? '<span class="spinner-border spinner-border-sm me-1" role="status"></span>' + label
            : label;
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function initials(name) {
        if (!name) return 'T';
        var parts = name.trim().split(/\s+/);
        if (parts.length === 1) return parts[0][0].toUpperCase();
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }

    function capitalize(str) {
        if (!str) return str;
        return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        try {
            var d = new Date(dateStr);
            if (isNaN(d.getTime())) return dateStr;
            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        } catch (e) {
            return dateStr;
        }
    }

    function escHtml(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

}());
