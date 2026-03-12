/*!
 * MasterfileScript.js
 * LiteRise Web Portal — Masterfile Section
 * Handles: Question Bank, Lessons & Modules, Badges, Administrators tabs
 * Dependencies: Bootstrap 5
 */
(function () {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════════════
    var PRIMARY_GREEN = '#11B067';

    var questionsPage     = 1;
    var QUESTIONS_PER_PAGE = 25;
    var pendingDeactivateId = null;
    var csvParsedRows     = [];

    // Lazy-load flags
    var modulesLoaded = false;
    var badgesLoaded  = false;

    // ═══════════════════════════════════════════════════════════════════════
    // TAB SWITCHING
    // ═══════════════════════════════════════════════════════════════════════
    function initTabs() {
        document.querySelectorAll('.tab-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var tab = btn.getAttribute('data-tab');
                switchTab(tab);
            });
        });
    }

    function switchTab(tab) {
        document.querySelectorAll('.tab-btn').forEach(function (b) {
            b.classList.remove('active');
        });
        var active = document.querySelector('.tab-btn[data-tab="' + tab + '"]');
        if (active) active.classList.add('active');

        document.querySelectorAll('.tab-panel').forEach(function (p) {
            p.style.display = 'none';
        });
        var panel = document.getElementById('tab-' + tab);
        if (panel) panel.style.display = '';

        if (tab === 'questions') {
            if (!window.allQuestions || window.allQuestions.length === 0) loadQuestions();
        } else if (tab === 'modules') {
            if (!modulesLoaded) loadModules();
        } else if (tab === 'badges') {
            if (!badgesLoaded) loadBadges();
        } else if (tab === 'admins') {
            if (!window.allAdmins || window.allAdmins.length === 0) loadAdmins();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // QUESTION BANK
    // ═══════════════════════════════════════════════════════════════════════
    function loadQuestions() {
        fetch('/Masterfile/GetQuestions')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                window.allQuestions = Array.isArray(data) ? data : (data.questions || []);
                questionsPage = 1;
                renderQuestions();
            })
            .catch(function (err) {
                console.error('loadQuestions error:', err);
                showToast('Failed to load questions.', 'danger');
            });
    }

    function filterQuestions() {
        questionsPage = 1;
        renderQuestions();
    }

    function renderQuestions() {
        var tbody = document.getElementById('questionsTableBody');
        if (!tbody) return;

        var search   = (document.getElementById('questionSearch')  || {}).value || '';
        var category = (document.getElementById('categoryFilter')  || {}).value || '';
        var type     = (document.getElementById('typeFilter')      || {}).value || '';

        var all = window.allQuestions || [];
        var filtered = all.filter(function (q) {
            var matchSearch   = !search   || (q.questionText || '').toLowerCase().includes(search.toLowerCase()) ||
                                             String(q.id || '').includes(search);
            var matchCategory = !category || (q.category || '') === category;
            var matchType     = !type     || (q.type || '') === type;
            return matchSearch && matchCategory && matchType;
        });

        var totalPages = Math.max(1, Math.ceil(filtered.length / QUESTIONS_PER_PAGE));
        if (questionsPage > totalPages) questionsPage = totalPages;

        var start = (questionsPage - 1) * QUESTIONS_PER_PAGE;
        var page  = filtered.slice(start, start + QUESTIONS_PER_PAGE);

        tbody.innerHTML = page.map(function (q) {
            var preview  = (q.questionText || '').slice(0, 60) + ((q.questionText || '').length > 60 ? '…' : '');
            var active   = q.isActive !== false;
            var statusBadge = active
                ? '<span class="badge bg-success">Active</span>'
                : '<span class="badge bg-secondary">Inactive</span>';
            var diffVal  = q.difficulty != null ? parseFloat(q.difficulty).toFixed(3) : '—';
            var discVal  = q.discrimination != null ? parseFloat(q.discrimination).toFixed(3) : '—';
            return '<tr>' +
                '<td>' + escHtml(String(q.id || '')) + '</td>' +
                '<td>' + escHtml(q.category || '') + '</td>' +
                '<td>' + diffVal + '</td>' +
                '<td>' + discVal + '</td>' +
                '<td>' + escHtml(q.type || '') + '</td>' +
                '<td class="text-truncate" style="max-width:240px;" title="' + escHtml(q.questionText || '') + '">' + escHtml(preview) + '</td>' +
                '<td>' + statusBadge + '</td>' +
                '<td>' +
                  '<button class="btn btn-sm btn-outline-primary me-1" onclick="openEditQuestion(' + q.id + ')">Edit</button>' +
                  (active ? '<button class="btn btn-sm btn-outline-danger" onclick="deactivateQuestion(' + q.id + ')">Deactivate</button>'
                          : '<button class="btn btn-sm btn-outline-success" onclick="deactivateQuestion(' + q.id + ')">Activate</button>') +
                '</td>' +
                '</tr>';
        }).join('');

        updateQuestionsPagination(filtered.length, totalPages);
    }

    function updateQuestionsPagination(total, totalPages) {
        var info = document.getElementById('questionsPaginationInfo');
        if (info) info.textContent = 'Page ' + questionsPage + ' of ' + totalPages + ' (' + total + ' questions)';

        var prevBtn = document.getElementById('questionsPrevBtn');
        var nextBtn = document.getElementById('questionsNextBtn');
        if (prevBtn) prevBtn.disabled = questionsPage <= 1;
        if (nextBtn) nextBtn.disabled = questionsPage >= totalPages;
    }

    function questionsPagePrev() {
        if (questionsPage > 1) { questionsPage--; renderQuestions(); }
    }

    function questionsPageNext() {
        questionsPage++;
        renderQuestions();
    }

    function openAddQuestion() {
        var form = document.getElementById('questionForm');
        if (form) form.reset();
        var hiddenId = document.getElementById('questionId');
        if (hiddenId) hiddenId.value = '';
        var title = document.getElementById('questionModalTitle');
        if (title) title.textContent = 'Add Question';
        showModal('questionModal');
    }

    function openEditQuestion(id) {
        var q = (window.allQuestions || []).find(function (x) { return x.id === id; });
        if (!q) { showToast('Question not found.', 'danger'); return; }

        setVal('questionId',         q.id);
        setVal('questionCategory',   q.category);
        setVal('questionType',       q.type);
        setVal('questionDifficulty', q.difficulty);
        setVal('questionDiscrim',    q.discrimination);
        setVal('questionText',       q.questionText);
        setVal('questionChoices',    (q.choices || []).join('\n'));
        setVal('questionAnswer',     q.answer);

        var title = document.getElementById('questionModalTitle');
        if (title) title.textContent = 'Edit Question';
        showModal('questionModal');
    }

    function saveQuestion() {
        var category   = getVal('questionCategory');
        var type       = getVal('questionType');
        var difficulty = getVal('questionDifficulty');
        var discrim    = getVal('questionDiscrim');
        var text       = getVal('questionText');

        if (!category)   { showToast('Category is required.', 'danger');        return; }
        if (!type)        { showToast('Question type is required.', 'danger');   return; }
        if (!difficulty)  { showToast('Difficulty (b) is required.', 'danger');  return; }
        if (!discrim)     { showToast('Discrimination (a) is required.', 'danger'); return; }
        if (!text.trim()) { showToast('Question text is required.', 'danger');   return; }

        var payload = {
            id:             getVal('questionId') || 0,
            category:       category,
            type:           type,
            difficulty:     parseFloat(difficulty),
            discrimination: parseFloat(discrim),
            questionText:   text,
            choices:        (getVal('questionChoices') || '').split('\n').map(function (s) { return s.trim(); }).filter(Boolean),
            answer:         getVal('questionAnswer')
        };

        fetch('/Masterfile/SaveQuestion', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(payload)
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res && res.success === false) throw new Error(res.message || 'Save failed.');
                hideModal('questionModal');
                showToast('Question saved successfully.', 'success');
                loadQuestions();
            })
            .catch(function (err) {
                showToast(err.message || 'Failed to save question.', 'danger');
            });
    }

    function deactivateQuestion(id) {
        var q      = (window.allQuestions || []).find(function (x) { return x.id === id; });
        var active = q ? q.isActive !== false : true;
        var action = active ? 'deactivate' : 'activate';
        if (!confirm('Are you sure you want to ' + action + ' this question?')) return;

        fetch('/Masterfile/DeactivateQuestion', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ id: id })
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res && res.success === false) throw new Error(res.message || 'Operation failed.');
                showToast('Question ' + action + 'd.', 'success');
                loadQuestions();
            })
            .catch(function (err) {
                showToast(err.message || 'Failed to update question.', 'danger');
            });
    }

    function triggerCsvImport() {
        var inp = document.getElementById('csvFileInput');
        if (inp) inp.click();
    }

    function handleCsvImport(input) {
        var file = input.files && input.files[0];
        if (!file) return;

        var reader = new FileReader();
        reader.onload = function (e) {
            var text = e.target.result;
            csvParsedRows = parseCsv(text);

            var preview = document.getElementById('csvPreviewBody');
            if (preview) {
                preview.innerHTML = csvParsedRows.slice(0, 10).map(function (row) {
                    return '<tr>' + row.map(function (cell) {
                        return '<td>' + escHtml(String(cell)) + '</td>';
                    }).join('') + '</tr>';
                }).join('');
            }
            var countEl = document.getElementById('csvPreviewCount');
            if (countEl) countEl.textContent = csvParsedRows.length + ' rows parsed.';
            showModal('csvPreviewModal');
        };
        reader.readAsText(file);
        input.value = '';
    }

    function parseCsv(text) {
        var lines = text.split(/\r?\n/).filter(function (l) { return l.trim(); });
        return lines.slice(1).map(function (line) {
            var result = [];
            var cur = '';
            var inQuote = false;
            for (var i = 0; i < line.length; i++) {
                var ch = line[i];
                if (ch === '"') {
                    if (inQuote && line[i + 1] === '"') { cur += '"'; i++; }
                    else { inQuote = !inQuote; }
                } else if (ch === ',' && !inQuote) {
                    result.push(cur); cur = '';
                } else {
                    cur += ch;
                }
            }
            result.push(cur);
            return result;
        });
    }

    function confirmCsvImport() {
        if (!csvParsedRows || csvParsedRows.length === 0) {
            showToast('No rows to import.', 'danger');
            return;
        }

        fetch('/Masterfile/ImportQuestions', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ rows: csvParsedRows })
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res && res.success === false) throw new Error(res.message || 'Import failed.');
                hideModal('csvPreviewModal');
                showToast('Questions imported successfully (' + (res.count || csvParsedRows.length) + ' rows).', 'success');
                csvParsedRows = [];
                loadQuestions();
            })
            .catch(function (err) {
                showToast(err.message || 'Failed to import questions.', 'danger');
            });
    }

    function exportQuestionsCsv() {
        var search   = (document.getElementById('questionSearch')  || {}).value || '';
        var category = (document.getElementById('categoryFilter')  || {}).value || '';
        var type     = (document.getElementById('typeFilter')      || {}).value || '';

        var all = window.allQuestions || [];
        var filtered = all.filter(function (q) {
            var matchSearch   = !search   || (q.questionText || '').toLowerCase().includes(search.toLowerCase()) ||
                                             String(q.id || '').includes(search);
            var matchCategory = !category || (q.category || '') === category;
            var matchType     = !type     || (q.type || '') === type;
            return matchSearch && matchCategory && matchType;
        });

        var headers = ['ID', 'Category', 'Type', 'Difficulty(b)', 'Discrimination(a)', 'QuestionText', 'Answer', 'Active'];
        var rows = [headers].concat(filtered.map(function (q) {
            return [
                q.id,
                q.category,
                q.type,
                q.difficulty,
                q.discrimination,
                '"' + (q.questionText || '').replace(/"/g, '""') + '"',
                '"' + (q.answer || '').replace(/"/g, '""') + '"',
                q.isActive !== false ? 'Yes' : 'No'
            ];
        }));

        var csv  = rows.map(function (r) { return r.join(','); }).join('\r\n');
        var blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
        var url  = URL.createObjectURL(blob);
        var a    = document.createElement('a');
        a.href     = url;
        a.download = 'questions_export_' + isoDate() + '.csv';
        document.body.appendChild(a);
        a.click();
        setTimeout(function () { document.body.removeChild(a); URL.revokeObjectURL(url); }, 1000);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MODULE LADDER
    // ═══════════════════════════════════════════════════════════════════════
    function loadModules() {
        fetch('/Masterfile/GetModuleLadder')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                modulesLoaded = true;
                var modules = Array.isArray(data) ? data : (data.modules || []);
                renderModulesAccordion(modules);
            })
            .catch(function (err) {
                console.error('loadModules error:', err);
                showToast('Failed to load modules.', 'danger');
            });
    }

    function renderModulesAccordion(modules) {
        var accordion = document.getElementById('modulesAccordion');
        if (!accordion) return;

        if (!modules || modules.length === 0) {
            accordion.innerHTML = '<div class="text-muted p-3">No modules found.</div>';
            return;
        }

        accordion.innerHTML = modules.map(function (mod, idx) {
            var collapseId = 'modCollapse' + idx;
            var headingId  = 'modHeading'  + idx;
            var nodes      = Array.isArray(mod.nodes) ? mod.nodes : [];
            var enabledBadge = mod.isEnabled !== false
                ? '<span class="badge bg-success ms-2">Enabled</span>'
                : '<span class="badge bg-secondary ms-2">Disabled</span>';

            var nodeRows = nodes.map(function (node) {
                var toggleLabel  = node.isEnabled !== false ? 'Disable' : 'Enable';
                var toggleClass  = node.isEnabled !== false ? 'btn-outline-warning' : 'btn-outline-success';
                var enabledIcon  = node.isEnabled !== false
                    ? '<span class="badge bg-success">On</span>'
                    : '<span class="badge bg-secondary">Off</span>';
                return '<tr>' +
                    '<td>' + escHtml(String(node.nodeNumber || '')) + '</td>' +
                    '<td>' + escHtml(node.title || '') + '</td>' +
                    '<td>' + escHtml(node.type  || '') + '</td>' +
                    '<td>' + escHtml(String(node.order != null ? node.order : '')) + '</td>' +
                    '<td>' + enabledIcon + '</td>' +
                    '<td>' +
                      '<button class="btn btn-sm ' + toggleClass + ' me-1" onclick="toggleNode(' + node.id + ',' + (node.isEnabled !== false ? 'false' : 'true') + ')">' + toggleLabel + '</button>' +
                      '<button class="btn btn-sm btn-outline-info" onclick="showNodePreview(' + node.id + ')">Preview</button>' +
                    '</td>' +
                    '</tr>';
            }).join('');

            var nodeTable = nodes.length > 0
                ? '<div class="table-responsive"><table class="table table-sm table-hover mb-0">' +
                  '<thead class="table-light"><tr><th>#</th><th>Title</th><th>Type</th><th>Order</th><th>Enabled</th><th>Actions</th></tr></thead>' +
                  '<tbody>' + nodeRows + '</tbody></table></div>'
                : '<p class="text-muted mb-0 ps-2">No nodes in this module.</p>';

            return '<div class="accordion-item">' +
                '<h2 class="accordion-header" id="' + headingId + '">' +
                '<button class="accordion-button collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#' + collapseId + '" aria-expanded="false" aria-controls="' + collapseId + '">' +
                '<strong>' + escHtml(mod.name || 'Module ' + (idx + 1)) + '</strong>' +
                '<span class="ms-3 text-muted small">' + nodes.length + ' node' + (nodes.length !== 1 ? 's' : '') + '</span>' +
                enabledBadge +
                '</button></h2>' +
                '<div id="' + collapseId + '" class="accordion-collapse collapse" aria-labelledby="' + headingId + '" data-bs-parent="#modulesAccordion">' +
                '<div class="accordion-body p-0">' + nodeTable + '</div>' +
                '</div></div>';
        }).join('');
    }

    function toggleNode(nodeId, enabled) {
        fetch('/Masterfile/ToggleNode', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ nodeId: nodeId, enabled: enabled })
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res && res.success === false) throw new Error(res.message || 'Toggle failed.');
                showToast('Node ' + (enabled ? 'enabled' : 'disabled') + '.', 'success');
                modulesLoaded = false;
                loadModules();
            })
            .catch(function (err) {
                showToast(err.message || 'Failed to toggle node.', 'danger');
            });
    }

    function showNodePreview(nodeId) {
        // Try to find node from already-rendered data via a global cache
        var allNodes = window.allNodes || [];
        var node = allNodes.find(function (n) { return n.id === nodeId; });

        var modalBody = document.getElementById('nodePreviewBody');
        if (modalBody) {
            if (node) {
                modalBody.innerHTML =
                    '<dl class="row mb-0">' +
                    '<dt class="col-sm-4">Node ID</dt><dd class="col-sm-8">' + escHtml(String(node.id)) + '</dd>' +
                    '<dt class="col-sm-4">Title</dt><dd class="col-sm-8">' + escHtml(node.title || '') + '</dd>' +
                    '<dt class="col-sm-4">Type</dt><dd class="col-sm-8">' + escHtml(node.type || '') + '</dd>' +
                    '<dt class="col-sm-4">Order</dt><dd class="col-sm-8">' + escHtml(String(node.order != null ? node.order : '')) + '</dd>' +
                    '<dt class="col-sm-4">Enabled</dt><dd class="col-sm-8">' + (node.isEnabled !== false ? 'Yes' : 'No') + '</dd>' +
                    '</dl>';
            } else {
                modalBody.innerHTML = '<p class="text-muted">Node ID: ' + nodeId + '</p>';
            }
        }
        showModal('nodePreviewModal');
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BADGES
    // ═══════════════════════════════════════════════════════════════════════
    function loadBadges() {
        fetch('/Masterfile/GetBadges')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                badgesLoaded = true;
                var badges = Array.isArray(data) ? data : (data.badges || []);
                renderBadgesGrid(badges);
                window.allBadges = badges;
            })
            .catch(function (err) {
                console.error('loadBadges error:', err);
                showToast('Failed to load badges.', 'danger');
            });
    }

    function renderBadgesGrid(badges) {
        var grid = document.getElementById('badgesGrid');
        if (!grid) return;

        if (!badges || badges.length === 0) {
            grid.innerHTML = '<div class="col-12 text-muted">No badges found.</div>';
            return;
        }

        grid.innerHTML = badges.map(function (b) {
            var icon = b.icon || '🏅';
            var catBadge = b.category
                ? '<span class="badge bg-primary-subtle text-primary-emphasis">' + escHtml(b.category) + '</span>'
                : '';
            return '<div class="col-sm-6 col-md-4 col-lg-3">' +
                '<div class="card h-100 shadow-sm">' +
                '<div class="card-body text-center">' +
                '<div style="font-size:2.5rem;">' + escHtml(icon) + '</div>' +
                '<h6 class="card-title mt-2 mb-1">' + escHtml(b.name || '') + '</h6>' +
                catBadge +
                '<p class="card-text small text-muted mt-2">' + escHtml(b.criteria || '') + '</p>' +
                '</div>' +
                '<div class="card-footer bg-transparent d-flex gap-2 justify-content-center">' +
                '<button class="btn btn-sm btn-outline-primary" onclick="openEditBadge(' + b.id + ')">Edit</button>' +
                '<button class="btn btn-sm btn-outline-danger" onclick="deleteBadge(' + b.id + ')">Delete</button>' +
                '</div></div></div>';
        }).join('');
    }

    function openAddBadge() {
        var form = document.getElementById('badgeForm');
        if (form) form.reset();
        var hiddenId = document.getElementById('badgeId');
        if (hiddenId) hiddenId.value = '';
        var title = document.getElementById('badgeModalTitle');
        if (title) title.textContent = 'Add Badge';
        showModal('badgeModal');
    }

    function openEditBadge(id) {
        var b = (window.allBadges || []).find(function (x) { return x.id === id; });
        if (!b) { showToast('Badge not found.', 'danger'); return; }

        setVal('badgeId',       b.id);
        setVal('badgeName',     b.name);
        setVal('badgeIcon',     b.icon);
        setVal('badgeCategory', b.category);
        setVal('badgeCriteria', b.criteria);

        var title = document.getElementById('badgeModalTitle');
        if (title) title.textContent = 'Edit Badge';
        showModal('badgeModal');
    }

    function saveBadge() {
        var name     = getVal('badgeName');
        var icon     = getVal('badgeIcon');
        var category = getVal('badgeCategory');
        var criteria = getVal('badgeCriteria');

        if (!name.trim())     { showToast('Badge name is required.', 'danger');     return; }
        if (!category.trim()) { showToast('Badge category is required.', 'danger'); return; }

        var payload = {
            id:       getVal('badgeId') || 0,
            name:     name,
            icon:     icon,
            category: category,
            criteria: criteria
        };

        fetch('/Masterfile/SaveBadge', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(payload)
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res && res.success === false) throw new Error(res.message || 'Save failed.');
                hideModal('badgeModal');
                showToast('Badge saved successfully.', 'success');
                badgesLoaded = false;
                loadBadges();
            })
            .catch(function (err) {
                showToast(err.message || 'Failed to save badge.', 'danger');
            });
    }

    function deleteBadge(id) {
        if (!confirm('Are you sure you want to delete this badge?')) return;

        fetch('/Masterfile/DeleteBadge', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ id: id })
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res && res.success === false) throw new Error(res.message || 'Delete failed.');
                showToast('Badge deleted.', 'success');
                badgesLoaded = false;
                loadBadges();
            })
            .catch(function (err) {
                showToast(err.message || 'Failed to delete badge.', 'danger');
            });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ADMIN ACCOUNTS
    // ═══════════════════════════════════════════════════════════════════════
    function loadAdmins() {
        fetch('/Masterfile/GetAdmins')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                window.allAdmins = Array.isArray(data) ? data : (data.admins || []);
                filterAdmins();
            })
            .catch(function (err) {
                console.error('loadAdmins error:', err);
                showToast('Failed to load administrators.', 'danger');
            });
    }

    function filterAdmins() {
        var search = (document.getElementById('adminSearch')     || {}).value || '';
        var role   = (document.getElementById('adminRoleFilter') || {}).value || '';

        var all = window.allAdmins || [];
        var filtered = all.filter(function (a) {
            var matchSearch = !search || (a.firstName + ' ' + a.lastName).toLowerCase().includes(search.toLowerCase()) ||
                              (a.email || '').toLowerCase().includes(search.toLowerCase());
            var matchRole   = !role   || (a.role || '') === role;
            return matchSearch && matchRole;
        });
        renderAdmins(filtered);
    }

    function renderAdmins(list) {
        var tbody = document.getElementById('adminsTableBody');
        if (!tbody) return;

        if (!list || list.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No administrators found.</td></tr>';
            return;
        }

        tbody.innerHTML = list.map(function (a) {
            var fullName   = ((a.firstName || '') + ' ' + (a.lastName || '')).trim();
            var initials   = ((a.firstName || ' ')[0] + (a.lastName || ' ')[0]).toUpperCase();
            var roleBadge  = roleColorClass(a.role);
            var active     = a.isActive !== false;
            var statusBadge = active
                ? '<span class="badge bg-success">Active</span>'
                : '<span class="badge bg-secondary">Inactive</span>';
            var lastLogin  = a.lastLogin ? formatDate(a.lastLogin) : 'Never';

            return '<tr>' +
                '<td><div class="d-flex align-items-center gap-2">' +
                '<div class="rounded-circle d-flex align-items-center justify-content-center text-white fw-bold" ' +
                'style="width:36px;height:36px;font-size:0.75rem;background:' + PRIMARY_GREEN + ';">' + escHtml(initials) + '</div>' +
                '<span>' + escHtml(fullName) + '</span></div></td>' +
                '<td>' + escHtml(a.email || '') + '</td>' +
                '<td><span class="badge ' + roleBadge + '">' + escHtml(a.role || '') + '</span></td>' +
                '<td>' + escHtml(a.school || '—') + '</td>' +
                '<td>' + lastLogin + '</td>' +
                '<td>' + statusBadge + '</td>' +
                '<td>' +
                  '<button class="btn btn-sm btn-outline-primary me-1" onclick="openEditAdmin(' + a.id + ')">Edit</button>' +
                  (active
                    ? '<button class="btn btn-sm btn-outline-danger" onclick="deactivateAdmin(' + a.id + ',\'' + escJs(fullName) + '\')">Deactivate</button>'
                    : '<button class="btn btn-sm btn-outline-success" onclick="deactivateAdmin(' + a.id + ',\'' + escJs(fullName) + '\')">Activate</button>') +
                '</td>' +
                '</tr>';
        }).join('');
    }

    function roleColorClass(role) {
        var map = {
            'SuperAdmin': 'bg-danger',
            'Admin':      'bg-primary',
            'Teacher':    'bg-info text-dark',
            'Viewer':     'bg-secondary'
        };
        return map[role] || 'bg-secondary';
    }

    function openAddAdmin() {
        var form = document.getElementById('adminForm');
        if (form) form.reset();
        var hiddenId = document.getElementById('adminId');
        if (hiddenId) hiddenId.value = '';
        var title = document.getElementById('adminModalTitle');
        if (title) title.textContent = 'Add Administrator';
        handleAdminRoleChange();
        showModal('adminModal');
    }

    function openEditAdmin(id) {
        var a = (window.allAdmins || []).find(function (x) { return x.id === id; });
        if (!a) { showToast('Admin not found.', 'danger'); return; }

        setVal('adminId',        a.id);
        setVal('adminFirstName', a.firstName);
        setVal('adminLastName',  a.lastName);
        setVal('adminEmail',     a.email);
        setVal('adminRole',      a.role);
        setVal('adminSchool',    a.school);

        var title = document.getElementById('adminModalTitle');
        if (title) title.textContent = 'Edit Administrator';
        handleAdminRoleChange();
        showModal('adminModal');
    }

    function handleAdminRoleChange() {
        var role       = getVal('adminRole');
        var schoolGrp  = document.getElementById('aSchoolGroup');
        if (schoolGrp) schoolGrp.style.display = role === 'Teacher' ? '' : 'none';
    }

    function saveAdmin() {
        var firstName = getVal('adminFirstName');
        var lastName  = getVal('adminLastName');
        var email     = getVal('adminEmail');
        var role      = getVal('adminRole');
        var school    = getVal('adminSchool');

        if (!firstName.trim()) { showToast('First name is required.', 'danger'); return; }
        if (!lastName.trim())  { showToast('Last name is required.',  'danger'); return; }
        if (!email.trim())     { showToast('Email is required.',      'danger'); return; }
        if (!validateEmail(email)) { showToast('Please enter a valid email address.', 'danger'); return; }
        if (!role)             { showToast('Role is required.',       'danger'); return; }
        if (role === 'Teacher' && !school.trim()) { showToast('School is required for Teacher role.', 'danger'); return; }

        var payload = {
            id:        getVal('adminId') || 0,
            firstName: firstName,
            lastName:  lastName,
            email:     email,
            role:      role,
            school:    school
        };

        fetch('/Masterfile/SaveAdmin', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify(payload)
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res && res.success === false) throw new Error(res.message || 'Save failed.');
                hideModal('adminModal');
                showToast('Administrator saved successfully.', 'success');
                loadAdmins();
            })
            .catch(function (err) {
                showToast(err.message || 'Failed to save administrator.', 'danger');
            });
    }

    function deactivateAdmin(id, name) {
        Swal.fire({
            title: 'Confirm Deactivation',
            html: 'Are you sure you want to deactivate <strong>' + escHtml(name || 'this administrator') + '</strong>?<br><small style="color:#6b7280">They will no longer be able to log in until reactivated.</small>',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#DC2626',
            cancelButtonColor: '#6b7280',
            confirmButtonText: 'Deactivate',
            cancelButtonText: 'Cancel'
        }).then(function (result) {
            if (result.isConfirmed) {
                pendingDeactivateId = id;
                confirmDeactivate();
            }
        });
    }

    function confirmDeactivate() {
        if (!pendingDeactivateId) return;

        fetch('/Masterfile/DeactivateAdmin', {
            method:  'POST',
            headers: { 'Content-Type': 'application/json' },
            body:    JSON.stringify({ id: pendingDeactivateId })
        })
            .then(function (r) { return r.json(); })
            .then(function (res) {
                if (res && res.success === false) throw new Error(res.message || 'Operation failed.');
                showToast('Administrator status updated.', 'success');
                pendingDeactivateId = null;
                loadAdmins();
            })
            .catch(function (err) {
                showToast(err.message || 'Failed to update administrator.', 'danger');
            });
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

        var toastId  = 'toast_' + Date.now();
        var bgClass  = type === 'success' ? 'bg-success' : 'bg-danger';
        var iconHtml = type === 'success' ? '&#10003;' : '&#33;';

        var html = '<div id="' + toastId + '" class="toast align-items-center text-white ' + bgClass + ' border-0" role="alert" aria-live="assertive" aria-atomic="true">' +
            '<div class="d-flex">' +
            '<div class="toast-body d-flex align-items-center gap-2">' +
            '<span>' + iconHtml + '</span>' +
            '<span>' + escHtml(message) + '</span>' +
            '</div>' +
            '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>' +
            '</div></div>';

        container.insertAdjacentHTML('beforeend', html);
        var toastEl = document.getElementById(toastId);
        if (toastEl && window.bootstrap && bootstrap.Toast) {
            var toast = new bootstrap.Toast(toastEl, { delay: 4000 });
            toast.show();
            toastEl.addEventListener('hidden.bs.toast', function () {
                toastEl.remove();
            });
        }
    }

    function showModal(id) {
        var el = document.getElementById(id);
        if (!el) return;
        if (window.bootstrap && bootstrap.Modal) {
            var m = bootstrap.Modal.getOrCreateInstance(el);
            m.show();
        }
    }

    function hideModal(id) {
        var el = document.getElementById(id);
        if (!el) return;
        if (window.bootstrap && bootstrap.Modal) {
            var m = bootstrap.Modal.getInstance(el);
            if (m) m.hide();
        }
    }

    function getVal(id) {
        var el = document.getElementById(id);
        return el ? el.value : '';
    }

    function setVal(id, value) {
        var el = document.getElementById(id);
        if (el) el.value = (value == null ? '' : value);
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
        return String(str).replace(/\\/g, '\\\\').replace(/'/g, "\\'");
    }

    function validateEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function formatDate(dateStr) {
        if (!dateStr) return '—';
        try {
            var d = new Date(dateStr);
            if (isNaN(d.getTime())) return dateStr;
            return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
        } catch (e) { return dateStr; }
    }

    function isoDate() {
        var d = new Date();
        return d.getFullYear() + '-' +
               String(d.getMonth() + 1).padStart(2, '0') + '-' +
               String(d.getDate()).padStart(2, '0');
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXPOSE GLOBALS (called from Razor view onclick attributes)
    // ═══════════════════════════════════════════════════════════════════════
    window.openAddQuestion   = openAddQuestion;
    window.openEditQuestion  = openEditQuestion;
    window.saveQuestion      = saveQuestion;
    window.deactivateQuestion = deactivateQuestion;
    window.questionsPagePrev = questionsPagePrev;
    window.questionsPageNext = questionsPageNext;
    window.filterQuestions   = filterQuestions;
    window.triggerCsvImport  = triggerCsvImport;
    window.handleCsvImport   = handleCsvImport;
    window.confirmCsvImport  = confirmCsvImport;
    window.exportQuestionsCsv = exportQuestionsCsv;

    window.toggleNode        = toggleNode;
    window.showNodePreview   = showNodePreview;

    window.openAddBadge      = openAddBadge;
    window.openEditBadge     = openEditBadge;
    window.saveBadge         = saveBadge;
    window.deleteBadge       = deleteBadge;

    window.openAddAdmin      = openAddAdmin;
    window.openEditAdmin     = openEditAdmin;
    window.handleAdminRoleChange = handleAdminRoleChange;
    window.saveAdmin         = saveAdmin;
    window.deactivateAdmin   = deactivateAdmin;
    window.confirmDeactivate = confirmDeactivate;
    window.filterAdmins      = filterAdmins;

    window.showToast         = window.showToast || showToast;

    // ═══════════════════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════════════════
    document.addEventListener('DOMContentLoaded', function () {
        initTabs();

        // Wire up search/filter inputs for questions
        var qSearch = document.getElementById('questionSearch');
        if (qSearch) qSearch.addEventListener('input', filterQuestions);

        var catFilter = document.getElementById('categoryFilter');
        if (catFilter) catFilter.addEventListener('change', filterQuestions);

        var typeFilter = document.getElementById('typeFilter');
        if (typeFilter) typeFilter.addEventListener('change', filterQuestions);

        // Wire up search/filter inputs for admins
        var aSearch = document.getElementById('adminSearch');
        if (aSearch) aSearch.addEventListener('input', filterAdmins);

        var roleFilter = document.getElementById('adminRoleFilter');
        if (roleFilter) roleFilter.addEventListener('change', filterAdmins);

        // Wire pagination buttons
        var prevBtn = document.getElementById('questionsPrevBtn');
        if (prevBtn) prevBtn.addEventListener('click', questionsPagePrev);

        var nextBtn = document.getElementById('questionsNextBtn');
        if (nextBtn) nextBtn.addEventListener('click', questionsPageNext);

        // Wire admin role change
        var adminRole = document.getElementById('adminRole');
        if (adminRole) adminRole.addEventListener('change', handleAdminRoleChange);

        // Default tab: questions
        switchTab('questions');
    });

}());
