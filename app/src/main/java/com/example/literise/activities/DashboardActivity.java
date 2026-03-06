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

import com.example.literise.activities.games.SynonymSprintActivity;
import com.example.literise.activities.games.WordExplosionActivity;
import com.example.literise.adapters.ModuleAdapter;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
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

    private RecyclerView rvModules;
    private ModuleAdapter moduleAdapter;
    private List<LearningModule> modules;

    // Game testing buttons
    private MaterialButton btnTestPhonicsNinja, btnTestSynonymSprint, btnTestWordExplosion;
    private MaterialButton btnTestSentenceChef, btnTestWordHunt, btnTestSentenceScramble;

    // Custom Bottom Navigation Views
    private LinearLayout navHome, navModules, navProgress, navProfile;
    private ImageView iconHome, iconModules, iconProgress, iconProfile;
    private TextView labelHome, labelModules, labelProgress, labelProfile;
    private View indicatorHome, indicatorModules, indicatorProgress, indicatorProfile;

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

        //btnContinueLesson = findViewById(R.id.btnContinueLesson);

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

        // Nav pill indicators
        indicatorHome = findViewById(R.id.indicatorHome);
        indicatorModules = findViewById(R.id.indicatorModules);
        indicatorProgress = findViewById(R.id.indicatorProgress);
        indicatorProfile = findViewById(R.id.indicatorProfile);

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

        // Game testing buttons
        btnTestPhonicsNinja = findViewById(R.id.btnTestPhonicsNinja);
        btnTestSynonymSprint = findViewById(R.id.btnTestSynonymSprint);
        btnTestWordExplosion = findViewById(R.id.btnTestWordExplosion);
        btnTestSentenceChef = findViewById(R.id.btnTestSentenceChef);
        btnTestWordHunt = findViewById(R.id.btnTestWordHunt);
        btnTestSentenceScramble = findViewById(R.id.btnTestSentenceScramble);

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

        // Game testing button listeners
        setupGameTestListeners();

    }

    /**
     * Setup game testing button listeners
     */
    private void setupGameTestListeners() {
        // Phonics Ninja - ACTIVE
       /* btnTestPhonicsNinja.setOnClickListener(v -> {
            Intent intent = new Intent(this, PhonicsNinjaActivity.class);
            intent.putExtra("node_id", 1);
            intent.putExtra("student_id", session.getStudentId());
            intent.putExtra("target_pattern", "CVCC");
            startActivity(intent);
        });*/

        btnTestSynonymSprint.setOnClickListener(v -> {
            Intent intent = new Intent(this, SynonymSprintActivity.class);
            intent.putExtra("node_id", 1);
            intent.putExtra("student_id", session.getStudentId());
            intent.putExtra("target_word", "happy");
            startActivity(intent);
        });

        // Word Explosion - Coming soon
        // Word Explosion - ACTIVE
        btnTestWordExplosion.setOnClickListener(v -> {
            Intent intent = new Intent(this, WordExplosionActivity.class);
            intent.putExtra("node_id", 1);
            intent.putExtra("student_id", session.getStudentId());
            startActivity(intent);
        });


        // Sentence Chef - Coming soon
        btnTestSentenceChef.setOnClickListener(v -> {
            Toast.makeText(this, "👨‍🍳 Sentence Chef - Coming Soon!", Toast.LENGTH_SHORT).show();
        });

        // Word Hunt - If exists
        btnTestWordHunt.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, Class.forName("com.example.literise.activities.WordHuntActivity"));
                intent.putExtra("node_id", 1);
                intent.putExtra("student_id", session.getStudentId());
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "🔍 Word Hunt - Activity not found", Toast.LENGTH_SHORT).show();
            }
        });

        // Sentence Scramble - If exists
        btnTestSentenceScramble.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(this, Class.forName("com.example.literise.activities.SentenceScrambleActivity"));
                intent.putExtra("node_id", 1);
                intent.putExtra("student_id", session.getStudentId());
                startActivity(intent);
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "🔀 Sentence Scramble - Activity not found", Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Handle navigation item selection with Material 3 style animation
     */
    private void selectNavItem(int position) {
        // Reset all items
        resetNavItems();

        // Activate selected item
        switch (position) {
            case 0: // Home
                activateNavItem(iconHome, labelHome, indicatorHome, R.drawable.ic_home_filled);
                loadModulesFromPlacementResults();
                break;

            case 1: // Modules
                activateNavItem(iconModules, labelModules, indicatorModules, R.drawable.ic_book_filled);
                Toast.makeText(this, "Modules - Coming Soon!", Toast.LENGTH_SHORT).show();
                break;

            case 2: // Progress
                activateNavItem(iconProgress, labelProgress, indicatorProgress, R.drawable.ic_chart_filled);
                startActivity(new Intent(this, ComparisonReportActivity.class));
                break;

            case 3: // Profile
                activateNavItem(iconProfile, labelProfile, indicatorProfile, R.drawable.ic_user_filled);
                openSettings();
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
        indicatorProfile.setVisibility(View.INVISIBLE);

        // Set all icons to outline versions
        iconHome.setImageResource(R.drawable.ic_home);
        iconModules.setImageResource(R.drawable.ic_book);
        iconProgress.setImageResource(R.drawable.ic_chart);
        iconProfile.setImageResource(R.drawable.ic_user);

        // Gray all icons
        iconHome.setColorFilter(grayColor);
        iconModules.setColorFilter(grayColor);
        iconProgress.setColorFilter(grayColor);
        iconProfile.setColorFilter(grayColor);

        // Gray all labels
        labelHome.setTextColor(grayColor);
        labelModules.setTextColor(grayColor);
        labelProgress.setTextColor(grayColor);
        labelProfile.setTextColor(grayColor);
        labelHome.setAlpha(1f);
        labelModules.setAlpha(1f);
        labelProgress.setAlpha(1f);
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

        tvStreak.setText(String.format("%d Day", currentStreak));

        tvBadges.setText(String.format("%d Badges Earned", totalBadges));

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
                    i + 1,                        // moduleId
                    moduleName,                   // title
                    getModuleSubtitle(i),         // subtitle
                    getModuleDomain(i),           // domain
                    performanceScore,             // performanceScore (0-1)
                    gradients[i * 2],             // gradientStart
                    gradients[i * 2 + 1]          // gradientEnd
            );
            module.setPriorityOrder(i + 1);
            module.setLocked(i != 0); // Only first module unlocked
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

    private String getModuleDomain(int index) {
        String[] domains = {"Phonics", "Vocabulary", "Grammar", "Comprehension", "Writing"};
        return index < domains.length ? domains[index] : "General";
    }

    private String getModuleSubtitle(int index) {
        String[] subtitles = {
                "Letter sounds and word patterns",
                "Building your word bank",
                "Sentence structure and rules",
                "Understanding what you read",
                "Express your ideas in writing"
        };
        return index < subtitles.length ? subtitles[index] : "English Module";
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
                        if (response.isSuccessful()
                                && response.body() != null
                                && response.body().isSuccess()
                                && response.body().isShouldTriggerPostAssessment()) {
                            showPostAssessmentDialog(
                                    response.body().getCompletedCount(),
                                    response.body().getTotalCount()
                            );
                        }
                    }

                    @Override
                    public void onFailure(Call<CheckModulesCompleteResponse> call, Throwable t) {
                        // Silently fail — do not interrupt the user
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