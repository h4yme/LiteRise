using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web.Configuration;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace Website.Services
{
    /// <summary>
    /// Thin wrapper around the LiteRise PHP API.
    /// Base URL is read from Web.config key "ApiBaseUrl".
    /// Default: http://192.168.1.13/api
    /// </summary>
    public class ApiService
    {
        private static readonly HttpClient _http;
        private static readonly string     _base;
        private readonly string _authToken;

        static ApiService()
        {
            _base = WebConfigurationManager.AppSettings["ApiBaseUrl"]
                    ?? "http://192.168.1.13/api";

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
                    new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", _authToken);
            var resp = await _http.SendAsync(request);
            resp.EnsureSuccessStatusCode();
            var json = await resp.Content.ReadAsStringAsync();
            return JToken.Parse(json);
        }

        private async Task<JToken> PostJsonAsync(string endpoint, Dictionary<string, string> data)
        {
            var request = new HttpRequestMessage(HttpMethod.Post, endpoint)
            {
                Content = new FormUrlEncodedContent(data)
            };
            if (!string.IsNullOrEmpty(_authToken))
                request.Headers.Authorization =
                    new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", _authToken);
            var resp = await _http.SendAsync(request);
            resp.EnsureSuccessStatusCode();
            var json = await resp.Content.ReadAsStringAsync();
            return JToken.Parse(json);
        }

        // ── Portal Auth ───────────────────────────────────────────────

        /// <summary>
        /// Authenticates an Admin or Teacher for the web portal.
        /// role: "admin" | "teacher"
        /// Returns: { success, user_id, name, email, role, message }
        /// </summary>
        public async Task<JObject> PortalLoginAsync(string email, string password, string role)
        {
            var data = new Dictionary<string, string>
            {
                ["email"]    = email,
                ["password"] = password,
                ["role"]     = role
            };
            var token = await PostJsonAsync("portal_login.php", data);
            return token as JObject ?? new JObject();
        }

        // ── Students ──────────────────────────────────────────────────

        /// <summary>Returns full student list with progress summary.</summary>
        public async Task<JArray> GetStudentsAsync(int? schoolId = null)
        {
            var url = "get_student_progress.php";
            if (schoolId.HasValue) url += $"?school_id={schoolId}";
            var token = await GetJsonAsync(url);
            return token as JArray ?? new JArray();
        }

        /// <summary>Returns detailed progress for a single student.</summary>
        public async Task<JObject> GetStudentProgressAsync(int studentId)
        {
            var token = await GetJsonAsync($"get_student_progress.php?student_id={studentId}");
            return token as JObject ?? new JObject();
        }

        // ── Assessment ────────────────────────────────────────────────

        /// <summary>Returns pre and post assessment results for a student.</summary>
        public async Task<JObject> GetPlacementProgressAsync(int studentId)
        {
            var token = await GetJsonAsync($"get_placement_progress.php?student_id={studentId}");
            return token as JObject ?? new JObject();
        }

        // ── Lessons & Modules ─────────────────────────────────────────

        /// <summary>Returns 65-node lesson progress for a student.</summary>
        public async Task<JArray> GetNodeProgressAsync(int studentId)
        {
            var token = await GetJsonAsync($"get_node_progress.php?student_id={studentId}");
            return token as JArray ?? new JArray();
        }

        /// <summary>Returns module ladder structure (5 modules × 13 nodes).</summary>
        public async Task<JArray> GetModuleLadderAsync(int studentId)
        {
            var token = await GetJsonAsync($"get_module_ladder.php?student_id={studentId}");
            return token as JArray ?? new JArray();
        }

        /// <summary>Returns lesson progress summary per lesson.</summary>
        public async Task<JArray> GetLessonProgressAsync(int studentId)
        {
            var token = await GetJsonAsync($"get_lesson_progress.php?student_id={studentId}");
            return token as JArray ?? new JArray();
        }

        // ── Games ─────────────────────────────────────────────────────

        /// <summary>Returns game result history for a student.</summary>
        public async Task<JArray> GetGameResultsAsync(int studentId)
        {
            var token = await GetJsonAsync($"get_game_data.php?student_id={studentId}");
            return token as JArray ?? new JArray();
        }

        // ── Badges ────────────────────────────────────────────────────

        /// <summary>Returns badges earned by a student.</summary>
        public async Task<JArray> GetBadgesAsync(int studentId)
        {
            var token = await GetJsonAsync($"get_badges.php?student_id={studentId}");
            return token as JArray ?? new JArray();
        }

        // ── Schools ───────────────────────────────────────────────────

        /// <summary>Returns all schools.</summary>
        public async Task<JArray> GetSchoolsAsync()
        {
            var token = await GetJsonAsync("get_schools.php");
            return token as JArray ?? new JArray();
        }
    }
}
