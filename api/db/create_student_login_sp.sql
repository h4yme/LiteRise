-- ===============================================
-- Create/Update SP_StudentLogin Stored Procedure
-- This procedure handles student login and returns all necessary student data
-- ===============================================

USE LiteRiseDB;
GO

-- Drop existing procedure if it exists
IF EXISTS (SELECT * FROM sys.objects WHERE type = 'P' AND name = 'SP_StudentLogin')
BEGIN
    DROP PROCEDURE dbo.SP_StudentLogin;
END
GO

-- Create the stored procedure
CREATE PROCEDURE dbo.SP_StudentLogin
    @Email VARCHAR(255),
    @Password VARCHAR(255)
AS
BEGIN
    SET NOCOUNT ON;

    -- Validate input
    IF @Email IS NULL OR @Email = '' OR @Password IS NULL OR @Password = ''
    BEGIN
        RAISERROR('Email and Password are required', 16, 1);
        RETURN;
    END

    -- Select student data including assessment status
    SELECT
        s.StudentID,
        s.FirstName,
        s.LastName,
        s.Email,
        s.Password,
        s.GradeLevel,
        s.Section,
        s.CurrentAbility,
        s.TotalXP,
        s.CurrentStreak,
        s.LongestStreak,
        s.LastLoginDate as LastLogin,
        s.Nickname,
        -- Assessment tracking fields
        ISNULL(s.PreAssessmentCompleted, 0) as PreAssessmentCompleted,
        ISNULL(s.AssessmentStatus, 'Not Started') as AssessmentStatus,
        s.PreAssessmentDate,
        s.PreAssessmentLevel,
        s.PreAssessmentTheta,
        s.PostAssessmentCompleted,
        s.PostAssessmentDate,
        s.PostAssessmentLevel,
        s.PostAssessmentTheta
    FROM
        dbo.Students s
    WHERE
        s.Email = @Email;
END
GO

-- Grant execute permission
GRANT EXECUTE ON dbo.SP_StudentLogin TO PUBLIC;
GO

PRINT 'SP_StudentLogin created successfully';
PRINT 'Procedure returns PreAssessmentCompleted and AssessmentStatus fields';
GO
