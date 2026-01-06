-- =============================================
-- LiteRise Base Database Schema
-- =============================================
-- Creates core tables required for the app:
-- - Students (user accounts)
-- - StudentSessions (login/activity tracking)
-- - Other essential tables
--
-- Run this FIRST before any other scripts!
-- =============================================

-- Create database if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'LiteRise')
BEGIN
    CREATE DATABASE LiteRise;
    PRINT '✓ Database LiteRise created';
END
ELSE
BEGIN
    PRINT '✓ Database LiteRise already exists';
END
GO

USE LiteRise;
GO

PRINT '========================================';
PRINT 'Creating LiteRise Base Schema';
PRINT '========================================';
PRINT '';

-- =============================================
-- Students Table
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Students')
BEGIN
    PRINT 'Creating Students table...';

    CREATE TABLE dbo.Students (
        StudentID INT IDENTITY(1,1) PRIMARY KEY,

        -- Account Information
        Email NVARCHAR(255) NOT NULL UNIQUE,
        Password NVARCHAR(255) NOT NULL, -- Hashed password

        -- Personal Information
        FirstName NVARCHAR(100) NOT NULL,
        LastName NVARCHAR(100) NOT NULL,
        DateOfBirth DATE NULL,
        Gender VARCHAR(10) NULL,

        -- Reading Level
        CurrentReadingLevel INT DEFAULT 1,
        PreAssessmentCompleted BIT DEFAULT 0,
        AssessmentStatus VARCHAR(50) DEFAULT 'Not Started',

        -- Progress Tracking
        TotalBooksRead INT DEFAULT 0,
        TotalMinutesRead INT DEFAULT 0,
        CurrentStreak INT DEFAULT 0,
        LongestStreak INT DEFAULT 0,

        -- Gamification
        TotalPoints INT DEFAULT 0,
        CurrentLevel INT DEFAULT 1,

        -- Account Status
        IsActive BIT DEFAULT 1,
        EmailVerified BIT DEFAULT 0,

        -- Timestamps
        CreatedAt DATETIME DEFAULT GETDATE(),
        LastLogin DATETIME NULL,
        UpdatedAt DATETIME DEFAULT GETDATE(),

        -- Indexes
        INDEX IX_Students_Email (Email),
        INDEX IX_Students_Status (IsActive, AssessmentStatus)
    );

    PRINT '✓ Students table created';
END
ELSE
BEGIN
    PRINT '✓ Students table already exists';
END
GO

-- =============================================
-- StudentSessions Table
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentSessions')
BEGIN
    PRINT 'Creating StudentSessions table...';

    CREATE TABLE dbo.StudentSessions (
        SessionID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL,

        -- Session Information
        SessionType VARCHAR(50) NOT NULL, -- 'Login', 'Assessment', 'Reading', 'Exercise'
        StartTime DATETIME NOT NULL DEFAULT GETDATE(),
        EndTime DATETIME NULL,
        DurationMinutes INT NULL,

        -- Device Information
        DeviceInfo VARCHAR(255) NULL,
        IPAddress VARCHAR(50) NULL,
        AppVersion VARCHAR(20) NULL,

        -- Activity Metrics
        PagesRead INT DEFAULT 0,
        QuestionsAnswered INT DEFAULT 0,
        PointsEarned INT DEFAULT 0,

        -- Status
        IsActive BIT DEFAULT 1,

        CONSTRAINT FK_StudentSessions_Student FOREIGN KEY (StudentID)
            REFERENCES dbo.Students(StudentID) ON DELETE CASCADE,

        INDEX IX_StudentSessions_Student (StudentID),
        INDEX IX_StudentSessions_Type (SessionType),
        INDEX IX_StudentSessions_StartTime (StartTime)
    );

    PRINT '✓ StudentSessions table created';
END
ELSE
BEGIN
    PRINT '✓ StudentSessions table already exists';
END
GO

-- =============================================
-- StudentProgress Table
-- =============================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentProgress')
BEGIN
    PRINT 'Creating StudentProgress table...';

    CREATE TABLE dbo.StudentProgress (
        ProgressID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL,

        -- Reading Progress
        CurrentBookID INT NULL,
        CurrentPage INT DEFAULT 0,
        LastReadDate DATE NULL,

        -- Skills Progress
        VocabularyLevel INT DEFAULT 1,
        ComprehensionLevel INT DEFAULT 1,
        FluencyLevel INT DEFAULT 1,

        -- Weekly Stats
        BooksReadThisWeek INT DEFAULT 0,
        MinutesReadThisWeek INT DEFAULT 0,

        -- Timestamps
        UpdatedAt DATETIME DEFAULT GETDATE(),

        CONSTRAINT FK_StudentProgress_Student FOREIGN KEY (StudentID)
            REFERENCES dbo.Students(StudentID) ON DELETE CASCADE,

        INDEX IX_StudentProgress_Student (StudentID)
    );

    PRINT '✓ StudentProgress table created';
