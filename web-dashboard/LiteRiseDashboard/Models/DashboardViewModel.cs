namespace LiteRiseDashboard.Models
{
    public class DashboardViewModel
    {
        public int TotalStudents { get; set; }
        public int TotalAssessments { get; set; }
        public double AverageAbility { get; set; }
        public int ActiveStudentsToday { get; set; }
    }

    public class Student
    {
        public int StudentID { get; set; }
        public string FirstName { get; set; } = string.Empty;
        public string LastName { get; set; } = string.Empty;
        public string Email { get; set; } = string.Empty;
        public int GradeLevel { get; set; }
        public string? Section { get; set; }
        public double CurrentAbility { get; set; }
        public int TotalXP { get; set; }
        public int CurrentStreak { get; set; }
        public DateTime? LastLogin { get; set; }
    }

    public class TestSession
    {
        public int SessionID { get; set; }
        public int StudentID { get; set; }
        public string SessionType { get; set; } = string.Empty;
        public double InitialTheta { get; set; }
        public double? FinalTheta { get; set; }
        public DateTime StartTime { get; set; }
        public DateTime? EndTime { get; set; }
        public int TotalQuestions { get; set; }
        public int CorrectAnswers { get; set; }
        public double? AccuracyPercentage { get; set; }
        public bool IsCompleted { get; set; }
    }

    public class Item
    {
        public int ItemID { get; set; }
        public string ItemText { get; set; } = string.Empty;
        public string ItemType { get; set; } = string.Empty;
        public string DifficultyLevel { get; set; } = string.Empty;
        public double DifficultyParam { get; set; }
        public double DiscriminationParam { get; set; }
        public double GuessingParam { get; set; }
        public string? CorrectAnswer { get; set; }
        public string? AnswerChoices { get; set; }
        public int GradeLevel { get; set; }
        public bool IsActive { get; set; }
    }
}
