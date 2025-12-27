package com.example.literise.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.literise.R;

public class RegisterStep1Fragment extends Fragment {

    private EditText etNickname, etFirstName, etLastName;
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
        return inflater.inflate(R.layout.fragment_register_step1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNickname = view.findViewById(R.id.etNickname);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);

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

        etNickname.addTextChangedListener(validationWatcher);
        etFirstName.addTextChangedListener(validationWatcher);
        etLastName.addTextChangedListener(validationWatcher);

        // Handle keyboard navigation
        etNickname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    etFirstName.requestFocus();
                    return true;
                }
                return false;
            }
        });

        etFirstName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    etLastName.requestFocus();
                    return true;
                }
                return false;
            }
        });

        // Add focus change listeners for smooth scrolling
        etNickname.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.post(() -> v.getParent().requestChildFocus(v, v));
            }
        });

        etFirstName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.post(() -> v.getParent().requestChildFocus(v, v));
            }
        });

        etLastName.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                v.post(() -> v.getParent().requestChildFocus(v, v));
            }
        });
    }

    private void validateStep() {
        boolean isValid = !etNickname.getText().toString().trim().isEmpty() &&
                !etFirstName.getText().toString().trim().isEmpty() &&
                !etLastName.getText().toString().trim().isEmpty();

        if (validationListener != null) {
            validationListener.onValidationChanged(isValid);
        }
    }

    public String getNickname() {
        return etNickname.getText().toString().trim();
    }

    public String getFirstName() {
        return etFirstName.getText().toString().trim();
    }

    public String getLastName() {
        return etLastName.getText().toString().trim();
    }
}
