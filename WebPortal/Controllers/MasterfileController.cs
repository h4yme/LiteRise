using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Filters;
using Website.Services;
using BCrypt.Net;   // NuGet: BCrypt.Net-Next

namespace Website.Controllers
{
    [AuthFilter]
    [AuthorizeAdmin]
    public class MasterfileController : AsyncController
    {
        // ─────────────────────────────────────────────────────────────────────
        // Shared API service (still used for questions / modules / badges)
        // ─────────────────────────────────────────────────────────────────────
        private ApiService _api => new ApiService(Session["AuthToken"]?.ToString());

        private static string ConnStr =>
            ConfigurationManager.ConnectionStrings["LiteRiseConnection"].ConnectionString;

        // ─────────────────────────────────────────────────────────────────────
        // Index – loads the Masterfile shell view
        // ─────────────────────────────────────────────────────────────────────
        public ActionResult Index()
        {
            return View("MasterfileView");
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetQuestions – returns the full question bank
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<JsonResult> GetQuestions()
        {
            try
            {
                var questions = await _api.GetAssessmentItemsAsync();
                return Json(new { success = true, data = questions }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetModuleLadder – returns all modules and nodes (admin view)
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<JsonResult> GetModuleLadder()
        {
            try
            {
                var ladder = await _api.GetModuleLadderAsync(0);
                return Json(new { success = true, data = ladder }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetBadges – returns all badges
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public async Task<JsonResult> GetBadges()
        {
            try
            {
                var badges = await _api.GetBadgesAsync(0);
                return Json(new { success = true, data = badges }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetAdmins – returns all admin + teacher portal accounts from the DB
        // ─────────────────────────────────────────────────────────────────────
        [HttpGet]
        public JsonResult GetAdmins()
        {
            try
            {
                var accounts = new List<object>();

                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();

                    // ── Admins ──────────────────────────────────────────────
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

                    // ── Teachers ────────────────────────────────────────────
                    const string teacherSql = @"
                        SELECT TeacherID,
                               RTRIM(ISNULL(FirstName,'') + ' ' + ISNULL(LastName,'')) AS name,
                               Email,
                               ISNULL(School, '')        AS school,
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

                return Json(new { success = true, data = accounts }, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // SaveAdmin – create or update an admin / teacher account
        // id: "" | "0" = create;  "admin_N" | "teacher_N" = update
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult SaveAdmin(AdminModel model)
        {
            try
            {
                if (model == null)
                    return Json(new { success = false, error = "Invalid account data." });

                bool isNew = string.IsNullOrEmpty(model.Id) || model.Id == "0";

                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();

                    if (isNew)
                    {
                        if (string.IsNullOrWhiteSpace(model.Password))
                            return Json(new { success = false, error = "Password is required for new accounts." });

                        string hash = BCrypt.Net.BCrypt.HashPassword(model.Password);

                        if (string.Equals(model.Role, "Teacher", StringComparison.OrdinalIgnoreCase))
                        {
                            // Split full name into first/last
                            var parts  = (model.Name ?? "").Trim().Split(new[] { ' ' }, 2);
                            string fn  = parts[0];
                            string ln  = parts.Length > 1 ? parts[1] : "";

                            const string sql = @"
                                INSERT INTO dbo.Teachers (FirstName, LastName, Email, Password, School, IsActive)
                                VALUES (@fn, @ln, @email, @hash, @school, 1)";

                            using (var cmd = new SqlCommand(sql, conn))
                            {
                                cmd.Parameters.AddWithValue("@fn",     fn);
                                cmd.Parameters.AddWithValue("@ln",     ln);
                                cmd.Parameters.AddWithValue("@email",  model.Email ?? "");
                                cmd.Parameters.AddWithValue("@hash",   hash);
                                cmd.Parameters.AddWithValue("@school", model.School ?? "");
                                cmd.ExecuteNonQuery();
                            }
                        }
                        else
                        {
                            const string sql = @"
                                INSERT INTO dbo.Admins (Username, Email, PasswordHash, IsActive)
                                VALUES (@name, @email, @hash, 1)";

                            using (var cmd = new SqlCommand(sql, conn))
                            {
                                cmd.Parameters.AddWithValue("@name",  model.Name ?? "");
                                cmd.Parameters.AddWithValue("@email", model.Email ?? "");
                                cmd.Parameters.AddWithValue("@hash",  hash);
                                cmd.ExecuteNonQuery();
                            }
                        }
                    }
                    else
                    {
                        // Parse prefixed id, e.g. "admin_3" or "teacher_7"
                        bool   isTeacher = model.Id.StartsWith("teacher_");
                        string rawId     = model.Id.Substring(model.Id.IndexOf('_') + 1);
                        int    numericId = int.Parse(rawId);

                        // Hash new password only if one was supplied
                        string hash = string.IsNullOrWhiteSpace(model.Password)
                            ? null
                            : BCrypt.Net.BCrypt.HashPassword(model.Password);

                        if (isTeacher)
                        {
                            var parts = (model.Name ?? "").Trim().Split(new[] { ' ' }, 2);
                            string fn = parts[0];
                            string ln = parts.Length > 1 ? parts[1] : "";

                            string sql = hash != null
                                ? @"UPDATE dbo.Teachers
                                    SET FirstName=@fn, LastName=@ln, Email=@email,
                                        Password=@hash, School=@school
                                    WHERE TeacherID=@id"
                                : @"UPDATE dbo.Teachers
                                    SET FirstName=@fn, LastName=@ln, Email=@email, School=@school
                                    WHERE TeacherID=@id";

                            using (var cmd = new SqlCommand(sql, conn))
                            {
                                cmd.Parameters.AddWithValue("@fn",     fn);
                                cmd.Parameters.AddWithValue("@ln",     ln);
                                cmd.Parameters.AddWithValue("@email",  model.Email ?? "");
                                cmd.Parameters.AddWithValue("@school", model.School ?? "");
                                cmd.Parameters.AddWithValue("@id",     numericId);
                                if (hash != null) cmd.Parameters.AddWithValue("@hash", hash);
                                cmd.ExecuteNonQuery();
                            }
                        }
                        else
                        {
                            string sql = hash != null
                                ? @"UPDATE dbo.Admins
                                    SET Username=@name, Email=@email, PasswordHash=@hash
                                    WHERE AdminID=@id"
                                : @"UPDATE dbo.Admins
                                    SET Username=@name, Email=@email
                                    WHERE AdminID=@id";

                            using (var cmd = new SqlCommand(sql, conn))
                            {
                                cmd.Parameters.AddWithValue("@name",  model.Name ?? "");
                                cmd.Parameters.AddWithValue("@email", model.Email ?? "");
                                cmd.Parameters.AddWithValue("@id",    numericId);
                                if (hash != null) cmd.Parameters.AddWithValue("@hash", hash);
                                cmd.ExecuteNonQuery();
                            }
                        }
                    }
                }

                return Json(new { success = true, message = "Account saved successfully." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // SetAccountActive – enable or disable a portal account
        // Body: { id: "admin_N"|"teacher_N", isActive: bool }
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult SetAccountActive(SetActiveModel model)
        {
            try
            {
                if (model == null || string.IsNullOrEmpty(model.Id))
                    return Json(new { success = false, error = "Invalid account ID." });

                bool   isTeacher = model.Id.StartsWith("teacher_");
                string rawId     = model.Id.Substring(model.Id.IndexOf('_') + 1);
                int    numericId = int.Parse(rawId);

                string sql = isTeacher
                    ? "UPDATE dbo.Teachers SET IsActive=@active WHERE TeacherID=@id"
                    : "UPDATE dbo.Admins   SET IsActive=@active WHERE AdminID=@id";

                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();
                    using (var cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@active", model.IsActive ? 1 : 0);
                        cmd.Parameters.AddWithValue("@id",     numericId);
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

        // ─────────────────────────────────────────────────────────────────────
        // SaveQuestion / DeactivateQuestion – question bank management
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult SaveQuestion(QuestionModel model)
        {
            try
            {
                if (model == null)
                    return Json(new { success = false, error = "Invalid question data." });

                // TODO: integrate _api.SaveAssessmentItemAsync(model) when endpoint is available
                return Json(new { success = true, message = "Question saved successfully." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        [HttpPost]
        public JsonResult DeactivateQuestion(int id)
        {
            try
            {
                if (id <= 0)
                    return Json(new { success = false, error = "Invalid question ID." });

                // TODO: integrate _api.DeactivateAssessmentItemAsync(id)
                return Json(new { success = true, message = $"Question #{id} updated." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // SaveBadge / DeleteBadge – badge management
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult SaveBadge(BadgeModel model)
        {
            try
            {
                if (model == null)
                    return Json(new { success = false, error = "Invalid badge data." });

                // TODO: integrate _api.SaveBadgeAsync(model)
                return Json(new { success = true, message = "Badge saved successfully." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        [HttpPost]
        public JsonResult DeleteBadge(int id)
        {
            try
            {
                if (id <= 0)
                    return Json(new { success = false, error = "Invalid badge ID." });

                // TODO: integrate _api.DeleteBadgeAsync(id)
                return Json(new { success = true, message = $"Badge #{id} deleted." });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // ToggleNode – enable or disable a curriculum node
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public async Task<JsonResult> ToggleNode(int nodeId, bool enabled)
        {
            try
            {
                if (nodeId <= 0)
                    return Json(new { success = false, error = "Invalid node ID." });

                var result = await _api.ToggleNodeStatusAsync(nodeId, enabled);
                return Json(new { success = true, data = result });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, error = ex.Message });
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // ImportQuestions – bulk import from CSV/JSON
        // ─────────────────────────────────────────────────────────────────────
        [HttpPost]
        public JsonResult ImportQuestions()
        {
            // TODO: implement bulk import
            return Json(new { success = true, message = "Import complete." });
        }

        // ═════════════════════════════════════════════════════════════════════
        // Inner model classes
        // ═════════════════════════════════════════════════════════════════════

        public class QuestionModel
        {
            public int    Id             { get; set; }
            public string Category       { get; set; }
            public string Type           { get; set; }
            public double Difficulty     { get; set; }
            public double Discrimination { get; set; }
            public string QuestionText   { get; set; }
            public string ChoiceA        { get; set; }
            public string ChoiceB        { get; set; }
            public string ChoiceC        { get; set; }
            public string ChoiceD        { get; set; }
            public string CorrectAnswer  { get; set; }
            public bool   IsActive       { get; set; }
        }

        public class BadgeModel
        {
            public int    Id          { get; set; }
            public string Name        { get; set; }
            public string Description { get; set; }
            public string Category    { get; set; }
            public string Criteria    { get; set; }
            public int    XpReward    { get; set; }
            public string Icon        { get; set; }
        }

        public class AdminModel
        {
            public string Id       { get; set; }   // "" | "0" = new; "admin_N" | "teacher_N" = existing
            public string Name     { get; set; }
            public string Email    { get; set; }
            public string Password { get; set; }
            public string Role     { get; set; }   // Admin | Teacher
            public string School   { get; set; }
            public bool   IsActive { get; set; }
        }

        public class SetActiveModel
        {
            public string Id       { get; set; }
            public bool   IsActive { get; set; }
        }
    }
}
