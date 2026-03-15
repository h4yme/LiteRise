using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.SqlClient;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;
using Newtonsoft.Json;

namespace Website.Controllers
{
    // =========================================================================
    // AdministrationController — Admin & Teacher Account Management
    // =========================================================================
    [AuthFilter]
    [AuthorizeAdmin]
    public class AdministrationController : AsyncController
    {
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        private static string ConnStr =>
            ConfigurationManager.ConnectionStrings["LiteRiseConnection"].ConnectionString;

        // ── Index ─────────────────────────────────────────────────────────────
        public ActionResult Index()
        {
            return View("AdministrationView");
        }

        // ── GetAdmins — returns a raw JSON array for AdministrationScript.js ──
        [HttpGet]
        public JsonResult GetAdmins()
        {
            try
            {
                var accounts = new List<object>();
                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();

                    const string adminSql = @"
                        SELECT AdminID,
                               ISNULL(Username, Email)   AS name,
                               Email,
                               CAST(IsActive AS BIT)     AS is_active,
                               LastLoginDate
                        FROM   dbo.Admins
                        ORDER BY name";
                    using (var cmd = new SqlCommand(adminSql, conn))
                    using (var rdr = cmd.ExecuteReader())
                    {
                        while (rdr.Read())
                        {
                            accounts.Add(new
                            {
                                id       = "admin_" + rdr["AdminID"],
                                raw_id   = (int)rdr["AdminID"],
                                name     = rdr["name"]?.ToString(),
                                email    = rdr["Email"]?.ToString(),
                                role     = "Admin",
                                isActive = rdr["is_active"] != DBNull.Value && (bool)rdr["is_active"],
                                lastLogin = rdr["LastLoginDate"] == DBNull.Value
                                              ? (string)null
                                              : ((DateTime)rdr["LastLoginDate"]).ToString("yyyy-MM-dd HH:mm")
                            });
                        }
                    }

                    const string teacherSql = @"
                        SELECT TeacherID,
                               RTRIM(ISNULL(FirstName,'') + ' ' + ISNULL(LastName,'')) AS name,
                               Email,
                               ISNULL(Department, '')    AS school,
                               CAST(IsActive AS BIT)     AS is_active
                        FROM   dbo.Teachers
                        ORDER BY name";
                    using (var cmd = new SqlCommand(teacherSql, conn))
                    using (var rdr = cmd.ExecuteReader())
                    {
                        while (rdr.Read())
                        {
                            accounts.Add(new
                            {
                                id       = "teacher_" + rdr["TeacherID"],
                                raw_id   = (int)rdr["TeacherID"],
                                name     = rdr["name"]?.ToString(),
                                email    = rdr["Email"]?.ToString(),
                                role     = "Teacher",
                                school   = rdr["school"]?.ToString(),
                                isActive = rdr["is_active"] != DBNull.Value && (bool)rdr["is_active"],
                                lastLogin = (string)null
                            });
                        }
                    }
                }
                return Json(accounts, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                Response.StatusCode = 500;
                return Json(new { error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ── GetSchools — for the school dropdown in the add-account modal ─────
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
                return Json(new object[0], JsonRequestBehavior.AllowGet);
            }
        }

        // ── SaveAdmin — create or update an account ───────────────────────────
        [HttpPost]
        public async Task<JsonResult> SaveAdmin(AccountModel model)
        {
            if (model == null)
                return Json(new { success = false, message = "Invalid data." });

            if (string.IsNullOrWhiteSpace(model.name))
                return Json(new { success = false, message = "Full name is required." });

            if (string.IsNullOrWhiteSpace(model.email))
                return Json(new { success = false, message = "Email is required." });

            if (string.IsNullOrWhiteSpace(model.role))
                return Json(new { success = false, message = "Role is required." });

            bool isNew = string.IsNullOrEmpty(model.id) || model.id == "0";

            if (isNew && string.IsNullOrWhiteSpace(model.password))
                return Json(new { success = false, message = "Password is required for new accounts." });

            if (!string.IsNullOrWhiteSpace(model.password) && model.password.Length < 8)
                return Json(new { success = false, message = "Password must be at least 8 characters." });

            try
            {
                if (isNew)
                {
                    var result = await _api.CreatePortalAccountAsync(
                        model.name, model.email, model.password,
                        model.role, model.school);
                    return Json(new { success = true, message = "Account created successfully.", data = result });
                }
                else
                {
                    var result = await _api.UpdatePortalAccountAsync(
                        model.id, model.name, model.email, model.password,
                        model.role, model.school);
                    return Json(new { success = true, message = "Account updated successfully.", data = result });
                }
            }
            catch (Exception ex)
            {
                return Json(new { success = false, message = ex.Message });
            }
        }

        // ── DeactivateAdmin ───────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> DeactivateAdmin(string id)
        {
            if (string.IsNullOrEmpty(id))
                return Json(new { success = false, message = "Invalid account ID." });

            try
            {
                var result = await _api.SetPortalAccountActiveAsync(id, false);
                return Json(new { success = true, message = "Account deactivated.", data = result });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, message = ex.Message });
            }
        }

        // ── ReactivateAdmin ───────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> ReactivateAdmin(string id)
        {
            if (string.IsNullOrEmpty(id))
                return Json(new { success = false, message = "Invalid account ID." });

            try
            {
                var result = await _api.SetPortalAccountActiveAsync(id, true);
                return Json(new { success = true, message = "Account reactivated.", data = result });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, message = ex.Message });
            }
        }

        // ── Model ─────────────────────────────────────────────────────────────
        public class AccountModel
        {
            public string id       { get; set; }
            public string name     { get; set; }
            public string email    { get; set; }
            public string password { get; set; }
            public string role     { get; set; }   // Admin | Teacher
            public string school   { get; set; }   // school_id or school_name for Teacher
        }
    }
}
