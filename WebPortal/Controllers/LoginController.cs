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
                    return Content("DEBUG: API returned null — check ApiBaseUrl in web.config", "text/plain");

                // ── Serialise to JObject so we can inspect actual field names ──
                var json = JsonConvert.SerializeObject(result);
                var obj  = JObject.Parse(json);

                // DEBUG: return raw JSON so we can see the real API response shape
                // Remove this block once login works
                string token = obj.Value<string>("token")
                            ?? obj.Value<string>("auth_token")
                            ?? obj.Value<string>("access_token")
                            ?? obj.Value<string>("jwt");

                string status  = obj.Value<string>("status") ?? "";
                bool   success = obj.Value<bool?>("success") ?? false;

                if (string.IsNullOrEmpty(token) && status != "success" && !success)
                    return Content("DEBUG API RESPONSE:\n" + json, "text/plain");

                // If the API returned success but token is nested, try data object
                if (string.IsNullOrEmpty(token))
                {
                    var data = obj["data"] as JObject;
                    token = data?.Value<string>("token")
                         ?? data?.Value<string>("auth_token")
                         ?? data?.Value<string>("access_token");
                }

                if (string.IsNullOrEmpty(token))
                    return Content("DEBUG: success=true but no token found.\nFull response:\n" + json, "text/plain");

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
                return Content("DEBUG EXCEPTION: " + ex.GetType().Name + "\n" + ex.Message + "\n\n" + ex.ToString(), "text/plain");
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
