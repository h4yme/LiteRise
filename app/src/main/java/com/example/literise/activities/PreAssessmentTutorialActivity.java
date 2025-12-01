package com.example.literise.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;

public class PreAssessmentTutorialActivity extends AppCompatActivity {

    private View rootLayout;
    private View overlayDark;
    private View tutorialContentLayout;
    private TextView tvTutorialTitle;
    private TextView tvTutorialMessage;
    private TextView tvTapToContinue;

    // Mock UI elements
    private CardView cardMockPassage;
    private CardView cardMockPronunciation;
    private CardView cardMockQuestion;
    private View btnMockContinue;
    private Button btnMockOptionA;
    private Button btnMockOptionB;
    private Button btnMockOptionC;
    private Button btnMockOptionD;
    private CardView cardMockMicButton;
    private TextView tvMockPassageLabel;
    private TextView tvMockPassageContent;

    private MediaPlayer soundPlayer;
    private int currentStep = 0; // 0 to 5 (6 tutorial steps)
    private Handler hintHandler = new Handler(Looper.getMainLooper());
    private Runnable hintRunnable;
    private int hintLevel = 0; // Track hint progression

    // Tutorial content for each step
    private final String[] tutorialTitles = {
            "Welcome to Placement Test! 🦁",
            "Step 1: Read Carefully 📖",
            "Step 2: Tap the Passage! 👆",
            "Step 3: Choose Your Answer ✓",
            "Step 4: Pronunciation Test 🎤",
            "Step 5: Click Continue ▶"
    };

    private final String[] tutorialMessages = {
            "Hi! I'm Leo! 🦁\n\nLet's practice together! I'll guide you every step of the way!",

            "Great! Some questions will show a passage or scrambled words.\n\nLet me show you! ✨",

            "Perfect! Now try tapping on the card with scrambled words!\n\nGo ahead, tap it! 👆",

            "Awesome! 🌟 Now choose the correct sentence from the options!\n\nTry tapping option 'a' to practice!",

            "Fantastic! 🎉 For pronunciation questions, tap the green microphone button!\n\nGive it a try!",

            "You're doing amazing! 🌟 After answering, always tap Continue to move forward!\n\nTap it now!"
    };

    private final String[][] encouragementHints = {
            // Step 0 - Welcome
            {"Tap anywhere when you're ready!", "I'm here to help you! Tap to begin!", "Let's get started! Just tap!"},

            // Step 1 - Read Carefully
            {"Reading passages helps you answer better!", "Take your time to understand!", "You're doing great!"},

            // Step 2 - Tap the Passage
            {"Try tapping the white card with scrambled words!", "The card with words is waiting for your tap! 👆", "You can do it! Tap the card above!"},

            // Step 3 - Choose Answer
            {"Tap option 'a' to practice!", "Just tap the first option!", "Give it a try - tap 'The cat is black'! 💪"},

            // Step 4 - Pronunciation
            {"Tap the green microphone button! 🎤", "The mic button is ready for you!", "Go ahead, tap that green circle!"},

            // Step 5 - Continue
            {"Tap the Continue button below!", "Almost done! Tap Continue! 🎉", "You're so close! Tap Continue!"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_assessment_tutorial);

        // Initialize views
        rootLayout = findViewById(R.id.rootLayout);
        overlayDark = findViewById(R.id.overlayDark);
        tutorialContentLayout = findViewById(R.id.tutorialContentLayout);
        tvTutorialTitle = findViewById(R.id.tvTutorialTitle);
        tvTutorialMessage = findViewById(R.id.tvTutorialMessage);
        tvTapToContinue = findViewById(R.id.tvTapToContinue);

        // Mock UI elements
        cardMockPassage = findViewById(R.id.cardMockPassage);
        cardMockPronunciation = findViewById(R.id.cardMockPronunciation);
        cardMockQuestion = findViewById(R.id.cardMockQuestion);
        btnMockContinue = findViewById(R.id.btnMockContinue);
        btnMockOptionA = findViewById(R.id.btnMockOptionA);
        btnMockOptionB = findViewById(R.id.btnMockOptionB);
        btnMockOptionC = findViewById(R.id.btnMockOptionC);
        btnMockOptionD = findViewById(R.id.btnMockOptionD);
        cardMockMicButton = findViewById(R.id.cardMockMicButton);
        tvMockPassageLabel = findViewById(R.id.tvMockPassageLabel);
        tvMockPassageContent = findViewById(R.id.tvMockPassageContent);

        // Set up interactive click listeners for each step
        setupInteractiveListeners();

        // Only Step 0 (welcome) allows tap anywhere
        View.OnClickListener welcomeClickListener = v -> {
            if (currentStep == 0) {
                playClickSound();
                celebrateInteraction("Great! Let's go!");
                nextStep();
            }
        };

        rootLayout.setOnClickListener(welcomeClickListener);
        overlayDark.setOnClickListener(welcomeClickListener);
        tutorialContentLayout.setOnClickListener(welcomeClickListener);

        // Show first step
        updateTutorialStep();

        // Fade in animation on start
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(300);
        rootLayout.startAnimation(fadeIn);
    }

