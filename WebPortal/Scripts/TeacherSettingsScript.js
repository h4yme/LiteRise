/*!
 * TeacherSettingsScript.js
 * LiteRise Web Portal — Teacher Settings Section
 * Handles: Profile, Security, My Students
 * Dependencies: Bootstrap 5
 */
(function () {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════
    var ACTIVE_SECTION_KEY = 'teacherSettingsActiveSection';

    // ═══════════════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════════════
    var _allStudents = [];

    // ═══════════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ═══════════════════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', function () {
        initSidebarNav();
        loadProfile();
        loadAssignedStudents();

        // Restore last-visited section from sessionStorage
        var savedSection = sessionStorage.getItem(ACTIVE_SECTION_KEY);
        if (savedSection) {
            switchSection(savedSection);
        }

        // Clear student request modal form on close
        var modal = document.getElementById('studentRequestModal');
        if (modal) {
            modal.addEventListener('hidden.bs.modal', function () {
                var form = document.getElementById('studentRequestForm');
                if (form) form.reset();
            });
        }
    });

    // ═══════════════════════════════════════════════════════════════════════
    // SIDEBAR NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════
    function initSidebarNav() {
        var links = document.querySelectorAll('.sidebar-nav .nav-link');
        links.forEach(function (link) {
            link.addEventListener('click', function (e) {
                e.preventDefault();
                var section = this.getAttribute('data-section');
                switchSection(section);
            });
        });
    }

    function switchSection(sectionId) {
        var links = document.querySelectorAll('.sidebar-nav .nav-link');
        links.forEach(function (link) {
            link.classList.toggle('active', link.getAttribute('data-section') === sectionId);
        });

        var panels = document.querySelectorAll('.settings-section');
        panels.forEach(function (panel) {
            panel.classList.toggle('active', panel.id === 'section-' + sectionId);
        });

        sessionStorage.setItem(ACTIVE_SECTION_KEY, sectionId);
    }

    window.switchSection = switchSection;

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

                // Form fields
                setVal('profileName',        u.name   || '');
                setVal('profileEmail',       u.email  || '');
                setVal('profileSchoolField', u.school || '');
                setVal('profileGrade',       u.grade  || '');

                // Display elements
                setText('profileDisplayName', u.name   || '—');
                setText('profileSchool',       u.school || '—');
                setText('profileLastLogin',    u.lastLogin || '—');

                // Avatars — show initials
                var initial = initials(u.name || 'T');
                setText('profileAvatar', initial);
                setText('sidebarAvatar', initial);
                setText('sidebarName',   u.name || '');
                setText('headerUserName', u.name || '');
            })
            .catch(function (err) {
                console.error('Failed to load profile:', err);
                showToast('Could not reach the server.', 'danger');
            });
    }

    window.saveProfile = function () {
        var name  = getVal('profileName').trim();
        var email = getVal('profileEmail').trim();

        if (!name)  { showToast('Full name is required.', 'danger'); return; }
        if (name.length < 2) { showToast('Name must be at least 2 characters.', 'danger'); return; }
        if (!email || !isValidEmail(email)) {
            showToast('Please enter a valid email address.', 'danger'); return;
        }

        postJSON('/TeacherSettings/UpdateProfile', { name: name, email: email })
            .then(function (data) {
                if (data.success) {
                    var initial = initials(name);
                    setText('profileDisplayName', name);
                    setText('sidebarName', name);
                    setText('headerUserName', name);
                    setText('profileAvatar', initial);
                    setText('sidebarAvatar', initial);
                    showToast(data.message || 'Profile saved.', 'success');
                } else {
                    showToast(data.message || 'Failed to save profile.', 'danger');
                }
            })
            .catch(function () { showToast('Network error. Please try again.', 'danger'); });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // PASSWORD / SECURITY
    // ═══════════════════════════════════════════════════════════════════════
    window.changePassword = function () {
        var current = getVal('currentPassword');
        var newPwd  = getVal('newPassword');
        var confirm = getVal('confirmPassword');

        if (!current) { showToast('Current password is required.', 'danger'); return; }
        if (!newPwd)  { showToast('New password is required.', 'danger'); return; }
        if (newPwd.length < 8) {
            showToast('New password must be at least 8 characters.', 'danger'); return;
        }
        if (!confirm) { showToast('Please confirm your new password.', 'danger'); return; }
        if (newPwd !== confirm) {
            showToast('New password and confirmation do not match.', 'danger'); return;
        }
        if (current === newPwd) {
            showToast('New password must be different from the current password.', 'danger'); return;
        }

        postJSON('/TeacherSettings/ChangePassword', {
            currentPassword: current,
            newPassword:     newPwd,
            confirmPassword: confirm
        })
            .then(function (data) {
                if (data.success) {
                    document.getElementById('passwordForm').reset();
                    resetStrengthBar();
                    showToast(data.message || 'Password changed successfully.', 'success');
                } else {
                    showToast(data.message || 'Failed to change password.', 'danger');
                }
            })
            .catch(function () { showToast('Network error. Please try again.', 'danger'); });
    };

    window.checkPasswordStrength = function (value) {
        var bar   = document.getElementById('strengthBar');
        var label = document.getElementById('strengthLabel');
        if (!bar || !label) return;

        var strength = 0;
        if (value.length >= 8)           strength++;
        if (/[A-Z]/.test(value))         strength++;
        if (/[0-9]/.test(value))         strength++;
        if (/[^A-Za-z0-9]/.test(value)) strength++;

        var pct, color, text, cls;
        if (strength <= 1) {
            pct = 25; color = '#ef4444'; text = 'Weak'; cls = 'weak';
        } else if (strength === 2) {
            pct = 50; color = '#f59e0b'; text = 'Fair'; cls = 'medium';
        } else if (strength === 3) {
            pct = 75; color = '#3b82f6'; text = 'Good'; cls = 'medium';
        } else {
            pct = 100; color = '#22c55e'; text = 'Strong'; cls = 'strong';
        }

        bar.style.width      = pct + '%';
        bar.style.background = color;
        label.textContent    = value.length > 0 ? text : '';
        label.className      = 'strength-label ' + (value.length > 0 ? cls : '');
    };

    function resetStrengthBar() {
        var bar   = document.getElementById('strengthBar');
        var label = document.getElementById('strengthLabel');
        if (bar)   { bar.style.width = '0'; bar.style.background = ''; }
        if (label) { label.textContent = ''; label.className = 'strength-label'; }
    }

    window.toggleVisibility = function (inputId, btn) {
        var input = document.getElementById(inputId);
        if (!input) return;
        var icon = btn.querySelector('i');
        if (input.type === 'password') {
            input.type = 'text';
            if (icon) icon.className = 'fas fa-eye-slash';
        } else {
            input.type = 'password';
            if (icon) icon.className = 'fas fa-eye';
        }
    };

    // ═══════════════════════════════════════════════════════════════════════
    // ASSIGNED STUDENTS
    // ═══════════════════════════════════════════════════════════════════════
    function loadAssignedStudents() {
        fetch('/TeacherSettings/GetAssignedStudents')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) {
                    renderStudentsError('Failed to load students.');
                    return;
                }
                _allStudents = data.students || [];
                renderStudentsTable(_allStudents);
            })
            .catch(function () {
                renderStudentsError('Network error while loading students.');
            });
    }

    window.filterStudents = function () {
        var search = (getVal('studentSearch') || '').toLowerCase();

        var filtered = _allStudents.filter(function (s) {
            return !search ||
                (s.name       || '').toLowerCase().indexOf(search) !== -1 ||
                (s.grade      || '').toLowerCase().indexOf(search) !== -1 ||
                (s.level      || '').toLowerCase().indexOf(search) !== -1 ||
                (s.lastActive || '').toLowerCase().indexOf(search) !== -1;
        });

        renderStudentsTable(filtered);
    };

    function renderStudentsTable(students) {
        var tbody = document.getElementById('studentsTableBody');
        var empty = document.getElementById('studentsEmpty');
        if (!tbody) return;

        if (!students || students.length === 0) {
            tbody.innerHTML = '';
            if (empty) empty.style.display = 'block';
            return;
        }

        if (empty) empty.style.display = 'none';

        var rows = students.map(function (s) {
            var levelClass = getLevelClass(s.level);
            var lastActive = formatDate(s.lastActive || s.last_active);
            return '<tr>' +
                '<td>' +
                    '<span class="student-avatar-mini">' + escHtml(initials(s.name || '?')) + '</span>' +
                    ' ' + escHtml(s.name || '—') +
                '</td>' +
                '<td>' + escHtml(s.grade || '—') + '</td>' +
                '<td><span class="level-badge ' + levelClass + '">' + escHtml(capitalize(s.level || '—')) + '</span></td>' +
                '<td class="text-nowrap">' + escHtml(lastActive) + '</td>' +
                '</tr>';
        }).join('');

        tbody.innerHTML = rows;
    }

    function renderStudentsError(msg) {
        var tbody = document.getElementById('studentsTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center text-danger py-4">' +
                '<i class="fas fa-exclamation-circle me-2"></i>' + escHtml(msg) + '</td></tr>';
        }
    }

    function getLevelClass(level) {
        if (!level) return '';
        switch (level.toLowerCase()) {
            case 'beginner':     return 'level-beginner';
            case 'intermediate': return 'level-intermediate';
            case 'advanced':     return 'level-advanced';
            default:             return '';
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STUDENT CHANGE REQUEST
    // ═══════════════════════════════════════════════════════════════════════
    window.submitStudentRequest = function (event) {
        event.preventDefault();

        var studentName = getVal('reqStudentName').trim();
        var actionEl    = document.querySelector('input[name="reqAction"]:checked');
        var action      = actionEl ? actionEl.value : '';
        var reason      = getVal('reqReason').trim();

        if (!studentName) { showToast('Student name is required.', 'danger'); return; }
        if (!action)      { showToast('Please select an action.', 'danger'); return; }
        if (!reason || reason.length < 10) {
            showToast('Please provide a more detailed reason (at least 10 characters).', 'danger');
            return;
        }

        // Disable submit while in flight
        var submitBtn = document.querySelector('#studentRequestModal .btn-primary-custom');
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i> Submitting...';
        }

        postJSON('/TeacherSettings/RequestStudentChange', {
            studentName: studentName,
            action:      action,
            reason:      reason
        })
            .then(function (data) {
                if (data.success) {
                    var modalEl = document.getElementById('studentRequestModal');
                    if (modalEl && window.bootstrap) {
                        var bsModal = bootstrap.Modal.getInstance(modalEl);
                        if (bsModal) bsModal.hide();
                    }
                    showToast(data.message || 'Request submitted to admin.', 'success');
                } else {
                    showToast(data.message || 'Failed to submit request.', 'danger');
                }
            })
            .catch(function () {
                showToast('Network error. Please try again.', 'danger');
            })
            .finally(function () {
                if (submitBtn) {
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-paper-plane me-1"></i> Submit Request';
                }
            });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // TOAST NOTIFICATIONS
    // ═══════════════════════════════════════════════════════════════════════
    /**
     * showToast(message, type)
     * type: 'success' | 'danger' | 'info'
     */
    window.showToast = function (message, type) {
        var container = document.getElementById('toastContainer');
        if (!container) return;

        var toast = document.createElement('div');
        toast.className = 'toast ' + (type || 'info');
        toast.setAttribute('role', 'alert');
        toast.innerHTML =
            '<span class="toast-icon">' +
                (type === 'success' ? '<i class="fas fa-check-circle"></i>' :
                 type === 'danger'  ? '<i class="fas fa-times-circle"></i>' :
                                      '<i class="fas fa-info-circle"></i>') +
            '</span>' +
            '<span class="toast-msg">' + escHtml(message) + '</span>';

        container.appendChild(toast);

        setTimeout(function () {
            toast.style.animation = 'fadeOut 0.3s ease forwards';
            setTimeout(function () {
                if (toast.parentNode) toast.parentNode.removeChild(toast);
            }, 300);
        }, 4000);
    };

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════════════════════
    function postJSON(url, data) {
        return fetch(url, {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(data)
        }).then(function (r) {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.json();
        });
    }

    function getVal(id) {
        var el = document.getElementById(id);
        return el ? el.value : '';
    }

    function setVal(id, value) {
        var el = document.getElementById(id);
        if (el) el.value = (value !== null && value !== undefined) ? value : '';
    }

    function setText(id, text) {
        var el = document.getElementById(id);
        if (el) el.textContent = text || '';
    }

    function initials(name) {
        if (!name) return 'T';
        var parts = name.trim().split(/\s+/);
        if (parts.length === 1) return parts[0][0].toUpperCase();
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }

    function capitalize(str) {
        if (!str) return '';
        return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        try {
            var d = new Date(dateStr);
            if (isNaN(d.getTime())) return String(dateStr);
            return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        } catch (e) {
            return String(dateStr);
        }
    }

    function escHtml(str) {
        return String(str == null ? '' : str)
            .replace(/&/g,  '&amp;')
            .replace(/</g,  '&lt;')
            .replace(/>/g,  '&gt;')
            .replace(/"/g,  '&quot;')
            .replace(/'/g,  '&#39;');
    }

}());
