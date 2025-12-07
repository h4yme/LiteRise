package com.example.literise.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView btnVoiceNormal, btnVoiceSlow;
    private SwitchMaterial switchSounds, switchDownload;
    private CardView cardAboutApp;
    private TextView btnLogOut;

    private SharedPreferences prefs;
    private SessionManager session;
    private boolean isVoiceNormal = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        session = new SessionManager(this);
        prefs = getSharedPreferences("LiteRiseSettings", MODE_PRIVATE);

        initializeViews();
        loadSettings();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnVoiceNormal = findViewById(R.id.btnVoiceNormal);
        btnVoiceSlow = findViewById(R.id.btnVoiceSlow);
        switchSounds = findViewById(R.id.switchSounds);
        switchDownload = findViewById(R.id.switchDownload);
        cardAboutApp = findViewById(R.id.cardAboutApp);
        btnLogOut = findViewById(R.id.btnLogOut);
    }

    private void loadSettings() {
        // Load voice speed setting
        isVoiceNormal = prefs.getBoolean("voice_normal", true);
        updateVoiceSpeedUI();

        // Load sounds setting
        boolean soundsEnabled = prefs.getBoolean("sounds_enabled", true);
        switchSounds.setChecked(soundsEnabled);

        // Load download lessons setting
        boolean downloadEnabled = prefs.getBoolean("download_enabled", false);
        switchDownload.setChecked(downloadEnabled);
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Voice speed buttons
        btnVoiceNormal.setOnClickListener(v -> {
            isVoiceNormal = true;
            updateVoiceSpeedUI();
            saveVoiceSpeed();
        });

        btnVoiceSlow.setOnClickListener(v -> {
            isVoiceNormal = false;
            updateVoiceSpeedUI();
            saveVoiceSpeed();
        });

        // Sounds switch
        switchSounds.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("sounds_enabled", isChecked).apply();
        });

        // Download lessons switch
        switchDownload.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("download_enabled", isChecked).apply();
            if (isChecked) {
                // TODO: Implement download lessons functionality
                android.widget.Toast.makeText(this, "Download lessons feature coming soon!",
                    android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        // About App card
        cardAboutApp.setOnClickListener(v -> showAboutDialog());

        // Log Out button
        btnLogOut.setOnClickListener(v -> showLogoutDialog());
    }

    private void updateVoiceSpeedUI() {
        if (isVoiceNormal) {
            // Normal is selected
            btnVoiceNormal.setBackgroundResource(R.drawable.bg_voice_speed_selected);
            btnVoiceNormal.setTextColor(0xFFFFFFFF); // White
            btnVoiceSlow.setBackgroundResource(android.R.color.transparent);
            btnVoiceSlow.setTextColor(0xFF636E72); // Gray
        } else {
            // Slow is selected
            btnVoiceSlow.setBackgroundResource(R.drawable.bg_voice_speed_selected);
            btnVoiceSlow.setTextColor(0xFFFFFFFF); // White
            btnVoiceNormal.setBackgroundResource(android.R.color.transparent);
            btnVoiceNormal.setTextColor(0xFF636E72); // Gray
        }
    }

    private void saveVoiceSpeed() {
        prefs.edit().putBoolean("voice_normal", isVoiceNormal).apply();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("About LiteRise")
            .setMessage("LiteRise v1.0\n\n" +
                    "An adaptive literacy learning app for children.\n\n" +
                    "© 2024 LiteRise. All rights reserved.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out", (dialog, which) -> {
                session.logout();
                Intent intent = new Intent(SettingsActivity.this, SplashLoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
