-- =============================================
-- RESET AND INSERT ALL ASSESSMENT ITEMS
-- =============================================
-- This script completely resets the AssessmentItems table
-- and inserts ALL questions for the 28-question placement test
-- =============================================

USE LiteRiseDB;
GO

PRINT '========================================';
PRINT 'RESETTING ASSESSMENT ITEMS TABLE';
PRINT '========================================';
PRINT '';

-- =============================================
-- STEP 1: Add ReadingPassage Column (if missing)
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
ELSE
BEGIN
    PRINT '✓ ReadingPassage column exists';
END
PRINT '';

-- =============================================
-- STEP 2: Clear Existing Data
-- =============================================
PRINT 'Clearing existing assessment items...';
DELETE FROM dbo.AssessmentItems;
PRINT '✓ Table cleared';
PRINT '';

-- =============================================
-- STEP 3: Insert All Questions by Category
-- =============================================

-- =============================================
-- CATEGORY 1: ORAL LANGUAGE - PRONUNCIATION (30 items)
-- =============================================
PRINT 'Inserting Category 1: Oral Language (Pronunciation)...';

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips,
    EstimatedTime, IsActive
) VALUES
-- Easy Pronunciation (-2.0 to -1.0)
('Oral Language', 'Pronunciation', 'Basic Consonants', 'Say the word: CAT', 'Pronunciation', NULL, NULL, NULL, NULL, 'cat', -2.0, 1.1, 0.0, 1, 'cat', '/kæt/', 65, 'Start with a "k" sound, then say "at"', 20, 1),
('Oral Language', 'Pronunciation', 'Basic Consonants', 'Say the word: DOG', 'Pronunciation', NULL, NULL, NULL, NULL, 'dog', -1.9, 1.1, 0.0, 1, 'dog', '/dɔɡ/', 65, 'Start with a "d" sound, add the "og" sound', 20, 1),
('Oral Language', 'Pronunciation', 'Basic Consonants', 'Say the word: SUN', 'Pronunciation', NULL, NULL, NULL, NULL, 'sun', -1.8, 1.2, 0.0, 1, 'sun', '/sʌn/', 65, 'Make the "s" sound like a snake, then say "un"', 20, 1),
('Oral Language', 'Pronunciation', 'Basic Consonants', 'Say the word: BIG', 'Pronunciation', NULL, NULL, NULL, NULL, 'big', -1.7, 1.1, 0.0, 1, 'big', '/bɪɡ/', 65, 'Start with "b", then the short "i" sound, end with "g"', 20, 1),
('Oral Language', 'Pronunciation', 'Short Vowels', 'Say the word: HAT', 'Pronunciation', NULL, NULL, NULL, NULL, 'hat', -1.6, 1.2, 0.0, 1, 'hat', '/hæt/', 65, 'Breathe out "h", then the short "a" as in "cat"', 20, 1),

