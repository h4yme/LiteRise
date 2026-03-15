/**
 * TeacherAnalyticsScript.js
 * LiteRise Web Portal — Teacher Class Analytics
 * Tabs: Overview, Skills, Engagement
 */

'use strict';

// ─── Chart registry ──────────────────────────────────────────────────────────
const _chartInstances = {};

function destroyAndCreate(canvasId, config) {
    if (_chartInstances[canvasId]) {
        _chartInstances[canvasId].destroy();
        delete _chartInstances[canvasId];
    }
    const canvas = document.getElementById(canvasId);
    if (!canvas) return null;
    const chart = new Chart(canvas, config);
    _chartInstances[canvasId] = chart;
    return chart;
}

// ─── Color palette ───────────────────────────────────────────────────────────
const COLORS = {
    green:  '#11B067',
    blue:   '#0d6efd',
    orange: '#F6AD55',
    red:    '#FC8181',
    purple: '#B794F4',
    teal:   '#4FD1C5',
    gray:   '#A0AEC0',
    greenA: 'rgba(17,176,103,0.75)',
    blueA:  'rgba(13,110,253,0.75)',
    purpleA:'rgba(183,148,244,0.75)',
};

// ─── IRT helpers ─────────────────────────────────────────────────────────────
function classifyTheta(theta) {
    if (theta === null || theta === undefined || isNaN(theta)) return 'Not Taken';
    if (theta < -0.5)  return 'Beginner';
    if (theta <= 0.5)  return 'Intermediate';
    return 'Advanced';
}

function levelColor(level) {
    switch (level) {
        case 'Beginner':     return COLORS.red;
        case 'Intermediate': return COLORS.orange;
        case 'Advanced':     return COLORS.green;
        default:             return COLORS.gray;
    }
}

// ─── Date helpers ─────────────────────────────────────────────────────────────
function daysSince(dateStr) {
    if (!dateStr) return Infinity;
    const diff = Date.now() - new Date(dateStr).getTime();
    return diff / (1000 * 60 * 60 * 24);
}

// ─── Stat computation ─────────────────────────────────────────────────────────
function computeStats(students) {
    const totalStudents = students.length;

    const preStudents  = students.filter(s => s.pre_theta  !== null && s.pre_theta  !== undefined && !isNaN(s.pre_theta));
    const postStudents = students.filter(s => s.post_theta !== null && s.post_theta !== undefined && !isNaN(s.post_theta));

    const preCount  = preStudents.length;
    const postCount = postStudents.length;

    const avgPreTheta  = preCount  ? preStudents.reduce((a, s)  => a + parseFloat(s.pre_theta),  0) / preCount  : null;
    const avgPostTheta = postCount ? postStudents.reduce((a, s) => a + parseFloat(s.post_theta), 0) / postCount : null;

    const bothStudents = students.filter(s =>
        s.pre_theta  !== null && s.pre_theta  !== undefined && !isNaN(s.pre_theta) &&
        s.post_theta !== null && s.post_theta !== undefined && !isNaN(s.post_theta)
    );
    const avgGrowth = bothStudents.length
        ? bothStudents.reduce((a, s) => a + (parseFloat(s.post_theta) - parseFloat(s.pre_theta)), 0) / bothStudents.length
        : null;

    const avgLessonsDone = totalStudents
        ? students.reduce((a, s) => a + (parseInt(s.lessons_done) || 0), 0) / totalStudents
        : 0;

    const activeCount = students.filter(s => daysSince(s.last_active) <= 7).length;

    return { totalStudents, preCount, postCount, avgPreTheta, avgPostTheta, avgGrowth, avgLessonsDone, activeCount };
}

