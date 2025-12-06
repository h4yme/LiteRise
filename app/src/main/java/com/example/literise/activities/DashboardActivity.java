package com.example.literise.activities;



import android.content.Intent;

import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;

import android.widget.ImageView;

import android.widget.LinearLayout;

import android.widget.TextView;



import androidx.appcompat.app.AppCompatActivity;



import com.example.literise.R;

import com.example.literise.api.ApiClient;

import com.example.literise.api.ApiService;

import com.example.literise.utils.ModulePriorityManager;

import com.example.literise.database.SessionManager;

import com.example.literise.models.LessonProgressResponse;

import com.example.literise.utils.CustomToast;

import com.google.android.material.progressindicator.CircularProgressIndicator;



import java.util.ArrayList;

import java.util.List;



import retrofit2.Call;

import retrofit2.Callback;

import retrofit2.Response;



public class DashboardActivity extends BaseActivity {



    private TextView tvHeaderXP, tvStreak, tvWelcome, tvMotivation;

    private ImageView ivLeoMascot;

    private com.google.android.material.button.MaterialButton btnContinueLesson;

    private android.widget.GridLayout gridModules;

    private SessionManager session;

    private ModulePriorityManager priorityManager;



    private static final int TOTAL_LESSONS = 6;

    private List<LessonProgressResponse.LessonProgress> lessonProgressList = new ArrayList<>();

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

        // Leo mascot - show encouraging message when tapped

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



    private void loadLessonProgress() {

        int studentId = session.getStudentId();

        if (studentId <= 0) {

            populateLessonListWithDefaults();

            return;

        }



        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        apiService.getLessonProgress(studentId).enqueue(new Callback<LessonProgressResponse>() {

            @Override

            public void onResponse(Call<LessonProgressResponse> call, Response<LessonProgressResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {

                    LessonProgressResponse data = response.body();



                    // Update student stats

                    if (data.getStudent() != null) {

                        int totalXP = data.getStudent().getTotalXP();

                        currentStreak = data.getStudent().getCurrentStreak();



                        session.updateTotalXP(totalXP);

                        tvHeaderXP.setText(String.format("%d XP", totalXP));

                        tvStreak.setText(String.format("%d-Day Streak", currentStreak));

                    }



                    // Update lesson progress

                    if (data.getLessons() != null) {

                        lessonProgressList = data.getLessons();

                    }



                    populateLessonList();

                } else {

                    populateLessonListWithDefaults();

                }

            }



            @Override

            public void onFailure(Call<LessonProgressResponse> call, Throwable t) {

                android.util.Log.e("Dashboard", "Failed to load progress: " + t.getMessage());

                populateLessonListWithDefaults();

            }

        });

    }



    private void populateLessonListWithDefaults() {

        lessonProgressList.clear();

        for (int i = 1; i <= TOTAL_LESSONS; i++) {

            // Create default progress objects

            lessonProgressList.add(null);

        }

        populateLessonList();

    }



    private void populateLessonList() {

        lessonListContainer.removeAllViews();



        for (int i = 1; i <= TOTAL_LESSONS; i++) {

            LessonProgressResponse.LessonProgress progress = null;

            if (i <= lessonProgressList.size()) {

                progress = lessonProgressList.get(i - 1);

            }

            View lessonItem = createLessonItem(i, progress);

            lessonListContainer.addView(lessonItem);

        }

    }



    private View createLessonItem(int lessonNumber, LessonProgressResponse.LessonProgress progress) {

        View itemView = LayoutInflater.from(this).inflate(R.layout.item_lesson, lessonListContainer, false);



        TextView tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);

        ImageView ivLock = itemView.findViewById(R.id.ivLock);

        CircularProgressIndicator progressCircle = itemView.findViewById(R.id.progressCircle);

        TextView tvProgress = itemView.findViewById(R.id.tvProgress);

        View circleBackground = itemView.findViewById(R.id.circleBackground);



        tvLessonTitle.setText(String.format("Lesson %d", lessonNumber));



        int progressPercent = 0;

        boolean isCompleted = false;

        boolean hasProgress = false;



