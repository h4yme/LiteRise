using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;
using Newtonsoft.Json;

namespace Website.Controllers
{
    // =========================================================================
    // TeacherDashboardController — Teacher Dashboard
    // Displays class KPIs, level distribution, and student roster for the
    // teacher's assigned school (from Session["SchoolId"]).
    // =========================================================================
    [AuthFilter]
    public class TeacherDashboardController : AsyncController
    {
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        public async Task<ActionResult> Index()
        {
            int? schoolId = int.TryParse(Session["SchoolId"]?.ToString(), out int sid) ? sid : (int?)null;
            List<dynamic> students;

            try
            {
                students = await _api.GetStudentsAsync(schoolId) ?? new List<dynamic>();
            }
            catch (Exception ex)
            {
                ViewBag.Error        = "Unable to load dashboard data: " + ex.Message;
                ViewBag.StudentsJson = "[]";
                ViewBag.TotalStudents       = 0;
                ViewBag.AssessedStudents    = 0;
                ViewBag.AvgPreTheta         = "—";
                ViewBag.AvgLessonsDone      = 0;
                ViewBag.UserName            = Session["UserName"]?.ToString() ?? "Teacher";
                return View("Index");
            }

            // ── KPI calculations ──────────────────────────────────────────────
            int total = students.Count;

            int assessed = students.Count(s =>
            {
                var raw = (string)s.pre_theta;
                return !string.IsNullOrEmpty(raw) && raw != "null";
            });

            double avgPreTheta = assessed > 0
                ? students
                    .Where(s => double.TryParse((string)s.pre_theta, out _))
                    .Average(s => double.Parse((string)s.pre_theta))
                : 0.0;

            double avgLessons = total > 0
                ? students.Average(s => (double)(s.lessons_done ?? 0))
                : 0.0;

            // ── Needs Attention: inactive 7+ days or missing pre-assessment ───
            var cutoff = DateTime.UtcNow.AddDays(-7);
            var needsAttention = students.Where(s =>
            {
                bool missingAssessment = string.IsNullOrEmpty((string)s.pre_theta) || (string)s.pre_theta == "null";
                bool inactive = !DateTime.TryParse((string)s.last_active, out DateTime d) || d < cutoff;
                return missingAssessment || inactive;
            }).Take(10).ToList();

            // ── ViewBag ───────────────────────────────────────────────────────
            ViewBag.TotalStudents    = total;
            ViewBag.AssessedStudents = assessed;
            ViewBag.AvgPreTheta      = assessed > 0 ? avgPreTheta.ToString("F2") : "—";
            ViewBag.AvgLessonsDone   = (int)Math.Round(avgLessons);
            ViewBag.StudentsJson     = JsonConvert.SerializeObject(students);
            ViewBag.NeedsAttentionJson = JsonConvert.SerializeObject(needsAttention);
            ViewBag.UserName         = Session["UserName"]?.ToString() ?? "Teacher";
            ViewBag.SchoolName       = total > 0
                ? ((string)students[0].school_name ?? "My School")
                : "My School";

            return View("Index");
        }
    }
}
