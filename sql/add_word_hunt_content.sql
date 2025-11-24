-- =============================================
-- LiteRise: Add Word Hunt Game Content
-- Vocabulary words with definitions for Word Hunt game
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

-- Insert Word Hunt content into LessonGameContent
DECLARE @Lesson1 INT, @Lesson2 INT, @Lesson3 INT, @Lesson4 INT, @Lesson5 INT;

SELECT @Lesson1 = LessonID FROM Lessons WHERE LessonTitle = 'Basic Sentence Structure';
SELECT @Lesson2 = LessonID FROM Lessons WHERE LessonTitle = 'Simple Sentences';
SELECT @Lesson3 = LessonID FROM Lessons WHERE LessonTitle = 'Compound Sentences';
SELECT @Lesson4 = LessonID FROM Lessons WHERE LessonTitle = 'Complex Sentences';
SELECT @Lesson5 = LessonID FROM Lessons WHERE LessonTitle = 'Advanced Grammar';

-- Only insert if no WordHunt content exists
IF NOT EXISTS (SELECT 1 FROM LessonGameContent WHERE GameType = 'WordHunt')
BEGIN
    -- Lesson 1: Basic vocabulary (Grade 4 - Easy)
    IF @Lesson1 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, ContentData, Difficulty, Category)
        VALUES
        (@Lesson1, 'WordHunt', 'READ', '{"definition": "To look at and understand written words", "example": "I like to read books."}', 0.3, 'Verbs'),
        (@Lesson1, 'WordHunt', 'BOOK', '{"definition": "A written work with pages bound together", "example": "This book has many pictures."}', 0.3, 'Nouns'),
        (@Lesson1, 'WordHunt', 'WORD', '{"definition": "A unit of language with meaning", "example": "Can you spell this word?"}', 0.3, 'Nouns'),
        (@Lesson1, 'WordHunt', 'LEARN', '{"definition": "To gain knowledge or skill", "example": "We learn new things at school."}', 0.4, 'Verbs'),
        (@Lesson1, 'WordHunt', 'WRITE', '{"definition": "To form letters on paper", "example": "Please write your name here."}', 0.4, 'Verbs'),
        (@Lesson1, 'WordHunt', 'STORY', '{"definition": "An account of events, real or imaginary", "example": "Tell me a story before bed."}', 0.4, 'Nouns'),
        (@Lesson1, 'WordHunt', 'PLAY', '{"definition": "To engage in activity for fun", "example": "Children play in the park."}', 0.3, 'Verbs'),
        (@Lesson1, 'WordHunt', 'HELP', '{"definition": "To assist or aid someone", "example": "Can you help me with this?"}', 0.3, 'Verbs');
        PRINT 'Inserted Word Hunt content for Lesson 1';
    END

    -- Lesson 2: Simple vocabulary (Grade 4-5)
    IF @Lesson2 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, ContentData, Difficulty, Category)
        VALUES
        (@Lesson2, 'WordHunt', 'SPEAK', '{"definition": "To say words aloud", "example": "Please speak clearly."}', 0.5, 'Verbs'),
        (@Lesson2, 'WordHunt', 'LISTEN', '{"definition": "To pay attention to sounds", "example": "Listen to the teacher carefully."}', 0.5, 'Verbs'),
        (@Lesson2, 'WordHunt', 'THINK', '{"definition": "To use your mind to consider", "example": "Think before you answer."}', 0.5, 'Verbs'),
        (@Lesson2, 'WordHunt', 'ANSWER', '{"definition": "A response to a question", "example": "What is your answer?"}', 0.5, 'Nouns'),
        (@Lesson2, 'WordHunt', 'STUDY', '{"definition": "To learn about a subject", "example": "I study math every day."}', 0.5, 'Verbs'),
        (@Lesson2, 'WordHunt', 'SCHOOL', '{"definition": "A place where students learn", "example": "We go to school on weekdays."}', 0.4, 'Nouns'),
        (@Lesson2, 'WordHunt', 'TEACHER', '{"definition": "A person who teaches", "example": "Our teacher is very kind."}', 0.5, 'Nouns'),
        (@Lesson2, 'WordHunt', 'STUDENT', '{"definition": "A person who studies", "example": "Every student passed the test."}', 0.5, 'Nouns');
        PRINT 'Inserted Word Hunt content for Lesson 2';
    END

    -- Lesson 3: Compound vocabulary (Grade 5)
    IF @Lesson3 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, ContentData, Difficulty, Category)
        VALUES
        (@Lesson3, 'WordHunt', 'SENTENCE', '{"definition": "A set of words expressing a complete thought", "example": "Write a complete sentence."}', 0.7, 'Grammar'),
        (@Lesson3, 'WordHunt', 'PARAGRAPH', '{"definition": "A group of related sentences", "example": "Read the first paragraph."}', 0.7, 'Grammar'),
        (@Lesson3, 'WordHunt', 'CHAPTER', '{"definition": "A main division of a book", "example": "We finished chapter three."}', 0.6, 'Nouns'),
        (@Lesson3, 'WordHunt', 'DESCRIBE', '{"definition": "To tell about something in words", "example": "Describe your favorite food."}', 0.7, 'Verbs'),
        (@Lesson3, 'WordHunt', 'EXPLAIN', '{"definition": "To make something clear", "example": "Can you explain this problem?"}', 0.7, 'Verbs'),
        (@Lesson3, 'WordHunt', 'COMPARE', '{"definition": "To examine similarities and differences", "example": "Compare these two pictures."}', 0.7, 'Verbs'),
        (@Lesson3, 'WordHunt', 'EXAMPLE', '{"definition": "Something that shows what others are like", "example": "Give me an example."}', 0.6, 'Nouns'),
        (@Lesson3, 'WordHunt', 'MEANING', '{"definition": "What something represents or signifies", "example": "What is the meaning of this word?"}', 0.7, 'Nouns');
        PRINT 'Inserted Word Hunt content for Lesson 3';
    END

    -- Lesson 4: Complex vocabulary (Grade 5-6)
    IF @Lesson4 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, ContentData, Difficulty, Category)
        VALUES
        (@Lesson4, 'WordHunt', 'VOCABULARY', '{"definition": "All the words a person knows", "example": "Reading builds vocabulary."}', 0.9, 'Language'),
        (@Lesson4, 'WordHunt', 'COMPREHEND', '{"definition": "To understand fully", "example": "Did you comprehend the lesson?"}', 0.9, 'Verbs'),
        (@Lesson4, 'WordHunt', 'SUMMARIZE', '{"definition": "To give a brief statement of main points", "example": "Summarize the story in three sentences."}', 0.9, 'Verbs'),
        (@Lesson4, 'WordHunt', 'INTERPRET', '{"definition": "To explain the meaning of something", "example": "How do you interpret this poem?"}', 1.0, 'Verbs'),
        (@Lesson4, 'WordHunt', 'ANALYZE', '{"definition": "To examine in detail", "example": "Analyze the main characters actions."}', 1.0, 'Verbs'),
        (@Lesson4, 'WordHunt', 'CONCLUDE', '{"definition": "To reach a decision or judgment", "example": "What can you conclude from this?"}', 0.9, 'Verbs'),
        (@Lesson4, 'WordHunt', 'EVIDENCE', '{"definition": "Facts that prove something", "example": "Show evidence for your answer."}', 0.8, 'Nouns'),
        (@Lesson4, 'WordHunt', 'SUPPORT', '{"definition": "To provide backing or help", "example": "Support your opinion with facts."}', 0.8, 'Verbs');
        PRINT 'Inserted Word Hunt content for Lesson 4';
    END

    -- Lesson 5: Advanced vocabulary (Grade 6)
    IF @Lesson5 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, ContentData, Difficulty, Category)
        VALUES
        (@Lesson5, 'WordHunt', 'SYNTHESIZE', '{"definition": "To combine parts into a whole", "example": "Synthesize information from multiple sources."}', 1.2, 'Academic'),
        (@Lesson5, 'WordHunt', 'EVALUATE', '{"definition": "To assess the value or quality", "example": "Evaluate the authors argument."}', 1.1, 'Academic'),
        (@Lesson5, 'WordHunt', 'PERSPECTIVE', '{"definition": "A particular way of viewing things", "example": "Consider different perspectives."}', 1.1, 'Academic'),
        (@Lesson5, 'WordHunt', 'INFERENCE', '{"definition": "A conclusion based on evidence", "example": "Make an inference from the clues."}', 1.2, 'Academic'),
        (@Lesson5, 'WordHunt', 'CONTEXT', '{"definition": "The circumstances surrounding something", "example": "Understand words in context."}', 1.0, 'Academic'),
        (@Lesson5, 'WordHunt', 'STRUCTURE', '{"definition": "The arrangement of parts", "example": "Analyze the story structure."}', 1.0, 'Academic'),
        (@Lesson5, 'WordHunt', 'CONVEY', '{"definition": "To communicate or express", "example": "What message does the author convey?"}', 1.1, 'Verbs'),
        (@Lesson5, 'WordHunt', 'DEVELOP', '{"definition": "To grow or cause to grow", "example": "Develop your ideas further."}', 0.9, 'Verbs');
        PRINT 'Inserted Word Hunt content for Lesson 5';
    END
