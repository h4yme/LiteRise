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

 * - Supports different music tracks for different contexts



 * - Pauses when app goes to background



 * - Resumes when app comes to foreground



 * - Proper resource management



 *



 * Usage:



 * - Call MusicManager.getInstance(context).playMusic(MusicType) in onResume()



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



    private MusicType currentMusicType = null;







    /**



     * Enum for different music contexts in the app



     */



    public enum MusicType {



        INTRO(R.raw.intro_music),           // Welcome/Intro screens



        NICKNAME(R.raw.nickname_music),     // Nickname setup screen



        ASSESSMENT(R.raw.assessment_music), // Pre-assessment/test



        VICTORY(R.raw.victory_music),       // Results/completion



        GAME(R.raw.game_music),             // Game activities



        DASHBOARD(R.raw.bg_music);          // Dashboard/main menu







        private final int resourceId;







        MusicType(int resourceId) {



            this.resourceId = resourceId;



        }







        public int getResourceId() {



            return resourceId;



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







    public void playMusic(MusicType musicType) {



        if (!isMusicEnabled) {



            Log.d(TAG, "Music is disabled, not playing");



            return;



        }







        // If same music is already playing, just resume if paused



        if (currentMusicType == musicType && mediaPlayer != null) {



            if (isPaused) {



                mediaPlayer.start();



                isPaused = false;



                Log.d(TAG, musicType + " music resumed");



            }



            return;



        }







        // Stop current music before switching



        if (mediaPlayer != null) {



            mediaPlayer.stop();



            mediaPlayer.release();



            mediaPlayer = null;



        }







        // Start new music



        try {



            mediaPlayer = MediaPlayer.create(context, musicType.getResourceId());



            if (mediaPlayer != null) {



                mediaPlayer.setLooping(true);



                mediaPlayer.setVolume(0.2f, 0.2f); // 30% volume - not too loud



                mediaPlayer.start();



                isPaused = false;



                currentMusicType = musicType;



                Log.d(TAG, musicType + " music started");



            } else {



                Log.e(TAG, "Failed to create MediaPlayer for " + musicType);



            }



        } catch (Exception e) {



            Log.e(TAG, "Error starting " + musicType + " music: " + e.getMessage());



        }



    }







    /**



     * Play default dashboard music (for backward compatibility)



     */



    public void play() {



        playMusic(MusicType.DASHBOARD);

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