using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.SqlClient;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;
using Newtonsoft.Json;

namespace Website.Controllers
{
    // =========================================================================
    // TeacherReportsController
    // Serves the Teacher Reports section: individual student reports,
    // class-summary HTML reports, and CSV exports.
    // =========================================================================
    [AuthFilter]
    public class TeacherReportsController : AsyncController
    {
        // ─────────────────────────────────────────────────────────────────────
        // API service — scoped per request to the teacher's auth token
        // ─────────────────────────────────────────────────────────────────────
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        private static string ConnStr =>
            ConfigurationManager.ConnectionStrings["LiteRiseConnection"].ConnectionString;

        // ─────────────────────────────────────────────────────────────────────
        // Module / node constants (curriculum structure)
        // ─────────────────────────────────────────────────────────────────────
        private const int TotalModules = 5;
        private const int NodesPerModule = 13;

        // =========================================================================
        // Index — Teacher Reports dashboard
        // =========================================================================
        public ActionResult Index()
        {
            int? schoolId = int.TryParse(Session["SchoolId"]?.ToString(), out int sid0) ? sid0 : (int?)null;
            var students  = new List<object>();
            string schoolName = "My School";

            try
            {
                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();
                    const string sql = @"
                        SELECT s.StudentID                                           AS student_id,
                               RTRIM(s.FirstName + ' ' + s.LastName)                AS name,
                               CAST(s.GradeLevel AS NVARCHAR(10))                   AS grade,
                               s.PreAssessmentTheta                                 AS pre_theta,
                               s.PostAssessmentTheta                                AS post_theta,
                               ISNULL(sch.SchoolName, '')                           AS school_name,
                               CAST(ISNULL(s.IsActive, 0) AS BIT)                  AS is_active,
                               s.LastActivityDate                                   AS last_active,
                               ISNULL(s.TotalXP, 0)                                AS total_xp,
                               ISNULL(s.CurrentStreak, 0)                          AS streak_days
                        FROM   dbo.Students s
                        LEFT JOIN dbo.Schools sch ON s.SchoolID = sch.SchoolID
                        WHERE  (@schoolId IS NULL OR s.SchoolID = @schoolId)
                        ORDER BY name";

                    using (var cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@schoolId", (object)schoolId ?? DBNull.Value);
                        using (var rdr = cmd.ExecuteReader())
                        {
                            while (rdr.Read())
                            {
                                var studentName = rdr["name"] == DBNull.Value ? "" : rdr["name"].ToString();
                                students.Add(new
                                {
                                    student_id  = (int)rdr["student_id"],
                                    name        = studentName,
                                    full_name   = studentName,   // alias for JS compatibility
                                    FullName    = studentName,   // alias for JS compatibility
                                    grade       = rdr["grade"]?.ToString(),
                                    pre_theta   = rdr["pre_theta"]  == DBNull.Value ? (double?)null : (double)rdr["pre_theta"],
                                    post_theta  = rdr["post_theta"] == DBNull.Value ? (double?)null : (double)rdr["post_theta"],
                                    school_name = rdr["school_name"]?.ToString(),
                                    is_active   = rdr["is_active"] != DBNull.Value && (bool)rdr["is_active"],
                                    last_active = rdr["last_active"] == DBNull.Value ? null : ((DateTime)rdr["last_active"]).ToString("yyyy-MM-dd"),
                                    total_xp    = (int)rdr["total_xp"],
                                    streak_days = (int)rdr["streak_days"]
                                });
                            }
                        }
                    }
                }

                if (students.Count > 0)
                    schoolName = ((dynamic)students[0]).school_name ?? "My School";
            }
            catch (Exception ex)
            {
                ViewBag.Error        = "Unable to load student data: " + ex.Message;
                ViewBag.StudentsJson = "[]";
                ViewBag.TotalStudents = 0;
                ViewBag.SchoolName   = schoolName;
                ViewBag.UserName     = Session["UserName"]?.ToString() ?? "Teacher";
                return View("Index");
            }

            ViewBag.StudentsJson  = JsonConvert.SerializeObject(students);
            ViewBag.TotalStudents = students.Count;
            ViewBag.SchoolName    = schoolName;
            ViewBag.UserName      = Session["UserName"]?.ToString() ?? "Teacher";

            return View("Index");
        }

