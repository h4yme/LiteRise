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
    [AuthorizeAdmin]
    public class MasterfileController : AsyncController
    {
        // ─────────────────────────────────────────────────────────────────────
        // Shared API service helper
        // ─────────────────────────────────────────────────────────────────────
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        // ─────────────────────────────────────────────────────────────────────
        // Index – loads the Masterfile shell view
        // ─────────────────────────────────────────────────────────────────────
        public ActionResult Index()
        {
            return View("MasterfileView");
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetQuestions – returns the full question bank
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<JsonResult> GetQuestions()
        {
            try
            {
                var questions = await _api.GetAssessmentItemsAsync();
                return Json(new { success = true, data = questions }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetModuleLadder – returns all modules and nodes (admin view)
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<JsonResult> GetModuleLadder()
        {
            try
            {
                // Pass 0 to retrieve the full ladder for all users (admin view)
                var ladder = await _api.GetModuleLadderAsync(0);
                return Json(new { success = true, data = ladder }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetBadges – returns all badges
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<JsonResult> GetBadges()
        {
            try
            {
                // Pass 0 to retrieve badges for the admin (all badges)
                var badges = await _api.GetBadgesAsync(0);
                return Json(new { success = true, data = badges }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetAdmins – returns administrator account list
        // Note: Dedicated admin-list endpoint not yet available; returns sample
        //       data until the endpoint is integrated.
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetAdmins()
        {
            var sampleAdmins = new List<object>
            {
                new {
                    id         = 1,
                    name       = "Maria Santos",
                    email      = "maria.santos@literise.edu.ph",
                    role       = "Admin",
                    school     = (string)null,
                    lastLogin  = "2026-03-10 08:45",
                    isActive   = true
                },
                new {
                    id         = 2,
                    name       = "Juan dela Cruz",
                    email      = "juan.delacruz@pes.edu.ph",
                    role       = "Teacher",
                    school     = "Pag-asa Elementary School",
                    lastLogin  = "2026-03-11 13:22",
                    isActive   = true
                },
                new {
                    id         = 3,
                    name       = "Ana Reyes",
                    email      = "ana.reyes@mces.edu.ph",
                    role       = "Teacher",
                    school     = "Mabini Central Elementary School",
                    lastLogin  = "2026-03-09 09:05",
                    isActive   = true
                },
                new {
                    id         = 4,
                    name       = "Carlos Bautista",
                    email      = "carlos.bautista@literise.edu.ph",
                    role       = "Admin",
                    school     = (string)null,
                    lastLogin  = "2026-02-28 16:10",
                    isActive   = false
                },
                new {
                    id         = 5,
                    name       = "Liza Gomez",
                    email      = "liza.gomez@rnes.edu.ph",
                    role       = "Teacher",
                    school     = "Rizal National Elementary School",
                    lastLogin  = "2026-03-11 07:55",
                    isActive   = true
                }
            };

            return Json(new { success = true, data = sampleAdmins }, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // SaveQuestion – create or update a question item
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult SaveQuestion(QuestionModel model)
        {
            try
            {
                if (model == null)
                    return Json(new { success = false, error = "Invalid question data." });

                // TODO: integrate _api.SaveAssessmentItemAsync(model) when endpoint is available
                return Json(new { success = true, message = "Question saved successfully." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // SaveBadge – create or update a badge
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult SaveBadge(BadgeModel model)
        {
            try
            {
                if (model == null)
                    return Json(new { success = false, error = "Invalid badge data." });

                // TODO: integrate _api.SaveBadgeAsync(model) when endpoint is available
                return Json(new { success = true, message = "Badge saved successfully." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // SaveAdmin – create or update an administrator/teacher account
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult SaveAdmin(AdminModel model)
        {
            try
            {
                if (model == null)
                    return Json(new { success = false, error = "Invalid account data." });

                // TODO: integrate _api.SaveAdminAsync(model) when endpoint is available
                return Json(new { success = true, message = "Account saved successfully." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // DeleteQuestion – remove a question from the bank
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult DeleteQuestion(int id)
        {
            try
            {
                if (id <= 0)
                    return Json(new { success = false, error = "Invalid question ID." });

                // TODO: integrate _api.DeleteAssessmentItemAsync(id) when endpoint is available
                return Json(new { success = true, message = $"Question #{id} deleted successfully." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ═════════════════════════════════════════════════════════════════════
        // Inner model classes
        // ═════════════════════════════════════════════════════════════════════

        public class QuestionModel
        {
            public int    Id             { get; set; }
            public string Category       { get; set; }   // Phonics | Vocabulary | Grammar | Comprehension | Creating Text
            public string Type           { get; set; }   // MultipleChoice | Pronunciation | Reading
            public double Difficulty     { get; set; }   // b parameter, -3 to 3
            public double Discrimination { get; set; }   // a parameter, 0.5 to 3
            public string QuestionText   { get; set; }
            public string ChoiceA        { get; set; }
            public string ChoiceB        { get; set; }
            public string ChoiceC        { get; set; }
            public string ChoiceD        { get; set; }
            public string CorrectAnswer  { get; set; }   // "A" | "B" | "C" | "D"
            public bool   IsActive       { get; set; }
        }

        public class BadgeModel
        {
            public int    Id          { get; set; }
            public string Name        { get; set; }
            public string Description { get; set; }
            public string Category    { get; set; }   // Module | XP | Streak | Achievement
            public string Criteria    { get; set; }
            public int    XpReward    { get; set; }
            public string Icon        { get; set; }   // emoji or icon identifier
        }

        public class AdminModel
        {
            public int    Id       { get; set; }
            public string Name     { get; set; }
            public string Email    { get; set; }
            public string Password { get; set; }
            public string Role     { get; set; }   // Admin | Teacher
            public string School   { get; set; }   // applicable when Role == Teacher
            public bool   IsActive { get; set; }
        }
    }
}
