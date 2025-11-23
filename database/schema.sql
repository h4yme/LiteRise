-- ============================================
-- LITERISE DATABASE SCHEMA AND STORED PROCEDURES
-- Complete setup for IRT-based Adaptive Testing
-- ============================================

-- ============================================
-- STORED PROCEDURES
-- ============================================

-- SP_CreateTestSession: Create a new test session
IF OBJECT_ID('SP_CreateTestSession', 'P') IS NOT NULL DROP PROCEDURE SP_CreateTestSession;
GO

CREATE PROCEDURE SP_CreateTestSession
    @StudentID INT,
    @Type NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @InitialTheta FLOAT;

    -- Get student's current ability as initial theta
    SELECT @InitialTheta = ISNULL(CurrentAbility, 0.0)
    FROM Students
    WHERE StudentID = @StudentID;

    -- Insert new session
    INSERT INTO TestSessions (StudentID, SessionType, InitialTheta, StartTime, IsCompleted)
    VALUES (@StudentID, @Type, @InitialTheta, GETDATE(), 0);

    -- Return the created session
    SELECT
        SessionID,
        StudentID,
        SessionType,
        InitialTheta,
        StartTime
    FROM TestSessions
    WHERE SessionID = SCOPE_IDENTITY();
END;
GO


-- SP_GetPreAssessmentItems: Get items for pre-assessment
IF OBJECT_ID('SP_GetPreAssessmentItems', 'P') IS NOT NULL DROP PROCEDURE SP_GetPreAssessmentItems;
GO

CREATE PROCEDURE SP_GetPreAssessmentItems
AS
BEGIN
    SET NOCOUNT ON;

    -- Return all active items for adaptive selection
    -- The API will use IRT to select the best items
    SELECT
        ItemID,
        ItemText,
        ItemType,
        DifficultyLevel,
        DifficultyParam,
        DiscriminationParam,
        GuessingParam,
        AnswerChoices,
        CorrectAnswer,
        ImageURL,
        AudioURL,
        Phonetic,
        Definition,
        GradeLevel
    FROM Items
    WHERE IsActive = 1
    ORDER BY DifficultyParam; -- Order by difficulty for initial selection
END;
GO


-- SP_StudentLogin: Authenticate student and return profile
IF OBJECT_ID('SP_StudentLogin', 'P') IS NOT NULL DROP PROCEDURE SP_StudentLogin;
GO

CREATE PROCEDURE SP_StudentLogin
    @Email NVARCHAR(255),
    @Password NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    -- Return student data (password verification done in PHP)
    SELECT
        StudentID,
        FirstName,
        LastName,
        Email,
        Password,
        GradeLevel,
        Section,
        CurrentAbility,
        TotalXP,
        CurrentStreak,
        LongestStreak,
        LastLogin
    FROM Students
    WHERE Email = @Email AND IsActive = 1;

    -- Update last login time
    UPDATE Students
    SET LastLogin = GETDATE()
    WHERE Email = @Email;
END;
GO


-- SP_UpdateStudentAbility: Update student's theta after assessment
IF OBJECT_ID('SP_UpdateStudentAbility', 'P') IS NOT NULL DROP PROCEDURE SP_UpdateStudentAbility;
GO

CREATE PROCEDURE SP_UpdateStudentAbility
    @StudentID INT,
    @NewTheta FLOAT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE Students
    SET CurrentAbility = @NewTheta
    WHERE StudentID = @StudentID;

    SELECT
        StudentID,
        CurrentAbility,
        TotalXP,
        CurrentStreak
    FROM Students
    WHERE StudentID = @StudentID;
END;
GO


-- SP_GetLessonsByAbility: Get lessons appropriate for student's ability
IF OBJECT_ID('SP_GetLessonsByAbility', 'P') IS NOT NULL DROP PROCEDURE SP_GetLessonsByAbility;
GO

