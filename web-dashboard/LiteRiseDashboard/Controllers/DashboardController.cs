using Microsoft.AspNetCore.Mvc;
using LiteRiseDashboard.Models;
using Microsoft.Data.SqlClient;
using Microsoft.Extensions.Configuration;

namespace LiteRiseDashboard.Controllers
{
    public class DashboardController : Controller
    {
        private readonly IConfiguration _configuration;
        private readonly string _connectionString;

        public DashboardController(IConfiguration configuration)
        {
            _configuration = configuration;
            _connectionString = _configuration.GetConnectionString("LiteRiseDB") ?? "";
        }

        public IActionResult Index()
        {
            var model = new DashboardViewModel
            {
                TotalStudents = GetTotalStudents(),
                TotalAssessments = GetTotalAssessments(),
                AverageAbility = GetAverageAbility(),
                ActiveStudentsToday = GetActiveStudentsToday()
            };

            return View(model);
        }

        private int GetTotalStudents()
        {
            using (SqlConnection conn = new SqlConnection(_connectionString))
            {
                conn.Open();
                SqlCommand cmd = new SqlCommand("SELECT COUNT(*) FROM Students WHERE IsActive = 1", conn);
                return (int)cmd.ExecuteScalar();
            }
        }

        private int GetTotalAssessments()
        {
            using (SqlConnection conn = new SqlConnection(_connectionString))
            {
                conn.Open();
                SqlCommand cmd = new SqlCommand("SELECT COUNT(*) FROM TestSessions WHERE IsCompleted = 1", conn);
                return (int)cmd.ExecuteScalar();
            }
        }

        private double GetAverageAbility()
        {
            using (SqlConnection conn = new SqlConnection(_connectionString))
            {
                conn.Open();
                SqlCommand cmd = new SqlCommand("SELECT AVG(CurrentAbility) FROM Students WHERE IsActive = 1", conn);
                var result = cmd.ExecuteScalar();
                return result != DBNull.Value ? Convert.ToDouble(result) : 0.0;
            }
        }

        private int GetActiveStudentsToday()
        {
            using (SqlConnection conn = new SqlConnection(_connectionString))
            {
                conn.Open();
                SqlCommand cmd = new SqlCommand(
                    "SELECT COUNT(DISTINCT StudentID) FROM ActivityLog WHERE CAST(Timestamp AS DATE) = CAST(GETDATE() AS DATE)",
                    conn);
                return (int)cmd.ExecuteScalar();
            }
        }
    }
}
