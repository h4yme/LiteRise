-- Migration: Add 5 Category Scores to Students Table
-- Date: 2026-01-18
-- Purpose: Store placement test results for 5 new categories to prioritize modules

USE [LiteRiseDB]
GO

-- Add 5 category score columns to Students table
ALTER TABLE [dbo].[Students]
ADD
    [Cat1_PhonicsWordStudy] INT NULL,
    [Cat2_VocabularyWordKnowledge] INT NULL,
    [Cat3_GrammarAwareness] INT NULL,
    [Cat4_ComprehendingText] INT NULL,
    [Cat5_CreatingComposing] INT NULL;
GO

-- Add default constraints (scores default to NULL until placement test completed)
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [DF_Cat1_PhonicsWordStudy] DEFAULT (NULL) FOR [Cat1_PhonicsWordStudy];
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [DF_Cat2_VocabularyWordKnowledge] DEFAULT (NULL) FOR [Cat2_VocabularyWordKnowledge];
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [DF_Cat3_GrammarAwareness] DEFAULT (NULL) FOR [Cat3_GrammarAwareness];
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [DF_Cat4_ComprehendingText] DEFAULT (NULL) FOR [Cat4_ComprehendingText];
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [DF_Cat5_CreatingComposing] DEFAULT (NULL) FOR [Cat5_CreatingComposing];
GO

-- Add check constraints to ensure scores are valid percentages (0-100)
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [CHK_Cat1_Range] CHECK ([Cat1_PhonicsWordStudy] IS NULL OR ([Cat1_PhonicsWordStudy] >= 0 AND [Cat1_PhonicsWordStudy] <= 100));
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [CHK_Cat2_Range] CHECK ([Cat2_VocabularyWordKnowledge] IS NULL OR ([Cat2_VocabularyWordKnowledge] >= 0 AND [Cat2_VocabularyWordKnowledge] <= 100));
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [CHK_Cat3_Range] CHECK ([Cat3_GrammarAwareness] IS NULL OR ([Cat3_GrammarAwareness] >= 0 AND [Cat3_GrammarAwareness] <= 100));
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [CHK_Cat4_Range] CHECK ([Cat4_ComprehendingText] IS NULL OR ([Cat4_ComprehendingText] >= 0 AND [Cat4_ComprehendingText] <= 100));
ALTER TABLE [dbo].[Students] ADD CONSTRAINT [CHK_Cat5_Range] CHECK ([Cat5_CreatingComposing] IS NULL OR ([Cat5_CreatingComposing] >= 0 AND [Cat5_CreatingComposing] <= 100));
GO

PRINT 'Migration completed: Added 5 category score columns to Students table';
GO
