package com.example.literise.activities;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.google.android.material.button.MaterialButton;

public class PlacementIntroActivity extends AppCompatActivity {

    private MaterialButton btnStartPlacement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_intro);

        btnStartPlacement = findViewById(R.id.btnStartPlacement);

        // Fade-in animation
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootView.startAnimation(fadeIn);

        // TODO: Navigate to placement test when button is clicked
        btnStartPlacement.setOnClickListener(v -> {
            // Placeholder - will be implemented in Phase 3
        });
    }
}