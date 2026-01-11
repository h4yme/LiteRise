-- =============================================
-- Database Status Check for 28-Question Test
-- =============================================
-- Run this to verify your database is ready
-- =============================================

USE LiteRiseDB;
GO

PRINT '========================================';
PRINT 'DATABASE STATUS CHECK';
PRINT '========================================';
PRINT '';

-- Check 1: ReadingPassage column exists
PRINT '1. Checking ReadingPassage column...';
IF EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'AssessmentItems'
    AND COLUMN_NAME = 'ReadingPassage'
)
BEGIN
    PRINT '  ✓ ReadingPassage column EXISTS';
END
ELSE
BEGIN
    PRINT '  ✗ ReadingPassage column MISSING!';
    PRINT '  --> RUN: add_reading_passage_field.sql';
END
PRINT '';

-- Check 2: Pronunciation items in correct category
PRINT '2. Checking pronunciation categories...';
DECLARE @WrongCategoryCount INT;
SELECT @WrongCategoryCount = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
  AND Category != 'Oral Language';

IF @WrongCategoryCount = 0
BEGIN
    PRINT '  ✓ All pronunciation items in "Oral Language"';
END
ELSE
BEGIN
    PRINT '  ✗ ' + CAST(@WrongCategoryCount AS VARCHAR) + ' pronunciation items in WRONG category!';
    PRINT '  --> RUN: fix_pronunciation_categories.sql';
END
PRINT '';

-- Check 3: Reading questions with passages
PRINT '3. Checking reading comprehension questions...';
DECLARE @ReadingWithPassages INT;
SELECT @ReadingWithPassages = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading'
  AND ReadingPassage IS NOT NULL
  AND ReadingPassage != '';

PRINT '  Reading questions with passages: ' + CAST(@ReadingWithPassages AS VARCHAR);
IF @ReadingWithPassages >= 10
BEGIN
    PRINT '  ✓ Sufficient reading questions';
END
ELSE
BEGIN
    PRINT '  ✗ Need more reading questions!';
    PRINT '  --> RUN: sample_reading_comprehension.sql';
END
PRINT '';

-- Check 4: Category distribution
PRINT '4. Category distribution:';
SELECT
    '  ' + Category + ' - ' + QuestionType + ': ' + CAST(COUNT(*) AS VARCHAR) AS Distribution
FROM dbo.AssessmentItems
WHERE IsActive = 1
GROUP BY Category, QuestionType
ORDER BY Category, QuestionType;
PRINT '';

-- Check 5: Sample reading passage content
PRINT '5. Sample reading passage (first one):';
SELECT TOP 1
    '  ItemID: ' + CAST(ItemID AS VARCHAR) AS Info,
    '  Question: ' + LEFT(QuestionText, 50) + '...' AS Question,
    '  Passage: ' + LEFT(ISNULL(ReadingPassage, 'NULL'), 60) + '...' AS Passage
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading'
ORDER BY ItemID;
PRINT '';

-- Summary
PRINT '========================================';
PRINT 'REQUIRED ACTIONS:';
PRINT '========================================';

DECLARE @IssueCount INT = 0;

-- Check each issue
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'AssessmentItems' AND COLUMN_NAME = 'ReadingPassage')
BEGIN
    SET @IssueCount = @IssueCount + 1;
    PRINT CAST(@IssueCount AS VARCHAR) + '. Run add_reading_passage_field.sql';
END

SELECT @WrongCategoryCount = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation' AND Category != 'Oral Language';

IF @WrongCategoryCount > 0
BEGIN
    SET @IssueCount = @IssueCount + 1;
    PRINT CAST(@IssueCount AS VARCHAR) + '. Run fix_pronunciation_categories.sql';
END

SELECT @ReadingWithPassages = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL AND ReadingPassage != '';

IF @ReadingWithPassages < 10
BEGIN
    SET @IssueCount = @IssueCount + 1;
    PRINT CAST(@IssueCount AS VARCHAR) + '. Run sample_reading_comprehension.sql';
END

IF @IssueCount = 0
BEGIN
    PRINT '✓ DATABASE IS READY!';
    PRINT '';
    PRINT 'Next step: Rebuild Android app';
    PRINT '  1. Build > Clean Project';
    PRINT '  2. Build > Rebuild Project';
    PRINT '  3. Uninstall app from device';
    PRINT '  4. Run > Run ''app''';
END
ELSE
BEGIN
    PRINT '';
    PRINT 'After running required scripts:';
    PRINT '  Run DEPLOY_PLACEMENT_28Q.sql (runs all at once)';
END

PRINT '';
PRINT '========================================';

GO
