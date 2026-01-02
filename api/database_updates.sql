-- ================================================
-- LiteRise Database Updates for Registration & Password Reset
-- ================================================

USE [LiteRiseDB]
GO

-- Add missing fields to Students table
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'Nickname')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD [Nickname] NVARCHAR(50) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'Birthday')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD [Birthday] DATE NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'Gender')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD [Gender] NVARCHAR(20) NULL;
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'SchoolID')
BEGIN
    ALTER TABLE [dbo].[Students]
    ADD [SchoolID] INT NULL;
END
GO

-- Create Schools table if not exists
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[Schools]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[Schools] (
        [SchoolID] INT IDENTITY(1,1) NOT NULL,
        [SchoolName] NVARCHAR(200) NOT NULL,
        [District] NVARCHAR(100) NULL,
        [Address] NVARCHAR(300) NULL,
        [City] NVARCHAR(100) NULL,
        [Province] NVARCHAR(100) NULL,
        [IsActive] BIT NOT NULL DEFAULT 1,
        [DateCreated] DATETIME NOT NULL DEFAULT GETDATE(),
        PRIMARY KEY CLUSTERED ([SchoolID] ASC)
    );

    -- Insert default schools
    INSERT INTO [dbo].[Schools] ([SchoolName], [District], [City]) VALUES
    ('Sample Elementary School', 'Metro Manila', 'Manila'),
    ('Demo Primary School', 'Metro Manila', 'Quezon City'),
    ('Test Academy', 'Metro Manila', 'Makati'),
    ('Example Learning Center', 'Metro Manila', 'Pasig');
END
GO

-- Create PasswordResetOTP table
IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[PasswordResetOTP]') AND type in (N'U'))
BEGIN
    CREATE TABLE [dbo].[PasswordResetOTP] (
        [OTPID] INT IDENTITY(1,1) NOT NULL,
        [Email] NVARCHAR(100) NOT NULL,
        [OTPCode] NVARCHAR(6) NOT NULL,
        [CreatedAt] DATETIME NOT NULL DEFAULT GETDATE(),
        [ExpiresAt] DATETIME NOT NULL,
        [IsUsed] BIT NOT NULL DEFAULT 0,
        [UsedAt] DATETIME NULL,
        [IPAddress] NVARCHAR(50) NULL,
        PRIMARY KEY CLUSTERED ([OTPID] ASC)
    );

    -- Create index for faster lookups
    CREATE NONCLUSTERED INDEX IX_PasswordResetOTP_Email_OTPCode
    ON [dbo].[PasswordResetOTP] ([Email], [OTPCode])
    WHERE [IsUsed] = 0;
END
GO

-- Stored Procedure: Register New Student
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_RegisterStudent]') AND type in (N'P'))
BEGIN
    DROP PROCEDURE [dbo].[SP_RegisterStudent];
END
GO

CREATE PROCEDURE [dbo].[SP_RegisterStudent]
    @Nickname NVARCHAR(50),
    @FirstName NVARCHAR(50),
    @LastName NVARCHAR(50),
    @Email NVARCHAR(100),
    @Password NVARCHAR(255),
    @Birthday DATE = NULL,
    @Gender NVARCHAR(20) = NULL,
    @GradeLevel INT = 1,
    @SchoolID INT = NULL,
    @Section NVARCHAR(20) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Check if email already exists
    IF EXISTS (SELECT 1 FROM Students WHERE Email = @Email)
    BEGIN
        -- Return error code
        SELECT -1 AS StudentID, 'Email already registered' AS ErrorMessage;
        RETURN;
    END

    -- Insert new student
    INSERT INTO Students (
        Nickname,
        FirstName,
        LastName,
        Email,
        Password,
        Birthday,
        Gender,
        GradeLevel,
        SchoolID,
        Section,
        InitialAbility,
        CurrentAbility,
        TotalXP,
        CurrentStreak,
        LongestStreak,
        DateCreated,
        LastLogin,
        IsActive,
        ReadingTheta,
        SpeakingTheta,
        VocabularyTheta,
        SyntaxTheta
    )
    VALUES (
        @Nickname,
        @FirstName,
        @LastName,
        @Email,
        @Password,
        @Birthday,
        @Gender,
        @GradeLevel,
        @SchoolID,
        @Section,
        0.0,  -- InitialAbility
        0.0,  -- CurrentAbility
        0,    -- TotalXP
        0,    -- CurrentStreak
        0,    -- LongestStreak
        GETDATE(),
        NULL, -- LastLogin
        1,    -- IsActive
        0.0,  -- ReadingTheta
        0.0,  -- SpeakingTheta
        0.0,  -- VocabularyTheta
        0.0   -- SyntaxTheta
    );

    -- Return new student data
    SELECT
        StudentID,
        Nickname,
        FirstName,
        LastName,
        Email,
        Birthday,
        Gender,
        GradeLevel,
        SchoolID,
        Section,
        CurrentAbility,
        TotalXP,
        CurrentStreak,
        LongestStreak,
        DateCreated,
        IsActive
    FROM Students
    WHERE StudentID = SCOPE_IDENTITY();
