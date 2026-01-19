-- =====================================================
-- ADAPTIVE MODULE SYSTEM MIGRATION
-- Option A: IRT for Placement, Performance-Based Modules
-- =====================================================

-- =====================================================
-- 1. UPDATE STUDENTS TABLE
-- =====================================================

-- Add placement columns (if not already exists from previous migration)
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Students') AND name = 'PlacementLevel')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD PlacementLevel INT CHECK (PlacementLevel BETWEEN 1 AND 3);
END

IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Students') AND name = 'PlacementTheta')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD PlacementTheta FLOAT,
        PlacementDate DATETIME;
END

-- Add current learning state columns
IF NOT EXISTS (SELECT 1 FROM sys.columns WHERE object_id = OBJECT_ID('dbo.Students') AND name = 'CurrentModuleID')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD CurrentModuleID INT,
        CurrentNodeID INT,
        TotalXP INT DEFAULT 0,
        CurrentLevel INT DEFAULT 1,
        CurrentStreak INT DEFAULT 0,
        LongestStreak INT DEFAULT 0,
        LastActivityDate DATETIME,
        UpdatedDate DATETIME DEFAULT GETDATE();
END

-- =====================================================
-- 2. CREATE MODULES TABLE
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'Modules')
BEGIN
    CREATE TABLE [dbo].[Modules] (
        ModuleID INT PRIMARY KEY IDENTITY(1,1),
        ModuleName NVARCHAR(100) NOT NULL,
        ModuleCode NVARCHAR(20),
        CategoryMapping INT, -- Maps to Cat1-Cat5 from placement
        OrderIndex INT NOT NULL,
        TotalNodes INT DEFAULT 13,
        Description NVARCHAR(500),
        IsActive BIT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE()
    );

    -- Insert 5 MATATAG modules
    INSERT INTO [dbo].[Modules] (ModuleName, ModuleCode, CategoryMapping, OrderIndex, Description) VALUES
    ('Phonics and Word Study', 'EN3PWS', 1, 1, 'Master foundational phonics skills and word recognition'),
    ('Vocabulary and Word Knowledge', 'EN3VWK', 2, 2, 'Expand vocabulary and word usage skills'),
    ('Grammar Awareness and Grammatical Structures', 'EN3GAGS', 3, 3, 'Understand grammar rules and sentence structures'),
    ('Comprehending and Analyzing Text', 'EN3CAT', 4, 4, 'Develop reading comprehension and analysis skills'),
    ('Creating and Composing Text', 'EN3CCT', 5, 5, 'Build creative and expository writing abilities');
END

-- =====================================================
-- 3. CREATE NODES TABLE (Curriculum-Locked Lessons)
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'Nodes')
BEGIN
    CREATE TABLE [dbo].[Nodes] (
        NodeID INT PRIMARY KEY IDENTITY(1,1),
        ModuleID INT FOREIGN KEY REFERENCES [dbo].[Modules](ModuleID),
        NodeType NVARCHAR(20) NOT NULL CHECK (NodeType IN ('CORE_LESSON', 'FINAL_ASSESSMENT')),
        NodeNumber INT NOT NULL, -- 1-13 within module
        Quarter INT CHECK (Quarter BETWEEN 1 AND 4), -- NULL for final assessment

        -- Curriculum Content (Fixed)
        LessonTitle NVARCHAR(200),
        LearningObjectives NVARCHAR(MAX),
        ContentJSON NVARCHAR(MAX), -- JSON with lesson content

        -- Metadata
        SkillCategory NVARCHAR(50), -- Phonics, Vocabulary, Grammar, etc.
        EstimatedDuration INT, -- Minutes
        XPReward INT DEFAULT 20,

        IsActive BIT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE(),

        CONSTRAINT UQ_Module_NodeNumber UNIQUE (ModuleID, NodeNumber)
    );

    CREATE INDEX IX_Nodes_Module ON [dbo].[Nodes](ModuleID, NodeNumber);
END

