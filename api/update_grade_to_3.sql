-- Update existing student records to Grade 3
-- This script updates all test accounts to Grade 3 for the research study

USE LiteRiseDB;
GO

-- Update all existing students to Grade 3
UPDATE [dbo].[Students]
SET [GradeLevel] = 3
WHERE [GradeLevel] = 1;

-- Show updated records
SELECT
    StudentID,
    Nickname,
    FirstName,
    LastName,
    Email,
    GradeLevel,
    DateCreated
FROM [dbo].[Students]
ORDER BY StudentID;

PRINT 'All Grade 1 students have been updated to Grade 3';
GO
