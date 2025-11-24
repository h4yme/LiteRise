-- =============================================
-- LiteRise: Fix SP_SaveGameResult for Lesson-based Games
-- Run this script to update the stored procedure
-- =============================================

-- Add LessonID column to GameResults if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[GameResults]') AND name = 'LessonID')
BEGIN
    ALTER TABLE [dbo].[GameResults] ADD [LessonID] [int] NULL;
    PRINT 'Added LessonID column to GameResults';
END
ELSE
BEGIN
    PRINT 'LessonID column already exists in GameResults';
END
GO

-- Add LastActivityDate column to Students if it doesn't exist (for daily streak tracking)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID(N'[dbo].[Students]') AND name = 'LastActivityDate')
BEGIN
    ALTER TABLE [dbo].[Students] ADD [LastActivityDate] [date] NULL;
    PRINT 'Added LastActivityDate column to Students';
END
ELSE
BEGIN
    PRINT 'LastActivityDate column already exists in Students';
END
GO

-- Update the stored procedure with daily streak logic
CREATE OR ALTER PROCEDURE [dbo].[SP_SaveGameResult]
    @SessionID INT = NULL,          -- Made nullable for games without assessment session
    @StudentID INT,
    @GameType NVARCHAR(50),
    @Score INT,
    @AccuracyPercentage FLOAT = NULL,
    @TimeCompleted INT = NULL,
    @XPEarned INT = 0,
    @StreakAchieved INT = 0,        -- In-game streak (for record keeping, not daily streak)
    @LessonID INT = NULL            -- Added for lesson-based games
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @LastActivity DATE;
    DECLARE @Today DATE = CAST(GETDATE() AS DATE);
    DECLARE @Yesterday DATE = DATEADD(DAY, -1, @Today);
    DECLARE @CurrentStreak INT;
    DECLARE @LongestStreak INT;

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Insert game result
        INSERT INTO GameResults (
            SessionID, StudentID, GameType, Score,
            AccuracyPercentage, TimeCompleted, XPEarned,
            StreakAchieved, LessonID, DatePlayed
        )
        VALUES (
            @SessionID, @StudentID, @GameType, @Score,
            @AccuracyPercentage, @TimeCompleted, @XPEarned,
            @StreakAchieved, @LessonID, GETDATE()
        );

        -- Update student's TotalXP
        UPDATE Students
        SET TotalXP = ISNULL(TotalXP, 0) + @XPEarned
        WHERE StudentID = @StudentID;

        -- Get current streak info
        SELECT
            @LastActivity = LastActivityDate,
            @CurrentStreak = ISNULL(CurrentStreak, 0),
            @LongestStreak = ISNULL(LongestStreak, 0)
        FROM Students
        WHERE StudentID = @StudentID;

        -- Daily streak logic
        IF @LastActivity IS NULL
        BEGIN
            -- First time playing - start streak at 1
            SET @CurrentStreak = 1;
        END
        ELSE IF @LastActivity = @Today
        BEGIN
            -- Already played today - keep current streak (don't increment)
            SET @CurrentStreak = @CurrentStreak;
        END
        ELSE IF @LastActivity = @Yesterday
        BEGIN
            -- Played yesterday - increment streak by 1
            SET @CurrentStreak = @CurrentStreak + 1;
        END
        ELSE
        BEGIN
            -- Missed a day or more - reset streak to 1
            SET @CurrentStreak = 1;
        END

        -- Update longest streak if current is higher
        IF @CurrentStreak > @LongestStreak
        BEGIN
            SET @LongestStreak = @CurrentStreak;
        END

        -- Update student's streak and last activity date
        UPDATE Students
        SET CurrentStreak = @CurrentStreak,
            LongestStreak = @LongestStreak,
            LastActivityDate = @Today
        WHERE StudentID = @StudentID;

        -- Update StudentProgress for this lesson (if table exists)
        IF @LessonID IS NOT NULL AND EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentProgress')
        BEGIN
            IF EXISTS (SELECT 1 FROM StudentProgress WHERE StudentID = @StudentID AND LessonID = @LessonID)
            BEGIN
                UPDATE StudentProgress
                SET Score = CASE WHEN @AccuracyPercentage > ISNULL(Score, 0) THEN @AccuracyPercentage ELSE Score END,
                    CompletionStatus = 'Completed',
                    LastAttemptDate = GETDATE()
                WHERE StudentID = @StudentID AND LessonID = @LessonID;
            END
            ELSE
            BEGIN
                INSERT INTO StudentProgress (StudentID, LessonID, CompletionStatus, Score, LastAttemptDate)
                VALUES (@StudentID, @LessonID, 'Completed', @AccuracyPercentage, GETDATE());
            END
        END

        COMMIT TRANSACTION;

        -- Return updated student stats
        SELECT TotalXP, CurrentStreak, LongestStreak
        FROM Students
        WHERE StudentID = @StudentID;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END
GO

PRINT 'SP_SaveGameResult has been updated successfully!';
PRINT 'The stored procedure now:';
PRINT '  - Accepts nullable @SessionID';
PRINT '  - Includes @LessonID parameter';
PRINT '  - Updates TotalXP on Students table';
PRINT '  - Tracks DAILY streaks (increments by 1 each consecutive day)';
PRINT '  - Resets streak to 1 if a day is missed';
GO
