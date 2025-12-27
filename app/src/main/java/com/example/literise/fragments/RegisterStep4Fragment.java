package com.example.literise.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.literise.R;

public class RegisterStep4Fragment extends Fragment {

    private EditText etEmail, etPassword, etConfirmPassword;
    private CheckBox cbParentPermission;
    private StepValidationListener validationListener;

    public interface StepValidationListener {
        void onValidationChanged(boolean isValid);
    }

    public void setValidationListener(StepValidationListener listener) {
        this.validationListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_step4, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword);
        cbParentPermission = view.findViewById(R.id.cbParentPermission);

        TextWatcher validationWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateStep();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(validationWatcher);
        etPassword.addTextChangedListener(validationWatcher);
        etConfirmPassword.addTextChangedListener(validationWatcher);
        cbParentPermission.setOnCheckedChangeListener((buttonView, isChecked) -> validateStep());
    }

    private void validateStep() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        boolean isValid = !email.isEmpty() &&
                email.contains("@") &&
                password.length() >= 6 &&
                password.equals(confirmPassword) &&
                cbParentPermission.isChecked();

        if (validationListener != null) {
            validationListener.onValidationChanged(isValid);
        }
    }

    public String getEmail() {
        return etEmail.getText().toString().trim();
    }

    public String getPassword() {
        return etPassword.getText().toString().trim();
    }

    public boolean isValid() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        return !email.isEmpty() &&
                email.contains("@") &&
                password.length() >= 6 &&
                password.equals(confirmPassword) &&
                cbParentPermission.isChecked();
    }
}
