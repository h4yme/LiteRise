using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.SqlClient;
using System.Web.Mvc;
using Website.Filters;
using Newtonsoft.Json;

namespace Website.Controllers
{
    // =========================================================================
    // TeacherDashboardController — Teacher Dashboard
    // Filtered to teacher's school via direct SQL on LiteRiseConnection.
    // =========================================================================
    [AuthFilter]
    public class TeacherDashboardController : Controller
    {
        private static string ConnStr =>
            ConfigurationManager.ConnectionStrings["LiteRiseConnection"].ConnectionString;

        public ActionResult Index()
        {
            int teacherId;
            if (!int.TryParse(Session["UserId"]?.ToString(), out teacherId) || teacherId <= 0)
            {
                ViewBag.Error           = "Session expired. Please log in again.";
                ViewBag.StudentsJson    = "[]";
                ViewBag.NeedsAttentionJson = "[]";
                ViewBag.TotalStudents   = 0;
                ViewBag.AssessedStudents = 0;
                ViewBag.AvgPreTheta     = "—";
                ViewBag.AvgLessonsDone  = 0;
                ViewBag.SchoolName      = "My School";
                ViewBag.UserName        = Session["UserName"]?.ToString() ?? "Teacher";
                return View("Index");
            }

            var students  = new List<object>();
            string schoolName = "My School";

            try
            {
                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();
                    const string sql = @"
                        SELECT s.StudentID                                          AS student_id,
                               s.FirstName + ' ' + s.LastName                      AS name,
                               s.GradeLevel                                        AS grade,
                               sc.SchoolName                                       AS school_name,
                               s.PreAssessmentTheta                                AS pre_theta,
                               s.PostAssessmentTheta                               AS post_theta,
                               s.TotalXP                                           AS total_xp,
                               s.CurrentStreak                                     AS streak_days,
                               s.LastActivityDate                                  AS last_active,
                               CASE WHEN s.IsActive = 1 THEN 'active'
                                    ELSE 'inactive' END                            AS status,
                               0                                                   AS lessons_done
                        FROM   dbo.Students  s
                        INNER JOIN dbo.Schools  sc ON sc.SchoolID  = s.SchoolID
                        INNER JOIN dbo.Teachers t  ON t.Department = sc.SchoolName
                                                   AND t.TeacherID = @teacherId
                        ORDER  BY s.LastName, s.FirstName";

                    using (var cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@teacherId", teacherId);
                        using (var rdr = cmd.ExecuteReader())
                        {
                            while (rdr.Read())
                            {
                                schoolName = rdr["school_name"]?.ToString() ?? schoolName;
                                students.Add(new
                                {
                                    student_id  = (int)rdr["student_id"],
                                    name        = rdr["name"]?.ToString(),
                                    grade       = rdr["grade"]      == DBNull.Value ? (int?)null    : (int)rdr["grade"],
                                    school_name = rdr["school_name"]?.ToString(),
                                    pre_theta   = rdr["pre_theta"]  == DBNull.Value ? (double?)null : (double)rdr["pre_theta"],
                                    post_theta  = rdr["post_theta"] == DBNull.Value ? (double?)null : (double)rdr["post_theta"],
                                    total_xp    = rdr["total_xp"]   == DBNull.Value ? (int?)null    : (int)rdr["total_xp"],
                                    streak_days = rdr["streak_days"] == DBNull.Value ? (int?)null   : (int)rdr["streak_days"],
                                    last_active = rdr["last_active"] == DBNull.Value
                                                    ? (string)null
                                                    : ((DateTime)rdr["last_active"]).ToString("yyyy-MM-dd"),
                                    status      = rdr["status"]?.ToString() ?? "active",
                                    lessons_done = 0
                                });
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                ViewBag.Error              = "Unable to load dashboard data: " + ex.Message;
                ViewBag.StudentsJson       = "[]";
                ViewBag.NeedsAttentionJson = "[]";
                ViewBag.TotalStudents      = 0;
                ViewBag.AssessedStudents   = 0;
                ViewBag.AvgPreTheta        = "—";
                ViewBag.AvgLessonsDone     = 0;
                ViewBag.SchoolName         = schoolName;
                ViewBag.UserName           = Session["UserName"]?.ToString() ?? "Teacher";
                return View("Index");
            }

            // ── KPI calculations ───────────────────────────────────────────────
            int total    = students.Count;
            int assessed = 0;
            double sumPre = 0;
            var cutoff   = DateTime.UtcNow.AddDays(-7);
            var needsAtt = new List<object>();

            foreach (dynamic s in students)
            {
                double? pre = s.pre_theta;
                if (pre.HasValue) { assessed++; sumPre += pre.Value; }

                bool missingAssessment = !pre.HasValue;
                bool inactive = s.last_active == null ||
                                !DateTime.TryParse((string)s.last_active, out DateTime d) || d < cutoff;

                if ((missingAssessment || inactive) && needsAtt.Count < 10)
                    needsAtt.Add(s);
            }

            double avgPreTheta = assessed > 0 ? sumPre / assessed : 0.0;

            ViewBag.TotalStudents      = total;
            ViewBag.AssessedStudents   = assessed;
            ViewBag.AvgPreTheta        = assessed > 0 ? avgPreTheta.ToString("F2") : "—";
            ViewBag.AvgLessonsDone     = 0;
            ViewBag.StudentsJson       = JsonConvert.SerializeObject(students);
            ViewBag.NeedsAttentionJson = JsonConvert.SerializeObject(needsAtt);
            ViewBag.SchoolName         = schoolName;
            ViewBag.UserName           = Session["UserName"]?.ToString() ?? "Teacher";
            return View("Index");
        }
    }
}
