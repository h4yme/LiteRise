using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using LiteRiseAPI.Models.Requests;
using LiteRiseAPI.Services;

namespace LiteRiseAPI.Controllers;

/// <summary>
/// Endpoints for pronunciation checking and evaluation.
/// Mirrors check_pronunciation.php and evaluate_pronunciation.php.
/// Uses the Anthropic Claude API to score pronunciation accuracy.
/// </summary>
[ApiController]
[Route("api/[controller]")]
[Authorize]
public class PronunciationController : ControllerBase
{
    private readonly AnthropicService _anthropic;
    private readonly DatabaseService _db;
    private readonly ILogger<PronunciationController> _logger;

    public PronunciationController(
        AnthropicService anthropic,
        DatabaseService db,
        ILogger<PronunciationController> logger)
    {
        _anthropic = anthropic;
        _db = db;
        _logger = logger;
    }

    /// <summary>
    /// POST /api/pronunciation/check – mirrors check_pronunciation.php.
    /// Quick pronunciation check for a single word.
    /// </summary>
    [HttpPost("check")]
    public async Task<IActionResult> CheckPronunciation([FromBody] CheckPronunciationRequest req)
    {
        if (string.IsNullOrEmpty(req.TargetWord) || string.IsNullOrEmpty(req.RecognizedText))
            return BadRequest(new { success = false, error = "target_word and recognized_text are required." });

        try
        {
            var (score, feedback) = await _anthropic.EvaluatePronunciationAsync(
                req.TargetWord, req.RecognizedText);

            bool isCorrect = score >= 70;

            return Ok(new
            {
                success = true,
                targetWord = req.TargetWord,
                recognizedText = req.RecognizedText,
                score,
                feedback,
                isCorrect,
                passed = isCorrect
            });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "CheckPronunciation error");
            return StatusCode(500, new { success = false, error = "Pronunciation check failed." });
        }
    }

    /// <summary>
    /// POST /api/pronunciation/evaluate – mirrors evaluate_pronunciation.php.
    /// Detailed pronunciation evaluation for multi-word or sentence-level speech.
    /// </summary>
    [HttpPost("evaluate")]
    public async Task<IActionResult> EvaluatePronunciation([FromBody] EvaluatePronunciationRequest req)
    {
        if (string.IsNullOrEmpty(req.TargetText) || string.IsNullOrEmpty(req.RecognizedText))
            return BadRequest(new { success = false, error = "target_text and recognized_text are required." });

        try
        {
            var (score, feedback) = await _anthropic.EvaluatePronunciationAsync(
                req.TargetText, req.RecognizedText, req.Context);

            // Detailed word-level comparison
            var targetWords = req.TargetText.Split(' ', StringSplitOptions.RemoveEmptyEntries);
            var recognizedWords = req.RecognizedText.Split(' ', StringSplitOptions.RemoveEmptyEntries);
            int matchCount = targetWords.Count(tw =>
                recognizedWords.Any(rw => string.Equals(rw, tw, StringComparison.OrdinalIgnoreCase)));

            double wordAccuracy = targetWords.Length > 0
                ? Math.Round((double)matchCount / targetWords.Length * 100, 2)
                : 0;

            return Ok(new
            {
                success = true,
                targetText = req.TargetText,
                recognizedText = req.RecognizedText,
                overallScore = score,
                wordAccuracy,
                feedback,
                passed = score >= 70,
                wordCount = new { target = targetWords.Length, matched = matchCount }
            });
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "EvaluatePronunciation error");
            return StatusCode(500, new { success = false, error = "Pronunciation evaluation failed." });
        }
    }
}
