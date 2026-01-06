-- =============================================
-- COMPLETE ASSESSMENT SYSTEM SETUP
-- =============================================
-- Run this ONE script to set up everything:
-- - Assessment tables
-- - 36 multiple choice questions
-- - Pronunciation extension
-- - 30 pronunciation questions
-- =============================================

USE LiteRiseDB;
GO

PRINT '========================================';
PRINT 'LiteRise Complete Assessment Setup';
PRINT '========================================';
PRINT '';

-- =============================================
-- PART 1: Create AssessmentItems Table
-- =============================================
PRINT 'Creating AssessmentItems table...';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'AssessmentItems')
BEGIN
    PRINT 'Dropping existing AssessmentItems table...';
    -- Drop dependent tables first
    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'PronunciationScores')
        DROP TABLE dbo.PronunciationScores;
    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentResponses')
        DROP TABLE dbo.StudentResponses;
    DROP TABLE dbo.AssessmentItems;
END

CREATE TABLE dbo.AssessmentItems (
    ItemID INT IDENTITY(1,1) PRIMARY KEY,
    Category VARCHAR(50) NOT NULL,
    Subcategory VARCHAR(100) NULL,
    SkillArea VARCHAR(100) NULL,
    QuestionText NVARCHAR(MAX) NOT NULL,
    QuestionType VARCHAR(50) NOT NULL,
    OptionA NVARCHAR(500) NULL,
    OptionB NVARCHAR(500) NULL,
    OptionC NVARCHAR(500) NULL,
    OptionD NVARCHAR(500) NULL,
    CorrectAnswer NVARCHAR(200) NOT NULL, -- MC: 'A','B','C','D'; Pronunciation: full word
    DifficultyParam FLOAT NOT NULL,
    DiscriminationParam FLOAT NOT NULL,
    GuessingParam FLOAT DEFAULT 0.25,
    GradeLevel INT NULL,
    EstimatedTime INT DEFAULT 30,
    IsActive BIT DEFAULT 1,
    TimesAdministered INT DEFAULT 0,
    TimesCorrect INT DEFAULT 0,
    AverageResponseTime FLOAT NULL,
    -- Pronunciation fields
    AudioPromptURL NVARCHAR(500) NULL,
    TargetPronunciation NVARCHAR(200) NULL,
    PhoneticTranscription NVARCHAR(200) NULL,
    MinimumAccuracy INT DEFAULT 65,
    PronunciationTips NVARCHAR(MAX) NULL,
    CreatedDate DATETIME DEFAULT GETDATE(),
    ModifiedDate DATETIME DEFAULT GETDATE()
);

CREATE INDEX IX_AssessmentItems_Category ON dbo.AssessmentItems(Category);
CREATE INDEX IX_AssessmentItems_Difficulty ON dbo.AssessmentItems(DifficultyParam);
CREATE INDEX IX_AssessmentItems_Type ON dbo.AssessmentItems(QuestionType);

PRINT '✓ AssessmentItems table created';
GO

-- =============================================
-- PART 2: Create StudentResponses Table
-- =============================================
PRINT 'Creating StudentResponses table...';

CREATE TABLE dbo.StudentResponses (
    ResponseID INT IDENTITY(1,1) PRIMARY KEY,
    StudentID INT NOT NULL,
    ItemID INT NOT NULL,
    SessionID INT NOT NULL,
    AssessmentType VARCHAR(20) NOT NULL,
    SelectedAnswer NVARCHAR(200) NULL,
    IsCorrect BIT NOT NULL,
    StudentThetaAtTime FLOAT NOT NULL,
    ItemDifficulty FLOAT NOT NULL,
    ExpectedProbability FLOAT NULL,
    ResponseTime INT NULL,
    QuestionNumber INT NOT NULL,
    DeviceInfo VARCHAR(255) NULL,
    InteractionData NVARCHAR(MAX) NULL,
    RespondedAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_StudentResponses_Student FOREIGN KEY (StudentID)
        REFERENCES dbo.Students(StudentID) ON DELETE NO ACTION,
    CONSTRAINT FK_StudentResponses_Item FOREIGN KEY (ItemID)
        REFERENCES dbo.AssessmentItems(ItemID) ON DELETE NO ACTION
);

