-- ============================================================================
-- LiteRise Placement Test Restructure - 5 Module Categories
-- ============================================================================
-- Changes:
-- 1. Update placement test from 4 categories to 5 module-based categories
-- 2. Clear old assessment items
-- 3. Insert new items for 5 modules (6 items each = 30 total)
-- 4. Update module ordering logic based on weakest performance
-- ============================================================================

USE LiteRiseDB;
GO

-- ============================================================================
-- PHASE 1: Update Modules Table with All 5 Modules
-- ============================================================================

-- Clear existing modules if needed
DELETE FROM Modules WHERE ModuleID BETWEEN 1 AND 5;
GO

SET IDENTITY_INSERT Modules ON;

INSERT INTO Modules (ModuleID, ModuleName, Description, GradeLevel, TotalLessons, OrderIndex, IconName, ColorCode, CreatedDate)
VALUES
-- Module 1: Phonics and Word Study
(1, 'Phonics and Word Study',
 'Master foundational reading skills through phonics patterns, sight words, and word recognition strategies',
 3, 12, 1, 'ic_phonics', '#7C3AED', GETDATE()),

-- Module 2: Vocabulary and Word Knowledge
(2, 'Vocabulary and Word Knowledge',
 'Build a rich vocabulary through context clues, word relationships, and meaning-making strategies',
 3, 12, 2, 'ic_vocabulary', '#EF4444', GETDATE()),

-- Module 3: Grammar Awareness and Grammatical Structures
(3, 'Grammar Awareness and Grammatical Structures',
 'Develop understanding of sentence structure, parts of speech, and grammatical conventions',
 3, 12, 3, 'ic_grammar', '#10B981', GETDATE()),

-- Module 4: Comprehending and Analyzing Text
(4, 'Comprehending and Analyzing Text',
 'Strengthen reading comprehension through inference, analysis, and critical thinking skills',
 3, 12, 4, 'ic_comprehension', '#F59E0B', GETDATE()),

-- Module 5: Creating and Composing Text
(5, 'Creating and Composing Text',
 'Express ideas clearly through writing, focusing on organization, voice, and narrative techniques',
 3, 12, 5, 'ic_writing', '#3B82F6', GETDATE());

SET IDENTITY_INSERT Modules OFF;
GO

-- ============================================================================
-- PHASE 2: Update Items Table Category Field
-- ============================================================================

-- Add ModuleCategory field if not exists
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Items' AND COLUMN_NAME = 'ModuleCategory')
BEGIN
    ALTER TABLE Items ADD ModuleCategory NVARCHAR(100) NULL;
END
GO

-- Add IsPlacementItem field to distinguish placement test items
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Items' AND COLUMN_NAME = 'IsPlacementItem')
BEGIN
    ALTER TABLE Items ADD IsPlacementItem BIT DEFAULT 0;
END
GO

-- ============================================================================
-- PHASE 3: Clear Old Placement Test Items
-- ============================================================================

-- Delete old placement test items (be careful with this in production!)
DELETE FROM Responses WHERE ItemID IN (SELECT ItemID FROM Items WHERE IsPlacementItem = 1);
DELETE FROM Items WHERE IsPlacementItem = 1;
GO

PRINT 'Old placement test items cleared.';
GO

-- ============================================================================
-- PHASE 4: Insert New Placement Test Items (5 Modules x 6 Items = 30 Items)
-- ============================================================================

-- ============================================================================
-- MODULE 1: Phonics and Word Study (Items 1-6)
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, IsPlacementItem, ModuleCategory, CreatedAt)
VALUES
-- Item 1: Easy - Beginning sounds
('Which word starts with the same sound as "cat"?',
 'multiple_choice', 'Easy', -1.5, 1.2, 0.25,
 'can', '["can", "dog", "sun", "pen"]',
 3, 1, 1, 'Phonics and Word Study', GETDATE()),

-- Item 2: Easy - Rhyming words
('Which word rhymes with "hat"?',
 'multiple_choice', 'Easy', -1.2, 1.3, 0.25,
 'bat', '["bat", "hop", "run", "big"]',
 3, 1, 1, 'Phonics and Word Study', GETDATE()),

