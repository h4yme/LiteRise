using System;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;

namespace Website.Controllers
{
    [AuthFilter]   // existing auth filter — keeps session guard in one place
    public class StudentController : BaseController
    {
        private readonly ApiService _api = new ApiService();

        // GET /Student  (or /Student/Index)
        public async Task<ActionResult> Index(string school = null, string level = null, string status = null)
        {
            int? schoolId = null;
            if (int.TryParse(school, out int sid)) schoolId = sid;

            try
            {
                var students = await _api.GetStudentsAsync(schoolId);
                var schools  = await _api.GetSchoolsAsync();

                ViewBag.Students     = students;
                ViewBag.Schools      = schools;
                ViewBag.FilterSchool = school;
                ViewBag.FilterLevel  = level;
                ViewBag.FilterStatus = status;
                ViewBag.Title        = "Students";
            }
            catch (Exception ex)
            {
                ViewBag.ApiError = "Could not load students: " + ex.Message;
                ViewBag.Students = new Newtonsoft.Json.Linq.JArray();
                ViewBag.Schools  = new Newtonsoft.Json.Linq.JArray();
            }

            return View("StudentView");
        }

        // GET /Student/Detail/5
        public async Task<ActionResult> Detail(int id)
        {
            if (id <= 0) return RedirectToAction("Index");

            try
            {
                // Fire all requests in parallel — much faster than sequential
                var tProgress   = _api.GetStudentProgressAsync(id);
                var tAssessment = _api.GetPlacementProgressAsync(id);
                var tNodes      = _api.GetNodeProgressAsync(id);
                var tModules    = _api.GetModuleLadderAsync(id);
                var tGames      = _api.GetGameResultsAsync(id);
                var tBadges     = _api.GetBadgesAsync(id);
                var tLessons    = _api.GetLessonProgressAsync(id);

                await Task.WhenAll(tProgress, tAssessment, tNodes, tModules, tGames, tBadges, tLessons);

                ViewBag.Student    = tProgress.Result;
                ViewBag.Assessment = tAssessment.Result;
                ViewBag.Nodes      = tNodes.Result;
                ViewBag.Modules    = tModules.Result;
                ViewBag.Games      = tGames.Result;
                ViewBag.Badges     = tBadges.Result;
                ViewBag.Lessons    = tLessons.Result;
                ViewBag.StudentId  = id;
                ViewBag.Title      = "Student Profile";
            }
            catch (Exception ex)
            {
                ViewBag.ApiError  = "Could not load student data: " + ex.Message;
                ViewBag.StudentId = id;
                ViewBag.Title     = "Student Profile";
            }

            return View("StudentDetailView");
        }
    }
}
