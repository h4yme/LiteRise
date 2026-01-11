-- =============================================
-- Add ReadingPassage Field for Reading Comprehension
-- =============================================
-- This migration adds a ReadingPassage column to store
-- separate reading passages for comprehension questions
-- =============================================

USE LiteRiseDB;
GO

PRINT 'Adding ReadingPassage column to AssessmentItems...';
PRINT '';

-- Add ReadingPassage column if it doesn't exist
IF NOT EXISTS (
    SELECT * FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'AssessmentItems'
    AND COLUMN_NAME = 'ReadingPassage'
)
BEGIN
    ALTER TABLE dbo.AssessmentItems
    ADD ReadingPassage NVARCHAR(MAX) NULL;

    PRINT '✓ ReadingPassage column added successfully';
END
ELSE
BEGIN
    PRINT 'ReadingPassage column already exists';
END

PRINT '';
PRINT 'Migration complete!';
GO
