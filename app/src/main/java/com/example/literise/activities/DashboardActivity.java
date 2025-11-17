package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvHeaderXP, tvStreak, tvBadges, tvWelcome;
    private ImageView ivSettings, ivTrophy;
    private com.google.android.material.button.MaterialButton btnContinueLesson;
    private LinearLayout lessonListContainer;
    private SessionManager session;

    // Sample lesson data
    private static final int TOTAL_LESSONS = 6;
    private int currentLessonProgress = 65; // Lesson 1 is 65% complete
    private String lastLessonType = "reading"; // Last accessed lesson

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
        // Get user data from session
        String fullName = session.getFullName();
        int xp = session.getXP();

        // Set welcome message
        if (fullName != null && !fullName.isEmpty()) {
            tvWelcome.setText(String.format("Welcome back,\n%s!", fullName));
        }

        // Set XP
        tvHeaderXP.setText(String.format("%d XP", xp));

        // Calculate streak (placeholder - should come from database)
        int streakDays = calculateStreak();
        tvStreak.setText(String.format("%d-Day Streak", streakDays));

        // Calculate badges (placeholder - should come from database)
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
            // Lesson 1 is in progress
            circleBackground.setVisibility(View.GONE);
            progressCircle.setVisibility(View.VISIBLE);
            progressCircle.setProgress(currentLessonProgress);
            tvProgress.setVisibility(View.VISIBLE);
            tvProgress.setText(String.valueOf(currentLessonProgress));

            // Unlocked
            ivLock.setImageResource(R.drawable.ic_lock_open);

            // Clickable
            itemView.setOnClickListener(v -> startLesson(lastLessonType));

        } else if (lessonNumber == 2) {
            // Lesson 2 is unlocked but not started
            ivLock.setImageResource(R.drawable.ic_lock);
            ivLock.setColorFilter(getResources().getColor(R.color.gray_medium, null));

            // Make clickable
            itemView.setOnClickListener(v -> startLesson("vocabulary"));

        } else {
            // Lessons 3-6 are locked
            ivLock.setImageResource(R.drawable.ic_lock);
            ivLock.setColorFilter(getResources().getColor(R.color.gray_medium, null));

            // Make less prominent
            itemView.setAlpha(0.6f);
            itemView.setEnabled(false);
        }

        return itemView;
    }

    private void continueLesson() {
        // Continue the last accessed lesson (Lesson 1)
        startLesson(lastLessonType);
    }

    private void startLesson(String lessonType) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("lesson_type", lessonType);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    private int calculateStreak() {
        // TODO: Calculate from database
        // For now, return placeholder
        return 10;
    }

    private int calculateBadges() {
        // TODO: Calculate from database
        // For now, return placeholder
        return 7;
    }

    private void openSettings() {
        // TODO: Navigate to settings
    }

    private void openAchievements() {
        // TODO: Navigate to achievements/badges screen
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to dashboard
        loadUserData();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to assessment
        // User should log out from settings instead
        moveTaskToBack(true);
    }
}
