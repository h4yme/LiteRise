/*!
 * TeacherReportsScript.js
 * LiteRise Web Portal — Teacher Reports Section
 * Handles: student select population, individual report generation,
 *          class report (print preview), CSV export, toast helpers.
 * Dependencies: Bootstrap 5, Font Awesome
 */
(function () {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════
    var PRIMARY_GREEN   = '#11B067';
    var TOTAL_MODULES   = 5;
    var NODES_PER_MOD   = 13;

    var MODULE_LABELS = [
        'Module 1', 'Module 2', 'Module 3',
        'Module 4', 'Module 5'
    ];

    var LEVEL_BADGE = {
        beginner:     { cls: 'bg-info text-dark',    label: 'Beginner'     },
        intermediate: { cls: 'bg-warning text-dark',  label: 'Intermediate' },
        advanced:     { cls: 'bg-success text-white', label: 'Advanced'     }
    };

    // ═══════════════════════════════════════════════════════════════════════
    // INIT — run after DOM is ready
    // ═══════════════════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', function () {
        populateStudentSelect();
    });

    // ═══════════════════════════════════════════════════════════════════════
    // populateStudentSelect
    // Reads window.studentsData injected by the Razor view and fills the
    // #studentSelect dropdown.
    // ═══════════════════════════════════════════════════════════════════════
    function populateStudentSelect() {
        var select = document.getElementById('studentSelect');
        if (!select) return;

        var data = window.studentsData;
        if (!Array.isArray(data) || data.length === 0) {
            select.innerHTML = '<option value="">No students found</option>';
            select.disabled = true;
            return;
        }

        // Sort alphabetically by name
        var sorted = data.slice().sort(function (a, b) {
            var na = (a.full_name || a.name || '').toLowerCase();
            var nb = (b.full_name || b.name || '').toLowerCase();
            return na < nb ? -1 : na > nb ? 1 : 0;
        });

        var html = '<option value="">— Choose a student —</option>';
        sorted.forEach(function (s) {
            var id    = s.student_id || s.id || '';
            var name  = s.full_name  || s.name || 'Unknown';
            var grade = s.grade      ? ' (Grade ' + s.grade + ')' : '';
            html += '<option value="' + escAttr(String(id)) + '">'
                  + escHtml(name + grade)
                  + '</option>';
        });

        select.innerHTML = html;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // generateStudentReport  (exposed globally for onclick)
    // ═══════════════════════════════════════════════════════════════════════
    window.generateStudentReport = function () {
        var select    = document.getElementById('studentSelect');
        var studentId = select ? select.value : '';

        if (!studentId) {
            showToast('Please select a student first.', 'warning');
            return;
        }

        showLoading(true);
        hidePreview();

        fetch('/TeacherReports/GetStudentReportData?studentId=' + encodeURIComponent(studentId), {
            method: 'GET',
            headers: { 'X-Requested-With': 'XMLHttpRequest' }
        })
        .then(function (res) {
            if (!res.ok) throw new Error('Server returned ' + res.status);
            return res.json();
        })
        .then(function (data) {
            showLoading(false);
            if (!data.success) {
                showToast('Error loading report: ' + (data.message || 'Unknown error'), 'danger');
                return;
            }
            var html = buildStudentReportHtml(data.student, data.placement, data.nodeProgress, data.moduleLadder);
            renderPreview(html);
        })
        .catch(function (err) {
            showLoading(false);
            showToast('Request failed: ' + err.message, 'danger');
        });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // buildStudentReportHtml
    // Assembles the inline preview HTML from the four API payloads.
    // ═══════════════════════════════════════════════════════════════════════
    function buildStudentReportHtml(student, placement, nodeProgress, moduleLadder) {
        student      = student      || {};
        placement    = placement    || {};
        nodeProgress = nodeProgress || {};
        moduleLadder = moduleLadder || {};

        var name     = student.full_name   || student.name           || '—';
        var grade    = student.grade                                  || '—';
        var school   = student.school_name                            || '—';
        var gender   = student.gender                                 || '—';
        var level    = student.placement_level                        || '—';
        var preTheta = safeFixed(student.pre_theta,  4);
        var postTheta= safeFixed(student.post_theta, 4);
        var xp       = student.total_xp    != null ? Number(student.total_xp).toLocaleString() : '0';
        var streak   = student.streak_days != null ? student.streak_days + ' day(s)' : '0 days';
        var lastActive = student.last_active || '—';

        // Level badge
        var lvlKey   = (level || '').toLowerCase();
        var lvlBadge = LEVEL_BADGE[lvlKey]
                     ? '<span class="badge ' + LEVEL_BADGE[lvlKey].cls + '">' + LEVEL_BADGE[lvlKey].label + '</span>'
                     : '<span class="badge bg-secondary">' + escHtml(level) + '</span>';

        // ── Section 1: Student info table ─────────────────────────────────
        var infoHtml = '<div class="report-section">'
            + '<h4 class="report-section-title"><i class="fas fa-id-card me-2"></i>Student Information</h4>'
            + '<table class="info-table">'
            + '<tbody>'
            + '<tr><th>Full Name</th><td>' + escHtml(name)   + '</td>'
            +     '<th>School</th><td>'    + escHtml(school) + '</td></tr>'
            + '<tr><th>Grade</th><td>'     + escHtml(grade)  + '</td>'
            +     '<th>Gender</th><td>'    + escHtml(gender) + '</td></tr>'
            + '<tr><th>Level</th><td>'     + lvlBadge        + '</td>'
            +     '<th>Total XP</th><td><strong>' + escHtml(xp) + '</strong></td></tr>'
            + '<tr><th>Streak</th><td>'    + escHtml(streak) + '</td>'
            +     '<th>Last Active</th><td>' + escHtml(lastActive) + '</td></tr>'
            + '</tbody></table>'
            + '</div>';

        // ── Section 2: Pre vs Post Assessment ────────────────────────────
        var assessHtml = '';
        if (student.pre_theta != null || student.post_theta != null) {
            var growth = (student.pre_theta != null && student.post_theta != null)
                ? (Number(student.post_theta) - Number(student.pre_theta))
                : null;
            var growthStr  = growth != null
                ? (growth >= 0 ? '+' : '') + growth.toFixed(4)
                : '—';
            var growthCls  = growth == null ? '' : (growth >= 0 ? 'growth-positive' : 'growth-negative');

            assessHtml = '<div class="report-section">'
                + '<h4 class="report-section-title"><i class="fas fa-chart-line me-2"></i>Assessment Scores</h4>'
                + '<table class="info-table">'
                + '<tbody>'
                + '<tr><th>Pre-Assessment (θ)</th><td>' + escHtml(preTheta)  + '</td>'
                +     '<th>Post-Assessment (θ)</th><td>' + escHtml(postTheta) + '</td></tr>'
                + '<tr><th>Growth</th><td colspan="3"><span class="' + growthCls + '">' + escHtml(growthStr) + '</span></td></tr>'
                + '</tbody></table>'
                + '</div>';
        }

        // ── Section 3: Module completion grid ────────────────────────────
        var modulesHtml = buildModuleGrid(nodeProgress, moduleLadder);

        // ── Print button ──────────────────────────────────────────────────
        var printBtn = '<div class="report-actions">'
            + '<button class="btn btn-outline-secondary btn-sm" onclick="window.print()">'
            + '<i class="fas fa-print me-1"></i>Print Preview'
            + '</button>'
            + '</div>';

        return printBtn + infoHtml + assessHtml + modulesHtml;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // buildModuleGrid
    // Renders a 5-module × N-node completion grid.
    // Accepts both nodeProgress and moduleLadder payloads; uses whichever
    // has richer data.
    // ═══════════════════════════════════════════════════════════════════════
    function buildModuleGrid(nodeProgress, moduleLadder) {
        // Try to get per-module completion counts from moduleLadder
        // Expected shape: { modules: [ { module_number, nodes_completed, nodes_total, ... }, ... ] }
        var modules = null;

        if (moduleLadder && Array.isArray(moduleLadder.modules) && moduleLadder.modules.length) {
            modules = moduleLadder.modules;
        } else if (moduleLadder && Array.isArray(moduleLadder) && moduleLadder.length) {
            modules = moduleLadder;
        }

        // Fallback: derive from nodeProgress array
        // Expected shape: [ { module_number, node_number, is_completed }, ... ]
        var nodeArr = null;
        if (Array.isArray(nodeProgress)) {
            nodeArr = nodeProgress;
        } else if (nodeProgress && Array.isArray(nodeProgress.nodes)) {
            nodeArr = nodeProgress.nodes;
        }

        var html = '<div class="report-section">'
            + '<h4 class="report-section-title"><i class="fas fa-th me-2"></i>Lesson Progress</h4>';

        if (!modules && !nodeArr) {
            html += '<p class="text-muted small">No lesson progress data available.</p></div>';
            return html;
        }

        html += '<table class="info-table module-grid">'
              + '<thead><tr><th>Module</th><th>Completed</th><th>Total Nodes</th><th>Progress</th></tr></thead>'
              + '<tbody>';

        for (var m = 1; m <= TOTAL_MODULES; m++) {
            var completed  = 0;
            var totalNodes = NODES_PER_MOD;
            var label      = MODULE_LABELS[m - 1] || ('Module ' + m);

            // Prefer moduleLadder data
            if (modules) {
                var found = null;
                for (var i = 0; i < modules.length; i++) {
                    var mn = modules[i].module_number || modules[i].module || modules[i].id;
                    if (parseInt(mn, 10) === m) { found = modules[i]; break; }
                }
                if (found) {
                    completed  = parseInt(found.nodes_completed || found.completed || 0, 10);
                    totalNodes = parseInt(found.nodes_total     || found.total     || NODES_PER_MOD, 10);
                    label      = found.module_name || found.name || label;
                }
            } else if (nodeArr) {
                // Count completed nodes for this module
                var moduleNodes = nodeArr.filter(function (n) {
                    return parseInt(n.module_number || n.module || 0, 10) === m;
                });
                totalNodes = moduleNodes.length || NODES_PER_MOD;
                completed  = moduleNodes.filter(function (n) {
                    return n.is_completed || n.completed || n.status === 'completed';
                }).length;
            }

            var pct      = totalNodes > 0 ? Math.min(Math.round(completed / totalNodes * 100), 100) : 0;
            var barColor = pct >= 80 ? PRIMARY_GREEN : pct >= 40 ? '#ffc107' : '#6c757d';

            html += '<tr>'
                  + '<td><strong>' + escHtml(label) + '</strong></td>'
                  + '<td>' + completed + ' / ' + totalNodes + '</td>'
                  + '<td>' + totalNodes + ' nodes</td>'
                  + '<td>'
                  +   '<div class="progress-bar-wrap">'
                  +     '<div class="progress-bar-track">'
                  +       '<div class="progress-bar-fill" style="width:' + pct + '%;background:' + barColor + '"></div>'
                  +     '</div>'
                  +     '<span class="progress-pct">' + pct + '%</span>'
                  +   '</div>'
                  + '</td>'
                  + '</tr>';
        }

        html += '</tbody></table></div>';
        return html;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // generateClassReport  (exposed globally)
    // POSTs to /TeacherReports/GenerateClassReport, opens result in new tab.
    // ═══════════════════════════════════════════════════════════════════════
    window.generateClassReport = function () {
        showToast('Generating class report…', 'info');

        var form = document.createElement('form');
        form.method = 'POST';
        form.action = '/TeacherReports/GenerateClassReport';
        form.target = '_blank';

        // Include anti-forgery token if present
        var tokenField = document.querySelector('input[name="__RequestVerificationToken"]');
        if (tokenField) {
            var hidden = document.createElement('input');
            hidden.type  = 'hidden';
            hidden.name  = '__RequestVerificationToken';
            hidden.value = tokenField.value;
            form.appendChild(hidden);
        }

        document.body.appendChild(form);
        form.submit();
        document.body.removeChild(form);
    };

    // ═══════════════════════════════════════════════════════════════════════
    // exportCsv  (exposed globally)
    // ═══════════════════════════════════════════════════════════════════════
    window.exportCsv = function () {
        showToast('Preparing CSV download…', 'info');
        window.location = '/TeacherReports/ExportClassCsv';
    };

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS — preview pane
    // ═══════════════════════════════════════════════════════════════════════
    function renderPreview(html) {
        var el = document.getElementById('reportPreview');
        if (!el) return;
        el.innerHTML = html;
        el.style.display = 'block';
        // Smooth scroll into view
        setTimeout(function () {
            el.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 80);
    }

    function hidePreview() {
        var el = document.getElementById('reportPreview');
        if (el) { el.style.display = 'none'; el.innerHTML = ''; }
    }

    function showLoading(show) {
        var el = document.getElementById('reportLoading');
        if (el) el.style.display = show ? 'flex' : 'none';
    }

    // ═══════════════════════════════════════════════════════════════════════
    // showToast  (exposed globally, used from view inline handlers too)
    // ═══════════════════════════════════════════════════════════════════════
    window.showToast = function (message, type) {
        type = type || 'info';

        var bgMap = {
            success : 'bg-success text-white',
            danger  : 'bg-danger  text-white',
            warning : 'bg-warning text-dark',
            info    : 'bg-info    text-dark'
        };

        var container = document.getElementById('trToastContainer');
        if (!container) return;

        var id   = 'tr-toast-' + Date.now();
        var cls  = bgMap[type] || 'bg-secondary text-white';

        var html = '<div id="' + id + '" class="toast align-items-center ' + cls + ' border-0" role="alert" '
                 + 'aria-live="assertive" aria-atomic="true" data-bs-delay="3500">'
                 + '<div class="d-flex">'
                 + '<div class="toast-body">' + escHtml(message) + '</div>'
                 + '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>'
                 + '</div></div>';

        container.insertAdjacentHTML('beforeend', html);

        var toastEl = document.getElementById(id);
        if (window.bootstrap && window.bootstrap.Toast) {
            var t = new bootstrap.Toast(toastEl);
            t.show();
            toastEl.addEventListener('hidden.bs.toast', function () {
                toastEl.parentNode && toastEl.parentNode.removeChild(toastEl);
            });
        } else {
            // Fallback: simple fade-out after 3.5 s
            toastEl.style.cssText = 'padding:10px 16px;border-radius:6px;margin-top:8px;opacity:1;transition:opacity 0.4s';
            setTimeout(function () {
                toastEl.style.opacity = '0';
                setTimeout(function () {
                    toastEl.parentNode && toastEl.parentNode.removeChild(toastEl);
                }, 450);
            }, 3500);
        }
    };

    // ═══════════════════════════════════════════════════════════════════════
    // MICRO UTILITIES
    // ═══════════════════════════════════════════════════════════════════════
    function safeFixed(val, decimals) {
        var n = parseFloat(val);
        return isNaN(n) ? '—' : n.toFixed(decimals);
    }

    function escHtml(str) {
        if (str == null) return '';
        return String(str)
            .replace(/&/g,  '&amp;')
            .replace(/</g,  '&lt;')
            .replace(/>/g,  '&gt;')
            .replace(/"/g,  '&quot;')
            .replace(/'/g,  '&#39;');
    }

    function escAttr(str) {
        return escHtml(str);
    }

}());
