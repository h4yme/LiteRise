<?php

/**
 * LiteRise Get Module Ladder API
 * GET /api/get_module_ladder.php?student_id=1&module_id=3
 *
 * module_id maps to GradeLevel in Lessons table.
 */

require_once __DIR__ . '/src/db.php';

$studentID = (int)($_GET['student_id'] ?? 0);
$moduleID  = (int)($_GET['module_id'] ?? 0);

if ($studentID <= 0 || $moduleID <= 0) {
    sendError("student_id and module_id are required", 400);
}

try {
    // Get student placement level
    $stuStmt = $conn->prepare(
        "SELECT ISNULL(PreAssessmentLevel, 1) AS PlacementLevel FROM Students WHERE StudentID = ?"
    );
    $stuStmt->execute([$studentID]);
    $stuRow = $stuStmt->fetch(PDO::FETCH_ASSOC);
    $placementLevel = (int)($stuRow['PlacementLevel'] ?? 1);

    // Get all active lessons for this module (grade level)
    $lessonsStmt = $conn->prepare(
        "SELECT LessonID, LessonTitle, LessonType
         FROM Lessons
         WHERE GradeLevel = ? AND IsActive = 1
         ORDER BY LessonID ASC"
    );
    $lessonsStmt->execute([$moduleID]);
    $lessons = $lessonsStmt->fetchAll(PDO::FETCH_ASSOC);

    // Get student progress for all lessons
    $progressStmt = $conn->prepare(
        "SELECT LessonID, CompletionStatus, Score
         FROM StudentProgress WHERE StudentID = ?"
    );
    $progressStmt->execute([$studentID]);
    $progressMap = [];
    while ($row = $progressStmt->fetch(PDO::FETCH_ASSOC)) {
        $progressMap[(int)$row['LessonID']] = $row;
    }

    // Build node list
    $nodes         = [];
    $currentNodeID = null;
    $nodeNumber    = 1;

    foreach ($lessons as $lesson) {
        $lessonID  = (int)$lesson['LessonID'];
        $progress  = $progressMap[$lessonID] ?? null;
        $status    = $progress['CompletionStatus'] ?? 'not_started';

        $lessonDone = in_array($status, ['lesson_completed', 'game_completed', 'quiz_completed']) ? '1' : '0';
        $gameDone   = in_array($status, ['game_completed', 'quiz_completed']) ? '1' : '0';
        $quizDone   = ($status === 'quiz_completed') ? '1' : '0';

        // First node not completed is current
        if ($currentNodeID === null && $quizDone === '0') {
            $currentNodeID = $lessonID;
        }

        $nodeType = strtoupper($lesson['LessonType'] ?? 'CORE_LESSON');
        if ($nodeType !== 'FINAL_ASSESSMENT') {
            $nodeType = 'CORE_LESSON';
        }

        // Quarter: evenly distribute nodes into 4 quarters
        $quarter = (string)min(4, (int)ceil($nodeNumber / max(1, ceil(count($lessons) / 4))));

        $nodes[] = [
            'NodeID'          => $lessonID,
            'NodeNumber'      => $nodeNumber,
            'LessonTitle'     => $lesson['LessonTitle'],
            'NodeType'        => $nodeType,
            'Quarter'         => $quarter,
            'LessonCompleted' => $lessonDone,
            'GameCompleted'   => $gameDone,
            'QuizCompleted'   => $quizDone,
        ];

        $nodeNumber++;
    }

    sendResponse([
        'success'        => true,
        'nodes'          => $nodes,
        'currentNodeId'  => $currentNodeID,
        'placementLevel' => $placementLevel,
        'message'        => count($nodes) > 0
            ? 'Module ladder loaded successfully'
            : 'No lessons found for this module',
    ]);

} catch (PDOException $e) {
    error_log("get_module_ladder error: " . $e->getMessage());
    sendError("Failed to get module ladder", 500, $e->getMessage());
}
?>
