using System;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Services;

namespace Website.Controllers
{
    // =========================================================================
    // LoginController
    // Handles portal login for Admin and Teacher roles.
    // On success: stores AuthToken, UserRole, SchoolId, UserName in Session
    // and redirects to the appropriate dashboard.
    // =========================================================================
    public class LoginController : AsyncController
    {
        [HttpGet]
        public ActionResult Index()
        {
            // Already logged in → redirect
            if (Session["AuthToken"] != null)
                return RedirectToDashboard(Session["UserRole"]?.ToString());

            return View("LoginView");
        }

        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<ActionResult> Login(string email, string password, string role)
        {
            if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password))
            {
                ViewBag.Error = "Email and password are required.";
                return View("LoginView");
            }

            try
            {
                var api    = new ApiService();
                var result = await api.PortalLoginAsync(email.Trim(), password, role ?? "admin");

                if (result == null || result.token == null)
                {
                    ViewBag.Error = "Invalid credentials. Please try again.";
                    return View("LoginView");
                }

                // ── Persist session ───────────────────────────────────────────
                Session["AuthToken"] = (string)result.token;
                Session["UserRole"]  = (string)(result.role ?? role ?? "admin");
                Session["UserName"]  = (string)(result.name ?? result.email ?? email);
                Session["SchoolId"]  = result.school_id != null ? (string)result.school_id.ToString() : null;

                return RedirectToDashboard(Session["UserRole"]?.ToString());
            }
            catch (Exception ex)
            {
                ViewBag.Error = "Login failed: " + ex.Message;
                return View("LoginView");
            }
        }

        public ActionResult Logout()
        {
            Session.Clear();
            Session.Abandon();
            return RedirectToAction("Index", "Login");
        }

        // ── Helper ─────────────────────────────────────────────────────────────
        private ActionResult RedirectToDashboard(string role)
        {
            if (string.Equals(role, "teacher", StringComparison.OrdinalIgnoreCase))
                return RedirectToAction("Index", "TeacherDashboard");

            return RedirectToAction("Index", "Dashboard");
        }
    }
}
