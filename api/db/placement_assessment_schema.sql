-- ===============================================
-- LiteRise Placement Assessment Schema
-- Database Migration for Pre/Post Assessment Tracking
-- ===============================================

USE LiteRise;
GO

-- ===============================================
-- 1. ADD COLUMNS TO EXISTING STUDENTS TABLE
-- ===============================================

-- Add placement assessment tracking columns to Students table
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'PreAssessmentCompleted')
BEGIN
    ALTER TABLE Students ADD PreAssessmentCompleted BIT DEFAULT 0;
    ALTER TABLE Students ADD PreAssessmentDate DATETIME NULL;
    ALTER TABLE Students ADD PreAssessmentLevel INT NULL;
    ALTER TABLE Students ADD PreAssessmentTheta FLOAT DEFAULT 0.0;
    ALTER TABLE Students ADD PostAssessmentCompleted BIT DEFAULT 0;
    ALTER TABLE Students ADD PostAssessmentDate DATETIME NULL;
    ALTER TABLE Students ADD PostAssessmentLevel INT NULL;
    ALTER TABLE Students ADD PostAssessmentTheta FLOAT DEFAULT 0.0;
    ALTER TABLE Students ADD AssessmentStatus VARCHAR(50) DEFAULT 'Not Started'; -- 'Not Started', 'Pre-Completed', 'In Progress', 'Post-Completed'
    ALTER TABLE Students ADD LastLoginDate DATETIME NULL;
    ALTER TABLE Students ADD TotalLoginCount INT DEFAULT 0;
END
GO

-- ===============================================
-- 2. CREATE PLACEMENT_RESULTS TABLE
-- ===============================================

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES
               WHERE TABLE_NAME = 'PlacementResults')
BEGIN
    CREATE TABLE PlacementResults (
        ResultID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
        SessionID INT NOT NULL FOREIGN KEY REFERENCES TestSessions(SessionID),
        AssessmentType VARCHAR(20) NOT NULL, -- 'PreAssessment' or 'PostAssessment'
        CompletedDate DATETIME NOT NULL DEFAULT GETDATE(),

        -- Overall Results
        FinalTheta FLOAT NOT NULL,
        PlacementLevel INT NOT NULL,
        LevelName VARCHAR(50) NOT NULL,
        TotalQuestions INT NOT NULL,
        CorrectAnswers INT NOT NULL,
        AccuracyPercentage FLOAT NOT NULL,

        -- Category Breakdown (4 categories)
        Category1Score FLOAT NULL,
        Category2Score FLOAT NULL,
        Category3Score FLOAT NULL,
        Category4Score FLOAT NULL,

        -- Category Theta Values
        Category1Theta FLOAT NULL,
        Category2Theta FLOAT NULL,
        Category3Theta FLOAT NULL,
        Category4Theta FLOAT NULL,

        -- Question Type Breakdown
        VocabularyScore FLOAT NULL,
        PhonologicalScore FLOAT NULL,
        ReadingScore FLOAT NULL,
        GrammarScore FLOAT NULL,

        -- Metadata
        TimeSpentSeconds INT NULL,
        DeviceInfo VARCHAR(255) NULL,
        AppVersion VARCHAR(20) NULL,

        CONSTRAINT CHK_AssessmentType CHECK (AssessmentType IN ('PreAssessment', 'PostAssessment'))
    );

    CREATE INDEX IDX_PlacementResults_Student ON PlacementResults(StudentID);
    CREATE INDEX IDX_PlacementResults_Type ON PlacementResults(AssessmentType);
    CREATE INDEX IDX_PlacementResults_Date ON PlacementResults(CompletedDate);
END
GO

-- ===============================================
-- 3. CREATE STUDENT_SESSION_LOGS TABLE
-- ===============================================

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES
               WHERE TABLE_NAME = 'StudentSessionLogs')
BEGIN
    CREATE TABLE StudentSessionLogs (
        LogID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL FOREIGN KEY REFERENCES Students(StudentID),
        SessionType VARCHAR(50) NOT NULL, -- 'Login', 'Logout', 'AssessmentStart', 'AssessmentComplete', 'LessonStart', 'LessonComplete'
        SessionTag VARCHAR(100) NULL, -- Custom tags for categorizing sessions
        LoggedAt DATETIME NOT NULL DEFAULT GETDATE(),
        DeviceInfo VARCHAR(255) NULL,
        IPAddress VARCHAR(50) NULL,
        AdditionalData NVARCHAR(MAX) NULL -- JSON format for extensibility
    );

    CREATE INDEX IDX_SessionLogs_Student ON StudentSessionLogs(StudentID);
    CREATE INDEX IDX_SessionLogs_Type ON StudentSessionLogs(SessionType);
    CREATE INDEX IDX_SessionLogs_Date ON StudentSessionLogs(LoggedAt);
    CREATE INDEX IDX_SessionLogs_Tag ON StudentSessionLogs(SessionTag);
