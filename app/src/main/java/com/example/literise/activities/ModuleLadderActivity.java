package com.example.literise.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.ModuleLadderResponse;
import com.example.literise.models.NodeData;
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

    // Path coordinates
    // Update the PATH coordinates in ModuleLadderActivity.java
    private static final int[] PATH_X = {48, 28, 16, 32, 49, 65, 77, 63, 47, 23, 37, 55, 25};
    private static final int[] PATH_Y = {77, 74, 66, 61, 58, 55, 50, 46, 43, 38, 32, 27, 15};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_ladder);

        Log.d(TAG, "onCreate started");

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

            // Get position from arrays (check bounds)
            if (nodeNumber - 1 >= PATH_X.length || nodeNumber - 1 >= PATH_Y.length) {
                Log.e(TAG, "Node number " + nodeNumber + " exceeds path coordinates!");
                continue;
            }

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

        if (node.getState() == NodeView.NodeState.LOCKED) {
            Toast.makeText(this, "🔒 Complete previous lessons first!", Toast.LENGTH_SHORT).show();
        } else if (node.getState() == NodeView.NodeState.CURRENT ||
                node.getState() == NodeView.NodeState.UNLOCKED) {
            startLesson(node);
        } else if (node.getState() == NodeView.NodeState.COMPLETED) {
            Toast.makeText(this, "✓ Lesson completed!", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLesson(NodeView node) {
        // TODO: Start lesson activity with node data
        Toast.makeText(this, "Starting: " + node.getTitle(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Starting lesson for node: " + node.getNodeId());

        // You can launch your lesson activity here
        // Intent intent = new Intent(this, LessonActivity.class);
        // intent.putExtra("node_id", node.getNodeId());
        // intent.putExtra("module_id", moduleId);
        // intent.putExtra("lesson_title", node.getTitle());
        // startActivity(intent);
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

    @Override
    public void onBackPressed() {
        // Handle back button press - return to Dashboard
        Log.d(TAG, "onBackPressed called");
        super.onBackPressed();
        finish();
    }
}
