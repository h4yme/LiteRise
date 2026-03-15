/**
 * AdministrationScript.js
 * LiteRise Web Portal — Admin Account Management
 */

'use strict';

// ─── State ────────────────────────────────────────────────────────────────────
window.allAdmins  = [];
window.allSchools = [];
let _pendingDeactivateId = null;

// ─── Load schools for the modal dropdown ──────────────────────────────────────
async function loadSchools() {
    try {
        const res  = await fetch('/Administration/GetSchools', { headers: { 'X-Requested-With': 'XMLHttpRequest' } });
        if (!res.ok) return;
        const data = await res.json();
        window.allSchools = Array.isArray(data) ? data : [];
        populateSchoolDropdown();
    } catch (err) {
        console.warn('[AdministrationScript] loadSchools failed:', err);
    }
}

function populateSchoolDropdown(selectedId) {
    const select = document.getElementById('aSchool');
    if (!select) return;
    select.innerHTML = '<option value="">— Select school —</option>';
    window.allSchools.forEach(function(sc) {
        const id   = sc.school_id || sc.SchoolID || sc.id || '';
        const name = sc.school_name || sc.SchoolName || '';
        const opt  = document.createElement('option');
        opt.value       = id;
        opt.textContent = name;
        if (String(id) === String(selectedId)) opt.selected = true;
        select.appendChild(opt);
    });
}

// ─── Fetch & load ─────────────────────────────────────────────────────────────
async function loadAdmins() {
    try {
        const res = await fetch('/Administration/GetAdmins', {
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        });

        if (!res.ok) throw new Error(`Server responded ${res.status}`);

        const data = await res.json();
        window.allAdmins = Array.isArray(data) ? data : [];
        updateStats(window.allAdmins);
        filterAdmins();
    } catch (err) {
        console.error('[AdministrationScript] loadAdmins failed:', err);
        showToast('Failed to load accounts. Please refresh the page.', 'danger');
        window.allAdmins = [];
        renderAdmins([]);
    }
}

// ─── Stats ────────────────────────────────────────────────────────────────────
function updateStats(list) {
    const totalAdmins    = list.filter(a => (a.role || '').toLowerCase() === 'admin').length;
    const totalTeachers  = list.filter(a => (a.role || '').toLowerCase() === 'teacher').length;
    const activeAccounts = list.filter(a => a.isActive || a.status === 'Active').length;

    setText('statTotalAdmins',    totalAdmins);
    setText('statTotalTeachers',  totalTeachers);
    setText('statActiveAccounts', activeAccounts);
}

function setText(id, val) {
    const el = document.getElementById(id);
    if (el) el.textContent = val;
}

// ─── Filter ───────────────────────────────────────────────────────────────────
function filterAdmins() {
    const search     = (document.getElementById('adminSearch')?.value     || '').toLowerCase().trim();
    const roleFilter = (document.getElementById('adminRoleFilter')?.value || '').toLowerCase();
    const statusFlt  = (document.getElementById('statusFilter')?.value    || '').toLowerCase();

    const filtered = window.allAdmins.filter(a => {
        const name   = (a.name   || a.fullName || '').toLowerCase();
        const email  = (a.email  || '').toLowerCase();
        const role   = (a.role   || '').toLowerCase();
        const active = a.isActive || a.status === 'Active';
        const status = active ? 'active' : 'inactive';

        const matchSearch = !search || name.includes(search) || email.includes(search);
        const matchRole   = !roleFilter || role === roleFilter;
        const matchStatus = !statusFlt  || status === statusFlt;

        return matchSearch && matchRole && matchStatus;
    });

    renderAdmins(filtered);
}