-- Item 3: Medium - Sight words
('Complete the sentence: I ____ to school every day.',
 'multiple_choice', 'Medium', 0.0, 1.5, 0.25,
 'go', '["go", "going", "went", "goes"]',
 3, 1, 1, 'Phonics and Word Study', GETDATE()),

-- Item 4: Medium - Blending sounds
('Which word can you make with these sounds: /c/ /a/ /t/?',
 'multiple_choice', 'Medium', 0.3, 1.4, 0.25,
 'cat', '["cat", "cut", "cot", "cart"]',
 3, 1, 1, 'Phonics and Word Study', GETDATE()),

-- Item 5: Hard - Complex phonics patterns
('Which word has the same vowel sound as "cake"?',
 'multiple_choice', 'Hard', 1.0, 1.6, 0.25,
 'rain', '["rain", "can", "car", "ran"]',
 3, 1, 1, 'Phonics and Word Study', GETDATE()),

-- Item 6: Hard - Advanced word patterns
('Choose the word that follows the CVCe pattern (consonant-vowel-consonant-e):',
 'multiple_choice', 'Hard', 1.2, 1.5, 0.25,
 'bike', '["bike", "brick", "back", "black"]',
 3, 1, 1, 'Phonics and Word Study', GETDATE());
GO

-- ============================================================================
-- MODULE 2: Vocabulary and Word Knowledge (Items 7-12)
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, IsPlacementItem, ModuleCategory, CreatedAt)
VALUES
-- Item 7: Easy - Basic synonyms
('Which word means the same as "happy"?',
 'multiple_choice', 'Easy', -1.3, 1.2, 0.25,
 'glad', '["glad", "sad", "angry", "tired"]',
 3, 1, 1, 'Vocabulary and Word Knowledge', GETDATE()),

-- Item 8: Easy - Basic antonyms
('What is the opposite of "hot"?',
 'multiple_choice', 'Easy', -1.0, 1.3, 0.25,
 'cold', '["cold", "warm", "cool", "fire"]',
 3, 1, 1, 'Vocabulary and Word Knowledge', GETDATE()),

-- Item 9: Medium - Context clues
('The dog was very THIRSTY after running. Thirsty means:',
 'multiple_choice', 'Medium', 0.2, 1.4, 0.25,
 'needing water', '["needing water", "very tired", "very fast", "very happy"]',
 3, 1, 1, 'Vocabulary and Word Knowledge', GETDATE()),

-- Item 10: Medium - Word categories
('Which word does NOT belong in this group: apple, banana, chair, mango?',
 'multiple_choice', 'Medium', 0.4, 1.5, 0.25,
 'chair', '["chair", "apple", "banana", "mango"]',
 3, 1, 1, 'Vocabulary and Word Knowledge', GETDATE()),

-- Item 11: Hard - Multiple meanings
('The word "bat" can mean a flying animal OR:',
 'multiple_choice', 'Hard', 1.1, 1.6, 0.25,
 'something used to hit a ball', '["something used to hit a ball", "a type of bird", "a kind of fish", "a small insect"]',
 3, 1, 1, 'Vocabulary and Word Knowledge', GETDATE()),

-- Item 12: Hard - Advanced vocabulary
('A "journey" is best described as:',
 'multiple_choice', 'Hard', 1.3, 1.5, 0.25,
 'a trip from one place to another', '["a trip from one place to another", "a type of vehicle", "a map", "a destination"]',
 3, 1, 1, 'Vocabulary and Word Knowledge', GETDATE());
GO

-- ============================================================================
-- MODULE 3: Grammar Awareness and Grammatical Structures (Items 13-18)
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, IsPlacementItem, ModuleCategory, CreatedAt)
VALUES
-- Item 13: Easy - Basic sentence structure
('Which sentence is correct?',
 'multiple_choice', 'Easy', -1.4, 1.2, 0.25,
 'The cat is sleeping.', '["The cat is sleeping.", "Cat the sleeping is.", "Is sleeping cat the.", "Sleeping is the cat."]',
 3, 1, 1, 'Grammar Awareness and Grammatical Structures', GETDATE()),

-- Item 14: Easy - Capitalization
('Which word should start with a capital letter? I like to play in the ____.',
 'multiple_choice', 'Easy', -1.1, 1.3, 0.25,
 'park', '["park", "Park", "PARK", "pArk"]',
 3, 1, 1, 'Grammar Awareness and Grammatical Structures', GETDATE()),

