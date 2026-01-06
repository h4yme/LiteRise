-- ===============================================
-- Assessment Items Schema for IRT-Based Adaptive Testing
-- ===============================================
-- This schema supports Item Response Theory (IRT) based adaptive testing
-- with a large question bank and ML-ready student response tracking
-- ===============================================

USE LiteRiseDB;
GO

-- ===============================================
-- 1. CREATE ASSESSMENT_ITEMS TABLE
-- ===============================================
-- Stores all assessment questions with IRT parameters

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES
               WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'AssessmentItems')
BEGIN
    CREATE TABLE dbo.AssessmentItems (
        ItemID INT IDENTITY(1,1) PRIMARY KEY,

        -- Item Classification
        Category VARCHAR(50) NOT NULL, -- 'Oral Language', 'Word Knowledge', 'Reading Comprehension', 'Language Structure'
        Subcategory VARCHAR(100) NULL, -- More specific classification
        SkillArea VARCHAR(100) NULL, -- Specific skill being tested

        -- Question Content
        QuestionText NVARCHAR(MAX) NOT NULL,
        QuestionType VARCHAR(50) NOT NULL, -- 'MultipleChoice', 'TrueFalse', 'FillInBlank', 'Matching'

        -- Answer Options (for multiple choice)
        OptionA NVARCHAR(500) NULL,
        OptionB NVARCHAR(500) NULL,
        OptionC NVARCHAR(500) NULL,
        OptionD NVARCHAR(500) NULL,
        CorrectAnswer NVARCHAR(200) NOT NULL, -- For MC: 'A','B','C','D'; For Pronunciation: full word like 'cat', 'elephant'

        -- IRT Parameters (3-Parameter Logistic Model)
        DifficultyParam FLOAT NOT NULL, -- b parameter: difficulty (-3 to +3, 0 = average)
        DiscriminationParam FLOAT NOT NULL, -- a parameter: discrimination (0.5 to 2.5, higher = better differentiation)
        GuessingParam FLOAT DEFAULT 0.25, -- c parameter: probability of guessing correct (0.0 to 0.5)

        -- Additional Metadata
        GradeLevel INT NULL, -- Target grade level
        EstimatedTime INT DEFAULT 30, -- Estimated time in seconds
        IsActive BIT DEFAULT 1, -- Whether item is currently in use

        -- ML/Analytics Fields
        TimesAdministered INT DEFAULT 0, -- How many times shown to students
        TimesCorrect INT DEFAULT 0, -- How many times answered correctly
        AverageResponseTime FLOAT NULL, -- Average time to answer (seconds)

        -- Audit Fields
        CreatedDate DATETIME DEFAULT GETDATE(),
        LastModified DATETIME DEFAULT GETDATE(),
        CreatedBy VARCHAR(100) DEFAULT 'System'
    );

    CREATE INDEX IDX_Items_Category ON dbo.AssessmentItems(Category);
    CREATE INDEX IDX_Items_Difficulty ON dbo.AssessmentItems(DifficultyParam);
    CREATE INDEX IDX_Items_Active ON dbo.AssessmentItems(IsActive);

    PRINT 'AssessmentItems table created successfully';
END
ELSE
BEGIN
    PRINT 'AssessmentItems table already exists';
END
GO

-- ===============================================
-- 2. CREATE STUDENT_RESPONSES TABLE
-- ===============================================
-- Tracks every student response for ML training and analysis

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES
               WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'StudentResponses')
