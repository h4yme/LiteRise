package com.example.literise.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.SavePlacementResultRequest;
import com.example.literise.models.SavePlacementResultResponse;
import com.example.literise.utils.SessionLogger;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlacementResultActivity extends AppCompatActivity {

    private static final String TAG = "PlacementResultActivity";

    // Leo + Lottie
    private ImageView ivLeoResult;
    private LottieAnimationView lottieResultCelebrate;
    private LottieAnimationView lottieBadgeSparkle;
    private LottieAnimationView lottieResultsSparkle;

    // Text
    private TextView tvCongrats, tvLevelName, tvLevelNumber;
    private TextView tvAccuracy, tvQuestionsAnswered;
    private TextView tvCategory1Score, tvCategory2Score, tvCategory3Score, tvCategory4Score, tvCategory5Score;

    // Progress bars
    private ProgressBar pbCategory1, pbCategory2, pbCategory3, pbCategory4, pbCategory5;

    // Category rows + container
    private View rowCategory1, rowCategory2, rowCategory3, rowCategory4, rowCategory5;
    private View contentSection;
    private MaterialButton btnContinueToDashboard;
    private MaterialButton btnViewCertificate;

    // Post-assessment improvement views
    private LinearLayout sectionImprovement;
    private TextView tvPreThetaValue, tvPreLevelLabel;
    private TextView tvPostThetaValue, tvPostLevelLabel;
    private TextView tvThetaImprovement, tvImprovementMessage;

    // Audio
    private MediaPlayer resultVoicePlayer;

    // Placement data
    private int placementLevel;
    private String levelName;
    private double accuracy;
    private int totalAnswered;
    private int totalCorrect;
    private int[] categoryScores;
    private double finalTheta;
    private long startTime;
    private boolean isPostAssessment;

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
        ivLeoResult    = findViewById(R.id.ivLeoResult);
        contentSection = findViewById(R.id.contentSection);

        lottieResultCelebrate = findViewById(R.id.lottieResultCelebrate);
        lottieBadgeSparkle    = findViewById(R.id.lottieBadgeSparkle);
        lottieResultsSparkle  = findViewById(R.id.lottieResultsSparkle);

        tvCongrats          = findViewById(R.id.tvCongrats);
        tvLevelName         = findViewById(R.id.tvLevelName);
        tvLevelNumber       = findViewById(R.id.tvLevelNumber);
        tvAccuracy          = findViewById(R.id.tvAccuracy);
        tvQuestionsAnswered = findViewById(R.id.tvQuestionsAnswered);
        tvCategory1Score    = findViewById(R.id.tvCategory1Score);
        tvCategory2Score    = findViewById(R.id.tvCategory2Score);
        tvCategory3Score    = findViewById(R.id.tvCategory3Score);
        tvCategory4Score    = findViewById(R.id.tvCategory4Score);
        tvCategory5Score    = findViewById(R.id.tvCategory5Score);

        pbCategory1 = findViewById(R.id.pbCategory1);
        pbCategory2 = findViewById(R.id.pbCategory2);
        pbCategory3 = findViewById(R.id.pbCategory3);
        pbCategory4 = findViewById(R.id.pbCategory4);
        pbCategory5 = findViewById(R.id.pbCategory5);

        rowCategory1 = findViewById(R.id.rowCategory1);
        rowCategory2 = findViewById(R.id.rowCategory2);
        rowCategory3 = findViewById(R.id.rowCategory3);
        rowCategory4 = findViewById(R.id.rowCategory4);
        rowCategory5 = findViewById(R.id.rowCategory5);

        btnContinueToDashboard = findViewById(R.id.btnContinueToDashboard);
        btnViewCertificate     = findViewById(R.id.btnViewCertificate);

        sectionImprovement   = findViewById(R.id.sectionImprovement);
        tvPreThetaValue      = findViewById(R.id.tvPreThetaValue);
        tvPreLevelLabel      = findViewById(R.id.tvPreLevelLabel);
        tvPostThetaValue     = findViewById(R.id.tvPostThetaValue);
        tvPostLevelLabel     = findViewById(R.id.tvPostLevelLabel);
        tvThetaImprovement   = findViewById(R.id.tvThetaImprovement);
        tvImprovementMessage = findViewById(R.id.tvImprovementMessage);
    }

    private void getResultsFromIntent() {
        Intent intent  = getIntent();
        placementLevel = intent.getIntExtra("placement_level", 3);
        levelName      = intent.getStringExtra("level_name");
        accuracy       = intent.getDoubleExtra("accuracy", 0.0);
        totalAnswered  = intent.getIntExtra("total_answered", 0);
        totalCorrect   = intent.getIntExtra("total_correct", 0);
        categoryScores    = intent.getIntArrayExtra("category_scores");
        finalTheta        = intent.getDoubleExtra("final_theta", 0.0);
        isPostAssessment  = intent.getBooleanExtra("is_post_assessment", false);
        long passedStartTime = intent.getLongExtra("start_time", 0);
        if (passedStartTime > 0) startTime = passedStartTime;

        if (levelName == null || levelName.isEmpty()) levelName = getLevelNameFromLevel(placementLevel);
        if (categoryScores == null) categoryScores = new int[]{0, 0, 0, 0, 0};
    }

    private String getLevelNameFromLevel(int level) {
        switch (level) {
            case 1:  return "Beginner";
            case 2:  return "Intermediate";
            case 3:  return "Advanced";
            default: return "Intermediate";
        }
    }

    private void displayResults() {
        tvLevelName.setText(levelName);
        tvLevelNumber.setText("Level " + placementLevel);
        tvAccuracy.setText(String.format("%.0f%%", accuracy));
        tvQuestionsAnswered.setText(Math.min(totalAnswered, 25) + "/25");

        if (isPostAssessment) {
            tvCongrats.setText("You Did It! \uD83C\uDF93 LiteRise Complete!");
            btnContinueToDashboard.setText("Back to Dashboard");
            showImprovementSection();
            sectionImprovement.setVisibility(View.VISIBLE);
            btnViewCertificate.setVisibility(View.VISIBLE);
        }

        if (categoryScores.length >= 5) {
            tvCategory1Score.setText(categoryScores[0] + "%");
            tvCategory2Score.setText(categoryScores[1] + "%");
            tvCategory3Score.setText(categoryScores[2] + "%");
            tvCategory4Score.setText(categoryScores[3] + "%");
            tvCategory5Score.setText(categoryScores[4] + "%");

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                animateProgressBar(pbCategory1, categoryScores[0]);
                animateProgressBar(pbCategory2, categoryScores[1]);
                animateProgressBar(pbCategory3, categoryScores[2]);
                animateProgressBar(pbCategory4, categoryScores[3]);
                animateProgressBar(pbCategory5, categoryScores[4]);
            }, 900);
        }
    }

    private void showImprovementSection() {
        double preTheta  = sessionManager.getPreTheta();
        String preLevel  = sessionManager.getPreLevel();
        double thetaDiff = finalTheta - preTheta;

        tvPreThetaValue.setText(String.format(Locale.US, "%.2f", preTheta));
        tvPreLevelLabel.setText(preLevel.isEmpty() ? "—" : preLevel);
        tvPostThetaValue.setText(String.format(Locale.US, "%.2f", finalTheta));
        tvPostLevelLabel.setText(levelName);

        String sign = thetaDiff >= 0 ? "+" : "";
        tvThetaImprovement.setText(sign + String.format(Locale.US, "%.2f", thetaDiff));

        if (thetaDiff > 0.1) {
            tvThetaImprovement.setTextColor(0xFF059669);
            tvImprovementMessage.setText("\uD83C\uDF1F Amazing growth! You've improved so much since you started LiteRise!");
            tvImprovementMessage.setTextColor(0xFF059669);
        } else if (thetaDiff >= -0.1) {
            tvThetaImprovement.setTextColor(0xFFD97706);
            tvImprovementMessage.setText("\uD83D\uDCAA You maintained your level! Keep practicing to grow even more!");
            tvImprovementMessage.setTextColor(0xFFD97706);
        } else {
            tvThetaImprovement.setTextColor(0xFFDC2626);
            tvImprovementMessage.setText("\uD83D\uDCDA Keep going! Every reader grows at their own pace. You've got this!");
            tvImprovementMessage.setTextColor(0xFFDC2626);
        }
    }

    private void animateProgressBar(ProgressBar bar, int target) {
        android.animation.ObjectAnimator anim =
                android.animation.ObjectAnimator.ofInt(bar, "progress", 0, target);
        anim.setDuration(750);
        anim.setInterpolator(new android.view.animation.DecelerateInterpolator());
        anim.start();
    }

    private void startEntranceAnimations() {
        // Leo: drop in from above with overshoot bounce
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

        // Category rows: staggered slide in from left
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

        // Single voice over fires after everything settles
        new Handler(Looper.getMainLooper()).postDelayed(this::playResultVoiceOver, 1200);
    }

    /** Gentle infinite breathing pulse on Leo after entrance completes */
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

    /**
     * Plays the single result voice over (plays once, then releases).
     * Drop your MP3 into app/src/main/res/raw/ named: result_voiceover.mp3
     */
    private void playResultVoiceOver() {
        try {
            resultVoicePlayer = MediaPlayer.create(this, R.raw.step1);
            if (resultVoicePlayer != null) {
                resultVoicePlayer.start();
                resultVoicePlayer.setOnCompletionListener(mp -> {
                    mp.release();
                    resultVoicePlayer = null;
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing result voice over: " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseLotties();
        ivLeoResult.animate().cancel();
        if (resultVoicePlayer != null && resultVoicePlayer.isPlaying()) {
            resultVoicePlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeLotties();
        if (resultVoicePlayer != null && !resultVoicePlayer.isPlaying()) {
            resultVoicePlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (resultVoicePlayer != null) {
            resultVoicePlayer.release();
            resultVoicePlayer = null;
        }
    }

    private void pauseLotties() {
        if (lottieResultCelebrate != null) lottieResultCelebrate.pauseAnimation();
        if (lottieBadgeSparkle   != null) lottieBadgeSparkle.pauseAnimation();
        if (lottieResultsSparkle != null) lottieResultsSparkle.pauseAnimation();
    }

    private void resumeLotties() {
        if (lottieResultCelebrate != null) lottieResultCelebrate.resumeAnimation();
        if (lottieBadgeSparkle   != null) lottieBadgeSparkle.resumeAnimation();
        if (lottieResultsSparkle != null) lottieResultsSparkle.resumeAnimation();
    }

    private void setupListeners() {
        btnContinueToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(PlacementResultActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        btnViewCertificate.setOnClickListener(v -> shareCertificate());
    }

    private void shareCertificate() {
        String name = sessionManager.getFullname();
        if (name == null || name.isEmpty()) name = sessionManager.getNickname();
        if (name == null || name.isEmpty()) name = "Student";
        com.example.literise.utils.CertificateHelper.generateAndShare(
                this, name, levelName, finalTheta, sessionManager.getPreTheta(), accuracy);
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
        // Always persist post-assessment cert data so the user can reprint later
        if (isPostAssessment) {
            sessionManager.savePostAssessmentResult(finalTheta, levelName, accuracy);
        }

        int studentId = sessionManager.getStudentId();
        if (studentId == 0) {
            Log.w(TAG, "Cannot save placement result: No student ID found");
            return;
        }

        String assessmentType = isPostAssessment ? "PostAssessment" : "PreAssessment";

        if (com.example.literise.utils.AppConfig.DEMO_MODE) {
            Log.d(TAG, "Demo mode: Skipping API save, updating local session only");
            if (!isPostAssessment) {
                sessionManager.setAssessmentCompleted(true);
                sessionManager.setAssessmentStarted(false);
            }
            return;
        }

        int sessionId        = (int) (System.currentTimeMillis() / 1000);
        int timeSpentSeconds = (int) ((System.currentTimeMillis() - startTime) / 1000);
        String deviceInfo    = Build.MANUFACTURER + " " + Build.MODEL + ", Android " + Build.VERSION.RELEASE;

        SavePlacementResultRequest request = new SavePlacementResultRequest(
                studentId, sessionId, assessmentType,
                finalTheta, placementLevel, levelName,
                totalAnswered, totalCorrect, accuracy
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
            public void onResponse(Call<SavePlacementResultResponse> call,
                                   Response<SavePlacementResultResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Placement result saved: " + response.body().getMessage());
                    if (!isPostAssessment) {
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
        // Prevent back — user must tap Continue to go to dashboard
    }
}