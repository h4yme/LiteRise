-- =============================================
-- FIX: Update SP_GetNextAdaptiveQuestion to include ReadingPassage
-- =============================================

USE LiteRiseDB;
GO

PRINT 'Updating SP_GetNextAdaptiveQuestion to include ReadingPassage...';

-- Drop existing procedure
IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_GetNextAdaptiveQuestion')
BEGIN
    DROP PROCEDURE dbo.SP_GetNextAdaptiveQuestion;
    PRINT '✓ Dropped old procedure';
END

GO

-- Create updated procedure with ReadingPassage
CREATE PROCEDURE dbo.SP_GetNextAdaptiveQuestion
    @StudentID INT,
    @SessionID INT,
    @CurrentTheta FLOAT,
    @AssessmentType VARCHAR(20),
    @CategoryFilter VARCHAR(50) = NULL
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
    SELECT TOP 1
        ItemID,
        Category,
        Subcategory,
        SkillArea,
        QuestionText,
        QuestionType,
        ReadingPassage,  -- ← ADDED THIS!
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

PRINT '✓ Updated SP_GetNextAdaptiveQuestion with ReadingPassage column';
PRINT '';
PRINT 'Testing the fix...';

-- Test the procedure
DECLARE @TestResult TABLE (
    ItemID INT,
    QuestionType VARCHAR(50),
    ReadingPassage NVARCHAR(MAX)
);

INSERT INTO @TestResult
EXEC dbo.SP_GetNextAdaptiveQuestion
    @StudentID = 999,
    @SessionID = 999,
    @CurrentTheta = 0.0,
    @AssessmentType = 'PreAssessment',
    @CategoryFilter = 'Reading Comprehension';

-- Check if ReadingPassage is returned
IF EXISTS (SELECT 1 FROM @TestResult WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL)
BEGIN
    PRINT '✓ SUCCESS! ReadingPassage is now being returned';
    SELECT TOP 1
        'ItemID: ' + CAST(ItemID AS VARCHAR) AS Info,
        'Passage: ' + LEFT(ReadingPassage, 50) + '...' AS PassagePreview
    FROM @TestResult;
END
ELSE
BEGIN
    PRINT '✗ WARNING: ReadingPassage still null';
END

PRINT '';
PRINT '========================================';
PRINT 'FIX COMPLETE!';
PRINT '========================================';
PRINT 'The API will now return reading passages.';
PRINT 'No need to rebuild Android app.';
PRINT 'Just restart the test and it will work!';
PRINT '';

GO
