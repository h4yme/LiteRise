-- ============================================================================
-- LiteRise Adaptive Learning System - Database Schema Updates
-- Version: 2.0 - Adaptive Branching with IRT Integration
-- Timeline: 2-3 days implementation
-- ============================================================================

USE LiteRiseDB;
GO

-- ============================================================================
-- PHASE 1: Update Existing Tables
-- ============================================================================

-- Update Lessons table structure
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Lessons' AND COLUMN_NAME = 'Quarter')
BEGIN
    ALTER TABLE Lessons ADD Quarter INT NULL; -- 1-4 for quarterly distribution
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Lessons' AND COLUMN_NAME = 'LessonNumber')
BEGIN
    ALTER TABLE Lessons ADD LessonNumber INT NULL; -- 1-12 per module
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Lessons' AND COLUMN_NAME = 'GameType')
BEGIN
    ALTER TABLE Lessons ADD GameType VARCHAR(50) NULL; -- 'word_hunt', 'sentence_scramble', etc.
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Lessons' AND COLUMN_NAME = 'InterventionThreshold')
BEGIN
    ALTER TABLE Lessons ADD InterventionThreshold INT DEFAULT 60; -- Quiz score to unlock intervention
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Lessons' AND COLUMN_NAME = 'EnrichmentThreshold')
BEGIN
    ALTER TABLE Lessons ADD EnrichmentThreshold INT DEFAULT 85; -- Quiz score to unlock enrichment
END
GO

-- Update StudentProgress table
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'StudentProgress' AND COLUMN_NAME = 'QuizScore')
BEGIN
    ALTER TABLE StudentProgress ADD QuizScore INT NULL; -- 0-100
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'StudentProgress' AND COLUMN_NAME = 'GameScore')
BEGIN
    ALTER TABLE StudentProgress ADD GameScore INT NULL; -- Points from game
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'StudentProgress' AND COLUMN_NAME = 'XPEarned')
BEGIN
    ALTER TABLE StudentProgress ADD XPEarned INT DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'StudentProgress' AND COLUMN_NAME = 'Attempts')
BEGIN
    ALTER TABLE StudentProgress ADD Attempts INT DEFAULT 0;
END
GO

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'StudentProgress' AND COLUMN_NAME = 'InterventionRequired')
BEGIN
    ALTER TABLE StudentProgress ADD InterventionRequired BIT DEFAULT 0;
END
GO

-- ============================================================================
-- PHASE 2: Create New Tables for Branching
-- ============================================================================

-- Table: LessonBranches (Intervention & Enrichment Content)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'LessonBranches')
BEGIN
    CREATE TABLE LessonBranches (
        BranchID INT IDENTITY(1,1) PRIMARY KEY,
        ParentLessonID INT NOT NULL, -- The main lesson this branches from
        BranchType VARCHAR(20) NOT NULL, -- 'intervention' or 'enrichment'
        Title NVARCHAR(255) NOT NULL,
        Description NVARCHAR(MAX),
        ContentType VARCHAR(50), -- 'practice', 'video', 'interactive'
        Difficulty VARCHAR(20), -- 'beginner', 'intermediate', 'advanced'
        RequiredForProgression BIT DEFAULT 0, -- If true, must complete to advance
        EstimatedMinutes INT DEFAULT 10,
        CreatedDate DATETIME DEFAULT GETDATE(),

        FOREIGN KEY (ParentLessonID) REFERENCES Lessons(LessonID),
        CONSTRAINT CK_BranchType CHECK (BranchType IN ('intervention', 'enrichment'))
    );
END
GO

-- Table: StudentBranches (Track completion of intervention/enrichment)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'StudentBranches')
BEGIN
    CREATE TABLE StudentBranches (
        StudentBranchID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL,
        BranchID INT NOT NULL,
        Status VARCHAR(20) DEFAULT 'locked', -- 'locked', 'in_progress', 'completed'
        Score INT NULL, -- Performance on branch content
        StartedDate DATETIME NULL,
        CompletedDate DATETIME NULL,
        AttemptCount INT DEFAULT 0,

        FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
        FOREIGN KEY (BranchID) REFERENCES LessonBranches(BranchID),
        CONSTRAINT UQ_StudentBranch UNIQUE (StudentID, BranchID)
    );
END
GO

