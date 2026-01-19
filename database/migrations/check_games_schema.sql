-- =====================================================
-- CHECK GAMES TABLE SCHEMA
-- Discover the actual column names in the Games table
-- =====================================================

PRINT '====================================================================';
PRINT 'CHECKING GAMES TABLE SCHEMA';
PRINT '====================================================================';
PRINT '';

-- Check if Games table exists
IF OBJECT_ID('dbo.Games', 'U') IS NULL
BEGIN
    PRINT 'ERROR: Games table does not exist!';
END
ELSE
BEGIN
    PRINT 'Games table exists.';
    PRINT '';
    PRINT 'Column definitions:';
    PRINT '';

    -- Show column details
    SELECT
        COLUMN_NAME,
        DATA_TYPE,
        CHARACTER_MAXIMUM_LENGTH,
        IS_NULLABLE
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'Games'
    ORDER BY ORDINAL_POSITION;

    PRINT '';
    PRINT 'Row count:';
    SELECT COUNT(*) AS TotalGames FROM [dbo].[Games];

    PRINT '';
    PRINT 'All records in Games table:';
    SELECT * FROM [dbo].[Games];
END

PRINT '';
PRINT '====================================================================';
