package com.example.literise.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for playing background music and sound effects
 * Makes the placement test more engaging and fun for kids
 */
public class SoundEffectsHelper {

    private static final String TAG = "SoundEffectsHelper";
    private static final String PREFS_NAME = "SoundPrefs";
    private static final String KEY_SOUND_ENABLED = "sound_enabled";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";

    private Context context;
    private SoundPool soundPool;
    private Map<String, Integer> soundMap;
    private MediaPlayer backgroundMusicPlayer;
    private SharedPreferences prefs;

    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private float soundVolume = 0.7f;
    private float musicVolume = 0.3f;

    // Sound effect types
    public static final String SOUND_SUCCESS = "success";
    public static final String SOUND_ERROR = "error";
    public static final String SOUND_CLICK = "click";
    public static final String SOUND_CELEBRATION = "celebration";
    public static final String SOUND_CHIME = "chime";
    public static final String SOUND_POP = "pop";
    public static final String SOUND_TRANSITION = "transition";

    public SoundEffectsHelper(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true);
        this.musicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
        initializeSoundPool();
        loadSounds();
    }

    private void initializeSoundPool() {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build();

        soundMap = new HashMap<>();
    }

    /**
     * Load sound effects
     * NOTE: For a production app, you would load actual sound files from res/raw/
     * For now, we'll create placeholder entries
     */
    private void loadSounds() {
        // In a real implementation, you would load sound files like this:
        // soundMap.put(SOUND_SUCCESS, soundPool.load(context, R.raw.success, 1));
        // soundMap.put(SOUND_ERROR, soundPool.load(context, R.raw.error, 1));
        // etc.

        // For now, we'll just log that sounds would be loaded
        Log.d(TAG, "Sound effects initialized (add sound files to res/raw/ for actual sounds)");
    }

    /**
     * Play a sound effect
     */
    public void playSound(String soundType) {
        if (!soundEnabled) {
            return;
        }

        Integer soundId = soundMap.get(soundType);
        if (soundId != null) {
            soundPool.play(soundId, soundVolume, soundVolume, 1, 0, 1.0f);
        } else {
            Log.d(TAG, "Playing sound effect: " + soundType + " (placeholder - add actual sound file)");
        }
    }

    /**
     * Play success sound (correct answer)
     */
    public void playSuccess() {
        playSound(SOUND_SUCCESS);
    }

    /**
     * Play error sound (wrong answer)
     */
    public void playError() {
        playSound(SOUND_ERROR);
    }

    /**
     * Play click/tap sound
     */
    public void playClick() {
        playSound(SOUND_CLICK);
    }

    /**
     * Play celebration sound (level complete, milestone)
     */
    public void playCelebration() {
        playSound(SOUND_CELEBRATION);
    }

    /**
     * Play chime sound (question complete)
     */
    public void playChime() {
        playSound(SOUND_CHIME);
    }

    /**
     * Play pop sound (UI interaction)
     */
    public void playPop() {
        playSound(SOUND_POP);
    }

    /**
     * Play transition sound (moving between categories)
     */
    public void playTransition() {
        playSound(SOUND_TRANSITION);
    }

    /**
     * Start background music
     * NOTE: For production, load actual music file from res/raw/
     */
    public void startBackgroundMusic() {
        if (!musicEnabled || backgroundMusicPlayer != null) {
            return;
        }

        try {
            // In a real implementation:
            // backgroundMusicPlayer = MediaPlayer.create(context, R.raw.background_music);
            // backgroundMusicPlayer.setLooping(true);
            // backgroundMusicPlayer.setVolume(musicVolume, musicVolume);
            // backgroundMusicPlayer.start();

            Log.d(TAG, "Background music started (placeholder - add music file to res/raw/)");
        } catch (Exception e) {
            Log.e(TAG, "Error starting background music", e);
        }
    }

    /**
     * Stop background music
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            backgroundMusicPlayer.release();
            backgroundMusicPlayer = null;
        }
    }

    /**
     * Pause background music
     */
    public void pauseBackgroundMusic() {
        if (backgroundMusicPlayer != null && backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.pause();
        }
    }

    /**
     * Resume background music
     */
    public void resumeBackgroundMusic() {
        if (backgroundMusicPlayer != null && !backgroundMusicPlayer.isPlaying()) {
            backgroundMusicPlayer.start();
        }
    }

    /**
     * Enable/disable sound effects
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply();
    }

    /**
     * Enable/disable background music
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply();

        if (!enabled) {
            stopBackgroundMusic();
        } else {
            startBackgroundMusic();
        }
    }

    /**
     * Set sound effects volume (0.0 to 1.0)
     */
    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    /**
     * Set music volume (0.0 to 1.0)
     */
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0.0f, Math.min(1.0f, volume));
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(musicVolume, musicVolume);
        }
    }

    /**
     * Check if sound effects are enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Check if background music is enabled
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    /**
     * Cleanup resources
     */
    public void release() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        stopBackgroundMusic();
        soundMap.clear();
    }
}