-- Item 15: Medium - Verb tense
('Choose the correct verb: Yesterday, I ____ to the store.',
 'multiple_choice', 'Medium', 0.1, 1.4, 0.25,
 'went', '["went", "go", "going", "will go"]',
 3, 1, 1, 'Grammar Awareness and Grammatical Structures', GETDATE()),

-- Item 16: Medium - Plural forms
('What is the plural of "child"?',
 'multiple_choice', 'Medium', 0.5, 1.5, 0.25,
 'children', '["children", "childs", "childes", "child"]',
 3, 1, 1, 'Grammar Awareness and Grammatical Structures', GETDATE()),

-- Item 17: Hard - Subject-verb agreement
('Which sentence has correct grammar?',
 'multiple_choice', 'Hard', 1.0, 1.6, 0.25,
 'The dogs are playing in the yard.', '["The dogs are playing in the yard.", "The dogs is playing in the yard.", "The dog are playing in the yard.", "The dogs playing in the yard."]',
 3, 1, 1, 'Grammar Awareness and Grammatical Structures', GETDATE()),

-- Item 18: Hard - Complex sentences
('Choose the sentence with correct punctuation:',
 'multiple_choice', 'Hard', 1.2, 1.5, 0.25,
 'My favorite foods are pizza, ice cream, and cookies.', '["My favorite foods are pizza, ice cream, and cookies.", "My favorite foods are pizza ice cream and cookies.", "My favorite foods are, pizza, ice cream, and cookies.", "My favorite foods are pizza ice cream, and cookies."]',
 3, 1, 1, 'Grammar Awareness and Grammatical Structures', GETDATE());
GO

-- ============================================================================
-- MODULE 4: Comprehending and Analyzing Text (Items 19-24)
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, IsPlacementItem, ModuleCategory, CreatedAt)
VALUES
-- Item 19: Easy - Literal comprehension
('Read: "The sun was shining. Birds were singing." What was the weather like?',
 'multiple_choice', 'Easy', -1.2, 1.2, 0.25,
 'sunny', '["sunny", "rainy", "cloudy", "snowy"]',
 3, 1, 1, 'Comprehending and Analyzing Text', GETDATE()),

-- Item 20: Easy - Sequencing
('Read: "First, I woke up. Then, I ate breakfast. Finally, I went to school." What happened second?',
 'multiple_choice', 'Easy', -0.9, 1.3, 0.25,
 'ate breakfast', '["ate breakfast", "woke up", "went to school", "came home"]',
 3, 1, 1, 'Comprehending and Analyzing Text', GETDATE()),

-- Item 21: Medium - Inference
('Read: "Maria saw dark clouds and grabbed her umbrella." Why did Maria take her umbrella?',
 'multiple_choice', 'Medium', 0.3, 1.4, 0.25,
 'She thought it might rain.', '["She thought it might rain.", "It was sunny outside.", "She liked umbrellas.", "Her mom told her to."]',
 3, 1, 1, 'Comprehending and Analyzing Text', GETDATE()),

-- Item 22: Medium - Main idea
('Read: "Dogs make great pets. They are loyal and friendly. They love to play and protect their families." What is the main idea?',
 'multiple_choice', 'Medium', 0.6, 1.5, 0.25,
 'Dogs are good pets.', '["Dogs are good pets.", "Dogs like to play.", "Families need protection.", "All animals are friendly."]',
 3, 1, 1, 'Comprehending and Analyzing Text', GETDATE()),

-- Item 23: Hard - Making predictions
('Read: "The egg began to crack. A tiny beak poked through the shell." What will most likely happen next?',
 'multiple_choice', 'Hard', 1.1, 1.6, 0.25,
 'A baby bird will come out.', '["A baby bird will come out.", "The egg will disappear.", "A snake will appear.", "The shell will fix itself."]',
 3, 1, 1, 'Comprehending and Analyzing Text', GETDATE()),

-- Item 24: Hard - Author's purpose
('Read: "Visit Sunny Beach Resort! We have pools, games, and delicious food!" Why was this written?',
 'multiple_choice', 'Hard', 1.3, 1.5, 0.25,
 'to convince people to visit the resort', '["to convince people to visit the resort", "to teach about beaches", "to tell a story", "to give directions"]',
 3, 1, 1, 'Comprehending and Analyzing Text', GETDATE());
