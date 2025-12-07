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

 * - Pauses when app goes to background

 * - Resumes when app comes to foreground

 * - Proper resource management

 *

 * Usage:

 * - Call MusicManager.getInstance(context).play() in onResume()

 * - Call MusicManager.getInstance(context).pause() in onPause()

 * - Call MusicManager.getInstance(context).stop() when app is destroyed

 */

public class MusicManager {

    private static final String TAG = "MusicManager";

    private static MusicManager instance;

    private MediaPlayer mediaPlayer;

    private boolean isPaused = false;

    private boolean isMusicEnabled = true; // For future mute/unmute feature

    private Context context;



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

     * Initialize and start playing background music

     */

    public void play() {

        if (!isMusicEnabled) {

            Log.d(TAG, "Music is disabled, not playing");

            return;

        }



        if (mediaPlayer == null) {

            try {

                mediaPlayer = MediaPlayer.create(context, R.raw.bg_music);

                if (mediaPlayer != null) {

                    mediaPlayer.setLooping(true);

                    mediaPlayer.setVolume(0.3f, 0.3f); // 30% volume - not too loud

                    mediaPlayer.start();

                    isPaused = false;

                    Log.d(TAG, "Background music started");

                } else {

                    Log.e(TAG, "Failed to create MediaPlayer - check if bg_music.mp3 exists in res/raw/");

                }

            } catch (Exception e) {

                Log.e(TAG, "Error starting background music: " + e.getMessage());

            }

        } else if (isPaused) {

            mediaPlayer.start();

            isPaused = false;

            Log.d(TAG, "Background music resumed");

        }

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