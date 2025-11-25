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
import com.example.literise.database.SessionManager;
import com.example.literise.models.LessonProgressResponse;
import com.example.literise.utils.CustomToast;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvHeaderXP, tvStreak, tvBadges, tvWelcome;
    private ImageView ivSettings, ivTrophy;
    private com.google.android.material.button.MaterialButton btnContinueLesson;
    private LinearLayout lessonListContainer;
    private SessionManager session;

    private static final int TOTAL_LESSONS = 6;
    private List<LessonProgressResponse.LessonProgress> lessonProgressList = new ArrayList<>();
    private int currentStreak = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new SessionManager(this);

        initializeViews();
        loadUserData();
        loadLessonProgress();
        setupListeners();
    }

    private void initializeViews() {
        tvHeaderXP = findViewById(R.id.tvHeaderXP);
        tvStreak = findViewById(R.id.tvStreak);
        tvBadges = findViewById(R.id.tvBadges);
        tvWelcome = findViewById(R.id.tvWelcome);
        ivSettings = findViewById(R.id.ivSettings);
        ivTrophy = findViewById(R.id.ivTrophy);
        btnContinueLesson = findViewById(R.id.btnContinueLesson);
        lessonListContainer = findViewById(R.id.lessonListContainer);
    }

    private void setupListeners() {
        ivSettings.setOnClickListener(v -> openSettings());
        ivTrophy.setOnClickListener(v -> openAchievements());
        btnContinueLesson.setOnClickListener(v -> continueLesson());
    }

    private void loadUserData() {
        String fullName = session.getFullName();
        int xp = session.getXP();

        if (fullName != null && !fullName.isEmpty()) {
            tvWelcome.setText(String.format("Welcome back,\n%s!", fullName));
        }

        tvHeaderXP.setText(String.format("%d XP", xp));
        tvStreak.setText(String.format("%d-Day Streak", currentStreak));
        tvBadges.setText(String.format("%d Badges", 0));
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
            // Lesson is only complete if status is "Completed" AND progress is 100%
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