BEGIN
    CREATE TABLE dbo.StudentResponses (
        ResponseID INT IDENTITY(1,1) PRIMARY KEY,

        -- Student and Item References
        StudentID INT NOT NULL FOREIGN KEY REFERENCES dbo.Students(StudentID),
        ItemID INT NOT NULL FOREIGN KEY REFERENCES dbo.AssessmentItems(ItemID),
        SessionID INT NOT NULL, -- Links responses from same assessment session

        -- Response Details
        AssessmentType VARCHAR(20) NOT NULL, -- 'PreAssessment' or 'PostAssessment'
        SelectedAnswer VARCHAR(10) NULL, -- Student's answer
        IsCorrect BIT NOT NULL, -- Whether answer was correct

        -- IRT Context
        StudentThetaAtTime FLOAT NOT NULL, -- Student's estimated ability when shown this item
        ItemDifficulty FLOAT NOT NULL, -- Difficulty of this item (snapshot)
        ExpectedProbability FLOAT NULL, -- IRT-predicted probability of correct answer

        -- Timing
        ResponseTime INT NULL, -- Time taken to answer (seconds)
        PresentedAt DATETIME NOT NULL DEFAULT GETDATE(),
        AnsweredAt DATETIME NULL,

        -- Question Position
        QuestionNumber INT NOT NULL, -- Which question # in the assessment (1, 2, 3...)

        -- Metadata for ML
        DeviceInfo VARCHAR(255) NULL,
        InteractionData NVARCHAR(MAX) NULL -- JSON: mouse movements, hesitations, etc.
    );

    CREATE INDEX IDX_Responses_Student ON dbo.StudentResponses(StudentID);
    CREATE INDEX IDX_Responses_Item ON dbo.StudentResponses(ItemID);
    CREATE INDEX IDX_Responses_Session ON dbo.StudentResponses(SessionID);
    CREATE INDEX IDX_Responses_Assessment ON dbo.StudentResponses(AssessmentType);
    CREATE INDEX IDX_Responses_Correctness ON dbo.StudentResponses(IsCorrect);

    PRINT 'StudentResponses table created successfully';
END
ELSE
BEGIN
    PRINT 'StudentResponses table already exists';
END
GO

-- ===============================================
-- 3. CREATE STORED PROCEDURE: Get Next Adaptive Question
-- ===============================================
-- Selects the most appropriate next question based on current theta

IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_GetNextAdaptiveQuestion')
BEGIN
    DROP PROCEDURE dbo.SP_GetNextAdaptiveQuestion;
END
GO

CREATE PROCEDURE dbo.SP_GetNextAdaptiveQuestion
    @StudentID INT,
    @SessionID INT,
    @CurrentTheta FLOAT,
    @AssessmentType VARCHAR(20),
    @CategoryFilter VARCHAR(50) = NULL -- Optional: filter by category
AS
BEGIN
    SET NOCOUNT ON;

    -- Get items already shown in this session
    DECLARE @ShownItemIDs TABLE (ItemID INT);
    INSERT INTO @ShownItemIDs
    SELECT ItemID
    FROM dbo.StudentResponses
    WHERE StudentID = @StudentID AND SessionID = @SessionID;

    -- Select best next item using Maximum Information criterion
    -- Information is highest when item difficulty matches student ability
    SELECT TOP 1
        ItemID,
        Category,
        Subcategory,
        QuestionText,
        QuestionType,
        OptionA,
        OptionB,
        OptionC,
        OptionD,
        CorrectAnswer,
        DifficultyParam,
        DiscriminationParam,
        GuessingParam,
        EstimatedTime,
        -- Calculate information value
        (DiscriminationParam * DiscriminationParam *
         (1 - GuessingParam) * (1 - GuessingParam)) /
         (1 + EXP(1.7 * DiscriminationParam * (DifficultyParam - @CurrentTheta))) AS Information
    FROM dbo.AssessmentItems
    WHERE IsActive = 1
      AND ItemID NOT IN (SELECT ItemID FROM @ShownItemIDs)
      AND (@CategoryFilter IS NULL OR Category = @CategoryFilter)
    ORDER BY
        -- Prioritize items near current theta
        ABS(DifficultyParam - @CurrentTheta) ASC,
        -- Then by information
        Information DESC,
        -- Then by least-used items
        TimesAdministered ASC;
END
GO

-- ===============================================
-- 4. CREATE STORED PROCEDURE: Record Student Response
-- ===============================================
-- Records a student's answer and updates item statistics

IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_RecordStudentResponse')
BEGIN
    DROP PROCEDURE dbo.SP_RecordStudentResponse;
END
GO

