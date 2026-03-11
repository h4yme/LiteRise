namespace LiteRiseAPI.Models.Requests;

public class GenerateGameContentRequest
{
    public int StudentId { get; set; }
    public int NodeId { get; set; }
    public string GameType { get; set; } = string.Empty;
    public int GradeLevel { get; set; }
    public string? Topic { get; set; }
    public bool ForceRegenerate { get; set; }
}

public class SaveGameResultsRequest
{
    public int StudentId { get; set; }
    public int NodeId { get; set; }
    public string GameType { get; set; } = string.Empty;
    public int Score { get; set; }
    public int MaxScore { get; set; }
    public bool Passed { get; set; }
    public int TimeSpentSeconds { get; set; }
    public int XpEarned { get; set; }
}

public class EvaluateGamePronunciationRequest
{
    public int StudentId { get; set; }
    public string GameType { get; set; } = string.Empty;
    public string TargetText { get; set; } = string.Empty;
    public string RecognizedText { get; set; } = string.Empty;
    public string? AudioBase64 { get; set; }
}
