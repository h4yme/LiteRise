package com.example.literise.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.SavePlacementResultRequest;
import com.example.literise.models.SavePlacementResultResponse;
import com.example.literise.utils.SessionLogger;
import com.example.literise.views.LeoDialogueView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlacementResultActivity extends AppCompatActivity {

    private static final String TAG = "PlacementResultActivity";

    private ConstraintLayout rootLayout;
    private TextView tvLevelName, tvLevelNumber;
    private TextView tvAccuracy, tvQuestionsAnswered;
    private TextView tvCategory1Score, tvCategory2Score, tvCategory3Score, tvCategory4Score, tvCategory5Score;
    private MaterialButton btnContinueToDashboard;

    // Placement results data
    private int placementLevel;
    private String levelName;
    private double accuracy;
    private int totalAnswered;
    private int totalCorrect;
    private int[] categoryScores;
    private double finalTheta;
    private long startTime;

    // Session and API
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_result);

        sessionManager = new SessionManager(this);
        startTime = System.currentTimeMillis();

        initViews();
        getResultsFromIntent();
        displayResults();
        savePlacementResult();

        // Leo's congratulation will be shown on Dashboard instead
        // Results screen should be clean and focused on the assessment results

        setupListeners();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        tvLevelName = findViewById(R.id.tvLevelName);
        tvLevelNumber = findViewById(R.id.tvLevelNumber);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvQuestionsAnswered = findViewById(R.id.tvQuestionsAnswered);
        tvCategory1Score = findViewById(R.id.tvCategory1Score);
        tvCategory2Score = findViewById(R.id.tvCategory2Score);
        tvCategory3Score = findViewById(R.id.tvCategory3Score);
        tvCategory4Score = findViewById(R.id.tvCategory4Score);
        tvCategory5Score = findViewById(R.id.tvCategory5Score);
        btnContinueToDashboard = findViewById(R.id.btnContinueToDashboard);
    }

    private void getResultsFromIntent() {
        Intent intent = getIntent();
        placementLevel = intent.getIntExtra("placement_level", 3);
        levelName = intent.getStringExtra("level_name");
        accuracy = intent.getDoubleExtra("accuracy", 0.0);
        totalAnswered = intent.getIntExtra("total_answered", 0);
        totalCorrect = intent.getIntExtra("total_correct", 0);
        categoryScores = intent.getIntArrayExtra("category_scores");
        finalTheta = intent.getDoubleExtra("final_theta", 0.0);
        long passedStartTime = intent.getLongExtra("start_time", 0);
        if (passedStartTime > 0) {
            startTime = passedStartTime;
        }

        // Default values if not provided
        if (levelName == null || levelName.isEmpty()) {
            levelName = getLevelNameFromLevel(placementLevel);
        }
        if (categoryScores == null) {
            categoryScores = new int[]{0, 0, 0, 0, 0};
        }
    }

    private String getLevelNameFromLevel(int level) {
        switch (level) {
            case 1: return "Beginner";
            case 2: return "Intermediate";
            case 3: return "Advanced";
            default: return "Intermediate";
        }
    }

    private void displayResults() {
        // Display reading level
        tvLevelName.setText(levelName);
        tvLevelNumber.setText("Level " + placementLevel);

        // Display accuracy
        tvAccuracy.setText(String.format("%.0f%%", accuracy));

        // Display questions answered
        tvQuestionsAnswered.setText(totalAnswered + "/" + 25);

        // Display category scores
        if (categoryScores.length >= 5) {
            tvCategory1Score.setText(categoryScores[0] + "%");
            tvCategory2Score.setText(categoryScores[1] + "%");
            tvCategory3Score.setText(categoryScores[2] + "%");
            tvCategory4Score.setText(categoryScores[3] + "%");
            tvCategory5Score.setText(categoryScores[4] + "%");
        }
    }

    private void showLeoCongratulation() {
        List<LeoDialogueView.DialogueMessage> messages = new ArrayList<>();

        // Congratulation message based on level
        String congratsMessage = getCongratsMessage(placementLevel);
        messages.add(new LeoDialogueView.DialogueMessage(congratsMessage));

        // Encouraging message
        String encouragementMessage = getEncouragementMessage(placementLevel);
        messages.add(new LeoDialogueView.DialogueMessage(encouragementMessage));

        // Next steps message
        messages.add(new LeoDialogueView.DialogueMessage(
                "Now, let's start your reading adventure!\n\n" +
                        "I've picked the perfect stories for your level. " +
                        "You'll get better and better with each story you read! 📚✨"
        ));

        // Create and show dialogue view
        LeoDialogueView dialogueView = new LeoDialogueView(this);
        dialogueView.setDialogueMessages(messages);
        dialogueView.setDialogueCompleteListener(() -> {
            // Dialogue complete - user can now proceed
        });
        dialogueView.show((ViewGroup) rootLayout);
    }

    private String getCongratsMessage(int level) {
        switch (level) {
            case 1:
                return "🌟 Wonderful job!\n\n" +
                        "You're a Beginner Reader! " +
                        "You're just starting your reading journey, and that's amazing! " +
                        "Every great reader starts right where you are! 🎉";
            case 2:
                return "🌟 Fantastic work!\n\n" +
                        "You're an Early Reader! " +
                        "You're building your reading skills beautifully! " +
                        "Keep up the great work! 🎉";
            case 3:
                return "🌟 Excellent job!\n\n" +
                        "You're a Developing Reader! " +
                        "You're making wonderful progress in your reading! " +
                        "You're doing so well! 🎉";
            case 4:
                return "🌟 Outstanding!\n\n" +
                        "You're a Fluent Reader! " +
                        "Wow! You're reading really well! " +
                        "I'm so proud of you! 🎉";
            default:
                return "🌟 Great job completing the test! 🎉";
        }
    }

    private String getEncouragementMessage(int level) {
        switch (level) {
            case 1:
                return "I'll be with you every step of the way! 💪\n\n" +
                        "We'll start with fun, simple stories that are just right for you. " +
                        "Remember, every reader was once a beginner!";
            case 2:
                return "You're growing as a reader! 💪\n\n" +
                        "I've selected stories that will challenge you just the right amount. " +
                        "Keep reading, and you'll be amazed at how much you'll learn!";
            case 3:
                return "You're becoming a strong reader! 💪\n\n" +
                        "The stories I've picked will help you become even better! " +
                        "You're on your way to becoming a fluent reader!";
            case 4:
                return "You're an amazing reader! 💪\n\n" +
                        "I've found exciting stories that will keep challenging you! " +
                        "Keep reading and exploring new books!";
            default:
                return "Let's keep learning together! 💪";
        }
    }

    private void setupListeners() {
        btnContinueToDashboard.setOnClickListener(v -> {
            // Navigate to Dashboard
            Intent intent = new Intent(PlacementResultActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }

    private String getAppVersion() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }

    private void savePlacementResult() {
        int studentId = sessionManager.getStudentId();
        if (studentId == 0) {
            Log.w(TAG, "Cannot save placement result: No student ID found");
            return;
        }

        // Determine assessment type based on whether student has completed assessment before
        String assessmentType = sessionManager.hasCompletedAssessment() ? "PostAssessment" : "PreAssessment";

        // Skip API call in demo mode, but still update local session manager
        if (com.example.literise.utils.AppConfig.DEMO_MODE) {
            Log.d(TAG, "Demo mode: Skipping API save, updating local session only");
            if ("PreAssessment".equals(assessmentType)) {
                sessionManager.setAssessmentCompleted(true);
                sessionManager.setAssessmentStarted(false); // Clear started flag
            }
            return;
        }

        // Generate session ID based on timestamp
        int sessionId = (int) (System.currentTimeMillis() / 1000);

        // Calculate time spent in seconds
        int timeSpentSeconds = (int) ((System.currentTimeMillis() - startTime) / 1000);

        // Build device info
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL + ", Android " + Build.VERSION.RELEASE;

        // Create request
        SavePlacementResultRequest request = new SavePlacementResultRequest(
                studentId,
                sessionId,
                assessmentType,
                finalTheta,
                placementLevel,
                levelName,
                totalAnswered,
                totalCorrect,
                accuracy
        );

        // Set category scores
        for (int i = 0; i < categoryScores.length && i < 5; i++) {
            request.setCategoryScore(i + 1, categoryScores[i]);
        }

        // Set optional fields
        request.setTimeSpentSeconds(timeSpentSeconds);
        request.setDeviceInfo(deviceInfo);
        request.setAppVersion(getAppVersion());

        // Make API call
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        Call<SavePlacementResultResponse> call = apiService.savePlacementResult(request);

        call.enqueue(new Callback<SavePlacementResultResponse>() {
            @Override
            public void onResponse(Call<SavePlacementResultResponse> call, Response<SavePlacementResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SavePlacementResultResponse result = response.body();
                    Log.d(TAG, "Placement result saved successfully: " + result.getMessage());

                    // Mark assessment as completed if this was PreAssessment
                    if ("PreAssessment".equals(assessmentType)) {
                        sessionManager.setAssessmentCompleted(true);
                        sessionManager.setAssessmentStarted(false); // Clear started flag
                    }

                    // Log assessment completion
                    SessionLogger.logAssessmentComplete(PlacementResultActivity.this, studentId, assessmentType);
                } else {
                    Log.e(TAG, "Failed to save placement result: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SavePlacementResultResponse> call, Throwable t) {
                Log.e(TAG, "Error saving placement result: " + t.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Don't allow back - user must continue to dashboard
    }
}