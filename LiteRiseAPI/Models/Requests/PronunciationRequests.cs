namespace LiteRiseAPI.Models.Requests;

public class CheckPronunciationRequest
{
    public int StudentId { get; set; }
    public string TargetWord { get; set; } = string.Empty;
    public string RecognizedText { get; set; } = string.Empty;
    public string? AudioBase64 { get; set; }
    public string? Locale { get; set; } = "en-US";
}

public class EvaluatePronunciationRequest
{
    public int StudentId { get; set; }
    public string TargetText { get; set; } = string.Empty;
    public string RecognizedText { get; set; } = string.Empty;
    public string? AudioBase64 { get; set; }
    public string? Locale { get; set; } = "en-US";
    public string? Context { get; set; }
}