CREATE INDEX IX_StudentResponses_Student ON dbo.StudentResponses(StudentID);
CREATE INDEX IX_StudentResponses_Session ON dbo.StudentResponses(SessionID);
CREATE INDEX IX_StudentResponses_Item ON dbo.StudentResponses(ItemID);

PRINT '✓ StudentResponses table created';
GO

-- =============================================
-- PART 3: Create PronunciationScores Table
-- =============================================
PRINT 'Creating PronunciationScores table...';

CREATE TABLE dbo.PronunciationScores (
    ScoreID INT IDENTITY(1,1) PRIMARY KEY,
    ResponseID INT NOT NULL,
    StudentID INT NOT NULL,
    ItemID INT NOT NULL,
    RecognizedText NVARCHAR(500) NULL,
    Confidence FLOAT NULL,
    OverallAccuracy INT NOT NULL,
    PronunciationScore FLOAT NULL,
    FluencyScore FLOAT NULL,
    CompletenessScore FLOAT NULL,
    PhonemeAccuracyJSON NVARCHAR(MAX) NULL,
    ErrorPhonemes NVARCHAR(500) NULL,
    AudioDuration INT NULL,
    AudioQuality FLOAT NULL,
    BackgroundNoiseLevel FLOAT NULL,
    SpeechAPIProvider VARCHAR(50) DEFAULT 'Google Cloud Speech',
    APIResponseJSON NVARCHAR(MAX) NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_PronunciationScores_Response FOREIGN KEY (ResponseID)
        REFERENCES dbo.StudentResponses(ResponseID) ON DELETE CASCADE,
    CONSTRAINT FK_PronunciationScores_Student FOREIGN KEY (StudentID)
        REFERENCES dbo.Students(StudentID) ON DELETE NO ACTION,
    CONSTRAINT FK_PronunciationScores_Item FOREIGN KEY (ItemID)
        REFERENCES dbo.AssessmentItems(ItemID) ON DELETE NO ACTION
);

CREATE INDEX IX_PronunciationScores_Student ON dbo.PronunciationScores(StudentID);
CREATE INDEX IX_PronunciationScores_Item ON dbo.PronunciationScores(ItemID);

PRINT '✓ PronunciationScores table created';
GO

-- =============================================
-- PART 4: Create StudentPronunciationProgress Table
-- =============================================
PRINT 'Creating StudentPronunciationProgress table...';

-- Drop if exists
IF EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentPronunciationProgress')
BEGIN
    PRINT 'Dropping existing StudentPronunciationProgress table...';
    DROP TABLE dbo.StudentPronunciationProgress;
END

CREATE TABLE dbo.StudentPronunciationProgress (
    ProgressID INT IDENTITY(1,1) PRIMARY KEY,
    StudentID INT NOT NULL,
    AssessmentDate DATE NOT NULL,
    AverageAccuracy FLOAT NOT NULL,
    WordsAttempted INT DEFAULT 0,
    WordsPassed INT DEFAULT 0,
    MasteredPhonemes NVARCHAR(500) NULL,
    ProblematicPhonemes NVARCHAR(500) NULL,
    AccuracyImprovement FLOAT NULL,
    StreakDays INT DEFAULT 0,
    CreatedAt DATETIME DEFAULT GETDATE(),
    UpdatedAt DATETIME DEFAULT GETDATE(),
    CONSTRAINT FK_PronunciationProgress_Student FOREIGN KEY (StudentID)
        REFERENCES dbo.Students(StudentID) ON DELETE CASCADE
);

CREATE INDEX IX_PronunciationProgress_Student ON dbo.StudentPronunciationProgress(StudentID);

PRINT '✓ StudentPronunciationProgress table created';
PRINT '';
GO

-- =============================================
-- PART 5: Create Stored Procedures
-- =============================================
PRINT 'Creating stored procedures...';

-- SP_GetNextAdaptiveQuestion
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_GetNextAdaptiveQuestion')
    DROP PROCEDURE dbo.SP_GetNextAdaptiveQuestion;
GO

