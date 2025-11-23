-- LiteRise Stored Procedures
-- All procedures corrected based on actual table structures

-- =============================================================================
-- SP_UpdateStudentAbility
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_UpdateStudentAbility
    @StudentID INT,
    @NewTheta FLOAT
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE Students
    SET CurrentAbility = @NewTheta
    WHERE StudentID = @StudentID;
END
GO

-- =============================================================================
-- SP_GetLessonsByAbility
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_GetLessonsByAbility
    @StudentID INT,
    @AbilityLevel FLOAT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        l.LessonID,
        l.LessonTitle,
        l.LessonDescription,
        l.LessonContent,
        l.RequiredAbility,
        l.GradeLevel,
        l.LessonType,
        CASE
            WHEN sp.CompletionStatus = 'Completed' THEN 1
            ELSE 0
        END AS IsCompleted,
        sp.Score AS LastScore,
        sp.AttemptsCount
    FROM Lessons l
    LEFT JOIN StudentProgress sp ON l.LessonID = sp.LessonID AND sp.StudentID = @StudentID
    WHERE l.IsActive = 1
    AND l.RequiredAbility <= @AbilityLevel + 0.5
    ORDER BY l.RequiredAbility ASC, l.GradeLevel ASC;
END
GO

-- =============================================================================
-- SP_CheckBadgeUnlock
-- Uses UnlockCondition field to parse badge requirements
-- Format: "XP:500", "Streak:7", "Games:10", "Lessons:5", "Perfect:3"
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_CheckBadgeUnlock
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @TotalXP INT, @CurrentStreak INT, @LongestStreak INT;
    DECLARE @TotalGames INT, @PerfectGames INT, @TotalLessons INT;

    -- Get student stats
    SELECT @TotalXP = ISNULL(TotalXP, 0),
           @CurrentStreak = ISNULL(CurrentStreak, 0),
           @LongestStreak = ISNULL(LongestStreak, 0)
    FROM Students WHERE StudentID = @StudentID;

    -- Count completed games
    SELECT @TotalGames = COUNT(*),
           @PerfectGames = SUM(CASE WHEN AccuracyPercentage = 100 THEN 1 ELSE 0 END)
    FROM GameResults WHERE StudentID = @StudentID;

    -- Count completed lessons
    SELECT @TotalLessons = COUNT(*)
    FROM StudentProgress
    WHERE StudentID = @StudentID AND CompletionStatus = 'Completed';

    -- Find badges to unlock based on UnlockCondition
    INSERT INTO StudentBadges (StudentID, BadgeID, DateEarned)
    SELECT @StudentID, b.BadgeID, GETDATE()
    FROM Badges b
    WHERE b.BadgeID NOT IN (SELECT BadgeID FROM StudentBadges WHERE StudentID = @StudentID)
    AND (
        -- XP-based badges
        (b.UnlockCondition LIKE 'XP:%' AND @TotalXP >= CAST(SUBSTRING(b.UnlockCondition, 4, 10) AS INT))
        -- Streak-based badges
        OR (b.UnlockCondition LIKE 'Streak:%' AND @LongestStreak >= CAST(SUBSTRING(b.UnlockCondition, 8, 10) AS INT))
        -- Games completed badges
        OR (b.UnlockCondition LIKE 'Games:%' AND @TotalGames >= CAST(SUBSTRING(b.UnlockCondition, 7, 10) AS INT))
        -- Lessons completed badges
        OR (b.UnlockCondition LIKE 'Lessons:%' AND @TotalLessons >= CAST(SUBSTRING(b.UnlockCondition, 9, 10) AS INT))
        -- Perfect score badges
        OR (b.UnlockCondition LIKE 'Perfect:%' AND @PerfectGames >= CAST(SUBSTRING(b.UnlockCondition, 9, 10) AS INT))
    );

    -- Return newly unlocked badges
    SELECT b.BadgeID, b.BadgeName, b.BadgeDescription, b.BadgeIconURL,
           b.XPReward, b.BadgeCategory, sb.DateEarned
    FROM StudentBadges sb
    JOIN Badges b ON sb.BadgeID = b.BadgeID
    WHERE sb.StudentID = @StudentID
    AND sb.DateEarned >= DATEADD(SECOND, -5, GETDATE());
END
GO

-- =============================================================================
-- SP_GetStudentProgress
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_GetStudentProgress
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        sp.ProgressID,
        sp.LessonID,
        l.LessonTitle,
        l.LessonDescription,
        l.LessonType,
        l.GradeLevel,
        sp.CompletionStatus,
        sp.Score,
        sp.AttemptsCount,
        sp.LastAttemptDate,
        sp.CompletionDate
    FROM StudentProgress sp
    JOIN Lessons l ON sp.LessonID = l.LessonID
    WHERE sp.StudentID = @StudentID
    ORDER BY sp.LastAttemptDate DESC;
