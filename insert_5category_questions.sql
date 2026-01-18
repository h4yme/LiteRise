-- Insert Assessment Items for 5 Categories
-- Date: 2026-01-18
USE [LiteRiseDB]
GO

-- Clear old items (optional - comment out if you want to keep existing items)
-- DELETE FROM [dbo].[AssessmentItems];

SET IDENTITY_INSERT [dbo].[AssessmentItems] ON;
GO

-- ============================================
-- Category 1: Phonics and Word Study
-- ============================================

INSERT INTO [dbo].[AssessmentItems]
    ([ItemID], [Category], [Subcategory], [QuestionText], [QuestionType],
     [OptionA], [OptionB], [OptionC], [OptionD], [CorrectAnswer],
     [DifficultyParam], [DiscriminationParam], [GuessingParam], [GradeLevel], [IsActive])
VALUES
(1, 'Phonics and Word Study', 'Letter Sounds', 'Which word starts with the same sound as "cat"?', 'multiple_choice',
 'dog', 'car', 'ball', 'sun', 'car', -1.5, 1.2, 0.25, 1, 1),

(2, 'Phonics and Word Study', 'Rhyming', 'Which word rhymes with "bat"?', 'multiple_choice',
 'cat', 'dog', 'ball', 'red', 'cat', -1.2, 1.3, 0.25, 1, 1),

(3, 'Phonics and Word Study', 'Blending', 'What word do these sounds make: /c/ /a/ /t/?', 'multiple_choice',
 'can', 'cat', 'cap', 'cut', 'cat', -0.8, 1.4, 0.25, 2, 1),

(4, 'Phonics and Word Study', 'Digraphs', 'Which word has the "sh" sound?', 'multiple_choice',
 'chip', 'ship', 'tip', 'dip', 'ship', 0.0, 1.5, 0.25, 2, 1),

(5, 'Phonics and Word Study', 'Long Vowels', 'Which word has a long "a" sound?', 'multiple_choice',
 'cat', 'cake', 'can', 'cap', 'cake', 0.5, 1.6, 0.25, 3, 1),

(6, 'Phonics and Word Study', 'Syllables', 'How many syllables are in "elephant"?', 'multiple_choice',
 '2', '3', '4', '5', '3', 1.0, 1.4, 0.25, 3, 1),

(7, 'Phonics and Word Study', 'Prefixes', 'What does the prefix "un-" mean in "unhappy"?', 'multiple_choice',
 'very', 'not', 'again', 'before', 'not', 1.5, 1.5, 0.25, 4, 1);

-- ============================================
-- Category 2: Vocabulary and Word Knowledge
-- ============================================

INSERT INTO [dbo].[AssessmentItems]
    ([ItemID], [Category], [Subcategory], [QuestionText], [QuestionType],
     [OptionA], [OptionB], [OptionC], [OptionD], [CorrectAnswer],
     [DifficultyParam], [DiscriminationParam], [GuessingParam], [GradeLevel], [IsActive])
VALUES
(8, 'Vocabulary and Word Knowledge', 'Basic Words', 'A dog is a type of _____.', 'multiple_choice',
 'plant', 'animal', 'food', 'toy', 'animal', -1.5, 1.2, 0.25, 1, 1),

(9, 'Vocabulary and Word Knowledge', 'Opposites', 'What is the opposite of "hot"?', 'multiple_choice',
 'warm', 'cold', 'big', 'small', 'cold', -1.0, 1.3, 0.25, 2, 1),

(10, 'Vocabulary and Word Knowledge', 'Synonyms', 'Which word means the same as "happy"?', 'multiple_choice',
 'sad', 'joyful', 'angry', 'tired', 'joyful', -0.5, 1.4, 0.25, 2, 1),

(11, 'Vocabulary and Word Knowledge', 'Context Clues', 'The enormous elephant was too big to fit. What does "enormous" mean?', 'multiple_choice',
 'small', 'very large', 'fast', 'slow', 'very large', 0.2, 1.5, 0.25, 3, 1),

(12, 'Vocabulary and Word Knowledge', 'Multiple Meanings', 'In "I can see a bat fly", what does "bat" mean?', 'multiple_choice',
 'a stick for baseball', 'a flying animal', 'to hit something', 'a tool', 'a flying animal', 0.8, 1.6, 0.25, 3, 1),