CREATE PROCEDURE SP_GetLessonsByAbility
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @CurrentAbility FLOAT;
    DECLARE @GradeLevel INT;

    SELECT
        @CurrentAbility = ISNULL(CurrentAbility, 0.0),
        @GradeLevel = GradeLevel
    FROM Students
    WHERE StudentID = @StudentID;

    -- Return lessons within ability range
    SELECT
        l.LessonID,
        l.LessonTitle,
        l.LessonDescription,
        l.RequiredAbility,
        l.GradeLevel,
        l.LessonType,
        l.ContentURL,
        l.Duration,
        CASE
            WHEN @CurrentAbility >= (l.RequiredAbility - 0.3) THEN 1
            ELSE 0
        END AS IsUnlocked
    FROM Lessons l
    WHERE l.IsActive = 1
      AND l.GradeLevel <= @GradeLevel + 1
    ORDER BY l.RequiredAbility ASC;
END;
GO


-- SP_GetSentenceScrambleData: Get syntax items for Sentence Scramble game
IF OBJECT_ID('SP_GetSentenceScrambleData', 'P') IS NOT NULL DROP PROCEDURE SP_GetSentenceScrambleData;
GO

CREATE PROCEDURE SP_GetSentenceScrambleData
    @GradeLevel INT,
    @Count INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@Count)
        ItemID,
        ItemText,
        ItemType,
        DifficultyLevel,
        DifficultyParam,
        CorrectAnswer,
        AnswerChoices
    FROM Items
    WHERE IsActive = 1
      AND ItemType = 'Syntax'
      AND GradeLevel <= @GradeLevel + 1
    ORDER BY NEWID(); -- Random selection
END;
GO


-- SP_GetTimedTrailData: Get mixed items for Timed Trail game
IF OBJECT_ID('SP_GetTimedTrailData', 'P') IS NOT NULL DROP PROCEDURE SP_GetTimedTrailData;
GO

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
        DifficultyLevel,
        DifficultyParam,
        CorrectAnswer,
        AnswerChoices
    FROM Items
    WHERE IsActive = 1
      AND ItemType IN ('Spelling', 'Grammar', 'Pronunciation')
      AND GradeLevel <= @GradeLevel + 1
    ORDER BY NEWID(); -- Random selection
END;
GO


-- SP_SaveGameResult: Save game results and award XP
IF OBJECT_ID('SP_SaveGameResult', 'P') IS NOT NULL DROP PROCEDURE SP_SaveGameResult;
GO

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

    -- Insert game result
    INSERT INTO GameResults (SessionID, StudentID, GameType, Score, AccuracyPercentage, TimeCompleted, XPEarned, StreakAchieved, DatePlayed)
    VALUES (@SessionID, @StudentID, @GameType, @Score, @AccuracyPercentage, @TimeCompleted, @XPEarned, @StreakAchieved, GETDATE());

    -- Update student XP and streak
    UPDATE Students
    SET TotalXP = TotalXP + @XPEarned,
        CurrentStreak = @StreakAchieved,
        LongestStreak = CASE
            WHEN @StreakAchieved > LongestStreak THEN @StreakAchieved
            ELSE LongestStreak
        END
    WHERE StudentID = @StudentID;
END;
GO


-- SP_CheckBadgeUnlock: Check and unlock badges based on achievements
IF OBJECT_ID('SP_CheckBadgeUnlock', 'P') IS NOT NULL DROP PROCEDURE SP_CheckBadgeUnlock;
GO

CREATE PROCEDURE SP_CheckBadgeUnlock
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @TotalXP INT;
    DECLARE @LongestStreak INT;
    DECLARE @TotalGames INT;

    SELECT
        @TotalXP = TotalXP,
        @LongestStreak = LongestStreak
    FROM Students
    WHERE StudentID = @StudentID;

    SELECT @TotalGames = COUNT(*)
    FROM GameResults
    WHERE StudentID = @StudentID;

    -- Check for new badges to unlock
    INSERT INTO StudentBadges (StudentID, BadgeID, DateEarned)
    SELECT @StudentID, b.BadgeID, GETDATE()
    FROM Badges b
    WHERE b.IsActive = 1
      AND b.BadgeID NOT IN (SELECT BadgeID FROM StudentBadges WHERE StudentID = @StudentID)
      AND (
          (b.RequirementType = 'XP' AND @TotalXP >= b.RequirementValue) OR
          (b.RequirementType = 'Streak' AND @LongestStreak >= b.RequirementValue) OR
          (b.RequirementType = 'Games' AND @TotalGames >= b.RequirementValue)
      );

    -- Return newly unlocked badges
    SELECT
        b.BadgeID,
        b.BadgeName,
        b.BadgeDescription,
        b.BadgeIcon,
        sb.DateEarned
    FROM StudentBadges sb
    JOIN Badges b ON sb.BadgeID = b.BadgeID
    WHERE sb.StudentID = @StudentID
      AND sb.DateEarned >= DATEADD(SECOND, -5, GETDATE()); -- Badges earned in last 5 seconds
