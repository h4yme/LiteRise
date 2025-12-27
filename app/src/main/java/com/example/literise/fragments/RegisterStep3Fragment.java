package com.example.literise.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.literise.R;

public class RegisterStep3Fragment extends Fragment {

    private Spinner spinnerSchool;
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
        return inflater.inflate(R.layout.fragment_register_step3, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerSchool = view.findViewById(R.id.spinnerSchool);
        setupSchoolSpinner();
    }

    private void setupSchoolSpinner() {
        String[] schools = {
                "Select School",
                "Tandang Sora Elementary School",
                "Quezon City Elementary School",
                "Manila Central School",
                "Other School"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, schools);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSchool.setAdapter(adapter);
    }

    public int getSchoolId() {
        return spinnerSchool.getSelectedItemPosition();
    }

    public boolean isValid() {
        return spinnerSchool.getSelectedItemPosition() > 0;
    }
}