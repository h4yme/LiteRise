-- LiteRise: Speak-Type Pronunciation Items
-- These items require the student to speak the word (no MCQ options)
-- The Android app will show the microphone interface for these items

-- =============================================================================
-- SPEAK-TYPE PRONUNCIATION ITEMS (14 items to bring total to 100)
-- =============================================================================

-- Very Easy (theta ~ -2.5 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, Definition, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('cat', 'Pronunciation', 'Very Easy', 'cat', NULL, '/kæt/', 'A small domesticated carnivorous mammal with soft fur', 1.2, -2.3, 0.0),
('dog', 'Pronunciation', 'Very Easy', 'dog', NULL, '/dɒɡ/', 'A domesticated carnivorous mammal with four legs', 1.1, -2.1, 0.0),
('sun', 'Pronunciation', 'Very Easy', 'sun', NULL, '/sʌn/', 'The star around which the Earth orbits', 1.3, -1.9, 0.0);

-- Easy (theta ~ -1.5 to -0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, Definition, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('apple', 'Pronunciation', 'Easy', 'apple', NULL, '/ˈæp.əl/', 'A round fruit with red, green, or yellow skin', 1.4, -1.4, 0.0),
('happy', 'Pronunciation', 'Easy', 'happy', NULL, '/ˈhæp.i/', 'Feeling or showing pleasure or contentment', 1.5, -1.1, 0.0),
('water', 'Pronunciation', 'Easy', 'water', NULL, '/ˈwɔː.tər/', 'A clear liquid essential for life', 1.3, -0.8, 0.0);

-- Medium (theta ~ -0.5 to 0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, Definition, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('beautiful', 'Pronunciation', 'Medium', 'beautiful', NULL, '/ˈbjuː.tɪ.fəl/', 'Pleasing the senses or mind aesthetically', 1.6, -0.3, 0.0),
('important', 'Pronunciation', 'Medium', 'important', NULL, '/ɪmˈpɔː.tənt/', 'Of great significance or value', 1.7, 0.0, 0.0),
('different', 'Pronunciation', 'Medium', 'different', NULL, '/ˈdɪf.ər.ənt/', 'Not the same as another or each other', 1.5, 0.3, 0.0);

-- Hard (theta ~ 0.5 to 1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, Definition, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('pronunciation', 'Pronunciation', 'Hard', 'pronunciation', NULL, '/prəˌnʌn.siˈeɪ.ʃən/', 'The way in which a word is pronounced', 1.8, 0.8, 0.0),
('comfortable', 'Pronunciation', 'Hard', 'comfortable', NULL, '/ˈkʌm.fə.tə.bəl/', 'Providing physical ease and relaxation', 1.7, 1.1, 0.0),
('vocabulary', 'Pronunciation', 'Hard', 'vocabulary', NULL, '/vəˈkæb.jə.ler.i/', 'The body of words used in a particular language', 1.9, 1.4, 0.0);

-- Very Hard (theta ~ 1.5 to 2.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, Definition, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('onomatopoeia', 'Pronunciation', 'Very Hard', 'onomatopoeia', NULL, '/ˌɒn.ə.mæt.əˈpiː.ə/', 'The formation of words that imitate sounds', 2.0, 1.8, 0.0),
('entrepreneurship', 'Pronunciation', 'Very Hard', 'entrepreneurship', NULL, '/ˌɒn.trə.prəˈnɜː.ʃɪp/', 'The activity of setting up businesses', 1.9, 2.2, 0.0);

-- =============================================================================
-- VERIFICATION: Run this to check the distribution of pronunciation items
-- =============================================================================
-- SELECT
--     ItemType,
--     CASE WHEN AnswerChoices IS NULL THEN 'Speak' ELSE 'MCQ' END AS SubType,
--     COUNT(*) as ItemCount
-- FROM Items
-- WHERE ItemType = 'Pronunciation'
-- GROUP BY ItemType, CASE WHEN AnswerChoices IS NULL THEN 'Speak' ELSE 'MCQ' END;
