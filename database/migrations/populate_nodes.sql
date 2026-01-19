-- =====================================================
-- POPULATE NODES FOR ALL 5 MODULES
-- 13 nodes per module: 12 core lessons + 1 final assessment
-- =====================================================

-- =====================================================
-- MODULE 1: PHONICS AND WORD STUDY (EN3PWS)
-- =====================================================

INSERT INTO [dbo].[Nodes] (ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, SkillCategory, EstimatedDuration, XPReward)
VALUES
-- Quarter 1 (Foundation)
(1, 'CORE_LESSON', 1, 1, 'Basic Sight Words',
 'Recognize and read common sight words (the, and, is, it, to, you, that, was, for, on)',
 'Phonics', 10, 20),

(1, 'CORE_LESSON', 2, 1, 'CVC Patterns',
 'Blend and read consonant-vowel-consonant words (cat, dog, run, sit, top)',
 'Phonics', 12, 20),

(1, 'CORE_LESSON', 3, 1, 'Short Vowel Sounds',
 'Identify and pronounce short vowel sounds in words (a as in cat, e as in bed, i as in sit, o as in hot, u as in cup)',
 'Phonics', 10, 20),

-- Quarter 2 (Building)
(1, 'CORE_LESSON', 4, 2, 'Long Vowel Sounds',
 'Recognize long vowel patterns with silent e (cake, bike, home, cube, hope)',
 'Phonics', 12, 20),

(1, 'CORE_LESSON', 5, 2, 'Blends and Digraphs',
 'Read words with consonant blends (bl, cr, st, tr) and digraphs (sh, ch, th, wh)',
 'Phonics', 15, 20),

(1, 'CORE_LESSON', 6, 2, 'R-Controlled Vowels',
 'Pronounce r-controlled vowel sounds (ar, er, ir, or, ur) in words like car, her, bird, for, turn',
 'Phonics', 12, 20),

-- Quarter 3 (Advancing)
(1, 'CORE_LESSON', 7, 3, 'Silent Letters',
 'Identify and read words with silent letters (knee, write, lamb, gnome, comb)',
 'Phonics', 10, 20),

(1, 'CORE_LESSON', 8, 3, 'Multi-Syllabic Words',
 'Decode two and three-syllable words (win-dow, re-mem-ber, fan-tas-tic)',
 'Phonics', 15, 20),

(1, 'CORE_LESSON', 9, 3, 'Word Families',
 'Read and spell word families (-at, -an, -it, -op, -ug families)',
 'Phonics', 10, 20),

-- Quarter 4 (Mastery)
(1, 'CORE_LESSON', 10, 4, 'Advanced Phonics Patterns',
 'Decode complex vowel teams (oi/oy, au/aw, ou/ow) and diphthongs',
 'Phonics', 15, 20),

(1, 'CORE_LESSON', 11, 4, 'Reading Fluency Practice',
 'Read connected text with appropriate speed, accuracy, and expression',
 'Phonics', 12, 20),

(1, 'CORE_LESSON', 12, 4, 'Phonics Review and Application',
 'Apply all phonics skills to decode unfamiliar words in context',
 'Phonics', 10, 20),

-- Final Assessment
(1, 'FINAL_ASSESSMENT', 13, NULL, 'Module 1 Mastery Assessment',
 'Demonstrate mastery of all phonics and word study skills covered in this module',
 'Phonics', 20, 50);

PRINT 'Module 1 (Phonics and Word Study) - 13 nodes created';

-- =====================================================
-- MODULE 2: VOCABULARY AND WORD KNOWLEDGE (EN3VWK)
-- =====================================================

INSERT INTO [dbo].[Nodes] (ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, SkillCategory, EstimatedDuration, XPReward)
VALUES
-- Quarter 1
(2, 'CORE_LESSON', 1, 1, 'Basic Nouns and Verbs',
 'Identify and use common nouns (person, place, thing) and action verbs',
 'Vocabulary', 10, 20),