-- Table: Modules (Organize lessons into modules)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Modules')
BEGIN
    CREATE TABLE Modules (
        ModuleID INT IDENTITY(1,1) PRIMARY KEY,
        ModuleName NVARCHAR(255) NOT NULL,
        Description NVARCHAR(MAX),
        GradeLevel INT NOT NULL,
        TotalLessons INT DEFAULT 12, -- 12 lessons per module
        OrderIndex INT, -- Display order
        IconName VARCHAR(50),
        ColorCode VARCHAR(7), -- Hex color for UI
        CreatedDate DATETIME DEFAULT GETDATE()
    );
END
GO

-- Update Lessons to reference Modules
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS
               WHERE TABLE_NAME = 'Lessons' AND COLUMN_NAME = 'ModuleID')
BEGIN
    ALTER TABLE Lessons ADD ModuleID INT NULL;
    -- Add foreign key after populating data
END
GO

-- Table: ModuleAssessments (Node 13 - Final Assessment)
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'ModuleAssessments')
BEGIN
    CREATE TABLE ModuleAssessments (
        AssessmentID INT IDENTITY(1,1) PRIMARY KEY,
        ModuleID INT NOT NULL,
        AssessmentTitle NVARCHAR(255) NOT NULL,
        Description NVARCHAR(MAX),
        TotalQuestions INT DEFAULT 10,
        PassingScore INT DEFAULT 70,
        TimeLimit INT DEFAULT 30, -- minutes
        CreatedDate DATETIME DEFAULT GETDATE(),

        FOREIGN KEY (ModuleID) REFERENCES Modules(ModuleID)
    );
END
GO

-- Table: StudentModuleAssessments
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'StudentModuleAssessments')
BEGIN
    CREATE TABLE StudentModuleAssessments (
        StudentAssessmentID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL,
        AssessmentID INT NOT NULL,
        Score INT NULL,
        Passed BIT DEFAULT 0,
        CompletedDate DATETIME NULL,
        TimeSpentMinutes INT NULL,

        FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
        FOREIGN KEY (AssessmentID) REFERENCES ModuleAssessments(AssessmentID)
    );
END
GO

-- ============================================================================
-- PHASE 3: Seed Data - Module 1: Phonics and Word Study
-- ============================================================================

-- Insert Module 1
IF NOT EXISTS (SELECT * FROM Modules WHERE ModuleID = 1)
BEGIN
    SET IDENTITY_INSERT Modules ON;

    INSERT INTO Modules (ModuleID, ModuleName, Description, GradeLevel, TotalLessons, OrderIndex, IconName, ColorCode)
    VALUES (
        1,
        'Phonics and Word Study',
        'Master foundational reading skills through phonics, sight words, and word recognition patterns',
        3, -- Grade 3
        12, -- 12 lessons
        1,
        'ic_phonics',
        '#7C3AED' -- Purple
    );

    SET IDENTITY_INSERT Modules OFF;
END
GO

-- Update existing lessons to assign Module 1 (if lessons already exist)
-- This assumes lessons 101-112 belong to Module 1
UPDATE Lessons
SET ModuleID = 1,
    Quarter = CASE
        WHEN LessonID BETWEEN 101 AND 103 THEN 1
        WHEN LessonID BETWEEN 104 AND 106 THEN 2
        WHEN LessonID BETWEEN 107 AND 109 THEN 3
        WHEN LessonID BETWEEN 110 AND 112 THEN 4
        ELSE NULL
    END,
    LessonNumber = CASE
        WHEN LessonID = 101 THEN 1
        WHEN LessonID = 102 THEN 2
        WHEN LessonID = 103 THEN 3
        WHEN LessonID = 104 THEN 4
        WHEN LessonID = 105 THEN 5
        WHEN LessonID = 106 THEN 6
        WHEN LessonID = 107 THEN 7
        WHEN LessonID = 108 THEN 8
        WHEN LessonID = 109 THEN 9
        WHEN LessonID = 110 THEN 10
        WHEN LessonID = 111 THEN 11
        WHEN LessonID = 112 THEN 12
        ELSE NULL
    END,
    GameType = CASE LessonID
        WHEN 101 THEN 'word_hunt'
        WHEN 102 THEN 'word_hunt'
        WHEN 103 THEN 'sentence_scramble'
        WHEN 104 THEN 'sentence_scramble'
        WHEN 105 THEN 'word_hunt'
        WHEN 106 THEN 'sentence_scramble'
        WHEN 107 THEN 'word_hunt'
        WHEN 108 THEN 'sentence_scramble'
        WHEN 109 THEN 'word_hunt'
        WHEN 110 THEN 'sentence_scramble'
        WHEN 111 THEN 'shadow_read'
        WHEN 112 THEN 'traditional'
        ELSE 'traditional'
    END
