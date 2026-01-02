package com.example.literise.activities;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.models.ForgotPasswordRequest;
import com.example.literise.models.ForgotPasswordResponse;
import com.example.literise.models.ResetPasswordRequest;
import com.example.literise.models.ResetPasswordResponse;
import com.example.literise.models.VerifyOTPRequest;
import com.example.literise.models.VerifyOTPResponse;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    // Step containers
    private LinearLayout step1Container, step2Container, step3Container, successContainer;

    // Step 1: Email
    private EditText etEmail;
    private MaterialButton btnSendOTP;

    // Step 2: OTP
    private EditText etOTP;
    private MaterialButton btnVerifyOTP, btnResendOTP;
    private TextView tvOTPTimer;

    // Step 3: New Password
    private EditText etNewPassword, etConfirmPassword;
    private ImageView ivPasswordToggle1, ivPasswordToggle2;
    private MaterialButton btnResetPassword;

    // Success screen
    private MaterialButton btnBackToLogin;

    // Back button
    private ImageView btnBack;

    // Step indicators
    private View step1Indicator, step2Indicator, step3Indicator;
    private TextView tvStepTitle, tvStepSubtitle;

    // State
    private int currentStep = 1;
    private String userEmail = "";
    private String otpCode = "";
    private CountDownTimer otpTimer;
    private boolean isPassword1Visible = false;
    private boolean isPassword2Visible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupListeners();
        showStep(1);

        // Fade-in animation
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootView.startAnimation(fadeIn);
    }

    private void initializeViews() {
        // Step containers
        step1Container = findViewById(R.id.step1Container);
        step2Container = findViewById(R.id.step2Container);
        step3Container = findViewById(R.id.step3Container);
        successContainer = findViewById(R.id.successContainer);

        // Step 1 views
        etEmail = findViewById(R.id.etEmail);
        btnSendOTP = findViewById(R.id.btnSendOTP);

        // Step 2 views
        etOTP = findViewById(R.id.etOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        btnResendOTP = findViewById(R.id.btnResendOTP);
        tvOTPTimer = findViewById(R.id.tvOTPTimer);

        // Step 3 views
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivPasswordToggle1 = findViewById(R.id.ivPasswordToggle1);
        ivPasswordToggle2 = findViewById(R.id.ivPasswordToggle2);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // Success views
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Common views
        btnBack = findViewById(R.id.btnBack);
        step1Indicator = findViewById(R.id.step1Indicator);
        step2Indicator = findViewById(R.id.step2Indicator);
        step3Indicator = findViewById(R.id.step3Indicator);
        tvStepTitle = findViewById(R.id.tvStepTitle);
        tvStepSubtitle = findViewById(R.id.tvStepSubtitle);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            if (currentStep > 1 && currentStep < 4) {
                showStep(currentStep - 1);
            } else {
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        // Step 1: Send OTP
        btnSendOTP.setOnClickListener(v -> sendOTP());

        // Step 2: Verify OTP
        btnVerifyOTP.setOnClickListener(v -> verifyOTP());
        btnResendOTP.setOnClickListener(v -> resendOTP());

        // Step 3: Reset Password
        btnResetPassword.setOnClickListener(v -> resetPassword());

        // Password visibility toggles
        ivPasswordToggle1.setOnClickListener(v -> togglePasswordVisibility(etNewPassword, ivPasswordToggle1, 1));
        ivPasswordToggle2.setOnClickListener(v -> togglePasswordVisibility(etConfirmPassword, ivPasswordToggle2, 2));

        // Success: Back to login
        btnBackToLogin.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });
    }

    private void showStep(int step) {
        currentStep = step;

        // Hide all containers
        step1Container.setVisibility(View.GONE);
        step2Container.setVisibility(View.GONE);
        step3Container.setVisibility(View.GONE);
        successContainer.setVisibility(View.GONE);

        // Update indicators
        updateIndicator(step1Indicator, step >= 1);
        updateIndicator(step2Indicator, step >= 2);
        updateIndicator(step3Indicator, step >= 3);

        // Show current step
        switch (step) {
            case 1:
                step1Container.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Forgot Password?");
                tvStepSubtitle.setText("Enter your email to receive a verification code");
                break;

            case 2:
                step2Container.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Verify Code");
                tvStepSubtitle.setText("Enter the 6-digit code sent to " + maskEmail(userEmail));
                startOTPTimer();
                break;

            case 3:
                step3Container.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Create New Password");
                tvStepSubtitle.setText("Enter your new password");
                if (otpTimer != null) {
                    otpTimer.cancel();
                }
                break;

            case 4:
                successContainer.setVisibility(View.VISIBLE);
                tvStepTitle.setText("Success!");
                tvStepSubtitle.setText("Your password has been reset");
                break;
        }
    }

    private void updateIndicator(View indicator, boolean isActive) {
        indicator.setBackgroundColor(isActive ? 0xFF6C5CE7 : 0xFFE0E0E0);
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty() || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String local = parts[0];
        String domain = parts[1];

        if (local.length() <= 2) {
            return local.charAt(0) + "***@" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }

    private void sendOTP() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            CustomToast.showWarning(this, "Please enter your email");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            CustomToast.showWarning(this, "Please enter a valid email");
            return;
        }

        userEmail = email;

        // Disable button
        btnSendOTP.setEnabled(false);
        btnSendOTP.setText("Sending...");

        ForgotPasswordRequest request = new ForgotPasswordRequest(email);
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        apiService.forgotPassword(request).enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                btnSendOTP.setEnabled(true);
                btnSendOTP.setText("Send Code");

                if (response.isSuccessful() && response.body() != null) {
                    ForgotPasswordResponse forgotResponse = response.body();

                    if (forgotResponse.isSuccess()) {
                        CustomToast.showSuccess(ForgotPasswordActivity.this,
                                forgotResponse.getMessage() != null ?
                                        forgotResponse.getMessage() :
                                        "Verification code sent to your email");

                        // For testing: show OTP in debug mode
                        if (forgotResponse.getDebugOtp() != null && !forgotResponse.getDebugOtp().isEmpty()) {
                            android.util.Log.d("ForgotPassword", "Debug OTP: " + forgotResponse.getDebugOtp());
                        }

                        showStep(2);
                    } else {
                        String errorMessage = forgotResponse.getError() != null ?
                                forgotResponse.getError() :
                                (forgotResponse.getMessage() != null ?
                                        forgotResponse.getMessage() :
                                        "Failed to send verification code");
                        CustomToast.showError(ForgotPasswordActivity.this, errorMessage);
                    }
                } else {
                    CustomToast.showError(ForgotPasswordActivity.this, "Failed to send code. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                btnSendOTP.setEnabled(true);
                btnSendOTP.setText("Send Code");
                CustomToast.showError(ForgotPasswordActivity.this, "Connection error: " + t.getMessage());
            }
        });
    }

    private void verifyOTP() {
        String otp = etOTP.getText().toString().trim();

        if (otp.isEmpty()) {
            CustomToast.showWarning(this, "Please enter the verification code");
            return;
        }

        if (otp.length() != 6) {
            CustomToast.showWarning(this, "Please enter a 6-digit code");
            return;
        }

        otpCode = otp;

        // Disable button
        btnVerifyOTP.setEnabled(false);
        btnVerifyOTP.setText("Verifying...");

        VerifyOTPRequest request = new VerifyOTPRequest(userEmail, otp);
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        apiService.verifyOTP(request).enqueue(new Callback<VerifyOTPResponse>() {
            @Override
            public void onResponse(Call<VerifyOTPResponse> call, Response<VerifyOTPResponse> response) {
                btnVerifyOTP.setEnabled(true);
                btnVerifyOTP.setText("Verify Code");

                if (response.isSuccessful() && response.body() != null) {
                    VerifyOTPResponse verifyResponse = response.body();

                    if (verifyResponse.isSuccess() && verifyResponse.isValid()) {
                        CustomToast.showSuccess(ForgotPasswordActivity.this,
                                verifyResponse.getMessage() != null ?
                                        verifyResponse.getMessage() :
                                        "Code verified successfully");
                        showStep(3);
                    } else {
                        String errorMessage = verifyResponse.getError() != null ?
                                verifyResponse.getError() :
                                (verifyResponse.getMessage() != null ?
                                        verifyResponse.getMessage() :
                                        "Invalid verification code");
                        CustomToast.showError(ForgotPasswordActivity.this, errorMessage);
                    }
                } else {
                    CustomToast.showError(ForgotPasswordActivity.this, "Invalid verification code");
                }
            }

            @Override
            public void onFailure(Call<VerifyOTPResponse> call, Throwable t) {
                btnVerifyOTP.setEnabled(true);
                btnVerifyOTP.setText("Verify Code");
                CustomToast.showError(ForgotPasswordActivity.this, "Connection error: " + t.getMessage());
            }
        });
    }

    private void resendOTP() {
        // Cancel existing timer
        if (otpTimer != null) {
            otpTimer.cancel();
        }

        // Clear OTP field
        etOTP.setText("");

        // Send new OTP
        btnResendOTP.setEnabled(false);
        btnResendOTP.setText("Sending...");

        ForgotPasswordRequest request = new ForgotPasswordRequest(userEmail);
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        apiService.forgotPassword(request).enqueue(new Callback<ForgotPasswordResponse>() {
            @Override
            public void onResponse(Call<ForgotPasswordResponse> call, Response<ForgotPasswordResponse> response) {
                btnResendOTP.setEnabled(true);
                btnResendOTP.setText("Resend Code");

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    CustomToast.showSuccess(ForgotPasswordActivity.this, "New code sent to your email");

                    // For testing: show OTP in debug mode
                    if (response.body().getDebugOtp() != null && !response.body().getDebugOtp().isEmpty()) {
                        android.util.Log.d("ForgotPassword", "Debug OTP: " + response.body().getDebugOtp());
                    }

                    startOTPTimer();
                } else {
                    CustomToast.showError(ForgotPasswordActivity.this, "Failed to resend code");
                }
            }

            @Override
            public void onFailure(Call<ForgotPasswordResponse> call, Throwable t) {
                btnResendOTP.setEnabled(true);
                btnResendOTP.setText("Resend Code");
                CustomToast.showError(ForgotPasswordActivity.this, "Connection error: " + t.getMessage());
            }
        });
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            CustomToast.showWarning(this, "Please fill in all fields");
            return;
        }

        if (newPassword.length() < 6) {
            CustomToast.showWarning(this, "Password must be at least 6 characters");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            CustomToast.showWarning(this, "Passwords do not match");
            return;
        }

        // Disable button
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Resetting...");

        ResetPasswordRequest request = new ResetPasswordRequest(userEmail, otpCode, newPassword);
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        apiService.resetPassword(request).enqueue(new Callback<ResetPasswordResponse>() {
            @Override
            public void onResponse(Call<ResetPasswordResponse> call, Response<ResetPasswordResponse> response) {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Reset Password");

                if (response.isSuccessful() && response.body() != null) {
                    ResetPasswordResponse resetResponse = response.body();

                    if (resetResponse.isSuccess()) {
                        CustomToast.showSuccess(ForgotPasswordActivity.this,
                                resetResponse.getMessage() != null ?
                                        resetResponse.getMessage() :
                                        "Password reset successfully");
                        showStep(4);
                    } else {
                        String errorMessage = resetResponse.getError() != null ?
                                resetResponse.getError() :
                                (resetResponse.getMessage() != null ?
                                        resetResponse.getMessage() :
                                        "Failed to reset password");
                        CustomToast.showError(ForgotPasswordActivity.this, errorMessage);
                    }
                } else {
                    CustomToast.showError(ForgotPasswordActivity.this, "Failed to reset password. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<ResetPasswordResponse> call, Throwable t) {
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Reset Password");
                CustomToast.showError(ForgotPasswordActivity.this, "Connection error: " + t.getMessage());
            }
        });
    }

    private void startOTPTimer() {
        btnResendOTP.setEnabled(false);

        otpTimer = new CountDownTimer(600000, 1000) { // 10 minutes
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = millisUntilFinished / 60000;
                long seconds = (millisUntilFinished % 60000) / 1000;
                tvOTPTimer.setText(String.format("Code expires in %d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                tvOTPTimer.setText("Code expired");
                btnResendOTP.setEnabled(true);
            }
        }.start();

        // Enable resend button after 30 seconds
        new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Do nothing
            }

            @Override
            public void onFinish() {
                btnResendOTP.setEnabled(true);
            }
        }.start();
    }

    private void togglePasswordVisibility(EditText editText, ImageView toggleIcon, int passwordField) {
        boolean isVisible = (passwordField == 1) ? isPassword1Visible : isPassword2Visible;

        if (isVisible) {
            // Hide password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggleIcon.setImageResource(android.R.drawable.ic_menu_view);
            if (passwordField == 1) {
                isPassword1Visible = false;
            } else {
                isPassword2Visible = false;
            }
        } else {
            // Show password
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggleIcon.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
            if (passwordField == 1) {
                isPassword1Visible = true;
            } else {
                isPassword2Visible = true;
            }
        }

        // Move cursor to end
        editText.setSelection(editText.getText().length());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (otpTimer != null) {
            otpTimer.cancel();
        }
    }
}