-- =====================================================
-- 4. CREATE GAMES TABLE
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'Games')
BEGIN
    CREATE TABLE [dbo].[Games] (
        GameID INT PRIMARY KEY IDENTITY(1,1),
        GameType NVARCHAR(50) NOT NULL,
        GameName NVARCHAR(100),
        Description NVARCHAR(500),
        SkillReinforced NVARCHAR(50),

        -- Difficulty variants
        HasEasyMode BIT DEFAULT 1,
        HasMediumMode BIT DEFAULT 1,
        HasHardMode BIT DEFAULT 1,

        ConfigJSON NVARCHAR(MAX),
        IsActive BIT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE()
    );

    -- Insert game types
    INSERT INTO [dbo].[Games] (GameType, GameName, SkillReinforced, HasEasyMode, HasMediumMode, HasHardMode) VALUES
    ('word_hunt', 'Word Hunt', 'Phonics', 1, 1, 1),
    ('sentence_scramble', 'Sentence Scramble', 'Grammar', 1, 1, 1),
    ('picture_match', 'Picture Match', 'Vocabulary', 1, 1, 1),
    ('minimal_pairs', 'Sound Pairs', 'Phonics', 1, 1, 1),
    ('dialogue_reading', 'Dialogue Practice', 'Reading', 1, 1, 1),
    ('timed_trail', 'Timed Trail', 'Fluency', 1, 1, 1),
    ('story_sequencing', 'Story Order', 'Comprehension', 1, 1, 1),
    ('fill_blanks', 'Fill the Blanks', 'Grammar', 1, 1, 1);
END

-- =====================================================
-- 5. CREATE NODE-GAME MAPPING
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'NodeGameMapping')
BEGIN
    CREATE TABLE [dbo].[NodeGameMapping] (
        MappingID INT PRIMARY KEY IDENTITY(1,1),
        NodeID INT FOREIGN KEY REFERENCES [dbo].[Nodes](NodeID),
        GameID INT FOREIGN KEY REFERENCES [dbo].[Games](GameID),
        IsRequired BIT DEFAULT 1,
        OrderIndex INT,
        CreatedDate DATETIME DEFAULT GETDATE()
    );

    CREATE INDEX IX_NodeGameMapping_Node ON [dbo].[NodeGameMapping](NodeID);
END

-- =====================================================
-- 6. CREATE QUIZ QUESTIONS TABLE
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'QuizQuestions')
BEGIN
    CREATE TABLE [dbo].[QuizQuestions] (
        QuestionID INT PRIMARY KEY IDENTITY(1,1),
        NodeID INT FOREIGN KEY REFERENCES [dbo].[Nodes](NodeID),
        QuestionText NVARCHAR(MAX) NOT NULL,
        QuestionType NVARCHAR(50), -- multiple_choice, pronunciation, reading_comprehension
        OptionsJSON NVARCHAR(MAX), -- JSON array of options
        CorrectAnswer NVARCHAR(10), -- A, B, C, D

        -- For analytics (not used for ongoing IRT)
        EstimatedDifficulty NVARCHAR(20), -- Easy, Medium, Hard (qualitative)

        SkillCategory NVARCHAR(50),
        MediaURL NVARCHAR(500),
        ReadingPassage NVARCHAR(MAX),

        IsActive BIT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE()
    );

    CREATE INDEX IX_QuizQuestions_Node ON [dbo].[QuizQuestions](NodeID);
END

-- =====================================================
-- 7. CREATE SUPPLEMENTAL NODES TABLE
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'SupplementalNodes')
BEGIN
    CREATE TABLE [dbo].[SupplementalNodes] (
        SupplementalNodeID INT PRIMARY KEY IDENTITY(1,1),
        NodeType NVARCHAR(20) NOT NULL CHECK (NodeType IN ('SUPPLEMENTAL', 'INTERVENTION', 'ENRICHMENT')),

        AfterNodeID INT FOREIGN KEY REFERENCES [dbo].[Nodes](NodeID),

        -- Trigger Rules (Simple, Score-Based)
        TriggerLogic NVARCHAR(500),

        -- Content
        Title NVARCHAR(200),
        ContentJSON NVARCHAR(MAX),
        SkillCategory NVARCHAR(50),
        EstimatedDuration INT,
        XPReward INT DEFAULT 10,

        IsActive BIT DEFAULT 1,
        CreatedDate DATETIME DEFAULT GETDATE()
    );

    CREATE INDEX IX_SupplementalNodes_AfterNode ON [dbo].[SupplementalNodes](AfterNodeID);
END

