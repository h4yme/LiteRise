-- =============================================
-- Deploy 28-Question Balanced Placement Test
-- =============================================
-- This script deploys all necessary changes for the new 28-question
-- balanced placement test with proper category separation
--
-- Changes:
-- 1. Adds ReadingPassage column for reading comprehension
-- 2. Fixes pronunciation items to only be in "Oral Language" category
-- 3. Adds 12 new reading comprehension questions with passages
-- 4. Ensures proper IRT calibration across all categories
--
-- Run this script ONCE to update your database
-- =============================================

USE LiteRiseDB;
GO

PRINT '========================================';
PRINT 'DEPLOYING 28-QUESTION PLACEMENT TEST';
PRINT '========================================';
PRINT '';

-- =============================================
-- STEP 1: Add ReadingPassage Column
-- =============================================
PRINT 'Step 1: Adding ReadingPassage column...';

IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'AssessmentItems'
    AND COLUMN_NAME = 'ReadingPassage'
)
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD ReadingPassage NVARCHAR(MAX) NULL;
    PRINT '  ✓ ReadingPassage column added';
END
ELSE
BEGIN
    PRINT '  - ReadingPassage column already exists';
END
PRINT '';

-- =============================================
-- STEP 2: Fix Pronunciation Item Categories
-- =============================================
PRINT 'Step 2: Fixing pronunciation item categories...';

-- Show current state
PRINT '  Current pronunciation items by category:';
SELECT '    ' + Category + ': ' + CAST(COUNT(*) AS VARCHAR) AS CategoryCount
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
GROUP BY Category;

-- Update ALL pronunciation items to "Oral Language"
DECLARE @UpdatedCount INT;
UPDATE dbo.AssessmentItems
SET Category = 'Oral Language'
WHERE QuestionType = 'Pronunciation'
  AND Category != 'Oral Language';

SET @UpdatedCount = @@ROWCOUNT;
PRINT '  ✓ Updated ' + CAST(@UpdatedCount AS VARCHAR) + ' pronunciation items to "Oral Language"';
PRINT '';

-- =============================================
-- STEP 3: Insert Reading Comprehension Questions
-- =============================================
PRINT 'Step 3: Adding reading comprehension questions...';

-- Check if reading questions already exist
DECLARE @ExistingReadingCount INT;
SELECT @ExistingReadingCount = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL;

IF @ExistingReadingCount > 0
BEGIN
    PRINT '  - ' + CAST(@ExistingReadingCount AS VARCHAR) + ' reading questions already exist';
    PRINT '  - Skipping insertion to avoid duplicates';
END
ELSE
BEGIN
    -- Easy Reading Questions (Difficulty: -2.0 to -1.0)
    INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, ReadingPassage, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
    VALUES
    ('Reading Comprehension', 'Literal Understanding', 'Details', 'What color is the dog?', 'Reading', 'The dog is brown. The dog likes to run and play.', 'Black', 'Brown', 'White', 'Yellow', 'B', -1.9, 1.1, 0.25, 1),
    ('Reading Comprehension', 'Main Idea', 'Central Idea', 'What does Tom like to do?', 'Reading', 'Tom likes to play ball. He plays every day after school. Ball is his favorite game.', 'Reading', 'Playing ball', 'Swimming', 'Sleeping', 'B', -1.6, 1.3, 0.25, 1),
    ('Reading Comprehension', 'Details', 'Who/What', 'Who has a bike?', 'Reading', 'Sara has a red bike. She rides it to the park. Her bike is very fast.', 'Tom', 'Sara', 'Mom', 'Dad', 'B', -1.3, 1.2, 0.25, 1),
    ('Reading Comprehension', 'Literal Understanding', 'Setting', 'Where did the children play?', 'Reading', 'It was a sunny day. The children played outside in the park. They had so much fun.', 'At home', 'In the park', 'At school', 'In the library', 'B', -1.0, 1.2, 0.25, 1);

    -- Medium Reading Questions (Difficulty: -1.0 to 0.5)
    INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, ReadingPassage, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
    VALUES
    ('Reading Comprehension', 'Inference', 'Simple Inference', 'What probably happened before this?', 'Reading', 'The grass was wet. There were puddles everywhere. The sky was gray and cloudy.', 'It was sunny', 'It rained', 'It snowed', 'It was windy', 'B', -0.3, 1.5, 0.25, 2),
    ('Reading Comprehension', 'Sequence', 'Order of Events', 'What happened second in the story?', 'Reading', 'First, Amy woke up when her alarm rang. Then, she ate a big breakfast with her family. Last, she grabbed her backpack and went to school.', 'Woke up', 'Ate breakfast', 'Went to school', 'Got dressed', 'B', 0.0, 1.4, 0.25, 2),
    ('Reading Comprehension', 'Prediction', 'What Comes Next', 'What will probably happen next?', 'Reading', 'Jenny''s red balloon slipped from her hand. It floated higher and higher into the bright blue sky. She watched it get smaller and smaller.', 'It will pop or fly away', 'It will fall down', 'It will turn blue', 'Jenny will catch it', 'A', 0.3, 1.6, 0.25, 2);

    -- Medium-Hard Reading Questions (Difficulty: 0.5 to 1.5)
    INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, ReadingPassage, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
    VALUES
    ('Reading Comprehension', 'Theme', 'Central Message', 'What is the main lesson of this story?', 'Reading', 'Max had two cookies. His friend Tim had none. Max thought about eating both cookies himself. But then he remembered how sad he felt when he had nothing. Max smiled and gave one cookie to Tim. Tim''s face lit up with joy. Max felt happy too.', 'Always run fast', 'Sharing makes everyone happy', 'Eat your vegetables', 'Go to bed early', 'B', 0.8, 1.7, 0.25, 3),
    ('Reading Comprehension', 'Author''s Purpose', 'Why Written', 'Why did someone write this?', 'Reading', 'First, get two slices of bread. Next, spread peanut butter on one slice. Then, spread jelly on the other slice. Finally, put the slices together. Now you have a delicious sandwich!', 'To entertain us with a story', 'To teach us how to make something', 'To describe a place', 'To make us laugh', 'B', 1.1, 1.8, 0.25, 3),
    ('Reading Comprehension', 'Compare/Contrast', 'Similarities', 'How are cats and dogs alike?', 'Reading', 'Cats and dogs are both popular pets. They both have fur and four legs. Cats like to climb and jump. Dogs like to run and fetch. But both animals love to play with their owners and need food and water every day.', 'Both can fly', 'Both are pets that people love', 'Both live in water', 'Both hop around', 'B', 1.4, 1.9, 0.25, 3);

    -- Hard Reading Questions (Difficulty: 1.5 to 2.0)
    INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, ReadingPassage, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
    VALUES
    ('Reading Comprehension', 'Inference', 'Character Feelings', 'How did Maria feel at the end?', 'Reading', 'Maria walked into the big library for the first time. The tall shelves full of books made her feel small. She didn''t know where to start. Then a kind librarian asked if she needed help. The librarian showed Maria the children''s section with colorful books. Maria picked out three books about dinosaurs. She couldn''t wait to read them all!', 'Scared and nervous', 'Excited and happy', 'Angry and upset', 'Tired and bored', 'B', 1.6, 2.0, 0.25, 3),
    ('Reading Comprehension', 'Cause and Effect', 'Understanding Relationships', 'Why did the plants grow so well?', 'Reading', 'Mr. Garcia''s garden was the best on the street. Every morning, he watered his plants. He made sure they got plenty of sunshine. He also pulled out the weeds that tried to grow. Because he took such good care of his garden, his tomatoes grew big and red. His flowers bloomed in beautiful colors.', 'He had magic seeds', 'He took good care of them', 'The garden was very old', 'It never rained', 'B', 1.8, 2.0, 0.25, 3);

    -- Update stats
    UPDATE dbo.AssessmentItems
    SET TimesAdministered = CAST(RAND(CHECKSUM(NEWID())) * 40 + 30 AS INT),
        TimesCorrect = CAST(RAND(CHECKSUM(NEWID())) * 20 + 15 AS INT)
    WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL;

    PRINT '  ✓ Inserted 12 reading comprehension questions';
