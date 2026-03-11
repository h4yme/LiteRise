namespace LiteRiseAPI.Models.Requests;

public class SubmitQuizRequest
{
    public int StudentId { get; set; }
    public int LessonId { get; set; }
    public int NodeId { get; set; }
    public List<QuizAnswer> Answers { get; set; } = new();
}

public class QuizAnswer
{
    public int QuestionId { get; set; }
    public string Answer { get; set; } = string.Empty;
    public bool IsCorrect { get; set; }
    public int TimeSpentSeconds { get; set; }
}

public class SubmitResponsesRequest
{
    public int StudentId { get; set; }
    public string SessionId { get; set; } = string.Empty;
    public string AssessmentType { get; set; } = "PreAssessment";
    public List<ItemResponse> Responses { get; set; } = new();
    public bool IsComplete { get; set; }
    public string? DeviceInfo { get; set; }
    public string? AppVersion { get; set; }
}

public class ItemResponse
{
    public int QuestionId { get; set; }
    public string? Answer { get; set; }
    public bool IsCorrect { get; set; }
    public int TimeSpentSeconds { get; set; }
    public double? Theta { get; set; }
}

public class UpdateAbilityRequest
{
    public int StudentId { get; set; }
    public double NewTheta { get; set; }
    public string? AssessmentType { get; set; }
}

public class UpdateNodeProgressRequest
{
    public int StudentId { get; set; }
    public int NodeId { get; set; }
    public bool? LessonCompleted { get; set; }
    public bool? GameCompleted { get; set; }
    public bool? QuizCompleted { get; set; }
    public int? XpEarned { get; set; }
}

public class LogSessionRequest
{
    public int StudentId { get; set; }
    public string SessionType { get; set; } = string.Empty;
    public string? SessionTag { get; set; }
    public string? DeviceInfo { get; set; }
    public string? IpAddress { get; set; }
}
