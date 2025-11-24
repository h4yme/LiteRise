-- =============================================
-- LiteRise: Fix Lesson Progress Status
-- Only mark lesson as 'Completed' after 5 games
-- =============================================

-- Update the stored procedure to properly track progress
CREATE OR ALTER PROCEDURE [dbo].[SP_SaveGameResult]
    @SessionID INT = NULL,
    @StudentID INT,
    @GameType NVARCHAR(50),
    @Score INT,
    @AccuracyPercentage FLOAT = NULL,
    @TimeCompleted INT = NULL,
    @XPEarned INT = 0,
    @StreakAchieved INT = 0,
    @LessonID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @LastActivity DATE;
    DECLARE @Today DATE = CAST(GETDATE() AS DATE);
    DECLARE @Yesterday DATE = DATEADD(DAY, -1, @Today);
    DECLARE @CurrentStreak INT;
    DECLARE @LongestStreak INT;
    DECLARE @GamesPlayedForLesson INT;
    DECLARE @TotalGamesRequired INT = 5;
    DECLARE @NewStatus NVARCHAR(20);

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
            SET @CurrentStreak = 1;
        END
        ELSE IF @LastActivity = @Today
        BEGIN
            SET @CurrentStreak = @CurrentStreak;
        END
        ELSE IF @LastActivity = @Yesterday
        BEGIN
            SET @CurrentStreak = @CurrentStreak + 1;
        END
        ELSE
        BEGIN
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

        -- Update StudentProgress for this lesson
        IF @LessonID IS NOT NULL AND EXISTS (SELECT * FROM sys.tables WHERE name = 'StudentProgress')
        BEGIN
            -- Count games played for this lesson
            SELECT @GamesPlayedForLesson = COUNT(*)
            FROM GameResults
            WHERE StudentID = @StudentID AND LessonID = @LessonID;

            -- Determine status based on games played
            IF @GamesPlayedForLesson >= @TotalGamesRequired
                SET @NewStatus = 'Completed';
            ELSE
                SET @NewStatus = 'InProgress';

            IF EXISTS (SELECT 1 FROM StudentProgress WHERE StudentID = @StudentID AND LessonID = @LessonID)
            BEGIN
                UPDATE StudentProgress
                SET Score = CASE WHEN @AccuracyPercentage > ISNULL(Score, 0) THEN @AccuracyPercentage ELSE Score END,
                    CompletionStatus = @NewStatus,
                    LastAttemptDate = GETDATE()
                WHERE StudentID = @StudentID AND LessonID = @LessonID;
            END
            ELSE
            BEGIN
                INSERT INTO StudentProgress (StudentID, LessonID, CompletionStatus, Score, LastAttemptDate)
                VALUES (@StudentID, @LessonID, @NewStatus, @AccuracyPercentage, GETDATE());
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

-- Also fix any existing records that were incorrectly marked as Completed
-- Reset lessons where games played < 5 to 'InProgress'
UPDATE sp
SET sp.CompletionStatus = CASE
    WHEN (SELECT COUNT(*) FROM GameResults gr WHERE gr.StudentID = sp.StudentID AND gr.LessonID = sp.LessonID) >= 5
    THEN 'Completed'
    ELSE 'InProgress'
END
FROM StudentProgress sp
WHERE sp.CompletionStatus = 'Completed';

PRINT 'SP_SaveGameResult has been updated!';
PRINT 'Now properly tracks lesson progress:';
PRINT '  - InProgress: 1-4 games completed';
PRINT '  - Completed: 5+ games completed';
PRINT 'Existing records have been corrected.';
GO
