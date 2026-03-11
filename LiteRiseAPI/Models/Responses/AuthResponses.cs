namespace LiteRiseAPI.Models.Responses;

public class ApiResponse<T>
{
    public bool Success { get; set; }
    public string? Error { get; set; }
    public T? Data { get; set; }

    public static ApiResponse<T> Ok(T data) => new() { Success = true, Data = data };
    public static ApiResponse<T> Fail(string error) => new() { Success = false, Error = error };
}

public class LoginResponse
{
    public bool Success { get; set; } = true;
    public int StudentID { get; set; }
    public string FullName { get; set; } = string.Empty;
    public string FirstName { get; set; } = string.Empty;
    public string LastName { get; set; } = string.Empty;
    public string Nickname { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public int GradeLevel { get; set; }
    public string? Section { get; set; }
    public double CurrentAbility { get; set; }
    public double AbilityScore { get; set; }
    public int TotalXP { get; set; }
    public int XP { get; set; }
    public int CurrentStreak { get; set; }
    public int LongestStreak { get; set; }
    public string? LastLogin { get; set; }
    public bool PreAssessmentCompleted { get; set; }
    public string AssessmentStatus { get; set; } = "Not Started";
    public int Cat1_PhonicsWordStudy { get; set; }
    public int Cat2_VocabularyWordKnowledge { get; set; }
    public int Cat3_GrammarAwareness { get; set; }
    public int Cat4_ComprehendingText { get; set; }
    public int Cat5_CreatingComposing { get; set; }
    public string Token { get; set; } = string.Empty;
}

public class RegisterResponse
{
    public bool Success { get; set; } = true;
    public string Message { get; set; } = string.Empty;
    public StudentInfo? Student { get; set; }
    public string Token { get; set; } = string.Empty;
}

public class StudentInfo
{
    public int StudentID { get; set; }
    public string Nickname { get; set; } = string.Empty;
    public string FirstName { get; set; } = string.Empty;
    public string LastName { get; set; } = string.Empty;
    public string FullName { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;
    public string? Birthday { get; set; }
    public string? Gender { get; set; }
    public int GradeLevel { get; set; }
    public int? SchoolID { get; set; }
    public string? Section { get; set; }
    public double CurrentAbility { get; set; }
    public double AbilityScore { get; set; }
    public int TotalXP { get; set; }
    public int XP { get; set; }
    public int CurrentStreak { get; set; }
    public int LongestStreak { get; set; }
    public string? DateCreated { get; set; }
    public bool IsActive { get; set; }
}

public class SimpleSuccessResponse
{
    public bool Success { get; set; } = true;
    public string Message { get; set; } = string.Empty;
}

public class ErrorResponse
{
    public bool Success { get; set; } = false;
    public string Error { get; set; } = string.Empty;
}
