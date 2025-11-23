-- Convert Pronunciation items from MCQ to Speak-type (Voice Recognition)
-- This removes AnswerChoices and sets ItemText to the word to pronounce

-- Very Easy (IDs 51-55)
UPDATE Items SET
    ItemText = 'cat',
    AnswerChoices = NULL,
    CorrectAnswer = 'cat',
    Definition = 'A small domesticated feline animal'
WHERE ItemID = 51;

UPDATE Items SET
    ItemText = 'day',
    AnswerChoices = NULL,
    CorrectAnswer = 'day',
    Definition = 'A period of 24 hours'
WHERE ItemID = 52;

UPDATE Items SET
    ItemText = 'ship',
    AnswerChoices = NULL,
    CorrectAnswer = 'ship',
    Definition = 'A large boat for traveling on water'
WHERE ItemID = 53;

UPDATE Items SET
    ItemText = 'sing',
    AnswerChoices = NULL,
    CorrectAnswer = 'sing',
    Definition = 'To make musical sounds with the voice'
WHERE ItemID = 54;

UPDATE Items SET
    ItemText = 'bed',
    AnswerChoices = NULL,
    CorrectAnswer = 'bed',
    Definition = 'A piece of furniture for sleeping'
WHERE ItemID = 55;

-- Easy (IDs 56-60)
UPDATE Items SET
    ItemText = 'knight',
    AnswerChoices = NULL,
    CorrectAnswer = 'knight',
    Definition = 'A medieval soldier who served a lord'
WHERE ItemID = 56;

UPDATE Items SET
    ItemText = 'think',
    AnswerChoices = NULL,
    CorrectAnswer = 'think',
    Definition = 'To use the mind to consider something'
WHERE ItemID = 57;

UPDATE Items SET
    ItemText = 'meet',
    AnswerChoices = NULL,
    CorrectAnswer = 'meet',
    Definition = 'To come together with someone'
WHERE ItemID = 58;

UPDATE Items SET
    ItemText = 'banana',
    AnswerChoices = NULL,
    CorrectAnswer = 'banana',
    Definition = 'A long curved yellow fruit'
WHERE ItemID = 59;

UPDATE Items SET
    ItemText = 'sit',
    AnswerChoices = NULL,
    CorrectAnswer = 'sit',
    Definition = 'To rest with your weight on your bottom'
WHERE ItemID = 60;

-- Medium (IDs 61-65)
UPDATE Items SET
    ItemText = 'enough',
    AnswerChoices = NULL,
    CorrectAnswer = 'enough',
    Definition = 'As much as is necessary'
WHERE ItemID = 61;

UPDATE Items SET
    ItemText = 'about',
    AnswerChoices = NULL,
    CorrectAnswer = 'about',
    Definition = 'On the subject of; concerning'
WHERE ItemID = 62;

UPDATE Items SET
    ItemText = 'begin',
    AnswerChoices = NULL,
    CorrectAnswer = 'begin',
    Definition = 'To start or commence'
WHERE ItemID = 63;

UPDATE Items SET
    ItemText = 'bear',
    AnswerChoices = NULL,
    CorrectAnswer = 'bear',
    Phonetic = '/beər/',
    Definition = 'A large heavy mammal with thick fur'
WHERE ItemID = 64;

UPDATE Items SET
    ItemText = 'climb',
    AnswerChoices = NULL,
    CorrectAnswer = 'climb',
    Definition = 'To go up or ascend'
WHERE ItemID = 65;

-- Hard (IDs 66-70)
UPDATE Items SET
    ItemText = 'house',
    AnswerChoices = NULL,
    CorrectAnswer = 'house',
    Definition = 'A building for people to live in'
WHERE ItemID = 66;

UPDATE Items SET
    ItemText = 'thought',
    AnswerChoices = NULL,
    CorrectAnswer = 'thought',
    Definition = 'An idea or opinion produced by thinking'
WHERE ItemID = 67;

UPDATE Items SET
    ItemText = 'understand',
    AnswerChoices = NULL,
    CorrectAnswer = 'understand',
    Definition = 'To comprehend the meaning of something'
WHERE ItemID = 68;

UPDATE Items SET
    ItemText = 'photography',
    AnswerChoices = NULL,
    CorrectAnswer = 'photography',
    Definition = 'The art of taking photographs'
WHERE ItemID = 69;

UPDATE Items SET
    ItemText = 'measure',
    AnswerChoices = NULL,
    CorrectAnswer = 'measure',
    Definition = 'To determine the size or amount of something'
WHERE ItemID = 70;

-- Very Hard (IDs 71-75)
UPDATE Items SET
    ItemText = 'entrepreneur',
    AnswerChoices = NULL,
    CorrectAnswer = 'entrepreneur',
    Phonetic = '/ˌɒntrəprəˈnɜːr/',
    Definition = 'A person who starts and runs a business'
WHERE ItemID = 71;

UPDATE Items SET
    ItemText = 'bureaucracy',
    AnswerChoices = NULL,
    CorrectAnswer = 'bureaucracy',
    Phonetic = '/bjʊəˈrɒkrəsi/',
    Definition = 'A system of government with many rules and procedures'
WHERE ItemID = 72;

UPDATE Items SET
    ItemText = 'conscientious',
    AnswerChoices = NULL,
    CorrectAnswer = 'conscientious',
    Phonetic = '/ˌkɒnʃiˈenʃəs/',
    Definition = 'Wishing to do what is right; thorough and careful'
WHERE ItemID = 73;

UPDATE Items SET
    ItemText = 'onomatopoeia',
    AnswerChoices = NULL,
    CorrectAnswer = 'onomatopoeia',
    Phonetic = '/ˌɒnəˌmætəˈpiːə/',
    Definition = 'A word that sounds like what it describes'
WHERE ItemID = 74;

UPDATE Items SET
    ItemText = 'pneumonia',
    AnswerChoices = NULL,
    CorrectAnswer = 'pneumonia',
    Phonetic = '/njuːˈməʊniə/',
    Definition = 'A serious infection of the lungs'
WHERE ItemID = 75;

-- Verify the changes
SELECT ItemID, ItemText, ItemType, Phonetic, Definition, AnswerChoices, DifficultyLevel
FROM Items
WHERE ItemType = 'Pronunciation'
ORDER BY ItemID;