END
GO

-- Also populate VocabularyWords table for general use
IF NOT EXISTS (SELECT 1 FROM VocabularyWords)
BEGIN
    INSERT INTO VocabularyWords (Word, Definition, ExampleSentence, Difficulty, Category, GradeLevel)
    VALUES
    -- Grade 4 words
    ('READ', 'To look at and understand written words', 'I like to read books every night.', 0.3, 'Verbs', 4),
    ('BOOK', 'A written work with pages bound together', 'This book has a great story.', 0.3, 'Nouns', 4),
    ('WORD', 'A unit of language with meaning', 'Every word has a definition.', 0.3, 'Nouns', 4),
    ('LEARN', 'To gain knowledge or skill', 'We learn new things every day.', 0.4, 'Verbs', 4),
    ('WRITE', 'To form letters on paper', 'Write your name at the top.', 0.4, 'Verbs', 4),
    ('STORY', 'An account of events', 'Tell me a funny story.', 0.4, 'Nouns', 4),
    ('PLAY', 'To engage in activity for fun', 'Children love to play games.', 0.3, 'Verbs', 4),
    ('HELP', 'To assist or aid someone', 'Can you help me carry this?', 0.3, 'Verbs', 4),

    -- Grade 5 words
    ('SPEAK', 'To say words aloud', 'Speak loudly so everyone can hear.', 0.5, 'Verbs', 5),
    ('LISTEN', 'To pay attention to sounds', 'Listen carefully to the instructions.', 0.5, 'Verbs', 5),
    ('THINK', 'To use your mind', 'Think about your answer first.', 0.5, 'Verbs', 5),
    ('SENTENCE', 'A complete thought in words', 'Write a complete sentence.', 0.6, 'Grammar', 5),
    ('PARAGRAPH', 'A group of related sentences', 'The paragraph has five sentences.', 0.7, 'Grammar', 5),
    ('DESCRIBE', 'To tell about something', 'Describe what you see.', 0.6, 'Verbs', 5),
    ('EXPLAIN', 'To make clear', 'Explain how you solved the problem.', 0.7, 'Verbs', 5),
    ('COMPARE', 'To examine similarities', 'Compare these two poems.', 0.7, 'Verbs', 5),

    -- Grade 6 words
    ('VOCABULARY', 'All words a person knows', 'Build your vocabulary by reading.', 0.8, 'Language', 6),
    ('COMPREHEND', 'To understand fully', 'Did you comprehend the passage?', 0.9, 'Verbs', 6),
    ('SUMMARIZE', 'To give main points briefly', 'Summarize the chapter.', 0.9, 'Verbs', 6),
    ('ANALYZE', 'To examine in detail', 'Analyze the characters motives.', 1.0, 'Verbs', 6),
    ('EVIDENCE', 'Facts that prove something', 'Support your claim with evidence.', 0.8, 'Nouns', 6),
    ('PERSPECTIVE', 'A way of viewing things', 'Consider the authors perspective.', 1.0, 'Academic', 6),
    ('INFERENCE', 'A conclusion from evidence', 'Make an inference from the text.', 1.1, 'Academic', 6),
    ('CONTEXT', 'Surrounding circumstances', 'Use context clues to find meaning.', 1.0, 'Academic', 6);

    PRINT 'Inserted vocabulary words';
END
GO

PRINT 'Word Hunt content setup complete!';
GO