END;
GO


-- SP_GetStudentProgress: Get comprehensive student progress
IF OBJECT_ID('SP_GetStudentProgress', 'P') IS NOT NULL DROP PROCEDURE SP_GetStudentProgress;
GO

CREATE PROCEDURE SP_GetStudentProgress
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Student stats
    SELECT
        s.StudentID,
        s.FirstName,
        s.LastName,
        s.CurrentAbility,
        s.TotalXP,
        s.CurrentStreak,
        s.LongestStreak,
        s.GradeLevel,
        (SELECT COUNT(*) FROM StudentBadges WHERE StudentID = @StudentID) AS BadgeCount,
        (SELECT COUNT(*) FROM StudentProgress WHERE StudentID = @StudentID AND CompletionStatus = 'Completed') AS LessonsCompleted,
        (SELECT COUNT(*) FROM TestSessions WHERE StudentID = @StudentID AND IsCompleted = 1) AS AssessmentsCompleted
    FROM Students s
    WHERE s.StudentID = @StudentID;

    -- Recent sessions
    SELECT TOP 5
        SessionID,
        SessionType,
        InitialTheta,
        FinalTheta,
        TotalQuestions,
        CorrectAnswers,
        AccuracyPercentage,
        StartTime,
        EndTime
    FROM TestSessions
    WHERE StudentID = @StudentID AND IsCompleted = 1
    ORDER BY EndTime DESC;
END;
GO


-- SP_StartPostAssessment: Start post-assessment after lessons complete
IF OBJECT_ID('SP_StartPostAssessment', 'P') IS NOT NULL DROP PROCEDURE SP_StartPostAssessment;
GO

CREATE PROCEDURE SP_StartPostAssessment
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @LessonsCompleted INT;
    DECLARE @TotalLessons INT;
    DECLARE @PreTheta FLOAT;

    -- Count completed lessons
    SELECT @LessonsCompleted = COUNT(*)
    FROM StudentProgress
    WHERE StudentID = @StudentID AND CompletionStatus = 'Completed';

    -- Count total assigned lessons
    SELECT @TotalLessons = COUNT(*)
    FROM Lessons l
    JOIN Students s ON l.GradeLevel <= s.GradeLevel + 1
    WHERE s.StudentID = @StudentID AND l.IsActive = 1;

    -- Get pre-assessment theta
    SELECT TOP 1 @PreTheta = FinalTheta
    FROM TestSessions
    WHERE StudentID = @StudentID
      AND SessionType = 'PreAssessment'
      AND IsCompleted = 1
    ORDER BY EndTime DESC;

    -- Check if eligible for post-assessment
    IF @LessonsCompleted < @TotalLessons * 0.8
    BEGIN
        SELECT
            0 AS Eligible,
            'Complete at least 80% of lessons before post-assessment' AS Message,
            @LessonsCompleted AS LessonsCompleted,
            @TotalLessons AS TotalLessons;
        RETURN;
    END

    -- Create post-assessment session
    DECLARE @CurrentTheta FLOAT;
    SELECT @CurrentTheta = CurrentAbility FROM Students WHERE StudentID = @StudentID;

    INSERT INTO TestSessions (StudentID, SessionType, InitialTheta, StartTime, IsCompleted)
    VALUES (@StudentID, 'PostAssessment', @CurrentTheta, GETDATE(), 0);

    SELECT
        1 AS Eligible,
        'Post-assessment started' AS Message,
        SCOPE_IDENTITY() AS SessionID,
        @PreTheta AS PreAssessmentTheta,
        @CurrentTheta AS CurrentTheta,
        @LessonsCompleted AS LessonsCompleted;
END;
GO