// ─── Avatar helper ────────────────────────────────────────────────────────────
function getInitials(name) {
    const parts = (name || '?').trim().split(/\s+/);
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

function avatarHtml(admin) {
    const name  = admin.name || admin.fullName || '?';
    const role  = (admin.role || '').toLowerCase();
    const bg    = role === 'admin' ? '#0d6efd' : '#11B067';
    const initials = getInitials(name);
    return `<div style="width:36px;height:36px;border-radius:50%;background:${bg};color:#fff;
                display:flex;align-items:center;justify-content:center;
                font-weight:700;font-size:0.8rem;flex-shrink:0;">${initials}</div>`;
}

// ─── Date formatter ───────────────────────────────────────────────────────────
function formatDate(dateStr) {
    if (!dateStr) return 'Never';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return 'Never';
    return d.toLocaleDateString('en-AU', { day: '2-digit', month: 'short', year: 'numeric' });
}

// ─── Render table ─────────────────────────────────────────────────────────────
function renderAdmins(list) {
    const tbody = document.getElementById('adminsTableBody');
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!list.length) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">No accounts found.</td></tr>`;
        return;
    }

    list.forEach(admin => {
        const id     = admin.id || admin.userId || '';
        const name   = admin.name || admin.fullName || '—';
        const email  = admin.email || '—';
        const role   = admin.role || '—';
        const school = admin.school || admin.schoolName || null;
        const active = admin.isActive !== undefined ? admin.isActive : (admin.status === 'Active');

        const roleIsAdmin = role.toLowerCase() === 'admin';
        const roleBadge   = roleIsAdmin
            ? `<span class="badge bg-primary">Admin</span>`
            : `<span class="badge bg-success">Teacher</span>`;

        const statusBadge = active
            ? `<span class="badge bg-success">Active</span>`
            : `<span class="badge bg-secondary">Inactive</span>`;

        const schoolCell = roleIsAdmin ? '—' : (school || '—');

        const safeName = (name || '').replace(/\\/g, '\\\\').replace(/'/g, "\\'");
        const deactivateBtn = active
            ? `<button class="btn btn-sm btn-outline-danger ms-1" onclick="deactivateAdmin('${id}', '${safeName}')">
                   <i class="bi bi-slash-circle"></i> Deactivate
               </button>`
            : `<button class="btn btn-sm btn-outline-success ms-1" onclick="reactivateAdmin('${id}')">
                   <i class="bi bi-check-circle"></i> Reactivate
               </button>`;

        tbody.insertAdjacentHTML('beforeend', `
            <tr>
                <td>
                    <div class="d-flex align-items-center gap-2">
                        ${avatarHtml(admin)}
                        <div>
                            <div class="fw-semibold">${escHtml(name)}</div>
                            <div class="text-muted small">${escHtml(email)}</div>
                        </div>
                    </div>
                </td>
                <td class="align-middle">${escHtml(email)}</td>
                <td class="align-middle">${roleBadge}</td>
                <td class="align-middle">${escHtml(schoolCell)}</td>
                <td class="align-middle">${formatDate(admin.lastLogin || admin.last_login)}</td>
                <td class="align-middle">${statusBadge}</td>
                <td class="align-middle">
                    <button class="btn btn-sm btn-outline-primary" onclick="openEditAdmin(${JSON.stringify(id)})">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    ${deactivateBtn}
                </td>
            </tr>
        `);
    });
}

