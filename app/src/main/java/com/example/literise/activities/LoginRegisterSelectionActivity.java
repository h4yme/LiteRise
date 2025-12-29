package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

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

        // Start floating animations for decorative elements
        startFloatingAnimations();

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

    private void startFloatingAnimations() {
        // Find all floating ImageViews by checking content description
        View rootView = findViewById(android.R.id.content);

        // Load different animation types
        Animation floatUpDown = AnimationUtils.loadAnimation(this, R.anim.float_up_down);
        Animation floatSlow = AnimationUtils.loadAnimation(this, R.anim.float_up_down_slow);
        Animation floatRotate = AnimationUtils.loadAnimation(this, R.anim.float_rotate);
        Animation twinkle = AnimationUtils.loadAnimation(this, R.anim.twinkle);

        // Apply animations with delays for staggered effect
        applyAnimationToView(rootView, "Floating book", floatRotate, 0);
        applyAnimationToView(rootView, "Floating star", twinkle, 300);
        applyAnimationToView(rootView, "Sparkle", twinkle, 600);
        applyAnimationToView(rootView, "Floating coin", floatRotate, 200);
        applyAnimationToView(rootView, "Globe", floatSlow, 400);
        applyAnimationToView(rootView, "Pencil", floatUpDown, 100);
        applyAnimationToView(rootView, "Small star", twinkle, 500);
        applyAnimationToView(rootView, "Small coin", floatRotate, 700);
    }

    private void applyAnimationToView(View parent, String contentDescription, Animation animation, long startDelay) {
        // Find ImageView by content description
        ImageView imageView = findImageViewByDescription(parent, contentDescription);
        if (imageView != null) {
            animation.setStartOffset(startDelay);
            imageView.startAnimation(animation);
        }
    }

    private ImageView findImageViewByDescription(View parent, String description) {
        if (parent instanceof ImageView) {
            ImageView imageView = (ImageView) parent;
            CharSequence contentDesc = imageView.getContentDescription();
            if (contentDesc != null && contentDesc.toString().equals(description)) {
                return imageView;
            }
        }

        if (parent instanceof android.view.ViewGroup) {
            android.view.ViewGroup viewGroup = (android.view.ViewGroup) parent;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                ImageView result = findImageViewByDescription(viewGroup.getChildAt(i), description);
                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }
}