WHERE LessonID BETWEEN 101 AND 112;
GO

-- Create Module 1 Assessment (Node 13)
IF NOT EXISTS (SELECT * FROM ModuleAssessments WHERE ModuleID = 1)
BEGIN
    INSERT INTO ModuleAssessments (ModuleID, AssessmentTitle, Description, TotalQuestions, PassingScore, TimeLimit)
    VALUES (
        1,
        'Phonics Mastery Assessment',
        'Comprehensive assessment covering all phonics and word study concepts from Quarter 1-4',
        15,
        75, -- 75% to pass
        25 -- 25 minutes
    );
END
GO

-- ============================================================================
-- PHASE 4: Sample Intervention & Enrichment Branches
-- ============================================================================

-- Intervention for Lesson 1 (Beginner level)
INSERT INTO LessonBranches (ParentLessonID, BranchType, Title, Description, ContentType, Difficulty, RequiredForProgression, EstimatedMinutes)
VALUES
(101, 'intervention', 'Sight Words Foundations', 'Extra practice with basic sight words using flashcards and audio support', 'interactive', 'beginner', 1, 10),
(101, 'intervention', 'Letter Sound Review', 'Review individual letter sounds before learning sight words', 'interactive', 'beginner', 1, 8);
GO

-- Enrichment for Lesson 1 (Advanced level)
INSERT INTO LessonBranches (ParentLessonID, BranchType, Title, Description, ContentType, Difficulty, RequiredForProgression, EstimatedMinutes)
VALUES
(101, 'enrichment', 'Advanced Sight Words', 'Challenge yourself with Grade 4+ sight words', 'interactive', 'advanced', 0, 12),
(101, 'enrichment', 'Sight Words in Context', 'Use sight words to write complete sentences', 'interactive', 'advanced', 0, 15);
GO

-- Intervention for Lesson 3 (Sentence Scramble)
INSERT INTO LessonBranches (ParentLessonID, BranchType, Title, Description, ContentType, Difficulty, RequiredForProgression, EstimatedMinutes)
VALUES
(103, 'intervention', 'Sentence Building Basics', 'Learn the parts of a sentence before scrambling', 'video', 'beginner', 1, 12),
(103, 'intervention', 'Word Order Practice', 'Practice putting words in the correct order with guidance', 'interactive', 'beginner', 1, 10);
GO

-- ============================================================================
-- PHASE 5: Stored Procedures for Adaptive Branching
-- ============================================================================

-- SP: Check if intervention should be unlocked
CREATE OR ALTER PROCEDURE SP_CheckInterventionUnlock
    @StudentID INT,
    @LessonID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @QuizScore INT;
    DECLARE @InterventionThreshold INT;

    -- Get student's quiz score
    SELECT @QuizScore = QuizScore
    FROM StudentProgress
    WHERE StudentID = @StudentID AND LessonID = @LessonID;

    -- Get lesson's intervention threshold
    SELECT @InterventionThreshold = InterventionThreshold
    FROM Lessons
    WHERE LessonID = @LessonID;

    -- Return intervention branches if score below threshold
    IF @QuizScore < @InterventionThreshold OR @QuizScore IS NULL
    BEGIN
        SELECT
            lb.*,
            ISNULL(sb.Status, 'locked') AS StudentStatus,
            ISNULL(sb.Score, 0) AS StudentScore
        FROM LessonBranches lb
        LEFT JOIN StudentBranches sb ON lb.BranchID = sb.BranchID AND sb.StudentID = @StudentID
        WHERE lb.ParentLessonID = @LessonID
        AND lb.BranchType = 'intervention'
        ORDER BY lb.BranchID;
    END
END
GO

