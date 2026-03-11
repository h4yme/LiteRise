<?php
require_once __DIR__ . '/../auth_check.php';
require_once __DIR__ . '/../src/layout.php';

$user = requirePortalAuth('Teacher');

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

    .coming-soon {
        display: flex; flex-direction: column; align-items: center; justify-content: center;
        gap: 10px; padding: 40px 20px; color: var(--text-muted); text-align: center;
    }
    .coming-soon .icon { font-size: 2.5rem; opacity: .4; }
    .coming-soon p { font-size: .85rem; }

    /* Attention list */
    .attention-list { display: flex; flex-direction: column; gap: 10px; }
    .attention-item {
        display: flex; align-items: center; gap: 12px;
        padding: 10px 12px; border-radius: 10px;
        background: #FFF8E6; border: 1px solid #FDC94A33;
        font-size: .85rem;
    }
    .attention-dot { width: 8px; height: 8px; border-radius: 50%; background: #FDC94A; flex-shrink: 0; }
    .attention-text { color: var(--text-sec); flex: 1; line-height: 1.4; }
    .attention-badge {
        font-size: .7rem; font-weight: 700; padding: 2px 8px; border-radius: 6px;
        background: #FDC94A22; color: #D97706;
    }

    /* Level distribution */
    .level-bars { display: flex; flex-direction: column; gap: 10px; }
    .level-row { display: flex; align-items: center; gap: 10px; font-size: .85rem; }
    .level-name { width: 90px; color: var(--text-sec); font-weight: 600; flex-shrink: 0; }
    .level-bar-wrap { flex: 1; background: #F1F5F9; border-radius: 999px; height: 8px; overflow: hidden; }
    .level-bar { height: 100%; border-radius: 999px; }
    .level-count { width: 28px; text-align: right; color: var(--text-muted); font-size: .78rem; }
</style>

<div class="page-header">
    <div class="breadcrumb">Teacher Portal <span>/ Dashboard</span></div>
    <h1>My Class Dashboard</h1>
    <p>Overview of your assigned students and class performance</p>
</div>

<!-- KPI cards -->
<div class="kpi-grid">
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#11B067"></div>
        <div class="kpi-label">My Students</div>
        <div class="kpi-value" id="kpiTotal">—</div>
        <div class="kpi-sub">Assigned to your class</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#7C3AED"></div>
        <div class="kpi-label">Pre-Assessment Done</div>
        <div class="kpi-value" id="kpiAssessed">—</div>
        <div class="kpi-sub">Completed placement test</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#14B8A6"></div>
        <div class="kpi-label">Class Avg. Theta</div>
        <div class="kpi-value" id="kpiTheta">—</div>
        <div class="kpi-sub">Average ability score</div>
    </div>
    <div class="kpi-card">
        <div class="kpi-accent" style="background:#FDC94A"></div>
        <div class="kpi-label">Lesson Completion</div>
        <div class="kpi-value" id="kpiCompletion">—</div>
        <div class="kpi-sub">Avg. nodes done of 65</div>
    </div>
</div>

<!-- Main sections -->
<div class="section-grid">
    <!-- Level distribution -->
    <div class="section-card">
        <div class="section-title">
            Class Level Distribution
            <a href="../analytics/index.php">Full Analytics →</a>
        </div>
        <div class="level-bars" id="levelBars">
            <div class="level-row">
                <span class="level-name">Beginner</span>
                <div class="level-bar-wrap"><div class="level-bar" style="width:0%;background:#7C3AED"></div></div>
                <span class="level-count" id="cntBeg">—</span>
            </div>
            <div class="level-row">
                <span class="level-name">Intermediate</span>
                <div class="level-bar-wrap"><div class="level-bar" style="width:0%;background:#11B067"></div></div>
                <span class="level-count" id="cntInt">—</span>
            </div>
            <div class="level-row">
                <span class="level-name">Advanced</span>
                <div class="level-bar-wrap"><div class="level-bar" style="width:0%;background:#FDC94A"></div></div>
                <span class="level-count" id="cntAdv">—</span>
            </div>
            <div class="coming-soon" style="padding:20px">
                <div class="icon">📊</div>
                <p>Distribution loads after database connection is set up.</p>
            </div>
        </div>
    </div>

    <!-- Students needing attention -->
    <div class="section-card">
        <div class="section-title">
            Students Needing Attention
            <a href="../students/index.php">All Students →</a>
        </div>
        <div class="attention-list" id="attentionList">
            <div class="coming-soon" style="padding:20px">
                <div class="icon">👀</div>
                <p>Students inactive 7+ days or with low accuracy will appear here.</p>
            </div>
        </div>
    </div>
</div>

<div style="height:20px"></div>

<div class="section-grid">
    <!-- Category skills snapshot -->
    <div class="section-card">
        <div class="section-title">
            Class Category Scores
            <a href="../analytics/index.php">Skill Breakdown →</a>
        </div>
        <div class="coming-soon">
            <div class="icon">🎯</div>
            <p>Avg. class score per category:<br>Phonics · Vocabulary · Grammar · Comprehension · Creating Text</p>
        </div>
    </div>

    <!-- Recent activity -->
    <div class="section-card">
        <div class="section-title">
            Recent Student Activity
            <a href="../students/index.php">View Roster →</a>
        </div>
        <div class="coming-soon">
            <div class="icon">⏱</div>
            <p>Latest lesson completions, badge awards, and login events from your class.</p>
        </div>
    </div>
</div>

<script>
async function loadTeacherDashboard() {
    // Placeholder: will call get_student_progress.php filtered by teacher's school
    // const resp = await fetch('../../api/get_student_progress.php?school_id=<?= (int)($user['school_id'] ?? 0) ?>');
    console.log('Teacher dashboard ready — data will load once API endpoints are wired.');
}
loadTeacherDashboard();
</script>

<?php portalFooter(); ?>
