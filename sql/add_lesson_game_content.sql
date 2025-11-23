-- =============================================
-- LiteRise: Add Lesson Game Content Table
-- This table stores game-specific content for each lesson
-- Items table is reserved for Pre/Post Assessments only
-- =============================================

-- First, check if Lessons table exists, if not create it
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Lessons')
BEGIN
    CREATE TABLE [dbo].[Lessons](
        [LessonID] [int] IDENTITY(1,1) NOT NULL,
        [LessonTitle] [nvarchar](200) NOT NULL,
        [LessonDescription] [nvarchar](max) NULL,
        [LessonContent] [nvarchar](max) NULL,
        [RequiredAbility] [float] NULL DEFAULT 0.0,
        [GradeLevel] [int] NOT NULL DEFAULT 4,
        [LessonType] [nvarchar](50) NULL,
        [DateCreated] [datetime] NULL DEFAULT GETDATE(),
        [IsActive] [bit] NULL DEFAULT 1,
    PRIMARY KEY CLUSTERED ([LessonID] ASC)
    ) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY];
    PRINT 'Created Lessons table';
END
GO

-- Check if GameResults table exists, if not create it
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'GameResults')
BEGIN
    CREATE TABLE [dbo].[GameResults](
        [GameResultID] [int] IDENTITY(1,1) NOT NULL,
        [SessionID] [int] NULL,
        [StudentID] [int] NULL,
        [GameType] [nvarchar](50) NOT NULL,
        [Score] [int] NOT NULL,
        [AccuracyPercentage] [float] NULL,
        [TimeCompleted] [int] NULL,
        [XPEarned] [int] NULL,
        [StreakAchieved] [int] NULL,
        [LessonID] [int] NULL,
        [DatePlayed] [datetime] NULL DEFAULT GETDATE(),
    PRIMARY KEY CLUSTERED ([GameResultID] ASC)
    ) ON [PRIMARY];
    PRINT 'Created GameResults table';
END
GO

-- Check if StudentProgress table exists, if not create it
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentProgress')
BEGIN
    CREATE TABLE [dbo].[StudentProgress](
        [ProgressID] [int] IDENTITY(1,1) NOT NULL,
        [StudentID] [int] NOT NULL,
        [LessonID] [int] NOT NULL,
        [CompletionStatus] [nvarchar](50) NULL DEFAULT 'NotStarted',
        [Score] [float] NULL,
        [LastAttemptDate] [datetime] NULL,
    PRIMARY KEY CLUSTERED ([ProgressID] ASC)
    ) ON [PRIMARY];
    PRINT 'Created StudentProgress table';
END
GO

-- Create LessonGameContent table if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'LessonGameContent')
BEGIN
    CREATE TABLE [dbo].[LessonGameContent](
        [ContentID] [int] IDENTITY(1,1) NOT NULL,
        [LessonID] [int] NOT NULL,
        [GameType] [nvarchar](50) NOT NULL, -- SentenceScramble, TimedTrail, WordHunt, ShadowRead, MinimalPairs
        [ContentText] [nvarchar](500) NOT NULL, -- The sentence/word/passage
        [ContentData] [nvarchar](max) NULL, -- JSON for additional data (scrambled words, choices, etc.)
        [Difficulty] [float] NOT NULL DEFAULT 1.0,
        [Category] [nvarchar](50) NULL, -- Simple, Compound, Complex, etc.
        [AudioURL] [nvarchar](500) NULL, -- For pronunciation games
        [ImageURL] [nvarchar](500) NULL, -- For visual games
        [IsActive] [bit] NOT NULL DEFAULT 1,
        [CreatedAt] [datetime] NOT NULL DEFAULT GETDATE(),
    PRIMARY KEY CLUSTERED ([ContentID] ASC)
    ) ON [PRIMARY];
    PRINT 'Created LessonGameContent table';
END
GO

-- Add foreign key to Lessons table if not exists
IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_LessonGameContent_Lessons')
BEGIN
    ALTER TABLE [dbo].[LessonGameContent]
    ADD CONSTRAINT FK_LessonGameContent_Lessons
    FOREIGN KEY (LessonID) REFERENCES [dbo].[Lessons](LessonID);
    PRINT 'Added FK_LessonGameContent_Lessons constraint';
END
GO

-- Add LessonID to GameResults if not exists
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[GameResults]') AND name = 'LessonID')
BEGIN
    ALTER TABLE [dbo].[GameResults] ADD [LessonID] [int] NULL;
    PRINT 'Added LessonID column to GameResults';
END
GO

-- Create index for faster lookups if not exists
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_LessonGameContent_LessonID_GameType')
BEGIN
    CREATE NONCLUSTERED INDEX IX_LessonGameContent_LessonID_GameType
    ON [dbo].[LessonGameContent] ([LessonID], [GameType])
    WHERE IsActive = 1;
    PRINT 'Created index IX_LessonGameContent_LessonID_GameType';
END
GO