END
ELSE
BEGIN
    PRINT '✓ StudentProgress table already exists';
END
GO

-- =============================================
-- Stored Procedure: SP_StudentLogin
-- =============================================
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_StudentLogin')
    DROP PROCEDURE dbo.SP_StudentLogin;
GO

PRINT 'Creating SP_StudentLogin stored procedure...';
GO

CREATE PROCEDURE dbo.SP_StudentLogin
    @Email NVARCHAR(255),
    @Password NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    -- Verify credentials and return student info
    SELECT
        StudentID,
        Email,
        FirstName,
        LastName,
        DateOfBirth,
        Gender,
        CurrentReadingLevel,
        PreAssessmentCompleted,
        AssessmentStatus,
        TotalBooksRead,
        TotalMinutesRead,
        CurrentStreak,
        TotalPoints,
        CurrentLevel,
        IsActive,
        LastLogin
    FROM dbo.Students
    WHERE Email = @Email
        AND Password = @Password
        AND IsActive = 1;

    -- Update last login time
    UPDATE dbo.Students
    SET LastLogin = GETDATE()
    WHERE Email = @Email
        AND Password = @Password;
END
GO

PRINT '✓ SP_StudentLogin created';
GO

-- =============================================
-- Stored Procedure: SP_LogStudentSession
-- =============================================
IF EXISTS (SELECT * FROM sys.procedures WHERE name = 'SP_LogStudentSession')
    DROP PROCEDURE dbo.SP_LogStudentSession;
GO

PRINT 'Creating SP_LogStudentSession stored procedure...';
GO

CREATE PROCEDURE dbo.SP_LogStudentSession
    @StudentID INT,
    @SessionType VARCHAR(50),
    @DeviceInfo VARCHAR(255) = NULL,
    @IPAddress VARCHAR(50) = NULL,
    @AppVersion VARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    INSERT INTO dbo.StudentSessions (
        StudentID, SessionType, StartTime,
        DeviceInfo, IPAddress, AppVersion
    )
    VALUES (
        @StudentID, @SessionType, GETDATE(),
        @DeviceInfo, @IPAddress, @AppVersion
    );

    -- Return the created session ID
    SELECT SCOPE_IDENTITY() AS SessionID;
END
GO

PRINT '✓ SP_LogStudentSession created';
GO

-- =============================================
-- Create Sample Student Account (for testing)
-- =============================================
PRINT '';
PRINT 'Creating sample student account...';

-- Check if sample account already exists
IF NOT EXISTS (SELECT * FROM Students WHERE Email = 'test@literise.com')
BEGIN
    INSERT INTO dbo.Students (
        Email, Password,
        FirstName, LastName,
        DateOfBirth, Gender,
        CurrentReadingLevel,
        PreAssessmentCompleted,
        AssessmentStatus,
        IsActive
    )
    VALUES (
        'test@literise.com',
        'password123', -- In production, this should be hashed!
        'Test',
        'Student',
        '2015-01-01',
        'Other',
        1,
        0,
        'Not Started',
        1
    );

    PRINT '✓ Sample account created:';
    PRINT '  Email: test@literise.com';
    PRINT '  Password: password123';
END
ELSE
BEGIN
    PRINT '✓ Sample account already exists';
END
GO

-- =============================================
-- Summary
-- =============================================
PRINT '';
PRINT '========================================';
PRINT 'Base Schema Created Successfully!';
PRINT '========================================';
PRINT '';
PRINT 'Tables Created:';
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_NAME IN ('Students', 'StudentSessions', 'StudentProgress')
ORDER BY TABLE_NAME;
GO

PRINT '';
PRINT 'Stored Procedures:';
SELECT ROUTINE_NAME FROM INFORMATION_SCHEMA.ROUTINES
WHERE ROUTINE_NAME IN ('SP_StudentLogin', 'SP_LogStudentSession')
ORDER BY ROUTINE_NAME;
GO

PRINT '';
PRINT '========================================';
PRINT 'Next Steps:';
PRINT '========================================';
PRINT '1. Run: api/db/assessment_items_schema.sql';
PRINT '2. Run: api/db/sample_assessment_items.sql';
PRINT '3. Run: api/db/pronunciation_schema.sql';
PRINT '4. Run: api/db/sample_pronunciation_items.sql';
PRINT '';
GO
