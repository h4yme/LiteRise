using System;
using System.Web.Mvc;

namespace Website.Filters
{
    /// <summary>
    /// Ensures the current session belongs to an admin-role user.
    /// Applied in addition to [AuthFilter] on admin-only controllers.
    /// Redirects to Login if Session["UserRole"] is not "admin".
    /// </summary>
    public class AuthorizeAdminAttribute : ActionFilterAttribute
    {
        public override void OnActionExecuting(ActionExecutingContext filterContext)
        {
            var session = filterContext.HttpContext.Session;
            var role    = session["UserRole"]?.ToString() ?? "";

            bool isAdmin = string.Equals(role, "admin", StringComparison.OrdinalIgnoreCase)
                        || string.Equals(role, "administrator", StringComparison.OrdinalIgnoreCase)
                        || string.Equals(role, "superadmin", StringComparison.OrdinalIgnoreCase);

            if (!isAdmin)
            {
                filterContext.Result = new RedirectToRouteResult(
                    new System.Web.Routing.RouteValueDictionary
                    {
                        { "controller", "Login" },
                        { "action",     "Index" }
                    });
                return;
            }

            base.OnActionExecuting(filterContext);
        }
    }
}
