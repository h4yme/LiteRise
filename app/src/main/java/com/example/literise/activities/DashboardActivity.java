package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvUserName, tvProfileInitial, tvXP, tvAbilityLevel;
    private TextView tvProgressPercentage;
    private ProgressBar progressBar;
    private CardView cardReadingModule, cardVocabModule, cardGrammarModule;
    private com.google.android.material.button.MaterialButton btnRetakeAssessment;
    private ImageView ivSettings;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new SessionManager(this);

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        tvProfileInitial = findViewById(R.id.tvProfileInitial);
        tvXP = findViewById(R.id.tvXP);
        tvAbilityLevel = findViewById(R.id.tvAbilityLevel);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        progressBar = findViewById(R.id.progressBar);
        cardReadingModule = findViewById(R.id.cardReadingModule);
        cardVocabModule = findViewById(R.id.cardVocabModule);
        cardGrammarModule = findViewById(R.id.cardGrammarModule);
        btnRetakeAssessment = findViewById(R.id.btnRetakeAssessment);
        ivSettings = findViewById(R.id.ivSettings);

        // Load user data
        loadUserData();

        // Set click listeners
        btnRetakeAssessment.setOnClickListener(v -> retakeAssessment());
        ivSettings.setOnClickListener(v -> openSettings());

        // Module click listeners (placeholder for now)
        cardReadingModule.setOnClickListener(v -> {
            // TODO: Navigate to Reading Comprehension module
        });

        cardVocabModule.setOnClickListener(v -> {
            // TODO: Navigate to Vocabulary Building module
        });

        cardGrammarModule.setOnClickListener(v -> {
            // TODO: Navigate to Grammar Practice module
        });
    }

    private void loadUserData() {
        // Get user data from session
        String fullName = session.getFullName();
        int xp = session.getXP();
        float ability = session.getAbility();

        // Set user name
        tvUserName.setText(fullName);

        // Set profile initial (first letter of name)
        if (fullName != null && !fullName.isEmpty()) {
            tvProfileInitial.setText(String.valueOf(fullName.charAt(0)).toUpperCase());
        }

        // Set XP
        tvXP.setText(String.valueOf(xp));

        // Classify and display ability level
        String abilityLevel = classifyAbility(ability);
        tvAbilityLevel.setText(abilityLevel);

        // Calculate progress (example: based on XP)
        int currentLevel = xp / 1000; // Every 1000 XP = 1 level
        int xpInCurrentLevel = xp % 1000;
        int progressPercentage = (xpInCurrentLevel * 100) / 1000;

        tvProgressPercentage.setText(progressPercentage + "%");
        progressBar.setProgress(progressPercentage);
    }

    private String classifyAbility(float theta) {
        if (theta < -1.0) return "Below Basic";
        else if (theta < 0.5) return "Basic";
        else if (theta < 1.5) return "Intermediate";
        else if (theta < 2.5) return "Proficient";
        else return "Advanced";
    }

    private void retakeAssessment() {
        Intent intent = new Intent(this, AdaptivePreAssessmentActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void openSettings() {
        // TODO: Navigate to settings
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to assessment
        // User should log out from settings instead
        moveTaskToBack(true);
    }
}
