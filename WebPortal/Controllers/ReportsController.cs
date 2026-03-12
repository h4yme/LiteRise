using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;
using Newtonsoft.Json;

namespace Website.Controllers
{
    [AuthFilter]
    [AuthorizeAdmin]
    public class ReportsController : AsyncController
    {
        // ─────────────────────────────────────────────────────────────────────
        // Shared API service helper
        // ─────────────────────────────────────────────────────────────────────
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        // ═════════════════════════════════════════════════════════════════════
        // Index – loads the Reports dashboard view
        // ═════════════════════════════════════════════════════════════════════
        public async Task<ActionResult> Index()
        {
            var api = _api;

            List<dynamic> students = new List<dynamic>();
            dynamic schools = null;

            try
            {
                var studentsTask = api.GetAllStudentsAsync();
                var schoolsTask  = api.GetSchoolsAsync();
                await Task.WhenAll(studentsTask, schoolsTask);

                students = studentsTask.Result ?? new List<dynamic>();
                schools  = schoolsTask.Result;
            }
            catch (Exception ex)
            {
                ViewBag.Error = "Unable to load report data: " + ex.Message;
            }

            ViewBag.Students     = students;
            ViewBag.StudentsJson = JsonConvert.SerializeObject(students);
            ViewBag.Schools      = schools;
            ViewBag.SchoolsJson  = JsonConvert.SerializeObject(schools ?? new object[0]);

            return View("ReportsView");
        }

