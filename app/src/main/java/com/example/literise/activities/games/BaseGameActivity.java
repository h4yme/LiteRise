package com.example.literise.activities.games;



import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.UpdateProgressRequest;
import com.example.literise.models.UpdateProgressResponse;
import com.example.literise.utils.MusicManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



/**

 * BaseGameActivity - Base class for all game activities

 *

 * Automatically plays game music when the activity is visible

 * and pauses when the activity goes to background.

 * Also provides markGamePhaseComplete() so every game can mark

 * GameCompleted = 1 in StudentNodeProgress after the player finishes.

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

    /**
     * Marks the game phase complete in StudentNodeProgress.
     * Call this at the end of every game (in saveGameResults / endGame).
     * Non-fatal: logs on failure but never blocks the game flow.
     */
    protected void markGamePhaseComplete(int nodeId) {
        if (nodeId <= 0) return;

        SessionManager session = new SessionManager(this);
        int studentId = session.getStudentId();
        if (studentId <= 0) return;

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.updateNodeProgress(new UpdateProgressRequest(studentId, nodeId, "game"))
                .enqueue(new Callback<UpdateProgressResponse>() {
                    @Override
                    public void onResponse(Call<UpdateProgressResponse> call,
                                           Response<UpdateProgressResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            android.util.Log.d("BaseGameActivity",
                                    "Game phase marked complete for node " + nodeId);
                        } else {
                            android.util.Log.w("BaseGameActivity",
                                    "Game phase update returned non-success for node " + nodeId
                                    + " code=" + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<UpdateProgressResponse> call, Throwable t) {
                        android.util.Log.w("BaseGameActivity",
                                "Game phase update failed for node " + nodeId + ": " + t.getMessage());
                    }
                });
    }

}
