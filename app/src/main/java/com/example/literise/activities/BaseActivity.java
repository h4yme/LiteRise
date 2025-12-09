package com.example.literise.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.literise.utils.MusicManager;

/**
 * BaseActivity - Base class for activities that should have background music
 *
 * Activities that extend this class will automatically:
 * - Play background music when activity starts/resumes
 * - Pause background music when activity pauses
 * - Handle music across activity transitions seamlessly
 * - Support different music tracks for different contexts
 *
 * Override getMusicTrack() to specify which music to play
 * Override shouldPlayMusic() to disable music for specific activities
 *
 * Activities that should NOT have music (e.g., Login):
 * - Should extend AppCompatActivity directly instead of BaseActivity
 */
public abstract class BaseActivity extends AppCompatActivity {

    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        musicManager = MusicManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start/resume background music when activity becomes visible
        if (shouldPlayMusic()) {
            musicManager.play(getMusicTrack());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause background music when activity goes to background
        if (shouldPlayMusic()) {
            musicManager.pause();
        }
    }

    /**
     * Override this method to specify which music track to play
     * Default is DASHBOARD music (menu/navigation music)
     *
     * @return MusicTrack to play for this activity
     */
    protected MusicManager.MusicTrack getMusicTrack() {
        return MusicManager.MusicTrack.DASHBOARD;
    }

    /**
     * Override this method if you want to conditionally disable music
     * for specific activities that extend BaseActivity
     *
     * @return true if music should play (default), false to disable
     */
    protected boolean shouldPlayMusic() {
        return true;
    }
}