        // ═════════════════════════════════════════════════════════════════════
        // GenerateStudentReport – builds a print-ready HTML report for one student
        // ═════════════════════════════════════════════════════════════════════
        [HttpPost]
        public async Task<ActionResult> GenerateStudentReport(int studentId)
        {
            var api = _api;

            // 6 parallel API calls — same pattern as StudentController.Details
            var studentTask   = api.GetPortalStudentAsync(studentId);
            var placementTask = api.GetPlacementProgressAsync(studentId);
            var nodeTask      = api.GetNodeProgressAsync(studentId);
            var ladderTask    = api.GetModuleLadderAsync(studentId);
            var gamesTask     = api.GetGameResultsAsync(studentId);
            var badgesTask    = api.GetBadgesAsync(studentId);

            await Task.WhenAll(studentTask, placementTask, nodeTask, ladderTask, gamesTask, badgesTask);

            dynamic student   = studentTask.Result;
            dynamic placement = placementTask.Result;
            dynamic nodes     = nodeTask.Result;
            dynamic ladder    = ladderTask.Result;
            dynamic games     = gamesTask.Result;
            dynamic badges    = badgesTask.Result;

            // ── Student profile section ───────────────────────────────────────
            string studentName   = student?.full_name  ?? student?.name ?? "Unknown Student";
            string schoolName    = student?.school_name ?? "—";
            string grade         = student?.grade       ?? "—";
            string gender        = student?.gender      ?? "—";
            string level         = student?.placement_level ?? "—";
            string preTheta      = student?.pre_theta  != null  ? ((double)student.pre_theta).ToString("F4")  : "—";
            string postTheta     = student?.post_theta != null  ? ((double)student.post_theta).ToString("F4") : "—";
            string thetaGrowth   = (student?.pre_theta != null && student?.post_theta != null)
                                        ? (((double)student.post_theta) - ((double)student.pre_theta)).ToString("+0.0000;-0.0000")
                                        : "—";
            string totalXp       = student?.total_xp   != null  ? ((int)student.total_xp).ToString("N0")     : "—";
            string streak        = student?.current_streak != null ? student.current_streak.ToString() + " days" : "—";
            string lastActive    = student?.last_active ?? "—";

            var profileSb = new StringBuilder();
            profileSb.Append("<div class=\"report-section\">");
            profileSb.Append("<h2 class=\"section-heading\">Student Profile</h2>");
            profileSb.Append("<table class=\"info-table\">");
            profileSb.AppendFormat("<tr><th>Full Name</th><td>{0}</td><th>School</th><td>{1}</td></tr>", HtmlEncode(studentName), HtmlEncode(schoolName));
            profileSb.AppendFormat("<tr><th>Grade</th><td>{0}</td><th>Gender</th><td>{1}</td></tr>", HtmlEncode(grade), HtmlEncode(gender));
            profileSb.AppendFormat("<tr><th>Placement Level</th><td>{0}</td><th>Total XP</th><td>{1}</td></tr>", HtmlEncode(level), HtmlEncode(totalXp));
            profileSb.AppendFormat("<tr><th>Current Streak</th><td>{0}</td><th>Last Active</th><td>{1}</td></tr>", HtmlEncode(streak), HtmlEncode(lastActive));
            profileSb.Append("</table>");
            profileSb.Append("</div>");

            // ── Pre / Post assessment comparison ─────────────────────────────
            var assessSb = new StringBuilder();
            assessSb.Append("<div class=\"report-section\">");
            assessSb.Append("<h2 class=\"section-heading\">Assessment Summary</h2>");
            assessSb.Append("<table class=\"data-table\">");
            assessSb.Append("<thead><tr><th>Metric</th><th>Pre-Assessment</th><th>Post-Assessment</th><th>Growth</th></tr></thead>");
            assessSb.Append("<tbody>");
            assessSb.AppendFormat("<tr><td>IRT Theta (θ)</td><td>{0}</td><td>{1}</td><td class=\"growth\">{2}</td></tr>",
                HtmlEncode(preTheta), HtmlEncode(postTheta), HtmlEncode(thetaGrowth));
            assessSb.AppendFormat("<tr><td>Placement Level</td><td colspan=\"3\">{0}</td></tr>", HtmlEncode(level));
            assessSb.Append("</tbody></table>");
            assessSb.Append("</div>");

            // ── Category scores table (from placement progress) ───────────────
            var catSb = new StringBuilder();
            catSb.Append("<div class=\"report-section\">");
            catSb.Append("<h2 class=\"section-heading\">Category Scores</h2>");
            catSb.Append("<table class=\"data-table\">");
            catSb.Append("<thead><tr><th>Category</th><th>Score / Theta</th><th>Level</th></tr></thead>");
            catSb.Append("<tbody>");
            try
            {
                if (placement != null)
                {
                    var categories = new[] { "phonics", "vocabulary", "grammar", "comprehension", "creating_text" };
                    var catLabels  = new[] { "Phonics", "Vocabulary", "Grammar", "Comprehension", "Creating Text" };
                    for (int i = 0; i < categories.Length; i++)
                    {
                        string cat = categories[i];
                        string lbl = catLabels[i];
                        dynamic catData = placement?[cat];
                        string score = catData?.theta    != null ? ((double)catData.theta).ToString("F4")  : "—";
                        string clvl  = catData?.level    != null ? (string)catData.level                    : "—";
                        catSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td></tr>", HtmlEncode(lbl), HtmlEncode(score), HtmlEncode(clvl));
                    }
                }
                else
                {
                    catSb.Append("<tr><td colspan=\"3\">No category data available.</td></tr>");
                }
            }
            catch
            {
                catSb.Append("<tr><td colspan=\"3\">Category data could not be loaded.</td></tr>");
            }
            catSb.Append("</tbody></table>");
            catSb.Append("</div>");

            // ── Lesson / node progress table ──────────────────────────────────
            var lessonSb = new StringBuilder();
            lessonSb.Append("<div class=\"report-section\">");
            lessonSb.Append("<h2 class=\"section-heading\">Lesson Progress</h2>");
            lessonSb.Append("<table class=\"data-table\">");
            lessonSb.Append("<thead><tr><th>#</th><th>Lesson / Node</th><th>Module</th><th>Status</th><th>Score</th><th>Attempts</th></tr></thead>");
            lessonSb.Append("<tbody>");
            try
            {
                if (nodes != null)
                {
                    int row = 1;
                    foreach (var n in nodes)
                    {
                        string nName    = n?.node_name    ?? n?.name     ?? "—";
                        string mName    = n?.module_name  ?? "—";
                        string nStatus  = n?.status       ?? "—";
                        string nScore   = n?.score        != null ? ((double)n.score).ToString("F1") : "—";
                        string nAttempts= n?.attempts     != null ? n.attempts.ToString()             : "—";
                        lessonSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td></tr>",
                            row++, HtmlEncode(nName), HtmlEncode(mName), HtmlEncode(nStatus), HtmlEncode(nScore), HtmlEncode(nAttempts));
                    }
                }
                else
                {
                    lessonSb.Append("<tr><td colspan=\"6\">No lesson progress data available.</td></tr>");
                }
            }
            catch
            {
                lessonSb.Append("<tr><td colspan=\"6\">Lesson data could not be loaded.</td></tr>");
            }
            lessonSb.Append("</tbody></table>");
            lessonSb.Append("</div>");