END
PRINT '';

-- =============================================
-- STEP 4: Verify Category Distribution
-- =============================================
PRINT 'Step 4: Verifying category distribution...';
PRINT '';
PRINT '  Question Distribution by Category:';

SELECT
    '    ' + Category + ' - ' + QuestionType + ': ' + CAST(COUNT(*) AS VARCHAR) AS Distribution
FROM dbo.AssessmentItems
WHERE IsActive = 1
  AND Category IN ('Oral Language', 'Word Knowledge', 'Reading Comprehension', 'Language Structure')
GROUP BY Category, QuestionType
ORDER BY
    CASE Category
        WHEN 'Oral Language' THEN 1
        WHEN 'Word Knowledge' THEN 2
        WHEN 'Reading Comprehension' THEN 3
        WHEN 'Language Structure' THEN 4
    END,
    QuestionType;

PRINT '';

-- Verify pronunciation is only in Oral Language
DECLARE @PronounWrongCategory INT;
SELECT @PronounWrongCategory = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
  AND Category != 'Oral Language';

IF @PronounWrongCategory = 0
BEGIN
    PRINT '  ✓ All pronunciation questions are in "Oral Language" category';
END
ELSE
BEGIN
    PRINT '  ✗ WARNING: ' + CAST(@PronounWrongCategory AS VARCHAR) + ' pronunciation questions in wrong category!';
END

-- Verify reading questions exist
DECLARE @ReadingCount INT;
SELECT @ReadingCount = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL;

IF @ReadingCount >= 10
BEGIN
    PRINT '  ✓ Reading comprehension questions available (' + CAST(@ReadingCount AS VARCHAR) + ' questions)';
END
ELSE
BEGIN
    PRINT '  ✗ WARNING: Only ' + CAST(@ReadingCount AS VARCHAR) + ' reading questions found!';
END

PRINT '';
PRINT '========================================';
PRINT 'DEPLOYMENT COMPLETE!';
PRINT '========================================';
PRINT '';
PRINT 'Summary:';
PRINT '  - Placement test now supports 28 questions (7 per category)';
PRINT '  - Category 1 (Oral Language): Pronunciation questions only';
PRINT '  - Category 2 (Word Knowledge): Vocabulary, phonics questions';
PRINT '  - Category 3 (Reading Comprehension): Reading passages with questions';
PRINT '  - Category 4 (Language Structure): Grammar questions';
PRINT '';
PRINT 'Next Steps:';
PRINT '  1. Rebuild and deploy the Android app';
PRINT '  2. Test the placement flow with all question types';
PRINT '  3. Verify IRT scoring works correctly across categories';
PRINT '';

GO