-- Medium Pronunciation (-1.0 to 0.5)
('Oral Language', 'Pronunciation', 'Consonant Blends', 'Say the word: STOP', 'Pronunciation', NULL, NULL, NULL, NULL, 'stop', -1.0, 1.3, 0.0, 2, 'stop', '/stɑp/', 70, 'Blend "s" and "t" together smoothly, then "op"', 25, 1),
('Oral Language', 'Pronunciation', 'Consonant Blends', 'Say the word: TREE', 'Pronunciation', NULL, NULL, NULL, NULL, 'tree', -0.9, 1.3, 0.0, 2, 'tree', '/triː/', 70, 'Blend "t" and "r" together, then long "ee" sound', 25, 1),
('Oral Language', 'Pronunciation', 'Long Vowels', 'Say the word: MAKE', 'Pronunciation', NULL, NULL, NULL, NULL, 'make', -0.8, 1.4, 0.0, 2, 'make', '/meɪk/', 70, 'Say "m", then the long "a" sound like saying the letter A', 25, 1),
('Oral Language', 'Pronunciation', 'Long Vowels', 'Say the word: BIKE', 'Pronunciation', NULL, NULL, NULL, NULL, 'bike', -0.7, 1.3, 0.0, 2, 'bike', '/baɪk/', 70, 'Start with "b", then say "ike" like the word "I"', 25, 1),
('Oral Language', 'Pronunciation', 'Consonant Blends', 'Say the word: JUMP', 'Pronunciation', NULL, NULL, NULL, NULL, 'jump', -0.6, 1.3, 0.0, 2, 'jump', '/dʒʌmp/', 70, 'Make the "j" sound, then "ump" like "bump"', 25, 1),
('Oral Language', 'Pronunciation', 'R-Controlled Vowels', 'Say the word: BIRD', 'Pronunciation', NULL, NULL, NULL, NULL, 'bird', -0.5, 1.4, 0.0, 2, 'bird', '/bɜːrd/', 72, 'The "ir" makes a special sound - like "er" in "her"', 25, 1),
('Oral Language', 'Pronunciation', 'Digraphs', 'Say the word: SHIP', 'Pronunciation', NULL, NULL, NULL, NULL, 'ship', -0.4, 1.4, 0.0, 2, 'ship', '/ʃɪp/', 72, 'Make the "sh" sound by blowing air softly', 25, 1),
('Oral Language', 'Pronunciation', 'Digraphs', 'Say the word: THEN', 'Pronunciation', NULL, NULL, NULL, NULL, 'then', -0.3, 1.5, 0.0, 2, 'then', '/ðɛn/', 72, 'Put tongue between teeth for "th", then say "en"', 25, 1),
('Oral Language', 'Pronunciation', 'Diphthongs', 'Say the word: COIN', 'Pronunciation', NULL, NULL, NULL, NULL, 'coin', -0.2, 1.4, 0.0, 2, 'coin', '/kɔɪn/', 72, 'Say "oi" like "oy" in "boy"', 25, 1),
('Oral Language', 'Pronunciation', 'Silent Letters', 'Say the word: KNIFE', 'Pronunciation', NULL, NULL, NULL, NULL, 'knife', -0.1, 1.5, 0.0, 2, 'knife', '/naɪf/', 75, 'The "k" is silent! Start with "n", then "ife"', 30, 1),

-- Medium-Hard Pronunciation (0.5 to 1.5)
('Oral Language', 'Pronunciation', 'Complex Blends', 'Say the word: SPRING', 'Pronunciation', NULL, NULL, NULL, NULL, 'spring', 0.6, 1.6, 0.0, 3, 'spring', '/sprɪŋ/', 75, 'Blend "s", "p", and "r" smoothly, end with "ing"', 30, 1),
('Oral Language', 'Pronunciation', 'Complex Blends', 'Say the word: SPLASH', 'Pronunciation', NULL, NULL, NULL, NULL, 'splash', 0.7, 1.6, 0.0, 3, 'splash', '/splæʃ/', 75, 'Three consonants together: "s-p-l", then "ash"', 30, 1),
('Oral Language', 'Pronunciation', 'Two Syllables', 'Say the word: HAPPY', 'Pronunciation', NULL, NULL, NULL, NULL, 'happy', 0.8, 1.5, 0.0, 3, 'happy', '/ˈhæp.i/', 75, 'Two parts: "HAP" (stressed) and "py"', 30, 1),
('Oral Language', 'Pronunciation', 'Two Syllables', 'Say the word: TIGER', 'Pronunciation', NULL, NULL, NULL, NULL, 'tiger', 0.9, 1.5, 0.0, 3, 'tiger', '/ˈtaɪ.ɡər/', 75, 'TI (like "tie") gets stress, then "ger"', 30, 1),
('Oral Language', 'Pronunciation', 'Two Syllables', 'Say the word: TABLE', 'Pronunciation', NULL, NULL, NULL, NULL, 'table', 1.0, 1.5, 0.0, 3, 'table', '/ˈteɪ.bəl/', 75, 'TA (long "a") is stressed, "ble" is softer', 30, 1),
('Oral Language', 'Pronunciation', 'Tricky Vowels', 'Say the word: SCHOOL', 'Pronunciation', NULL, NULL, NULL, NULL, 'school', 1.1, 1.6, 0.0, 3, 'school', '/skuːl/', 78, 'The "oo" makes a long "oo" sound like "pool"', 30, 1),
('Oral Language', 'Pronunciation', 'Tricky Vowels', 'Say the word: THOUGHT', 'Pronunciation', NULL, NULL, NULL, NULL, 'thought', 1.2, 1.7, 0.0, 3, 'thought', '/θɔːt/', 78, 'Start with "th", then "ought" sounds like "awt"', 30, 1),
('Oral Language', 'Pronunciation', 'Three Syllables', 'Say the word: ELEPHANT', 'Pronunciation', NULL, NULL, NULL, NULL, 'elephant', 1.3, 1.6, 0.0, 3, 'elephant', '/ˈɛl.ɪ.fənt/', 78, 'Three parts: EL-e-phant, stress on first syllable', 35, 1),
('Oral Language', 'Pronunciation', 'Three Syllables', 'Say the word: IMPORTANT', 'Pronunciation', NULL, NULL, NULL, NULL, 'important', 1.4, 1.6, 0.0, 3, 'important', '/ɪmˈpɔr.tənt/', 78, 'Three syllables: im-POR-tant, stress on middle', 35, 1),

