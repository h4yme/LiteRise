using System;
using System.Collections.Generic;
using System.Web.Mvc;
using Website.Filters;

namespace Website.Controllers
{
    [AuthFilter]
    public class NotificationsController : Controller
    {
        // ─────────────────────────────────────────────────────────────────────
        // Index – loads the Notifications shell view
        // ─────────────────────────────────────────────────────────────────────
        public ActionResult Index()
        {
            return View("NotificationsView");
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetNotifications – returns sample notification history
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetNotifications()
        {
            var notifications = new List<object>
            {
                new {
                    id             = 1,
                    title          = "Welcome Back, Students!",
                    target         = "All",
                    type           = "Info",
                    sentAt         = "2025-03-10T08:00:00",
                    status         = "Sent",
                    recipientCount = 1240
                },
                new {
                    id             = 2,
                    title          = "Outstanding Achievement Award",
                    target         = "BySchool",
                    schoolId       = 3,
                    schoolName     = "Mapayapa Elementary",
                    type           = "Achievement",
                    sentAt         = "2025-03-09T14:30:00",
                    status         = "Sent",
                    recipientCount = 320
                },
                new {
                    id             = 3,
                    title          = "Upcoming Post-Assessment Reminder",
                    target         = "All",
                    type           = "Reminder",
                    sentAt         = "2025-03-08T09:00:00",
                    status         = "Sent",
                    recipientCount = 1240
                },
                new {
                    id             = 4,
                    title          = "Grade 3 Weekly Challenge",
                    target         = "ByGrade",
                    gradeLevel     = 3,
                    type           = "Info",
                    sentAt         = "2025-03-07T10:00:00",
                    status         = "Sent",
                    recipientCount = 215
                },
                new {
                    id             = 5,
                    title          = "End-of-Term Assessment Notice",
                    target         = "All",
                    type           = "Reminder",
                    sentAt         = "2025-03-15T07:00:00",
                    status         = "Scheduled",
                    recipientCount = 1240
                },
                new {
                    id             = 6,
                    title          = "Top Scorers This Month",
                    target         = "BySchool",
                    schoolId       = 1,
                    schoolName     = "Bagong Pag-asa Elementary",
                    type           = "Achievement",
                    sentAt         = "2025-03-06T11:15:00",
                    status         = "Sent",
                    recipientCount = 410
                }
            };

            return Json(new { success = true, notifications }, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // SendNotification – validates and queues a push notification
        // Note: Integrate with a push notification API (e.g. Firebase FCM,
        //       OneSignal, or a custom PHP endpoint) to deliver messages.
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult SendNotification(NotificationModel model)
        {
            if (model == null)
                return Json(new { success = false, error = "No data received." });

            if (string.IsNullOrWhiteSpace(model.Title))
                return Json(new { success = false, error = "Title is required." });

            if (string.IsNullOrWhiteSpace(model.Body))
                return Json(new { success = false, error = "Message body is required." });

            if (model.Body.Length > 500)
                return Json(new { success = false, error = "Message body must not exceed 500 characters." });

            if (model.Target == "BySchool" && (model.SchoolId == null || model.SchoolId <= 0))
                return Json(new { success = false, error = "Please select a school." });

            if (model.Target == "ByGrade" && (model.GradeLevel == null || model.GradeLevel < 1 || model.GradeLevel > 6))
                return Json(new { success = false, error = "Please select a valid grade level (1–6)." });

            if (!model.SendNow && model.ScheduledFor == null)
                return Json(new { success = false, error = "Please provide a scheduled date/time." });

            if (!model.SendNow && model.ScheduledFor.HasValue && model.ScheduledFor.Value <= DateTime.Now)
                return Json(new { success = false, error = "Scheduled time must be in the future." });

            // TODO: Call push notification API here.
            // Example: await _pushService.SendAsync(model.Title, model.Body, model.Target, ...);

            return Json(new
            {
                success   = true,
                message   = model.SendNow
                                ? "Notification sent successfully."
                                : $"Notification scheduled for {model.ScheduledFor:MMM d, yyyy h:mm tt}.",
                notificationId = new Random().Next(1000, 9999)
            });
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetTemplates – returns quick-compose notification templates
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetTemplates()
        {
            var templates = new List<object>
            {
                new {
                    id    = 1,
                    label = "Weekly Reminder",
                    title = "Don't Forget Your Weekly Lessons!",
                    body  = "Hi! Your weekly lessons are waiting for you in LiteRise. Keep up the great work and stay on track this week!"
                },
                new {
                    id    = 2,
                    label = "Achievement Shoutout",
                    title = "Congratulations on Your Achievement!",
                    body  = "Amazing work! You've earned a new achievement in LiteRise. Keep pushing forward and reach for the next milestone!"
                },
                new {
                    id    = 3,
                    label = "Assessment Notice",
                    title = "Upcoming Assessment — Be Prepared!",
                    body  = "A post-assessment is coming up soon. Make sure you've completed all your lessons and practice activities. You've got this!"
                }
            };

            return Json(new { success = true, templates }, JsonRequestBehavior.AllowGet);
        }

        // ─────────────────────────────────────────────────────────────────────
        // Inner model classes
        // ─────────────────────────────────────────────────────────────────────
        public class NotificationModel
        {
            public string   Title        { get; set; }
            public string   Body         { get; set; }
            /// <summary>All | BySchool | ByGrade</summary>
            public string   Target       { get; set; }
            public int?     SchoolId     { get; set; }
            public int?     GradeLevel   { get; set; }
            /// <summary>Info | Achievement | Reminder</summary>
            public string   Type         { get; set; }
            public DateTime? ScheduledFor { get; set; }
            public bool     SendNow      { get; set; }
        }
    }
}