-- =====================================================
-- 8. CREATE STUDENT PROGRESS TABLES
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'StudentNodeProgress')
BEGIN
    CREATE TABLE [dbo].[StudentNodeProgress] (
        ProgressID INT PRIMARY KEY IDENTITY(1,1),
        StudentID INT FOREIGN KEY REFERENCES [dbo].[Students](StudentID),
        NodeID INT FOREIGN KEY REFERENCES [dbo].[Nodes](NodeID),

        -- Simple State Machine
        NodeState NVARCHAR(20) DEFAULT 'LOCKED' CHECK (NodeState IN ('LOCKED', 'UNLOCKED', 'IN_PROGRESS', 'COMPLETED', 'MASTERED')),

        -- Performance Tracking (No Theta)
        AttemptCount INT DEFAULT 0,
        BestQuizScore INT DEFAULT 0,
        LatestQuizScore INT DEFAULT 0,
        AverageQuizScore FLOAT,

        -- Phases Completed
        LessonCompleted BIT DEFAULT 0,
        GameCompleted BIT DEFAULT 0,
        QuizCompleted BIT DEFAULT 0,

        -- Timestamps
        UnlockedDate DATETIME,
        CompletedDate DATETIME,
        LastAttemptDate DATETIME,
        CreatedDate DATETIME DEFAULT GETDATE(),

        CONSTRAINT UQ_Student_Node UNIQUE (StudentID, NodeID)
    );

    CREATE INDEX IX_StudentNodeProgress_Student ON [dbo].[StudentNodeProgress](StudentID);
    CREATE INDEX IX_StudentNodeProgress_State ON [dbo].[StudentNodeProgress](StudentID, NodeState);
END

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'StudentSupplementalProgress')
BEGIN
    CREATE TABLE [dbo].[StudentSupplementalProgress] (
        SupplementalProgressID INT PRIMARY KEY IDENTITY(1,1),
        StudentID INT FOREIGN KEY REFERENCES [dbo].[Students](StudentID),
        SupplementalNodeID INT FOREIGN KEY REFERENCES [dbo].[SupplementalNodes](SupplementalNodeID),

        -- Visibility Control
        IsVisible BIT DEFAULT 1,
        TriggerReason NVARCHAR(500),

        -- Completion
        IsCompleted BIT DEFAULT 0,
        CompletedDate DATETIME,

        CreatedDate DATETIME DEFAULT GETDATE(),

        CONSTRAINT UQ_Student_Supplemental UNIQUE (StudentID, SupplementalNodeID)
    );

    CREATE INDEX IX_StudentSupplementalProgress_Student ON [dbo].[StudentSupplementalProgress](StudentID, IsVisible);
END

-- =====================================================
-- 9. CREATE QUIZ ATTEMPTS TABLE
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'QuizAttempts')
BEGIN
    CREATE TABLE [dbo].[QuizAttempts] (
        AttemptID INT PRIMARY KEY IDENTITY(1,1),
        StudentID INT FOREIGN KEY REFERENCES [dbo].[Students](StudentID),
        NodeID INT FOREIGN KEY REFERENCES [dbo].[Nodes](NodeID),

        AttemptNumber INT NOT NULL,

        -- Simple Performance Metrics
        Score INT NOT NULL CHECK (Score BETWEEN 0 AND 100),
        TotalQuestions INT NOT NULL,
        CorrectAnswers INT NOT NULL,

        TimeSpentSeconds INT,
        Passed BIT, -- Score >= 70

        CompletedDate DATETIME DEFAULT GETDATE()
    );

    CREATE INDEX IX_QuizAttempts_Student_Node ON [dbo].[QuizAttempts](StudentID, NodeID, CompletedDate DESC);
END

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'QuizResponses')
BEGIN
    CREATE TABLE [dbo].[QuizResponses] (
        ResponseID INT PRIMARY KEY IDENTITY(1,1),
        AttemptID INT FOREIGN KEY REFERENCES [dbo].[QuizAttempts](AttemptID),
        QuestionID INT FOREIGN KEY REFERENCES [dbo].[QuizQuestions](QuestionID),

        SelectedAnswer NVARCHAR(10),
        IsCorrect BIT,
        ResponseTimeSeconds INT,

        CreatedDate DATETIME DEFAULT GETDATE()
    );

    CREATE INDEX IX_QuizResponses_Attempt ON [dbo].[QuizResponses](AttemptID);
END

-- =====================================================
-- 10. CREATE GAME SESSIONS TABLE
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'GameSessions')
BEGIN
    CREATE TABLE [dbo].[GameSessions] (
        SessionID INT PRIMARY KEY IDENTITY(1,1),
        StudentID INT FOREIGN KEY REFERENCES [dbo].[Students](StudentID),
        NodeID INT FOREIGN KEY REFERENCES [dbo].[Nodes](NodeID),
        GameID INT FOREIGN KEY REFERENCES [dbo].[Games](GameID),

        Difficulty NVARCHAR(20) CHECK (Difficulty IN ('EASY', 'MEDIUM', 'HARD')),

        Score INT,
        Accuracy INT,
        XPEarned INT,
        TimeSpentSeconds INT,

        CompletedDate DATETIME DEFAULT GETDATE()
    );

    CREATE INDEX IX_GameSessions_Student_Node ON [dbo].[GameSessions](StudentID, NodeID, CompletedDate DESC);
END