END
GO

-- Stored Procedure: Create OTP
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_CreatePasswordResetOTP]') AND type in (N'P'))
BEGIN
    DROP PROCEDURE [dbo].[SP_CreatePasswordResetOTP];
END
GO

CREATE PROCEDURE [dbo].[SP_CreatePasswordResetOTP]
    @Email NVARCHAR(100),
    @OTPCode NVARCHAR(6),
    @ExpiryMinutes INT = 10,
    @IPAddress NVARCHAR(50) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    -- Check if email exists in Students table
    IF NOT EXISTS (SELECT 1 FROM Students WHERE Email = @Email AND IsActive = 1)
    BEGIN
        SELECT -1 AS OTPID, 'Email not found' AS ErrorMessage;
        RETURN;
    END

    -- Invalidate any existing unused OTPs for this email
    UPDATE PasswordResetOTP
    SET IsUsed = 1, UsedAt = GETDATE()
    WHERE Email = @Email AND IsUsed = 0;

    -- Create new OTP
    INSERT INTO PasswordResetOTP (Email, OTPCode, ExpiresAt, IPAddress)
    VALUES (@Email, @OTPCode, DATEADD(MINUTE, @ExpiryMinutes, GETDATE()), @IPAddress);

    -- Return OTP details
    SELECT
        OTPID,
        Email,
        OTPCode,
        CreatedAt,
        ExpiresAt,
        'OTP created successfully' AS Message
    FROM PasswordResetOTP
    WHERE OTPID = SCOPE_IDENTITY();
END
GO

-- Stored Procedure: Verify OTP
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_VerifyPasswordResetOTP]') AND type in (N'P'))
BEGIN
    DROP PROCEDURE [dbo].[SP_VerifyPasswordResetOTP];
END
GO

CREATE PROCEDURE [dbo].[SP_VerifyPasswordResetOTP]
    @Email NVARCHAR(100),
    @OTPCode NVARCHAR(6)
AS
BEGIN
    SET NOCOUNT ON;

    -- Find valid OTP
    DECLARE @OTPID INT;
    DECLARE @ExpiresAt DATETIME;

    SELECT TOP 1
        @OTPID = OTPID,
        @ExpiresAt = ExpiresAt
    FROM PasswordResetOTP
    WHERE Email = @Email
        AND OTPCode = @OTPCode
        AND IsUsed = 0
    ORDER BY CreatedAt DESC;

    -- Check if OTP exists
    IF @OTPID IS NULL
    BEGIN
        SELECT 0 AS IsValid, 'Invalid or already used OTP' AS Message;
        RETURN;
    END

    -- Check if OTP is expired
    IF @ExpiresAt < GETDATE()
    BEGIN
        SELECT 0 AS IsValid, 'OTP has expired' AS Message;
        RETURN;
    END

    -- OTP is valid
    SELECT 1 AS IsValid, @OTPID AS OTPID, 'OTP verified successfully' AS Message;
END
GO

-- Stored Procedure: Reset Password
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[SP_ResetPassword]') AND type in (N'P'))
BEGIN
    DROP PROCEDURE [dbo].[SP_ResetPassword];
END
GO

CREATE PROCEDURE [dbo].[SP_ResetPassword]
    @Email NVARCHAR(100),
    @OTPCode NVARCHAR(6),
    @NewPassword NVARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    BEGIN TRANSACTION;

    BEGIN TRY
        -- Verify OTP first
        DECLARE @OTPID INT;
        DECLARE @ExpiresAt DATETIME;

        SELECT TOP 1
            @OTPID = OTPID,
            @ExpiresAt = ExpiresAt
        FROM PasswordResetOTP
        WHERE Email = @Email
            AND OTPCode = @OTPCode
            AND IsUsed = 0
        ORDER BY CreatedAt DESC;

        -- Check if OTP exists
        IF @OTPID IS NULL
        BEGIN
            SELECT 0 AS Success, 'Invalid or already used OTP' AS Message;
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Check if OTP is expired
        IF @ExpiresAt < GETDATE()
        BEGIN
            SELECT 0 AS Success, 'OTP has expired' AS Message;
            ROLLBACK TRANSACTION;
            RETURN;
        END

        -- Update password
        UPDATE Students
        SET Password = @NewPassword
        WHERE Email = @Email AND IsActive = 1;

        -- Mark OTP as used
        UPDATE PasswordResetOTP
        SET IsUsed = 1, UsedAt = GETDATE()
        WHERE OTPID = @OTPID;

        COMMIT TRANSACTION;

        SELECT 1 AS Success, 'Password reset successfully' AS Message;
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        SELECT 0 AS Success, ERROR_MESSAGE() AS Message;
    END CATCH
END
GO

PRINT 'Database updates completed successfully!';
