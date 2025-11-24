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
import com.example.literise.database.SessionManager;
import com.example.literise.utils.CustomToast;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvHeaderXP, tvStreak, tvBadges, tvWelcome;
    private ImageView ivSettings, ivTrophy;
    private com.google.android.material.button.MaterialButton btnContinueLesson;
    private LinearLayout lessonListContainer;
    private SessionManager session;

    private static final int TOTAL_LESSONS = 6;
    private int currentLessonProgress = 65;
    private String lastLessonType = "reading";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        session = new SessionManager(this);

        initializeViews();
        loadUserData();
        populateLessonList();
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

        int streakDays = calculateStreak();
        tvStreak.setText(String.format("%d-Day Streak", streakDays));

        int badgeCount = calculateBadges();
        tvBadges.setText(String.format("%d Badges", badgeCount));
    }

    private void populateLessonList() {
        lessonListContainer.removeAllViews();

        for (int i = 1; i <= TOTAL_LESSONS; i++) {
            View lessonItem = createLessonItem(i);
            lessonListContainer.addView(lessonItem);
        }
    }

    private View createLessonItem(int lessonNumber) {
        View itemView = LayoutInflater.from(this).inflate(R.layout.item_lesson, lessonListContainer, false);

        TextView tvLessonTitle = itemView.findViewById(R.id.tvLessonTitle);
        ImageView ivLock = itemView.findViewById(R.id.ivLock);
        CircularProgressIndicator progressCircle = itemView.findViewById(R.id.progressCircle);
        TextView tvProgress = itemView.findViewById(R.id.tvProgress);
        View circleBackground = itemView.findViewById(R.id.circleBackground);

        tvLessonTitle.setText(String.format("Lesson %d", lessonNumber));

        if (lessonNumber == 1) {
            circleBackground.setVisibility(View.GONE);
            progressCircle.setVisibility(View.VISIBLE);
            progressCircle.setProgress(currentLessonProgress);
            tvProgress.setVisibility(View.VISIBLE);
            tvProgress.setText(String.valueOf(currentLessonProgress));

            ivLock.setImageResource(R.drawable.ic_lock_open);

            itemView.setOnClickListener(v -> startLesson(lastLessonType, 1));

        } else if (lessonNumber == 2) {
            ivLock.setImageResource(R.drawable.ic_lock);
            ivLock.setColorFilter(getResources().getColor(R.color.gray_medium, null));

            itemView.setOnClickListener(v -> startLesson("vocabulary", 2));

        } else {
            ivLock.setImageResource(R.drawable.ic_lock);
            ivLock.setColorFilter(getResources().getColor(R.color.gray_medium, null));

            itemView.setAlpha(0.6f);
            itemView.setEnabled(false);
        }

        return itemView;
    }

    private void continueLesson() {
        startLesson(lastLessonType, 1);
    }

    private void startLesson(String lessonType, int lessonId) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("lesson_type", lessonType);
        intent.putExtra("lesson_id", lessonId);
        startActivity(intent);  // THIS LINE WAS MISSING IN YOUR CODE!
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private int calculateStreak() {
        return 10;
    }

    private int calculateBadges() {
        return 7;
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
        loadUserData();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}