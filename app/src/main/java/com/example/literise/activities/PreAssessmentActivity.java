package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.database.SessionManager;

public class PreAssessmentActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnLogout;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_pre_assessment);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);

        // Initialize session
        session = new SessionManager(this);

        // Show student name
        if (session.isLoggedIn()) {
            tvWelcome.setText("Welcome " + session.getFullname() + "!");
        } else {
            // If not logged in, redirect to LoginActivity
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        // Logout button
        btnLogout.setOnClickListener(v -> {
            session.logout(); // clear session
            Intent intent = new Intent(PreAssessmentActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