-- Hard Pronunciation (1.5 to 2.5)
('Oral Language', 'Pronunciation', 'Irregular Vowels', 'Say the word: ENOUGH', 'Pronunciation', NULL, NULL, NULL, NULL, 'enough', 1.6, 1.7, 0.0, 4, 'enough', '/ɪˈnʌf/', 80, 'The "ough" sounds like "uff". Stress on second part', 35, 1),
('Oral Language', 'Pronunciation', 'Irregular Vowels', 'Say the word: THROUGH', 'Pronunciation', NULL, NULL, NULL, NULL, 'through', 1.7, 1.8, 0.0, 4, 'through', '/θruː/', 80, 'Sounds like "threw". The "ough" makes "oo" sound', 35, 1),
('Oral Language', 'Pronunciation', 'Four Syllables', 'Say the word: BEAUTIFUL', 'Pronunciation', NULL, NULL, NULL, NULL, 'beautiful', 1.8, 1.7, 0.0, 4, 'beautiful', '/ˈbjuː.tɪ.fəl/', 80, 'BEAU-ti-ful, four syllables, stress on first', 40, 1),
('Oral Language', 'Pronunciation', 'Irregular Words', 'Say the word: WRITE', 'Pronunciation', NULL, NULL, NULL, NULL, 'write', 1.9, 1.8, 0.0, 3, 'write', '/raɪt/', 82, 'Silent "w"! Just say "rite" like "right"', 30, 1),
('Oral Language', 'Pronunciation', 'Complex Words', 'Say the word: WATER', 'Pronunciation', NULL, NULL, NULL, NULL, 'water', 2.0, 1.8, 0.0, 3, 'water', '/ˈwɔː.tər/', 82, 'WA-ter, the "t" sounds like "d" in American English', 30, 1),
('Oral Language', 'Pronunciation', 'Advanced Words', 'Say the word: ANSWER', 'Pronunciation', NULL, NULL, NULL, NULL, 'answer', 2.1, 1.9, 0.0, 3, 'answer', '/ˈæn.sər/', 85, 'The "w" is silent! Say AN-ser', 35, 1);

PRINT '✓ Inserted 30 pronunciation items in Oral Language';
PRINT '';

-- =============================================
-- CATEGORY 2: WORD KNOWLEDGE (15 items)
-- =============================================
PRINT 'Inserting Category 2: Word Knowledge...';

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
-- Vocabulary Items
('Word Knowledge', 'Vocabulary', 'Synonyms', 'Which word means the same as HAPPY?', 'MultipleChoice', 'Sad', 'Joyful', 'Angry', 'Tired', 'B', -1.5, 1.2, 0.25, 1, 25, 1),
('Word Knowledge', 'Vocabulary', 'Antonyms', 'Which word means the opposite of HOT?', 'MultipleChoice', 'Warm', 'Cold', 'Big', 'Fast', 'B', -1.2, 1.3, 0.25, 1, 25, 1),
('Word Knowledge', 'Vocabulary', 'Meanings', 'A DOCTOR is someone who:', 'MultipleChoice', 'Teaches students', 'Helps sick people', 'Drives a bus', 'Cooks food', 'B', -0.8, 1.4, 0.25, 2, 30, 1),

