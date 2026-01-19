-- =====================================================
-- VERIFY GAMES TABLE
-- Check if all required games exist before populating mappings
-- =====================================================

PRINT '====================================================================';
PRINT 'VERIFYING GAMES TABLE';
PRINT '====================================================================';
PRINT '';

-- Check if Games table exists
IF OBJECT_ID('dbo.Games', 'U') IS NULL
BEGIN
    PRINT 'ERROR: Games table does not exist!';
    PRINT 'Please create the Games table first.';
    PRINT '';
END
ELSE
BEGIN
    PRINT 'Games table exists.';
    PRINT '';

    -- Show all games in the table
    PRINT 'Current games in database:';
    SELECT GameID, GameTitle, GameType, SkillCategory FROM [dbo].[Games] ORDER BY GameID;

    PRINT '';
    PRINT 'Required games for node mappings:';
    PRINT '  GameID 1: word_hunt';
    PRINT '  GameID 2: sentence_scramble';
    PRINT '  GameID 3: picture_match';
    PRINT '  GameID 4: minimal_pairs';
    PRINT '  GameID 5: dialogue_reading';
    PRINT '  GameID 6: timed_trail';
    PRINT '  GameID 7: story_sequencing';
    PRINT '  GameID 8: fill_blanks';
    PRINT '';

    -- Check each required game
    DECLARE @missing INT = 0;

    IF NOT EXISTS (SELECT 1 FROM [dbo].[Games] WHERE GameID = 1)
    BEGIN
        PRINT 'WARNING: GameID 1 (word_hunt) is missing!';
        SET @missing = @missing + 1;
    END

    IF NOT EXISTS (SELECT 1 FROM [dbo].[Games] WHERE GameID = 2)
    BEGIN
        PRINT 'WARNING: GameID 2 (sentence_scramble) is missing!';
        SET @missing = @missing + 1;
    END

    IF NOT EXISTS (SELECT 1 FROM [dbo].[Games] WHERE GameID = 3)
    BEGIN
        PRINT 'WARNING: GameID 3 (picture_match) is missing!';
        SET @missing = @missing + 1;
    END

    IF NOT EXISTS (SELECT 1 FROM [dbo].[Games] WHERE GameID = 4)
    BEGIN
        PRINT 'WARNING: GameID 4 (minimal_pairs) is missing!';
        SET @missing = @missing + 1;
    END

    IF NOT EXISTS (SELECT 1 FROM [dbo].[Games] WHERE GameID = 5)
    BEGIN
        PRINT 'WARNING: GameID 5 (dialogue_reading) is missing!';
        SET @missing = @missing + 1;
    END

    IF NOT EXISTS (SELECT 1 FROM [dbo].[Games] WHERE GameID = 6)
    BEGIN
        PRINT 'WARNING: GameID 6 (timed_trail) is missing!';
        SET @missing = @missing + 1;
    END

    IF NOT EXISTS (SELECT 1 FROM [dbo].[Games] WHERE GameID = 7)
    BEGIN
        PRINT 'WARNING: GameID 7 (story_sequencing) is missing!';
        SET @missing = @missing + 1;
    END

    IF NOT EXISTS (SELECT 1 FROM [dbo].[Games] WHERE GameID = 8)
    BEGIN
        PRINT 'WARNING: GameID 8 (fill_blanks) is missing!';
        SET @missing = @missing + 1;
    END

    PRINT '';
    IF @missing = 0
    BEGIN
        PRINT 'SUCCESS: All required games are present!';
    END
    ELSE
    BEGIN
        PRINT 'ERROR: ' + CAST(@missing AS NVARCHAR) + ' required game(s) missing!';
        PRINT 'Game mappings will fail until these games are added.';
    END
END

PRINT '';
PRINT '====================================================================';
