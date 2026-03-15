-- ============================================================
-- Migration: Rename Teachers.Department -> Teachers.School
-- Run once against LiteRiseDB
-- ============================================================

USE [LiteRiseDB];
GO

-- 1. Rename the column
EXEC sp_rename 'dbo.Teachers.Department', 'School', 'COLUMN';
GO

-- 2. Verify
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM   INFORMATION_SCHEMA.COLUMNS
WHERE  TABLE_NAME  = 'Teachers'
AND    COLUMN_NAME = 'School';
GO
