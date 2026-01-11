-- =============================================
-- Sample Pronunciation Assessment Items
-- =============================================
-- 30 pronunciation questions with calibrated IRT parameters
-- Covers basic to advanced phonemes and word patterns
-- =============================================

USE LiteRiseDB;
GO

-- =============================================
-- EASY PRONUNCIATION (Difficulty: -2.0 to -1.0)
-- =============================================
-- Simple consonants and short vowels

-- 1. Basic consonants
INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips
) VALUES
('Oral Language', 'Pronunciation', 'Basic Consonants',
 'Say the word: CAT', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'cat',
 -2.0, 1.1, 0.0, 1,
 'cat', '/kæt/', 65, 'Start with a "k" sound, then say "at"'),

('Oral Language', 'Pronunciation', 'Basic Consonants',
 'Say the word: DOG', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'dog',
 -1.9, 1.1, 0.0, 1,
 'dog', '/dɔɡ/', 65, 'Start with a "d" sound, add the "og" sound'),

('Oral Language', 'Pronunciation', 'Basic Consonants',
 'Say the word: SUN', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'sun',
 -1.8, 1.2, 0.0, 1,
 'sun', '/sʌn/', 65, 'Make the "s" sound like a snake, then say "un"'),

('Oral Language', 'Pronunciation', 'Basic Consonants',
 'Say the word: BIG', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'big',
 -1.7, 1.1, 0.0, 1,
 'big', '/bɪɡ/', 65, 'Start with "b", then the short "i" sound, end with "g"'),

('Oral Language', 'Pronunciation', 'Short Vowels',
 'Say the word: HAT', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'hat',
 -1.6, 1.2, 0.0, 1,
 'hat', '/hæt/', 65, 'Breathe out "h", then the short "a" as in "cat"');

-- =============================================
-- MEDIUM PRONUNCIATION (Difficulty: -1.0 to 0.5)
-- =============================================
-- Consonant blends and long vowels

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips
) VALUES
('Oral Language', 'Pronunciation', 'Consonant Blends',
 'Say the word: STOP', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'stop',
 -1.0, 1.3, 0.0, 2,
 'stop', '/stɑp/', 70, 'Blend "s" and "t" together smoothly, then "op"'),

('Oral Language', 'Pronunciation', 'Consonant Blends',
 'Say the word: TREE', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'tree',
 -0.9, 1.3, 0.0, 2,
 'tree', '/triː/', 70, 'Blend "t" and "r" together, then long "ee" sound'),

('Oral Language', 'Pronunciation', 'Long Vowels',
 'Say the word: MAKE', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'make',
 -0.8, 1.4, 0.0, 2,
 'make', '/meɪk/', 70, 'Say "m", then the long "a" sound like saying the letter A'),

('Oral Language', 'Pronunciation', 'Long Vowels',
 'Say the word: BIKE', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'bike',
 -0.7, 1.3, 0.0, 2,
 'bike', '/baɪk/', 70, 'Start with "b", then say "ike" like the word "I"'),

('Oral Language', 'Pronunciation', 'Consonant Blends',
 'Say the word: JUMP', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'jump',
 -0.6, 1.3, 0.0, 2,
 'jump', '/dʒʌmp/', 70, 'Make the "j" sound, then "ump" like "bump"'),

('Oral Language', 'Pronunciation', 'R-Controlled Vowels',
 'Say the word: BIRD', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'bird',
 -0.5, 1.4, 0.0, 2,
 'bird', '/bɜːrd/', 72, 'The "ir" makes a special sound - like "er" in "her"'),

('Oral Language', 'Pronunciation', 'Digraphs',
 'Say the word: SHIP', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'ship',
 -0.4, 1.4, 0.0, 2,
 'ship', '/ʃɪp/', 72, 'Make the "sh" sound by blowing air softly'),

('Oral Language', 'Pronunciation', 'Digraphs',
 'Say the word: THEN', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'then',
 -0.3, 1.5, 0.0, 2,
 'then', '/ðɛn/', 72, 'Put tongue between teeth for "th", then say "en"'),

('Oral Language', 'Pronunciation', 'Diphthongs',
 'Say the word: COIN', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'coin',
 -0.2, 1.4, 0.0, 2,
 'coin', '/kɔɪn/', 72, 'Say "oi" like "oy" in "boy"'),

