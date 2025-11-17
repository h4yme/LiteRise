-- =============================================
-- LiteRise Database Schema
-- SQL Server Database Creation Script
-- =============================================

USE master;
GO

-- Drop database if exists
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'LiteRiseDB')
BEGIN
    ALTER DATABASE LiteRiseDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE LiteRiseDB;
END
GO

-- Create database
CREATE DATABASE LiteRiseDB;
GO

USE LiteRiseDB;
GO

-- =============================================
-- TABLES
-- =============================================

-- Students Table
CREATE TABLE Students (
    StudentID INT PRIMARY KEY IDENTITY(1,1),
    FirstName NVARCHAR(50) NOT NULL,
    LastName NVARCHAR(50) NOT NULL,
    Email NVARCHAR(100) UNIQUE NOT NULL,
    Password NVARCHAR(255) NOT NULL, -- Will store hashed password
    GradeLevel INT NOT NULL CHECK (GradeLevel BETWEEN 4 AND 6),
    Section NVARCHAR(20),
    InitialAbility FLOAT DEFAULT 0.0, -- Initial theta
    CurrentAbility FLOAT DEFAULT 0.0, -- Current theta
    TotalXP INT DEFAULT 0,
    CurrentStreak INT DEFAULT 0,
    LongestStreak INT DEFAULT 0,
    DateCreated DATETIME DEFAULT GETDATE(),
    LastLogin DATETIME NULL,
    IsActive BIT DEFAULT 1
);

-- Teachers Table
CREATE TABLE Teachers (
    TeacherID INT PRIMARY KEY IDENTITY(1,1),
    FirstName NVARCHAR(50) NOT NULL,
    LastName NVARCHAR(50) NOT NULL,
    Email NVARCHAR(100) UNIQUE NOT NULL,
    Password NVARCHAR(255) NOT NULL,
    Department NVARCHAR(50),
    DateCreated DATETIME DEFAULT GETDATE(),
    IsActive BIT DEFAULT 1
);

-- Items (Question Bank)
CREATE TABLE Items (
    ItemID INT PRIMARY KEY IDENTITY(1,1),
    ItemText NVARCHAR(MAX) NOT NULL,
    ItemType NVARCHAR(50) NOT NULL, -- Reading, Grammar, Pronunciation, Spelling, Syntax
    DifficultyLevel NVARCHAR(20) NOT NULL, -- Easy, Medium, Hard
    DifficultyParam FLOAT NOT NULL, -- IRT 'b' parameter
    DiscriminationParam FLOAT NOT NULL, -- IRT 'a' parameter
    GuessingParam FLOAT DEFAULT 0.25, -- IRT 'c' parameter
    CorrectAnswer NVARCHAR(500),
    AnswerChoices NVARCHAR(MAX), -- JSON array for multiple choice
    ImageURL NVARCHAR(500) NULL,
    AudioURL NVARCHAR(500) NULL,
    GradeLevel INT NOT NULL,
    DateCreated DATETIME DEFAULT GETDATE(),
    IsActive BIT DEFAULT 1
);

-- Test Sessions
CREATE TABLE TestSessions (
    SessionID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT FOREIGN KEY REFERENCES Students(StudentID),
    SessionType NVARCHAR(50) NOT NULL, -- PreAssessment, Lesson, PostAssessment, Game
    InitialTheta FLOAT,
    FinalTheta FLOAT,
    StartTime DATETIME DEFAULT GETDATE(),
    EndTime DATETIME NULL,
    TotalQuestions INT DEFAULT 0,
    CorrectAnswers INT DEFAULT 0,
    AccuracyPercentage FLOAT,
    IsCompleted BIT DEFAULT 0
);

-- Student Responses
CREATE TABLE Responses (
    ResponseID INT PRIMARY KEY IDENTITY(1,1),
    SessionID INT FOREIGN KEY REFERENCES TestSessions(SessionID),
    ItemID INT FOREIGN KEY REFERENCES Items(ItemID),
    StudentResponse NVARCHAR(500),
    IsCorrect BIT NOT NULL,
    TimeSpent INT, -- in seconds
    ThetaBeforeResponse FLOAT,
    ThetaAfterResponse FLOAT,
    Timestamp DATETIME DEFAULT GETDATE()
);

