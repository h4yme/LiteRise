package com.example.literise;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.literise.adapters.TutorialPagerAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class WelcomeTutorialActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private TabLayout tabIndicator;
    private MaterialButton btnNext;
    private TextView btnSkip;
    private TutorialPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_tutorial);

        viewPager = findViewById(R.id.viewPager);
        tabIndicator = findViewById(R.id.tabIndicator);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);

        // Setup ViewPager2
        adapter = new TutorialPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabIndicator, viewPager,
                (tab, position) -> {
                    // Tab configuration handled by tab_selector drawable
                }
        ).attach();

        // Update button text based on current page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 3) {
                    btnNext.setText("START!");
                    btnSkip.setVisibility(View.GONE);
                } else {
                    btnNext.setText("Next");
                    btnSkip.setVisibility(View.VISIBLE);
                }
            }
        });

        // Next button click
        btnNext.setOnClickListener(v -> {
            int currentItem = viewPager.getCurrentItem();
            if (currentItem < 3) {
                viewPager.setCurrentItem(currentItem + 1, true);
            } else {
                // Navigate to Placement Intro
                navigateToPlacementIntro();
            }
        });

        // Skip button click
        btnSkip.setOnClickListener(v -> {
            navigateToPlacementIntro();
        });
    }

    private void navigateToPlacementIntro() {
        Intent intent = new Intent(this, PlacementIntroActivity.class);
        startActivity(intent);
        finish();
    }
}
