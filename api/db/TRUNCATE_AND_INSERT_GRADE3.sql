-- =============================================
-- TRUNCATE AND INSERT GRADE 3 QUESTIONS ONLY
-- =============================================
-- Simple reset: Clear everything, add only what's needed
-- =============================================

USE LiteRiseDB;
GO

PRINT '========================================';
PRINT 'TRUNCATE AND INSERT GRADE 3 QUESTIONS';
PRINT '========================================';
PRINT '';

-- =============================================
-- STEP 1: Add ReadingPassage column if missing
-- =============================================
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'AssessmentItems'
    AND COLUMN_NAME = 'ReadingPassage'
)
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD ReadingPassage NVARCHAR(MAX) NULL;
    PRINT '✓ ReadingPassage column added';
END
PRINT '';

-- =============================================
-- STEP 2: Clear all data (handle foreign keys)
-- =============================================
PRINT 'Clearing all data...';

-- Delete student responses first (foreign key)
DELETE FROM dbo.StudentResponses;
PRINT '✓ Cleared StudentResponses';

-- Delete pronunciation scores (foreign key)
IF OBJECT_ID('dbo.PronunciationScores', 'U') IS NOT NULL
BEGIN
    DELETE FROM dbo.PronunciationScores;
    PRINT '✓ Cleared PronunciationScores';
END

-- Truncate assessment items
TRUNCATE TABLE dbo.AssessmentItems;
PRINT '✓ Truncated AssessmentItems';
PRINT '';

-- =============================================
-- STEP 3: Insert Grade 3 Questions Only
-- =============================================

-- CATEGORY 1: ORAL LANGUAGE (10 Pronunciation Items)
PRINT 'Inserting Oral Language (Pronunciation) - 10 items...';

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips,
    EstimatedTime, IsActive
) VALUES
('Oral Language', 'Pronunciation', 'Basic Words', 'Say the word: CAT', 'Pronunciation', NULL, NULL, NULL, NULL, 'cat', -1.5, 1.2, 0.0, 3, 'cat', '/kæt/', 65, 'Start with "k", then say "at"', 20, 1),
('Oral Language', 'Pronunciation', 'Basic Words', 'Say the word: DOG', 'Pronunciation', NULL, NULL, NULL, NULL, 'dog', -1.2, 1.2, 0.0, 3, 'dog', '/dɔɡ/', 65, 'Say "d" then "og"', 20, 1),
('Oral Language', 'Pronunciation', 'Basic Words', 'Say the word: SUN', 'Pronunciation', NULL, NULL, NULL, NULL, 'sun', -0.9, 1.2, 0.0, 3, 'sun', '/sʌn/', 65, 'Say "s" then "un"', 20, 1),
('Oral Language', 'Pronunciation', 'Blends', 'Say the word: STOP', 'Pronunciation', NULL, NULL, NULL, NULL, 'stop', -0.5, 1.3, 0.0, 3, 'stop', '/stɑp/', 70, 'Blend "s" and "t"', 25, 1),
('Oral Language', 'Pronunciation', 'Long Vowels', 'Say the word: MAKE', 'Pronunciation', NULL, NULL, NULL, NULL, 'make', 0.0, 1.4, 0.0, 3, 'make', '/meɪk/', 70, 'Long "a" sound', 25, 1),
('Oral Language', 'Pronunciation', 'Silent Letters', 'Say the word: KNIFE', 'Pronunciation', NULL, NULL, NULL, NULL, 'knife', 0.5, 1.5, 0.0, 3, 'knife', '/naɪf/', 75, 'Silent "k"!', 30, 1),
('Oral Language', 'Pronunciation', 'Two Syllables', 'Say the word: HAPPY', 'Pronunciation', NULL, NULL, NULL, NULL, 'happy', 1.0, 1.5, 0.0, 3, 'happy', '/ˈhæp.i/', 75, 'HAP-py', 30, 1),
('Oral Language', 'Pronunciation', 'Two Syllables', 'Say the word: TIGER', 'Pronunciation', NULL, NULL, NULL, NULL, 'tiger', 1.3, 1.5, 0.0, 3, 'tiger', '/ˈtaɪ.ɡər/', 75, 'TI-ger', 30, 1),
('Oral Language', 'Pronunciation', 'Three Syllables', 'Say the word: ELEPHANT', 'Pronunciation', NULL, NULL, NULL, NULL, 'elephant', 1.6, 1.6, 0.0, 3, 'elephant', '/ˈɛl.ɪ.fənt/', 78, 'EL-e-phant', 35, 1),
('Oral Language', 'Pronunciation', 'Tricky Words', 'Say the word: WRITE', 'Pronunciation', NULL, NULL, NULL, NULL, 'write', 2.0, 1.7, 0.0, 3, 'write', '/raɪt/', 80, 'Silent "w"!', 35, 1);