-- Phonics Items
('Word Knowledge', 'Phonics', 'Beginning Sounds', 'Which word starts with the same sound as CAT?', 'MultipleChoice', 'Dog', 'Cup', 'Ball', 'Sun', 'B', -1.0, 1.3, 0.25, 1, 25, 1),
('Word Knowledge', 'Phonics', 'Ending Sounds', 'Which word ends with the same sound as HAT?', 'MultipleChoice', 'Cat', 'Dog', 'Sun', 'Cup', 'A', -0.7, 1.3, 0.25, 1, 25, 1),
('Word Knowledge', 'Phonics', 'Rhyming', 'Which word rhymes with CAKE?', 'MultipleChoice', 'Ball', 'Make', 'Cup', 'Dog', 'B', -0.5, 1.4, 0.25, 2, 25, 1),

-- Sight Words
('Word Knowledge', 'Sight Words', 'Common Words', 'Complete the sentence: I ___ a dog.', 'MultipleChoice', 'am', 'have', 'is', 'are', 'B', -0.3, 1.3, 0.25, 1, 25, 1),
('Word Knowledge', 'Sight Words', 'Common Words', 'Which word fits: The cat ___ on the mat.', 'MultipleChoice', 'sit', 'sits', 'sitting', 'sat', 'D', 0.0, 1.4, 0.25, 2, 30, 1),

-- Syllables
('Word Knowledge', 'Syllables', 'Counting Syllables', 'How many syllables in ELEPHANT?', 'MultipleChoice', 'Two', 'Three', 'Four', 'Five', 'B', 0.5, 1.5, 0.25, 2, 30, 1),
('Word Knowledge', 'Syllables', 'Counting Syllables', 'How many syllables in BANANA?', 'MultipleChoice', 'Two', 'Three', 'Four', 'One', 'B', 0.8, 1.5, 0.25, 2, 30, 1),

-- Prefixes/Suffixes
('Word Knowledge', 'Prefixes', 'Meaning', 'UN-happy means:', 'MultipleChoice', 'Very happy', 'Not happy', 'A little happy', 'Always happy', 'B', 1.0, 1.6, 0.25, 3, 35, 1),
('Word Knowledge', 'Suffixes', 'Meaning', 'A person who TEACHES is a:', 'MultipleChoice', 'Teached', 'Teacher', 'Teaching', 'Teaches', 'B', 1.2, 1.6, 0.25, 3, 35, 1),

-- Compound Words
('Word Knowledge', 'Compound Words', 'Combining', 'RAIN + BOW makes:', 'MultipleChoice', 'Raincoat', 'Rainbow', 'Raindrop', 'Rainfall', 'B', 1.5, 1.7, 0.25, 3, 35, 1),
('Word Knowledge', 'Compound Words', 'Combining', 'SUN + SHINE makes:', 'MultipleChoice', 'Sunlight', 'Sunset', 'Sunshine', 'Sunday', 'C', 1.8, 1.7, 0.25, 3, 35, 1),

-- Advanced Vocabulary
('Word Knowledge', 'Advanced Vocabulary', 'Context Clues', 'The huge elephant was ___:', 'MultipleChoice', 'Tiny', 'Enormous', 'Small', 'Little', 'B', 2.0, 1.8, 0.25, 4, 40, 1);

PRINT '✓ Inserted 15 Word Knowledge items';
PRINT '';

-- =============================================
-- CATEGORY 3: READING COMPREHENSION (15 items)
-- =============================================
PRINT 'Inserting Category 3: Reading Comprehension...';

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType, ReadingPassage,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
-- Easy Reading (-2.0 to -1.0)
('Reading Comprehension', 'Literal Understanding', 'Details',
 'What color is the dog?', 'Reading',
 'The dog is brown. The dog likes to run and play in the park.',
 'Black', 'Brown', 'White', 'Yellow', 'B', -1.9, 1.1, 0.25, 1, 60, 1),

('Reading Comprehension', 'Main Idea', 'Central Idea',
 'What does Tom like to do?', 'Reading',
 'Tom likes to play ball. He plays every day after school. Ball is his favorite game.',
 'Reading', 'Playing ball', 'Swimming', 'Sleeping', 'B', -1.6, 1.3, 0.25, 1, 60, 1),

('Reading Comprehension', 'Details', 'Who/What',
 'Who has a bike?', 'Reading',
 'Sara has a red bike. She rides it to the park every morning. Her bike is very fast.',
 'Tom', 'Sara', 'Mom', 'Dad', 'B', -1.3, 1.2, 0.25, 1, 60, 1),

