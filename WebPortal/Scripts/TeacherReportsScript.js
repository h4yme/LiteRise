/**
 * TeacherReportsScript.js
 * LiteRise Web Portal — Teacher Reports Page
 */

'use strict';

// ─── Constants ────────────────────────────────────────────────────────────────
const TR_MODULES = [
    { label: 'Module 1: Phonics',         totalNodes: 13 },
    { label: 'Module 2: Vocabulary',       totalNodes: 13 },
    { label: 'Module 3: Grammar',          totalNodes: 13 },
    { label: 'Module 4: Comprehension',    totalNodes: 13 },
    { label: 'Module 5: Creating Text',    totalNodes: 13 },
];

// ─── IRT helpers ─────────────────────────────────────────────────────────────
function trClassifyTheta(theta) {
    if (theta === null || theta === undefined || isNaN(parseFloat(theta))) return null;
    const t = parseFloat(theta);
    if (t < -0.5)  return 'Beginner';
    if (t <= 0.5)  return 'Intermediate';
    return 'Advanced';
}

function levelBadge(level) {
    if (!level) return '<span class="badge bg-secondary">Not Taken</span>';
    switch (level.toLowerCase()) {
        case 'beginner':     return '<span class="badge" style="background:#FC8181;">Beginner</span>';
        case 'intermediate': return '<span class="badge" style="background:#F6AD55;color:#744210;">Intermediate</span>';
        case 'advanced':     return '<span class="badge" style="background:#11B067;">Advanced</span>';
        default:             return `<span class="badge bg-secondary">${trEscHtml(level)}</span>`;
    }
}

// ─── Date formatter ───────────────────────────────────────────────────────────
function trFormatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '—';
    return d.toLocaleDateString('en-AU', { day: '2-digit', month: 'short', year: 'numeric' });
}

