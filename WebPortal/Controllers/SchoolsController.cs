using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Text;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Newtonsoft.Json;

namespace Website.Controllers
{
    // =========================================================================
    // SchoolsController — Admin School Management
    // Queries Azure SQL directly using LiteRiseConnection.
    // =========================================================================
    [AuthFilter]
    [AuthorizeAdmin]
    public class SchoolsController : Controller
    {
        private static string ConnStr =>
            ConfigurationManager.ConnectionStrings["LiteRiseConnection"].ConnectionString;

        // ── Index ─────────────────────────────────────────────────────────────
        public ActionResult Index()
        {
            try
            {
                var schools  = QuerySchools();
                var students = QueryStudentSchools();

                ViewBag.SchoolsJson   = JsonConvert.SerializeObject(schools);
                ViewBag.StudentsJson  = JsonConvert.SerializeObject(students);
                ViewBag.TotalStudents = students.Count;
            }
            catch (Exception ex)
            {
                ViewBag.Error         = "Unable to load school data: " + ex.Message;
                ViewBag.SchoolsJson   = "[]";
                ViewBag.StudentsJson  = "[]";
                ViewBag.TotalStudents = 0;
            }

            return View("Index");
        }

        // ── GetSchools — JSON endpoint for client-side use ────────────────────
        [HttpGet]
        public JsonResult GetSchools()
        {
            try
            {
                var schools = QuerySchools();
                return Json(schools, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ── CreateSchool ──────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult CreateSchool(SchoolModel model)
        {
            if (model == null || string.IsNullOrWhiteSpace(model.SchoolName))
                return Json(new { success = false, error = "School name is required." });

            try
            {
                const string sql = @"
                    INSERT INTO dbo.Schools (SchoolName, District, Address, City, Province, IsActive, DateCreated)
                    VALUES (@name, @district, @address, @city, @province, 1, GETDATE())";

                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();
                    using (var cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@name",     model.SchoolName ?? "");
                        cmd.Parameters.AddWithValue("@district", model.District   ?? "");
                        cmd.Parameters.AddWithValue("@address",  model.Address    ?? "");
                        cmd.Parameters.AddWithValue("@city",     model.City       ?? "");
                        cmd.Parameters.AddWithValue("@province", model.Province   ?? "");
                        cmd.ExecuteNonQuery();
                    }
                }

                return Json(new { success = true });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ── UpdateSchool ──────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult UpdateSchool(SchoolModel model)
        {
            if (model == null || model.SchoolId <= 0)
                return Json(new { success = false, error = "Invalid school ID." });

            try
            {
                const string sql = @"
                    UPDATE dbo.Schools
                    SET    SchoolName = @name,
                           District   = @district,
                           Address    = @address,
                           City       = @city,
                           Province   = @province
                    WHERE  SchoolID   = @id";

                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();
                    using (var cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@name",     model.SchoolName ?? "");
                        cmd.Parameters.AddWithValue("@district", model.District   ?? "");
                        cmd.Parameters.AddWithValue("@address",  model.Address    ?? "");
                        cmd.Parameters.AddWithValue("@city",     model.City       ?? "");
                        cmd.Parameters.AddWithValue("@province", model.Province   ?? "");
                        cmd.Parameters.AddWithValue("@id",       model.SchoolId);
                        cmd.ExecuteNonQuery();
                    }
                }

                return Json(new { success = true });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ── DeleteSchool ──────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult DeleteSchool(int id)
        {
            if (id <= 0)
                return Json(new { success = false, error = "Invalid school ID." });

            try
            {
                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();
                    using (var cmd = new SqlCommand("DELETE FROM dbo.Schools WHERE SchoolID = @id", conn))
                    {
                        cmd.Parameters.AddWithValue("@id", id);
                        cmd.ExecuteNonQuery();
                    }
                }

                return Json(new { success = true });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ── ExportCsv ─────────────────────────────────────────────────────────
        [HttpGet]
        public ActionResult ExportCsv()
        {
            List<dynamic> students;
            List<dynamic> schools;
            try
            {
                schools  = QuerySchools();
                students = QueryStudentSchools();
            }
            catch
            {
                schools  = new List<dynamic>();
                students = new List<dynamic>();
            }

            int total = students.Count;
            var lines = new StringBuilder();
            lines.AppendLine("School Name,District,City,Province,Students,% of Total");

            foreach (dynamic sc in schools)
            {
                string name     = sc.school_name ?? "";
                string district = sc.district    ?? "";
                string city     = sc.city        ?? "";
                string province = sc.province    ?? "";
                int    count    = 0;
                foreach (dynamic s in students)
                    if ((string)s.school_name == name) count++;
                double pct = total > 0 ? Math.Round((double)count / total * 100, 1) : 0;
                lines.AppendLine($"\"{name}\",\"{district}\",\"{city}\",\"{province}\",{count},{pct}%");
            }

            byte[] bytes = Encoding.UTF8.GetBytes(lines.ToString());
            return File(bytes, "text/csv", "schools_export.csv");
        }

        // ─────────────────────────────────────────────────────────────────────
        // Private helpers
        // ─────────────────────────────────────────────────────────────────────

        private static List<dynamic> QuerySchools()
        {
            const string sql = @"
                SELECT SchoolID                          AS school_id,
                       SchoolName                        AS school_name,
                       ISNULL(District,  '')             AS district,
                       ISNULL(Address,   '')             AS address,
                       ISNULL(City,      '')             AS city,
                       ISNULL(Province,  '')             AS province
                FROM   dbo.Schools
                WHERE  ISNULL(IsActive, 1) = 1
                ORDER  BY SchoolName";

            var list = new List<dynamic>();
            using (var conn = new SqlConnection(ConnStr))
            {
                conn.Open();
                using (var cmd = new SqlCommand(sql, conn))
                using (var rdr = cmd.ExecuteReader())
                {
                    while (rdr.Read())
                    {
                        list.Add(new
                        {
                            school_id   = (int)rdr["school_id"],
                            school_name = rdr["school_name"]?.ToString(),
                            district    = rdr["district"]?.ToString(),
                            address     = rdr["address"]?.ToString(),
                            city        = rdr["city"]?.ToString(),
                            province    = rdr["province"]?.ToString()
                        });
                    }
                }
            }
            return list;
        }

        /// <summary>Returns a lightweight list of students with their school name for count calculations.</summary>
        private static List<dynamic> QueryStudentSchools()
        {
            const string sql = @"
                SELECT s.StudentID,
                       ISNULL(sc.SchoolName, '') AS school_name
                FROM   dbo.Students  s
                LEFT   JOIN dbo.Schools sc ON sc.SchoolID = s.SchoolID";

            var list = new List<dynamic>();
            using (var conn = new SqlConnection(ConnStr))
            {
                conn.Open();
                using (var cmd = new SqlCommand(sql, conn))
                using (var rdr = cmd.ExecuteReader())
                {
                    while (rdr.Read())
                    {
                        list.Add(new
                        {
                            student_id  = (int)rdr["StudentID"],
                            school_name = rdr["school_name"]?.ToString()
                        });
                    }
                }
            }
            return list;
        }

        // ═════════════════════════════════════════════════════════════════════
        // Model
        // ═════════════════════════════════════════════════════════════════════
        public class SchoolModel
        {
            public int    SchoolId   { get; set; }
            public string SchoolName { get; set; }
            public string District   { get; set; }
            public string Address    { get; set; }
            public string City       { get; set; }
            public string Province   { get; set; }
        }
    }
}