// ─── HTML escape ──────────────────────────────────────────────────────────────
function escHtml(str) {
    return String(str)
        .replace(/&/g,  '&amp;')
        .replace(/</g,  '&lt;')
        .replace(/>/g,  '&gt;')
        .replace(/"/g,  '&quot;')
        .replace(/'/g,  '&#39;');
}

// ─── Add account modal ────────────────────────────────────────────────────────
function openAddAdmin() {
    document.getElementById('aId').value        = '';
    document.getElementById('aName').value      = '';
    document.getElementById('aEmail').value     = '';
    document.getElementById('aPassword').value  = '';
    document.getElementById('aRole').value      = '';

    const title = document.getElementById('adminModalTitleText');
    if (title) title.textContent = 'Add Account';

    populateSchoolDropdown();
    const schoolGroup = document.getElementById('aSchoolGroup');
    if (schoolGroup) schoolGroup.style.display = 'none';

    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('adminModal'));
    modal.show();
}

// ─── Edit account modal ───────────────────────────────────────────────────────
function openEditAdmin(id) {
    const admin = window.allAdmins.find(a => String(a.id || a.userId) === String(id));
    if (!admin) {
        showToast('Account not found.', 'danger');
        return;
    }

    document.getElementById('aId').value       = admin.id || admin.userId || '';
    document.getElementById('aName').value     = admin.name || admin.fullName || '';
    document.getElementById('aEmail').value    = admin.email || '';
    document.getElementById('aPassword').value = '';
    document.getElementById('aRole').value     = admin.role || '';

    const title = document.getElementById('adminModalTitleText');
    if (title) title.textContent = 'Edit Account';

    populateSchoolDropdown(admin.school_id || admin.schoolId || '');
    handleAdminRoleChange();

    const modal = bootstrap.Modal.getOrCreateInstance(document.getElementById('adminModal'));
    modal.show();
}

// ─── Role change handler ──────────────────────────────────────────────────────
function handleAdminRoleChange() {
    const role        = (document.getElementById('aRole')?.value || '').toLowerCase();
    const schoolGroup = document.getElementById('aSchoolGroup');
    if (!schoolGroup) return;
    schoolGroup.style.display = role === 'teacher' ? '' : 'none';
}

// ─── Save account ─────────────────────────────────────────────────────────────
async function saveAdmin() {
    const id       = document.getElementById('aId').value.trim();
    const name     = document.getElementById('aName').value.trim();
    const email    = document.getElementById('aEmail').value.trim();
    const password = document.getElementById('aPassword').value;
    const role     = document.getElementById('aRole').value.trim();
    const school   = document.getElementById('aSchool')?.value.trim() || '';

    // Validation
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

    const payload = { id, name, email, password, role, school };

    try {
        const res = await postJson('/Administration/SaveAdmin', payload);
        if (!res.ok) throw new Error(`Server responded ${res.status}`);
        const result = await res.json();
        if (result && result.success === false) throw new Error(result.message || 'Save failed.');

        const modal = bootstrap.Modal.getInstance(document.getElementById('adminModal'));
        if (modal) modal.hide();

        showToast(id ? 'Account updated successfully.' : 'Account created successfully.', 'success');
        await loadAdmins();
    } catch (err) {
        console.error('[AdministrationScript] saveAdmin failed:', err);
        showToast('Failed to save account: ' + err.message, 'danger');
    }
}

// ─── Deactivate flow ──────────────────────────────────────────────────────────
function deactivateAdmin(id, name) {
    Swal.fire({
        title: 'Deactivate Account',
        html: 'You are about to deactivate the account for:<br><strong>' + name + '</strong><br><small style="color:#6b7280">This will prevent them from logging in. You can reactivate at any time.</small>',
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

async function confirmDeactivate() {
    const id = _pendingDeactivateId;
    if (!id) return;

    try {
        const res = await postJson('/Administration/DeactivateAdmin', { id });
        if (!res.ok) throw new Error(`Server responded ${res.status}`);

        showToast('Account deactivated.', 'success');
        _pendingDeactivateId = null;
        await loadAdmins();
    } catch (err) {
        console.error('[AdministrationScript] confirmDeactivate failed:', err);
        showToast('Failed to deactivate account: ' + err.message, 'danger');
    }
}

// ─── Reactivate ───────────────────────────────────────────────────────────────
async function reactivateAdmin(id) {
    try {
        const res = await postJson('/Administration/ReactivateAdmin', { id });
        if (!res.ok) throw new Error(`Server responded ${res.status}`);
        showToast('Account reactivated.', 'success');
        await loadAdmins();
    } catch (err) {
        console.error('[AdministrationScript] reactivateAdmin failed:', err);
        showToast('Failed to reactivate account: ' + err.message, 'danger');
    }
}

// ─── HTTP helper ──────────────────────────────────────────────────────────────
function postJson(url, data) {
    return fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest',
        },
        body: JSON.stringify(data),
    });
}

// ─── Toast ────────────────────────────────────────────────────────────────────
function showToast(message, type) {
    type = type || 'info';
    const bgMap = {
        success: 'bg-success text-white',
        danger:  'bg-danger text-white',
        warning: 'bg-warning text-dark',
        info:    'bg-info text-dark',
    };
    const bg = bgMap[type] || 'bg-secondary text-white';

    let container = document.getElementById('adminToastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'adminToastContainer';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '1100';
        document.body.appendChild(container);
    }

    const id   = 'toast_' + Date.now();
    const html = `
        <div id="${id}" class="toast align-items-center ${bg} border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body">${message}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>`;
    container.insertAdjacentHTML('beforeend', html);
    const el    = document.getElementById(id);
    const toast = new bootstrap.Toast(el, { delay: 4000 });
    toast.show();
    el.addEventListener('hidden.bs.toast', () => el.remove());
}

// ─── Bind UI events ───────────────────────────────────────────────────────────
function bindEvents() {
    // Search / filter inputs
    document.getElementById('adminSearch')?.addEventListener('input', filterAdmins);
    document.getElementById('adminRoleFilter')?.addEventListener('change', filterAdmins);
    document.getElementById('statusFilter')?.addEventListener('change', filterAdmins);

    // Add button
    document.getElementById('addAdminBtn')?.addEventListener('click', openAddAdmin);

    // Save button inside modal
    document.getElementById('saveAdminBtn')?.addEventListener('click', saveAdmin);

    // Role change inside modal
    document.getElementById('aRole')?.addEventListener('change', handleAdminRoleChange);

}

// ─── Entry point ──────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    bindEvents();
    loadSchools();
    loadAdmins();
});