    /**
     * Set up interactive click listeners for mock UI elements
     */
    private void setupInteractiveListeners() {
        // Passage card interaction
        cardMockPassage.setOnClickListener(v -> {
            if (currentStep == 2) {
                playClickSound();
                celebrateInteraction("Perfect! You read the passage! 📖");
                nextStep();
            }
        });

        // Answer options interaction
        View.OnClickListener optionClickListener = v -> {
            if (currentStep == 3) {
                playClickSound();
                celebrateInteraction("Excellent choice! 🌟");
                nextStep();
            }
        };

        btnMockOptionA.setOnClickListener(optionClickListener);
        btnMockOptionB.setOnClickListener(optionClickListener);
        btnMockOptionC.setOnClickListener(optionClickListener);
        btnMockOptionD.setOnClickListener(optionClickListener);

        // Microphone button interaction
        cardMockMicButton.setOnClickListener(v -> {
            if (currentStep == 4) {
                playClickSound();
                celebrateInteraction("Great pronunciation! 🎤");
                nextStep();
            }
        });

        // Continue button interaction
        btnMockContinue.setOnClickListener(v -> {
            if (currentStep == 5) {
                playClickSound();
                celebrateInteraction("You're ready! 🚀");
                nextStep();
            }
        });
    }

    /**
     * Update tutorial content and highlights based on current step
     */
    private void updateTutorialStep() {
        // Update text content
        tvTutorialTitle.setText(tutorialTitles[currentStep]);
        tvTutorialMessage.setText(tutorialMessages[currentStep]);

        // Reset hint system
        cancelHints();
        hintLevel = 0;

        // Reset all highlights
        resetHighlights();

        // Update "Tap to Continue" text based on step
        if (currentStep == 0) {
            tvTapToContinue.setText("Tap to Continue");
            // Enable overlay clicks for welcome step

            overlayDark.setClickable(true);

            tutorialContentLayout.setClickable(true);

        } else if (currentStep == 1) {

            tvTapToContinue.setText("Watch and Learn");

            // Disable overlay clicks to allow mock UI interaction

            overlayDark.setClickable(false);

            tutorialContentLayout.setClickable(false);

        } else {

            tvTapToContinue.setText("Try It Yourself!");

            // Disable overlay clicks to allow mock UI interaction

            overlayDark.setClickable(false);

            tutorialContentLayout.setClickable(false);
        }

        // Apply step-specific highlights and visibility
        switch (currentStep) {
            case 0: // Welcome
                // All mock UI visible but dimmed
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                startProgressiveHints();
                break;

            case 1: // Show passage (non-interactive)
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                tvMockPassageLabel.setText("Read the passage:");
                tvMockPassageContent.setText("The quick brown fox jumps over the lazy dog. This sentence contains every letter of the alphabet!");
                highlightView(cardMockPassage);
                // Auto-advance after 2.5 seconds
                hintHandler.postDelayed(() -> {
                    celebrateInteraction("Let's try it!");
                    nextStep();
                }, 2500);
                break;

            case 2: // Interactive - Tap passage
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                tvMockPassageLabel.setText("Use these words:");
                tvMockPassageContent.setText("the | is | cat | black");
                highlightView(cardMockPassage);
                startProgressiveHints();
                break;

            case 3: // Interactive - Choose answer
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                tvMockPassageLabel.setText("Use these words:");
                tvMockPassageContent.setText("the | is | cat | black");
                highlightView(cardMockQuestion);
                startProgressiveHints();
                break;

            case 4: // Interactive - Tap microphone
                cardMockPassage.setVisibility(View.GONE);
                cardMockPronunciation.setVisibility(View.VISIBLE);
                cardMockQuestion.setVisibility(View.GONE);
                btnMockContinue.setVisibility(View.VISIBLE);
                highlightView(cardMockMicButton);
                startProgressiveHints();
                break;

            case 5: // Interactive - Tap continue
                cardMockPassage.setVisibility(View.VISIBLE);
                cardMockPronunciation.setVisibility(View.GONE);
                cardMockQuestion.setVisibility(View.VISIBLE);
                btnMockContinue.setVisibility(View.VISIBLE);
                tvMockPassageLabel.setText("Use these words:");
                tvMockPassageContent.setText("the | is | cat | black");
                highlightView(btnMockContinue);
                startProgressiveHints();
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
        cardMockMicButton.setAlpha(0.5f);
    }

    /**
     * Highlight a specific view (make it stand out)
     */
    private void highlightView(View view) {
        view.setAlpha(1.0f);
        view.setElevation(16f);

        // Pulse animation with bounce
        ScaleAnimation bounce = new ScaleAnimation(
                1.0f, 1.05f,  // X scale
                1.0f, 1.05f,  // Y scale
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        bounce.setDuration(600);
        bounce.setRepeatMode(Animation.REVERSE);
        bounce.setRepeatCount(Animation.INFINITE);
        view.startAnimation(bounce);

        // Also add alpha pulse
        AlphaAnimation pulse = new AlphaAnimation(0.9f, 1.0f);
        pulse.setDuration(600);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(Animation.INFINITE);
        view.startAnimation(pulse);
    }

    /**
     * Celebrate when user interacts correctly
     */
    private void celebrateInteraction(String message) {
        cancelHints();

        // Update message with celebration
        tvTutorialMessage.setText(message);

        // Scale animation for the speech bubble
        ScaleAnimation celebrate = new ScaleAnimation(
                1.0f, 1.1f,
                1.0f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        celebrate.setDuration(200);
        celebrate.setRepeatMode(Animation.REVERSE);
        celebrate.setRepeatCount(1);
        tutorialContentLayout.startAnimation(celebrate);

        // Play success sound
        playSuccessSound();
    }

    /**
     * Start progressive hints if user doesn't interact
     */
    private void startProgressiveHints() {
        hintRunnable = new Runnable() {
            @Override
            public void run() {
                if (hintLevel < encouragementHints[currentStep].length) {
                    // Update message with hint
                    String hint = encouragementHints[currentStep][hintLevel];
                    tvTutorialMessage.setText(tutorialMessages[currentStep] + "\n\n💡 " + hint);

                    // Gentle shake animation on the highlighted element
                    View highlightedView = getHighlightedViewForCurrentStep();
                    if (highlightedView != null) {
                        shakeView(highlightedView);
                    }

                    hintLevel++;
                    hintHandler.postDelayed(this, 3000); // Next hint in 3 seconds
                }
            }
        };
        hintHandler.postDelayed(hintRunnable, 3000); // First hint after 3 seconds
    }

    /**
     * Cancel all pending hints
     */
    private void cancelHints() {
        if (hintRunnable != null) {
            hintHandler.removeCallbacks(hintRunnable);
        }
    }

    /**
     * Get the view that should be highlighted for the current step
     */
    private View getHighlightedViewForCurrentStep() {
        switch (currentStep) {
            case 2: return cardMockPassage;
            case 3: return cardMockQuestion;
            case 4: return cardMockMicButton;
            case 5: return btnMockContinue;
            default: return null;
        }
    }

    /**
     * Shake animation for hints
     */
    private void shakeView(View view) {
        view.animate()
                .translationX(-10f)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(10f)
                        .setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(-10f)
                                .setDuration(50)
                                .withEndAction(() -> view.animate()
                                        .translationX(0f)
                                        .setDuration(50)
                                        .start())
                                .start())
                        .start())
                .start();
    }

    /**
     * Move to next tutorial step
     */
    private void nextStep() {
        cancelHints();

        if (currentStep < tutorialTitles.length - 1) {
            currentStep++;

            // Fade transition
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.7f);
            fadeOut.setDuration(200);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {
                    updateTutorialStep();
                    AlphaAnimation fadeIn = new AlphaAnimation(0.7f, 1.0f);
                    fadeIn.setDuration(200);
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
            soundPlayer = MediaPlayer.create(this, R.raw.sound_button_click);
            soundPlayer.setOnCompletionListener(mp -> mp.release());
            soundPlayer.start();
        } catch (Exception e) {
            // Silently fail if sound not found
        }
    }

    /**
     * Play success sound effect for correct interactions
     */
    private void playSuccessSound() {
        try {
            MediaPlayer successPlayer = MediaPlayer.create(this, R.raw.sound_success);
            if (successPlayer != null) {
                successPlayer.setOnCompletionListener(mp -> mp.release());
                successPlayer.start();
            }
        } catch (Exception e) {
            // Silently fail if sound not found, use click sound as fallback
            playClickSound();
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