('Reading Comprehension', 'Literal Understanding', 'Setting',
 'Where did the children play?', 'Reading',
 'It was a sunny day. The children played outside in the park. They had so much fun together.',
 'At home', 'In the park', 'At school', 'In the library', 'B', -1.0, 1.2, 0.25, 1, 60, 1),

-- Medium Reading (-1.0 to 0.5)
('Reading Comprehension', 'Inference', 'Simple Inference',
 'What probably happened before this?', 'Reading',
 'The grass was wet. There were puddles everywhere. The sky was gray and cloudy.',
 'It was sunny', 'It rained', 'It snowed', 'It was windy', 'B', -0.3, 1.5, 0.25, 2, 75, 1),

('Reading Comprehension', 'Sequence', 'Order of Events',
 'What happened second in the story?', 'Reading',
 'First, Amy woke up when her alarm rang. Then, she ate a big breakfast with her family. Last, she grabbed her backpack and went to school.',
 'Woke up', 'Ate breakfast', 'Went to school', 'Got dressed', 'B', 0.0, 1.4, 0.25, 2, 75, 1),

('Reading Comprehension', 'Prediction', 'What Comes Next',
 'What will probably happen next?', 'Reading',
 'Jenny''s red balloon slipped from her hand. It floated higher and higher into the bright blue sky. She watched it get smaller and smaller.',
 'It will pop or fly away', 'It will fall down', 'It will turn blue', 'Jenny will catch it', 'A', 0.3, 1.6, 0.25, 2, 75, 1),

('Reading Comprehension', 'Character Feelings', 'Emotions',
 'How does Sam feel?', 'Reading',
 'Sam lost his favorite toy. He looked everywhere but could not find it. He sat on his bed with tears in his eyes.',
 'Happy', 'Sad', 'Excited', 'Angry', 'B', 0.5, 1.5, 0.25, 2, 75, 1),

-- Medium-Hard Reading (0.5 to 1.5)
('Reading Comprehension', 'Theme', 'Central Message',
 'What is the main lesson of this story?', 'Reading',
 'Max had two cookies. His friend Tim had none. Max thought about eating both cookies himself. But then he remembered how sad he felt when he had nothing. Max smiled and gave one cookie to Tim. Tim''s face lit up with joy. Max felt happy too.',
 'Always run fast', 'Sharing makes everyone happy', 'Eat your vegetables', 'Go to bed early', 'B', 0.8, 1.7, 0.25, 3, 90, 1),

('Reading Comprehension', 'Author''s Purpose', 'Why Written',
 'Why did someone write this?', 'Reading',
 'First, get two slices of bread. Next, spread peanut butter on one slice. Then, spread jelly on the other slice. Finally, put the slices together. Now you have a delicious sandwich!',
 'To entertain us', 'To teach us how to make something', 'To describe a place', 'To make us laugh', 'B', 1.1, 1.8, 0.25, 3, 90, 1),

('Reading Comprehension', 'Compare/Contrast', 'Similarities',
 'How are cats and dogs alike?', 'Reading',
 'Cats and dogs are both popular pets. They both have fur and four legs. Cats like to climb and jump. Dogs like to run and fetch. But both animals love to play with their owners and need food and water every day.',
 'Both can fly', 'Both are pets that people love', 'Both live in water', 'Both hop around', 'B', 1.4, 1.9, 0.25, 3, 90, 1),

-- Hard Reading (1.5 to 2.0)
('Reading Comprehension', 'Inference', 'Character Feelings',
 'How did Maria feel at the end?', 'Reading',
 'Maria walked into the big library for the first time. The tall shelves full of books made her feel small. She didn''t know where to start. Then a kind librarian asked if she needed help. The librarian showed Maria the children''s section with colorful books. Maria picked out three books about dinosaurs. She couldn''t wait to read them all!',
 'Scared and nervous', 'Excited and happy', 'Angry and upset', 'Tired and bored', 'B', 1.6, 2.0, 0.25, 3, 120, 1),

('Reading Comprehension', 'Cause and Effect', 'Understanding Relationships',
 'Why did the plants grow so well?', 'Reading',
 'Mr. Garcia''s garden was the best on the street. Every morning, he watered his plants carefully. He made sure they got plenty of sunshine. He also pulled out the weeds that tried to grow. Because he took such good care of his garden, his tomatoes grew big and red. His flowers bloomed in beautiful colors.',
 'He had magic seeds', 'He took good care of them', 'The garden was very old', 'It never rained', 'B', 1.8, 2.0, 0.25, 3, 120, 1),