(13, 'Vocabulary and Word Knowledge', 'Idioms', 'What does "piece of cake" mean?', 'multiple_choice',
 'a dessert', 'very easy', 'very hard', 'delicious', 'very easy', 1.5, 1.5, 0.25, 4, 1),

(14, 'Vocabulary and Word Knowledge', 'Academic Words', 'To "analyze" means to:', 'multiple_choice',
 'memorize', 'examine carefully', 'write down', 'forget', 'examine carefully', 2.0, 1.6, 0.25, 5, 1);

-- ============================================
-- Category 3: Grammar Awareness
-- ============================================

INSERT INTO [dbo].[AssessmentItems]
    ([ItemID], [Category], [Subcategory], [QuestionText], [QuestionType],
     [OptionA], [OptionB], [OptionC], [OptionD], [CorrectAnswer],
     [DifficultyParam], [DiscriminationParam], [GuessingParam], [GradeLevel], [IsActive])
VALUES
(15, 'Grammar Awareness and Grammatical Structures', 'Nouns', 'Which word is a noun?', 'multiple_choice',
 'run', 'dog', 'happy', 'quickly', 'dog', -1.5, 1.2, 0.25, 1, 1),

(16, 'Grammar Awareness and Grammatical Structures', 'Verbs', 'Which word is a verb?', 'multiple_choice',
 'cat', 'jump', 'blue', 'slowly', 'jump', -1.2, 1.3, 0.25, 2, 1),

(17, 'Grammar Awareness and Grammatical Structures', 'Adjectives', 'Which word describes a noun?', 'multiple_choice',
 'run', 'quickly', 'beautiful', 'dog', 'beautiful', -0.5, 1.4, 0.25, 2, 1),

(18, 'Grammar Awareness and Grammatical Structures', 'Sentence Structure', 'Which is a complete sentence?', 'multiple_choice',
 'The dog', 'Running fast', 'The cat sleeps.', 'Under the tree', 'The cat sleeps.', 0.0, 1.5, 0.25, 3, 1),

(19, 'Grammar Awareness and Grammatical Structures', 'Punctuation', 'Where should the period go? "I like apples"', 'multiple_choice',
 'After I', 'After like', 'After apples', 'No period needed', 'After apples', 0.5, 1.3, 0.25, 2, 1),

(20, 'Grammar Awareness and Grammatical Structures', 'Subject-Verb Agreement', 'Choose the correct verb: The dogs ___ running.', 'multiple_choice',
 'is', 'are', 'am', 'be', 'are', 1.0, 1.6, 0.25, 3, 1),

(21, 'Grammar Awareness and Grammatical Structures', 'Tenses', 'Which sentence is in past tense?', 'multiple_choice',
 'I walk to school.', 'I walked to school.', 'I will walk to school.', 'I am walking to school.', 'I walked to school.', 1.5, 1.5, 0.25, 4, 1);

-- ============================================
-- Category 4: Comprehending and Analyzing Text
-- ============================================

INSERT INTO [dbo].[AssessmentItems]
    ([ItemID], [Category], [Subcategory], [QuestionText], [QuestionType], [ReadingPassage],
     [OptionA], [OptionB], [OptionC], [OptionD], [CorrectAnswer],
     [DifficultyParam], [DiscriminationParam], [GuessingParam], [GradeLevel], [IsActive])
VALUES
(22, 'Comprehending and Analyzing Text', 'Literal Comprehension', 'What color is the ball?', 'reading_comprehension',
 'Tom has a red ball. He plays with it every day.',
 'blue', 'red', 'green', 'yellow', 'red', -1.5, 1.2, 0.25, 1, 1),

(23, 'Comprehending and Analyzing Text', 'Main Idea', 'What is this story mainly about?', 'reading_comprehension',
 'Sara loves to read. She reads every night before bed. She has many books in her room. Her favorite books are about animals.',
 'sleeping', 'Sara loving to read', 'animals', 'nighttime', 'Sara loving to read', -0.8, 1.4, 0.25, 2, 1),

(24, 'Comprehending and Analyzing Text', 'Inference', 'How does Maria feel?', 'reading_comprehension',
 'Maria ran to her mom with a big smile. "I got an A on my test!" she shouted.',
 'sad', 'angry', 'happy', 'scared', 'happy', -0.3, 1.3, 0.25, 2, 1),