// ─── Populate stat cards ──────────────────────────────────────────────────────
function populateStats(stats) {
    function setText(id, val) {
        const el = document.getElementById(id);
        if (el) el.textContent = val;
    }

    setText('taStatTotalStudents', stats.totalStudents);
    setText('taStatPreCount',      stats.preCount);
    setText('taStatPostCount',     stats.postCount);
    setText('taStatAvgPre',        stats.avgPreTheta  !== null ? stats.avgPreTheta.toFixed(2)  : '—');
    setText('taStatAvgPost',       stats.avgPostTheta !== null ? stats.avgPostTheta.toFixed(2) : '—');
    setText('taStatAvgGrowth',     stats.avgGrowth    !== null ? (stats.avgGrowth >= 0 ? '+' : '') + stats.avgGrowth.toFixed(2) : '—');
    setText('taStatAvgLessons',    stats.avgLessonsDone.toFixed(1));
    setText('taStatActiveCount',   stats.activeCount);
}

// ─── Tab switching ────────────────────────────────────────────────────────────
function initTabs() {
    const headers = document.querySelectorAll('.tab-header[data-tab]');
    const panels  = document.querySelectorAll('.tab-panel');

    headers.forEach(btn => {
        btn.addEventListener('click', () => {
            headers.forEach(h => h.classList.remove('active'));
            panels.forEach(p  => p.classList.add('d-none'));

            btn.classList.add('active');
            const target = document.getElementById(btn.dataset.tab);
            if (target) target.classList.remove('d-none');
        });
    });
}

// ─── Overview: Level doughnut ─────────────────────────────────────────────────
function renderLevelChart(students) {
    const counts = { Beginner: 0, Intermediate: 0, Advanced: 0, 'Not Taken': 0 };
    students.forEach(s => {
        const lvl = s.placement_level || classifyTheta(s.pre_theta);
        if (counts[lvl] !== undefined) counts[lvl]++;
        else counts['Not Taken']++;
    });

    destroyAndCreate('taLevelChart', {
        type: 'doughnut',
        data: {
            labels: ['Beginner', 'Intermediate', 'Advanced', 'Not Taken'],
            datasets: [{
                data: [counts.Beginner, counts.Intermediate, counts.Advanced, counts['Not Taken']],
                backgroundColor: [COLORS.red, COLORS.orange, COLORS.green, COLORS.gray],
                borderWidth: 2,
                borderColor: '#fff',
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.label}: ${ctx.parsed} students`
                    }
                }
            }
        }
    });
}

// ─── Overview: Growth bar chart ───────────────────────────────────────────────
function renderGrowthChart(students) {
    const both = students
        .filter(s =>
            s.pre_theta  !== null && s.pre_theta  !== undefined && !isNaN(s.pre_theta) &&
            s.post_theta !== null && s.post_theta !== undefined && !isNaN(s.post_theta)
        )
        .map(s => ({
            name:      s.FullName || s.full_name || s.name || 'Unknown',
            pre:       parseFloat(s.pre_theta),
            post:      parseFloat(s.post_theta),
            growth:    parseFloat(s.post_theta) - parseFloat(s.pre_theta),
        }))
        .sort((a, b) => b.post - a.post)
        .slice(0, 15);

    const labels   = both.map(s => s.name);
    const preData  = both.map(s => s.pre);
    const postData = both.map(s => s.post);

    destroyAndCreate('taGrowthChart', {
        type: 'bar',
        data: {
            labels,
            datasets: [
                {
                    label: 'Pre-Theta',
                    data: preData,
                    backgroundColor: COLORS.blueA,
                    borderColor: COLORS.blue,
                    borderWidth: 1,
                },
                {
                    label: 'Post-Theta',
                    data: postData,
                    backgroundColor: COLORS.greenA,
                    borderColor: COLORS.green,
                    borderWidth: 1,
                }
            ]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'top' },
                tooltip: {
                    callbacks: {
                        afterLabel: ctx => {
                            const s = both[ctx.dataIndex];
                            return ctx.datasetIndex === 1 ? `Growth: ${s.growth >= 0 ? '+' : ''}${s.growth.toFixed(3)}` : '';
                        }
                    }
                }
            },
            scales: {
                x: {
                    title: { display: true, text: 'Theta (θ)' },
                    min: -3,
                    max: 3,
                }
            }
        }
    });
}

// ─── Overview: Lessons bar chart ──────────────────────────────────────────────
function renderLessonsChart(students) {
    // Approximate: split each student's lessons_done evenly across 5 modules
    const moduleTotals = [0, 0, 0, 0, 0];
    const NODES_PER_MODULE = 13;

    students.forEach(s => {
        const done = parseInt(s.lessons_done) || 0;
        const fullModules   = Math.floor(done / NODES_PER_MODULE);
        const partialNodes  = done % NODES_PER_MODULE;

        for (let m = 0; m < 5; m++) {
            if (m < fullModules) {
                moduleTotals[m] += NODES_PER_MODULE;
            } else if (m === fullModules) {
                moduleTotals[m] += partialNodes;
            }
        }
    });

    // Average completed nodes per student per module
    const avgNodes = moduleTotals.map(t => students.length ? (t / students.length).toFixed(1) : 0);

    destroyAndCreate('taLessonsChart', {
        type: 'bar',
        data: {
            labels: ['Module 1', 'Module 2', 'Module 3', 'Module 4', 'Module 5'],
            datasets: [{
                label: 'Avg Nodes Completed',
                data: avgNodes,
                backgroundColor: COLORS.greenA,
                borderColor: COLORS.green,
                borderWidth: 1,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.parsed.y} / ${NODES_PER_MODULE} avg nodes`
                    }
                }
            },
            scales: {
                y: {
                    min: 0,
                    max: NODES_PER_MODULE,
                    title: { display: true, text: 'Avg Nodes Completed' }
                }
            }
        }
    });
}

