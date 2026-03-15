/**
 * AdministrationScript.js
 * LiteRise Web Portal — Admin Account Management
 */

'use strict';

window.allAdmins  = [];
window.allSchools = [];
var _pendingDeactivateId = null;

// ── Load schools for modal dropdown ──────────────────────────────────────────
function loadSchools() {
    fetch('/Administration/GetSchools', { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(function (res) {
            if (!res.ok) return;
            return res.json();
        })
        .then(function (data) {
            if (!data) return;
            window.allSchools = Array.isArray(data) ? data : [];
            populateSchoolDropdown();
        })
        .catch(function (err) {
            console.warn('loadSchools failed:', err);
        });
}

function populateSchoolDropdown(selectedId) {
    var select = document.getElementById('aSchool');
    if (!select) return;
    select.innerHTML = '<option value="">-- Select school --</option>';
    window.allSchools.forEach(function (sc) {
        var id   = sc.school_id   || sc.SchoolID   || sc.id   || '';
        var name = sc.school_name || sc.SchoolName || '';
        var opt  = document.createElement('option');
        opt.value       = id;
        opt.textContent = name;
        if (String(id) === String(selectedId)) opt.selected = true;
        select.appendChild(opt);
    });
}

// ── Fetch & load accounts ─────────────────────────────────────────────────────
function loadAdmins() {
    fetch('/Administration/GetAdmins', { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(function (res) {
            if (!res.ok) throw new Error('Server responded ' + res.status);
            return res.json();
        })
        .then(function (data) {
            window.allAdmins = Array.isArray(data) ? data : [];
            updateStats(window.allAdmins);
            filterAdmins();
        })
        .catch(function (err) {
            console.error('loadAdmins failed:', err);
            showToast('Failed to load accounts. Please refresh the page.', 'danger');
            window.allAdmins = [];
            renderAdmins([]);
        });
}

// ── Stats ─────────────────────────────────────────────────────────────────────
function updateStats(list) {
    var totalAdmins    = list.filter(function (a) { return (a.role || '').toLowerCase() === 'admin';   }).length;
    var totalTeachers  = list.filter(function (a) { return (a.role || '').toLowerCase() === 'teacher'; }).length;
    var activeAccounts = list.filter(function (a) { return a.isActive || a.status === 'Active';        }).length;
    setText('statTotalAdmins',    totalAdmins);
    setText('statTotalTeachers',  totalTeachers);
    setText('statActiveAccounts', activeAccounts);
}

function setText(id, val) {
    var el = document.getElementById(id);
    if (el) el.textContent = val;
}

// ── Filter ────────────────────────────────────────────────────────────────────
function filterAdmins() {
    var searchEl = document.getElementById('adminSearch');
    var roleEl   = document.getElementById('adminRoleFilter');
    var statusEl = document.getElementById('statusFilter');

    var search     = searchEl  ? searchEl.value.toLowerCase().trim()  : '';
    var roleFilter = roleEl    ? roleEl.value.toLowerCase()           : '';
    var statusFlt  = statusEl  ? statusEl.value.toLowerCase()         : '';

    var filtered = window.allAdmins.filter(function (a) {
        var name   = (a.name   || a.fullName || '').toLowerCase();
        var email  = (a.email  || '').toLowerCase();
        var role   = (a.role   || '').toLowerCase();
        var active = a.isActive || a.status === 'Active';
        var status = active ? 'active' : 'inactive';

        var matchSearch = !search     || name.includes(search)  || email.includes(search);
        var matchRole   = !roleFilter || role === roleFilter;
        var matchStatus = !statusFlt  || status === statusFlt;

        return matchSearch && matchRole && matchStatus;
    });

    renderAdmins(filtered);
}

// ── Avatar ────────────────────────────────────────────────────────────────────
function getInitials(name) {
    var parts = (name || '?').trim().split(/\s+/);
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

function avatarHtml(admin) {
    var name     = admin.name || admin.fullName || '?';
    var role     = (admin.role || '').toLowerCase();
    var bg       = role === 'admin' ? '#0d6efd' : '#11B067';
    var initials = getInitials(name);
    return '<div style="width:36px;height:36px;border-radius:50%;background:' + bg + ';color:#fff;'
         + 'display:flex;align-items:center;justify-content:center;'
         + 'font-weight:700;font-size:0.8rem;flex-shrink:0;">' + initials + '</div>';
}

// ── Date formatter ────────────────────────────────────────────────────────────
function formatDate(dateStr) {
    if (!dateStr) return 'Never';
    var d = new Date(dateStr);
    if (isNaN(d.getTime())) return 'Never';
    return d.toLocaleDateString('en-AU', { day: '2-digit', month: 'short', year: 'numeric' });
}

// ── Escape helpers ────────────────────────────────────────────────────────────
function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function escAttr(str) {
    return String(str).replace(/&/g, '&amp;').replace(/"/g, '&quot;');
}

// ── Render table ──────────────────────────────────────────────────────────────
function renderAdmins(list) {
    var tbody = document.getElementById('adminsTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!list.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">No accounts found.</td></tr>';
        return;
    }

    list.forEach(function (admin) {
        var id     = admin.id     || admin.userId    || '';
        var name   = admin.name   || admin.fullName  || 'N/A';
        var email  = admin.email  || 'N/A';
        var role   = admin.role   || 'N/A';
        var school = admin.school || admin.schoolName || null;
        var active = admin.isActive !== undefined ? admin.isActive : (admin.status === 'Active');

        var roleIsAdmin = role.toLowerCase() === 'admin';

        var roleBadge = roleIsAdmin
            ? '<span class="badge bg-primary">Admin</span>'
            : '<span class="badge bg-success">Teacher</span>';

        var statusBadge = active
            ? '<span class="badge bg-success">Active</span>'
            : '<span class="badge bg-secondary">Inactive</span>';

        var schoolCell = roleIsAdmin ? 'N/A' : (school || 'N/A');

        var toggleBtn = active
            ? '<button class="btn btn-sm btn-outline-danger ms-1" data-action="deactivate" data-id="' + escAttr(id) + '">'
              + '<i class="fas fa-ban"></i> Deactivate</button>'
            : '<button class="btn btn-sm btn-outline-success ms-1" data-action="reactivate" data-id="' + escAttr(id) + '">'
              + '<i class="fas fa-check-circle"></i> Reactivate</button>';

        var row = '<tr>'
            + '<td><div class="d-flex align-items-center gap-2">'
            + avatarHtml(admin)
            + '<div><div class="fw-semibold">' + escHtml(name) + '</div>'
            + '<div class="text-muted small">' + escHtml(email) + '</div></div>'
            + '</div></td>'
            + '<td class="align-middle">' + escHtml(email) + '</td>'
            + '<td class="align-middle">' + roleBadge + '</td>'
            + '<td class="align-middle">' + escHtml(schoolCell) + '</td>'
            + '<td class="align-middle">' + formatDate(admin.lastLogin || admin.last_login) + '</td>'
            + '<td class="align-middle">' + statusBadge + '</td>'
            + '<td class="align-middle">'
            + '<button class="btn btn-sm btn-outline-primary" data-action="edit" data-id="' + escAttr(id) + '">'
            + '<i class="fas fa-pencil-alt"></i> Edit</button>'
            + toggleBtn
            + '</td>'
            + '</tr>';

        tbody.insertAdjacentHTML('beforeend', row);
    });
}

// ── Add account modal ─────────────────────────────────────────────────────────
function openAddAdmin() {
    document.getElementById('aId').value       = '';
    document.getElementById('aName').value     = '';
    document.getElementById('aEmail').value    = '';
    document.getElementById('aPassword').value = '';
    document.getElementById('aRole').value     = '';

    var title = document.getElementById('adminModalTitleText');
    if (title) title.textContent = 'Add Account';

    var schoolGroup = document.getElementById('aSchoolGroup');
    if (schoolGroup) schoolGroup.style.display = 'none';

    populateSchoolDropdown();

    var modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('adminModal'));
    modal.show();
}

// ── Edit account modal ────────────────────────────────────────────────────────
function openEditAdmin(id) {
    var admin = null;
    for (var i = 0; i < window.allAdmins.length; i++) {
        if (String(window.allAdmins[i].id || window.allAdmins[i].userId) === String(id)) {
            admin = window.allAdmins[i];
            break;
        }
    }

    if (!admin) {
        showToast('Account not found.', 'danger');
        return;
    }

    document.getElementById('aId').value       = admin.id    || admin.userId   || '';
    document.getElementById('aName').value     = admin.name  || admin.fullName || '';
    document.getElementById('aEmail').value    = admin.email || '';
    document.getElementById('aPassword').value = '';
    document.getElementById('aRole').value     = admin.role  || '';

    var title = document.getElementById('adminModalTitleText');
    if (title) title.textContent = 'Edit Account';

    var schoolEntry = null;
    for (var j = 0; j < window.allSchools.length; j++) {
        var s = window.allSchools[j];
        if ((s.school_name || s.SchoolName || '') === (admin.school || '')) {
            schoolEntry = s;
            break;
        }
    }
    var schoolId = schoolEntry ? (schoolEntry.school_id || schoolEntry.SchoolID || '') : '';
    populateSchoolDropdown(schoolId);
    handleAdminRoleChange();

    var modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('adminModal'));
    modal.show();
}

// ── Role change ───────────────────────────────────────────────────────────────
function handleAdminRoleChange() {
    var roleEl      = document.getElementById('aRole');
    var schoolGroup = document.getElementById('aSchoolGroup');
    if (!schoolGroup) return;
    var role = roleEl ? roleEl.value.toLowerCase() : '';
    schoolGroup.style.display = role === 'teacher' ? '' : 'none';
}

// ── Save account ──────────────────────────────────────────────────────────────
function saveAdmin() {
    var id       = document.getElementById('aId').value.trim();
    var name     = document.getElementById('aName').value.trim();
    var email    = document.getElementById('aEmail').value.trim();
    var password = document.getElementById('aPassword').value;
    var role     = document.getElementById('aRole').value.trim();
    var schoolEl = document.getElementById('aSchool');
    var school   = schoolEl ? schoolEl.value.trim() : '';

    if (!name) {
        showToast('Name is required.', 'warning');
        return;
    }
    if (!email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
        showToast('A valid email address is required.', 'warning');
        return;
    }
    if (!role) {
        showToast('Role is required.', 'warning');
        return;
    }

    var payload = { id: id, name: name, email: email, password: password, role: role, school: school };

    postJson('/Administration/SaveAdmin', payload)
        .then(function (res) {
            if (!res.ok) throw new Error('Server responded ' + res.status);
            return res.json();
        })
        .then(function (result) {
            if (result && result.success === false) throw new Error(result.message || 'Save failed.');
            var modal = bootstrap.Modal.getInstance(document.getElementById('adminModal'));
            if (modal) modal.hide();
            showToast(id ? 'Account updated successfully.' : 'Account created successfully.', 'success');
            loadAdmins();
        })
        .catch(function (err) {
            console.error('saveAdmin failed:', err);
            showToast('Failed to save account: ' + err.message, 'danger');
        });
}

// ── Deactivate ────────────────────────────────────────────────────────────────
function deactivateAdmin(id, name) {
    Swal.fire({
        title: 'Deactivate Account',
        html: 'Deactivate account for: <strong>' + escHtml(name) + '</strong><br>'
            + '<small style="color:#6b7280">They will not be able to log in. You can reactivate at any time.</small>',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#DC2626',
        cancelButtonColor: '#6b7280',
        confirmButtonText: 'Confirm Deactivate',
        cancelButtonText: 'Cancel'
    }).then(function (result) {
        if (result.isConfirmed) {
            _pendingDeactivateId = id;
            confirmDeactivate();
        }
    });
}

function confirmDeactivate() {
    var id = _pendingDeactivateId;
    if (!id) return;

    postJson('/Administration/DeactivateAdmin', { id: id })
        .then(function (res) {
            if (!res.ok) throw new Error('Server responded ' + res.status);
            return res.json();
        })
        .then(function () {
            showToast('Account deactivated.', 'success');
            _pendingDeactivateId = null;
            loadAdmins();
        })
        .catch(function (err) {
            console.error('confirmDeactivate failed:', err);
            showToast('Failed to deactivate: ' + err.message, 'danger');
        });
}

// ── Reactivate ────────────────────────────────────────────────────────────────
function reactivateAdmin(id) {
    postJson('/Administration/ReactivateAdmin', { id: id })
        .then(function (res) {
            if (!res.ok) throw new Error('Server responded ' + res.status);
            return res.json();
        })
        .then(function () {
            showToast('Account reactivated.', 'success');
            loadAdmins();
        })
        .catch(function (err) {
            console.error('reactivateAdmin failed:', err);
            showToast('Failed to reactivate: ' + err.message, 'danger');
        });
}

// ── HTTP helper ───────────────────────────────────────────────────────────────
function postJson(url, data) {
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify(data)
    });
}

// ── Toast ─────────────────────────────────────────────────────────────────────
function showToast(message, type) {
    type = type || 'info';
    var bgMap = {
        success: 'bg-success text-white',
        danger:  'bg-danger text-white',
        warning: 'bg-warning text-dark',
        info:    'bg-info text-dark'
    };
    var bg = bgMap[type] || 'bg-secondary text-white';

    var container = document.getElementById('adminToastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'adminToastContainer';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '1100';
        document.body.appendChild(container);
    }

    var toastId = 'toast_' + Date.now();
    var html = '<div id="' + toastId + '" class="toast align-items-center ' + bg + ' border-0" role="alert" aria-live="assertive" aria-atomic="true">'
             + '<div class="d-flex"><div class="toast-body">' + message + '</div>'
             + '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>'
             + '</div></div>';

    container.insertAdjacentHTML('beforeend', html);
    var el    = document.getElementById(toastId);
    var toast = new bootstrap.Toast(el, { delay: 4000 });
    toast.show();
    el.addEventListener('hidden.bs.toast', function () { el.remove(); });
}

// ── Bind events ───────────────────────────────────────────────────────────────
function bindEvents() {
    var searchEl = document.getElementById('adminSearch');
    if (searchEl) searchEl.addEventListener('input', filterAdmins);

    var roleFilterEl = document.getElementById('adminRoleFilter');
    if (roleFilterEl) roleFilterEl.addEventListener('change', filterAdmins);

    var statusEl = document.getElementById('statusFilter');
    if (statusEl) statusEl.addEventListener('change', filterAdmins);

    var addBtn = document.getElementById('addAdminBtn');
    if (addBtn) addBtn.addEventListener('click', openAddAdmin);

    var saveBtn = document.getElementById('saveAdminBtn');
    if (saveBtn) saveBtn.addEventListener('click', saveAdmin);

    var roleEl = document.getElementById('aRole');
    if (roleEl) roleEl.addEventListener('change', handleAdminRoleChange);

    // Delegated click handler for table action buttons
    var tbody = document.getElementById('adminsTableBody');
    if (tbody) {
        tbody.addEventListener('click', function (e) {
            var btn = e.target.closest('[data-action]');
            if (!btn) return;
            var action = btn.getAttribute('data-action');
            var id     = btn.getAttribute('data-id');
            var admin  = null;
            for (var i = 0; i < window.allAdmins.length; i++) {
                if (String(window.allAdmins[i].id || window.allAdmins[i].userId) === String(id)) {
                    admin = window.allAdmins[i];
                    break;
                }
            }
            if (action === 'edit')       openEditAdmin(id);
            if (action === 'deactivate') deactivateAdmin(id, admin ? (admin.name || admin.fullName || '') : '');
            if (action === 'reactivate') reactivateAdmin(id);
        });
    }
}

// ── Entry point ───────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
    bindEvents();
    loadSchools();
    loadAdmins();
});
