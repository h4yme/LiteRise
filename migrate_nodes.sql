-- =============================================================================
-- LiteRise Database Migration: Node/Module System
-- Run this on LiteRiseDB after scriptjan.sql
-- =============================================================================

USE [LiteRiseDB]
GO

-- =============================================================================
-- 1. Modules table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'Modules')
BEGIN
    CREATE TABLE [dbo].[Modules] (
        [ModuleID]      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [ModuleName]    NVARCHAR(100) NOT NULL,
        [Description]  NVARCHAR(MAX) NULL,
        [GradeLevel]   INT NOT NULL DEFAULT 3,
        [OrderNumber]  INT NOT NULL DEFAULT 1,
        [IsActive]     BIT NOT NULL DEFAULT 1,
        [CreatedAt]    DATETIME NOT NULL DEFAULT GETDATE()
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
        [ModuleID]             INT NOT NULL REFERENCES [dbo].[Modules]([ModuleID]),
        [NodeNumber]           INT NOT NULL,
        [LessonTitle]          NVARCHAR(200) NOT NULL,
        [LearningObjectives]   NVARCHAR(MAX) NULL,
        [ContentJSON]          NVARCHAR(MAX) NULL,
        [NodeType]             NVARCHAR(50) NOT NULL DEFAULT 'CORE_LESSON',
                               -- CORE_LESSON | FINAL_ASSESSMENT
        [Quarter]              INT NULL,
        [IsActive]             BIT NOT NULL DEFAULT 1,
        [CreatedAt]            DATETIME NOT NULL DEFAULT GETDATE()
    );
    PRINT 'Created Nodes table';
END
GO

-- =============================================================================
-- 3. QuizQuestions table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'QuizQuestions')
BEGIN
    CREATE TABLE [dbo].[QuizQuestions] (
        [QuestionID]    INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [NodeID]        INT NOT NULL REFERENCES [dbo].[Nodes]([NodeID]),
        [QuestionText]  NVARCHAR(MAX) NOT NULL,
        [OptionA]       NVARCHAR(500) NOT NULL,
        [OptionB]       NVARCHAR(500) NOT NULL,
        [OptionC]       NVARCHAR(500) NULL,
        [OptionD]       NVARCHAR(500) NULL,
        [CorrectAnswer] INT NOT NULL,  -- 1=A, 2=B, 3=C, 4=D
        [Difficulty]    FLOAT NULL DEFAULT 0.5,
        [IsActive]      BIT NOT NULL DEFAULT 1
    );
    PRINT 'Created QuizQuestions table';
END
GO

-- =============================================================================
-- 4. StudentNodeProgress table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'StudentNodeProgress')
BEGIN
    CREATE TABLE [dbo].[StudentNodeProgress] (
        [ProgressID]       INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [StudentID]        INT NOT NULL REFERENCES [dbo].[Students]([StudentID]),
        [NodeID]           INT NOT NULL REFERENCES [dbo].[Nodes]([NodeID]),
        [LessonCompleted]  BIT NOT NULL DEFAULT 0,
        [GameCompleted]    BIT NOT NULL DEFAULT 0,
        [QuizCompleted]    BIT NOT NULL DEFAULT 0,
        [LatestQuizScore]  FLOAT NULL,
        [QuizScore]        FLOAT NULL,
        [AdaptiveDecision] NVARCHAR(50) NULL,  -- PROCEED | ADD_INTERVENTION | ADD_SUPPLEMENTAL | OFFER_ENRICHMENT
        [NodeState]        NVARCHAR(50) NULL,  -- alias for AdaptiveDecision used in get_node_progress
        [CompletedAt]      DATETIME NULL,
        [CompletedDate]    DATETIME NULL,
        CONSTRAINT UQ_StudentNodeProgress UNIQUE (StudentID, NodeID)
    );
    PRINT 'Created StudentNodeProgress table';
END
GO

-- =============================================================================
-- 5. SupplementalNodes table
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'SupplementalNodes')
BEGIN
    CREATE TABLE [dbo].[SupplementalNodes] (
        [NodeID]      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [AfterNodeID] INT NOT NULL REFERENCES [dbo].[Nodes]([NodeID]),
        [Title]       NVARCHAR(200) NOT NULL,
        [ContentJSON] NVARCHAR(MAX) NULL,
        [NodeType]    NVARCHAR(50) NOT NULL DEFAULT 'SUPPLEMENTAL',
                      -- INTERVENTION | SUPPLEMENTAL | ENRICHMENT
        [IsVisible]   BIT NOT NULL DEFAULT 1,
        [IsActive]    BIT NOT NULL DEFAULT 1,
        [CreatedAt]   DATETIME NOT NULL DEFAULT GETDATE()
    );
    PRINT 'Created SupplementalNodes table';
END
GO

-- =============================================================================
-- 6. Add new columns to Students table (if not already present)
-- =============================================================================
IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'CurrentNodeID')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [CurrentNodeID] INT NULL;
    PRINT 'Added CurrentNodeID to Students';
END
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = 'Students' AND COLUMN_NAME = 'PlacementLevel')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [PlacementLevel] INT NULL DEFAULT 1;
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
    ALTER TABLE [dbo].[Students] ADD [TotalLoginCount] INT NOT NULL DEFAULT 0;
    PRINT 'Added TotalLoginCount to Students';
END
GO

PRINT 'Migration complete.';
GO