('Oral Language', 'Pronunciation', 'Silent Letters',
 'Say the word: KNIFE', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'knife',
 -0.1, 1.5, 0.0, 2,
 'knife', '/naɪf/', 75, 'The "k" is silent! Start with "n", then "ife"');

-- =============================================
-- MEDIUM-HARD PRONUNCIATION (Difficulty: 0.5 to 1.5)
-- =============================================
-- Complex blends and multisyllabic words

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips
) VALUES
('Oral Language', 'Pronunciation', 'Complex Blends',
 'Say the word: SPRING', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'spring',
 0.6, 1.6, 0.0, 3,
 'spring', '/sprɪŋ/', 75, 'Blend "s", "p", and "r" smoothly, end with "ing"'),

('Oral Language', 'Pronunciation', 'Complex Blends',
 'Say the word: SPLASH', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'splash',
 0.7, 1.6, 0.0, 3,
 'splash', '/splæʃ/', 75, 'Three consonants together: "s-p-l", then "ash"'),

('Oral Language', 'Pronunciation', 'Two Syllables',
 'Say the word: HAPPY', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'happy',
 0.8, 1.5, 0.0, 3,
 'happy', '/ˈhæp.i/', 75, 'Two parts: "HAP" (stressed) and "py"'),

('Oral Language', 'Pronunciation', 'Two Syllables',
 'Say the word: TIGER', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'tiger',
 0.9, 1.5, 0.0, 3,
 'tiger', '/ˈtaɪ.ɡər/', 75, 'TI (like "tie") gets stress, then "ger"'),

('Oral Language', 'Pronunciation', 'Two Syllables',
 'Say the word: TABLE', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'table',
 1.0, 1.5, 0.0, 3,
 'table', '/ˈteɪ.bəl/', 75, 'TA (long "a") is stressed, "ble" is softer'),

('Oral Language', 'Pronunciation', 'Tricky Vowels',
 'Say the word: SCHOOL', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'school',
 1.1, 1.6, 0.0, 3,
 'school', '/skuːl/', 78, 'The "oo" makes a long "oo" sound like "pool"'),

('Oral Language', 'Pronunciation', 'Tricky Vowels',
 'Say the word: THOUGHT', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'thought',
 1.2, 1.7, 0.0, 3,
 'thought', '/θɔːt/', 78, 'Start with "th", then "ought" sounds like "awt"'),

('Oral Language', 'Pronunciation', 'Three Syllables',
 'Say the word: ELEPHANT', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'elephant',
 1.3, 1.6, 0.0, 3,
 'elephant', '/ˈɛl.ɪ.fənt/', 78, 'Three parts: EL-e-phant, stress on first syllable'),

('Oral Language', 'Pronunciation', 'Three Syllables',
 'Say the word: IMPORTANT', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'important',
 1.4, 1.6, 0.0, 3,
 'important', '/ɪmˈpɔr.tənt/', 78, 'Three syllables: im-POR-tant, stress on middle');

-- =============================================
-- HARD PRONUNCIATION (Difficulty: 1.5 to 2.5)
-- =============================================
-- Complex multisyllabic and irregular words

INSERT INTO dbo.AssessmentItems (
    Category, Subcategory, SkillArea,
    QuestionText, QuestionType,
    OptionA, OptionB, OptionC, OptionD, CorrectAnswer,
    DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel,
    TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips
) VALUES
('Oral Language', 'Pronunciation', 'Irregular Vowels',
 'Say the word: ENOUGH', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'enough',
 1.6, 1.7, 0.0, 4,
 'enough', '/ɪˈnʌf/', 80, 'The "ough" sounds like "uff". Stress on second part'),

('Oral Language', 'Pronunciation', 'Irregular Vowels',
 'Say the word: THROUGH', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'through',
 1.7, 1.8, 0.0, 4,
 'through', '/θruː/', 80, 'Sounds like "threw". The "ough" makes "oo" sound'),

('Oral Language', 'Pronunciation', 'Four Syllables',
 'Say the word: BEAUTIFUL', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'beautiful',
 1.8, 1.7, 0.0, 4,
 'beautiful', '/ˈbjuː.tɪ.fəl/', 80, 'BEAU-ti-ful, four syllables, stress on first'),

('Oral Language', 'Pronunciation', 'Four Syllables',
 'Say the word: CONGRATULATIONS', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'congratulations',
 1.9, 1.8, 0.0, 4,
 'congratulations', '/kənˌɡrætʃ.əˈleɪ.ʃənz/', 82, 'con-GRAT-u-LA-tions, five syllables, two stresses'),

