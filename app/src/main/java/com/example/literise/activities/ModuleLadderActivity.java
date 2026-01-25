package com.example.literise.activities;



import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;

import android.widget.ImageView;

import android.widget.LinearLayout;

import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.NodeProgressResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class ModuleLadderActivity extends AppCompatActivity {


    private ImageView backButton;
    private TextView moduleTitle;
    private ProgressBar moduleProgress;
    private TextView progressText;
    private com.example.literise.views.ModulePathView modulePathView;

    private SessionManager sessionManager;

    private String moduleName;
    private int moduleId;

    private int totalLessons = 15; // Total lessons per module (15 for Module 1)
    private int currentLesson = 15; // Current unlocked lesson



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_module_ladder);



        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Get module info from intent

        moduleName = getIntent().getStringExtra("module_name");
        moduleId = getIntent().getIntExtra("module_id", 1);

        if (moduleName == null) {

            moduleName = "Reading Comprehension";

        }



        initializeViews();
        setupListeners();
        loadModuleProgress();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        moduleTitle = findViewById(R.id.moduleTitle);
        moduleProgress = findViewById(R.id.moduleProgress);
        progressText = findViewById(R.id.progressText);
        modulePathView = findViewById(R.id.modulePathView);

        // Set module title
        moduleTitle.setText(moduleName);
    }

    private void setupListeners() {
        backButton.setOnClickListener(v -> finish());

        // ModulePathView will handle node clicks through its own listener
        // We'll need to set up the click listener for the custom view
    }

    private void loadModuleProgress() {
        // TODO: Load actual progress from API
        // For now, show default progress
        moduleProgress.setProgress(0);
        progressText.setText("0%");
    }



    /**
     * Opens a lesson - implements 3-phase flow: LESSON → GAME → QUIZ
     * Routes to appropriate activity based on completion status
     */
    private void openLesson(int lessonNumber) {
        // Calculate node ID: Module 1 = 1-13, Module 2 = 14-26, etc.
        int nodeId = ((moduleId - 1) * 13) + lessonNumber;

        // Query API to get actual completion status
        checkNodeProgressAndRoute(nodeId, lessonNumber);
    }

    /**
     * Check node progress from API and route to appropriate phase
     */
    private void checkNodeProgressAndRoute(int nodeId, int lessonNumber) {
        int studentId = sessionManager.getStudentId();

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getNodeProgress(studentId, nodeId).enqueue(new Callback<NodeProgressResponse>() {
            @Override
            public void onResponse(Call<NodeProgressResponse> call, Response<NodeProgressResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    NodeProgressResponse.Progress progress = response.body().getProgress();
                    routeToAppropriatePhase(nodeId, lessonNumber, progress);
                } else {
                    // No progress found - start from lesson phase
                    startLessonPhase(nodeId, lessonNumber);
                }
            }

            @Override
            public void onFailure(Call<NodeProgressResponse> call, Throwable t) {
                android.widget.Toast.makeText(ModuleLadderActivity.this,
                    "Network error: " + t.getMessage(),
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Route to appropriate phase based on completion status
     */
    private void routeToAppropriatePhase(int nodeId, int lessonNumber, NodeProgressResponse.Progress progress) {
        if (!progress.isLessonCompleted()) {
            // PHASE 1: Show Lesson Content
            startLessonPhase(nodeId, lessonNumber);
        } else if (!progress.isGameCompleted()) {
            // PHASE 2: Show Game
            startGamePhase(nodeId, lessonNumber);
        } else if (!progress.isQuizCompleted()) {
            // PHASE 3: Show Quiz
            startQuizPhase(nodeId, lessonNumber);
        } else {
            // All phases complete - show options (review, replay game, retake quiz)
            showCompletedNodeOptions(nodeId, lessonNumber);
        }
    }

    /**
     * PHASE 1: Start Lesson Content Display
     * Shows adaptive content based on placement level
     */
    private void startLessonPhase(int nodeId, int lessonNumber) {
        android.widget.Toast.makeText(this,
            "📚 Starting Lesson " + lessonNumber,
            android.widget.Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LessonContentActivity.class);
        intent.putExtra("node_id", nodeId);
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", lessonNumber);
        intent.putExtra("module_name", moduleName);
        startActivity(intent);
    }

    /**
     * PHASE 2: Start Game
     * Routes to appropriate game activity based on game type
     */
    private void startGamePhase(int nodeId, int lessonNumber) {
        int legacyLessonId = (moduleId * 100) + lessonNumber;
        String gameType = getLessonGameType(legacyLessonId);

        android.widget.Toast.makeText(this,
            "🎮 Starting Game: " + gameType,
            android.widget.Toast.LENGTH_SHORT).show();

        Intent intent = null;

        // Route to the appropriate fun game activity
        switch (gameType) {
            case "sentence_scramble":
                intent = new Intent(this, com.example.literise.activities.games.SentenceScrambleActivity.class);
                break;

            case "word_hunt":
                intent = new Intent(this, com.example.literise.activities.games.WordHuntActivity.class);
                break;

            case "timed_trail":
                intent = new Intent(this, com.example.literise.activities.games.TimedTrailActivity.class);
                break;

            case "shadow_read":
                intent = new Intent(this, com.example.literise.activities.games.DialogueReadingActivity.class);
                break;

            case "minimal_pairs":
                intent = new Intent(this, com.example.literise.activities.games.MinimalPairsActivity.class);
                break;

            case "traditional":
            default:
                // Fall back to traditional lesson activity
                intent = new Intent(this, ModuleLessonActivity.class);
                break;
        }

        if (intent != null) {
            intent.putExtra("node_id", nodeId);
            intent.putExtra("module_id", moduleId);
            intent.putExtra("lesson_number", lessonNumber);
            startActivity(intent);
        }
    }

    /**
     * PHASE 3: Start Quiz
     * Shows quiz questions and handles adaptive decisions
     */
    private void startQuizPhase(int nodeId, int lessonNumber) {
        android.widget.Toast.makeText(this,
            "✅ Starting Quiz " + lessonNumber,
            android.widget.Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("node_id", nodeId);
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", lessonNumber);
        intent.putExtra("module_name", moduleName);
        startActivity(intent);
    }

    /**
     * Shows options when all phases are complete
     * Allows review, replay game, or retake quiz
     */
    private void showCompletedNodeOptions(int nodeId, int lessonNumber) {
        android.widget.Toast.makeText(this,
            "✨ Lesson " + lessonNumber + " Complete!\n📖 Review | 🎮 Replay | ✅ Retake",
            android.widget.Toast.LENGTH_LONG).show();

        // TODO: Show dialog with options to review lesson, replay game, or retake quiz
    }


    private String getLessonGameType(int lessonId) {
        // For Module 1, get game type from content provider
        if (moduleId == 1) {
            try {
                java.lang.reflect.Method method = com.example.literise.content.Module1ContentProvider.class
                        .getDeclaredMethod("getAllLessons");
                java.util.List<?> lessons = (java.util.List<?>) method.invoke(null);

                android.util.Log.d("ModuleLadder", "Got " + lessons.size() + " lessons from provider");

                for (Object lessonObj : lessons) {
                    com.example.literise.models.Lesson lesson = (com.example.literise.models.Lesson) lessonObj;
                    if (lesson.getLessonId() == lessonId) {
                        String gameType = lesson.getGameType();
                        android.util.Log.d("ModuleLadder", "Lesson " + lessonId + " has game type: " + gameType);
                        return gameType != null ? gameType : "traditional";
                    }
                }
                android.util.Log.w("ModuleLadder", "Lesson " + lessonId + " not found in provider");
            } catch (Exception e) {
                android.util.Log.e("ModuleLadder", "Error getting game type: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return "traditional";
    }

    /**
     * Sets the game badge icon based on the game type
     */
    private void setGameBadge(android.widget.ImageView gameBadge, String gameType) {
        if (gameBadge == null) return;

        int iconResource;
        switch (gameType) {
            case "word_hunt":
                iconResource = R.drawable.ic_game_word_hunt;
                gameBadge.setVisibility(android.view.View.VISIBLE);
                break;
            case "sentence_scramble":
                iconResource = R.drawable.ic_game_sentence_scramble;
                gameBadge.setVisibility(android.view.View.VISIBLE);
                break;
            case "timed_trail":
                iconResource = R.drawable.ic_game_timed_trail;
                gameBadge.setVisibility(android.view.View.VISIBLE);
                break;
            case "shadow_read":
                iconResource = R.drawable.ic_game_shadow_read;
                gameBadge.setVisibility(android.view.View.VISIBLE);
                break;
            case "minimal_pairs":
                iconResource = R.drawable.ic_game_minimal_pairs;
                gameBadge.setVisibility(android.view.View.VISIBLE);
                break;
            case "traditional":
            default:
                // No badge for traditional lessons
                gameBadge.setVisibility(android.view.View.GONE);
                return;
        }
        gameBadge.setImageResource(iconResource);
    }

}