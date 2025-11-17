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
        AttemptDate DATETIME NOT NULL DEFAULT GETDATE(),
        FOREIGN KEY (StudentID) REFERENCES Students(StudentID),
        FOREIGN KEY (ItemID) REFERENCES AssessmentItems(ItemID)
    );

    PRINT 'PronunciationAttempts table created successfully';
END
ELSE
BEGIN
    PRINT 'PronunciationAttempts table already exists';
END
GO
