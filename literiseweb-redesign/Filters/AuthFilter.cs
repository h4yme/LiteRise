using System.Web.Mvc;

namespace Website.Filters
{
    /// <summary>
    /// Redirects unauthenticated requests to the Login page.
    /// Applied at the controller level to guard all actions in one place.
    /// </summary>
    public class AuthFilterAttribute : ActionFilterAttribute
    {
        public override void OnActionExecuting(ActionExecutingContext filterContext)
        {
            var session = filterContext.HttpContext.Session;

            if (session == null || session["UserId"] == null)
            {
                filterContext.Result = new RedirectResult("~/Login");
                return;
            }

            base.OnActionExecuting(filterContext);
        }
    }
}
