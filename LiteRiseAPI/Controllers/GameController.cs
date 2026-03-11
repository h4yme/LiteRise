using System.Text.Json;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using LiteRiseAPI.Models.Requests;
using LiteRiseAPI.Services;

namespace LiteRiseAPI.Controllers;

/// <summary>
/// Endpoints for game data, AI-powered game content generation, and game results.
/// Mirrors get_game_data.php, generate_game_content.php, save_game_results.php,
/// evaluate_game_pronunciation.php.
/// </summary>
[ApiController]
[Route("api/[controller]")]
[Authorize]
public class GameController : ControllerBase
{
    private readonly DatabaseService _db;
    private readonly AnthropicService _anthropic;
    private readonly ILogger<GameController> _logger;

    private static readonly Dictionary<string, string> GameSystemPrompts = new()
    {
        ["minimal_pairs"] = "Generate minimal pairs for English pronunciation practice. Return JSON with pairs of words that differ by one phoneme.",
        ["timed_trail"] = "Generate vocabulary words with definitions for a timed learning activity. Return JSON array of {word, definition, example} objects.",
        ["picture_match"] = "Generate word-image association pairs for vocabulary learning. Return JSON array of {word, imageHint, category} objects.",
        ["story_sequencing"] = "Generate a short story split into 4-6 sentences for sequencing practice. Return JSON with {title, sentences: []} where sentences are shuffled.",
        ["synonym_sprint"] = "Generate synonym sets for vocabulary practice. Return JSON array of {word, synonyms: [], difficulty} objects.",
        ["dialogue_reading"] = "Generate a short dialogue for reading comprehension. Return JSON with {title, characters, dialogue: [{speaker, line}], questions: []}.",
        ["fill_in_blanks"] = "Generate fill-in-the-blank sentences for grammar practice. Return JSON array of {sentence, blank, answer, options: []} objects.",
        ["sentence_scramble"] = "Generate sentences for unscrambling practice. Return JSON array of {words: [], correctOrder: [], hint} objects.",
        ["word_explosion"] = "Generate phonics word sets. Return JSON array of {word, phonemes: [], category} objects.",
        ["word_hunt"] = "Generate a word hunt puzzle. Return JSON with {words: [], grid: [], clues: []}."
    };

    public GameController(DatabaseService db, AnthropicService anthropic, ILogger<GameController> logger)
    {
        _db = db;
        _anthropic = anthropic;
        _logger = logger;
    }

