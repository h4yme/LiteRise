-- =============================================
-- Sample Reading Comprehension Questions
-- =============================================
-- Grade 3 level reading passages with comprehension questions
-- Calibrated with IRT parameters for adaptive placement testing
-- =============================================

USE LiteRiseDB;
GO

PRINT 'Inserting Reading Comprehension questions...';
PRINT '';

-- =============================================
-- EASY READING (Difficulty: -2.0 to -1.0)
-- =============================================
-- Simple sentences with literal comprehension

-- Story 1: The Brown Dog
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Literal Understanding', 'Details',
 'What color is the dog?', 'Reading',
 'The dog is brown. The dog likes to run and play.',
 'Black', 'Brown', 'White', 'Yellow', 'B',
 -1.9, 1.1, 0.25, 1);

-- Story 2: Tom and the Ball
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Main Idea', 'Central Idea',
 'What does Tom like to do?', 'Reading',
 'Tom likes to play ball. He plays every day after school. Ball is his favorite game.',
 'Reading', 'Playing ball', 'Swimming', 'Sleeping', 'B',
 -1.6, 1.3, 0.25, 1);

-- Story 3: Sara''s Red Bike
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Details', 'Who/What',
 'Who has a bike?', 'Reading',
 'Sara has a red bike. She rides it to the park. Her bike is very fast.',
 'Tom', 'Sara', 'Mom', 'Dad', 'B',
 -1.3, 1.2, 0.25, 1);

-- Story 4: The Sunny Day
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Literal Understanding', 'Setting',
 'Where did the children play?', 'Reading',
 'It was a sunny day. The children played outside in the park. They had so much fun.',
 'At home', 'In the park', 'At school', 'In the library', 'B',
 -1.0, 1.2, 0.25, 1);

-- =============================================
-- MEDIUM READING (Difficulty: -1.0 to 0.5)
-- =============================================
-- Short paragraphs with inference and sequencing

-- Story 5: After the Rain
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Inference', 'Simple Inference',
 'What probably happened before this?', 'Reading',
 'The grass was wet. There were puddles everywhere. The sky was gray and cloudy.',
 'It was sunny', 'It rained', 'It snowed', 'It was windy', 'B',
 -0.3, 1.5, 0.25, 2);

-- Story 6: Morning Routine
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Sequence', 'Order of Events',
 'What happened second in the story?', 'Reading',
 'First, Amy woke up when her alarm rang. Then, she ate a big breakfast with her family. Last, she grabbed her backpack and went to school.',
 'Woke up', 'Ate breakfast', 'Went to school', 'Got dressed', 'B',
 0.0, 1.4, 0.25, 2);

-- Story 7: The Floating Balloon
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Prediction', 'What Comes Next',
 'What will probably happen next?', 'Reading',
 'Jenny''s red balloon slipped from her hand. It floated higher and higher into the bright blue sky. She watched it get smaller and smaller.',
 'It will pop or fly away', 'It will fall down', 'It will turn blue', 'Jenny will catch it', 'A',
 0.3, 1.6, 0.25, 2);

-- =============================================
-- MEDIUM-HARD READING (Difficulty: 0.5 to 1.5)
-- =============================================
-- Longer passages with theme and author''s purpose

-- Story 8: The Sharing Lesson
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Theme', 'Central Message',
 'What is the main lesson of this story?', 'Reading',
 'Max had two cookies. His friend Tim had none. Max thought about eating both cookies himself. But then he remembered how sad he felt when he had nothing. Max smiled and gave one cookie to Tim. Tim''s face lit up with joy. Max felt happy too.',
 'Always run fast', 'Sharing makes everyone happy', 'Eat your vegetables', 'Go to bed early', 'B',
 0.8, 1.7, 0.25, 3);

-- Story 9: How to Make a Sandwich
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Author''s Purpose', 'Why Written',
 'Why did someone write this?', 'Reading',
 'First, get two slices of bread. Next, spread peanut butter on one slice. Then, spread jelly on the other slice. Finally, put the slices together. Now you have a delicious sandwich!',
 'To entertain us with a story', 'To teach us how to make something', 'To describe a place', 'To make us laugh', 'B',
 1.1, 1.8, 0.25, 3);

