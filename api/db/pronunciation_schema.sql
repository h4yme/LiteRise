-- =============================================
-- Pronunciation Assessment Schema Extension
-- =============================================
-- Extends the adaptive assessment system with
-- audio-based pronunciation scoring capabilities
-- =============================================

USE LiteRise;
GO

-- =============================================
-- Add pronunciation fields to AssessmentItems
-- =============================================

-- Check if columns don't exist before adding them
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.AssessmentItems') AND name = 'AudioPromptURL')
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD AudioPromptURL NVARCHAR(500) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.AssessmentItems') AND name = 'TargetPronunciation')
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD TargetPronunciation NVARCHAR(200) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.AssessmentItems') AND name = 'PhoneticTranscription')
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD PhoneticTranscription NVARCHAR(200) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.AssessmentItems') AND name = 'MinimumAccuracy')
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD MinimumAccuracy INT DEFAULT 65;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.AssessmentItems') AND name = 'PronunciationTips')
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD PronunciationTips NVARCHAR(MAX) NULL;
END
GO

-- =============================================
-- PronunciationScores Table
-- =============================================
-- Stores detailed pronunciation assessment results
-- including phoneme-level accuracy and speech features

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PronunciationScores')
BEGIN
    CREATE TABLE dbo.PronunciationScores (
        ScoreID INT IDENTITY(1,1) PRIMARY KEY,
        ResponseID INT NOT NULL,
        StudentID INT NOT NULL,
        ItemID INT NOT NULL,

        -- Speech Recognition Results
        RecognizedText NVARCHAR(500) NULL,
        Confidence FLOAT NULL, -- 0.0 to 1.0

        -- Pronunciation Scoring
        OverallAccuracy INT NOT NULL, -- 0-100 percentage
        PronunciationScore FLOAT NULL, -- Detailed score from speech API
        FluencyScore FLOAT NULL,
        CompletenessScore FLOAT NULL,

        -- Phoneme-Level Analysis
        PhonemeAccuracyJSON NVARCHAR(MAX) NULL, -- JSON array of phoneme scores
        ErrorPhonemes NVARCHAR(500) NULL, -- Comma-separated list of mispronounced phonemes

        -- Audio Metadata
        AudioDuration INT NULL, -- Duration in milliseconds
        AudioQuality FLOAT NULL, -- 0.0 to 1.0
        BackgroundNoiseLevel FLOAT NULL,

        -- Speech API Details
        SpeechAPIProvider VARCHAR(50) DEFAULT 'Google Cloud Speech',
        APIResponseJSON NVARCHAR(MAX) NULL, -- Full API response for debugging

        -- Timestamps
        CreatedAt DATETIME DEFAULT GETDATE(),

        -- Foreign Keys
        CONSTRAINT FK_PronunciationScores_Response FOREIGN KEY (ResponseID)
            REFERENCES dbo.StudentResponses(ResponseID) ON DELETE CASCADE,
        CONSTRAINT FK_PronunciationScores_Student FOREIGN KEY (StudentID)
            REFERENCES dbo.Students(StudentID) ON DELETE NO ACTION,
        CONSTRAINT FK_PronunciationScores_Item FOREIGN KEY (ItemID)
            REFERENCES dbo.AssessmentItems(ItemID) ON DELETE NO ACTION
    );

    -- Indexes for performance
    CREATE INDEX IX_PronunciationScores_Student ON dbo.PronunciationScores(StudentID);
    CREATE INDEX IX_PronunciationScores_Item ON dbo.PronunciationScores(ItemID);
    CREATE INDEX IX_PronunciationScores_Accuracy ON dbo.PronunciationScores(OverallAccuracy);
    CREATE INDEX IX_PronunciationScores_CreatedAt ON dbo.PronunciationScores(CreatedAt);
END
GO

-- =============================================
-- StudentPronunciationProgress Table
-- =============================================
-- Tracks long-term pronunciation improvement

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentPronunciationProgress')
BEGIN
    CREATE TABLE dbo.StudentPronunciationProgress (
        ProgressID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL,

        -- Progress Metrics
        AssessmentDate DATE NOT NULL,
        AverageAccuracy FLOAT NOT NULL,
        WordsAttempted INT DEFAULT 0,
        WordsPassed INT DEFAULT 0, -- Accuracy >= MinimumAccuracy

        -- Phoneme Mastery
        MasteredPhonemes NVARCHAR(500) NULL, -- Comma-separated
        ProblematicPhonemes NVARCHAR(500) NULL, -- Comma-separated

        -- Improvement Tracking
        AccuracyImprovement FLOAT NULL, -- Compared to previous week
        StreakDays INT DEFAULT 0,

        CreatedAt DATETIME DEFAULT GETDATE(),
        UpdatedAt DATETIME DEFAULT GETDATE(),

        CONSTRAINT FK_PronunciationProgress_Student FOREIGN KEY (StudentID)
            REFERENCES dbo.Students(StudentID) ON DELETE CASCADE
    );

    CREATE INDEX IX_PronunciationProgress_Student ON dbo.StudentPronunciationProgress(StudentID);
    CREATE INDEX IX_PronunciationProgress_Date ON dbo.StudentPronunciationProgress(AssessmentDate);
