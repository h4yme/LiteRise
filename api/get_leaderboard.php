<?php
/**
 * get_leaderboard.php
 * Returns a ranked leaderboard of all active students.
 *
 * Query params:
 *   filter  — "xp" | "streak" | "lessons" | "badges"   (default: "xp")
 *   limit   — max entries to return (default 50, max 100)
 *
 * Response:
 *   { "success": true, "filter": "xp", "filter_label": "Total XP",
 *     "leaderboard": [ { rank, student_id, name, grade, value, label } ] }
 */

require_once __DIR__ . '/src/db.php'; // sets $conn (PDO)

$filter = isset($_GET['filter']) ? trim($_GET['filter']) : 'xp';
$limit  = isset($_GET['limit'])  ? min((int)$_GET['limit'], 100) : 50;
if ($limit < 1) $limit = 50;

$validFilters = ['xp', 'streak', 'lessons', 'badges'];
if (!in_array($filter, $validFilters, true)) $filter = 'xp';

$labelMap = [
    'xp'      => 'Total XP',
    'streak'  => 'Day Streak',
    'lessons' => 'Lessons Completed',
    'badges'  => 'Badges Earned',
];
$label    = $labelMap[$filter];
$limitInt = (int) $limit;

// Build the appropriate query for each filter
if ($filter === 'xp') {
    $sql = "
        SELECT TOP ($limitInt)
            StudentID,
            FirstName + ' ' + LastName AS FullName,
            GradeLevel,
            COALESCE(TotalXP, 0) AS score
        FROM Students
        WHERE IsActive = 1
        ORDER BY score DESC
    ";
} elseif ($filter === 'streak') {
    $sql = "
        SELECT TOP ($limitInt)
            StudentID,
            FirstName + ' ' + LastName AS FullName,
            GradeLevel,
            COALESCE(CurrentStreak, 0) AS score
        FROM Students
        WHERE IsActive = 1
        ORDER BY score DESC
    ";
} elseif ($filter === 'lessons') {
    $sql = "
        SELECT TOP ($limitInt)
            s.StudentID,
            s.FirstName + ' ' + s.LastName AS FullName,
            s.GradeLevel,
            COALESCE(lp.lesson_count, 0) AS score
        FROM Students s
        LEFT JOIN (
            SELECT StudentID, COUNT(*) AS lesson_count
            FROM   StudentNodeProgress
            WHERE  LessonCompleted = 1
            GROUP  BY StudentID
        ) lp ON s.StudentID = lp.StudentID
        WHERE s.IsActive = 1
        ORDER BY score DESC
    ";
} else { // badges
    $sql = "
        SELECT TOP ($limitInt)
            s.StudentID,
            s.FirstName + ' ' + s.LastName AS FullName,
            s.GradeLevel,
            COALESCE(b.badge_count, 0) AS score
        FROM Students s
        LEFT JOIN (
            SELECT StudentID, COUNT(*) AS badge_count
            FROM   StudentBadges
            GROUP  BY StudentID
        ) b ON s.StudentID = b.StudentID
        WHERE s.IsActive = 1
        ORDER BY score DESC
    ";
}

try {
    $stmt = $conn->prepare($sql);
    $stmt->execute();
    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $leaderboard = [];
    $rank = 1;
    foreach ($rows as $row) {
        $leaderboard[] = [
            'rank'       => $rank++,
            'student_id' => (int) $row['StudentID'],
            'name'       => $row['FullName'],
            'grade'      => (int) $row['GradeLevel'],
            'value'      => (int) $row['score'],
            'label'      => $label,
        ];
    }

    echo json_encode([
        'success'      => true,
        'filter'       => $filter,
        'filter_label' => $label,
        'leaderboard'  => $leaderboard,
    ]);

} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['success' => false, 'message' => 'Server error.']);
}
