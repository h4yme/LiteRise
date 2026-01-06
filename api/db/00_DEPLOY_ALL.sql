-- =============================================
-- MASTER DEPLOYMENT SCRIPT
-- =============================================
-- Run this script to deploy the complete adaptive
-- assessment system with pronunciation support
--
-- Order:
-- 1. Base assessment schema
-- 2. Base sample questions
-- 3. Pronunciation extension
-- 4. Pronunciation sample questions
-- =============================================

USE LiteRise;
GO

PRINT '========================================';
PRINT 'Starting LiteRise Assessment Deployment';
PRINT '========================================';
PRINT '';

-- =============================================
-- STEP 1: Check Prerequisites
-- =============================================
PRINT 'Step 1: Checking prerequisites...';

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Students')
BEGIN
    PRINT 'ERROR: Students table not found!';
    PRINT 'Please ensure the base LiteRise database is set up first.';
    PRINT 'This includes: Students, StudentSessions, and other core tables.';
    RAISERROR('Students table missing. Cannot continue.', 16, 1);
    RETURN;
END

PRINT '✓ Students table exists';
PRINT '';

-- =============================================
-- STEP 2: Run Base Assessment Schema
-- =============================================
PRINT 'Step 2: Creating base assessment schema...';
PRINT 'File: assessment_items_schema.sql';
PRINT '';

-- You'll need to execute the contents of assessment_items_schema.sql here
-- Or run it separately in SSMS before running this script

-- Check if base tables exist
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'AssessmentItems')
BEGIN
    PRINT '✓ AssessmentItems table already exists';
END
ELSE
BEGIN
    PRINT '⚠ AssessmentItems table not found!';
    PRINT 'Please run: api/db/assessment_items_schema.sql';
    PRINT 'Then come back to this script.';
    RAISERROR('AssessmentItems table missing. Please run assessment_items_schema.sql first.', 16, 1);
    RETURN;
END

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentResponses')
BEGIN
    PRINT '✓ StudentResponses table already exists';
END
ELSE
BEGIN
    PRINT '⚠ StudentResponses table not found!';
    RAISERROR('StudentResponses table missing. Please run assessment_items_schema.sql first.', 16, 1);
    RETURN;
END

PRINT '';

-- =============================================
-- STEP 3: Load Base Sample Questions
-- =============================================
PRINT 'Step 3: Checking base sample questions...';

DECLARE @BaseQuestionCount INT;
SELECT @BaseQuestionCount = COUNT(*)
FROM AssessmentItems
WHERE QuestionType = 'MultipleChoice';

IF @BaseQuestionCount >= 36
BEGIN
    PRINT '✓ Base questions already loaded (' + CAST(@BaseQuestionCount AS VARCHAR) + ' questions)';
END
ELSE
BEGIN
    PRINT '⚠ Only ' + CAST(@BaseQuestionCount AS VARCHAR) + ' base questions found';
    PRINT 'Expected: 36 multiple choice questions';
    PRINT 'Please run: api/db/sample_assessment_items.sql';
END

PRINT '';

-- =============================================
-- STEP 4: Create Pronunciation Extension
-- =============================================
PRINT 'Step 4: Creating pronunciation extension...';

-- Check if pronunciation columns already exist
IF EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.AssessmentItems') AND name = 'TargetPronunciation')
BEGIN
    PRINT '✓ Pronunciation columns already exist';
END
ELSE
BEGIN
    PRINT 'Adding pronunciation columns to AssessmentItems...';

    ALTER TABLE dbo.AssessmentItems
    ADD AudioPromptURL NVARCHAR(500) NULL,
        TargetPronunciation NVARCHAR(200) NULL,
        PhoneticTranscription NVARCHAR(200) NULL,
        MinimumAccuracy INT DEFAULT 65,
        PronunciationTips NVARCHAR(MAX) NULL;

    PRINT '✓ Pronunciation columns added';
END

