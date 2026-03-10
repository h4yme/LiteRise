package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.CheckModulesCompleteResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProgressViewActivity extends BaseNavActivity {

    private TextView tvStatXP, tvStatStreak, tvStatLessons;
    private TextView tvScorePhonics, tvScoreVocab, tvScoreGrammar, tvScoreComprehension, tvScoreWriting;
    private ProgressBar progressPhonics, progressVocab, progressGrammar, progressComprehension, progressWriting;
    private MaterialButton btnViewFullReport;

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_view);

        session = new SessionManager(this);

        tvStatXP              = findViewById(R.id.tvStatXP);
        tvStatStreak          = findViewById(R.id.tvStatStreak);
        tvStatLessons         = findViewById(R.id.tvStatLessons);
        tvScorePhonics        = findViewById(R.id.tvScorePhonics);
        tvScoreVocab          = findViewById(R.id.tvScoreVocab);
        tvScoreGrammar        = findViewById(R.id.tvScoreGrammar);
        tvScoreComprehension  = findViewById(R.id.tvScoreComprehension);
        tvScoreWriting        = findViewById(R.id.tvScoreWriting);
        progressPhonics       = findViewById(R.id.progressPhonics);
        progressVocab         = findViewById(R.id.progressVocab);
        progressGrammar       = findViewById(R.id.progressGrammar);
        progressComprehension = findViewById(R.id.progressComprehension);
        progressWriting       = findViewById(R.id.progressWriting);
        btnViewFullReport     = findViewById(R.id.btnViewFullReport);

        setupBottomNav(getNavIndex());
        loadProgressData();

        btnViewFullReport.setOnClickListener(v ->
                startActivity(new Intent(this, ComparisonReportActivity.class)));
    }

    private void loadProgressData() {
        // Top stats
        tvStatXP.setText(String.valueOf(session.getXP()));
        tvStatStreak.setText(String.valueOf(session.getStreak()));
        tvStatLessons.setText("0"); // updated from API below
        fetchLessonsDone();

        // Skills from placement test scores
        setSkill(tvScorePhonics, progressPhonics,
                session.getCategoryScore("Cat1_PhonicsWordStudy"));
        setSkill(tvScoreVocab, progressVocab,
                session.getCategoryScore("Cat2_VocabularyWordKnowledge"));
        setSkill(tvScoreGrammar, progressGrammar,
                session.getCategoryScore("Cat3_GrammarAwareness"));
        setSkill(tvScoreComprehension, progressComprehension,
                session.getCategoryScore("Cat4_ComprehendingText"));
        setSkill(tvScoreWriting, progressWriting,
                session.getCategoryScore("Cat5_CreatingComposing"));
    }

    private void setSkill(TextView label, ProgressBar bar, int score) {
        label.setText(score + "%");
        bar.setProgress(score);
    }

    private void fetchLessonsDone() {
        int studentId = session.getStudentId();
        if (studentId <= 0) return;

        ApiClient.getClient(this).create(ApiService.class)
                .checkModulesComplete(studentId)
                .enqueue(new Callback<CheckModulesCompleteResponse>() {
                    @Override
                    public void onResponse(Call<CheckModulesCompleteResponse> call,
                                           Response<CheckModulesCompleteResponse> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            tvStatLessons.setText(
                                    String.valueOf(response.body().getCompletedCount()));
                        }
                    }
                    @Override
                    public void onFailure(Call<CheckModulesCompleteResponse> call, Throwable t) { }
                });
    }

    @Override
    protected int getNavIndex() { return 2; }
}