CREATE PROCEDURE dbo.SP_RecordStudentResponse
    @StudentID INT,
    @ItemID INT,
    @SessionID INT,
    @AssessmentType VARCHAR(20),
    @SelectedAnswer VARCHAR(10),
    @IsCorrect BIT,
    @StudentThetaAtTime FLOAT,
    @ResponseTime INT = NULL,
    @QuestionNumber INT,
    @DeviceInfo VARCHAR(255) = NULL,
    @InteractionData NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Get item parameters for snapshot
    DECLARE @ItemDifficulty FLOAT;
    DECLARE @ItemDiscrimination FLOAT;
    DECLARE @ItemGuessing FLOAT;

    SELECT
        @ItemDifficulty = DifficultyParam,
        @ItemDiscrimination = DiscriminationParam,
        @ItemGuessing = GuessingParam
    FROM dbo.AssessmentItems
    WHERE ItemID = @ItemID;

    -- Calculate expected probability using 3PL IRT model
    DECLARE @ExpectedProbability FLOAT;
    SET @ExpectedProbability = @ItemGuessing +
        (1 - @ItemGuessing) /
        (1 + EXP(-1.7 * @ItemDiscrimination * (@StudentThetaAtTime - @ItemDifficulty)));

    -- Insert response
    INSERT INTO dbo.StudentResponses (
        StudentID, ItemID, SessionID, AssessmentType,
        SelectedAnswer, IsCorrect,
        StudentThetaAtTime, ItemDifficulty, ExpectedProbability,
        ResponseTime, QuestionNumber,
        DeviceInfo, InteractionData,
        PresentedAt, AnsweredAt
    )
    VALUES (
        @StudentID, @ItemID, @SessionID, @AssessmentType,
        @SelectedAnswer, @IsCorrect,
        @StudentThetaAtTime, @ItemDifficulty, @ExpectedProbability,
        @ResponseTime, @QuestionNumber,
        @DeviceInfo, @InteractionData,
        GETDATE(), GETDATE()
    );

    -- Update item statistics
    UPDATE dbo.AssessmentItems
    SET TimesAdministered = TimesAdministered + 1,
        TimesCorrect = TimesCorrect + (CASE WHEN @IsCorrect = 1 THEN 1 ELSE 0 END),
        AverageResponseTime =
            CASE
                WHEN AverageResponseTime IS NULL THEN @ResponseTime
                WHEN @ResponseTime IS NULL THEN AverageResponseTime
                ELSE (AverageResponseTime * TimesAdministered + @ResponseTime) / (TimesAdministered + 1)
            END,
        LastModified = GETDATE()
    WHERE ItemID = @ItemID;

    -- Return the response ID
    SELECT SCOPE_IDENTITY() AS ResponseID;
END
GO

-- ===============================================
-- 5. CREATE VIEW: Item Statistics
-- ===============================================
-- Provides analytics on item performance

IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'V_ItemStatistics')
BEGIN
    DROP VIEW dbo.V_ItemStatistics;
END
GO

CREATE VIEW dbo.V_ItemStatistics AS
SELECT
    i.ItemID,
    i.Category,
    i.Subcategory,
    i.DifficultyParam AS ConfiguredDifficulty,
    i.DiscriminationParam,
    i.GuessingParam,
    i.TimesAdministered,
    i.TimesCorrect,
    CASE
        WHEN i.TimesAdministered > 0 THEN
            CAST(i.TimesCorrect AS FLOAT) / i.TimesAdministered
        ELSE NULL
    END AS EmpiricalPCorrect,
    i.AverageResponseTime,
    COUNT(DISTINCT r.StudentID) AS UniqueStudentsSeen,
    AVG(r.StudentThetaAtTime) AS AvgThetaWhenShown,
    STDEV(r.StudentThetaAtTime) AS StdDevThetaWhenShown
FROM dbo.AssessmentItems i
LEFT JOIN dbo.StudentResponses r ON i.ItemID = r.ItemID
GROUP BY
    i.ItemID, i.Category, i.Subcategory,
    i.DifficultyParam, i.DiscriminationParam, i.GuessingParam,
    i.TimesAdministered, i.TimesCorrect, i.AverageResponseTime;
GO

PRINT '';
PRINT '=====================================================';
PRINT 'Assessment Items Schema Created Successfully!';
PRINT '=====================================================';
PRINT 'Tables Created:';
PRINT '  - AssessmentItems (for storing questions with IRT parameters)';
PRINT '  - StudentResponses (for tracking all answers - ML ready)';
PRINT '';
PRINT 'Stored Procedures Created:';
PRINT '  - SP_GetNextAdaptiveQuestion (adaptive question selection)';
PRINT '  - SP_RecordStudentResponse (record answers with metadata)';
PRINT '';
PRINT 'Views Created:';
PRINT '  - V_ItemStatistics (analytics on item performance)';
PRINT '';
PRINT 'Next Steps:';
PRINT '  1. Populate AssessmentItems table with questions';
PRINT '  2. Create API endpoints to use these procedures';
PRINT '  3. Modify Android app to fetch questions from API';
PRINT '=====================================================';
GO
