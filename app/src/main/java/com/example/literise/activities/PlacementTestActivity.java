package com.example.literise.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android:widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.google.android.material.button.MaterialButton;

public class PlacementTestActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView btnSkip;
    private TextView tvCategoryIcon, tvCategoryName;
    private TextView tvQuestionType;
    private FrameLayout questionContainer;
    private TextView tvLeoHint;
    private MaterialButton btnContinue, btnRetry;

    // Question tracking
    private int currentQuestionNumber = 1;
    private int totalQuestions = 25;
    private int currentCategory = 1;
    private String selectedAnswer = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_test);

        initViews();
        setupListeners();
        loadQuestion();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);
        btnSkip = findViewById(R.id.btnSkip);
        tvCategoryIcon = findViewById(R.id.tvCategoryIcon);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvQuestionType = findViewById(R.id.tvQuestionType);
        questionContainer = findViewById(R.id.questionContainer);
        tvLeoHint = findViewById(R.id.tvLeoHint);
        btnContinue = findViewById(R.id.btnContinue);
        btnRetry = findViewById(R.id.btnRetry);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());
        
        btnSkip.setOnClickListener(v -> {
            // Skip to next question
            nextQuestion();
        });

        btnContinue.setOnClickListener(v -> {
            if (!selectedAnswer.isEmpty()) {
                // Check answer and proceed
                checkAnswer();
            }
        });

        btnRetry.setOnClickListener(v -> {
            // Reload current question
            selectedAnswer = "";
            loadQuestion();
        });
    }

    private void loadQuestion() {
        // Update progress
        updateProgress();

        // Update category
        updateCategory();

        // Load question fragment
        loadMultipleChoiceQuestion();

        // Reset buttons
        btnContinue.setEnabled(false);
        btnRetry.setVisibility(View.GONE);
    }

    private void updateProgress() {
        int progress = (int) ((currentQuestionNumber / (float) totalQuestions) * 100);
        progressBar.setProgress(progress);
        tvProgress.setText(currentQuestionNumber + "/" + totalQuestions);
    }

    private void updateCategory() {
        String categoryIcon = "";
        String categoryName = "";

        switch (currentCategory) {
            case 1:
                categoryIcon = "📚";
                categoryName = "Category 1: Oral Language";
                break;
            case 2:
                categoryIcon = "🔤";
                categoryName = "Category 2: Word Knowledge";
                break;
            case 3:
                categoryIcon = "📖";
                categoryName = "Category 3: Reading Comprehension";
                break;
            case 4:
                categoryIcon = "✏️";
                categoryName = "Category 4: Language Structure";
                break;
        }

        tvCategoryIcon.setText(categoryIcon);
        tvCategoryName.setText(categoryName);
    }

    private void loadMultipleChoiceQuestion() {
        // Inflate multiple choice question layout
        questionContainer.removeAllViews();
        View questionView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_question_multiple_choice, questionContainer, false);
        questionContainer.addView(questionView);

        // Set question type
        tvQuestionType.setText("Choose the Correct Word");

        // Set Leo hint
        tvLeoHint.setText("Pick the right word!");

        // Setup option click listeners
        setupOptionClickListeners(questionView);
    }

    private void setupOptionClickListeners(View questionView) {
        CardView optionA = questionView.findViewById(R.id.optionA);
        CardView optionB = questionView.findViewById(R.id.optionB);
        CardView optionC = questionView.findViewById(R.id.optionC);
        CardView optionD = questionView.findViewById(R.id.optionD);

        View.OnClickListener optionClickListener = v -> {
            // Clear all selections
            optionA.setCardBackgroundColor(getColor(android.R.color.white));
            optionB.setCardBackgroundColor(getColor(android.R.color.white));
            optionC.setCardBackgroundColor(getColor(android.R.color.white));
            optionD.setCardBackgroundColor(getColor(android.R.color.white));

            // Highlight selected
            ((CardView) v).setCardBackgroundColor(getColor(R.color.option_selected));

            // Store selected answer
            if (v.getId() == R.id.optionA) selectedAnswer = "A";
            else if (v.getId() == R.id.optionB) selectedAnswer = "B";
            else if (v.getId() == R.id.optionC) selectedAnswer = "C";
            else if (v.getId() == R.id.optionD) selectedAnswer = "D";

            // Enable continue button
            btnContinue.setEnabled(true);
        };

        optionA.setOnClickListener(optionClickListener);
        optionB.setOnClickListener(optionClickListener);
        optionC.setOnClickListener(optionClickListener);
        optionD.setOnClickListener(optionClickListener);
    }

    private void checkAnswer() {
        // TODO: Implement IRT answer checking
        // For now, just move to next question
        nextQuestion();
    }

    private void nextQuestion() {
        currentQuestionNumber++;

        if (currentQuestionNumber > totalQuestions) {
            // Test complete - show results
            showResults();
        } else {
            // Update category based on question number
            if (currentQuestionNumber <= 7) currentCategory = 1;
            else if (currentQuestionNumber <= 14) currentCategory = 2;
            else if (currentQuestionNumber <= 21) currentCategory = 3;
            else currentCategory = 4;

            selectedAnswer = "";
            loadQuestion();
        }
    }

    private void showResults() {
        // TODO: Navigate to PlacementResultActivity
        // For now, go back to dashboard
        finish();
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before exiting
        // For now, just finish
        super.onBackPressed();
    }
}