GO

-- ============================================================================
-- MODULE 5: Creating and Composing Text (Items 25-30)
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, IsPlacementItem, ModuleCategory, CreatedAt)
VALUES
-- Item 25: Easy - Basic sentence writing
('Which is a complete sentence?',
 'multiple_choice', 'Easy', -1.3, 1.2, 0.25,
 'I like to read books.', '["I like to read books.", "Like to read.", "Books and reading.", "To read books like."]',
 3, 1, 1, 'Creating and Composing Text', GETDATE()),

-- Item 26: Easy - Organizing ideas
('What should come first in a story about a birthday party?',
 'multiple_choice', 'Easy', -1.0, 1.3, 0.25,
 'Getting ready for the party', '["Getting ready for the party", "Eating the cake", "Opening presents", "Saying goodbye to friends"]',
 3, 1, 1, 'Creating and Composing Text', GETDATE()),

-- Item 27: Medium - Descriptive writing
('Which sentence uses better describing words?',
 'multiple_choice', 'Medium', 0.2, 1.4, 0.25,
 'The fluffy, white puppy played happily in the green grass.', '["The fluffy, white puppy played happily in the green grass.", "The puppy played in the grass.", "A dog was outside.", "The animal moved."]',
 3, 1, 1, 'Creating and Composing Text', GETDATE()),

-- Item 28: Medium - Story elements
('Every good story needs:',
 'multiple_choice', 'Medium', 0.5, 1.5, 0.25,
 'a beginning, middle, and end', '["a beginning, middle, and end", "just a title", "only characters", "many pages"]',
 3, 1, 1, 'Creating and Composing Text', GETDATE()),

-- Item 29: Hard - Voice and style
('Which sentence shows the character is excited?',
 'multiple_choice', 'Hard', 1.0, 1.6, 0.25,
 '"I can''t wait to go to the park!" shouted Maya.', '["\\"I can''t wait to go to the park!\\" shouted Maya.", "Maya went to the park.", "The park is fun.", "She walked slowly."]',
 3, 1, 1, 'Creating and Composing Text', GETDATE()),

-- Item 30: Hard - Revision and editing
('Which sentence needs to be fixed?',
 'multiple_choice', 'Hard', 1.2, 1.5, 0.25,
 'me and my friend went to the mall', '["me and my friend went to the mall", "My friend and I went to the mall.", "We enjoyed our trip to the mall.", "The mall was very crowded."]',
 3, 1, 1, 'Creating and Composing Text', GETDATE());
GO

PRINT '✅ 30 new placement test items inserted successfully!';
PRINT 'Items per module:';
PRINT '  - Phonics and Word Study: 6 items';
PRINT '  - Vocabulary and Word Knowledge: 6 items';
PRINT '  - Grammar Awareness and Grammatical Structures: 6 items';
PRINT '  - Comprehending and Analyzing Text: 6 items';
PRINT '  - Creating and Composing Text: 6 items';
GO

-- ============================================================================
-- PHASE 5: Update Stored Procedure for Module Ordering
-- ============================================================================