('Reading Comprehension', 'Making Connections', 'Text to Self',
 'What lesson can you learn from this story?', 'Reading',
 'Carlos practiced piano every day even when he didn''t feel like it. Some days his fingers hurt. Some days he wanted to play outside instead. But he kept practicing. After six months, he could play his favorite song perfectly. His family clapped and cheered when he played it for them.',
 'Always play outside', 'Practice helps you get better', 'Piano is easy', 'Music is boring', 'B', 2.0, 2.0, 0.25, 4, 120, 1),

('Reading Comprehension', 'Drawing Conclusions', 'Inference',
 'What can you conclude about Emma?', 'Reading',
 'Emma always carries a book in her backpack. During lunch, she reads instead of playing. Her room has three big bookshelves filled with books. She visits the library every Saturday. Her favorite birthday presents are always books.',
 'Emma loves to read', 'Emma hates sports', 'Emma is mean', 'Emma is sleepy', 'A', 2.2, 2.0, 0.25, 4, 120, 1);

PRINT '✓ Inserted 15 Reading Comprehension items with passages';
PRINT '';

-- =============================================
-- CATEGORY 4: LANGUAGE STRUCTURE (15 items)
-- =============================================
PRINT 'Inserting Category 4: Language Structure...';

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    EstimatedTime, IsActive
) VALUES
-- Grammar Basics
('Language Structure', 'Grammar', 'Nouns', 'Which word is a NOUN (person, place, or thing)?', 'MultipleChoice', 'Run', 'Happy', 'Dog', 'Quickly', 'C', -1.5, 1.2, 0.25, 1, 25, 1),
('Language Structure', 'Grammar', 'Verbs', 'Which word is a VERB (action word)?', 'MultipleChoice', 'Book', 'Jump', 'Red', 'Big', 'B', -1.2, 1.3, 0.25, 1, 25, 1),
('Language Structure', 'Grammar', 'Adjectives', 'Which word describes the cat: The ___ cat', 'MultipleChoice', 'Run', 'Sleep', 'Fluffy', 'Jump', 'C', -0.9, 1.3, 0.25, 2, 30, 1),

-- Sentence Structure
('Language Structure', 'Sentence Construction', 'Complete Sentences', 'Which is a complete sentence?', 'MultipleChoice', 'The dog', 'Ran fast', 'The dog ran fast.', 'Fast dog', 'C', -0.5, 1.4, 0.25, 2, 30, 1),
('Language Structure', 'Sentence Construction', 'Word Order', 'Put the words in order: "park / to / We / went / the"', 'MultipleChoice', 'Park the to went we', 'We went to the park', 'Went we park the to', 'The park went to we', 'B', 0.0, 1.5, 0.25, 2, 35, 1),

-- Punctuation
('Language Structure', 'Punctuation', 'End Marks', 'What goes at the end: "Where is my book"', 'MultipleChoice', '.', '?', '!', ',', 'B', 0.3, 1.4, 0.25, 2, 25, 1),
('Language Structure', 'Punctuation', 'Capitals', 'Which word needs a capital letter?', 'MultipleChoice', 'the', 'dog', 'sarah', 'is', 'C', 0.6, 1.5, 0.25, 2, 30, 1),
('Language Structure', 'Punctuation', 'Commas', 'Where does the comma go: "I like apples oranges and bananas"', 'MultipleChoice', 'After like', 'After apples and oranges', 'No comma needed', 'After bananas', 'B', 1.0, 1.6, 0.25, 3, 35, 1),

-- Subject-Verb Agreement
('Language Structure', 'Agreement', 'Subject-Verb', 'The dog ___ in the yard.', 'MultipleChoice', 'run', 'runs', 'running', 'ran', 'B', 0.8, 1.5, 0.25, 2, 30, 1),
('Language Structure', 'Agreement', 'Subject-Verb', 'They ___ to school every day.', 'MultipleChoice', 'goes', 'go', 'going', 'went', 'B', 1.2, 1.6, 0.25, 3, 30, 1),

