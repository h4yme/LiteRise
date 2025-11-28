-- Migration: Add onboarding and module tracking features
-- Date: 2025-11-28
-- Description: Adds nickname, tutorial completion, priority module tracking

USE [LiteRiseDB]
GO

-- Add new columns to Students table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'Nickname')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD Nickname NVARCHAR(50) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'CompletedWelcome')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD CompletedWelcome BIT DEFAULT 0 NOT NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'CompletedTutorial')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD CompletedTutorial BIT DEFAULT 0 NOT NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'PriorityModule')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD PriorityModule NVARCHAR(50) NULL;
END
GO

-- Create Modules table for the new ladder-style progression
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Modules]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[Modules] (
        ModuleID INT IDENTITY(1,1) PRIMARY KEY,
        ModuleName NVARCHAR(100) NOT NULL,
        ModuleKey NVARCHAR(50) NOT NULL UNIQUE, -- e.g., 'phonics', 'vocabulary', 'comprehension'
        ModuleDescription NVARCHAR(MAX),
        IconName NVARCHAR(50), -- Icon identifier for app
        ColorCode NVARCHAR(20), -- Hex color for module theme
        OrderIndex INT NOT NULL, -- Display order
        RequiredScore FLOAT DEFAULT 0, -- Minimum assessment score to unlock
        IsActive BIT DEFAULT 1
    );
END
GO

-- Create ModuleLadderSteps table for Duolingo-style progression
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[ModuleLadderSteps]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[ModuleLadderSteps] (
        StepID INT IDENTITY(1,1) PRIMARY KEY,
        ModuleID INT NOT NULL FOREIGN KEY REFERENCES [dbo].[Modules](ModuleID),
        StepNumber INT NOT NULL,
        StepName NVARCHAR(100) NOT NULL,
        StepType NVARCHAR(50) NOT NULL, -- 'game', 'lesson', 'practice', 'test'
        ActivityType NVARCHAR(50), -- 'word_hunt', 'scramble', 'timed_trail', etc.
        DifficultyLevel INT, -- 1-5
        XPReward INT DEFAULT 10,
        RequiredAccuracy FLOAT DEFAULT 0.6, -- 60% to pass
        IsActive BIT DEFAULT 1,
        CONSTRAINT UQ_ModuleStep UNIQUE(ModuleID, StepNumber)
    );
END
GO

-- Create StudentModuleProgress table to track individual progress
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[StudentModuleProgress]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[StudentModuleProgress] (
        ProgressID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL FOREIGN KEY REFERENCES [dbo].[Students](StudentID),
        ModuleID INT NOT NULL FOREIGN KEY REFERENCES [dbo].[Modules](ModuleID),
        CurrentStepNumber INT DEFAULT 0,
        CompletedSteps INT DEFAULT 0,
        TotalSteps INT DEFAULT 0,
        ModuleScore FLOAT DEFAULT 0,
        IsCompleted BIT DEFAULT 0,
        StartedAt DATETIME DEFAULT GETDATE(),
        CompletedAt DATETIME NULL,
        LastActivityAt DATETIME DEFAULT GETDATE(),
        CONSTRAINT UQ_StudentModule UNIQUE(StudentID, ModuleID)
    );
END
GO

-- Create StudentStepResults table to track individual step performance
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[StudentStepResults]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[StudentStepResults] (
        ResultID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL FOREIGN KEY REFERENCES [dbo].[Students](StudentID),
        StepID INT NOT NULL FOREIGN KEY REFERENCES [dbo].[ModuleLadderSteps](StepID),
        Score FLOAT,
        Accuracy FLOAT,
        TimeSpent INT, -- in seconds
        IsPassed BIT DEFAULT 0,
        XPEarned INT DEFAULT 0,
        CompletedAt DATETIME DEFAULT GETDATE(),
        Attempts INT DEFAULT 1
    );
END
GO

-- Create index for faster queries
CREATE NONCLUSTERED INDEX IX_StudentModuleProgress_Student
ON [dbo].[StudentModuleProgress] (StudentID);
GO

CREATE NONCLUSTERED INDEX IX_StudentStepResults_Student
ON [dbo].[StudentStepResults] (StudentID, StepID);
GO

-- Insert default modules
IF NOT EXISTS (SELECT * FROM [dbo].[Modules] WHERE ModuleKey = 'phonics')
BEGIN
    INSERT INTO [dbo].[Modules] (ModuleName, ModuleKey, ModuleDescription, IconName, ColorCode, OrderIndex, RequiredScore)
    VALUES
    ('Phonics & Sounds', 'phonics', 'Learn letter sounds and basic phonics patterns', 'ic_phonics', '#FF6B9D', 1, 0),
    ('Vocabulary Building', 'vocabulary', 'Expand your word knowledge and meanings', 'ic_vocabulary', '#4ECDC4', 2, 0.3),
    ('Reading Comprehension', 'comprehension', 'Understand and analyze what you read', 'ic_comprehension', '#95E1D3', 3, 0.5),
    ('Spelling Mastery', 'spelling', 'Master spelling patterns and rules', 'ic_spelling', '#F38181', 4, 0.4),
    ('Fluency & Expression', 'fluency', 'Read smoothly and with expression', 'ic_fluency', '#AA96DA', 5, 0.6);
END
GO

-- Update the SP_StudentLogin stored procedure to include new fields
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_StudentLogin]') AND type in (N'P', N'PC'))
BEGIN
    DROP PROCEDURE [dbo].[SP_StudentLogin];
END
GO

CREATE PROCEDURE [dbo].[SP_StudentLogin]
    @Email NVARCHAR(100),
    @Password NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    SELECT
        StudentID,
        FirstName,
        LastName,
        Email,
        Password,
        GradeLevel,
        Section,
        CurrentAbility,
        TotalXP,
        CurrentStreak,
        LongestStreak,
        LastLogin,
        Nickname,
        CompletedWelcome,
        CompletedTutorial,
        PriorityModule
    FROM Students
    WHERE Email = @Email;
END
GO

PRINT 'Migration completed successfully!';
