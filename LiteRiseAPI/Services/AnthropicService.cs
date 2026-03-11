using System.Text;
using System.Text.Json;

namespace LiteRiseAPI.Services;

/// <summary>
/// Wraps the Anthropic Messages API for game-content generation.
/// Mirrors the logic in generate_game_content.php.
/// </summary>
public class AnthropicService
{
    private readonly HttpClient _http;
    private readonly string _apiKey;
    private readonly string _model;
    private readonly int _maxTokens;
    private const string ApiUrl = "https://api.anthropic.com/v1/messages";

    public AnthropicService(HttpClient http, IConfiguration config)
    {
        _http = http;
        _apiKey = config["Anthropic:ApiKey"] ?? string.Empty;
        _model = config["Anthropic:Model"] ?? "claude-opus-4-6";
        _maxTokens = int.TryParse(config["Anthropic:MaxTokens"], out var t) ? t : 4000;
    }

    public bool IsConfigured => !string.IsNullOrEmpty(_apiKey);

    /// <summary>
    /// Generate game content using the Claude API.
    /// Returns raw JSON string from the model, or null if generation fails.
    /// </summary>
    public async Task<string?> GenerateGameContentAsync(
        string gameType,
        int gradeLevel,
        string? topic,
        string systemPrompt,
        string userPrompt)
    {
        if (!IsConfigured) return null;

        var requestBody = new
        {
            model = _model,
            max_tokens = _maxTokens,
            system = systemPrompt,
            messages = new[]
            {
                new { role = "user", content = userPrompt }
            }
        };

        var json = JsonSerializer.Serialize(requestBody);
        using var request = new HttpRequestMessage(HttpMethod.Post, ApiUrl);
        request.Headers.Add("x-api-key", _apiKey);
        request.Headers.Add("anthropic-version", "2023-06-01");
        request.Content = new StringContent(json, Encoding.UTF8, "application/json");

        var response = await _http.SendAsync(request);
        if (!response.IsSuccessStatusCode) return null;

        var responseJson = await response.Content.ReadAsStringAsync();
        using var doc = JsonDocument.Parse(responseJson);
        var content = doc.RootElement
            .GetProperty("content")[0]
            .GetProperty("text")
            .GetString();

        return content;
    }

    /// <summary>
    /// Evaluate pronunciation accuracy using the Claude API.
    /// Returns a score 0-100 and brief feedback.
    /// </summary>
    public async Task<(int Score, string Feedback)> EvaluatePronunciationAsync(
        string targetText,
        string recognizedText,
        string? context = null)
    {
        if (!IsConfigured) return (0, "Pronunciation service unavailable.");

        var systemPrompt =
            "You are a pronunciation evaluator for English language learners. " +
            "Evaluate how closely the recognized speech matches the target text. " +
            "Respond with a JSON object: {\"score\": <0-100>, \"feedback\": \"<brief helpful feedback>\"}";

        var userPrompt =
            $"Target text: \"{targetText}\"\n" +
            $"Recognized speech: \"{recognizedText}\"\n" +
            (context != null ? $"Context: {context}\n" : "") +
            "Evaluate the pronunciation and return a JSON response.";

        var requestBody = new
        {
            model = _model,
            max_tokens = 300,
            system = systemPrompt,
            messages = new[] { new { role = "user", content = userPrompt } }
        };

        try
        {
            var json = JsonSerializer.Serialize(requestBody);
            using var request = new HttpRequestMessage(HttpMethod.Post, ApiUrl);
            request.Headers.Add("x-api-key", _apiKey);
            request.Headers.Add("anthropic-version", "2023-06-01");
            request.Content = new StringContent(json, Encoding.UTF8, "application/json");

            var response = await _http.SendAsync(request);
            if (!response.IsSuccessStatusCode) return (0, "Evaluation failed.");

            var responseJson = await response.Content.ReadAsStringAsync();
            using var doc = JsonDocument.Parse(responseJson);
            var text = doc.RootElement
                .GetProperty("content")[0]
                .GetProperty("text")
                .GetString() ?? "{}";

            // Strip markdown code fences if present
            text = text.Trim();
            if (text.StartsWith("```")) text = text[text.IndexOf('\n')..].TrimStart();
            if (text.EndsWith("```")) text = text[..text.LastIndexOf("```")].TrimEnd();

            using var result = JsonDocument.Parse(text);
            var score = result.RootElement.TryGetProperty("score", out var s)
                ? s.GetInt32() : 0;
            var feedback = result.RootElement.TryGetProperty("feedback", out var f)
                ? f.GetString() ?? "" : "";

            return (score, feedback);
        }
        catch
        {
            return (0, "Evaluation error.");
        }
    }
}
