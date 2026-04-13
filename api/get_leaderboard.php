<?php
/**
 * get_leaderboard.php
 * Returns a ranked leaderboard of all students.
 *
 * Query params:
 *   filter  — "xp" | "streak" | "phonics" | "vocabulary" | "grammar" | "comprehension" | "writing"
 *             Default: "xp"
 *   limit   — number of entries to return (default 50, max 100)
 *
 * Response:
 *   { "success": true, "filter": "xp", "leaderboard": [ { rank, student_id, name, grade, value, label } ] }
 */

require_once __DIR__ . '/src/db.php'; // sets $conn (PDO)

$filter = isset($_GET['filter']) ? trim($_GET['filter']) : 'xp';
$limit  = isset($_GET['limit'])  ? min((int)$_GET['limit'], 100) : 50;
if ($limit < 1) $limit = 50;

// Map filter key → column name and display label
$filterMap = [
    'xp'            => ['col' => 'TotalXP',                       'label' => 'Total XP'],
    'streak'        => ['col' => 'CurrentStreak',                  'label' => 'Day Streak'],
    'phonics'       => ['col' => 'Cat1_PhonicsWordStudy',          'label' => 'Phonics & Word Study'],
    'vocabulary'    => ['col' => 'Cat2_VocabularyWordKnowledge',   'label' => 'Vocabulary'],
    'grammar'       => ['col' => 'Cat3_GrammarAwareness',          'label' => 'Grammar Awareness'],
    'comprehension' => ['col' => 'Cat4_ComprehendingText',         'label' => 'Comprehension'],
    'writing'       => ['col' => 'Cat5_CreatingComposing',         'label' => 'Writing & Composing'],
];

if (!array_key_exists($filter, $filterMap)) {
    $filter = 'xp';
}

$col   = $filterMap[$filter]['col'];
$label = $filterMap[$filter]['label'];

try {
    $stmt = $conn->prepare("
        SELECT TOP (:lim)
            StudentID,
            FullName,
            GradeLevel,
            $col AS score
        FROM Students
        WHERE Status = 'active'
        ORDER BY $col DESC
    ");
    $stmt->bindValue(':lim', $limit, PDO::PARAM_INT);
    $stmt->execute();

    $rows = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $leaderboard = [];
    $rank = 1;
    foreach ($rows as $row) {
        $leaderboard[] = [
            'rank'       => $rank++,
            'student_id' => (int) $row['StudentID'],
            'name'       => $row['FullName'],
            'grade'      => $row['GradeLevel'],
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