CREATE PROCEDURE dbo.SP_GetNextAdaptiveQuestion
    @StudentID INT,
    @SessionID INT,
    @CurrentTheta FLOAT,
    @AssessmentType VARCHAR(20),
    @CategoryFilter VARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    SELECT TOP 1
        ai.ItemID, ai.Category, ai.Subcategory, ai.SkillArea,
        ai.QuestionText, ai.QuestionType,
        ai.OptionA, ai.OptionB, ai.OptionC, ai.OptionD,
        ai.DifficultyParam, ai.DiscriminationParam, ai.GuessingParam,
        ai.EstimatedTime, ai.TargetPronunciation, ai.PhoneticTranscription
    FROM dbo.AssessmentItems ai
    WHERE ai.IsActive = 1
        AND (@CategoryFilter IS NULL OR ai.Category = @CategoryFilter)
        AND ai.ItemID NOT IN (
            SELECT ItemID FROM dbo.StudentResponses
            WHERE StudentID = @StudentID AND SessionID = @SessionID
        )
    ORDER BY ABS(ai.DifficultyParam - @CurrentTheta), NEWID();
END
GO

-- SP_RecordStudentResponse
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_RecordStudentResponse')
    DROP PROCEDURE dbo.SP_RecordStudentResponse;
GO

CREATE PROCEDURE dbo.SP_RecordStudentResponse
    @StudentID INT,
    @ItemID INT,
    @SessionID INT,
    @AssessmentType VARCHAR(20),
    @SelectedAnswer NVARCHAR(200),
    @IsCorrect BIT,
    @StudentThetaAtTime FLOAT,
    @ResponseTime INT = NULL,
    @QuestionNumber INT,
    @DeviceInfo VARCHAR(255) = NULL,
    @InteractionData NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRY
        DECLARE @ItemDifficulty FLOAT, @ExpectedProb FLOAT, @Discrimination FLOAT, @Guessing FLOAT;
        SELECT @ItemDifficulty = DifficultyParam, @Discrimination = DiscriminationParam, @Guessing = GuessingParam
        FROM dbo.AssessmentItems WHERE ItemID = @ItemID;
        SET @ExpectedProb = @Guessing + (1 - @Guessing) / (1 + EXP(-1.7 * @Discrimination * (@StudentThetaAtTime - @ItemDifficulty)));
        INSERT INTO dbo.StudentResponses (StudentID, ItemID, SessionID, AssessmentType, SelectedAnswer, IsCorrect, StudentThetaAtTime, ItemDifficulty, ExpectedProbability, ResponseTime, QuestionNumber, DeviceInfo, InteractionData)
        VALUES (@StudentID, @ItemID, @SessionID, @AssessmentType, @SelectedAnswer, @IsCorrect, @StudentThetaAtTime, @ItemDifficulty, @ExpectedProb, @ResponseTime, @QuestionNumber, @DeviceInfo, @InteractionData);
        UPDATE dbo.AssessmentItems SET TimesAdministered = TimesAdministered + 1, TimesCorrect = TimesCorrect + (CASE WHEN @IsCorrect = 1 THEN 1 ELSE 0 END) WHERE ItemID = @ItemID;
        SELECT SCOPE_IDENTITY() AS ResponseID;
    END TRY
    BEGIN CATCH
        THROW;
    END CATCH
END
GO

-- SP_RecordPronunciationScore
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_RecordPronunciationScore')
    DROP PROCEDURE dbo.SP_RecordPronunciationScore;
GO

CREATE PROCEDURE dbo.SP_RecordPronunciationScore
    @ResponseID INT, @StudentID INT, @ItemID INT, @RecognizedText NVARCHAR(500),
    @Confidence FLOAT, @OverallAccuracy INT, @PronunciationScore FLOAT = NULL,
    @FluencyScore FLOAT = NULL, @CompletenessScore FLOAT = NULL,
    @PhonemeAccuracyJSON NVARCHAR(MAX) = NULL, @ErrorPhonemes NVARCHAR(500) = NULL,
    @AudioDuration INT = NULL, @AudioQuality FLOAT = NULL, @APIResponseJSON NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO dbo.PronunciationScores (ResponseID, StudentID, ItemID, RecognizedText, Confidence, OverallAccuracy, PronunciationScore, FluencyScore, CompletenessScore, PhonemeAccuracyJSON, ErrorPhonemes, AudioDuration, AudioQuality, APIResponseJSON)
    VALUES (@ResponseID, @StudentID, @ItemID, @RecognizedText, @Confidence, @OverallAccuracy, @PronunciationScore, @FluencyScore, @CompletenessScore, @PhonemeAccuracyJSON, @ErrorPhonemes, @AudioDuration, @AudioQuality, @APIResponseJSON);
    SELECT SCOPE_IDENTITY() AS ScoreID;