-- Badges
CREATE TABLE Badges (
    BadgeID INT PRIMARY KEY IDENTITY(1,1),
    BadgeName NVARCHAR(100) NOT NULL,
    BadgeDescription NVARCHAR(500),
    BadgeIconURL NVARCHAR(500),
    UnlockCondition NVARCHAR(500), -- Description of how to unlock
    XPReward INT DEFAULT 0,
    BadgeCategory NVARCHAR(50) -- Fluency, Comprehension, Speed, Streak, etc.
);

-- Student Badges (Many-to-Many)
CREATE TABLE StudentBadges (
    StudentBadgeID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT FOREIGN KEY REFERENCES Students(StudentID),
    BadgeID INT FOREIGN KEY REFERENCES Badges(BadgeID),
    DateEarned DATETIME DEFAULT GETDATE(),
    UNIQUE(StudentID, BadgeID)
);

-- Lessons/Modules
CREATE TABLE Lessons (
    LessonID INT PRIMARY KEY IDENTITY(1,1),
    LessonTitle NVARCHAR(200) NOT NULL,
    LessonDescription NVARCHAR(MAX),
    LessonContent NVARCHAR(MAX), -- Text content or reference
    RequiredAbility FLOAT, -- Minimum theta required
    GradeLevel INT NOT NULL,
    LessonType NVARCHAR(50), -- Reading, Grammar, Vocabulary, etc.
    DateCreated DATETIME DEFAULT GETDATE(),
    IsActive BIT DEFAULT 1
);

-- Student Progress
CREATE TABLE StudentProgress (
    ProgressID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT FOREIGN KEY REFERENCES Students(StudentID),
    LessonID INT FOREIGN KEY REFERENCES Lessons(LessonID),
    CompletionStatus NVARCHAR(20), -- NotStarted, InProgress, Completed
    Score FLOAT,
    AttemptsCount INT DEFAULT 0,
    LastAttemptDate DATETIME,
    CompletionDate DATETIME NULL
);

-- Game Results
CREATE TABLE GameResults (
    GameResultID INT PRIMARY KEY IDENTITY(1,1),
    SessionID INT FOREIGN KEY REFERENCES TestSessions(SessionID),
    StudentID INT FOREIGN KEY REFERENCES Students(StudentID),
    GameType NVARCHAR(50) NOT NULL, -- SentenceScramble, TimedTrail
    Score INT NOT NULL,
    AccuracyPercentage FLOAT,
    TimeCompleted INT, -- in seconds
    XPEarned INT,
    StreakAchieved INT,
    DatePlayed DATETIME DEFAULT GETDATE()
);

-- Pronunciation Records
CREATE TABLE PronunciationRecords (
    RecordID INT PRIMARY KEY IDENTITY(1,1),
    SessionID INT FOREIGN KEY REFERENCES TestSessions(SessionID),
    StudentID INT FOREIGN KEY REFERENCES Students(StudentID),
    Word NVARCHAR(200) NOT NULL,
    ExpectedPronunciation NVARCHAR(500),
    ActualPronunciation NVARCHAR(500), -- From speech recognition
    AccuracyScore FLOAT, -- 0-100
    FluencyScore FLOAT,
    AudioURL NVARCHAR(500), -- Stored audio file
    Timestamp DATETIME DEFAULT GETDATE()
);

-- Activity Log
CREATE TABLE ActivityLog (
    LogID INT PRIMARY KEY IDENTITY(1,1),
    StudentID INT FOREIGN KEY REFERENCES Students(StudentID),
    ActivityType NVARCHAR(100), -- Login, Logout, LessonStart, LessonComplete, GamePlay, etc.
    ActivityDetails NVARCHAR(MAX),
    Timestamp DATETIME DEFAULT GETDATE()
);

GO

-- =============================================
-- STORED PROCEDURES
-- =============================================

-- SP_StudentLogin
CREATE PROCEDURE SP_StudentLogin
    @Email NVARCHAR(100),
    @Password NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        StudentID,
        FirstName,
        LastName,
        Email,
        GradeLevel,
        Section,
        CurrentAbility,
        TotalXP,
        CurrentStreak,
        LongestStreak,
        Password -- Will verify in PHP
    FROM Students
    WHERE Email = @Email AND IsActive = 1;

    -- Update last login
    UPDATE Students
    SET LastLogin = GETDATE()
    WHERE Email = @Email;
END
GO

