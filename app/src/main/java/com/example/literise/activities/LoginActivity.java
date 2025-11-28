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
import com.example.literise.models.Students;
import com.example.literise.utils.CustomToast;
import com.example.literise.utils.AppConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Fade-in animation on load

        View rootView = findViewById(android.R.id.content);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);

        fadeIn.setDuration(800);

        rootView.startAnimation(fadeIn);



        // Button click with scale animation

        btnLogin.setOnClickListener(v -> {

            // Button press animation

            v.animate()

                    .scaleX(0.95f)

                    .scaleY(0.95f)

                    .setDuration(100)

                    .withEndAction(() -> {

                        v.animate()

                                .scaleX(1f)

                                .scaleY(1f)

                                .setDuration(100)

                                .start();

                        doLogin();

                    })

                    .start();

        });
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            CustomToast.showWarning(this, "Please enter email and password");
            return;
        }

        Students student = new Students();
        student.setEmail(email);
        student.setPassword(password);

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.login(student).enqueue(new Callback<Students>() {
            @Override
            public void onResponse(Call<Students> call, Response<Students> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getStudent_id() != 0) {
                    Students s = response.body();

                    SessionManager sessionManager = new SessionManager(LoginActivity.this);

                    sessionManager.saveStudent(s.getStudent_id(), s.getFullname(), s.getEmail());



                    // Save token if available

                    if (s.getToken() != null && !s.getToken().isEmpty()) {

                        sessionManager.saveToken(s.getToken());

                    }



                    // Save ability and XP if available

                    sessionManager.saveAbility(s.getAbility_score());

                    sessionManager.saveXP(s.getXp());

                    CustomToast.showSuccess(LoginActivity.this, "Welcome " + s.getFullname() + "!");

                    Intent intent;

                    // Check if user has seen welcome screens

                    if (!sessionManager.hasSeenWelcome()) {

                        // First time user - show welcome/intro screens

                        intent = new Intent(LoginActivity.this, WelcomeActivity.class);

                    } else if (s.getAbility_score() == 0.0f || Math.abs(s.getAbility_score()) < 0.01f) {



                        // Seen welcome but no assessment - go to adaptive assessment

                        intent = new Intent(LoginActivity.this, AdaptivePreAssessmentActivity.class);

                    } else {

                        // Already completed assessment - go to dashboard

                        intent = new Intent(LoginActivity.this, DashboardActivity.class);

                    }
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    finish();
                } else {
                    CustomToast.showError(LoginActivity.this, "Invalid credentials");
                }
            }

            @Override
            public void onFailure(Call<Students> call, Throwable t) {
                CustomToast.showError(LoginActivity.this, "Connection error. Please try again.");
            }
        });
    }
}