(2, 'CORE_LESSON', 2, 1, 'Describing Words',
 'Use adjectives to describe people, places, and things (big, small, happy, sad)',
 'Vocabulary', 12, 20),

(2, 'CORE_LESSON', 3, 1, 'Everyday Words',
 'Learn high-frequency vocabulary for daily life (colors, numbers, family, school)',
 'Vocabulary', 10, 20),

-- Quarter 2
(2, 'CORE_LESSON', 4, 2, 'Synonyms and Antonyms',
 'Recognize words with similar and opposite meanings (big/large, hot/cold)',
 'Vocabulary', 12, 20),

(2, 'CORE_LESSON', 5, 2, 'Context Clues',
 'Use surrounding words to figure out meanings of unfamiliar words',
 'Vocabulary', 15, 20),

(2, 'CORE_LESSON', 6, 2, 'Word Roots and Prefixes',
 'Understand common prefixes (un-, re-, pre-) and their meanings',
 'Vocabulary', 12, 20),

-- Quarter 3
(2, 'CORE_LESSON', 7, 3, 'Compound Words',
 'Form and understand compound words (sunflower, rainbow, homework)',
 'Vocabulary', 10, 20),

(2, 'CORE_LESSON', 8, 3, 'Multiple Meaning Words',
 'Recognize that some words have more than one meaning (bat, park, watch)',
 'Vocabulary', 15, 20),

(2, 'CORE_LESSON', 9, 3, 'Academic Vocabulary',
 'Learn content-area vocabulary for reading, math, and science',
 'Vocabulary', 10, 20),

-- Quarter 4
(2, 'CORE_LESSON', 10, 4, 'Figurative Language Basics',
 'Understand simple similes and metaphors (as brave as a lion)',
 'Vocabulary', 15, 20),

(2, 'CORE_LESSON', 11, 4, 'Word Choice and Usage',
 'Select precise words to express ideas clearly',
 'Vocabulary', 12, 20),

(2, 'CORE_LESSON', 12, 4, 'Vocabulary in Context',
 'Apply vocabulary knowledge to understand and create meaningful text',
 'Vocabulary', 10, 20),

-- Final Assessment
(2, 'FINAL_ASSESSMENT', 13, NULL, 'Module 2 Mastery Assessment',
 'Demonstrate mastery of vocabulary and word knowledge skills',
 'Vocabulary', 20, 50);

PRINT 'Module 2 (Vocabulary and Word Knowledge) - 13 nodes created';

-- =====================================================
-- MODULE 3: GRAMMAR AWARENESS (EN3GAGS)
-- =====================================================

INSERT INTO [dbo].[Nodes] (ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, SkillCategory, EstimatedDuration, XPReward)
VALUES
-- Quarter 1
(3, 'CORE_LESSON', 1, 1, 'Sentence Basics',
 'Recognize complete sentences with subject and predicate',
 'Grammar', 10, 20),

(3, 'CORE_LESSON', 2, 1, 'Capitalization Rules',
 'Capitalize first word in sentence, proper nouns, and the word I',
 'Grammar', 12, 20),

(3, 'CORE_LESSON', 3, 1, 'End Punctuation',
 'Use periods, question marks, and exclamation points correctly',
 'Grammar', 10, 20),

-- Quarter 2
(3, 'CORE_LESSON', 4, 2, 'Nouns and Pronouns',
 'Identify and use singular/plural nouns and personal pronouns',
 'Grammar', 12, 20),

(3, 'CORE_LESSON', 5, 2, 'Verbs and Tenses',
 'Use present, past, and future tense verbs correctly',
 'Grammar', 15, 20),

(3, 'CORE_LESSON', 6, 2, 'Subject-Verb Agreement',
 'Match subjects and verbs in number (he runs, they run)',
 'Grammar', 12, 20),

-- Quarter 3
(3, 'CORE_LESSON', 7, 3, 'Adjectives and Adverbs',
 'Use adjectives to describe nouns and adverbs to describe verbs',
 'Grammar', 10, 20),

