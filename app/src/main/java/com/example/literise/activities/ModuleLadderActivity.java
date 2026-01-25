package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.activities.games.DialogueReadingActivity;
import com.example.literise.activities.games.FillInTheBlanksActivity;
import com.example.literise.activities.games.MinimalPairsActivity;
import com.example.literise.activities.games.PictureMatchActivity;
import com.example.literise.activities.games.SentenceScrambleActivity;
import com.example.literise.activities.games.StorySequencingActivity;
import com.example.literise.activities.games.TimedTrailActivity;
import com.example.literise.activities.games.WordHuntActivity;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.ModuleLadderResponse;
import com.example.literise.models.NodeData;
import com.example.literise.models.NodeProgressResponse;
import com.example.literise.models.NodeView;
import com.example.literise.views.ModulePathView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ModuleLadderActivity extends AppCompatActivity {
    private static final String TAG = "ModuleLadderActivity";

    private ModulePathView pathView;
    private TextView moduleTitle;
    private TextView progressText;
    private ProgressBar moduleProgress;
    private ImageButton backButton;
    private FrameLayout loadingOverlay;

    private ApiService apiService;
    private SessionManager sessionManager;
    private int studentId;
    private int moduleId;
    private String moduleName;
    private String moduleDomain;
    private int moduleLevel;
    private int priority;
    private int placementLevel;

    // Activity result launchers for auto-proceeding through phases
    private ActivityResultLauncher<Intent> lessonLauncher;
    private ActivityResultLauncher<Intent> gameLauncher;
    private ActivityResultLauncher<Intent> quizLauncher;

    // Track current node for auto-progression
    private NodeView currentNode;
    private boolean isAutoProceedMode = true; // Auto-proceed through phases

    // Path coordinates
    // Better coordinates that follow the actual background path
    private static final int[] PATH_X = {50, 28, 18, 22, 32, 48, 65, 72, 68, 52, 38, 45, 50};
    private static final int[] PATH_Y = {88, 82, 76, 70, 64, 58, 52, 46, 40, 34, 28, 22, 15};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_ladder);

        Log.d(TAG, "onCreate started");

        // Initialize activity result launchers BEFORE setContentView
        initializeActivityLaunchers();

        // Initialize views
        pathView = findViewById(R.id.modulePathView);
        moduleTitle = findViewById(R.id.moduleTitle);
        progressText = findViewById(R.id.progressText);
        moduleProgress = findViewById(R.id.moduleProgress);
        backButton = findViewById(R.id.backButton);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        // Initialize API and session
        apiService = ApiClient.getClient(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        // Get data from intent (passed from DashboardActivity)
        studentId = sessionManager.getStudentId();
        moduleId = getIntent().getIntExtra("module_id", 1);
        moduleName = getIntent().getStringExtra("module_name");
        moduleDomain = getIntent().getStringExtra("module_domain");
        moduleLevel = getIntent().getIntExtra("module_level", 1);
        priority = getIntent().getIntExtra("priority", 1);
        placementLevel = convertPlacementLevelToInt(sessionManager.getPlacementLevel());

        // Fallback if module name not provided
        if (moduleName == null || moduleName.isEmpty()) {
            moduleName = "Module " + moduleId;
        }

        Log.d(TAG, "Module Info:");
        Log.d(TAG, "  Student ID: " + studentId);
        Log.d(TAG, "  Module ID: " + moduleId);
        Log.d(TAG, "  Module Name: " + moduleName);
        Log.d(TAG, "  Module Domain: " + moduleDomain);
        Log.d(TAG, "  Module Level: " + moduleLevel);
        Log.d(TAG, "  Priority: " + priority);

        // Set module title
        moduleTitle.setText(moduleName);

        // Set up back button - returns to Dashboard
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish(); // Returns to DashboardActivity
        });

        // Set up node click listener
        pathView.setOnNodeClickListener(node -> handleNodeClick(node));

        // Load module data from API
        loadModuleLadder();

        // OPTIONAL: Uncomment to test with dummy data
        // loadDummyData();
    }

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Initialize activity result launchers for auto-progressing through phases
     * Flow: LESSON → GAME → QUIZ → LADDER (seamless progression)
     */
    private void initializeActivityLaunchers() {
        // Lesson launcher - auto-proceed to game after completion
        lessonLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Returned from LessonContentActivity");

                if (isAutoProceedMode && currentNode != null) {
                    // Automatically proceed to game phase
                    Log.d(TAG, "Auto-proceeding to Game phase");
                    Toast.makeText(this, "✅ Lesson Complete! Now let's play! 🎮", Toast.LENGTH_SHORT).show();

                    // Small delay for toast to show
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        startGamePhase(currentNode);
                    }, 800);
                } else {
                    // User backed out or reviewing - refresh ladder
                    loadModuleLadder();
                }
            }
        );

        // Game launcher - auto-proceed to quiz after completion
        gameLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Returned from Game Activity");

                if (isAutoProceedMode && currentNode != null) {
                    // Automatically proceed to quiz phase
                    Log.d(TAG, "Auto-proceeding to Quiz phase");
                    Toast.makeText(this, "🎉 Great job! Time for the quiz! ✅", Toast.LENGTH_SHORT).show();

                    // Small delay for toast to show
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        startQuizPhase(currentNode);
                    }, 800);
                } else {
                    // User backed out or reviewing - refresh ladder
                    loadModuleLadder();
                }
            }
        );

        // Quiz launcher - final phase, always refresh ladder
        quizLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d(TAG, "Returned from QuizActivity - End of learning flow");
                Toast.makeText(this, "🌟 Awesome! Lesson complete!", Toast.LENGTH_SHORT).show();

                // Quiz is the final phase - refresh ladder to show completion
                currentNode = null; // Clear current node
                loadModuleLadder();
            }
        );
    }

    private void loadModuleLadder() {
        Log.d(TAG, "Loading module ladder for student: " + studentId + ", module: " + moduleId);
        showLoading(true);

        apiService.getModuleLadder(studentId, moduleId).enqueue(new Callback<ModuleLadderResponse>() {
            @Override
            public void onResponse(Call<ModuleLadderResponse> call, Response<ModuleLadderResponse> response) {
                showLoading(false);
                Log.d(TAG, "API Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ModuleLadderResponse data = response.body();
                    Log.d(TAG, "Success: " + data.isSuccess());
                    Log.d(TAG, "Nodes received: " + (data.getNodes() != null ? data.getNodes().size() : "null"));
                    Log.d(TAG, "Current node ID: " + data.getCurrentNodeId());

                    // Debug: Log raw response
                    try {
                        String rawJson = new com.google.gson.Gson().toJson(response.body());
                        Log.d(TAG, "Raw JSON Response: " + rawJson);
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to serialize response to JSON", e);
                    }

                    parseAndDisplayNodes(data);
                } else {
                    Log.e(TAG, "API failed with code: " + response.code());
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "null";
                        Log.e(TAG, "Error body: " + errorBody);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(ModuleLadderActivity.this,
                            "Failed to load module (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();

                    // Load dummy data as fallback
                    loadDummyData();
                }
            }

            @Override
            public void onFailure(Call<ModuleLadderResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                t.printStackTrace();
                Toast.makeText(ModuleLadderActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();

                // Load dummy data as fallback
                loadDummyData();
            }
        });
    }

    private void parseAndDisplayNodes(ModuleLadderResponse data) {
        Log.d(TAG, "Parsing nodes...");
        List<NodeData> nodesData = data.getNodes();

        if (nodesData == null || nodesData.isEmpty()) {
            Log.e(TAG, "No nodes data received!");
            Toast.makeText(this, "No nodes found for this module", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Total nodes: " + nodesData.size());

        int currentNodeId = data.getCurrentNodeId();
        List<NodeView> nodeViews = new ArrayList<>();

        for (int i = 0; i < nodesData.size(); i++) {
            NodeData nodeData = nodesData.get(i);
            int nodeNumber = nodeData.getNodeNumber();

            Log.d(TAG, "Processing node " + nodeNumber + ": " + nodeData.getLessonTitle());
            Log.d(TAG, "  Node ID: " + nodeData.getNodeId());
            Log.d(TAG, "  Node Type: " + nodeData.getNodeType());
            Log.d(TAG, "  Quarter: " + nodeData.getQuarter());
            Log.d(TAG, "  Lesson Completed: " + nodeData.isLessonCompleted());

            // Validate node number
            if (nodeNumber < 1 || nodeNumber > 13) {
                Log.e(TAG, "Invalid node number: " + nodeNumber + " - skipping (lessonTitle=" + nodeData.getLessonTitle() + ", nodeId=" + nodeData.getNodeId() + ")");
                continue;
            }

            // Determine node state
            NodeView.NodeState state;
            if (nodeData.getNodeId() == currentNodeId) {
                state = NodeView.NodeState.CURRENT;
            } else if (nodeData.isLessonCompleted() &&
                    nodeData.isGameCompleted() &&
                    nodeData.isQuizCompleted()) {
                state = NodeView.NodeState.COMPLETED;
            } else if (nodeNumber == 1 || (i > 0 && nodeViews.get(i - 1).getState() == NodeView.NodeState.COMPLETED)) {
                state = NodeView.NodeState.UNLOCKED;
            } else {
                state = NodeView.NodeState.LOCKED;
            }

            // Get position from arrays (already validated above)
            float x = PATH_X[nodeNumber - 1];
            float y = PATH_Y[nodeNumber - 1];

            boolean isFinal = nodeData.getNodeType().equals("FINAL_ASSESSMENT");

            NodeView nodeView = new NodeView(
                    nodeData.getNodeId(),
                    nodeNumber,
                    nodeData.getLessonTitle(),
                    state,
                    x, y,
                    nodeData.getQuarter(),
                    isFinal
            );

            nodeViews.add(nodeView);
            Log.d(TAG, "Added node: " + nodeNumber + " at (" + x + ", " + y + ") state: " + state);
        }

        Log.d(TAG, "Setting " + nodeViews.size() + " nodes to pathView");

        if (nodeViews.isEmpty()) {
            Log.e(TAG, "No valid nodes! All nodes were skipped.");
            Toast.makeText(this, "Error: No valid node data received from API", Toast.LENGTH_LONG).show();
            // Load dummy data as fallback
            loadDummyData();
            return;
        }

        pathView.setNodes(nodeViews);

        // Update progress
        int completedNodes = 0;
        for (NodeView node : nodeViews) {
            if (node.getState() == NodeView.NodeState.COMPLETED) {
                completedNodes++;
            }
        }
        int progress = (completedNodes * 100) / nodeViews.size();
        moduleProgress.setProgress(progress);
        progressText.setText(progress + "%");
    }

    private void handleNodeClick(NodeView node) {
        Log.d(TAG, "Node clicked: " + node.getNodeNumber() + " - " + node.getTitle() + " (State: " + node.getState() + ")");

        switch (node.getState()) {
            case LOCKED:
                Toast.makeText(this, "🔒 Complete previous lessons first!", Toast.LENGTH_SHORT).show();
                break;

            case CURRENT:
            case UNLOCKED:
                // Start the 3-phase flow from where they left off (auto-proceed mode)
                isAutoProceedMode = true;
                currentNode = node;
                startLesson(node);
                break;

            case COMPLETED:
            case MASTERED:
                // Show options to review, replay, or retake (manual mode)
                showCompletedNodeOptions(node);
                break;
        }
    }

    private void startLesson(NodeView node) {
        Log.d(TAG, "Starting lesson for node: " + node.getNodeId() + " - " + node.getTitle());

        // Check progress to determine which phase to start
        int studentId = sessionManager.getStudentId();
        showLoading(true);

        apiService.getNodeProgress(studentId, node.getNodeId()).enqueue(new Callback<NodeProgressResponse>() {
            @Override
            public void onResponse(Call<NodeProgressResponse> call, Response<NodeProgressResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    NodeProgressResponse.Progress progress = response.body().getProgress();
                    Log.d(TAG, "Node progress - Lesson: " + progress.isLessonCompleted() +
                          ", Game: " + progress.isGameCompleted() +
                          ", Quiz: " + progress.isQuizCompleted());

                    routeToAppropriatePhase(node, progress);
                } else {
                    // No progress found - start from lesson phase
                    Log.d(TAG, "No progress found, starting from lesson phase");
                    startLessonPhase(node);
                }
            }

            @Override
            public void onFailure(Call<NodeProgressResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Failed to get node progress: " + t.getMessage());
                // On error, start from lesson phase
                startLessonPhase(node);
            }
        });
    }

    /**
     * Route to appropriate phase based on completion status
     */
    private void routeToAppropriatePhase(NodeView node, NodeProgressResponse.Progress progress) {
        if (!progress.isLessonCompleted()) {
            // PHASE 1: Show Lesson Content
            startLessonPhase(node);
        } else if (!progress.isGameCompleted()) {
            // PHASE 2: Show Game
            startGamePhase(node);
        } else if (!progress.isQuizCompleted()) {
            // PHASE 3: Show Quiz
            startQuizPhase(node);
        } else {
            // All phases complete - show options (review, replay game, retake quiz)
            showCompletedNodeOptions(node);
        }
    }

    /**
     * PHASE 1: Start Lesson Content Display
     */
    private void startLessonPhase(NodeView node) {
        Log.d(TAG, "Starting PHASE 1: LESSON for node " + node.getNodeNumber());
        Toast.makeText(this, "📚 Starting Lesson: " + node.getTitle(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LessonContentActivity.class);
        intent.putExtra("node_id", node.getNodeId());
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", node.getNodeNumber());
        intent.putExtra("lesson_title", node.getTitle());
        intent.putExtra("placement_level", placementLevel);

        // Use launcher to refresh ladder on return
        lessonLauncher.launch(intent);
    }

    /**
     * PHASE 2: Start Game
     */
    private void startGamePhase(NodeView node) {
        Log.d(TAG, "Starting PHASE 2: GAME for node " + node.getNodeNumber());

        // Determine which game to play based on node/module
        String gameType = selectGameForNode(node);
        Class<?> gameActivityClass = getGameActivityClass(gameType);

        if (gameActivityClass == null) {
            Log.w(TAG, "No game available for type: " + gameType + ", skipping to quiz");
            Toast.makeText(this, "🎮 Game coming soon! Moving to quiz...", Toast.LENGTH_SHORT).show();
            startQuizPhase(node);
            return;
        }

        Toast.makeText(this, "🎮 Starting Game: " + formatGameName(gameType), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, gameActivityClass);
        intent.putExtra("node_id", node.getNodeId());
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", node.getNodeNumber());
        intent.putExtra("lesson_id", node.getNodeId()); // Some games expect lesson_id
        intent.putExtra("placement_level", placementLevel);
        intent.putExtra("game_type", gameType);

        // Use launcher to refresh ladder on return
        gameLauncher.launch(intent);
    }

    /**
     * Select appropriate game based on node and module characteristics
     */
    private String selectGameForNode(NodeView node) {
        // Strategy: Vary games to keep engagement high
        // Use node number modulo to cycle through available games
        int nodeNum = node.getNodeNumber();

        // Final assessment gets a special game
        if (node.isFinalAssessment()) {
            return "word_hunt"; // Challenging game for final assessment
        }

        // Rotate through games based on module domain and node number
        if (moduleDomain != null) {
            switch (moduleDomain.toLowerCase()) {
                case "phonics":
                    // Phonics benefits from word-focused games
                    return (nodeNum % 3 == 0) ? "word_hunt" :
                           (nodeNum % 3 == 1) ? "minimal_pairs" : "picture_match";

                case "grammar":
                    // Grammar benefits from sentence-focused games
                    return (nodeNum % 2 == 0) ? "sentence_scramble" : "fill_in_blanks";

                case "vocabulary":
                    // Vocabulary benefits from word association games
                    return (nodeNum % 3 == 0) ? "word_hunt" :
                           (nodeNum % 3 == 1) ? "picture_match" : "story_sequencing";

                case "comprehending":
                    // Comprehension benefits from reading games
                    return (nodeNum % 2 == 0) ? "dialogue_reading" : "story_sequencing";

                case "creating":
                    // Writing benefits from sentence building
                    return (nodeNum % 2 == 0) ? "sentence_scramble" : "fill_in_blanks";
            }
        }

        // Default rotation if domain unknown
        String[] defaultGames = {"word_hunt", "sentence_scramble", "picture_match"};
        return defaultGames[nodeNum % defaultGames.length];
    }

    /**
     * Get the Activity class for a game type
     */
    private Class<?> getGameActivityClass(String gameType) {
        switch (gameType) {
            case "word_hunt":
                return WordHuntActivity.class;
            case "sentence_scramble":
                return SentenceScrambleActivity.class;
            case "picture_match":
                return PictureMatchActivity.class;
            case "fill_in_blanks":
                return FillInTheBlanksActivity.class;
            case "dialogue_reading":
                return DialogueReadingActivity.class;
            case "story_sequencing":
                return StorySequencingActivity.class;
            case "minimal_pairs":
                return MinimalPairsActivity.class;
            case "timed_trail":
                return TimedTrailActivity.class;
            default:
                Log.w(TAG, "Unknown game type: " + gameType);
                return null;
        }
    }

    /**
     * Format game type name for display
     */
    private String formatGameName(String gameType) {
        switch (gameType) {
            case "word_hunt": return "Word Hunt";
            case "sentence_scramble": return "Sentence Scramble";
            case "picture_match": return "Picture Match";
            case "fill_in_blanks": return "Fill in the Blanks";
            case "dialogue_reading": return "Dialogue Reading";
            case "story_sequencing": return "Story Sequencing";
            case "minimal_pairs": return "Minimal Pairs";
            case "timed_trail": return "Timed Trail";
            default: return gameType;
        }
    }

    /**
     * PHASE 3: Start Quiz
     */
    private void startQuizPhase(NodeView node) {
        Log.d(TAG, "Starting PHASE 3: QUIZ for node " + node.getNodeNumber());
        Toast.makeText(this, "✅ Starting Quiz: " + node.getTitle(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("node_id", node.getNodeId());
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", node.getNodeNumber());
        intent.putExtra("module_name", moduleName);
        intent.putExtra("lesson_title", node.getTitle());
        intent.putExtra("placement_level", placementLevel);

        // Use launcher to refresh ladder on return
        quizLauncher.launch(intent);
    }

    /**
     * All phases complete - show options
     */
    private void showCompletedNodeOptions(NodeView node) {
        Log.d(TAG, "Showing completed node options for node " + node.getNodeNumber());

        new AlertDialog.Builder(this)
            .setTitle("✨ " + node.getTitle())
            .setMessage("You've completed this lesson! What would you like to do?")
            .setPositiveButton("📖 Review Lesson", (dialog, which) -> {
                // Review mode - don't auto-proceed
                isAutoProceedMode = false;
                currentNode = node;
                startLessonPhase(node);
            })
            .setNeutralButton("🎮 Replay Game", (dialog, which) -> {
                // Review mode - don't auto-proceed
                isAutoProceedMode = false;
                currentNode = node;
                startGamePhase(node);
            })
            .setNegativeButton("✅ Retake Quiz", (dialog, which) -> {
                // Review mode - don't auto-proceed
                isAutoProceedMode = false;
                currentNode = node;
                startQuizPhase(node);
            })
            .setCancelable(true)
            .show();
    }

    // TEMPORARY: Test with dummy data
    private void loadDummyData() {
        Log.d(TAG, "Loading dummy data for module: " + moduleName);
        List<NodeView> dummyNodes = new ArrayList<>();

        // Different lesson titles based on module
        String[] lessonTitles = getLessonTitlesForModule(moduleId);

        for (int i = 0; i < 13; i++) {
            NodeView.NodeState state;
            if (i == 0) {
                state = NodeView.NodeState.CURRENT;
            } else if (i < 3) {
                state = NodeView.NodeState.UNLOCKED;
            } else {
                state = NodeView.NodeState.LOCKED;
            }

            int quarter = (i / 3) + 1;
            if (i == 12) quarter = 0; // Final assessment

            NodeView node = new NodeView(
                    i + 1,
                    i + 1,
                    lessonTitles[i],
                    state,
                    PATH_X[i],
                    PATH_Y[i],
                    quarter,
                    i == 12
            );
            dummyNodes.add(node);
            Log.d(TAG, "Dummy node " + (i + 1) + ": " + lessonTitles[i] + " at (" + PATH_X[i] + ", " + PATH_Y[i] + ")");
        }

        pathView.setNodes(dummyNodes);
        moduleProgress.setProgress(15);
        progressText.setText("15%");

        Toast.makeText(this, "Loaded dummy data (API unavailable)", Toast.LENGTH_LONG).show();
    }

    private String[] getLessonTitlesForModule(int moduleId) {
        // Return appropriate lesson titles based on module ID
        // Module IDs are assigned based on priority order from placement test
        // We need to map back to the actual module content

        // Get the actual module name to determine content
        String[] titles = new String[13];

        if (moduleName.contains("Phonics")) {
            titles = new String[] {
                    "Basic Sight Words", "CVC Patterns", "Short Vowel Sounds",
                    "Long Vowel Sounds", "Blends and Digraphs", "R-Controlled Vowels",
                    "Silent Letters", "Multi-Syllabic Words", "Word Families",
                    "Advanced Phonics Patterns", "Reading Fluency Practice", "Phonics Review",
                    "Module Assessment"
            };
        } else if (moduleName.contains("Vocabulary")) {
            titles = new String[] {
                    "Basic Nouns and Verbs", "Describing Words", "Everyday Words",
                    "Synonyms and Antonyms", "Context Clues", "Word Roots and Prefixes",
                    "Compound Words", "Multiple Meaning Words", "Academic Vocabulary",
                    "Figurative Language Basics", "Word Choice and Usage", "Vocabulary in Context",
                    "Module Assessment"
            };
        } else if (moduleName.contains("Grammar")) {
            titles = new String[] {
                    "Sentence Basics", "Capitalization Rules", "End Punctuation",
                    "Nouns and Pronouns", "Verbs and Tenses", "Subject-Verb Agreement",
                    "Adjectives and Adverbs", "Conjunctions", "Types of Sentences",
                    "Sentence Expansion", "Common Grammar Errors", "Grammar in Writing",
                    "Module Assessment"
            };
        } else if (moduleName.contains("Comprehending")) {
            titles = new String[] {
                    "Main Idea and Details", "Story Elements", "Sequence of Events",
                    "Making Predictions", "Cause and Effect", "Compare and Contrast",
                    "Making Inferences", "Author's Purpose", "Fact vs Opinion",
                    "Text Features", "Summarizing", "Critical Reading",
                    "Module Assessment"
            };
        } else if (moduleName.contains("Creating")) {
            titles = new String[] {
                    "Writing Complete Sentences", "Organizing Ideas", "Narrative Writing Basics",
                    "Descriptive Writing", "Writing Dialogue", "Informative Writing",
                    "Opinion Writing", "Writing Process", "Paragraph Structure",
                    "Creative Story Writing", "Revising and Editing", "Publishing and Sharing",
                    "Module Assessment"
            };
        } else {
            // Default generic titles
            titles = new String[] {
                    "Lesson 1", "Lesson 2", "Lesson 3", "Lesson 4", "Lesson 5", "Lesson 6",
                    "Lesson 7", "Lesson 8", "Lesson 9", "Lesson 10", "Lesson 11", "Lesson 12",
                    "Module Assessment"
            };
        }

        return titles;
    }

    /**
     * Convert placement level string to integer for API calls
     * Grade 2 / Beginner → 1
     * Grade 3 / Intermediate → 2 (default)
     * Grade 4 / Advanced → 3
     */
    private int convertPlacementLevelToInt(String levelString) {
        if (levelString == null) return 2; // Default to intermediate

        if (levelString.contains("2") || levelString.toLowerCase().contains("beginner")) {
            return 1;
        } else if (levelString.contains("4") || levelString.toLowerCase().contains("advanced")) {
            return 3;
        } else {
            return 2; // Grade 3 or intermediate
        }
    }

    @Override
    public void onBackPressed() {
        // Handle back button press - return to Dashboard
        Log.d(TAG, "onBackPressed called");
        super.onBackPressed();
        finish();
    }
}
