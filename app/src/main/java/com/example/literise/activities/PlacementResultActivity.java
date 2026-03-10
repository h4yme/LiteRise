package com.example.literise.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
        try {
            // ── Canvas setup ────────────────────────────────────────────
            int width  = 1600;
            int height = 1130;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            // ── Load fonts ──────────────────────────────────────────────
            android.graphics.Typeface poppinsBold     = ResourcesCompat.getFont(this, R.font.poppins_bold);
            android.graphics.Typeface poppinsSemiBold = ResourcesCompat.getFont(this, R.font.poppins_semibold);
            android.graphics.Typeface poppinsRegular  = ResourcesCompat.getFont(this, R.font.poppins_regular);
            android.graphics.Typeface serifBold       = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD);
            android.graphics.Typeface serifItalic     = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.ITALIC);
            android.graphics.Typeface serifBoldItalic = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD_ITALIC);

            // ── Color palette ───────────────────────────────────────────
            int cBackground  = 0xFFFDF8F0; // warm parchment
            int cPurpleDark  = 0xFF3B0764; // deep purple
            int cPurple      = 0xFF7C3AED; // brand purple
            int cPurpleLight = 0xFFDDD6FE; // lavender
            int cPurpleMid   = 0xFFEDE9FE; // very light purple
            int cGold        = 0xFFAD8A20; // gold accent
            int cDarkText    = 0xFF1E1B2E; // near-black
            int cGrayText    = 0xFF6B7280; // muted gray
            int cGreen       = 0xFF047857; // deep green

            // ── Background (parchment) ──────────────────────────────────
            android.graphics.Paint bgPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(cBackground);
            canvas.drawRect(0, 0, width, height, bgPaint);

            // Subtle inner glow — very light purple tint in center
            android.graphics.Paint glowPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            glowPaint.setShader(new android.graphics.RadialGradient(
                    width / 2f, height / 2f, width * 0.65f,
                    0x08C4B5FD, 0x00C4B5FD,
                    android.graphics.Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, width, height, glowPaint);

            // ── Faint watermark ──────────────────────────────────────────
            android.graphics.Paint wmPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            wmPaint.setColor(0x087C3AED);
            wmPaint.setTextSize(220f);
            if (poppinsBold != null) wmPaint.setTypeface(poppinsBold);
            wmPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.save();
            canvas.rotate(-28f, width / 2f, height / 2f);
            canvas.drawText("LiteRise", width / 2f, height / 2f + 70, wmPaint);
            canvas.restore();

            // ── Outer border (deep purple) ──────────────────────────────
            android.graphics.Paint outerBorderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            outerBorderPaint.setColor(cPurpleDark);
            outerBorderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            outerBorderPaint.setStrokeWidth(22f);
            canvas.drawRoundRect(14, 14, width - 14, height - 14, 14, 14, outerBorderPaint);

            // ── Gold mid-border ─────────────────────────────────────────
            android.graphics.Paint goldBorderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            goldBorderPaint.setColor(cGold);
            goldBorderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            goldBorderPaint.setStrokeWidth(5f);
            canvas.drawRoundRect(30, 30, width - 30, height - 30, 8, 8, goldBorderPaint);

            // ── Inner hairline border ───────────────────────────────────
            android.graphics.Paint innerBorderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            innerBorderPaint.setColor(cPurpleLight);
            innerBorderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            innerBorderPaint.setStrokeWidth(2f);
            canvas.drawRoundRect(44, 44, width - 44, height - 44, 4, 4, innerBorderPaint);

            // ── Corner ornaments ─────────────────────────────────────────
            android.graphics.Paint cornerLinePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            cornerLinePaint.setColor(cGold);
            cornerLinePaint.setStyle(android.graphics.Paint.Style.STROKE);
            cornerLinePaint.setStrokeWidth(3.5f);
            android.graphics.Paint cornerDotPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            cornerDotPaint.setColor(cGold);
            cornerDotPaint.setStyle(android.graphics.Paint.Style.FILL);
            float cs = 60f, cp = 60f;
            // Top-left
            canvas.drawLine(cp, cp + cs, cp, cp, cornerLinePaint);
            canvas.drawLine(cp, cp, cp + cs, cp, cornerLinePaint);
            // Top-right
            canvas.drawLine(width - cp - cs, cp, width - cp, cp, cornerLinePaint);
            canvas.drawLine(width - cp, cp, width - cp, cp + cs, cornerLinePaint);
            // Bottom-left
            canvas.drawLine(cp, height - cp - cs, cp, height - cp, cornerLinePaint);
            canvas.drawLine(cp, height - cp, cp + cs, height - cp, cornerLinePaint);
            // Bottom-right
            canvas.drawLine(width - cp - cs, height - cp, width - cp, height - cp, cornerLinePaint);
            canvas.drawLine(width - cp, height - cp, width - cp, height - cp - cs, cornerLinePaint);
            // Corner dots
            canvas.drawCircle(cp, cp, 7, cornerDotPaint);
            canvas.drawCircle(width - cp, cp, 7, cornerDotPaint);
            canvas.drawCircle(cp, height - cp, 7, cornerDotPaint);
            canvas.drawCircle(width - cp, height - cp, 7, cornerDotPaint);

            // ── Gold diamond helper ──────────────────────────────────────
            android.graphics.Paint diamondFillPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            diamondFillPaint.setColor(cGold);
            diamondFillPaint.setStyle(android.graphics.Paint.Style.FILL);

            // ── Decorative rule helper ───────────────────────────────────
            android.graphics.Paint rulePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            rulePaint.setColor(cGold);
            rulePaint.setStrokeWidth(2.5f);

            // ── Header: brand name ───────────────────────────────────────
            // Small top diamond ornament
            android.graphics.Path topDiamond = new android.graphics.Path();
            topDiamond.moveTo(width / 2f, 82f);
            topDiamond.lineTo(width / 2f + 14, 96f);
            topDiamond.lineTo(width / 2f, 110f);
            topDiamond.lineTo(width / 2f - 14, 96f);
            topDiamond.close();
            canvas.drawPath(topDiamond, diamondFillPaint);

            android.graphics.Paint brandPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            brandPaint.setColor(cPurpleDark);
            brandPaint.setTextSize(86f);
            if (poppinsBold != null) brandPaint.setTypeface(poppinsBold);
            brandPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("LiteRise", width / 2f, 185f, brandPaint);

            android.graphics.Paint programPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            programPaint.setColor(cGrayText);
            programPaint.setTextSize(30f);
            if (poppinsRegular != null) programPaint.setTypeface(poppinsRegular);
            programPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("E N G L I S H   R E A D I N G   P R O G R A M", width / 2f, 226f, programPaint);

            // ── Ornamental rule below header ─────────────────────────────
            float ruleY = 258f;
            canvas.drawLine(90, ruleY, width / 2f - 32, ruleY, rulePaint);
            canvas.drawLine(width / 2f + 32, ruleY, width - 90, ruleY, rulePaint);
            android.graphics.Path midDiamond = new android.graphics.Path();
            midDiamond.moveTo(width / 2f, ruleY - 14);
            midDiamond.lineTo(width / 2f + 18, ruleY);
            midDiamond.lineTo(width / 2f, ruleY + 14);
            midDiamond.lineTo(width / 2f - 18, ruleY);
            midDiamond.close();
            canvas.drawPath(midDiamond, diamondFillPaint);

            // ── "Certificate of Completion" ──────────────────────────────
            android.graphics.Paint certTitlePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            certTitlePaint.setColor(cDarkText);
            certTitlePaint.setTextSize(74f);
            certTitlePaint.setTypeface(serifBold);
            certTitlePaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("Certificate of Completion", width / 2f, 348f, certTitlePaint);

            // Thin lavender underline beneath title
            android.graphics.Paint thinLinePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            thinLinePaint.setColor(cPurpleLight);
            thinLinePaint.setStrokeWidth(2f);
            canvas.drawLine(width / 2f - 370, 366f, width / 2f + 370, 366f, thinLinePaint);

            // ── "This certifies that" ────────────────────────────────────
            android.graphics.Paint certifiesPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            certifiesPaint.setColor(cGrayText);
            certifiesPaint.setTextSize(34f);
            certifiesPaint.setTypeface(serifItalic);
            certifiesPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("This certifies that", width / 2f, 428f, certifiesPaint);

            // ── Student name ─────────────────────────────────────────────
            String studentName = sessionManager.getFullname();
            if (studentName == null || studentName.isEmpty()) studentName = sessionManager.getNickname();
            if (studentName == null || studentName.isEmpty()) studentName = "Student";
            android.graphics.Paint namePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            namePaint.setColor(cPurple);
            namePaint.setTextSize(90f);
            namePaint.setTypeface(serifBoldItalic);
            namePaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText(studentName, width / 2f, 534f, namePaint);

            // Decorative name underline with end-dots
            float nw = namePaint.measureText(studentName);
            android.graphics.Paint nameUnderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            nameUnderPaint.setColor(cPurple);
            nameUnderPaint.setStrokeWidth(3f);
            canvas.drawLine(width / 2f - nw / 2 - 28, 554f,
                    width / 2f + nw / 2 + 28, 554f, nameUnderPaint);
            canvas.drawCircle(width / 2f - nw / 2 - 28, 554f, 6, cornerDotPaint);
            canvas.drawCircle(width / 2f + nw / 2 + 28, 554f, 6, cornerDotPaint);

            // ── Completion description ───────────────────────────────────
            android.graphics.Paint completedPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            completedPaint.setColor(cGrayText);
            completedPaint.setTextSize(32f);
            completedPaint.setTypeface(serifItalic);
            completedPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("has successfully completed the", width / 2f, 610f, completedPaint);

            android.graphics.Paint progNamePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            progNamePaint.setColor(cDarkText);
            progNamePaint.setTextSize(44f);
            if (poppinsSemiBold != null) progNamePaint.setTypeface(poppinsSemiBold);
            else progNamePaint.setFakeBoldText(true);
            progNamePaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("LiteRise English Reading Program", width / 2f, 668f, progNamePaint);

            // ── Level achieved badge ─────────────────────────────────────
            android.graphics.RectF badgeRect = new android.graphics.RectF(
                    width / 2f - 270, 694f, width / 2f + 270, 756f);
            android.graphics.Paint badgeBgPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            badgeBgPaint.setColor(cPurpleMid);
            canvas.drawRoundRect(badgeRect, 32, 32, badgeBgPaint);
            android.graphics.Paint badgeBorderPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            badgeBorderPaint.setColor(cPurple);
            badgeBorderPaint.setStyle(android.graphics.Paint.Style.STROKE);
            badgeBorderPaint.setStrokeWidth(2.5f);
            canvas.drawRoundRect(badgeRect, 32, 32, badgeBorderPaint);
            android.graphics.Paint badgeTextPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            badgeTextPaint.setColor(cPurpleDark);
            badgeTextPaint.setTextSize(36f);
            if (poppinsSemiBold != null) badgeTextPaint.setTypeface(poppinsSemiBold);
            else badgeTextPaint.setFakeBoldText(true);
            badgeTextPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("\u2605  Level Achieved: " + levelName + "  \u2605", width / 2f, 737f, badgeTextPaint);

            // ── Stats row ────────────────────────────────────────────────
            double preTheta  = sessionManager.getPreTheta();
            double thetaDiff = finalTheta - preTheta;
            String sign = thetaDiff >= 0 ? "+" : "";
            android.graphics.Paint statsPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            statsPaint.setColor(cGreen);
            statsPaint.setTextSize(30f);
            if (poppinsRegular != null) statsPaint.setTypeface(poppinsRegular);
            statsPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText(
                    "Ability Growth: " + sign + String.format(Locale.US, "%.2f", thetaDiff)
                            + "   \u2022   Accuracy: " + String.format(Locale.US, "%.0f%%", accuracy),
                    width / 2f, 806f, statsPaint);

            // ── Bottom ornamental rule ───────────────────────────────────
            float bruleY = 836f;
            canvas.drawLine(90, bruleY, width / 2f - 32, bruleY, rulePaint);
            canvas.drawLine(width / 2f + 32, bruleY, width - 90, bruleY, rulePaint);
            android.graphics.Path bDiamond = new android.graphics.Path();
            bDiamond.moveTo(width / 2f, bruleY - 14);
            bDiamond.lineTo(width / 2f + 18, bruleY);
            bDiamond.lineTo(width / 2f, bruleY + 14);
            bDiamond.lineTo(width / 2f - 18, bruleY);
            bDiamond.close();
            canvas.drawPath(bDiamond, diamondFillPaint);

            // ── Bottom section: date | seal | director ───────────────────
            String date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).format(new Date());
            float leftX  = width / 4f;
            float rightX = 3f * width / 4f;
            float bLineY = 920f;

            android.graphics.Paint bLabelPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            bLabelPaint.setColor(cGrayText);
            bLabelPaint.setTextSize(24f);
            if (poppinsRegular != null) bLabelPaint.setTypeface(poppinsRegular);
            bLabelPaint.setTextAlign(android.graphics.Paint.Align.CENTER);

            android.graphics.Paint bValuePaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            bValuePaint.setColor(cDarkText);
            bValuePaint.setTextSize(26f);
            if (poppinsSemiBold != null) bValuePaint.setTypeface(poppinsSemiBold);
            else bValuePaint.setFakeBoldText(true);
            bValuePaint.setTextAlign(android.graphics.Paint.Align.CENTER);

            // Left column — date
            canvas.drawText("Date of Completion", leftX, bLineY - 8, bLabelPaint);
            canvas.drawLine(leftX - 110, bLineY + 8, leftX + 110, bLineY + 8, rulePaint);
            canvas.drawText(date, leftX, bLineY + 44, bValuePaint);

            // Right column — director
            canvas.drawText("Program Director", rightX, bLineY - 8, bLabelPaint);
            canvas.drawLine(rightX - 110, bLineY + 8, rightX + 110, bLineY + 8, rulePaint);
            canvas.drawText("LiteRise Academy", rightX, bLineY + 44, bValuePaint);

            // Center seal
            float sealCX = width / 2f, sealCY = bLineY + 22;
            float sealR  = 62f;
            android.graphics.Paint sealOuterPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            sealOuterPaint.setColor(cPurpleDark);
            sealOuterPaint.setStyle(android.graphics.Paint.Style.STROKE);
            sealOuterPaint.setStrokeWidth(5f);
            canvas.drawCircle(sealCX, sealCY, sealR, sealOuterPaint);
            android.graphics.Paint sealInnerPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            sealInnerPaint.setColor(cGold);
            sealInnerPaint.setStyle(android.graphics.Paint.Style.STROKE);
            sealInnerPaint.setStrokeWidth(2.5f);
            canvas.drawCircle(sealCX, sealCY, sealR - 9, sealInnerPaint);
            android.graphics.Paint sealTextPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            sealTextPaint.setColor(cPurpleDark);
            sealTextPaint.setTextSize(38f);
            if (poppinsBold != null) sealTextPaint.setTypeface(poppinsBold);
            else sealTextPaint.setFakeBoldText(true);
            sealTextPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("LR", sealCX, sealCY + 14f, sealTextPaint);

            // ── Footer ───────────────────────────────────────────────────
            android.graphics.Paint footerPaint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
            footerPaint.setColor(cPurple);
            footerPaint.setTextSize(24f);
            if (poppinsRegular != null) footerPaint.setTypeface(poppinsRegular);
            footerPaint.setTextAlign(android.graphics.Paint.Align.CENTER);
            canvas.drawText("www.literise.app", width / 2f, height - 30f, footerPaint);

            // ── Save and share ───────────────────────────────────────────
            File cachesDir = new File(getCacheDir(), "certificates");
            if (!cachesDir.exists()) cachesDir.mkdirs();
            File certFile = new File(cachesDir, "LiteRise_Certificate.png");
            FileOutputStream fos = new FileOutputStream(certFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Uri contentUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    certFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "LiteRise Certificate of Completion");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share Certificate"));

        } catch (Exception e) {
            Log.e(TAG, "Error generating certificate: " + e.getMessage());
            android.widget.Toast.makeText(this, "Could not generate certificate", android.widget.Toast.LENGTH_SHORT).show();
        }
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