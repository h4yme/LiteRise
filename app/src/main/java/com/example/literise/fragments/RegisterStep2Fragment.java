package com.example.literise.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.literise.R;

import java.util.Calendar;

public class RegisterStep2Fragment extends Fragment {

    private EditText etBirthday;
    private Spinner spinnerGender;
    private String selectedBirthday = "";
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
        return inflater.inflate(R.layout.fragment_register_step2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etBirthday = view.findViewById(R.id.etBirthday);
        spinnerGender = view.findViewById(R.id.spinnerGender);

        setupGenderSpinner();
        setupBirthdayPicker();
    }

    private void setupGenderSpinner() {
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupBirthdayPicker() {
        etBirthday.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 7;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedBirthday = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    etBirthday.setText(selectedBirthday);
                    validateStep();
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void validateStep() {
        boolean isValid = !selectedBirthday.isEmpty() && spinnerGender.getSelectedItemPosition() > 0;
        if (validationListener != null) {
            validationListener.onValidationChanged(isValid);
        }
    }

    public String getBirthday() {
        return selectedBirthday;
    }

    public String getGender() {
        return spinnerGender.getSelectedItem().toString();
    }
}
