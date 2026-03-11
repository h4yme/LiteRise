using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using LiteRiseAPI.Services;

namespace LiteRiseAPI.Controllers;

/// <summary>
/// Portal-specific endpoints for admin and teacher dashboards.
/// Provides student listing, school listing, and per-student detail data
/// needed by the LiteRise web portal (literiseweb-redesign).
/// </summary>
[ApiController]
[Route("api/[controller]")]
[Authorize]
public class PortalController : ControllerBase
{
    private readonly DatabaseService _db;
    private readonly ILogger<PortalController> _logger;

    public PortalController(DatabaseService db, ILogger<PortalController> logger)
    {
        _db = db;
        _logger = logger;
    }

    // ─── Students ───────────────────────────────────────────────────────────

    /// <summary>
    /// GET /api/portal/students?school_id=
    /// Returns all active students with portal-summary fields.
    /// Pass school_id to filter by school (for teacher dashboards).
    /// </summary>
    [HttpGet("students")]
    public async Task<IActionResult> GetStudents([FromQuery] int? school_id)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();

            var sql = @"
                SELECT
                    S.StudentID                                         AS student_id,
                    S.FirstName + ' ' + S.LastName                     AS name,
                    S.GradeLevel                                        AS grade,
                    S.TotalXP                                           AS total_xp,
                    S.CurrentStreak                                     AS streak_days,
                    SC.SchoolName                                       AS school_name,
                    S.SchoolID                                          AS school_id,
                    CASE
                        WHEN S.LastLogin >= DATEADD(day,-7,GETUTCDATE()) THEN 'active'
                        ELSE 'inactive'
                    END                                                 AS status,
                    CONVERT(varchar(10), S.LastLogin, 120)              AS last_active,
                    S.PreAssessmentTheta                                AS pre_theta,
                    S.PostAssessmentTheta                               AS post_theta,
                    CASE
                        WHEN S.PreAssessmentTheta IS NULL  THEN NULL
                        WHEN S.PreAssessmentTheta < -0.5   THEN 'beginner'
                        WHEN S.PreAssessmentTheta <  0.5   THEN 'intermediate'
                        ELSE 'advanced'
                    END                                                 AS placement_level,
                    COALESCE(lp.lessons_done, 0)                        AS lessons_done
                FROM Students S
                LEFT JOIN Schools SC        ON S.SchoolID   = SC.SchoolID
                LEFT JOIN (
                    SELECT StudentID, COUNT(*) AS lessons_done
                    FROM   StudentNodeProgress
                    WHERE  LessonCompleted = 1
                    GROUP  BY StudentID
                ) lp ON S.StudentID = lp.StudentID
                WHERE S.IsActive = 1";

            if (school_id.HasValue) sql += " AND S.SchoolID = @SchoolID";
            sql += " ORDER BY S.LastName, S.FirstName";

            await using var cmd = new SqlCommand(sql, conn);
            if (school_id.HasValue) cmd.Parameters.AddWithValue("@SchoolID", school_id.Value);

