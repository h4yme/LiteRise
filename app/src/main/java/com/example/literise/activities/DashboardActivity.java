package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.example.literise.utils.ModulePriorityManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DashboardActivity extends BaseActivity {

    private TextView tvHeaderXP, tvStreak, tvBadges, tvWelcome, tvMotivation;
    private ImageView ivLeoMascot, ivSettings;
    private MaterialButton btnContinueLesson;
    private android.widget.GridLayout gridModules;

    // Tutorial views
    private View tutorialOverlay;
    private TextView tvTutorialTitle, tvTutorialMessage;
    private ImageView ivTutorialIcon;
    private MaterialButton btnNext, btnSkip;
    private LinearLayout stepIndicatorContainer;

    private SessionManager session;
    private ModulePriorityManager priorityManager;
    private int currentStreak = 10;
    private int totalBadges = 7;
    private int currentTutorialStep = 0;
    private final int TOTAL_TUTORIAL_STEPS = 5;

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
        tvBadges = findViewById(R.id.tvBadges);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvMotivation = findViewById(R.id.tvMotivation);
        ivLeoMascot = findViewById(R.id.ivLeoMascot);
        ivSettings = findViewById(R.id.ivSettings);
        btnContinueLesson = findViewById(R.id.btnContinueLesson);
        gridModules = findViewById(R.id.gridModules);

        // Tutorial views
        tutorialOverlay = findViewById(R.id.tutorialOverlay);
        tvTutorialTitle = findViewById(R.id.tvTutorialTitle);
        tvTutorialMessage = findViewById(R.id.tvTutorialMessage);
        ivTutorialIcon = findViewById(R.id.ivTutorialIcon);
        btnNext = findViewById(R.id.btnNext);
        btnSkip = findViewById(R.id.btnSkip);
        stepIndicatorContainer = findViewById(R.id.stepIndicatorContainer);
    }

    private void setupListeners() {
        btnContinueLesson.setOnClickListener(v -> continueLesson());
        ivLeoMascot.setOnClickListener(v -> showLeoEncouragement());
        ivSettings.setOnClickListener(v -> openSettings());

        // Tutorial listeners
        btnNext.setOnClickListener(v -> nextTutorialStep());
        btnSkip.setOnClickListener(v -> dismissTutorial());

        // Show tutorial on first visit
        showTutorialIfFirstTime();
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
        tvStreak.setText(String.format("%d-Day Streak", currentStreak));
        tvBadges.setText(String.format("%d Badges", totalBadges));
    }

    /**
     * Display 6 module cards ordered by priority (weakest to strongest)
     * Simple frosted white cards matching DASHBOARD DESIGN.png
     */
    private void displayModules() {
        gridModules.removeAllViews();

        // Get modules ordered from weakest to strongest
        List<String> orderedModules = priorityManager.getOrderedModules();

        for (int i = 0; i < Math.min(6, orderedModules.size()); i++) {
            String moduleName = orderedModules.get(i);

            View moduleCard = LayoutInflater.from(this).inflate(
                    R.layout.item_dashboard_module,
                    gridModules,
                    false
            );

            // Set module icon based on module type
            ImageView ivModuleIcon = moduleCard.findViewById(R.id.ivModuleIcon);
            ivModuleIcon.setImageResource(getModuleIcon(moduleName));

            // Set module name
            TextView tvModuleName = moduleCard.findViewById(R.id.tvModuleName);
            tvModuleName.setText(moduleName);

            // Click listener to open module
            final int moduleIndex = i;
            moduleCard.setOnClickListener(v -> openModule(moduleName, moduleIndex));

            gridModules.addView(moduleCard);
        }
    }

    /**
     * Get the appropriate icon for each module
     */
    private int getModuleIcon(String moduleName) {
        switch (moduleName) {
            case "Reading Comprehension":
                return R.drawable.ic_book_reading;
            case "Phonics & Pronunciation":
                return R.drawable.ic_mic;
            case "Vocabulary Building":
                return R.drawable.ic_lightbulb;
            case "Grammar & Syntax":
                return R.drawable.ic_edit;
            case "Reading Fluency":
                return R.drawable.ic_timer;
            case "Spelling & Writing":
                return R.drawable.ic_pen;
            default:
                return R.drawable.ic_star; // Fallback icon
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
        Intent intent = new Intent(this, ModuleLadderActivity.class);
        intent.putExtra("module_name", moduleName);
        intent.putExtra("priority", priority + 1);
        startActivity(intent);
    }

    private void continueLesson() {
        // TODO: Navigate to last incomplete lesson/module
        android.widget.Toast.makeText(
                this,
                "Continue lesson feature coming soon!",
                android.widget.Toast.LENGTH_SHORT
        ).show();
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showTutorialIfFirstTime() {
        android.content.SharedPreferences prefs = getSharedPreferences("LiteRisePrefs", MODE_PRIVATE);
        boolean hasSeenTutorial = prefs.getBoolean("dashboard_tutorial_seen", false);

        if (!hasSeenTutorial) {
            currentTutorialStep = 0;
            tutorialOverlay.setVisibility(View.VISIBLE);
            setupStepIndicators();
            showTutorialStep(0);
        }
    }

    private void setupStepIndicators() {
        stepIndicatorContainer.removeAllViews();

        for (int i = 0; i < TOTAL_TUTORIAL_STEPS; i++) {
            View dot = new View(this);
            int size = (int) (8 * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(4, 0, 4, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(i == 0 ? R.drawable.bg_step_indicator_active : R.drawable.bg_step_indicator_inactive);
            stepIndicatorContainer.addView(dot);
        }
    }

    private void updateStepIndicators(int activeStep) {
        for (int i = 0; i < stepIndicatorContainer.getChildCount(); i++) {
            View dot = stepIndicatorContainer.getChildAt(i);
            dot.setBackgroundResource(i == activeStep ? R.drawable.bg_step_indicator_active : R.drawable.bg_step_indicator_inactive);
        }
    }

    private void nextTutorialStep() {
        currentTutorialStep++;

        if (currentTutorialStep >= TOTAL_TUTORIAL_STEPS) {
            dismissTutorial();
        } else {
            showTutorialStep(currentTutorialStep);
        }
    }

    private void showTutorialStep(int step) {
        updateStepIndicators(step);

        switch (step) {
            case 0: // Welcome
                tvTutorialTitle.setText("Welcome to LiteRise!");
                tvTutorialMessage.setText("Let's take a quick tour of your personalized learning dashboard. We'll show you how to make the most of your learning journey!");
                ivTutorialIcon.setVisibility(View.VISIBLE);
                ivTutorialIcon.setImageResource(R.drawable.leo_placement_test);
                btnNext.setText("Let's Go!");
                break;

            case 1: // XP & Streak
                tvTutorialTitle.setText("Track Your Progress");
                tvTutorialMessage.setText("Earn XP points for completing lessons and maintain your daily streak! The more consistent you are, the faster you'll learn.");
                ivTutorialIcon.setVisibility(View.VISIBLE);
                ivTutorialIcon.setImageResource(R.drawable.ic_fire);
                btnNext.setText("Next");
                break;

            case 2: // Modules
                tvTutorialTitle.setText("Personalized Learning Path");
                tvTutorialMessage.setText("Your modules are ordered based on your placement test results. Start with the first card to practice areas you need most!");
                ivTutorialIcon.setVisibility(View.VISIBLE);
                ivTutorialIcon.setImageResource(R.drawable.ic_book_reading);
                btnNext.setText("Next");
                break;

            case 3: // Module Ladder
                tvTutorialTitle.setText("Progress Through Lessons");
                tvTutorialMessage.setText("Each module has a ladder with lessons to complete. Unlock new lessons as you progress and track your achievements!");
                ivTutorialIcon.setVisibility(View.VISIBLE);
                ivTutorialIcon.setImageResource(R.drawable.ic_star);
                btnNext.setText("Next");
                break;

            case 4: // Leo Mascot
                tvTutorialTitle.setText("Meet Leo!");
                tvTutorialMessage.setText("I'm Leo, your learning companion! Tap on me anytime for encouragement. Ready to start your learning adventure?");
                ivTutorialIcon.setVisibility(View.VISIBLE);
                ivTutorialIcon.setImageResource(R.drawable.leo_placement_test);
                btnNext.setText("Start Learning!");
                break;
        }
    }

    private void dismissTutorial() {
        tutorialOverlay.setVisibility(View.GONE);

        // Mark tutorial as seen
        android.content.SharedPreferences prefs = getSharedPreferences("LiteRisePrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("dashboard_tutorial_seen", true).apply();
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
