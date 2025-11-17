<?php
/**
 * LiteRise - Get Item Details API
 * Endpoint: GET /api/get_item_details.php?ItemID=1
 * Returns detailed information about a specific assessment item
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

// Get ItemID
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $itemId = isset($_GET['ItemID']) ? (int)$_GET['ItemID'] : 0;
} else {
    $input = json_decode(file_get_contents('php://input'), true);
    $itemId = isset($input['ItemID']) ? (int)$input['ItemID'] : 0;
}

if ($itemId <= 0) {
    http_response_code(400);
    echo json_encode(['error' => 'Valid ItemID required']);
    exit();
}

try {
    $db = new Database();
    $conn = $db->getConnection();

    if (!$conn) {
        throw new Exception('Database connection failed');
    }

    // Get item details
    $sql = "SELECT ItemID, ItemType, QuestionText, PassageText,
                   AnswerChoices, CorrectAnswer,
                   DifficultyParam, DiscriminationParam, GuessingParam,
                   GradeLevel, Skill, Topic,
                   IsActive, CreatedDate
            FROM Items
            WHERE ItemID = :itemId";

    $stmt = $conn->prepare($sql);
    $stmt->bindParam(':itemId', $itemId, PDO::PARAM_INT);
    $stmt->execute();
    $item = $stmt->fetch();

    if (!$item) {
        http_response_code(404);
        echo json_encode(['error' => 'Item not found']);
        exit();
    }

    // Parse answer choices
    $answerChoices = json_decode($item['AnswerChoices'], true);
    if (!$answerChoices || !is_array($answerChoices)) {
        $answerChoices = [];
    }

    // Get usage statistics
    $statsSql = "SELECT
                    COUNT(*) as TimesUsed,
                    SUM(CASE WHEN IsCorrect = 1 THEN 1 ELSE 0 END) as TimesCorrect,
                    AVG(CAST(IsCorrect as FLOAT)) * 100 as SuccessRate,
                    AVG(TimeSpent) as AverageTimeSpent
                 FROM Responses
                 WHERE ItemID = :itemId";

    $statsStmt = $conn->prepare($statsSql);
    $statsStmt->bindParam(':itemId', $itemId, PDO::PARAM_INT);
    $statsStmt->execute();
    $stats = $statsStmt->fetch();

    // Parse question text to extract options
    $questionText = $item['QuestionText'];
    $passageText = $item['PassageText'];

    $response = [
        'success' => true,
        'item' => [
            'ItemID' => (int)$item['ItemID'],
            'ItemType' => $item['ItemType'],
            'QuestionText' => $questionText,
            'PassageText' => $passageText,
            'AnswerChoices' => $answerChoices,
            'CorrectAnswer' => $item['CorrectAnswer'],
            'GradeLevel' => (int)$item['GradeLevel'],
            'Skill' => $item['Skill'],
            'Topic' => $item['Topic'],
            'IsActive' => (bool)$item['IsActive'],
            'CreatedDate' => $item['CreatedDate']
        ],
        'irtParameters' => [
            'Difficulty' => (float)$item['DifficultyParam'],
            'Discrimination' => (float)$item['DiscriminationParam'],
            'Guessing' => (float)$item['GuessingParam']
        ],
        'statistics' => [
            'TimesUsed' => (int)$stats['TimesUsed'],
            'TimesCorrect' => (int)$stats['TimesCorrect'],
            'SuccessRate' => round((float)$stats['SuccessRate'], 2),
            'AverageTimeSpent' => round((float)$stats['AverageTimeSpent'], 2)
        ]
    ];

    http_response_code(200);
    echo json_encode($response);

} catch (Exception $e) {
    error_log("Get item details error: " . $e->getMessage());
    http_response_code(500);
    echo json_encode(['error' => 'Failed to retrieve item details']);
}