            var students = await ReadAllRowsAsync(cmd);
            return Ok(students);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetStudents error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve students." });
        }
    }

    /// <summary>
    /// GET /api/portal/students/{id}
    /// Returns portal-view detail for a single student.
    /// </summary>
    [HttpGet("students/{id:int}")]
    public async Task<IActionResult> GetStudent(int id)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT
                    S.StudentID                                         AS student_id,
                    S.FirstName + ' ' + S.LastName                     AS name,
                    S.Nickname                                          AS nickname,
                    S.GradeLevel                                        AS grade,
                    S.Gender                                            AS gender,
                    S.TotalXP                                           AS total_xp,
                    S.CurrentStreak                                     AS streak_days,
                    S.LongestStreak                                     AS longest_streak,
                    SC.SchoolName                                       AS school_name,
                    S.SchoolID                                          AS school_id,
                    CASE
                        WHEN S.LastLogin >= DATEADD(day,-7,GETUTCDATE()) THEN 'active'
                        ELSE 'inactive'
                    END                                                 AS status,
                    CONVERT(varchar(10), S.LastLogin, 120)              AS last_active,
                    S.PreAssessmentTheta                                AS pre_theta,
                    S.PostAssessmentTheta                               AS post_theta,
                    CASE
                        WHEN S.PreAssessmentTheta IS NULL  THEN NULL
                        WHEN S.PreAssessmentTheta < -0.5   THEN 'beginner'
                        WHEN S.PreAssessmentTheta <  0.5   THEN 'intermediate'
                        ELSE 'advanced'
                    END                                                 AS placement_level,
                    S.PreAssessmentCompleted                            AS pre_assessment_done,
                    S.AssessmentStatus                                  AS assessment_status
                FROM Students S
                LEFT JOIN Schools SC ON S.SchoolID = SC.SchoolID
                WHERE S.StudentID = @StudentID AND S.IsActive = 1", conn);
            cmd.Parameters.AddWithValue("@StudentID", id);

            await using var reader = await cmd.ExecuteReaderAsync();
            if (!await reader.ReadAsync())
                return NotFound(new { success = false, error = "Student not found." });

            var row = ReadRow(reader);
            return Ok(row);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetStudent error for id={Id}", id);
            return StatusCode(500, new { success = false, error = "Failed to retrieve student." });
        }
    }

    /// <summary>
    /// GET /api/portal/students/{id}/nodes
    /// Returns node progress array for a student.
    /// </summary>
    [HttpGet("students/{id:int}/nodes")]
    public async Task<IActionResult> GetStudentNodes(int id)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT
                    N.NodeID,
                    N.NodeNumber,
                    N.NodeTitle,
                    N.NodeType,
                    COALESCE(SNP.LessonCompleted, 0) AS lesson_completed,
                    COALESCE(SNP.GameCompleted,   0) AS game_completed,
                    COALESCE(SNP.QuizCompleted,   0) AS quiz_completed,
                    CASE
                        WHEN COALESCE(SNP.LessonCompleted,0) = 1
                         AND COALESCE(SNP.GameCompleted,  0) = 1
                         AND COALESCE(SNP.QuizCompleted,  0) = 1
                        THEN 'completed'
                        WHEN SNP.NodeID IS NOT NULL THEN 'in_progress'
                        ELSE 'locked'
                    END AS status
                FROM Nodes N
                LEFT JOIN StudentNodeProgress SNP
                    ON N.NodeID = SNP.NodeID AND SNP.StudentID = @StudentID
                ORDER BY N.NodeNumber", conn);
            cmd.Parameters.AddWithValue("@StudentID", id);

            var nodes = await ReadAllRowsAsync(cmd);
            return Ok(nodes);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetStudentNodes error for id={Id}", id);
            return StatusCode(500, new { success = false, error = "Failed to retrieve node progress." });
        }
    }

    /// <summary>
    /// GET /api/portal/students/{id}/games
    /// Returns game result history for a student.
    /// </summary>
    [HttpGet("students/{id:int}/games")]
    public async Task<IActionResult> GetStudentGames(int id)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT TOP 50
                    GR.ResultID,
                    GR.GameType,
                    GR.Score,
                    GR.XPEarned,
                    GR.TimeTaken,
                    GR.PlayedAt,
                    N.NodeTitle
                FROM GameResults GR
                LEFT JOIN Nodes N ON GR.NodeID = N.NodeID
                WHERE GR.StudentID = @StudentID
                ORDER BY GR.PlayedAt DESC", conn);
            cmd.Parameters.AddWithValue("@StudentID", id);

            var games = await ReadAllRowsAsync(cmd);
            return Ok(games);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetStudentGames error for id={Id}", id);
            return StatusCode(500, new { success = false, error = "Failed to retrieve game results." });
        }
    }

    /// <summary>
    /// GET /api/portal/students/{id}/lesson-progress
    /// Returns lesson-level completion summary for a student.
    /// </summary>
    [HttpGet("students/{id:int}/lesson-progress")]
    public async Task<IActionResult> GetStudentLessonProgress(int id)
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT
                    L.LessonID,
                    L.LessonTitle,
                    L.LessonOrder,
                    COUNT(N.NodeID)                                          AS total_nodes,
                    SUM(COALESCE(SNP.LessonCompleted, 0))                   AS lessons_done,
                    SUM(COALESCE(SNP.GameCompleted,   0))                   AS games_done,
                    SUM(COALESCE(SNP.QuizCompleted,   0))                   AS quizzes_done
                FROM Lessons L
                JOIN Nodes N ON L.LessonID = N.LessonID
                LEFT JOIN StudentNodeProgress SNP
                    ON N.NodeID = SNP.NodeID AND SNP.StudentID = @StudentID
                GROUP BY L.LessonID, L.LessonTitle, L.LessonOrder
                ORDER BY L.LessonOrder", conn);
            cmd.Parameters.AddWithValue("@StudentID", id);

            var lessons = await ReadAllRowsAsync(cmd);
            return Ok(lessons);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetStudentLessonProgress error for id={Id}", id);
            return StatusCode(500, new { success = false, error = "Failed to retrieve lesson progress." });
        }
    }

    // ─── Schools ────────────────────────────────────────────────────────────

    /// <summary>GET /api/portal/schools – returns all schools</summary>
    [HttpGet("schools")]
    public async Task<IActionResult> GetSchools()
    {
        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(
                "SELECT SchoolID AS school_id, SchoolName AS school_name, Address AS address FROM Schools ORDER BY SchoolName",
                conn);

            var schools = await ReadAllRowsAsync(cmd);
            return Ok(schools);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetSchools error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve schools." });
        }
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

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
}
