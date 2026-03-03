-- =============================================================================
-- LiteRise Database Migration: Node/Module System
-- Run this on LiteRiseDB after scriptjan.sql
-- Based on actual schema from scripttoday.sql (exported 2026-03-03)
-- =============================================================================

USE [LiteRiseDB]
GO

-- =============================================================================
-- 1. Modules table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Modules')
BEGIN
    CREATE TABLE [dbo].[Modules] (
        [ModuleID]         INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [ModuleName]       NVARCHAR(100) NOT NULL,
        [ModuleCode]       NVARCHAR(20) NULL,
        [CategoryMapping]  INT NULL,
        [OrderIndex]       INT NOT NULL,
        [TotalNodes]       INT NULL,
        [Description]      NVARCHAR(500) NULL,
        [IsActive]         BIT NULL,
        [CreatedDate]      DATETIME NULL
    );
    PRINT 'Created Modules table';
END
GO

-- =============================================================================
-- 2. Nodes table (lesson nodes inside modules)
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Nodes')
BEGIN
    CREATE TABLE [dbo].[Nodes] (
        [NodeID]               INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [ModuleID]             INT NULL REFERENCES [dbo].[Modules]([ModuleID]),
        [NodeType]             NVARCHAR(20) NOT NULL,
                               -- CORE_LESSON | FINAL_ASSESSMENT
        [NodeNumber]           INT NOT NULL,
        [Quarter]              INT NULL,
        [LessonTitle]          NVARCHAR(200) NULL,
        [LearningObjectives]   NVARCHAR(MAX) NULL,
        [ContentJSON]          NVARCHAR(MAX) NULL,
        [SkillCategory]        NVARCHAR(50) NULL,
        [EstimatedDuration]    INT NULL,
        [XPReward]             INT NULL,
        [IsActive]             BIT NULL,
        [CreatedDate]          DATETIME NULL,
        CONSTRAINT [UQ_Module_NodeNumber] UNIQUE ([ModuleID], [NodeNumber])
    );
    PRINT 'Created Nodes table';
END
GO

-- =============================================================================
-- 3. NodeGameMapping table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'NodeGameMapping')
BEGIN
    CREATE TABLE [dbo].[NodeGameMapping] (
        [MappingID]    INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [NodeID]       INT NULL,
        [GameID]       INT NULL,
        [IsRequired]   BIT NULL,
        [OrderIndex]   INT NULL,
        [CreatedDate]  DATETIME NULL
    );
    PRINT 'Created NodeGameMapping table';
END
GO

-- =============================================================================
-- 4. QuizQuestions table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'QuizQuestions')
BEGIN
    CREATE TABLE [dbo].[QuizQuestions] (
        [QuestionID]          INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [NodeID]              INT NULL REFERENCES [dbo].[Nodes]([NodeID]),
        [QuestionText]        NVARCHAR(MAX) NOT NULL,
        [QuestionType]        NVARCHAR(50) NULL,
        [OptionsJSON]         NVARCHAR(MAX) NULL,
        [CorrectAnswer]       NVARCHAR(10) NULL,
        [EstimatedDifficulty] NVARCHAR(20) NULL,
        [SkillCategory]       NVARCHAR(50) NULL,
        [MediaURL]            NVARCHAR(500) NULL,
        [ReadingPassage]      NVARCHAR(MAX) NULL,
        [IsActive]            BIT NULL,
        [CreatedDate]         DATETIME NULL
    );
    PRINT 'Created QuizQuestions table';
END
GO

-- =============================================================================
-- 5. SupplementalNodes table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SupplementalNodes')
BEGIN
    CREATE TABLE [dbo].[SupplementalNodes] (
        [SupplementalNodeID]  INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [NodeType]            NVARCHAR(20) NOT NULL,
                              -- INTERVENTION | SUPPLEMENTAL | ENRICHMENT
        [AfterNodeID]         INT NULL REFERENCES [dbo].[Nodes]([NodeID]),
        [TriggerLogic]        NVARCHAR(500) NULL,
        [Title]               NVARCHAR(200) NULL,
        [ContentJSON]         NVARCHAR(MAX) NULL,
        [SkillCategory]       NVARCHAR(50) NULL,
        [EstimatedDuration]   INT NULL,
        [XPReward]            INT NULL,
        [IsActive]            BIT NULL,
        [CreatedDate]         DATETIME NULL,
        [IsVisible]           BIT NULL
    );
    PRINT 'Created SupplementalNodes table';
END
GO

-- =============================================================================
-- 6. StudentNodeProgress table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'StudentNodeProgress')
BEGIN
    CREATE TABLE [dbo].[StudentNodeProgress] (
        [ProgressID]       INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [StudentID]        INT NULL REFERENCES [dbo].[Students]([StudentID]),
        [NodeID]           INT NULL REFERENCES [dbo].[Nodes]([NodeID]),
        [NodeState]        NVARCHAR(20) NULL,
                           -- LOCKED | UNLOCKED | IN_PROGRESS | COMPLETED | PROCEED | ADD_INTERVENTION | ADD_SUPPLEMENTAL | OFFER_ENRICHMENT
        [AttemptCount]     INT NULL,
        [BestQuizScore]    INT NULL,
        [LatestQuizScore]  INT NULL,
        [AverageQuizScore] FLOAT NULL,
        [LessonCompleted]  BIT NULL,
        [GameCompleted]    BIT NULL,
        [QuizCompleted]    BIT NULL,
        [UnlockedDate]     DATETIME NULL,
        [CompletedDate]    DATETIME NULL,
        [LastAttemptDate]  DATETIME NULL,
        [CreatedDate]      DATETIME NULL,
        CONSTRAINT [UQ_Student_Node] UNIQUE ([StudentID], [NodeID])
    );
    PRINT 'Created StudentNodeProgress table';
END
GO

-- =============================================================================
-- 7. StudentSupplementalProgress table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'StudentSupplementalProgress')
BEGIN
    CREATE TABLE [dbo].[StudentSupplementalProgress] (
        [SupplementalProgressID]  INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [StudentID]               INT NULL,
        [SupplementalNodeID]      INT NULL,
        [IsVisible]               BIT NULL,
        [TriggerReason]           NVARCHAR(500) NULL,
        [IsCompleted]             BIT NULL,
        [CompletedDate]           DATETIME NULL,
        [CreatedDate]             DATETIME NULL,
        CONSTRAINT [UQ_Student_Supplemental] UNIQUE ([StudentID], [SupplementalNodeID])
    );
    PRINT 'Created StudentSupplementalProgress table';
END
GO

-- =============================================================================
-- 8. Add new columns to Students table (if not already present)
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'CurrentNodeID')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [CurrentNodeID] INT NULL;
    PRINT 'Added CurrentNodeID to Students';
END
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'CurrentModuleID')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [CurrentModuleID] INT NULL;
    PRINT 'Added CurrentModuleID to Students';
END
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'PlacementLevel')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [PlacementLevel] INT NULL;
    PRINT 'Added PlacementLevel to Students';
END
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'PreAssessmentLevel')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [PreAssessmentLevel] INT NULL;
    PRINT 'Added PreAssessmentLevel to Students';
END
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'TotalLoginCount')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [TotalLoginCount] INT NULL;
    PRINT 'Added TotalLoginCount to Students';
END
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'CurrentLevel')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [CurrentLevel] INT NULL;
    PRINT 'Added CurrentLevel to Students';
END
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'UpdatedDate')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [UpdatedDate] DATETIME NULL;
    PRINT 'Added UpdatedDate to Students';
END
GO

PRINT 'Migration complete.';
GO
