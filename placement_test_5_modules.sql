-- ============================================================================
-- LiteRise Placement Test Restructure - 5 Module Categories
-- ============================================================================
-- Changes:
-- 1. Update placement test from 4 categories to 5 module-based categories
-- 2. Map ItemType values to 5 modules
-- 3. Update SP_GetPreAssessmentItems to return 30 items (6 per module)
-- 4. Clear old placement items and insert new categorized items
-- 5. Add module ordering stored procedure based on weakest performance
-- ============================================================================

USE LiteRiseDB;
GO

-- ============================================================================
-- PHASE 1: Update Modules Table with All 5 Modules
-- ============================================================================

-- Clear existing modules if needed (safe for development, review for production)
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

PRINT '5 Modules created successfully.';
GO

-- ============================================================================
-- PHASE 2: Clear Old Placement Test Items
-- ============================================================================
-- NOTE: We'll mark old items as inactive instead of deleting to preserve data integrity

UPDATE Items
SET IsActive = 0
WHERE GradeLevel = 3
  AND DifficultyParam BETWEEN -2.0 AND 2.0;
GO

PRINT 'Old placement test items marked as inactive.';
GO

-- ============================================================================
-- PHASE 3: Insert New Placement Test Items (5 Modules x 6 Items = 30 Items)
-- ============================================================================
-- ItemType mapping to Modules:
-- Module 1: Phonics, Phonological, Word Recognition
-- Module 2: Vocabulary, Word Meaning
-- Module 3: Grammar, Syntax
-- Module 4: Reading Comprehension, Inference
-- Module 5: Writing, Composition
-- ============================================================================

-- ============================================================================
-- MODULE 1: Phonics and Word Study (Items 1-6)
-- ItemType: Phonics, Phonological
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, Phonetic, CreatedAt)
VALUES
-- Item 1: Easy - Beginning sounds
('Which word starts with the same sound as "cat"?',
 'Phonics', 'Easy', -1.5, 1.2, 0.25,
 'can', '["can", "dog", "sun", "pen"]',
 3, 1, '/k/', GETDATE()),

-- Item 2: Easy - Rhyming words
('Which word rhymes with "bat"?',
 'Phonological', 'Easy', -1.2, 1.3, 0.25,
 'hat', '["hat", "ball", "car", "dog"]',
 3, 1, NULL, GETDATE()),

-- Item 3: Medium - Vowel sounds
('Which word has the long "a" sound?',
 'Phonics', 'Medium', -0.3, 1.4, 0.25,
 'cake', '["cake", "cat", "cap", "can"]',
 3, 1, '/eɪ/', GETDATE()),

-- Item 4: Medium - Consonant blends
('Which word starts with a consonant blend?',
 'Phonics', 'Medium', 0.1, 1.3, 0.25,
 'blue', '["blue", "dog", "cat", "hat"]',
 3, 1, '/bl/', GETDATE()),

-- Item 5: Hard - Silent letters
('Which word has a silent letter?',
 'Phonics', 'Hard', 1.0, 1.5, 0.25,
 'knife', '["knife", "cake", "jump", "flag"]',
 3, 1, '/naɪf/', GETDATE()),

-- Item 6: Hard - Syllable counting
('How many syllables are in "butterfly"?',
 'Phonological', 'Hard', 1.3, 1.4, 0.25,
 'three', '["two", "three", "four", "five"]',
 3, 1, NULL, GETDATE());
GO

PRINT 'Module 1: Phonics and Word Study items inserted (6 items).';
GO

-- ============================================================================
-- MODULE 2: Vocabulary and Word Knowledge (Items 7-12)
-- ItemType: Vocabulary
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, Definition, CreatedAt)
VALUES
-- Item 7: Easy - Basic synonyms
('Which word means the same as "happy"?',
 'Vocabulary', 'Easy', -1.4, 1.2, 0.25,
 'glad', '["glad", "sad", "angry", "tired"]',
 3, 1, 'feeling pleasure or contentment', GETDATE()),

-- Item 8: Easy - Common words
('What does "enormous" mean?',
 'Vocabulary', 'Easy', -1.0, 1.3, 0.25,
 'very big', '["very big", "very small", "very fast", "very slow"]',
 3, 1, 'extremely large in size', GETDATE()),

-- Item 9: Medium - Context clues
('The desert was so arid that no plants could grow. What does "arid" mean?',
 'Vocabulary', 'Medium', 0.0, 1.4, 0.25,
 'dry', '["dry", "wet", "cold", "hot"]',
 3, 1, 'having little or no rain; very dry', GETDATE()),

-- Item 10: Medium - Antonyms
('Which word is the opposite of "rough"?',
 'Vocabulary', 'Medium', 0.3, 1.3, 0.25,
 'smooth', '["smooth", "bumpy", "hard", "soft"]',
 3, 1, 'having an even surface', GETDATE()),