// ─── Skills: Radar chart ──────────────────────────────────────────────────────
const CATEGORY_LABELS = ['Phonics', 'Vocabulary', 'Grammar', 'Comprehension', 'Creating Text'];

function renderCategoryChart(dataValues) {
    const values = dataValues || [0, 0, 0, 0, 0];

    destroyAndCreate('taCategoryChart', {
        type: 'radar',
        data: {
            labels: CATEGORY_LABELS,
            datasets: [{
                label: 'Class Average (%)',
                data: values,
                backgroundColor: 'rgba(17,176,103,0.2)',
                borderColor: COLORS.green,
                borderWidth: 2,
                pointBackgroundColor: COLORS.green,
                pointRadius: 4,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                r: {
                    min: 0,
                    max: 100,
                    ticks: { stepSize: 20, callback: v => v + '%' },
                    pointLabels: { font: { size: 13 } }
                }
            },
            plugins: {
                legend: { position: 'top' }
            }
        }
    });
}

// ─── Skills: Category table ───────────────────────────────────────────────────
function cellStyle(pct) {
    if (pct < 60)  return 'background:#FED7D7;color:#742A2A;';
    if (pct < 80)  return 'background:#FEFCBF;color:#744210;';
    return                 'background:#C6F6D5;color:#22543D;';
}

function renderCategoryTable(students, categoryData) {
    const tbody = document.getElementById('taCategoryTable');
    if (!tbody) return;

    tbody.innerHTML = '';

    students.forEach((s, i) => {
        const cats = categoryData ? categoryData[i] : null;
        const scores = cats || CATEGORY_LABELS.map(() => null);
        const hasData = scores.some(v => v !== null);

        const overall = hasData
            ? Math.round(scores.filter(v => v !== null).reduce((a, v) => a + v, 0) / scores.filter(v => v !== null).length)
            : null;

        const catCells = scores.map(v =>
            v !== null
                ? `<td style="${cellStyle(v)};text-align:center;">${v}%</td>`
                : `<td style="text-align:center;color:#A0AEC0;">—</td>`
        ).join('');

        const overallCell = overall !== null
            ? `<td style="${cellStyle(overall)};text-align:center;font-weight:600;">${overall}%</td>`
            : `<td style="text-align:center;color:#A0AEC0;">—</td>`;

        const lvl = s.placement_level || classifyTheta(s.pre_theta);
        const badge = `<span class="badge" style="background:${levelColor(lvl)}">${lvl}</span>`;

        tbody.insertAdjacentHTML('beforeend', `
            <tr>
                <td>${s.FullName || s.full_name || s.name || '—'}</td>
                <td>${s.grade || '—'}</td>
                ${catCells}
                ${overallCell}
            </tr>
        `);
    });

    if (!students.length) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted">No students found.</td></tr>';
    }
}

