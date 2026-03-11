using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using LiteRiseAPI.Models.Requests;
using LiteRiseAPI.Services;

namespace LiteRiseAPI.Controllers;

/// <summary>
/// Endpoints for student progress, ability updates, quiz submission, and session logging.
/// Mirrors submit_quiz.php, submit_responses.php, update_ability.php,
/// update_node_progress.php, get_student_progress.php, get_lesson_progress.php,
/// get_placement_progress.php, log_session.php, check_student_ability.php,
/// check_modules_complete.php.
/// </summary>
[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ProgressController : ControllerBase
{
    private readonly DatabaseService _db;
    private readonly ILogger<ProgressController> _logger;

    public ProgressController(DatabaseService db, ILogger<ProgressController> logger)
    {
        _db = db;
        _logger = logger;
    }

    // ─── Quiz & responses ───────────────────────────────────────────────────

    /// <summary>POST /api/progress/submit-quiz – mirrors submit_quiz.php</summary>
    [HttpPost("submit-quiz")]
    public async Task<IActionResult> SubmitQuiz([FromBody] SubmitQuizRequest req)
    {
        if (req.StudentId == 0 || req.NodeId == 0)
            return BadRequest(new { success = false, error = "student_id and node_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            int correct = req.Answers.Count(a => a.IsCorrect);
            int total = req.Answers.Count;
            double score = total > 0 ? Math.Round((double)correct / total * 100, 2) : 0;
            bool passed = score >= 70;

            await using var cmd = new SqlCommand(@"
                EXEC SP_SubmitQuiz
                    @StudentID = @StudentID,
                    @NodeID = @NodeID,
                    @Score = @Score,
                    @Passed = @Passed,
                    @TotalQuestions = @Total,
                    @CorrectAnswers = @Correct", conn);
            cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            cmd.Parameters.AddWithValue("@NodeID", req.NodeId);
            cmd.Parameters.AddWithValue("@Score", score);
            cmd.Parameters.AddWithValue("@Passed", passed);
            cmd.Parameters.AddWithValue("@Total", total);
            cmd.Parameters.AddWithValue("@Correct", correct);
            await cmd.ExecuteNonQueryAsync();

            return Ok(new { success = true, score, passed, correct, total });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "SubmitQuiz error");
            return StatusCode(500, new { success = false, error = "Failed to submit quiz." });
        }
    }

    /// <summary>POST /api/progress/submit-responses – mirrors submit_responses.php</summary>
    [HttpPost("submit-responses")]
    public async Task<IActionResult> SubmitResponses([FromBody] SubmitResponsesRequest req)
    {
        if (req.StudentId == 0)
            return BadRequest(new { success = false, error = "student_id is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();

            foreach (var r in req.Responses)
            {
                await using var cmd = new SqlCommand(@"
                    INSERT INTO PlacementResponses
                        (StudentID, SessionID, QuestionID, Answer, IsCorrect, TimeSpentSeconds, Theta, AssessmentType)
                    VALUES
                        (@StudentID, @SessionID, @QuestionID, @Answer, @IsCorrect, @Time, @Theta, @Type)", conn);
                cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
                cmd.Parameters.AddWithValue("@SessionID", (object?)req.SessionId ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@QuestionID", r.QuestionId);
                cmd.Parameters.AddWithValue("@Answer", (object?)r.Answer ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@IsCorrect", r.IsCorrect);
                cmd.Parameters.AddWithValue("@Time", r.TimeSpentSeconds);
                cmd.Parameters.AddWithValue("@Theta", (object?)r.Theta ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@Type", req.AssessmentType);
                await cmd.ExecuteNonQueryAsync();
            }

            if (req.IsComplete)
            {
                await using var completeCmd = new SqlCommand(@"
                    EXEC SP_CompleteAssessment
                        @StudentID = @StudentID,
                        @SessionID = @SessionID,
                        @AssessmentType = @Type,
                        @DeviceInfo = @Device,
                        @AppVersion = @Version", conn);
                completeCmd.Parameters.AddWithValue("@StudentID", req.StudentId);
                completeCmd.Parameters.AddWithValue("@SessionID", (object?)req.SessionId ?? DBNull.Value);
                completeCmd.Parameters.AddWithValue("@Type", req.AssessmentType);
                completeCmd.Parameters.AddWithValue("@Device", (object?)req.DeviceInfo ?? DBNull.Value);
                completeCmd.Parameters.AddWithValue("@Version", (object?)req.AppVersion ?? DBNull.Value);
                await completeCmd.ExecuteNonQueryAsync();
            }

            return Ok(new { success = true, message = "Responses recorded." });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "SubmitResponses error");
            return StatusCode(500, new { success = false, error = "Failed to submit responses." });
        }
    }

    // ─── Ability & node progress ────────────────────────────────────────────

    /// <summary>POST /api/progress/update-ability – mirrors update_ability.php</summary>
    [HttpPost("update-ability")]
    public async Task<IActionResult> UpdateAbility([FromBody] UpdateAbilityRequest req)
    {
        if (req.StudentId == 0)
            return BadRequest(new { success = false, error = "student_id is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                UPDATE Students
                SET CurrentAbility = @Theta
                WHERE StudentID = @StudentID", conn);
            cmd.Parameters.AddWithValue("@Theta", req.NewTheta);
            cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            await cmd.ExecuteNonQueryAsync();

            return Ok(new { success = true, newAbility = req.NewTheta });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "UpdateAbility error");
            return StatusCode(500, new { success = false, error = "Failed to update ability." });
        }
    }

    /// <summary>POST /api/progress/update-node-progress – mirrors update_node_progress.php</summary>
    [HttpPost("update-node-progress")]
    public async Task<IActionResult> UpdateNodeProgress([FromBody] UpdateNodeProgressRequest req)
    {
        if (req.StudentId == 0 || req.NodeId == 0)
            return BadRequest(new { success = false, error = "student_id and node_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                MERGE StudentNodeProgress AS target
                USING (VALUES (@StudentID, @NodeID)) AS source (StudentID, NodeID)
                ON target.StudentID = source.StudentID AND target.NodeID = source.NodeID
                WHEN MATCHED THEN
                    UPDATE SET
                        LessonCompleted = CASE WHEN @LessonCompleted IS NOT NULL THEN @LessonCompleted ELSE LessonCompleted END,
                        GameCompleted   = CASE WHEN @GameCompleted   IS NOT NULL THEN @GameCompleted   ELSE GameCompleted   END,
                        QuizCompleted   = CASE WHEN @QuizCompleted   IS NOT NULL THEN @QuizCompleted   ELSE QuizCompleted   END,
                        UpdatedAt       = GETUTCDATE()
                WHEN NOT MATCHED THEN
                    INSERT (StudentID, NodeID, LessonCompleted, GameCompleted, QuizCompleted)
                    VALUES (@StudentID, @NodeID,
                            COALESCE(@LessonCompleted, 0),
                            COALESCE(@GameCompleted, 0),
                            COALESCE(@QuizCompleted, 0));", conn);
            cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            cmd.Parameters.AddWithValue("@NodeID", req.NodeId);
            cmd.Parameters.AddWithValue("@LessonCompleted", (object?)req.LessonCompleted ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@GameCompleted", (object?)req.GameCompleted ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@QuizCompleted", (object?)req.QuizCompleted ?? DBNull.Value);
            await cmd.ExecuteNonQueryAsync();

            // Award XP if provided
            if (req.XpEarned.HasValue && req.XpEarned > 0)
            {
                await using var xpCmd = new SqlCommand(@"
                    UPDATE Students SET TotalXP = TotalXP + @XP WHERE StudentID = @StudentID", conn);
                xpCmd.Parameters.AddWithValue("@XP", req.XpEarned.Value);
                xpCmd.Parameters.AddWithValue("@StudentID", req.StudentId);
                await xpCmd.ExecuteNonQueryAsync();
            }

            return Ok(new { success = true, message = "Progress updated." });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "UpdateNodeProgress error");
            return StatusCode(500, new { success = false, error = "Failed to update node progress." });
        }
    }

    // ─── Progress retrieval ─────────────────────────────────────────────────

    /// <summary>GET /api/progress/student?student_id= – mirrors get_student_progress.php</summary>
    [HttpGet("student")]
    public async Task<IActionResult> GetStudentProgress([FromQuery] int student_id)
    {
        if (student_id == 0)
            return BadRequest(new { success = false, error = "student_id is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT S.StudentID, S.FirstName, S.LastName, S.GradeLevel,
                       S.CurrentAbility, S.TotalXP, S.CurrentStreak, S.LongestStreak,
                       S.PreAssessmentCompleted, S.AssessmentStatus,
                       COUNT(SNP.NodeID) AS NodesAttempted,
                       SUM(CASE WHEN SNP.LessonCompleted = 1 THEN 1 ELSE 0 END) AS LessonsCompleted,
                       SUM(CASE WHEN SNP.GameCompleted = 1 THEN 1 ELSE 0 END) AS GamesCompleted,
                       SUM(CASE WHEN SNP.QuizCompleted = 1 THEN 1 ELSE 0 END) AS QuizzesCompleted
                FROM Students S
                LEFT JOIN StudentNodeProgress SNP ON S.StudentID = SNP.StudentID
                WHERE S.StudentID = @StudentID
                GROUP BY S.StudentID, S.FirstName, S.LastName, S.GradeLevel,
                         S.CurrentAbility, S.TotalXP, S.CurrentStreak, S.LongestStreak,
                         S.PreAssessmentCompleted, S.AssessmentStatus", conn);
            cmd.Parameters.AddWithValue("@StudentID", student_id);

            await using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync())
                return NotFound(new { success = false, error = "Student not found." });

            var data = new Dictionary<string, object?>();
            for (int i = 0; i < reader.FieldCount; i++)
                data[reader.GetName(i)] = reader.IsDBNull(i) ? null : reader.GetValue(i);

            return Ok(new { success = true, progress = data });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetStudentProgress error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve progress." });
        }
    }

    /// <summary>GET /api/progress/lesson?student_id=&lesson_id= – mirrors get_lesson_progress.php</summary>
    [HttpGet("lesson")]
    public async Task<IActionResult> GetLessonProgress([FromQuery] int student_id, [FromQuery] int lesson_id)
    {
        if (student_id == 0 || lesson_id == 0)
            return BadRequest(new { success = false, error = "student_id and lesson_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT N.NodeID, N.NodeNumber, N.NodeTitle, N.NodeType,
                       COALESCE(SNP.LessonCompleted, 0) AS LessonCompleted,
                       COALESCE(SNP.GameCompleted, 0) AS GameCompleted,
                       COALESCE(SNP.QuizCompleted, 0) AS QuizCompleted
                FROM Nodes N
                LEFT JOIN StudentNodeProgress SNP
                    ON N.NodeID = SNP.NodeID AND SNP.StudentID = @StudentID
                WHERE N.LessonID = @LessonID
                ORDER BY N.NodeNumber", conn);
            cmd.Parameters.AddWithValue("@StudentID", student_id);
            cmd.Parameters.AddWithValue("@LessonID", lesson_id);

            var nodes = await ReadAllRowsAsync(cmd);
            return Ok(new { success = true, nodes });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetLessonProgress error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve lesson progress." });
        }
    }

    /// <summary>GET /api/progress/placement?student_id= – mirrors get_placement_progress.php</summary>
    [HttpGet("placement")]
    public async Task<IActionResult> GetPlacementProgress([FromQuery] int student_id)
    {
        if (student_id == 0)
            return BadRequest(new { success = false, error = "student_id is required." });

        // Enforce that authenticated student can only see their own data
        var authenticatedId = GetAuthenticatedStudentId();
        if (authenticatedId != null && authenticatedId != student_id)
            return StatusCode(403, new { success = false, error = "Unauthorized." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand("EXEC SP_GetStudentProgress @StudentID = @StudentID", conn);
            cmd.Parameters.AddWithValue("@StudentID", student_id);
            await using var reader = await cmd.ExecuteReaderAsync();

            // First result set: student info
            if (!await reader.ReadAsync())
                return NotFound(new { success = false, error = "Student not found." });

            var student = ReadRow(reader);

            // Second result set: placement results
            await reader.NextResultAsync();
            var allResults = await ReadAllFromReaderAsync(reader);

            var preResult = allResults.FirstOrDefault(r => r["AssessmentType"]?.ToString() == "PreAssessment");
            var postResult = allResults.FirstOrDefault(r => r["AssessmentType"]?.ToString() == "PostAssessment");

            // Third result set: session history
            await reader.NextResultAsync();
            var sessionHistory = await ReadAllFromReaderAsync(reader);

            // Fourth result set: comparison
            await reader.NextResultAsync();
            Dictionary<string, object?>? comparison = null;
            if (await reader.ReadAsync())
                comparison = ReadRow(reader);

            // Override FinalTheta from authoritative Students.PreAssessmentTheta
            if (preResult != null)
            {
                await reader.CloseAsync();
                await using var thetaCmd = new SqlCommand(
                    "SELECT PreAssessmentTheta FROM dbo.Students WHERE StudentID = @StudentID", conn);
                thetaCmd.Parameters.AddWithValue("@StudentID", student_id);
                var theta = await thetaCmd.ExecuteScalarAsync();
                if (theta != null && theta != DBNull.Value)
                    preResult["FinalTheta"] = Convert.ToDouble(theta);
            }

            return Ok(new { success = true, student, preResult, postResult, comparison, sessionHistory });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetPlacementProgress error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve placement progress." });
        }
    }

    // ─── Session & miscellaneous ────────────────────────────────────────────

    /// <summary>POST /api/progress/log-session – mirrors log_session.php</summary>
    [HttpPost("log-session")]
    public async Task<IActionResult> LogSession([FromBody] LogSessionRequest req)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                INSERT INTO SessionLog (StudentID, SessionType, SessionTag, DeviceInfo, IPAddress, LoggedAt)
                VALUES (@StudentID, @Type, @Tag, @Device, @IP, GETUTCDATE())", conn);
            cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            cmd.Parameters.AddWithValue("@Type", req.SessionType);
            cmd.Parameters.AddWithValue("@Tag", (object?)req.SessionTag ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@Device", (object?)req.DeviceInfo ?? DBNull.Value);
            cmd.Parameters.AddWithValue("@IP", (object?)req.IpAddress ?? DBNull.Value);
            await cmd.ExecuteNonQueryAsync();

            return Ok(new { success = true, message = "Session logged." });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "LogSession error");
            return StatusCode(500, new { success = false, error = "Failed to log session." });
        }
    }

    /// <summary>GET /api/progress/check-ability?student_id= – mirrors check_student_ability.php</summary>
    [HttpGet("check-ability")]
    public async Task<IActionResult> CheckStudentAbility([FromQuery] int student_id)
    {
        if (student_id == 0)
            return BadRequest(new { success = false, error = "student_id is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(
                "SELECT CurrentAbility, PlacementLevel FROM Students WHERE StudentID = @StudentID", conn);
            cmd.Parameters.AddWithValue("@StudentID", student_id);
            await using var reader = await cmd.ExecuteReaderAsync();

            if (!await reader.ReadAsync())
                return NotFound(new { success = false, error = "Student not found." });

            return Ok(new
            {
                success = true,
                currentAbility = reader["CurrentAbility"] != DBNull.Value ? Convert.ToDouble(reader["CurrentAbility"]) : 0,
                placementLevel = reader["PlacementLevel"] != DBNull.Value ? Convert.ToInt32(reader["PlacementLevel"]) : (int?)null
            });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "CheckStudentAbility error");
            return StatusCode(500, new { success = false, error = "Failed to check ability." });
        }
    }

    /// <summary>GET /api/progress/check-modules-complete?student_id= – mirrors check_modules_complete.php</summary>
    [HttpGet("check-modules-complete")]
    public async Task<IActionResult> CheckModulesComplete([FromQuery] int student_id)
    {
        if (student_id == 0)
            return BadRequest(new { success = false, error = "student_id is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand("EXEC SP_CheckModulesComplete @StudentID = @StudentID", conn);
            cmd.Parameters.AddWithValue("@StudentID", student_id);

            await using var reader = await cmd.ExecuteReaderAsync();
            var result = new Dictionary<string, object?>();
            if (await reader.ReadAsync())
                for (int i = 0; i < reader.FieldCount; i++)
                    result[reader.GetName(i)] = reader.IsDBNull(i) ? null : reader.GetValue(i);

            return Ok(new { success = true, data = result });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "CheckModulesComplete error");
            return StatusCode(500, new { success = false, error = "Failed to check module completion." });
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private int? GetAuthenticatedStudentId()
    {
        var claim = User.FindFirst("studentID") ?? User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier);
        if (claim == null) return null;
        return int.TryParse(claim.Value, out var id) ? id : null;
    }

    private static Dictionary<string, object?> ReadRow(SqlDataReader reader)
    {
        var row = new Dictionary<string, object?>();
        for (int i = 0; i < reader.FieldCount; i++)
            row[reader.GetName(i)] = reader.IsDBNull(i) ? null : reader.GetValue(i);
        return row;
    }

    private static async Task<List<Dictionary<string, object?>>> ReadAllRowsAsync(SqlCommand cmd)
    {
        var rows = new List<Dictionary<string, object?>>();
        await using var reader = await cmd.ExecuteReaderAsync();
        while (await reader.ReadAsync()) rows.Add(ReadRow(reader));
        return rows;
    }

    private static async Task<List<Dictionary<string, object?>>> ReadAllFromReaderAsync(SqlDataReader reader)
    {
        var rows = new List<Dictionary<string, object?>>();
        while (await reader.ReadAsync()) rows.Add(ReadRow(reader));
        return rows;
    }
}