            // ── Game history ──────────────────────────────────────────────────
            var gameSb = new StringBuilder();
            gameSb.Append("<div class=\"report-section\">");
            gameSb.Append("<h2 class=\"section-heading\">Game History</h2>");
            gameSb.Append("<table class=\"data-table\">");
            gameSb.Append("<thead><tr><th>#</th><th>Game</th><th>Score</th><th>XP Earned</th><th>Date</th></tr></thead>");
            gameSb.Append("<tbody>");
            try
            {
                if (games != null)
                {
                    int row = 1;
                    foreach (var g in games)
                    {
                        string gName  = g?.game_name  ?? g?.game_type ?? "—";
                        string gScore = g?.score      != null ? g.score.ToString()    : "—";
                        string gXp    = g?.xp_earned  != null ? g.xp_earned.ToString(): "—";
                        string gDate  = g?.played_at  ?? g?.date ?? "—";
                        gameSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td></tr>",
                            row++, HtmlEncode(gName), HtmlEncode(gScore), HtmlEncode(gXp), HtmlEncode(gDate));
                    }
                }
                else
                {
                    gameSb.Append("<tr><td colspan=\"5\">No game history available.</td></tr>");
                }
            }
            catch
            {
                gameSb.Append("<tr><td colspan=\"5\">Game data could not be loaded.</td></tr>");
            }
            gameSb.Append("</tbody></table>");
            gameSb.Append("</div>");