END
GO

-- =============================================
-- Stored Procedure: Record Pronunciation Score
-- =============================================

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_RecordPronunciationScore')
    DROP PROCEDURE dbo.SP_RecordPronunciationScore;
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

        -- Insert pronunciation score
        INSERT INTO dbo.PronunciationScores (
            ResponseID, StudentID, ItemID,
            RecognizedText, Confidence,
            OverallAccuracy, PronunciationScore, FluencyScore, CompletenessScore,
            PhonemeAccuracyJSON, ErrorPhonemes,
            AudioDuration, AudioQuality,
            APIResponseJSON
        )
        VALUES (
            @ResponseID, @StudentID, @ItemID,
            @RecognizedText, @Confidence,
            @OverallAccuracy, @PronunciationScore, @FluencyScore, @CompletenessScore,
            @PhonemeAccuracyJSON, @ErrorPhonemes,
            @AudioDuration, @AudioQuality,
            @APIResponseJSON
        );

        -- Update or create pronunciation progress
        DECLARE @Today DATE = CAST(GETDATE() AS DATE);
        DECLARE @ExistingProgress INT;

        SELECT @ExistingProgress = ProgressID
        FROM dbo.StudentPronunciationProgress
        WHERE StudentID = @StudentID AND AssessmentDate = @Today;

        IF @ExistingProgress IS NOT NULL
        BEGIN
            -- Update existing progress
            UPDATE dbo.StudentPronunciationProgress
            SET WordsAttempted = WordsAttempted + 1,
                WordsPassed = WordsPassed + CASE WHEN @OverallAccuracy >= 65 THEN 1 ELSE 0 END,
                AverageAccuracy = (
                    SELECT AVG(CAST(ps.OverallAccuracy AS FLOAT))
                    FROM dbo.PronunciationScores ps
                    WHERE ps.StudentID = @StudentID
                        AND CAST(ps.CreatedAt AS DATE) = @Today
                ),
                UpdatedAt = GETDATE()
            WHERE ProgressID = @ExistingProgress;
        END
        ELSE
        BEGIN
            -- Create new progress entry
            INSERT INTO dbo.StudentPronunciationProgress (
                StudentID, AssessmentDate,
                AverageAccuracy, WordsAttempted, WordsPassed
            )
            VALUES (
                @StudentID, @Today,
                @OverallAccuracy, 1,
                CASE WHEN @OverallAccuracy >= 65 THEN 1 ELSE 0 END
            );
        END

        COMMIT TRANSACTION;

        -- Return the created score ID
        SELECT SCOPE_IDENTITY() AS ScoreID;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        THROW;
    END CATCH
END
GO

-- =============================================
-- Stored Procedure: Get Student Pronunciation Progress
-- =============================================

IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_GetPronunciationProgress')
    DROP PROCEDURE dbo.SP_GetPronunciationProgress;
GO

CREATE PROCEDURE dbo.SP_GetPronunciationProgress
    @StudentID INT,
    @DaysBack INT = 30
AS
BEGIN
    SET NOCOUNT ON;

    -- Get progress over time
    SELECT
        AssessmentDate,
        AverageAccuracy,
        WordsAttempted,
        WordsPassed,
        CAST(WordsPassed AS FLOAT) / NULLIF(WordsAttempted, 0) * 100 AS PassRate,
        MasteredPhonemes,
        ProblematicPhonemes,
        StreakDays
    FROM dbo.StudentPronunciationProgress
    WHERE StudentID = @StudentID
        AND AssessmentDate >= DATEADD(DAY, -@DaysBack, GETDATE())
    ORDER BY AssessmentDate DESC;

    -- Get most common errors
    SELECT TOP 10
        ErrorPhonemes,
        COUNT(*) AS ErrorCount
    FROM dbo.PronunciationScores
    WHERE StudentID = @StudentID
        AND ErrorPhonemes IS NOT NULL
        AND ErrorPhonemes != ''
    GROUP BY ErrorPhonemes
    ORDER BY ErrorCount DESC;
END
GO

-- =============================================
-- Sample Comment
-- =============================================
PRINT 'Pronunciation schema extension created successfully!';
PRINT 'Added columns: AudioPromptURL, TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips';
PRINT 'Created tables: PronunciationScores, StudentPronunciationProgress';
PRINT 'Created procedures: SP_RecordPronunciationScore, SP_GetPronunciationProgress';
GO
