package com.example.literise.activities;



import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;

import android.widget.ImageView;

import android.widget.LinearLayout;

import android.view.ViewGroup;

import android.widget.FrameLayout;

import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;

import com.google.android.material.button.MaterialButton;



public class ModuleLadderActivity extends AppCompatActivity {


    private ScrollView scrollView;
    private ImageView btnBack;

    private TextView tvModuleTitle, tvModuleSubtitle;

    private LinearLayout lessonNodesContainer;

    private MaterialButton btnStart;



    private String moduleName;
    private int moduleId;

    private int totalLessons = 15; // Total lessons per module (15 for Module 1)

    private int currentLesson = 15; // Current unlocked lesson



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_module_ladder);



        // Get module info from intent

        moduleName = getIntent().getStringExtra("module_name");
        moduleId = getIntent().getIntExtra("module_id", 1);

        if (moduleName == null) {

            moduleName = "Reading Comprehension";

        }



        initializeViews();

        setupListeners();

        displayLessonNodes();
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

    }



    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        tvModuleTitle = findViewById(R.id.tvModuleTitle);

        tvModuleSubtitle = findViewById(R.id.tvModuleSubtitle);

        lessonNodesContainer = findViewById(R.id.lessonNodesContainer);

        btnStart = findViewById(R.id.btnStart);


        scrollView = findViewById(R.id.scrollView);
        // Set module title

        tvModuleTitle.setText(moduleName);

    }



    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());



        btnStart.setOnClickListener(v -> {

            // Start first unlocked lesson
            openLesson(currentLesson);

        });

    }



    private void displayLessonNodes() {

        lessonNodesContainer.removeAllViews();


        // Convert dp to pixels for positioning

        float density = getResources().getDisplayMetrics().density;

        int horizontalOffset = (int) (40 * density); // Offset from center for zigzag



        // Display nodes from bottom to top (reversed order)

        // So lesson 1 (current) is at the bottom near START button

        for (int i = totalLessons; i >= 1; i--) {

            // Create a wrapper FrameLayout for horizontal positioning

            FrameLayout wrapper = new FrameLayout(this);

            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(

                    ViewGroup.LayoutParams.MATCH_PARENT,

                    ViewGroup.LayoutParams.WRAP_CONTENT

            );

            wrapper.setLayoutParams(wrapperParams);



            // Inflate the node view

            View nodeView = LayoutInflater.from(this).inflate(

                    R.layout.item_lesson_node,

                    wrapper,

                    false

            );



            ImageView ivNodeBackground = nodeView.findViewById(R.id.ivNodeBackground);

            ImageView ivNodeIcon = nodeView.findViewById(R.id.ivNodeIcon);

            ImageView ivGameBadge = nodeView.findViewById(R.id.ivGameBadge);

            TextView tvLessonNumber = nodeView.findViewById(R.id.tvLessonNumber);



            tvLessonNumber.setText(String.valueOf(i));



            if (i < currentLesson) {

                // Completed lesson - gold

                ivNodeBackground.setImageResource(R.drawable.bg_lesson_node_completed);

                ivNodeIcon.setImageResource(R.drawable.ic_star);

                ivNodeIcon.setColorFilter(0xFFFFFFFF); // White star

            } else if (i == currentLesson) {

                // Current unlocked lesson - white

                ivNodeBackground.setImageResource(R.drawable.bg_lesson_node_unlocked);

                ivNodeIcon.setImageResource(R.drawable.ic_play);

                ivNodeIcon.setColorFilter(0xFF7C3AED); // Purple play icon

            } else {

                // Locked lesson - translucent white

                ivNodeBackground.setImageResource(R.drawable.bg_lesson_node_locked);

                ivNodeIcon.setImageResource(R.drawable.ic_lock);

                ivNodeIcon.setColorFilter(0xFF9D68F5); // Light purple lock

            }

            // Set game type badge
            int lessonId = (moduleId * 100) + i;
            String gameType = getLessonGameType(lessonId);
            setGameBadge(ivGameBadge, gameType);



            // Zigzag pattern - calculate horizontal position

            int position = (i - 1) % 4; // Pattern repeats every 4 nodes

            int leftMargin;



            switch (position) {

                case 0: // Center

                    leftMargin = 0; // Will be centered by gravity

                    break;

                case 1: // Right

                    leftMargin = horizontalOffset;

                    break;

                case 2: // Center

                    leftMargin = 0;

                    break;

                case 3: // Left

                    leftMargin = -horizontalOffset;

                    break;

                default:

                    leftMargin = 0;

                    break;

            }



            // Position node within wrapper using FrameLayout params

            FrameLayout.LayoutParams nodeParams = new FrameLayout.LayoutParams(

                    FrameLayout.LayoutParams.WRAP_CONTENT,

                    FrameLayout.LayoutParams.WRAP_CONTENT

            );

            nodeParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;

            nodeParams.leftMargin = leftMargin;

            nodeParams.rightMargin = -leftMargin; // Compensate

            nodeView.setLayoutParams(nodeParams);



            wrapper.addView(nodeView);



            final int lessonNumber = i;

            wrapper.setOnClickListener(v -> {

                if (lessonNumber <= currentLesson) {

                    // Can play this lesson
                    openLesson(lessonNumber);

                } else {

                    // Locked lesson

                    android.widget.Toast.makeText(this,

                            "Complete previous lessons to unlock",

                            android.widget.Toast.LENGTH_SHORT).show();

                }

            });



            lessonNodesContainer.addView(wrapper);

        }

    }
    /**
     * Opens a lesson - implements 3-phase flow: LESSON → GAME → QUIZ
     * Routes to appropriate activity based on completion status
     */
    private void openLesson(int lessonNumber) {
        // Calculate lesson ID: Module 1 = 101-115, Module 2 = 201-215, etc.
        int lessonId = (moduleId * 100) + lessonNumber;

        // TODO: Query StudentNodeProgress table to get actual completion status
        // For now, using dummy logic to demonstrate the flow

        // Check completion status for this node
        NodeProgress progress = getNodeProgress(lessonId);

        if (!progress.lessonCompleted) {
            // PHASE 1: Show Lesson Content
            startLessonPhase(lessonId, lessonNumber);
        } else if (!progress.gameCompleted) {
            // PHASE 2: Show Game
            startGamePhase(lessonId, lessonNumber);
        } else if (!progress.quizCompleted) {
            // PHASE 3: Show Quiz
            startQuizPhase(lessonId, lessonNumber);
        } else {
            // All phases complete - show options (review, replay game, retake quiz)
            showCompletedNodeOptions(lessonId, lessonNumber);
        }
    }

    /**
     * PHASE 1: Start Lesson Content Display
     * Shows adaptive content based on placement level
     */
    private void startLessonPhase(int lessonId, int lessonNumber) {
        android.widget.Toast.makeText(this,
            "📚 Starting Lesson " + lessonNumber,
            android.widget.Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LessonContentActivity.class);
        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", lessonNumber);
        intent.putExtra("module_name", moduleName);
        // TODO: Add placement_level from SessionManager
        startActivity(intent);
    }

    /**
     * PHASE 2: Start Game
     * Routes to appropriate game activity based on game type
     */
    private void startGamePhase(int lessonId, int lessonNumber) {
        String gameType = getLessonGameType(lessonId);

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
            intent.putExtra("lesson_id", lessonId);
            intent.putExtra("module_id", moduleId);
            intent.putExtra("lesson_number", lessonNumber);
            startActivity(intent);
        }
    }

    /**
     * PHASE 3: Start Quiz
     * Shows quiz questions and handles adaptive decisions
     */
    private void startQuizPhase(int lessonId, int lessonNumber) {
        android.widget.Toast.makeText(this,
            "✅ Starting Quiz " + lessonNumber,
            android.widget.Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", lessonNumber);
        intent.putExtra("module_name", moduleName);
        startActivity(intent);
    }

    /**
     * Shows options when all phases are complete
     * Allows review, replay game, or retake quiz
     */
    private void showCompletedNodeOptions(int lessonId, int lessonNumber) {
        android.widget.Toast.makeText(this,
            "✨ Lesson " + lessonNumber + " Complete!\n📖 Review | 🎮 Replay | ✅ Retake",
            android.widget.Toast.LENGTH_LONG).show();

        // TODO: Show dialog with options to review lesson, replay game, or retake quiz
    }

    /**
     * Gets node progress from cache or database
     * TODO: Replace with actual API call or local database query
     */
    private NodeProgress getNodeProgress(int lessonId) {
        // DUMMY DATA for testing
        // In production, this should query StudentNodeProgress table
        NodeProgress progress = new NodeProgress();
        progress.lessonCompleted = false; // Force to show lesson first
        progress.gameCompleted = false;
        progress.quizCompleted = false;
        return progress;
    }

    /**
     * Inner class to hold node completion status
     */
    private static class NodeProgress {
        boolean lessonCompleted;
        boolean gameCompleted;
        boolean quizCompleted;
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