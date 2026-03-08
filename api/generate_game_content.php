<?php
/**
 * Generate Game Content using Claude AI
 *
 * POST /api/generate_game_content.php
 *
 * Generates game-specific content from lesson JSON using the Anthropic Claude API.
 * Results are cached in the database by node_id + game_type to avoid repeated calls.
 *
 * Request Body:
 * {
 *   "node_id":       101,
 *   "game_type":     "minimal_pairs",   // minimal_pairs | timed_trail | picture_match | story_sequencing | synonym_sprint
 *   "lesson_content": "{...}"           // ContentJSON string from the lesson
 * }
 *
 * Response:
 * {
 *   "success":      true,
 *   "game_type":    "minimal_pairs",
 *   "from_cache":   false,
 *   "content":      { ... game-specific JSON ... }
 * }
 */

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Authorization');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

// ─── CONFIG ──────────────────────────────────────────────────────────────────
// Set ANTHROPIC_API_KEY in your server's environment variables or .env file.
// Never commit the actual key to git.
$anthropicApiKey = getenv('ANTHROPIC_API_KEY') ?: '';

require_once __DIR__ . '/src/db.php';

// ─── INPUT ───────────────────────────────────────────────────────────────────
$body = json_decode(file_get_contents('php://input'), true);
$nodeId       = isset($body['node_id'])       ? intval($body['node_id'])           : 0;
$gameType     = isset($body['game_type'])     ? trim($body['game_type'])            : '';
$lessonContent = isset($body['lesson_content']) ? trim($body['lesson_content'])     : '';

$allowedTypes = ['minimal_pairs', 'timed_trail', 'picture_match', 'story_sequencing', 'synonym_sprint'];

if ($nodeId === 0 || !in_array($gameType, $allowedTypes)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'node_id and valid game_type are required']);
    exit;
}

if (empty($lessonContent)) {
    http_response_code(400);
    echo json_encode(['success' => false, 'message' => 'lesson_content is required']);
    exit;
}

// ─── CACHE CHECK ─────────────────────────────────────────────────────────────
try {
    $cacheStmt = $conn->prepare(
        "SELECT ContentJSON FROM GeneratedGameContent WHERE NodeID = ? AND GameType = ?"
    );
    $cacheStmt->execute([$nodeId, $gameType]);
    $cached = $cacheStmt->fetch(PDO::FETCH_ASSOC);

    if ($cached && !empty($cached['ContentJSON'])) {
        echo json_encode([
            'success'    => true,
            'game_type'  => $gameType,
            'from_cache' => true,
            'content'    => json_decode($cached['ContentJSON'], true)
        ]);
        exit;
    }
} catch (Exception $e) {
    // Cache table may not exist yet – proceed to generate
}

// ─── BUILD PROMPT ────────────────────────────────────────────────────────────
$prompts = [
    'minimal_pairs' => <<<PROMPT
You are a Grade 3 English phonics teacher in the Philippines.
Given this lesson content JSON: {LESSON}

Generate 8 minimal pairs for a pronunciation discrimination game.
Focus on the phonics pattern or key words in the lesson (e.g., CVCC, CCVC, vowel sounds, antonyms).
Each pair has:
- targetWord: the word students will try to say
- contrastWord: a similar-sounding word that differs by one phoneme
- hint: a short kid-friendly tip (max 8 words) about the difference

Return ONLY valid JSON, no explanation:
{"pairs":[{"targetWord":"frog","contrastWord":"fog","hint":"Frog starts with fr- blend"},{"targetWord":"stop","contrastWord":"top","hint":"Stop starts with st- blend"},...]}
PROMPT,

    'timed_trail' => <<<PROMPT
You are a Grade 3 English teacher in the Philippines.
Given this lesson content JSON: {LESSON}

Generate 10 multiple-choice questions about the lesson for a timed quiz game.
Questions should test the phonics rule, sight word usage, or vocabulary in the lesson.
Each question has 4 options (A–D). Keep language simple for 8-year-olds.

Return ONLY valid JSON, no explanation:
{"questions":[{"question":"Which word follows the CVCC pattern?","optionA":"frog","optionB":"lamp","optionC":"robot","optionD":"tiger","correct":"B"},...]}
PROMPT,

    'picture_match' => <<<PROMPT
You are a Grade 3 English teacher in the Philippines.
Given this lesson content JSON: {LESSON}

Generate 5 word-emoji pairs for a picture matching game.
Choose concrete, easily-pictureable words from the lesson keywords or examples.
Use a single emoji that clearly represents each word.

Return ONLY valid JSON, no explanation:
{"items":[{"word":"frog","emoji":"🐸"},{"word":"lamp","emoji":"💡"},{"word":"milk","emoji":"🥛"},{"word":"drum","emoji":"🥁"},{"word":"star","emoji":"⭐"}]}
PROMPT,

    'story_sequencing' => <<<PROMPT
You are a Grade 3 English teacher in the Philippines.
Given this lesson content JSON: {LESSON}

Create a short 6-event story that uses words from the lesson.
The story should be set in a Filipino context and use simple sentences for 8-year-olds.
The events must be in a logical sequence (order 1–6).

Return ONLY valid JSON, no explanation:
{"title":"The Lost Puppy","events":[{"order":1,"text":"Maria walked home from school."},{"order":2,"text":"She heard a sound near the bench."},{"order":3,"text":"A small puppy was hiding there."},{"order":4,"text":"She picked up the puppy carefully."},{"order":5,"text":"She found a tag on its collar."},{"order":6,"text":"The owner came to take the puppy home."}]}
PROMPT,

    'synonym_sprint' => <<<PROMPT
You are a Grade 3 English teacher in the Philippines.
Given this lesson content JSON: {LESSON}

Generate 6 word groups for a synonym/antonym sprint game.
Each group has a target word (from the lesson keywords), 3 synonyms or related words, and 3 antonyms or unrelated words.
Keep vocabulary at Grade 3 level.

Return ONLY valid JSON, no explanation:
{"groups":[{"targetWord":"bright","synonyms":["shiny","glowing","light"],"antonyms":["dark","dull","dim"]},{"targetWord":"clean","synonyms":["tidy","neat","fresh"],"antonyms":["dirty","messy","filthy"]},{"targetWord":"small","synonyms":["tiny","little","mini"],"antonyms":["big","large","huge"]},{"targetWord":"full","synonyms":["filled","packed","loaded"],"antonyms":["empty","hollow","bare"]},{"targetWord":"fast","synonyms":["quick","speedy","swift"],"antonyms":["slow","lazy","sluggish"]},{"targetWord":"bright","synonyms":["smart","clever","sharp"],"antonyms":["dull","slow","dim"]}]}
PROMPT
];

