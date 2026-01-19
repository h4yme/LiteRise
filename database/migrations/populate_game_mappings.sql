-- =====================================================
-- POPULATE NODE-GAME MAPPINGS
-- Map games to nodes for LESSON → GAME → QUIZ flow
-- Each node should have 1-2 appropriate games
-- =====================================================

-- =====================================================
-- MODULE 1: PHONICS AND WORD STUDY
-- Games: word_hunt (1), minimal_pairs (4)
-- =====================================================

INSERT INTO [dbo].[NodeGameMapping] (NodeID, GameID, OrderIndex)
SELECT NodeID, 1, 1 FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber IN (1, 2, 3, 7, 9) -- Word Hunt
UNION ALL
SELECT NodeID, 4, 2 FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber IN (3, 4, 5, 6, 10); -- Minimal Pairs

PRINT 'Module 1 game mappings created';

-- =====================================================
-- MODULE 2: VOCABULARY AND WORD KNOWLEDGE
-- Games: picture_match (3), word_hunt (1)
-- =====================================================

INSERT INTO [dbo].[NodeGameMapping] (NodeID, GameID, OrderIndex)
SELECT NodeID, 3, 1 FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber IN (1, 2, 3, 7, 8) -- Picture Match
UNION ALL
SELECT NodeID, 1, 2 FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber IN (4, 5, 6, 9, 10); -- Word Hunt

PRINT 'Module 2 game mappings created';

-- =====================================================
-- MODULE 3: GRAMMAR AWARENESS
-- Games: sentence_scramble (2), fill_blanks (8)
-- =====================================================

INSERT INTO [dbo].[NodeGameMapping] (NodeID, GameID, OrderIndex)
SELECT NodeID, 2, 1 FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber IN (1, 3, 6, 9, 10) -- Sentence Scramble
UNION ALL
SELECT NodeID, 8, 2 FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber IN (4, 5, 7, 8, 11); -- Fill Blanks

PRINT 'Module 3 game mappings created';

-- =====================================================
-- MODULE 4: COMPREHENDING AND ANALYZING TEXT
-- Games: dialogue_reading (5), story_sequencing (7)
-- =====================================================

INSERT INTO [dbo].[NodeGameMapping] (NodeID, GameID, OrderIndex)
SELECT NodeID, 5, 1 FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber IN (1, 2, 5, 8, 11) -- Dialogue Reading
UNION ALL
SELECT NodeID, 7, 2 FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber IN (3, 4, 6, 7, 9); -- Story Sequencing

PRINT 'Module 4 game mappings created';

-- =====================================================
-- MODULE 5: CREATING AND COMPOSING TEXT
-- Games: sentence_scramble (2), fill_blanks (8)
-- =====================================================

INSERT INTO [dbo].[NodeGameMapping] (NodeID, GameID, OrderIndex)
SELECT NodeID, 2, 1 FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber IN (1, 2, 6, 9, 12) -- Sentence Scramble
UNION ALL
SELECT NodeID, 8, 2 FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber IN (3, 4, 5, 7, 8); -- Fill Blanks

PRINT 'Module 5 game mappings created';

-- =====================================================
-- ADD TIMED TRAIL FOR ALL FINAL ASSESSMENTS
-- =====================================================

INSERT INTO [dbo].[NodeGameMapping] (NodeID, GameID, OrderIndex)
SELECT NodeID, 6, 1 FROM [dbo].[Nodes] WHERE NodeType = 'FINAL_ASSESSMENT'; -- Timed Trail

PRINT 'Final assessment game mappings created';

-- =====================================================
-- SUMMARY
-- =====================================================

PRINT '';
PRINT '====================================================================';
PRINT 'GAME MAPPINGS COMPLETED!';
PRINT '====================================================================';
PRINT 'Games mapped to nodes:';
PRINT '  - Module 1: word_hunt, minimal_pairs';
PRINT '  - Module 2: picture_match, word_hunt';
PRINT '  - Module 3: sentence_scramble, fill_blanks';
PRINT '  - Module 4: dialogue_reading, story_sequencing';
PRINT '  - Module 5: sentence_scramble, fill_blanks';
PRINT '  - All final assessments: timed_trail';
PRINT '';
PRINT 'Each core lesson node now has 1-2 games in the LESSON → GAME → QUIZ flow';
PRINT '====================================================================';
