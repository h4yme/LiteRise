-- =============================================
-- LiteRise: Create LessonGameContent Table
-- Stores game-specific content for each lesson
-- =============================================

-- Create the LessonGameContent table
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'LessonGameContent')
BEGIN
    CREATE TABLE LessonGameContent (
        ContentID INT IDENTITY(1,1) PRIMARY KEY,
        LessonID INT NOT NULL,
        GameType NVARCHAR(50) NOT NULL,  -- 'SentenceScramble', 'WordHunt', 'TimedTrail', etc.
        ContentText NVARCHAR(500) NOT NULL,  -- The sentence or word
        ContentData NVARCHAR(MAX) NULL,  -- JSON data (scrambled words, hints, etc.)
        Difficulty FLOAT DEFAULT 1.0,
        Category NVARCHAR(100) NULL,
        IsActive BIT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE(),

        CONSTRAINT FK_LessonGameContent_Lessons
            FOREIGN KEY (LessonID) REFERENCES Lessons(LessonID)
    );

    CREATE INDEX IX_LessonGameContent_LessonGame
        ON LessonGameContent(LessonID, GameType);

    PRINT 'LessonGameContent table created successfully!';
END
ELSE
BEGIN
    PRINT 'LessonGameContent table already exists.';
END
GO

-- =============================================
-- Sample data for Sentence Scramble (optional)
-- =============================================

-- Lesson 1 - Reading (Grade 4 level)
INSERT INTO LessonGameContent (LessonID, GameType, ContentText, Difficulty, Category)
VALUES
(1, 'SentenceScramble', 'The cat sat on the mat', 0.5, 'Simple'),
(1, 'SentenceScramble', 'She goes to school every day', 0.6, 'Simple'),
(1, 'SentenceScramble', 'The dog runs in the park', 0.5, 'Simple'),
(1, 'SentenceScramble', 'My mother cooks delicious food', 0.6, 'Simple'),
(1, 'SentenceScramble', 'The children play happily together', 0.7, 'Simple');

-- Lesson 2 - Vocabulary (Grade 4-5 level)
INSERT INTO LessonGameContent (LessonID, GameType, ContentText, Difficulty, Category)
VALUES
(2, 'SentenceScramble', 'Maria finished her homework diligently', 0.8, 'Compound'),
(2, 'SentenceScramble', 'The students are reading their books quietly', 0.9, 'Compound'),
(2, 'SentenceScramble', 'The teacher explained the lesson clearly', 0.8, 'Compound'),
(2, 'SentenceScramble', 'We visited the beautiful museum yesterday', 0.9, 'Compound'),
(2, 'SentenceScramble', 'The quick brown fox jumps over the lazy dog', 1.0, 'Compound');

-- Lesson 3 - Grammar (Grade 5 level)
INSERT INTO LessonGameContent (LessonID, GameType, ContentText, Difficulty, Category)
VALUES
(3, 'SentenceScramble', 'Reading books regularly helps improve vocabulary skills', 1.2, 'Complex'),
(3, 'SentenceScramble', 'My family and I visited the science museum yesterday', 1.3, 'Complex'),
(3, 'SentenceScramble', 'The beautiful butterfly landed gently on the colorful flower', 1.2, 'Complex'),
(3, 'SentenceScramble', 'Learning new words makes reading more enjoyable and interesting', 1.4, 'Complex'),
(3, 'SentenceScramble', 'The hardworking students completed their challenging project successfully', 1.5, 'Complex');

PRINT 'Sample Sentence Scramble content added!';
GO
