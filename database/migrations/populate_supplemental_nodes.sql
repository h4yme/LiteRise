-- =====================================================
-- POPULATE SUPPLEMENTAL NODES
-- Conditional support nodes that appear based on quiz performance
-- Types: INTERVENTION, SUPPLEMENTAL, ENRICHMENT
-- =====================================================

-- =====================================================
-- MODULE 1: PHONICS - SUPPLEMENTAL NODES
-- =====================================================

-- Node 1: Basic Sight Words - Support Nodes
INSERT INTO [dbo].[SupplementalNodes] (NodeType, AfterNodeID, TriggerLogic, Title, SkillCategory, EstimatedDuration, XPReward, ContentJSON)
VALUES
('INTERVENTION',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 1),
 'quiz_score < 70',
 'Sight Words Intensive Practice',
 'Phonics',
 15,
 15,
 '{"activities": ["Flashcard drill", "Word matching game", "Sight word sentences"], "practice_words": ["the", "and", "is", "to", "you"]}'),

('SUPPLEMENTAL',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 1),
 'quiz_score >= 70 AND quiz_score < 80 AND placement_level = 1',
 'Sight Words Review',
 'Phonics',
 10,
 10,
 '{"activities": ["Quick review", "Reading practice"], "focus": "Reinforcement for beginners"}'),

('ENRICHMENT',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 1),
 'quiz_score >= 90 AND placement_level = 3',
 'Advanced Sight Word Challenge',
 'Phonics',
 12,
 25,
 '{"activities": ["Create sight word story", "Advanced word games"], "difficulty": "challenging"}');

-- Node 2: CVC Patterns - Support Nodes
INSERT INTO [dbo].[SupplementalNodes] (NodeType, AfterNodeID, TriggerLogic, Title, SkillCategory, EstimatedDuration, XPReward, ContentJSON)
VALUES
('INTERVENTION',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 2),
 'quiz_score < 70',
 'CVC Pattern Remediation',
 'Phonics',
 15,
 15,
 '{"activities": ["Sound blending practice", "CVC word building"], "focus": "cat, dog, run, sit, top"}'),

('SUPPLEMENTAL',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 2),
 'quiz_score >= 70 AND quiz_score < 80',
 'CVC Practice Boost',
 'Phonics',
 10,
 10,
 '{"activities": ["Extra CVC exercises", "Word family practice"]}');

PRINT 'Module 1 supplemental nodes created';

-- =====================================================
-- MODULE 2: VOCABULARY - SUPPLEMENTAL NODES
-- =====================================================

INSERT INTO [dbo].[SupplementalNodes] (NodeType, AfterNodeID, TriggerLogic, Title, SkillCategory, EstimatedDuration, XPReward, ContentJSON)
VALUES
('INTERVENTION',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber = 1),
 'quiz_score < 70',
 'Nouns and Verbs Intensive Help',
 'Vocabulary',
 15,
 15,
 '{"activities": ["Noun sorting", "Verb action games", "Picture labeling"]}'),

('SUPPLEMENTAL',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber = 1),
 'quiz_score >= 70 AND quiz_score < 80 AND placement_level = 1',
 'Vocabulary Building Practice',
 'Vocabulary',
 10,
 10,
 '{"activities": ["Word categorization", "Noun-verb identification games"]}'),

('ENRICHMENT',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 2 AND NodeNumber = 1),
 'quiz_score >= 90 AND placement_level = 3',
 'Creative Vocabulary Challenge',
 'Vocabulary',
 12,
 25,
 '{"activities": ["Write your own sentences", "Advanced word usage"]}');

PRINT 'Module 2 supplemental nodes created';

-- =====================================================
-- MODULE 3: GRAMMAR - SUPPLEMENTAL NODES
-- =====================================================

INSERT INTO [dbo].[SupplementalNodes] (NodeType, AfterNodeID, TriggerLogic, Title, SkillCategory, EstimatedDuration, XPReward, ContentJSON)
VALUES
('INTERVENTION',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber = 1),
 'quiz_score < 70',
 'Sentence Structure Remediation',
 'Grammar',
 15,
 15,
 '{"activities": ["Subject-predicate practice", "Sentence building", "Fragment fixing"]}'),

('SUPPLEMENTAL',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber = 1),
 'quiz_score >= 70 AND quiz_score < 80',
 'Sentence Basics Review',
 'Grammar',
 10,
 10,
 '{"activities": ["Complete sentence practice", "Subject identification"]}'),

('ENRICHMENT',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 3 AND NodeNumber = 1),
 'quiz_score >= 90 AND placement_level = 3',
 'Advanced Sentence Construction',
 'Grammar',
 12,
 25,
 '{"activities": ["Compound sentences", "Creative sentence writing"]}');

