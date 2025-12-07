package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.example.literise.utils.ModulePriorityManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DashboardActivity extends BaseActivity {

    private TextView tvHeaderXP, tvStreak, tvWelcome, tvMotivation;
    private ImageView ivLeoMascot;
    private MaterialButton btnContinueLesson;
    private android.widget.GridLayout gridModules;
    private SessionManager session;
    private ModulePriorityManager priorityManager;
    private int currentStreak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_new);

        session = new SessionManager(this);
        priorityManager = new ModulePriorityManager(this);

        initializeViews();
        loadUserData();
        displayModules();
        setupListeners();
    }

    private void initializeViews() {
        tvHeaderXP = findViewById(R.id.tvHeaderXP);
        tvStreak = findViewById(R.id.tvStreak);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvMotivation = findViewById(R.id.tvMotivation);
        ivLeoMascot = findViewById(R.id.ivLeoMascot);
        btnContinueLesson = findViewById(R.id.btnContinueLesson);
        gridModules = findViewById(R.id.gridModules);
    }

    private void setupListeners() {
        btnContinueLesson.setOnClickListener(v -> continueLesson());
        ivLeoMascot.setOnClickListener(v -> showLeoEncouragement());
    }

    private void loadUserData() {
        String nickname = session.getNickname();
        int xp = session.getXP();

        if (nickname != null && !nickname.isEmpty()) {
            tvWelcome.setText(String.format("Hello, %s", nickname));
        } else {
            tvWelcome.setText("Hello, Student");
        }

        tvHeaderXP.setText(String.format("%d XP", xp));
        tvStreak.setText(String.format("%d", currentStreak));
    }

    /**
     * Display 6 module cards ordered by priority (weakest to strongest)
     */
    private void displayModules() {
        gridModules.removeAllViews();

        // Get modules ordered from weakest to strongest
        List<String> orderedModules = priorityManager.getOrderedModules();

        // Module card background colors (soft gradients matching design)
        int[] moduleColors = {
                0xFFFDBEBD, // Soft pink (priority 1 - weakest)
                0xFFFDD4BC, // Soft peach (priority 2)
                0xFFFFF4CE, // Soft yellow (priority 3)
                0xFFD4F1D4, // Soft green (priority 4)
                0xFFBBDEFB, // Soft blue (priority 5)
                0xFFD4C5F9  // Soft purple (priority 6 - strongest)
        };

        // Priority badge colors
        int[] badgeColors = {
                0xFFE74C3C, // Red (highest priority)
                0xFFE67E22, // Orange
                0xFFF39C12, // Yellow
                0xFF00B894, // Green
                0xFF0984E3, // Blue
                0xFF6C5CE7  // Purple (lowest priority)
        };

        for (int i = 0; i < Math.min(6, orderedModules.size()); i++) {
            String moduleName = orderedModules.get(i);
            int priority = i + 1;

            View moduleCard = LayoutInflater.from(this).inflate(
                    R.layout.item_dashboard_module,
                    gridModules,
                    false
            );

            // Set views
            LinearLayout cardContainer = moduleCard.findViewById(R.id.moduleCardContainer);
            TextView tvPriority = moduleCard.findViewById(R.id.tvPriorityNumber);
            TextView tvModuleName = moduleCard.findViewById(R.id.tvModuleName);
            TextView tvProgress = moduleCard.findViewById(R.id.tvModuleProgress);

            // Set card background color
            cardContainer.setBackgroundColor(moduleColors[i]);

            // Set priority badge
            tvPriority.setText(String.valueOf(priority));
            tvPriority.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(badgeColors[i])
            );

            // Set module name
            tvModuleName.setText(moduleName);

            // Set progress (TODO: load from database)
            tvProgress.setText("0/20");

            // Click listener to open module
            final int moduleIndex = i;
            moduleCard.setOnClickListener(v -> openModule(moduleName, moduleIndex));

            gridModules.addView(moduleCard);
        }
    }

    private void showLeoEncouragement() {
        String[] encouragements = {
                "You're doing great! Keep it up! 🌟",
                "Learning is an adventure! Let's go! 🚀",
                "Every step counts! You've got this! 💪",
                "I believe in you! 🦁"
        };
        int randomIndex = (int) (Math.random() * encouragements.length);
        android.widget.Toast.makeText(this, encouragements[randomIndex], android.widget.Toast.LENGTH_SHORT).show();
    }

    private void openModule(String moduleName, int priority) {
        // TODO: Navigate to ModuleLadderActivity
        android.widget.Toast.makeText(
                this,
                "Opening " + moduleName + " (Priority " + (priority + 1) + ")",
                android.widget.Toast.LENGTH_SHORT
        ).show();
    }

    private void continueLesson() {
        // TODO: Navigate to last incomplete lesson/module
        android.widget.Toast.makeText(
                this,
                "Continue lesson feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadUserData();
        displayModules();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        // Stay on dashboard, don't go back
        moveTaskToBack(true);
    }
}