END
GO

-- ===============================================
-- 4. CREATE ASSESSMENT_COMPARISON_VIEW
-- ===============================================

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = 'V_AssessmentComparison')
    DROP VIEW V_AssessmentComparison;
GO

CREATE VIEW V_AssessmentComparison AS
SELECT
    s.StudentID,
    s.FirstName,
    s.LastName,
    s.Nickname,
    s.GradeLevel,

    -- Pre Assessment
    pre.ResultID AS PreResultID,
    pre.CompletedDate AS PreAssessmentDate,
    pre.FinalTheta AS PreTheta,
    pre.PlacementLevel AS PreLevel,
    pre.LevelName AS PreLevelName,
    pre.AccuracyPercentage AS PreAccuracy,
    pre.TotalQuestions AS PreTotalQuestions,
    pre.CorrectAnswers AS PreCorrectAnswers,

    -- Post Assessment
    post.ResultID AS PostResultID,
    post.CompletedDate AS PostAssessmentDate,
    post.FinalTheta AS PostTheta,
    post.PlacementLevel AS PostLevel,
    post.LevelName AS PostLevelName,
    post.AccuracyPercentage AS PostAccuracy,
    post.TotalQuestions AS PostTotalQuestions,
    post.CorrectAnswers AS PostCorrectAnswers,

    -- Growth Metrics
    (post.FinalTheta - pre.FinalTheta) AS ThetaGrowth,
    (post.PlacementLevel - pre.PlacementLevel) AS LevelGrowth,
    (post.AccuracyPercentage - pre.AccuracyPercentage) AS AccuracyGrowth,

    -- Category Comparisons
    (post.Category1Score - pre.Category1Score) AS Category1Growth,
    (post.Category2Score - pre.Category2Score) AS Category2Growth,
    (post.Category3Score - pre.Category3Score) AS Category3Growth,
    (post.Category4Score - pre.Category4Score) AS Category4Growth,

    -- Status
    s.AssessmentStatus,
    CASE
        WHEN post.ResultID IS NOT NULL THEN 'Completed Both'
        WHEN pre.ResultID IS NOT NULL THEN 'Pre Only'
        ELSE 'Not Started'
    END AS ComparisonStatus

FROM Students s
LEFT JOIN PlacementResults pre ON s.StudentID = pre.StudentID AND pre.AssessmentType = 'PreAssessment'
LEFT JOIN PlacementResults post ON s.StudentID = post.StudentID AND post.AssessmentType = 'PostAssessment';
GO

-- ===============================================
-- 5. CREATE STORED PROCEDURE: SP_SavePlacementResult
-- ===============================================

IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_SavePlacementResult')
    DROP PROCEDURE SP_SavePlacementResult;
GO

