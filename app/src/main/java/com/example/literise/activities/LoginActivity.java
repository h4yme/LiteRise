package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.literise.utils.AppConfig;
import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.Students;
import com.example.literise.utils.CustomToast;
import com.example.literise.utils.SessionLogger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    ImageView ivPasswordToggle;
    ImageView btnBack;
    TextView tvSignUp, tvForgotPassword;
    boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnBack = findViewById(R.id.btnBack);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        ivPasswordToggle = findViewById(R.id.ivPasswordToggle);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Back button click listener
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Password visibility toggle

        ivPasswordToggle.setOnClickListener(v -> {

            if (isPasswordVisible) {

                // Hide password

                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

                ivPasswordToggle.setImageResource(android.R.drawable.ic_menu_view);

                isPasswordVisible = false;

            } else {

                // Show password

                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

                ivPasswordToggle.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

                isPasswordVisible = true;

            }

            // Move cursor to end of text

            etPassword.setSelection(etPassword.getText().length());

        });
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

        // Sign Up link click
        if (tvSignUp != null) {
            tvSignUp.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            });
        }

        // Forgot Password link click
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            });
        }
    }

    private void doLogin() {
        String email = etEmail.getText().toString().trim();

        String password = etPassword.getText().toString().trim();



        // DEMO MODE: Accept any input and proceed with demo login

        if (AppConfig.DEMO_MODE) {

            // Still show a message if fields are empty for better UX

            if (email.isEmpty() || password.isEmpty()) {

                CustomToast.showWarning(this, "Please enter email and password");

                return;

            }

            // Accept any credentials in demo mode

            performDemoLogin();

            return;

        }

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

                    // CRITICAL DEBUG: Log the raw response
                    android.util.Log.d("LoginActivity", "=== LOGIN RESPONSE DEBUG ===");
                    android.util.Log.d("LoginActivity", "StudentID: " + s.getStudent_id());
                    android.util.Log.d("LoginActivity", "FullName: " + s.getFullname());
                    android.util.Log.d("LoginActivity", "Nickname: " + s.getNickname());
                    android.util.Log.d("LoginActivity", "AbilityScore: " + s.getAbility_score());
                    android.util.Log.d("LoginActivity", "PreAssessmentCompleted (raw): " + s.isPreAssessmentCompleted());
                    android.util.Log.d("LoginActivity", "AssessmentStatus: " + s.getAssessmentStatus());

                    SessionManager sessionManager = new SessionManager(LoginActivity.this);

                    sessionManager.saveStudent(s.getStudent_id(), s.getFullname(), s.getEmail());



                    // Save token if available

                    if (s.getToken() != null && !s.getToken().isEmpty()) {

                        sessionManager.saveToken(s.getToken());

                    }



                    // Save ability and XP if available

                    sessionManager.saveAbility(s.getAbility_score());

                    sessionManager.saveXP(s.getXp());








                    // Sync server data with local flags



                    boolean hasAbilityScore = (s.getAbility_score() != 0.0f && Math.abs(s.getAbility_score()) >= 0.01f);



                    boolean hasNickname = (s.getNickname() != null && !s.getNickname().isEmpty());

                    boolean preAssessmentCompleted = s.isPreAssessmentCompleted();







                    // Debug logging



                    android.util.Log.d("LoginActivity", "Server data - Nickname: " + s.getNickname() + ", AbilityScore: " + s.getAbility_score() + ", PreAssessmentCompleted: " + preAssessmentCompleted);



                    android.util.Log.d("LoginActivity", "Checks - hasNickname: " + hasNickname + ", hasAbilityScore: " + hasAbilityScore + ", preAssessmentCompleted: " + preAssessmentCompleted);







                    // Sync assessment completion status from server



                    if (preAssessmentCompleted) {



                        sessionManager.setHasSeenWelcome(true);



                        sessionManager.setAssessmentCompleted(true);



                    }







                    // Save nickname if available



                    if (hasNickname) {



                        sessionManager.saveNickname(s.getNickname());



                        sessionManager.setHasSeenWelcome(true);


                    }



                    CustomToast.showSuccess(LoginActivity.this, "Welcome " + s.getFullname() + "!");

                    // Log successful login session
                    SessionLogger.logLogin(LoginActivity.this, s.getStudent_id());

                    // CRITICAL DEBUG: Check navigation logic
                    android.util.Log.d("LoginActivity", "=== NAVIGATION DEBUG ===");
                    android.util.Log.d("LoginActivity", "hasSeenWelcome: " + sessionManager.hasSeenWelcome());
                    android.util.Log.d("LoginActivity", "hasStartedAssessment: " + sessionManager.hasStartedAssessment());
                    android.util.Log.d("LoginActivity", "hasCompletedAssessment: " + sessionManager.hasCompletedAssessment());

                    // Navigate based on user's progress
                    Intent intent;
                    if (!sessionManager.hasSeenWelcome()) {
                        // First time - show welcome onboarding
                        android.util.Log.d("LoginActivity", "NAVIGATION: Going to WelcomeOnboardingActivity (not seen welcome)");
                        intent = new Intent(LoginActivity.this, WelcomeOnboardingActivity.class);
                    } else if (sessionManager.hasStartedAssessment() && !sessionManager.hasCompletedAssessment()) {
                        // Assessment in progress - resume test
                        android.util.Log.d("LoginActivity", "NAVIGATION: Going to PlacementTestActivity (resume assessment)");
                        intent = new Intent(LoginActivity.this, PlacementTestActivity.class);
                    } else if (!sessionManager.hasCompletedAssessment()) {
                        // Seen welcome but hasn't started placement test yet
                        android.util.Log.d("LoginActivity", "NAVIGATION: Going to WelcomeOnboardingActivity (assessment not started)");
                        intent = new Intent(LoginActivity.this, WelcomeOnboardingActivity.class);
                    } else {
                        // Completed everything - go to Dashboard
                        android.util.Log.d("LoginActivity", "NAVIGATION: Going to DashboardActivity (assessment completed)");
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
    /**

     * Demo mode login - bypass API and auto-login with demo user

     */

    private void performDemoLogin() {

        SessionManager sessionManager = new SessionManager(LoginActivity.this);



        // SessionManager auto-sets up demo user in constructor when DEMO_MODE is true

        // But let's ensure it's set up properly

        if (!sessionManager.isLoggedIn()) {

            sessionManager.setupDemoUser();

        }



        CustomToast.showSuccess(LoginActivity.this, "Welcome to LiteRise Demo!");

        // Log successful login session for demo user
        SessionLogger.logLogin(LoginActivity.this, sessionManager.getStudentId());

        // Navigate based on user's progress
        Intent intent;
        if (!sessionManager.hasSeenWelcome()) {
            // First time - show welcome onboarding
            intent = new Intent(LoginActivity.this, WelcomeOnboardingActivity.class);
        } else if (sessionManager.hasStartedAssessment() && !sessionManager.hasCompletedAssessment()) {
            // Assessment in progress - resume test
            intent = new Intent(LoginActivity.this, PlacementTestActivity.class);
        } else if (!sessionManager.hasCompletedAssessment()) {
            // Seen welcome but hasn't started placement test yet
            intent = new Intent(LoginActivity.this, WelcomeOnboardingActivity.class);
        } else {
            // Completed everything - go to Dashboard
            intent = new Intent(LoginActivity.this, DashboardActivity.class);
        }
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();

    }
}