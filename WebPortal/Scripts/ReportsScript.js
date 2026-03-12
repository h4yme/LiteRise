/*!
 * ReportsScript.js
 * LiteRise Web Portal — Reports Section
 * Handles: Student Report, School Report, System Usage Report, Print, Toasts
 * Dependencies: Bootstrap 5
 */
(function () {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════
    var ENDPOINTS = {
        studentReport: '/Reports/GenerateStudentReport',
        schoolReport:  '/Reports/GenerateSchoolReport',
        systemReport:  '/Reports/GenerateSystemUsageReport'
    };

    // Holds the last successfully generated HTML report string (for print)
    var _lastReportHtml = '';
    var _lastReportType = '';

    // ═══════════════════════════════════════════════════════════════════════
    // INITIALISATION — runs after DOM ready
    // ═══════════════════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', function () {
        populateStudentSelect();
        populateSchoolSelect();
        setDefaultDateRange();
    });

    // ───────────────────────────────────────────────────────────────────────
    // populateStudentSelect
    // Fills #studentSelect from window.studentsData.
    // Format shown: "Full Name — School"
    // ───────────────────────────────────────────────────────────────────────
    function populateStudentSelect() {
        var sel = document.getElementById('studentSelect');
        if (!sel) return;

        var students = window.studentsData;
        if (!Array.isArray(students) || students.length === 0) {
            sel.innerHTML = '<option value="">No students available</option>';
            return;
        }

        // Sort alphabetically by full_name / name
        var sorted = students.slice().sort(function (a, b) {
            var na = (a.full_name || a.name || '').toLowerCase();
            var nb = (b.full_name || b.name || '').toLowerCase();
            return na < nb ? -1 : na > nb ? 1 : 0;
        });

        var html = '<option value="">— Choose a student —</option>';
        sorted.forEach(function (s) {
            var id   = s.student_id || s.id || '';
            var name = s.full_name   || s.name        || 'Unknown';
            var school = s.school_name || s.school    || '';
            var label  = school ? (name + ' \u2014 ' + school) : name;
            html += '<option value="' + escHtml(String(id)) + '">' + escHtml(label) + '</option>';
        });

        sel.innerHTML = html;
    }

    // ───────────────────────────────────────────────────────────────────────
    // populateSchoolSelect
    // Fills #schoolSelect from window.schoolsData.
    // ───────────────────────────────────────────────────────────────────────
    function populateSchoolSelect() {
        var sel = document.getElementById('schoolSelect');
        if (!sel) return;

        var schools = window.schoolsData;
        if (!Array.isArray(schools) || schools.length === 0) {
            sel.innerHTML = '<option value="">No schools available</option>';
            return;
        }

        var sorted = schools.slice().sort(function (a, b) {
            var na = (a.school_name || a.name || '').toLowerCase();
            var nb = (b.school_name || b.name || '').toLowerCase();
            return na < nb ? -1 : na > nb ? 1 : 0;
        });

        var html = '<option value="">— Choose a school —</option>';
        sorted.forEach(function (sc) {
            var id   = sc.school_id || sc.id || '';
            var name = sc.school_name || sc.name || 'Unknown';
            html += '<option value="' + escHtml(String(id)) + '">' + escHtml(name) + '</option>';
        });

        sel.innerHTML = html;
    }

    // ───────────────────────────────────────────────────────────────────────
    // setDefaultDateRange — pre-fill start/end date inputs
    // ───────────────────────────────────────────────────────────────────────
    function setDefaultDateRange() {
        var end   = document.getElementById('endDate');
        var start = document.getElementById('startDate');
        if (!end || !start) return;

        var today = new Date();
        var prior = new Date(today);
        prior.setDate(today.getDate() - 30);

        end.value   = formatDateInput(today);
        start.value = formatDateInput(prior);
    }

    function formatDateInput(d) {
        var y  = d.getFullYear();
        var m  = String(d.getMonth() + 1).padStart(2, '0');
        var dd = String(d.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + dd;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GENERATE — Student Progress Report
    // ═══════════════════════════════════════════════════════════════════════
    window.generateStudentReport = function () {
        var sel = document.getElementById('studentSelect');
        var studentId = sel ? sel.value : '';

        if (!studentId) {
            showToast('Please select a student first.', 'warning');
            return;
        }

        var btn = document.getElementById('btnGenStudent');
        setButtonLoading(btn, true, 'Generating…');
        showLoadingOverlay('Generating student report…');

        postJson(ENDPOINTS.studentReport, { studentId: parseInt(studentId, 10) })
            .then(function (html) {
                _lastReportHtml = html;
                _lastReportType = 'student';
                renderPreview(html);
                showPrintButton(true);
                showToast('Student report generated successfully.', 'success');
            })
            .catch(function (err) {
                showToast('Failed to generate report: ' + err, 'danger');
            })
            .finally(function () {
                setButtonLoading(btn, false, '<i class="fas fa-chart-line"></i> Generate Report');
                hideLoadingOverlay();
            });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // GENERATE — School Summary Report
    // ═══════════════════════════════════════════════════════════════════════
    window.generateSchoolReport = function () {
        var sel = document.getElementById('schoolSelect');
        var schoolId = sel ? sel.value : '';

        if (!schoolId) {
            showToast('Please select a school first.', 'warning');
            return;
        }

        var btn = document.getElementById('btnGenSchool');
        setButtonLoading(btn, true, 'Generating…');
        showLoadingOverlay('Generating school report…');

        postJson(ENDPOINTS.schoolReport, { schoolId: parseInt(schoolId, 10) })
            .then(function (html) {
                _lastReportHtml = html;
                _lastReportType = 'school';
                renderPreview(html);
                showPrintButton(true);
                showToast('School report generated successfully.', 'success');
            })
            .catch(function (err) {
                showToast('Failed to generate report: ' + err, 'danger');
            })
            .finally(function () {
                setButtonLoading(btn, false, '<i class="fas fa-chart-bar"></i> Generate Report');
                hideLoadingOverlay();
            });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // GENERATE — System Usage Report
    // ═══════════════════════════════════════════════════════════════════════
    window.generateSystemReport = function () {
        var startDate = (document.getElementById('startDate') || {}).value || '';
        var endDate   = (document.getElementById('endDate')   || {}).value || '';

        var btn = document.getElementById('btnGenSystem');
        setButtonLoading(btn, true, 'Generating…');
        showLoadingOverlay('Generating system usage report…');

        postJson(ENDPOINTS.systemReport, { startDate: startDate, endDate: endDate })
            .then(function (html) {
                _lastReportHtml = html;
                _lastReportType = 'system';
                renderPreview(html);
                showPrintButton(true);
                showToast('System usage report generated successfully.', 'success');
            })
            .catch(function (err) {
                showToast('Failed to generate report: ' + err, 'danger');
            })
            .finally(function () {
                setButtonLoading(btn, false, '<i class="fas fa-cog"></i> Generate Report');
                hideLoadingOverlay();
            });
    };

    // ═══════════════════════════════════════════════════════════════════════
    // PRINT REPORT
    // Opens the last generated report HTML in a new window and triggers print.
    // ═══════════════════════════════════════════════════════════════════════
    window.printReport = function () {
        if (!_lastReportHtml) {
            showToast('No report to print. Please generate a report first.', 'warning');
            return;
        }

        var printWin = window.open('', '_blank', 'width=900,height=700');
        if (!printWin) {
            showToast('Pop-up blocked. Please allow pop-ups for this site to print.', 'warning');
            return;
        }

        printWin.document.open();
        printWin.document.write(_lastReportHtml);
        printWin.document.close();
        // The BuildReportHtml template in the controller already auto-triggers
        // window.print() on load, but we also call it here as a safety net.
        printWin.onload = function () {
            try { printWin.print(); } catch (e) { /* ignored */ }
        };
    };

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS — DOM
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Renders the report HTML into the #reportPreview container as an iframe
     * so the print styles are isolated and display correctly in the preview.
     */
    function renderPreview(html) {
        var container = document.getElementById('reportPreview');
        if (!container) return;

        // Remove placeholder
        var placeholder = document.getElementById('previewPlaceholder');
        if (placeholder) placeholder.style.display = 'none';

        // Build or reuse an iframe for the preview
        var iframe = document.getElementById('reportIframe');
        if (!iframe) {
            iframe = document.createElement('iframe');
            iframe.id = 'reportIframe';
            iframe.style.cssText = [
                'width:100%',
                'min-height:600px',
                'border:none',
                'border-radius:4px',
                'display:block'
            ].join(';');
            container.appendChild(iframe);
        }

        // Write the HTML into the iframe
        var doc = iframe.contentDocument || iframe.contentWindow.document;
        doc.open();
        doc.write(html);
        doc.close();

        // Auto-resize iframe to content height after load
        iframe.onload = function () {
            try {
                var body = iframe.contentDocument.body;
                iframe.style.height = (body.scrollHeight + 40) + 'px';
            } catch (e) { /* cross-origin safety */ }
        };

        // Show preview actions toolbar
        var previewActions = document.getElementById('previewActions');
        if (previewActions) previewActions.style.display = 'block';

        // Scroll smoothly to the preview section
        container.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }

    function showPrintButton(show) {
        // Card-level print button (Card 1 — Student only)
        var cardBtn = document.getElementById('btnPrintStudent');
        if (cardBtn) cardBtn.style.display = show ? 'inline-flex' : 'none';

        // Preview panel print button
        var previewBtn = document.getElementById('btnPrintPreview');
        if (previewBtn) previewBtn.style.display = show ? 'inline-flex' : 'none';
    }

    function setButtonLoading(btn, loading, label) {
        if (!btn) return;
        btn.disabled = loading;
        if (loading) {
            btn.dataset.originalHtml = btn.innerHTML;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin me-1"></i>' + escHtml(label);
        } else {
            btn.innerHTML = label;
        }
    }

    function showLoadingOverlay(message) {
        var overlay = document.getElementById('loadingOverlay');
        var msg     = document.getElementById('loadingMessage');
        if (overlay) overlay.classList.add('active');
        if (msg)     msg.textContent = message || 'Loading…';
    }

    function hideLoadingOverlay() {
        var overlay = document.getElementById('loadingOverlay');
        if (overlay) overlay.classList.remove('active');
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS — Network
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * POST JSON to a URL, returns Promise<string> (the response text).
     * Adds ASP.NET MVC RequestVerificationToken if present in the DOM.
     */
    function postJson(url, payload) {
        return new Promise(function (resolve, reject) {
            var xhr = new XMLHttpRequest();
            xhr.open('POST', url, true);
            xhr.setRequestHeader('Content-Type', 'application/json; charset=utf-8');
            xhr.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

            // Include AntiForgery token if present
            var tokenEl = document.querySelector('input[name="__RequestVerificationToken"]');
            if (tokenEl) {
                xhr.setRequestHeader('RequestVerificationToken', tokenEl.value);
            }

            xhr.onload = function () {
                if (xhr.status >= 200 && xhr.status < 300) {
                    resolve(xhr.responseText);
                } else {
                    reject('Server returned ' + xhr.status + ': ' + xhr.statusText);
                }
            };

            xhr.onerror = function () {
                reject('Network error — please check your connection.');
            };

            xhr.ontimeout = function () {
                reject('Request timed out.');
            };

            xhr.timeout = 60000; // 60 s — reports can be slow on large datasets

            try {
                xhr.send(JSON.stringify(payload));
            } catch (e) {
                reject('Failed to send request: ' + e.message);
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // showToast — Bootstrap 5 toast helper
    // type: 'success' | 'danger' | 'warning' | 'info'
    // ═══════════════════════════════════════════════════════════════════════
    window.showToast = function (message, type) {
        type = type || 'info';

        var iconMap = {
            success: 'fas fa-check-circle',
            danger:  'fas fa-exclamation-circle',
            warning: 'fas fa-exclamation-triangle',
            info:    'fas fa-info-circle'
        };

        var colorMap = {
            success: '#11B067',
            danger:  '#dc3545',
            warning: '#f59e0b',
            info:    '#0d6efd'
        };

        var icon  = iconMap[type]  || iconMap.info;
        var color = colorMap[type] || colorMap.info;

        var id = 'toast-' + Date.now();
        var toastHtml = [
            '<div id="' + id + '" class="toast align-items-center border-0 shadow-sm" role="alert"',
            ' aria-live="assertive" aria-atomic="true" data-bs-delay="4000"',
            ' style="border-left:4px solid ' + color + ' !important;">',
            '  <div class="d-flex">',
            '    <div class="toast-body d-flex align-items-center gap-2" style="font-size:13px;">',
            '      <i class="' + icon + '" style="color:' + color + ';font-size:15px;flex-shrink:0;"></i>',
            '      <span>' + escHtml(message) + '</span>',
            '    </div>',
            '    <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast"></button>',
            '  </div>',
            '</div>'
        ].join('');

        var container = document.getElementById('toastContainer');
        if (!container) return;

        container.insertAdjacentHTML('beforeend', toastHtml);

        var el = document.getElementById(id);
        if (el && window.bootstrap && bootstrap.Toast) {
            var toast = new bootstrap.Toast(el, { autohide: true, delay: 4000 });
            toast.show();
            el.addEventListener('hidden.bs.toast', function () {
                if (el.parentNode) el.parentNode.removeChild(el);
            });
        }
    };

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITY — escHtml
    // Prevents XSS when inserting dynamic text into innerHTML.
    // ═══════════════════════════════════════════════════════════════════════
    function escHtml(str) {
        if (str === null || str === undefined) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

}());
