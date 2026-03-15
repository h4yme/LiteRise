using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.SqlClient;
using System.Web.Mvc;
using Website.Filters;
using BCrypt.Net;   // NuGet: BCrypt.Net-Next

namespace Website.Controllers
{
    // =========================================================================
    // AdministrationController — Admin & Teacher Account Management
    // All data access uses direct SQL against LiteRiseConnection.
    // =========================================================================
    [AuthFilter]
    [AuthorizeAdmin]
    public class AdministrationController : Controller
    {
        private static string ConnStr =>
            ConfigurationManager.ConnectionStrings["LiteRiseConnection"].ConnectionString;

        // ── Index ─────────────────────────────────────────────────────────────
        public ActionResult Index()
        {
            return View("AdministrationView");
        }

        // ── GetAdmins — returns JSON array for AdministrationScript.js ────────
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
                                id        = "admin_" + rdr["AdminID"],
                                raw_id    = (int)rdr["AdminID"],
                                name      = rdr["name"]?.ToString(),
                                email     = rdr["Email"]?.ToString(),
                                role      = "Admin",
                                isActive  = rdr["is_active"] != DBNull.Value && (bool)rdr["is_active"],
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
                                id        = "teacher_" + rdr["TeacherID"],
                                raw_id    = (int)rdr["TeacherID"],
                                name      = rdr["name"]?.ToString(),
                                email     = rdr["Email"]?.ToString(),
                                role      = "Teacher",
                                school    = rdr["school"]?.ToString(),
                                isActive  = rdr["is_active"] != DBNull.Value && (bool)rdr["is_active"],
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

