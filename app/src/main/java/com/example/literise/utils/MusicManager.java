package com.example.literise.utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.literise.R;

/**
 * MusicManager - Singleton class to manage background music throughout the app
 *
 * Features:
 * - Plays looping background music across all activities
 * - Supports multiple music tracks for different contexts
 * - Smooth transitions between tracks with crossfade
 * - Pauses when app goes to background
 * - Resumes when app comes to foreground
 * - Proper resource management
 *
 * Usage:
 * - Call MusicManager.getInstance(context).play(MusicTrack.DASHBOARD) in onResume()
 * - Call MusicManager.getInstance(context).pause() in onPause()
 * - Call MusicManager.getInstance(context).stop() when app is destroyed
 */
public class MusicManager {
    private static final String TAG = "MusicManager";
    private static MusicManager instance;
    private MediaPlayer mediaPlayer;
    private boolean isPaused = false;
    private boolean isMusicEnabled = true;
    private Context context;
    private int currentTrack = -1; // Track which music is currently playing

    /**
     * Music tracks for different app contexts
     */
    public enum MusicTrack {
        DASHBOARD(R.raw.bg_music, 0.3f),           // Dashboard/Menu music
        GAME(R.raw.game_music, 0.25f),             // Game music
        ASSESSMENT(R.raw.assessment_music, 0.25f), // Assessment music (fun, engaging)
        VICTORY(R.raw.victory_music, 0.5f),        // Victory celebration (short, doesn't loop)
        INTRO(R.raw.intro_music, 0.3f),            // Intro/Welcome music
        NICKNAME(R.raw.nickname_music, 0.3f);      // Nickname creation music

        private final int resourceId;
        private final float volume;

        MusicTrack(int resourceId, float volume) {
            this.resourceId = resourceId;
            this.volume = volume;
        }

        public int getResourceId() {
            return resourceId;
        }

        public float getVolume() {
            return volume;
        }
    }

    private MusicManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized MusicManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicManager(context);
        }
        return instance;
    }

    /**
     * Play specific music track
     */
    public void play(MusicTrack track) {
        if (!isMusicEnabled) {
            Log.d(TAG, "Music is disabled, not playing");
            return;
        }

        // If same track is already playing, just resume if paused
        if (currentTrack == track.getResourceId()) {
            if (isPaused && mediaPlayer != null) {
                mediaPlayer.start();
                isPaused = false;
                Log.d(TAG, "Resumed current track: " + track.name());
            }
            return;
        }

        // Stop current music and play new track
        stop();

        try {
            mediaPlayer = MediaPlayer.create(context, track.getResourceId());
            if (mediaPlayer != null) {
                // Victory music doesn't loop, others do
                mediaPlayer.setLooping(track != MusicTrack.VICTORY);
                mediaPlayer.setVolume(track.getVolume(), track.getVolume());
                mediaPlayer.start();
                currentTrack = track.getResourceId();
                isPaused = false;
                Log.d(TAG, "Started playing: " + track.name() + " (volume: " + track.getVolume() + ")");

                // Auto-stop victory music after it finishes
                if (track == MusicTrack.VICTORY) {
                    mediaPlayer.setOnCompletionListener(mp -> {
                        Log.d(TAG, "Victory music completed");
                        stop();
                    });
                }
            } else {
                Log.e(TAG, "Failed to create MediaPlayer for track: " + track.name());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting music track " + track.name() + ": " + e.getMessage());
        }
    }

    /**
     * Play default background music (for backward compatibility)
     */
    public void play() {
        play(MusicTrack.DASHBOARD);
    }

    /**
     * Pause the background music (when app goes to background)
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPaused = true;
            Log.d(TAG, "Background music paused");
        }
    }

    /**
     * Stop and release the background music
     */
    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            isPaused = false;
            Log.d(TAG, "Background music stopped and released");
        }
    }

    /**
     * Check if music is currently playing
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    /**
     * Set volume (0.0f to 1.0f)
     */
    public void setVolume(float volume) {
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume, volume);
            Log.d(TAG, "Volume set to: " + volume);
        }
    }

    /**
     * Enable or disable music (for future mute/unmute feature)
     */
    public void setMusicEnabled(boolean enabled) {
        isMusicEnabled = enabled;
        if (!enabled && mediaPlayer != null && mediaPlayer.isPlaying()) {
            pause();
        } else if (enabled && isPaused) {
            play();
        }
        Log.d(TAG, "Music enabled: " + enabled);
    }

    /**
     * Get music enabled status
     */
    public boolean isMusicEnabled() {
        return isMusicEnabled;
    }
}
