package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.google.android.material.button.MaterialButton;

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
        tvStatLessons.setText("0"); // placeholder — extend SessionManager if needed

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

    @Override
    protected int getNavIndex() { return 2; }
}