-- =============================================
-- Stored Procedure: Get Game Content by Lesson
-- =============================================
CREATE OR ALTER PROCEDURE [dbo].[SP_GetLessonGameContent]
    @LessonID INT,
    @GameType NVARCHAR(50),
    @Count INT = 10
AS
BEGIN
    SET NOCOUNT ON;

    SELECT TOP (@Count)
        ContentID,
        LessonID,
        GameType,
        ContentText,
        ContentData,
        Difficulty,
        Category,
        AudioURL,
        ImageURL
    FROM LessonGameContent
    WHERE LessonID = @LessonID
      AND GameType = @GameType
      AND IsActive = 1
    ORDER BY NEWID(); -- Random order
END
GO

-- =============================================
-- Stored Procedure: Save Game Result with XP Update
-- =============================================
CREATE OR ALTER PROCEDURE [dbo].[SP_SaveGameResult]
    @SessionID INT = NULL,
    @StudentID INT,
    @GameType NVARCHAR(50),
    @Score INT,
    @AccuracyPercentage FLOAT = NULL,
    @TimeCompleted INT = NULL,
    @XPEarned INT = 0,
    @StreakAchieved INT = 0,
    @LessonID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Insert game result
        INSERT INTO GameResults (
            SessionID, StudentID, GameType, Score,
            AccuracyPercentage, TimeCompleted, XPEarned,
            StreakAchieved, LessonID, DatePlayed
        )
        VALUES (
            @SessionID, @StudentID, @GameType, @Score,
            @AccuracyPercentage, @TimeCompleted, @XPEarned,
            @StreakAchieved, @LessonID, GETDATE()
        );

        -- Update student's TotalXP
        UPDATE Students
        SET TotalXP = ISNULL(TotalXP, 0) + @XPEarned
        WHERE StudentID = @StudentID;

        -- Update streak if higher than current
        IF @StreakAchieved > 0
        BEGIN
            UPDATE Students
            SET CurrentStreak = CASE
                    WHEN @StreakAchieved > ISNULL(CurrentStreak, 0) THEN @StreakAchieved
                    ELSE CurrentStreak
                END,
                LongestStreak = CASE
                    WHEN @StreakAchieved > ISNULL(LongestStreak, 0) THEN @StreakAchieved
                    ELSE LongestStreak
                END
            WHERE StudentID = @StudentID;
        END

        -- Update StudentProgress for this lesson
        IF @LessonID IS NOT NULL
        BEGIN
            IF EXISTS (SELECT 1 FROM StudentProgress WHERE StudentID = @StudentID AND LessonID = @LessonID)
            BEGIN
                UPDATE StudentProgress
                SET Score = CASE WHEN @AccuracyPercentage > ISNULL(Score, 0) THEN @AccuracyPercentage ELSE Score END,
                    CompletionStatus = 'Completed',
                    LastAttemptDate = GETDATE()
                WHERE StudentID = @StudentID AND LessonID = @LessonID;
            END
            ELSE
            BEGIN
                INSERT INTO StudentProgress (StudentID, LessonID, CompletionStatus, Score, LastAttemptDate)
                VALUES (@StudentID, @LessonID, 'Completed', @AccuracyPercentage, GETDATE());
            END
        END

        COMMIT TRANSACTION;

        -- Return updated student stats
        SELECT TotalXP, CurrentStreak, LongestStreak
        FROM Students
        WHERE StudentID = @StudentID;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

-- =============================================
-- Insert Sample Lessons if none exist
-- =============================================
IF NOT EXISTS (SELECT 1 FROM Lessons)
BEGIN
    INSERT INTO Lessons (LessonTitle, LessonDescription, GradeLevel, LessonType, RequiredAbility, IsActive)
    VALUES
    ('Basic Sentence Structure', 'Learn how to form simple sentences with subject and predicate.', 4, 'Grammar', 0.3, 1),
    ('Simple Sentences', 'Practice arranging words to create meaningful simple sentences.', 4, 'Grammar', 0.4, 1),
    ('Compound Sentences', 'Learn to combine ideas using conjunctions.', 5, 'Grammar', 0.6, 1),
    ('Complex Sentences', 'Master complex sentence structures with dependent clauses.', 5, 'Grammar', 0.8, 1),
    ('Advanced Grammar', 'Challenge yourself with advanced sentence patterns.', 6, 'Grammar', 1.0, 1);
    PRINT 'Inserted sample lessons';
END
GO

-- =============================================
-- Sample Data: Insert game content for lessons
-- =============================================
DECLARE @Lesson1 INT, @Lesson2 INT, @Lesson3 INT, @Lesson4 INT, @Lesson5 INT;

-- Get lesson IDs
SELECT @Lesson1 = LessonID FROM Lessons WHERE LessonTitle = 'Basic Sentence Structure';
SELECT @Lesson2 = LessonID FROM Lessons WHERE LessonTitle = 'Simple Sentences';
SELECT @Lesson3 = LessonID FROM Lessons WHERE LessonTitle = 'Compound Sentences';
SELECT @Lesson4 = LessonID FROM Lessons WHERE LessonTitle = 'Complex Sentences';
SELECT @Lesson5 = LessonID FROM Lessons WHERE LessonTitle = 'Advanced Grammar';