// ─── Engagement: Streak chart ─────────────────────────────────────────────────
function renderStreakChart(students) {
    const buckets = { '0 days': 0, '1–3 days': 0, '4–7 days': 0, '8–14 days': 0, '15–30 days': 0, '30+ days': 0 };

    students.forEach(s => {
        const streak = parseInt(s.streak_days) || 0;
        if      (streak === 0)  buckets['0 days']++;
        else if (streak <= 3)   buckets['1–3 days']++;
        else if (streak <= 7)   buckets['4–7 days']++;
        else if (streak <= 14)  buckets['8–14 days']++;
        else if (streak <= 30)  buckets['15–30 days']++;
        else                    buckets['30+ days']++;
    });

    destroyAndCreate('taStreakChart', {
        type: 'bar',
        data: {
            labels: Object.keys(buckets),
            datasets: [{
                label: 'Students',
                data: Object.values(buckets),
                backgroundColor: [COLORS.red, COLORS.orange, COLORS.orange, COLORS.teal, COLORS.green, COLORS.green],
                borderWidth: 1,
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 },
                    title: { display: true, text: 'Number of Students' }
                }
            }
        }
    });
}

// ─── Engagement: Active doughnut ──────────────────────────────────────────────
function renderActiveChart(students) {
    const activeCount   = students.filter(s => daysSince(s.last_active) <= 7).length;
    const inactiveCount = students.length - activeCount;

    destroyAndCreate('taActiveChart', {
        type: 'doughnut',
        data: {
            labels: ['Active (7 days)', 'Inactive'],
            datasets: [{
                data: [activeCount, inactiveCount],
                backgroundColor: [COLORS.green, COLORS.red],
                borderWidth: 2,
                borderColor: '#fff',
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: ctx => ` ${ctx.label}: ${ctx.parsed} students`
                    }
                }
            }
        }
    });
}

// ─── Engagement: XP bar chart ─────────────────────────────────────────────────
function renderXpChart(students) {
    const top10 = [...students]
        .sort((a, b) => (parseInt(b.total_xp) || 0) - (parseInt(a.total_xp) || 0))
        .slice(0, 10);

    destroyAndCreate('taXpChart', {
        type: 'bar',
        data: {
            labels: top10.map(s => s.FullName || s.full_name || s.name || 'Unknown'),
            datasets: [{
                label: 'Total XP',
                data: top10.map(s => parseInt(s.total_xp) || 0),
                backgroundColor: COLORS.purpleA,
                borderColor: COLORS.purple,
                borderWidth: 1,
            }]
        },
        options: {
            indexAxis: 'y',
            responsive: true,
            maintainAspectRatio: false,
            plugins: { legend: { display: false } },
            scales: {
                x: {
                    beginAtZero: true,
                    title: { display: true, text: 'Total XP' }
                }
            }
        }
    });
}

