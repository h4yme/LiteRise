package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.database.QuestionBankHelper;
import com.example.literise.models.PlacementQuestion;
import com.example.literise.utils.IRTEngine;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class PlacementTestActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private TextView btnSkip;
    private TextView tvCategoryIcon, tvCategoryName;
    private TextView tvQuestionType;
    private FrameLayout questionContainer;
    private LinearLayout leoHintContainer;
    private TextView tvLeoHint;
    private MaterialButton btnContinue, btnRetry;

    // IRT Engine and Question Bank
    private IRTEngine irtEngine;
    private QuestionBankHelper questionBankHelper;
    private PlacementQuestion currentQuestion;
    private List<PlacementQuestion> categoryQuestions;

    // Question tracking
    private int currentQuestionNumber = 1;
    private int totalQuestions = 25;
    private int currentCategory = 1;
    private String selectedAnswer = "";
    private int questionsPerCategory = 6; // Approximate

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_test);

        // Initialize IRT Engine and Question Bank
        irtEngine = new IRTEngine();
        questionBankHelper = new QuestionBankHelper(this);

        initViews();
        setupListeners();
        loadNextQuestion();
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
        leoHintContainer = findViewById(R.id.leoHintContainer);
        tvLeoHint = findViewById(R.id.tvLeoHint);
        btnContinue = findViewById(R.id.btnContinue);
        btnRetry = findViewById(R.id.btnRetry);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> onBackPressed());

        btnSkip.setOnClickListener(v -> {
            // Skip to next question (counts as incorrect)
            checkAnswer();
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
            displayCurrentQuestion();
        });
    }

    private void loadNextQuestion() {
        // Update category based on question number
        updateCurrentCategory();

        // Load questions for current category
        categoryQuestions = questionBankHelper.getQuestionsByCategory(currentCategory);

        // Use IRT engine to select best question
        currentQuestion = irtEngine.selectNextQuestion(categoryQuestions);

        if (currentQuestion != null) {
            displayCurrentQuestion();
        } else {
            // No more questions in category, move to next category or finish
            if (currentCategory < 4) {
                currentCategory++;
                loadNextQuestion();
            } else {
                showResults();
            }
        }
    }

    private void displayCurrentQuestion() {
        // Update progress
        updateProgress();

        // Update category badge
        updateCategoryBadge();

        // Load question based on type
        loadQuestionView();

        // Show Leo hint
        showLeoHint();

        // Reset buttons
        btnContinue.setEnabled(false);
        btnRetry.setVisibility(View.GONE);
        selectedAnswer = "";
    }

    private void updateProgress() {
        int progress = (int) ((currentQuestionNumber / (float) totalQuestions) * 100);
        progressBar.setProgress(progress);
        tvProgress.setText(currentQuestionNumber + "/" + totalQuestions);
    }

    private void updateCurrentCategory() {
        if (currentQuestionNumber <= questionsPerCategory) {
            currentCategory = 1;
        } else if (currentQuestionNumber <= questionsPerCategory * 2) {
            currentCategory = 2;
        } else if (currentQuestionNumber <= questionsPerCategory * 3) {
            currentCategory = 3;
        } else {
            currentCategory = 4;
        }
    }

    private void updateCategoryBadge() {
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

    private void loadQuestionView() {
        if (currentQuestion == null) return;

        // For now, all questions are multiple choice
        // Later: switch based on questionType
        loadMultipleChoiceQuestion();
    }

    private void loadMultipleChoiceQuestion() {
        // Inflate multiple choice question layout
        questionContainer.removeAllViews();
        View questionView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_question_multiple_choice, questionContainer, false);
        questionContainer.addView(questionView);

        // Set question type based on subcategory
        String questionTypeText = getQuestionTypeText(currentQuestion.getSubcategory());
        tvQuestionType.setText(questionTypeText);

        // Set question text
        TextView tvQuestion = questionView.findViewById(R.id.tvQuestion);
        tvQuestion.setText(currentQuestion.getQuestionText());

        // Set answer options
        List<String> options = currentQuestion.getOptions();
        if (options != null && options.size() >= 4) {
            TextView tvOptionA = questionView.findViewById(R.id.tvOptionA);
            TextView tvOptionB = questionView.findViewById(R.id.tvOptionB);
            TextView tvOptionC = questionView.findViewById(R.id.tvOptionC);
            TextView tvOptionD = questionView.findViewById(R.id.tvOptionD);

            tvOptionA.setText(options.get(0));
            tvOptionB.setText(options.get(1));
            tvOptionC.setText(options.get(2));
            tvOptionD.setText(options.get(3));
        }

        // Setup option click listeners
        setupOptionClickListeners(questionView);
    }

    private String getQuestionTypeText(String subcategory) {
        switch (subcategory) {
            case "Vocabulary":
                return "✨ Choose the Correct Word";
            case "Phonological":
                return "🎵 Sound Recognition";
            case "Phonics":
                return "🔤 Letter Sounds";
            case "Word Study":
                return "📝 Word Families";
            case "Narrative":
                return "📖 Story Comprehension";
            case "Informational":
                return "📰 Reading for Information";
            case "Grammar":
                return "✏️ Grammar Skills";
            case "Sentence Construction":
                return "🔨 Build Sentences";
            default:
                return "✨ Answer the Question";
        }
    }

    private void showLeoHint() {
        if (currentQuestion != null && currentQuestion.getLeoHint() != null) {
            leoHintContainer.setVisibility(View.VISIBLE);
            tvLeoHint.setText(currentQuestion.getLeoHint());
        } else {
            leoHintContainer.setVisibility(View.GONE);
        }
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
            if (v.getId() == R.id.optionA) selectedAnswer = currentQuestion.getOptions().get(0);
            else if (v.getId() == R.id.optionB) selectedAnswer = currentQuestion.getOptions().get(1);
            else if (v.getId() == R.id.optionC) selectedAnswer = currentQuestion.getOptions().get(2);
            else if (v.getId() == R.id.optionD) selectedAnswer = currentQuestion.getOptions().get(3);

            // Enable continue button
            btnContinue.setEnabled(true);
        };

        optionA.setOnClickListener(optionClickListener);
        optionB.setOnClickListener(optionClickListener);
        optionC.setOnClickListener(optionClickListener);
        optionD.setOnClickListener(optionClickListener);
    }

    private void checkAnswer() {
        if (currentQuestion == null) return;

        boolean isCorrect = false;

        if (!selectedAnswer.isEmpty()) {
            // Check if answer is correct
            isCorrect = selectedAnswer.equalsIgnoreCase(currentQuestion.getCorrectAnswer());
        }

        // Update IRT engine with result
        irtEngine.updateTheta(currentQuestion, isCorrect);

        // Move to next question
        currentQuestionNumber++;

        if (currentQuestionNumber > totalQuestions) {
            // Test complete - show results
            showResults();
        } else {
            loadNextQuestion();
        }
    }

    private void showResults() {
        // Navigate to PlacementResultActivity with IRT results
        int placementLevel = irtEngine.calculatePlacementLevel();
        String levelName = irtEngine.getPlacementLevelName();
        double accuracy = irtEngine.getAccuracyPercentage();
        int totalAnswered = irtEngine.getTotalAnswered();
        int totalCorrect = irtEngine.getTotalCorrect();
        int[] categoryScores = irtEngine.getCategoryScores();

        // Create intent with results
        Intent intent = new Intent(PlacementTestActivity.this, PlacementResultActivity.class);
        intent.putExtra("placement_level", placementLevel);
        intent.putExtra("level_name", levelName);
        intent.putExtra("accuracy", accuracy);
        intent.putExtra("total_answered", totalAnswered);
        intent.putExtra("total_correct", totalCorrect);
        intent.putExtra("category_scores", categoryScores);

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before exiting
        // For now, just finish
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionBankHelper != null) {
            questionBankHelper.close();
        }
    }
}
