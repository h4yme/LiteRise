using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using LiteRiseAPI.Models.Requests;
using LiteRiseAPI.Services;

namespace LiteRiseAPI.Controllers;

/// <summary>
/// Endpoints for badges and achievements.
/// Mirrors get_badges.php and award_badge.php.
/// </summary>
[ApiController]
[Route("api/[controller]")]
[Authorize]
public class BadgeController : ControllerBase
{
    private readonly DatabaseService _db;
    private readonly ILogger<BadgeController> _logger;

    public BadgeController(DatabaseService db, ILogger<BadgeController> logger)
    {
        _db = db;
        _logger = logger;
    }

    /// <summary>GET /api/badge?student_id= – mirrors get_badges.php</summary>
    [HttpGet]
    public async Task<IActionResult> GetBadges([FromQuery] int student_id)
    {
        if (student_id == 0)
            return BadRequest(new { success = false, error = "student_id is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();

            // All available badges
            await using var allCmd = new SqlCommand("SELECT * FROM Badges WHERE IsActive = 1 ORDER BY BadgeOrder", conn);
            var allBadges = await ReadAllRowsAsync(allCmd);

            // Earned badges
            await using var earnedCmd = new SqlCommand(@"
                SELECT b.*, sb.EarnedDate
                FROM StudentBadges sb
                JOIN Badges b ON sb.BadgeID = b.BadgeID
                WHERE sb.StudentID = @StudentID
                ORDER BY sb.EarnedDate DESC", conn);
            earnedCmd.Parameters.AddWithValue("@StudentID", student_id);
            var earnedBadges = await ReadAllRowsAsync(earnedCmd);

            return Ok(new { success = true, allBadges, earnedBadges });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetBadges error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve badges." });
        }
    }

    /// <summary>POST /api/badge/award – mirrors award_badge.php</summary>
    [HttpPost("award")]
    public async Task<IActionResult> AwardBadge([FromBody] AwardBadgeRequest req)
    {
        if (req.StudentId == 0 || req.BadgeId == 0)
            return BadRequest(new { success = false, error = "student_id and badge_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();

            // Check already earned
            await using var checkCmd = new SqlCommand(@"
                SELECT COUNT(1) FROM StudentBadges
                WHERE StudentID = @StudentID AND BadgeID = @BadgeID", conn);
            checkCmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            checkCmd.Parameters.AddWithValue("@BadgeID", req.BadgeId);
            var count = (int)(await checkCmd.ExecuteScalarAsync() ?? 0);
            if (count > 0)
                return Ok(new { success = true, message = "Badge already awarded.", alreadyEarned = true });

            // Award badge
            await using var awardCmd = new SqlCommand(@"
                INSERT INTO StudentBadges (StudentID, BadgeID, EarnedDate)
                VALUES (@StudentID, @BadgeID, GETUTCDATE())", conn);
            awardCmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            awardCmd.Parameters.AddWithValue("@BadgeID", req.BadgeId);
            await awardCmd.ExecuteNonQueryAsync();

            // Get badge details for response
            await using var detailCmd = new SqlCommand("SELECT * FROM Badges WHERE BadgeID = @BadgeID", conn);
            detailCmd.Parameters.AddWithValue("@BadgeID", req.BadgeId);
            var badges = await ReadAllRowsAsync(detailCmd);

            return Ok(new { success = true, message = "Badge awarded!", badge = badges.FirstOrDefault(), alreadyEarned = false });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "AwardBadge error");
            return StatusCode(500, new { success = false, error = "Failed to award badge." });
        }
    }

    // ─── Helper ─────────────────────────────────────────────────────────────

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
