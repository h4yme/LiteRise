package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NicknameSetupActivity extends AppCompatActivity {

    private EditText etNickname;
    private MaterialButton btnContinue;
    private TextView tvSkipNickname;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname_setup);

        sessionManager = new SessionManager(this);

        initializeViews();
        setupAnimations();
        setupListeners();
    }

    private void initializeViews() {
        etNickname = findViewById(R.id.etNickname);
        btnContinue = findViewById(R.id.btnContinue);
        tvSkipNickname = findViewById(R.id.tvSkipNickname);
    }

    private void setupAnimations() {
        // Fade-in animation on load
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootView.startAnimation(fadeIn);
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> {
            // Button press animation
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        saveNickname();
                    })
                    .start();
        });

        tvSkipNickname.setOnClickListener(v -> {
            // Skip nickname setup and proceed
            proceedToAssessment();
        });
    }

    private void saveNickname() {
        String nickname = etNickname.getText().toString().trim();

        if (nickname.isEmpty()) {
            CustomToast.showWarning(this, "Please enter a nickname or skip");
            return;
        }

        // Validate nickname (alphanumeric and spaces only)
        if (!nickname.matches("[a-zA-Z0-9 ]+")) {
            CustomToast.showWarning(this, "Please use only letters and numbers");
            return;
        }

        // Save nickname locally first
        sessionManager.saveNickname(nickname);

        // Try to save to server
        int studentId = sessionManager.getStudentId();
        if (studentId > 0) {
            ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

            // Create request body (you'll need to create SaveNicknameRequest model)
            // For now, we'll save locally and proceed
            CustomToast.showSuccess(this, "Welcome, " + nickname + "!");
            proceedToAssessment();
        } else {
            CustomToast.showSuccess(this, "Welcome, " + nickname + "!");
            proceedToAssessment();
        }
    }

    private void proceedToAssessment() {
        // Mark welcome screens as seen
        sessionManager.setHasSeenWelcome(true);

        // Navigate to adaptive assessment
        Intent intent = new Intent(NicknameSetupActivity.this, AdaptivePreAssessmentActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Allow going back to welcome screens
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