PRINT '✓ 10 pronunciation items';
PRINT '';

-- CATEGORY 2: WORD KNOWLEDGE (10 Items)
PRINT 'Inserting Word Knowledge - 10 items...';

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
('Word Knowledge', 'Vocabulary', 'Synonyms', 'Which word means the same as HAPPY?', 'MultipleChoice', 'Sad', 'Joyful', 'Angry', 'Tired', 'B', -1.5, 1.2, 0.25, 3, 25, 1),
('Word Knowledge', 'Vocabulary', 'Antonyms', 'Which word means the opposite of HOT?', 'MultipleChoice', 'Warm', 'Cold', 'Big', 'Fast', 'B', -1.0, 1.3, 0.25, 3, 25, 1),
('Word Knowledge', 'Phonics', 'Beginning Sounds', 'Which word starts with the same sound as CAT?', 'MultipleChoice', 'Dog', 'Cup', 'Ball', 'Sun', 'B', -0.5, 1.3, 0.25, 3, 25, 1),
('Word Knowledge', 'Phonics', 'Rhyming', 'Which word rhymes with CAKE?', 'MultipleChoice', 'Ball', 'Make', 'Cup', 'Dog', 'B', 0.0, 1.4, 0.25, 3, 25, 1),
('Word Knowledge', 'Sight Words', 'Common Words', 'Complete: I ___ a dog.', 'MultipleChoice', 'am', 'have', 'is', 'are', 'B', 0.3, 1.3, 0.25, 3, 25, 1),
('Word Knowledge', 'Syllables', 'Counting', 'How many syllables in ELEPHANT?', 'MultipleChoice', 'Two', 'Three', 'Four', 'Five', 'B', 0.8, 1.5, 0.25, 3, 30, 1),
('Word Knowledge', 'Prefixes', 'Meaning', 'UN-happy means:', 'MultipleChoice', 'Very happy', 'Not happy', 'A little happy', 'Always happy', 'B', 1.2, 1.6, 0.25, 3, 35, 1),
('Word Knowledge', 'Suffixes', 'Meaning', 'A person who TEACHES is a:', 'MultipleChoice', 'Teached', 'Teacher', 'Teaching', 'Teaches', 'B', 1.5, 1.6, 0.25, 3, 35, 1),
('Word Knowledge', 'Compound Words', 'Combining', 'RAIN + BOW makes:', 'MultipleChoice', 'Raincoat', 'Rainbow', 'Raindrop', 'Rainfall', 'B', 1.8, 1.7, 0.25, 3, 35, 1),
('Word Knowledge', 'Advanced', 'Context', 'The huge elephant was ___:', 'MultipleChoice', 'Tiny', 'Enormous', 'Small', 'Little', 'B', 2.0, 1.8, 0.25, 3, 40, 1);

PRINT '✓ 10 word knowledge items';
PRINT '';

