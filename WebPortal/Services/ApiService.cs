using System;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Text;
using System.Threading.Tasks;
using Newtonsoft.Json;

namespace Website.Services
{
    public class ApiService
    {
        private readonly HttpClient _client;
        private readonly string _baseUrl;

        public ApiService(string authToken = null)
        {
            _client = new HttpClient();
            _baseUrl = System.Configuration.ConfigurationManager.AppSettings["ApiBaseUrl"]
                       ?? "http://192.168.1.13/api";
            if (!string.IsNullOrEmpty(authToken))
            {
                _client.DefaultRequestHeaders.Authorization =
                    new AuthenticationHeaderValue("Bearer", authToken);
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // LoginAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> LoginAsync(string email, string password, string role)
        {
            var payload = JsonConvert.SerializeObject(new { email, password, role });
            var content = new StringContent(payload, Encoding.UTF8, "application/json");

            var response = await _client.PostAsync($"{_baseUrl}/portal_login.php", content);
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetAllStudentsAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<System.Collections.Generic.List<dynamic>> GetAllStudentsAsync(int? schoolId = null)
        {
            var url = schoolId.HasValue
                ? $"{_baseUrl}/get_all_students.php?school_id={schoolId.Value}"
                : $"{_baseUrl}/get_all_students.php";

            var response = await _client.GetAsync(url);
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<System.Collections.Generic.List<dynamic>>(responseString)
                   ?? new System.Collections.Generic.List<dynamic>();
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetPortalStudentAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetPortalStudentAsync(int studentId)
        {
            var response = await _client.GetAsync($"{_baseUrl}/get_portal_student.php?student_id={studentId}");
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetPlacementProgressAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetPlacementProgressAsync(int studentId)
        {
            var response = await _client.GetAsync($"{_baseUrl}/get_portal_placement_progress.php?student_id={studentId}");
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetNodeProgressAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetNodeProgressAsync(int studentId)
        {
            var response = await _client.GetAsync($"{_baseUrl}/get_portal_node_progress.php?student_id={studentId}");
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetModuleLadderAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetModuleLadderAsync(int studentId)
        {
            var response = await _client.GetAsync($"{_baseUrl}/get_portal_module_ladder.php?student_id={studentId}");
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetGameResultsAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetGameResultsAsync(int studentId)
        {
            var response = await _client.GetAsync($"{_baseUrl}/get_game_results.php?student_id={studentId}");
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetBadgesAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetBadgesAsync(int studentId)
        {
            var response = await _client.GetAsync($"{_baseUrl}/get_badges.php?student_id={studentId}");
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetSchoolsAsync
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetSchoolsAsync()
        {
            var response = await _client.GetAsync($"{_baseUrl}/get_schools.php");
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ═════════════════════════════════════════════════════════════════════
        // NEW METHODS
        // ═════════════════════════════════════════════════════════════════════

        // ─────────────────────────────────────────────────────────────────────
        // GetAssessmentItemsAsync
        // POST get_preassessment_items.php  – returns full pre-assessment bank
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetAssessmentItemsAsync()
        {
            var payload = JsonConvert.SerializeObject(new { });
            var content = new StringContent(payload, Encoding.UTF8, "application/json");

            var response = await _client.PostAsync($"{_baseUrl}/get_preassessment_items.php", content);
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetQuizQuestionsAsync
        // POST get_quiz_questions.php  { node_id: nodeId }
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetQuizQuestionsAsync(int nodeId)
        {
            var payload = JsonConvert.SerializeObject(new { node_id = nodeId });
            var content = new StringContent(payload, Encoding.UTF8, "application/json");

            var response = await _client.PostAsync($"{_baseUrl}/get_quiz_questions.php", content);
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetLessonProgressAsync
        // POST get_lesson_progress.php  { student_id: studentId }
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetLessonProgressAsync(int studentId)
        {
            var payload = JsonConvert.SerializeObject(new { student_id = studentId });
            var content = new StringContent(payload, Encoding.UTF8, "application/json");

            var response = await _client.PostAsync($"{_baseUrl}/get_lesson_progress.php", content);
            response.EnsureSuccessStatusCode();

            var responseString = await response.Content.ReadAsStringAsync();
            return JsonConvert.DeserializeObject<dynamic>(responseString);
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetGameSummaryAsync
        // GET get_game_summary.php  – aggregate game data (endpoint not yet built)
        // Returns null gracefully on any error (404, connection failure, etc.)
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetGameSummaryAsync()
        {
            try
            {
                var response = await _client.GetAsync($"{_baseUrl}/get_game_summary.php");
                response.EnsureSuccessStatusCode();

                var responseString = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<dynamic>(responseString);
            }
            catch (Exception)
            {
                return null;
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetAssessmentSummaryAsync
        // GET get_assessment_summary.php  – aggregate assessment data (not yet built)
        // Returns null gracefully on any error.
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetAssessmentSummaryAsync()
        {
            try
            {
                var response = await _client.GetAsync($"{_baseUrl}/get_assessment_summary.php");
                response.EnsureSuccessStatusCode();

                var responseString = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<dynamic>(responseString);
            }
            catch (Exception)
            {
                return null;
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // ToggleNodeStatusAsync
        // POST toggle_node_status.php  { node_id, enabled }  (placeholder endpoint)
        // Returns null gracefully on any error.
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> ToggleNodeStatusAsync(int nodeId, bool enabled)
        {
            try
            {
                var payload = JsonConvert.SerializeObject(new { node_id = nodeId, enabled });
                var content = new StringContent(payload, Encoding.UTF8, "application/json");

                var response = await _client.PostAsync($"{_baseUrl}/toggle_node_status.php", content);
                response.EnsureSuccessStatusCode();

                var responseString = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<dynamic>(responseString);
            }
            catch (Exception)
            {
                return null;
            }
        }

        // ─────────────────────────────────────────────────────────────────────
        // GetAllSessionsAsync
        // GET get_all_sessions.php  (placeholder endpoint)
        // Returns null gracefully on any error.
        // ─────────────────────────────────────────────────────────────────────
        public async Task<dynamic> GetAllSessionsAsync()
        {
            try
            {
                var response = await _client.GetAsync($"{_baseUrl}/get_all_sessions.php");
                response.EnsureSuccessStatusCode();

                var responseString = await response.Content.ReadAsStringAsync();
                return JsonConvert.DeserializeObject<dynamic>(responseString);
            }
            catch (Exception)
            {
                return null;
            }
        }
    }
}
