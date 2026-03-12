using System;
using System.Threading.Tasks;
using System.Web.Mvc;
using Website.Services;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Website.Controllers
{
    public class LoginController : AsyncController
    {
        [HttpGet]
        public ActionResult Index()
        {
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

                if (result == null)
                {
                    ViewBag.Error = "No response from server. Check API connection.";
                    return View("LoginView");
                }

                var json = JsonConvert.SerializeObject(result);
                var obj  = JObject.Parse(json);

                string token = obj.Value<string>("token")
                            ?? obj.Value<string>("auth_token")
                            ?? obj.Value<string>("access_token")
                            ?? obj.Value<string>("jwt");

                string status  = obj.Value<string>("status") ?? "";
                bool   success = obj.Value<bool?>("success") ?? false;

                if (string.IsNullOrEmpty(token) && status != "success" && !success)
                {
                    ViewBag.Error = obj.Value<string>("message")
                                 ?? obj.Value<string>("error")
                                 ?? "Invalid credentials. Please try again.";
                    return View("LoginView");
                }

                if (string.IsNullOrEmpty(token))
                {
                    var data = obj["data"] as JObject;
                    token = data?.Value<string>("token")
                         ?? data?.Value<string>("auth_token")
                         ?? data?.Value<string>("access_token");
                }

                if (string.IsNullOrEmpty(token))
                {
                    ViewBag.Error = "Login succeeded but no token was returned.";
                    return View("LoginView");
                }

                // ── Persist session ───────────────────────────────────────────
                // AuthFilter checks Session["UserId"] — must be set for auth to pass
                Session["UserId"]    = obj.Value<string>("user_id") ?? obj["user_id"]?.ToString();
                Session["AuthToken"] = token;
                Session["UserRole"]  = obj.Value<string>("role")
                                    ?? obj.Value<string>("user_role")
                                    ?? role ?? "admin";
                Session["UserName"]  = obj.Value<string>("name")
                                    ?? obj.Value<string>("full_name")
                                    ?? obj.Value<string>("email")
                                    ?? email;
                var schoolIdRaw = obj["school_id"];
                Session["SchoolId"] = schoolIdRaw != null && schoolIdRaw.Type != JTokenType.Null
                    ? schoolIdRaw.ToString()
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