(25, 'Comprehending and Analyzing Text', 'Sequence', 'What happened first?', 'reading_comprehension',
 'First, Ana woke up. Then she ate breakfast. After that, she went to school.',
 'went to school', 'ate breakfast', 'woke up', 'came home', 'woke up', 0.2, 1.5, 0.25, 2, 1),

(26, 'Comprehending and Analyzing Text', 'Cause and Effect', 'Why did the plant die?', 'reading_comprehension',
 'John forgot to water his plant for two weeks. When he finally remembered, the plant was brown and dried out.',
 'too much water', 'no water', 'too much sun', 'too cold', 'no water', 0.8, 1.6, 0.25, 3, 1),

(27, 'Comprehending and Analyzing Text', 'Author''s Purpose', 'Why did the author write this?', 'reading_comprehension',
 'Wash your hands before eating. Use soap and warm water. Scrub for 20 seconds. This keeps you healthy.',
 'to entertain', 'to teach', 'to sell something', 'to describe', 'to teach', 1.5, 1.5, 0.25, 4, 1),

(28, 'Comprehending and Analyzing Text', 'Making Connections', 'This story is similar to:', 'reading_comprehension',
 'A boy worked hard all summer. He saved his money. Finally, he bought the bike he wanted.',
 'a fairy tale', 'working toward a goal', 'making friends', 'losing something', 'working toward a goal', 2.0, 1.6, 0.25, 5, 1);

-- ============================================
-- Category 5: Creating and Composing Text
-- ============================================

INSERT INTO [dbo].[AssessmentItems]
    ([ItemID], [Category], [Subcategory], [QuestionText], [QuestionType],
     [OptionA], [OptionB], [OptionC], [OptionD], [CorrectAnswer],
     [DifficultyParam], [DiscriminationParam], [GuessingParam], [GradeLevel], [IsActive])
VALUES
(29, 'Creating and Composing Text', 'Sentence Formation', 'Put these words in the correct order: dog / The / runs', 'multiple_choice',
 'runs dog The', 'The dog runs', 'dog runs The', 'The runs dog', 'The dog runs', -1.2, 1.3, 0.25, 1, 1),

(30, 'Creating and Composing Text', 'Capitalization', 'Which word should be capitalized?', 'multiple_choice',
 'i went to school', 'I went to school', 'i Went to school', 'i went To school', 'I went to school', -0.8, 1.2, 0.25, 2, 1),

(31, 'Creating and Composing Text', 'Story Sequence', 'What is the best beginning sentence for a story about a lost dog?', 'multiple_choice',
 'The dog was found.', 'One day, Max ran away.', 'Dogs are good pets.', 'I like dogs.', 'One day, Max ran away.', 0.0, 1.5, 0.25, 3, 1),

(32, 'Creating and Composing Text', 'Descriptive Writing', 'Which sentence has the best description?', 'multiple_choice',
 'The dog is nice.', 'The fluffy brown dog wagged its tail happily.', 'There is a dog.', 'A dog walked.', 'The fluffy brown dog wagged its tail happily.', 0.5, 1.6, 0.25, 3, 1),

(33, 'Creating and Composing Text', 'Paragraph Organization', 'Which sentence is the best conclusion?', 'multiple_choice',
 'First, I woke up.', 'That is why I love summer.', 'Next, I ate lunch.', 'The weather was nice.', 'That is why I love summer.', 1.0, 1.5, 0.25, 4, 1),

(34, 'Creating and Composing Text', 'Dialogue', 'Which sentence correctly shows someone speaking?', 'multiple_choice',
 'She said I am happy', '"I am happy," she said.', 'I am happy she said', 'She said "I am happy', '"I am happy," she said.', 1.5, 1.6, 0.25, 4, 1),

(35, 'Creating and Composing Text', 'Revision', 'Which sentence is written correctly?', 'multiple_choice',
 'Me and him went to the store', 'He and I went to the store', 'Him and me went to the store', 'Me and he went to the store', 'He and I went to the store', 2.0, 1.7, 0.25, 5, 1);

SET IDENTITY_INSERT [dbo].[AssessmentItems] OFF;
GO

PRINT '35 assessment items inserted for 5 categories (7 per category)';
GO
