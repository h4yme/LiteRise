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
    // StudentController — Admin Student Management
    // Index: full student roster with filters and CSV export.
    // Details: 5-tab student profile (Profile, Assessment, Lessons, Games, Badges).
    // =========================================================================
    [AuthFilter]
    [AuthorizeAdmin]
    public class StudentController : AsyncController
    {
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        // =========================================================================
        // Index — Student list
        // =========================================================================
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
                ViewBag.Error        = "Unable to load students: " + ex.Message;
                ViewBag.StudentsJson = "[]";
                ViewBag.SchoolsJson  = "[]";
                return View("Index");
            }

            ViewBag.StudentsJson = JsonConvert.SerializeObject(students);
            ViewBag.SchoolsJson  = JsonConvert.SerializeObject(schools ?? new List<dynamic>());
            ViewBag.TotalStudents = students.Count;
            ViewBag.UserName     = Session["UserName"]?.ToString() ?? "Admin";

            return View("Index");
        }

        // =========================================================================
        // Details — Student 5-tab profile page
        // =========================================================================
        public async Task<ActionResult> Details(int id)
        {
            try
            {
                var profileTask    = _api.GetPortalStudentAsync(id);
                var placementTask  = _api.GetPlacementProgressAsync(id);
                var progressTask   = _api.GetStudentProgressAsync(id);
                var ladderTask     = _api.GetModuleLadderAsync(id);
                var gamesTask      = _api.GetGameResultsAsync(id);
                var badgesTask     = _api.GetBadgesAsync(id);

                await Task.WhenAll(profileTask, placementTask, progressTask,
                                   ladderTask, gamesTask, badgesTask);

                ViewBag.ProfileJson    = JsonConvert.SerializeObject(profileTask.Result);
                ViewBag.PlacementJson  = JsonConvert.SerializeObject(placementTask.Result);
                ViewBag.ProgressJson   = JsonConvert.SerializeObject(progressTask.Result);
                ViewBag.LadderJson     = JsonConvert.SerializeObject(ladderTask.Result);
                ViewBag.GamesJson      = JsonConvert.SerializeObject(gamesTask.Result);
                ViewBag.BadgesJson     = JsonConvert.SerializeObject(badgesTask.Result);
                ViewBag.StudentId      = id;
                ViewBag.UserName       = Session["UserName"]?.ToString() ?? "Admin";
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
            }

            return View("Details");
        }

        // =========================================================================
        // ExportCsv — Download filtered student list as CSV
        // =========================================================================
        [HttpGet]
        public async Task<ActionResult> ExportCsv()
        {
            List<dynamic> students;
            try
            {
                students = await _api.GetStudentsAsync() ?? new List<dynamic>();
            }
            catch
            {
                students = new List<dynamic>();
            }

            var csv = new System.Text.StringBuilder();
            csv.AppendLine("Name,Grade,School,Barangay,Level,Pre-Theta,Post-Theta,XP,LessonsDone,LastActive,Status");

            foreach (var s in students)
            {
                string level = ThetaToLevel((string)s.pre_theta);
                csv.AppendLine(string.Join(",",
                    CsvCell((string)s.FullName),
                    CsvCell((string)s.grade),
                    CsvCell((string)s.school_name),
                    CsvCell((string)s.barangay),
                    CsvCell(level),
                    CsvCell((string)s.pre_theta),
                    CsvCell((string)s.post_theta),
                    CsvCell((string)s.total_xp),
                    CsvCell((string)s.lessons_done),
                    CsvCell((string)s.last_active),
                    CsvCell((string)s.status)
                ));
            }

            var bytes = System.Text.Encoding.UTF8.GetBytes(csv.ToString());
            return File(bytes, "text/csv", $"students_{DateTime.Now:yyyyMMdd}.csv");
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