-- SP: Check if enrichment should be unlocked
CREATE OR ALTER PROCEDURE SP_CheckEnrichmentUnlock
    @StudentID INT,
    @LessonID INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @QuizScore INT;
    DECLARE @EnrichmentThreshold INT;

    -- Get student's quiz score
    SELECT @QuizScore = QuizScore
    FROM StudentProgress
    WHERE StudentID = @StudentID AND LessonID = @LessonID;

    -- Get lesson's enrichment threshold
    SELECT @EnrichmentThreshold = EnrichmentThreshold
    FROM Lessons
    WHERE LessonID = @LessonID;

    -- Return enrichment branches if score above threshold
    IF @QuizScore >= @EnrichmentThreshold
    BEGIN
        SELECT
            lb.*,
            ISNULL(sb.Status, 'locked') AS StudentStatus,
            ISNULL(sb.Score, 0) AS StudentScore
        FROM LessonBranches lb
        LEFT JOIN StudentBranches sb ON lb.BranchID = sb.BranchID AND sb.StudentID = @StudentID
        WHERE lb.ParentLessonID = @LessonID
        AND lb.BranchType = 'enrichment'
        ORDER BY lb.BranchID;
    END
END
GO

-- SP: Get Module Structure with Branching
CREATE OR ALTER PROCEDURE SP_GetModuleStructure
    @StudentID INT,
    @ModuleID INT
AS
BEGIN
    SET NOCOUNT ON;

    -- Get student's ability level
    DECLARE @StudentAbility FLOAT;
    SELECT @StudentAbility = CurrentAbility FROM Students WHERE StudentID = @StudentID;

    -- Classify ability
    DECLARE @ProficiencyLevel VARCHAR(20);
    SET @ProficiencyLevel = CASE
        WHEN @StudentAbility < -1.0 THEN 'beginner'
        WHEN @StudentAbility > 1.0 THEN 'advanced'
        ELSE 'intermediate'
    END;

    -- Get lessons with progress and branching info
    SELECT
        l.LessonID,
        l.LessonTitle,
        l.LessonDescription,
        l.Quarter,
        l.LessonNumber,
        l.GameType,
        l.RequiredAbility,
        l.InterventionThreshold,
        l.EnrichmentThreshold,

        ISNULL(sp.Status, 'locked') AS Status,
        sp.QuizScore,
        sp.GameScore,
        sp.XPEarned,
        sp.Attempts,
        sp.InterventionRequired,

        -- Branching status
        CASE
            WHEN sp.QuizScore < l.InterventionThreshold THEN 'needs_intervention'
            WHEN sp.QuizScore >= l.EnrichmentThreshold THEN 'eligible_enrichment'
            ELSE 'standard'
        END AS BranchingStatus,

        -- Available branches count
        (SELECT COUNT(*)
         FROM LessonBranches lb
         LEFT JOIN StudentBranches sb ON lb.BranchID = sb.BranchID AND sb.StudentID = @StudentID
         WHERE lb.ParentLessonID = l.LessonID
         AND lb.BranchType = 'intervention'
         AND (sp.QuizScore < l.InterventionThreshold OR sp.QuizScore IS NULL)
        ) AS InterventionCount,

        (SELECT COUNT(*)
         FROM LessonBranches lb
         WHERE lb.ParentLessonID = l.LessonID
         AND lb.BranchType = 'enrichment'
         AND sp.QuizScore >= l.EnrichmentThreshold
        ) AS EnrichmentCount

    FROM Lessons l
    LEFT JOIN StudentProgress sp ON l.LessonID = sp.LessonID AND sp.StudentID = @StudentID
    WHERE l.ModuleID = @ModuleID
    ORDER BY l.LessonNumber;

    -- Also return module assessment
    SELECT
        ma.*,
        ISNULL(sma.Score, 0) AS StudentScore,
        ISNULL(sma.Passed, 0) AS StudentPassed,
        sma.CompletedDate
    FROM ModuleAssessments ma
    LEFT JOIN StudentModuleAssessments sma ON ma.AssessmentID = sma.AssessmentID AND sma.StudentID = @StudentID
    WHERE ma.ModuleID = @ModuleID;

    -- Return proficiency level
    SELECT @ProficiencyLevel AS ProficiencyLevel, @StudentAbility AS StudentAbility;
END
GO

