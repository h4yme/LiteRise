package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.literise.R;
import com.example.literise.adapters.OnboardingSlideAdapter;
import com.example.literise.database.SessionManager;
import com.google.android.material.button.MaterialButton;

public class WelcomeOnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private View indicator1, indicator2, indicator3;
    private MaterialButton btnContinue;
    private TextView tvSkip;
    private OnboardingSlideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_onboarding);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        indicator1 = findViewById(R.id.indicator1);
        indicator2 = findViewById(R.id.indicator2);
        indicator3 = findViewById(R.id.indicator3);
        btnContinue = findViewById(R.id.btnContinue);
        tvSkip = findViewById(R.id.tvSkip);

        // Setup ViewPager with adapter
        adapter = new OnboardingSlideAdapter(this);
        viewPager.setAdapter(adapter);

        // Skip button
        tvSkip.setOnClickListener(v -> finishOnboarding());

        // Continue button
        btnContinue.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < adapter.getItemCount() - 1) {
                // Go to next slide
                viewPager.setCurrentItem(currentItem + 1);
            } else {
                // Last slide - finish onboarding
                finishOnboarding();
            }
        });

        // Update button text and indicators based on current page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // Update button text
                if (position == adapter.getItemCount() - 1) {
                    btnContinue.setText("Get Started!");
                } else {
                    btnContinue.setText("Continue");
                }

                // Update indicators
                updateIndicators(position);
            }
        });
    }

    private void updateIndicators(int position) {
        // Reset all indicators to inactive
        indicator1.setBackgroundResource(R.drawable.tab_indicator_active);
        indicator2.setBackgroundResource(R.drawable.tab_indicator_inactive);
        indicator3.setBackgroundResource(R.drawable.tab_indicator_inactive);

        // Set the current indicator to active
        switch (position) {
            case 0:
                indicator1.setBackgroundResource(R.drawable.tab_indicator_active);
                break;
            case 1:
                indicator2.setBackgroundResource(R.drawable.tab_indicator_active);
                break;
            case 2:
                indicator3.setBackgroundResource(R.drawable.tab_indicator_active);
                break;
        }
    }

    private void finishOnboarding() {
        // Mark onboarding as completed
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.setHasSeenWelcome(true);

        // Navigate to Placement Intro
        Intent intent = new Intent(WelcomeOnboardingActivity.this, PlacementIntroActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}