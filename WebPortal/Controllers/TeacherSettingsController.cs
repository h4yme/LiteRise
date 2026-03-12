using System;
using System.Collections.Generic;
using System.Web.Mvc;
using Website.Filters;

namespace Website.Controllers
{
    [AuthFilter]
    public class TeacherSettingsController : Controller
    {
        // ─────────────────────────────────────────────────────────────────────
        // Index – loads the Teacher Settings shell view
        // ─────────────────────────────────────────────────────────────────────
        public ActionResult Index()
        {
            ViewBag.UserName = Session["UserName"]?.ToString() ?? "Teacher";
            return View("Index");
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetProfile – returns current teacher profile data
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetProfile()
        {
            var userName = Session["UserName"]?.ToString() ?? "Teacher";
            var schoolId = Session["SchoolId"]?.ToString() ?? "1";

            var user = new
            {
                name      = userName,
                email     = "teacher@literise.edu.ph",
                school    = "Bagong Pag-asa Elementary School",
                grade     = "Grade 4",
                lastLogin = DateTime.Now.AddHours(-1).ToString("MMM d, yyyy h:mm tt")
            };

            return Json(new { success = true, user }, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // UpdateProfile – saves teacher profile changes
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

            // TODO: Persist to API — await _api.UpdateTeacherProfileAsync(model);

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
            // await _api.ChangeTeacherPasswordAsync(model.CurrentPassword, model.NewPassword);

            return Json(new { success = true, message = "Password changed successfully." });
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetAssignedStudents – returns students assigned to this teacher
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetAssignedStudents()
        {
            var students = new List<object>
            {
                new {
                    name       = "Maria Santos",
                    grade      = "Grade 4",
                    level      = "Intermediate",
                    lastActive = DateTime.Now.AddHours(-2).ToString("MMM d, yyyy")
                },
                new {
                    name       = "Juan dela Cruz",
                    grade      = "Grade 4",
                    level      = "Beginner",
                    lastActive = DateTime.Now.AddDays(-1).ToString("MMM d, yyyy")
                },
                new {
                    name       = "Ana Reyes",
                    grade      = "Grade 4",
                    level      = "Advanced",
                    lastActive = DateTime.Now.ToString("MMM d, yyyy")
                },
                new {
                    name       = "Carlos Mendoza",
                    grade      = "Grade 4",
                    level      = "Intermediate",
                    lastActive = DateTime.Now.AddDays(-3).ToString("MMM d, yyyy")
                },
                new {
                    name       = "Liza Garcia",
                    grade      = "Grade 4",
                    level      = "Beginner",
                    lastActive = DateTime.Now.AddDays(-2).ToString("MMM d, yyyy")
                }
            };

            return Json(new { success = true, students }, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // RequestStudentChange – submits a student add/remove request to admin
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult RequestStudentChange(StudentChangeRequestModel model)
        {
            if (model == null)
                return Json(new { success = false, message = "No request data received." });

            if (string.IsNullOrWhiteSpace(model.StudentName))
                return Json(new { success = false, message = "Student name is required." });

            if (string.IsNullOrWhiteSpace(model.Action))
                return Json(new { success = false, message = "Please select an action (Add or Remove)." });

            if (model.Action != "Add" && model.Action != "Remove")
                return Json(new { success = false, message = "Invalid action. Must be 'Add' or 'Remove'." });

            if (string.IsNullOrWhiteSpace(model.Reason))
                return Json(new { success = false, message = "A reason is required for the request." });

            if (model.Reason.Trim().Length < 10)
                return Json(new { success = false, message = "Please provide a more detailed reason (at least 10 characters)." });

            // TODO: Submit to admin via API — await _api.SubmitStudentChangeRequestAsync(model);

            return Json(new { success = true, message = "Request submitted to admin." });
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

        public class StudentChangeRequestModel
        {
            public string StudentName { get; set; }
            /// <summary>Add | Remove</summary>
            public string Action      { get; set; }
            public string Reason      { get; set; }
        }
    }
}