            // ── Badges ────────────────────────────────────────────────────────
            var badgeSb = new StringBuilder();
            badgeSb.Append("<div class=\"report-section\">");
            badgeSb.Append("<h2 class=\"section-heading\">Badges Earned</h2>");
            badgeSb.Append("<table class=\"data-table\">");
            badgeSb.Append("<thead><tr><th>Badge</th><th>Category</th><th>Description</th><th>Date Earned</th></tr></thead>");
            badgeSb.Append("<tbody>");
            try
            {
                if (badges != null)
                {
                    foreach (var b in badges)
                    {
                        string bName  = b?.name        ?? "—";
                        string bCat   = b?.category    ?? "—";
                        string bDesc  = b?.description ?? "—";
                        string bDate  = b?.earned_at   ?? b?.date ?? "—";
                        badgeSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td></tr>",
                            HtmlEncode(bName), HtmlEncode(bCat), HtmlEncode(bDesc), HtmlEncode(bDate));
                    }
                }
                else
                {
                    badgeSb.Append("<tr><td colspan=\"4\">No badges earned yet.</td></tr>");
                }
            }
            catch
            {
                badgeSb.Append("<tr><td colspan=\"4\">Badge data could not be loaded.</td></tr>");
            }
            badgeSb.Append("</tbody></table>");
            badgeSb.Append("</div>");

            // ── Assemble body and wrap ─────────────────────────────────────────
            string body = profileSb.ToString()
                        + assessSb.ToString()
                        + catSb.ToString()
                        + lessonSb.ToString()
                        + gameSb.ToString()
                        + badgeSb.ToString();

            string htmlString = BuildReportHtml(
                $"Student Report — {HtmlEncode(studentName)}",
                $"<p class=\"report-meta\">School: {HtmlEncode(schoolName)} &nbsp;|&nbsp; Grade: {HtmlEncode(grade)} &nbsp;|&nbsp; Generated: {DateTime.Now:MMMM dd, yyyy HH:mm}</p>"
                + body);

            return Content(htmlString, "text/html");
        }

        // ═════════════════════════════════════════════════════════════════════
        // GenerateSchoolReport – builds a print-ready HTML report for one school
        // ═════════════════════════════════════════════════════════════════════
        [HttpPost]
        public async Task<ActionResult> GenerateSchoolReport(int schoolId)
        {
            var api = _api;

            List<dynamic> students;
            try
            {
                students = await api.GetAllStudentsAsync(schoolId);
            }
            catch (Exception ex)
            {
                return Content(BuildReportHtml("School Report — Error",
                    $"<p class=\"error\">Unable to load student data: {HtmlEncode(ex.Message)}</p>"), "text/html");
            }

            string schoolName = students.Count > 0
                ? (string)(students[0].school_name) ?? $"School #{schoolId}"
                : $"School #{schoolId}";

            int totalStudents   = students.Count;
            int preCount        = students.Count(s => s.pre_theta  != null);
            int postCount       = students.Count(s => s.post_theta != null);

            double avgPreTheta  = preCount  > 0 ? students.Where(s => s.pre_theta  != null).Average(s => (double)s.pre_theta)  : 0;
            double avgPostTheta = postCount > 0 ? students.Where(s => s.post_theta != null).Average(s => (double)s.post_theta) : 0;
            double avgGrowth    = 0;
            var bothStudents    = students.Where(s => s.pre_theta != null && s.post_theta != null).ToList();
            if (bothStudents.Count > 0)
                avgGrowth = bothStudents.Average(s => (double)s.post_theta - (double)s.pre_theta);

            double avgXp        = students.Count(s => s.total_xp != null) > 0
                                    ? students.Where(s => s.total_xp != null).Average(s => (double)s.total_xp)
                                    : 0;
            double avgLessons   = students.Count(s => s.lessons_done != null) > 0
                                    ? students.Where(s => s.lessons_done != null).Average(s => (double)s.lessons_done)
                                    : 0;

            // ── Summary metrics ───────────────────────────────────────────────
            var metricsSb = new StringBuilder();
            metricsSb.Append("<div class=\"report-section\">");
            metricsSb.Append("<h2 class=\"section-heading\">School Overview</h2>");
            metricsSb.Append("<table class=\"info-table\">");
            metricsSb.AppendFormat("<tr><th>Total Students</th><td>{0}</td><th>Pre-Assessment Taken</th><td>{1}</td></tr>", totalStudents, preCount);
            metricsSb.AppendFormat("<tr><th>Post-Assessment Taken</th><td>{0}</td><th>Avg θ Growth</th><td>{1}</td></tr>",
                postCount, avgGrowth.ToString("+0.0000;-0.0000"));
            metricsSb.AppendFormat("<tr><th>Avg Pre θ</th><td>{0}</td><th>Avg Post θ</th><td>{1}</td></tr>",
                avgPreTheta.ToString("F4"), avgPostTheta.ToString("F4"));
            metricsSb.AppendFormat("<tr><th>Avg Total XP</th><td>{0}</td><th>Avg Lessons Done</th><td>{1}</td></tr>",
                avgXp.ToString("N0"), avgLessons.ToString("F1"));
            metricsSb.Append("</table>");
            metricsSb.Append("</div>");

            // ── Student roster table ──────────────────────────────────────────
            var rosterSb = new StringBuilder();
            rosterSb.Append("<div class=\"report-section\">");
            rosterSb.Append("<h2 class=\"section-heading\">Student Roster</h2>");
            rosterSb.Append("<table class=\"data-table\">");
            rosterSb.Append("<thead><tr><th>#</th><th>Name</th><th>Grade</th><th>Level</th><th>Pre θ</th><th>Post θ</th><th>Growth</th><th>XP</th><th>Lessons</th></tr></thead>");
            rosterSb.Append("<tbody>");
            int idx = 1;
            foreach (var s in students.OrderBy(s => (string)(s.full_name ?? s.name ?? "")))
            {
                string sName    = s?.full_name  ?? s?.name ?? "—";
                string sGrade   = s?.grade      ?? "—";
                string sLevel   = s?.placement_level ?? "—";
                string sPre     = s?.pre_theta  != null ? ((double)s.pre_theta).ToString("F4")  : "—";
                string sPost    = s?.post_theta != null ? ((double)s.post_theta).ToString("F4") : "—";
                string sGrowth  = (s?.pre_theta != null && s?.post_theta != null)
                                    ? (((double)s.post_theta) - ((double)s.pre_theta)).ToString("+0.0000;-0.0000")
                                    : "—";
                string sXp      = s?.total_xp     != null ? ((double)s.total_xp).ToString("N0") : "—";
                string sLessons = s?.lessons_done  != null ? s.lessons_done.ToString()            : "—";
                rosterSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td><td>{6}</td><td>{7}</td><td>{8}</td></tr>",
                    idx++, HtmlEncode(sName), HtmlEncode(sGrade), HtmlEncode(sLevel),
                    HtmlEncode(sPre), HtmlEncode(sPost), HtmlEncode(sGrowth),
                    HtmlEncode(sXp), HtmlEncode(sLessons));
            }
            rosterSb.Append("</tbody></table>");
            rosterSb.Append("</div>");

            // ── Top 5 performers ──────────────────────────────────────────────
            var topSb = new StringBuilder();
            topSb.Append("<div class=\"report-section\">");
            topSb.Append("<h2 class=\"section-heading\">Top 5 Performers (by Post θ)</h2>");
            topSb.Append("<table class=\"data-table\">");
            topSb.Append("<thead><tr><th>Rank</th><th>Name</th><th>Grade</th><th>Post θ</th><th>Growth</th><th>XP</th></tr></thead>");
            topSb.Append("<tbody>");
            var top5 = students
                .Where(s => s.post_theta != null)
                .OrderByDescending(s => (double)s.post_theta)
                .Take(5)
                .ToList();
            for (int r = 0; r < top5.Count; r++)
            {
                var s = top5[r];
                string tName   = s?.full_name ?? s?.name ?? "—";
                string tGrade  = s?.grade     ?? "—";
                string tPost   = ((double)s.post_theta).ToString("F4");
                string tGrowth = (s?.pre_theta != null)
                                    ? (((double)s.post_theta) - ((double)s.pre_theta)).ToString("+0.0000;-0.0000")
                                    : "—";
                string tXp     = s?.total_xp  != null ? ((double)s.total_xp).ToString("N0") : "—";
                topSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td></tr>",
                    r + 1, HtmlEncode(tName), HtmlEncode(tGrade), HtmlEncode(tPost), HtmlEncode(tGrowth), HtmlEncode(tXp));
            }
            if (top5.Count == 0)
                topSb.Append("<tr><td colspan=\"6\">No post-assessment data available.</td></tr>");
            topSb.Append("</tbody></table>");
            topSb.Append("</div>");

            string body = metricsSb.ToString() + rosterSb.ToString() + topSb.ToString();

            string htmlString = BuildReportHtml(
                $"School Report — {HtmlEncode(schoolName)}",
                $"<p class=\"report-meta\">School: {HtmlEncode(schoolName)} &nbsp;|&nbsp; Generated: {DateTime.Now:MMMM dd, yyyy HH:mm}</p>"
                + body);

            return Content(htmlString, "text/html");
        }

        // ═════════════════════════════════════════════════════════════════════
        // GenerateSystemReport – builds a system-wide usage HTML report
        // ═════════════════════════════════════════════════════════════════════
        [HttpPost]
        public async Task<ActionResult> GenerateSystemReport()
        {
            var api = _api;

            List<dynamic> students;
            try
            {
                students = await api.GetAllStudentsAsync();
            }
            catch (Exception ex)
            {
                return Content(BuildReportHtml("System Report — Error",
                    $"<p class=\"error\">Unable to load data: {HtmlEncode(ex.Message)}</p>"), "text/html");
            }

            int total        = students.Count;
            int activeCount  = students.Count(s => s.is_active != null && (bool)s.is_active == true);
            int inactiveCount= total - activeCount;
            int preCount     = students.Count(s => s.pre_theta  != null);
            int postCount    = students.Count(s => s.post_theta != null);

            double avgPreTheta  = preCount  > 0 ? students.Where(s => s.pre_theta  != null).Average(s => (double)s.pre_theta)  : 0;
            double avgPostTheta = postCount > 0 ? students.Where(s => s.post_theta != null).Average(s => (double)s.post_theta) : 0;

            double avgXp      = students.Count(s => s.total_xp     != null) > 0
                                    ? students.Where(s => s.total_xp     != null).Average(s => (double)s.total_xp)     : 0;
            double avgLessons = students.Count(s => s.lessons_done != null) > 0
                                    ? students.Where(s => s.lessons_done != null).Average(s => (double)s.lessons_done) : 0;
            double avgStreak  = students.Count(s => s.current_streak != null) > 0
                                    ? students.Where(s => s.current_streak != null).Average(s => (double)s.current_streak) : 0;

            int beginnerCount     = students.Count(s => (string)(s.placement_level) == "beginner");
            int intermediateCount = students.Count(s => (string)(s.placement_level) == "intermediate");
            int advancedCount     = students.Count(s => (string)(s.placement_level) == "advanced");
            int noLevelCount      = total - beginnerCount - intermediateCount - advancedCount;

            // ── Totals section ────────────────────────────────────────────────
            var totalsSb = new StringBuilder();
            totalsSb.Append("<div class=\"report-section\">");
            totalsSb.Append("<h2 class=\"section-heading\">System Overview</h2>");
            totalsSb.Append("<table class=\"info-table\">");
            totalsSb.AppendFormat("<tr><th>Total Students</th><td>{0}</td><th>Active Students</th><td>{1}</td></tr>", total, activeCount);
            totalsSb.AppendFormat("<tr><th>Inactive Students</th><td>{0}</td><th>Pre-Assessment Taken</th><td>{1}</td></tr>", inactiveCount, preCount);
            totalsSb.AppendFormat("<tr><th>Post-Assessment Taken</th><td>{0}</td><th>Avg Pre θ</th><td>{1}</td></tr>", postCount, avgPreTheta.ToString("F4"));
            totalsSb.AppendFormat("<tr><th>Avg Post θ</th><td>{0}</td><th>Avg Total XP</th><td>{1}</td></tr>", avgPostTheta.ToString("F4"), avgXp.ToString("N0"));
            totalsSb.AppendFormat("<tr><th>Avg Lessons Done</th><td>{0}</td><th>Avg Current Streak</th><td>{1} days</td></tr>",
                avgLessons.ToString("F1"), avgStreak.ToString("F1"));
            totalsSb.Append("</table>");
            totalsSb.Append("</div>");

            // ── Active / Inactive breakdown ───────────────────────────────────
            var breakdownSb = new StringBuilder();
            breakdownSb.Append("<div class=\"report-section\">");
            breakdownSb.Append("<h2 class=\"section-heading\">Active / Inactive Breakdown</h2>");
            breakdownSb.Append("<table class=\"data-table\">");
            breakdownSb.Append("<thead><tr><th>Status</th><th>Count</th><th>Percentage</th></tr></thead>");
            breakdownSb.Append("<tbody>");
            breakdownSb.AppendFormat("<tr><td>Active</td><td>{0}</td><td>{1:F1}%</td></tr>",
                activeCount,   total > 0 ? (activeCount  * 100.0 / total) : 0);
            breakdownSb.AppendFormat("<tr><td>Inactive</td><td>{0}</td><td>{1:F1}%</td></tr>",
                inactiveCount, total > 0 ? (inactiveCount * 100.0 / total) : 0);
            breakdownSb.Append("</tbody></table>");

            breakdownSb.Append("<br><table class=\"data-table\">");
            breakdownSb.Append("<thead><tr><th>Placement Level</th><th>Count</th><th>Percentage</th></tr></thead>");
            breakdownSb.Append("<tbody>");
            breakdownSb.AppendFormat("<tr><td>Beginner</td><td>{0}</td><td>{1:F1}%</td></tr>",
                beginnerCount,     total > 0 ? (beginnerCount     * 100.0 / total) : 0);
            breakdownSb.AppendFormat("<tr><td>Intermediate</td><td>{0}</td><td>{1:F1}%</td></tr>",
                intermediateCount, total > 0 ? (intermediateCount * 100.0 / total) : 0);
            breakdownSb.AppendFormat("<tr><td>Advanced</td><td>{0}</td><td>{1:F1}%</td></tr>",
                advancedCount,     total > 0 ? (advancedCount     * 100.0 / total) : 0);
            breakdownSb.AppendFormat("<tr><td>Not Assessed</td><td>{0}</td><td>{1:F1}%</td></tr>",
                noLevelCount,      total > 0 ? (noLevelCount      * 100.0 / total) : 0);
            breakdownSb.Append("</tbody></table>");
            breakdownSb.Append("</div>");

            // ── Per-school comparison table ───────────────────────────────────
            var schoolCompSb = new StringBuilder();
            schoolCompSb.Append("<div class=\"report-section\">");
            schoolCompSb.Append("<h2 class=\"section-heading\">School Comparison</h2>");
            schoolCompSb.Append("<table class=\"data-table\">");
            schoolCompSb.Append("<thead><tr><th>School</th><th>Students</th><th>Active</th><th>Avg Pre θ</th><th>Avg Post θ</th><th>Avg Growth</th><th>Avg XP</th></tr></thead>");
            schoolCompSb.Append("<tbody>");

            var schoolGroups = students
                .GroupBy(s => (string)(s.school_name) ?? "Unknown")
                .OrderByDescending(g => g.Count())
                .ToList();

            foreach (var group in schoolGroups)
            {
                var groupList  = group.ToList();
                int gTotal     = groupList.Count;
                int gActive    = groupList.Count(s => s.is_active != null && (bool)s.is_active == true);
                double gPreAvg = groupList.Count(s => s.pre_theta != null) > 0
                                    ? groupList.Where(s => s.pre_theta != null).Average(s => (double)s.pre_theta)
                                    : 0;
                double gPostAvg= groupList.Count(s => s.post_theta != null) > 0
                                    ? groupList.Where(s => s.post_theta != null).Average(s => (double)s.post_theta)
                                    : 0;
                double gGrowth = 0;
                var gBoth      = groupList.Where(s => s.pre_theta != null && s.post_theta != null).ToList();
                if (gBoth.Count > 0)
                    gGrowth = gBoth.Average(s => (double)s.post_theta - (double)s.pre_theta);
                double gXpAvg  = groupList.Count(s => s.total_xp != null) > 0
                                    ? groupList.Where(s => s.total_xp != null).Average(s => (double)s.total_xp)
                                    : 0;

                schoolCompSb.AppendFormat(
                    "<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td><td>{6}</td></tr>",
                    HtmlEncode(group.Key), gTotal, gActive,
                    gPreAvg.ToString("F4"), gPostAvg.ToString("F4"),
                    gGrowth.ToString("+0.0000;-0.0000"), gXpAvg.ToString("N0"));
            }
            if (!schoolGroups.Any())
                schoolCompSb.Append("<tr><td colspan=\"7\">No data available.</td></tr>");

            schoolCompSb.Append("</tbody></table>");
            schoolCompSb.Append("</div>");

            string body = totalsSb.ToString() + breakdownSb.ToString() + schoolCompSb.ToString();

            string htmlString = BuildReportHtml(
                "System Usage Report",
                $"<p class=\"report-meta\">Generated: {DateTime.Now:MMMM dd, yyyy HH:mm} &nbsp;|&nbsp; Total Students: {total}</p>"
                + body);

            return Content(htmlString, "text/html");
        }

        // ═════════════════════════════════════════════════════════════════════
        // BuildReportHtml – wraps content in a print-ready A4 HTML document
        // ═════════════════════════════════════════════════════════════════════
        private static string BuildReportHtml(string title, string bodyContent)
        {
            return $@"<!DOCTYPE html>
<html lang=""en"">
<head>
    <meta charset=""UTF-8"">
    <meta name=""viewport"" content=""width=device-width, initial-scale=1.0"">
    <title>{title}</title>
    <style>
        /* ── Reset & base ─────────────────────────────────────── */
        *, *::before, *::after {{ box-sizing: border-box; margin: 0; padding: 0; }}
        body {{
            font-family: 'Segoe UI', Arial, sans-serif;
            font-size: 11pt;
            color: #1a1a1a;
            background: #fff;
            padding: 20mm 18mm;
            max-width: 210mm;
            margin: 0 auto;
        }}

        /* ── Report header ────────────────────────────────────── */
        .report-header {{
            border-bottom: 3px solid #2563eb;
            padding-bottom: 12px;
            margin-bottom: 20px;
        }}
        .report-header h1 {{
            font-size: 20pt;
            color: #1e3a8a;
            margin-bottom: 4px;
        }}
        .report-header .report-brand {{
            font-size: 10pt;
            color: #64748b;
            letter-spacing: 0.05em;
            text-transform: uppercase;
        }}
        .report-meta {{
            font-size: 9pt;
            color: #64748b;
            margin-bottom: 18px;
            padding: 6px 10px;
            background: #f8fafc;
            border-left: 3px solid #2563eb;
        }}

        /* ── Section ──────────────────────────────────────────── */
        .report-section {{
            margin-bottom: 24px;
            page-break-inside: avoid;
        }}
        .section-heading {{
            font-size: 13pt;
            color: #1e3a8a;
            border-bottom: 1px solid #e2e8f0;
            padding-bottom: 5px;
            margin-bottom: 10px;
        }}

        /* ── Info table (key-value pairs) ─────────────────────── */
        .info-table {{
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 8px;
        }}
        .info-table th {{
            background: #f1f5f9;
            color: #334155;
            font-weight: 600;
            font-size: 9pt;
            padding: 5px 8px;
            text-align: left;
            border: 1px solid #e2e8f0;
            width: 20%;
        }}
        .info-table td {{
            padding: 5px 8px;
            border: 1px solid #e2e8f0;
            font-size: 10pt;
            width: 30%;
        }}

        /* ── Data table ───────────────────────────────────────── */
        .data-table {{
            width: 100%;
            border-collapse: collapse;
            font-size: 9.5pt;
        }}
        .data-table thead tr {{
            background: #1e3a8a;
            color: #fff;
        }}
        .data-table thead th {{
            padding: 6px 8px;
            text-align: left;
            font-weight: 600;
        }}
        .data-table tbody tr:nth-child(odd)  {{ background: #f8fafc; }}
        .data-table tbody tr:nth-child(even) {{ background: #ffffff; }}
        .data-table tbody td {{
            padding: 5px 8px;
            border-bottom: 1px solid #e2e8f0;
        }}
        .data-table tbody tr:hover {{ background: #eff6ff; }}

        /* ── Growth value coloring ────────────────────────────── */
        .growth {{ color: #16a34a; font-weight: 600; }}

        /* ── Error ────────────────────────────────────────────── */
        .error {{
            color: #dc2626;
            background: #fef2f2;
            padding: 10px;
            border-radius: 4px;
            border: 1px solid #fca5a5;
        }}

        /* ── Print settings ───────────────────────────────────── */
        @media print {{
            body {{
                padding: 15mm 15mm;
                font-size: 10pt;
            }}
            .report-section {{
                page-break-inside: avoid;
            }}
            .data-table thead tr {{
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
            }}
            .info-table th {{
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
            }}
        }}
    </style>
</head>
<body>
    <div class=""report-header"">
        <div class=""report-brand"">LiteRise &mdash; Admin Portal</div>
        <h1>{title}</h1>
    </div>
    {bodyContent}
    <script>
        // Auto-trigger print dialog after content loads
        window.addEventListener('load', function () {{
            // Small delay to ensure styles render before print dialog
            setTimeout(function () {{ window.print(); }}, 400);
        }});
    </script>
</body>
</html>";
        }

        // ─────────────────────────────────────────────────────────────────────
        // HtmlEncode helper – prevent XSS in generated report content
        // ─────────────────────────────────────────────────────────────────────
        private static string HtmlEncode(string value)
        {
            if (string.IsNullOrEmpty(value)) return string.Empty;
            return System.Web.HttpUtility.HtmlEncode(value);
        }
    }
}