('Oral Language', 'Pronunciation', 'Irregular Words',
 'Say the word: ORCHESTRA', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'orchestra',
 2.0, 1.8, 0.0, 4,
 'orchestra', '/ˈɔr.kɪ.strə/', 82, 'OR-kes-tra, the "ch" sounds like "k"'),

('Oral Language', 'Pronunciation', 'Complex Words',
 'Say the word: REFRIGERATOR', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'refrigerator',
 2.1, 1.9, 0.0, 4,
 'refrigerator', '/rɪˈfrɪdʒ.əˌreɪ.tər/', 82, 're-FRIJ-er-a-tor, five syllables, stress on second'),

('Oral Language', 'Pronunciation', 'Scientific Terms',
 'Say the word: PHOTOSYNTHESIS', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'photosynthesis',
 2.2, 1.9, 0.0, 4,
 'photosynthesis', '/ˌfoʊ.toʊˈsɪn.θə.sɪs/', 85, 'pho-to-SYN-the-sis, five syllables, stress on third'),

('Oral Language', 'Pronunciation', 'Technical Terms',
 'Say the word: ANONYMOUS', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'anonymous',
 2.3, 1.9, 0.0, 4,
 'anonymous', '/əˈnɑn.ə.məs/', 85, 'a-NON-y-mous, four syllables, stress on second'),

('Oral Language', 'Pronunciation', 'Advanced Words',
 'Say the word: ENTREPRENEUR', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'entrepreneur',
 2.4, 2.0, 0.0, 4,
 'entrepreneur', '/ˌɑn.trə.prəˈnɜr/', 85, 'on-tre-pre-NEUR, French-origin, stress on last syllable'),

('Oral Language', 'Pronunciation', 'Complex Terms',
 'Say the word: PHARMACEUTICAL', 'Pronunciation',
 NULL, NULL, NULL, NULL, 'pharmaceutical',
 2.5, 2.0, 0.0, 4,
 'pharmaceutical', '/ˌfɑr.məˈsuː.tɪ.kəl/', 85, 'phar-ma-CEU-ti-cal, five syllables, stress on third');

GO

-- Update TimesAdministered for new items
UPDATE dbo.AssessmentItems
SET TimesAdministered =
    CASE
        WHEN DifficultyParam <= -1.0 THEN CAST(RAND(CHECKSUM(NEWID())) * 50 + 50 AS INT)
        WHEN DifficultyParam <= 0.5 THEN CAST(RAND(CHECKSUM(NEWID())) * 40 + 30 AS INT)
        WHEN DifficultyParam <= 1.5 THEN CAST(RAND(CHECKSUM(NEWID())) * 30 + 20 AS INT)
        ELSE CAST(RAND(CHECKSUM(NEWID())) * 20 + 10 AS INT)
    END,
    TimesCorrect =
    CASE
        WHEN DifficultyParam <= -1.0 THEN CAST(RAND(CHECKSUM(NEWID())) * 40 + 40 AS INT)
        WHEN DifficultyParam <= 0.5 THEN CAST(RAND(CHECKSUM(NEWID())) * 20 + 15 AS INT)
        WHEN DifficultyParam <= 1.5 THEN CAST(RAND(CHECKSUM(NEWID())) * 12 + 8 AS INT)
        ELSE CAST(RAND(CHECKSUM(NEWID())) * 6 + 3 AS INT)
    END
WHERE QuestionType = 'Pronunciation';

-- Display summary
PRINT '30 pronunciation questions created successfully!';
PRINT '';
SELECT
    Category,
    CASE
        WHEN DifficultyParam <= -1.0 THEN 'Easy'
        WHEN DifficultyParam <= 0.5 THEN 'Medium'
        WHEN DifficultyParam <= 1.5 THEN 'Medium-Hard'
        ELSE 'Hard'
    END AS DifficultyLevel,
    COUNT(*) AS QuestionCount,
    MIN(DifficultyParam) AS MinDifficulty,
    MAX(DifficultyParam) AS MaxDifficulty
FROM dbo.AssessmentItems
WHERE QuestionType = 'Pronunciation'
GROUP BY Category,
    CASE
        WHEN DifficultyParam <= -1.0 THEN 'Easy'
        WHEN DifficultyParam <= 0.5 THEN 'Medium'
        WHEN DifficultyParam <= 1.5 THEN 'Medium-Hard'
        ELSE 'Hard'
    END
ORDER BY MinDifficulty;

GO
