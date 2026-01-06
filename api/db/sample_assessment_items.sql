-- ===============================================
-- Sample Assessment Items for IRT-Based Testing
-- ===============================================
-- This file provides sample questions across all categories
-- with properly calibrated IRT parameters
-- ===============================================

USE LiteRiseDB;
GO

PRINT 'Inserting sample assessment items...';
PRINT '';

-- ===============================================
-- Category 1: ORAL LANGUAGE
-- ===============================================
-- Tests listening comprehension, vocabulary, and verbal expression

PRINT 'Category 1: Oral Language Items...';

-- Easy items (difficulty: -2 to -1)
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Oral Language', 'Basic Vocabulary', 'Common Objects', 'What do you use to write on paper?', 'MultipleChoice', 'Pencil', 'Spoon', 'Shoe', 'Ball', 'A', -1.8, 1.2, 0.25, 1),
('Oral Language', 'Basic Vocabulary', 'Common Actions', 'What does "run" mean?', 'MultipleChoice', 'To move slowly', 'To move fast with your legs', 'To sit down', 'To sleep', 'B', -1.6, 1.1, 0.25, 1),
('Oral Language', 'Following Directions', 'Simple Instructions', 'If I say "clap your hands," what should you do?', 'MultipleChoice', 'Jump', 'Clap', 'Sit', 'Walk', 'B', -1.4, 1.3, 0.25, 1);

-- Medium items (difficulty: -0.5 to 0.5)
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Oral Language', 'Vocabulary', 'Descriptive Words', 'Which word means the same as "happy"?', 'MultipleChoice', 'Sad', 'Angry', 'Joyful', 'Tired', 'C', -0.2, 1.5, 0.25, 2),
('Oral Language', 'Listening Comprehension', 'Story Details', 'Listen: "The cat sat on the mat." Where is the cat?', 'MultipleChoice', 'On the bed', 'On the mat', 'In a tree', 'Under the table', 'B', 0.1, 1.4, 0.25, 2),
('Oral Language', 'Vocabulary', 'Opposite Words', 'What is the opposite of "big"?', 'MultipleChoice', 'Large', 'Small', 'Huge', 'Tall', 'B', 0.3, 1.6, 0.25, 2);

-- Hard items (difficulty: 1 to 2.5)
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Oral Language', 'Advanced Vocabulary', 'Multiple Meanings', 'Which word can mean both "a small insect" AND "to annoy"?', 'MultipleChoice', 'Fly', 'Bug', 'Ant', 'Bee', 'B', 1.2, 1.8, 0.25, 3),
('Oral Language', 'Figurative Language', 'Idioms', 'What does "raining cats and dogs" mean?', 'MultipleChoice', 'Animals are falling', 'Raining very hard', 'A sunny day', 'A light rain', 'B', 1.5, 1.7, 0.25, 3),
('Oral Language', 'Advanced Comprehension', 'Inference', 'If someone says "I''m all ears," what do they mean?', 'MultipleChoice', 'They have big ears', 'They''re ready to listen', 'They can''t hear', 'They''re tired', 'B', 1.8, 1.9, 0.25, 4);

-- ===============================================
-- Category 2: WORD KNOWLEDGE
-- ===============================================
-- Tests phonics, word recognition, and spelling

PRINT 'Category 2: Word Knowledge Items...';

-- Easy items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Word Knowledge', 'Letter Recognition', 'Uppercase Letters', 'Which one is the letter "A"?', 'MultipleChoice', 'B', 'A', 'C', 'D', 'B', -2.0, 1.0, 0.25, 1),
('Word Knowledge', 'Phonics', 'Beginning Sounds', 'What sound does the letter "B" make?', 'MultipleChoice', '/a/', '/b/', '/c/', '/d/', 'B', -1.7, 1.2, 0.25, 1),
('Word Knowledge', 'Simple Words', 'CVC Words', 'Which word says "cat"?', 'MultipleChoice', 'dog', 'cat', 'pig', 'rat', 'B', -1.5, 1.3, 0.25, 1);

-- Medium items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Word Knowledge', 'Blends', 'Beginning Blends', 'What word starts with "st"?', 'MultipleChoice', 'cat', 'stop', 'run', 'jump', 'B', -0.3, 1.5, 0.25, 2),
('Word Knowledge', 'Word Families', 'Rhyming Words', 'Which word rhymes with "cat"?', 'MultipleChoice', 'dog', 'hat', 'pen', 'sun', 'B', 0.0, 1.4, 0.25, 2),
('Word Knowledge', 'Sight Words', 'Common Words', 'Which is spelled correctly?', 'MultipleChoice', 'wuz', 'was', 'wos', 'wus', 'B', 0.4, 1.6, 0.25, 2);

-- Hard items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Word Knowledge', 'Syllables', 'Multi-syllable Words', 'How many syllables are in "butterfly"?', 'MultipleChoice', '2', '3', '4', '5', 'B', 1.1, 1.8, 0.25, 3),
('Word Knowledge', 'Word Parts', 'Prefixes', 'What does "un-" mean in "unhappy"?', 'MultipleChoice', 'Very', 'Not', 'More', 'Less', 'B', 1.4, 1.7, 0.25, 3),
('Word Knowledge', 'Spelling', 'Complex Words', 'Which word is spelled correctly?', 'MultipleChoice', 'becuz', 'because', 'becaus', 'becuase', 'B', 1.7, 1.9, 0.25, 4);

-- ===============================================
-- Category 3: READING COMPREHENSION
-- ===============================================
-- Tests understanding of written texts

PRINT 'Category 3: Reading Comprehension Items...';

