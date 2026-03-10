package com.example.literise.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;

public class BadgesActivity extends BaseNavActivity {

    private TextView tvBadgeCount;
    private RecyclerView rvBadges;

    private SessionManager session;

    // Badge definitions (id, name, description, icon, earned)
    private static final Object[][] BADGES = {
        {1, "First Steps",      "Complete your first lesson",          true},
        {2, "Quick Learner",    "Complete 5 lessons in one day",       false},
        {3, "Word Wizard",      "Score 100% on a vocabulary quiz",     false},
        {4, "Phonics Phenom",   "Master all phonics lessons",          false},
        {5, "Grammar Guru",     "Complete the Grammar module",         false},
        {6, "Reading Star",     "Finish the Comprehension module",     false},
        {7, "LiteRise Legend",  "Complete all 5 modules",              false},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);

        session = new SessionManager(this);

        tvBadgeCount = findViewById(R.id.tvBadgeCount);
        rvBadges     = findViewById(R.id.rvBadges);

        // Count earned badges
        int earned = 0;
        for (Object[] badge : BADGES) {
            if (Boolean.TRUE.equals(badge[3])) earned++;
        }
        tvBadgeCount.setText(earned + " / " + BADGES.length);

        // Simple grid layout (2 columns) for earned badges
        rvBadges.setLayoutManager(new GridLayoutManager(this, 3));

        setupBottomNav(getNavIndex());
    }

    @Override
    protected int getNavIndex() { return 3; }
}