(3, 'CORE_LESSON', 8, 3, 'Conjunctions',
 'Connect words and sentences using and, but, or, so',
 'Grammar', 15, 20),

(3, 'CORE_LESSON', 9, 3, 'Types of Sentences',
 'Create declarative, interrogative, imperative, and exclamatory sentences',
 'Grammar', 10, 20),

-- Quarter 4
(3, 'CORE_LESSON', 10, 4, 'Sentence Expansion',
 'Add details to simple sentences to make them more interesting',
 'Grammar', 15, 20),

(3, 'CORE_LESSON', 11, 4, 'Common Grammar Errors',
 'Identify and correct common mistakes (its/it\'s, their/there/they\'re)',
 'Grammar', 12, 20),

(3, 'CORE_LESSON', 12, 4, 'Grammar in Writing',
 'Apply grammar rules to write clear, correct sentences',
 'Grammar', 10, 20),

-- Final Assessment
(3, 'FINAL_ASSESSMENT', 13, NULL, 'Module 3 Mastery Assessment',
 'Demonstrate mastery of grammar awareness and grammatical structures',
 'Grammar', 20, 50);

PRINT 'Module 3 (Grammar Awareness) - 13 nodes created';

-- =====================================================
-- MODULE 4: COMPREHENDING AND ANALYZING TEXT (EN3CAT)
-- =====================================================

INSERT INTO [dbo].[Nodes] (ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, SkillCategory, EstimatedDuration, XPReward)
VALUES
-- Quarter 1
(4, 'CORE_LESSON', 1, 1, 'Main Idea and Details',
 'Identify the main idea and supporting details in a text',
 'Comprehension', 10, 20),

(4, 'CORE_LESSON', 2, 1, 'Story Elements',
 'Recognize characters, setting, problem, and solution in stories',
 'Comprehension', 12, 20),

(4, 'CORE_LESSON', 3, 1, 'Sequence of Events',
 'Put story events in order using words like first, next, then, last',
 'Comprehension', 10, 20),

-- Quarter 2
(4, 'CORE_LESSON', 4, 2, 'Making Predictions',
 'Use text clues and prior knowledge to predict what will happen next',
 'Comprehension', 12, 20),

(4, 'CORE_LESSON', 5, 2, 'Cause and Effect',
 'Identify why things happen (cause) and what happens (effect)',
 'Comprehension', 15, 20),

(4, 'CORE_LESSON', 6, 2, 'Compare and Contrast',
 'Find similarities and differences between characters, events, or texts',
 'Comprehension', 12, 20),

-- Quarter 3
(4, 'CORE_LESSON', 7, 3, 'Making Inferences',
 'Use clues from text and pictures to figure out unstated information',
 'Comprehension', 10, 20),

(4, 'CORE_LESSON', 8, 3, 'Author\'s Purpose',
 'Determine why author wrote text (to inform, entertain, or persuade)',
 'Comprehension', 15, 20),

(4, 'CORE_LESSON', 9, 3, 'Fact vs Opinion',
 'Distinguish between statements that can be proven and personal beliefs',
 'Comprehension', 10, 20),

-- Quarter 4
(4, 'CORE_LESSON', 10, 4, 'Text Features',
 'Use headings, captions, bold words, and diagrams to understand text',
 'Comprehension', 15, 20),

(4, 'CORE_LESSON', 11, 4, 'Summarizing',
 'Retell the most important parts of a text in your own words',
 'Comprehension', 12, 20),

(4, 'CORE_LESSON', 12, 4, 'Critical Reading',
 'Ask questions, make connections, and think deeply about what you read',
 'Comprehension', 10, 20),

-- Final Assessment
(4, 'FINAL_ASSESSMENT', 13, NULL, 'Module 4 Mastery Assessment',
 'Demonstrate mastery of text comprehension and analysis skills',
 'Comprehension', 20, 50);

