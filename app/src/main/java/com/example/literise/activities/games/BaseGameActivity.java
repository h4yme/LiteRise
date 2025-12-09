package com.example.literise.activities.games;



import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.utils.MusicManager;



/**

 * BaseGameActivity - Base class for all game activities

 *

 * Automatically plays game music when the activity is visible

 * and pauses when the activity goes to background

 */

public abstract class BaseGameActivity extends AppCompatActivity {



    private MusicManager musicManager;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        musicManager = MusicManager.getInstance(this);

    }



    @Override

    protected void onResume() {

        super.onResume();

        // Play game music when activity becomes visible

        if (shouldPlayMusic()) {

            musicManager.playMusic(MusicManager.MusicType.GAME);

        }

    }



    @Override

    protected void onPause() {

        super.onPause();

        // Pause music when activity goes to background

        if (shouldPlayMusic()) {

            musicManager.pause();

        }

    }



    /**

     * Override this method if you want to conditionally disable music

     * for specific game activities

     *

     * @return true if music should play (default), false to disable

     */

    protected boolean shouldPlayMusic() {

        return true;

    }

}