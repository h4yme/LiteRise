-- ===============================================
-- Quick Verification and Creation Script
-- LiteRise Session Tracking System
-- ===============================================

USE LiteRiseDB;
GO

-- Check what exists
PRINT '=== Checking existing objects ===';

IF EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentSessionLogs')
    PRINT '✓ StudentSessionLogs table exists'
ELSE
    PRINT '✗ StudentSessionLogs table MISSING';

IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_LogStudentSession')
    PRINT '✓ SP_LogStudentSession stored procedure exists'
ELSE
    PRINT '✗ SP_LogStudentSession stored procedure MISSING';

GO

-- Create StudentSessionLogs table if missing
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES
               WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'StudentSessionLogs')
BEGIN
    PRINT 'Creating StudentSessionLogs table...';

    CREATE TABLE dbo.StudentSessionLogs (
        LogID INT IDENTITY(1,1) PRIMARY KEY,
        StudentID INT NOT NULL FOREIGN KEY REFERENCES dbo.Students(StudentID),
        SessionType VARCHAR(50) NOT NULL,
        SessionTag VARCHAR(100) NULL,
        LoggedAt DATETIME NOT NULL DEFAULT GETDATE(),
        DeviceInfo VARCHAR(255) NULL,
        IPAddress VARCHAR(50) NULL,
        AdditionalData NVARCHAR(MAX) NULL
    );

    CREATE INDEX IDX_SessionLogs_Student ON dbo.StudentSessionLogs(StudentID);
    CREATE INDEX IDX_SessionLogs_Type ON dbo.StudentSessionLogs(SessionType);
    CREATE INDEX IDX_SessionLogs_Date ON dbo.StudentSessionLogs(LoggedAt);
    CREATE INDEX IDX_SessionLogs_Tag ON dbo.StudentSessionLogs(SessionTag);

    PRINT '✓ StudentSessionLogs table created';
END
ELSE
BEGIN
    PRINT '  StudentSessionLogs table already exists, skipping...';
END
GO

-- Create or replace SP_LogStudentSession
IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_LogStudentSession')
BEGIN
    PRINT 'Dropping existing SP_LogStudentSession...';
    DROP PROCEDURE dbo.SP_LogStudentSession;
END
GO

PRINT 'Creating SP_LogStudentSession...';
GO

CREATE PROCEDURE dbo.SP_LogStudentSession
    @StudentID INT,
    @SessionType VARCHAR(50),
    @SessionTag VARCHAR(100) = NULL,
    @DeviceInfo VARCHAR(255) = NULL,
    @IPAddress VARCHAR(50) = NULL,
    @AdditionalData NVARCHAR(MAX) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Insert session log
    INSERT INTO dbo.StudentSessionLogs (StudentID, SessionType, SessionTag, LoggedAt, DeviceInfo, IPAddress, AdditionalData)
    VALUES (@StudentID, @SessionType, @SessionTag, GETDATE(), @DeviceInfo, @IPAddress, @AdditionalData);

    -- Update last login date if it's a login event
    IF @SessionType = 'Login'
    BEGIN
        UPDATE dbo.Students
        SET LastLoginDate = GETDATE(),
            TotalLoginCount = ISNULL(TotalLoginCount, 0) + 1
        WHERE StudentID = @StudentID;
    END

    -- Return the inserted log ID
    SELECT SCOPE_IDENTITY() AS LogID;
END
GO

PRINT '✓ SP_LogStudentSession created successfully';
GO

-- Test the stored procedure
PRINT '';
PRINT '=== Testing SP_LogStudentSession ===';
PRINT 'Logging a test session for student ID 27...';

EXEC dbo.SP_LogStudentSession
    @StudentID = 27,
    @SessionType = 'Login',
    @SessionTag = 'test_session',
    @DeviceInfo = 'Test Device',
    @IPAddress = '192.168.1.1',
    @AdditionalData = '{"test": true}';

PRINT '';
PRINT '=== Verification Complete ===';
PRINT 'Check the results above. If you see ✓ marks, everything is ready!';
PRINT '';
PRINT 'View logged sessions:';
PRINT 'SELECT TOP 5 * FROM dbo.StudentSessionLogs ORDER BY LoggedAt DESC;';
GO