-- Item 11: Hard - Multiple meanings
('She will present the present to her mom. What does the second "present" mean?',
 'Vocabulary', 'Hard', 0.9, 1.5, 0.25,
 'gift', '["gift", "show", "give", "time"]',
 3, 1, 'something given; a gift', GETDATE()),

-- Item 12: Hard - Academic vocabulary
('What does "analyze" mean?',
 'Vocabulary', 'Hard', 1.2, 1.4, 0.25,
 'examine carefully', '["examine carefully", "read quickly", "write down", "count numbers"]',
 3, 1, 'to study something closely', GETDATE());
GO

PRINT 'Module 2: Vocabulary and Word Knowledge items inserted (6 items).';
GO

-- ============================================================================
-- MODULE 3: Grammar Awareness and Grammatical Structures (Items 13-18)
-- ItemType: Grammar, Syntax
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, CreatedAt)
VALUES
-- Item 13: Easy - Basic punctuation
('Which sentence uses correct punctuation?',
 'Grammar', 'Easy', -1.3, 1.2, 0.25,
 'I like pizza.', '["I like pizza.", "I like pizza", "i like pizza.", "I Like Pizza."]',
 3, 1, GETDATE()),

-- Item 14: Easy - Sentence scramble (Syntax)
('the / cat / is / sleeping',
 'Syntax', 'Easy', -1.1, 1.3, 0.25,
 'The cat is sleeping.', NULL,
 3, 1, GETDATE()),

-- Item 15: Medium - Subject-verb agreement
('Which sentence is correct?',
 'Grammar', 'Medium', 0.2, 1.4, 0.25,
 'The dogs are running.', '["The dogs are running.", "The dogs is running.", "The dog are running.", "The dogs am running."]',
 3, 1, GETDATE()),

-- Item 16: Medium - Parts of speech
('In the sentence "The quick brown fox jumps," what is "quick"?',
 'Grammar', 'Medium', 0.4, 1.3, 0.25,
 'adjective', '["adjective", "noun", "verb", "adverb"]',
 3, 1, GETDATE()),

-- Item 17: Hard - Complex sentence structure
('Which sentence is a compound sentence?',
 'Grammar', 'Hard', 1.1, 1.5, 0.25,
 'I went to the store, and I bought milk.', '["I went to the store, and I bought milk.", "I went to the store.", "When I went to the store.", "Going to the store."]',
 3, 1, GETDATE()),

-- Item 18: Hard - Advanced syntax
('yesterday / the / park / we / to / went',
 'Syntax', 'Hard', 1.4, 1.4, 0.25,
 'Yesterday we went to the park.', NULL,
 3, 1, GETDATE());
GO

PRINT 'Module 3: Grammar Awareness items inserted (6 items).';
GO

-- ============================================================================
-- MODULE 4: Comprehending and Analyzing Text (Items 19-24)
-- ItemType: Reading Comprehension
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, CreatedAt)
VALUES
-- Item 19: Easy - Literal comprehension
('The sun was shining brightly in the sky. What was the weather like?',
 'Reading Comprehension', 'Easy', -1.4, 1.2, 0.25,
 'sunny', '["sunny", "rainy", "cloudy", "snowy"]',
 3, 1, GETDATE()),

-- Item 20: Easy - Main idea
('Tom loves to read books. He reads every night before bed. What is Tom''s hobby?',
 'Reading Comprehension', 'Easy', -1.0, 1.3, 0.25,
 'reading', '["reading", "sleeping", "eating", "running"]',
 3, 1, GETDATE()),

-- Item 21: Medium - Making inferences
('Sarah put on her coat and grabbed her umbrella before leaving the house. What can you infer?',
 'Reading Comprehension', 'Medium', 0.1, 1.4, 0.25,
 'It might rain.', '["It might rain.", "It is sunny.", "She is going to bed.", "She forgot something."]',
 3, 1, GETDATE()),

-- Item 22: Medium - Cause and effect
('Because it was raining, the game was canceled. Why was the game canceled?',
 'Reading Comprehension', 'Medium', 0.5, 1.3, 0.25,
 'because of rain', '["because of rain", "because of sun", "no one came", "the field was dirty"]',
 3, 1, GETDATE()),

-- Item 23: Hard - Drawing conclusions
('The flowers were wilting, and the soil was dry and cracked. What should be done?',
 'Reading Comprehension', 'Hard', 1.0, 1.5, 0.25,
 'water the plants', '["water the plants", "pick the flowers", "add more soil", "move them to shade"]',
 3, 1, GETDATE()),

