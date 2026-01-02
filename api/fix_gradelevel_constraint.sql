-- Fix GradeLevel CHECK Constraint
-- This script removes the old constraint (4-6) and adds a new one (1-12)

USE LiteRiseDB;
GO

-- Step 1: Find and drop the old CHECK constraint
DECLARE @ConstraintName NVARCHAR(200);

SELECT @ConstraintName = name
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.Students')
  AND definition LIKE '%GradeLevel%';

IF @ConstraintName IS NOT NULL
BEGIN
    DECLARE @SQL NVARCHAR(500);
    SET @SQL = 'ALTER TABLE [dbo].[Students] DROP CONSTRAINT ' + @ConstraintName;
    EXEC sp_executesql @SQL;
    PRINT 'Old constraint dropped: ' + @ConstraintName;
END
ELSE
BEGIN
    PRINT 'No GradeLevel constraint found';
END
GO

-- Step 2: Add new CHECK constraint allowing grades 1-12
ALTER TABLE [dbo].[Students]
ADD CONSTRAINT CK_Students_GradeLevel
CHECK ([GradeLevel] >= 1 AND [GradeLevel] <= 12);
GO

PRINT 'New constraint added: GradeLevel must be between 1 and 12';
GO

-- Verify the constraint
SELECT
    name AS ConstraintName,
    definition AS ConstraintDefinition
FROM sys.check_constraints
WHERE parent_object_id = OBJECT_ID('dbo.Students')
  AND definition LIKE '%GradeLevel%';
GO
