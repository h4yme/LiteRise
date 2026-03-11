namespace LiteRiseAPI.Models.Requests;

public class GetLessonContentRequest
{
    public int StudentId { get; set; }
    public int LessonId { get; set; }
}

public class GetNextItemRequest
{
    public int StudentId { get; set; }
    public int ModuleId { get; set; }
    public int? CurrentNodeId { get; set; }
    public string? SessionId { get; set; }
}

public class CheckTutorialRequest
{
    public int StudentId { get; set; }
    public string TutorialKey { get; set; } = string.Empty;
}

public class CompleteTutorialRequest
{
    public int StudentId { get; set; }
    public string TutorialKey { get; set; } = string.Empty;
}