-- SP_CreateTestSession
CREATE PROCEDURE SP_CreateTestSession
    @StudentID INT,
    @Type NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @InitialTheta FLOAT;

    -- Get current ability
    SELECT @InitialTheta = CurrentAbility
    FROM Students
    WHERE StudentID = @StudentID;

    -- Create session
    INSERT INTO TestSessions (StudentID, SessionType, InitialTheta)
    VALUES (@StudentID, @Type, @InitialTheta);

    -- Return new session
    SELECT
        SessionID,
        StudentID,
        SessionType,
        InitialTheta,
        StartTime
    FROM TestSessions
    WHERE SessionID = SCOPE_IDENTITY();
END
GO

-- SP_GetPreAssessmentItems
CREATE PROCEDURE SP_GetPreAssessmentItems
AS
BEGIN
    SET NOCOUNT ON;

    -- Get 20 items with varied difficulty
    SELECT TOP 20
        ItemID,
        ItemText,
        ItemType,
        DifficultyLevel,
        AnswerChoices,
        CorrectAnswer,
        DifficultyParam,
        DiscriminationParam,
        GuessingParam
    FROM Items
    WHERE IsActive = 1
    ORDER BY DifficultyParam; -- Start easy, then harder
END
GO

-- SP_SaveResponses
CREATE PROCEDURE SP_SaveResponses
    @SessionID INT,
    @Responses NVARCHAR(MAX) -- JSON: [{"ItemID":1,"Response":"A","IsCorrect":1,"TimeSpent":15}]
AS
BEGIN
    SET NOCOUNT ON;

    -- Parse JSON and insert responses
    INSERT INTO Responses (SessionID, ItemID, StudentResponse, IsCorrect, TimeSpent)
    SELECT
        @SessionID,
        JSON_VALUE(value, '$.ItemID'),
        JSON_VALUE(value, '$.Response'),
        CAST(JSON_VALUE(value, '$.IsCorrect') AS BIT),
        JSON_VALUE(value, '$.TimeSpent')
    FROM OPENJSON(@Responses);

    -- Update session stats
    UPDATE TestSessions
    SET
        TotalQuestions = (SELECT COUNT(*) FROM Responses WHERE SessionID = @SessionID),
        CorrectAnswers = (SELECT COUNT(*) FROM Responses WHERE SessionID = @SessionID AND IsCorrect = 1),
        AccuracyPercentage = (SELECT CAST(COUNT(CASE WHEN IsCorrect = 1 THEN 1 END) AS FLOAT) / COUNT(*) * 100
                              FROM Responses WHERE SessionID = @SessionID)
    WHERE SessionID = @SessionID;
END
GO

-- SP_UpdateStudentAbility
CREATE PROCEDURE SP_UpdateStudentAbility
    @StudentID INT,
    @NewTheta FLOAT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE Students
    SET CurrentAbility = @NewTheta
    WHERE StudentID = @StudentID;

    SELECT CurrentAbility FROM Students WHERE StudentID = @StudentID;
END
GO

-- SP_GetStudentProgress
CREATE PROCEDURE SP_GetStudentProgress
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        s.FirstName,
        s.LastName,
        s.CurrentAbility,
        s.TotalXP,
        s.CurrentStreak,
        s.LongestStreak,
        COUNT(DISTINCT ts.SessionID) AS TotalSessions,
        AVG(ts.AccuracyPercentage) AS AverageAccuracy,
        COUNT(DISTINCT sb.BadgeID) AS TotalBadges
    FROM Students s
    LEFT JOIN TestSessions ts ON s.StudentID = ts.StudentID
    LEFT JOIN StudentBadges sb ON s.StudentID = sb.StudentID
    WHERE s.StudentID = @StudentID
    GROUP BY s.FirstName, s.LastName, s.CurrentAbility, s.TotalXP, s.CurrentStreak, s.LongestStreak;
END
GO

-- SP_GetLessonsByAbility
CREATE PROCEDURE SP_GetLessonsByAbility
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @CurrentAbility FLOAT;

    SELECT @CurrentAbility = CurrentAbility
    FROM Students
    WHERE StudentID = @StudentID;

    -- Return lessons within ability range
    SELECT
        LessonID,
        LessonTitle,
        LessonDescription,
        RequiredAbility,
        GradeLevel,
        LessonType
    FROM Lessons
    WHERE RequiredAbility <= @CurrentAbility + 0.5 -- Allow slight challenge
      AND IsActive = 1
    ORDER BY RequiredAbility;
END
GO