-- Easy items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Reading Comprehension', 'Basic Understanding', 'Literal Comprehension', 'Read: "The dog is brown." What color is the dog?', 'MultipleChoice', 'Black', 'Brown', 'White', 'Yellow', 'B', -1.9, 1.1, 0.25, 1),
('Reading Comprehension', 'Main Idea', 'Simple Stories', 'Read: "Tom likes to play ball. He plays every day." What does Tom like?', 'MultipleChoice', 'Reading', 'Playing ball', 'Swimming', 'Sleeping', 'B', -1.6, 1.3, 0.25, 1),
('Reading Comprehension', 'Details', 'Who/What Questions', 'Read: "Sara has a red bike." Who has a bike?', 'MultipleChoice', 'Tom', 'Sara', 'Mom', 'Dad', 'B', -1.3, 1.2, 0.25, 1);

-- Medium items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Reading Comprehension', 'Inference', 'Simple Inference', 'Read: "The grass was wet. Puddles were everywhere." What probably happened?', 'MultipleChoice', 'It was sunny', 'It rained', 'It snowed', 'It was windy', 'B', -0.1, 1.5, 0.25, 2),
('Reading Comprehension', 'Sequence', 'Order of Events', 'Read: "First, I woke up. Then, I ate breakfast. Last, I went to school." What happened second?', 'MultipleChoice', 'Woke up', 'Ate breakfast', 'Went to school', 'Got dressed', 'B', 0.2, 1.4, 0.25, 2),
('Reading Comprehension', 'Prediction', 'What Comes Next', 'Read: "The balloon floated higher and higher into the sky." What will probably happen next?', 'MultipleChoice', 'It will pop or fly away', 'It will fall down', 'It will turn blue', 'It will get bigger', 'A', 0.5, 1.6, 0.25, 2);

-- Hard items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Reading Comprehension', 'Theme', 'Central Message', 'Read a short story about sharing. What is the main lesson?', 'MultipleChoice', 'Always run fast', 'Sharing is caring', 'Eat your vegetables', 'Go to bed early', 'B', 1.3, 1.8, 0.25, 3),
('Reading Comprehension', 'Author''s Purpose', 'Why Written', 'Why would someone write a recipe?', 'MultipleChoice', 'To entertain', 'To teach how to make something', 'To tell a story', 'To describe a place', 'B', 1.6, 1.7, 0.25, 3),
('Reading Comprehension', 'Compare/Contrast', 'Similarities/Differences', 'How are a cat and a dog alike?', 'MultipleChoice', 'Both fly', 'Both are pets', 'Both live in water', 'Both hop', 'B', 1.9, 1.9, 0.25, 4);

-- ===============================================
-- Category 4: LANGUAGE STRUCTURE
-- ===============================================
-- Tests grammar, sentence structure, and mechanics

PRINT 'Category 4: Language Structure Items...';

-- Easy items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Language Structure', 'Basic Grammar', 'Capitalization', 'Which word should start with a capital letter?', 'MultipleChoice', 'cat', 'dog', 'Tom', 'run', 'C', -1.8, 1.2, 0.25, 1),
('Language Structure', 'Basic Grammar', 'End Punctuation', 'What goes at the end of this sentence: "I like cats__"', 'MultipleChoice', '!', '.', '?', ',', 'B', -1.5, 1.1, 0.25, 1),
('Language Structure', 'Parts of Speech', 'Nouns', 'Which word is a naming word (noun)?', 'MultipleChoice', 'run', 'book', 'happy', 'quickly', 'B', -1.2, 1.3, 0.25, 1);

-- Medium items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Language Structure', 'Sentence Structure', 'Complete Sentences', 'Which is a complete sentence?', 'MultipleChoice', 'The big dog', 'Running fast', 'I like pizza', 'In the park', 'C', 0.0, 1.5, 0.25, 2),
('Language Structure', 'Verb Tense', 'Past Tense', 'What is the past tense of "run"?', 'MultipleChoice', 'runs', 'running', 'ran', 'runned', 'C', 0.3, 1.4, 0.25, 2),
('Language Structure', 'Plurals', 'Regular Plurals', 'What is the plural of "cat"?', 'MultipleChoice', 'cat', 'cats', 'cates', 'caties', 'B', 0.6, 1.6, 0.25, 2);

-- Hard items
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel)
VALUES
('Language Structure', 'Complex Sentences', 'Conjunctions', 'Which word connects two ideas: "I like apples __ oranges"?', 'MultipleChoice', 'but', 'and', 'because', 'when', 'B', 1.4, 1.8, 0.25, 3),
('Language Structure', 'Subject-Verb Agreement', 'Singular/Plural', 'Which is correct?', 'MultipleChoice', 'He run fast', 'He runs fast', 'He running fast', 'He runned fast', 'B', 1.7, 1.7, 0.25, 3),
('Language Structure', 'Punctuation', 'Advanced Punctuation', 'Where does the comma go: "I like pizza ice cream and cake"?', 'MultipleChoice', 'After pizza and ice cream', 'After like only', 'After cream only', 'No comma needed', 'A', 2.0, 1.9, 0.25, 4);

PRINT '';
PRINT '=====================================================';
PRINT 'Sample Items Inserted Successfully!';
PRINT '=====================================================';

-- Display summary statistics
SELECT
    Category,
    COUNT(*) AS TotalItems,
    MIN(DifficultyParam) AS MinDifficulty,
    MAX(DifficultyParam) AS MaxDifficulty,
    AVG(DifficultyParam) AS AvgDifficulty,
    AVG(DiscriminationParam) AS AvgDiscrimination
FROM dbo.AssessmentItems
GROUP BY Category
ORDER BY Category;

PRINT '';
PRINT 'Note: These are sample items for testing.';
PRINT 'Add more items to build a comprehensive question bank.';
PRINT '=====================================================';
GO
