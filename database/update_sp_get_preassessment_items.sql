USE [LiteRiseDB]
GO

-- Update SP_GetPreAssessmentItems to include CorrectAnswer, Phonetic, and Definition fields
ALTER PROCEDURE [dbo].[SP_GetPreAssessmentItems]
AS
BEGIN
    SET NOCOUNT ON;

    -- Get 20 items with varied difficulty
    SELECT TOP 20
        ItemID,
        ItemText,
        ItemType,
        DifficultyLevel,
        AnswerChoices,
        CorrectAnswer,      -- ⭐ ADDED: This is needed for Syntax items
        DifficultyParam,
        DiscriminationParam,
        GuessingParam,
        ImageURL,           -- ⭐ ADDED: For future image support
        AudioURL,           -- ⭐ ADDED: For pronunciation audio
        Phonetic,           -- ⭐ ADDED: Phonetic breakdown/IPA for pronunciation
        Definition          -- ⭐ ADDED: Word definition for pronunciation items
    FROM Items
    WHERE IsActive = 1
    ORDER BY DifficultyParam; -- Start easy, then harder
END
GO

PRINT 'SP_GetPreAssessmentItems updated successfully - now includes CorrectAnswer, ImageURL, AudioURL, Phonetic, and Definition';
