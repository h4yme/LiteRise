-- Create table for pronunciation attempts
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'PronunciationAttempts')
BEGIN
    CREATE TABLE PronunciationAttempts (
        AttemptID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL,
        ItemID INT NOT NULL,
        ExpectedWord NVARCHAR(255) NOT NULL,
        RecognizedText NVARCHAR(255) NOT NULL,
        Score INT NOT NULL,
        AttemptDate DATETIME NOT NULL DEFAULT GETDATE()
    );

    -- Add foreign key for Students if table exists
    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'Students')
    BEGIN
        ALTER TABLE PronunciationAttempts
        ADD CONSTRAINT FK_PronunciationAttempts_Students
        FOREIGN KEY (StudentID) REFERENCES Students(StudentID);
        PRINT 'Added foreign key constraint for Students';
    END

    -- Add foreign key for AssessmentItems if table exists
    IF EXISTS (SELECT * FROM sys.tables WHERE name = 'AssessmentItems')
    BEGIN
        ALTER TABLE PronunciationAttempts
        ADD CONSTRAINT FK_PronunciationAttempts_AssessmentItems
        FOREIGN KEY (ItemID) REFERENCES AssessmentItems(ItemID);
        PRINT 'Added foreign key constraint for AssessmentItems';
    END

    PRINT 'PronunciationAttempts table created successfully';
END
ELSE
BEGIN
    PRINT 'PronunciationAttempts table already exists';
END
GO
