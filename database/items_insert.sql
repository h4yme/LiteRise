-- LiteRise: 86 New Items to Complete 100-Item Pool
-- Items distributed across difficulty levels (-2.5 to +2.5)
-- Types: Spelling, Grammar, Pronunciation, Syntax

-- =============================================================================
-- SPELLING ITEMS (22 items)
-- =============================================================================

-- Very Easy (theta ~ -2.5 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which is spelled correctly?', 'Spelling', 'Very Easy', 'cat', '["cat", "kat", "catt", "katt"]', 1.2, -2.4, 0.25),
('Which is spelled correctly?', 'Spelling', 'Very Easy', 'dog', '["dag", "dog", "dogg", "doge"]', 1.1, -2.2, 0.25),
('Which is spelled correctly?', 'Spelling', 'Very Easy', 'run', '["run", "runn", "rune", "ruhn"]', 1.3, -2.0, 0.25),
('Which is spelled correctly?', 'Spelling', 'Very Easy', 'book', '["buk", "bouk", "book", "bokk"]', 1.2, -1.8, 0.25),
('Which is spelled correctly?', 'Spelling', 'Very Easy', 'happy', '["hapy", "happy", "happi", "heppy"]', 1.4, -1.6, 0.25);

-- Easy (theta ~ -1.5 to -0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which is spelled correctly?', 'Spelling', 'Easy', 'friend', '["freind", "friend", "frend", "frind"]', 1.5, -1.4, 0.25),
('Which is spelled correctly?', 'Spelling', 'Easy', 'because', '["becuase", "becouse", "because", "becase"]', 1.3, -1.2, 0.25),
('Which is spelled correctly?', 'Spelling', 'Easy', 'people', '["people", "peaple", "poeple", "peopel"]', 1.4, -1.0, 0.25),
('Which is spelled correctly?', 'Spelling', 'Easy', 'beautiful', '["beatiful", "beutiful", "beautiful", "beautful"]', 1.6, -0.8, 0.25),
('Which is spelled correctly?', 'Spelling', 'Easy', 'different', '["diferent", "diffrent", "different", "differant"]', 1.5, -0.6, 0.25);

-- Medium (theta ~ -0.5 to 0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which is spelled correctly?', 'Spelling', 'Medium', 'necessary', '["neccessary", "necessary", "necesary", "neccesary"]', 1.7, -0.3, 0.25),
('Which is spelled correctly?', 'Spelling', 'Medium', 'definitely', '["definately", "definitly", "definitely", "definatley"]', 1.8, 0.0, 0.25),
('Which is spelled correctly?', 'Spelling', 'Medium', 'occurrence', '["occurence", "occurrence", "occurance", "occurrance"]', 1.6, 0.2, 0.25),
('Which is spelled correctly?', 'Spelling', 'Medium', 'receive', '["recieve", "receive", "receve", "receeve"]', 1.5, 0.4, 0.25);

-- Hard (theta ~ 0.5 to 1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which is spelled correctly?', 'Spelling', 'Hard', 'accommodate', '["accomodate", "acommodate", "accommodate", "acomodate"]', 1.8, 0.7, 0.25),
('Which is spelled correctly?', 'Spelling', 'Hard', 'conscientious', '["conscientious", "consciencious", "conscentious", "conciencious"]', 1.9, 1.0, 0.25),
('Which is spelled correctly?', 'Spelling', 'Hard', 'maintenance', '["maintainance", "maintenence", "maintenance", "maintanence"]', 1.7, 1.2, 0.25),
('Which is spelled correctly?', 'Spelling', 'Hard', 'pronunciation', '["pronounciation", "pronunciation", "prononciation", "pronuciation"]', 1.8, 1.4, 0.25);

-- Very Hard (theta ~ 1.5 to 2.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which is spelled correctly?', 'Spelling', 'Very Hard', 'onomatopoeia', '["onomatopeia", "onomatopoeia", "onomatapoeia", "onomatopea"]', 2.0, 1.7, 0.25),
('Which is spelled correctly?', 'Spelling', 'Very Hard', 'entrepreneur', '["entrepeneur", "entreprenuer", "entrepreneur", "entreprenur"]', 1.9, 2.0, 0.25),
('Which is spelled correctly?', 'Spelling', 'Very Hard', 'pneumonia', '["neumonia", "pnemonia", "pneumonia", "pnuemonia"]', 1.8, 2.2, 0.25),
('Which is spelled correctly?', 'Spelling', 'Very Hard', 'hemorrhage', '["hemmorage", "hemorrhage", "hemorage", "hemorrage"]', 2.0, 2.4, 0.25);

-- =============================================================================
-- GRAMMAR ITEMS (22 items)
-- =============================================================================

-- Very Easy (theta ~ -2.5 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('She ___ a student.', 'Grammar', 'Very Easy', 'is', '["is", "are", "am", "be"]', 1.2, -2.3, 0.25),
('I ___ to school every day.', 'Grammar', 'Very Easy', 'go', '["goes", "going", "go", "went"]', 1.3, -2.1, 0.25),
('They ___ playing outside.', 'Grammar', 'Very Easy', 'are', '["is", "are", "am", "was"]', 1.1, -1.9, 0.25),
('He ___ a ball.', 'Grammar', 'Very Easy', 'has', '["have", "has", "had", "having"]', 1.4, -1.7, 0.25);

-- Easy (theta ~ -1.5 to -0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('The children ___ in the park yesterday.', 'Grammar', 'Easy', 'played', '["play", "plays", "played", "playing"]', 1.5, -1.3, 0.25),
('She ___ her homework before dinner.', 'Grammar', 'Easy', 'finished', '["finish", "finishes", "finished", "finishing"]', 1.4, -1.1, 0.25),
('My mother ___ delicious food.', 'Grammar', 'Easy', 'cooks', '["cook", "cooks", "cooked", "cooking"]', 1.3, -0.9, 0.25),
('We ___ to the movies last weekend.', 'Grammar', 'Easy', 'went', '["go", "goes", "went", "going"]', 1.5, -0.7, 0.25);

-- Medium (theta ~ -0.5 to 0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('If I ___ rich, I would travel the world.', 'Grammar', 'Medium', 'were', '["am", "was", "were", "be"]', 1.7, -0.4, 0.25),
('She asked me where I ___ going.', 'Grammar', 'Medium', 'was', '["am", "was", "were", "is"]', 1.6, -0.1, 0.25),
('The book ___ on the table since morning.', 'Grammar', 'Medium', 'has been', '["is", "was", "has been", "had been"]', 1.8, 0.1, 0.25),
('Neither the teacher nor the students ___ present.', 'Grammar', 'Medium', 'were', '["was", "were", "is", "are"]', 1.7, 0.3, 0.25),
('By the time we arrived, they ___ already left.', 'Grammar', 'Medium', 'had', '["have", "has", "had", "having"]', 1.6, 0.5, 0.25);

-- Hard (theta ~ 0.5 to 1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Had I known earlier, I ___ have helped.', 'Grammar', 'Hard', 'would', '["will", "would", "could", "should"]', 1.9, 0.8, 0.25),
('The news ___ shocking to everyone.', 'Grammar', 'Hard', 'was', '["were", "was", "are", "have been"]', 1.8, 1.1, 0.25),
('Scarcely had he left ___ the phone rang.', 'Grammar', 'Hard', 'when', '["than", "when", "then", "before"]', 1.7, 1.3, 0.25),
('It is essential that he ___ on time.', 'Grammar', 'Hard', 'be', '["is", "was", "be", "being"]', 1.9, 1.5, 0.25);

-- Very Hard (theta ~ 1.5 to 2.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Were it not for his help, we ___ failed.', 'Grammar', 'Very Hard', 'would have', '["will have", "would have", "could", "have"]', 2.0, 1.8, 0.25),
('The committee ___ divided in their opinions.', 'Grammar', 'Very Hard', 'were', '["was", "were", "is", "has been"]', 1.9, 2.1, 0.25),
('Seldom ___ such dedication in students.', 'Grammar', 'Very Hard', 'do we see', '["we see", "do we see", "we do see", "see we"]', 2.0, 2.3, 0.25);

-- =============================================================================
-- PRONUNCIATION ITEMS (21 items)
-- =============================================================================

-- Very Easy (theta ~ -2.5 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which word has the same vowel sound as "cat"?', 'Pronunciation', 'Very Easy', 'hat', '["hat", "hate", "hot", "hit"]', '/kæt/', 1.2, -2.2, 0.25),
('Which word rhymes with "day"?', 'Pronunciation', 'Very Easy', 'play', '["play", "die", "do", "dew"]', '/deɪ/', 1.1, -2.0, 0.25),
('Which word starts with the same sound as "ship"?', 'Pronunciation', 'Very Easy', 'shoe', '["see", "chip", "shoe", "sip"]', '/ʃɪp/', 1.3, -1.8, 0.25),
('Which word has the same ending sound as "sing"?', 'Pronunciation', 'Very Easy', 'ring', '["ring", "sin", "sign", "thing"]', '/sɪŋ/', 1.2, -1.6, 0.25);

-- Easy (theta ~ -1.5 to -0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which word has a silent letter?', 'Pronunciation', 'Easy', 'knight', '["kite", "knight", "night", "kit"]', '/naɪt/', 1.5, -1.4, 0.25),
('Which word has the "th" sound as in "think"?', 'Pronunciation', 'Easy', 'thick', '["tick", "thick", "sick", "this"]', '/θɪŋk/', 1.4, -1.2, 0.25),
('Which word has a long "e" sound?', 'Pronunciation', 'Easy', 'meet', '["met", "meet", "meat", "mitt"]', '/miːt/', 1.3, -1.0, 0.25),
('Which word has the same stress pattern as "banana"?', 'Pronunciation', 'Easy', 'tomato', '["apple", "tomato", "orange", "mango"]', '/bəˈnænə/', 1.5, -0.8, 0.25);

-- Medium (theta ~ -0.5 to 0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('In which word is "gh" pronounced as /f/?', 'Pronunciation', 'Medium', 'enough', '["ghost", "through", "enough", "daughter"]', '/ɪˈnʌf/', 1.7, -0.3, 0.25),
('Which word has the schwa sound /ə/?', 'Pronunciation', 'Medium', 'about', '["about", "out", "shout", "doubt"]', '/əˈbaʊt/', 1.6, 0.0, 0.25),
('Which word is stressed on the second syllable?', 'Pronunciation', 'Medium', 'begin', '["happen", "begin", "open", "broken"]', '/bɪˈgɪn/', 1.8, 0.2, 0.25),
('Which pair of words are homophones?', 'Pronunciation', 'Medium', 'bare/bear', '["bare/bear", "read/red", "live/leave", "close/clothes"]', '/beər/', 1.7, 0.4, 0.25);

-- Hard (theta ~ 0.5 to 1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which word contains the diphthong /aʊ/?', 'Pronunciation', 'Hard', 'house', '["house", "horse", "hose", "whose"]', '/haʊs/', 1.8, 0.7, 0.25),
('In which word does "ough" sound like /ɔː/?', 'Pronunciation', 'Hard', 'thought', '["though", "through", "thought", "tough"]', '/θɔːt/', 1.9, 1.0, 0.25),
('Which word has primary stress on the third syllable?', 'Pronunciation', 'Hard', 'understand', '["beautiful", "understand", "comfortable", "interesting"]', '/ˌʌndəˈstænd/', 1.7, 1.2, 0.25),
('Which word pair shows vowel reduction in the second word?', 'Pronunciation', 'Hard', 'photograph/photography', '["record/record", "photograph/photography", "present/present", "object/object"]', '/ˈfəʊtəgrɑːf/-/fəˈtɒgrəfi/', 1.9, 1.4, 0.25);

-- Very Hard (theta ~ 1.5 to 2.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, Phonetic, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('Which word demonstrates linking-r?', 'Pronunciation', 'Very Hard', 'far away', '["car park", "far away", "for you", "her book"]', '/fɑːr əˈweɪ/', 2.0, 1.7, 0.25),
('Identify the word with an intrusive /r/?', 'Pronunciation', 'Very Hard', 'idea of', '["for it", "idea of", "there are", "more in"]', '/aɪˈdɪər əv/', 1.9, 2.0, 0.25),
('Which shows correct weak form usage?', 'Pronunciation', 'Very Hard', 'cup of tea /əv/', '["cup of tea /ɒf/", "cup of tea /əv/", "glass of water /ɒv/", "piece of cake /ɒf/"]', '/kʌp əv tiː/', 2.0, 2.2, 0.25),
('Which word pair shows assimilation?', 'Pronunciation', 'Very Hard', 'ten people /tem/', '["ten cats", "ten people /tem/", "ten dogs", "ten birds"]', '/tem ˈpiːpl/', 1.9, 2.4, 0.25);

-- =============================================================================
-- SYNTAX ITEMS (21 items) - Sentence Scramble
-- =============================================================================

-- Very Easy (theta ~ -2.5 to -1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('is / cat / The / black', 'Syntax', 'Very Easy', 'The cat is black', NULL, 1.2, -2.3, 0.25),
('like / I / apples', 'Syntax', 'Very Easy', 'I like apples', NULL, 1.1, -2.1, 0.25),
('is / He / happy', 'Syntax', 'Very Easy', 'He is happy', NULL, 1.3, -1.9, 0.25),
('play / We / games', 'Syntax', 'Very Easy', 'We play games', NULL, 1.2, -1.7, 0.25);

-- Easy (theta ~ -1.5 to -0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('the / plays / in / She / park', 'Syntax', 'Easy', 'She plays in the park', NULL, 1.5, -1.4, 0.25),
('reading / is / a / He / book', 'Syntax', 'Easy', 'He is reading a book', NULL, 1.4, -1.2, 0.25),
('to / go / school / I / every / day', 'Syntax', 'Easy', 'I go to school every day', NULL, 1.3, -1.0, 0.25),
('mother / My / delicious / cooks / food', 'Syntax', 'Easy', 'My mother cooks delicious food', NULL, 1.5, -0.8, 0.25);

-- Medium (theta ~ -0.5 to 0.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('have / finished / already / homework / I / my', 'Syntax', 'Medium', 'I have already finished my homework', NULL, 1.7, -0.4, 0.25),
('visited / We / museum / the / yesterday / famous', 'Syntax', 'Medium', 'We visited the famous museum yesterday', NULL, 1.6, -0.1, 0.25),
('been / has / She / to / never / Paris', 'Syntax', 'Medium', 'She has never been to Paris', NULL, 1.8, 0.1, 0.25),
('the / quickly / The / ran / across / dog / street', 'Syntax', 'Medium', 'The dog ran quickly across the street', NULL, 1.7, 0.3, 0.25),
('interesting / This / very / is / book / a', 'Syntax', 'Medium', 'This is a very interesting book', NULL, 1.6, 0.5, 0.25);

-- Hard (theta ~ 0.5 to 1.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('would / known / I / had / helped / have / I / earlier', 'Syntax', 'Hard', 'Had I known earlier I would have helped', NULL, 1.9, 0.8, 0.25),
('not / The / that / only / interesting / was / also / movie / entertaining / but', 'Syntax', 'Hard', 'The movie was not only interesting but also entertaining', NULL, 1.8, 1.1, 0.25),
('despite / finished / rain / the / the / project / We / heavy', 'Syntax', 'Hard', 'We finished the project despite the heavy rain', NULL, 1.7, 1.3, 0.25),
('sooner / had / train / arrived / the / than / left / it / we', 'Syntax', 'Hard', 'No sooner had we arrived than the train left', NULL, 1.9, 1.5, 0.25);

-- Very Hard (theta ~ 1.5 to 2.5)
INSERT INTO Items (ItemText, ItemType, DifficultyLevel, CorrectAnswer, AnswerChoices, DiscriminationParam, DifficultyParam, GuessingParam)
VALUES
('been / the / for / would / not / help / have / It / his / we / failed', 'Syntax', 'Very Hard', 'Were it not for his help we would have failed', NULL, 2.0, 1.8, 0.25),
('scarcely / when / had / phone / the / he / rang / left', 'Syntax', 'Very Hard', 'Scarcely had he left when the phone rang', NULL, 1.9, 2.1, 0.25),
('dedication / Seldom / such / students / see / in / we / do', 'Syntax', 'Very Hard', 'Seldom do we see such dedication in students', NULL, 2.0, 2.3, 0.25),
('circumstances / Under / would / no / accept / this / I / offer', 'Syntax', 'Very Hard', 'Under no circumstances would I accept this offer', NULL, 1.9, 2.5, 0.25);

-- =============================================================================
-- VERIFICATION QUERY
-- =============================================================================
-- Run this to verify item distribution:
-- SELECT ItemType, DifficultyLevel, COUNT(*) as ItemCount,
--        AVG(DifficultyParam) as AvgDifficulty
-- FROM Items
-- GROUP BY ItemType, DifficultyLevel
-- ORDER BY ItemType, AVG(DifficultyParam);