-- SP_GetImprovementReport: Calculate learning improvement
IF OBJECT_ID('SP_GetImprovementReport', 'P') IS NOT NULL DROP PROCEDURE SP_GetImprovementReport;
GO

CREATE PROCEDURE SP_GetImprovementReport
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @PreTheta FLOAT;
    DECLARE @PostTheta FLOAT;
    DECLARE @PreDate DATETIME;
    DECLARE @PostDate DATETIME;

    -- Get pre-assessment data
    SELECT TOP 1
        @PreTheta = FinalTheta,
        @PreDate = EndTime
    FROM TestSessions
    WHERE StudentID = @StudentID
      AND SessionType = 'PreAssessment'
      AND IsCompleted = 1
    ORDER BY EndTime ASC; -- First pre-assessment

    -- Get post-assessment data
    SELECT TOP 1
        @PostTheta = FinalTheta,
        @PostDate = EndTime
    FROM TestSessions
    WHERE StudentID = @StudentID
      AND SessionType = 'PostAssessment'
      AND IsCompleted = 1
    ORDER BY EndTime DESC; -- Most recent post-assessment

    -- Calculate improvement
    SELECT
        s.StudentID,
        s.FirstName + ' ' + s.LastName AS StudentName,
        s.GradeLevel,
        @PreTheta AS PreAssessmentTheta,
        @PostTheta AS PostAssessmentTheta,
        ISNULL(@PostTheta, s.CurrentAbility) - ISNULL(@PreTheta, 0) AS ThetaImprovement,
        CASE
            WHEN @PreTheta < -1.0 THEN 'Below Basic'
            WHEN @PreTheta < 0.5 THEN 'Basic'
            WHEN @PreTheta < 1.5 THEN 'Proficient'
            ELSE 'Advanced'
        END AS PreClassification,
        CASE
            WHEN ISNULL(@PostTheta, s.CurrentAbility) < -1.0 THEN 'Below Basic'
            WHEN ISNULL(@PostTheta, s.CurrentAbility) < 0.5 THEN 'Basic'
            WHEN ISNULL(@PostTheta, s.CurrentAbility) < 1.5 THEN 'Proficient'
            ELSE 'Advanced'
        END AS PostClassification,
        @PreDate AS PreAssessmentDate,
        @PostDate AS PostAssessmentDate,
        DATEDIFF(DAY, @PreDate, ISNULL(@PostDate, GETDATE())) AS DaysBetween,
        (SELECT COUNT(*) FROM StudentProgress WHERE StudentID = @StudentID AND CompletionStatus = 'Completed') AS LessonsCompleted,
        (SELECT COUNT(*) FROM GameResults WHERE StudentID = @StudentID) AS GamesPlayed,
        s.TotalXP
    FROM Students s
    WHERE s.StudentID = @StudentID;
END;
GO


-- ============================================
-- INSERT 86 NEW ITEMS (100 total with existing 14)
-- ============================================

-- Check current item count
-- SELECT COUNT(*) AS CurrentItems FROM Items;

