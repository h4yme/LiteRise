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
                                students.Add(new
                                {
                                    student_id  = (int)rdr["student_id"],
                                    name        = rdr["name"]?.ToString(),
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
