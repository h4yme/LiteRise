package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;

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
    private TextView tvAdaptiveDecision;
    private TextView tvXpAwarded;
    private TextView tvNextSteps;
    private Button btnContinue;
    private Button btnRetakeQuiz;

    // Data
    private int lessonId;
    private int moduleId;
    private int lessonNumber;
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
        lessonId = getIntent().getIntExtra("lesson_id", 101);
        moduleId = getIntent().getIntExtra("module_id", 1);
        lessonNumber = getIntent().getIntExtra("lesson_number", 1);
        scorePercent = getIntent().getIntExtra("score_percent", 0);
        correctCount = getIntent().getIntExtra("correct_count", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 5);
        adaptiveDecision = getIntent().getStringExtra("adaptive_decision");
        placementLevel = getIntent().getIntExtra("placement_level", 2);

        initializeViews();
        setupListeners();
        displayResults();
    }

    private void initializeViews() {
        ivResultIcon = findViewById(R.id.ivResultIcon);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        tvScore = findViewById(R.id.tvScore);
        tvCorrectCount = findViewById(R.id.tvCorrectCount);
        tvAdaptiveDecision = findViewById(R.id.tvAdaptiveDecision);
        tvXpAwarded = findViewById(R.id.tvXpAwarded);
        tvNextSteps = findViewById(R.id.tvNextSteps);
        btnContinue = findViewById(R.id.btnContinue);
        btnRetakeQuiz = findViewById(R.id.btnRetakeQuiz);
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
        // Display score
        tvScore.setText(scorePercent + "%");
        tvCorrectCount.setText(correctCount + " / " + totalQuestions + " correct");

        // Calculate XP based on performance
        int xpAwarded = calculateXP(scorePercent);
        tvXpAwarded.setText("+" + xpAwarded + " XP");

        // Display adaptive decision and next steps
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
     * Display adaptive decision with explanation
     */
    private void displayAdaptiveDecision() {
        switch (adaptiveDecision) {
            case "ADD_INTERVENTION":
                // Score <70% - Mandatory remedial
                ivResultIcon.setImageResource(R.drawable.ic_warning);
                tvResultTitle.setText("📚 Additional Practice Needed");
                tvResultTitle.setTextColor(getResources().getColor(R.color.orange_500));
                tvAdaptiveDecision.setText("Intervention Required");
                tvNextSteps.setText(
                    "Your score indicates you need more practice on this topic.\n\n" +
                    "✅ An Intervention Node has been unlocked with:\n" +
                    "• Simplified content\n" +
                    "• Extra practice exercises\n" +
                    "• Step-by-step guidance\n\n" +
                    "Complete this before moving forward!"
                );
                btnRetakeQuiz.setVisibility(android.view.View.VISIBLE);
                break;

            case "ADD_SUPPLEMENTAL":
                // Score 70-79% + Beginner - Optional practice
                ivResultIcon.setImageResource(R.drawable.ic_star_half);
                tvResultTitle.setText("✨ Good Progress!");
                tvResultTitle.setTextColor(getResources().getColor(R.color.blue_500));
                tvAdaptiveDecision.setText("Supplemental Practice Available");
                tvNextSteps.setText(
                    "You passed! As a beginner, we recommend extra practice.\n\n" +
                    "💡 A Supplemental Node is available (optional):\n" +
                    "• Additional examples\n" +
                    "• Practice exercises\n" +
                    "• Skill reinforcement\n\n" +
                    "You can continue or take the supplemental lesson first."
                );
                btnRetakeQuiz.setVisibility(android.view.View.GONE);
                break;

            case "OFFER_ENRICHMENT":
                // Score 90%+ + Advanced - Optional challenge
                ivResultIcon.setImageResource(R.drawable.ic_trophy);
                tvResultTitle.setText("🏆 Excellent Work!");
                tvResultTitle.setTextColor(getResources().getColor(R.color.gold));
                tvAdaptiveDecision.setText("Enrichment Available");
                tvNextSteps.setText(
                    "Outstanding! You've mastered this content.\n\n" +
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
                // Score 80-89% - Normal progression
                ivResultIcon.setImageResource(R.drawable.ic_check_circle);
                tvResultTitle.setText("✅ Great Job!");
                tvResultTitle.setTextColor(getResources().getColor(R.color.green_500));
                tvAdaptiveDecision.setText("Ready to Proceed");
                tvNextSteps.setText(
                    "You've successfully completed this lesson!\n\n" +
                    "✨ Next lesson is now unlocked:\n" +
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