-- Create PronunciationScores table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PronunciationScores')
BEGIN
    PRINT 'Creating PronunciationScores table...';

    CREATE TABLE dbo.PronunciationScores (
        ScoreID INT IDENTITY(1,1) PRIMARY KEY,
        ResponseID INT NOT NULL,
        StudentID INT NOT NULL,
        ItemID INT NOT NULL,
        RecognizedText NVARCHAR(500) NULL,
        Confidence FLOAT NULL,
        OverallAccuracy INT NOT NULL,
        PronunciationScore FLOAT NULL,
        FluencyScore FLOAT NULL,
        CompletenessScore FLOAT NULL,
        PhonemeAccuracyJSON NVARCHAR(MAX) NULL,
        ErrorPhonemes NVARCHAR(500) NULL,
        AudioDuration INT NULL,
        AudioQuality FLOAT NULL,
        BackgroundNoiseLevel FLOAT NULL,
        SpeechAPIProvider VARCHAR(50) DEFAULT 'Google Cloud Speech',
        APIResponseJSON NVARCHAR(MAX) NULL,
        CreatedAt DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_PronunciationScores_Response FOREIGN KEY (ResponseID)
            REFERENCES dbo.StudentResponses(ResponseID) ON DELETE CASCADE,
        CONSTRAINT FK_PronunciationScores_Student FOREIGN KEY (StudentID)
            REFERENCES dbo.Students(StudentID) ON DELETE NO ACTION,
        CONSTRAINT FK_PronunciationScores_Item FOREIGN KEY (ItemID)
            REFERENCES dbo.AssessmentItems(ItemID) ON DELETE NO ACTION
    );

    CREATE INDEX IX_PronunciationScores_Student ON dbo.PronunciationScores(StudentID);
    CREATE INDEX IX_PronunciationScores_Item ON dbo.PronunciationScores(ItemID);
    CREATE INDEX IX_PronunciationScores_Accuracy ON dbo.PronunciationScores(OverallAccuracy);

    PRINT '✓ PronunciationScores table created';
END
ELSE
BEGIN
    PRINT '✓ PronunciationScores table already exists';
END

-- Create StudentPronunciationProgress table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentPronunciationProgress')
BEGIN
    PRINT 'Creating StudentPronunciationProgress table...';

    CREATE TABLE dbo.StudentPronunciationProgress (
        ProgressID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL,
        AssessmentDate DATE NOT NULL,
        AverageAccuracy FLOAT NOT NULL,
        WordsAttempted INT DEFAULT 0,
        WordsPassed INT DEFAULT 0,
        MasteredPhonemes NVARCHAR(500) NULL,
        ProblematicPhonemes NVARCHAR(500) NULL,
        AccuracyImprovement FLOAT NULL,
        StreakDays INT DEFAULT 0,
        CreatedAt DATETIME DEFAULT GETDATE(),
        UpdatedAt DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_PronunciationProgress_Student FOREIGN KEY (StudentID)
            REFERENCES dbo.Students(StudentID) ON DELETE CASCADE
    );

    CREATE INDEX IX_PronunciationProgress_Student ON dbo.StudentPronunciationProgress(StudentID);
    CREATE INDEX IX_PronunciationProgress_Date ON dbo.StudentPronunciationProgress(AssessmentDate);

    PRINT '✓ StudentPronunciationProgress table created';
END
ELSE
BEGIN
    PRINT '✓ StudentPronunciationProgress table already exists';
END

PRINT '';

-- =============================================
-- STEP 5: Create Stored Procedures
-- =============================================
PRINT 'Step 5: Creating stored procedures...';

-- SP_RecordPronunciationScore
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_RecordPronunciationScore')
    DROP PROCEDURE dbo.SP_RecordPronunciationScore;

PRINT 'Creating SP_RecordPronunciationScore...';
GO

