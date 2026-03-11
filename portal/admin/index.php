<?php
require_once __DIR__ . '/../auth_check.php';
require_once __DIR__ . '/../src/layout.php';

$user = requirePortalAuth('Admin');

portalHeader($user, 'Dashboard', 'Dashboard');
?>

<style>
    .kpi-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
        gap: 16px;
        margin-bottom: 28px;
    }
    .kpi-card {
        background: #fff;
        border: 1px solid var(--border);
        border-radius: 14px;
        padding: 20px;
        box-shadow: var(--shadow-sm);
        display: flex;
        flex-direction: column;
        gap: 8px;
    }
    .kpi-accent { height: 4px; border-radius: 4px; margin-bottom: 4px; }
    .kpi-label { font-size: .8rem; font-weight: 600; color: var(--text-muted); text-transform: uppercase; letter-spacing: .5px; }
    .kpi-value { font-size: 2rem; font-weight: 800; color: var(--text); line-height: 1; }
    .kpi-sub   { font-size: .78rem; color: var(--text-muted); }
    .kpi-change-pos { color: #11B067; font-weight: 700; }
    .kpi-change-neg { color: #EF4444; font-weight: 700; }

    .section-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
    @media (max-width: 900px) { .section-grid { grid-template-columns: 1fr; } }

    .section-card {
        background: #fff; border: 1px solid var(--border);
        border-radius: 14px; padding: 22px; box-shadow: var(--shadow-sm);
    }
    .section-title {
        font-size: .95rem; font-weight: 700; color: var(--text);
        margin-bottom: 16px; padding-bottom: 12px;
        border-bottom: 1px solid var(--border);
        display: flex; align-items: center; justify-content: space-between;
    }
    .section-title a { font-size: .78rem; font-weight: 600; color: var(--green); text-decoration: none; }
    .section-title a:hover { color: var(--green-dark); }

    /* Coming soon placeholder */
    .coming-soon {
        display: flex; flex-direction: column; align-items: center; justify-content: center;
        gap: 10px; padding: 40px 20px; color: var(--text-muted);
        text-align: center;
    }
    .coming-soon .icon { font-size: 2.5rem; opacity: .4; }
    .coming-soon p { font-size: .85rem; }

    /* Activity list */
    .activity-list { display: flex; flex-direction: column; gap: 12px; }
    .activity-item {
        display: flex; align-items: flex-start; gap: 12px;
        padding-bottom: 12px; border-bottom: 1px solid var(--border);
        font-size: .85rem;
    }
    .activity-item:last-child { border-bottom: none; padding-bottom: 0; }
    .activity-dot {
        width: 8px; height: 8px; border-radius: 50%; margin-top: 5px;
        background: var(--green); flex-shrink: 0;
    }
    .activity-text { color: var(--text-sec); line-height: 1.5; }
    .activity-time { font-size: .72rem; color: var(--text-muted); margin-top: 2px; }
</style>

<div class="page-header">
    <div class="breadcrumb">Admin Portal <span>/ Dashboard</span></div>
    <h1>Dashboard</h1>
    <p>System-wide overview for all schools and students</p>
</div>

<!-- KPI cards -->
<div class="kpi-grid">
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#7C3AED"></div>
        <div class="kpi-label">Total Students</div>
        <div class="kpi-value" id="kpiStudents">—</div>
        <div class="kpi-sub">Registered learners</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#11B067"></div>
        <div class="kpi-label">Partner Schools</div>
        <div class="kpi-value" id="kpiSchools">—</div>
        <div class="kpi-sub">Active institutions</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#FDC94A"></div>
        <div class="kpi-label">Active Students</div>
        <div class="kpi-value" id="kpiActive">—</div>
        <div class="kpi-sub">Logged in last 7 days</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#14B8A6"></div>
        <div class="kpi-label">Pre-Assessment Done</div>
        <div class="kpi-value" id="kpiAssessed">—</div>
        <div class="kpi-sub">Placement completed</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#F97316"></div>
        <div class="kpi-label">Avg. Theta (θ)</div>
        <div class="kpi-value" id="kpiTheta">—</div>
        <div class="kpi-sub">Ability across all students</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#EC4899"></div>
        <div class="kpi-label">Avg. XP</div>
        <div class="kpi-value" id="kpiXP">—</div>
        <div class="kpi-sub">Experience points earned</div>
    </div>
</div>

<!-- Charts + Activity -->
<div class="section-grid">
    <div class="section-card">
        <div class="section-title">
            Performance Overview
            <a href="../analytics/index.php">View Analytics →</a>
        </div>
        <div class="coming-soon">
            <div class="icon">📊</div>
            <p>Ability growth &amp; XP distribution charts will load here.</p>
        </div>
    </div>

    <div class="section-card">
        <div class="section-title">
            Recent Activity
            <a href="../students/index.php">All Students →</a>
        </div>
        <div class="activity-list" id="activityFeed">
            <div class="activity-item">
                <div class="activity-dot"></div>
                <div>
                    <div class="activity-text">Activity feed loading…</div>
                    <div class="activity-time">Connecting to database</div>
                </div>
            </div>
        </div>
    </div>
</div>

<div style="height:20px"></div>

<div class="section-grid">
    <div class="section-card">
        <div class="section-title">
            Top Performing Schools
            <a href="../schools/index.php">All Schools →</a>
        </div>
        <div class="coming-soon">
            <div class="icon">🏫</div>
            <p>School leaderboard by avg. theta &amp; completion rate.</p>
        </div>
    </div>

    <div class="section-card">
        <div class="section-title">
            Game Engagement
            <a href="../analytics/index.php">View Detail →</a>
        </div>
        <div class="coming-soon">
            <div class="icon">🎮</div>
            <p>Most-played games and accuracy breakdown by game type.</p>
        </div>
    </div>
</div>

<script>
// Load KPI summary from existing API endpoints
async function loadDashboard() {
    try {
        const resp = await fetch('../../api/get_student_progress.php');
        if (!resp.ok) return;
        const data = await resp.json();
        // Populate KPIs when the API is wired up
        // document.getElementById('kpiStudents').textContent = data.total_students;
    } catch (e) {
        console.warn('Dashboard data unavailable:', e);
    }
}

loadDashboard();
</script>

<?php portalFooter(); ?>
