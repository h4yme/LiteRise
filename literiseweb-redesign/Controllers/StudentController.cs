using System;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;

namespace Website.Controllers
{
    [AuthFilter]
    public class StudentController : Controller
    {
        private readonly ApiService _api = new ApiService();

        // GET /Student
        public async Task<ActionResult> Index()
        {
            try
            {
                var studentsTask = _api.GetStudentsAsync();
                var schoolsTask  = _api.GetSchoolsAsync();
                await Task.WhenAll(studentsTask, schoolsTask);

                ViewBag.Students = studentsTask.Result;
                ViewBag.Schools  = schoolsTask.Result;
            }
            catch (Exception ex)
            {
                ViewBag.Error    = ex.Message;
                ViewBag.Students = new Newtonsoft.Json.Linq.JArray();
                ViewBag.Schools  = new Newtonsoft.Json.Linq.JArray();
            }

            ViewBag.Title = "Students";
            return View();
        }

        // GET /Student/Details/5
        public async Task<ActionResult> Details(int id)
        {
            try
            {
                var progressTask    = _api.GetStudentProgressAsync(id);
                var assessmentTask  = _api.GetPlacementProgressAsync(id);
                var nodesTask       = _api.GetNodeProgressAsync(id);
                var modulesTask     = _api.GetModuleLadderAsync(id);
                var gamesTask       = _api.GetGameResultsAsync(id);
                var badgesTask      = _api.GetBadgesAsync(id);

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
            return View();
        }
    }
}
