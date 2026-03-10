package com.example.literise.activities;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.literise.R;
import com.google.android.material.button.MaterialButton;

public class PlacementIntroActivity extends AppCompatActivity {

    private static final int STEP_COUNT = 4;

    private ImageView ivLeo;
    private TextView tvStepTitle;
    private TextView tvStepDescription;
    private MaterialButton btnNext;
    private TextView tvSkip;
    private View[] dots;

    private ObjectAnimator floatAnimator;
    private MediaPlayer voicePlayer;
    private int currentStep = 0;

    private static final String[] TITLES = {
            "Hi! I'm Leo! \uD83D\uDC4B",
            "A Quick Reading Check \uD83D\uDCDA",
            "What to Expect \uD83C\uDFAF",
            "You're All Set! \uD83C\uDF1F"
    };

    private static final String[] DESCRIPTIONS = {
            "I'm your reading buddy here at LiteRise! Together we'll go on an amazing English adventure built just for you.",
            "I'll ask you 25 fun questions to find your perfect reading level. Don't worry — it's not a real test! Think of it as a reading adventure.",
            "We'll explore phonics, vocabulary, grammar, reading, and writing! Questions adjust as we go — I'll make sure it's just right for you.",
            "Take your time, trust yourself, and have fun! I'll be right here cheering you on every single step of the way. Ready to rise?"
    };

    private static final int[] LEO_EXPRESSIONS = {
            R.drawable.leo_wave,      // Step 1
            R.drawable.leo_thinking,  // Step 2
            R.drawable.leo_explain,   // Step 3
            R.drawable.leo_cheer      // Step 4
    };

    // One MP3 per step — place all 4 in res/raw/
    private static final int[] STEP_VOICEOVERS = {
            R.raw.step1,      // Step 1 ✅ already have
            R.raw.step2,  // Step 2 ✅ already have
            R.raw.step3,       // Step 3 — add your mp3
            R.raw.step4         // Step 4 — add your mp3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip intro for post-assessment — go straight to the test
        String assessmentType = getIntent().getStringExtra("assessment_type");
        if ("POST".equals(assessmentType)) {
            startPlacementTest();
            return;
        }

        setContentView(R.layout.activity_placement_intro);

        ivLeo             = findViewById(R.id.ivLeo);
        tvStepTitle       = findViewById(R.id.tvStepTitle);
        tvStepDescription = findViewById(R.id.tvStepDescription);
        btnNext           = findViewById(R.id.btnNext);
        tvSkip            = findViewById(R.id.tvSkip);

        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4)
        };

        startLeoFloat();
        applyStep(0);

        btnNext.setOnClickListener(v -> {
            if (currentStep < STEP_COUNT - 1) {
                currentStep++;
                animateToNextStep();
            } else {
                startPlacementTest();
            }
        });

        tvSkip.setOnClickListener(v -> startPlacementTest());
    }

    // ─── Voiceover ────────────────────────────────────────────────────────────

    private void playStepVoiceover(int step) {
        stopVoiceover();
        try {
            voicePlayer = MediaPlayer.create(this, STEP_VOICEOVERS[step]);
            if (voicePlayer != null) {
                voicePlayer.setOnCompletionListener(mp -> stopVoiceover());
                voicePlayer.start();
            }
        } catch (Exception e) {
            // Missing file or error — silently skip so UI still works
        }
    }

    private void stopVoiceover() {
        if (voicePlayer != null) {
            if (voicePlayer.isPlaying()) voicePlayer.stop();
            voicePlayer.release();
            voicePlayer = null;
        }
    }

    // ─── Leo floating animation ───────────────────────────────────────────────

    private void startLeoFloat() {
        float density = getResources().getDisplayMetrics().density;
        floatAnimator = ObjectAnimator.ofFloat(ivLeo, "translationY",
                -12 * density, 12 * density);
        floatAnimator.setDuration(1800);
        floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimator.setRepeatMode(ValueAnimator.REVERSE);
        floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnimator.start();
    }

    // ─── Step transitions ─────────────────────────────────────────────────────

    private void applyStep(int step) {
        ivLeo.setImageResource(LEO_EXPRESSIONS[step]);
        tvStepTitle.setText(TITLES[step]);
        tvStepDescription.setText(DESCRIPTIONS[step]);
        btnNext.setText(step == STEP_COUNT - 1 ? "Start Test! \uD83D\uDE80" : "Next \u2192");
        updateDots(step);
        playStepVoiceover(step);  // ← plays voiceover for this step
    }

    private void animateToNextStep() {
        tvStepTitle.animate().alpha(0f).setDuration(140).withEndAction(() -> {
            tvStepTitle.setText(TITLES[currentStep]);
            tvStepTitle.animate().alpha(1f).setDuration(200).start();
        }).start();

        tvStepDescription.animate().alpha(0f).setDuration(140).withEndAction(() -> {
            tvStepDescription.setText(DESCRIPTIONS[currentStep]);
            tvStepDescription.animate().alpha(1f).setDuration(200).start();
        }).start();

        ivLeo.animate()
                .scaleX(0.88f).scaleY(0.88f)
                .setDuration(130)
                .withEndAction(() -> {
                    ivLeo.setImageResource(LEO_EXPRESSIONS[currentStep]);
                    ivLeo.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(220)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                }).start();

        btnNext.setText(currentStep == STEP_COUNT - 1 ? "Start Test! \uD83D\uDE80" : "Next \u2192");
        updateDots(currentStep);
        playStepVoiceover(currentStep);  // ← stops old, plays new
    }

    // ─── Dot indicators ───────────────────────────────────────────────────────

    private void updateDots(int activeStep) {
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < dots.length; i++) {
            boolean isActive = (i == activeStep);
            int targetPx = (int) ((isActive ? 32 : 10) * density);
            dots[i].setBackgroundResource(isActive
                    ? R.drawable.indicator_dot_active
                    : R.drawable.indicator_dot_placement_inactive);

            final View dot = dots[i];
            ValueAnimator anim = ValueAnimator.ofInt(dot.getLayoutParams().width, targetPx);
            anim.setDuration(250);
            anim.addUpdateListener(va -> {
                ViewGroup.LayoutParams params = dot.getLayoutParams();
                params.width = (int) va.getAnimatedValue();
                dot.setLayoutParams(params);
            });
            anim.start();
        }
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    private void startPlacementTest() {
        stopVoiceover();
        Intent intent = new Intent(PlacementIntroActivity.this, PlacementTestActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    protected void onPause() {
        super.onPause();
        if (voicePlayer != null && voicePlayer.isPlaying()) voicePlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (voicePlayer != null && !voicePlayer.isPlaying()) voicePlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (floatAnimator != null) floatAnimator.cancel();
        stopVoiceover();
    }

    @Override
    public void onBackPressed() {
        // Block back — user must complete or skip walkthrough
    }
}
