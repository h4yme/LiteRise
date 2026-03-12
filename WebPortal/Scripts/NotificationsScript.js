/*!
 * NotificationsScript.js
 * LiteRise Web Portal — Notifications Section
 * Handles: Notifications list, compose form, templates, filtering
 * Dependencies: Bootstrap 5
 */
(function () {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═══════════════════════════════════════════════════════════════════════
    var MAX_BODY_CHARS = 500;

    // ═══════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS LIST
    // ═══════════════════════════════════════════════════════════════════════
    function loadNotifications() {
        fetch('/Notifications/GetNotifications')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                window.allNotifications = Array.isArray(data) ? data : (data.notifications || []);
                renderNotifications(window.allNotifications);
            })
            .catch(function (err) {
                console.error('loadNotifications error:', err);
                showToast('Failed to load notifications.', 'danger');
            });
    }

    function filterNotifications() {
        var search = (document.getElementById('notifSearch')       || {}).value || '';
        var type   = (document.getElementById('notifTypeFilter')   || {}).value || '';
        var status = (document.getElementById('notifStatusFilter') || {}).value || '';

        var all = window.allNotifications || [];
        var filtered = all.filter(function (n) {
            var matchSearch = !search ||
                (n.title  || '').toLowerCase().includes(search.toLowerCase()) ||
                (n.body   || '').toLowerCase().includes(search.toLowerCase());
            var matchType   = !type   || (n.type   || '') === type;
            var matchStatus = !status || (n.status || '').toLowerCase() === status.toLowerCase();
            return matchSearch && matchType && matchStatus;
        });

        renderNotifications(filtered);
    }

    function renderNotifications(list) {
        var container = document.getElementById('notificationsList');
        if (!container) return;

        if (!list || list.length === 0) {
            container.innerHTML =
                '<div class="text-center text-muted py-5">' +
                '<div style="font-size:2.5rem;">&#128276;</div>' +
                '<p class="mt-2">No notifications found.</p>' +
                '</div>';
            return;
        }

        container.innerHTML = list.map(function (n) {
            var statusBadge  = buildStatusBadge(n.status);
            var typeBadge    = buildTypeBadge(n.type);
            var targetBadge  = '<span class="badge bg-secondary">' + escHtml(n.target || 'All') + '</span>';
            var sentDate     = n.sentAt     ? formatDate(n.sentAt)     : (n.scheduledFor ? 'Scheduled: ' + formatDate(n.scheduledFor) : '—');
            var recipients   = n.recipientCount != null ? n.recipientCount.toLocaleString() : '—';

            return '<div class="list-group-item list-group-item-action p-3 border-bottom">' +
                '<div class="d-flex justify-content-between align-items-start gap-2">' +
                '<div class="flex-grow-1 min-width-0">' +
                '<div class="d-flex flex-wrap align-items-center gap-1 mb-1">' +
                targetBadge + typeBadge + statusBadge +
                '</div>' +
                '<h6 class="mb-1 text-truncate fw-semibold">' + escHtml(n.title || '') + '</h6>' +
                '<p class="mb-1 small text-muted text-truncate">' + escHtml((n.body || '').slice(0, 120)) + '</p>' +
                '<div class="d-flex gap-3 small text-muted mt-1">' +
                '<span>&#128197; ' + sentDate + '</span>' +
                '<span>&#128100; ' + recipients + ' recipients</span>' +
                '</div>' +
                '</div>' +
                '</div></div>';
        }).join('');
    }

    function buildStatusBadge(status) {
        var s = (status || '').toLowerCase();
        if (s === 'sent')       return '<span class="badge bg-success">Sent</span>';
        if (s === 'scheduled')  return '<span class="badge" style="background:#f97316;">Scheduled</span>';
        if (s === 'failed')     return '<span class="badge bg-danger">Failed</span>';
        if (s === 'draft')      return '<span class="badge bg-secondary">Draft</span>';
        return '<span class="badge bg-secondary">' + escHtml(status || 'Unknown') + '</span>';
    }

    function buildTypeBadge(type) {
        var map = {
            'Announcement': 'bg-primary',
            'Reminder':     'bg-warning text-dark',
            'Alert':        'bg-danger',
            'Achievement':  'bg-success',
            'System':       'bg-secondary'
        };
        var cls = map[type] || 'bg-info text-dark';
        return '<span class="badge ' + cls + '">' + escHtml(type || 'General') + '</span>';
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TEMPLATES
    // ═══════════════════════════════════════════════════════════════════════
    function loadTemplates() {
        fetch('/Notifications/GetTemplates')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                var templates = Array.isArray(data) ? data : (data.templates || []);
                renderTemplates(templates);
            })
            .catch(function (err) {
                console.error('loadTemplates error:', err);
                // Non-critical — fail silently; templates section stays empty
            });
    }

    function renderTemplates(templates) {
        var section = document.getElementById('templatesSection');
        if (!section) return;

        if (!templates || templates.length === 0) {
            section.innerHTML = '<p class="text-muted small">No templates available.</p>';
            return;
        }

        section.innerHTML = '<p class="small text-muted mb-2">Quick templates:</p>' +
            '<div class="d-flex flex-wrap gap-2">' +
            templates.map(function (t) {
                var title = escJs(t.title || '');
                var body  = escJs(t.body  || '');
                return '<button type="button" class="btn btn-sm btn-outline-secondary" ' +
                    'onclick="applyTemplate(\'' + title + '\',\'' + body + '\')">' +
                    escHtml(t.name || t.title || 'Template') +
                    '</button>';
            }).join('') +
            '</div>';
    }

    function applyTemplate(title, body) {
        var titleEl = document.getElementById('notifTitle');
        var bodyEl  = document.getElementById('notifBody');
        if (titleEl) titleEl.value = title;
        if (bodyEl)  { bodyEl.value = body; updateCharCounter(); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // COMPOSE FORM
    // ═══════════════════════════════════════════════════════════════════════
    function handleTargetChange() {
        var target      = getVal('notifTarget');
        var schoolGroup = document.getElementById('notifSchoolGroup');
        var gradeGroup  = document.getElementById('notifGradeGroup');

        if (schoolGroup) schoolGroup.style.display = target === 'BySchool' ? '' : 'none';
        if (gradeGroup)  gradeGroup.style.display  = target === 'ByGrade'  ? '' : 'none';
    }

    function handleSendNowToggle() {
        var toggle       = document.getElementById('sendNow');
        var scheduledGrp = document.getElementById('scheduledForGroup');
        if (!toggle || !scheduledGrp) return;
        scheduledGrp.style.display = toggle.checked ? 'none' : '';
    }

    function updateCharCounter() {
        var bodyEl   = document.getElementById('notifBody');
        var counterEl = document.getElementById('notifBodyCounter');
        if (!bodyEl) return;

        var len  = (bodyEl.value || '').length;
        var over = len > MAX_BODY_CHARS;

        if (counterEl) {
            counterEl.textContent = len + ' / ' + MAX_BODY_CHARS;
            counterEl.style.color = over ? '#dc3545' : '';
            counterEl.style.fontWeight = over ? '600' : '';
        }
        bodyEl.classList.toggle('is-invalid', over);
    }

    function sendNotification() {
        var title       = getVal('notifTitle');
        var body        = getVal('notifBody');
        var target      = getVal('notifTarget');
        var school      = getVal('notifSchool');
        var grade       = getVal('notifGrade');
        var type        = getVal('notifType');
        var sendNowEl   = document.getElementById('sendNow');
        var sendNow     = sendNowEl ? sendNowEl.checked : true;
        var scheduledFor = getVal('scheduledFor');

        // Validation
        if (!title.trim()) {
            showToast('Notification title is required.', 'danger');
            focusField('notifTitle');
            return;
        }
        if (!body.trim()) {
            showToast('Notification body is required.', 'danger');
            focusField('notifBody');
            return;
        }
        if (body.length > MAX_BODY_CHARS) {
            showToast('Body must be ' + MAX_BODY_CHARS + ' characters or fewer.', 'danger');
            focusField('notifBody');
            return;
        }
        if (target === 'BySchool' && !school.trim()) {
            showToast('Please select a school.', 'danger');
            focusField('notifSchool');
            return;
        }
        if (target === 'ByGrade' && !grade.trim()) {
            showToast('Please select a grade.', 'danger');
            focusField('notifGrade');
            return;
        }
        if (!sendNow && !scheduledFor.trim()) {
            showToast('Please select a scheduled date and time.', 'danger');
            focusField('scheduledFor');
            return;
        }
        if (!sendNow && scheduledFor) {
            var schedDate = new Date(scheduledFor);
            if (isNaN(schedDate.getTime()) || schedDate <= new Date()) {
                showToast('Scheduled time must be in the future.', 'danger');
                focusField('scheduledFor');
                return;
            }
        }

        var payload = {
            title:        title,
            body:         body,
            target:       target,
            school:       target === 'BySchool' ? school : null,
            grade:        target === 'ByGrade'  ? grade  : null,
            type:         type,
            sendNow:      sendNow,
            scheduledFor: sendNow ? null : scheduledFor
        };

        function doSend() {
            // Disable send button to prevent double submit
            var sendBtn = document.getElementById('sendNotifBtn');
            if (sendBtn) { sendBtn.disabled = true; sendBtn.textContent = sendNow ? 'Sending…' : 'Scheduling…'; }

            fetch('/Notifications/SendNotification', {
                method:  'POST',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify(payload)
            })
                .then(function (r) { return r.json(); })
                .then(function (res) {
                    if (res && res.success === false) throw new Error(res.message || 'Operation failed.');
                    var msg = sendNow ? 'Notification sent successfully.' : 'Notification scheduled successfully.';
                    showToast(msg, 'success');
                    clearCompose();
                    loadNotifications();
                })
                .catch(function (err) {
                    showToast(err.message || 'Failed to send notification.', 'danger');
                })
                .finally(function () {
                    if (sendBtn) { sendBtn.disabled = false; sendBtn.textContent = sendNow ? 'Send Now' : 'Schedule'; }
                });
        }

        if (!target || target === 'All') {
            Swal.fire({
                title: 'Confirm Send',
                html: 'You are about to send a notification to <strong>ALL students</strong>.<br><small style="color:#6b7280">This action cannot be undone.</small>',
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#11B067',
                cancelButtonColor: '#6b7280',
                confirmButtonText: 'Yes, Send',
                cancelButtonText: 'Cancel'
            }).then(function (result) {
                if (result.isConfirmed) doSend();
            });
        } else {
            doSend();
        }
    }

    function clearCompose() {
        var form = document.getElementById('composeForm');
        if (form) {
            form.reset();
        } else {
            // Manually clear known fields
            ['notifTitle', 'notifBody', 'notifTarget', 'notifSchool', 'notifGrade',
             'notifType', 'scheduledFor'].forEach(function (id) {
                var el = document.getElementById(id);
                if (el) el.value = '';
            });
            var sendNow = document.getElementById('sendNow');
            if (sendNow) sendNow.checked = true;
        }
        updateCharCounter();
        handleTargetChange();
        handleSendNowToggle();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════════════════════
    function showToast(message, type) {
        type = type || 'success';
        var container = document.getElementById('toastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'toastContainer';
            container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            container.style.zIndex = '1090';
            document.body.appendChild(container);
        }

        var toastId = 'toast_' + Date.now();
        var bgClass = type === 'success' ? 'bg-success' : 'bg-danger';
        var icon    = type === 'success' ? '&#10003;' : '&#33;';

        var html = '<div id="' + toastId + '" class="toast align-items-center text-white ' + bgClass + ' border-0" ' +
            'role="alert" aria-live="assertive" aria-atomic="true">' +
            '<div class="d-flex">' +
            '<div class="toast-body d-flex align-items-center gap-2">' +
            '<span>' + icon + '</span>' +
            '<span>' + escHtml(message) + '</span>' +
            '</div>' +
            '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>' +
            '</div></div>';

        container.insertAdjacentHTML('beforeend', html);
        var toastEl = document.getElementById(toastId);
        if (toastEl && window.bootstrap && bootstrap.Toast) {
            var toast = new bootstrap.Toast(toastEl, { delay: 4500 });
            toast.show();
            toastEl.addEventListener('hidden.bs.toast', function () { toastEl.remove(); });
        }
    }

    function getVal(id) {
        var el = document.getElementById(id);
        return el ? el.value : '';
    }

    function focusField(id) {
        var el = document.getElementById(id);
        if (el) { el.focus(); el.scrollIntoView({ behavior: 'smooth', block: 'center' }); }
    }

    function escHtml(str) {
        return String(str)
            .replace(/&/g,  '&amp;')
            .replace(/</g,  '&lt;')
            .replace(/>/g,  '&gt;')
            .replace(/"/g,  '&quot;')
            .replace(/'/g,  '&#39;');
    }

    function escJs(str) {
        return String(str).replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/\n/g, '\\n');
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        try {
            var d = new Date(dateStr);
            if (isNaN(d.getTime())) return String(dateStr);
            return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
        } catch (e) { return String(dateStr); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXPOSE GLOBALS
    // ═══════════════════════════════════════════════════════════════════════
    window.loadNotifications    = loadNotifications;
    window.filterNotifications  = filterNotifications;
    window.loadTemplates        = loadTemplates;
    window.applyTemplate        = applyTemplate;
    window.handleTargetChange   = handleTargetChange;
    window.handleSendNowToggle  = handleSendNowToggle;
    window.updateCharCounter    = updateCharCounter;
    window.sendNotification     = sendNotification;
    window.clearCompose         = clearCompose;
    window.showToast            = window.showToast || showToast;

    // ═══════════════════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', function () {
        // Search and filter inputs
        var searchEl = document.getElementById('notifSearch');
        if (searchEl) searchEl.addEventListener('input', filterNotifications);

        var typeFilter = document.getElementById('notifTypeFilter');
        if (typeFilter) typeFilter.addEventListener('change', filterNotifications);

        var statusFilter = document.getElementById('notifStatusFilter');
        if (statusFilter) statusFilter.addEventListener('change', filterNotifications);

        // Compose form controls
        var targetSel = document.getElementById('notifTarget');
        if (targetSel) targetSel.addEventListener('change', handleTargetChange);

        var sendNowToggle = document.getElementById('sendNow');
        if (sendNowToggle) sendNowToggle.addEventListener('change', handleSendNowToggle);

        var bodyEl = document.getElementById('notifBody');
        if (bodyEl) bodyEl.addEventListener('input', updateCharCounter);

        // Init display states
        handleTargetChange();
        handleSendNowToggle();
        updateCharCounter();

        // Load data
        loadNotifications();
        loadTemplates();
    });

}());
