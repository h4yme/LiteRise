<?php
/**
 * LiteRise Get Student Profile API
 * GET /api/get_student_profile.php
 *
 * Returns authenticated student's profile and stats
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';
require_once __DIR__ . '/irt.php';

$authUser = requireAuth();
$studentID = $authUser['studentID'];

try {
    // Get student profile
    $stmt = $conn->prepare(
        "SELECT StudentID, FirstName, LastName, Email, GradeLevel,
                CurrentAbility, TotalXP, CurrentStreak, LongestStreak,
                RegistrationDate
         FROM Students WHERE StudentID = ?"
    );
    $stmt->execute([$studentID]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        sendError("Student not found", 404);
    }

    // Get ability classification
    $irt = new ItemResponseTheory();
    $classification = $irt->classifyAbility((float)$student['CurrentAbility']);

    // Get lesson progress stats
    $stmt = $conn->prepare(
        "SELECT COUNT(*) as total,
                SUM(CASE WHEN CompletionStatus = 'Completed' THEN 1 ELSE 0 END) as completed
         FROM StudentProgress WHERE StudentID = ?"
    );
    $stmt->execute([$studentID]);
    $lessonStats = $stmt->fetch(PDO::FETCH_ASSOC);

    // Get game stats
    $stmt = $conn->prepare(
        "SELECT COUNT(*) as games_played,
                ISNULL(AVG(AccuracyPercentage), 0) as avg_accuracy,
                ISNULL(MAX(Score), 0) as high_score
         FROM GameResults WHERE StudentID = ?"
    );
    $stmt->execute([$studentID]);
    $gameStats = $stmt->fetch(PDO::FETCH_ASSOC);

    // Get badge count
    $stmt = $conn->prepare(
        "SELECT COUNT(*) as badge_count
         FROM StudentBadges WHERE StudentID = ?"
    );
    $stmt->execute([$studentID]);
    $badgeStats = $stmt->fetch(PDO::FETCH_ASSOC);

    // Get assessment history
    $stmt = $conn->prepare(
        "SELECT SessionType, FinalTheta, IsCompleted, StartTime, EndTime
         FROM TestSessions
         WHERE StudentID = ? AND SessionType IN ('PreAssessment', 'PostAssessment')
         ORDER BY StartTime DESC"
    );
    $stmt->execute([$studentID]);
    $assessments = $stmt->fetchAll(PDO::FETCH_ASSOC);

    sendResponse([
        'success' => true,
        'student' => [
            'StudentID' => (int)$student['StudentID'],
            'FirstName' => $student['FirstName'],
            'LastName' => $student['LastName'],
            'Email' => $student['Email'],
            'GradeLevel' => $student['GradeLevel'],
            'CurrentAbility' => round((float)$student['CurrentAbility'], 3),
            'AbilityClassification' => $classification,
            'TotalXP' => (int)$student['TotalXP'],
            'CurrentStreak' => (int)$student['CurrentStreak'],
            'LongestStreak' => (int)$student['LongestStreak'],
            'RegistrationDate' => $student['RegistrationDate']
        ],
        'stats' => [
            'lessons_started' => (int)$lessonStats['total'],
            'lessons_completed' => (int)$lessonStats['completed'],
            'games_played' => (int)$gameStats['games_played'],
            'average_accuracy' => round((float)$gameStats['avg_accuracy'], 1),
            'high_score' => (int)$gameStats['high_score'],
            'badges_earned' => (int)$badgeStats['badge_count']
        ],
        'assessments' => $assessments
    ], 200);

} catch (PDOException $e) {
    error_log("Get student profile error: " . $e->getMessage());
    sendError("Failed to get profile", 500);
}
?>