-- Only insert if no content exists
IF NOT EXISTS (SELECT 1 FROM LessonGameContent)
BEGIN
    -- Lesson 1: Basic Sentence Structure (Grade 4 - Easy)
    IF @Lesson1 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, Difficulty, Category)
        VALUES
        (@Lesson1, 'SentenceScramble', 'The cat sat on the mat', 0.4, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'A dog runs fast', 0.3, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'She reads a book', 0.3, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'He plays in the park', 0.4, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'Birds fly in the sky', 0.4, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'I like to eat apples', 0.5, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'The sun is very bright', 0.5, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'My mother cooks food', 0.4, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'We go to school daily', 0.5, 'Simple'),
        (@Lesson1, 'SentenceScramble', 'The baby is sleeping', 0.4, 'Simple');
        PRINT 'Inserted content for Lesson 1';
    END

    -- Lesson 2: Simple Sentences (Grade 4 - Easy-Medium)
    IF @Lesson2 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, Difficulty, Category)
        VALUES
        (@Lesson2, 'SentenceScramble', 'She goes to school every day', 0.6, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'The dog runs in the park', 0.5, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'My mother cooks delicious food', 0.6, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'The children play happily together', 0.7, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'Father drives to work early', 0.6, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'The flowers bloom in spring', 0.6, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'Students study hard for exams', 0.7, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'The moon shines at night', 0.5, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'Rain falls from the clouds', 0.6, 'Simple'),
        (@Lesson2, 'SentenceScramble', 'The teacher helps her students', 0.6, 'Simple');
        PRINT 'Inserted content for Lesson 2';
    END

    -- Lesson 3: Compound Sentences (Grade 5 - Medium)
    IF @Lesson3 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, Difficulty, Category)
        VALUES
        (@Lesson3, 'SentenceScramble', 'Maria finished her homework diligently', 0.8, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'The students are reading their books quietly', 0.9, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'The teacher explained the lesson clearly', 0.8, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'We visited the beautiful museum yesterday', 0.9, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'The quick brown fox jumps over the lazy dog', 1.0, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'My sister enjoys playing the piano daily', 0.8, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'The brave firefighter saved the little kitten', 0.9, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'Our class went on an exciting field trip', 0.8, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'The talented artist painted a beautiful landscape', 0.9, 'Compound'),
        (@Lesson3, 'SentenceScramble', 'Children should always respect their elders', 0.8, 'Compound');
        PRINT 'Inserted content for Lesson 3';
    END

    -- Lesson 4: Complex Sentences (Grade 5-6 - Medium-Hard)
    IF @Lesson4 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, Difficulty, Category)
        VALUES
        (@Lesson4, 'SentenceScramble', 'Reading books regularly helps improve vocabulary skills', 1.2, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'My family and I visited the science museum yesterday', 1.3, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'The beautiful butterfly landed gently on the colorful flower', 1.2, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'Learning new words makes reading more enjoyable and interesting', 1.4, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'The hardworking students completed their challenging project successfully', 1.5, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'Ancient civilizations built magnificent structures that still stand today', 1.3, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'Scientists discovered an important breakthrough in medical research', 1.4, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'The young explorer embarked on an adventurous journey abroad', 1.2, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'Environmental conservation requires active participation from everyone', 1.4, 'Complex'),
        (@Lesson4, 'SentenceScramble', 'Technology has dramatically transformed how we communicate daily', 1.3, 'Complex');
        PRINT 'Inserted content for Lesson 4';
    END

    -- Lesson 5: Advanced Grammar (Grade 6 - Hard)
    IF @Lesson5 IS NOT NULL
    BEGIN
        INSERT INTO LessonGameContent (LessonID, GameType, ContentText, Difficulty, Category)
        VALUES
        (@Lesson5, 'SentenceScramble', 'Despite the challenging circumstances the team persevered remarkably', 1.6, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'The prestigious university offers exceptional academic programs worldwide', 1.7, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'Sustainable development requires balancing economic growth with environmental protection', 1.8, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'Historical documents provide valuable insights into past civilizations', 1.6, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'Collaborative learning encourages students to share diverse perspectives effectively', 1.7, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'The innovative entrepreneur revolutionized the technology industry significantly', 1.8, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'Critical thinking skills enable individuals to analyze information objectively', 1.7, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'Biodiversity conservation protects countless species from potential extinction', 1.8, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'Effective communication bridges cultural differences and fosters understanding', 1.6, 'Advanced'),
        (@Lesson5, 'SentenceScramble', 'Mathematical reasoning develops logical problem-solving abilities systematically', 1.7, 'Advanced');
        PRINT 'Inserted content for Lesson 5';
    END
END
GO

PRINT 'Script completed successfully!';
GO