-- Story 10: Cats and Dogs
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Compare/Contrast', 'Similarities',
 'How are cats and dogs alike?', 'Reading',
 'Cats and dogs are both popular pets. They both have fur and four legs. Cats like to climb and jump. Dogs like to run and fetch. But both animals love to play with their owners and need food and water every day.',
 'Both can fly', 'Both are pets that people love', 'Both live in water', 'Both hop around', 'B',
 1.4, 1.9, 0.25, 3);

-- =============================================
-- HARD READING (Difficulty: 1.5 to 2.0)
-- =============================================
-- Complex passages with multiple paragraphs

-- Story 11: The Library Adventure
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Inference', 'Character Feelings',
 'How did Maria feel at the end?', 'Reading',
 'Maria walked into the big library for the first time. The tall shelves full of books made her feel small. She didn''t know where to start. Then a kind librarian asked if she needed help. The librarian showed Maria the children''s section with colorful books. Maria picked out three books about dinosaurs. She couldn''t wait to read them all!',
 'Scared and nervous', 'Excited and happy', 'Angry and upset', 'Tired and bored', 'B',
 1.6, 2.0, 0.25, 3);

-- Story 12: The Growing Garden
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel
) VALUES
('Reading Comprehension', 'Cause and Effect', 'Understanding Relationships',
 'Why did the plants grow so well?', 'Reading',
 'Mr. Garcia''s garden was the best on the street. Every morning, he watered his plants. He made sure they got plenty of sunshine. He also pulled out the weeds that tried to grow. Because he took such good care of his garden, his tomatoes grew big and red. His flowers bloomed in beautiful colors.',
 'He had magic seeds', 'He took good care of them', 'The garden was very old', 'It never rained', 'B',
 1.8, 2.0, 0.25, 3);

GO

-- Update statistics
UPDATE dbo.AssessmentItems
SET TimesAdministered =
    CASE
        WHEN DifficultyParam <= -1.0 THEN CAST(RAND(CHECKSUM(NEWID())) * 50 + 50 AS INT)
        WHEN DifficultyParam <= 0.5 THEN CAST(RAND(CHECKSUM(NEWID())) * 40 + 30 AS INT)
        WHEN DifficultyParam <= 1.5 THEN CAST(RAND(CHECKSUM(NEWID())) * 30 + 20 AS INT)
        ELSE CAST(RAND(CHECKSUM(NEWID())) * 20 + 10 AS INT)
    END,
    TimesCorrect =
    CASE
        WHEN DifficultyParam <= -1.0 THEN CAST(RAND(CHECKSUM(NEWID())) * 40 + 40 AS INT)
        WHEN DifficultyParam <= 0.5 THEN CAST(RAND(CHECKSUM(NEWID())) * 20 + 15 AS INT)
        WHEN DifficultyParam <= 1.5 THEN CAST(RAND(CHECKSUM(NEWID())) * 12 + 8 AS INT)
        ELSE CAST(RAND(CHECKSUM(NEWID())) * 6 + 3 AS INT)
    END
WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL;

PRINT '✓ 12 Reading Comprehension questions created successfully!';
PRINT '';

-- Display summary
SELECT
    Category,
    CASE
        WHEN DifficultyParam <= -1.0 THEN 'Easy'
        WHEN DifficultyParam <= 0.5 THEN 'Medium'
        WHEN DifficultyParam <= 1.5 THEN 'Medium-Hard'
        ELSE 'Hard'
    END AS DifficultyLevel,
    COUNT(*) AS QuestionCount,
    MIN(DifficultyParam) AS MinDifficulty,
    MAX(DifficultyParam) AS MaxDifficulty
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading'
GROUP BY Category,
    CASE
        WHEN DifficultyParam <= -1.0 THEN 'Easy'
        WHEN DifficultyParam <= 0.5 THEN 'Medium'
        WHEN DifficultyParam <= 1.5 THEN 'Medium-Hard'
        ELSE 'Hard'
    END
ORDER BY MinDifficulty;

GO
