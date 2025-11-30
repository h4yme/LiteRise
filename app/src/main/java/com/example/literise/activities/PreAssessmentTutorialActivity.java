package com.example.literise.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;

public class PreAssessmentTutorialActivity extends AppCompatActivity {

    private View rootLayout;
    private MediaPlayer soundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_assessment_tutorial);

        // Initialize views
        rootLayout = findViewById(R.id.rootLayout);

        // Set click listener on entire layout
        rootLayout.setOnClickListener(v -> {
            playClickSound();
            goToPlacementTest();
        });

        // Fade in animation on start
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(300);
        rootLayout.startAnimation(fadeIn);
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
