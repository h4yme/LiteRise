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
                intent = new Intent(SplashActivity.this, PreAssessmentActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out); // 👈 fade animation
            finish();
        }, 2000);

    }
}