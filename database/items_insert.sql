-- LiteRise: 100-Item Pool for CAT Assessment
-- Drop and recreate Items table with proper structure

-- Drop foreign key constraints referencing Items table
DECLARE @sql NVARCHAR(MAX) = '';
SELECT @sql += 'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + '.' + QUOTENAME(OBJECT_NAME(parent_object_id)) + ' DROP CONSTRAINT ' + QUOTENAME(name) + ';'
FROM sys.foreign_keys
WHERE referenced_object_id = OBJECT_ID('Items');
EXEC sp_executesql @sql;
GO

-- Drop existing Items table
IF OBJECT_ID('Items', 'U') IS NOT NULL DROP TABLE Items;
GO

-- Create Items table
CREATE TABLE Items (
    ItemID INT IDENTITY(1,1) PRIMARY KEY,
    ItemText NVARCHAR(500) NOT NULL,
    ItemType NVARCHAR(50) NOT NULL,
    DifficultyLevel NVARCHAR(20) NOT NULL,
    DifficultyParam FLOAT NOT NULL,
    DiscriminationParam FLOAT NOT NULL DEFAULT 1.0,
    GuessingParam FLOAT NOT NULL DEFAULT 0.25,
    CorrectAnswer NVARCHAR(255) NOT NULL,
    AnswerChoices NVARCHAR(MAX) NULL,
    Phonetic NVARCHAR(100) NULL,
    Definition NVARCHAR(500) NULL,
    ImageURL NVARCHAR(255) NULL,
    AudioURL NVARCHAR(255) NULL,
    GradeLevel INT NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME NOT NULL DEFAULT GETDATE()
);
GO

-- =============================================================================
-- SPELLING ITEMS (25 items)
-- =============================================================================

