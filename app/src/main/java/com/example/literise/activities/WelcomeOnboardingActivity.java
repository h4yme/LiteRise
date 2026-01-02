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
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WelcomeOnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabDots;
    private MaterialButton btnContinue;
    private TextView tvSkip;
    private OnboardingSlideAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_onboarding);

        // Initialize views
        viewPager = findViewById(R.id.viewPager);
        tabDots = findViewById(R.id.tabDots);
        btnContinue = findViewById(R.id.btnContinue);
        tvSkip = findViewById(R.id.tvSkip);

        // Setup ViewPager with adapter
        adapter = new OnboardingSlideAdapter(this);
        viewPager.setAdapter(adapter);

        // Link TabLayout dots with ViewPager
        new TabLayoutMediator(tabDots, viewPager, (tab, position) -> {
            // Just creates the dots, no text needed
        }).attach();

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

        // Update button text based on current page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == adapter.getItemCount() - 1) {
                    btnContinue.setText("Get Started!");
                } else {
                    btnContinue.setText("Continue");
                }
            }
        });
    }

    private void finishOnboarding() {
        // Mark onboarding as completed
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.setHasSeenWelcome(true);

        // Navigate to Placement Intro (to be built)
        // For now, go to Dashboard
        Intent intent = new Intent(WelcomeOnboardingActivity.this, DashboardActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }
}