-- SP_SaveGameResult
CREATE PROCEDURE SP_SaveGameResult
    @SessionID INT,
    @StudentID INT,
    @GameType NVARCHAR(50),
    @Score INT,
    @AccuracyPercentage FLOAT,
    @TimeCompleted INT,
    @XPEarned INT,
    @StreakAchieved INT
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO GameResults (SessionID, StudentID, GameType, Score, AccuracyPercentage, TimeCompleted, XPEarned, StreakAchieved)
    VALUES (@SessionID, @StudentID, @GameType, @Score, @AccuracyPercentage, @TimeCompleted, @XPEarned, @StreakAchieved);

    -- Update student XP and streak
    UPDATE Students
    SET
        TotalXP = TotalXP + @XPEarned,
        CurrentStreak = @StreakAchieved,
        LongestStreak = CASE WHEN @StreakAchieved > LongestStreak THEN @StreakAchieved ELSE LongestStreak END
    WHERE StudentID = @StudentID;

    SELECT @@ROWCOUNT AS RowsAffected;
END
GO

-- SP_GetSentenceScrambleData
CREATE PROCEDURE SP_GetSentenceScrambleData
    @GradeLevel INT,
    @Count INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@Count)
        ItemID,
        ItemText,
        CorrectAnswer,
        DifficultyLevel
    FROM Items
    WHERE ItemType = 'Syntax'
      AND GradeLevel = @GradeLevel
      AND IsActive = 1
    ORDER BY NEWID(); -- Random order
END
GO

-- SP_GetTimedTrailData
CREATE PROCEDURE SP_GetTimedTrailData
    @GradeLevel INT,
    @Count INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@Count)
        ItemID,
        ItemText,
        ItemType,
        CorrectAnswer,
        AnswerChoices,
        DifficultyLevel
    FROM Items
    WHERE ItemType IN ('Spelling', 'Grammar', 'Pronunciation')
      AND GradeLevel = @GradeLevel
      AND IsActive = 1
    ORDER BY NEWID();
END
GO

-- SP_CheckBadgeUnlock
CREATE PROCEDURE SP_CheckBadgeUnlock
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Example: Check for "First Steps" badge
    IF NOT EXISTS (SELECT 1 FROM StudentBadges WHERE StudentID = @StudentID AND BadgeID = 1)
    BEGIN
        IF EXISTS (SELECT 1 FROM TestSessions WHERE StudentID = @StudentID AND SessionType = 'PreAssessment' AND IsCompleted = 1)
        BEGIN
            INSERT INTO StudentBadges (StudentID, BadgeID) VALUES (@StudentID, 1);
        END
    END

    -- Return newly unlocked badges
    SELECT
        b.BadgeID,
        b.BadgeName,
        b.BadgeDescription,
        b.BadgeIconURL,
        b.XPReward
    FROM StudentBadges sb
    JOIN Badges b ON sb.BadgeID = b.BadgeID
    WHERE sb.StudentID = @StudentID
      AND sb.DateEarned >= DATEADD(MINUTE, -5, GETDATE()); -- Earned in last 5 minutes
END
GO

-- =============================================
-- SAMPLE DATA
-- =============================================

-- Insert Badges
INSERT INTO Badges (BadgeName, BadgeDescription, BadgeIconURL, XPReward, BadgeCategory) VALUES
('First Steps', 'Complete your first pre-assessment', 'badge_first_steps.png', 50, 'Achievement'),
('Syntax Master', 'Complete 10 perfect Sentence Scrambles', 'badge_syntax_master.png', 100, 'Fluency'),
('Clear Speaker', 'Achieve 95%+ pronunciation accuracy on 20 words', 'badge_clear_speaker.png', 150, 'Pronunciation'),
('Word Master', 'Unscramble and pronounce 15 words correctly', 'badge_word_master.png', 120, 'Vocabulary'),
('Speed Reader', 'Complete 3 Timed Trails under 45 seconds', 'badge_speed_reader.png', 100, 'Speed'),
('Streak Champion', 'Achieve a 10-question correct streak', 'badge_streak_champion.png', 200, 'Streak'),
('Fluency Pro', 'Reach ability level (theta) above 2.0', 'badge_fluency_pro.png', 250, 'Achievement');

-- Insert Sample Students (Password: 'password123' - should be hashed in production)
INSERT INTO Students (FirstName, LastName, Email, Password, GradeLevel, Section) VALUES
('Maria', 'Santos', 'maria.santos@student.com', '$2y$10$somehashedpassword', 4, 'A'),
('Juan', 'Dela Cruz', 'juan.delacruz@student.com', '$2y$10$somehashedpassword', 5, 'B'),
('Ana', 'Reyes', 'ana.reyes@student.com', '$2y$10$somehashedpassword', 6, 'A');