// ─── HTML escape ──────────────────────────────────────────────────────────────
function trEscHtml(str) {
    return String(str ?? '')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

// ─── Populate student select ──────────────────────────────────────────────────
function populateStudentSelect() {
    const students = window.studentsData || [];
    const select   = document.getElementById('studentSelect');
    if (!select) return;

    if (!students.length) {
        select.innerHTML = '<option value="">No students found</option>';
        select.disabled  = true;
        return;
    }

    select.innerHTML = '<option value="">— Select a student —</option>';

    // Sort alphabetically
    const sorted = [...students].sort((a, b) => {
        const na = (a.FullName || a.full_name || a.name || '').toLowerCase();
        const nb = (b.FullName || b.full_name || b.name || '').toLowerCase();
        return na < nb ? -1 : na > nb ? 1 : 0;
    });

    sorted.forEach((s, i) => {
        const name  = s.FullName || s.full_name || s.name || 'Unknown';
        const grade = s.grade    || '?';
        const level = s.placement_level || trClassifyTheta(s.pre_theta) || 'Not Taken';
        const id    = s.id || s.userId || s.student_id || i;

        const opt         = document.createElement('option');
        opt.value         = id;
        opt.dataset.index = sorted.indexOf(s);
        opt.textContent   = `${name} — Grade ${grade} [${level}]`;
        select.appendChild(opt);
    });
}

// ─── Loading helpers ──────────────────────────────────────────────────────────
function showLoading(btnId) {
    const btn = document.getElementById(btnId);
    if (!btn) return;
    btn.dataset.originalHtml = btn.innerHTML;
    btn.disabled  = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Loading…`;
}

function hideLoading(btnId) {
    const btn = document.getElementById(btnId);
    if (!btn) return;
    btn.disabled = false;
    if (btn.dataset.originalHtml) btn.innerHTML = btn.dataset.originalHtml;
}

// ─── Growth arrow ─────────────────────────────────────────────────────────────
function growthArrow(pre, post) {
    if (pre == null || post == null) return '—';
    const diff = parseFloat(post) - parseFloat(pre);
    if (isNaN(diff)) return '—';
    const sign   = diff >= 0 ? '+' : '';
    const color  = diff >= 0 ? '#11B067' : '#FC8181';
    const symbol = diff >= 0 ? '↑' : '↓';
    return `<span style="color:${color};font-weight:700;">${symbol} ${sign}${diff.toFixed(3)}</span>`;
}

// ─── Progress bar HTML ────────────────────────────────────────────────────────
function trProgressBar(completed, total) {
    const pct   = total > 0 ? Math.min(100, Math.round((completed / total) * 100)) : 0;
    const color = pct >= 80 ? '#11B067' : pct >= 50 ? '#F6AD55' : '#FC8181';
    return `
        <div class="d-flex align-items-center gap-2">
            <div style="flex:1;background:#E2E8F0;border-radius:4px;height:8px;overflow:hidden;">
                <div style="width:${pct}%;background:${color};height:100%;border-radius:4px;transition:width 0.4s;"></div>
            </div>
            <span class="text-muted small" style="white-space:nowrap;">${completed}/${total}</span>
        </div>`;
}

// ─── Module completion estimate from total lessons_done ───────────────────────
function estimateModuleCompletion(lessonsTotal) {
    const done = parseInt(lessonsTotal) || 0;
    return TR_MODULES.map((mod, i) => {
        const start     = i * mod.totalNodes;
        const completed = Math.max(0, Math.min(mod.totalNodes, done - start));
        return { ...mod, completed };
    });
}

// ─── Student report HTML builder ──────────────────────────────────────────────
function buildStudentReportHtml(data) {
    const s          = data.student || data;
    const name       = s.FullName      || s.full_name    || s.name || '—';
    const grade      = s.grade                           || '—';
    const school     = s.school        || s.school_name  || '—';
    const preTheta   = s.pre_theta  != null ? parseFloat(s.pre_theta)  : null;
    const postTheta  = s.post_theta != null ? parseFloat(s.post_theta) : null;
    const xp         = parseInt(s.total_xp)    || 0;
    const streak     = parseInt(s.streak_days) || 0;
    const lastActive = trFormatDate(s.last_active);
    const level      = s.placement_level || trClassifyTheta(preTheta);
    const modules    = (data.moduleProgress && Array.isArray(data.moduleProgress))
                       ? buildModulesFromApi(data.moduleProgress)
                       : estimateModuleCompletion(s.lessons_done);

    const preLevel  = trClassifyTheta(preTheta);
    const postLevel = trClassifyTheta(postTheta);

    return `
<div class="report-wrapper p-3" style="font-family:'Segoe UI',sans-serif;max-width:900px;">

    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-4 pb-2 border-bottom">
        <div>
            <h4 class="mb-0 fw-bold" style="color:#11B067;">LiteRise Student Report</h4>
            <small class="text-muted">Generated: ${new Date().toLocaleString('en-AU')}</small>
        </div>
        <button class="btn btn-sm btn-outline-secondary d-print-none" onclick="printReport()">
            <i class="bi bi-printer"></i> Print
        </button>
    </div>

    <!-- Student Profile -->
    <div class="card mb-4 shadow-sm">
        <div class="card-header fw-semibold" style="background:#f8f9fa;">
            <i class="bi bi-person-badge me-2"></i>Student Profile
        </div>
        <div class="card-body p-0">
            <table class="table table-sm mb-0">
                <tbody>
                    <tr>
                        <th style="width:160px;" class="ps-3">Full Name</th>
                        <td>${trEscHtml(name)}</td>
                        <th style="width:120px;">School</th>
                        <td>${trEscHtml(school)}</td>
                    </tr>
                    <tr>
                        <th class="ps-3">Grade</th>
                        <td>${trEscHtml(String(grade))}</td>
                        <th>Placement Level</th>
                        <td>${levelBadge(level)}</td>
                    </tr>
                    <tr>
                        <th class="ps-3">Pre θ</th>
                        <td>${preTheta !== null ? preTheta.toFixed(4) : '—'}</td>
                        <th>Post θ</th>
                        <td>${postTheta !== null ? postTheta.toFixed(4) : '—'}</td>
                    </tr>
                    <tr>
                        <th class="ps-3">Total XP</th>
                        <td><strong>${xp.toLocaleString()} XP</strong></td>
                        <th>Streak</th>
                        <td>${streak} day${streak !== 1 ? 's' : ''}</td>
                    </tr>
                    <tr>
                        <th class="ps-3">Last Active</th>
                        <td colspan="3">${lastActive}</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Assessment Summary -->
    <div class="card mb-4 shadow-sm">
        <div class="card-header fw-semibold" style="background:#f8f9fa;">
            <i class="bi bi-graph-up me-2"></i>Assessment Summary
        </div>
        <div class="card-body p-0">
            <table class="table table-sm table-bordered mb-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-3">Assessment</th>
                        <th>Theta (θ)</th>
                        <th>Level</th>
                        <th>Date</th>
                        <th>Growth vs Previous</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td class="ps-3">Pre-Assessment</td>
                        <td>${preTheta !== null ? preTheta.toFixed(4) : '—'}</td>
                        <td>${levelBadge(preLevel)}</td>
                        <td>${trFormatDate(s.pre_date)}</td>
                        <td>—</td>
                    </tr>
                    ${postTheta !== null ? `
                    <tr>
                        <td class="ps-3">Post-Assessment</td>
                        <td>${postTheta.toFixed(4)}</td>
                        <td>${levelBadge(postLevel)}</td>
                        <td>${trFormatDate(s.post_date)}</td>
                        <td>${growthArrow(preTheta, postTheta)}</td>
                    </tr>` : `
                    <tr>
                        <td colspan="5" class="text-muted text-center py-2">Post-assessment not yet completed.</td>
                    </tr>`}
                </tbody>
            </table>
        </div>
    </div>

    <!-- Lesson Progress -->
    <div class="card mb-4 shadow-sm">
        <div class="card-header fw-semibold" style="background:#f8f9fa;">
            <i class="bi bi-journal-check me-2"></i>Lesson Progress
        </div>
        <div class="card-body p-0">
            <table class="table table-sm table-bordered mb-0">
                <thead class="table-light">
                    <tr>
                        <th class="ps-3" style="width:45%;">Module</th>
                        <th>Progress Bar</th>
                        <th style="width:100px;text-align:center;">Nodes</th>
                        <th style="width:80px;text-align:center;">% Done</th>
                    </tr>
                </thead>
                <tbody>
                    ${modules.map(mod => {
                        const pct = mod.totalNodes > 0
                            ? Math.round((mod.completed / mod.totalNodes) * 100)
                            : 0;
                        return `<tr>
                            <td class="ps-3">${trEscHtml(mod.label)}</td>
                            <td>${trProgressBar(mod.completed, mod.totalNodes)}</td>
                            <td style="text-align:center;">${mod.completed}/${mod.totalNodes}</td>
                            <td style="text-align:center;">${pct}%</td>
                        </tr>`;
                    }).join('')}
                </tbody>
            </table>
        </div>
    </div>

    <p class="text-muted small mt-2 d-print-none">
        * Module breakdown is estimated from total lessons completed where per-module data is unavailable.
    </p>

</div>`;
}

// ─── Build module rows from API payload ───────────────────────────────────────
function buildModulesFromApi(moduleProgress) {
    return TR_MODULES.map((mod, i) => {
        const num   = i + 1;
        const found = moduleProgress.find(m =>
            parseInt(m.module_number || m.module || m.id || 0, 10) === num
        );
        return {
            label:      (found && (found.module_name || found.name)) || mod.label,
            totalNodes: (found && parseInt(found.nodes_total || found.total || mod.totalNodes, 10)) || mod.totalNodes,
            completed:  (found && parseInt(found.nodes_completed || found.completed || 0, 10)) || 0,
        };
    });
}

// ─── Generate student report ──────────────────────────────────────────────────
function generateStudentReport() {
    const select = document.getElementById('studentSelect');
    if (!select || !select.value) {
        showTrToast('Please select a student first.', 'warning');
        return;
    }

    showLoading('generateStudentBtn');

    try {
        const form        = document.createElement('form');
        form.method       = 'POST';
        form.action       = '/TeacherReports/GenerateStudentReport';
        form.target       = '_blank';

        const idInput     = document.createElement('input');
        idInput.type      = 'hidden';
        idInput.name      = 'studentId';
        idInput.value     = select.value;
        form.appendChild(idInput);

        // Include anti-forgery token if present
        const token = document.querySelector('input[name="__RequestVerificationToken"]');
        if (token) {
            const hidden       = document.createElement('input');
            hidden.type        = 'hidden';
            hidden.name        = '__RequestVerificationToken';
            hidden.value       = token.value;
            form.appendChild(hidden);
        }

        document.body.appendChild(form);
        form.submit();
        document.body.removeChild(form);
    } catch (err) {
        console.error('[TeacherReportsScript] generateStudentReport failed:', err);
        showTrToast('Failed to generate student report: ' + err.message, 'danger');
    } finally {
        hideLoading('generateStudentBtn');
    }
}

// ─── Generate class report ────────────────────────────────────────────────────
async function generateClassReport() {
    showTrToast('Generating class report…', 'info');
    showLoading('generateClassBtn');

    try {
        // Use a form POST so the response opens in a new tab correctly
        const form   = document.createElement('form');
        form.method  = 'POST';
        form.action  = '/TeacherReports/GenerateClassReport';
        form.target  = '_blank';

        // Include anti-forgery token if present
        const token = document.querySelector('input[name="__RequestVerificationToken"]');
        if (token) {
            const hidden       = document.createElement('input');
            hidden.type        = 'hidden';
            hidden.name        = '__RequestVerificationToken';
            hidden.value       = token.value;
            form.appendChild(hidden);
        }

        document.body.appendChild(form);
        form.submit();
        document.body.removeChild(form);
    } catch (err) {
        console.error('[TeacherReportsScript] generateClassReport failed:', err);
        showTrToast('Failed to generate class report: ' + err.message, 'danger');
    } finally {
        hideLoading('generateClassBtn');
    }
}

// ─── Export CSV ───────────────────────────────────────────────────────────────
function exportCsv() {
    showTrToast('Preparing CSV download…', 'info');
    window.location.href = '/TeacherReports/ExportClassCsv';
}

// ─── Print report ─────────────────────────────────────────────────────────────
function printReport() {
    const preview = document.getElementById('reportPreview');
    if (!preview || !preview.innerHTML.trim()) {
        showTrToast('Nothing to print. Generate a report first.', 'warning');
        return;
    }

    const styles = Array.from(document.querySelectorAll('link[rel="stylesheet"], style'))
        .map(el => el.outerHTML)
        .join('\n');

    const win = window.open('', '_blank');
    if (!win) {
        showTrToast('Pop-up blocked. Please allow pop-ups for this site.', 'warning');
        return;
    }

    win.document.write(`<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>LiteRise Student Report</title>
    ${styles}
    <style>
        @media print { .btn, button, .d-print-none { display: none !important; } }
        body { padding: 24px; background: #fff; }
    </style>
</head>
<body>
${preview.innerHTML}
</body>
</html>`);
    win.document.close();
    win.addEventListener('load', () => win.print());
}

// ─── Toast ────────────────────────────────────────────────────────────────────
function showTrToast(message, type) {
    type = type || 'info';
    const bgMap = {
        success: 'bg-success text-white',
        danger:  'bg-danger text-white',
        warning: 'bg-warning text-dark',
        info:    'bg-info text-dark',
    };
    const bg = bgMap[type] || 'bg-secondary text-white';

    let container = document.getElementById('trToastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id        = 'trToastContainer';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        container.style.zIndex = '1100';
        document.body.appendChild(container);
    }

    const id   = 'tr_toast_' + Date.now();
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

// Keep backward-compat alias used by the view if present
window.showToast = showTrToast;

// ─── Bind UI events ───────────────────────────────────────────────────────────
function bindTrEvents() {
    document.getElementById('generateStudentBtn')?.addEventListener('click', generateStudentReport);
    document.getElementById('generateClassBtn')?.addEventListener('click',   generateClassReport);
    document.getElementById('exportCsvBtn')?.addEventListener('click',       exportCsv);
}

// ─── Expose for inline onclick use ───────────────────────────────────────────
window.generateStudentReport = generateStudentReport;
window.generateClassReport   = generateClassReport;
window.exportCsv             = exportCsv;
window.printReport           = printReport;

// ─── Entry point ──────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    populateStudentSelect();
    bindTrEvents();
});
