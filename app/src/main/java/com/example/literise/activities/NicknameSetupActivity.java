package com.example.literise.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.literise.utils.AppConfig;
import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.ResponseModel;
import com.example.literise.models.Students;
import com.example.literise.utils.CustomToast;
import com.example.literise.utils.MusicManager;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NicknameSetupActivity extends AppCompatActivity {

    private ImageView ivNicknameScreen;
    private TextView tvSkip;
    private TextView tvTapToContinue;
    private EditText etNickname;
    private MaterialButton btnContinue;
    private TextView tvGreeting;
    private View rootLayout;

    private int currentScreen = 0; // 0 to 3 (4 screens)
    private final int[] nicknameImages = {
            R.drawable.nickname_slide_1,
            R.drawable.nickname_slide_2,
            R.drawable.nickname_slide_3,
            R.drawable.nickname_slide_4
    };

    private String nickname = "";
    private MediaPlayer soundPlayer;
    private SessionManager sessionManager;
    private ApiService apiService;
    private MusicManager musicManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname_setup);

        // Initialize SessionManager and API
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getClient(this).create(ApiService.class);

        // Initialize music manager
        musicManager = MusicManager.getInstance(this);

        // Initialize views
        ivNicknameScreen = findViewById(R.id.ivNicknameScreen);
        tvSkip = findViewById(R.id.tvSkip);
        tvTapToContinue = findViewById(R.id.tvTapToContinue);
        etNickname = findViewById(R.id.etNickname);
        btnContinue = findViewById(R.id.btnContinue);
        tvGreeting = findViewById(R.id.tvGreeting);
        rootLayout = findViewById(R.id.rootLayout);

        // Fade-in animation
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootLayout.startAnimation(fadeIn);

        // Tap anywhere to continue (for slides 1 & 2)
        rootLayout.setOnClickListener(v -> {
            if (currentScreen < 2) { // Only slides 1 & 2 have tap to continue
                playClickSound();
                nextScreen();
            }
        });

        // Skip button click
        tvSkip.setOnClickListener(v -> {
            playClickSound();
            goToPreAssessment();
        });

        // Continue button click (Slide 3)
        btnContinue.setOnClickListener(v -> {
            String inputNickname = etNickname.getText().toString().trim();

            if (inputNickname.isEmpty()) {
                CustomToast.showWarning(this, "Please enter your nickname");
                return;
            }

            nickname = inputNickname;
            playClickSound();

            // Button press animation
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        saveNicknameToBackend(); // Save to backend first
                    })
                    .start();
        });
    }

    private void saveNicknameToBackend() {
        int studentId = sessionManager.getStudentId();
        // DEMO MODE: Skip backend save, just save locally

        if (AppConfig.DEMO_MODE) {

            saveNicknameLocally();

            CustomToast.showSuccess(NicknameSetupActivity.this, "Nickname saved!");

            nextScreen();

            return;

        }
        if (studentId == 0) {
            // No student logged in - skip backend save
            saveNicknameLocally();
            nextScreen();
            return;
        }

        // Create request
        Students student = new Students();
        student.setStudent_id(studentId);
        student.setNickname(nickname);

        // Call API
        apiService.saveNickname(student).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ResponseModel result = response.body();
                    if (result.isSuccess()) {
                        // Save locally
                        saveNicknameLocally();
                        CustomToast.showSuccess(NicknameSetupActivity.this, "Nickname saved!");
                        nextScreen(); // Go to slide 4
                    } else {
                        CustomToast.showError(NicknameSetupActivity.this,
                                result.getMessage() != null ? result.getMessage() : "Failed to save nickname");
                    }
                } else {
                    CustomToast.showError(NicknameSetupActivity.this, "Failed to save nickname");
                }
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                // Save locally even if backend fails
                saveNicknameLocally();
                CustomToast.showWarning(NicknameSetupActivity.this, "Saved locally (offline mode)");
                nextScreen(); // Go to slide 4 anyway
            }
        });
    }

    private void saveNicknameLocally() {
        sessionManager.saveNickname(nickname);
    }

    private void nextScreen() {
        if (currentScreen < 3) {
            // Move to next screen
            currentScreen++;

            // Fade transition animation
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.7f);
            fadeOut.setDuration(150);
            fadeOut.setFillAfter(true);

            ivNicknameScreen.startAnimation(fadeOut);

            // Change image
            ivNicknameScreen.postDelayed(() -> {
                ivNicknameScreen.setImageResource(nicknameImages[currentScreen]);

                AlphaAnimation fadeInScreen = new AlphaAnimation(0.7f, 1.0f);
                fadeInScreen.setDuration(150);
                ivNicknameScreen.startAnimation(fadeInScreen);

                // Handle each screen
                switch (currentScreen) {
                    case 2: // Slide 3 - Nickname input
                        tvTapToContinue.setVisibility(View.GONE);
                        tvSkip.setVisibility(View.GONE);
                        etNickname.setVisibility(View.VISIBLE);
                        btnContinue.setVisibility(View.VISIBLE);
                        break;

                    case 3: // Slide 4 - Greeting with nickname
                        etNickname.setVisibility(View.GONE);
                        btnContinue.setVisibility(View.GONE);
                        tvGreeting.setVisibility(View.VISIBLE);
                        tvTapToContinue.setVisibility(View.VISIBLE);
                        tvTapToContinue.setText("Tap to start");

                        // Update greeting with nickname
                        tvGreeting.setText("Hi " + nickname + "! I'm so excited\nto learn with you!");

                        // Make screen tappable again to proceed
                        rootLayout.setOnClickListener(v -> {
                            playClickSound();
                            goToPreAssessment();
                        });
                        break;
                }

            }, 150);
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

    private void goToPreAssessment() {
        Intent intent = new Intent(NicknameSetupActivity.this, AdaptivePreAssessmentActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Play nickname music when activity becomes visible
        musicManager.playMusic(MusicManager.MusicType.NICKNAME);
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
