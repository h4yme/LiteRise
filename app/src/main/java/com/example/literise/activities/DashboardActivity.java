package com.example.literise.activities;



import android.content.Intent;

import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;

import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.adapters.ModuleAdapter;
import com.example.literise.database.SessionManager;
import com.example.literise.models.LearningModule;
import com.example.literise.utils.ModuleOrderingHelper;
import com.example.literise.utils.ModulePriorityManager;
import com.google.android.material.button.MaterialButton;

import java.util.List;


public class DashboardActivity extends BaseActivity {



    private TextView tvHeaderXP, tvStreak, tvBadges, tvWelcome, tvMotivation, tvModuleSummary;

    private ImageView ivLeoMascot, ivSettings;

    private MaterialButton btnContinueLesson;

    private RecyclerView rvModules;
    private ModuleAdapter moduleAdapter;
    private List<LearningModule> modules;

    // Custom Bottom Navigation Views
    private LinearLayout navHome, navModules, navProgress, navProfile;
    private ImageView iconHome, iconModules, iconProgress, iconProfile;
    private TextView labelHome, labelModules, labelProgress, labelProfile;

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

        setContentView(R.layout.activity_dashboard);



        session = new SessionManager(this);

        priorityManager = new ModulePriorityManager(this);



        initializeViews();

        loadUserData();

        loadModulesFromPlacementResults();

