-- Migration: Revert Pronunciation Questions to Voice Input
-- Date: 2025-11-30
-- Description: Removes AnswerChoices from pronunciation questions to make them voice-based instead of MCQ

USE LiteRiseDB
GO

-- Remove AnswerChoices from all pronunciation questions
UPDATE Items
SET AnswerChoices = NULL
WHERE ItemType = 'Pronunciation';

-- Verify the update
SELECT ItemID, ItemText, ItemType, AnswerChoices
FROM Items
WHERE ItemType = 'Pronunciation'
ORDER BY ItemID;