    /// <summary>GET /api/game/data?student_id=&node_id=&game_type= – mirrors get_game_data.php</summary>
    [HttpGet("data")]
    public async Task<IActionResult> GetGameData(
        [FromQuery] int student_id,
        [FromQuery] int node_id,
        [FromQuery] string game_type)
    {
        if (student_id == 0 || node_id == 0)
            return BadRequest(new { success = false, error = "student_id and node_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                SELECT * FROM GameData
                WHERE NodeID = @NodeID
                  AND (@GameType = '' OR GameType = @GameType)
                ORDER BY DifficultyLevel", conn);
            cmd.Parameters.AddWithValue("@NodeID", node_id);
            cmd.Parameters.AddWithValue("@GameType", game_type ?? "");

            var rows = await ReadAllRowsAsync(cmd);
            return Ok(new { success = true, gameData = rows });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GetGameData error");
            return StatusCode(500, new { success = false, error = "Failed to retrieve game data." });
        }
    }

    /// <summary>
    /// POST /api/game/generate-content – mirrors generate_game_content.php.
    /// Uses Claude API; falls back to cached content in the database.
    /// </summary>
    [HttpPost("generate-content")]
    public async Task<IActionResult> GenerateGameContent([FromBody] GenerateGameContentRequest req)
    {
        if (string.IsNullOrEmpty(req.GameType))
            return BadRequest(new { success = false, error = "game_type is required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();

            // Check for cached content (unless force regenerate)
            if (!req.ForceRegenerate)
            {
                await using var cacheCmd = new SqlCommand(@"
                    SELECT ContentJson, GeneratedAt FROM GeneratedGameContent
                    WHERE NodeID = @NodeID AND GameType = @GameType
                      AND GradeLevel = @GradeLevel
                      AND GeneratedAt > DATEADD(day, -7, GETUTCDATE())
                    ORDER BY GeneratedAt DESC", conn);
                cacheCmd.Parameters.AddWithValue("@NodeID", req.NodeId);
                cacheCmd.Parameters.AddWithValue("@GameType", req.GameType);
                cacheCmd.Parameters.AddWithValue("@GradeLevel", req.GradeLevel);
                await using var cacheReader = await cacheCmd.ExecuteReaderAsync();
                if (await cacheReader.ReadAsync())
                {
                    var cached = cacheReader["ContentJson"]?.ToString();
                    if (!string.IsNullOrEmpty(cached))
                        return Ok(new { success = true, content = JsonSerializer.Deserialize<object>(cached), fromCache = true });
                }
            }

            // Generate with Claude API
            var systemPrompt = GameSystemPrompts.TryGetValue(req.GameType, out var sp)
                ? sp
                : "Generate educational game content. Return valid JSON.";

            var userPrompt =
                $"Generate {req.GameType} game content for Grade {req.GradeLevel} English learners." +
                (req.Topic != null ? $" Topic: {req.Topic}." : "") +
                " Return only valid JSON, no explanation.";

            var generated = await _anthropic.GenerateGameContentAsync(
                req.GameType, req.GradeLevel, req.Topic, systemPrompt, userPrompt);

            if (generated == null)
                return StatusCode(503, new { success = false, error = "Content generation service unavailable." });

            // Strip markdown code fences
            var json = generated.Trim();
            if (json.StartsWith("```")) json = json[json.IndexOf('\n')..].TrimStart();
            if (json.EndsWith("```")) json = json[..json.LastIndexOf("```")].TrimEnd();

            // Cache the result
            await using var saveCmd = new SqlCommand(@"
                MERGE GeneratedGameContent AS target
                USING (VALUES (@NodeID, @GameType, @GradeLevel)) AS source (NodeID, GameType, GradeLevel)
                ON target.NodeID = source.NodeID AND target.GameType = source.GameType AND target.GradeLevel = source.GradeLevel
                WHEN MATCHED THEN
                    UPDATE SET ContentJson = @Content, GeneratedAt = GETUTCDATE()
                WHEN NOT MATCHED THEN
                    INSERT (NodeID, GameType, GradeLevel, ContentJson, GeneratedAt)
                    VALUES (@NodeID, @GameType, @GradeLevel, @Content, GETUTCDATE());", conn);
            saveCmd.Parameters.AddWithValue("@NodeID", req.NodeId);
            saveCmd.Parameters.AddWithValue("@GameType", req.GameType);
            saveCmd.Parameters.AddWithValue("@GradeLevel", req.GradeLevel);
            saveCmd.Parameters.AddWithValue("@Content", json);
            await saveCmd.ExecuteNonQueryAsync();

            return Ok(new { success = true, content = JsonSerializer.Deserialize<object>(json), fromCache = false });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "GenerateGameContent error");
            return StatusCode(500, new { success = false, error = "Failed to generate game content." });
        }
    }

    /// <summary>POST /api/game/save-results – save game session results</summary>
    [HttpPost("save-results")]
    public async Task<IActionResult> SaveGameResults([FromBody] SaveGameResultsRequest req)
    {
        if (req.StudentId == 0 || req.NodeId == 0)
            return BadRequest(new { success = false, error = "student_id and node_id are required." });

        try
        {
            await using var conn = await _db.GetConnectionAsync();
            await using var cmd = new SqlCommand(@"
                INSERT INTO GameResults (StudentID, NodeID, GameType, Score, MaxScore, Passed, TimeSpentSeconds, XpEarned, CompletedAt)
                VALUES (@StudentID, @NodeID, @GameType, @Score, @MaxScore, @Passed, @Time, @XP, GETUTCDATE())", conn);
            cmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            cmd.Parameters.AddWithValue("@NodeID", req.NodeId);
            cmd.Parameters.AddWithValue("@GameType", req.GameType);
            cmd.Parameters.AddWithValue("@Score", req.Score);
            cmd.Parameters.AddWithValue("@MaxScore", req.MaxScore);
            cmd.Parameters.AddWithValue("@Passed", req.Passed);
            cmd.Parameters.AddWithValue("@Time", req.TimeSpentSeconds);
            cmd.Parameters.AddWithValue("@XP", req.XpEarned);
            await cmd.ExecuteNonQueryAsync();

            // Award XP
            if (req.XpEarned > 0)
            {
                await using var xpCmd = new SqlCommand(
                    "UPDATE Students SET TotalXP = TotalXP + @XP WHERE StudentID = @StudentID", conn);
                xpCmd.Parameters.AddWithValue("@XP", req.XpEarned);
                xpCmd.Parameters.AddWithValue("@StudentID", req.StudentId);
                await xpCmd.ExecuteNonQueryAsync();
            }

            // Mark game as completed in node progress
            await using var progressCmd = new SqlCommand(@"
                MERGE StudentNodeProgress AS target
                USING (VALUES (@StudentID, @NodeID)) AS source (StudentID, NodeID)
                ON target.StudentID = source.StudentID AND target.NodeID = source.NodeID
                WHEN MATCHED THEN UPDATE SET GameCompleted = 1, UpdatedAt = GETUTCDATE()
                WHEN NOT MATCHED THEN INSERT (StudentID, NodeID, GameCompleted) VALUES (@StudentID, @NodeID, 1);", conn);
            progressCmd.Parameters.AddWithValue("@StudentID", req.StudentId);
            progressCmd.Parameters.AddWithValue("@NodeID", req.NodeId);
            await progressCmd.ExecuteNonQueryAsync();

            return Ok(new { success = true, message = "Game results saved.", xpEarned = req.XpEarned });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "SaveGameResults error");
            return StatusCode(500, new { success = false, error = "Failed to save game results." });
        }
    }

    /// <summary>POST /api/game/evaluate-pronunciation – mirrors evaluate_game_pronunciation.php</summary>
    [HttpPost("evaluate-pronunciation")]
    public async Task<IActionResult> EvaluateGamePronunciation([FromBody] EvaluateGamePronunciationRequest req)
    {
        if (string.IsNullOrEmpty(req.TargetText) || string.IsNullOrEmpty(req.RecognizedText))
            return BadRequest(new { success = false, error = "target_text and recognized_text are required." });

        try
        {
            var (score, feedback) = await _anthropic.EvaluatePronunciationAsync(
                req.TargetText, req.RecognizedText, $"Game: {req.GameType}");

            return Ok(new { success = true, score, feedback, passed = score >= 70 });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "EvaluateGamePronunciation error");
            return StatusCode(500, new { success = false, error = "Pronunciation evaluation failed." });
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
