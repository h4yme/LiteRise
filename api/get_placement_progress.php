<?php

/**
 * LiteRise Get Placement Progress API
 * GET /api/get_placement_progress.php?student_id=1
 */

require_once __DIR__ . '/src/db.php';

$studentID = (int)($_GET['student_id'] ?? 0);

if ($studentID <= 0) {
    sendError("student_id is required", 400);
}

try {
    // Get student info
    $stmt = $conn->prepare(
        "SELECT StudentID, FirstName, LastName, Email,
                ISNULL(PreAssessmentCompleted,0) AS PreAssessmentCompleted,
                ISNULL(PostAssessmentCompleted,0) AS PostAssessmentCompleted,
                ISNULL(AssessmentStatus,'Not Started') AS AssessmentStatus
         FROM Students WHERE StudentID = ?"
    );
    $stmt->execute([$studentID]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        sendError("Student not found", 404);
    }

    // Get placement results
    $resStmt = $conn->prepare(
        "SELECT TOP 1 ResultID, AssessmentType, CompletedDate, FinalTheta,
                PlacementLevel, LevelName, AccuracyPercentage, TotalQuestions, CorrectAnswers,
                Category1Score, Category2Score, Category3Score, Category4Score
         FROM PlacementResults
         WHERE StudentID = ? AND AssessmentType = 'PreAssessment'
         ORDER BY CompletedDate DESC"
    );
    $resStmt->execute([$studentID]);
    $preResult = $resStmt->fetch(PDO::FETCH_ASSOC);

    $postStmt = $conn->prepare(
        "SELECT TOP 1 ResultID, AssessmentType, CompletedDate, FinalTheta,
                PlacementLevel, LevelName, AccuracyPercentage, TotalQuestions, CorrectAnswers,
                Category1Score, Category2Score, Category3Score, Category4Score
         FROM PlacementResults
         WHERE StudentID = ? AND AssessmentType = 'PostAssessment'
         ORDER BY CompletedDate DESC"
    );
    $postStmt->execute([$studentID]);
    $postResult = $postStmt->fetch(PDO::FETCH_ASSOC);

    // Build category scores map
    function buildCategoryScores($row) {
        if (!$row) return null;
        return [
            'ResultID'           => (int)$row['ResultID'],
            'AssessmentType'     => $row['AssessmentType'],
            'CompletedDate'      => $row['CompletedDate'],
            'FinalTheta'         => (float)$row['FinalTheta'],
            'PlacementLevel'     => (int)$row['PlacementLevel'],
            'LevelName'          => $row['LevelName'],
            'AccuracyPercentage' => (float)$row['AccuracyPercentage'],
            'TotalQuestions'     => (int)$row['TotalQuestions'],
            'CorrectAnswers'     => (int)$row['CorrectAnswers'],
            'CategoryScores'     => [
                'Category1' => $row['Category1Score'] !== null ? (float)$row['Category1Score'] : null,
                'Category2' => $row['Category2Score'] !== null ? (float)$row['Category2Score'] : null,
                'Category3' => $row['Category3Score'] !== null ? (float)$row['Category3Score'] : null,
                'Category4' => $row['Category4Score'] !== null ? (float)$row['Category4Score'] : null,
            ],
        ];
    }

    // Comparison data
    $comparison = null;
    if ($preResult && $postResult) {
        $comparison = [
            'ThetaGrowth'       => (float)$postResult['FinalTheta'] - (float)$preResult['FinalTheta'],
            'LevelGrowth'       => (int)$postResult['PlacementLevel'] - (int)$preResult['PlacementLevel'],
            'AccuracyGrowth'    => (float)$postResult['AccuracyPercentage'] - (float)$preResult['AccuracyPercentage'],
            'ComparisonStatus'  => ((float)$postResult['FinalTheta'] > (float)$preResult['FinalTheta'])
                                    ? 'Improved' : 'No Change',
        ];
    }

    // Recent session logs
    $logStmt = $conn->prepare(
        "SELECT TOP 10 LogID, SessionType, SessionTag, LoggedAt, DeviceInfo
         FROM StudentSessionLogs WHERE StudentID = ? ORDER BY LoggedAt DESC"
    );
    $logStmt->execute([$studentID]);
    $sessionHistory = $logStmt->fetchAll(PDO::FETCH_ASSOC);

    sendResponse([
        'success' => true,
        'student' => [
            'StudentID'              => (int)$student['StudentID'],
            'FirstName'              => $student['FirstName'],
            'LastName'               => $student['LastName'],
            'Email'                  => $student['Email'],
            'PreAssessmentCompleted' => (bool)$student['PreAssessmentCompleted'],
            'PostAssessmentCompleted'=> (bool)$student['PostAssessmentCompleted'],
            'AssessmentStatus'       => $student['AssessmentStatus'],
        ],
        'results' => [
            'pre'  => buildCategoryScores($preResult),
            'post' => buildCategoryScores($postResult),
        ],
        'comparison'      => $comparison,
        'session_history' => $sessionHistory,
    ]);

} catch (PDOException $e) {
    error_log("get_placement_progress error: " . $e->getMessage());
    sendError("Failed to get placement progress", 500, $e->getMessage());
}
?>