-- Insert Sample Teachers
INSERT INTO Teachers (FirstName, LastName, Email, Password, Department) VALUES
('Ms. Elena', 'Torres', 'elena.torres@teacher.com', '$2y$10$somehashedpassword', 'Elementary'),
('Mr. Carlos', 'Mendoza', 'carlos.mendoza@teacher.com', '$2y$10$somehashedpassword', 'Elementary');

-- Insert Sample Items (Sentence Scramble - Syntax)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, GradeLevel) VALUES
('homework / diligently / finished / her / Maria', 'Syntax', 'Easy', -1.0, 1.5, 0.0, 'Maria diligently finished her homework.', 4),
('the / quickly / to / ran / school / boy', 'Syntax', 'Easy', -0.8, 1.4, 0.0, 'The boy quickly ran to school.', 4),
('book / interesting / read / I / an', 'Syntax', 'Medium', 0.0, 1.6, 0.0, 'I read an interesting book.', 5),
('carefully / teacher / the / explained / lesson / the', 'Syntax', 'Medium', 0.3, 1.7, 0.0, 'The teacher carefully explained the lesson.', 5),
('despite / challenges / persevered / the / she / numerous', 'Syntax', 'Hard', 1.2, 1.8, 0.0, 'Despite the numerous challenges, she persevered.', 6);

-- Insert Sample Items (Spelling)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel) VALUES
('Choose the correct spelling:', 'Spelling', 'Easy', -0.5, 1.3, 0.33, 'B', '["recieve", "receive", "recive"]', 4),
('Choose the correct spelling:', 'Spelling', 'Medium', 0.2, 1.5, 0.33, 'B', '["acommodation", "accommodation", "accomodation"]', 5),
('Choose the correct spelling:', 'Spelling', 'Hard', 0.9, 1.7, 0.33, 'C', '["seperate", "separete", "separate"]', 6);

-- Insert Sample Items (Grammar)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel) VALUES
('She _ to school every day.', 'Grammar', 'Easy', -0.7, 1.4, 0.33, 'B', '["go", "goes", "going"]', 4),
('They _ playing basketball yesterday.', 'Grammar', 'Medium', 0.1, 1.6, 0.33, 'B', '["was", "were", "are"]', 5),
('If I _ known, I would have helped.', 'Grammar', 'Hard', 1.0, 1.8, 0.33, 'B', '["have", "had", "has"]', 6);

-- Insert Sample Items (Pronunciation)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, GradeLevel) VALUES
('education', 'Pronunciation', 'Easy', -0.6, 1.2, 0.0, 'ed-ju-kei-shun', 4),
('comprehension', 'Pronunciation', 'Medium', 0.3, 1.5, 0.0, 'kom-pri-hen-shun', 5),
('pronunciation', 'Pronunciation', 'Hard', 1.1, 1.7, 0.0, 'pruh-nun-see-ei-shun', 6);

-- Insert Sample Lessons
INSERT INTO Lessons (LessonTitle, LessonDescription, RequiredAbility, GradeLevel, LessonType) VALUES
('Introduction to Reading Comprehension', 'Learn basic reading strategies', -1.0, 4, 'Reading'),
('Building Vocabulary', 'Expand your word knowledge', -0.5, 4, 'Vocabulary'),
('Grammar Fundamentals', 'Understanding sentence structure', 0.0, 5, 'Grammar'),
('Advanced Reading Techniques', 'Master complex texts', 0.5, 5, 'Reading'),
('Fluency Development', 'Improve reading speed and accuracy', 1.0, 6, 'Fluency');

GO

-- =============================================
-- INDEXES FOR PERFORMANCE
-- =============================================

CREATE INDEX IX_Students_Email ON Students(Email);
CREATE INDEX IX_TestSessions_StudentID ON TestSessions(StudentID);
CREATE INDEX IX_Responses_SessionID ON Responses(SessionID);
CREATE INDEX IX_Items_ItemType ON Items(ItemType);
CREATE INDEX IX_Items_GradeLevel ON Items(GradeLevel);
CREATE INDEX IX_GameResults_StudentID ON GameResults(StudentID);
CREATE INDEX IX_StudentProgress_StudentID ON StudentProgress(StudentID);

GO

PRINT 'Database schema created successfully!';
