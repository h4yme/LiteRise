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

                // ── Serialise to JObject so we can inspect actual field names ──
                var json = JsonConvert.SerializeObject(result);
                var obj  = JObject.Parse(json);

                // Accept token under any common field name
                string token = obj.Value<string>("token")
                            ?? obj.Value<string>("auth_token")
                            ?? obj.Value<string>("access_token")
                            ?? obj.Value<string>("jwt");

                // Accept status / success flags
                string status  = obj.Value<string>("status") ?? "";
                bool   success = obj.Value<bool?>("success") ?? false;

                if (string.IsNullOrEmpty(token) && status != "success" && !success)
                {
                    // Surface the API's own message if available
                    string msg = obj.Value<string>("message")
                              ?? obj.Value<string>("error")
                              ?? obj.Value<string>("msg")
                              ?? "Invalid credentials. Please try again.";
                    ViewBag.Error = msg;
                    return View("LoginView");
                }

                // If the API returned success but token is nested, try data object
                if (string.IsNullOrEmpty(token))
                {
                    var data = obj["data"] as JObject;
                    token = data?.Value<string>("token")
                         ?? data?.Value<string>("auth_token")
                         ?? data?.Value<string>("access_token");
                }

                if (string.IsNullOrEmpty(token))
                {
                    ViewBag.Error = "Login succeeded but no token was returned. Contact support.";
                    return View("LoginView");
                }

                // ── Persist session ───────────────────────────────────────────
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
