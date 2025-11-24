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

-- Update the stored procedure with nullable SessionID and LessonID parameter
CREATE OR ALTER PROCEDURE [dbo].[SP_SaveGameResult]
    @SessionID INT = NULL,          -- Made nullable for games without assessment session
    @StudentID INT,
    @GameType NVARCHAR(50),
    @Score INT,
    @AccuracyPercentage FLOAT = NULL,
    @TimeCompleted INT = NULL,
    @XPEarned INT = 0,
    @StreakAchieved INT = 0,
    @LessonID INT = NULL            -- Added for lesson-based games
AS
BEGIN
    SET NOCOUNT ON;

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

        -- Update streak if higher than current
        IF @StreakAchieved > 0
        BEGIN
            UPDATE Students
            SET CurrentStreak = CASE
                    WHEN @StreakAchieved > ISNULL(CurrentStreak, 0) THEN @StreakAchieved
                    ELSE CurrentStreak
                END,
                LongestStreak = CASE
                    WHEN @StreakAchieved > ISNULL(LongestStreak, 0) THEN @StreakAchieved
                    ELSE LongestStreak
                END
            WHERE StudentID = @StudentID;
        END

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
PRINT '  - Tracks game results by lesson';
GO
