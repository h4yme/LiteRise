package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * PHASE 3: Quiz Activity with Adaptive Decisions
 *
 * Displays 5-10 quiz questions and determines adaptive branching:
 * - Score <70%: ADD_INTERVENTION (mandatory remedial node)
 * - Score 70-79% + Beginner: ADD_SUPPLEMENTAL (optional practice)
 * - Score 80-89%: PROCEED (next node unlocks)
 * - Score 90%+ + Advanced: OFFER_ENRICHMENT (optional challenge)
 *
 * Flow:
 * 1. Load quiz questions from API (get_quiz_questions.php)
 * 2. Display questions one by one
 * 3. Submit answers to API (submit_quiz.php)
 * 4. Receive adaptive decision and XP award
 * 5. Mark QuizCompleted = 1
 * 6. Show results and unlock appropriate nodes
 */
public class QuizActivity extends AppCompatActivity {

    // UI Components
    private ImageView btnBack;
    private TextView tvQuizTitle;
    private TextView tvQuestionNumber;
    private TextView tvQuestionText;
    private RadioGroup radioGroupOptions;
    private RadioButton rbOption1, rbOption2, rbOption3, rbOption4;
    private Button btnNext;
    private ProgressBar progressBar;
    private TextView tvProgress;

    // Data
    private int lessonId;
    private int moduleId;
    private int lessonNumber;
    private String moduleName;
    private int placementLevel;
    private SessionManager sessionManager;

    // Quiz State
    private List<QuizQuestion> questions = new ArrayList<>();
    private List<Integer> selectedAnswers = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int totalQuestions = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        sessionManager = new SessionManager(this);

        // Get data from intent
        lessonId = getIntent().getIntExtra("lesson_id", 101);
        moduleId = getIntent().getIntExtra("module_id", 1);
        lessonNumber = getIntent().getIntExtra("lesson_number", 1);
        moduleName = getIntent().getStringExtra("module_name");
        placementLevel = getIntent().getIntExtra("placement_level",
                sessionManager.getPlacementLevel());

        initializeViews();
        setupListeners();
        loadQuizQuestions();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        rbOption1 = findViewById(R.id.rbOption1);
        rbOption2 = findViewById(R.id.rbOption2);
        rbOption3 = findViewById(R.id.rbOption3);
        rbOption4 = findViewById(R.id.rbOption4);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        // Set quiz title
        tvQuizTitle.setText("Quiz " + lessonNumber);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            // Warn user about losing progress
            Toast.makeText(this, "Quiz progress will be lost", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Enable next button when option is selected
        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
            btnNext.setEnabled(true);
        });

        btnNext.setOnClickListener(v -> {
            handleNextButton();
        });
    }

    /**
     * Load quiz questions from API or dummy data
     */
    private void loadQuizQuestions() {
        progressBar.setVisibility(View.VISIBLE);

        // TODO: Replace with actual API call to get_quiz_questions.php
        // For now, using dummy questions

        generateDummyQuestions();

        progressBar.setVisibility(View.GONE);
        displayCurrentQuestion();
    }

    /**
     * Generate dummy quiz questions
     * TODO: Replace with API data
     */
    private void generateDummyQuestions() {
        questions.clear();

        for (int i = 1; i <= totalQuestions; i++) {
            QuizQuestion question = new QuizQuestion();
            question.questionId = i;
            question.questionText = "Question " + i + ": What is the main idea of the passage?";
            question.option1 = "Option A: The story is about friendship";
            question.option2 = "Option B: The story is about adventure";
            question.option3 = "Option C: The story is about learning";
            question.option4 = "Option D: The story is about nature";
            question.correctAnswer = 1; // Option A
            questions.add(question);
        }
    }

    /**
     * Display current question
     */
    private void displayCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            submitQuiz();
            return;
        }

        QuizQuestion question = questions.get(currentQuestionIndex);

        // Update UI
        tvQuestionNumber.setText("Question " + (currentQuestionIndex + 1) + " of " + totalQuestions);
        tvProgress.setText((currentQuestionIndex + 1) + "/" + totalQuestions);
        tvQuestionText.setText(question.questionText);
        rbOption1.setText(question.option1);
        rbOption2.setText(question.option2);
        rbOption3.setText(question.option3);
        rbOption4.setText(question.option4);

        // Clear previous selection
        radioGroupOptions.clearCheck();
        btnNext.setEnabled(false);

        // Update button text
        if (currentQuestionIndex == totalQuestions - 1) {
            btnNext.setText("Submit Quiz");
        } else {
            btnNext.setText("Next Question →");
        }
    }

    /**
     * Handle next button click
     */
    private void handleNextButton() {
        // Save selected answer
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        int selectedOptionIndex = 0;

        if (selectedId == R.id.rbOption1) selectedOptionIndex = 1;
        else if (selectedId == R.id.rbOption2) selectedOptionIndex = 2;
        else if (selectedId == R.id.rbOption3) selectedOptionIndex = 3;
        else if (selectedId == R.id.rbOption4) selectedOptionIndex = 4;

        selectedAnswers.add(selectedOptionIndex);

        // Move to next question or submit
        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    /**
     * Submit quiz and get adaptive decision
     */
    private void submitQuiz() {
        progressBar.setVisibility(View.VISIBLE);
        btnNext.setEnabled(false);

        // Calculate score
        int correctCount = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (selectedAnswers.get(i) == questions.get(i).correctAnswer) {
                correctCount++;
            }
        }

        int scorePercent = (correctCount * 100) / totalQuestions;

        // Determine adaptive decision
        String adaptiveDecision = determineAdaptiveDecision(scorePercent);

        // TODO: API call to submit_quiz.php with answers
        // Receive XP award and adaptive decision

        // Show results
        showQuizResults(scorePercent, correctCount, adaptiveDecision);
    }

    /**
     * Determine adaptive decision based on score and placement level
     */
    private String determineAdaptiveDecision(int scorePercent) {
        if (scorePercent < 70) {
            return "ADD_INTERVENTION"; // Mandatory remedial node
        } else if (scorePercent >= 70 && scorePercent < 80 && placementLevel == 1) {
            return "ADD_SUPPLEMENTAL"; // Optional practice for beginners
        } else if (scorePercent >= 90 && placementLevel == 3) {
            return "OFFER_ENRICHMENT"; // Optional challenge for advanced
        } else {
            return "PROCEED"; // Normal progression
        }
    }

    /**
     * Show quiz results and adaptive decision
     */
    private void showQuizResults(int scorePercent, int correctCount, String decision) {
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("lesson_id", lessonId);
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", lessonNumber);
        intent.putExtra("score_percent", scorePercent);
        intent.putExtra("correct_count", correctCount);
        intent.putExtra("total_questions", totalQuestions);
        intent.putExtra("adaptive_decision", decision);
        intent.putExtra("placement_level", placementLevel);
        startActivity(intent);
        finish();
    }

    /**
     * Inner class for quiz question
     */
    private static class QuizQuestion {
        int questionId;
        String questionText;
        String option1;
        String option2;
        String option3;
        String option4;
        int correctAnswer; // 1-4
    }
}