-- Very Easy Spelling (5 items, GradeLevel 4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('Which is spelled correctly?', 'Spelling', 'Very Easy', -2.4, 1.2, 0.25, 'cat', '["cat", "kat", "catt", "katt"]', 4),
('Which is spelled correctly?', 'Spelling', 'Very Easy', -2.2, 1.1, 0.25, 'dog', '["dag", "dog", "dogg", "doge"]', 4),
('Which is spelled correctly?', 'Spelling', 'Very Easy', -2.0, 1.3, 0.25, 'run', '["run", "runn", "rune", "ruhn"]', 4),
('Which is spelled correctly?', 'Spelling', 'Very Easy', -1.8, 1.2, 0.25, 'book', '["buk", "bouk", "book", "bokk"]', 4),
('Which is spelled correctly?', 'Spelling', 'Very Easy', -1.6, 1.4, 0.25, 'happy', '["hapy", "happy", "happi", "heppy"]', 4);

-- Easy Spelling (5 items, GradeLevel 4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('Which is spelled correctly?', 'Spelling', 'Easy', -1.4, 1.5, 0.25, 'friend', '["freind", "friend", "frend", "frind"]', 4),
('Which is spelled correctly?', 'Spelling', 'Easy', -1.2, 1.3, 0.25, 'because', '["becuase", "becouse", "because", "becase"]', 4),
('Which is spelled correctly?', 'Spelling', 'Easy', -1.0, 1.4, 0.25, 'people', '["people", "peaple", "poeple", "peopel"]', 4),
('Which is spelled correctly?', 'Spelling', 'Easy', -0.8, 1.6, 0.25, 'school', '["school", "scool", "skool", "schol"]', 4),
('Which is spelled correctly?', 'Spelling', 'Easy', -0.6, 1.5, 0.25, 'different', '["diferent", "diffrent", "different", "differant"]', 4);

-- Medium Spelling (5 items, GradeLevel 5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('Which is spelled correctly?', 'Spelling', 'Medium', -0.3, 1.7, 0.25, 'necessary', '["neccessary", "necessary", "necesary", "neccesary"]', 5),
('Which is spelled correctly?', 'Spelling', 'Medium', 0.0, 1.8, 0.25, 'definitely', '["definately", "definitly", "definitely", "definatley"]', 5),
('Which is spelled correctly?', 'Spelling', 'Medium', 0.2, 1.6, 0.25, 'occurrence', '["occurence", "occurrence", "occurance", "occurrance"]', 5),
('Which is spelled correctly?', 'Spelling', 'Medium', 0.4, 1.5, 0.25, 'receive', '["recieve", "receive", "receve", "receeve"]', 5),
('Which is spelled correctly?', 'Spelling', 'Medium', 0.5, 1.7, 0.25, 'beautiful', '["beatiful", "beutiful", "beautiful", "beautful"]', 5);

-- Hard Spelling (5 items, GradeLevel 6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('Which is spelled correctly?', 'Spelling', 'Hard', 0.7, 1.8, 0.25, 'accommodate', '["accomodate", "acommodate", "accommodate", "acomodate"]', 6),
('Which is spelled correctly?', 'Spelling', 'Hard', 1.0, 1.9, 0.25, 'conscientious', '["conscientious", "consciencious", "conscentious", "conciencious"]', 6),
('Which is spelled correctly?', 'Spelling', 'Hard', 1.2, 1.7, 0.25, 'maintenance', '["maintainance", "maintenence", "maintenance", "maintanence"]', 6),
('Which is spelled correctly?', 'Spelling', 'Hard', 1.4, 1.8, 0.25, 'pronunciation', '["pronounciation", "pronunciation", "prononciation", "pronuciation"]', 6),
('Which is spelled correctly?', 'Spelling', 'Hard', 1.5, 1.6, 0.25, 'embarrass', '["embarrass", "embarass", "embarras", "embaress"]', 6);

-- Very Hard Spelling (5 items, GradeLevel 6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('Which is spelled correctly?', 'Spelling', 'Very Hard', 1.7, 2.0, 0.25, 'onomatopoeia', '["onomatopeia", "onomatopoeia", "onomatapoeia", "onomatopea"]', 6),
('Which is spelled correctly?', 'Spelling', 'Very Hard', 2.0, 1.9, 0.25, 'entrepreneur', '["entrepeneur", "entreprenuer", "entrepreneur", "entreprenur"]', 6),
('Which is spelled correctly?', 'Spelling', 'Very Hard', 2.2, 1.8, 0.25, 'pneumonia', '["neumonia", "pnemonia", "pneumonia", "pnuemonia"]', 6),
('Which is spelled correctly?', 'Spelling', 'Very Hard', 2.4, 2.0, 0.25, 'hemorrhage', '["hemmorage", "hemorrhage", "hemorage", "hemorrage"]', 6),
('Which is spelled correctly?', 'Spelling', 'Very Hard', 2.5, 1.9, 0.25, 'bureaucracy', '["beaurocracy", "bureacracy", "bureaucracy", "buearucracy"]', 6);

-- =============================================================================
-- GRAMMAR ITEMS (25 items)
-- =============================================================================

-- Very Easy Grammar (5 items, GradeLevel 4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('She ___ a student.', 'Grammar', 'Very Easy', -2.3, 1.2, 0.25, 'is', '["is", "are", "am", "be"]', 4),
('I ___ to school every day.', 'Grammar', 'Very Easy', -2.1, 1.3, 0.25, 'go', '["goes", "going", "go", "went"]', 4),
('They ___ playing outside.', 'Grammar', 'Very Easy', -1.9, 1.1, 0.25, 'are', '["is", "are", "am", "was"]', 4),
('He ___ a ball.', 'Grammar', 'Very Easy', -1.7, 1.4, 0.25, 'has', '["have", "has", "had", "having"]', 4),
('The cat ___ on the mat.', 'Grammar', 'Very Easy', -1.6, 1.2, 0.25, 'sits', '["sit", "sits", "sitting", "sitted"]', 4);

-- Easy Grammar (5 items, GradeLevel 4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('The children ___ in the park yesterday.', 'Grammar', 'Easy', -1.3, 1.5, 0.25, 'played', '["play", "plays", "played", "playing"]', 4),
('She ___ her homework before dinner.', 'Grammar', 'Easy', -1.1, 1.4, 0.25, 'finished', '["finish", "finishes", "finished", "finishing"]', 4),
('My mother ___ delicious food.', 'Grammar', 'Easy', -0.9, 1.3, 0.25, 'cooks', '["cook", "cooks", "cooked", "cooking"]', 4),
('We ___ to the movies last weekend.', 'Grammar', 'Easy', -0.7, 1.5, 0.25, 'went', '["go", "goes", "went", "going"]', 4),
('The birds ___ in the sky.', 'Grammar', 'Easy', -0.6, 1.4, 0.25, 'fly', '["fly", "flies", "flying", "flied"]', 4);

-- Medium Grammar (5 items, GradeLevel 5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('If I ___ rich, I would travel the world.', 'Grammar', 'Medium', -0.4, 1.7, 0.25, 'were', '["am", "was", "were", "be"]', 5),
('She asked me where I ___ going.', 'Grammar', 'Medium', -0.1, 1.6, 0.25, 'was', '["am", "was", "were", "is"]', 5),
('The book ___ on the table since morning.', 'Grammar', 'Medium', 0.1, 1.8, 0.25, 'has been', '["is", "was", "has been", "had been"]', 5),
('Neither the teacher nor the students ___ present.', 'Grammar', 'Medium', 0.3, 1.7, 0.25, 'were', '["was", "were", "is", "are"]', 5),
('By the time we arrived, they ___ already left.', 'Grammar', 'Medium', 0.5, 1.6, 0.25, 'had', '["have", "has", "had", "having"]', 5);

-- Hard Grammar (5 items, GradeLevel 6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('Had I known earlier, I ___ have helped.', 'Grammar', 'Hard', 0.8, 1.9, 0.25, 'would', '["will", "would", "could", "should"]', 6),
('The news ___ shocking to everyone.', 'Grammar', 'Hard', 1.1, 1.8, 0.25, 'was', '["were", "was", "are", "have been"]', 6),
('Scarcely had he left ___ the phone rang.', 'Grammar', 'Hard', 1.3, 1.7, 0.25, 'when', '["than", "when", "then", "before"]', 6),
('It is essential that he ___ on time.', 'Grammar', 'Hard', 1.5, 1.9, 0.25, 'be', '["is", "was", "be", "being"]', 6),
('I wish I ___ taller.', 'Grammar', 'Hard', 1.4, 1.8, 0.25, 'were', '["was", "were", "am", "is"]', 6);

-- Very Hard Grammar (5 items, GradeLevel 6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('Were it not for his help, we ___ failed.', 'Grammar', 'Very Hard', 1.8, 2.0, 0.25, 'would have', '["will have", "would have", "could", "have"]', 6),
('The committee ___ divided in their opinions.', 'Grammar', 'Very Hard', 2.1, 1.9, 0.25, 'were', '["was", "were", "is", "has been"]', 6),
('Seldom ___ such dedication in students.', 'Grammar', 'Very Hard', 2.3, 2.0, 0.25, 'do we see', '["we see", "do we see", "we do see", "see we"]', 6),
('Not only ___ late, but he also forgot the documents.', 'Grammar', 'Very Hard', 2.4, 1.9, 0.25, 'was he', '["he was", "was he", "he is", "is he"]', 6),
('Had the weather ___ better, we would have gone.', 'Grammar', 'Very Hard', 2.5, 2.0, 0.25, 'been', '["be", "been", "being", "was"]', 6);

-- =============================================================================
-- PRONUNCIATION ITEMS (25 items)
-- =============================================================================

-- Very Easy Pronunciation (5 items, GradeLevel 4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, Phonetic, GradeLevel)
VALUES
('Which word has the same vowel sound as "cat"?', 'Pronunciation', 'Very Easy', -2.2, 1.2, 0.25, 'hat', '["hat", "hate", "hot", "hit"]', '/kæt/', 4),
('Which word rhymes with "day"?', 'Pronunciation', 'Very Easy', -2.0, 1.1, 0.25, 'play', '["play", "die", "do", "dew"]', '/deɪ/', 4),
('Which word starts with the same sound as "ship"?', 'Pronunciation', 'Very Easy', -1.8, 1.3, 0.25, 'shoe', '["see", "chip", "shoe", "sip"]', '/ʃɪp/', 4),
('Which word has the same ending sound as "sing"?', 'Pronunciation', 'Very Easy', -1.6, 1.2, 0.25, 'ring', '["ring", "sin", "sign", "thing"]', '/sɪŋ/', 4),
('Which word rhymes with "bed"?', 'Pronunciation', 'Very Easy', -1.5, 1.3, 0.25, 'red', '["red", "bead", "bid", "bad"]', '/bed/', 4);

-- Easy Pronunciation (5 items, GradeLevel 4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, Phonetic, GradeLevel)
VALUES
('Which word has a silent letter?', 'Pronunciation', 'Easy', -1.4, 1.5, 0.25, 'knight', '["kite", "knight", "night", "kit"]', '/naɪt/', 4),
('Which word has the "th" sound as in "think"?', 'Pronunciation', 'Easy', -1.2, 1.4, 0.25, 'thick', '["tick", "thick", "sick", "this"]', '/θɪŋk/', 4),
('Which word has a long "e" sound?', 'Pronunciation', 'Easy', -1.0, 1.3, 0.25, 'meet', '["met", "meet", "meat", "mitt"]', '/miːt/', 4),
('Which word has the same stress pattern as "banana"?', 'Pronunciation', 'Easy', -0.8, 1.5, 0.25, 'tomato', '["apple", "tomato", "orange", "mango"]', '/bəˈnænə/', 4),
('Which word has the short "i" sound?', 'Pronunciation', 'Easy', -0.6, 1.4, 0.25, 'sit', '["seat", "site", "sit", "set"]', '/sɪt/', 4);

-- Medium Pronunciation (5 items, GradeLevel 5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, Phonetic, GradeLevel)
VALUES
('In which word is "gh" pronounced as /f/?', 'Pronunciation', 'Medium', -0.3, 1.7, 0.25, 'enough', '["ghost", "through", "enough", "daughter"]', '/ɪˈnʌf/', 5),
('Which word has the schwa sound /ə/?', 'Pronunciation', 'Medium', 0.0, 1.6, 0.25, 'about', '["about", "out", "shout", "doubt"]', '/əˈbaʊt/', 5),
('Which word is stressed on the second syllable?', 'Pronunciation', 'Medium', 0.2, 1.8, 0.25, 'begin', '["happen", "begin", "open", "broken"]', '/bɪˈgɪn/', 5),
('Which pair of words are homophones?', 'Pronunciation', 'Medium', 0.4, 1.7, 0.25, 'bare/bear', '["bare/bear", "read/red", "live/leave", "close/clothes"]', '/beər/', 5),
('Which word has a silent "b"?', 'Pronunciation', 'Medium', 0.5, 1.6, 0.25, 'climb', '["cab", "climb", "crab", "club"]', '/klaɪm/', 5);

-- Hard Pronunciation (5 items, GradeLevel 6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, Phonetic, GradeLevel)
VALUES
('Which word contains the diphthong /aʊ/?', 'Pronunciation', 'Hard', 0.7, 1.8, 0.25, 'house', '["house", "horse", "hose", "whose"]', '/haʊs/', 6),
('In which word does "ough" sound like /ɔː/?', 'Pronunciation', 'Hard', 1.0, 1.9, 0.25, 'thought', '["though", "through", "thought", "tough"]', '/θɔːt/', 6),
('Which word has primary stress on the third syllable?', 'Pronunciation', 'Hard', 1.2, 1.7, 0.25, 'understand', '["beautiful", "understand", "comfortable", "interesting"]', '/ˌʌndəˈstænd/', 6),
('Which word pair shows vowel reduction?', 'Pronunciation', 'Hard', 1.4, 1.9, 0.25, 'photograph/photography', '["record/record", "photograph/photography", "present/present", "object/object"]', '/fəˈtɒgrəfi/', 6),
('Which word has the /ʒ/ sound?', 'Pronunciation', 'Hard', 1.5, 1.8, 0.25, 'measure', '["message", "measure", "master", "missile"]', '/ˈmeʒə/', 6);

-- Very Hard Pronunciation (5 items, GradeLevel 6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, Phonetic, GradeLevel)
VALUES
('Which word demonstrates linking-r?', 'Pronunciation', 'Very Hard', 1.7, 2.0, 0.25, 'far away', '["car park", "far away", "for you", "her book"]', '/fɑːr əˈweɪ/', 6),
('Identify the phrase with intrusive /r/', 'Pronunciation', 'Very Hard', 2.0, 1.9, 0.25, 'idea of', '["for it", "idea of", "there are", "more in"]', '/aɪˈdɪər əv/', 6),
('Which shows correct weak form usage?', 'Pronunciation', 'Very Hard', 2.2, 2.0, 0.25, 'cup of tea /əv/', '["cup of tea /ɒf/", "cup of tea /əv/", "glass of water /ɒv/", "piece of cake /ɒf/"]', '/kʌp əv tiː/', 6),
('Which word pair shows assimilation?', 'Pronunciation', 'Very Hard', 2.4, 1.9, 0.25, 'ten people /tem/', '["ten cats", "ten people /tem/", "ten dogs", "ten birds"]', '/tem ˈpiːpl/', 6),
('Which word has the /θ/ not /ð/ sound?', 'Pronunciation', 'Very Hard', 2.5, 2.0, 0.25, 'think', '["the", "this", "think", "that"]', '/θɪŋk/', 6);

-- =============================================================================
-- SYNTAX ITEMS (25 items) - Sentence Scramble
-- =============================================================================

-- Very Easy Syntax (5 items, GradeLevel 4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('is / cat / The / black', 'Syntax', 'Very Easy', -2.3, 1.2, 0.25, 'The cat is black', NULL, 4),
('like / I / apples', 'Syntax', 'Very Easy', -2.1, 1.1, 0.25, 'I like apples', NULL, 4),
('is / He / happy', 'Syntax', 'Very Easy', -1.9, 1.3, 0.25, 'He is happy', NULL, 4),
('play / We / games', 'Syntax', 'Very Easy', -1.7, 1.2, 0.25, 'We play games', NULL, 4),
('red / is / apple / the', 'Syntax', 'Very Easy', -1.6, 1.3, 0.25, 'The apple is red', NULL, 4);

-- Easy Syntax (5 items, GradeLevel 4)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('the / plays / in / She / park', 'Syntax', 'Easy', -1.4, 1.5, 0.25, 'She plays in the park', NULL, 4),
('reading / is / a / He / book', 'Syntax', 'Easy', -1.2, 1.4, 0.25, 'He is reading a book', NULL, 4),
('to / go / school / I / every / day', 'Syntax', 'Easy', -1.0, 1.3, 0.25, 'I go to school every day', NULL, 4),
('mother / My / delicious / cooks / food', 'Syntax', 'Easy', -0.8, 1.5, 0.25, 'My mother cooks delicious food', NULL, 4),
('loudly / sings / the / bird', 'Syntax', 'Easy', -0.6, 1.4, 0.25, 'The bird sings loudly', NULL, 4);

-- Medium Syntax (5 items, GradeLevel 5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('have / finished / already / homework / I / my', 'Syntax', 'Medium', -0.4, 1.7, 0.25, 'I have already finished my homework', NULL, 5),
('visited / We / museum / the / yesterday / famous', 'Syntax', 'Medium', -0.1, 1.6, 0.25, 'We visited the famous museum yesterday', NULL, 5),
('been / has / She / to / never / Paris', 'Syntax', 'Medium', 0.1, 1.8, 0.25, 'She has never been to Paris', NULL, 5),
('the / quickly / The / ran / across / dog / street', 'Syntax', 'Medium', 0.3, 1.7, 0.25, 'The dog ran quickly across the street', NULL, 5),
('interesting / This / very / is / book / a', 'Syntax', 'Medium', 0.5, 1.6, 0.25, 'This is a very interesting book', NULL, 5);

-- Hard Syntax (5 items, GradeLevel 6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('would / known / I / had / helped / have / I / earlier', 'Syntax', 'Hard', 0.8, 1.9, 0.25, 'Had I known earlier I would have helped', NULL, 6),
('not / The / that / only / interesting / was / also / movie / entertaining / but', 'Syntax', 'Hard', 1.1, 1.8, 0.25, 'The movie was not only interesting but also entertaining', NULL, 6),
('despite / finished / rain / the / the / project / We / heavy', 'Syntax', 'Hard', 1.3, 1.7, 0.25, 'We finished the project despite the heavy rain', NULL, 6),
('sooner / had / train / arrived / the / than / left / it / we', 'Syntax', 'Hard', 1.5, 1.9, 0.25, 'No sooner had we arrived than the train left', NULL, 6),
('although / tired / she / was / continued / she / working', 'Syntax', 'Hard', 1.4, 1.8, 0.25, 'Although she was tired, she continued working', NULL, 6);

-- Very Hard Syntax (5 items, GradeLevel 6)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, DifficultyParam, DiscriminationParam, GuessingParam, CorrectAnswer, AnswerChoices, GradeLevel)
VALUES
('been / the / for / would / not / help / have / It / his / we / failed', 'Syntax', 'Very Hard', 1.8, 2.0, 0.25, 'Were it not for his help we would have failed', NULL, 6),
('scarcely / when / had / phone / the / he / rang / left', 'Syntax', 'Very Hard', 2.1, 1.9, 0.25, 'Scarcely had he left when the phone rang', NULL, 6),
('dedication / Seldom / such / students / see / in / we / do', 'Syntax', 'Very Hard', 2.3, 2.0, 0.25, 'Seldom do we see such dedication in students', NULL, 6),
('circumstances / Under / would / no / accept / this / I / offer', 'Syntax', 'Very Hard', 2.5, 1.9, 0.25, 'Under no circumstances would I accept this offer', NULL, 6),
('unprecedented / challenges / despite / the / organization / succeeded / the', 'Syntax', 'Very Hard', 2.4, 2.0, 0.25, 'Despite the unprecedented challenges, the organization succeeded', NULL, 6);

-- =============================================================================
-- VERIFICATION QUERIES
-- =============================================================================

-- Verify item count by type and difficulty
SELECT
    ItemType,
    DifficultyLevel,
    COUNT(*) as ItemCount,
    MIN(DifficultyParam) as MinDifficulty,
    MAX(DifficultyParam) as MaxDifficulty
FROM Items
WHERE IsActive = 1
GROUP BY ItemType, DifficultyLevel
ORDER BY ItemType, MIN(DifficultyParam);

-- Total item count
SELECT COUNT(*) AS TotalItems FROM Items WHERE IsActive = 1;

-- Items per type
SELECT ItemType, COUNT(*) AS ItemCount FROM Items WHERE IsActive = 1 GROUP BY ItemType;

-- Items per grade level
SELECT GradeLevel, COUNT(*) AS ItemCount FROM Items WHERE IsActive = 1 GROUP BY GradeLevel ORDER BY GradeLevel;