END
GO

PRINT '✓ Stored procedures created';
PRINT '';
GO

-- =============================================
-- PART 6: Insert 36 Multiple Choice Questions
-- =============================================
PRINT 'Loading 36 multiple choice questions...';

-- Easy Oral Language (5 questions)
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel) VALUES
('Oral Language', 'Basic Vocabulary', 'Common Objects', 'What do you use to write on paper?', 'MultipleChoice', 'Pencil', 'Spoon', 'Shoe', 'Ball', 'A', -1.8, 1.2, 0.25, 1),
('Oral Language', 'Basic Vocabulary', 'Common Objects', 'Which one can you wear on your head?', 'MultipleChoice', 'Cup', 'Hat', 'Book', 'Apple', 'B', -1.7, 1.2, 0.25, 1),
('Oral Language', 'Basic Vocabulary', 'Animals', 'Which animal says "meow"?', 'MultipleChoice', 'Dog', 'Bird', 'Cat', 'Fish', 'C', -1.6, 1.1, 0.25, 1),
('Oral Language', 'Basic Vocabulary', 'Colors', 'What color is the sky on a sunny day?', 'MultipleChoice', 'Green', 'Red', 'Yellow', 'Blue', 'D', -1.5, 1.2, 0.25, 1),
('Oral Language', 'Listening Comprehension', 'Following Directions', 'If someone says "close the door", what should you do?', 'MultipleChoice', 'Open it', 'Close it', 'Paint it', 'Break it', 'B', -1.4, 1.3, 0.25, 1);

-- Medium Oral Language (4 questions)
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel) VALUES
('Oral Language', 'Vocabulary', 'Opposites', 'What is the opposite of "hot"?', 'MultipleChoice', 'Warm', 'Cold', 'Dry', 'Wet', 'B', 0.2, 1.4, 0.25, 2),
('Oral Language', 'Listening Comprehension', 'Story Understanding', 'In the story, the boy found a lost puppy. What did he find?', 'MultipleChoice', 'A kitten', 'A puppy', 'A toy', 'A ball', 'B', 0.5, 1.4, 0.25, 2),
('Oral Language', 'Vocabulary', 'Categories', 'Which one is a fruit?', 'MultipleChoice', 'Carrot', 'Potato', 'Apple', 'Bread', 'C', 0.8, 1.3, 0.25, 2),
('Oral Language', 'Advanced Vocabulary', 'Descriptive Words', 'Which word means "very happy"?', 'MultipleChoice', 'Sad', 'Angry', 'Joyful', 'Tired', 'C', 1.8, 1.5, 0.25, 3);

-- Word Knowledge (9 questions)
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel) VALUES
('Word Knowledge', 'Phonics', 'Beginning Sounds', 'Which word starts with the same sound as "cat"?', 'MultipleChoice', 'Dog', 'Car', 'Ball', 'Fish', 'B', -2.0, 1.1, 0.25, 1),
('Word Knowledge', 'Phonics', 'Rhyming', 'Which word rhymes with "cat"?', 'MultipleChoice', 'Dog', 'Hat', 'Sun', 'Big', 'B', -1.5, 1.2, 0.25, 1),
('Word Knowledge', 'Sight Words', 'Common Words', 'Which word is "the"?', 'MultipleChoice', 'cat', 'the', 'run', 'big', 'B', -1.0, 1.3, 0.25, 1),
('Word Knowledge', 'Word Families', 'CVC Words', 'Which word belongs to the "-at" family?', 'MultipleChoice', 'dog', 'mat', 'run', 'big', 'B', -0.5, 1.3, 0.25, 1),
('Word Knowledge', 'Syllables', 'Counting Syllables', 'How many syllables are in "happy"?', 'MultipleChoice', 'One', 'Two', 'Three', 'Four', 'B', 0.0, 1.4, 0.25, 2),
('Word Knowledge', 'Prefixes', 'Un- prefix', 'What does "unhappy" mean?', 'MultipleChoice', 'Very happy', 'Not happy', 'A little happy', 'Always happy', 'B', 0.5, 1.5, 0.25, 2),
('Word Knowledge', 'Suffixes', '-ing ending', 'Which word means "to jump right now"?', 'MultipleChoice', 'Jumped', 'Jumping', 'Jumps', 'Jump', 'B', 1.0, 1.4, 0.25, 2),
('Word Knowledge', 'Compound Words', 'Combining Words', 'What two words make "rainbow"?', 'MultipleChoice', 'Rain + drop', 'Rain + bow', 'Sun + shine', 'Sky + blue', 'B', 1.5, 1.5, 0.25, 3),
('Word Knowledge', 'Synonyms', 'Similar Meanings', 'Which word means almost the same as "big"?', 'MultipleChoice', 'Small', 'Large', 'Tiny', 'Little', 'B', 1.7, 1.6, 0.25, 3);

