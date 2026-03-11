using System;
using System.Linq;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;

namespace Website.Controllers
{
    [AuthFilter]
    public class DashboardController : Controller
    {
        private readonly ApiService _api = new ApiService();

        public async Task<ActionResult> Index()
        {
            // Admin only
            if (Session["UserRole"]?.ToString() == "teacher")
                return RedirectToAction("Index", "TeacherDashboard");

            try
            {
                var studentsTask = _api.GetStudentsAsync();
                var schoolsTask  = _api.GetSchoolsAsync();
                await Task.WhenAll(studentsTask, schoolsTask);

                var students = studentsTask.Result;
                var schools  = schoolsTask.Result;

                ViewBag.TotalStudents  = students.Count;
                ViewBag.TotalSchools   = schools.Count;
                ViewBag.ActiveStudents = students.Count(s => s["status"]?.ToString() == "active");
                ViewBag.AvgXP          = students.Any()
                    ? Math.Round(students.Average(s => (double)(s["total_xp"] ?? 0)), 0)
                    : 0;

                ViewBag.Students = students;
                ViewBag.Schools  = schools;
            }
            catch (Exception ex)
            {
                ViewBag.Error = ex.Message;
                ViewBag.TotalStudents  = 0;
                ViewBag.TotalSchools   = 0;
                ViewBag.ActiveStudents = 0;
                ViewBag.AvgXP          = 0;
            }

            ViewBag.Title = "Dashboard";
            return View();
        }
    }
}
