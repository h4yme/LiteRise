package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.utils.SessionManager;

/**
 * PHASE 1: Lesson Content Display Activity
 *
 * Displays lesson content with adaptive pacing based on placement level:
 * - BEGINNER (Level 1): SLOW pacing, HIGH scaffolding (detailed examples, hints, step-by-step)
 * - INTERMEDIATE (Level 2): MODERATE pacing, BALANCED scaffolding
 * - ADVANCED (Level 3): FAST pacing, MINIMAL scaffolding (brief explanations)
 *
 * Flow:
 * 1. Load lesson content from API (get_lesson_content.php)
 * 2. Get pacing strategy based on placement level (get_pacing_strategy.php)
 * 3. Display content with appropriate scaffolding
 * 4. Mark LessonCompleted = 1 when student finishes
 * 5. Route to PHASE 2 (Game)
 */
public class LessonContentActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack;
    private TextView tvLessonTitle;
    private TextView tvLessonNumber;
    private TextView tvLessonContent;
    private TextView tvScaffolding;
    private ProgressBar progressBar;
    private Button btnComplete;

    // Data
    private int lessonId;
    private int moduleId;
    private int lessonNumber;
    private String moduleName;
    private int placementLevel; // 1=Beginner, 2=Intermediate, 3=Advanced
    private SessionManager sessionManager;

    // Pacing Strategy
    private String pacingSpeed; // SLOW, MODERATE, FAST
    private String scaffoldingLevel; // HIGH, BALANCED, MINIMAL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_content);

        sessionManager = new SessionManager(this);

        // Get data from intent
        lessonId = getIntent().getIntExtra("lesson_id", 101);
        moduleId = getIntent().getIntExtra("module_id", 1);
        lessonNumber = getIntent().getIntExtra("lesson_number", 1);
        moduleName = getIntent().getStringExtra("module_name");
        placementLevel = getIntent().getIntExtra("placement_level",
                sessionManager.getPlacementLevel());

        initializeViews();
        setupListeners();
        loadLessonContent();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonNumber = findViewById(R.id.tvLessonNumber);
        tvLessonContent = findViewById(R.id.tvLessonContent);
        tvScaffolding = findViewById(R.id.tvScaffolding);
        progressBar = findViewById(R.id.progressBar);
        btnComplete = findViewById(R.id.btnComplete);

        // Set lesson number
        tvLessonNumber.setText("Lesson " + lessonNumber);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnComplete.setOnClickListener(v -> {
            markLessonCompleted();
        });
    }

    /**
     * Load lesson content with adaptive pacing
     */
    private void loadLessonContent() {
        progressBar.setVisibility(View.VISIBLE);

        // Determine pacing strategy based on placement level
        determinePacingStrategy();

        // TODO: Replace with actual API call to get_lesson_content.php
        // For now, using dummy content to demonstrate the flow

        String lessonTitle = "Lesson " + lessonNumber + ": Reading Comprehension";
        String lessonContent = generateDummyContent();

        // Display content
        tvLessonTitle.setText(lessonTitle);
        tvLessonContent.setText(lessonContent);

        progressBar.setVisibility(View.GONE);
        btnComplete.setEnabled(true);
    }

    /**
     * Determine pacing strategy based on placement level
     */
    private void determinePacingStrategy() {
        switch (placementLevel) {
            case 1: // BEGINNER
                pacingSpeed = "SLOW";
                scaffoldingLevel = "HIGH";
                tvScaffolding.setText("📖 BEGINNER MODE: Detailed explanations with examples");
                break;

            case 2: // INTERMEDIATE
                pacingSpeed = "MODERATE";
                scaffoldingLevel = "BALANCED";
                tvScaffolding.setText("📘 INTERMEDIATE MODE: Balanced content");
                break;

            case 3: // ADVANCED
                pacingSpeed = "FAST";
                scaffoldingLevel = "MINIMAL";
                tvScaffolding.setText("📕 ADVANCED MODE: Concise content");
                break;

            default:
                pacingSpeed = "MODERATE";
                scaffoldingLevel = "BALANCED";
                tvScaffolding.setText("📘 INTERMEDIATE MODE: Balanced content");
                break;
        }
    }

    /**
     * Generate dummy content based on pacing strategy
     * TODO: Replace with actual content from API
     */
    private String generateDummyContent() {
        StringBuilder content = new StringBuilder();

        switch (scaffoldingLevel) {
            case "HIGH": // BEGINNER - Detailed with examples
                content.append("Welcome to this lesson! Let's learn step by step.\n\n");
                content.append("📌 Key Concept:\n");
                content.append("Reading comprehension is understanding what you read.\n\n");
                content.append("💡 Example 1:\n");
                content.append("\"The cat sat on the mat.\"\n");
                content.append("Who? The cat. Where? On the mat.\n\n");
                content.append("💡 Example 2:\n");
                content.append("\"John walked to school.\"\n");
                content.append("Who? John. Where did he go? To school.\n\n");
                content.append("✨ Tip: Always ask yourself: Who? What? Where? When? Why?\n\n");
                content.append("Now you try! Read the passage below carefully...");
                break;

            case "BALANCED": // INTERMEDIATE
                content.append("Let's review reading comprehension strategies.\n\n");
                content.append("📌 Key Concepts:\n");
                content.append("• Identify main idea\n");
                content.append("• Find supporting details\n");
                content.append("• Make inferences\n\n");
                content.append("💡 Example:\n");
                content.append("\"The rain poured down. Sarah grabbed her umbrella.\"\n");
                content.append("Main idea: It's raining. Inference: Sarah needs protection.\n\n");
                content.append("Let's practice with the passage below...");
                break;

            case "MINIMAL": // ADVANCED - Brief
                content.append("Reading Comprehension: Advanced Strategies\n\n");
                content.append("Focus areas:\n");
                content.append("• Critical analysis\n");
                content.append("• Implicit meaning\n");
                content.append("• Author's purpose\n\n");
                content.append("Apply these strategies to the passage below...");
                break;
        }

        return content.toString();
    }

    /**
     * Mark lesson as completed and proceed to next phase (Game)
     */
    private void markLessonCompleted() {
        progressBar.setVisibility(View.VISIBLE);
        btnComplete.setEnabled(false);

        // TODO: API call to update_node_progress.php
        // Set LessonCompleted = 1

        Toast.makeText(this,
            "✅ Lesson Complete! Moving to Game...",
            Toast.LENGTH_SHORT).show();

        // Return to ModuleLadderActivity - it will route to Phase 2 (Game)
        finish();
    }
}
