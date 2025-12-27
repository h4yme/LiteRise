package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;

public class LoginRegisterSelectionActivity extends AppCompatActivity {

    CardView cardLogin, cardRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register_selection);

        cardLogin = findViewById(R.id.cardLogin);
        cardRegister = findViewById(R.id.cardRegister);

        // Fade-in animation on load
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootView.startAnimation(fadeIn);

        // Login card click
        cardLogin.setOnClickListener(v -> {
            // Card press animation
            v.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                        navigateToLogin();
                    })
                    .start();
        });

        // Register card click
        cardRegister.setOnClickListener(v -> {
            // Card press animation
            v.animate()
                    .scaleX(0.97f)
                    .scaleY(0.97f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                        navigateToRegister();
                    })
                    .start();
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(LoginRegisterSelectionActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginRegisterSelectionActivity.this, RegisterActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
