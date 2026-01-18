<?php

/**
 * LiteRise Get Module Structure API
 *
 * POST /api/get_module_structure.php
 *
 * Returns the 13-node structure for a module with student progress
 * Includes: 12 lessons (3 per quarter) + 1 module assessment
 *
 * Request Body:
 * {
 *   "student_id": 1,
 *   "module_id": 1
 * }
 *
 * Response:
 * {
 *   "success": true,
 *   "module": {
 *     "ModuleID": 1,
 *     "ModuleName": "Phonics and Word Study",
 *     "TotalLessons": 12,
 *     "CompletedLessons": 3,
 *     "ProgressPercentage": 25,
 *     "CurrentLessonID": 104,
 *     "IsActive": true
 *   },
 *   "lessons": [
 *     {
 *       "LessonID": 101,
 *       "LessonNumber": 1,
 *       "Quarter": 1,
 *       "Title": "Sight Words: The Basics",
 *       "Description": "...",
 *       "GameType": "word_hunt",
 *       "RequiredAbility": -1.0,
 *       "InterventionThreshold": 60,
 *       "EnrichmentThreshold": 85,
 *       "XPReward": 20,
 *       "IsUnlocked": true,
 *       "IsCompleted": true,
 *       "QuizScore": 85,
 *       "CompletedAt": "2026-01-15 10:30:00",
 *       "HasIntervention": true,
 *       "HasEnrichment": true,
 *       "InterventionStatus": "not_unlocked",
 *       "EnrichmentStatus": "completed"
 *     },
 *     ...
 *   ],
 *   "assessment": {
 *     "LessonID": 113,
 *     "Title": "Module 1 Assessment",
 *     "IsUnlocked": false,
 *     "RequiredLessons": 12
 *   },
 *   "proficiency": {
 *     "CurrentAbility": 0.5,
 *     "ProficiencyLevel": "Intermediate",
 *     "Theta": 0.5
 *   }
 * }
 */

require_once __DIR__ . '/src/db.php';
require_once __DIR__ . '/src/auth.php';
require_once __DIR__ . '/irt.php';

// Require authentication
$authUser = requireAuth();

// Get JSON input
$data = getJsonInput();
$studentID = $data['student_id'] ?? null;
$moduleID = $data['module_id'] ?? null;

// Validate inputs
if (!$studentID) {
    sendError("student_id is required", 400);
}
if (!$moduleID) {
    sendError("module_id is required", 400);
}

// Verify authenticated user
if ($authUser['studentID'] != $studentID) {
    sendError("Unauthorized: Cannot view module structure for another student", 403);
}

try {
    $irt = new ItemResponseTheory();

    // Get student's current ability
    $stmt = $conn->prepare("SELECT CurrentAbility FROM Students WHERE StudentID = ?");
    $stmt->execute([$studentID]);
    $student = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$student) {
        sendError("Student not found", 404);
    }

    $currentAbility = (float)$student['CurrentAbility'];
    $proficiencyLevel = $irt->classifyAbility($currentAbility);

    // Call stored procedure to get module structure
    $stmt = $conn->prepare("EXEC SP_GetModuleStructure @StudentID = ?, @ModuleID = ?");
    $stmt->execute([$studentID, $moduleID]);

    // Get module info (first result set)
    $moduleInfo = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$moduleInfo) {
        sendError("Module not found", 404);
    }

    // Move to next result set (lessons)
    $stmt->nextRowset();
    $lessons = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // Move to next result set (assessment)
    $stmt->nextRowset();
    $assessment = $stmt->fetch(PDO::FETCH_ASSOC);

    // Format lessons
    $formattedLessons = array_map(function($lesson) {
        return [
            'LessonID' => (int)$lesson['LessonID'],
            'LessonNumber' => (int)$lesson['LessonNumber'],
            'Quarter' => (int)$lesson['Quarter'],
            'Title' => $lesson['LessonTitle'],
            'Description' => $lesson['LessonDescription'],
            'GameType' => $lesson['GameType'] ?? 'traditional',
            'RequiredAbility' => (float)($lesson['RequiredAbility'] ?? 0.0),
            'InterventionThreshold' => (int)($lesson['InterventionThreshold'] ?? 60),
            'EnrichmentThreshold' => (int)($lesson['EnrichmentThreshold'] ?? 85),
            'XPReward' => (int)($lesson['XPReward'] ?? 20),
            'IsUnlocked' => (bool)$lesson['IsUnlocked'],
            'IsCompleted' => (bool)$lesson['IsCompleted'],
            'QuizScore' => $lesson['QuizScore'] ? (int)$lesson['QuizScore'] : null,
            'CompletedAt' => $lesson['CompletedAt'],
            'HasIntervention' => (bool)$lesson['HasIntervention'],
            'HasEnrichment' => (bool)$lesson['HasEnrichment'],
            'InterventionStatus' => $lesson['InterventionStatus'] ?? 'not_unlocked',
            'EnrichmentStatus' => $lesson['EnrichmentStatus'] ?? 'not_unlocked'
        ];
    }, $lessons);

    // Format module
    $formattedModule = [
        'ModuleID' => (int)$moduleInfo['ModuleID'],
        'ModuleName' => $moduleInfo['ModuleName'],
        'TotalLessons' => (int)$moduleInfo['TotalLessons'],
        'CompletedLessons' => (int)$moduleInfo['CompletedLessons'],
        'ProgressPercentage' => round((float)$moduleInfo['ProgressPercentage'], 1),
        'CurrentLessonID' => $moduleInfo['CurrentLessonID'] ? (int)$moduleInfo['CurrentLessonID'] : null,
        'IsActive' => (bool)$moduleInfo['IsActive']
    ];

    // Format assessment
    $formattedAssessment = null;
    if ($assessment) {
        $formattedAssessment = [
            'LessonID' => (int)$assessment['LessonID'],
            'Title' => $assessment['LessonTitle'],
            'Description' => $assessment['LessonDescription'],
            'IsUnlocked' => (bool)$assessment['IsUnlocked'],
            'IsCompleted' => (bool)$assessment['IsCompleted'],
            'RequiredLessons' => (int)($assessment['RequiredLessons'] ?? 12),
            'Score' => $assessment['Score'] ? (int)$assessment['Score'] : null
        ];
    }

    // Prepare response
    $response = [
        'success' => true,
        'module' => $formattedModule,
        'lessons' => $formattedLessons,
        'assessment' => $formattedAssessment,
        'proficiency' => [
            'CurrentAbility' => round($currentAbility, 3),
            'ProficiencyLevel' => $proficiencyLevel,
            'Theta' => round($currentAbility, 3)
        ]
    ];

    sendResponse($response, 200);

} catch (PDOException $e) {
    error_log("Get module structure error: " . $e->getMessage());
    sendError("Failed to fetch module structure", 500, $e->getMessage());
} catch (Exception $e) {
    error_log("Get module structure error: " . $e->getMessage());
    sendError("An error occurred", 500, $e->getMessage());
}

?>