PRINT 'Module 4 (Comprehending and Analyzing Text) - 13 nodes created';

-- =====================================================
-- MODULE 5: CREATING AND COMPOSING TEXT (EN3CCT)
-- =====================================================

INSERT INTO [dbo].[Nodes] (ModuleID, NodeType, NodeNumber, Quarter, LessonTitle, LearningObjectives, SkillCategory, EstimatedDuration, XPReward)
VALUES
-- Quarter 1
(5, 'CORE_LESSON', 1, 1, 'Writing Complete Sentences',
 'Write simple sentences with subject and predicate',
 'Writing', 10, 20),

(5, 'CORE_LESSON', 2, 1, 'Organizing Ideas',
 'Put ideas in a logical order (beginning, middle, end)',
 'Writing', 12, 20),

(5, 'CORE_LESSON', 3, 1, 'Narrative Writing Basics',
 'Write a simple personal story with clear events',
 'Writing', 10, 20),

-- Quarter 2
(5, 'CORE_LESSON', 4, 2, 'Descriptive Writing',
 'Use sensory details to describe people, places, and things',
 'Writing', 12, 20),

(5, 'CORE_LESSON', 5, 2, 'Writing Dialogue',
 'Use quotation marks and speaker tags in conversation',
 'Writing', 15, 20),

(5, 'CORE_LESSON', 6, 2, 'Informative Writing',
 'Write to explain or teach about a topic using facts',
 'Writing', 12, 20),

-- Quarter 3
(5, 'CORE_LESSON', 7, 3, 'Opinion Writing',
 'State an opinion and give reasons to support it',
 'Writing', 10, 20),

(5, 'CORE_LESSON', 8, 3, 'Writing Process',
 'Plan, draft, revise, edit, and publish written work',
 'Writing', 15, 20),

(5, 'CORE_LESSON', 9, 3, 'Paragraph Structure',
 'Write paragraphs with topic sentence, details, and conclusion',
 'Writing', 10, 20),

-- Quarter 4
(5, 'CORE_LESSON', 10, 4, 'Creative Story Writing',
 'Create imaginative stories with interesting characters and plots',
 'Writing', 15, 20),

(5, 'CORE_LESSON', 11, 4, 'Revising and Editing',
 'Improve writing by checking for clarity, grammar, and spelling',
 'Writing', 12, 20),

(5, 'CORE_LESSON', 12, 4, 'Publishing and Sharing',
 'Prepare final writing pieces to share with others',
 'Writing', 10, 20),

-- Final Assessment
(5, 'FINAL_ASSESSMENT', 13, NULL, 'Module 5 Mastery Assessment',
 'Demonstrate mastery of creating and composing text skills',
 'Writing', 20, 50);

PRINT 'Module 5 (Creating and Composing Text) - 13 nodes created';

-- =====================================================
-- SUMMARY
-- =====================================================

PRINT '';
PRINT '====================================================================';
PRINT 'NODES POPULATION COMPLETED!';
PRINT '====================================================================';
PRINT 'Total nodes created: 65 (13 nodes × 5 modules)';
PRINT '  - Module 1: Phonics and Word Study (13 nodes)';
PRINT '  - Module 2: Vocabulary and Word Knowledge (13 nodes)';
PRINT '  - Module 3: Grammar Awareness (13 nodes)';
PRINT '  - Module 4: Comprehending and Analyzing Text (13 nodes)';
PRINT '  - Module 5: Creating and Composing Text (13 nodes)';
PRINT '';
PRINT 'Each module has:';
PRINT '  - Quarter 1: Lessons 1-3 (Foundation)';
PRINT '  - Quarter 2: Lessons 4-6 (Building)';
PRINT '  - Quarter 3: Lessons 7-9 (Advancing)';
PRINT '  - Quarter 4: Lessons 10-12 (Mastery)';
PRINT '  - Final Assessment: Lesson 13';
PRINT '====================================================================';