        setupListeners();

    }



    private void initializeViews() {

        tvHeaderXP = findViewById(R.id.tvHeaderXP);

        tvStreak = findViewById(R.id.tvStreak);

        tvBadges = findViewById(R.id.tvBadges);

        tvWelcome = findViewById(R.id.tvWelcome);

        tvMotivation = findViewById(R.id.tvMotivation);

        tvModuleSummary = findViewById(R.id.tvModuleSummary);

        ivLeoMascot = findViewById(R.id.ivLeoMascot);

        ivSettings = findViewById(R.id.ivSettings);

        btnContinueLesson = findViewById(R.id.btnContinueLesson);

        rvModules = findViewById(R.id.rvModules);

        // Setup RecyclerView
        rvModules.setLayoutManager(new LinearLayoutManager(this));

        // Custom Bottom Navigation
        navHome = findViewById(R.id.navHome);
        navModules = findViewById(R.id.navModules);
        navProgress = findViewById(R.id.navProgress);
        navProfile = findViewById(R.id.navProfile);

        iconHome = findViewById(R.id.iconHome);
        iconModules = findViewById(R.id.iconModules);
        iconProgress = findViewById(R.id.iconProgress);
        iconProfile = findViewById(R.id.iconProfile);

        labelHome = findViewById(R.id.labelHome);
        labelModules = findViewById(R.id.labelModules);
        labelProgress = findViewById(R.id.labelProgress);
        labelProfile = findViewById(R.id.labelProfile);

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

        // Continue lesson button (optional, may not be in layout)
        if (btnContinueLesson != null) {
            btnContinueLesson.setOnClickListener(v -> continueLesson());
        }

        ivLeoMascot.setOnClickListener(v -> showLeoEncouragement());

        ivSettings.setOnClickListener(v -> openSettings());
        btnNext.setOnClickListener(v -> nextTutorialStep());

        btnSkip.setOnClickListener(v -> dismissTutorial());

        // Custom Bottom Navigation Listeners
        navHome.setOnClickListener(v -> selectNavItem(0));
        navModules.setOnClickListener(v -> selectNavItem(1));
        navProgress.setOnClickListener(v -> selectNavItem(2));
        navProfile.setOnClickListener(v -> selectNavItem(3));

        // Show tutorial on first visit

        showTutorialIfFirstTime();

    }

    /**
     * Handle navigation item selection with animation
     */
    private void selectNavItem(int position) {
        // Reset all items with animation
        resetNavItems();

        // Set selected item with expanding animation
        switch (position) {
            case 0: // Home
                animateNavSelection(navHome, iconHome, labelHome);
                loadModulesFromPlacementResults();
                break;

            case 1: // Modules
                animateNavSelection(navModules, iconModules, labelModules);
                Toast.makeText(this, "Modules - Coming Soon!", Toast.LENGTH_SHORT).show();
                break;

            case 2: // Progress
                animateNavSelection(navProgress, iconProgress, labelProgress);
                Toast.makeText(this, "Progress - Coming Soon!", Toast.LENGTH_SHORT).show();
                break;

            case 3: // Profile
                animateNavSelection(navProfile, iconProfile, labelProfile);
                openSettings();
                break;
        }
    }

    /**
     * Animate navigation item selection with expand effect
     */
    private void animateNavSelection(LinearLayout navItem, ImageView icon, TextView label) {
        // Set background
        navItem.setBackgroundResource(R.drawable.nav_item_selected_bg);

        // Change icon color
        icon.setColorFilter(getResources().getColor(R.color.purple_600));

        // Show and animate label with scale and fade
        label.setVisibility(View.VISIBLE);
        label.setAlpha(0f);
        label.setScaleX(0.8f);
        label.setScaleY(0.8f);

        label.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start();

        // Animate pill background with scale
        navItem.setScaleX(0.95f);
        navItem.setScaleY(0.95f);
        navItem.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(200)
            .start();
    }

    /**
     * Reset all navigation items to unselected state with fade out animation
     */
    private void resetNavItems() {
        // Animate hide all labels
        animateHideLabel(labelHome);
        animateHideLabel(labelModules);
        animateHideLabel(labelProgress);
        animateHideLabel(labelProfile);

        // Reset backgrounds
        navHome.setBackgroundResource(android.R.color.transparent);
        navModules.setBackgroundResource(android.R.color.transparent);
        navProgress.setBackgroundResource(android.R.color.transparent);
        navProfile.setBackgroundResource(android.R.color.transparent);

        // Reset icon colors to gray
        int grayColor = getResources().getColor(R.color.gray_400);
        iconHome.setColorFilter(grayColor);
        iconModules.setColorFilter(grayColor);
        iconProgress.setColorFilter(grayColor);
        iconProfile.setColorFilter(grayColor);
    }

    /**
     * Animate hiding label with fade and scale
     */
    private void animateHideLabel(TextView label) {
        if (label.getVisibility() == View.VISIBLE) {
            label.animate()
                .alpha(0f)
                .scaleX(0.8f)
                .scaleY(0.8f)
                .setDuration(150)
                .withEndAction(() -> label.setVisibility(View.GONE))
                .start();
        }
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

        tvStreak.setText(String.format("%d Day", currentStreak));

        tvBadges.setText(String.format("%d Badges Earned", totalBadges));

    }



    /**
     * Load modules based on placement test results
     * Creates 5 Grade 3 English modules ordered by priority (weakest first)
     * Aligned with MATATAG English Curriculum Guide 2023
     */
    private void loadModulesFromPlacementResults() {
        // Get placement test results from session
        double oralLanguageScore = session.getCategoryAccuracy("Oral Language");
        double wordKnowledgeScore = session.getCategoryAccuracy("Word Knowledge");
        double readingCompScore = session.getCategoryAccuracy("Reading Comprehension");
        double languageStructScore = session.getCategoryAccuracy("Language Structure");
        String placementLevel = session.getPlacementLevel();

        // If no placement test taken yet, use default values
        if (oralLanguageScore == 0 && wordKnowledgeScore == 0 &&
            readingCompScore == 0 && languageStructScore == 0) {
            // Default values for new students
            oralLanguageScore = 0.60;
            wordKnowledgeScore = 0.55;
            readingCompScore = 0.50;
            languageStructScore = 0.65;
            placementLevel = "Mid Grade 3";
        }

        // Create modules using ModuleOrderingHelper
        modules = ModuleOrderingHelper.createModulesFromPlacementResults(
            oralLanguageScore,
            wordKnowledgeScore,
            readingCompScore,
            languageStructScore
        );

        // Apply locking based on placement level
        ModuleOrderingHelper.applyModuleLocking(modules, placementLevel);

        // Debug: Log module count
        android.util.Log.d("DashboardActivity", "Total modules created: " + modules.size());
        for (int i = 0; i < modules.size(); i++) {
            android.util.Log.d("DashboardActivity", "Module " + (i+1) + ": " + modules.get(i).getTitle());
        }

        // Update summary text
        String summary = ModuleOrderingHelper.getModuleSummary(modules);
        tvModuleSummary.setText("Modules prioritized by your needs • " + summary);

        // Setup adapter with click listener
        moduleAdapter = new ModuleAdapter(this, modules, module -> {
            openModule(module);
        });
        rvModules.setAdapter(moduleAdapter);

        // Force RecyclerView to recalculate height
        rvModules.post(() -> rvModules.requestLayout());
    }






    /**
     * Open a learning module
     */
    private void openModule(LearningModule module) {
        if (module.isLocked()) {
            Toast.makeText(this,
                "Complete previous modules to unlock " + module.getTitle(),
                Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ModuleLadderActivity.class);
        intent.putExtra("module_id", module.getModuleId());
        intent.putExtra("module_name", module.getTitle());
        intent.putExtra("module_domain", module.getDomain());
        intent.putExtra("module_level", module.getLevel());
        intent.putExtra("priority", module.getPriorityOrder());
        startActivity(intent);
    }

    private void showLeoEncouragement() {
        String[] encouragements = {
                "You're doing great! Keep it up! 🌟",
                "Learning is an adventure! Let's go! 🚀",
                "Every step counts! You've got this! 💪",
                "I believe in you! 🦁"
        };
        int randomIndex = (int) (Math.random() * encouragements.length);
        Toast.makeText(this, encouragements[randomIndex], Toast.LENGTH_SHORT).show();
    }



    private void continueLesson() {
        // Get the highest priority unlocked module
        LearningModule recommended = ModuleOrderingHelper.getRecommendedModule(modules);
        if (recommended != null) {
            openModule(recommended);
        } else {
            Toast.makeText(this, "No lessons available yet", Toast.LENGTH_SHORT).show();
        }
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

        loadModulesFromPlacementResults();

        // Always reset to Home tab when returning
        selectNavItem(0);

    }



    @SuppressWarnings("deprecation")

    @Override

    public void onBackPressed() {

        // Stay on dashboard, don't go back

        moveTaskToBack(true);

    }

}

