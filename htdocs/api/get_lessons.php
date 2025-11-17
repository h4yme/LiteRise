<?php
/**
 * LiteRise - Get Lessons by Student Ability API
 * Endpoint: GET/POST /api/get_lessons.php
 * Returns personalized lessons based on student ability level
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST');
header('Access-Control-Allow-Headers: Content-Type');

require_once 'src/db.php';

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

$studentId = null;

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $studentId = isset($_GET['StudentID']) ? (int)$_GET['StudentID'] : null;
} else {
    $input = json_decode(file_get_contents('php://input'), true);
    $studentId = isset($input['StudentID']) ? (int)$input['StudentID'] : null;
}

if (!$studentId) {
    http_response_code(400);
    echo json_encode(['error' => 'StudentID required']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Call stored procedure
    $sql = "EXEC SP_GetLessonsByAbility @StudentID = :studentId";
    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $stmt->execute();

    $lessons = $stmt->fetchAll();

    // Get student's progress for each lesson
    $progressSql = "SELECT LessonID, CompletionStatus, Score, AttemptsCount,
                           LastAttemptDate, CompletionDate
                    FROM StudentProgress
                    WHERE StudentID = :studentId";

    $progressStmt = $conn->prepare($progressSql);
    $progressStmt->bindParam(':studentId', $studentId, PDO::PARAM_INT);
    $progressStmt->execute();
    $progressData = $progressStmt->fetchAll();

    // Create lookup for progress
    $progressLookup = [];
    foreach ($progressData as $progress) {
        $progressLookup[$progress['LessonID']] = $progress;
    }

    // Enhance lessons with progress data
    $enhancedLessons = [];
    foreach ($lessons as $lesson) {
        $lessonId = (int)$lesson['LessonID'];

        $lessonData = [
            'LessonID' => $lessonId,
            'LessonTitle' => $lesson['LessonTitle'],
            'LessonDescription' => $lesson['LessonDescription'],
            'RequiredAbility' => (float)$lesson['RequiredAbility'],
            'GradeLevel' => (int)$lesson['GradeLevel'],
            'LessonType' => $lesson['LessonType'],
            'CompletionStatus' => 'NotStarted',
            'Score' => null,
            'AttemptsCount' => 0,
            'LastAttemptDate' => null,
            'CompletionDate' => null
        ];

        // Add progress if exists
        if (isset($progressLookup[$lessonId])) {
            $progress = $progressLookup[$lessonId];
            $lessonData['CompletionStatus'] = $progress['CompletionStatus'];
            $lessonData['Score'] = $progress['Score'] ? (float)$progress['Score'] : null;
            $lessonData['AttemptsCount'] = (int)$progress['AttemptsCount'];
            $lessonData['LastAttemptDate'] = $progress['LastAttemptDate'];
            $lessonData['CompletionDate'] = $progress['CompletionDate'];
        }

        $enhancedLessons[] = $lessonData;
    }

    http_response_code(200);
    echo json_encode($enhancedLessons);

} catch (Exception $e) {
    error_log("Get lessons error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve lessons']);
}
