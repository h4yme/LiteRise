package com.example.literise.activities;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.literise.R;

/**
 * BaseNavActivity — shared bottom-navigation logic for all main tabs.
 * Subclasses call setupBottomNav(selectedIndex) after setContentView.
 * Indices: 0=Home 1=Modules 2=Progress 3=Badges 4=Profile
 */
public abstract class BaseNavActivity extends BaseActivity {

    private static final int PURPLE = 0xFF7C3AED;
    private static final int GRAY   = 0xFF9CA3AF;

    private LinearLayout navHome, navModules, navProgress, navBadges, navProfile;
    private ImageView iconHome, iconModules, iconProgress, iconBadges, iconProfile;
    private TextView  labelHome, labelModules, labelProgress, labelBadges, labelProfile;
    private View      indHome, indModules, indProgress, indBadges, indProfile;

    protected void setupBottomNav(int selectedIndex) {
        navHome     = findViewById(R.id.navHome);
        navModules  = findViewById(R.id.navModules);
        navProgress = findViewById(R.id.navProgress);
        navBadges   = findViewById(R.id.navBadges);
        navProfile  = findViewById(R.id.navProfile);

        iconHome     = findViewById(R.id.iconHome);
        iconModules  = findViewById(R.id.iconModules);
        iconProgress = findViewById(R.id.iconProgress);
        iconBadges   = findViewById(R.id.iconBadges);
        iconProfile  = findViewById(R.id.iconProfile);

        labelHome     = findViewById(R.id.labelHome);
        labelModules  = findViewById(R.id.labelModules);
        labelProgress = findViewById(R.id.labelProgress);
        labelBadges   = findViewById(R.id.labelBadges);
        labelProfile  = findViewById(R.id.labelProfile);

        indHome     = findViewById(R.id.indicatorHome);
        indModules  = findViewById(R.id.indicatorModules);
        indProgress = findViewById(R.id.indicatorProgress);
        indBadges   = findViewById(R.id.indicatorBadges);
        indProfile  = findViewById(R.id.indicatorProfile);

        // Activate the current tab
        resetAll();
        activateIndex(selectedIndex);

        // Listeners
        if (navHome != null)     navHome.setOnClickListener(v     -> navigate(0));
        if (navModules != null)  navModules.setOnClickListener(v  -> navigate(1));
        if (navProgress != null) navProgress.setOnClickListener(v -> navigate(2));
        if (navBadges != null)   navBadges.setOnClickListener(v   -> navigate(3));
        if (navProfile != null)  navProfile.setOnClickListener(v  -> navigate(4));
    }

    private void navigate(int index) {
        int current = getNavIndex();
        if (index == current) return; // already here
        Intent intent;
        switch (index) {
            case 0:
                intent = new Intent(this, DashboardActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                break;
            case 1:
                startActivity(new Intent(this, ModulesViewActivity.class));
                break;
            case 2:
                startActivity(new Intent(this, ProgressViewActivity.class));
                break;
            case 3:
                startActivity(new Intent(this, BadgesActivity.class));
                break;
            case 4:
                startActivity(new Intent(this, ProfileViewActivity.class));
                break;
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void resetAll() {
        View[]      inds   = {indHome, indModules, indProgress, indBadges, indProfile};
        ImageView[] icons  = {iconHome, iconModules, iconProgress, iconBadges, iconProfile};
        TextView[]  labels = {labelHome, labelModules, labelProgress, labelBadges, labelProfile};
        int[] outlineIcons = {R.drawable.ic_home, R.drawable.ic_book, R.drawable.ic_chart,
                              R.drawable.ic_badge, R.drawable.ic_user};

        for (int i = 0; i < 5; i++) {
            if (inds[i] != null)   inds[i].setVisibility(View.INVISIBLE);
            if (icons[i] != null)  { icons[i].setImageResource(outlineIcons[i]); icons[i].setColorFilter(GRAY); }
            if (labels[i] != null) { labels[i].setTextColor(GRAY); labels[i].setAlpha(1f); }
        }
    }

    private void activateIndex(int idx) {
        View[] inds = {indHome, indModules, indProgress, indBadges, indProfile};
        ImageView[] icons = {iconHome, iconModules, iconProgress, iconBadges, iconProfile};
        TextView[] labels = {labelHome, labelModules, labelProgress, labelBadges, labelProfile};
        int[] filledIcons = {R.drawable.ic_home_filled, R.drawable.ic_book_filled,
                             R.drawable.ic_chart_filled, R.drawable.ic_badge_filled,
                             R.drawable.ic_user_filled};

        if (idx < 0 || idx > 4) return;
        if (inds[idx] != null) {
            inds[idx].setVisibility(View.VISIBLE);
            inds[idx].setAlpha(0f);
            inds[idx].setScaleX(0.3f);
            inds[idx].setScaleY(0.3f);
            inds[idx].animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(280).start();
        }
        if (icons[idx] != null) {
            icons[idx].setImageResource(filledIcons[idx]);
            icons[idx].setColorFilter(PURPLE);
            icons[idx].animate().scaleX(1.2f).scaleY(1.2f).setDuration(140)
                    .withEndAction(() -> icons[idx].animate().scaleX(1f).scaleY(1f).setDuration(140).start())
                    .start();
        }
        if (labels[idx] != null) {
            labels[idx].setTextColor(PURPLE);
        }
    }

    /** Return the nav tab index for this screen (0-4). */
    protected abstract int getNavIndex();
}