-- =====================================================
-- 11. CREATE ADAPTIVE DECISIONS TABLE
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'AdaptiveDecisions')
BEGIN
    CREATE TABLE [dbo].[AdaptiveDecisions] (
        DecisionID INT PRIMARY KEY IDENTITY(1,1),
        StudentID INT FOREIGN KEY REFERENCES [dbo].[Students](StudentID),
        NodeID INT FOREIGN KEY REFERENCES [dbo].[Nodes](NodeID),

        DecisionType NVARCHAR(50) CHECK (DecisionType IN (
            'PROCEED',
            'ADD_SUPPLEMENTAL',
            'ADD_INTERVENTION',
            'OFFER_ENRICHMENT',
            'RETRY_REQUIRED'
        )),

        Reason NVARCHAR(500),
        QuizScore INT,
        AttemptCount INT,
        PlacementLevel INT,
        RecentScoresTrend NVARCHAR(50), -- IMPROVING, DECLINING, STABLE

        Timestamp DATETIME DEFAULT GETDATE()
    );

    CREATE INDEX IX_AdaptiveDecisions_Student ON [dbo].[AdaptiveDecisions](StudentID, Timestamp DESC);
END

-- =====================================================
-- 12. CREATE BADGES TABLE
-- =====================================================

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'Badges')
BEGIN
    CREATE TABLE [dbo].[Badges] (
        BadgeID INT PRIMARY KEY IDENTITY(1,1),
        BadgeName NVARCHAR(100) NOT NULL,
        Description NVARCHAR(500),
        IconName NVARCHAR(50),
        Requirement NVARCHAR(200),
        XPBonus INT DEFAULT 0,
        CreatedDate DATETIME DEFAULT GETDATE()
    );

    INSERT INTO [dbo].[Badges] (BadgeName, Description, IconName, Requirement, XPBonus) VALUES
    ('First Steps', 'Complete your first lesson', 'badge_first_steps', 'Complete 1 lesson', 10),
    ('Module Master', 'Complete an entire module', 'badge_module_master', 'Complete 1 module', 50),
    ('Perfect Score', 'Score 100% on a quiz', 'badge_perfect', 'Get 100% on any quiz', 25),
    ('Dedicated Learner', 'Maintain a 7-day streak', 'badge_streak', 'Login 7 days in a row', 30),
    ('Champion', 'Complete all 5 modules', 'badge_champion', 'Complete all modules', 100),
    ('Fast Learner', 'Complete 5 lessons in one day', 'badge_fast', 'Complete 5 lessons in 1 day', 40),
    ('Resilient', 'Pass a quiz after 2 failed attempts', 'badge_resilient', 'Pass after failing twice', 20),
    ('Word Wizard', 'Master 100 vocabulary words', 'badge_word_wizard', 'Master 100 words', 50),
    ('Grammar Guru', 'Complete all grammar lessons with 90%+', 'badge_grammar', 'Grammar mastery', 60),
    ('Reading Star', 'Complete all reading lessons with 90%+', 'badge_reading', 'Reading mastery', 60);
END

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'StudentBadges')
BEGIN
    CREATE TABLE [dbo].[StudentBadges] (
        StudentBadgeID INT PRIMARY KEY IDENTITY(1,1),
        StudentID INT FOREIGN KEY REFERENCES [dbo].[Students](StudentID),
        BadgeID INT FOREIGN KEY REFERENCES [dbo].[Badges](BadgeID),
        EarnedDate DATETIME DEFAULT GETDATE(),
        CONSTRAINT UQ_Student_Badge UNIQUE (StudentID, BadgeID)
    );

    CREATE INDEX IX_StudentBadges_Student ON [dbo].[StudentBadges](StudentID);
END

-- =====================================================
-- MIGRATION COMPLETE
-- =====================================================

PRINT 'Adaptive Module System Migration Completed Successfully';
PRINT 'Tables Created:';
PRINT '  - Modules (5 MATATAG modules)';
PRINT '  - Nodes (13 nodes per module: 12 lessons + 1 assessment)';
PRINT '  - Games (8 game types)';
PRINT '  - QuizQuestions';
PRINT '  - SupplementalNodes';
PRINT '  - StudentNodeProgress';
PRINT '  - StudentSupplementalProgress';
PRINT '  - QuizAttempts & QuizResponses';
PRINT '  - GameSessions';
PRINT '  - AdaptiveDecisions';
PRINT '  - Badges & StudentBadges';
PRINT '';
PRINT 'Next Steps:';
PRINT '  1. Populate Nodes table with 13 nodes per module (12 lessons + 1 assessment)';
PRINT '  2. Add QuizQuestions for each node';
PRINT '  3. Map games to nodes via NodeGameMapping';
PRINT '  4. Create supplemental nodes with trigger logic';