-- CATEGORY 3: READING COMPREHENSION (10 Items with Passages)
PRINT 'Inserting Reading Comprehension - 10 items...';

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType, ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
('Reading Comprehension', 'Details', 'Literal', 'What color is the dog?', 'Reading', 'The dog is brown. The dog likes to run and play in the park.', 'Black', 'Brown', 'White', 'Yellow', 'B', -1.5, 1.1, 0.25, 3, 60, 1),
('Reading Comprehension', 'Main Idea', 'Central', 'What does Tom like to do?', 'Reading', 'Tom likes to play ball. He plays every day after school. Ball is his favorite game.', 'Reading', 'Playing ball', 'Swimming', 'Sleeping', 'B', -1.0, 1.3, 0.25, 3, 60, 1),
('Reading Comprehension', 'Setting', 'Where', 'Where did the children play?', 'Reading', 'It was a sunny day. The children played outside in the park. They had so much fun together.', 'At home', 'In the park', 'At school', 'Library', 'B', -0.5, 1.2, 0.25, 3, 60, 1),
('Reading Comprehension', 'Inference', 'What happened', 'What probably happened before this?', 'Reading', 'The grass was wet. There were puddles everywhere. The sky was gray and cloudy.', 'It was sunny', 'It rained', 'It snowed', 'It was windy', 'B', 0.0, 1.5, 0.25, 3, 75, 1),
('Reading Comprehension', 'Sequence', 'Order', 'What happened second?', 'Reading', 'First, Amy woke up. Then, she ate breakfast with her family. Last, she went to school.', 'Woke up', 'Ate breakfast', 'Went to school', 'Got dressed', 'B', 0.5, 1.4, 0.25, 3, 75, 1),
('Reading Comprehension', 'Prediction', 'What next', 'What will probably happen next?', 'Reading', 'Jenny''s red balloon slipped from her hand. It floated higher and higher into the sky. She watched it get smaller.', 'It will fly away', 'It will fall down', 'It will turn blue', 'She will catch it', 'A', 1.0, 1.6, 0.25, 3, 75, 1),
('Reading Comprehension', 'Theme', 'Lesson', 'What is the main lesson?', 'Reading', 'Max had two cookies. His friend Tim had none. Max smiled and gave one cookie to Tim. Tim was so happy. Max felt happy too.', 'Run fast', 'Sharing is nice', 'Eat vegetables', 'Go to bed early', 'B', 1.3, 1.7, 0.25, 3, 90, 1),
('Reading Comprehension', 'Purpose', 'Why written', 'Why was this written?', 'Reading', 'First, get two slices of bread. Next, spread peanut butter on one. Then, spread jelly on the other. Put them together.', 'To entertain', 'To teach how to make something', 'To describe', 'To make us laugh', 'B', 1.6, 1.8, 0.25, 3, 90, 1),
('Reading Comprehension', 'Compare', 'Alike', 'How are cats and dogs alike?', 'Reading', 'Cats and dogs are both pets. They both have fur and four legs. Both need food and water every day.', 'Both fly', 'Both are pets', 'Both swim', 'Both hop', 'B', 1.8, 1.9, 0.25, 3, 90, 1),
('Reading Comprehension', 'Feelings', 'Emotions', 'How did Maria feel at the end?', 'Reading', 'Maria walked into the big library. A kind librarian helped her find books about dinosaurs. Maria was so excited to read them!', 'Scared', 'Excited and happy', 'Angry', 'Tired', 'B', 2.0, 2.0, 0.25, 3, 120, 1);

PRINT '✓ 10 reading comprehension items';
PRINT '';