-- SPELLING ITEMS (22 new)
-- Very Easy Spelling (b = -2.0 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('Choose the correct spelling:', 'Spelling', 'VeryEasy', -2.0, 1.2, 0.25, 'cat', '["cat", "kat", "catt", "cht"]', 4, 1),
('Choose the correct spelling:', 'Spelling', 'VeryEasy', -1.9, 1.2, 0.25, 'dog', '["dog", "dawg", "dogg", "dag"]', 4, 1),
('Choose the correct spelling:', 'Spelling', 'VeryEasy', -1.8, 1.3, 0.25, 'book', '["book", "bok", "bouk", "buk"]', 4, 1),
('Choose the correct spelling:', 'Spelling', 'VeryEasy', -1.7, 1.2, 0.25, 'girl', '["girl", "gurl", "gerl", "gril"]', 4, 1),
('Choose the correct spelling:', 'Spelling', 'VeryEasy', -1.6, 1.3, 0.25, 'house', '["house", "hous", "howse", "hause"]', 4, 1);

-- Easy Spelling (b = -1.4 to -0.6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('Choose the correct spelling:', 'Spelling', 'Easy', -1.4, 1.3, 0.25, 'friend', '["freind", "friend", "frend", "frind"]', 4, 1),
('Choose the correct spelling:', 'Spelling', 'Easy', -1.2, 1.4, 0.25, 'school', '["school", "scool", "skool", "schol"]', 4, 1),
('Choose the correct spelling:', 'Spelling', 'Easy', -1.0, 1.3, 0.25, 'because', '["becuse", "because", "becaus", "becos"]', 4, 1),
('Choose the correct spelling:', 'Spelling', 'Easy', -0.8, 1.4, 0.25, 'people', '["people", "peple", "poeple", "pepole"]', 4, 1);

-- Medium Spelling (b = -0.4 to +0.4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('Choose the correct spelling:', 'Spelling', 'Medium', -0.3, 1.5, 0.25, 'beautiful', '["beatiful", "beutiful", "beautiful", "beautful"]', 5, 1),
('Choose the correct spelling:', 'Spelling', 'Medium', -0.1, 1.5, 0.25, 'different', '["diferent", "diffrent", "different", "differant"]', 5, 1),
('Choose the correct spelling:', 'Spelling', 'Medium', 0.1, 1.6, 0.25, 'necessary', '["necessary", "neccessary", "necesary", "necessery"]', 5, 1),
('Choose the correct spelling:', 'Spelling', 'Medium', 0.4, 1.5, 0.25, 'definitely', '["definately", "definitely", "definitly", "definatly"]', 5, 1);

-- Hard Spelling (b = +0.6 to +1.4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('Choose the correct spelling:', 'Spelling', 'Hard', 0.6, 1.6, 0.25, 'occurred', '["occured", "occurred", "ocurred", "occurrd"]', 6, 1),
('Choose the correct spelling:', 'Spelling', 'Hard', 0.8, 1.7, 0.25, 'embarrass', '["embarrass", "embarass", "embarras", "embaress"]', 6, 1),
('Choose the correct spelling:', 'Spelling', 'Hard', 1.1, 1.6, 0.25, 'privilege', '["priviledge", "privelege", "privilege", "privlege"]', 6, 1),
('Choose the correct spelling:', 'Spelling', 'Hard', 1.3, 1.7, 0.25, 'rhythm', '["rythm", "rhythym", "rhythm", "rythym"]', 6, 1);

-- Very Hard Spelling (b = +1.6 to +2.2)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('Choose the correct spelling:', 'Spelling', 'VeryHard', 1.6, 1.7, 0.25, 'conscience', '["concience", "consciense", "conscience", "consience"]', 6, 1),
('Choose the correct spelling:', 'Spelling', 'VeryHard', 1.8, 1.6, 0.25, 'mischievous', '["mischievous", "mischevious", "mischievious", "mischevous"]', 6, 1),
('Choose the correct spelling:', 'Spelling', 'VeryHard', 2.0, 1.7, 0.25, 'occasionally', '["occasionaly", "occassionally", "occasionally", "ocassionally"]', 6, 1),
('Choose the correct spelling:', 'Spelling', 'VeryHard', 2.2, 1.6, 0.25, 'superintendent', '["superintendant", "superindendent", "superintendent", "superentendent"]', 6, 1);


-- GRAMMAR ITEMS (22 new)
-- Very Easy Grammar (b = -2.0 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('I ___ a student.', 'Grammar', 'VeryEasy', -2.0, 1.2, 0.25, 'am', '["am", "is", "are", "be"]', 4, 1),
('The cat ___ on the mat.', 'Grammar', 'VeryEasy', -1.9, 1.3, 0.25, 'sits', '["sit", "sits", "sitting", "sitted"]', 4, 1),
('She ___ a red dress.', 'Grammar', 'VeryEasy', -1.7, 1.2, 0.25, 'has', '["have", "has", "having", "haves"]', 4, 1),
('We ___ to the park.', 'Grammar', 'VeryEasy', -1.6, 1.3, 0.25, 'go', '["go", "goes", "going", "went"]', 4, 1),
('The dog ___ loudly.', 'Grammar', 'VeryEasy', -1.5, 1.2, 0.25, 'barks', '["bark", "barks", "barking", "barked"]', 4, 1);

-- Easy Grammar (b = -1.4 to -0.6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('The children ___ playing outside.', 'Grammar', 'Easy', -1.3, 1.4, 0.25, 'are', '["is", "are", "was", "be"]', 4, 1),
('He ___ his homework yesterday.', 'Grammar', 'Easy', -1.1, 1.3, 0.25, 'did', '["do", "does", "did", "done"]', 4, 1),
('My mother ___ delicious food.', 'Grammar', 'Easy', -0.9, 1.4, 0.25, 'cooks', '["cook", "cooks", "cooking", "cooked"]', 4, 1),
('The birds ___ in the sky.', 'Grammar', 'Easy', -0.6, 1.3, 0.25, 'fly', '["fly", "flies", "flying", "flied"]', 4, 1);

-- Medium Grammar (b = -0.4 to +0.4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('Neither the boy nor his friends ___ present.', 'Grammar', 'Medium', -0.2, 1.5, 0.25, 'were', '["was", "were", "is", "are"]', 5, 1),
('Each of the students ___ a book.', 'Grammar', 'Medium', 0.0, 1.6, 0.25, 'has', '["have", "has", "having", "had"]', 5, 1),
('The news ___ very surprising.', 'Grammar', 'Medium', 0.2, 1.5, 0.25, 'was', '["was", "were", "are", "is"]', 5, 1),
('She asked me where I ___ going.', 'Grammar', 'Medium', 0.4, 1.6, 0.25, 'was', '["am", "was", "were", "is"]', 5, 1);

-- Hard Grammar (b = +0.6 to +1.4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('I wish I ___ taller.', 'Grammar', 'Hard', 0.6, 1.7, 0.25, 'were', '["was", "were", "am", "is"]', 6, 1),
('By next year, she ___ here for a decade.', 'Grammar', 'Hard', 0.8, 1.6, 0.25, 'will have been', '["will be", "will have been", "would be", "has been"]', 6, 1),
('The book, along with the pens, ___ on the table.', 'Grammar', 'Hard', 1.2, 1.7, 0.25, 'is', '["is", "are", "was", "were"]', 6, 1),
('He suggested that she ___ the doctor.', 'Grammar', 'Hard', 1.4, 1.6, 0.25, 'see', '["sees", "see", "saw", "seen"]', 6, 1);

-- Very Hard Grammar (b = +1.6 to +2.2)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('Had I known earlier, I ___ you.', 'Grammar', 'VeryHard', 1.6, 1.7, 0.25, 'would have helped', '["will help", "would help", "would have helped", "helped"]', 6, 1),
('Not only ___ late, but he also forgot the documents.', 'Grammar', 'VeryHard', 1.8, 1.6, 0.25, 'was he', '["he was", "was he", "he is", "is he"]', 6, 1),
('Seldom ___ such a beautiful sunset.', 'Grammar', 'VeryHard', 2.0, 1.7, 0.25, 'have I seen', '["I have seen", "have I seen", "I saw", "did I see"]', 6, 1),
('It is essential that he ___ on time.', 'Grammar', 'VeryHard', 2.2, 1.6, 0.25, 'be', '["is", "be", "was", "were"]', 6, 1);


-- SYNTAX ITEMS (20 new)
-- Very Easy Syntax (b = -2.0 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('is / cat / the / big', 'Syntax', 'VeryEasy', -2.0, 1.3, 0.25, 'The cat is big.', NULL, 4, 1),
('run / I / can', 'Syntax', 'VeryEasy', -1.9, 1.2, 0.25, 'I can run.', NULL, 4, 1),
('red / is / apple / the', 'Syntax', 'VeryEasy', -1.7, 1.3, 0.25, 'The apple is red.', NULL, 4, 1),
('happy / am / I', 'Syntax', 'VeryEasy', -1.6, 1.2, 0.25, 'I am happy.', NULL, 4, 1),
('ball / the / round / is', 'Syntax', 'VeryEasy', -1.5, 1.3, 0.25, 'The ball is round.', NULL, 4, 1);

-- Easy Syntax (b = -1.4 to -0.6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('plays / park / in / the / she', 'Syntax', 'Easy', -1.3, 1.4, 0.25, 'She plays in the park.', NULL, 4, 1),
('eats / breakfast / every / he / morning', 'Syntax', 'Easy', -1.1, 1.5, 0.25, 'He eats breakfast every morning.', NULL, 4, 1),
('garden / beautiful / has / a / mother / my', 'Syntax', 'Easy', -0.9, 1.4, 0.25, 'My mother has a beautiful garden.', NULL, 4, 1),
('loudly / sings / the / bird', 'Syntax', 'Easy', -0.7, 1.5, 0.25, 'The bird sings loudly.', NULL, 4, 1);

-- Medium Syntax (b = -0.4 to +0.4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('stories / grandmother / interesting / tells / my / always', 'Syntax', 'Medium', -0.3, 1.6, 0.25, 'My grandmother always tells interesting stories.', NULL, 5, 1),
('library / borrowed / from / books / the / she / two', 'Syntax', 'Medium', -0.1, 1.5, 0.25, 'She borrowed two books from the library.', NULL, 5, 1),
('competition / won / the / student / talented / the', 'Syntax', 'Medium', 0.2, 1.6, 0.25, 'The talented student won the competition.', NULL, 5, 1),
('patiently / waited / the / for / children / bus / the', 'Syntax', 'Medium', 0.5, 1.5, 0.25, 'The children waited patiently for the bus.', NULL, 5, 1);

-- Hard Syntax (b = +0.6 to +1.4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('although / tired / she / was / continued / she / working', 'Syntax', 'Hard', 0.7, 1.7, 0.25, 'Although she was tired, she continued working.', NULL, 6, 1),
('scientist / discovered / the / brilliant / cure / new / a', 'Syntax', 'Hard', 0.9, 1.6, 0.25, 'The brilliant scientist discovered a new cure.', NULL, 6, 1),
('before / leaves / make / fall / sure / the / you / rake', 'Syntax', 'Hard', 1.1, 1.7, 0.25, 'Make sure you rake the leaves before fall.', NULL, 6, 1),
('festival / celebrating / were / cultural / the / community / their', 'Syntax', 'Hard', 1.4, 1.6, 0.25, 'The community were celebrating their cultural festival.', NULL, 6, 1);

-- Very Hard Syntax (b = +1.6 to +2.2)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('unprecedented / challenges / despite / the / organization / succeeded / the', 'Syntax', 'VeryHard', 1.7, 1.7, 0.25, 'Despite the unprecedented challenges, the organization succeeded.', NULL, 6, 1),
('opportunity / remarkable / presented / was / a / students / the / to', 'Syntax', 'VeryHard', 1.9, 1.6, 0.25, 'A remarkable opportunity was presented to the students.', NULL, 6, 1),
('consequences / without / considering / acted / the / he / impulsively', 'Syntax', 'VeryHard', 2.1, 1.7, 0.25, 'He acted impulsively without considering the consequences.', NULL, 6, 1),
('negotiations / diplomatic / resolution / peaceful / the / led / a / to', 'Syntax', 'VeryHard', 2.3, 1.6, 0.25, 'The diplomatic negotiations led to a peaceful resolution.', NULL, 6, 1);


-- PRONUNCIATION ITEMS (22 new)
-- Very Easy Pronunciation (b = -2.0 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('cat', 'Pronunciation', 'VeryEasy', -2.0, 1.1, 0.0, 'kat', NULL, 4, 1),
('dog', 'Pronunciation', 'VeryEasy', -1.9, 1.2, 0.0, 'dawg', NULL, 4, 1),
('book', 'Pronunciation', 'VeryEasy', -1.8, 1.1, 0.0, 'buk', NULL, 4, 1),
('play', 'Pronunciation', 'VeryEasy', -1.7, 1.2, 0.0, 'pley', NULL, 4, 1),
('home', 'Pronunciation', 'VeryEasy', -1.6, 1.1, 0.0, 'hohm', NULL, 4, 1);

-- Easy Pronunciation (b = -1.4 to -0.6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('beautiful', 'Pronunciation', 'Easy', -1.3, 1.3, 0.0, 'byoo-tuh-fuhl', NULL, 4, 1),
('animal', 'Pronunciation', 'Easy', -1.1, 1.2, 0.0, 'an-uh-muhl', NULL, 4, 1),
('important', 'Pronunciation', 'Easy', -0.9, 1.3, 0.0, 'im-por-tuhnt', NULL, 4, 1),
('remember', 'Pronunciation', 'Easy', -0.7, 1.2, 0.0, 'ri-mem-ber', NULL, 4, 1);

-- Medium Pronunciation (b = -0.4 to +0.4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('vocabulary', 'Pronunciation', 'Medium', -0.2, 1.4, 0.0, 'voh-kab-yuh-ler-ee', NULL, 5, 1),
('environment', 'Pronunciation', 'Medium', 0.0, 1.5, 0.0, 'en-vahy-ruhn-muhnt', NULL, 5, 1),
('temperature', 'Pronunciation', 'Medium', 0.2, 1.4, 0.0, 'tem-per-uh-cher', NULL, 5, 1),
('dictionary', 'Pronunciation', 'Medium', 0.5, 1.5, 0.0, 'dik-shuh-ner-ee', NULL, 5, 1);

-- Hard Pronunciation (b = +0.6 to +1.4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('choreography', 'Pronunciation', 'Hard', 0.7, 1.5, 0.0, 'kor-ee-og-ruh-fee', NULL, 6, 1),
('bibliography', 'Pronunciation', 'Hard', 0.9, 1.6, 0.0, 'bib-lee-og-ruh-fee', NULL, 6, 1),
('archipelago', 'Pronunciation', 'Hard', 1.2, 1.5, 0.0, 'ahr-kuh-pel-uh-goh', NULL, 6, 1),
('entrepreneur', 'Pronunciation', 'Hard', 1.4, 1.6, 0.0, 'ahn-truh-pruh-nur', NULL, 6, 1);

-- Very Hard Pronunciation (b = +1.6 to +2.2)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel, IsActive) VALUES
('onomatopoeia', 'Pronunciation', 'VeryHard', 1.6, 1.6, 0.0, 'on-uh-mat-uh-pee-uh', NULL, 6, 1),
('pseudonym', 'Pronunciation', 'VeryHard', 1.8, 1.5, 0.0, 'soo-dn-im', NULL, 6, 1),
('anemone', 'Pronunciation', 'VeryHard', 2.0, 1.6, 0.0, 'uh-nem-uh-nee', NULL, 6, 1),
('phenomenon', 'Pronunciation', 'VeryHard', 2.2, 1.5, 0.0, 'fi-nom-uh-non', NULL, 6, 1);


-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Verify item count by type and difficulty
SELECT
    ItemType,
    DifficultyLevel,
    COUNT(*) as ItemCount,
    MIN(DifficultyParam) as MinB,
    MAX(DifficultyParam) as MaxB,
    AVG(DiscriminationParam) as AvgA
FROM Items
WHERE IsActive = 1
GROUP BY ItemType, DifficultyLevel
ORDER BY ItemType, MIN(DifficultyParam);

-- Total item count
SELECT COUNT(*) AS TotalActiveItems FROM Items WHERE IsActive = 1;

-- Verify difficulty distribution
SELECT
    CASE
        WHEN DifficultyParam < -1.5 THEN 'VeryEasy (< -1.5)'
        WHEN DifficultyParam < -0.5 THEN 'Easy (-1.5 to -0.5)'
        WHEN DifficultyParam < 0.5 THEN 'Medium (-0.5 to 0.5)'
        WHEN DifficultyParam < 1.5 THEN 'Hard (0.5 to 1.5)'
        ELSE 'VeryHard (> 1.5)'
    END AS DifficultyRange,
    COUNT(*) AS ItemCount
FROM Items
WHERE IsActive = 1
GROUP BY
    CASE
        WHEN DifficultyParam < -1.5 THEN 'VeryEasy (< -1.5)'
        WHEN DifficultyParam < -0.5 THEN 'Easy (-1.5 to -0.5)'
        WHEN DifficultyParam < 0.5 THEN 'Medium (-0.5 to 0.5)'
        WHEN DifficultyParam < 1.5 THEN 'Hard (0.5 to 1.5)'
        ELSE 'VeryHard (> 1.5)'
    END
ORDER BY MIN(DifficultyParam);
