-- Add Nickname column to Students table
-- Run this migration to support nickname feature

USE LiteRiseDB;
GO

-- Check if column exists before adding
IF NOT EXISTS (SELECT * FROM sys.columns 
               WHERE object_id = OBJECT_ID(N'[dbo].[Students]') 
               AND name = 'Nickname')
BEGIN
    ALTER TABLE Students
    ADD Nickname NVARCHAR(20) NULL;
    
    PRINT 'Nickname column added successfully to Students table';
END
ELSE
BEGIN
    PRINT 'Nickname column already exists in Students table';
END
GO
