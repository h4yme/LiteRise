<?php

/**
 * LiteRise Get Lesson Branches API
 *
 * POST /api/get_lesson_branches.php
 *
 * Returns intervention and enrichment branches for a lesson with student progress
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "lesson_id": 101
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "lesson": {
 *     "LessonID": 101,
 *     "Title": "Sight Words: The Basics",
 *     "QuizScore": 55
 *   },
 *   "intervention": {
 *     "BranchID": 1,
 *     "BranchType": "intervention",
 *     "Title": "Sight Words Review",
 *     "Description": "Practice the words we just learned",
 *     "ContentData": {...},
 *     "RequiredAbility": -1.5,
 *     "Status": "unlocked",
 *     "Score": null,
 *     "UnlockedAt": "2026-01-18 10:30:00",
 *     "CompletedAt": null
 *   },
 *   "enrichment": {
 *     "BranchID": 2,
 *     "BranchType": "enrichment",
 *     "Title": "Advanced Sight Words",
 *     "Description": "Challenge yourself with Key Stage 2 words",
 *     "ContentData": {...},
 *     "RequiredAbility": 1.5,
 *     "Status": "locked",
 *     "Score": null,
 *     "UnlockedAt": null,
 *     "CompletedAt": null
 *   }
 * }
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';

// Require authentication
$authUser = requireAuth();

// Get JSON input
$data = getJsonInput();
$studentID = $data['student_id'] ?? null;
$lessonID = $data['lesson_id'] ?? null;

// Validate inputs
if (!$studentID) {
    sendError("student_id is required", 400);
}
if (!$lessonID) {
    sendError("lesson_id is required", 400);
}

// Verify authenticated user
if ($authUser['studentID'] != $studentID) {
    sendError("Unauthorized: Cannot view branches for another student", 403);
}

try {
    // Call stored procedure to get lesson branches
    $stmt = $conn->prepare("EXEC SP_GetLessonBranches @StudentID = ?, @LessonID = ?");
    $stmt->execute([$studentID, $lessonID]);

    // Get lesson info (first result set)
    $lessonInfo = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$lessonInfo) {
        sendError("Lesson not found", 404);
    }

    // Move to next result set (branches)
    $stmt->nextRowset();
    $branches = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Separate intervention and enrichment branches
    $interventionBranch = null;
    $enrichmentBranch = null;

    foreach ($branches as $branch) {
        $formattedBranch = [
            'BranchID' => (int)$branch['BranchID'],
            'BranchType' => $branch['BranchType'],
            'Title' => $branch['Title'],
            'Description' => $branch['Description'],
            'ContentData' => json_decode($branch['ContentData'], true),
            'RequiredAbility' => (float)($branch['RequiredAbility'] ?? 0.0),
            'Status' => $branch['Status'] ?? 'locked',
            'Score' => $branch['Score'] ? (int)$branch['Score'] : null,
            'UnlockedAt' => $branch['UnlockedAt'],
            'CompletedAt' => $branch['CompletedAt']
        ];

        if ($branch['BranchType'] === 'intervention') {
            $interventionBranch = $formattedBranch;
        } elseif ($branch['BranchType'] === 'enrichment') {
            $enrichmentBranch = $formattedBranch;
        }
    }

    // Format lesson
    $formattedLesson = [
        'LessonID' => (int)$lessonInfo['LessonID'],
        'Title' => $lessonInfo['LessonTitle'],
        'QuizScore' => $lessonInfo['QuizScore'] ? (int)$lessonInfo['QuizScore'] : null,
        'CompletionStatus' => $lessonInfo['CompletionStatus'] ?? 'NotStarted'
    ];

    // Prepare response
    $response = [
        'success' => true,
        'lesson' => $formattedLesson,
        'intervention' => $interventionBranch,
        'enrichment' => $enrichmentBranch,
        'has_intervention' => $interventionBranch !== null,
        'has_enrichment' => $enrichmentBranch !== null
    ];

    sendResponse($response, 200);

} catch (PDOException $e) {
    error_log("Get lesson branches error: " . $e->getMessage());
    sendError("Failed to fetch lesson branches", 500, $e->getMessage());
} catch (Exception $e) {
    error_log("Get lesson branches error: " . $e->getMessage());
    sendError("An error occurred", 500, $e->getMessage());
}

?>