CREATE PROCEDURE dbo.SP_RecordPronunciationScore
    @ResponseID INT,
    @StudentID INT,
    @ItemID INT,
    @RecognizedText NVARCHAR(500),
    @Confidence FLOAT,
    @OverallAccuracy INT,
    @PronunciationScore FLOAT = NULL,
    @FluencyScore FLOAT = NULL,
    @CompletenessScore FLOAT = NULL,
    @PhonemeAccuracyJSON NVARCHAR(MAX) = NULL,
    @ErrorPhonemes NVARCHAR(500) = NULL,
    @AudioDuration INT = NULL,
    @AudioQuality FLOAT = NULL,
    @APIResponseJSON NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        BEGIN TRANSACTION;

        INSERT INTO dbo.PronunciationScores (
            ResponseID, StudentID, ItemID, RecognizedText, Confidence,
            OverallAccuracy, PronunciationScore, FluencyScore, CompletenessScore,
            PhonemeAccuracyJSON, ErrorPhonemes, AudioDuration, AudioQuality, APIResponseJSON
        )
        VALUES (
            @ResponseID, @StudentID, @ItemID, @RecognizedText, @Confidence,
            @OverallAccuracy, @PronunciationScore, @FluencyScore, @CompletenessScore,
            @PhonemeAccuracyJSON, @ErrorPhonemes, @AudioDuration, @AudioQuality, @APIResponseJSON
        );

        DECLARE @Today DATE = CAST(GETDATE() AS DATE);
        DECLARE @ExistingProgress INT;

        SELECT @ExistingProgress = ProgressID
        FROM dbo.StudentPronunciationProgress
        WHERE StudentID = @StudentID AND AssessmentDate = @Today;

        IF @ExistingProgress IS NOT NULL
        BEGIN
            UPDATE dbo.StudentPronunciationProgress
            SET WordsAttempted = WordsAttempted + 1,
                WordsPassed = WordsPassed + CASE WHEN @OverallAccuracy >= 65 THEN 1 ELSE 0 END,
                AverageAccuracy = (
                    SELECT AVG(CAST(ps.OverallAccuracy AS FLOAT))
                    FROM dbo.PronunciationScores ps
                    WHERE ps.StudentID = @StudentID AND CAST(ps.CreatedAt AS DATE) = @Today
                ),
                UpdatedAt = GETDATE()
            WHERE ProgressID = @ExistingProgress;
        END
        ELSE
        BEGIN
            INSERT INTO dbo.StudentPronunciationProgress (StudentID, AssessmentDate, AverageAccuracy, WordsAttempted, WordsPassed)
            VALUES (@StudentID, @Today, @OverallAccuracy, 1, CASE WHEN @OverallAccuracy >= 65 THEN 1 ELSE 0 END);
        END

        COMMIT TRANSACTION;
        SELECT SCOPE_IDENTITY() AS ScoreID;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

PRINT '✓ SP_RecordPronunciationScore created';

-- =============================================
-- STEP 6: Check Pronunciation Questions
-- =============================================
PRINT '';
PRINT 'Step 6: Checking pronunciation questions...';

DECLARE @PronunciationCount INT;
SELECT @PronunciationCount = COUNT(*)
FROM AssessmentItems
WHERE QuestionType = 'Pronunciation';

IF @PronunciationCount >= 30
BEGIN
    PRINT '✓ Pronunciation questions already loaded (' + CAST(@PronunciationCount AS VARCHAR) + ' questions)';
END
ELSE
BEGIN
    PRINT '⚠ Only ' + CAST(@PronunciationCount AS VARCHAR) + ' pronunciation questions found';
    PRINT 'Expected: 30 pronunciation questions';
    PRINT 'Please run: api/db/sample_pronunciation_items.sql';
END

PRINT '';

-- =============================================
-- STEP 7: Summary
-- =============================================
PRINT '========================================';
PRINT 'Deployment Summary';
PRINT '========================================';

SELECT 'Question Type' = QuestionType, 'Count' = COUNT(*)
FROM AssessmentItems
GROUP BY QuestionType;

PRINT '';
PRINT 'Tables:';
SELECT 'Table Name' = TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('AssessmentItems', 'StudentResponses', 'PronunciationScores', 'StudentPronunciationProgress')
ORDER BY TABLE_NAME;

PRINT '';
PRINT 'Stored Procedures:';
SELECT 'Procedure Name' = ROUTINE_NAME
FROM INFORMATION_SCHEMA.ROUTINES
WHERE ROUTINE_NAME LIKE 'SP_%Pronunciation%'
   OR ROUTINE_NAME LIKE 'SP_%Adaptive%'
ORDER BY ROUTINE_NAME;

PRINT '';
PRINT '========================================';
PRINT 'Deployment Complete!';
PRINT '========================================';
PRINT '';
PRINT 'Next Steps:';
PRINT '1. Copy API files to XAMPP:';
PRINT '   - get_next_question.php';
PRINT '   - submit_answer.php';
PRINT '   - evaluate_pronunciation.php';
PRINT '';
PRINT '2. Rebuild Android app';
PRINT '';
PRINT '3. Test adaptive assessment flow';
PRINT '';
GO
