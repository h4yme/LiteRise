-- =====================================================
-- POPULATE QUIZ QUESTIONS
-- Sample questions for first lesson of each module
-- Pattern can be replicated for all 65 nodes
-- =====================================================

-- =====================================================
-- MODULE 1, NODE 1: Basic Sight Words
-- =====================================================

INSERT INTO [dbo].[QuizQuestions] (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory)
VALUES
-- Get NodeID for Module 1, Node 1
((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 1),
 'Which word is a sight word?',
 'multiple_choice',
 '["cat", "the", "jump", "happy"]',
 'B',
 'Easy',
 'Phonics'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 1),
 'Read the word: "and"',
 'pronunciation',
 NULL,
 NULL,
 'Easy',
 'Phonics'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 1),
 'Which sight word completes: "I ___ happy"?',
 'multiple_choice',
 '["am", "run", "big", "dog"]',
 'A',
 'Medium',
 'Phonics'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 1),
 'Which word is NOT a sight word?',
 'multiple_choice',
 '["is", "cat", "the", "and"]',
 'B',
 'Medium',
 'Phonics'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 1),
 'Complete the sentence: "___ is a dog."',
 'multiple_choice',
 '["It", "Run", "Big", "Jump"]',
 'A',
 'Easy',
 'Phonics');

PRINT 'Module 1, Node 1 - 5 quiz questions created';

-- =====================================================
-- MODULE 1, NODE 2: CVC Patterns
-- =====================================================

INSERT INTO [dbo].[QuizQuestions] (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory)
VALUES
((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 2),
 'Which word has a CVC pattern?',
 'multiple_choice',
 '["tree", "cat", "play", "rain"]',
 'B',
 'Easy',
 'Phonics'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 2),
 'Pronounce the word: "dog"',
 'pronunciation',
 NULL,
 NULL,
 'Easy',
 'Phonics'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 2),
 'What is the vowel sound in "sit"?',
 'multiple_choice',
 '["short i", "long i", "short e", "long a"]',
 'A',
 'Medium',
 'Phonics'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 2),
 'Which word does NOT follow CVC pattern?',
 'multiple_choice',
 '["run", "top", "boat", "pen"]',
 'C',
 'Medium',
 'Phonics'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 2),
 'Blend these sounds: /c/ /a/ /t/',
 'multiple_choice',
 '["cut", "cat", "cot", "coat"]',
 'B',
 'Hard',
 'Phonics');

PRINT 'Module 1, Node 2 - 5 quiz questions created';

-- =====================================================
-- MODULE 2, NODE 1: Basic Nouns and Verbs
-- =====================================================

INSERT INTO [dbo].[QuizQuestions] (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory)
VALUES
((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber = 1),
 'Which word is a noun?',
 'multiple_choice',
 '["run", "happy", "book", "quickly"]',
 'C',
 'Easy',
 'Vocabulary'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber = 1),
 'Which word is an action verb?',
 'multiple_choice',
 '["table", "jump", "red", "slowly"]',
 'B',
 'Easy',
 'Vocabulary'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber = 1),
 'What is a noun?',
 'multiple_choice',
 '["An action word", "A describing word", "A person, place, or thing", "A connecting word"]',
 'C',
 'Medium',
 'Vocabulary'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber = 1),
 'In "The dog runs fast", which is the verb?',
 'multiple_choice',
 '["dog", "runs", "fast", "the"]',
 'B',
 'Medium',
 'Vocabulary'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber = 1),
 'Which sentence has both a noun and a verb?',
 'multiple_choice',
 '["Happy and sad", "The cat sleeps", "Very quickly", "Red and blue"]',
 'B',
 'Hard',
 'Vocabulary');

PRINT 'Module 2, Node 1 - 5 quiz questions created';

-- =====================================================
-- MODULE 3, NODE 1: Sentence Basics
-- =====================================================

INSERT INTO [dbo].[QuizQuestions] (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory)
VALUES
((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber = 1),
 'Which is a complete sentence?',
 'multiple_choice',
 '["Running fast", "The dog barks", "In the park", "Very happy"]',
 'B',
 'Easy',
 'Grammar'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber = 1),
 'Every sentence needs a subject and a ___.',
 'multiple_choice',
 '["noun", "adjective", "predicate", "period"]',
 'C',
 'Medium',
 'Grammar'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber = 1),
 'In "Birds fly", what is the subject?',
 'multiple_choice',
 '["Birds", "fly", "Birds fly", "None"]',
 'A',
 'Easy',
 'Grammar'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber = 1),
 'Which is NOT a complete sentence?',
 'multiple_choice',
 '["She reads books.", "Under the tree.", "They play games.", "I am happy."]',
 'B',
 'Medium',
 'Grammar'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber = 1),
 'Fix this sentence: "run the children"',
 'multiple_choice',
 '["Run the children.", "The children run.", "Children the run.", "Run children the."]',
 'B',
 'Hard',
 'Grammar');