CREATE OR ALTER PROCEDURE SP_GetModuleOrderByPlacementScore
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Calculate performance score for each module based on placement test
    WITH ModuleScores AS (
        SELECT
            i.ModuleCategory,
            COUNT(*) as TotalQuestions,
            SUM(CASE WHEN r.IsCorrect = 1 THEN 1 ELSE 0 END) as CorrectAnswers,
            CAST(SUM(CASE WHEN r.IsCorrect = 1 THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) as PerformanceScore
        FROM Responses r
        JOIN Items i ON r.ItemID = i.ItemID
        WHERE r.SessionID IN (
            SELECT SessionID
            FROM TestSessions
            WHERE StudentID = @StudentID
            AND SessionType = 'Placement'
        )
        AND i.IsPlacementItem = 1
        GROUP BY i.ModuleCategory
    )
    SELECT
        m.ModuleID,
        m.ModuleName,
        m.Description,
        m.IconName,
        m.ColorCode,
        ISNULL(ms.PerformanceScore, 0.0) as PerformanceScore,
        ISNULL(ms.TotalQuestions, 0) as QuestionsAnswered,
        ISNULL(ms.CorrectAnswers, 0) as CorrectAnswers,
        -- Lower score = higher priority (appears first)
        ROW_NUMBER() OVER (ORDER BY ISNULL(ms.PerformanceScore, 0.0) ASC) as PriorityOrder,
        -- Proficiency level
        CASE
            WHEN ms.PerformanceScore IS NULL THEN 'Not Assessed'
            WHEN ms.PerformanceScore < 0.50 THEN 'Needs Improvement'
            WHEN ms.PerformanceScore < 0.70 THEN 'Developing'
            WHEN ms.PerformanceScore < 0.85 THEN 'Proficient'
            ELSE 'Advanced'
        END as ProficiencyLevel
    FROM Modules m
    LEFT JOIN ModuleScores ms ON m.ModuleName = ms.ModuleCategory
    ORDER BY PriorityOrder;
END
GO

-- ============================================================================
-- PHASE 6: Update Placement Test Completion Procedure
-- ============================================================================

CREATE OR ALTER PROCEDURE SP_CompletePlacementTest
    @StudentID INT,
    @SessionID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Calculate overall ability from all 5 module categories
    DECLARE @OverallAbility FLOAT;
    DECLARE @TotalCorrect INT;
    DECLARE @TotalQuestions INT;

    SELECT
        @TotalCorrect = SUM(CASE WHEN r.IsCorrect = 1 THEN 1 ELSE 0 END),
        @TotalQuestions = COUNT(*)
    FROM Responses r
    JOIN Items i ON r.ItemID = i.ItemID
    WHERE r.SessionID = @SessionID
    AND i.IsPlacementItem = 1;

    -- Calculate ability using simple percentage for now
    -- (Can be replaced with full IRT estimation later)
    SET @OverallAbility = (CAST(@TotalCorrect AS FLOAT) / @TotalQuestions) * 2.0 - 1.0; -- Scale to -1 to 1

    -- Update student's initial and current ability
    UPDATE Students
    SET InitialAbility = @OverallAbility,
        CurrentAbility = @OverallAbility,
        LastLogin = GETDATE()
    WHERE StudentID = @StudentID;

    -- Mark session as completed
    UPDATE TestSessions
    SET IsCompleted = 1,
        EndTime = GETDATE(),
        FinalTheta = @OverallAbility,
        AccuracyPercentage = (CAST(@TotalCorrect AS FLOAT) / @TotalQuestions) * 100
    WHERE SessionID = @SessionID;

    -- Return results
    SELECT
        @StudentID as StudentID,
        @OverallAbility as OverallAbility,
        @TotalCorrect as CorrectAnswers,
        @TotalQuestions as TotalQuestions,
        (CAST(@TotalCorrect AS FLOAT) / @TotalQuestions) * 100 as PercentageScore,
        CASE
            WHEN @OverallAbility < -0.5 THEN 'Beginner'
            WHEN @OverallAbility > 0.5 THEN 'Advanced'
            ELSE 'Intermediate'
        END as OverallProficiency;
END
GO

-- ============================================================================
-- PHASE 7: Verification Queries
-- ============================================================================

-- Check all items inserted
SELECT
    ModuleCategory,
    COUNT(*) as ItemCount,
    AVG(DifficultyParam) as AvgDifficulty,
    MIN(DifficultyParam) as MinDifficulty,
    MAX(DifficultyParam) as MaxDifficulty
FROM Items
WHERE IsPlacementItem = 1
GROUP BY ModuleCategory
ORDER BY ModuleCategory;
GO

-- Check modules
SELECT * FROM Modules ORDER BY ModuleID;
GO

-- Sample module ordering query (for student who hasn't taken placement yet)
EXEC SP_GetModuleOrderByPlacementScore @StudentID = 1;
GO

PRINT '';
PRINT '✅ Placement test restructure complete!';
PRINT '';
PRINT 'Summary:';
PRINT '  - 5 modules created';
PRINT '  - 30 placement test items (6 per module)';
PRINT '  - Module ordering based on weakest performance';
PRINT '  - Stored procedures updated';
PRINT '';
PRINT 'Next steps:';
PRINT '1. Update Android app to use 5 module categories';
PRINT '2. Test placement test flow';
PRINT '3. Verify module ordering in dashboard';
GO
