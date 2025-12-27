package com.example.literise.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.RegisterRequest;
import com.example.literise.models.RegisterResponse;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    EditText etNickname, etFirstName, etLastName, etBirthday, etEmail, etPassword, etConfirmPassword;
    Spinner spinnerGender, spinnerSchool;
    CheckBox cbParentPermission;
    MaterialButton btnRegister;
    ImageView btnBack;

    private String selectedBirthday = "";
    private int selectedSchoolId = 1; // Default school ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        btnBack = findViewById(R.id.btnBack);
        etNickname = findViewById(R.id.etNickname);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etBirthday = findViewById(R.id.etBirthday);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        cbParentPermission = findViewById(R.id.cbParentPermission);
        btnRegister = findViewById(R.id.btnRegister);

        // Fade-in animation
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootView.startAnimation(fadeIn);

        // Setup spinners
        setupGenderSpinner();
        setupSchoolSpinner();

        // Birthday picker
        etBirthday.setOnClickListener(v -> showDatePicker());

        // Back button
        btnBack.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // Text watchers for validation
        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etNickname.addTextChangedListener(validationWatcher);
        etFirstName.addTextChangedListener(validationWatcher);
        etLastName.addTextChangedListener(validationWatcher);
        etEmail.addTextChangedListener(validationWatcher);
        etPassword.addTextChangedListener(validationWatcher);
        etConfirmPassword.addTextChangedListener(validationWatcher);

        cbParentPermission.setOnCheckedChangeListener((buttonView, isChecked) -> validateForm());

        // Register button click
        btnRegister.setOnClickListener(v -> {
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
                        doRegister();
                    })
                    .start();
        });
    }

    private void setupGenderSpinner() {
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupSchoolSpinner() {
        // Mock schools - In production, fetch from API
        String[] schools = {
                "Select School",
                "Tandang Sora Elementary School",
                "Quezon City Elementary School",
                "Manila Central School",
                "Other School"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, schools);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchool.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 7; // Default to 7 years old (Grade 1)
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedBirthday = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etBirthday.setText(selectedBirthday);
                    validateForm();
                },
                year, month, day
        );

        // Set max date to today (can't be born in future)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        // Set min date to 100 years ago
        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.YEAR, year - 100);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }

    private void validateForm() {
        String nickname = etNickname.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = !nickname.isEmpty() &&
                !firstName.isEmpty() &&
                !lastName.isEmpty() &&
                !selectedBirthday.isEmpty() &&
                spinnerGender.getSelectedItemPosition() > 0 &&
                spinnerSchool.getSelectedItemPosition() > 0 &&
                !email.isEmpty() &&
                password.length() >= 6 &&
                password.equals(confirmPassword) &&
                cbParentPermission.isChecked();

        btnRegister.setEnabled(isValid);
        btnRegister.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private void doRegister() {
        String nickname = etNickname.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        // Final validation
        if (nickname.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            CustomToast.showWarning(this, "Please fill in all required fields");
            return;
        }

        if (selectedBirthday.isEmpty()) {
            CustomToast.showWarning(this, "Please select your birthday");
            return;
        }

        if (spinnerGender.getSelectedItemPosition() == 0) {
            CustomToast.showWarning(this, "Please select your gender");
            return;
        }

        if (spinnerSchool.getSelectedItemPosition() == 0) {
            CustomToast.showWarning(this, "Please select your school");
            return;
        }

        if (!email.contains("@")) {
            CustomToast.showWarning(this, "Please enter a valid email address");
            return;
        }

        if (password.length() < 6) {
            CustomToast.showWarning(this, "Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            CustomToast.showWarning(this, "Passwords do not match");
            return;
        }

        if (!cbParentPermission.isChecked()) {
            CustomToast.showWarning(this, "Parent permission is required");
            return;
        }

        // Map school selection to school_id (adjust based on your database)
        selectedSchoolId = spinnerSchool.getSelectedItemPosition(); // Simple mapping for now

        // Disable button during API call
        btnRegister.setEnabled(false);

        // Create register request
        RegisterRequest request = new RegisterRequest(
                nickname,
                firstName,
                lastName,
                selectedBirthday,
                gender,
                selectedSchoolId,
                "Grade 1", // Fixed for this app
                email,
                password
        );

        // Make API call
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                btnRegister.setEnabled(true);

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

                        // Navigate to WelcomeActivity
                        Intent intent = new Intent(RegisterActivity.this, WelcomeActivity.class);
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
                btnRegister.setEnabled(true);
                CustomToast.showError(RegisterActivity.this, "Connection error. Please try again.");
            }
        });
    }
}
