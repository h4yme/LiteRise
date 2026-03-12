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
    // TeacherStudentsController — My Students (Teacher Portal)
    // Roster filtered to teacher's school_id; same 5-tab detail page as admin.
    // =========================================================================
    [AuthFilter]
    public class TeacherStudentsController : AsyncController
    {
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        // =========================================================================
        // Index — Student roster for teacher's school
        // =========================================================================
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
                ViewBag.Error        = "Unable to load students: " + ex.Message;
                ViewBag.StudentsJson = "[]";
                ViewBag.TotalStudents = 0;
                return View("Index");
            }

            ViewBag.StudentsJson  = JsonConvert.SerializeObject(students);
            ViewBag.TotalStudents = students.Count;
            ViewBag.UserName      = Session["UserName"]?.ToString() ?? "Teacher";
            ViewBag.SchoolName    = students.Count > 0
                ? ((string)students[0].school_name ?? "My School")
                : "My School";

            return View("Index");
        }

        // =========================================================================
        // Details — 5-tab student profile (read-only, no admin CRUD controls)
        // =========================================================================
        public async Task<ActionResult> Details(int id)
        {
            try
            {
                var profileTask   = _api.GetPortalStudentAsync(id);
                var placementTask = _api.GetPlacementProgressAsync(id);
                var progressTask  = _api.GetStudentProgressAsync(id);
                var ladderTask    = _api.GetModuleLadderAsync(id);
                var gamesTask     = _api.GetGameResultsAsync(id);
                var badgesTask    = _api.GetBadgesAsync(id);

                await Task.WhenAll(profileTask, placementTask, progressTask,
                                   ladderTask, gamesTask, badgesTask);

                ViewBag.ProfileJson   = JsonConvert.SerializeObject(profileTask.Result);
                ViewBag.PlacementJson = JsonConvert.SerializeObject(placementTask.Result);
                ViewBag.ProgressJson  = JsonConvert.SerializeObject(progressTask.Result);
                ViewBag.LadderJson    = JsonConvert.SerializeObject(ladderTask.Result);
                ViewBag.GamesJson     = JsonConvert.SerializeObject(gamesTask.Result);
                ViewBag.BadgesJson    = JsonConvert.SerializeObject(badgesTask.Result);
                ViewBag.StudentId     = id;
                ViewBag.IsTeacherView = true;
                ViewBag.UserName      = Session["UserName"]?.ToString() ?? "Teacher";
            }
            catch (Exception ex)
            {
                ViewBag.Error = "Unable to load student details: " + ex.Message;
                ViewBag.ProfileJson   = "{}";
                ViewBag.PlacementJson = "{}";
                ViewBag.ProgressJson  = "[]";
                ViewBag.LadderJson    = "[]";
                ViewBag.GamesJson     = "[]";
                ViewBag.BadgesJson    = "[]";
                ViewBag.StudentId     = id;
                ViewBag.IsTeacherView = true;
            }

            return View("Details");
        }

        // =========================================================================
        // ExportCsv — Download class roster as CSV
        // =========================================================================
        [HttpGet]
        public async Task<ActionResult> ExportCsv()
        {
            int? schoolId = int.TryParse(Session["SchoolId"]?.ToString(), out int sid) ? sid : (int?)null;
            List<dynamic> students;
            try
            {
                students = await _api.GetStudentsAsync(schoolId) ?? new List<dynamic>();
            }
            catch
            {
                students = new List<dynamic>();
            }

            var csv = new System.Text.StringBuilder();
            csv.AppendLine("Name,Grade,Level,Pre-Theta,Post-Theta,LessonsDone,XP,LastActive");

            foreach (var s in students)
            {
                string level = ThetaToLevel((string)s.pre_theta);
                csv.AppendLine(string.Join(",",
                    CsvCell((string)s.FullName),
                    CsvCell((string)s.grade),
                    CsvCell(level),
                    CsvCell((string)s.pre_theta),
                    CsvCell((string)s.post_theta),
                    CsvCell((string)s.lessons_done),
                    CsvCell((string)s.total_xp),
                    CsvCell((string)s.last_active)
                ));
            }

            var bytes = System.Text.Encoding.UTF8.GetBytes(csv.ToString());
            return File(bytes, "text/csv", $"my_students_{DateTime.Now:yyyyMMdd}.csv");
        }

        // ── Helpers ───────────────────────────────────────────────────────────
        private static string ThetaToLevel(string raw)
        {
            if (!double.TryParse(raw, out double theta)) return "—";
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