END
GO

-- =============================================================================
-- SP_StartPostAssessment
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_StartPostAssessment
    @StudentID INT,
    @SessionID INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @CurrentAbility FLOAT;

    -- Get current ability
    SELECT @CurrentAbility = ISNULL(CurrentAbility, 0.0)
    FROM Students WHERE StudentID = @StudentID;

    -- Create post-assessment session
    INSERT INTO TestSessions (StudentID, SessionType, InitialTheta, StartTime, IsCompleted)
    VALUES (@StudentID, 'PostAssessment', @CurrentAbility, GETDATE(), 0);

    SET @SessionID = SCOPE_IDENTITY();

    -- Return session info
    SELECT @SessionID AS SessionID,
           'PostAssessment' AS SessionType,
           @CurrentAbility AS InitialTheta;
END
GO

-- =============================================================================
-- SP_GetImprovementReport
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_GetImprovementReport
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @PreTheta FLOAT, @PostTheta FLOAT, @CurrentAbility FLOAT;
    DECLARE @PreDate DATETIME, @PostDate DATETIME;

    -- Get pre-assessment result
    SELECT TOP 1 @PreTheta = FinalTheta, @PreDate = EndTime
    FROM TestSessions
    WHERE StudentID = @StudentID AND SessionType = 'PreAssessment' AND IsCompleted = 1
    ORDER BY StartTime ASC;

    -- Get latest post-assessment result
    SELECT TOP 1 @PostTheta = FinalTheta, @PostDate = EndTime
    FROM TestSessions
    WHERE StudentID = @StudentID AND SessionType = 'PostAssessment' AND IsCompleted = 1
    ORDER BY StartTime DESC;

    -- Get current ability
    SELECT @CurrentAbility = CurrentAbility FROM Students WHERE StudentID = @StudentID;

    -- Return improvement report
    SELECT
        @StudentID AS StudentID,
        @PreTheta AS PreAssessmentTheta,
        @PreDate AS PreAssessmentDate,
        @PostTheta AS PostAssessmentTheta,
        @PostDate AS PostAssessmentDate,
        @CurrentAbility AS CurrentAbility,
        (@PostTheta - @PreTheta) AS ThetaImprovement,
        CASE
            WHEN @PostTheta - @PreTheta > 0.5 THEN 'Significant Improvement'
            WHEN @PostTheta - @PreTheta > 0.2 THEN 'Moderate Improvement'
            WHEN @PostTheta - @PreTheta > 0 THEN 'Slight Improvement'
            WHEN @PostTheta - @PreTheta = 0 THEN 'No Change'
            ELSE 'Needs Review'
        END AS ImprovementCategory,
        (SELECT COUNT(*) FROM StudentProgress WHERE StudentID = @StudentID AND CompletionStatus = 'Completed') AS LessonsCompleted,
        (SELECT COUNT(*) FROM GameResults WHERE StudentID = @StudentID) AS GamesPlayed,
        (SELECT ISNULL(SUM(XPEarned), 0) FROM GameResults WHERE StudentID = @StudentID) AS TotalXPFromGames;
END
GO

-- =============================================================================
-- SP_SaveGameResult
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_SaveGameResult
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
    INSERT INTO GameResults (SessionID, StudentID, GameType, Score, AccuracyPercentage,
                            TimeCompleted, XPEarned, StreakAchieved, DatePlayed)
    VALUES (@SessionID, @StudentID, @GameType, @Score, @AccuracyPercentage,
            @TimeCompleted, @XPEarned, @StreakAchieved, GETDATE());

    -- Update student XP and streak
    UPDATE Students
    SET TotalXP = ISNULL(TotalXP, 0) + @XPEarned,
        CurrentStreak = CASE WHEN @StreakAchieved > CurrentStreak THEN @StreakAchieved ELSE CurrentStreak END,
        LongestStreak = CASE WHEN @StreakAchieved > LongestStreak THEN @StreakAchieved ELSE LongestStreak END
    WHERE StudentID = @StudentID;
END
GO

