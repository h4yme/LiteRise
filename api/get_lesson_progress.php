<?php
/**
 * LiteRise Get Lesson Progress API
 * GET /api/get_lesson_progress.php?student_id=X
 * GET /api/get_lesson_progress.php?student_id=X&lesson_id=Y
 *
 * Returns lesson progress for a student
 */

require_once __DIR__ . '/src/db.php';

try {
    $studentID = (int)($_GET['student_id'] ?? 0);
    $lessonID = isset($_GET['lesson_id']) ? (int)$_GET['lesson_id'] : null;

    if ($studentID <= 0) {
        sendError("student_id is required", 400);
        exit;
    }

    // Get student info
    $studentSql = "SELECT TotalXP, CurrentStreak, LongestStreak, GradeLevel FROM Students WHERE StudentID = ?";
    $studentStmt = $conn->prepare($studentSql);
    $studentStmt->execute([$studentID]);
    $student = $studentStmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        sendError("Student not found", 404);
        exit;
    }

    $lessons = [];

    if ($lessonID !== null) {
        // Get progress for specific lesson
        $sql = "SELECT
                    sp.LessonID,
                    sp.CompletionStatus,
                    sp.Score,
                    sp.LastAttemptDate,
                    l.LessonTitle,
                    l.LessonType,
                    (SELECT COUNT(*) FROM GameResults gr WHERE gr.StudentID = ? AND gr.LessonID = sp.LessonID) as GamesPlayed
                FROM StudentProgress sp
                LEFT JOIN Lessons l ON sp.LessonID = l.LessonID
                WHERE sp.StudentID = ? AND sp.LessonID = ?";

        $stmt = $conn->prepare($sql);
        $stmt->execute([$studentID, $studentID, $lessonID]);
        $progress = $stmt->fetch(PDO::FETCH_ASSOC);

        if ($progress) {
            $lessons[] = [
                'lesson_id' => (int)$progress['LessonID'],
                'lesson_title' => $progress['LessonTitle'] ?? 'Lesson ' . $progress['LessonID'],
                'lesson_type' => $progress['LessonType'] ?? 'reading',
                'completion_status' => $progress['CompletionStatus'],
                'score' => (float)($progress['Score'] ?? 0),
                'games_played' => (int)($progress['GamesPlayed'] ?? 0),
                'last_attempt' => $progress['LastAttemptDate']
            ];
        } else {
            // No progress yet for this lesson
            $lessons[] = [
                'lesson_id' => $lessonID,
                'lesson_title' => 'Lesson ' . $lessonID,
                'lesson_type' => 'reading',
                'completion_status' => 'NotStarted',
                'score' => 0,
                'games_played' => 0,
                'last_attempt' => null
            ];
        }
    } else {
        // Get progress for all lessons (1-6)
        for ($i = 1; $i <= 6; $i++) {
            $sql = "SELECT
                        sp.CompletionStatus,
                        sp.Score,
                        sp.LastAttemptDate,
                        (SELECT COUNT(*) FROM GameResults gr WHERE gr.StudentID = ? AND gr.LessonID = ?) as GamesPlayed
                    FROM StudentProgress sp
                    WHERE sp.StudentID = ? AND sp.LessonID = ?";

            $stmt = $conn->prepare($sql);
            $stmt->execute([$studentID, $i, $studentID, $i]);
            $progress = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($progress) {
                $gamesPlayed = (int)($progress['GamesPlayed'] ?? 0);
                $totalGamesRequired = 5;
                $progressPercent = min(100, (int)(($gamesPlayed / $totalGamesRequired) * 100));

                $lessons[] = [
                    'lesson_id' => $i,
                    'lesson_title' => 'Lesson ' . $i,
                    'completion_status' => $progress['CompletionStatus'],
                    'score' => (float)($progress['Score'] ?? 0),
                    'games_played' => $gamesPlayed,
                    'progress_percent' => $progressPercent,
                    'last_attempt' => $progress['LastAttemptDate']
                ];
            } else {
                // No progress yet
                $lessons[] = [
                    'lesson_id' => $i,
                    'lesson_title' => 'Lesson ' . $i,
                    'completion_status' => 'NotStarted',
                    'score' => 0,
                    'games_played' => 0,
                    'progress_percent' => 0,
                    'last_attempt' => null
                ];
            }
        }
    }

    sendResponse([
        'success' => true,
        'student' => [
            'total_xp' => (int)($student['TotalXP'] ?? 0),
            'current_streak' => (int)($student['CurrentStreak'] ?? 0),
            'longest_streak' => (int)($student['LongestStreak'] ?? 0),
            'grade_level' => (int)($student['GradeLevel'] ?? 4)
        ],
        'lessons' => $lessons
    ]);

} catch (PDOException $e) {
    error_log("Get lesson progress error: " . $e->getMessage());
    sendError("Database error: " . $e->getMessage(), 500);
} catch (Exception $e) {
    error_log("Get lesson progress error: " . $e->getMessage());
    sendError("Error: " . $e->getMessage(), 500);
}
?>
