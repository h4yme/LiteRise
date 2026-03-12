/*!
 * SettingsScript.js
 * LiteRise Web Portal — Admin Settings Section
 * Handles: Profile, Security, System Config, IRT Parameters, Audit Log
 * Dependencies: Bootstrap 5
 */
(function () {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════
    var PRIMARY_GREEN = '#11B067';
    var ACTIVE_SECTION_KEY = 'settingsActiveSection';

    // ═══════════════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════════════
    var _allAuditEntries = [];
    var _isDarkMode = false;

    // ═══════════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ═══════════════════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', function () {
        initSidebarNav();
        initDarkMode();
        loadProfile();
        loadSystemConfig();
        loadAuditLog();

        // Restore last-visited section from sessionStorage
        var savedSection = sessionStorage.getItem(ACTIVE_SECTION_KEY);
        if (savedSection) {
            switchSection(savedSection);
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
        // Update nav active state
        var links = document.querySelectorAll('.sidebar-nav .nav-link');
        links.forEach(function (link) {
            link.classList.toggle('active', link.getAttribute('data-section') === sectionId);
        });

        // Show/hide content panels
        var panels = document.querySelectorAll('.settings-section');
        panels.forEach(function (panel) {
            panel.classList.toggle('active', panel.id === 'section-' + sectionId);
        });

        sessionStorage.setItem(ACTIVE_SECTION_KEY, sectionId);
    }

    // Expose for audit log refresh button
    window.switchSection = switchSection;

    // ═══════════════════════════════════════════════════════════════════════
    // DARK MODE
    // ═══════════════════════════════════════════════════════════════════════
    function initDarkMode() {
        _isDarkMode = localStorage.getItem('literiseDarkMode') === 'true';
        applyDarkMode(_isDarkMode);

        var btn = document.getElementById('toggleDarkMode');
        if (btn) {
            btn.addEventListener('click', function () {
                _isDarkMode = !_isDarkMode;
                localStorage.setItem('literiseDarkMode', _isDarkMode);
                applyDarkMode(_isDarkMode);
            });
        }
    }

    function applyDarkMode(enabled) {
        document.body.classList.toggle('dark-mode', enabled);
        var btn = document.getElementById('toggleDarkMode');
        if (btn) {
            btn.innerHTML = enabled
                ? '<i class="fas fa-sun"></i> Light Mode'
                : '<i class="fas fa-moon"></i> Dark Mode';
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // PROFILE
    // ═══════════════════════════════════════════════════════════════════════
    function loadProfile() {
        fetch('/Settings/GetProfile')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) return;
                var u = data.user;

                // Form fields
                setVal('profileName',  u.name);
                setVal('profileEmail', u.email);
                setVal('profileRoleDisplay', u.role ? capitalize(u.role) : '');

                // Display elements
                setText('profileDisplayName', u.name);
                setText('profileRole',        capitalize(u.role || 'Administrator'));
                setText('profileLastLogin',   u.lastLogin || '—');

                // Avatars
                var initial = (u.name || 'A').charAt(0).toUpperCase();
                setText('profileAvatar', initial);
                setText('sidebarAvatar', initial);
                setText('sidebarName',   u.name);
                setText('headerUserName', u.name);
            })
            .catch(function (err) {
                console.error('Failed to load profile:', err);
            });
    }

    window.saveProfile = function () {
        var name  = getVal('profileName').trim();
        var email = getVal('profileEmail').trim();

        if (!name) { showToast('Full name is required.', 'danger'); return; }
        if (!email || !isValidEmail(email)) { showToast('Please enter a valid email address.', 'danger'); return; }

        postJSON('/Settings/UpdateProfile', { name: name, email: email })
            .then(function (data) {
                if (data.success) {
                    // Reflect new name in UI
                    setText('profileDisplayName', name);
                    setText('sidebarName', name);
                    setText('headerUserName', name);
                    setText('profileAvatar', name.charAt(0).toUpperCase());
                    setText('sidebarAvatar', name.charAt(0).toUpperCase());
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
        if (newPwd.length < 8) { showToast('New password must be at least 8 characters.', 'danger'); return; }
        if (!confirm) { showToast('Please confirm your new password.', 'danger'); return; }
        if (newPwd !== confirm) { showToast('New password and confirmation do not match.', 'danger'); return; }

        postJSON('/Settings/ChangePassword', {
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
        if (value.length >= 8)             strength++;
        if (/[A-Z]/.test(value))           strength++;
        if (/[0-9]/.test(value))           strength++;
        if (/[^A-Za-z0-9]/.test(value))   strength++;

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
            if (icon) { icon.className = 'fas fa-eye-slash'; }
        } else {
            input.type = 'password';
            if (icon) { icon.className = 'fas fa-eye'; }
        }
    };

    // ═══════════════════════════════════════════════════════════════════════
    // SYSTEM CONFIG
    // ═══════════════════════════════════════════════════════════════════════
    function loadSystemConfig() {
        fetch('/Settings/GetSystemConfig')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) return;
                var c = data.config;

                setVal('cfgPassingThreshold', c.passingThreshold);
                setVal('cfgMaxLessons',       c.maxLessonsBeforePostAssessment);
                setChecked('cfgEnableGameModule',  c.enableGameModule);
                setChecked('cfgEnableBadges',      c.enableBadges);
                setChecked('cfgMaintenanceMode',   c.maintenanceMode);

                // IRT fields (populated in same call)
                setVal('irtMinItems',  c.irtMinItems);
                setVal('irtMaxItems',  c.irtMaxItems);
                setVal('irtSemTarget', c.irtSemTarget);
            })
            .catch(function (err) {
                console.error('Failed to load system config:', err);
            });
    }

    window.saveSystemConfig = function () {
        var threshold = parseInt(getVal('cfgPassingThreshold'), 10);
        var maxLessons = parseInt(getVal('cfgMaxLessons'), 10);

        if (isNaN(threshold) || threshold < 0 || threshold > 100) {
            showToast('Passing threshold must be between 0 and 100.', 'danger'); return;
        }
        if (isNaN(maxLessons) || maxLessons < 1) {
            showToast('Max lessons must be at least 1.', 'danger'); return;
        }

        var payload = {
            passingThreshold:               threshold,
            maxLessonsBeforePostAssessment: maxLessons,
            irtMinItems:                    parseInt(getVal('irtMinItems'), 10) || 10,
            irtMaxItems:                    parseInt(getVal('irtMaxItems'), 10) || 30,
            irtSemTarget:                   parseFloat(getVal('irtSemTarget')) || 0.3,
            enableGameModule:               isChecked('cfgEnableGameModule'),
            enableBadges:                   isChecked('cfgEnableBadges'),
            maintenanceMode:                isChecked('cfgMaintenanceMode')
        };

        postJSON('/Settings/UpdateSystemConfig', payload)
            .then(function (data) {
                showToast(
                    data.success
                        ? (data.message || 'System config saved.')
                        : (data.message || 'Failed to save config.'),
                    data.success ? 'success' : 'danger'
                );
            })
            .catch(function () { showToast('Network error. Please try again.', 'danger'); });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // IRT PARAMETERS
    // ═══════════════════════════════════════════════════════════════════════
    window.saveIrtSettings = function () {
        var minItems  = parseInt(getVal('irtMinItems'), 10);
        var maxItems  = parseInt(getVal('irtMaxItems'), 10);
        var semTarget = parseFloat(getVal('irtSemTarget'));

        if (isNaN(minItems) || minItems < 1) {
            showToast('Minimum items must be at least 1.', 'danger'); return;
        }
        if (isNaN(maxItems) || maxItems < minItems) {
            showToast('Maximum items must be greater than or equal to minimum items.', 'danger'); return;
        }
        if (maxItems > 200) {
            showToast('Maximum items cannot exceed 200.', 'danger'); return;
        }
        if (isNaN(semTarget) || semTarget <= 0 || semTarget > 2.0) {
            showToast('SEM target must be between 0.01 and 2.00.', 'danger'); return;
        }

        var payload = {
            passingThreshold:               parseInt(getVal('cfgPassingThreshold'), 10) || 70,
            maxLessonsBeforePostAssessment: parseInt(getVal('cfgMaxLessons'), 10)       || 65,
            irtMinItems:                    minItems,
            irtMaxItems:                    maxItems,
            irtSemTarget:                   semTarget,
            enableGameModule:               isChecked('cfgEnableGameModule'),
            enableBadges:                   isChecked('cfgEnableBadges'),
            maintenanceMode:                isChecked('cfgMaintenanceMode')
        };

        postJSON('/Settings/UpdateSystemConfig', payload)
            .then(function (data) {
                showToast(
                    data.success
                        ? (data.message || 'IRT settings saved.')
                        : (data.message || 'Failed to save IRT settings.'),
                    data.success ? 'success' : 'danger'
                );
            })
            .catch(function () { showToast('Network error. Please try again.', 'danger'); });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // AUDIT LOG
    // ═══════════════════════════════════════════════════════════════════════
    function loadAuditLog() {
        fetch('/Settings/GetAuditLog')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (!data.success) {
                    renderAuditError('Failed to load audit log.');
                    return;
                }
                _allAuditEntries = data.log || [];
                renderAuditTable(_allAuditEntries);
            })
            .catch(function () {
                renderAuditError('Network error while loading audit log.');
            });
    }

    // Expose for refresh button in view
    window.loadAuditLog = loadAuditLog;

    window.filterAuditLog = function () {
        var search = (getVal('auditSearch') || '').toLowerCase();
        var action = (getVal('auditActionFilter') || '').toLowerCase();

        var filtered = _allAuditEntries.filter(function (entry) {
            var matchSearch = !search ||
                (entry.action    || '').toLowerCase().indexOf(search) !== -1 ||
                (entry.adminName || '').toLowerCase().indexOf(search) !== -1 ||
                (entry.target    || '').toLowerCase().indexOf(search) !== -1 ||
                (entry.ipAddress || '').toLowerCase().indexOf(search) !== -1;

            var matchAction = !action || (entry.action || '').toLowerCase() === action;

            return matchSearch && matchAction;
        });

        renderAuditTable(filtered);
    };

    function renderAuditTable(entries) {
        var tbody = document.getElementById('auditTableBody');
        if (!tbody) return;

        if (!entries || entries.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">' +
                '<i class="fas fa-inbox me-2"></i>No audit entries found.</td></tr>';
            return;
        }

        var rows = entries.map(function (entry) {
            return '<tr>' +
                '<td><span class="action-badge ' + escHtml(entry.action || '') + '">' +
                    escHtml(capitalize(entry.action || '—')) + '</span></td>' +
                '<td>' + escHtml(entry.adminName || '—') + '</td>' +
                '<td>' + escHtml(entry.target    || '—') + '</td>' +
                '<td class="text-nowrap">' + escHtml(entry.timestamp || '—') + '</td>' +
                '<td class="text-nowrap font-monospace">' + escHtml(entry.ipAddress || '—') + '</td>' +
                '</tr>';
        }).join('');

        tbody.innerHTML = rows;
    }

    function renderAuditError(msg) {
        var tbody = document.getElementById('auditTableBody');
        if (tbody) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-4">' +
                '<i class="fas fa-exclamation-circle me-2"></i>' + escHtml(msg) + '</td></tr>';
        }
    }

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

        // Auto-dismiss after 4 seconds
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
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
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

    function setChecked(id, value) {
        var el = document.getElementById(id);
        if (el) el.checked = !!value;
    }

    function isChecked(id) {
        var el = document.getElementById(id);
        return el ? el.checked : false;
    }

    function capitalize(str) {
        if (!str) return '';
        return str.charAt(0).toUpperCase() + str.slice(1);
    }

    function isValidEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function escHtml(str) {
        return String(str)
            .replace(/&/g,  '&amp;')
            .replace(/</g,  '&lt;')
            .replace(/>/g,  '&gt;')
            .replace(/"/g,  '&quot;')
            .replace(/'/g,  '&#39;');
    }

}());
