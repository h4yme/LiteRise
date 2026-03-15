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
    // TeacherStudentsController — My Students (Teacher Portal)
    // Roster filtered to teacher's school via direct SQL on LiteRiseConnection.
    // =========================================================================
    [AuthFilter]
    public class TeacherStudentsController : Controller
    {
        private static string ConnStr =>
            ConfigurationManager.ConnectionStrings["LiteRiseConnection"].ConnectionString;

        // =========================================================================
        // Index — Student roster for teacher's school
        // =========================================================================
        public ActionResult Index()
        {
            int teacherId;
            if (!int.TryParse(Session["UserId"]?.ToString(), out teacherId) || teacherId <= 0)
            {
                ViewBag.Error        = "Session expired. Please log in again.";
                ViewBag.StudentsJson = "[]";
                ViewBag.TotalStudents = 0;
                ViewBag.SchoolName   = "My School";
                ViewBag.UserName     = Session["UserName"]?.ToString() ?? "Teacher";
                return View("Index");
            }

            var students = new List<object>();
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
                               s.LastActivityDate                                  AS last_active,
                               CASE WHEN s.IsActive = 1 THEN 'active'
                                    ELSE 'inactive' END                            AS status
                        FROM   dbo.Students  s
                        INNER JOIN dbo.Schools  sc ON sc.SchoolID   = s.SchoolID
                        INNER JOIN dbo.Teachers t  ON t.Department  = sc.SchoolName
                                                   AND t.TeacherID  = @teacherId
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
                                    grade       = rdr["grade"] == DBNull.Value ? (int?)null : (int)rdr["grade"],
                                    school_name = rdr["school_name"]?.ToString(),
                                    pre_theta   = rdr["pre_theta"]  == DBNull.Value ? (double?)null : (double)rdr["pre_theta"],
                                    post_theta  = rdr["post_theta"] == DBNull.Value ? (double?)null : (double)rdr["post_theta"],
                                    total_xp    = rdr["total_xp"]   == DBNull.Value ? (int?)null    : (int)rdr["total_xp"],
                                    last_active = rdr["last_active"] == DBNull.Value
                                                    ? (string)null
                                                    : ((DateTime)rdr["last_active"]).ToString("yyyy-MM-dd"),
                                    status      = rdr["status"]?.ToString() ?? "active"
                                });
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                ViewBag.Error        = "Unable to load students: " + ex.Message;
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
        // ExportCsv — Download class roster as CSV
        // =========================================================================
        [HttpGet]
        public ActionResult ExportCsv()
        {
            int teacherId;
            if (!int.TryParse(Session["UserId"]?.ToString(), out teacherId) || teacherId <= 0)
                return new HttpStatusCodeResult(401);

            var students = new List<dynamic>();
            try
            {
                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();
                    const string sql = @"
                        SELECT s.FirstName + ' ' + s.LastName AS name,
                               s.GradeLevel                   AS grade,
                               s.PreAssessmentTheta           AS pre_theta,
                               s.PostAssessmentTheta          AS post_theta,
                               s.TotalXP                      AS total_xp,
                               s.LastActivityDate             AS last_active
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
                                students.Add(new
                                {
                                    name        = rdr["name"]?.ToString(),
                                    grade       = rdr["grade"]      == DBNull.Value ? "" : rdr["grade"].ToString(),
                                    pre_theta   = rdr["pre_theta"]  == DBNull.Value ? "" : ((double)rdr["pre_theta"]).ToString("F4"),
                                    post_theta  = rdr["post_theta"] == DBNull.Value ? "" : ((double)rdr["post_theta"]).ToString("F4"),
                                    total_xp    = rdr["total_xp"]   == DBNull.Value ? "" : rdr["total_xp"].ToString(),
                                    last_active = rdr["last_active"] == DBNull.Value ? "" : ((DateTime)rdr["last_active"]).ToString("yyyy-MM-dd")
                                });
                            }
                        }
                    }
                }
            }
            catch
            {
                students = new List<dynamic>();
            }

            var csv = new System.Text.StringBuilder();
            csv.AppendLine("Name,Grade,Pre-Theta,Post-Theta,XP,LastActive");
            foreach (var s in students)
            {
                string level = ThetaToLevel(s.pre_theta);
                csv.AppendLine(string.Join(",",
                    CsvCell((string)s.name),
                    CsvCell((string)s.grade),
                    CsvCell((string)s.pre_theta),
                    CsvCell((string)s.post_theta),
                    CsvCell((string)s.total_xp),
                    CsvCell((string)s.last_active)
                ));
            }

            var bytes = System.Text.Encoding.UTF8.GetBytes(csv.ToString());
            return File(bytes, "text/csv", string.Format("my_students_{0:yyyyMMdd}.csv", DateTime.Now));
        }

        // ── Helpers ───────────────────────────────────────────────────────────
        private static string ThetaToLevel(string raw)
        {
            double theta;
            if (!double.TryParse(raw, out theta)) return "—";
            if (theta < -0.5) return "Beginner";
            if (theta <= 0.5) return "Intermediate";
            return "Advanced";
        }

        private static string CsvCell(string val)
        {
            val = val ?? "";
            return val.Contains(",") || val.Contains("\"") || val.Contains("\n")
                ? "\"" + val.Replace("\"", "\"\"") + "\""
                : val;
        }
    }
}
