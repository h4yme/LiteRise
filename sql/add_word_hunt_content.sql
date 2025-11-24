-- =============================================
-- LiteRise: Add Word Hunt Game Content
-- Vocabulary words with definitions for Word Hunt game
-- Uses VocabularyWords table (fallback for Word Hunt API)
-- =============================================

-- Create VocabularyWords table if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'VocabularyWords')
BEGIN
    CREATE TABLE [dbo].[VocabularyWords](
        [WordID] [int] IDENTITY(1,1) NOT NULL,
        [Word] [nvarchar](50) NOT NULL,
        [Definition] [nvarchar](500) NOT NULL,
        [ExampleSentence] [nvarchar](500) NULL,
        [Difficulty] [float] NOT NULL DEFAULT 0.5,
        [Category] [nvarchar](50) NULL,
        [GradeLevel] [int] NOT NULL DEFAULT 4,
        [IsActive] [bit] NOT NULL DEFAULT 1,
        [CreatedAt] [datetime] NOT NULL DEFAULT GETDATE(),
    PRIMARY KEY CLUSTERED ([WordID] ASC)
    ) ON [PRIMARY];
    PRINT 'Created VocabularyWords table';
END
GO

-- Populate VocabularyWords table for Word Hunt game
IF NOT EXISTS (SELECT 1 FROM VocabularyWords)
BEGIN
    INSERT INTO VocabularyWords (Word, Definition, ExampleSentence, Difficulty, Category, GradeLevel)
    VALUES
    -- Grade 4 words (Easy)
    ('READ', 'To look at and understand written words', 'I like to read books every night.', 0.3, 'Verbs', 4),
    ('BOOK', 'A written work with pages bound together', 'This book has a great story.', 0.3, 'Nouns', 4),
    ('WORD', 'A unit of language with meaning', 'Every word has a definition.', 0.3, 'Nouns', 4),
    ('LEARN', 'To gain knowledge or skill', 'We learn new things every day.', 0.4, 'Verbs', 4),
    ('WRITE', 'To form letters on paper', 'Write your name at the top.', 0.4, 'Verbs', 4),
    ('STORY', 'An account of events', 'Tell me a funny story.', 0.4, 'Nouns', 4),
    ('PLAY', 'To engage in activity for fun', 'Children love to play games.', 0.3, 'Verbs', 4),
    ('HELP', 'To assist or aid someone', 'Can you help me carry this?', 0.3, 'Verbs', 4),
    ('GAME', 'An activity done for fun', 'Lets play a fun game together.', 0.3, 'Nouns', 4),
    ('FIND', 'To discover or locate something', 'Can you find my lost pencil?', 0.4, 'Verbs', 4),

    -- Grade 5 words (Medium)
    ('SPEAK', 'To say words aloud', 'Speak loudly so everyone can hear.', 0.5, 'Verbs', 5),
    ('LISTEN', 'To pay attention to sounds', 'Listen carefully to the instructions.', 0.5, 'Verbs', 5),
    ('THINK', 'To use your mind', 'Think about your answer first.', 0.5, 'Verbs', 5),
    ('SENTENCE', 'A complete thought in words', 'Write a complete sentence.', 0.6, 'Grammar', 5),
    ('PARAGRAPH', 'A group of related sentences', 'The paragraph has five sentences.', 0.7, 'Grammar', 5),
    ('DESCRIBE', 'To tell about something', 'Describe what you see.', 0.6, 'Verbs', 5),
    ('EXPLAIN', 'To make clear', 'Explain how you solved the problem.', 0.7, 'Verbs', 5),
    ('COMPARE', 'To examine similarities', 'Compare these two poems.', 0.7, 'Verbs', 5),
    ('STUDY', 'To learn about a subject', 'I study math every day.', 0.5, 'Verbs', 5),
    ('ANSWER', 'A response to a question', 'What is your answer?', 0.5, 'Nouns', 5),

    -- Grade 6 words (Hard)
    ('VOCABULARY', 'All words a person knows', 'Build your vocabulary by reading.', 0.8, 'Language', 6),
    ('COMPREHEND', 'To understand fully', 'Did you comprehend the passage?', 0.9, 'Verbs', 6),
    ('SUMMARIZE', 'To give main points briefly', 'Summarize the chapter.', 0.9, 'Verbs', 6),
    ('ANALYZE', 'To examine in detail', 'Analyze the characters motives.', 1.0, 'Verbs', 6),
    ('EVIDENCE', 'Facts that prove something', 'Support your claim with evidence.', 0.8, 'Nouns', 6),
    ('PERSPECTIVE', 'A way of viewing things', 'Consider the authors perspective.', 1.0, 'Academic', 6),
    ('INFERENCE', 'A conclusion from evidence', 'Make an inference from the text.', 1.1, 'Academic', 6),
    ('CONTEXT', 'Surrounding circumstances', 'Use context clues to find meaning.', 1.0, 'Academic', 6),
    ('EVALUATE', 'To assess the value or quality', 'Evaluate the authors argument.', 1.1, 'Academic', 6),
    ('CONCLUDE', 'To reach a decision or judgment', 'What can you conclude from this?', 0.9, 'Verbs', 6);

    PRINT 'Inserted vocabulary words for Word Hunt';
END
ELSE
BEGIN
    PRINT 'VocabularyWords already has data';
END
GO

PRINT 'Word Hunt vocabulary setup complete!';
PRINT 'The Word Hunt API will use VocabularyWords table for game content.';
GO
