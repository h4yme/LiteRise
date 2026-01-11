-- =============================================
-- FIX: Reset Identity and Clean Data
-- =============================================
-- This ensures ItemID starts from 1 and all data is fresh
-- =============================================

USE LiteRiseDB;
GO

PRINT '========================================';
PRINT 'FIXING IDENTITY AND RESETTING DATA';
PRINT '========================================';
PRINT '';

-- Step 1: Check current state
PRINT 'Current state:';
SELECT
    'Total Items: ' + CAST(COUNT(*) AS VARCHAR) AS Info
FROM dbo.AssessmentItems;

SELECT
    'Item ID Range: ' + CAST(MIN(ItemID) AS VARCHAR) + ' to ' + CAST(MAX(ItemID) AS VARCHAR) AS Info
FROM dbo.AssessmentItems;
PRINT '';

-- Step 2: Delete ALL data properly (handles foreign keys)
PRINT 'Clearing all data...';

-- Delete student responses first
DELETE FROM dbo.StudentResponses;
PRINT '✓ Cleared StudentResponses';

-- Delete pronunciation scores
IF OBJECT_ID('dbo.PronunciationScores', 'U') IS NOT NULL
BEGIN
    DELETE FROM dbo.PronunciationScores;
    PRINT '✓ Cleared PronunciationScores';
END

-- Delete all assessment items
DELETE FROM dbo.AssessmentItems;
PRINT '✓ Cleared AssessmentItems';

-- Step 3: Reset IDENTITY to 1
DBCC CHECKIDENT ('dbo.AssessmentItems', RESEED, 0);
PRINT '✓ Reset IDENTITY counter to 0 (next insert will be 1)';
PRINT '';

-- Step 4: Verify ReadingPassage column exists
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'AssessmentItems'
    AND COLUMN_NAME = 'ReadingPassage'
)
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD ReadingPassage NVARCHAR(MAX) NULL;
    PRINT '✓ Added ReadingPassage column';
END
PRINT '';

-- Step 5: Insert fresh Grade 3 data
PRINT 'Inserting 40 Grade 3 questions...';
PRINT '';

-- CATEGORY 1: Pronunciation (10 items)
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea, QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips,
    EstimatedTime, IsActive
) VALUES
('Oral Language', 'Pronunciation', 'Basic', 'Say the word: CAT', 'Pronunciation', NULL, NULL, NULL, NULL, 'cat', -1.5, 1.2, 0.0, 3, 'cat', '/kæt/', 65, 'Say "k" then "at"', 20, 1),
('Oral Language', 'Pronunciation', 'Basic', 'Say the word: DOG', 'Pronunciation', NULL, NULL, NULL, NULL, 'dog', -1.2, 1.2, 0.0, 3, 'dog', '/dɔɡ/', 65, 'Say "d" then "og"', 20, 1),
('Oral Language', 'Pronunciation', 'Basic', 'Say the word: SUN', 'Pronunciation', NULL, NULL, NULL, NULL, 'sun', -0.9, 1.2, 0.0, 3, 'sun', '/sʌn/', 65, 'Say "s" then "un"', 20, 1),
('Oral Language', 'Pronunciation', 'Blends', 'Say the word: STOP', 'Pronunciation', NULL, NULL, NULL, NULL, 'stop', -0.5, 1.3, 0.0, 3, 'stop', '/stɑp/', 70, 'Blend s-t', 25, 1),
('Oral Language', 'Pronunciation', 'Vowels', 'Say the word: MAKE', 'Pronunciation', NULL, NULL, NULL, NULL, 'make', 0.0, 1.4, 0.0, 3, 'make', '/meɪk/', 70, 'Long a', 25, 1),
('Oral Language', 'Pronunciation', 'Silent', 'Say the word: KNIFE', 'Pronunciation', NULL, NULL, NULL, NULL, 'knife', 0.5, 1.5, 0.0, 3, 'knife', '/naɪf/', 75, 'Silent k', 30, 1),
('Oral Language', 'Pronunciation', 'Syllables', 'Say the word: HAPPY', 'Pronunciation', NULL, NULL, NULL, NULL, 'happy', 1.0, 1.5, 0.0, 3, 'happy', '/ˈhæp.i/', 75, 'HAP-py', 30, 1),
('Oral Language', 'Pronunciation', 'Syllables', 'Say the word: TIGER', 'Pronunciation', NULL, NULL, NULL, NULL, 'tiger', 1.3, 1.5, 0.0, 3, 'tiger', '/ˈtaɪ.ɡər/', 75, 'TI-ger', 30, 1),
('Oral Language', 'Pronunciation', 'Syllables', 'Say the word: ELEPHANT', 'Pronunciation', NULL, NULL, NULL, NULL, 'elephant', 1.6, 1.6, 0.0, 3, 'elephant', '/ˈɛl.ɪ.fənt/', 78, 'EL-e-phant', 35, 1),
('Oral Language', 'Pronunciation', 'Silent', 'Say the word: WRITE', 'Pronunciation', NULL, NULL, NULL, NULL, 'write', 2.0, 1.7, 0.0, 3, 'write', '/raɪt/', 80, 'Silent w', 35, 1);

