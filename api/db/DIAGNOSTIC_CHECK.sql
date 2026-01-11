-- =============================================
-- DIAGNOSTIC: Check Database State
-- =============================================

USE LiteRiseDB;
GO

PRINT '========================================';
PRINT 'DATABASE DIAGNOSTIC CHECK';
PRINT '========================================';
PRINT '';

-- Check 1: Total items
PRINT '1. Total Items in Database:';
SELECT COUNT(*) AS TotalItems FROM dbo.AssessmentItems;
PRINT '';

-- Check 2: Item ID range
PRINT '2. Item ID Range:';
SELECT
    MIN(ItemID) AS MinID,
    MAX(ItemID) AS MaxID
FROM dbo.AssessmentItems;
PRINT '';

-- Check 3: Active vs Inactive
PRINT '3. Active Status:';
SELECT
    IsActive,
    COUNT(*) AS Count
FROM dbo.AssessmentItems
GROUP BY IsActive;
PRINT '';

-- Check 4: Reading items with/without passages
PRINT '4. Reading Comprehension Items:';
SELECT
    ItemID,
    QuestionText,
    CASE
        WHEN ReadingPassage IS NULL THEN 'NULL'
        WHEN ReadingPassage = '' THEN 'EMPTY'
        ELSE 'HAS PASSAGE (' + CAST(LEN(ReadingPassage) AS VARCHAR) + ' chars)'
    END AS PassageStatus
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading'
ORDER BY ItemID;
PRINT '';

-- Check 5: Sample reading passage
PRINT '5. Sample Reading Passage Content:';
SELECT TOP 1
    ItemID,
    QuestionText,
    ReadingPassage
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading'
  AND ReadingPassage IS NOT NULL
  AND ReadingPassage != ''
ORDER BY ItemID;
PRINT '';

-- Check 6: Items by category
PRINT '6. Distribution by Category:';
SELECT
    Category,
    QuestionType,
    COUNT(*) AS Count
FROM dbo.AssessmentItems
GROUP BY Category, QuestionType
ORDER BY Category;
PRINT '';

-- Check 7: Specific item 327
PRINT '7. Checking Item 327:';
IF EXISTS (SELECT 1 FROM dbo.AssessmentItems WHERE ItemID = 327)
BEGIN
    SELECT
        ItemID,
        Category,
        QuestionType,
        QuestionText,
        CASE
            WHEN ReadingPassage IS NULL THEN '❌ NULL'
            WHEN ReadingPassage = '' THEN '❌ EMPTY STRING'
            ELSE '✓ HAS PASSAGE: ' + LEFT(ReadingPassage, 50) + '...'
        END AS PassageStatus,
        IsActive
    FROM dbo.AssessmentItems
    WHERE ItemID = 327;
END
ELSE
BEGIN
    PRINT 'Item 327 does NOT exist';
END
PRINT '';

PRINT '========================================';
PRINT 'RECOMMENDATIONS:';
PRINT '========================================';

DECLARE @MaxID INT, @MinID INT, @Total INT;
SELECT @MaxID = MAX(ItemID), @MinID = MIN(ItemID), @Total = COUNT(*)
FROM dbo.AssessmentItems;

IF @MinID != 1 OR @MaxID > 100
BEGIN
    PRINT '❌ PROBLEM: Item IDs not starting from 1';
    PRINT '   Current range: ' + CAST(@MinID AS VARCHAR) + ' - ' + CAST(@MaxID AS VARCHAR);
    PRINT '   Expected: 1 - 40';
    PRINT '';
    PRINT 'ACTION REQUIRED:';
    PRINT '1. The table was NOT properly truncated';
    PRINT '2. You need to run the fix script below';
END
ELSE IF @Total != 40
BEGIN
    PRINT '❌ PROBLEM: Wrong number of items';
    PRINT '   Current: ' + CAST(@Total AS VARCHAR);
    PRINT '   Expected: 40';
    PRINT '';
    PRINT 'ACTION REQUIRED: Re-run insert script';
END
ELSE
BEGIN
    PRINT '✓ Item IDs look correct (1-40)';
    PRINT '';

    -- Check reading passages
    DECLARE @NullPassages INT;
    SELECT @NullPassages = COUNT(*)
    FROM dbo.AssessmentItems
    WHERE QuestionType = 'Reading'
      AND (ReadingPassage IS NULL OR ReadingPassage = '');

    IF @NullPassages > 0
    BEGIN
        PRINT '❌ PROBLEM: ' + CAST(@NullPassages AS VARCHAR) + ' reading items missing passages';
        PRINT 'ACTION REQUIRED: Update reading items with passages';
    END
    ELSE
    BEGIN
        PRINT '✓ All reading items have passages!';
        PRINT '';
        PRINT 'Database is ready! Problem might be in app cache.';
        PRINT 'Try:';
        PRINT '  1. Rebuild Android app';
        PRINT '  2. Clear app data on device';
        PRINT '  3. Fresh install';
    END
END

PRINT '';
GO
