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
    // =========================================================================
    // SchoolsController — Admin School Management
    // Displays school list with per-school student counts.
    // =========================================================================
    [AuthFilter]
    [AuthorizeAdmin]
    public class SchoolsController : AsyncController
    {
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        // ── Index ─────────────────────────────────────────────────────────────
        public async Task<ActionResult> Index()
        {
            List<dynamic> students;
            dynamic schools;

            try
            {
                var studentsTask = _api.GetStudentsAsync();
                var schoolsTask  = _api.GetSchoolsAsync();
                await Task.WhenAll(studentsTask, schoolsTask);
                students = studentsTask.Result ?? new List<dynamic>();
                schools  = schoolsTask.Result;
            }
            catch (Exception ex)
            {
                ViewBag.Error       = "Unable to load school data: " + ex.Message;
                ViewBag.SchoolsJson = "[]";
                ViewBag.TotalStudents = 0;
                return View("Index");
            }

            ViewBag.SchoolsJson   = JsonConvert.SerializeObject(schools ?? new List<dynamic>());
            ViewBag.StudentsJson  = JsonConvert.SerializeObject(students);
            ViewBag.TotalStudents = students.Count;

            return View("Index");
        }

        // ── GetSchools — JSON endpoint for client-side use ────────────────────
        [HttpGet]
        public async Task<JsonResult> GetSchools()
        {
            try
            {
                var schools = await _api.GetSchoolsAsync();
                return Json(schools, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ── CreateSchool ──────────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> CreateSchool(SchoolModel model)
        {
            if (model == null || string.IsNullOrWhiteSpace(model.SchoolName))
                return Json(new { success = false, error = "School name is required." });

            try
            {
                var result = await _api.CreateSchoolAsync(model.SchoolName, model.Barangay);
                return Json(new { success = true, data = result });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ── UpdateSchool ──────────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> UpdateSchool(SchoolModel model)
        {
            if (model == null || model.SchoolId <= 0)
                return Json(new { success = false, error = "Invalid school ID." });

            try
            {
                var result = await _api.UpdateSchoolAsync(model.SchoolId, model.SchoolName, model.Barangay);
                return Json(new { success = true, data = result });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ── DeleteSchool ──────────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> DeleteSchool(int id)
        {
            if (id <= 0)
                return Json(new { success = false, error = "Invalid school ID." });

            try
            {
                var result = await _api.DeleteSchoolAsync(id);
                return Json(new { success = true, data = result });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ── ExportCsv ─────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<ActionResult> ExportCsv()
        {
            List<dynamic> students;
            dynamic schools;
            try
            {
                var st = _api.GetStudentsAsync();
                var sc = _api.GetSchoolsAsync();
                await Task.WhenAll(st, sc);
                students = st.Result ?? new List<dynamic>();
                schools  = sc.Result;
            }
            catch
            {
                students = new List<dynamic>();
                schools  = new List<dynamic>();
            }

            var schoolList = schools as IEnumerable<dynamic> ?? new List<dynamic>();
            int total = students.Count;

            var lines = new System.Text.StringBuilder();
            lines.AppendLine("School Name,School Code,Barangay,Students,% of Total");
            foreach (var sc in schoolList)
            {
                string name     = (string)sc.school_name ?? "";
                string code     = (string)sc.school_code ?? (string)sc.school_id?.ToString() ?? "";
                string barangay = (string)sc.barangay    ?? "";
                int    count    = students.Count(s => (string)s.school_name == name);
                double pct      = total > 0 ? Math.Round((double)count / total * 100, 1) : 0;
                lines.AppendLine($"\"{name}\",\"{code}\",\"{barangay}\",{count},{pct}%");
            }

            byte[] bytes = System.Text.Encoding.UTF8.GetBytes(lines.ToString());
            return File(bytes, "text/csv", "schools_export.csv");
        }

        public class SchoolModel
        {
            public int    SchoolId   { get; set; }
            public string SchoolName { get; set; }
            public string Barangay   { get; set; }
        }
    }
}
