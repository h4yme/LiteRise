using System;
using System.Collections.Generic;
using System.Web.Mvc;
using Website.Filters;

namespace Website.Controllers
{
    [AuthFilter]
    [AuthorizeAdmin]
    public class SettingsController : Controller
    {
        // ─────────────────────────────────────────────────────────────────────
        // Index – loads the Settings shell view
        // ─────────────────────────────────────────────────────────────────────
        public ActionResult Index()
        {
            ViewBag.UserName = Session["UserName"]?.ToString() ?? "Admin";
            return View("SettingsView");
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetProfile – returns current admin profile data
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetProfile()
        {
            var userName = Session["UserName"]?.ToString() ?? "Administrator";
            var role     = Session["UserRole"]?.ToString() ?? "admin";

            var user = new
            {
                name      = userName,
                email     = "admin@literise.edu.ph",
                role      = role,
                lastLogin = DateTime.Now.AddHours(-2).ToString("MMM d, yyyy h:mm tt")
            };

            return Json(new { success = true, user }, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // UpdateProfile – saves admin profile changes
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult UpdateProfile(ProfileModel model)
        {
            if (model == null)
                return Json(new { success = false, message = "No data received." });

            if (string.IsNullOrWhiteSpace(model.Name))
                return Json(new { success = false, message = "Full name is required." });

            if (model.Name.Trim().Length < 2)
                return Json(new { success = false, message = "Name must be at least 2 characters." });

            if (string.IsNullOrWhiteSpace(model.Email))
                return Json(new { success = false, message = "Email address is required." });

            if (!IsValidEmail(model.Email))
                return Json(new { success = false, message = "Please enter a valid email address." });

            // Update session display name
            Session["UserName"] = model.Name.Trim();

            // TODO: Persist to API — await _api.UpdateAdminProfileAsync(model);

            return Json(new { success = true, message = "Profile updated successfully." });
        }

        // ─────────────────────────────────────────────────────────────────────
        // ChangePassword – validates and processes password change
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult ChangePassword(PasswordModel model)
        {
            if (model == null)
                return Json(new { success = false, message = "No data received." });

            if (string.IsNullOrWhiteSpace(model.CurrentPassword))
                return Json(new { success = false, message = "Current password is required." });

            if (string.IsNullOrWhiteSpace(model.NewPassword))
                return Json(new { success = false, message = "New password is required." });

            if (model.NewPassword.Length < 8)
                return Json(new { success = false, message = "New password must be at least 8 characters." });

            if (string.IsNullOrWhiteSpace(model.ConfirmPassword))
                return Json(new { success = false, message = "Please confirm your new password." });

            if (model.NewPassword != model.ConfirmPassword)
                return Json(new { success = false, message = "New password and confirmation do not match." });

            if (model.CurrentPassword == model.NewPassword)
                return Json(new { success = false, message = "New password must be different from the current password." });

            // TODO: Verify current password and persist change via API.
            // await _api.ChangeAdminPasswordAsync(model.CurrentPassword, model.NewPassword);

            return Json(new { success = true, message = "Password changed successfully." });
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetSystemConfig – returns current system configuration values
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetSystemConfig()
        {
            var config = new
            {
                passingThreshold               = 70,
                maxLessonsBeforePostAssessment = 65,
                irtMinItems                    = 10,
                irtMaxItems                    = 30,
                irtSemTarget                   = 0.3,
                enableGameModule               = true,
                enableBadges                   = true,
                maintenanceMode                = false
            };

            return Json(new { success = true, config }, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // UpdateSystemConfig – validates and saves system configuration
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult UpdateSystemConfig(SystemConfigModel model)
        {
            if (model == null)
                return Json(new { success = false, message = "No configuration data received." });

            if (model.PassingThreshold < 0 || model.PassingThreshold > 100)
                return Json(new { success = false, message = "Passing threshold must be between 0 and 100." });

            if (model.MaxLessonsBeforePostAssessment < 1)
                return Json(new { success = false, message = "Max lessons before post-assessment must be at least 1." });

            if (model.IrtMinItems < 1)
                return Json(new { success = false, message = "IRT minimum items must be at least 1." });

            if (model.IrtMaxItems < model.IrtMinItems)
                return Json(new { success = false, message = "IRT maximum items must be greater than or equal to minimum items." });

            if (model.IrtMaxItems > 200)
                return Json(new { success = false, message = "IRT maximum items cannot exceed 200." });

            if (model.IrtSemTarget <= 0 || model.IrtSemTarget > 2.0)
                return Json(new { success = false, message = "IRT SEM target must be between 0.01 and 2.0." });

            // TODO: Persist to API — await _api.UpdateSystemConfigAsync(model);

            var scope = "configuration";
            if (model.IrtMinItems > 0 && !model.PassingThreshold.Equals(0))
                scope = "system configuration and IRT parameters";

            return Json(new { success = true, message = $"System {scope} saved successfully." });
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetAuditLog – returns recent admin activity log entries
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetAuditLog()
        {
            var log = new List<object>
            {
                new {
                    id         = 1,
                    action     = "login",
                    adminName  = Session["UserName"]?.ToString() ?? "Administrator",
                    target     = "System",
                    timestamp  = DateTime.Now.AddMinutes(-30).ToString("MMM d, yyyy h:mm tt"),
                    ipAddress  = "192.168.1.50"
                },
                new {
                    id         = 2,
                    action     = "edit",
                    adminName  = Session["UserName"]?.ToString() ?? "Administrator",
                    target     = "System Config — Passing Threshold changed to 70%",
                    timestamp  = DateTime.Now.AddHours(-1).ToString("MMM d, yyyy h:mm tt"),
                    ipAddress  = "192.168.1.50"
                },
                new {
                    id         = 3,
                    action     = "create",
                    adminName  = "SuperAdmin",
                    target     = "User: teacher_santos@literise.edu.ph",
                    timestamp  = DateTime.Now.AddHours(-3).ToString("MMM d, yyyy h:mm tt"),
                    ipAddress  = "192.168.1.12"
                },
                new {
                    id         = 4,
                    action     = "export",
                    adminName  = Session["UserName"]?.ToString() ?? "Administrator",
                    target     = "Analytics Report — All Schools",
                    timestamp  = DateTime.Now.AddHours(-5).ToString("MMM d, yyyy h:mm tt"),
                    ipAddress  = "192.168.1.50"
                },
                new {
                    id         = 5,
                    action     = "delete",
                    adminName  = "SuperAdmin",
                    target     = "Notification #18 (Scheduled)",
                    timestamp  = DateTime.Now.AddDays(-1).ToString("MMM d, yyyy h:mm tt"),
                    ipAddress  = "192.168.1.12"
                },
                new {
                    id         = 6,
                    action     = "edit",
                    adminName  = "SuperAdmin",
                    target     = "IRT Parameters — SEM Target updated to 0.30",
                    timestamp  = DateTime.Now.AddDays(-1).AddHours(-2).ToString("MMM d, yyyy h:mm tt"),
                    ipAddress  = "192.168.1.12"
                }
            };

            return Json(new { success = true, log }, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // Helper – basic email format validation
        // ─────────────────────────────────────────────────────────────────────
        private bool IsValidEmail(string email)
        {
            if (string.IsNullOrWhiteSpace(email)) return false;
            try
            {
                var addr = new System.Net.Mail.MailAddress(email);
                return addr.Address == email.Trim();
            }
            catch
            {
                return false;
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // Inner model classes
        // ─────────────────────────────────────────────────────────────────────
        public class ProfileModel
        {
            public string Name  { get; set; }
            public string Email { get; set; }
        }

        public class PasswordModel
        {
            public string CurrentPassword { get; set; }
            public string NewPassword     { get; set; }
            public string ConfirmPassword { get; set; }
        }

        public class SystemConfigModel
        {
            public int    PassingThreshold               { get; set; }
            public int    MaxLessonsBeforePostAssessment { get; set; }
            public int    IrtMinItems                    { get; set; }
            public int    IrtMaxItems                    { get; set; }
            public double IrtSemTarget                   { get; set; }
            public bool   EnableGameModule               { get; set; }
            public bool   EnableBadges                   { get; set; }
            public bool   MaintenanceMode                { get; set; }
        }
    }
}