PRINT 'Module 3, Node 1 - 5 quiz questions created';

-- =====================================================
-- MODULE 4, NODE 1: Main Idea and Details
-- =====================================================

INSERT INTO [dbo].[QuizQuestions] (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory)
VALUES
((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber = 1),
 'Read: "Dogs are good pets. They are loyal. They protect homes." What is the main idea?',
 'multiple_choice',
 '["Dogs protect homes", "Dogs are loyal", "Dogs are good pets", "Dogs are animals"]',
 'C',
 'Medium',
 'Comprehension'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber = 1),
 'What is a main idea?',
 'multiple_choice',
 '["One small detail", "The most important point", "The first sentence", "The last word"]',
 'B',
 'Easy',
 'Comprehension'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber = 1),
 'Supporting details help explain the ___.',
 'multiple_choice',
 '["title", "main idea", "author", "picture"]',
 'B',
 'Easy',
 'Comprehension'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber = 1),
 'Read: "Apples are red or green. They grow on trees. Apples are healthy." Main idea?',
 'multiple_choice',
 '["Apples are red", "Trees grow apples", "Facts about apples", "Apples are green"]',
 'C',
 'Medium',
 'Comprehension'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber = 1),
 'Which is a supporting detail in: "Birds can fly. They have wings. Some migrate."',
 'multiple_choice',
 '["Birds can fly", "They have wings", "All birds are blue", "Birds live in water"]',
 'B',
 'Hard',
 'Comprehension');

PRINT 'Module 4, Node 1 - 5 quiz questions created';

-- =====================================================
-- MODULE 5, NODE 1: Writing Complete Sentences
-- =====================================================

INSERT INTO [dbo].[QuizQuestions] (NodeID, QuestionText, QuestionType, OptionsJSON, CorrectAnswer, EstimatedDifficulty, SkillCategory)
VALUES
((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber = 1),
 'A complete sentence must have a subject and a ___.',
 'multiple_choice',
 '["noun", "verb", "period", "comma"]',
 'B',
 'Easy',
 'Writing'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber = 1),
 'Which is a complete sentence?',
 'multiple_choice',
 '["The big tree", "Runs very fast", "She likes ice cream", "In the morning"]',
 'C',
 'Easy',
 'Writing'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber = 1),
 'How should a sentence begin?',
 'multiple_choice',
 '["With a period", "With a capital letter", "With a comma", "With a question mark"]',
 'B',
 'Easy',
 'Writing'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber = 1),
 'Complete this sentence: "The cat ___ on the mat."',
 'multiple_choice',
 '["happy", "sits", "quickly", "red"]',
 'B',
 'Medium',
 'Writing'),

((SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber = 1),
 'Which sentence is written correctly?',
 'multiple_choice',
 '["they play outside.", "They play outside.", "they Play outside.", "They Play Outside."]',
 'B',
 'Hard',
 'Writing');

PRINT 'Module 5, Node 1 - 5 quiz questions created';

-- =====================================================
-- SUMMARY
-- =====================================================

PRINT '';
PRINT '====================================================================';
PRINT 'QUIZ QUESTIONS SAMPLE CREATED!';
PRINT '====================================================================';
PRINT 'Questions created for first lesson of each module:';
PRINT '  - Module 1, Node 1: Basic Sight Words (5 questions)';
PRINT '  - Module 1, Node 2: CVC Patterns (5 questions)';
PRINT '  - Module 2, Node 1: Basic Nouns and Verbs (5 questions)';
PRINT '  - Module 3, Node 1: Sentence Basics (5 questions)';
PRINT '  - Module 4, Node 1: Main Idea and Details (5 questions)';
PRINT '  - Module 5, Node 1: Writing Complete Sentences (5 questions)';
PRINT '';
PRINT 'Total: 30 sample questions';
PRINT '';
PRINT 'TO DO: Create 5-10 questions for remaining 59 nodes';
PRINT 'Follow same pattern with mix of:';
PRINT '  - Easy questions (basic recall)';
PRINT '  - Medium questions (application)';
PRINT '  - Hard questions (analysis/synthesis)';
PRINT '  - Multiple choice and pronunciation types';
PRINT '====================================================================';