        // =========================================================================
        // GetStudentReportData — JSON endpoint for individual student report card
        // =========================================================================
        [HttpGet]
        public async Task<JsonResult> GetStudentReportData(int studentId)
        {
            try
            {
                // Run all four API calls in parallel
                var studentTask   = _api.GetPortalStudentAsync(studentId);
                var placementTask = _api.GetPlacementProgressAsync(studentId);
                var nodeTask      = _api.GetNodeProgressAsync(studentId);
                var ladderTask    = _api.GetModuleLadderAsync(studentId);

                await Task.WhenAll(studentTask, placementTask, nodeTask, ladderTask);

                return Json(new
                {
                    success      = true,
                    student      = studentTask.Result,
                    placement    = placementTask.Result,
                    nodeProgress = nodeTask.Result,
                    moduleLadder = ladderTask.Result
                }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new
                {
                    success = false,
                    message = ex.Message
                }, JsonRequestBehavior.AllowGet);
            }
        }

        // =========================================================================
        // GenerateStudentReport — builds a print-ready HTML report for one student
        // (mirrors admin format, scoped to the teacher's school)
        // =========================================================================
        [HttpPost]
        public async Task<ActionResult> GenerateStudentReport(int studentId)
        {
            // ── Verify the student belongs to this teacher's school ────────────
            int? schoolId = int.TryParse(Session["SchoolId"]?.ToString(), out int sidCheck) ? sidCheck : (int?)null;
            if (schoolId.HasValue)
            {
                try
                {
                    using (var conn = new SqlConnection(ConnStr))
                    {
                        conn.Open();
                        const string checkSql = "SELECT COUNT(1) FROM dbo.Students WHERE StudentID = @sid AND SchoolID = @schId";
                        using (var cmd = new SqlCommand(checkSql, conn))
                        {
                            cmd.Parameters.AddWithValue("@sid",   studentId);
                            cmd.Parameters.AddWithValue("@schId", schoolId.Value);
                            int count = (int)cmd.ExecuteScalar();
                            if (count == 0)
                                return Content(BuildReportHtml("Access Denied",
                                    "<p class=\"error\">You are not authorised to view this student's report.</p>"), "text/html");
                        }
                    }
                }
                catch (Exception ex)
                {
                    return Content(BuildReportHtml("Report Error",
                        $"<p class=\"error\">Unable to verify student access: {HtmlEncode(ex.Message)}</p>"), "text/html");
                }
            }

            // ── Pull all data from SQL ────────────────────────────────────────
            // profile fields
            string studentName = "Unknown Student";
            string schoolName  = "—";
            string grade       = "—";
            string gender      = "—";
            string level       = "—";
            string preTheta    = "—";
            string postTheta   = "—";
            string thetaGrowth = "—";
            string totalXp     = "—";
            string streak      = "—";
            string lastActive  = "—";

            // placement / category fields
            double? preVocab = null, prePhono = null, preRead = null, preGram = null;
            double? preCat1T = null, preCat2T = null, preCat3T = null, preCat4T = null;
            string  preLevelName  = "—";
            double? postVocab = null, postPhono = null, postRead = null, postGram = null;
            double? postCat1T = null, postCat2T = null, postCat3T = null, postCat4T = null;
            string  postLevelName = "—";

            // row collections
            var lessonRows = new List<(string title, string type, string status, string score, string attempts)>();
            var gameRows   = new List<(string game, string score, string xp, string date)>();
            var badgeRows  = new List<(string name, string cat, string desc, string date)>();

            try
            {
                using (var conn = new SqlConnection(ConnStr))
                {
                    await conn.OpenAsync();

                    // ── 1. Student profile ────────────────────────────────────
                    const string profileSql = @"
                        SELECT RTRIM(s.FirstName + ' ' + ISNULL(s.LastName,'')) AS full_name,
                               CAST(s.GradeLevel AS NVARCHAR(10))               AS grade,
                               ISNULL(s.Gender, '—')                            AS gender,
                               s.PreAssessmentTheta,
                               s.PostAssessmentTheta,
                               ISNULL(s.TotalXP, 0)                             AS total_xp,
                               ISNULL(s.CurrentStreak, 0)                       AS current_streak,
                               s.LastActivityDate,
                               ISNULL(sch.SchoolName, '—')                      AS school_name
                        FROM   dbo.Students s
                        LEFT JOIN dbo.Schools sch ON s.SchoolID = sch.SchoolID
                        WHERE  s.StudentID = @sid";

                    using (var cmd = new SqlCommand(profileSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@sid", studentId);
                        using (var rdr = await cmd.ExecuteReaderAsync())
                        {
                            if (await rdr.ReadAsync())
                            {
                                studentName = rdr["full_name"]?.ToString() ?? "—";
                                schoolName  = rdr["school_name"]?.ToString() ?? "—";
                                grade       = rdr["grade"]?.ToString() ?? "—";
                                gender      = rdr["gender"]?.ToString() ?? "—";

                                double? preTh  = rdr["PreAssessmentTheta"]  == DBNull.Value ? (double?)null : (double)rdr["PreAssessmentTheta"];
                                double? postTh = rdr["PostAssessmentTheta"] == DBNull.Value ? (double?)null : (double)rdr["PostAssessmentTheta"];

                                preTheta  = preTh  != null ? preTh.Value.ToString("F4")  : "—";
                                postTheta = postTh != null ? postTh.Value.ToString("F4") : "—";
                                thetaGrowth = (preTh != null && postTh != null)
                                    ? (postTh.Value - preTh.Value).ToString("+0.0000;-0.0000")
                                    : "—";

                                // Derive level from pre-theta
                                if (preTh != null)
                                {
                                    if      (preTh.Value < -0.5) level = "Beginner";
                                    else if (preTh.Value <= 0.5) level = "Intermediate";
                                    else                          level = "Advanced";
                                }

                                totalXp    = ((int)rdr["total_xp"]).ToString("N0");
                                streak     = ((int)rdr["current_streak"]).ToString() + " days";
                                lastActive = rdr["LastActivityDate"] == DBNull.Value
                                    ? "—"
                                    : ((DateTime)rdr["LastActivityDate"]).ToString("dd MMM yyyy");
                            }
                        }
                    }

                    // ── 2. Placement results (latest pre & post) ──────────────
                    const string placeSql = @"
                        SELECT TOP 1 AssessmentType,
                               VocabularyScore, PhonologicalScore, ReadingScore, GrammarScore,
                               Category1Theta,  Category2Theta,   Category3Theta, Category4Theta,
                               LevelName
                        FROM   dbo.PlacementResults
                        WHERE  StudentID = @sid AND AssessmentType = @atype
                        ORDER BY CompletedDate DESC";

                    // Pre-assessment categories
                    using (var cmd = new SqlCommand(placeSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@sid",   studentId);
                        cmd.Parameters.AddWithValue("@atype", "PreAssessment");
                        using (var rdr = await cmd.ExecuteReaderAsync())
                        {
                            if (await rdr.ReadAsync())
                            {
                                preVocab = rdr["VocabularyScore"]  == DBNull.Value ? (double?)null : (double)rdr["VocabularyScore"];
                                prePhono = rdr["PhonologicalScore"] == DBNull.Value ? (double?)null : (double)rdr["PhonologicalScore"];
                                preRead  = rdr["ReadingScore"]      == DBNull.Value ? (double?)null : (double)rdr["ReadingScore"];
                                preGram  = rdr["GrammarScore"]      == DBNull.Value ? (double?)null : (double)rdr["GrammarScore"];
                                preCat1T = rdr["Category1Theta"]    == DBNull.Value ? (double?)null : (double)rdr["Category1Theta"];
                                preCat2T = rdr["Category2Theta"]    == DBNull.Value ? (double?)null : (double)rdr["Category2Theta"];
                                preCat3T = rdr["Category3Theta"]    == DBNull.Value ? (double?)null : (double)rdr["Category3Theta"];
                                preCat4T = rdr["Category4Theta"]    == DBNull.Value ? (double?)null : (double)rdr["Category4Theta"];
                                preLevelName = rdr["LevelName"]?.ToString() ?? "—";
                            }
                        }
                    }

                    // Post-assessment categories
                    using (var cmd = new SqlCommand(placeSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@sid",   studentId);
                        cmd.Parameters.AddWithValue("@atype", "PostAssessment");
                        using (var rdr = await cmd.ExecuteReaderAsync())
                        {
                            if (await rdr.ReadAsync())
                            {
                                postVocab = rdr["VocabularyScore"]  == DBNull.Value ? (double?)null : (double)rdr["VocabularyScore"];
                                postPhono = rdr["PhonologicalScore"] == DBNull.Value ? (double?)null : (double)rdr["PhonologicalScore"];
                                postRead  = rdr["ReadingScore"]      == DBNull.Value ? (double?)null : (double)rdr["ReadingScore"];
                                postGram  = rdr["GrammarScore"]      == DBNull.Value ? (double?)null : (double)rdr["GrammarScore"];
                                postCat1T = rdr["Category1Theta"]    == DBNull.Value ? (double?)null : (double)rdr["Category1Theta"];
                                postCat2T = rdr["Category2Theta"]    == DBNull.Value ? (double?)null : (double)rdr["Category2Theta"];
                                postCat3T = rdr["Category3Theta"]    == DBNull.Value ? (double?)null : (double)rdr["Category3Theta"];
                                postCat4T = rdr["Category4Theta"]    == DBNull.Value ? (double?)null : (double)rdr["Category4Theta"];
                                postLevelName = rdr["LevelName"]?.ToString() ?? "—";
                            }
                        }
                    }

                    // Override level with PlacementResults LevelName if available
                    if (preLevelName != "—") level = preLevelName;

                    // ── 3. Lesson progress ────────────────────────────────────
                    const string lessonSql = @"
                        SELECT l.LessonTitle,
                               ISNULL(l.LessonType, '—')          AS lesson_type,
                               ISNULL(sp.CompletionStatus, '—')   AS status,
                               sp.Score,
                               ISNULL(sp.AttemptsCount, 0)        AS attempts
                        FROM   dbo.StudentProgress sp
                        JOIN   dbo.Lessons l ON sp.LessonID = l.LessonID
                        WHERE  sp.StudentID = @sid
                        ORDER BY sp.ProgressID";

                    using (var cmd = new SqlCommand(lessonSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@sid", studentId);
                        using (var rdr = await cmd.ExecuteReaderAsync())
                        {
                            while (await rdr.ReadAsync())
                            {
                                lessonRows.Add((
                                    rdr["LessonTitle"]?.ToString() ?? "—",
                                    rdr["lesson_type"]?.ToString() ?? "—",
                                    rdr["status"]?.ToString()      ?? "—",
                                    rdr["Score"] == DBNull.Value   ? "—" : ((double)rdr["Score"]).ToString("F1"),
                                    rdr["attempts"]?.ToString()    ?? "0"
                                ));
                            }
                        }
                    }

                    // ── 4. Game history ───────────────────────────────────────
                    const string gameSql = @"
                        SELECT TOP 20
                               GameType,
                               Score,
                               ISNULL(XPEarned, 0)                               AS XPEarned,
                               CONVERT(nvarchar(20), DatePlayed, 105)             AS DatePlayed
                        FROM   dbo.GameResults
                        WHERE  StudentID = @sid
                        ORDER BY DatePlayed DESC";

                    using (var cmd = new SqlCommand(gameSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@sid", studentId);
                        using (var rdr = await cmd.ExecuteReaderAsync())
                        {
                            while (await rdr.ReadAsync())
                            {
                                gameRows.Add((
                                    rdr["GameType"]?.ToString()  ?? "—",
                                    rdr["Score"]?.ToString()     ?? "—",
                                    rdr["XPEarned"]?.ToString()  ?? "0",
                                    rdr["DatePlayed"]?.ToString() ?? "—"
                                ));
                            }
                        }
                    }

                    // ── 5. Badges ─────────────────────────────────────────────
                    const string badgeSql = @"
                        SELECT b.BadgeName,
                               ISNULL(b.BadgeCategory, '—')                      AS BadgeCategory,
                               ISNULL(b.BadgeDescription, '—')                   AS BadgeDescription,
                               CONVERT(nvarchar(20), sb.DateEarned, 105)          AS DateEarned
                        FROM   dbo.StudentBadges sb
                        JOIN   dbo.Badges b ON sb.BadgeID = b.BadgeID
                        WHERE  sb.StudentID = @sid
                        ORDER BY sb.DateEarned DESC";

                    using (var cmd = new SqlCommand(badgeSql, conn))
                    {
                        cmd.Parameters.AddWithValue("@sid", studentId);
                        using (var rdr = await cmd.ExecuteReaderAsync())
                        {
                            while (await rdr.ReadAsync())
                            {
                                badgeRows.Add((
                                    rdr["BadgeName"]?.ToString()        ?? "—",
                                    rdr["BadgeCategory"]?.ToString()    ?? "—",
                                    rdr["BadgeDescription"]?.ToString() ?? "—",
                                    rdr["DateEarned"]?.ToString()       ?? "—"
                                ));
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                return Content(BuildReportHtml("Report Error",
                    $"<p class=\"error\">Unable to load student data: {HtmlEncode(ex.Message)}</p>"), "text/html");
            }

            // ── Build: Student Profile ────────────────────────────────────────
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

            // ── Build: Assessment Summary ─────────────────────────────────────
            var assessSb = new StringBuilder();
            assessSb.Append("<div class=\"report-section\">");
            assessSb.Append("<h2 class=\"section-heading\">Assessment Summary</h2>");
            assessSb.Append("<table class=\"data-table\">");
            assessSb.Append("<thead><tr><th>Metric</th><th>Pre-Assessment</th><th>Post-Assessment</th><th>Growth</th></tr></thead>");
            assessSb.Append("<tbody>");
            assessSb.AppendFormat("<tr><td>IRT Theta (θ)</td><td>{0}</td><td>{1}</td><td class=\"growth\">{2}</td></tr>",
                HtmlEncode(preTheta), HtmlEncode(postTheta), HtmlEncode(thetaGrowth));
            assessSb.AppendFormat("<tr><td>Placement Level</td><td>{0}</td><td>{1}</td><td>—</td></tr>",
                HtmlEncode(preLevelName), HtmlEncode(postLevelName));
            assessSb.Append("</tbody></table>");
            assessSb.Append("</div>");

            // ── Build: Category Scores ────────────────────────────────────────
            // Map: Phonics=Phonological, Vocabulary=Vocabulary, Grammar=Grammar,
            //      Reading/Comprehension=Reading  (4 named categories in PlacementResults)
            var catSb = new StringBuilder();
            catSb.Append("<div class=\"report-section\">");
            catSb.Append("<h2 class=\"section-heading\">Category Scores</h2>");
            catSb.Append("<table class=\"data-table\">");
            catSb.Append("<thead><tr><th>Category</th><th>Pre-Score</th><th>Pre θ</th><th>Post-Score</th><th>Post θ</th></tr></thead>");
            catSb.Append("<tbody>");

            var catDefs = new[]
            {
                ("Phonics",               prePhono, preCat1T, postPhono, postCat1T),
                ("Vocabulary",            preVocab, preCat2T, postVocab, postCat2T),
                ("Grammar",               preGram,  preCat3T, postGram,  postCat3T),
                ("Reading / Comprehension", preRead, preCat4T, postRead,  postCat4T),
            };
            bool hasCatData = catDefs.Any(c => c.Item2 != null || c.Item3 != null);
            if (hasCatData)
            {
                foreach (var (lbl, preScore, preTh, postScore, postTh) in catDefs)
                {
                    catSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td></tr>",
                        HtmlEncode(lbl),
                        preScore  != null ? preScore.Value.ToString("F4")  : "—",
                        preTh     != null ? preTh.Value.ToString("F4")     : "—",
                        postScore != null ? postScore.Value.ToString("F4") : "—",
                        postTh    != null ? postTh.Value.ToString("F4")    : "—");
                }
            }
            else
            {
                catSb.Append("<tr><td colspan=\"5\">No category data available.</td></tr>");
            }
            catSb.Append("</tbody></table>");
            catSb.Append("</div>");

            // ── Build: Lesson Progress ────────────────────────────────────────
            var lessonSb = new StringBuilder();
            lessonSb.Append("<div class=\"report-section\">");
            lessonSb.Append("<h2 class=\"section-heading\">Lesson Progress</h2>");
            lessonSb.Append("<table class=\"data-table\">");
            lessonSb.Append("<thead><tr><th>#</th><th>Lesson</th><th>Type</th><th>Status</th><th>Score</th><th>Attempts</th></tr></thead>");
            lessonSb.Append("<tbody>");
            if (lessonRows.Count > 0)
            {
                int row = 1;
                foreach (var (title, type, status, score, attempts) in lessonRows)
                    lessonSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td></tr>",
                        row++, HtmlEncode(title), HtmlEncode(type), HtmlEncode(status), HtmlEncode(score), HtmlEncode(attempts));
            }
            else
            {
                lessonSb.Append("<tr><td colspan=\"6\">No lesson progress data available.</td></tr>");
            }
            lessonSb.Append("</tbody></table>");
            lessonSb.Append("</div>");

            // ── Build: Game History ───────────────────────────────────────────
            var gameSb = new StringBuilder();
            gameSb.Append("<div class=\"report-section\">");
            gameSb.Append("<h2 class=\"section-heading\">Game History</h2>");
            gameSb.Append("<table class=\"data-table\">");
            gameSb.Append("<thead><tr><th>#</th><th>Game</th><th>Score</th><th>XP Earned</th><th>Date</th></tr></thead>");
            gameSb.Append("<tbody>");
            if (gameRows.Count > 0)
            {
                int row = 1;
                foreach (var (game, score, xp, date) in gameRows)
                    gameSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td></tr>",
                        row++, HtmlEncode(game), HtmlEncode(score), HtmlEncode(xp), HtmlEncode(date));
            }
            else
            {
                gameSb.Append("<tr><td colspan=\"5\">No game history available.</td></tr>");
            }
            gameSb.Append("</tbody></table>");
            gameSb.Append("</div>");

            // ── Build: Badges ─────────────────────────────────────────────────
            var badgeSb = new StringBuilder();
            badgeSb.Append("<div class=\"report-section\">");
            badgeSb.Append("<h2 class=\"section-heading\">Badges Earned</h2>");
            badgeSb.Append("<table class=\"data-table\">");
            badgeSb.Append("<thead><tr><th>Badge</th><th>Category</th><th>Description</th><th>Date Earned</th></tr></thead>");
            badgeSb.Append("<tbody>");
            if (badgeRows.Count > 0)
            {
                foreach (var (name, cat, desc, date) in badgeRows)
                    badgeSb.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td></tr>",
                        HtmlEncode(name), HtmlEncode(cat), HtmlEncode(desc), HtmlEncode(date));
            }
            else
            {
                badgeSb.Append("<tr><td colspan=\"4\">No badges earned yet.</td></tr>");
            }
            badgeSb.Append("</tbody></table>");
            badgeSb.Append("</div>");

            // ── Assemble and return ───────────────────────────────────────────
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

        // =========================================================================
        // GenerateClassReport — builds a print-ready HTML class-summary report
        // =========================================================================
        [HttpPost]
        public async Task<ActionResult> GenerateClassReport()
        {
            int? schoolId = int.TryParse(Session["SchoolId"]?.ToString(), out int sid1) ? sid1 : (int?)null;
            List<dynamic> students;

            try
            {
                students = await _api.GetAllStudentsAsync(schoolId) ?? new List<dynamic>();
            }
            catch (Exception ex)
            {
                var errHtml = BuildReportHtml(
                    "Class Report — Error",
                    $"<p class=\"error\">Unable to load student data: {HtmlEncode(ex.Message)}</p>");
                return Content(errHtml, "text/html");
            }

            int total = students.Count;

            // ── Theta aggregates ──────────────────────────────────────────────
            var preStudents  = students.Where(s => s.pre_theta  != null).ToList();
            var postStudents = students.Where(s => s.post_theta != null).ToList();
            double avgPre  = preStudents.Count  > 0 ? preStudents .Average(s => (double)s.pre_theta)  : 0;
            double avgPost = postStudents.Count > 0 ? postStudents.Average(s => (double)s.post_theta) : 0;

            // ── Level distribution ────────────────────────────────────────────
            int beginnerCount     = students.Count(s => string.Equals((string)s.placement_level, "beginner",     StringComparison.OrdinalIgnoreCase));
            int intermediateCount = students.Count(s => string.Equals((string)s.placement_level, "intermediate", StringComparison.OrdinalIgnoreCase));
            int advancedCount     = students.Count(s => string.Equals((string)s.placement_level, "advanced",     StringComparison.OrdinalIgnoreCase));

            // ── Class summary section ─────────────────────────────────────────
            string schoolName = total > 0 ? (string)students[0].school_name ?? "—" : "—";

            var summary = new StringBuilder();
            summary.Append("<div class=\"report-section\">");
            summary.Append("<h2 class=\"section-heading\">Class Summary</h2>");
            summary.Append("<table class=\"info-table\">");
            summary.AppendFormat("<tr><th>School</th><td>{0}</td><th>Total Students</th><td>{1}</td></tr>",
                HtmlEncode(schoolName), total);
            summary.AppendFormat("<tr><th>Avg Pre-Theta</th><td>{0}</td><th>Avg Post-Theta</th><td>{1}</td></tr>",
                preStudents.Count  > 0 ? avgPre .ToString("F4") : "—",
                postStudents.Count > 0 ? avgPost.ToString("F4") : "—");
            summary.AppendFormat("<tr><th>Report Generated</th><td>{0}</td><th>Teacher</th><td>{1}</td></tr>",
                HtmlEncode(DateTime.Now.ToString("MMMM dd, yyyy HH:mm")),
                HtmlEncode(Session["UserName"]?.ToString() ?? "Teacher"));
            summary.Append("</table>");
            summary.Append("</div>");

            // ── Level distribution table ──────────────────────────────────────
            var levelDist = new StringBuilder();
            levelDist.Append("<div class=\"report-section\">");
            levelDist.Append("<h2 class=\"section-heading\">Level Distribution</h2>");
            levelDist.Append("<table class=\"data-table\">");
            levelDist.Append("<thead><tr><th>Level</th><th>Students</th><th>% of Class</th></tr></thead>");
            levelDist.Append("<tbody>");

            var levels = new[] {
                ("Beginner",     beginnerCount),
                ("Intermediate", intermediateCount),
                ("Advanced",     advancedCount)
            };

            foreach (var (label, count) in levels)
            {
                double pct = total > 0 ? Math.Round((double)count / total * 100, 1) : 0;
                levelDist.AppendFormat("<tr><td>{0}</td><td>{1}</td><td>{2}%</td></tr>",
                    HtmlEncode(label), count, pct);
            }

            int unassigned = total - beginnerCount - intermediateCount - advancedCount;
            if (unassigned > 0)
            {
                double pct = Math.Round((double)unassigned / total * 100, 1);
                levelDist.AppendFormat("<tr><td>Not Assessed</td><td>{0}</td><td>{1}%</td></tr>",
                    unassigned, pct);
            }
            levelDist.Append("</tbody></table>");
            levelDist.Append("</div>");

            // ── Per-student summary table ─────────────────────────────────────
            var perStudent = new StringBuilder();
            perStudent.Append("<div class=\"report-section\">");
            perStudent.Append("<h2 class=\"section-heading\">Student Summary</h2>");
            perStudent.Append("<table class=\"data-table\">");
            perStudent.Append("<thead><tr>");
            perStudent.Append("<th>Name</th><th>Grade</th><th>Level</th>");
            perStudent.Append("<th>Pre-Theta</th><th>Post-Theta</th>");
            perStudent.Append("<th>Lessons Done</th><th>Total XP</th>");
            perStudent.Append("</tr></thead>");
            perStudent.Append("<tbody>");

            foreach (var s in students)
            {
                string name         = HtmlEncode((string)s.full_name ?? (string)s.name ?? "—");
                string grade        = HtmlEncode((string)s.grade      ?? "—");
                string level        = HtmlEncode((string)s.placement_level ?? "—");
                string preTheta     = s.pre_theta  != null ? ((double)s.pre_theta ).ToString("F4") : "—";
                string postTheta    = s.post_theta != null ? ((double)s.post_theta).ToString("F4") : "—";
                string lessonsDone  = s.lessons_done != null ? ((int)s.lessons_done).ToString() : "0";
                string xp           = s.total_xp     != null ? ((long)s.total_xp  ).ToString("N0") : "0";

                perStudent.AppendFormat(
                    "<tr><td>{0}</td><td>{1}</td><td>{2}</td><td>{3}</td><td>{4}</td><td>{5}</td><td>{6}</td></tr>",
                    name, grade, level, preTheta, postTheta, lessonsDone, xp);
            }

            if (total == 0)
                perStudent.Append("<tr><td colspan=\"7\">No students found.</td></tr>");

            perStudent.Append("</tbody></table>");
            perStudent.Append("</div>");

            // ── Assemble and return ───────────────────────────────────────────
            string bodyContent =
                $"<p class=\"report-meta\">Generated: {DateTime.Now:MMMM dd, yyyy HH:mm} &nbsp;|&nbsp; Total Students: {total}</p>"
                + summary
                + levelDist
                + perStudent;

            string html = BuildReportHtml(
                $"Class Report — {HtmlEncode(schoolName)}",
                bodyContent);

            return Content(html, "text/html");
        }

        // =========================================================================
        // ExportClassCsv — downloads a CSV of all students for the school
        // =========================================================================
        [HttpGet]
        public async Task<ActionResult> ExportClassCsv()
        {
            int? schoolId = int.TryParse(Session["SchoolId"]?.ToString(), out int sid2) ? sid2 : (int?)null;
            List<dynamic> students;

            try
            {
                students = await _api.GetAllStudentsAsync(schoolId) ?? new List<dynamic>();
            }
            catch
            {
                students = new List<dynamic>();
            }

            var csv = new StringBuilder();

            // Headers
            csv.AppendLine("Name,Grade,School,Level,PreTheta,PostTheta,LessonsDone,TotalXP,LastActive");

            foreach (var s in students)
            {
                string name        = CsvEscape((string)s.full_name ?? (string)s.name ?? "");
                string grade       = CsvEscape((string)s.grade         ?? "");
                string school      = CsvEscape((string)s.school_name   ?? "");
                string level       = CsvEscape((string)s.placement_level ?? "");
                string preTheta    = s.pre_theta  != null ? ((double)s.pre_theta ).ToString("F4") : "";
                string postTheta   = s.post_theta != null ? ((double)s.post_theta).ToString("F4") : "";
                string lessonsDone = s.lessons_done != null ? ((int)s.lessons_done).ToString() : "0";
                string totalXp     = s.total_xp     != null ? ((long)s.total_xp  ).ToString()   : "0";
                string lastActive  = CsvEscape((string)s.last_active ?? "");

                csv.AppendLine($"{name},{grade},{school},{level},{preTheta},{postTheta},{lessonsDone},{totalXp},{lastActive}");
            }

            byte[] bytes = Encoding.UTF8.GetBytes(csv.ToString());
            string fileName = $"class_report_{DateTime.Now:yyyyMMdd}.csv";

            return File(bytes, "text/csv", fileName);
        }

        // =========================================================================
        // BuildReportHtml — wraps content in a print-ready A4 HTML document
        // =========================================================================
        private static string BuildReportHtml(string title, string bodyContent)
        {
            return $@"<!DOCTYPE html>
<html lang=""en"">
<head>
    <meta charset=""UTF-8"">
    <meta name=""viewport"" content=""width=device-width, initial-scale=1.0"">
    <title>{title}</title>
    <style>
        *, *::before, *::after {{ box-sizing: border-box; margin: 0; padding: 0; }}
        body {{
            font-family: 'Segoe UI', Arial, sans-serif;
            font-size: 11pt;
            color: #1a1a2e;
            background: #fff;
            padding: 20mm 18mm;
            max-width: 210mm;
            margin: 0 auto;
        }}
        .report-header {{
            border-bottom: 3px solid #11B067;
            padding-bottom: 12px;
            margin-bottom: 20px;
        }}
        .report-header h1 {{
            font-size: 20pt;
            color: #1a1a2e;
            margin-bottom: 4px;
        }}
        .report-header .report-brand {{
            font-size: 10pt;
            color: #6c757d;
            letter-spacing: 0.05em;
            text-transform: uppercase;
        }}
        .report-meta {{
            font-size: 9pt;
            color: #6c757d;
            margin-bottom: 18px;
            padding: 6px 10px;
            background: #f8f9fa;
            border-left: 3px solid #11B067;
        }}
        .report-section {{
            margin-bottom: 24px;
            page-break-inside: avoid;
        }}
        .section-heading {{
            font-size: 13pt;
            color: #1a1a2e;
            border-bottom: 1px solid #dee2e6;
            padding-bottom: 5px;
            margin-bottom: 10px;
        }}
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
            padding: 6px 10px;
            text-align: left;
            border: 1px solid #dee2e6;
            width: 20%;
        }}
        .info-table td {{
            padding: 6px 10px;
            border: 1px solid #dee2e6;
            font-size: 10pt;
            width: 30%;
        }}
        .data-table {{
            width: 100%;
            border-collapse: collapse;
            font-size: 9.5pt;
        }}
        .data-table thead tr {{
            background: #11B067;
            color: #fff;
        }}
        .data-table thead th {{
            padding: 7px 10px;
            text-align: left;
            font-weight: 600;
        }}
        .data-table tbody tr:nth-child(odd)  {{ background: #f8f9fa; }}
        .data-table tbody tr:nth-child(even) {{ background: #ffffff; }}
        .data-table tbody td {{
            padding: 6px 10px;
            border-bottom: 1px solid #dee2e6;
        }}
        .error {{
            color: #dc2626;
            background: #fef2f2;
            padding: 12px 16px;
            border-radius: 4px;
            border: 1px solid #fca5a5;
        }}
        @media print {{
            body {{ padding: 15mm 15mm; font-size: 10pt; }}
            .report-section {{ page-break-inside: avoid; }}
            .data-table thead tr,
            .info-table th {{
                -webkit-print-color-adjust: exact;
                print-color-adjust: exact;
            }}
        }}
    </style>
</head>
<body>
    <div class=""report-header"">
        <div class=""report-brand"">LiteRise &mdash; Teacher Portal</div>
        <h1>{title}</h1>
    </div>
    {bodyContent}
    <script>
        window.addEventListener('load', function () {{
            setTimeout(function () {{ window.print(); }}, 400);
        }});
    </script>
</body>
</html>";
        }

        // ─────────────────────────────────────────────────────────────────────
        // HtmlEncode helper
        // ─────────────────────────────────────────────────────────────────────
        private static string HtmlEncode(string value)
        {
            if (string.IsNullOrEmpty(value)) return string.Empty;
            return System.Web.HttpUtility.HtmlEncode(value);
        }

        // ─────────────────────────────────────────────────────────────────────
        // CsvEscape helper — wraps fields containing commas/quotes/newlines
        // ─────────────────────────────────────────────────────────────────────
        private static string CsvEscape(string value)
        {
            if (string.IsNullOrEmpty(value)) return string.Empty;
            if (value.IndexOfAny(new[] { ',', '"', '\r', '\n' }) >= 0)
                return "\"" + value.Replace("\"", "\"\"") + "\"";
            return value;
        }
    }
}