$prompt = str_replace('{LESSON}', $lessonContent, $prompts[$gameType]);

// ─── CALL ANTHROPIC API ───────────────────────────────────────────────────────
if (empty($anthropicApiKey)) {
    // No API key — return a helpful error
    http_response_code(503);
    echo json_encode([
        'success' => false,
        'message' => 'ANTHROPIC_API_KEY not configured on server. Set it as an environment variable.'
    ]);
    exit;
}

$requestPayload = json_encode([
    'model'      => 'claude-haiku-4-5-20251001',
    'max_tokens' => 1024,
    'messages'   => [
        ['role' => 'user', 'content' => $prompt]
    ]
]);

$ch = curl_init('https://api.anthropic.com/v1/messages');
curl_setopt_array($ch, [
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_POST           => true,
    CURLOPT_POSTFIELDS     => $requestPayload,
    CURLOPT_TIMEOUT        => 30,
    CURLOPT_HTTPHEADER     => [
        'Content-Type: application/json',
        'x-api-key: ' . $anthropicApiKey,
        'anthropic-version: 2023-06-01'
    ]
]);

$response    = curl_exec($ch);
$httpCode    = curl_getinfo($ch, CURLINFO_HTTP_CODE);
$curlError   = curl_error($ch);
curl_close($ch);

if ($curlError || $httpCode !== 200) {
    http_response_code(502);
    echo json_encode([
        'success' => false,
        'message' => 'Claude API error: ' . ($curlError ?: "HTTP $httpCode"),
        'details' => $response
    ]);
    exit;
}

// ─── PARSE RESPONSE ──────────────────────────────────────────────────────────
$apiResult = json_decode($response, true);
$rawText   = $apiResult['content'][0]['text'] ?? '';

// Extract JSON from the response (Claude may wrap it in markdown code blocks)
if (preg_match('/```(?:json)?\s*(\{.*?\})\s*```/s', $rawText, $matches)) {
    $jsonText = $matches[1];
} elseif (preg_match('/(\{.*\})/s', $rawText, $matches)) {
    $jsonText = $matches[1];
} else {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'message' => 'Could not extract JSON from Claude response',
        'raw'     => $rawText
    ]);
    exit;
}

$gameContent = json_decode($jsonText, true);
if (json_last_error() !== JSON_ERROR_NONE) {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'message' => 'Invalid JSON returned by Claude: ' . json_last_error_msg(),
        'raw'     => $jsonText
    ]);
    exit;
}

// ─── CACHE RESULT ────────────────────────────────────────────────────────────
try {
    $upsert = $conn->prepare("
        IF EXISTS (SELECT 1 FROM GeneratedGameContent WHERE NodeID = ? AND GameType = ?)
            UPDATE GeneratedGameContent SET ContentJSON = ?, UpdatedAt = GETDATE() WHERE NodeID = ? AND GameType = ?
        ELSE
            INSERT INTO GeneratedGameContent (NodeID, GameType, ContentJSON, CreatedAt, UpdatedAt)
            VALUES (?, ?, ?, GETDATE(), GETDATE())
    ");
    $upsert->execute([
        $nodeId, $gameType, $jsonText, $nodeId, $gameType,
        $nodeId, $gameType, $jsonText
    ]);
} catch (Exception $e) {
    // Cache write failed — not fatal, return content anyway
    error_log('GeneratedGameContent cache write failed: ' . $e->getMessage());
}

// ─── RESPOND ─────────────────────────────────────────────────────────────────
echo json_encode([
    'success'    => true,
    'game_type'  => $gameType,
    'from_cache' => false,
    'content'    => $gameContent
]);
