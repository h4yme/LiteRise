package com.example.literise.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.google.android.material.button.MaterialButton;

public class AssessmentResultsActivity extends AppCompatActivity {

    private TextView tvStudentLevel;
    private TextView tvAbilityScore;
    private TextView tvPersonalizedMessage;
    private LinearLayout containerModulePriorities;
    private MaterialButton btnContinueToDashboard;

    private SessionManager sessionManager;
    private MediaPlayer soundPlayer;

    private double estimatedAbility = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_results);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Initialize views
        tvStudentLevel = findViewById(R.id.tvStudentLevel);
        tvAbilityScore = findViewById(R.id.tvAbilityScore);
        tvPersonalizedMessage = findViewById(R.id.tvPersonalizedMessage);
        containerModulePriorities = findViewById(R.id.containerModulePriorities);
        btnContinueToDashboard = findViewById(R.id.btnContinueToDashboard);

        // Get assessment results from intent
        estimatedAbility = getIntent().getDoubleExtra("estimated_ability", 0.0);

        // Display results
        displayResults();

        // Set up continue button
        btnContinueToDashboard.setOnClickListener(v -> {
            playClickSound();
            goToDashboard();
        });

        // Fade in animation
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        rootView.startAnimation(fadeIn);
    }

    /**
     * Display assessment results
     */
    private void displayResults() {
        // Format and display ability score
        tvAbilityScore.setText(String.format("%.2f", estimatedAbility));

        // Determine level based on ability score
        String level = determineLevel(estimatedAbility);
        tvStudentLevel.setText(level);

        // Set personalized message
        String nickname = sessionManager.getNickname();
        if (nickname != null && !nickname.isEmpty()) {
            tvPersonalizedMessage.setText("Great job, " + nickname + "! We've created a personalized learning path just for you.");
        }

        // Display module priorities
        displayModulePriorities(estimatedAbility);
    }

    /**
     * Determine student level based on ability score
     */
    private String determineLevel(double ability) {
        if (ability < -1.0) {
            return "Beginner";
        } else if (ability < 0.0) {
            return "Elementary";
        } else if (ability < 1.0) {
            return "Intermediate";
        } else if (ability < 2.0) {
            return "Advanced";
        } else {
            return "Expert";
        }
    }

    /**
     * Display module priorities based on assessment results
     */
    private void displayModulePriorities(double ability) {
        containerModulePriorities.removeAllViews();

        // Define the 6 literacy modules
        String[] moduleNames = {
                "Reading Comprehension",
                "Phonics & Pronunciation",
                "Vocabulary Building",
                "Grammar & Syntax",
                "Reading Fluency",
                "Spelling & Writing"
        };

        String[] moduleDescriptions = {
                "Start here to improve faster",
                "Build your foundation",
                "Expand your word knowledge",
                "Master sentence structure",
                "Read with confidence",
                "Write like a pro"
        };

        int[] modulePriorityColors = {
                0xFFE74C3C, // Red (highest priority)
                0xFFE67E22, // Orange
                0xFFF39C12, // Yellow
                0xFF00B894, // Green
                0xFF0984E3, // Blue
                0xFF6C5CE7  // Purple (lowest priority)
        };

        // For demo purposes, show all 6 modules in order
        // In production, this would be based on actual assessment analysis
        for (int i = 0; i < Math.min(6, moduleNames.length); i++) {
            View moduleView = LayoutInflater.from(this).inflate(R.layout.item_module_priority, containerModulePriorities, false);

            TextView tvPriorityBadge = moduleView.findViewById(R.id.tvPriorityBadge);
            TextView tvModuleName = moduleView.findViewById(R.id.tvModuleName);
            TextView tvModuleDescription = moduleView.findViewById(R.id.tvModuleDescription);

            tvPriorityBadge.setText(String.valueOf(i + 1));
            tvPriorityBadge.setBackgroundTintList(android.content.res.ColorStateList.valueOf(modulePriorityColors[i]));
            tvModuleName.setText(moduleNames[i]);
            tvModuleDescription.setText(moduleDescriptions[i]);

            containerModulePriorities.addView(moduleView);
        }
    }

    /**
     * Play click sound effect
     */
    private void playClickSound() {
        try {
            if (soundPlayer != null) {
                soundPlayer.release();
            }
            soundPlayer = MediaPlayer.create(this, R.raw.click_sound);
            soundPlayer.setOnCompletionListener(mp -> mp.release());
            soundPlayer.start();
        } catch (Exception e) {
            // Silently fail if sound not found
        }
    }

    /**
     * Navigate to Dashboard
     */
    private void goToDashboard() {
        // Mark that assessment is completed
        sessionManager.setAssessmentCompleted(true);

        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(300);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(AssessmentResultsActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        findViewById(android.R.id.content).startAnimation(fadeOut);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPlayer != null) {
            soundPlayer.release();
            soundPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to assessment
        // User must continue to dashboard
    }
}
