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

 *

 * Activities that should NOT have music (e.g., Login, Placement Test):

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

            musicManager.play();

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

     * Override this method if you want to conditionally disable music

     * for specific activities that extend BaseActivity

     *

     * @return true if music should play (default), false to disable

     */

    protected boolean shouldPlayMusic() {

        return true;

    }

}

