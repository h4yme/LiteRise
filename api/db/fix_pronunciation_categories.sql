-- =============================================
-- Fix Pronunciation Items Category Assignment
-- =============================================
-- This script ensures ALL pronunciation items are categorized as "Oral Language" only
-- Fixes the issue where pronunciation items were scattered across multiple categories
-- =============================================

USE LiteRiseDB;
GO

PRINT 'Fixing pronunciation item categories...';
PRINT '';

-- Show current state
PRINT 'Current pronunciation items by category:';
SELECT
    Category,
    COUNT(*) AS Count
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
GROUP BY Category
ORDER BY Category;
PRINT '';

-- Update ALL pronunciation items to be in "Oral Language" category
UPDATE dbo.AssessmentItems
SET Category = 'Oral Language'
WHERE QuestionType = 'Pronunciation'
  AND Category != 'Oral Language';

PRINT 'Updated pronunciation items to "Oral Language" category.';
PRINT '';

-- Show fixed state
PRINT 'Fixed pronunciation items by category:';
SELECT
    Category,
    COUNT(*) AS Count
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
GROUP BY Category
ORDER BY Category;
PRINT '';

-- Verify no pronunciation items exist in other categories
DECLARE @WrongCategoryCount INT;
SELECT @WrongCategoryCount = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
  AND Category != 'Oral Language';

IF @WrongCategoryCount = 0
BEGIN
    PRINT '✓ SUCCESS: All pronunciation items are now in "Oral Language" category!';
END
ELSE
BEGIN
    PRINT '✗ WARNING: ' + CAST(@WrongCategoryCount AS VARCHAR) + ' pronunciation items still in wrong category!';
END

PRINT '';
PRINT 'Category distribution summary:';
SELECT
    Category,
    QuestionType,
    COUNT(*) AS ItemCount
FROM dbo.AssessmentItems
WHERE Category IN ('Oral Language', 'Word Knowledge', 'Reading Comprehension', 'Language Structure')
GROUP BY Category, QuestionType
ORDER BY Category, QuestionType;

GO
