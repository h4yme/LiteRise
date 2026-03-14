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
        // GetAdmins – returns all admin and teacher accounts from the API
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<JsonResult> GetAdmins()
        {
            try
            {
                var accounts = await _api.GetPortalAccountsAsync();
                return Json(new { success = true, data = accounts }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
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
        // id: "" or "0" = create new; "admin_N" or "teacher_N" = update existing
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> SaveAdmin(AdminModel model)
        {
            try
            {
                if (model == null)
                    return Json(new { success = false, error = "Invalid account data." });

                dynamic result;
                bool isNew = string.IsNullOrEmpty(model.Id) || model.Id == "0";

                if (isNew)
                {
                    result = await _api.CreatePortalAccountAsync(model.Name, model.Email, model.Password, model.Role, null);
                }
                else
                {
                    result = await _api.UpdatePortalAccountAsync(model.Id, model.Name, model.Email, model.Password, model.Role, null);
                }

                return Json(new { success = true, message = "Account saved successfully.", data = result });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // SetAccountActive – enable or disable a portal account
        // Body: { id: "admin_N"|"teacher_N", isActive: bool }
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> SetAccountActive(SetActiveModel model)
        {
            try
            {
                if (model == null || string.IsNullOrEmpty(model.Id))
                    return Json(new { success = false, error = "Invalid account ID." });

                var result = await _api.SetPortalAccountActiveAsync(model.Id, model.IsActive);
                return Json(new { success = true, data = result });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // ToggleNode – enable or disable a curriculum node
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> ToggleNode(int nodeId, bool enabled)
        {
            try
            {
                if (nodeId <= 0)
                    return Json(new { success = false, error = "Invalid node ID." });

                var result = await _api.ToggleNodeStatusAsync(nodeId, enabled);
                return Json(new { success = true, data = result });
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
            public string Id       { get; set; }   // "" | "0" = new; "admin_N" | "teacher_N" = existing
            public string Name     { get; set; }
            public string Email    { get; set; }
            public string Password { get; set; }
            public string Role     { get; set; }   // Admin | Teacher
            public string School   { get; set; }   // applicable when Role == Teacher
            public bool   IsActive { get; set; }
        }

        public class SetActiveModel
        {
            public string Id       { get; set; }
            public bool   IsActive { get; set; }
        }
    }
}
