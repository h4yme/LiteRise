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
import com.example.literise.utils.MusicManager;

public class WelcomeTutorialActivity extends AppCompatActivity {

    private ImageView ivTutorialScreen;
    private ImageView ivLeoCharacter;
    private TextView tvTutorialTitle;
    private TextView tvTutorialDescription;
    private TextView btnSkip;
    private TextView tvTapToContinue;
    private View rootLayout;

    private int currentScreen = 0; // 0 to 3 (4 screens)

    private final int[] tutorialBackgrounds = {
            R.drawable.tutorial_slide_1,
            R.drawable.tutorial_slide_2,
            R.drawable.tutorial_slide_3,
            R.drawable.tutorial_slide_4
    };

    private final int[] leoImages = {
            R.drawable.leo_welcome_1,
            R.drawable.leo_welcome_2,
            R.drawable.leo_welcome_3,
            R.drawable.leo_welcome_4
    };

    private final String[] tutorialTitles = {
            "Welcome to LiteRise!",
            "Learn and Play!",
            "Practice Reading!",
            "Ready to Start?"
    };

    private final String[] tutorialDescriptions = {
            "I'm Leo, your reading buddy!\nI'll help you learn to read in a fun way!",
            "We'll play fun games and\nearn exciting rewards as you learn!",
            "You'll practice reading out loud\nand I'll help you improve!",
            "Let's find your reading level first\nso I can give you the perfect lessons!"
    };

    private MediaPlayer soundPlayer;
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_tutorial);

        musicManager = MusicManager.getInstance(this);

        // Initialize views
        ivTutorialScreen = findViewById(R.id.ivTutorialScreen);
        ivLeoCharacter = findViewById(R.id.ivLeoCharacter);
        tvTutorialTitle = findViewById(R.id.tvTutorialTitle);
        tvTutorialDescription = findViewById(R.id.tvTutorialDescription);
        btnSkip = findViewById(R.id.btnSkip);
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
        btnSkip.setOnClickListener(v -> {
            playClickSound();
            navigateToPlacementIntro();
        });
    }

    private void nextScreen() {
        if (currentScreen < 3) {
            // Move to next screen
            currentScreen++;

            // Fade transition animation
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.7f);
            fadeOut.setDuration(150);
            fadeOut.setFillAfter(true);

            rootLayout.startAnimation(fadeOut);

            // Change content
            rootLayout.postDelayed(() -> {
                // Update background
                ivTutorialScreen.setImageResource(tutorialBackgrounds[currentScreen]);

                // Update Leo character
                ivLeoCharacter.setImageResource(leoImages[currentScreen]);

                // Update title and description
                tvTutorialTitle.setText(tutorialTitles[currentScreen]);
                tvTutorialDescription.setText(tutorialDescriptions[currentScreen]);

                // Fade in animation
                AlphaAnimation fadeInScreen = new AlphaAnimation(0.7f, 1.0f);
                fadeInScreen.setDuration(150);
                rootLayout.startAnimation(fadeInScreen);

                // Change text on last screen
                if (currentScreen == 3) {
                    tvTapToContinue.setText("Tap to start");
                }
            }, 150);

        } else {
            // Last screen - proceed to placement test
            navigateToPlacementIntro();
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
            // Sound file might not exist - ignore
        }
    }

    private void navigateToPlacementIntro() {
        Intent intent = new Intent(this, PlacementIntroActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Play intro music when activity becomes visible
        musicManager.playMusic(MusicManager.MusicType.INTRO);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause music when activity goes to background
        musicManager.pause();
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
