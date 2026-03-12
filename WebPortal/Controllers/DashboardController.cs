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
    // DashboardController — Admin Dashboard
    // Displays KPI cards, demographics, engagement, and performance charts.
    // =========================================================================
    [AuthFilter]
    public class DashboardController : AsyncController
    {
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        public async Task<ActionResult> Index()
        {
            List<dynamic> students;
            dynamic schools;

            try
            {
                var studentsTask = _api.GetStudentsAsync();
                var schoolsTask  = _api.GetSchoolsAsync();
                await Task.WhenAll(studentsTask, schoolsTask);
                students = studentsTask.Result ?? new List<dynamic>();
                schools  = schoolsTask.Result;
            }
            catch (Exception ex)
            {
                ViewBag.Error        = "Unable to load dashboard data: " + ex.Message;
                ViewBag.StudentsJson = "[]";
                ViewBag.SchoolsJson  = "[]";
                ViewBag.TotalStudents    = 0;
                ViewBag.ActiveStudents   = 0;
                ViewBag.AverageXp        = 0;
                ViewBag.TotalSchools     = 0;
                return View("Index");
            }

            // ── KPI calculations ──────────────────────────────────────────────
            int totalStudents = students.Count;

            var cutoff = DateTime.UtcNow.AddDays(-7);
            int activeStudents = students.Count(s =>
            {
                var raw = (string)s.last_active;
                return DateTime.TryParse(raw, out DateTime d) && d >= cutoff;
            });

            double avgXp = totalStudents > 0
                ? students.Average(s => (double)(s.total_xp ?? 0))
                : 0.0;

            int totalSchools = 0;
            if (schools != null)
            {
                try { totalSchools = ((IEnumerable<dynamic>)schools).Count(); }
                catch { totalSchools = 0; }
            }

            // ── ViewBag ───────────────────────────────────────────────────────
            ViewBag.TotalStudents  = totalStudents;
            ViewBag.ActiveStudents = activeStudents;
            ViewBag.AverageXp      = (int)Math.Round(avgXp);
            ViewBag.TotalSchools   = totalSchools;
            ViewBag.StudentsJson   = JsonConvert.SerializeObject(students);
            ViewBag.SchoolsJson    = JsonConvert.SerializeObject(schools ?? new List<dynamic>());
            ViewBag.UserName       = Session["UserName"]?.ToString() ?? "Admin";

            return View("Index");
        }
    }
}
