package com.example.literise.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.database.SessionManager;

public class WelcomeActivity extends AppCompatActivity {

    private ImageView ivWelcomeScreen;
    private TextView tvSkip;
    private TextView tvTapToContinue;
    private View rootLayout;
    
    private int currentScreen = 0; // 0 to 4 (5 screens)
    private final int[] welcomeImages = {
        R.drawable.welcome_slide_1,
        R.drawable.welcome_slide_2,
        R.drawable.welcome_slide_3,
        R.drawable.welcome_slide_4,
        R.drawable.welcome_slide_5
    };
    
    private MediaPlayer soundPlayer;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Initialize views
        ivWelcomeScreen = findViewById(R.id.ivWelcomeScreen);
        tvSkip = findViewById(R.id.tvSkip);
        tvTapToContinue = findViewById(R.id.tvTapToContinue);
        rootLayout = findViewById(R.id.rootLayout);

        // Fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootLayout.startAnimation(fadeIn);

        // Tap anywhere to continue
        rootLayout.setOnClickListener(v -> {
            playClickSound();
            nextScreen();
        });

        // Skip button click
        tvSkip.setOnClickListener(v -> {
            playClickSound();
            goToNicknameSetup();
        });
    }

    private void nextScreen() {
        if (currentScreen < 4) {
            // Move to next screen
            currentScreen++;
            
            // Fade transition animation
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.7f);
            fadeOut.setDuration(150);
            fadeOut.setFillAfter(true);
            
            ivWelcomeScreen.startAnimation(fadeOut);
            
            // Change image
            ivWelcomeScreen.postDelayed(() -> {
                ivWelcomeScreen.setImageResource(welcomeImages[currentScreen]);
                
                AlphaAnimation fadeInScreen = new AlphaAnimation(0.7f, 1.0f);
                fadeInScreen.setDuration(150);
                ivWelcomeScreen.startAnimation(fadeInScreen);
                
                // Hide "Tap to continue" on last screen
                if (currentScreen == 4) {
                    tvTapToContinue.setText("Tap to start");
                }
            }, 150);
            
        } else {
            // Last screen - proceed to nickname setup
            goToNicknameSetup();
        }
    }

    private void playClickSound() {
        try {
            if (soundPlayer != null) {
                soundPlayer.release();
            }
            soundPlayer = MediaPlayer.create(this, R.raw.sound_button_click);
            soundPlayer.setOnCompletionListener(MediaPlayer::release);
            soundPlayer.start();
        } catch (Exception e) {
            // Sound file might not exist yet - ignore
        }
    }

    private void goToNicknameSetup() {
        // Mark welcome screens as seen
        sessionManager.setHasSeenWelcome(true);
        
        Intent intent = new Intent(WelcomeActivity.this, NicknameSetupActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPlayer != null) {
            soundPlayer.release();
            soundPlayer = null;
        }
    }
}