PRINT 'Module 3 supplemental nodes created';

-- =====================================================
-- MODULE 4: COMPREHENSION - SUPPLEMENTAL NODES
-- =====================================================

INSERT INTO [dbo].[SupplementalNodes] (NodeType, AfterNodeID, TriggerLogic, Title, SkillCategory, EstimatedDuration, XPReward, ContentJSON)
VALUES
('INTERVENTION',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber = 1),
 'quiz_score < 70',
 'Main Idea Intensive Practice',
 'Comprehension',
 15,
 15,
 '{"activities": ["Guided reading", "Main idea identification drills", "Detail sorting"]}'),

('SUPPLEMENTAL',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber = 1),
 'quiz_score >= 70 AND quiz_score < 80 AND placement_level = 1',
 'Finding Main Ideas Review',
 'Comprehension',
 10,
 10,
 '{"activities": ["Extra reading practice", "Main idea games"]}'),

('ENRICHMENT',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 4 AND NodeNumber = 1),
 'quiz_score >= 90 AND placement_level = 3',
 'Critical Reading Challenge',
 'Comprehension',
 12,
 25,
 '{"activities": ["Complex text analysis", "Multiple main ideas"]}');

PRINT 'Module 4 supplemental nodes created';

-- =====================================================
-- MODULE 5: WRITING - SUPPLEMENTAL NODES
-- =====================================================

INSERT INTO [dbo].[SupplementalNodes] (NodeType, AfterNodeID, TriggerLogic, Title, SkillCategory, EstimatedDuration, XPReward, ContentJSON)
VALUES
('INTERVENTION',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber = 1),
 'quiz_score < 70',
 'Sentence Writing Remediation',
 'Writing',
 15,
 15,
 '{"activities": ["Guided sentence building", "Subject-verb matching", "Capitalization practice"]}'),

('SUPPLEMENTAL',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber = 1),
 'quiz_score >= 70 AND quiz_score < 80',
 'Sentence Writing Practice',
 'Writing',
 10,
 10,
 '{"activities": ["Extra sentence exercises", "Grammar review"]}'),

('ENRICHMENT',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 5 AND NodeNumber = 1),
 'quiz_score >= 90 AND placement_level = 3',
 'Creative Sentence Challenge',
 'Writing',
 12,
 25,
 '{"activities": ["Complex sentence creation", "Descriptive writing"]}');

PRINT 'Module 5 supplemental nodes created';

-- =====================================================
-- UNIVERSAL INTERVENTION NODE (For Multiple Failures)
-- =====================================================

INSERT INTO [dbo].[SupplementalNodes] (NodeType, AfterNodeID, TriggerLogic, Title, SkillCategory, EstimatedDuration, XPReward, ContentJSON)
VALUES
('INTERVENTION',
 (SELECT NodeID FROM [dbo].[Nodes] WHERE ModuleID = 1 AND NodeNumber = 3),
 'quiz_score < 70 AND attempt_count >= 2',
 'Intensive Support Session',
 'Phonics',
 20,
 20,
 '{"activities": ["One-on-one guided practice", "Skill breakdown", "Confidence building"], "note": "Triggered after 2+ failed attempts"}');

PRINT 'Universal intervention nodes created';

-- =====================================================
-- SUMMARY
-- =====================================================

PRINT '';
PRINT '====================================================================';
PRINT 'SUPPLEMENTAL NODES CREATED!';
PRINT '====================================================================';
PRINT 'Node types created:';
PRINT '  - INTERVENTION: For quiz scores < 70% (required)';
PRINT '  - SUPPLEMENTAL: For borderline passes 70-79% (recommended)';
PRINT '  - ENRICHMENT: For mastery 90%+ (optional challenge)';
PRINT '';
PRINT 'Sample nodes created for:';
PRINT '  - Module 1, Nodes 1-2 (Phonics)';
PRINT '  - Module 2, Node 1 (Vocabulary)';
PRINT '  - Module 3, Node 1 (Grammar)';
PRINT '  - Module 4, Node 1 (Comprehension)';
PRINT '  - Module 5, Node 1 (Writing)';
PRINT '';
PRINT 'Total: 16 supplemental nodes';
PRINT '';
PRINT 'Trigger Logic Examples:';
PRINT '  - quiz_score < 70 → INTERVENTION';
PRINT '  - quiz_score >= 70 AND quiz_score < 80 AND placement_level = 1 → SUPPLEMENTAL';
PRINT '  - quiz_score >= 90 AND placement_level = 3 → ENRICHMENT';
PRINT '';
PRINT 'TO DO: Create supplemental nodes for remaining lessons as needed';
PRINT '====================================================================';