// ─── Export CSV ───────────────────────────────────────────────────────────────
function exportCsv() {
    const students = window.studentsData || [];
    if (!students.length) {
        showToast('No student data to export.', 'warning');
        return;
    }

    const headers = ['Name', 'Grade', 'Level', 'PreTheta', 'PostTheta', 'LessonsDone', 'XP', 'StreakDays', 'LastActive'];
    const rows = students.map(s => [
        `"${(s.FullName || s.full_name || s.name || '').replace(/"/g, '""')}"`,
        `"${s.grade || ''}"`,
        `"${s.placement_level || classifyTheta(s.pre_theta)}"`,
        s.pre_theta  !== null && s.pre_theta  !== undefined ? parseFloat(s.pre_theta).toFixed(4)  : '',
        s.post_theta !== null && s.post_theta !== undefined ? parseFloat(s.post_theta).toFixed(4) : '',
        parseInt(s.lessons_done) || 0,
        parseInt(s.total_xp)     || 0,
        parseInt(s.streak_days)  || 0,
        `"${s.last_active || ''}"`,
    ]);

    const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\r\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const url  = URL.createObjectURL(blob);
    const a    = document.createElement('a');
    a.href     = url;
    a.download = `class_analytics_${new Date().toISOString().slice(0, 10)}.csv`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

// ─── Dark mode ────────────────────────────────────────────────────────────────
function toggleDarkMode() {
    document.body.classList.toggle('dark-mode');
    const isDark = document.body.classList.contains('dark-mode');
    const tickColor   = isDark ? '#E2E8F0' : '#4A5568';
    const gridColor   = isDark ? 'rgba(255,255,255,0.1)' : 'rgba(0,0,0,0.08)';

    Object.values(_chartInstances).forEach(chart => {
        // Update scale colors
        if (chart.options.scales) {
            Object.values(chart.options.scales).forEach(scale => {
                if (scale.ticks)  scale.ticks.color  = tickColor;
                if (scale.grid)   scale.grid.color   = gridColor;
                if (scale.pointLabels) scale.pointLabels.color = tickColor;
                if (scale.title)  scale.title.color  = tickColor;
            });
        }
        // Update legend
        if (chart.options.plugins && chart.options.plugins.legend && chart.options.plugins.legend.labels) {
            chart.options.plugins.legend.labels.color = tickColor;
        }
        chart.update();
    });
}

// ─── Category demo loader ─────────────────────────────────────────────────────
function loadCategoryData() {
    const students = window.studentsData || [];
    // Simulate random class averages for demo purposes
    const demo = CATEGORY_LABELS.map(() => Math.round(50 + Math.random() * 45));
    renderCategoryChart(demo);

    // Per-student random scores for table demo
    const perStudentData = students.map(() =>
        CATEGORY_LABELS.map(() => Math.round(40 + Math.random() * 55))
    );
    renderCategoryTable(students, perStudentData);

    showToast('Demo category data loaded. Real data requires placement progress API.', 'info');
}

// ─── Toast ────────────────────────────────────────────────────────────────────
function showToast(message, type) {
    type = type || 'info';
    const bgMap = { success: 'bg-success', danger: 'bg-danger', warning: 'bg-warning text-dark', info: 'bg-info text-dark' };
    const bg = bgMap[type] || 'bg-secondary';

    let container = document.getElementById('taToastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'taToastContainer';
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
    const el = document.getElementById(id);
    const toast = new bootstrap.Toast(el, { delay: 4000 });
    toast.show();
    el.addEventListener('hidden.bs.toast', () => el.remove());
}

// ─── Render all charts ────────────────────────────────────────────────────────
function renderAllCharts(students) {
    // Overview
    renderLevelChart(students);
    renderGrowthChart(students);
    renderLessonsChart(students);

    // Skills — placeholders until Load button is clicked
    renderCategoryChart([0, 0, 0, 0, 0]);
    renderCategoryTable(students, null);

    // Engagement
    renderStreakChart(students);
    renderActiveChart(students);
    renderXpChart(students);
}

// ─── Wire up export / dark mode / load category buttons ──────────────────────
function bindActionButtons() {
    const exportBtn = document.getElementById('exportCsvBtn');
    if (exportBtn) exportBtn.addEventListener('click', exportCsv);

    const darkBtn = document.getElementById('toggleDarkModeBtn');
    if (darkBtn) darkBtn.addEventListener('click', toggleDarkMode);

    const loadCatBtn = document.getElementById('loadCategoryDataBtn');
    if (loadCatBtn) loadCatBtn.addEventListener('click', loadCategoryData);
}

// ─── Entry point ──────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    const students = Array.isArray(window.studentsData) ? window.studentsData : [];

    if (!students.length) {
        showToast('No student data found. Charts will show empty state.', 'warning');
    }

    const stats = computeStats(students);
    populateStats(stats);
    initTabs();
    renderAllCharts(students);
    bindActionButtons();
});