-- Reading Comprehension (9 questions)
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel) VALUES
('Reading Comprehension', 'Literal Comprehension', 'Details', 'The cat is black. What color is the cat?', 'MultipleChoice', 'White', 'Black', 'Brown', 'Orange', 'B', -1.9, 1.1, 0.25, 1),
('Reading Comprehension', 'Literal Comprehension', 'Main Idea', 'Tom plays with his dog. Who plays with the dog?', 'MultipleChoice', 'Mom', 'Tom', 'Dad', 'Sister', 'B', -1.4, 1.2, 0.25, 1),
('Reading Comprehension', 'Sequencing', 'Order of Events', 'First I wake up. Then I eat breakfast. What do I do first?', 'MultipleChoice', 'Sleep', 'Wake up', 'Eat', 'Play', 'B', -0.8, 1.3, 0.25, 1),
('Reading Comprehension', 'Predictions', 'What Happens Next', 'The sky is dark. It starts to rain. What might you need?', 'MultipleChoice', 'Sunglasses', 'Umbrella', 'Shorts', 'Sandals', 'B', -0.2, 1.4, 0.25, 2),
('Reading Comprehension', 'Inference', 'Drawing Conclusions', 'Sarah is wearing a coat and gloves. It might be...', 'MultipleChoice', 'Hot', 'Cold', 'Rainy', 'Windy', 'B', 0.3, 1.5, 0.25, 2),
('Reading Comprehension', 'Character Analysis', 'Feelings', 'Tim is smiling and laughing. How does Tim feel?', 'MultipleChoice', 'Sad', 'Happy', 'Angry', 'Scared', 'B', 0.8, 1.4, 0.25, 2),
('Reading Comprehension', 'Theme', 'Central Message', 'A story about sharing toys teaches us to...', 'MultipleChoice', 'Keep everything', 'Be selfish', 'Share with others', 'Hide our toys', 'C', 1.3, 1.6, 0.25, 3),
('Reading Comprehension', 'Compare and Contrast', 'Similarities', 'Both cats and dogs have...', 'MultipleChoice', 'Wings', 'Fur', 'Fins', 'Scales', 'B', 1.6, 1.5, 0.25, 3),
('Reading Comprehension', 'Author Purpose', 'Why Written', 'A story that makes you laugh is written to...', 'MultipleChoice', 'Teach', 'Inform', 'Entertain', 'Persuade', 'C', 1.9, 1.7, 0.25, 3);

