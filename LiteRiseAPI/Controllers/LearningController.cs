using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using LiteRiseAPI.Models.Requests;
using LiteRiseAPI.Services;

namespace LiteRiseAPI.Controllers;

/// <summary>
/// Endpoints for learning content: modules, lessons, nodes, placement items.
/// Mirrors get_lessons.php, get_lesson_content.php, get_module_ladder.php,
/// get_next_item.php, get_preassessment_items.php, get_quiz_questions.php,
/// get_scramble_sentences.php, get_word_hunt.php, check_tutorial.php,
/// complete_tutorial.php.
/// </summary>
[ApiController]
[Route("api/[controller]")]
[Authorize]
public class LearningController : ControllerBase
{
    private readonly DatabaseService _db;
    private readonly ILogger<LearningController> _logger;

    public LearningController(DatabaseService db, ILogger<LearningController> logger)
    {
        _db = db;
        _logger = logger;
    }

    // ─── Modules & lessons ─────────────────────────────────────────────────

    /// <summary>GET /api/learning/lessons?student_id=&module_id= – mirrors get_lessons.php</summary>
    [HttpGet("lessons")]
    public async Task<IActionResult> GetLessons([FromQuery] int student_id, [FromQuery] int module_id)
    {
        if (student_id == 0 || module_id == 0)
            return BadRequest(new { success = false, error = "student_id and module_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT L.*,
                       COALESCE(SP.IsCompleted, 0) AS IsCompleted,
                       COALESCE(SP.Score, 0) AS Score
                FROM Lessons L
                LEFT JOIN StudentProgress SP
                    ON L.LessonID = SP.LessonID AND SP.StudentID = @StudentID
                WHERE L.ModuleID = @ModuleID
                ORDER BY L.LessonOrder", conn);
            cmd.Parameters.AddWithValue("@StudentID", student_id);
            cmd.Parameters.AddWithValue("@ModuleID", module_id);

            var lessons = await ReadAllRowsAsync(cmd);
            return Ok(new { success = true, lessons });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetLessons error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve lessons." });
        }
    }

    /// <summary>POST /api/learning/lesson-content – mirrors get_lesson_content.php</summary>
    [HttpPost("lesson-content")]
    public async Task<IActionResult> GetLessonContent([FromBody] GetLessonContentRequest req)
    {
        if (req.StudentId == 0 || req.LessonId == 0)
            return BadRequest(new { success = false, error = "student_id and lesson_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT L.*, M.ModuleName, M.Category
                FROM Lessons L
                JOIN LearningModules M ON L.ModuleID = M.ModuleID
                WHERE L.LessonID = @LessonID", conn);
            cmd.Parameters.AddWithValue("@LessonID", req.LessonId);

            var lessons = await ReadAllRowsAsync(cmd);
            if (lessons.Count == 0)
                return NotFound(new { success = false, error = "Lesson not found." });

            // Get lesson nodes
            await using var nodeCmd = new SqlCommand(@"
                SELECT N.*,
                       COALESCE(SNP.LessonCompleted, 0) AS LessonCompleted,
                       COALESCE(SNP.GameCompleted, 0) AS GameCompleted,
                       COALESCE(SNP.QuizCompleted, 0) AS QuizCompleted
                FROM Nodes N
                LEFT JOIN StudentNodeProgress SNP
                    ON N.NodeID = SNP.NodeID AND SNP.StudentID = @StudentID
                WHERE N.LessonID = @LessonID
                ORDER BY N.NodeNumber", conn);
            nodeCmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            nodeCmd.Parameters.AddWithValue("@LessonID", req.LessonId);

            var nodes = await ReadAllRowsAsync(nodeCmd);

            return Ok(new { success = true, lesson = lessons[0], nodes });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetLessonContent error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve lesson content." });
        }
    }

    /// <summary>GET /api/learning/module-ladder?student_id=&module_id= – mirrors get_module_ladder.php</summary>
    [HttpGet("module-ladder")]
    public async Task<IActionResult> GetModuleLadder([FromQuery] int student_id, [FromQuery] int module_id)
    {
        if (student_id == 0 || module_id == 0)
            return BadRequest(new { success = false, error = "student_id and module_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();

            // Get student info
            await using var studentCmd = new SqlCommand(
                "SELECT CurrentNodeID, PlacementLevel FROM Students WHERE StudentID = @StudentID", conn);
            studentCmd.Parameters.AddWithValue("@StudentID", student_id);
            await using var studentReader = await studentCmd.ExecuteReaderAsync();
            if (!await studentReader.ReadAsync())
                return NotFound(new { success = false, error = "Student not found." });

            var currentNodeId = studentReader["CurrentNodeID"] != DBNull.Value
                ? Convert.ToInt32(studentReader["CurrentNodeID"]) : (int?)null;
            var placementLevel = studentReader["PlacementLevel"] != DBNull.Value
                ? Convert.ToInt32(studentReader["PlacementLevel"]) : (int?)null;
            await studentReader.CloseAsync();

            // Get nodes with progress
            await using var nodesCmd = new SqlCommand(@"
                SELECT N.*,
                       COALESCE(SNP.LessonCompleted, 0) AS LessonCompleted,
                       COALESCE(SNP.GameCompleted, 0) AS GameCompleted,
                       COALESCE(SNP.QuizCompleted, 0) AS QuizCompleted
                FROM Nodes N
                LEFT JOIN StudentNodeProgress SNP
                    ON N.NodeID = SNP.NodeID AND SNP.StudentID = @StudentID
                WHERE N.ModuleID = @ModuleID
                ORDER BY N.NodeNumber", conn);
            nodesCmd.Parameters.AddWithValue("@StudentID", student_id);
            nodesCmd.Parameters.AddWithValue("@ModuleID", module_id);
            var nodes = await ReadAllRowsAsync(nodesCmd);

            // Get supplemental nodes
            await using var suppCmd = new SqlCommand(@"
                SELECT sn.SupplementalNodeID, sn.AfterNodeID, sn.Title, sn.NodeType,
                       ssp.IsCompleted, ssp.TriggerReason
                FROM SupplementalNodes sn
                JOIN StudentSupplementalProgress ssp
                    ON sn.SupplementalNodeID = ssp.SupplementalNodeID
                WHERE sn.AfterNodeID IN (SELECT NodeID FROM Nodes WHERE ModuleID = @ModuleID)
                  AND ssp.StudentID = @StudentID
                  AND ssp.IsVisible = 1", conn);
            suppCmd.Parameters.AddWithValue("@ModuleID", module_id);
            suppCmd.Parameters.AddWithValue("@StudentID", student_id);
            var supplementalNodes = await ReadAllRowsAsync(suppCmd);

            return Ok(new
            {
                success = true,
                nodes,
                supplementalNodes,
                currentNodeId,
                placementLevel
            });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetModuleLadder error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve module ladder." });
        }
    }

    /// <summary>POST /api/learning/next-item – mirrors get_next_item.php</summary>
    [HttpPost("next-item")]
    public async Task<IActionResult> GetNextItem([FromBody] GetNextItemRequest req)
    {
        if (req.StudentId == 0 || req.ModuleId == 0)
            return BadRequest(new { success = false, error = "student_id and module_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand("EXEC SP_GetNextItem @StudentID = @StudentID, @ModuleID = @ModuleID", conn);
            cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            cmd.Parameters.AddWithValue("@ModuleID", req.ModuleId);

            var rows = await ReadAllRowsAsync(cmd);
            return Ok(new { success = true, item = rows.FirstOrDefault() });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetNextItem error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve next item." });
        }
    }

    // ─── Assessment & quizzes ───────────────────────────────────────────────

    /// <summary>GET /api/learning/preassessment-items?student_id= – mirrors get_preassessment_items.php</summary>
    [HttpGet("preassessment-items")]
    public async Task<IActionResult> GetPreassessmentItems([FromQuery] int student_id)
    {
        if (student_id == 0)
            return BadRequest(new { success = false, error = "student_id is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT TOP 30 pq.*
                FROM PlacementQuestions pq
                WHERE pq.IsActive = 1
                ORDER BY NEWID()", conn);

            var items = await ReadAllRowsAsync(cmd);
            return Ok(new { success = true, items });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetPreassessmentItems error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve pre-assessment items." });
        }
    }

    /// <summary>GET /api/learning/quiz-questions?node_id= – mirrors get_quiz_questions.php</summary>
    [HttpGet("quiz-questions")]
    public async Task<IActionResult> GetQuizQuestions([FromQuery] int node_id)
    {
        if (node_id == 0)
            return BadRequest(new { success = false, error = "node_id is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT * FROM Questions
                WHERE NodeID = @NodeID AND IsActive = 1
                ORDER BY QuestionOrder", conn);
            cmd.Parameters.AddWithValue("@NodeID", node_id);

            var questions = await ReadAllRowsAsync(cmd);
            return Ok(new { success = true, questions });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetQuizQuestions error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve quiz questions." });
        }
    }

    /// <summary>GET /api/learning/scramble-sentences?node_id=&grade_level= – mirrors get_scramble_sentences.php</summary>
    [HttpGet("scramble-sentences")]
    public async Task<IActionResult> GetScrambleSentences([FromQuery] int node_id, [FromQuery] int grade_level = 0)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT * FROM ScrambleSentences
                WHERE NodeID = @NodeID
                ORDER BY DifficultyLevel", conn);
            cmd.Parameters.AddWithValue("@NodeID", node_id);

            var sentences = await ReadAllRowsAsync(cmd);
            return Ok(new { success = true, sentences });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetScrambleSentences error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve scramble sentences." });
        }
    }

    /// <summary>GET /api/learning/word-hunt?node_id=&grade_level= – mirrors get_word_hunt.php</summary>
    [HttpGet("word-hunt")]
    public async Task<IActionResult> GetWordHunt([FromQuery] int node_id, [FromQuery] int grade_level = 0)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT TOP 20 * FROM WordHuntItems
                WHERE NodeID = @NodeID
                ORDER BY NEWID()", conn);
            cmd.Parameters.AddWithValue("@NodeID", node_id);

            var items = await ReadAllRowsAsync(cmd);
            return Ok(new { success = true, items });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetWordHunt error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve word hunt items." });
        }
    }

    // ─── Tutorial ───────────────────────────────────────────────────────────

    /// <summary>POST /api/learning/check-tutorial – mirrors check_tutorial.php</summary>
    [HttpPost("check-tutorial")]
    public async Task<IActionResult> CheckTutorial([FromBody] CheckTutorialRequest req)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT IsCompleted FROM StudentTutorials
                WHERE StudentID = @StudentID AND TutorialKey = @Key", conn);
            cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            cmd.Parameters.AddWithValue("@Key", req.TutorialKey);

            var result = await cmd.ExecuteScalarAsync();
            var completed = result != null && Convert.ToBoolean(result);
            return Ok(new { success = true, tutorial_completed = completed });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "CheckTutorial error");
            return StatusCode(500, new { success = false, error = "Failed to check tutorial." });
        }
    }

    /// <summary>POST /api/learning/complete-tutorial – mirrors complete_tutorial.php</summary>
    [HttpPost("complete-tutorial")]
    public async Task<IActionResult> CompleteTutorial([FromBody] CompleteTutorialRequest req)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                MERGE StudentTutorials AS target
                USING (VALUES (@StudentID, @Key)) AS source (StudentID, TutorialKey)
                ON target.StudentID = source.StudentID AND target.TutorialKey = source.TutorialKey
                WHEN MATCHED THEN
                    UPDATE SET IsCompleted = 1, CompletedAt = GETUTCDATE()
                WHEN NOT MATCHED THEN
                    INSERT (StudentID, TutorialKey, IsCompleted, CompletedAt)
                    VALUES (@StudentID, @Key, 1, GETUTCDATE());", conn);
            cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            cmd.Parameters.AddWithValue("@Key", req.TutorialKey);

            await cmd.ExecuteNonQueryAsync();
            return Ok(new { success = true, message = "Tutorial marked as complete." });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "CompleteTutorial error");
            return StatusCode(500, new { success = false, error = "Failed to complete tutorial." });
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private static async Task<List<Dictionary<string, object?>>> ReadAllRowsAsync(SqlCommand cmd)
    {
        var rows = new List<Dictionary<string, object?>>();
        await using var reader = await cmd.ExecuteReaderAsync();
        while (await reader.ReadAsync())
        {
            var row = new Dictionary<string, object?>();
            for (int i = 0; i < reader.FieldCount; i++)
                row[reader.GetName(i)] = reader.IsDBNull(i) ? null : reader.GetValue(i);
            rows.Add(row);
        }
        return rows;
    }
}