-- CATEGORY 4: LANGUAGE STRUCTURE (10 Items)
PRINT 'Inserting Language Structure - 10 items...';

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
('Language Structure', 'Grammar', 'Nouns', 'Which is a NOUN?', 'MultipleChoice', 'Run', 'Happy', 'Dog', 'Quickly', 'C', -1.5, 1.2, 0.25, 3, 25, 1),
('Language Structure', 'Grammar', 'Verbs', 'Which is a VERB?', 'MultipleChoice', 'Book', 'Jump', 'Red', 'Big', 'B', -1.0, 1.3, 0.25, 3, 25, 1),
('Language Structure', 'Adjectives', 'Describing', 'Which describes: The ___ cat', 'MultipleChoice', 'Run', 'Sleep', 'Fluffy', 'Jump', 'C', -0.5, 1.3, 0.25, 3, 30, 1),
('Language Structure', 'Sentences', 'Complete', 'Which is a complete sentence?', 'MultipleChoice', 'The dog', 'Ran fast', 'The dog ran fast.', 'Fast dog', 'C', 0.0, 1.4, 0.25, 3, 30, 1),
('Language Structure', 'Punctuation', 'End marks', 'What goes at the end: "Where is my book"', 'MultipleChoice', '.', '?', '!', ',', 'B', 0.5, 1.4, 0.25, 3, 25, 1),
('Language Structure', 'Capitals', 'Names', 'Which needs a capital?', 'MultipleChoice', 'the', 'dog', 'sarah', 'is', 'C', 0.8, 1.5, 0.25, 3, 30, 1),
('Language Structure', 'Agreement', 'Subject-Verb', 'The dog ___ in the yard.', 'MultipleChoice', 'run', 'runs', 'running', 'ran', 'B', 1.2, 1.5, 0.25, 3, 30, 1),
('Language Structure', 'Pronouns', 'Personal', 'Sara is nice. ___ is my friend.', 'MultipleChoice', 'He', 'She', 'It', 'They', 'B', 1.5, 1.7, 0.25, 3, 30, 1),
('Language Structure', 'Tenses', 'Past', 'Yesterday, I ___ to the park.', 'MultipleChoice', 'go', 'goes', 'went', 'going', 'C', 1.8, 1.6, 0.25, 3, 30, 1),
('Language Structure', 'Contractions', 'Meaning', 'What does "don''t" mean?', 'MultipleChoice', 'do not', 'did not', 'does not', 'will not', 'A', 2.0, 1.8, 0.25, 3, 35, 1);

PRINT '✓ 10 language structure items';
PRINT '';

-- =============================================
-- VERIFICATION
-- =============================================
PRINT '========================================';
PRINT 'VERIFICATION';
PRINT '========================================';
PRINT '';

DECLARE @Total INT;
SELECT @Total = COUNT(*) FROM dbo.AssessmentItems;
PRINT 'Total Items: ' + CAST(@Total AS VARCHAR);
PRINT '';

PRINT 'Items by Category:';
SELECT
    '  ' + Category + ' (' + QuestionType + '): ' + CAST(COUNT(*) AS VARCHAR)
FROM dbo.AssessmentItems
GROUP BY Category, QuestionType
ORDER BY Category;
PRINT '';

-- Check pronunciation
DECLARE @WrongCat INT;
SELECT @WrongCat = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation' AND Category != 'Oral Language';

IF @WrongCat = 0
    PRINT '✓ All pronunciation in "Oral Language"';
ELSE
    PRINT '✗ ERROR: Pronunciation in wrong category!';
PRINT '';

-- Check reading passages
DECLARE @ReadingCount INT;
SELECT @ReadingCount = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL;

PRINT 'Reading items with passages: ' + CAST(@ReadingCount AS VARCHAR);
IF @ReadingCount = 10
    PRINT '✓ All reading items have passages';
ELSE
    PRINT '✗ WARNING: Some reading items missing passages!';
PRINT '';

-- Sample passage
PRINT 'Sample passage:';
SELECT TOP 1 '  "' + LEFT(ReadingPassage, 50) + '..."'
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading';
PRINT '';

PRINT '========================================';
PRINT 'COMPLETE!';
PRINT '========================================';
PRINT '';
PRINT 'Summary:';
PRINT '  - 10 Pronunciation (Oral Language)';
PRINT '  - 10 Word Knowledge';
PRINT '  - 10 Reading Comprehension (with passages)';
PRINT '  - 10 Language Structure';
PRINT '  - Total: 40 Grade 3 questions';
PRINT '';
PRINT 'Enough for 28-question adaptive test (7 per category)';
PRINT '';
PRINT 'Next: Rebuild Android app!';
PRINT '';

GO
