using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.SqlClient;
using System.Web.Mvc;
using Website.Filters;
using Newtonsoft.Json;

namespace Website.Controllers
{
    [AuthFilter]
    public class TeacherAnalyticsController : Controller
    {
        private static string ConnStr =>
            ConfigurationManager.ConnectionStrings["LiteRiseConnection"].ConnectionString;

        public ActionResult Index()
        {
            int teacherId;
            if (!int.TryParse(Session["UserId"]?.ToString(), out teacherId) || teacherId <= 0)
            {
                ViewBag.Error = "Session expired. Please log in again.";
                SetEmptyViewBag();
                return View("Index");
            }

            var students   = new List<Dictionary<string, object>>();
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
                                    ELSE 'inactive' END                            AS status
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
                                students.Add(new Dictionary<string, object>
                                {
                                    ["student_id"]  = (int)rdr["student_id"],
                                    ["name"]        = rdr["name"]?.ToString(),
                                    ["grade"]       = rdr["grade"]      == DBNull.Value ? (object)null : (int)rdr["grade"],
                                    ["school_name"] = rdr["school_name"]?.ToString(),
                                    ["pre_theta"]   = rdr["pre_theta"]  == DBNull.Value ? (object)null : (double)rdr["pre_theta"],
                                    ["post_theta"]  = rdr["post_theta"] == DBNull.Value ? (object)null : (double)rdr["post_theta"],
                                    ["total_xp"]    = rdr["total_xp"]   == DBNull.Value ? (object)null : (int)rdr["total_xp"],
                                    ["streak_days"] = rdr["streak_days"] == DBNull.Value ? (object)null : (int)rdr["streak_days"],
                                    ["last_active"] = rdr["last_active"] == DBNull.Value
                                                        ? (object)null
                                                        : ((DateTime)rdr["last_active"]).ToString("yyyy-MM-dd"),
                                    ["status"]      = rdr["status"]?.ToString() ?? "active",
                                    ["lessons_done"] = 0
                                });
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                ViewBag.Error = "Unable to load analytics: " + ex.Message;
                SetEmptyViewBag();
                ViewBag.SchoolName   = schoolName;
                ViewBag.StudentsJson = "[]";
                return View("Index");
            }

            // ── Aggregate stats ───────────────────────────────────────────────
            int total = students.Count;
            int preCount = 0, postCount = 0, bothCount = 0;
            int beginner = 0, intermediate = 0, advanced = 0;
            double sumPre = 0, sumPost = 0, sumGrowth = 0, sumXp = 0, sumStreak = 0;
            int activeCount = 0;
            var cutoff = DateTime.UtcNow.AddDays(-7);

            foreach (var s in students)
            {
                double? pre    = s["pre_theta"]   as double?;
                double? post   = s["post_theta"]  as double?;
                int?    xp     = s["total_xp"]    as int?;
                int?    streak = s["streak_days"] as int?;
                string  la     = s["last_active"]  as string;

                if (pre.HasValue)
                {
                    preCount++;
                    sumPre += pre.Value;
                    if      (pre.Value < -0.5) beginner++;
                    else if (pre.Value <= 0.5) intermediate++;
                    else                       advanced++;
                }
                if (post.HasValue) { postCount++; sumPost += post.Value; }
                if (pre.HasValue && post.HasValue) { bothCount++; sumGrowth += post.Value - pre.Value; }

                if (la != null && DateTime.TryParse(la, out DateTime d) && d >= cutoff) activeCount++;
                sumXp     += xp.HasValue     ? xp.Value     : 0;
                sumStreak += streak.HasValue ? streak.Value : 0;
            }

            double avgPre    = preCount  > 0 ? sumPre    / preCount  : 0;
            double avgPost   = postCount > 0 ? sumPost   / postCount : 0;
            double avgGrowth = bothCount > 0 ? sumGrowth / bothCount : 0;
            double avgXp     = total     > 0 ? sumXp     / total     : 0;
            double avgStreak = total     > 0 ? sumStreak / total     : 0;

            ViewBag.StudentsJson         = JsonConvert.SerializeObject(students);
            ViewBag.TotalStudents        = total;
            ViewBag.PreAssessmentCount   = preCount;
            ViewBag.PostAssessmentCount  = postCount;
            ViewBag.BeginnerCount        = beginner;
            ViewBag.IntermediateCount    = intermediate;
            ViewBag.AdvancedCount        = advanced;
            ViewBag.AvgPreTheta          = Math.Round(avgPre,    3);
            ViewBag.AvgPostTheta         = Math.Round(avgPost,   3);
            ViewBag.AvgGrowth            = Math.Round(avgGrowth, 3);
            ViewBag.AvgLessonsDone       = 0;
            ViewBag.LessonCompletionRate = 0;
            ViewBag.AvgStreak            = Math.Round(avgStreak, 1);
            ViewBag.ActiveCount          = activeCount;
            ViewBag.ActivePercent        = total > 0 ? Math.Round((double)activeCount / total * 100, 1) : 0;
            ViewBag.AvgXp                = Math.Round(avgXp, 0);
            ViewBag.SchoolName           = schoolName;
            ViewBag.UserName             = Session["UserName"]?.ToString() ?? "Teacher";
            return View("Index");
        }

        private void SetEmptyViewBag()
        {
            ViewBag.StudentsJson         = "[]";
            ViewBag.TotalStudents        = 0;
            ViewBag.PreAssessmentCount   = 0;
            ViewBag.PostAssessmentCount  = 0;
            ViewBag.BeginnerCount        = 0;
            ViewBag.IntermediateCount    = 0;
            ViewBag.AdvancedCount        = 0;
            ViewBag.AvgPreTheta          = 0;
            ViewBag.AvgPostTheta         = 0;
            ViewBag.AvgGrowth            = 0;
            ViewBag.AvgLessonsDone       = 0;
            ViewBag.LessonCompletionRate = 0;
            ViewBag.AvgStreak            = 0;
            ViewBag.ActiveCount          = 0;
            ViewBag.ActivePercent        = 0;
            ViewBag.AvgXp                = 0;
            ViewBag.SchoolName           = "My School";
            ViewBag.UserName             = Session["UserName"]?.ToString() ?? "Teacher";
        }
    }
}
