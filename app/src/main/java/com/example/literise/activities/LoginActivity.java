package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.LrnLoginRequest;
import com.example.literise.models.Students;
import com.example.literise.utils.CustomToast;
import com.example.literise.utils.SessionLogger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etLrn;
    private EditText etLoginCode;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLrn       = findViewById(R.id.etLrn);
        etLoginCode = findViewById(R.id.etLoginCode);
        btnLogin    = findViewById(R.id.btnLogin);

        // Fade-in animation on load
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootView.startAnimation(fadeIn);

        btnLogin.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        doLogin();
                    }).start();
        });
    }

    private void doLogin() {
        String lrn       = etLrn.getText().toString().trim();
        String loginCode = etLoginCode.getText().toString().trim();

        if (lrn.isEmpty()) {
            CustomToast.showWarning(this, "Please enter your LRN");
            return;
        }
        if (loginCode.isEmpty()) {
            CustomToast.showWarning(this, "Please enter your 4-digit login code");
            return;
        }

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.loginWithLrn(new LrnLoginRequest(lrn, loginCode)).enqueue(new Callback<Students>() {
            @Override
            public void onResponse(Call<Students> call, Response<Students> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getStudent_id() != 0) {

                    Students s = response.body();
                    SessionManager session = new SessionManager(LoginActivity.this);

                    session.saveStudent(s.getStudent_id(), s.getFullname(), s.getEmail());

                    if (s.getToken() != null && !s.getToken().isEmpty()) {
                        session.saveToken(s.getToken());
                    }

                    session.saveAbility(s.getAbility_score());
                    session.saveXP(s.getXp());
                    session.saveStreak(s.getCurrentStreak());

                    boolean preAssessmentCompleted = s.isPreAssessmentCompleted();
                    boolean hasNickname = (s.getNickname() != null && !s.getNickname().isEmpty());

                    if (preAssessmentCompleted) {
                        session.setHasSeenWelcome(true);
                        session.setAssessmentCompleted(true);
                    }

                    if (hasNickname) {
                        session.saveNickname(s.getNickname());
                        session.setHasSeenWelcome(true);
                    }

                    CustomToast.showSuccess(LoginActivity.this, "Welcome " + s.getFullname() + "!");
                    SessionLogger.logLogin(LoginActivity.this, s.getStudent_id());

                    // Navigate based on progress
                    if (!session.hasSeenWelcome()) {
                        navigateTo(WelcomeOnboardingActivity.class);
                    } else if (session.hasStartedAssessment() && !session.hasCompletedAssessment()) {
                        navigateTo(PlacementTestActivity.class);
                    } else if (!session.hasCompletedAssessment()) {
                        navigateTo(WelcomeOnboardingActivity.class);
                    } else {
                        session.saveCategoryScore("Cat1_PhonicsWordStudy", s.getCat1PhonicsWordStudy());
                        session.saveCategoryScore("Cat2_VocabularyWordKnowledge", s.getCat2VocabularyWordKnowledge());
                        session.saveCategoryScore("Cat3_GrammarAwareness", s.getCat3GrammarAwareness());
                        session.saveCategoryScore("Cat4_ComprehendingText", s.getCat4ComprehendingText());
                        session.saveCategoryScore("Cat5_CreatingComposing", s.getCat5CreatingComposing());
                        navigateTo(DashboardActivity.class);
                    }
                } else {
                    CustomToast.showError(LoginActivity.this,
                            "Invalid LRN or login code. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<Students> call, Throwable t) {
                CustomToast.showError(LoginActivity.this, "Connection error. Please try again.");
            }
        });
    }

    private void navigateTo(Class<?> activityClass) {
        startActivity(new Intent(LoginActivity.this, activityClass));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
