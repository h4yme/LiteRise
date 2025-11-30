package com.example.literise.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;

public class PreAssessmentTutorialActivity extends AppCompatActivity {

    private View rootLayout;
    private View overlayDark;
    private TextView tvTutorialTitle;
    private TextView tvTutorialMessage;
    private TextView tvTapToContinue;

    // Mock UI elements
    private CardView cardMockPassage;
    private CardView cardMockPronunciation;
    private CardView cardMockQuestion;
    private View btnMockContinue;

    private MediaPlayer soundPlayer;
    private int currentStep = 0; // 0 to 4 (5 tutorial steps)

    // Tutorial content for each step
    private final String[] tutorialTitles = {
        "Welcome to Placement Test!",
        "Step 1: Read Carefully 📖",
        "Step 2: Choose Your Answer ✓",
        "Step 3: Pronunciation Test 🎤",
        "Step 4: Click Continue ▶"
    };

    private final String[] tutorialMessages = {
        "Hi! I'm Leo! 🦁\n\nLet me show you how this test works. Tap anywhere to continue!",

        "Some questions will have a passage to read.\n\nRead it carefully before answering the question!",

        "For most questions, you'll see 4 answer choices (a, b, c, d).\n\nJust tap the answer you think is correct!",

        "For pronunciation questions, you'll see a microphone button.\n\nTap it, then say the word out loud clearly!",

        "After selecting your answer, click the Continue button to move to the next question.\n\nReady? Let's start!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_assessment_tutorial);

        // Initialize views
        rootLayout = findViewById(R.id.rootLayout);
        overlayDark = findViewById(R.id.overlayDark);
        tvTutorialTitle = findViewById(R.id.tvTutorialTitle);
        tvTutorialMessage = findViewById(R.id.tvTutorialMessage);
        tvTapToContinue = findViewById(R.id.tvTapToContinue);

        // Mock UI elements
        cardMockPassage = findViewById(R.id.cardMockPassage);
        cardMockPronunciation = findViewById(R.id.cardMockPronunciation);
        cardMockQuestion = findViewById(R.id.cardMockQuestion);
        btnMockContinue = findViewById(R.id.btnMockContinue);

        // Set click listener on entire layout
        rootLayout.setOnClickListener(v -> {
            playClickSound();
            nextStep();
        });

        // Show first step
        updateTutorialStep();

        // Fade in animation on start
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(300);
        rootLayout.startAnimation(fadeIn);
    }

    /**
     * Update tutorial content and highlights based on current step
     */
    private void updateTutorialStep() {
        // Update text content
        tvTutorialTitle.setText(tutorialTitles[currentStep]);
        tvTutorialMessage.setText(tutorialMessages[currentStep]);

        // Reset all highlights
        resetHighlights();

        // Apply step-specific highlights and visibility
        switch (currentStep) {
            case 0: // Welcome
                // All mock UI visible but dimmed
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                break;

            case 1: // Highlight passage
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                highlightView(cardMockPassage);
                break;

            case 2: // Highlight answer options
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                highlightView(cardMockQuestion);
                break;

            case 3: // Highlight microphone
                cardMockPassage.setVisibility(View.GONE);
                cardMockPronunciation.setVisibility(View.VISIBLE);
                cardMockQuestion.setVisibility(View.GONE);
                btnMockContinue.setVisibility(View.VISIBLE);
                highlightView(cardMockPronunciation);
                break;

            case 4: // Highlight continue button
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                highlightView(btnMockContinue);
                break;
        }
    }

    /**
     * Reset all highlights
     */
    private void resetHighlights() {
        cardMockPassage.setAlpha(0.5f);
        cardMockPronunciation.setAlpha(0.5f);
        cardMockQuestion.setAlpha(0.5f);
        btnMockContinue.setAlpha(0.5f);
    }

    /**
     * Highlight a specific view (make it stand out)
     */
    private void highlightView(View view) {
        view.setAlpha(1.0f);
        view.setElevation(16f);

        // Pulse animation
        AlphaAnimation pulse = new AlphaAnimation(0.8f, 1.0f);
        pulse.setDuration(800);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        view.startAnimation(pulse);
    }

    /**
     * Move to next tutorial step
     */
    private void nextStep() {
        if (currentStep < tutorialTitles.length - 1) {
            currentStep++;

            // Fade transition
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.7f);
            fadeOut.setDuration(150);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    updateTutorialStep();
                    AlphaAnimation fadeIn = new AlphaAnimation(0.7f, 1.0f);
                    fadeIn.setDuration(150);
                    rootLayout.startAnimation(fadeIn);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            rootLayout.startAnimation(fadeOut);
        } else {
            // Tutorial complete, go to placement test
            goToPlacementTest();
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
     * Navigate to Placement Test
     */
    private void goToPlacementTest() {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(200);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(PreAssessmentTutorialActivity.this, AdaptivePreAssessmentActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        rootLayout.startAnimation(fadeOut);
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
        // Prevent going back during tutorial
        // User must tap to continue
    }
}