-- SP: Update Quiz Score and Check Branching
CREATE OR ALTER PROCEDURE SP_UpdateQuizScore
    @StudentID INT,
    @LessonID INT,
    @QuizScore INT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @InterventionThreshold INT;
    DECLARE @EnrichmentThreshold INT;

    -- Get thresholds
    SELECT
        @InterventionThreshold = InterventionThreshold,
        @EnrichmentThreshold = EnrichmentThreshold
    FROM Lessons WHERE LessonID = @LessonID;

    -- Update or insert progress
    IF EXISTS (SELECT 1 FROM StudentProgress WHERE StudentID = @StudentID AND LessonID = @LessonID)
    BEGIN
        UPDATE StudentProgress
        SET QuizScore = @QuizScore,
            InterventionRequired = CASE WHEN @QuizScore < @InterventionThreshold THEN 1 ELSE 0 END,
            Attempts = Attempts + 1,
            LastAttemptDate = GETDATE()
        WHERE StudentID = @StudentID AND LessonID = @LessonID;
    END
    ELSE
    BEGIN
        INSERT INTO StudentProgress (StudentID, LessonID, QuizScore, InterventionRequired, Attempts, LastAttemptDate)
        VALUES (@StudentID, @LessonID, @QuizScore,
                CASE WHEN @QuizScore < @InterventionThreshold THEN 1 ELSE 0 END,
                1, GETDATE());
    END

    -- Return branching decision
    SELECT
        @QuizScore AS QuizScore,
        CASE
            WHEN @QuizScore < @InterventionThreshold THEN 'intervention_required'
            WHEN @QuizScore >= @EnrichmentThreshold THEN 'enrichment_unlocked'
            ELSE 'proceed_standard'
        END AS BranchingDecision,
        @InterventionThreshold AS InterventionThreshold,
        @EnrichmentThreshold AS EnrichmentThreshold;
END
GO

-- ============================================================================
-- PHASE 6: Indexes for Performance
-- ============================================================================

CREATE NONCLUSTERED INDEX IX_StudentProgress_StudentLesson
ON StudentProgress(StudentID, LessonID) INCLUDE (QuizScore, GameScore, Status);
GO

CREATE NONCLUSTERED INDEX IX_LessonBranches_Parent
ON LessonBranches(ParentLessonID, BranchType);
GO

CREATE NONCLUSTERED INDEX IX_StudentBranches_Student
ON StudentBranches(StudentID, BranchID) INCLUDE (Status, Score);
GO

CREATE NONCLUSTERED INDEX IX_Lessons_Module
ON Lessons(ModuleID, LessonNumber);
GO

-- ============================================================================
-- PHASE 7: Views for Easy Querying
-- ============================================================================

CREATE OR ALTER VIEW VW_StudentLessonProgress AS
SELECT
    s.StudentID,
    s.FirstName + ' ' + s.LastName AS StudentName,
    s.CurrentAbility,
    l.ModuleID,
    m.ModuleName,
    l.LessonID,
    l.LessonTitle,
    l.Quarter,
    l.LessonNumber,
    l.GameType,
    sp.Status,
    sp.QuizScore,
    sp.GameScore,
    sp.XPEarned,
    sp.InterventionRequired,
    sp.Attempts,
    sp.LastAttemptDate
FROM Students s
CROSS JOIN Lessons l
LEFT JOIN StudentProgress sp ON s.StudentID = sp.StudentID AND l.LessonID = sp.LessonID
LEFT JOIN Modules m ON l.ModuleID = m.ModuleID;
GO

-- ============================================================================
-- VERIFICATION QUERIES
-- ============================================================================

-- Check tables created
SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('Modules', 'LessonBranches', 'StudentBranches', 'ModuleAssessments', 'StudentModuleAssessments')
ORDER BY TABLE_NAME;
GO

-- Check Module 1 structure
SELECT * FROM Modules WHERE ModuleID = 1;
GO

-- Check updated lessons
SELECT LessonID, LessonTitle, ModuleID, Quarter, LessonNumber, GameType
FROM Lessons
WHERE ModuleID = 1
ORDER BY LessonNumber;
GO

-- Check intervention/enrichment branches
SELECT * FROM LessonBranches;
GO

PRINT '✅ Database schema updated successfully!';
PRINT 'Next steps:';
PRINT '1. Update PHP API endpoints to use new stored procedures';
PRINT '2. Integrate Android app with adaptive branching';
PRINT '3. Test intervention/enrichment flow';
GO
