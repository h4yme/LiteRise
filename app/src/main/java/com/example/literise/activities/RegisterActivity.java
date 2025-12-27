package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.literise.R;
import com.example.literise.WelcomeTutorialActivity;
import com.example.literise.adapters.RegisterPagerAdapter;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.fragments.RegisterStep1Fragment;
import com.example.literise.fragments.RegisterStep2Fragment;
import com.example.literise.fragments.RegisterStep3Fragment;
import com.example.literise.fragments.RegisterStep4Fragment;
import com.example.literise.models.RegisterRequest;
import com.example.literise.models.RegisterResponse;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnNext, btnPrevious;
    private ImageView btnBack;
    private TextView tvStepTitle, tvStepSubtitle;
    private View step1Indicator, step2Indicator, step3Indicator, step4Indicator;

    private RegisterPagerAdapter pagerAdapter;
    private int currentStep = 0;

    private String[] stepTitles = {"About You", "Birthday & Gender", "School Info", "Account Setup"};
    private String[] stepSubtitles = {
            "Tell us about yourself",
            "When were you born?",
            "Where do you study?",
            "Create your account"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnBack = findViewById(R.id.btnBack);
        tvStepTitle = findViewById(R.id.tvStepTitle);
        tvStepSubtitle = findViewById(R.id.tvStepSubtitle);

        step1Indicator = findViewById(R.id.step1Indicator);
        step2Indicator = findViewById(R.id.step2Indicator);
        step3Indicator = findViewById(R.id.step3Indicator);
        step4Indicator = findViewById(R.id.step4Indicator);

        // Setup ViewPager
        pagerAdapter = new RegisterPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setUserInputEnabled(false); // Disable swipe

        // Fade-in animation
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootView.startAnimation(fadeIn);

        // ViewPager page change listener
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentStep = position;
                updateUI();
            }
        });

        // Back button
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Previous button
        btnPrevious.setOnClickListener(v -> {
            if (currentStep > 0) {
                viewPager.setCurrentItem(currentStep - 1, true);
            }
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            if (validateCurrentStep()) {
                if (currentStep < 3) {
                    viewPager.setCurrentItem(currentStep + 1, true);
                } else {
                    // Last step - submit registration
                    doRegister();
                }
            }
        });

        updateUI();
    }

    private void updateUI() {
        // Update step title and subtitle
        tvStepTitle.setText(stepTitles[currentStep]);
        tvStepSubtitle.setText(stepSubtitles[currentStep]);

        // Update indicators
        updateIndicator(step1Indicator, currentStep >= 0);
        updateIndicator(step2Indicator, currentStep >= 1);
        updateIndicator(step3Indicator, currentStep >= 2);
        updateIndicator(step4Indicator, currentStep >= 3);

        // Update button visibility
        if (currentStep == 0) {
            btnPrevious.setVisibility(View.GONE);
            btnNext.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    0, btnNext.getLayoutParams().height, 1f
            ));
        } else {
            btnPrevious.setVisibility(View.VISIBLE);
            btnNext.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                    0, btnNext.getLayoutParams().height, 1f
            ));
        }

        // Update Next button text
        if (currentStep == 3) {
            btnNext.setText("Create Account");
        } else {
            btnNext.setText("Next");
        }
    }

    private void updateIndicator(View indicator, boolean isActive) {
        indicator.setBackgroundColor(isActive ? 0xFF6C5CE7 : 0xFFE0E0E0);
    }

    private boolean validateCurrentStep() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + currentStep);

        switch (currentStep) {
            case 0:
                if (fragment instanceof RegisterStep1Fragment) {
                    RegisterStep1Fragment step1 = (RegisterStep1Fragment) fragment;
                    if (step1.getNickname().isEmpty() || step1.getFirstName().isEmpty() || step1.getLastName().isEmpty()) {
                        CustomToast.showWarning(this, "Please fill in all fields");
                        return false;
                    }
                    return true;
                }
                break;

            case 1:
                if (fragment instanceof RegisterStep2Fragment) {
                    RegisterStep2Fragment step2 = (RegisterStep2Fragment) fragment;
                    if (step2.getBirthday().isEmpty()) {
                        CustomToast.showWarning(this, "Please select your birthday");
                        return false;
                    }
                    if (step2.getGender().equals("Select Gender")) {
                        CustomToast.showWarning(this, "Please select your gender");
                        return false;
                    }
                    return true;
                }
                break;

            case 2:
                if (fragment instanceof RegisterStep3Fragment) {
                    RegisterStep3Fragment step3 = (RegisterStep3Fragment) fragment;
                    if (!step3.isValid()) {
                        CustomToast.showWarning(this, "Please select your school");
                        return false;
                    }
                    return true;
                }
                break;

            case 3:
                if (fragment instanceof RegisterStep4Fragment) {
                    RegisterStep4Fragment step4 = (RegisterStep4Fragment) fragment;
                    if (!step4.isValid()) {
                        CustomToast.showWarning(this, "Please complete all fields correctly");
                        return false;
                    }
                    return true;
                }
                break;
        }
        return true;
    }

    private void doRegister() {
        // Get data from all fragments
        RegisterStep1Fragment step1 = (RegisterStep1Fragment) getSupportFragmentManager().findFragmentByTag("f0");
        RegisterStep2Fragment step2 = (RegisterStep2Fragment) getSupportFragmentManager().findFragmentByTag("f1");
        RegisterStep3Fragment step3 = (RegisterStep3Fragment) getSupportFragmentManager().findFragmentByTag("f2");
        RegisterStep4Fragment step4 = (RegisterStep4Fragment) getSupportFragmentManager().findFragmentByTag("f3");

        if (step1 == null || step2 == null || step3 == null || step4 == null) {
            CustomToast.showError(this, "Please complete all steps");
            return;
        }

        String nickname = step1.getNickname();
        String firstName = step1.getFirstName();
        String lastName = step1.getLastName();
        String birthday = step2.getBirthday();
        String gender = step2.getGender();
        int schoolId = step3.getSchoolId();
        String email = step4.getEmail();
        String password = step4.getPassword();

        // Disable button during API call
        btnNext.setEnabled(false);
        btnNext.setText("Creating Account...");

        // Create register request
        RegisterRequest request = new RegisterRequest(
                nickname,
                firstName,
                lastName,
                birthday,
                gender,
                schoolId,
                "Grade 1",
                email,
                password
        );

        // Make API call
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                btnNext.setEnabled(true);
                btnNext.setText("Create Account");

                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    if (registerResponse.isSuccess()) {
                        // Save session data
                        SessionManager sessionManager = new SessionManager(RegisterActivity.this);
                        String fullName = firstName + " " + lastName;
                        sessionManager.saveStudent(registerResponse.getStudentId(), fullName, email);

                        if (registerResponse.getToken() != null && !registerResponse.getToken().isEmpty()) {
                            sessionManager.saveToken(registerResponse.getToken());
                        }

                        sessionManager.saveNickname(nickname);

                        CustomToast.showSuccess(RegisterActivity.this, "Registration successful!");

                        // Navigate to Welcome Tutorial
                        Intent intent = new Intent(RegisterActivity.this, WelcomeTutorialActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        finish();
                    } else {
                        CustomToast.showError(RegisterActivity.this, registerResponse.getMessage());
                    }
                } else {
                    CustomToast.showError(RegisterActivity.this, "Registration failed. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                btnNext.setEnabled(true);
                btnNext.setText("Create Account");
                CustomToast.showError(RegisterActivity.this, "Connection error. Please try again.");
            }
        });
    }
}