-- Pronouns
('Language Structure', 'Pronouns', 'Personal Pronouns', 'Sara is nice. ___ is my friend.', 'MultipleChoice', 'He', 'She', 'It', 'They', 'B', 1.5, 1.7, 0.25, 3, 30, 1),
('Language Structure', 'Pronouns', 'Possessive', 'This is ___ book. (belonging to me)', 'MultipleChoice', 'I', 'me', 'my', 'mine', 'C', 1.8, 1.7, 0.25, 3, 35, 1),

-- Tenses
('Language Structure', 'Verb Tenses', 'Past Tense', 'Yesterday, I ___ to the park.', 'MultipleChoice', 'go', 'goes', 'went', 'going', 'C', 1.3, 1.6, 0.25, 3, 30, 1),
('Language Structure', 'Verb Tenses', 'Future Tense', 'Tomorrow, we ___ have a test.', 'MultipleChoice', 'will', 'did', 'was', 'are', 'A', 1.6, 1.7, 0.25, 3, 35, 1),

-- Advanced Grammar
('Language Structure', 'Advanced Grammar', 'Contractions', 'What does "don''t" mean?', 'MultipleChoice', 'do not', 'did not', 'does not', 'will not', 'A', 2.0, 1.8, 0.25, 4, 35, 1);

PRINT '✓ Inserted 15 Language Structure items';
PRINT '';

-- =============================================
-- STEP 4: Update Statistics
-- =============================================
PRINT 'Updating item statistics...';

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
    END;

PRINT '✓ Statistics updated';
PRINT '';

-- =============================================
-- STEP 5: Verification
-- =============================================
PRINT '========================================';
PRINT 'VERIFICATION';
PRINT '========================================';
PRINT '';

-- Total count
DECLARE @TotalItems INT;
SELECT @TotalItems = COUNT(*) FROM dbo.AssessmentItems;
PRINT 'Total Items: ' + CAST(@TotalItems AS VARCHAR);
PRINT '';

-- Category breakdown
PRINT 'Items by Category:';
SELECT
    '  ' + Category + ' (' + QuestionType + '): ' + CAST(COUNT(*) AS VARCHAR) AS Distribution
FROM dbo.AssessmentItems
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

-- Check pronunciation categories
DECLARE @WrongPronunciation INT;
SELECT @WrongPronunciation = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation' AND Category != 'Oral Language';

IF @WrongPronunciation = 0
    PRINT '✓ All pronunciation items in "Oral Language" category';
ELSE
    PRINT '✗ WARNING: ' + CAST(@WrongPronunciation AS VARCHAR) + ' pronunciation items in wrong category!';
PRINT '';

-- Check reading passages
DECLARE @ReadingWithPassages INT;
SELECT @ReadingWithPassages = COUNT(*)
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL AND ReadingPassage != '';

PRINT 'Reading items with passages: ' + CAST(@ReadingWithPassages AS VARCHAR);
IF @ReadingWithPassages >= 10
    PRINT '✓ Sufficient reading comprehension items';
ELSE
    PRINT '✗ WARNING: Need more reading items with passages!';
PRINT '';

-- Sample reading passage
PRINT 'Sample Reading Passage:';
SELECT TOP 1
    '  ItemID ' + CAST(ItemID AS VARCHAR) + ': ' + LEFT(ReadingPassage, 80) + '...' AS Sample
FROM dbo.AssessmentItems
WHERE QuestionType = 'Reading' AND ReadingPassage IS NOT NULL
ORDER BY ItemID;
PRINT '';

PRINT '========================================';
PRINT 'RESET COMPLETE!';
PRINT '========================================';
PRINT '';
PRINT 'Summary:';
PRINT '  - 30 Pronunciation items (Oral Language)';
PRINT '  - 15 Word Knowledge items';
PRINT '  - 15 Reading Comprehension items (with passages)';
PRINT '  - 15 Language Structure items';
PRINT '  - Total: 75 calibrated questions';
PRINT '';
PRINT 'Ready for 28-question adaptive placement test!';
PRINT '';
PRINT 'Next step: Rebuild Android app';
PRINT '  1. Build > Clean Project';
PRINT '  2. Build > Rebuild Project';
PRINT '  3. Uninstall app from device';
PRINT '  4. Run > Run ''app''';
PRINT '';

GO