CREATE PROCEDURE SP_SavePlacementResult
    @StudentID INT,
    @SessionID INT,
    @AssessmentType VARCHAR(20),
    @FinalTheta FLOAT,
    @PlacementLevel INT,
    @LevelName VARCHAR(50),
    @TotalQuestions INT,
    @CorrectAnswers INT,
    @AccuracyPercentage FLOAT,
    @Category1Score FLOAT = NULL,
    @Category2Score FLOAT = NULL,
    @Category3Score FLOAT = NULL,
    @Category4Score FLOAT = NULL,
    @Category1Theta FLOAT = NULL,
    @Category2Theta FLOAT = NULL,
    @Category3Theta FLOAT = NULL,
    @Category4Theta FLOAT = NULL,
    @TimeSpentSeconds INT = NULL,
    @DeviceInfo VARCHAR(255) = NULL,
    @AppVersion VARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Insert placement result
        INSERT INTO PlacementResults (
            StudentID, SessionID, AssessmentType, CompletedDate,
            FinalTheta, PlacementLevel, LevelName, TotalQuestions, CorrectAnswers, AccuracyPercentage,
            Category1Score, Category2Score, Category3Score, Category4Score,
            Category1Theta, Category2Theta, Category3Theta, Category4Theta,
            TimeSpentSeconds, DeviceInfo, AppVersion
        )
        VALUES (
            @StudentID, @SessionID, @AssessmentType, GETDATE(),
            @FinalTheta, @PlacementLevel, @LevelName, @TotalQuestions, @CorrectAnswers, @AccuracyPercentage,
            @Category1Score, @Category2Score, @Category3Score, @Category4Score,
            @Category1Theta, @Category2Theta, @Category3Theta, @Category4Theta,
            @TimeSpentSeconds, @DeviceInfo, @AppVersion
        );

        -- Update Students table based on assessment type
        IF @AssessmentType = 'PreAssessment'
        BEGIN
            UPDATE Students
            SET PreAssessmentCompleted = 1,
                PreAssessmentDate = GETDATE(),
                PreAssessmentLevel = @PlacementLevel,
                PreAssessmentTheta = @FinalTheta,
                AssessmentStatus = 'Pre-Completed'
            WHERE StudentID = @StudentID;
        END
        ELSE IF @AssessmentType = 'PostAssessment'
        BEGIN
            UPDATE Students
            SET PostAssessmentCompleted = 1,
                PostAssessmentDate = GETDATE(),
                PostAssessmentLevel = @PlacementLevel,
                PostAssessmentTheta = @FinalTheta,
                AssessmentStatus = 'Post-Completed'
            WHERE StudentID = @StudentID;
        END

        -- Log the assessment completion
        INSERT INTO StudentSessionLogs (StudentID, SessionType, SessionTag, LoggedAt, DeviceInfo)
        VALUES (@StudentID, 'AssessmentComplete', @AssessmentType, GETDATE(), @DeviceInfo);

        COMMIT TRANSACTION;

        -- Return the saved result
        SELECT TOP 1 * FROM PlacementResults
        WHERE StudentID = @StudentID AND AssessmentType = @AssessmentType
        ORDER BY ResultID DESC;

    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

-- ===============================================
-- 6. CREATE STORED PROCEDURE: SP_LogStudentSession
-- ===============================================

IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_LogStudentSession')
    DROP PROCEDURE SP_LogStudentSession;
GO

CREATE PROCEDURE SP_LogStudentSession
    @StudentID INT,
    @SessionType VARCHAR(50),
    @SessionTag VARCHAR(100) = NULL,
    @DeviceInfo VARCHAR(255) = NULL,
    @IPAddress VARCHAR(50) = NULL,
    @AdditionalData NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Insert session log
    INSERT INTO StudentSessionLogs (StudentID, SessionType, SessionTag, LoggedAt, DeviceInfo, IPAddress, AdditionalData)
    VALUES (@StudentID, @SessionType, @SessionTag, GETDATE(), @DeviceInfo, @IPAddress, @AdditionalData);

    -- Update last login date if it's a login event
    IF @SessionType = 'Login'
    BEGIN
        UPDATE Students
        SET LastLoginDate = GETDATE(),
            TotalLoginCount = ISNULL(TotalLoginCount, 0) + 1
        WHERE StudentID = @StudentID;
    END

    SELECT SCOPE_IDENTITY() AS LogID;
END
GO

-- ===============================================
-- 7. CREATE STORED PROCEDURE: SP_GetStudentProgress
-- ===============================================

IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_GetStudentProgress')
    DROP PROCEDURE SP_GetStudentProgress;
GO

CREATE PROCEDURE SP_GetStudentProgress
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Student Info
    SELECT * FROM Students WHERE StudentID = @StudentID;

    -- Assessment Results
    SELECT * FROM PlacementResults WHERE StudentID = @StudentID ORDER BY CompletedDate DESC;

    -- Session History (last 30 days)
    SELECT TOP 50 * FROM StudentSessionLogs
    WHERE StudentID = @StudentID AND LoggedAt >= DATEADD(DAY, -30, GETDATE())
    ORDER BY LoggedAt DESC;

    -- Comparison View
    SELECT * FROM V_AssessmentComparison WHERE StudentID = @StudentID;
END
GO

-- ===============================================
-- 8. INSERT SAMPLE DATA (FOR TESTING)
-- ===============================================

PRINT 'Placement Assessment Schema Migration Completed Successfully!';
PRINT 'New Tables Created:';
PRINT '  - PlacementResults';
PRINT '  - StudentSessionLogs';
PRINT '';
PRINT 'Views Created:';
PRINT '  - V_AssessmentComparison';
PRINT '';
PRINT 'Stored Procedures Created:';
PRINT '  - SP_SavePlacementResult';
PRINT '  - SP_LogStudentSession';
PRINT '  - SP_GetStudentProgress';
GO