-- CATEGORY 2: Word Knowledge (10 items)
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea, QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
('Word Knowledge', 'Vocabulary', 'Synonyms', 'Which word means HAPPY?', 'MultipleChoice', 'Sad', 'Joyful', 'Angry', 'Tired', 'B', -1.5, 1.2, 0.25, 3, 25, 1),
('Word Knowledge', 'Vocabulary', 'Antonyms', 'Opposite of HOT?', 'MultipleChoice', 'Warm', 'Cold', 'Big', 'Fast', 'B', -1.0, 1.3, 0.25, 3, 25, 1),
('Word Knowledge', 'Phonics', 'Sounds', 'Same sound as CAT?', 'MultipleChoice', 'Dog', 'Cup', 'Ball', 'Sun', 'B', -0.5, 1.3, 0.25, 3, 25, 1),
('Word Knowledge', 'Phonics', 'Rhyme', 'Rhymes with CAKE?', 'MultipleChoice', 'Ball', 'Make', 'Cup', 'Dog', 'B', 0.0, 1.4, 0.25, 3, 25, 1),
('Word Knowledge', 'Sight Words', 'Complete', 'I ___ a dog.', 'MultipleChoice', 'am', 'have', 'is', 'are', 'B', 0.3, 1.3, 0.25, 3, 25, 1),
('Word Knowledge', 'Syllables', 'Count', 'Syllables in ELEPHANT?', 'MultipleChoice', 'Two', 'Three', 'Four', 'Five', 'B', 0.8, 1.5, 0.25, 3, 30, 1),
('Word Knowledge', 'Prefixes', 'Meaning', 'UN-happy means:', 'MultipleChoice', 'Very happy', 'Not happy', 'A little happy', 'Always happy', 'B', 1.2, 1.6, 0.25, 3, 35, 1),
('Word Knowledge', 'Suffixes', 'Meaning', 'Who TEACHES:', 'MultipleChoice', 'Teached', 'Teacher', 'Teaching', 'Teaches', 'B', 1.5, 1.6, 0.25, 3, 35, 1),
('Word Knowledge', 'Compound', 'Combine', 'RAIN + BOW:', 'MultipleChoice', 'Raincoat', 'Rainbow', 'Raindrop', 'Rainfall', 'B', 1.8, 1.7, 0.25, 3, 35, 1),
('Word Knowledge', 'Context', 'Meaning', 'Huge means:', 'MultipleChoice', 'Tiny', 'Enormous', 'Small', 'Little', 'B', 2.0, 1.8, 0.25, 3, 40, 1);

-- CATEGORY 3: Reading Comprehension (10 items WITH PASSAGES)
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea, QuestionText, QuestionType, ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
('Reading Comprehension', 'Details', 'Color', 'What color is the dog?', 'Reading', 'The dog is brown. The dog likes to run and play in the park.', 'Black', 'Brown', 'White', 'Yellow', 'B', -1.5, 1.1, 0.25, 3, 60, 1),
('Reading Comprehension', 'Main Idea', 'Activity', 'What does Tom like?', 'Reading', 'Tom likes to play ball. He plays every day after school. Ball is his favorite game.', 'Reading', 'Playing ball', 'Swimming', 'Sleeping', 'B', -1.0, 1.3, 0.25, 3, 60, 1),
('Reading Comprehension', 'Setting', 'Where', 'Where did they play?', 'Reading', 'It was a sunny day. The children played outside in the park. They had so much fun together.', 'At home', 'In the park', 'At school', 'Library', 'B', -0.5, 1.2, 0.25, 3, 60, 1),
('Reading Comprehension', 'Inference', 'Weather', 'What happened before?', 'Reading', 'The grass was wet. There were puddles everywhere. The sky was gray and cloudy.', 'It was sunny', 'It rained', 'It snowed', 'It was windy', 'B', 0.0, 1.5, 0.25, 3, 75, 1),
('Reading Comprehension', 'Sequence', 'Order', 'What happened second?', 'Reading', 'First, Amy woke up. Then, she ate breakfast with her family. Last, she went to school.', 'Woke up', 'Ate breakfast', 'Went to school', 'Got dressed', 'B', 0.5, 1.4, 0.25, 3, 75, 1),
('Reading Comprehension', 'Prediction', 'Next', 'What will happen next?', 'Reading', 'Jenny''s red balloon slipped from her hand. It floated higher and higher into the sky.', 'It will fly away', 'It will fall down', 'It will turn blue', 'She will catch it', 'A', 1.0, 1.6, 0.25, 3, 75, 1),
('Reading Comprehension', 'Theme', 'Lesson', 'Main lesson?', 'Reading', 'Max had two cookies. His friend Tim had none. Max gave one cookie to Tim. Both felt happy.', 'Run fast', 'Sharing is nice', 'Eat vegetables', 'Sleep early', 'B', 1.3, 1.7, 0.25, 3, 90, 1),
('Reading Comprehension', 'Purpose', 'Why', 'Why was this written?', 'Reading', 'First, get bread. Next, spread peanut butter. Then, add jelly. Put them together.', 'Entertain', 'Teach how to make', 'Describe', 'Make laugh', 'B', 1.6, 1.8, 0.25, 3, 90, 1),
('Reading Comprehension', 'Compare', 'Alike', 'How are they alike?', 'Reading', 'Cats and dogs are both pets. They both have fur and four legs. Both need food and water.', 'Both fly', 'Both are pets', 'Both swim', 'Both hop', 'B', 1.8, 1.9, 0.25, 3, 90, 1),
('Reading Comprehension', 'Feelings', 'Emotion', 'How did Maria feel?', 'Reading', 'Maria walked into the library. A librarian helped her find dinosaur books. Maria was excited!', 'Scared', 'Excited', 'Angry', 'Tired', 'B', 2.0, 2.0, 0.25, 3, 120, 1);

