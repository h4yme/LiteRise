-- =============================================================
-- LiteRise Quiz Questions Fix Script
-- Fixes 14 problematic quiz questions in the database
-- Run this on SQL Server (LiteRiseDB)
-- =============================================================

USE LiteRiseDB;
GO

-- ── FIX 1: Q1293 — Duplicate options ["bring","bring"] → ["bring","take"]
-- Lesson 107: Narrative Sight Words: Action and Sequence
UPDATE QuizQuestions
SET OptionsJSON = '["bring","take"]'
WHERE QuestionID = 1293
  AND NodeID = 107;
GO

-- ── FIX 2: Q1377–Q1386 — sentence_arrange → multiple_choice
-- Lesson 302: Sequencing Words in Sentences
-- The sentence_arrange question type is not supported by the quiz engine.
-- Converted to multiple_choice with full-sentence options.

UPDATE QuizQuestions
SET QuestionType = 'multiple_choice',
    QuestionText = 'Choose the correct word order: is / Manila / big',
    OptionsJSON  = '["Manila is big.","Big Manila is."]',
    CorrectAnswer = 'Manila is big.'
WHERE QuestionID = 1377
  AND NodeID = 302;

UPDATE QuizQuestions
SET QuestionType = 'multiple_choice',
    QuestionText = 'Choose the correct word order: mango / The / sweet / is',
    OptionsJSON  = '["The mango is sweet.","Sweet the mango is."]',
    CorrectAnswer = 'The mango is sweet.'
WHERE QuestionID = 1378
  AND NodeID = 302;

UPDATE QuizQuestions
SET QuestionType = 'multiple_choice',
    QuestionText = 'Choose the correct word order: plants / The / rice / farmer',
    OptionsJSON  = '["The farmer plants rice.","Rice the farmer plants."]',
    CorrectAnswer = 'The farmer plants rice.'
WHERE QuestionID = 1380
  AND NodeID = 302;

UPDATE QuizQuestions
SET QuestionType = 'multiple_choice',
    QuestionText = 'Choose the correct word order: colorful / festival / The / is',
    OptionsJSON  = '["The festival is colorful.","Colorful the festival is."]',
    CorrectAnswer = 'The festival is colorful.'
WHERE QuestionID = 1382
  AND NodeID = 302;

UPDATE QuizQuestions
SET QuestionType = 'multiple_choice',
    QuestionText = 'Choose the correct word order: teacher / My / kind / is',
    OptionsJSON  = '["My teacher is kind.","Is my teacher kind."]',
    CorrectAnswer = 'My teacher is kind.'
WHERE QuestionID = 1384
  AND NodeID = 302;

UPDATE QuizQuestions
SET QuestionType = 'multiple_choice',
    QuestionText = 'Choose the correct word order: swim / Fish / water / in',
    OptionsJSON  = '["Fish swim in water.","Water swim Fish in."]',
    CorrectAnswer = 'Fish swim in water.'
WHERE QuestionID = 1385
  AND NodeID = 302;

UPDATE QuizQuestions
SET QuestionType = 'multiple_choice',
    QuestionText = 'Choose the correct word order: play / We / the / park / in',
    OptionsJSON  = '["We play in the park.","In the park we play."]',
    CorrectAnswer = 'We play in the park.'
WHERE QuestionID = 1386
  AND NodeID = 302;
GO

-- ── FIX 3: Q1441 — sequence → multiple_choice
-- Lesson 308: Discourse Markers: Time Order Words
-- The sequence question type is not supported by the quiz engine.
-- Converted to multiple_choice asking for correct ordering.

UPDATE QuizQuestions
SET QuestionType  = 'multiple_choice',
    QuestionText  = 'What is the correct order of these time-order words?',
    OptionsJSON   = '["First, Next, Finally","Next, First, Finally","Finally, Next, First"]',
    CorrectAnswer = 'First, Next, Finally'
WHERE QuestionID = 1441
  AND NodeID = 308;
GO

-- ── FIX 4: Q1622–Q1626 — Wrong NodeID: 202 → 203
-- These Math/Science vocabulary questions belong to lesson 203
-- "Content-Specific Words: Math and Science", not lesson 202
-- "Regional and National Theme Vocabulary".

UPDATE QuizQuestions
SET NodeID = 203
WHERE QuestionID IN (1622, 1623, 1624, 1625, 1626)
  AND NodeID = 202;
GO

-- ── VERIFICATION QUERIES (optional — run to confirm fixes applied)
-- SELECT QuestionID, NodeID, QuestionType, OptionsJSON, CorrectAnswer
-- FROM QuizQuestions
-- WHERE QuestionID IN (1293, 1377, 1378, 1380, 1382, 1384, 1385, 1386, 1441, 1622, 1623, 1624, 1625, 1626)
-- ORDER BY QuestionID;
