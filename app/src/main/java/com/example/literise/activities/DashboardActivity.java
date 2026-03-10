package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import android.widget.ImageView;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.literise.R;

import com.example.literise.adapters.ModuleAdapter;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.BadgesResponse;
import com.example.literise.models.CheckModulesCompleteResponse;
import com.example.literise.models.CompleteTutorialRequest;
import com.example.literise.models.LearningModule;
import com.example.literise.models.ResponseModel;
import com.example.literise.models.TutorialStatusResponse;
import com.example.literise.utils.ModuleOrderingHelper;
import com.example.literise.utils.ModulePriorityManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DashboardActivity extends BaseActivity {



    private TextView tvHeaderXP, tvStreak, tvBadges, tvWelcome, tvMotivation, tvModuleSummary;

    private ImageView ivLeoMascot, ivSettings;

    private MaterialButton btnContinueLesson;
    private android.widget.LinearLayout cardCertificateBanner;
    private MaterialButton btnRePrintCertificate;

    private RecyclerView rvModules;
    private ModuleAdapter moduleAdapter;
    private List<LearningModule> modules;

    // Custom Bottom Navigation Views
    private LinearLayout navHome, navModules, navProgress, navBadges, navProfile;
    private ImageView iconHome, iconModules, iconProgress, iconBadges, iconProfile;
    private TextView labelHome, labelModules, labelProgress, labelBadges, labelProfile;
    private View indicatorHome, indicatorModules, indicatorProgress, indicatorBadges, indicatorProfile;

    // Lottie Animation Views
    private LottieAnimationView lottieHeaderSparkle, lottieStatsSparkle;

    // Tutorial views

    private View tutorialOverlay;

    private TextView tvTutorialTitle, tvTutorialMessage;

    private ImageView ivTutorialIcon;

    private MaterialButton btnNext, btnSkip;

    private LinearLayout stepIndicatorContainer;



    private SessionManager session;

    private ModulePriorityManager priorityManager;

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

        //btnContinueLesson = findViewById(R.id.btnContinueLesson);

        rvModules = findViewById(R.id.rvModules);
        cardCertificateBanner  = findViewById(R.id.cardCertificateBanner);
        btnRePrintCertificate  = findViewById(R.id.btnRePrintCertificate);

        // Setup RecyclerView
        rvModules.setLayoutManager(new LinearLayoutManager(this));

        // Custom Bottom Navigation
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

        // Nav pill indicators
        indicatorHome     = findViewById(R.id.indicatorHome);
        indicatorModules  = findViewById(R.id.indicatorModules);
        indicatorProgress = findViewById(R.id.indicatorProgress);
        indicatorBadges   = findViewById(R.id.indicatorBadges);
        indicatorProfile  = findViewById(R.id.indicatorProfile);

        // Lottie animations
        lottieHeaderSparkle = findViewById(R.id.lottieHeaderSparkle);
        lottieStatsSparkle = findViewById(R.id.lottieStatsSparkle);

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

        // Certificate re-print button
        if (btnRePrintCertificate != null) {
            btnRePrintCertificate.setOnClickListener(v -> {
                String name = session.getFullname();
                if (name == null || name.isEmpty()) name = session.getNickname();
                if (name == null || name.isEmpty()) name = "Student";
                com.example.literise.utils.CertificateHelper.generateAndShare(
                        this, name,
                        session.getPostLevelName(),
                        session.getPostTheta(),
                        session.getPreTheta(),
                        session.getPostAccuracy());
            });
        }

        ivLeoMascot.setOnClickListener(v -> showLeoEncouragement());

        ivSettings.setOnClickListener(v -> openSettings());
        btnNext.setOnClickListener(v -> nextTutorialStep());

        btnSkip.setOnClickListener(v -> dismissTutorial());

        // Custom Bottom Navigation Listeners
        navHome.setOnClickListener(v -> selectNavItem(0));
        navModules.setOnClickListener(v -> selectNavItem(1));
        navProgress.setOnClickListener(v -> selectNavItem(2));
        navBadges.setOnClickListener(v -> selectNavItem(3));
        navProfile.setOnClickListener(v -> selectNavItem(4));

        // Show tutorial on first visit

        showTutorialIfFirstTime();

    }

    /**
     * Handle navigation item selection with Material 3 style animation
     */
    private void selectNavItem(int position) {
        // Reset all items
        resetNavItems();

        // Activate selected item
        switch (position) {
            case 0: // Home (stay here)
                activateNavItem(iconHome, labelHome, indicatorHome, R.drawable.ic_home_filled);
                loadModulesFromPlacementResults();
                break;

            case 1: // Modules
                activateNavItem(iconModules, labelModules, indicatorModules, R.drawable.ic_book_filled);
                startActivity(new Intent(this, ModulesViewActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case 2: // Progress
                activateNavItem(iconProgress, labelProgress, indicatorProgress, R.drawable.ic_chart_filled);
                startActivity(new Intent(this, ProgressViewActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case 3: // Badges
                activateNavItem(iconBadges, labelBadges, indicatorBadges, R.drawable.ic_badge_filled);
                startActivity(new Intent(this, BadgesActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;

            case 4: // Profile
                activateNavItem(iconProfile, labelProfile, indicatorProfile, R.drawable.ic_user_filled);
                startActivity(new Intent(this, ProfileViewActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }
    }

    /**
     * Activate a nav item with filled icon, pill indicator, and bounce animation
     */
    private void activateNavItem(ImageView icon, TextView label, View indicator, int filledIconRes) {
        int purpleColor = 0xFF7C3AED;

        // Show pill indicator with scale-in animation
        indicator.setVisibility(View.VISIBLE);
        indicator.setAlpha(0f);
        indicator.setScaleX(0.3f);
        indicator.setScaleY(0.3f);
        indicator.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .start();

        // Switch to filled icon
        icon.setImageResource(filledIconRes);
        icon.setColorFilter(purpleColor);

        // Bounce icon animation
        icon.animate()
                .scaleX(1.25f)
                .scaleY(1.25f)
                .setDuration(150)
                .withEndAction(() ->
                        icon.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start()
                )
                .start();

        // Highlight label
        label.setTextColor(purpleColor);
        label.setAlpha(0f);
        label.animate().alpha(1f).setDuration(250).start();
    }

    /**
     * Reset all navigation items to unselected state
     */
    private void resetNavItems() {
        int grayColor = 0xFF9CA3AF;

        // Hide all pill indicators
        indicatorHome.setVisibility(View.INVISIBLE);
        indicatorModules.setVisibility(View.INVISIBLE);
        indicatorProgress.setVisibility(View.INVISIBLE);
        if (indicatorBadges != null) indicatorBadges.setVisibility(View.INVISIBLE);
        indicatorProfile.setVisibility(View.INVISIBLE);

        // Set all icons to outline versions
        iconHome.setImageResource(R.drawable.ic_home);
        iconModules.setImageResource(R.drawable.ic_book);
        iconProgress.setImageResource(R.drawable.ic_chart);
        if (iconBadges != null) iconBadges.setImageResource(R.drawable.ic_badge);
        iconProfile.setImageResource(R.drawable.ic_user);

        // Gray all icons
        iconHome.setColorFilter(grayColor);
        iconModules.setColorFilter(grayColor);
        iconProgress.setColorFilter(grayColor);
        if (iconBadges != null) iconBadges.setColorFilter(grayColor);
        iconProfile.setColorFilter(grayColor);

        // Gray all labels
        labelHome.setTextColor(grayColor);
        labelModules.setTextColor(grayColor);
        labelProgress.setTextColor(grayColor);
        if (labelBadges != null) labelBadges.setTextColor(grayColor);
        labelProfile.setTextColor(grayColor);
        labelHome.setAlpha(1f);
        labelModules.setAlpha(1f);
        labelProgress.setAlpha(1f);
        if (labelBadges != null) labelBadges.setAlpha(1f);
        labelProfile.setAlpha(1f);
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

        int streak = session.getStreak();
        tvStreak.setText(String.format("%d Day", streak));

        tvBadges.setText(String.format("%d Badges Earned", totalBadges));
        fetchRealBadgeCount();

        // Show the certificate banner if the user has completed the post-assessment
        if (cardCertificateBanner != null) {
            cardCertificateBanner.setVisibility(
                    session.hasCompletedPostAssessment() ? View.VISIBLE : View.GONE);
        }
    }



    private void fetchRealBadgeCount() {
        int studentId = session.getStudentId();
        if (studentId <= 0) return;
        ApiClient.getClient(this).create(ApiService.class)
                .getBadges(studentId)
                .enqueue(new Callback<BadgesResponse>() {
                    @Override
                    public void onResponse(Call<BadgesResponse> call,
                                           Response<BadgesResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            tvBadges.setText(response.body().getEarnedCount() + " Badges Earned");
                        }
                    }
                    @Override
                    public void onFailure(Call<BadgesResponse> call, Throwable t) { }
                });
    }

    /**
     * Load modules based on placement test results
     * Creates 5 Grade 3 English modules ordered by priority (weakest first)
     * Aligned with MATATAG English Curriculum Guide 2023
     */
    private void loadModulesFromPlacementResults() {
        // Calculate module priorities from placement test scores
        priorityManager.calculateModulePrioritiesFromPlacementTest();

        // Get ordered module names (lowest score = highest priority)
        List<String> orderedModuleNames = priorityManager.getOrderedModules();

        // Get placement level
        String placementLevel = session.getPlacementLevel();

        // Create LearningModule objects from ordered names
        modules = new ArrayList<>();
        String[] gradients = getModuleGradients();

        for (int i = 0; i < orderedModuleNames.size(); i++) {
            String moduleName = orderedModuleNames.get(i);
            int score = getScoreForModule(moduleName);
            double performanceScore = score / 100.0; // Convert 0-100 to 0-1

            LearningModule module = new LearningModule(
                    getModuleIdByName(moduleName),      // moduleId (actual DB ID, not position)
                    moduleName,                         // title
                    getModuleSubtitleByName(moduleName),// subtitle
                    getModuleDomainByName(moduleName),  // domain
                    performanceScore,                   // performanceScore (0-1)
                    gradients[i * 2],                   // gradientStart
                    gradients[i * 2 + 1]                // gradientEnd
            );
            module.setPriorityOrder(i + 1);
            module.setLocked(i != 0); // Only first module unlocked
            module.setTotalLessons(getLessonCountByName(moduleName));
            modules.add(module);
        }

        // Debug: Log module order
        android.util.Log.d("DashboardActivity", "Modules ordered by priority (weakest first):");
        for (int i = 0; i < modules.size(); i++) {
            android.util.Log.d("DashboardActivity", (i+1) + ". " + modules.get(i).getTitle() + " - Score: " + (int)(modules.get(i).getPerformanceScore() * 100) + "%");
        }

        // Update summary text
        tvModuleSummary.setText("Modules prioritized by your needs • Focus on areas where you need the most practice");

        // Setup adapter with click listener
        moduleAdapter = new ModuleAdapter(this, modules, module -> {
            openModule(module);
        });
        rvModules.setAdapter(moduleAdapter);

        // Force RecyclerView to recalculate height
        rvModules.post(() -> rvModules.requestLayout());
    }

    private int getScoreForModule(String moduleName) {
        switch (moduleName) {
            case "Phonics and Word Study":
                return session.getCategoryScore("Cat1_PhonicsWordStudy");
            case "Vocabulary and Word Knowledge":
                return session.getCategoryScore("Cat2_VocabularyWordKnowledge");
            case "Grammar Awareness and Grammatical Structures":
                return session.getCategoryScore("Cat3_GrammarAwareness");
            case "Comprehending and Analyzing Text":
                return session.getCategoryScore("Cat4_ComprehendingText");
            case "Creating and Composing Text":
                return session.getCategoryScore("Cat5_CreatingComposing");
            default:
                return 50; // default score
        }
    }

    private int getModuleIdByName(String moduleName) {
        if (moduleName == null) return 1;
        switch (moduleName) {
            case "Phonics and Word Study":                          return 1;
            case "Vocabulary and Word Knowledge":                   return 2;
            case "Grammar Awareness and Grammatical Structures":    return 3;
            case "Comprehending and Analyzing Text":                return 4;
            case "Creating and Composing Text":                     return 5;
            default:                                                return 1;
        }
    }

    private String getModuleDomainByName(String moduleName) {
        if (moduleName == null) return "General";
        if (moduleName.contains("Phonics"))       return "Phonics";
        if (moduleName.contains("Vocabulary"))    return "Vocabulary";
        if (moduleName.contains("Grammar"))       return "Grammar";
        if (moduleName.contains("Comprehend"))    return "Comprehending";
        if (moduleName.contains("Creating") || moduleName.contains("Composing")) return "Writing";
        return "General";
    }

    private String getModuleSubtitleByName(String moduleName) {
        if (moduleName == null) return "English Module";
        if (moduleName.contains("Phonics"))       return "Letter sounds and word patterns";
        if (moduleName.contains("Vocabulary"))    return "Building your word bank";
        if (moduleName.contains("Grammar"))       return "Sentence structure and rules";
        if (moduleName.contains("Comprehend"))    return "Understanding what you read";
        if (moduleName.contains("Creating") || moduleName.contains("Composing")) return "Express your ideas in writing";
        return "English Module";
    }

    private int getLessonCountByName(String moduleName) {
        if (moduleName == null) return 10;
        if (moduleName.contains("Phonics"))       return 12;
        if (moduleName.contains("Vocabulary"))    return 10;
        if (moduleName.contains("Grammar"))       return 8;
        if (moduleName.contains("Comprehend"))    return 10;
        if (moduleName.contains("Creating") || moduleName.contains("Composing")) return 8;
        return 10;
    }

    private String[] getModuleGradients() {
        return new String[]{
                "#FF6B6B", "#FF8E53", // Phonics - Red to Orange
                "#4ECDC4", "#44A08D", // Vocabulary - Teal to Green
                "#A770EF", "#CF57A3", // Grammar - Purple to Pink
                "#FFD93D", "#FFA93D", // Comprehension - Yellow to Orange
                "#667EEA", "#764BA2"  // Writing - Blue to Purple
        };
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
    private static final String TUTORIAL_KEY = "dashboard_tutorial";

    private void showTutorialIfFirstTime() {
        // Fast path: already seen locally
        android.content.SharedPreferences prefs = getSharedPreferences("LiteRisePrefs", MODE_PRIVATE);
        if (prefs.getBoolean("dashboard_tutorial_seen", false)) {
            return;
        }

        // Check server (authoritative source)
        int studentId = session.getStudentId();
        if (studentId > 0) {
            ApiClient.getClient(this).create(ApiService.class).checkTutorial(studentId, TUTORIAL_KEY)
                    .enqueue(new Callback<TutorialStatusResponse>() {
                        @Override
                        public void onResponse(Call<TutorialStatusResponse> call,
                                               Response<TutorialStatusResponse> response) {
                            boolean seen = response.isSuccessful()
                                    && response.body() != null
                                    && response.body().isSuccess()
                                    && response.body().isSeen();
                            if (seen) {
                                // Sync locally so we skip the API next time
                                prefs.edit().putBoolean("dashboard_tutorial_seen", true).apply();
                            } else {
                                showTutorialOverlay();
                            }
                        }

                        @Override
                        public void onFailure(Call<TutorialStatusResponse> call, Throwable t) {
                            // Offline fallback: show tutorial (will re-check next online session)
                            showTutorialOverlay();
                        }
                    });
        } else {
            showTutorialOverlay();
        }
    }

    private void showTutorialOverlay() {
        currentTutorialStep = 0;
        tutorialOverlay.setVisibility(View.VISIBLE);
        setupStepIndicators();
        showTutorialStep(0);
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

        // Mark seen locally (immediate)
        android.content.SharedPreferences prefs = getSharedPreferences("LiteRisePrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("dashboard_tutorial_seen", true).apply();

        // Persist to server (fire-and-forget)
        int studentId = session.getStudentId();
        if (studentId > 0) {
            ApiClient.getClient(this)
                    .create(ApiService.class)
                    .completeTutorial(new CompleteTutorialRequest(studentId, TUTORIAL_KEY))
                    .enqueue(new Callback<ResponseModel>() {
                        @Override
                        public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) { }
                        @Override
                        public void onFailure(Call<ResponseModel> call, Throwable t) { }
                    });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Refresh data when returning to dashboard
        loadUserData();
        loadModulesFromPlacementResults();

        // Always reset to Home tab when returning
        selectNavItem(0);

        // Check if all nodes complete → prompt post-assessment
        checkPostAssessmentTrigger();
    }

    private void checkPostAssessmentTrigger() {
        int studentId = session.getStudentId();
        if (studentId <= 0) return;

        ApiClient.getClient(this).create(ApiService.class).checkModulesComplete(studentId)
                .enqueue(new Callback<CheckModulesCompleteResponse>() {
                    @Override
                    public void onResponse(Call<CheckModulesCompleteResponse> call,
                                           Response<CheckModulesCompleteResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) return;

                        CheckModulesCompleteResponse body = response.body();

                        // Unlock modules sequentially based on how many nodes are completed.
                        // There are 5 modules with equal node counts (total_count / 5 nodes each).
                        // A module is fully complete when all its nodes are done, which unlocks the next module.
                        if (modules != null && moduleAdapter != null && body.getTotalCount() > 0) {
                            int nodesPerModule = body.getTotalCount() / modules.size();
                            if (nodesPerModule > 0) {
                                int completedModules = body.getCompletedCount() / nodesPerModule;
                                int remainingCompleted = body.getCompletedCount();
                                for (int i = 0; i < modules.size(); i++) {
                                    modules.get(i).setLocked(i > completedModules);
                                    int moduleCompleted = Math.min(remainingCompleted, nodesPerModule);
                                    modules.get(i).setCompletedLessons(Math.max(0, moduleCompleted));
                                    modules.get(i).setTotalLessons(nodesPerModule);
                                    remainingCompleted -= moduleCompleted;
                                }
                                moduleAdapter.notifyDataSetChanged();
                            }
                        }

                        if (body.isShouldTriggerPostAssessment()) {
                            showPostAssessmentDialog(body.getCompletedCount(), body.getTotalCount());
                        } else if (body.isPostAssessmentDone() && !session.hasCompletedPostAssessment()) {
                            // Post-assessment done on server but local data is missing —
                            // fetch the full result so the certificate has real values.
                            fetchAndSavePostAssessmentResult();
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckModulesCompleteResponse> call, Throwable t) {
                        // Silently fail — do not interrupt the user
                    }
                });
    }

    private void fetchAndSavePostAssessmentResult() {
        int studentId = session.getStudentId();
        if (studentId <= 0) return;

        ApiClient.getClient(this).create(ApiService.class).getPlacementProgress(studentId)
                .enqueue(new Callback<com.example.literise.models.PlacementProgressResponse>() {
                    @Override
                    public void onResponse(Call<com.example.literise.models.PlacementProgressResponse> call,
                                           Response<com.example.literise.models.PlacementProgressResponse> response) {
                        if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) return;

                        com.example.literise.models.PlacementProgressResponse body = response.body();
                        com.example.literise.models.PlacementProgressResponse.AssessmentResults results = body.getResults();
                        if (results == null) return;

                        com.example.literise.models.PlacementProgressResponse.AssessmentDetail post = results.getPost();
                        if (post == null) return;

                        // Persist the post-assessment result so the certificate has real values
                        session.savePostAssessmentResult(
                                post.getFinalTheta(),
                                post.getLevelName(),
                                post.getAccuracyPercentage());

                        // Also sync pre-theta if not already saved (needed for Ability Growth calc)
                        if (session.getPreTheta() == 0.0 && results.getPre() != null) {
                            session.savePreTheta(results.getPre().getFinalTheta());
                        }

                        if (cardCertificateBanner != null) {
                            cardCertificateBanner.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<com.example.literise.models.PlacementProgressResponse> call, Throwable t) {
                        // Silently fail
                    }
                });
    }

    private void showPostAssessmentDialog(int completed, int total) {
        new AlertDialog.Builder(this)
                .setTitle("All Lessons Complete!")
                .setMessage(String.format(
                        "Amazing work! You completed %d/%d lessons.\n\n"
                                + "You are ready for the Post-Assessment to see how much you have grown!",
                        completed, total))
                .setPositiveButton("Take Post-Assessment", (dialog, which) -> {
                    Intent intent = new Intent(this, PlacementIntroActivity.class);
                    intent.putExtra("assessment_type", "POST");
                    startActivity(intent);
                })
                .setNegativeButton("Later", null)
                .setCancelable(true)
                .show();
    }



    @SuppressWarnings("deprecation")

    @Override

    public void onBackPressed() {

        // Stay on dashboard, don't go back

        moveTaskToBack(true);

    }

}