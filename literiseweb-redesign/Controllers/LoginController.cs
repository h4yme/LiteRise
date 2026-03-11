using System;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Services;

namespace Website.Controllers
{
    public class LoginController : Controller
    {
        private readonly ApiService _api = new ApiService();

        // GET /Login
        public ActionResult Index()
        {
            // Already logged in → redirect based on role
            if (Session["UserId"] != null)
            {
                var existingRole = Session["UserRole"]?.ToString()?.ToLower() ?? "admin";
                return existingRole == "teacher"
                    ? RedirectToAction("Index", "TeacherDashboard")
                    : RedirectToAction("Index", "Dashboard");
            }

            return View("LoginView");
        }

        // POST /Login/Login
        [HttpPost]
        [ValidateAntiForgeryToken]
        public async Task<ActionResult> Login(string email, string password, string role)
        {
            if (string.IsNullOrWhiteSpace(email) || string.IsNullOrWhiteSpace(password))
            {
                ViewBag.ErrorMessage = "Email and password are required.";
                return View("LoginView");
            }

            // Normalise role value coming from the hidden input
            role = (role ?? "admin").Trim().ToLower();
            if (role != "admin" && role != "teacher")
                role = "admin";

            try
            {
                var result = await _api.PortalLoginAsync(email, password, role);

                bool success = result?["success"]?.ToObject<bool>() ?? false;
                if (success)
                {
                    Session["UserId"]    = result["user_id"]?.ToObject<int?>();
                    Session["UserEmail"] = result["email"]?.ToString() ?? email;
                    Session["UserName"]  = result["name"]?.ToString();
                    Session["UserRole"]  = result["role"]?.ToString();
                    Session["SchoolId"]  = result["school_id"]?.ToObject<int?>();
                    Session["AuthToken"] = result["token"]?.ToString();

                    var loginRole = result["role"]?.ToString()?.ToLower() ?? "admin";
                    return loginRole == "teacher"
                        ? RedirectToAction("Index", "TeacherDashboard")
                        : RedirectToAction("Index", "Dashboard");
                }

                ViewBag.ErrorMessage = result?["message"]?.ToString()
                                       ?? "Invalid email or password.";
            }
            catch (Exception ex)
            {
                ViewBag.ErrorMessage = "Could not reach the authentication server. " + ex.Message;
            }

            return View("LoginView");
        }

        // GET /Login/Logout
        public ActionResult Logout()
        {
            Session.Clear();
            return RedirectToAction("Index");
        }
    }
}