-- Item 24: Hard - Analyzing author''s purpose
('A poster says "Join the library today! Read hundreds of books for free!" What is the purpose?',
 'Reading Comprehension', 'Hard', 1.3, 1.4, 0.25,
 'to persuade people to join', '["to persuade people to join", "to inform about book prices", "to entertain readers", "to describe the library"]',
 3, 1, GETDATE());
GO

PRINT 'Module 4: Comprehending and Analyzing Text items inserted (6 items).';
GO

-- ============================================================================
-- MODULE 5: Creating and Composing Text (Items 25-30)
-- ItemType: Writing
-- ============================================================================

INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam,
                   CorrectAnswer, AnswerChoices, GradeLevel, IsActive, CreatedAt)
VALUES
-- Item 25: Easy - Complete the sentence
('Complete the sentence: I went to the park ____ I saw my friend.',
 'Writing', 'Easy', -1.2, 1.2, 0.25,
 'and', '["and", "but", "or", "because"]',
 3, 1, GETDATE()),

-- Item 26: Easy - Capitalize correctly
('Which sentence has correct capitalization?',
 'Writing', 'Easy', -0.9, 1.3, 0.25,
 'My name is Maria.', '["My name is Maria.", "my name is maria.", "My Name Is Maria.", "my name is Maria."]',
 3, 1, GETDATE()),

-- Item 27: Medium - Best opening sentence
('You are writing a story about a dog. Which is the best opening sentence?',
 'Writing', 'Medium', 0.2, 1.4, 0.25,
 'Max was a brave little dog who loved adventures.', '["Max was a brave little dog who loved adventures.", "This is about a dog.", "Dogs are nice.", "I have a dog."]',
 3, 1, GETDATE()),

-- Item 28: Medium - Adding details
('Which sentence has more details? "The cat sat." or:',
 'Writing', 'Medium', 0.4, 1.3, 0.25,
 'The fluffy orange cat sat on the soft pillow.', '["The fluffy orange cat sat on the soft pillow.", "The cat sat there.", "A cat sat.", "The cat was sitting."]',
 3, 1, GETDATE()),

-- Item 29: Hard - Organizing ideas
('You want to write about your weekend. What should you write about FIRST?',
 'Writing', 'Hard', 1.0, 1.5, 0.25,
 'What you did on Saturday morning', '["What you did on Saturday morning", "What you learned from your weekend", "What you did on Sunday night", "How you felt about Monday"]',
 3, 1, GETDATE()),

-- Item 30: Hard - Revision/editing
('Which sentence needs to be fixed? "The boy run to the store yesterday."',
 'Writing', 'Hard', 1.2, 1.4, 0.25,
 'Change "run" to "ran"', '["Change \"run\" to \"ran\"", "Change \"boy\" to \"boys\"", "Change \"yesterday\" to \"tomorrow\"", "The sentence is correct"]',
 3, 1, GETDATE());
GO

PRINT 'Module 5: Creating and Composing Text items inserted (6 items).';
GO

PRINT '======================================';
PRINT 'All 30 placement test items inserted!';
PRINT '======================================';
GO

-- ============================================================================
-- PHASE 4: Update SP_GetPreAssessmentItems to Return Items by Module Category
-- ============================================================================

-- Drop existing procedure
DROP PROCEDURE IF EXISTS SP_GetPreAssessmentItems;
GO

CREATE PROCEDURE SP_GetPreAssessmentItems
    @StudentID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Return 30 items (6 per module) grouped by ItemType/Module
    -- Module order: Phonics → Vocabulary → Grammar → Comprehension → Writing

    SELECT * FROM (
        -- Module 1: Phonics and Word Study (6 items)
        SELECT TOP 6 *,
               'Phonics and Word Study' as Category,
               1 as ModuleID,
               1 as CategoryOrder
        FROM Items
        WHERE ItemType IN ('Phonics', 'Phonological')
          AND IsActive = 1
          AND GradeLevel = 3
        ORDER BY DifficultyParam ASC

        UNION ALL

        -- Module 2: Vocabulary and Word Knowledge (6 items)
        SELECT TOP 6 *,
               'Vocabulary and Word Knowledge' as Category,
               2 as ModuleID,
               2 as CategoryOrder
        FROM Items
        WHERE ItemType IN ('Vocabulary')
          AND IsActive = 1
          AND GradeLevel = 3
        ORDER BY DifficultyParam ASC

        UNION ALL

        -- Module 3: Grammar Awareness (6 items)
        SELECT TOP 6 *,
               'Grammar Awareness and Grammatical Structures' as Category,
               3 as ModuleID,
               3 as CategoryOrder
        FROM Items
        WHERE ItemType IN ('Grammar', 'Syntax')
          AND IsActive = 1
          AND GradeLevel = 3
        ORDER BY DifficultyParam ASC

        UNION ALL

        -- Module 4: Comprehending and Analyzing Text (6 items)
        SELECT TOP 6 *,
               'Comprehending and Analyzing Text' as Category,
               4 as ModuleID,
               4 as CategoryOrder
        FROM Items
        WHERE ItemType IN ('Reading Comprehension')
          AND IsActive = 1
          AND GradeLevel = 3
        ORDER BY DifficultyParam ASC

        UNION ALL

        -- Module 5: Creating and Composing Text (6 items)
        SELECT TOP 6 *,
               'Creating and Composing Text' as Category,
               5 as ModuleID,
               5 as CategoryOrder
        FROM Items
        WHERE ItemType IN ('Writing')
          AND IsActive = 1
          AND GradeLevel = 3
        ORDER BY DifficultyParam ASC
    ) AS CategorizedItems
    ORDER BY CategoryOrder, DifficultyParam;
