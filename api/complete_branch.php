<?php

/**
 * LiteRise Complete Branch API
 *
 * POST /api/complete_branch.php
 *
 * Marks an intervention or enrichment branch as completed
 * Awards XP and allows student to retry quiz (for intervention) or proceed (for enrichment)
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "branch_id": 1,
 *   "score": 85,
 *   "time_spent": 300,
 *   "activities_completed": 5
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "message": "Intervention completed! You can now retry the quiz.",
 *   "branch": {
 *     "BranchID": 1,
 *     "BranchType": "intervention",
 *     "Title": "Sight Words Review",
 *     "Score": 85,
 *     "CompletedAt": "2026-01-18 11:00:00"
 *   },
 *   "xp_awarded": 10,
 *   "total_xp": 1350,
 *   "next_action": "retry_quiz",
 *   "parent_lesson_id": 101,
 *   "quiz_unlocked": true
 * }
 *
 * OR for enrichment:
 * {
 *   "success": true,
 *   "message": "Enrichment completed! Great work!",
 *   "branch": {
 *     "BranchID": 2,
 *     "BranchType": "enrichment",
 *     "Title": "Advanced Sight Words",
 *     "Score": 95,
 *     "CompletedAt": "2026-01-18 11:00:00"
 *   },
 *   "xp_awarded": 30,
 *   "total_xp": 1380,
 *   "next_action": "continue_lesson",
 *   "parent_lesson_id": 101,
 *   "badge_earned": {
 *     "BadgeID": 5,
 *     "BadgeName": "Enrichment Explorer",
 *     "BadgeDescription": "Completed your first enrichment activity"
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
$branchID = $data['branch_id'] ?? null;
$score = $data['score'] ?? 0;
$timeSpent = $data['time_spent'] ?? 0;
$activitiesCompleted = $data['activities_completed'] ?? 0;

// Validate inputs
if (!$studentID) {
    sendError("student_id is required", 400);
}
if (!$branchID) {
    sendError("branch_id is required", 400);
}
if ($score < 0 || $score > 100) {
    sendError("score must be between 0 and 100", 400);
}

// Verify authenticated user
if ($authUser['studentID'] != $studentID) {
    sendError("Unauthorized: Cannot complete branch for another student", 403);
}

try {
    // Get branch info
    $stmt = $conn->prepare(
        "SELECT lb.BranchID, lb.ParentLessonID, lb.BranchType, lb.Title, lb.Description
         FROM LessonBranches lb
         WHERE lb.BranchID = ?"
    );
    $stmt->execute([$branchID]);
    $branch = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$branch) {
        sendError("Branch not found", 404);
    }

    $branchType = $branch['BranchType'];
    $parentLessonID = (int)$branch['ParentLessonID'];

    // Call stored procedure to complete the branch
    $stmt = $conn->prepare("EXEC SP_CompleteBranch @StudentID = ?, @BranchID = ?, @Score = ?");
    $stmt->execute([$studentID, $branchID, $score]);

    $result = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$result || !$result['Success']) {
        sendError("Failed to complete branch", 500);
    }

    // Determine XP award based on branch type and score
    $xpAwarded = 0;
    if ($branchType === 'intervention') {
        $xpAwarded = 10; // Fixed XP for completing intervention
        $nextAction = 'retry_quiz';
        $message = "Intervention completed! You can now retry the quiz.";
    } else { // enrichment
        // Higher XP for enrichment
        if ($score >= 90) {
            $xpAwarded = 30;
        } elseif ($score >= 75) {
            $xpAwarded = 20;
        } else {
            $xpAwarded = 15;
        }
        $nextAction = 'continue_lesson';
        $message = "Enrichment completed! Great work on the advanced challenge!";
    }

    // Award XP to student
    $stmt = $conn->prepare(
        "UPDATE Students SET TotalXP = TotalXP + ? WHERE StudentID = ?"
    );
    $stmt->execute([$xpAwarded, $studentID]);

    // Get updated total XP
    $stmt = $conn->prepare("SELECT TotalXP FROM Students WHERE StudentID = ?");
    $stmt->execute([$studentID]);
    $studentData = $stmt->fetch(PDO::FETCH_ASSOC);
    $totalXP = (int)$studentData['TotalXP'];

    // Check for badge unlock (first enrichment completion)
    $badgeEarned = null;
    if ($branchType === 'enrichment') {
        // Check if this is the first enrichment
        $stmt = $conn->prepare(
            "SELECT COUNT(*) as EnrichmentCount
             FROM StudentBranches sb
             JOIN LessonBranches lb ON sb.BranchID = lb.BranchID
             WHERE sb.StudentID = ? AND lb.BranchType = 'enrichment' AND sb.Status = 'completed'"
        );
        $stmt->execute([$studentID]);
        $enrichmentData = $stmt->fetch(PDO::FETCH_ASSOC);

        if ((int)$enrichmentData['EnrichmentCount'] === 1) {
            // First enrichment! Award badge
            $stmt = $conn->prepare(
                "SELECT BadgeID, BadgeName, BadgeDescription
                 FROM Badges
                 WHERE BadgeName = 'Enrichment Explorer'"
            );
            $stmt->execute();
            $badge = $stmt->fetch(PDO::FETCH_ASSOC);

            if ($badge) {
                // Award the badge
                $stmt = $conn->prepare(
                    "IF NOT EXISTS (SELECT 1 FROM StudentBadges WHERE StudentID = ? AND BadgeID = ?)
                     BEGIN
                         INSERT INTO StudentBadges (StudentID, BadgeID, DateEarned)
                         VALUES (?, ?, GETDATE())
                     END"
                );
                $stmt->execute([
                    $studentID, $badge['BadgeID'],
                    $studentID, $badge['BadgeID']
                ]);

                $badgeEarned = [
                    'BadgeID' => (int)$badge['BadgeID'],
                    'BadgeName' => $badge['BadgeName'],
                    'BadgeDescription' => $badge['BadgeDescription']
                ];
            }
        }
    }

    // For intervention, unlock the quiz for retry
    $quizUnlocked = false;
    if ($branchType === 'intervention') {
        // Reset lesson lock status to allow quiz retry
        $stmt = $conn->prepare(
            "UPDATE StudentProgress
             SET CompletionStatus = 'InProgress'
             WHERE StudentID = ? AND LessonID = ?"
        );
        $stmt->execute([$studentID, $parentLessonID]);
        $quizUnlocked = true;
    }

    // Log activity
    logActivity($studentID, 'branch_completed', json_encode([
        'branch_id' => $branchID,
        'branch_type' => $branchType,
        'parent_lesson_id' => $parentLessonID,
        'score' => $score,
        'xp_awarded' => $xpAwarded
    ]));

    // Prepare response
    $response = [
        'success' => true,
        'message' => $message,
        'branch' => [
            'BranchID' => (int)$branchID,
            'BranchType' => $branchType,
            'Title' => $branch['Title'],
            'Score' => (int)$score,
            'CompletedAt' => date('Y-m-d H:i:s')
        ],
        'xp_awarded' => (int)$xpAwarded,
        'total_xp' => $totalXP,
        'next_action' => $nextAction,
        'parent_lesson_id' => $parentLessonID
    ];

    // Add conditional fields
    if ($quizUnlocked) {
        $response['quiz_unlocked'] = true;
    }

    if ($badgeEarned) {
        $response['badge_earned'] = $badgeEarned;
    }

    sendResponse($response, 200);

} catch (PDOException $e) {
    error_log("Complete branch error: " . $e->getMessage());
    sendError("Failed to complete branch", 500, $e->getMessage());
} catch (Exception $e) {
    error_log("Complete branch error: " . $e->getMessage());
    sendError("An error occurred", 500, $e->getMessage());
}

?>
