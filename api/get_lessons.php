<?php
/**
 * LiteRise Get Lessons API
 * GET /api/get_lessons.php
 *
 * Returns lessons appropriate for student's ability level
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

$authUser = requireAuth();
$studentID = $authUser['studentID'];

try {
    // Get student's current ability
    $stmt = $conn->prepare("SELECT CurrentAbility FROM Students WHERE StudentID = ?");
    $stmt->execute([$studentID]);
    $currentAbility = (float)($stmt->fetchColumn() ?? 0.0);

    // Get lessons with progress
    $stmt = $conn->prepare(
        "SELECT
            l.LessonID,
            l.LessonTitle,
            l.LessonDescription,
            l.LessonContent,
            l.RequiredAbility,
            l.GradeLevel,
            l.LessonType,
            l.IsActive,
            sp.CompletionStatus,
            sp.Score,
            sp.AttemptsCount,
            sp.LastAttemptDate,
            sp.CompletionDate,
            CASE WHEN l.RequiredAbility <= ? + 0.5 THEN 1 ELSE 0 END AS IsUnlocked
         FROM Lessons l
         LEFT JOIN StudentProgress sp ON l.LessonID = sp.LessonID AND sp.StudentID = ?
         WHERE l.IsActive = 1
         ORDER BY l.RequiredAbility ASC, l.GradeLevel ASC"
    );
    $stmt->execute([$currentAbility, $studentID]);
    $lessons = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Format lessons
    $formattedLessons = array_map(function($lesson) {
        return [
            'LessonID' => (int)$lesson['LessonID'],
            'LessonTitle' => $lesson['LessonTitle'],
            'LessonDescription' => $lesson['LessonDescription'],
            'LessonContent' => $lesson['LessonContent'],
            'RequiredAbility' => (float)$lesson['RequiredAbility'],
            'GradeLevel' => $lesson['GradeLevel'],
            'LessonType' => $lesson['LessonType'],
            'IsUnlocked' => (bool)$lesson['IsUnlocked'],
            'Progress' => [
                'CompletionStatus' => $lesson['CompletionStatus'] ?? 'NotStarted',
                'Score' => $lesson['Score'] ? (int)$lesson['Score'] : null,
                'AttemptsCount' => (int)($lesson['AttemptsCount'] ?? 0),
                'LastAttemptDate' => $lesson['LastAttemptDate'],
                'CompletionDate' => $lesson['CompletionDate']
            ]
        ];
    }, $lessons);

    // Separate into unlocked and locked
    $unlockedLessons = array_filter($formattedLessons, fn($l) => $l['IsUnlocked']);
    $lockedLessons = array_filter($formattedLessons, fn($l) => !$l['IsUnlocked']);

    sendResponse([
        'success' => true,
        'current_ability' => round($currentAbility, 3),
        'total_lessons' => count($lessons),
        'unlocked_count' => count($unlockedLessons),
        'lessons' => array_values($formattedLessons),
        'unlocked_lessons' => array_values($unlockedLessons),
        'locked_lessons' => array_values($lockedLessons)
    ], 200);

} catch (PDOException $e) {
    error_log("Get lessons error: " . $e->getMessage());
    sendError("Failed to get lessons", 500);
}
?>
