<?php
/**
 * Get Node Progress API
 * 
 * Endpoint: GET /api/get_node_progress.php
 * Description: Gets completion status for a student's node
 * 
 * Parameters:
 * - student_id (required)
 * - node_id (required)
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET');

require_once __DIR__ . '/src/db.php';

try {
    $studentId = isset($_GET['student_id']) ? intval($_GET['student_id']) : 0;
    $nodeId = isset($_GET['node_id']) ? intval($_GET['node_id']) : 0;

    if ($studentId === 0) {
        http_response_code(400);
        echo json_encode([
            'success' => false,
            'message' => 'Student ID is required'
        ]);
        exit;
    }

    // Portal mode: no node_id → return all 65 nodes for the student
    if ($nodeId === 0) {
        $stmt = $conn->prepare("
            SELECT
                N.NodeID        AS node_id,
                N.NodeNumber    AS node_number,
                N.ModuleID      AS module_id,
                N.LessonTitle   AS title,
                COALESCE(SNP.LessonCompleted, 0) AS lesson_completed,
                COALESCE(SNP.GameCompleted,   0) AS game_completed,
                COALESCE(SNP.QuizCompleted,   0) AS quiz_completed,
                SNP.LatestQuizScore             AS quiz_score,
                SNP.CompletedDate               AS completed_at,
                CASE
                    WHEN SNP.NodeID IS NULL                         THEN 'locked'
                    WHEN COALESCE(SNP.LessonCompleted, 0) = 1      THEN 'completed'
                    ELSE 'in_progress'
                END AS status
            FROM Nodes N
            LEFT JOIN StudentNodeProgress SNP
                ON N.NodeID = SNP.NodeID AND SNP.StudentID = ?
            WHERE N.IsActive = 1
            ORDER BY N.ModuleID, N.NodeNumber
        ");
        $stmt->execute([$studentId]);
        $nodes = $stmt->fetchAll(PDO::FETCH_ASSOC);
        foreach ($nodes as &$n) {
            $n['node_id']          = (int)$n['node_id'];
            $n['node_number']      = (int)$n['node_number'];
            $n['module_id']        = (int)$n['module_id'];
            $n['lesson_completed'] = (bool)$n['lesson_completed'];
            $n['game_completed']   = (bool)$n['game_completed'];
            $n['quiz_completed']   = (bool)$n['quiz_completed'];
            $n['quiz_score']       = $n['quiz_score'] !== null ? (float)$n['quiz_score'] : null;
        }
        unset($n);
        http_response_code(200);
        echo json_encode($nodes);
        exit;
    }

    $stmt = $conn->prepare("
        SELECT
            LessonCompleted,
            GameCompleted,
            QuizCompleted,
            LatestQuizScore,
            NodeState,
            CompletedDate
        FROM StudentNodeProgress
        WHERE StudentID = ? AND NodeID = ?
    ");
    $stmt->execute([$studentId, $nodeId]);
    $progress = $stmt->fetch(PDO::FETCH_ASSOC);
    
    if (!$progress) {
        http_response_code(200);
        echo json_encode([
            'success' => true,
            'progress' => [
                'lesson_completed' => false,
                'game_completed' => false,
                'quiz_completed' => false,
                'quiz_score' => 0,
                'adaptive_decision' => null,
                'completed_at' => null
            ]
        ]);
    } else {
        http_response_code(200);
        echo json_encode([
            'success' => true,
            'progress' => [
                'lesson_completed' => (bool)$progress['LessonCompleted'],
                'game_completed' => (bool)$progress['GameCompleted'],
                'quiz_completed' => (bool)$progress['QuizCompleted'],
                'quiz_score' => (float)$progress['LatestQuizScore'],
                'adaptive_decision' => $progress['NodeState'], // Using NodeState as adaptive decision
                'completed_at' => $progress['CompletedDate']
            ]
        ]);
    }
    
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Database error',
        'error' => $e->getMessage()
    ]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Server error',
        'error' => $e->getMessage()
    ]);
}

?>
