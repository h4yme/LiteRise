using System;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Services;

namespace Website.Controllers
{
    public class LoginController : AsyncController
    {
        [HttpGet]
        public ActionResult Index()
        {
            if (Session["AuthToken"] != null)
                return RedirectToDashboard(Session["UserRole"]?.ToString());

            return View("LoginView");
        }

        [HttpPost]
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

                Session["AuthToken"] = (string)result.token;
                Session["UserRole"]  = (string)(result.role ?? role ?? "admin");
                Session["UserName"]  = (string)(result.name ?? result.email ?? email);
                Session["SchoolId"]  = result.school_id != null
                    ? result.school_id.ToString()
                    : null;

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

        private ActionResult RedirectToDashboard(string role)
        {
            if (string.Equals(role, "teacher", StringComparison.OrdinalIgnoreCase))
                return RedirectToAction("Index", "TeacherDashboard");

            return RedirectToAction("Index", "Dashboard");
        }
    }
}