        // ── GetSchools — school dropdown in the add-account modal ─────────────
        [HttpGet]
        public JsonResult GetSchools()
        {
            try
            {
                var list = new List<object>();
                const string sql = @"
                    SELECT SchoolID   AS school_id,
                           SchoolName AS school_name
                    FROM   dbo.Schools
                    WHERE  ISNULL(IsActive, 1) = 1
                    ORDER  BY SchoolName";

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
                                school_name = rdr["school_name"]?.ToString()
                            });
                        }
                    }
                }
                return Json(list, JsonRequestBehavior.AllowGet);
            }
            catch (Exception ex)
            {
                Response.StatusCode = 500;
                return Json(new { error = ex.Message }, JsonRequestBehavior.AllowGet);
            }
        }

        // ── SaveAdmin — create or update an admin or teacher account ──────────
        [HttpPost]
        public JsonResult SaveAdmin(AccountModel model)
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

            string role = (model.role ?? "").Trim();
            if (string.Equals(role, "admin",   StringComparison.OrdinalIgnoreCase)) role = "Admin";
            else if (string.Equals(role, "teacher", StringComparison.OrdinalIgnoreCase)) role = "Teacher";
            else return Json(new { success = false, message = "Role must be Admin or Teacher." });

            try
            {
                string hash = !string.IsNullOrWhiteSpace(model.password)
                    ? BCrypt.Net.BCrypt.HashPassword(model.password)
                    : null;

                string email = model.email.Trim();
                string name  = model.name.Trim();

                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();

                    if (isNew)
                        return CreateAccount(conn, role, name, email, hash, model.school);
                    else
                        return UpdateAccount(conn, model.id, role, name, email, hash, model.school);
                }
            }
            catch (Exception ex)
            {
                return Json(new { success = false, message = ex.Message });
            }
        }

        // ── DeactivateAdmin ───────────────────────────────────────────────────
        [HttpPost]
        public JsonResult DeactivateAdmin(string id)
        {
            return SetAccountActive(id, false);
        }

        // ── ReactivateAdmin ───────────────────────────────────────────────────
        [HttpPost]
        public JsonResult ReactivateAdmin(string id)
        {
            return SetAccountActive(id, true);
        }

        // ─────────────────────────────────────────────────────────────────────
        // Private helpers
        // ─────────────────────────────────────────────────────────────────────

        private JsonResult CreateAccount(SqlConnection conn, string role, string name,
                                         string email, string hash, string school)
        {
            if (role == "Admin")
            {
                using (var chk = new SqlCommand(
                    "SELECT COUNT(*) FROM dbo.Admins WHERE Email = @email", conn))
                {
                    chk.Parameters.AddWithValue("@email", email);
                    if ((int)chk.ExecuteScalar() > 0)
                        return Json(new { success = false, message = "An admin with this email already exists." });
                }

                const string sql = @"
                    INSERT INTO dbo.Admins (Username, Email, PasswordHash, IsActive, CreatedDate)
                    OUTPUT INSERTED.AdminID
                    VALUES (@name, @email, @hash, 1, GETUTCDATE())";

                using (var cmd = new SqlCommand(sql, conn))
                {
                    cmd.Parameters.AddWithValue("@name",  name);
                    cmd.Parameters.AddWithValue("@email", email);
                    cmd.Parameters.AddWithValue("@hash",  hash);
                    int newId = (int)cmd.ExecuteScalar();
                    return Json(new { success = true, message = "Admin account created.", id = "admin_" + newId });
                }
            }
            else // Teacher
            {
                using (var chk = new SqlCommand(
                    "SELECT COUNT(*) FROM dbo.Teachers WHERE Email = @email", conn))
                {
                    chk.Parameters.AddWithValue("@email", email);
                    if ((int)chk.ExecuteScalar() > 0)
                        return Json(new { success = false, message = "A teacher with this email already exists." });
                }

                var   parts     = name.Split(new[] { ' ' }, 2);
                string firstName = parts[0];
                string lastName  = parts.Length > 1 ? parts[1] : "";
                string dept      = ResolveSchoolName(conn, school);

                const string sql = @"
                    INSERT INTO dbo.Teachers (FirstName, LastName, Email, Password, Department, IsActive, DateCreated)
                    OUTPUT INSERTED.TeacherID
                    VALUES (@first, @last, @email, @hash, @dept, 1, GETUTCDATE())";

                using (var cmd = new SqlCommand(sql, conn))
                {
                    cmd.Parameters.AddWithValue("@first", firstName);
                    cmd.Parameters.AddWithValue("@last",  lastName);
                    cmd.Parameters.AddWithValue("@email", email);
                    cmd.Parameters.AddWithValue("@hash",  hash);
                    cmd.Parameters.AddWithValue("@dept",  (object)dept ?? DBNull.Value);
                    int newId = (int)cmd.ExecuteScalar();
                    return Json(new { success = true, message = "Teacher account created.", id = "teacher_" + newId });
                }
            }
        }

        private JsonResult UpdateAccount(SqlConnection conn, string id, string role,
                                         string name, string email, string hash, string school)
        {
            var idParts = (id ?? "").Split('_');
            if (idParts.Length < 2 || !int.TryParse(idParts[1], out int rawId) || rawId <= 0)
                return Json(new { success = false, message = "Invalid account ID." });

            string table = idParts[0].ToLower();

            if (table == "admin")
            {
                using (var chk = new SqlCommand(
                    "SELECT COUNT(*) FROM dbo.Admins WHERE Email = @email AND AdminID <> @id", conn))
                {
                    chk.Parameters.AddWithValue("@email", email);
                    chk.Parameters.AddWithValue("@id",    rawId);
                    if ((int)chk.ExecuteScalar() > 0)
                        return Json(new { success = false, message = "Another admin with this email already exists." });
                }

                string sql = hash != null
                    ? "UPDATE dbo.Admins SET Username=@name, Email=@email, PasswordHash=@hash WHERE AdminID=@id"
                    : "UPDATE dbo.Admins SET Username=@name, Email=@email WHERE AdminID=@id";

                using (var cmd = new SqlCommand(sql, conn))
                {
                    cmd.Parameters.AddWithValue("@name",  name);
                    cmd.Parameters.AddWithValue("@email", email);
                    cmd.Parameters.AddWithValue("@id",    rawId);
                    if (hash != null) cmd.Parameters.AddWithValue("@hash", hash);
                    cmd.ExecuteNonQuery();
                }
            }
            else if (table == "teacher")
            {
                using (var chk = new SqlCommand(
                    "SELECT COUNT(*) FROM dbo.Teachers WHERE Email = @email AND TeacherID <> @id", conn))
                {
                    chk.Parameters.AddWithValue("@email", email);
                    chk.Parameters.AddWithValue("@id",    rawId);
                    if ((int)chk.ExecuteScalar() > 0)
                        return Json(new { success = false, message = "Another teacher with this email already exists." });
                }

                var    parts     = name.Split(new[] { ' ' }, 2);
                string firstName = parts[0];
                string lastName  = parts.Length > 1 ? parts[1] : "";
                string dept      = ResolveSchoolName(conn, school);

                string sql = hash != null
                    ? "UPDATE dbo.Teachers SET FirstName=@first, LastName=@last, Email=@email, Password=@hash, Department=@dept WHERE TeacherID=@id"
                    : "UPDATE dbo.Teachers SET FirstName=@first, LastName=@last, Email=@email, Department=@dept WHERE TeacherID=@id";

                using (var cmd = new SqlCommand(sql, conn))
                {
                    cmd.Parameters.AddWithValue("@first", firstName);
                    cmd.Parameters.AddWithValue("@last",  lastName);
                    cmd.Parameters.AddWithValue("@email", email);
                    cmd.Parameters.AddWithValue("@dept",  (object)dept ?? DBNull.Value);
                    cmd.Parameters.AddWithValue("@id",    rawId);
                    if (hash != null) cmd.Parameters.AddWithValue("@hash", hash);
                    cmd.ExecuteNonQuery();
                }
            }
            else
            {
                return Json(new { success = false, message = "Invalid account type." });
            }

            return Json(new { success = true, message = "Account updated successfully." });
        }

        private JsonResult SetAccountActive(string id, bool isActive)
        {
            if (string.IsNullOrEmpty(id))
                return Json(new { success = false, message = "Invalid account ID." });

            var idParts = id.Split('_');
            if (idParts.Length < 2 || !int.TryParse(idParts[1], out int rawId) || rawId <= 0)
                return Json(new { success = false, message = "Invalid account ID format." });

            string table = idParts[0].ToLower();
            string sql;
            if (table == "admin")
                sql = "UPDATE dbo.Admins   SET IsActive = @flag WHERE AdminID   = @id";
            else if (table == "teacher")
                sql = "UPDATE dbo.Teachers SET IsActive = @flag WHERE TeacherID = @id";
            else
                return Json(new { success = false, message = "Invalid account type." });

            try
            {
                using (var conn = new SqlConnection(ConnStr))
                {
                    conn.Open();
                    using (var cmd = new SqlCommand(sql, conn))
                    {
                        cmd.Parameters.AddWithValue("@flag", isActive ? 1 : 0);
                        cmd.Parameters.AddWithValue("@id",   rawId);
                        if (cmd.ExecuteNonQuery() == 0)
                            return Json(new { success = false, message = "Account not found." });
                    }
                }
                string msg = isActive ? "Account reactivated." : "Account deactivated.";
                return Json(new { success = true, message = msg });
            }
            catch (Exception ex)
            {
                return Json(new { success = false, message = ex.Message });
            }
        }

        /// <summary>
        /// If <paramref name="schoolIdOrName"/> is numeric, resolves to the school name
        /// from dbo.Schools; otherwise returns the value as-is (school name entered directly).
        /// </summary>
        private static string ResolveSchoolName(SqlConnection conn, string schoolIdOrName)
        {
            if (string.IsNullOrWhiteSpace(schoolIdOrName)) return null;

            if (int.TryParse(schoolIdOrName.Trim(), out int schoolId))
            {
                using (var cmd = new SqlCommand(
                    "SELECT SchoolName FROM dbo.Schools WHERE SchoolID = @id", conn))
                {
                    cmd.Parameters.AddWithValue("@id", schoolId);
                    var result = cmd.ExecuteScalar();
                    return result != null && result != DBNull.Value ? result.ToString() : null;
                }
            }
            return schoolIdOrName.Trim();
        }

        // ── Model ─────────────────────────────────────────────────────────────
        public class AccountModel
        {
            public string id       { get; set; }
            public string name     { get; set; }
            public string email    { get; set; }
            public string password { get; set; }
            public string role     { get; set; }   // Admin | Teacher
            public string school   { get; set; }   // school_id (int string) or school_name
        }
    }
}
