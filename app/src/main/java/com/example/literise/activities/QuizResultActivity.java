package com.example.literise.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.AwardBadgeRequest;
import com.example.literise.models.AwardBadgeResponse;
import com.example.literise.utils.BadgeEarnedDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Quiz Result Activity - Shows adaptive decision and XP award
 *
 * Displays:
 * - Score percentage and correct count
 * - Adaptive decision explanation
 * - XP awarded
 * - Next steps (unlock intervention/supplemental/enrichment nodes)
 *
 * Adaptive Decisions:
 * - <70%: Intervention Required (mandatory remedial)
 * - 70-79% + Beginner: Supplemental Practice (optional)
 * - 80-89%: Proceed to Next Lesson
 * - 90%+ + Advanced: Enrichment Available (optional challenge)
 */
public class QuizResultActivity extends AppCompatActivity {

    // UI Components
    private ImageView ivResultIcon;
    private TextView tvResultTitle;
    private TextView tvScore;
    private TextView tvCorrectCount;
    private CardView cardDecision;
    private TextView tvAdaptiveDecision;
    private TextView tvXpAwarded;
    private TextView tvNextSteps;
    private Button btnContinue;
    private Button btnRetakeQuiz;

    // Data
    private int lessonId;
    private int moduleId;
    private int lessonNumber;
    private int nodeId;
    private int scorePercent;
    private int correctCount;
    private int totalQuestions;
    private String adaptiveDecision;
    private int placementLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_result);

        // Get data from intent
        lessonId         = getIntent().getIntExtra("lesson_id", 101);
        moduleId         = getIntent().getIntExtra("module_id", 1);
        lessonNumber     = getIntent().getIntExtra("lesson_number", 1);
        nodeId           = getIntent().getIntExtra("node_id", 0);
        scorePercent     = getIntent().getIntExtra("score_percent", 0);
        correctCount     = getIntent().getIntExtra("correct_count", 0);
        totalQuestions   = getIntent().getIntExtra("total_questions", 5);
        adaptiveDecision = getIntent().getStringExtra("adaptive_decision");
        placementLevel   = getIntent().getIntExtra("placement_level", 2);

        initializeViews();
        setupListeners();
        displayResults();

        // Check badge unlocks after quiz completion
        if (nodeId > 0) checkBadges(nodeId);
    }

    private void initializeViews() {
        ivResultIcon      = findViewById(R.id.ivResultIcon);
        tvResultTitle     = findViewById(R.id.tvResultTitle);
        tvScore           = findViewById(R.id.tvScore);
        tvCorrectCount    = findViewById(R.id.tvCorrectCount);
        cardDecision      = findViewById(R.id.cardDecision);
        tvAdaptiveDecision = findViewById(R.id.tvAdaptiveDecision);
        tvXpAwarded       = findViewById(R.id.tvXpAwarded);
        tvNextSteps       = findViewById(R.id.tvNextSteps);
        btnContinue       = findViewById(R.id.btnContinue);
        btnRetakeQuiz     = findViewById(R.id.btnRetakeQuiz);
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> {
            // Return to module ladder - appropriate nodes will be unlocked
            finish();
        });

        btnRetakeQuiz.setOnClickListener(v -> {
            // Restart quiz
            Intent intent = new Intent(this, QuizActivity.class);
            intent.putExtra("lesson_id", lessonId);
            intent.putExtra("module_id", moduleId);
            intent.putExtra("lesson_number", lessonNumber);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Display quiz results and adaptive decision
     */
    private void displayResults() {
        tvScore.setText(scorePercent + "%");
        tvCorrectCount.setText(correctCount + " / " + totalQuestions);

        int xpAwarded = calculateXP(scorePercent);
        tvXpAwarded.setText("+" + xpAwarded + " XP Earned!");

        displayAdaptiveDecision();
    }

    /**
     * Calculate XP award based on score
     */
    private int calculateXP(int score) {
        if (score >= 90) return 100;
        else if (score >= 80) return 80;
        else if (score >= 70) return 60;
        else if (score >= 60) return 40;
        else return 20;
    }

    /**
     * Call award_badge.php to check and award any newly unlocked badges,
     * then show the earned badge dialog if any new badges were awarded.
     */
    private void checkBadges(int nodeId) {
        SessionManager session = new SessionManager(this);
        int studentId = session.getStudentId();
        if (studentId <= 0) return;

        ApiClient.getClient(this).create(ApiService.class)
                .awardBadge(new AwardBadgeRequest(studentId, nodeId))
                .enqueue(new Callback<AwardBadgeResponse>() {
                    @Override
                    public void onResponse(Call<AwardBadgeResponse> call,
                                           Response<AwardBadgeResponse> response) {
                        if (!response.isSuccessful() || response.body() == null) return;
                        AwardBadgeResponse body = response.body();
                        if (body.getNewBadges() != null && !body.getNewBadges().isEmpty()) {
                            BadgeEarnedDialog.show(QuizResultActivity.this, body.getNewBadges(), null);
                        }
                    }
                    @Override
                    public void onFailure(Call<AwardBadgeResponse> call, Throwable t) {
                        // Silently fail — badge check is non-critical
                    }
                });
    }

    /**
     * Display adaptive decision with Leo mascot, badge colour, and next-steps text.
     *
     * Decision pill colours:
     *   INTERVENTION  → red   bg #FEE2E2 / text #991B1B
     *   SUPPLEMENTAL  → blue  bg #DBEAFE / text #1E40AF
     *   ENRICHMENT    → amber bg #FEF3C7 / text #92400E
     *   PROCEED       → green bg #DCFCE7 / text #166534
     */
    private void displayAdaptiveDecision() {
        switch (adaptiveDecision) {

            case "ADD_INTERVENTION":
                ivResultIcon.setImageResource(R.drawable.leo_thinking);
                tvResultTitle.setText("Keep Practicing!");
                cardDecision.setCardBackgroundColor(Color.parseColor("#FEE2E2"));
                tvAdaptiveDecision.setText("⚠  Intervention Required");
                tvAdaptiveDecision.setTextColor(Color.parseColor("#991B1B"));
                tvNextSteps.setText(
                        "Your score shows there are some areas to strengthen.\n\n" +
                        "✅ An Intervention Node has been unlocked:\n" +
                        "• Simplified content\n" +
                        "• Extra practice exercises\n" +
                        "• Step-by-step guidance\n\n" +
                        "Complete this before moving forward!"
                );
                btnRetakeQuiz.setVisibility(android.view.View.VISIBLE);
                break;

            case "ADD_SUPPLEMENTAL":
                ivResultIcon.setImageResource(R.drawable.leo_happy);
                tvResultTitle.setText("Good Progress!");
                cardDecision.setCardBackgroundColor(Color.parseColor("#DBEAFE"));
                tvAdaptiveDecision.setText("💡  Supplemental Practice Available");
                tvAdaptiveDecision.setTextColor(Color.parseColor("#1E40AF"));
                tvNextSteps.setText(
                        "You passed! As a beginner, a little extra practice will help.\n\n" +
                        "💡 A Supplemental Node is available (optional):\n" +
                        "• Additional examples\n" +
                        "• Practice exercises\n" +
                        "• Skill reinforcement\n\n" +
                        "You can continue or take the supplemental lesson first."
                );
                btnRetakeQuiz.setVisibility(android.view.View.GONE);
                break;

            case "OFFER_ENRICHMENT":
                ivResultIcon.setImageResource(R.drawable.leo_success_confetti);
                tvResultTitle.setText("Excellent Work!");
                cardDecision.setCardBackgroundColor(Color.parseColor("#FEF3C7"));
                tvAdaptiveDecision.setText("🏆  Enrichment Available");
                tvAdaptiveDecision.setTextColor(Color.parseColor("#92400E"));
                tvNextSteps.setText(
                        "Outstanding! You've truly mastered this content.\n\n" +
                        "🚀 An Enrichment Node is available (optional):\n" +
                        "• Advanced concepts\n" +
                        "• Critical thinking challenges\n" +
                        "• Extension activities\n\n" +
                        "Continue to the next lesson or challenge yourself!"
                );
                btnRetakeQuiz.setVisibility(android.view.View.GONE);
                break;

            case "PROCEED":
            default:
                ivResultIcon.setImageResource(R.drawable.leo_result_celebrate);
                tvResultTitle.setText("Amazing Work!");
                cardDecision.setCardBackgroundColor(Color.parseColor("#DCFCE7"));
                tvAdaptiveDecision.setText("✓  Ready to Proceed");
                tvAdaptiveDecision.setTextColor(Color.parseColor("#166534"));
                tvNextSteps.setText(
                        "You've successfully completed this lesson!\n\n" +
                        "✨ The next lesson is now unlocked:\n" +
                        "• Continue your learning journey\n" +
                        "• Build on what you've learned\n" +
                        "• Keep up the great work!\n\n" +
                        "Tap Continue to move forward."
                );
                btnRetakeQuiz.setVisibility(android.view.View.GONE);
                break;
        }
    }
}
