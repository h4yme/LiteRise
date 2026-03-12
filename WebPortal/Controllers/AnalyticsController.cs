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
    public class AnalyticsController : AsyncController
    {
        // ─────────────────────────────────────────────────────────────────────
        // Index – loads the Analytics shell view
        // ─────────────────────────────────────────────────────────────────────
        public async Task<ActionResult> Index()
        {
            var _api = new ApiService(Session["AuthToken"]?.ToString());

            List<dynamic> students;
            try
            {
                students = await _api.GetAllStudentsAsync();
            }
            catch (Exception ex)
            {
                ViewBag.Error        = "Unable to load student data: " + ex.Message;
                ViewBag.StudentsJson = "[]";
                students             = new List<dynamic>();
            }

            // ── Raw JSON for client-side use ──────────────────────────────────
            ViewBag.StudentsJson = JsonConvert.SerializeObject(students);

            // ── Aggregate totals ──────────────────────────────────────────────
            int total     = students.Count;
            int preCount  = students.Count(s => s.pre_theta  != null);
            int postCount = students.Count(s => s.post_theta != null);

            // Level counts (placement_level field)
            int beginnerCount     = students.Count(s => (string)s.placement_level == "beginner");
            int intermediateCount = students.Count(s => (string)s.placement_level == "intermediate");
            int advancedCount     = students.Count(s => (string)s.placement_level == "advanced");

            // Average pre / post theta (only over students that have values)
            double avgPreTheta  = 0;
            double avgPostTheta = 0;
            double avgGrowth    = 0;

            var preStudents  = students.Where(s => s.pre_theta  != null).ToList();
            var postStudents = students.Where(s => s.post_theta != null).ToList();
            var bothStudents = students.Where(s => s.pre_theta  != null && s.post_theta != null).ToList();

            if (preStudents.Count  > 0) avgPreTheta  = preStudents .Average(s => (double)s.pre_theta);
            if (postStudents.Count > 0) avgPostTheta = postStudents.Average(s => (double)s.post_theta);
            if (bothStudents.Count > 0) avgGrowth    = bothStudents.Average(s => (double)s.post_theta - (double)s.pre_theta);

            // ── ViewBag assignments ───────────────────────────────────────────
            ViewBag.TotalStudents      = total;
            ViewBag.PreAssessmentCount = preCount;
            ViewBag.PostAssessmentCount= postCount;
            ViewBag.BeginnerCount      = beginnerCount;
            ViewBag.IntermediateCount  = intermediateCount;
            ViewBag.AdvancedCount      = advancedCount;
            ViewBag.AvgPreTheta        = Math.Round(avgPreTheta,  3);
            ViewBag.AvgPostTheta       = Math.Round(avgPostTheta, 3);
            ViewBag.AvgGrowth          = Math.Round(avgGrowth,    3);

            return View("AnalyticsView");
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetAssessmentData – JSON endpoint for Assessment tab charts
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<JsonResult> GetAssessmentData()
        {
            var _api = new ApiService(Session["AuthToken"]?.ToString());

            List<dynamic> students;
            try
            {
                students = await _api.GetAllStudentsAsync();
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }

            // ── Theta histogram buckets ───────────────────────────────────────
            // Ranges: [-3,-2) [-2,-1) [-1,-0.5) [-0.5,0) [0,0.5) [0.5,1) [1,2) [2,3]
            var bucketLabels = new[] { "-3 to -2", "-2 to -1", "-1 to -0.5", "-0.5 to 0",
                                       "0 to 0.5",  "0.5 to 1",  "1 to 2",   "2 to 3" };
            var bucketCounts = new int[8];

            foreach (var s in students)
            {
                if (s.pre_theta == null) continue;
                double theta = (double)s.pre_theta;

                if      (theta >= -3   && theta < -2  ) bucketCounts[0]++;
                else if (theta >= -2   && theta < -1  ) bucketCounts[1]++;
                else if (theta >= -1   && theta < -0.5) bucketCounts[2]++;
                else if (theta >= -0.5 && theta <  0  ) bucketCounts[3]++;
                else if (theta >=  0   && theta <  0.5) bucketCounts[4]++;
                else if (theta >=  0.5 && theta <  1  ) bucketCounts[5]++;
                else if (theta >=  1   && theta <  2  ) bucketCounts[6]++;
                else if (theta >=  2   && theta <= 3  ) bucketCounts[7]++;
            }

            // ── Level distribution ────────────────────────────────────────────
            int begCount  = students.Count(s => (string)s.placement_level == "beginner");
            int intCount  = students.Count(s => (string)s.placement_level == "intermediate");
            int advCount  = students.Count(s => (string)s.placement_level == "advanced");
            int nullCount = students.Count(s => s.placement_level == null);

            // ── Pre vs Post theta per school ──────────────────────────────────
            var schoolGroups = students
                .GroupBy(s => (string)s.school_name ?? "Unknown")
                .Select(g => new
                {
                    school      = g.Key,
                    avgPre      = g.Where(s => s.pre_theta  != null).Any()
                                    ? Math.Round(g.Where(s => s.pre_theta  != null).Average(s => (double)s.pre_theta),  3)
                                    : (double?)null,
                    avgPost     = g.Where(s => s.post_theta != null).Any()
                                    ? Math.Round(g.Where(s => s.post_theta != null).Average(s => (double)s.post_theta), 3)
                                    : (double?)null,
                    studentCount= g.Count()
                })
                .OrderByDescending(g => g.studentCount)
                .ToList();

            var result = new
            {
                success       = true,
                thetaHistogram = new
                {
                    labels = bucketLabels,
                    counts = bucketCounts
                },
                levelDistribution = new
                {
                    beginner     = begCount,
                    intermediate = intCount,
                    advanced     = advCount,
                    notTaken     = nullCount
                },
                schoolTheta = schoolGroups
            };

            return Json(result, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetGameData – JSON endpoint for Games tab charts
        // Note: A dedicated aggregate game endpoint (get_game_summary.php) is
        //       required for live data. This action returns a structured sample
        //       until that endpoint is available.
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetGameData()
        {
            var gameTypes = new[]
            {
                "SentenceScramble", "StorySequencing", "FillInTheBlanks",
                "PictureMatch",     "DialogueReading", "WordHunt",
                "TimedTrail",       "MinimalPairs",    "SynonymSprint",
                "WordExplosion",    "PhonicsNinja"
            };

            // Sample play counts per game type
            var samplePlayCounts = new[] { 420, 310, 580, 390, 270, 490, 360, 225, 310, 460, 340 };

            // Sample average accuracy (0–100 %)
            var sampleAccuracy   = new[] { 72.4, 68.1, 75.8, 81.2, 65.3, 70.5, 77.9, 63.7, 74.2, 79.1, 66.8 };

            // Sample XP earned per week (last 8 weeks) aggregated across all games
            var weekLabels = Enumerable.Range(1, 8).Select(w => $"Week {w}").ToArray();
            var sampleXpPerWeek = new[] { 1200, 1850, 2100, 1750, 2400, 2650, 2300, 2800 };

            var result = new
            {
                success  = true,
                note     = "Sample data. Integrate get_game_summary.php endpoint to replace with live aggregates.",
                gameTypes,
                playCountByGame = gameTypes.Zip(samplePlayCounts, (g, c) => new { game = g, count = c }),
                avgAccuracyByGame = gameTypes.Zip(sampleAccuracy,  (g, a) => new { game = g, accuracy = a }),
                xpOverTime = new
                {
                    labels   = weekLabels,
                    xpTotals = sampleXpPerWeek
                }
            };

            return Json(result, JsonRequestBehavior.AllowGet);
        }
    }
}
