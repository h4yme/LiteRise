package com.example.literise.activities;



import android.content.Intent;

import android.os.Bundle;

import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;

import com.example.literise.database.SessionManager;
import com.example.literise.utils.AppConfig;



public class SplashActivity extends AppCompatActivity {



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);



        new Handler().postDelayed(() -> {

            SessionManager session = new SessionManager(SplashActivity.this);

            Intent intent;



            if (session.isLoggedIn()) {

                // Check if user has seen welcome screens
                if (!session.hasSeenWelcome()) {
                    // First time user - show welcome/intro screens
                    intent = new Intent(SplashActivity.this, WelcomeActivity.class);
                } else {
                    // 🎯 Launch Adaptive Assessment (personalized questions)
                    float ability = session.getAbility();

                    if (ability == 0.0f || Math.abs(ability) < 0.01f) {

                        // No assessment taken yet - go to assessment

                        intent = new Intent(SplashActivity.this, AdaptivePreAssessmentActivity.class);

                    } else {

                        // Assessment already taken - go to dashboard

                        intent = new Intent(SplashActivity.this, DashboardActivity.class);

                    }
                }

            } else {

                intent = new Intent(SplashActivity.this, LoginActivity.class);

            }



            startActivity(intent);

            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            finish();

        }, 2000);

    }

}