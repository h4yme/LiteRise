package com.example.literise.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.PlacementProgressResponse;

import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ComparisonReportActivity extends BaseActivity {

    private SessionManager session;

    private ProgressBar progressBarLoading;
    private LinearLayout contentLayout;
    private LinearLayout layoutNoPostAssessment;
    private LinearLayout cardGrowth;

    private TextView tvPreTheta, tvPreLevel, tvPreAccuracy, tvPreDate;
    private TextView tvPostTheta, tvPostLevel, tvPostAccuracy, tvPostDate;
    private TextView tvThetaGrowth, tvLevelGrowth, tvAccuracyGrowth, tvComparisonStatus;
    private TextView tvProgressHint;

    private ProgressBar pbPhonics, pbVocabulary, pbGrammar, pbComprehension, pbCreating;
    private TextView tvPhonicsScore, tvVocabularyScore, tvGrammarScore, tvComprehensionScore, tvCreatingScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparison_report);

        session = new SessionManager(this);

        initViews();
        loadProgress();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        progressBarLoading = findViewById(R.id.progressBarLoading);
        contentLayout = findViewById(R.id.contentLayout);
        layoutNoPostAssessment = findViewById(R.id.layoutNoPostAssessment);
        cardGrowth = findViewById(R.id.cardGrowth);

        tvPreTheta = findViewById(R.id.tvPreTheta);
        tvPreLevel = findViewById(R.id.tvPreLevel);
        tvPreAccuracy = findViewById(R.id.tvPreAccuracy);
        tvPreDate = findViewById(R.id.tvPreDate);

        tvPostTheta = findViewById(R.id.tvPostTheta);
        tvPostLevel = findViewById(R.id.tvPostLevel);
        tvPostAccuracy = findViewById(R.id.tvPostAccuracy);
        tvPostDate = findViewById(R.id.tvPostDate);

        tvThetaGrowth = findViewById(R.id.tvThetaGrowth);
        tvLevelGrowth = findViewById(R.id.tvLevelGrowth);
        tvAccuracyGrowth = findViewById(R.id.tvAccuracyGrowth);
        tvComparisonStatus = findViewById(R.id.tvComparisonStatus);
        tvProgressHint = findViewById(R.id.tvProgressHint);

        pbPhonics = findViewById(R.id.pbPhonics);
        pbVocabulary = findViewById(R.id.pbVocabulary);
        pbGrammar = findViewById(R.id.pbGrammar);
        pbComprehension = findViewById(R.id.pbComprehension);
        pbCreating = findViewById(R.id.pbCreating);

        tvPhonicsScore = findViewById(R.id.tvPhonicsScore);
        tvVocabularyScore = findViewById(R.id.tvVocabularyScore);
        tvGrammarScore = findViewById(R.id.tvGrammarScore);
        tvComprehensionScore = findViewById(R.id.tvComprehensionScore);
        tvCreatingScore = findViewById(R.id.tvCreatingScore);
    }

    private void loadProgress() {
        progressBarLoading.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);

        int studentId = session.getStudentId();
        ApiService api = ApiClient.getApiService();

        api.getPlacementProgress(studentId).enqueue(new Callback<PlacementProgressResponse>() {
            @Override
            public void onResponse(Call<PlacementProgressResponse> call, Response<PlacementProgressResponse> response) {
                progressBarLoading.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    populateUI(response.body());
                } else {
                    showFallbackFromSession();
                }
            }

            @Override
            public void onFailure(Call<PlacementProgressResponse> call, Throwable t) {
                progressBarLoading.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                showFallbackFromSession();
            }
        });
    }

    private void populateUI(PlacementProgressResponse data) {
        // --- Pre-Assessment ---
        PlacementProgressResponse.AssessmentResults results = data.getResults();
        if (results != null && results.getPre() != null) {
            PlacementProgressResponse.AssessmentDetail pre = results.getPre();
            tvPreTheta.setText(String.format(Locale.US, "%.2f", pre.getFinalTheta()));
            tvPreLevel.setText(pre.getLevelName() != null ? pre.getLevelName() : String.valueOf(pre.getPlacementLevel()));
            tvPreAccuracy.setText(String.format(Locale.US, "%.1f%%", pre.getAccuracyPercentage()));
            tvPreDate.setText(formatDate(pre.getCompletedDate()));

            // Category scores
            Map<String, Double> catScores = pre.getCategoryScores();
            if (catScores != null) {
                setCategory(pbPhonics, tvPhonicsScore, catScores.get("Cat1_PhonicsWordStudy"));
                setCategory(pbVocabulary, tvVocabularyScore, catScores.get("Cat2_VocabularyWordKnowledge"));
                setCategory(pbGrammar, tvGrammarScore, catScores.get("Cat3_GrammarAwareness"));
                setCategory(pbComprehension, tvComprehensionScore, catScores.get("Cat4_ComprehendingText"));
                setCategory(pbCreating, tvCreatingScore, catScores.get("Cat5_CreatingComposing"));
            }
        }

        // --- Post-Assessment ---
        boolean hasPost = results != null && results.getPost() != null;
        if (hasPost) {
            PlacementProgressResponse.AssessmentDetail post = results.getPost();
            tvPostTheta.setText(String.format(Locale.US, "%.2f", post.getFinalTheta()));
            tvPostLevel.setText(post.getLevelName() != null ? post.getLevelName() : String.valueOf(post.getPlacementLevel()));
            tvPostAccuracy.setText(String.format(Locale.US, "%.1f%%", post.getAccuracyPercentage()));
            tvPostDate.setText(formatDate(post.getCompletedDate()));
        } else {
            tvPostTheta.setText("Not taken yet");
            tvPostLevel.setText("—");
            tvPostAccuracy.setText("—");
            tvPostDate.setText("—");
            layoutNoPostAssessment.setVisibility(View.VISIBLE);
        }

        // --- Growth Comparison ---
        PlacementProgressResponse.ComparisonData comparison = data.getComparison();
        if (hasPost && comparison != null) {
            cardGrowth.setVisibility(View.VISIBLE);
            layoutNoPostAssessment.setVisibility(View.GONE);

            Double thetaG = comparison.getThetaGrowth();
            Integer levelG = comparison.getLevelGrowth();
            Double accG = comparison.getAccuracyGrowth();

            tvThetaGrowth.setText(formatGrowthDouble(thetaG));
            tvLevelGrowth.setText(formatGrowthInt(levelG));
            tvAccuracyGrowth.setText(formatGrowthDouble(accG) + "%");

            applyGrowthColor(tvThetaGrowth, thetaG != null ? thetaG : 0);
            applyGrowthColor(tvLevelGrowth, levelG != null ? levelG : 0);
            applyGrowthColor(tvAccuracyGrowth, accG != null ? accG : 0);

            String status = comparison.getComparisonStatus();
            if (status != null && !status.isEmpty()) {
                tvComparisonStatus.setText(status);
                tvComparisonStatus.setBackgroundColor(
                        status.equals("IMPROVED") ? 0xFF059669 :
                        status.equals("MAINTAINED") ? 0xFFD97706 : 0xFFDC2626
                );
            }
        }
    }

    private void showFallbackFromSession() {
        // Use locally cached placement data from SessionManager
        int phonics = session.getCategoryScore("Cat1_PhonicsWordStudy");
        int vocab   = session.getCategoryScore("Cat2_VocabularyWordKnowledge");
        int grammar = session.getCategoryScore("Cat3_GrammarAwareness");
        int comp    = session.getCategoryScore("Cat4_ComprehendingText");
        int creating = session.getCategoryScore("Cat5_CreatingComposing");

        setCategory(pbPhonics, tvPhonicsScore, (double) phonics);
        setCategory(pbVocabulary, tvVocabularyScore, (double) vocab);
        setCategory(pbGrammar, tvGrammarScore, (double) grammar);
        setCategory(pbComprehension, tvComprehensionScore, (double) comp);
        setCategory(pbCreating, tvCreatingScore, (double) creating);

        tvPreTheta.setText(String.format(Locale.US, "%.2f", (double) session.getXP() / 100.0));
        tvPreLevel.setText(session.getPlacementLevel());
        tvPreAccuracy.setText("—");
        tvPreDate.setText("—");

        tvPostTheta.setText("Not taken yet");
        tvPostLevel.setText("—");
        tvPostAccuracy.setText("—");
        tvPostDate.setText("—");
        layoutNoPostAssessment.setVisibility(View.VISIBLE);
    }

    private void setCategory(ProgressBar pb, TextView tv, Double score) {
        int val = score != null ? (int) Math.round(score) : 0;
        pb.setProgress(val);
        tv.setText(val + "%");
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "—";
        // Just show first 10 chars of ISO date: "2024-03-15"
        return dateStr.length() >= 10 ? dateStr.substring(0, 10) : dateStr;
    }

    private String formatGrowthDouble(Double val) {
        if (val == null) return "—";
        return (val >= 0 ? "+" : "") + String.format(Locale.US, "%.2f", val);
    }

    private String formatGrowthInt(Integer val) {
        if (val == null) return "—";
        return (val >= 0 ? "+" : "") + val;
    }

    private void applyGrowthColor(TextView tv, double val) {
        tv.setTextColor(val > 0 ? 0xFF059669 : val < 0 ? 0xFFDC2626 : 0xFF6B7280);
    }
}