        if (progress != null) {

            progressPercent = progress.getProgressPercent();

            isCompleted = progress.isCompleted() && progressPercent >= 100;

            hasProgress = progressPercent > 0 || progress.getGamesPlayed() > 0;

        }



        // Determine lesson state based on progress

        boolean isUnlocked = lessonNumber <= 2 || isPreviousLessonCompleted(lessonNumber);



        if (isCompleted) {

            // Completed lesson - show 100%

            circleBackground.setVisibility(View.GONE);

            progressCircle.setVisibility(View.VISIBLE);

            progressCircle.setProgress(100);

            tvProgress.setVisibility(View.VISIBLE);

            tvProgress.setText("✓");

            ivLock.setImageResource(R.drawable.ic_lock_open);

            ivLock.setColorFilter(getResources().getColor(R.color.color_success, null));

            itemView.setOnClickListener(v -> startLesson(getLessonType(lessonNumber), lessonNumber));



        } else if (hasProgress) {

            // In progress lesson

            circleBackground.setVisibility(View.GONE);

            progressCircle.setVisibility(View.VISIBLE);

            progressCircle.setProgress(progressPercent);

            tvProgress.setVisibility(View.VISIBLE);

            tvProgress.setText(String.valueOf(progressPercent));

            ivLock.setImageResource(R.drawable.ic_lock_open);

            itemView.setOnClickListener(v -> startLesson(getLessonType(lessonNumber), lessonNumber));



        } else if (isUnlocked) {

            // Unlocked but not started

            ivLock.setImageResource(R.drawable.ic_lock_open);

            ivLock.setColorFilter(getResources().getColor(R.color.gray_medium, null));

            itemView.setOnClickListener(v -> startLesson(getLessonType(lessonNumber), lessonNumber));



        } else {

            // Locked lesson

            ivLock.setImageResource(R.drawable.ic_lock);

            ivLock.setColorFilter(getResources().getColor(R.color.gray_medium, null));

            itemView.setAlpha(0.6f);

            itemView.setEnabled(false);

        }



        return itemView;

    }



    private boolean isPreviousLessonCompleted(int lessonNumber) {

        if (lessonNumber <= 1) return true;

        if (lessonNumber - 2 < lessonProgressList.size()) {

            LessonProgressResponse.LessonProgress prev = lessonProgressList.get(lessonNumber - 2);

            return prev != null && prev.isCompleted();

        }

        return false;

    }



    private String getLessonType(int lessonNumber) {

        switch (lessonNumber) {

            case 1: return "reading";

            case 2: return "vocabulary";

            case 3: return "grammar";

            case 4: return "comprehension";

            case 5: return "fluency";

            case 6: return "review";

            default: return "reading";

        }

    }



    private void continueLesson() {

        // Find the first incomplete lesson

        for (int i = 0; i < lessonProgressList.size(); i++) {

            LessonProgressResponse.LessonProgress progress = lessonProgressList.get(i);

            if (progress == null || !progress.isCompleted()) {

                startLesson(getLessonType(i + 1), i + 1);

                return;

            }

        }

        // All lessons complete, start lesson 1

        startLesson("reading", 1);

    }



    private void startLesson(String lessonType, int lessonId) {

        Intent intent = new Intent(this, LessonActivity.class);

        intent.putExtra("lesson_type", lessonType);

        intent.putExtra("lesson_id", lessonId);

        startActivity(intent);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }



    private void openSettings() {

        new androidx.appcompat.app.AlertDialog.Builder(this)

                .setTitle("Settings")

                .setMessage("Do you want to logout?")

                .setPositiveButton("Logout", (dialog, which) -> logout())

                .setNegativeButton("Cancel", null)

                .show();

    }



    private void logout() {

        session.logout();

        CustomToast.showSuccess(this, "Logged out successfully");



        Intent intent = new Intent(this, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        finish();

    }



    private void openAchievements() {

        // TODO: Navigate to achievements/badges screen

    }



    @Override

    protected void onResume() {

        super.onResume();

        // Refresh data when returning to dashboard

        loadUserData();

        loadLessonProgress();

    }



    @SuppressWarnings("deprecation")

    @Override

    public void onBackPressed() {

        moveTaskToBack(true);

    }

}