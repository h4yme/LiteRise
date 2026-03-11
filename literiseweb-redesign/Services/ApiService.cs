using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using System.Web.Configuration;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Website.Services
{
    /// <summary>
    /// Thin wrapper around the LiteRise C# API.
    /// Base URL is read from Web.config key "ApiBaseUrl".
    /// Default: http://localhost:5000
    /// </summary>
    public class ApiService
    {
        private static readonly HttpClient _http;
        private static readonly string     _base;
        private readonly string _authToken;

        static ApiService()
        {
            _base = WebConfigurationManager.AppSettings["ApiBaseUrl"]
                    ?? "http://localhost:5000";

            _http = new HttpClient
            {
                BaseAddress = new Uri(_base.TrimEnd('/') + "/"),
                Timeout     = TimeSpan.FromSeconds(15)
            };
            _http.DefaultRequestHeaders.Add("Accept", "application/json");
        }

        public ApiService(string authToken = null)
        {
            _authToken = authToken;
        }

        // ── Generic helpers ───────────────────────────────────────────

        private async Task<JToken> GetJsonAsync(string endpoint)
        {
            var request = new HttpRequestMessage(HttpMethod.Get, endpoint);
            if (!string.IsNullOrEmpty(_authToken))
                request.Headers.Authorization =
                    new AuthenticationHeaderValue("Bearer", _authToken);
            var resp = await _http.SendAsync(request);
            resp.EnsureSuccessStatusCode();
            var json = await resp.Content.ReadAsStringAsync();
            return JToken.Parse(json);
        }

        private async Task<JToken> PostJsonAsync(string endpoint, object data)
        {
            var json    = JsonConvert.SerializeObject(data);
            var request = new HttpRequestMessage(HttpMethod.Post, endpoint)
            {
                Content = new StringContent(json, Encoding.UTF8, "application/json")
            };
            if (!string.IsNullOrEmpty(_authToken))
                request.Headers.Authorization =
                    new AuthenticationHeaderValue("Bearer", _authToken);
            var resp = await _http.SendAsync(request);
            resp.EnsureSuccessStatusCode();
            var body = await resp.Content.ReadAsStringAsync();
            return JToken.Parse(body);
        }

        // ── Portal Auth ───────────────────────────────────────────────

        /// <summary>
        /// Authenticates an Admin or Teacher for the web portal.
        /// Returns: { success, user_id, name, email, role, token, school_id, message }
        /// </summary>
        public async Task<JObject> PortalLoginAsync(string email, string password, string role)
        {
            var token = await PostJsonAsync("api/auth/portal-login", new
            {
                email,
                password,
                role
            });
            return token as JObject ?? new JObject();
        }

        // ── Students ──────────────────────────────────────────────────

        /// <summary>Returns full student list with progress summary.</summary>
        public async Task<JArray> GetStudentsAsync(int? schoolId = null)
        {
            var url = "api/portal/students";
            if (schoolId.HasValue) url += $"?school_id={schoolId}";
            var token = await GetJsonAsync(url);
            return token as JArray ?? new JArray();
        }

        /// <summary>Returns portal-summary detail for a single student.</summary>
        public async Task<JObject> GetStudentProgressAsync(int studentId)
        {
            var token = await GetJsonAsync($"api/portal/students/{studentId}");
            return token as JObject ?? new JObject();
        }

        // ── Assessment ────────────────────────────────────────────────

        /// <summary>Returns pre and post assessment results for a student.</summary>
        public async Task<JObject> GetPlacementProgressAsync(int studentId)
        {
            var token = await GetJsonAsync($"api/progress/placement?student_id={studentId}");
            return token as JObject ?? new JObject();
        }

        // ── Lessons & Modules ─────────────────────────────────────────

        /// <summary>Returns 65-node progress for a student.</summary>
        public async Task<JArray> GetNodeProgressAsync(int studentId)
        {
            var token = await GetJsonAsync($"api/portal/students/{studentId}/nodes");
            return token as JArray ?? new JArray();
        }

        /// <summary>Returns module ladder structure (5 modules × 13 nodes).</summary>
        public async Task<JArray> GetModuleLadderAsync(int studentId)
        {
            var token = await GetJsonAsync($"api/learning/module-ladder?student_id={studentId}");
            // The C# API wraps in { success, modules } — unwrap if needed
            if (token is JObject obj && obj["modules"] is JArray arr)
                return arr;
            return token as JArray ?? new JArray();
        }

        /// <summary>Returns lesson progress summary per lesson.</summary>
        public async Task<JArray> GetLessonProgressAsync(int studentId)
        {
            var token = await GetJsonAsync($"api/portal/students/{studentId}/lesson-progress");
            return token as JArray ?? new JArray();
        }

        // ── Games ─────────────────────────────────────────────────────

        /// <summary>Returns game result history for a student.</summary>
        public async Task<JArray> GetGameResultsAsync(int studentId)
        {
            var token = await GetJsonAsync($"api/portal/students/{studentId}/games");
            return token as JArray ?? new JArray();
        }

        // ── Badges ────────────────────────────────────────────────────

        /// <summary>Returns badges earned by a student.</summary>
        public async Task<JArray> GetBadgesAsync(int studentId)
        {
            var token = await GetJsonAsync($"api/badge?student_id={studentId}");
            // The C# API returns { success, allBadges, earnedBadges } — return earnedBadges
            if (token is JObject obj && obj["earnedBadges"] is JArray arr)
                return arr;
            return token as JArray ?? new JArray();
        }

        // ── Schools ───────────────────────────────────────────────────

        /// <summary>Returns all schools.</summary>
        public async Task<JArray> GetSchoolsAsync()
        {
            var token = await GetJsonAsync("api/portal/schools");
            return token as JArray ?? new JArray();
        }
    }
}
