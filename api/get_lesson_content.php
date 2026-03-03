<?php

/**
 * LiteRise Get Lesson Content API
 * GET /api/get_lesson_content.php?node_id=1&placement_level=3
 */

require_once __DIR__ . '/src/db.php';

$nodeID         = (int)($_GET['node_id'] ?? 0);
$placementLevel = (int)($_GET['placement_level'] ?? 1);

if ($nodeID <= 0) {
    sendError("node_id is required", 400);
}

try {
    $stmt = $conn->prepare(
        "SELECT LessonID, LessonTitle, LessonDescription, LessonContent,
                GradeLevel, LessonType, RequiredAbility
         FROM Lessons
         WHERE LessonID = ? AND IsActive = 1"
    );
    $stmt->execute([$nodeID]);
    $lesson = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$lesson) {
        sendError("Lesson not found", 404);
    }

    // Determine pacing strategy based on placement level
    $pacing = getPacingStrategy($placementLevel);

    $isFinalAssessment = (strtoupper($lesson['LessonType'] ?? '') === 'FINAL_ASSESSMENT');

    sendResponse([
        'success' => true,
        'lesson'  => [
            'node_id'              => (int)$lesson['LessonID'],
            'node_number'          => (int)$lesson['LessonID'],
            'title'                => $lesson['LessonTitle'],
            'objective'            => $lesson['LessonDescription'] ?? '',
            'content'              => $lesson['LessonContent'] ?? '',
            'module_id'            => (int)$lesson['GradeLevel'],
            'module_name'          => 'Grade ' . $lesson['GradeLevel'],
            'quarter'              => 1,
            'is_final_assessment'  => $isFinalAssessment,
        ],
        'pacing' => $pacing,
    ]);

} catch (PDOException $e) {
    error_log("get_lesson_content error: " . $e->getMessage());
    sendError("Failed to get lesson content", 500, $e->getMessage());
}

function getPacingStrategy(int $level): array {
    if ($level <= 1) {
        return [
            'speed'        => 'slow',
            'scaffolding'  => 'high',
            'description'  => 'Take your time. Extra hints are available.',
        ];
    } elseif ($level <= 3) {
        return [
            'speed'        => 'normal',
            'scaffolding'  => 'medium',
            'description'  => 'Standard pace with some guidance.',
        ];
    } else {
        return [
            'speed'        => 'fast',
            'scaffolding'  => 'low',
            'description'  => 'Challenge mode. Minimal hints.',
        ];
    }
}
?>
