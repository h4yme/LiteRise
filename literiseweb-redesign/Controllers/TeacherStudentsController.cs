using System;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;

namespace Website.Controllers
{
    [AuthFilter]
    public class TeacherStudentsController : Controller
    {
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        // GET /TeacherStudents
        public async Task<ActionResult> Index()
        {
            try
            {
                int? schoolId = Session["SchoolId"] != null ? (int?)Session["SchoolId"] : null;
                var students  = await _api.GetStudentsAsync(schoolId);
                ViewBag.Students = students;
            }
            catch (Exception ex)
            {
                ViewBag.Error    = ex.Message;
                ViewBag.Students = new Newtonsoft.Json.Linq.JArray();
            }

            ViewBag.Title = "My Students";
            return View();
        }

        // GET /TeacherStudents/Details/5
        // Reuses the same data loading as admin — only the view/permissions differ
        public async Task<ActionResult> Details(int id)
        {
            try
            {
                var progressTask   = _api.GetStudentProgressAsync(id);
                var assessmentTask = _api.GetPlacementProgressAsync(id);
                var nodesTask      = _api.GetNodeProgressAsync(id);
                var modulesTask    = _api.GetModuleLadderAsync(id);
                var gamesTask      = _api.GetGameResultsAsync(id);
                var badgesTask     = _api.GetBadgesAsync(id);

                await Task.WhenAll(progressTask, assessmentTask, nodesTask,
                                   modulesTask, gamesTask, badgesTask);

                ViewBag.Student    = progressTask.Result;
                ViewBag.Assessment = assessmentTask.Result;
                ViewBag.Nodes      = nodesTask.Result;
                ViewBag.Modules    = modulesTask.Result;
                ViewBag.Games      = gamesTask.Result;
                ViewBag.Badges     = badgesTask.Result;
            }
            catch (Exception ex)
            {
                ViewBag.Error = ex.Message;
            }

            ViewBag.Title     = "Student Details";
            ViewBag.StudentId = id;
            ViewBag.BackUrl   = Url.Action("Index", "TeacherStudents");
            return View("~/Views/Student/Details.cshtml");
        }
    }
}