-- =============================================================================
-- SP_GetPreAssessmentItems
-- Returns 15 random items stratified by difficulty for pre-assessment
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_GetPreAssessmentItems
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Get 15 items: 3 very easy, 4 easy, 4 medium, 3 hard, 1 very hard
    SELECT * FROM (
        SELECT TOP 3 *, 1 as DiffOrder FROM Items WHERE DifficultyParam < -1.5 ORDER BY NEWID()
        UNION ALL
        SELECT TOP 4 *, 2 as DiffOrder FROM Items WHERE DifficultyParam >= -1.5 AND DifficultyParam < -0.5 ORDER BY NEWID()
        UNION ALL
        SELECT TOP 4 *, 3 as DiffOrder FROM Items WHERE DifficultyParam >= -0.5 AND DifficultyParam < 0.5 ORDER BY NEWID()
        UNION ALL
        SELECT TOP 3 *, 4 as DiffOrder FROM Items WHERE DifficultyParam >= 0.5 AND DifficultyParam < 1.5 ORDER BY NEWID()
        UNION ALL
        SELECT TOP 1 *, 5 as DiffOrder FROM Items WHERE DifficultyParam >= 1.5 ORDER BY NEWID()
    ) AS CombinedItems
    ORDER BY DiffOrder, NEWID();
END
GO

-- =============================================================================
-- SP_GetNextAdaptiveItem
-- Returns the next best item based on current theta using Maximum Information
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_GetNextAdaptiveItem
    @SessionID INT,
    @CurrentTheta FLOAT
AS
BEGIN
    SET NOCOUNT ON;

    -- Get items not yet answered in this session
    -- Select item with difficulty closest to current theta (simplified max info)
    SELECT TOP 1 i.*
    FROM Items i
    WHERE i.ItemID NOT IN (
        SELECT ItemID FROM Responses WHERE SessionID = @SessionID
    )
    ORDER BY
        -- Prioritize items near current ability for maximum information
        ABS(i.DifficultyParam - @CurrentTheta),
        -- Add randomness for items at similar difficulty
        NEWID();
END
GO

-- =============================================================================
-- SP_CompleteSession
-- Marks a session as complete and updates student ability
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_CompleteSession
    @SessionID INT,
    @FinalTheta FLOAT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @StudentID INT;

    -- Get student ID
    SELECT @StudentID = StudentID FROM TestSessions WHERE SessionID = @SessionID;

    -- Update session
    UPDATE TestSessions
    SET IsCompleted = 1,
        EndTime = GETDATE(),
        FinalTheta = @FinalTheta
    WHERE SessionID = @SessionID;

    -- Update student ability
    UPDATE Students
    SET CurrentAbility = @FinalTheta
    WHERE StudentID = @StudentID;
END
GO

-- =============================================================================
-- SP_UpdateLessonProgress
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_UpdateLessonProgress
    @StudentID INT,
    @LessonID INT,
    @Score INT,
    @CompletionStatus NVARCHAR(50)
AS
BEGIN
    SET NOCOUNT ON;

    -- Check if progress record exists
    IF EXISTS (SELECT 1 FROM StudentProgress WHERE StudentID = @StudentID AND LessonID = @LessonID)
    BEGIN
        -- Update existing record
        UPDATE StudentProgress
        SET Score = @Score,
            CompletionStatus = @CompletionStatus,
            AttemptsCount = AttemptsCount + 1,
            LastAttemptDate = GETDATE(),
            CompletionDate = CASE WHEN @CompletionStatus = 'Completed' THEN GETDATE() ELSE CompletionDate END
        WHERE StudentID = @StudentID AND LessonID = @LessonID;
    END
    ELSE
    BEGIN
        -- Insert new record
        INSERT INTO StudentProgress (StudentID, LessonID, Score, CompletionStatus, AttemptsCount, LastAttemptDate, CompletionDate)
        VALUES (@StudentID, @LessonID, @Score, @CompletionStatus, 1, GETDATE(),
                CASE WHEN @CompletionStatus = 'Completed' THEN GETDATE() ELSE NULL END);
    END
END
GO

-- =============================================================================
-- SP_GetStudentDashboard
-- Returns comprehensive student dashboard data
-- =============================================================================
CREATE OR ALTER PROCEDURE SP_GetStudentDashboard
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Student basic info and stats
    SELECT
        s.StudentID,
        s.FirstName,
        s.LastName,
        s.Email,
        s.CurrentAbility,
        s.TotalXP,
        s.CurrentStreak,
        s.LongestStreak,
        (SELECT COUNT(*) FROM StudentProgress WHERE StudentID = @StudentID AND CompletionStatus = 'Completed') AS LessonsCompleted,
        (SELECT COUNT(*) FROM GameResults WHERE StudentID = @StudentID) AS GamesPlayed,
        (SELECT COUNT(*) FROM StudentBadges WHERE StudentID = @StudentID) AS BadgesEarned,
        (SELECT COUNT(*) FROM TestSessions WHERE StudentID = @StudentID AND SessionType = 'PreAssessment' AND IsCompleted = 1) AS PreAssessmentsDone,
        (SELECT COUNT(*) FROM TestSessions WHERE StudentID = @StudentID AND SessionType = 'PostAssessment' AND IsCompleted = 1) AS PostAssessmentsDone
    FROM Students s
    WHERE s.StudentID = @StudentID;
END
GO
