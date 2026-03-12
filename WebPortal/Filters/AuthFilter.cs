using System.Web.Mvc;

namespace Website.Filters
{
    /// <summary>
    /// Redirects unauthenticated requests to the Login page.
    /// Authentication is considered valid when Session["AuthToken"] is set
    /// (set by LoginController after a successful API login).
    /// Session["UserId"] is also accepted as a fallback check so that either
    /// key being present is sufficient.
    /// </summary>
    public class AuthFilterAttribute : ActionFilterAttribute
    {
        public override void OnActionExecuting(ActionExecutingContext filterContext)
        {
            var session = filterContext.HttpContext.Session;

            bool authenticated =
                !string.IsNullOrEmpty(session["AuthToken"]?.ToString()) ||
                !string.IsNullOrEmpty(session["UserId"]?.ToString());

            if (!authenticated)
            {
                filterContext.Result = new RedirectToRouteResult(
                    new System.Web.Routing.RouteValueDictionary
                    {
                        { "controller", "Login" },
                        { "action",     "Index" }
                    });
            }

            base.OnActionExecuting(filterContext);
        }
    }
}
