/*!
 * AnalyticsScript.js
 * LiteRise Web Portal — Analytics Section
 * Handles: Demographics, Engagement, Performance, Assessment, Games tabs
 * Dependencies: Chart.js 4.4.1, Bootstrap 5
 */
(function () {
    'use strict';

    // ═══════════════════════════════════════════════════════════════════════
    // CONSTANTS & PALETTE
    // ═══════════════════════════════════════════════════════════════════════
    var PRIMARY_GREEN  = '#11B067';
    var PRIMARY_BLUE   = '#0d6efd';
    var ACCENT_CYAN    = '#63B3ED';
    var COLOR_ORANGE   = '#F6AD55';
    var COLOR_RED      = '#FC8181';
    var COLOR_PURPLE   = '#B794F4';
    var COLOR_TEAL     = '#4FD1C5';
    var COLOR_YELLOW   = '#ECC94B';
    var COLOR_PINK     = '#F687B3';

    var DARK_BG        = '#2D3748';
    var DARK_TEXT      = '#E2E8F0';
    var DARK_ACCENT    = '#63B3ED';
    var DARK_GRID      = 'rgba(226,232,240,0.12)';
    var LIGHT_GRID     = 'rgba(0,0,0,0.08)';

    var SESSION_TAB_KEY = 'analyticsActiveTab';

    var GAME_TYPES = [
        'SentenceScramble', 'StorySequencing', 'FillInTheBlanks',
        'PictureMatch',     'DialogueReading',  'WordHunt',
        'TimedTrail',       'MinimalPairs',     'SynonymSprint',
        'WordExplosion',    'PhonicsNinja'
    ];

    var CHART_COLORS_MULTI = [
        PRIMARY_BLUE, PRIMARY_GREEN, COLOR_ORANGE, COLOR_RED,
        COLOR_PURPLE, COLOR_TEAL,    COLOR_YELLOW, COLOR_PINK,
        ACCENT_CYAN,  '#667EEA',     '#F56565'
    ];

    // IRT thresholds
    var IRT_BEGINNER_MAX     = -0.5;
    var IRT_INTERMEDIATE_MAX =  0.5;

    // ═══════════════════════════════════════════════════════════════════════
    // STATE
    // ═══════════════════════════════════════════════════════════════════════
    var ANALYTICS_DATA       = window.STUDENTS_DATA || [];
    var _charts              = {};   // keyed by canvas id
    var _demoPage            = 1;
    var _demoPageSize        = 25;
    var _demoFilteredData    = [];
    var _demoExpandedRows    = {};
    var _isDarkMode          = false;

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════════════════════
    function isDark() {
        return document.body.classList.contains('dark-mode');
    }

    function gridColor() {
        return isDark() ? DARK_GRID : LIGHT_GRID;
    }

    function labelColor() {
        return isDark() ? DARK_TEXT : '#374151';
    }

    function destroyChart(id) {
        if (_charts[id]) {
            _charts[id].destroy();
            delete _charts[id];
        }
    }

    function safeNum(val) {
        var n = parseFloat(val);
        return isNaN(n) ? null : n;
    }

    function avg(arr) {
        if (!arr.length) return 0;
        return arr.reduce(function (s, v) { return s + v; }, 0) / arr.length;
    }

    function formatNumber(n) {
        if (n >= 1000000) return (n / 1000000).toFixed(1) + 'M';
        if (n >= 1000)    return (n / 1000).toFixed(1) + 'k';
        return String(n);
    }

    function uniqueValues(arr, key) {
        var seen = {};
        var out  = [];
        arr.forEach(function (item) {
            var v = item[key];
            if (v && !seen[v]) { seen[v] = true; out.push(v); }
        });
        return out.sort();
    }

    function countBy(arr, key) {
        var map = {};
        arr.forEach(function (item) {
            var v = item[key] || 'Unknown';
            map[v] = (map[v] || 0) + 1;
        });
        return map;
    }

    function groupBy(arr, key) {
        var map = {};
        arr.forEach(function (item) {
            var v = item[key] || 'Unknown';
            if (!map[v]) map[v] = [];
            map[v].push(item);
        });
        return map;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DEFAULT CHART OPTIONS (shared baseline)
    // ═══════════════════════════════════════════════════════════════════════
    function baseChartOptions(overrides) {
        var defaults = {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    labels: {
                        color: labelColor(),
                        font: { family: 'Visby, sans-serif', size: 12 }
                    }
                },
                tooltip: {
                    titleFont:  { family: 'Visby, sans-serif' },
                    bodyFont:   { family: 'Visby, sans-serif' },
                    backgroundColor: isDark() ? '#1A202C' : '#fff',
                    titleColor:      isDark() ? DARK_TEXT   : '#111',
                    bodyColor:       isDark() ? DARK_TEXT   : '#374151',
                    borderColor:     isDark() ? DARK_GRID   : 'rgba(0,0,0,0.1)',
                    borderWidth: 1
                }
            },
            scales: {
                x: {
                    ticks:  { color: labelColor(), font: { family: 'Visby, sans-serif', size: 11 } },
                    grid:   { color: gridColor() }
                },
                y: {
                    ticks:  { color: labelColor(), font: { family: 'Visby, sans-serif', size: 11 } },
                    grid:   { color: gridColor() }
                }
            }
        };
        return mergeDeep(defaults, overrides || {});
    }

    // Lightweight deep-merge (no library dependency)
    function mergeDeep(target, source) {
        var out = Object.assign({}, target);
        Object.keys(source).forEach(function (key) {
            if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
                out[key] = mergeDeep(target[key] || {}, source[key]);
            } else {
                out[key] = source[key];
            }
        });
        return out;
    }

    function doughnutOptions(overrides) {
        var base = {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        color: labelColor(),
                        font: { family: 'Visby, sans-serif', size: 12 },
                        padding: 12
                    }
                },
                tooltip: {
                    titleFont:  { family: 'Visby, sans-serif' },
                    bodyFont:   { family: 'Visby, sans-serif' },
                    backgroundColor: isDark() ? '#1A202C' : '#fff',
                    titleColor:      isDark() ? DARK_TEXT   : '#111',
                    bodyColor:       isDark() ? DARK_TEXT   : '#374151',
                    borderColor:     isDark() ? DARK_GRID   : 'rgba(0,0,0,0.1)',
                    borderWidth: 1
                }
            }
        };
        return mergeDeep(base, overrides || {});
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB SWITCHING
    // ═══════════════════════════════════════════════════════════════════════
    function initTabSwitching() {
        var buttons = document.querySelectorAll('.tab-header[data-tab]');
        buttons.forEach(function (btn) {
            btn.addEventListener('click', function () {
                var tabName = btn.getAttribute('data-tab');
                activateTab(tabName);
            });
        });

        // Restore from session storage
        var savedTab = sessionStorage.getItem(SESSION_TAB_KEY);
        var startTab = savedTab || 'demographics';
        activateTab(startTab);
    }

    function activateTab(tabName) {
        // Hide all panels, deactivate all buttons
        document.querySelectorAll('.tab-panel').forEach(function (p) {
            p.classList.remove('active');
        });
        document.querySelectorAll('.tab-header').forEach(function (b) {
            b.classList.remove('active');
        });

        // Show target panel and activate button
        var panel = document.getElementById('tab-' + tabName);
        var btn   = document.querySelector('.tab-header[data-tab="' + tabName + '"]');
        if (panel) panel.classList.add('active');
        if (btn)   btn.classList.add('active');

        sessionStorage.setItem(SESSION_TAB_KEY, tabName);

        // Lazy-init charts for the newly shown tab
        switch (tabName) {
            case 'demographics': initDemographicsTab(); break;
            case 'engagement':   initEngagementTab();   break;
            case 'performance':  initPerformanceTab();  break;
            case 'assessment':   initAssessmentTab();   break;
            case 'games':        initGamesTab();         break;
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DARK MODE
    // ═══════════════════════════════════════════════════════════════════════
    function initDarkModeToggle() {
        var btn = document.getElementById('toggleDarkMode');
        if (!btn) return;

        btn.addEventListener('click', function () {
            document.body.classList.toggle('dark-mode');
            _isDarkMode = document.body.classList.contains('dark-mode');
            rebuildAllCharts();
        });

        // Watch for external dark-mode toggle (e.g. layout-level button)
        var observer = new MutationObserver(function (mutations) {
            mutations.forEach(function (m) {
                if (m.attributeName === 'class') {
                    var nowDark = document.body.classList.contains('dark-mode');
                    if (nowDark !== _isDarkMode) {
                        _isDarkMode = nowDark;
                        rebuildAllCharts();
                    }
                }
            });
        });
        observer.observe(document.body, { attributes: true });
    }

    function rebuildAllCharts() {
        // Destroy all existing charts then re-init the currently visible tab
        Object.keys(_charts).forEach(function (id) {
            destroyChart(id);
        });
        var activePanel = document.querySelector('.tab-panel.active');
        if (activePanel) {
            var tabName = activePanel.id.replace('tab-', '');
            switch (tabName) {
                case 'demographics': buildDemoCharts(_demoFilteredData.length ? _demoFilteredData : ANALYTICS_DATA); break;
                case 'engagement':   buildEngagementCharts(ANALYTICS_DATA); break;
                case 'performance':  buildPerformanceCharts(ANALYTICS_DATA); break;
                case 'assessment':   buildAssessmentCharts(ANALYTICS_DATA); break;
                case 'games':        buildGamesCharts(getSampleGameData());  break;
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── TAB 1: DEMOGRAPHICS
    // ═══════════════════════════════════════════════════════════════════════
    var _demoInitialized = false;

    function initDemographicsTab() {
        if (_demoInitialized) return;
        _demoInitialized = true;

        populateDemoFilters();
        _demoFilteredData = ANALYTICS_DATA.slice();
        buildDemoStats(_demoFilteredData);
        buildDemoCharts(_demoFilteredData);
        buildDemoTable(_demoFilteredData);

        document.getElementById('applyDemoFilters').addEventListener('click', applyDemoFilters);
        document.getElementById('clearDemoFilters').addEventListener('click',  clearDemoFilters);

        var searchInput = document.getElementById('demoTableSearch');
        if (searchInput) {
            searchInput.addEventListener('input', function () {
                var q = searchInput.value.trim().toLowerCase();
                var filtered = _demoFilteredData.filter(function (s) {
                    return (s.name || '').toLowerCase().indexOf(q) !== -1 ||
                           (s.school_name || '').toLowerCase().indexOf(q) !== -1;
                });
                buildDemoTable(filtered);
            });
        }
    }

    function populateDemoFilters() {
        var schoolSel   = document.getElementById('schoolFilter');
        var barangaySel = document.getElementById('barangayFilter');

        var schools   = uniqueValues(ANALYTICS_DATA, 'school_name');
        var barangays = uniqueValues(ANALYTICS_DATA, 'barangay');

        schools.forEach(function (s) {
            var opt = document.createElement('option');
            opt.value = s; opt.textContent = s;
            schoolSel.appendChild(opt);
        });

        barangays.forEach(function (b) {
            var opt = document.createElement('option');
            opt.value = b; opt.textContent = b;
            barangaySel.appendChild(opt);
        });
    }

    function applyDemoFilters() {
        var school   = document.getElementById('schoolFilter').value;
        var barangay = document.getElementById('barangayFilter').value;

        _demoFilteredData = ANALYTICS_DATA.filter(function (s) {
            var okSchool   = !school   || s.school_name === school;
            var okBarangay = !barangay || s.barangay    === barangay;
            return okSchool && okBarangay;
        });

        _demoPage = 1;
        buildDemoStats(_demoFilteredData);
        destroyChart('gradeDistributionChart');
        destroyChart('genderDistributionChart');
        buildDemoCharts(_demoFilteredData);
        buildDemoTable(_demoFilteredData);
    }

    function clearDemoFilters() {
        document.getElementById('schoolFilter').value   = '';
        document.getElementById('barangayFilter').value = '';
        _demoFilteredData = ANALYTICS_DATA.slice();
        _demoPage = 1;
        buildDemoStats(_demoFilteredData);
        destroyChart('gradeDistributionChart');
        destroyChart('genderDistributionChart');
        buildDemoCharts(_demoFilteredData);
        buildDemoTable(_demoFilteredData);
    }

    function buildDemoStats(data) {
        var schools  = uniqueValues(data, 'school_name');
        var active   = data.filter(function (s) { return s.status === 'active';   }).length;
        var inactive = data.filter(function (s) { return s.status === 'inactive'; }).length;

        setText('statTotalStudents',  data.length);
        setText('statTotalSchools',   schools.length);
        setText('statActiveStudents', active);
        setText('statInactiveStudents', inactive);
    }

    function buildDemoCharts(data) {
        // Grade distribution bar chart
        var gradeCounts = countBy(data, 'grade');
        var gradeLabels = Object.keys(gradeCounts).sort(function (a, b) {
            return (parseInt(a) || 0) - (parseInt(b) || 0);
        });
        var gradeValues = gradeLabels.map(function (g) { return gradeCounts[g]; });

        var gradeCtx = document.getElementById('gradeDistributionChart');
        if (gradeCtx) {
            destroyChart('gradeDistributionChart');
            _charts['gradeDistributionChart'] = new Chart(gradeCtx, {
                type: 'bar',
                data: {
                    labels: gradeLabels,
                    datasets: [{
                        label: 'Students',
                        data: gradeValues,
                        backgroundColor: PRIMARY_BLUE,
                        borderRadius: 4
                    }]
                },
                options: baseChartOptions({
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { title: { display: true, text: 'Grade', color: labelColor() } },
                        y: { title: { display: true, text: 'Count', color: labelColor() }, beginAtZero: true }
                    }
                })
            });
        }

        // Gender doughnut
        var genderCounts = countBy(data, 'gender');
        var genderCtx    = document.getElementById('genderDistributionChart');
        if (genderCtx) {
            destroyChart('genderDistributionChart');
            _charts['genderDistributionChart'] = new Chart(genderCtx, {
                type: 'doughnut',
                data: {
                    labels: Object.keys(genderCounts),
                    datasets: [{
                        data: Object.values(genderCounts),
                        backgroundColor: [PRIMARY_BLUE, PRIMARY_GREEN, COLOR_ORANGE, COLOR_PURPLE],
                        borderWidth: 2,
                        borderColor: isDark() ? DARK_BG : '#fff'
                    }]
                },
                options: doughnutOptions()
            });
        }
    }

    function buildDemoTable(data) {
        var tbody     = document.getElementById('demoTableBody');
        var pagDiv    = document.getElementById('demoPagination');
        if (!tbody) return;

        var totalPages = Math.max(1, Math.ceil(data.length / _demoPageSize));
        if (_demoPage > totalPages) _demoPage = totalPages;

        var start  = (_demoPage - 1) * _demoPageSize;
        var slice  = data.slice(start, start + _demoPageSize);

        var rows = slice.map(function (s) {
            var sid    = s.student_id || s.id || Math.random();
            var status = s.status === 'active'
                ? '<span class="badge badge-active">Active</span>'
                : '<span class="badge badge-inactive">Inactive</span>';

            return '<tr class="demo-row" data-sid="' + sid + '">' +
                '<td><button class="expand-btn" data-sid="' + sid + '">▶</button></td>' +
                '<td>' + escapeHtml(s.name) + '</td>' +
                '<td>' + escapeHtml(s.school_name) + '</td>' +
                '<td>' + escapeHtml(s.grade) + '</td>' +
                '<td>' + escapeHtml(s.gender) + '</td>' +
                '<td>' + status + '</td>' +
                '<td>' + formatNumber(s.total_xp || 0) + '</td>' +
            '</tr>' +
            '<tr class="expand-row" id="expand-' + sid + '" style="display:none;">' +
                '<td colspan="7">' +
                    '<div class="expand-detail">' +
                        '<span><strong>Streak:</strong> ' + (s.streak_days || 0) + ' days</span>' +
                        '<span><strong>Longest Streak:</strong> ' + (s.longest_streak || 0) + ' days</span>' +
                        '<span><strong>Last Active:</strong> ' + escapeHtml(s.last_active || '—') + '</span>' +
                        '<span><strong>Lessons Done:</strong> ' + (s.lessons_done || 0) + '</span>' +
                        '<span><strong>Pre θ:</strong> ' + (s.pre_theta != null ? Number(s.pre_theta).toFixed(3) : '—') + '</span>' +
                        '<span><strong>Post θ:</strong> ' + (s.post_theta != null ? Number(s.post_theta).toFixed(3) : '—') + '</span>' +
                        '<span><strong>Level:</strong> ' + escapeHtml(s.placement_level || '—') + '</span>' +
                    '</div>' +
                '</td>' +
            '</tr>';
        }).join('');

        tbody.innerHTML = rows;

        // Expand/collapse
        tbody.querySelectorAll('.expand-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var sid      = btn.getAttribute('data-sid');
                var expRow   = document.getElementById('expand-' + sid);
                var isOpen   = _demoExpandedRows[sid];
                if (expRow) {
                    expRow.style.display = isOpen ? 'none' : 'table-row';
                    btn.textContent       = isOpen ? '▶' : '▼';
                    _demoExpandedRows[sid] = !isOpen;
                }
            });
        });

        // Pagination
        if (pagDiv) {
            var paginHtml = '';
            if (totalPages > 1) {
                paginHtml += '<button class="pag-btn" ' + (_demoPage <= 1 ? 'disabled' : '') + ' id="pagPrev">‹ Prev</button>';
                paginHtml += '<span class="pag-info">Page ' + _demoPage + ' of ' + totalPages + ' (' + data.length + ' students)</span>';
                paginHtml += '<button class="pag-btn" ' + (_demoPage >= totalPages ? 'disabled' : '') + ' id="pagNext">Next ›</button>';
            } else {
                paginHtml = '<span class="pag-info">' + data.length + ' student(s)</span>';
            }
            pagDiv.innerHTML = paginHtml;

            var prevBtn = document.getElementById('pagPrev');
            var nextBtn = document.getElementById('pagNext');
            if (prevBtn) prevBtn.addEventListener('click', function () { _demoPage--; buildDemoTable(data); });
            if (nextBtn) nextBtn.addEventListener('click', function () { _demoPage++; buildDemoTable(data); });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── TAB 2: ENGAGEMENT
    // ═══════════════════════════════════════════════════════════════════════
    var _engagementInitialized = false;

    function initEngagementTab() {
        if (_engagementInitialized) return;
        _engagementInitialized = true;

        var data = ANALYTICS_DATA;

        // Stats
        var streaks     = data.map(function (s) { return s.streak_days || 0; });
        var longestList = data.map(function (s) { return s.longest_streak || 0; });
        var avgStreak   = streaks.length ? (avg(streaks)).toFixed(1) : '—';
        var maxStreak   = longestList.length ? Math.max.apply(null, longestList) : '—';
        var activeCount = data.filter(function (s) { return s.status === 'active'; }).length;
        var activeRate  = data.length ? Math.round(activeCount / data.length * 100) + '%' : '—';
        var xpList      = data.map(function (s) { return s.total_xp || 0; });
        var avgXp       = xpList.length ? Math.round(avg(xpList)) : 0;

        setText('statAvgStreak',  avgStreak);
        setText('statMaxStreak',  maxStreak);
        setText('statActiveRate', activeRate);
        setText('statAvgXp',      formatNumber(avgXp));

        buildEngagementCharts(data);
    }

    function buildEngagementCharts(data) {
        // Streak histogram (0-5, 6-10, 11-20, 21-30, 31-60, 61-90, 90+)
        var streakBuckets = [0, 0, 0, 0, 0, 0, 0];
        var streakLabels  = ['0-5d', '6-10d', '11-20d', '21-30d', '31-60d', '61-90d', '90+d'];
        data.forEach(function (s) {
            var d = s.streak_days || 0;
            if      (d <= 5)  streakBuckets[0]++;
            else if (d <= 10) streakBuckets[1]++;
            else if (d <= 20) streakBuckets[2]++;
            else if (d <= 30) streakBuckets[3]++;
            else if (d <= 60) streakBuckets[4]++;
            else if (d <= 90) streakBuckets[5]++;
            else              streakBuckets[6]++;
        });

        var streakCtx = document.getElementById('streakHistogramChart');
        if (streakCtx) {
            destroyChart('streakHistogramChart');
            _charts['streakHistogramChart'] = new Chart(streakCtx, {
                type: 'bar',
                data: {
                    labels: streakLabels,
                    datasets: [{
                        label: 'Students',
                        data: streakBuckets,
                        backgroundColor: PRIMARY_GREEN,
                        borderRadius: 4
                    }]
                },
                options: baseChartOptions({
                    plugins: { legend: { display: false } },
                    scales: {
                        y: { beginAtZero: true, title: { display: true, text: 'Count', color: labelColor() } },
                        x: { title: { display: true, text: 'Streak Range', color: labelColor() } }
                    }
                })
            });
        }

        // Active vs Inactive doughnut
        var activeCount   = data.filter(function (s) { return s.status === 'active';   }).length;
        var inactiveCount = data.filter(function (s) { return s.status === 'inactive'; }).length;
        var aiCtx         = document.getElementById('activeInactiveChart');
        if (aiCtx) {
            destroyChart('activeInactiveChart');
            _charts['activeInactiveChart'] = new Chart(aiCtx, {
                type: 'doughnut',
                data: {
                    labels: ['Active', 'Inactive'],
                    datasets: [{
                        data: [activeCount, inactiveCount],
                        backgroundColor: [PRIMARY_GREEN, COLOR_RED],
                        borderWidth: 2,
                        borderColor: isDark() ? DARK_BG : '#fff'
                    }]
                },
                options: doughnutOptions()
            });
        }

        // Login frequency line chart — group last_active dates by day
        var dayCounts = {};
        data.forEach(function (s) {
            if (!s.last_active) return;
            var dateStr = s.last_active.substring(0, 10);
            dayCounts[dateStr] = (dayCounts[dateStr] || 0) + 1;
        });
        var sortedDates = Object.keys(dayCounts).sort();
        var lastN       = sortedDates.slice(-30);   // last 30 distinct dates

        var loginCtx = document.getElementById('loginFrequencyChart');
        if (loginCtx) {
            destroyChart('loginFrequencyChart');
            _charts['loginFrequencyChart'] = new Chart(loginCtx, {
                type: 'line',
                data: {
                    labels: lastN,
                    datasets: [{
                        label: 'Students Active',
                        data: lastN.map(function (d) { return dayCounts[d]; }),
                        borderColor:     PRIMARY_BLUE,
                        backgroundColor: 'rgba(13,110,253,0.15)',
                        fill:            true,
                        tension:         0.4,
                        pointRadius:     3
                    }]
                },
                options: baseChartOptions({
                    scales: {
                        y: { beginAtZero: true },
                        x: { ticks: { maxRotation: 45 } }
                    }
                })
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── TAB 3: PERFORMANCE
    // ═══════════════════════════════════════════════════════════════════════
    var _perfInitialized = false;

    function initPerformanceTab() {
        if (_perfInitialized) return;
        _perfInitialized = true;

        var data = ANALYTICS_DATA;

        var totalXp    = data.reduce(function (s, st) { return s + (st.total_xp || 0); }, 0);
        var xpList     = data.map(function (s) { return s.total_xp || 0; });
        var lessonList = data.map(function (s) { return s.lessons_done || 0; });
        var avgLessons = lessonList.length ? avg(lessonList).toFixed(1) : '—';
        var topStudent = data.reduce(function (best, s) {
            return (!best || (s.total_xp || 0) > (best.total_xp || 0)) ? s : best;
        }, null);

        setText('statTotalXp',        formatNumber(totalXp));
        setText('statAvgLessons',     avgLessons);
        setText('statTopXpStudent',   topStudent ? escapeHtml(topStudent.name) : '—');
        setText('statAvgLessonsDone', avgLessons);

        buildPerformanceCharts(data);
    }

    function buildPerformanceCharts(data) {
        // Ability growth area chart — sorted by pre_theta, overlay post_theta
        var withPre = data.filter(function (s) { return s.pre_theta != null; });
        withPre.sort(function (a, b) { return (a.pre_theta || 0) - (b.pre_theta || 0); });

        // Subsample to max 80 points for readability
        var step    = Math.max(1, Math.floor(withPre.length / 80));
        var samples = withPre.filter(function (_, i) { return i % step === 0; });
        var labels  = samples.map(function (s) { return escapeHtml(s.name || ('S' + s.student_id)); });
        var preVals = samples.map(function (s) { return safeNum(s.pre_theta); });
        var postVals= samples.map(function (s) { return s.post_theta != null ? safeNum(s.post_theta) : null; });

        var agCtx = document.getElementById('abilityGrowthChart');
        if (agCtx) {
            destroyChart('abilityGrowthChart');
            _charts['abilityGrowthChart'] = new Chart(agCtx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: 'Pre θ',
                            data: preVals,
                            borderColor:     PRIMARY_BLUE,
                            backgroundColor: 'rgba(13,110,253,0.1)',
                            fill:            true,
                            tension:         0.3,
                            pointRadius:     0
                        },
                        {
                            label: 'Post θ',
                            data: postVals,
                            borderColor:     PRIMARY_GREEN,
                            backgroundColor: 'rgba(17,176,103,0.1)',
                            fill:            true,
                            tension:         0.3,
                            pointRadius:     0
                        }
                    ]
                },
                options: baseChartOptions({
                    scales: {
                        x: { ticks: { display: false }, title: { display: true, text: 'Students (sorted by Pre θ)', color: labelColor() } },
                        y: { title: { display: true, text: 'θ Score', color: labelColor() } }
                    }
                })
            });
        }

        // XP distribution histogram
        var xpList   = data.map(function (s) { return s.total_xp || 0; });
        var maxXp    = Math.max.apply(null, xpList) || 1;
        var buckets  = 10;
        var bucketW  = Math.ceil(maxXp / buckets);
        var xpCounts = new Array(buckets).fill(0);
        var xpLabels = [];
        for (var i = 0; i < buckets; i++) {
            xpLabels.push((i * bucketW) + '–' + ((i + 1) * bucketW));
        }
        xpList.forEach(function (xp) {
            var idx = Math.min(Math.floor(xp / bucketW), buckets - 1);
            xpCounts[idx]++;
        });

        var xpCtx = document.getElementById('xpDistributionChart');
        if (xpCtx) {
            destroyChart('xpDistributionChart');
            _charts['xpDistributionChart'] = new Chart(xpCtx, {
                type: 'bar',
                data: {
                    labels: xpLabels,
                    datasets: [{
                        label: 'Students',
                        data: xpCounts,
                        backgroundColor: COLOR_ORANGE,
                        borderRadius: 4
                    }]
                },
                options: baseChartOptions({
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { ticks: { maxRotation: 45 }, title: { display: true, text: 'XP Range', color: labelColor() } },
                        y: { beginAtZero: true, title: { display: true, text: 'Count', color: labelColor() } }
                    }
                })
            });
        }

        // Lessons distribution histogram
        var lessonList = data.map(function (s) { return s.lessons_done || 0; });
        var maxL       = Math.max.apply(null, lessonList) || 1;
        var lBuckets   = 8;
        var lW         = Math.ceil(maxL / lBuckets);
        var lCounts    = new Array(lBuckets).fill(0);
        var lLabels    = [];
        for (var j = 0; j < lBuckets; j++) {
            lLabels.push((j * lW) + '–' + ((j + 1) * lW));
        }
        lessonList.forEach(function (l) {
            var idx = Math.min(Math.floor(l / lW), lBuckets - 1);
            lCounts[idx]++;
        });

        var lCtx = document.getElementById('lessonsDistributionChart');
        if (lCtx) {
            destroyChart('lessonsDistributionChart');
            _charts['lessonsDistributionChart'] = new Chart(lCtx, {
                type: 'bar',
                data: {
                    labels: lLabels,
                    datasets: [{
                        label: 'Students',
                        data: lCounts,
                        backgroundColor: COLOR_PURPLE,
                        borderRadius: 4
                    }]
                },
                options: baseChartOptions({
                    plugins: { legend: { display: false } },
                    scales: {
                        x: { ticks: { maxRotation: 45 }, title: { display: true, text: 'Lessons Range', color: labelColor() } },
                        y: { beginAtZero: true, title: { display: true, text: 'Count', color: labelColor() } }
                    }
                })
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── TAB 4: ASSESSMENT
    // ═══════════════════════════════════════════════════════════════════════
    var _assessmentInitialized = false;

    function initAssessmentTab() {
        if (_assessmentInitialized) return;
        _assessmentInitialized = true;

        var data = ANALYTICS_DATA;

        // Compute stats from client-side data
        var preStudents  = data.filter(function (s) { return s.pre_theta  != null; });
        var postStudents = data.filter(function (s) { return s.post_theta != null; });
        var bothStudents = data.filter(function (s) { return s.pre_theta  != null && s.post_theta != null; });

        var preCount  = preStudents.length;
        var postCount = postStudents.length;

        var avgGrowth = 0;
        if (bothStudents.length) {
            avgGrowth = avg(bothStudents.map(function (s) {
                return (parseFloat(s.post_theta) || 0) - (parseFloat(s.pre_theta) || 0);
            }));
        }

        var levelCounts = {
            beginner:     data.filter(function (s) { return s.placement_level === 'beginner';     }).length,
            intermediate: data.filter(function (s) { return s.placement_level === 'intermediate'; }).length,
            advanced:     data.filter(function (s) { return s.placement_level === 'advanced';     }).length,
            notTaken:     data.filter(function (s) { return s.placement_level == null;            }).length
        };

        // Update stat cards (in case server values differ or page was loaded partially)
        setText('statPreAssessmentTaken',  preCount);
        setText('statPostAssessmentTaken', postCount);
        setText('statPrePostGrowth',       (avgGrowth >= 0 ? '+' : '') + avgGrowth.toFixed(3));

        var levelSummary = levelCounts.beginner + ' / ' + levelCounts.intermediate + ' / ' + levelCounts.advanced;
        setText('statLevelDistSummary', levelSummary);

        buildAssessmentCharts(data);
    }

    function buildAssessmentCharts(data) {
        buildThetaHistogram(data);
        buildLevelDistribution(data);
        buildPrePostGrowthChart(data);
        buildCategoryRadar(data);
    }

    function buildThetaHistogram(data) {
        // Buckets: [-3,-2), [-2,-1), [-1,-0.5), [-0.5,0), [0,0.5), [0.5,1), [1,2), [2,3]
        var bucketEdges  = [-3, -2, -1, -0.5, 0, 0.5, 1, 2, 3];
        var bucketLabels = ['-3 to -2', '-2 to -1', '-1 to -0.5', '-0.5 to 0',
                            '0 to 0.5',  '0.5 to 1',  '1 to 2',   '2 to 3'];
        var bucketCounts = new Array(8).fill(0);

        data.forEach(function (s) {
            if (s.pre_theta == null) return;
            var theta = parseFloat(s.pre_theta);
            for (var i = 0; i < 8; i++) {
                var lo = bucketEdges[i];
                var hi = bucketEdges[i + 1];
                if (i < 7) {
                    if (theta >= lo && theta < hi) { bucketCounts[i]++; break; }
                } else {
                    if (theta >= lo && theta <= hi) { bucketCounts[i]++; break; }
                }
            }
        });

        // Color buckets: red for beginner range, yellow for intermediate, green for advanced
        var bgColors = bucketLabels.map(function (lbl, i) {
            var midBucketEdge = (bucketEdges[i] + bucketEdges[i + 1]) / 2;
            if      (midBucketEdge < IRT_BEGINNER_MAX)     return COLOR_RED;
            else if (midBucketEdge < IRT_INTERMEDIATE_MAX) return COLOR_YELLOW;
            else                                            return PRIMARY_GREEN;
        });

        var ctx = document.getElementById('thetaDistributionChart');
        if (!ctx) return;
        destroyChart('thetaDistributionChart');

        _charts['thetaDistributionChart'] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: bucketLabels,
                datasets: [{
                    label: 'Students',
                    data: bucketCounts,
                    backgroundColor: bgColors,
                    borderRadius: 4
                }]
            },
            options: baseChartOptions({
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            afterLabel: function (context) {
                                var mid = (bucketEdges[context.dataIndex] + bucketEdges[context.dataIndex + 1]) / 2;
                                var level = mid < IRT_BEGINNER_MAX ? 'Beginner'
                                          : mid < IRT_INTERMEDIATE_MAX ? 'Intermediate' : 'Advanced';
                                return 'Level: ' + level;
                            }
                        }
                    }
                },
                scales: {
                    x: { title: { display: true, text: 'θ Score Range', color: labelColor() } },
                    y: { beginAtZero: true, title: { display: true, text: 'Student Count', color: labelColor() } }
                }
            })
        });
    }

    function buildLevelDistribution(data) {
        var levelCounts = {
            Beginner:     data.filter(function (s) { return s.placement_level === 'beginner';     }).length,
            Intermediate: data.filter(function (s) { return s.placement_level === 'intermediate'; }).length,
            Advanced:     data.filter(function (s) { return s.placement_level === 'advanced';     }).length,
            'Not Taken':  data.filter(function (s) { return s.placement_level == null;            }).length
        };

        var ctx = document.getElementById('levelDistributionChart');
        if (!ctx) return;
        destroyChart('levelDistributionChart');

        _charts['levelDistributionChart'] = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: Object.keys(levelCounts),
                datasets: [{
                    data: Object.values(levelCounts),
                    backgroundColor: [COLOR_RED, COLOR_YELLOW, PRIMARY_GREEN, '#CBD5E0'],
                    borderWidth: 2,
                    borderColor: isDark() ? DARK_BG : '#fff'
                }]
            },
            options: doughnutOptions({
                plugins: {
                    legend: { position: 'right' },
                    tooltip: {
                        callbacks: {
                            label: function (context) {
                                var total = context.dataset.data.reduce(function (a, b) { return a + b; }, 0);
                                var pct   = total ? Math.round(context.parsed / total * 100) : 0;
                                return context.label + ': ' + context.parsed + ' (' + pct + '%)';
                            }
                        }
                    }
                }
            })
        });
    }

    function buildPrePostGrowthChart(data) {
        // Group by school_name; compute avg pre and post theta per school
        var schoolGroups = groupBy(data, 'school_name');
        var schoolNames  = Object.keys(schoolGroups).sort();

        var avgPre  = [];
        var avgPost = [];
        var validSchools = [];

        schoolNames.forEach(function (school) {
            var group    = schoolGroups[school];
            var preList  = group.filter(function (s) { return s.pre_theta  != null; }).map(function (s) { return parseFloat(s.pre_theta);  });
            var postList = group.filter(function (s) { return s.post_theta != null; }).map(function (s) { return parseFloat(s.post_theta); });

            if (preList.length === 0 && postList.length === 0) return;

            validSchools.push(school);
            avgPre.push(preList.length  ? parseFloat(avg(preList).toFixed(3))  : null);
            avgPost.push(postList.length ? parseFloat(avg(postList).toFixed(3)) : null);
        });

        var ctx = document.getElementById('prePostGrowthChart');
        if (!ctx) return;
        destroyChart('prePostGrowthChart');

        _charts['prePostGrowthChart'] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: validSchools,
                datasets: [
                    {
                        label: 'Avg Pre θ',
                        data: avgPre,
                        backgroundColor: 'rgba(13,110,253,0.75)',
                        borderColor:     PRIMARY_BLUE,
                        borderWidth: 1,
                        borderRadius: 3
                    },
                    {
                        label: 'Avg Post θ',
                        data: avgPost,
                        backgroundColor: 'rgba(17,176,103,0.75)',
                        borderColor:     PRIMARY_GREEN,
                        borderWidth: 1,
                        borderRadius: 3
                    }
                ]
            },
            options: baseChartOptions({
                scales: {
                    x: { ticks: { maxRotation: 45 }, title: { display: true, text: 'School', color: labelColor() } },
                    y: { title: { display: true, text: 'Avg θ Score', color: labelColor() } }
                }
            })
        });
    }

    function buildCategoryRadar(data) {
        // Placeholder radar — actual data requires get_portal_placement_progress.php per student
        var ctx = document.getElementById('categoryRadarChart');
        if (!ctx) return;
        destroyChart('categoryRadarChart');

        // Show placeholder with sample category shape to illustrate expected data format
        var categories   = ['Vocabulary', 'Grammar', 'Reading', 'Listening', 'Phonics', 'Fluency'];
        var sampleAvgPre = [55, 48, 62, 50, 45, 52];
        var sampleAvgPost= [68, 61, 74, 63, 59, 67];

        _charts['categoryRadarChart'] = new Chart(ctx, {
            type: 'radar',
            data: {
                labels: categories,
                datasets: [
                    {
                        label: 'Pre (sample)',
                        data: sampleAvgPre,
                        borderColor:     'rgba(13,110,253,0.8)',
                        backgroundColor: 'rgba(13,110,253,0.15)',
                        pointBackgroundColor: PRIMARY_BLUE,
                        borderDash: [5, 3]
                    },
                    {
                        label: 'Post (sample)',
                        data: sampleAvgPost,
                        borderColor:     'rgba(17,176,103,0.8)',
                        backgroundColor: 'rgba(17,176,103,0.15)',
                        pointBackgroundColor: PRIMARY_GREEN
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: true,
                plugins: {
                    legend: {
                        labels: {
                            color: labelColor(),
                            font: { family: 'Visby, sans-serif', size: 12 }
                        }
                    },
                    tooltip: {
                        backgroundColor: isDark() ? '#1A202C' : '#fff',
                        titleColor:      isDark() ? DARK_TEXT   : '#111',
                        bodyColor:       isDark() ? DARK_TEXT   : '#374151',
                        borderColor:     isDark() ? DARK_GRID   : 'rgba(0,0,0,0.1)',
                        borderWidth: 1,
                        titleFont:  { family: 'Visby, sans-serif' },
                        bodyFont:   { family: 'Visby, sans-serif' }
                    }
                },
                scales: {
                    r: {
                        ticks: {
                            color:          labelColor(),
                            backdropColor:  'transparent',
                            font:           { family: 'Visby, sans-serif', size: 10 }
                        },
                        grid:        { color: gridColor() },
                        angleLines:  { color: gridColor() },
                        pointLabels: {
                            color: labelColor(),
                            font: { family: 'Visby, sans-serif', size: 12 }
                        },
                        suggestedMin: 0,
                        suggestedMax: 100
                    }
                }
            }
        });

        // Show the placeholder note
        var note = document.getElementById('radarPlaceholderNote');
        if (note) note.style.display = 'flex';
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ── TAB 5: GAMES
    // ═══════════════════════════════════════════════════════════════════════
    var _gamesInitialized = false;

    function initGamesTab() {
        if (_gamesInitialized) return;
        _gamesInitialized = true;

        // TODO: Replace getSampleGameData() with a fetch() call to get_game_summary.php
        //       when the endpoint is available. Expected response shape:
        //       { gameTypes: string[], playCountByGame: {game,count}[],
        //         avgAccuracyByGame: {game,accuracy}[], xpOverTime: {labels, xpTotals} }
        var gameData = getSampleGameData();

        // Stats
        var totalPlays  = gameData.samplePlayCounts.reduce(function (a, b) { return a + b; }, 0);
        var avgAcc      = avg(gameData.sampleAccuracy);
        var topIdx      = gameData.samplePlayCounts.indexOf(Math.max.apply(null, gameData.samplePlayCounts));
        var totalXp     = gameData.sampleXpPerWeek.reduce(function (a, b) { return a + b; }, 0);

        setText('statTotalPlays',    formatNumber(totalPlays));
        setText('statAvgAccuracy',   avgAcc.toFixed(1) + '%');
        setText('statTopGame',       GAME_TYPES[topIdx] || '—');
        setText('statTotalGameXp',   formatNumber(totalXp));

        buildGamesCharts(gameData);
    }

    function getSampleGameData() {
        return {
            gameTypes:        GAME_TYPES,
            samplePlayCounts: [420, 310, 580, 390, 270, 490, 360, 225, 310, 460, 340],
            sampleAccuracy:   [72.4, 68.1, 75.8, 81.2, 65.3, 70.5, 77.9, 63.7, 74.2, 79.1, 66.8],
            weekLabels:       ['Week 1','Week 2','Week 3','Week 4','Week 5','Week 6','Week 7','Week 8'],
            sampleXpPerWeek:  [1200, 1850, 2100, 1750, 2400, 2650, 2300, 2800]
        };
    }

    function buildGamesCharts(gameData) {
        buildGamePlayCountChart(gameData);
        buildGameAccuracyChart(gameData);
        buildGameXpTimeChart(gameData);
    }

    function buildGamePlayCountChart(gameData) {
        var ctx = document.getElementById('gamePlayCountChart');
        if (!ctx) return;
        destroyChart('gamePlayCountChart');

        _charts['gamePlayCountChart'] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: gameData.gameTypes,
                datasets: [{
                    label: 'Play Count',
                    data: gameData.samplePlayCounts,
                    backgroundColor: CHART_COLORS_MULTI,
                    borderRadius: 4
                }]
            },
            options: baseChartOptions({
                indexAxis: 'y',   // horizontal bar — Chart.js 4.x API
                plugins: {
                    legend: { display: false }
                },
                scales: {
                    x: {
                        beginAtZero: true,
                        title: { display: true, text: 'Play Count', color: labelColor() }
                    },
                    y: {
                        ticks: { font: { family: 'Visby, sans-serif', size: 11 } }
                    }
                }
            })
        });
    }

    function buildGameAccuracyChart(gameData) {
        var ctx = document.getElementById('gameAccuracyChart');
        if (!ctx) return;
        destroyChart('gameAccuracyChart');

        _charts['gameAccuracyChart'] = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: gameData.gameTypes,
                datasets: [{
                    label: 'Avg Accuracy (%)',
                    data: gameData.sampleAccuracy,
                    backgroundColor: gameData.sampleAccuracy.map(function (a) {
                        if (a >= 75) return PRIMARY_GREEN;
                        if (a >= 60) return COLOR_YELLOW;
                        return COLOR_RED;
                    }),
                    borderRadius: 4
                }]
            },
            options: baseChartOptions({
                plugins: { legend: { display: false } },
                scales: {
                    x: { ticks: { maxRotation: 45 } },
                    y: {
                        beginAtZero: true,
                        max: 100,
                        title: { display: true, text: 'Accuracy (%)', color: labelColor() }
                    }
                }
            })
        });
    }

    function buildGameXpTimeChart(gameData) {
        var ctx = document.getElementById('gameXpTimeChart');
        if (!ctx) return;
        destroyChart('gameXpTimeChart');

        _charts['gameXpTimeChart'] = new Chart(ctx, {
            type: 'line',
            data: {
                labels: gameData.weekLabels,
                datasets: [{
                    label: 'XP Earned',
                    data: gameData.sampleXpPerWeek,
                    borderColor:     PRIMARY_GREEN,
                    backgroundColor: 'rgba(17,176,103,0.15)',
                    fill:            true,
                    tension:         0.4,
                    pointRadius:     5,
                    pointHoverRadius: 7,
                    pointBackgroundColor: PRIMARY_GREEN
                }]
            },
            options: baseChartOptions({
                scales: {
                    y: {
                        beginAtZero: true,
                        title: { display: true, text: 'XP', color: labelColor() }
                    },
                    x: {
                        title: { display: true, text: 'Week', color: labelColor() }
                    }
                }
            })
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CSV EXPORT
    // ═══════════════════════════════════════════════════════════════════════
    function initCsvExport() {
        var btn = document.getElementById('exportCsvBtn');
        if (!btn) return;

        btn.addEventListener('click', function () {
            var headers = [
                'student_id', 'name', 'grade', 'gender', 'school_name',
                'total_xp', 'streak_days', 'longest_streak', 'last_active',
                'status', 'pre_theta', 'post_theta', 'placement_level', 'lessons_done'
            ];

            var rows = [headers.join(',')];
            ANALYTICS_DATA.forEach(function (s) {
                var row = headers.map(function (h) {
                    var val = s[h];
                    if (val == null) return '';
                    var str = String(val);
                    // Escape commas and quotes
                    if (str.indexOf(',') !== -1 || str.indexOf('"') !== -1 || str.indexOf('\n') !== -1) {
                        str = '"' + str.replace(/"/g, '""') + '"';
                    }
                    return str;
                });
                rows.push(row.join(','));
            });

            var csvContent = rows.join('\n');
            var blob       = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
            var url        = URL.createObjectURL(blob);
            var link       = document.createElement('a');
            link.setAttribute('href', url);
            link.setAttribute('download', 'literise_analytics_' + new Date().toISOString().slice(0, 10) + '.csv');
            document.body.appendChild(link);
            link.click();
            document.body.removeChild(link);
            URL.revokeObjectURL(url);
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // DOM HELPER
    // ═══════════════════════════════════════════════════════════════════════
    function setText(id, value) {
        var el = document.getElementById(id);
        if (el) el.textContent = value;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // INIT
    // ═══════════════════════════════════════════════════════════════════════
    function init() {
        // Validate data
        if (!Array.isArray(ANALYTICS_DATA)) {
            ANALYTICS_DATA = [];
        }

        initTabSwitching();
        initDarkModeToggle();
        initCsvExport();
    }

    // Run on DOM ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

}());
