using System;
using System.Linq;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;

namespace Website.Controllers
{
    [AuthFilter]
    public class TeacherDashboardController : Controller
    {
        private readonly ApiService _api = new ApiService();

        public async Task<ActionResult> Index()
        {
            // Teachers only — admins go to admin dashboard
            if (Session["UserRole"]?.ToString() == "admin")
                return RedirectToAction("Index", "Dashboard");

            try
            {
                int? schoolId = Session["SchoolId"] != null ? (int?)Session["SchoolId"] : null;
                var students  = await _api.GetStudentsAsync(schoolId);

                int total         = students.Count;
                int preCompleted  = students.Count(s => s["pre_theta"] != null);
                int postCompleted = students.Count(s => s["post_theta"] != null);
                int inactive7Days = students.Count(s => s["status"]?.ToString() == "inactive");

                double avgPreTheta  = preCompleted  > 0 ? Math.Round(students.Where(s => s["pre_theta"]  != null).Average(s => (double)s["pre_theta"]),  3) : 0;
                double avgPostTheta = postCompleted > 0 ? Math.Round(students.Where(s => s["post_theta"] != null).Average(s => (double)s["post_theta"]), 3) : 0;

                var avgLessons = total > 0
                    ? Math.Round(students.Average(s => (double)(s["lessons_done"] ?? 0)), 1)
                    : 0;

                ViewBag.Students        = students;
                ViewBag.Total           = total;
                ViewBag.PreCompleted    = preCompleted;
                ViewBag.PostCompleted   = postCompleted;
                ViewBag.Inactive7Days   = inactive7Days;
                ViewBag.AvgPreTheta     = avgPreTheta;
                ViewBag.AvgPostTheta    = avgPostTheta;
                ViewBag.AvgLessons      = avgLessons;
            }
            catch (Exception ex)
            {
                ViewBag.Error    = ex.Message;
                ViewBag.Students = new Newtonsoft.Json.Linq.JArray();
                ViewBag.Total    = 0;
            }

            ViewBag.Title = "Teacher Dashboard";
            return View();
        }
    }
}