-- Language Structure (9 questions)
INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel) VALUES
('Language Structure', 'Capitalization', 'Sentence Start', 'Which word should start with a capital letter? "the cat runs."', 'MultipleChoice', 'cat', 'runs', 'The', 'All of them', 'C', -1.8, 1.2, 0.25, 1),
('Language Structure', 'Punctuation', 'End Marks', 'What goes at the end of this sentence: "I like pizza"', 'MultipleChoice', '?', '.', '!', ',', 'B', -1.4, 1.3, 0.25, 1),
('Language Structure', 'Grammar', 'Nouns', 'Which word is a person, place, or thing?', 'MultipleChoice', 'Run', 'Happy', 'Dog', 'Quickly', 'C', -0.9, 1.4, 0.25, 2),
('Language Structure', 'Grammar', 'Verbs', 'Which word shows an action?', 'MultipleChoice', 'Cat', 'Jump', 'Big', 'The', 'B', -0.4, 1.4, 0.25, 2),
('Language Structure', 'Sentence Structure', 'Complete Sentences', 'Which is a complete sentence?', 'MultipleChoice', 'The dog', 'Running fast', 'I like cats.', 'Big and red', 'C', 0.1, 1.5, 0.25, 2),
('Language Structure', 'Grammar', 'Adjectives', 'Which word describes the noun? "The BIG dog"', 'MultipleChoice', 'The', 'Big', 'Dog', 'None', 'B', 0.6, 1.5, 0.25, 2),
('Language Structure', 'Punctuation', 'Question Marks', 'Which sentence needs a question mark?', 'MultipleChoice', 'I am happy', 'Where are you', 'The cat runs', 'Look at that', 'B', 1.1, 1.6, 0.25, 3),
('Language Structure', 'Grammar', 'Pronouns', 'What can replace "Sarah" in this sentence? "Sarah is nice."', 'MultipleChoice', 'He', 'She', 'They', 'We', 'B', 1.5, 1.6, 0.25, 3),
('Language Structure', 'Punctuation', 'Advanced Punctuation', 'Where does the comma go: "I like pizza ice cream and cake"?', 'MultipleChoice', 'After pizza and ice cream', 'After like only', 'After cream only', 'No comma needed', 'A', 2.0, 1.9, 0.25, 4);

PRINT '✓ 36 multiple choice questions loaded';
PRINT '';
GO

-- =============================================
-- PART 7: Insert 30 Pronunciation Questions
-- =============================================
PRINT 'Loading 30 pronunciation questions...';

