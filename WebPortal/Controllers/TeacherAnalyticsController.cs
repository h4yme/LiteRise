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
    [AuthFilter]
    public class TeacherAnalyticsController : AsyncController
    {
        // ─────────────────────────────────────────────────────────────────────
        // Shared API service helper – scoped to teacher's auth token
        // ─────────────────────────────────────────────────────────────────────
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        // IRT placement thresholds
        private const double BegInterBoundary = -0.5;
        private const double InterAdvBoundary =  0.5;

        // ─────────────────────────────────────────────────────────────────────
        // Index – loads Class Analytics shell view
        // ─────────────────────────────────────────────────────────────────────
        public async Task<ActionResult> Index()
        {
            int? schoolId = int.TryParse(Session["SchoolId"]?.ToString(), out int sid) ? sid : (int?)null;

            List<dynamic> students;
            try
            {
                students = await _api.GetAllStudentsAsync(schoolId);
            }
            catch (Exception ex)
            {
                ViewBag.Error        = "Unable to load student data: " + ex.Message;
                ViewBag.StudentsJson = "[]";
                students             = new List<dynamic>();
            }

            // ── Raw JSON for client-side charts ───────────────────────────────
            ViewBag.StudentsJson = JsonConvert.SerializeObject(students);

            // ── Aggregate totals ──────────────────────────────────────────────
            int total             = 0;
            int preCount          = 0;
            int postCount         = 0;
            int beginnerCount     = 0;
            int intermediateCount = 0;
            int advancedCount     = 0;
            double avgPreTheta    = 0;
            double avgPostTheta   = 0;
            double avgGrowth      = 0;
            double avgLessonsDone = 0;
            double lessonCompletionRate = 0;
            double avgStreak      = 0;
            int activeCount       = 0;
            double activePercent  = 0;
            double avgXp          = 0;
            string schoolName     = "My School";

            try
            {
                total     = students.Count;
                preCount  = students.Count(s => (double?)s.pre_theta  != null);
                postCount = students.Count(s => (double?)s.post_theta != null);

                // Level distribution from placement_level field
                beginnerCount     = students.Count(s => (string)s.placement_level == "beginner");
                intermediateCount = students.Count(s => (string)s.placement_level == "intermediate");
                advancedCount     = students.Count(s => (string)s.placement_level == "advanced");

                var preStudents  = students.Where(s => (double?)s.pre_theta  != null).ToList();
                var postStudents = students.Where(s => (double?)s.post_theta != null).ToList();
                var bothStudents = students.Where(s => (double?)s.pre_theta  != null && (double?)s.post_theta != null).ToList();

                if (preStudents.Count  > 0) avgPreTheta  = preStudents .Average(s => (double)s.pre_theta);
                if (postStudents.Count > 0) avgPostTheta = postStudents.Average(s => (double)s.post_theta);
                if (bothStudents.Count > 0) avgGrowth    = bothStudents.Average(s =>
                    (double)s.post_theta - (double)s.pre_theta);

                // Average lessons done
                avgLessonsDone = total > 0
                    ? students.Average(s => (double?)s.lessons_done != null ? (double)s.lessons_done : 0.0)
                    : 0;

                // Lesson completion rate (lessons_done / total modules = 13 assumed)
                const int totalModules = 13;
                lessonCompletionRate = avgLessonsDone > 0
                    ? Math.Min(Math.Round(avgLessonsDone / totalModules * 100, 1), 100)
                    : 0;

                // Engagement stats
                avgStreak = total > 0
                    ? students.Average(s => (double?)s.streak_days != null ? (double)s.streak_days : 0.0)
                    : 0;

                activeCount = students.Count(s =>
                {
                    if (s.last_active == null) return false;
                    if (DateTime.TryParse((string)s.last_active, out DateTime lastActive))
                        return (DateTime.UtcNow - lastActive).TotalDays <= 7;
                    return false;
                });

                activePercent = total > 0
                    ? Math.Round((double)activeCount / total * 100, 1)
                    : 0;

                avgXp = total > 0
                    ? students.Average(s => (double?)s.total_xp != null ? (double)s.total_xp : 0.0)
                    : 0;

                schoolName = students.Count > 0
                    ? (string)students[0].school_name ?? "My School"
                    : "My School";
            }
            catch
            {
                // Server-side stat cards default to 0; charts computed client-side from STUDENTS_DATA
            }

            // ── Category averages placeholder ─────────────────────────────────
            var categoryAverages = new
            {
                phonics        = 0,
                vocabulary     = 0,
                grammar        = 0,
                comprehension  = 0,
                creatingText   = 0,
                note           = "Category data loaded client-side via per-student fetch."
            };

            // ── ViewBag assignments ───────────────────────────────────────────
            ViewBag.TotalStudents         = total;
            ViewBag.PreAssessmentCount    = preCount;
            ViewBag.PostAssessmentCount   = postCount;
            ViewBag.BeginnerCount         = beginnerCount;
            ViewBag.IntermediateCount     = intermediateCount;
            ViewBag.AdvancedCount         = advancedCount;
            ViewBag.AvgPreTheta           = Math.Round(avgPreTheta,  3);
            ViewBag.AvgPostTheta          = Math.Round(avgPostTheta, 3);
            ViewBag.AvgGrowth             = Math.Round(avgGrowth,    3);
            ViewBag.AvgLessonsDone        = Math.Round(avgLessonsDone, 1);
            ViewBag.LessonCompletionRate  = lessonCompletionRate;
            ViewBag.CategoryAveragesJson  = JsonConvert.SerializeObject(categoryAverages);
            ViewBag.AvgStreak             = Math.Round(avgStreak, 1);
            ViewBag.ActiveCount           = activeCount;
            ViewBag.ActivePercent         = activePercent;
            ViewBag.AvgXp                 = Math.Round(avgXp, 0);
            ViewBag.SchoolName            = schoolName;
            ViewBag.UserName              = Session["UserName"]?.ToString() ?? "Teacher";

            return View("Index");
        }
    }
}
