package com.example.literise.activities;



import android.content.Intent;

import android.os.Bundle;

import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;

import com.example.literise.database.SessionManager;

public class SplashActivity extends AppCompatActivity {



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);



        new Handler().postDelayed(() -> {
            SessionManager session = new SessionManager(SplashActivity.this);
            Intent intent;

            if (session.isLoggedIn()) {
                // Navigate based on user's progress - same logic as LoginActivity
                if (!session.hasSeenWelcome()) {
                    // First time - show welcome onboarding
                    intent = new Intent(SplashActivity.this, WelcomeOnboardingActivity.class);
                } else if (session.hasStartedAssessment() && !session.hasCompletedAssessment()) {
                    // Assessment in progress - resume test
                    intent = new Intent(SplashActivity.this, PlacementTestActivity.class);
                } else if (!session.hasCompletedAssessment()) {
                    // Seen welcome but hasn't started placement test yet
                    intent = new Intent(SplashActivity.this, WelcomeOnboardingActivity.class);
                } else {
                    // Completed everything - go to Dashboard
                    intent = new Intent(SplashActivity.this, DashboardActivity.class);
                }
            } else {
                // Not logged in - show login/register selection
                intent = new Intent(SplashActivity.this, LoginRegisterSelectionActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        }, 2000);

    }

}