INSERT INTO dbo.AssessmentItems (Category, Subcategory, SkillArea, QuestionText, QuestionType, OptionA, OptionB, OptionC, OptionD, CorrectAnswer, DifficultyParam, DiscriminationParam, GuessingParam, GradeLevel, TargetPronunciation, PhoneticTranscription, MinimumAccuracy, PronunciationTips) VALUES
-- Easy (5)
('Oral Language', 'Pronunciation', 'Basic Consonants', 'Say the word: CAT', 'Pronunciation', NULL, NULL, NULL, NULL, 'cat', -2.0, 1.1, 0.0, 1, 'cat', '/kæt/', 65, 'Start with a "k" sound, then say "at"'),
('Oral Language', 'Pronunciation', 'Basic Consonants', 'Say the word: DOG', 'Pronunciation', NULL, NULL, NULL, NULL, 'dog', -1.9, 1.1, 0.0, 1, 'dog', '/dɔɡ/', 65, 'Start with a "d" sound, add the "og" sound'),
('Oral Language', 'Pronunciation', 'Basic Consonants', 'Say the word: SUN', 'Pronunciation', NULL, NULL, NULL, NULL, 'sun', -1.8, 1.2, 0.0, 1, 'sun', '/sʌn/', 65, 'Make the "s" sound like a snake, then say "un"'),
('Oral Language', 'Pronunciation', 'Basic Consonants', 'Say the word: BIG', 'Pronunciation', NULL, NULL, NULL, NULL, 'big', -1.7, 1.1, 0.0, 1, 'big', '/bɪɡ/', 65, 'Start with "b", then the short "i" sound, end with "g"'),
('Oral Language', 'Pronunciation', 'Short Vowels', 'Say the word: HAT', 'Pronunciation', NULL, NULL, NULL, NULL, 'hat', -1.6, 1.2, 0.0, 1, 'hat', '/hæt/', 65, 'Breathe out "h", then the short "a" as in "cat"'),
-- Medium (10)
('Oral Language', 'Pronunciation', 'Consonant Blends', 'Say the word: STOP', 'Pronunciation', NULL, NULL, NULL, NULL, 'stop', -1.0, 1.3, 0.0, 2, 'stop', '/stɑp/', 70, 'Blend "s" and "t" together smoothly, then "op"'),
('Oral Language', 'Pronunciation', 'Consonant Blends', 'Say the word: TREE', 'Pronunciation', NULL, NULL, NULL, NULL, 'tree', -0.9, 1.3, 0.0, 2, 'tree', '/triː/', 70, 'Blend "t" and "r" together, then long "ee" sound'),
('Oral Language', 'Pronunciation', 'Long Vowels', 'Say the word: MAKE', 'Pronunciation', NULL, NULL, NULL, NULL, 'make', -0.8, 1.4, 0.0, 2, 'make', '/meɪk/', 70, 'Say "m", then the long "a" sound like saying the letter A'),
('Oral Language', 'Pronunciation', 'Long Vowels', 'Say the word: BIKE', 'Pronunciation', NULL, NULL, NULL, NULL, 'bike', -0.7, 1.3, 0.0, 2, 'bike', '/baɪk/', 70, 'Start with "b", then say "ike" like the word "I"'),
('Oral Language', 'Pronunciation', 'Consonant Blends', 'Say the word: JUMP', 'Pronunciation', NULL, NULL, NULL, NULL, 'jump', -0.6, 1.3, 0.0, 2, 'jump', '/dʒʌmp/', 70, 'Make the "j" sound, then "ump" like "bump"'),
('Oral Language', 'Pronunciation', 'R-Controlled Vowels', 'Say the word: BIRD', 'Pronunciation', NULL, NULL, NULL, NULL, 'bird', -0.5, 1.4, 0.0, 2, 'bird', '/bɜːrd/', 72, 'The "ir" makes a special sound - like "er" in "her"'),
('Oral Language', 'Pronunciation', 'Digraphs', 'Say the word: SHIP', 'Pronunciation', NULL, NULL, NULL, NULL, 'ship', -0.4, 1.4, 0.0, 2, 'ship', '/ʃɪp/', 72, 'Make the "sh" sound by blowing air softly'),
('Oral Language', 'Pronunciation', 'Digraphs', 'Say the word: THEN', 'Pronunciation', NULL, NULL, NULL, NULL, 'then', -0.3, 1.5, 0.0, 2, 'then', '/ðɛn/', 72, 'Put tongue between teeth for "th", then say "en"'),
('Oral Language', 'Pronunciation', 'Diphthongs', 'Say the word: COIN', 'Pronunciation', NULL, NULL, NULL, NULL, 'coin', -0.2, 1.4, 0.0, 2, 'coin', '/kɔɪn/', 72, 'Say "oi" like "oy" in "boy"'),
('Oral Language', 'Pronunciation', 'Silent Letters', 'Say the word: KNIFE', 'Pronunciation', NULL, NULL, NULL, NULL, 'knife', -0.1, 1.5, 0.0, 2, 'knife', '/naɪf/', 75, 'The "k" is silent! Start with "n", then "ife"'),
-- Medium-Hard (9)
('Oral Language', 'Pronunciation', 'Complex Blends', 'Say the word: SPRING', 'Pronunciation', NULL, NULL, NULL, NULL, 'spring', 0.6, 1.6, 0.0, 3, 'spring', '/sprɪŋ/', 75, 'Blend "s", "p", and "r" smoothly, end with "ing"'),
('Oral Language', 'Pronunciation', 'Complex Blends', 'Say the word: SPLASH', 'Pronunciation', NULL, NULL, NULL, NULL, 'splash', 0.7, 1.6, 0.0, 3, 'splash', '/splæʃ/', 75, 'Three consonants together: "s-p-l", then "ash"'),
('Word Knowledge', 'Pronunciation', 'Two Syllables', 'Say the word: HAPPY', 'Pronunciation', NULL, NULL, NULL, NULL, 'happy', 0.8, 1.5, 0.0, 3, 'happy', '/ˈhæp.i/', 75, 'Two parts: "HAP" (stressed) and "py"'),
('Word Knowledge', 'Pronunciation', 'Two Syllables', 'Say the word: TIGER', 'Pronunciation', NULL, NULL, NULL, NULL, 'tiger', 0.9, 1.5, 0.0, 3, 'tiger', '/ˈtaɪ.ɡər/', 75, 'TI (like "tie") gets stress, then "ger"'),
('Word Knowledge', 'Pronunciation', 'Two Syllables', 'Say the word: TABLE', 'Pronunciation', NULL, NULL, NULL, NULL, 'table', 1.0, 1.5, 0.0, 3, 'table', '/ˈteɪ.bəl/', 75, 'TA (long "a") is stressed, "ble" is softer'),
('Word Knowledge', 'Pronunciation', 'Tricky Vowels', 'Say the word: SCHOOL', 'Pronunciation', NULL, NULL, NULL, NULL, 'school', 1.1, 1.6, 0.0, 3, 'school', '/skuːl/', 78, 'The "oo" makes a long "oo" sound like "pool"'),
('Word Knowledge', 'Pronunciation', 'Tricky Vowels', 'Say the word: THOUGHT', 'Pronunciation', NULL, NULL, NULL, NULL, 'thought', 1.2, 1.7, 0.0, 3, 'thought', '/θɔːt/', 78, 'Start with "th", then "ought" sounds like "awt"'),
('Word Knowledge', 'Pronunciation', 'Three Syllables', 'Say the word: ELEPHANT', 'Pronunciation', NULL, NULL, NULL, NULL, 'elephant', 1.3, 1.6, 0.0, 3, 'elephant', '/ˈɛl.ɪ.fənt/', 78, 'Three parts: EL-e-phant, stress on first syllable'),
('Word Knowledge', 'Pronunciation', 'Three Syllables', 'Say the word: IMPORTANT', 'Pronunciation', NULL, NULL, NULL, NULL, 'important', 1.4, 1.6, 0.0, 3, 'important', '/ɪmˈpɔr.tənt/', 78, 'Three syllables: im-POR-tant, stress on middle'),
-- Hard (6)
('Word Knowledge', 'Pronunciation', 'Irregular Vowels', 'Say the word: ENOUGH', 'Pronunciation', NULL, NULL, NULL, NULL, 'enough', 1.6, 1.7, 0.0, 4, 'enough', '/ɪˈnʌf/', 80, 'The "ough" sounds like "uff". Stress on second part'),
('Word Knowledge', 'Pronunciation', 'Irregular Vowels', 'Say the word: THROUGH', 'Pronunciation', NULL, NULL, NULL, NULL, 'through', 1.7, 1.8, 0.0, 4, 'through', '/θruː/', 80, 'Sounds like "threw". The "ough" makes "oo" sound'),
('Word Knowledge', 'Pronunciation', 'Four Syllables', 'Say the word: BEAUTIFUL', 'Pronunciation', NULL, NULL, NULL, NULL, 'beautiful', 1.8, 1.7, 0.0, 4, 'beautiful', '/ˈbjuː.tɪ.fəl/', 80, 'BEAU-ti-ful, four syllables, stress on first'),
('Language Structure', 'Pronunciation', 'Four Syllables', 'Say the word: CONGRATULATIONS', 'Pronunciation', NULL, NULL, NULL, NULL, 'congratulations', 1.9, 1.8, 0.0, 4, 'congratulations', '/kənˌɡrætʃ.əˈleɪ.ʃənz/', 82, 'con-GRAT-u-LA-tions, five syllables, two stresses'),
('Language Structure', 'Pronunciation', 'Irregular Words', 'Say the word: ORCHESTRA', 'Pronunciation', NULL, NULL, NULL, NULL, 'orchestra', 2.0, 1.8, 0.0, 4, 'orchestra', '/ˈɔr.kɪ.strə/', 82, 'OR-kes-tra, the "ch" sounds like "k"'),
('Language Structure', 'Pronunciation', 'Complex Words', 'Say the word: REFRIGERATOR', 'Pronunciation', NULL, NULL, NULL, NULL, 'refrigerator', 2.1, 1.9, 0.0, 4, 'refrigerator', '/rɪˈfrɪdʒ.əˌreɪ.tər/', 82, 're-FRIJ-er-a-tor, five syllables, stress on second');

PRINT '✓ 30 pronunciation questions loaded';
PRINT '';
GO

-- =============================================
-- SUMMARY
-- =============================================
PRINT '========================================';
PRINT 'Setup Complete!';
PRINT '========================================';
PRINT '';

SELECT 'Question Type' = QuestionType, 'Count' = COUNT(*)
FROM dbo.AssessmentItems
GROUP BY QuestionType;

PRINT '';

-- Get total count using a variable to avoid subquery error
DECLARE @TotalQuestions INT;
SELECT @TotalQuestions = COUNT(*) FROM dbo.AssessmentItems;
PRINT 'Total Questions: ' + CAST(@TotalQuestions AS VARCHAR);

PRINT '';
PRINT '✓ All tables created';
PRINT '✓ All stored procedures created';
PRINT '✓ All sample data loaded';
PRINT '';
PRINT 'Ready for deployment!';
GO
