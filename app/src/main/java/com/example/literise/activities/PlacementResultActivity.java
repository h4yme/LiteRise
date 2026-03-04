package com.example.literise.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
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

    // Leo + header Lottie
    private ImageView ivLeoResult;
    private LottieAnimationView lottieStarsBg;
    private LottieAnimationView lottieConfetti;
    private LottieAnimationView lottieBadgeSparkle;

    // Content section Lottie
    private LottieAnimationView lottieResultsSparkle;
    private LottieAnimationView lottieCategoryAccent;

    // Text
    private TextView tvCongrats, tvLevelName, tvLevelNumber;
    private TextView tvAccuracy, tvQuestionsAnswered;
    private TextView tvCategory1Score, tvCategory2Score, tvCategory3Score, tvCategory4Score, tvCategory5Score;

    // Progress bars
    private ProgressBar pbCategory1, pbCategory2, pbCategory3, pbCategory4, pbCategory5;

    // Animated rows + container
    private View rowCategory1, rowCategory2, rowCategory3, rowCategory4, rowCategory5;
    private View contentSection;
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
        startEntranceAnimations();
        setupListeners();
    }

    private void initViews() {
        ivLeoResult = findViewById(R.id.ivLeoResult);
        contentSection = findViewById(R.id.contentSection);

        // Lottie views
        lottieStarsBg = findViewById(R.id.lottieStarsBg);
        lottieConfetti = findViewById(R.id.lottieConfetti);
        lottieBadgeSparkle = findViewById(R.id.lottieBadgeSparkle);
        lottieResultsSparkle = findViewById(R.id.lottieResultsSparkle);
        lottieCategoryAccent = findViewById(R.id.lottieCategoryAccent);

        // Text views
        tvCongrats = findViewById(R.id.tvCongrats);
        tvLevelName = findViewById(R.id.tvLevelName);
        tvLevelNumber = findViewById(R.id.tvLevelNumber);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvQuestionsAnswered = findViewById(R.id.tvQuestionsAnswered);
        tvCategory1Score = findViewById(R.id.tvCategory1Score);
        tvCategory2Score = findViewById(R.id.tvCategory2Score);
        tvCategory3Score = findViewById(R.id.tvCategory3Score);
        tvCategory4Score = findViewById(R.id.tvCategory4Score);
        tvCategory5Score = findViewById(R.id.tvCategory5Score);

        // Progress bars
        pbCategory1 = findViewById(R.id.pbCategory1);
        pbCategory2 = findViewById(R.id.pbCategory2);
        pbCategory3 = findViewById(R.id.pbCategory3);
        pbCategory4 = findViewById(R.id.pbCategory4);
        pbCategory5 = findViewById(R.id.pbCategory5);

        // Category rows for stagger animation
        rowCategory1 = findViewById(R.id.rowCategory1);
        rowCategory2 = findViewById(R.id.rowCategory2);
        rowCategory3 = findViewById(R.id.rowCategory3);
        rowCategory4 = findViewById(R.id.rowCategory4);
        rowCategory5 = findViewById(R.id.rowCategory5);

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
        if (passedStartTime > 0) startTime = passedStartTime;

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
        tvLevelName.setText(levelName);
        tvLevelNumber.setText("Level " + placementLevel);
        tvAccuracy.setText(String.format("%.0f%%", accuracy));
        tvQuestionsAnswered.setText(totalAnswered + "/" + 25);

        if (categoryScores.length >= 5) {
            tvCategory1Score.setText(categoryScores[0] + "%");
            tvCategory2Score.setText(categoryScores[1] + "%");
            tvCategory3Score.setText(categoryScores[2] + "%");
            tvCategory4Score.setText(categoryScores[3] + "%");
            tvCategory5Score.setText(categoryScores[4] + "%");

            // Animate progress bars after entrance animations complete
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                animateProgressBar(pbCategory1, categoryScores[0]);
                animateProgressBar(pbCategory2, categoryScores[1]);
                animateProgressBar(pbCategory3, categoryScores[2]);
                animateProgressBar(pbCategory4, categoryScores[3]);
                animateProgressBar(pbCategory5, categoryScores[4]);
            }, 900);
        }
    }

    private void animateProgressBar(ProgressBar bar, int target) {
        android.animation.ObjectAnimator anim = android.animation.ObjectAnimator.ofInt(bar, "progress", 0, target);
        anim.setDuration(750);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.start();
    }

    private void startEntranceAnimations() {
        // Leo: appear from above with overshoot bounce
        ivLeoResult.setTranslationY(-120f);
        ivLeoResult.setAlpha(0f);
        ivLeoResult.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(700)
                .setInterpolator(new OvershootInterpolator(1.0f))
                .withEndAction(this::startLeoPulse)
                .start();

        // Content card: slide up from below
        contentSection.setTranslationY(80f);
        contentSection.setAlpha(0f);
        contentSection.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(280)
                .setInterpolator(new OvershootInterpolator(0.5f))
                .start();

        // Category rows: staggered slide-in from left
        View[] rows = {rowCategory1, rowCategory2, rowCategory3, rowCategory4, rowCategory5};
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == null) continue;
            rows[i].setAlpha(0f);
            rows[i].setTranslationX(-40f);
            rows[i].animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(380)
                    .setStartDelay(500 + i * 90L)
                    .setInterpolator(new android.view.animation.DecelerateInterpolator())
                    .start();
        }

        // Continue button: fade up last
        btnContinueToDashboard.setAlpha(0f);
        btnContinueToDashboard.setTranslationY(20f);
        btnContinueToDashboard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(980)
                .start();

        // Leo dialogue fires after everything settles
        new Handler(Looper.getMainLooper()).postDelayed(this::showLeoCongratulation, 1500);
    }

    /** Gentle infinite breathing/pulse on Leo after entrance completes */
    private void startLeoPulse() {
        ivLeoResult.animate()
                .scaleX(1.07f).scaleY(1.07f)
                .setDuration(900)
                .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                .withEndAction(() -> ivLeoResult.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(900)
                        .setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator())
                        .withEndAction(this::startLeoPulse)
                        .start())
                .start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseLotties();
        ivLeoResult.animate().cancel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeLotties();
    }

    private void pauseLotties() {
        if (lottieStarsBg != null) lottieStarsBg.pauseAnimation();
        if (lottieConfetti != null) lottieConfetti.pauseAnimation();
        if (lottieBadgeSparkle != null) lottieBadgeSparkle.pauseAnimation();
        if (lottieResultsSparkle != null) lottieResultsSparkle.pauseAnimation();
        if (lottieCategoryAccent != null) lottieCategoryAccent.pauseAnimation();
    }

    private void resumeLotties() {
        if (lottieStarsBg != null) lottieStarsBg.resumeAnimation();
        if (lottieConfetti != null) lottieConfetti.resumeAnimation();
        if (lottieBadgeSparkle != null) lottieBadgeSparkle.resumeAnimation();
        if (lottieResultsSparkle != null) lottieResultsSparkle.resumeAnimation();
        if (lottieCategoryAccent != null) lottieCategoryAccent.resumeAnimation();
    }

    private void showLeoCongratulation() {
        List<LeoDialogueView.DialogueMessage> messages = new ArrayList<>();
        messages.add(new LeoDialogueView.DialogueMessage(getCongratsMessage(placementLevel)));
        messages.add(new LeoDialogueView.DialogueMessage(getEncouragementMessage(placementLevel)));
        messages.add(new LeoDialogueView.DialogueMessage(
                "Now, let's start your reading adventure!\n\n" +
                "I've picked the perfect stories for your level. " +
                "You'll get better and better with each story you read! \uD83D\uDCDA✨"
        ));

        LeoDialogueView dialogueView = new LeoDialogueView(this);
        dialogueView.setDialogueMessages(messages);
        dialogueView.setDialogueCompleteListener(() -> { /* user can proceed */ });
        dialogueView.show((ViewGroup) getWindow().getDecorView().getRootView());
    }

    private String getCongratsMessage(int level) {
        switch (level) {
            case 1:
                return "\uD83C\uDF1F Yay! You finished!\n\n" +
                        "I knew you could do it! I am SO happy for you right now! " +
                        "You're a Beginner Reader — and guess what? That's exactly where every great reading adventure starts! \uD83C\uDF89";
            case 2:
                return "\uD83C\uDF1F Wow, look at you go!\n\n" +
                        "You did an amazing job on the whole test! I'm literally clapping for you! " +
                        "You're an Early Reader — you're already learning so many cool things! \uD83C\uDF89";
            case 3:
                return "\uD83C\uDF1F Incredible!\n\n" +
                        "You did SO great on the test! Seriously, you should feel really proud right now — I definitely am! " +
                        "You're a Developing Reader — you're growing stronger every single day! \uD83C\uDF89";
            case 4:
                return "\uD83C\uDF1F Oh WOW!\n\n" +
                        "You are such a strong, incredible reader! I am beyond proud of you — you really, truly impressed me! " +
                        "You're a Fluent Reader — that is SO amazing! \uD83C\uDF89";
            default:
                return "\uD83C\uDF1F Great job completing the test! \uD83C\uDF89";
        }
    }

    private String getEncouragementMessage(int level) {
        switch (level) {
            case 1:
                return "You're already a star to me! \uD83D\uDCAA\n\n" +
                        "I'll be with you every step of the way! We'll start with fun, " +
                        "simple stories that are just right for you. Remember — every great reader was once a beginner!";
            case 2:
                return "You're growing as a reader! \uD83D\uDCAA\n\n" +
                        "I chose some really fun stories that are just perfect for where you are right now. " +
                        "Each one will make you even more awesome. You've got this!";
            case 3:
                return "You're becoming a strong reader! \uD83D\uDCAA\n\n" +
                        "I found the perfect stories that will help you level up even more! " +
                        "You're on your way to becoming a fluent reader — and I can't wait to see you get there!";
            case 4:
                return "You are an amazing reader! \uD83D\uDCAA\n\n" +
                        "I've saved my most exciting and challenging stories just for readers like you. " +
                        "Get ready — this is going to be SO amazing!";
            default:
                return "Let's keep learning together! \uD83D\uDCAA";
        }
    }

    private void setupListeners() {
        btnContinueToDashboard.setOnClickListener(v -> {
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

        String assessmentType = sessionManager.hasCompletedAssessment() ? "PostAssessment" : "PreAssessment";

        if (com.example.literise.utils.AppConfig.DEMO_MODE) {
            Log.d(TAG, "Demo mode: Skipping API save, updating local session only");
            if ("PreAssessment".equals(assessmentType)) {
                sessionManager.setAssessmentCompleted(true);
                sessionManager.setAssessmentStarted(false);
            }
            return;
        }

        int sessionId = (int) (System.currentTimeMillis() / 1000);
        int timeSpentSeconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
        String deviceInfo = Build.MANUFACTURER + " " + Build.MODEL + ", Android " + Build.VERSION.RELEASE;

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

        for (int i = 0; i < categoryScores.length && i < 5; i++) {
            request.setCategoryScore(i + 1, categoryScores[i]);
        }

        request.setTimeSpentSeconds(timeSpentSeconds);
        request.setDeviceInfo(deviceInfo);
        request.setAppVersion(getAppVersion());

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        Call<SavePlacementResultResponse> call = apiService.savePlacementResult(request);

        call.enqueue(new Callback<SavePlacementResultResponse>() {
            @Override
            public void onResponse(Call<SavePlacementResultResponse> call, Response<SavePlacementResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SavePlacementResultResponse result = response.body();
                    Log.d(TAG, "Placement result saved successfully: " + result.getMessage());

                    if ("PreAssessment".equals(assessmentType)) {
                        sessionManager.setAssessmentCompleted(true);
                        sessionManager.setAssessmentStarted(false);
                    }

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
        // Prevent back navigation — user must continue to dashboard
    }
}
