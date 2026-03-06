-- =============================================================================
-- LiteRise Migration: TutorialProgress table
-- Run after migrate_nodes.sql
-- =============================================================================

USE [LiteRiseDB]
GO

IF NOT EXISTS (SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'TutorialProgress')
BEGIN
    CREATE TABLE [dbo].[TutorialProgress] (
        [TutorialProgressID]  INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        [StudentID]           INT NOT NULL REFERENCES [dbo].[Students]([StudentID]),
        [TutorialKey]         NVARCHAR(100) NOT NULL,
        [SeenAt]              DATETIME NOT NULL DEFAULT GETDATE(),
        CONSTRAINT [UQ_Student_Tutorial] UNIQUE ([StudentID], [TutorialKey])
    );
    PRINT 'Created TutorialProgress table';
END
ELSE
    PRINT 'TutorialProgress table already exists - skipped';
GO

PRINT 'TutorialProgress migration complete.';
GO