-- CATEGORY 4: Language Structure (10 items)
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea, QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
('Language Structure', 'Grammar', 'Nouns', 'Which is a NOUN?', 'MultipleChoice', 'Run', 'Happy', 'Dog', 'Quickly', 'C', -1.5, 1.2, 0.25, 3, 25, 1),
('Language Structure', 'Grammar', 'Verbs', 'Which is a VERB?', 'MultipleChoice', 'Book', 'Jump', 'Red', 'Big', 'B', -1.0, 1.3, 0.25, 3, 25, 1),
('Language Structure', 'Adjectives', 'Describe', 'The ___ cat', 'MultipleChoice', 'Run', 'Sleep', 'Fluffy', 'Jump', 'C', -0.5, 1.3, 0.25, 3, 30, 1),
('Language Structure', 'Sentences', 'Complete', 'Complete sentence?', 'MultipleChoice', 'The dog', 'Ran fast', 'The dog ran.', 'Fast dog', 'C', 0.0, 1.4, 0.25, 3, 30, 1),
('Language Structure', 'Punctuation', 'Question', 'Where is my book', 'MultipleChoice', '.', '?', '!', ',', 'B', 0.5, 1.4, 0.25, 3, 25, 1),
('Language Structure', 'Capitals', 'Names', 'Needs capital?', 'MultipleChoice', 'the', 'dog', 'sarah', 'is', 'C', 0.8, 1.5, 0.25, 3, 30, 1),
('Language Structure', 'Agreement', 'Verb', 'The dog ___ fast.', 'MultipleChoice', 'run', 'runs', 'running', 'ran', 'B', 1.2, 1.5, 0.25, 3, 30, 1),
('Language Structure', 'Pronouns', 'Replace', 'Sara is nice. ___ helps.', 'MultipleChoice', 'He', 'She', 'It', 'They', 'B', 1.5, 1.7, 0.25, 3, 30, 1),
('Language Structure', 'Tenses', 'Past', 'Yesterday, I ___ there.', 'MultipleChoice', 'go', 'goes', 'went', 'going', 'C', 1.8, 1.6, 0.25, 3, 30, 1),
('Language Structure', 'Contractions', 'Meaning', 'don''t means:', 'MultipleChoice', 'do not', 'did not', 'does not', 'will not', 'A', 2.0, 1.8, 0.25, 3, 35, 1);

PRINT '✓ Inserted 40 items';
PRINT '';

-- Verify
PRINT '========================================';
PRINT 'VERIFICATION';
PRINT '========================================';
PRINT '';

SELECT 'Total Items: ' + CAST(COUNT(*) AS VARCHAR) AS Result FROM dbo.AssessmentItems;
SELECT 'Item ID Range: ' + CAST(MIN(ItemID) AS VARCHAR) + ' to ' + CAST(MAX(ItemID) AS VARCHAR) AS Result FROM dbo.AssessmentItems;

DECLARE @ReadingNull INT;
SELECT @ReadingNull = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading' AND (ReadingPassage IS NULL OR ReadingPassage = '');

IF @ReadingNull = 0
    PRINT '✓ All 10 reading items have passages!';
ELSE
    PRINT '❌ ' + CAST(@ReadingNull AS VARCHAR) + ' reading items missing passages!';

PRINT '';
PRINT '========================================';
PRINT 'COMPLETE!';
PRINT '========================================';
PRINT 'Item IDs now start from 1';
PRINT 'All reading items have passages';
PRINT 'Ready for testing!';
PRINT '';

GO