END
GO

PRINT 'SP_GetPreAssessmentItems updated to return 30 items across 5 modules.';
GO

-- ============================================================================
-- PHASE 5: Create Stored Procedure for Module Ordering Based on Performance
-- ============================================================================

-- This SP returns modules ordered by weakest performance first
DROP PROCEDURE IF EXISTS SP_GetModuleOrderByPlacementScore;
GO

CREATE PROCEDURE SP_GetModuleOrderByPlacementScore
    @StudentID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Get the most recent PreAssessment session for this student
    DECLARE @SessionID INT;

    SELECT TOP 1 @SessionID = SessionID
    FROM Sessions
    WHERE StudentID = @StudentID
      AND SessionType = 'PreAssessment'
    ORDER BY SessionDate DESC;

    -- If no session found, return default order
    IF @SessionID IS NULL
    BEGIN
        SELECT
            ModuleID,
            ModuleName,
            Description,
            1 as PriorityOrder,
            0.0 as PerformanceScore,
            IconName,
            ColorCode
        FROM Modules
        WHERE ModuleID BETWEEN 1 AND 5
        ORDER BY ModuleID;
        RETURN;
    END

    -- Calculate performance score per module category based on responses
    WITH ModulePerformance AS (
        SELECT
            CASE
                WHEN i.ItemType IN ('Phonics', 'Phonological') THEN 1
                WHEN i.ItemType IN ('Vocabulary') THEN 2
                WHEN i.ItemType IN ('Grammar', 'Syntax') THEN 3
                WHEN i.ItemType IN ('Reading Comprehension') THEN 4
                WHEN i.ItemType IN ('Writing') THEN 5
                ELSE 1
            END as ModuleID,
            COUNT(*) as TotalItems,
            SUM(CASE WHEN r.IsCorrect = 1 THEN 1 ELSE 0 END) as CorrectItems,
            CAST(SUM(CASE WHEN r.IsCorrect = 1 THEN 1 ELSE 0 END) AS FLOAT) / COUNT(*) as PerformanceScore
        FROM Responses r
        INNER JOIN Items i ON r.ItemID = i.ItemID
        WHERE r.SessionID = @SessionID
          AND i.ItemType IN ('Phonics', 'Phonological', 'Vocabulary', 'Grammar', 'Syntax', 'Reading Comprehension', 'Writing')
        GROUP BY
            CASE
                WHEN i.ItemType IN ('Phonics', 'Phonological') THEN 1
                WHEN i.ItemType IN ('Vocabulary') THEN 2
                WHEN i.ItemType IN ('Grammar', 'Syntax') THEN 3
                WHEN i.ItemType IN ('Reading Comprehension') THEN 4
                WHEN i.ItemType IN ('Writing') THEN 5
                ELSE 1
            END
    )
    SELECT
        m.ModuleID,
        m.ModuleName,
        m.Description,
        ROW_NUMBER() OVER (ORDER BY ISNULL(mp.PerformanceScore, 0) ASC) as PriorityOrder,
        ISNULL(mp.PerformanceScore, 0) as PerformanceScore,
        ISNULL(mp.TotalItems, 0) as ItemsAnswered,
        ISNULL(mp.CorrectItems, 0) as ItemsCorrect,
        m.IconName,
        m.ColorCode
    FROM Modules m
    LEFT JOIN ModulePerformance mp ON m.ModuleID = mp.ModuleID
    WHERE m.ModuleID BETWEEN 1 AND 5
    ORDER BY PriorityOrder; -- Weakest performance appears first
END
GO

PRINT 'SP_GetModuleOrderByPlacementScore created successfully.';
GO

PRINT '';
PRINT '============================================================================';
PRINT 'Placement Test Restructure Complete!';
PRINT '============================================================================';
PRINT '- 5 Modules created';
PRINT '- 30 placement test items inserted (6 per module)';
PRINT '- SP_GetPreAssessmentItems updated to return categorized items';
PRINT '- SP_GetModuleOrderByPlacementScore created for adaptive ordering';
PRINT '============================================================================';
GO
