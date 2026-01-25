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
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.QuizQuestionsResponse;
import com.example.literise.models.QuizSubmitRequest;
import com.example.literise.models.QuizSubmitResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private int nodeId;
    private int moduleId;
    private int lessonNumber;
    private String moduleName;
    private int placementLevel;
    private SessionManager sessionManager;

    // Quiz State
    private List<QuizQuestionsResponse.Question> questions = new ArrayList<>();
    private Map<Integer, Integer> selectedAnswers = new HashMap<>();
    private int currentQuestionIndex = 0;
    private int totalQuestions = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        sessionManager = new SessionManager(this);

        // Get data from intent
        nodeId = getIntent().getIntExtra("node_id", 1);
        moduleId = getIntent().getIntExtra("module_id", 1);
        lessonNumber = getIntent().getIntExtra("lesson_number", 1);
        moduleName = getIntent().getStringExtra("module_name");
        placementLevel = sessionManager.getPlacementLevel();

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
     * Load quiz questions from API
     */
    private void loadQuizQuestions() {
        progressBar.setVisibility(View.VISIBLE);
        btnNext.setEnabled(false);

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getQuizQuestions(nodeId, placementLevel).enqueue(new Callback<QuizQuestionsResponse>() {
            @Override
            public void onResponse(Call<QuizQuestionsResponse> call, Response<QuizQuestionsResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    questions = response.body().getQuiz().getQuestions();
                    totalQuestions = questions.size();
                    displayCurrentQuestion();
                } else {
                    Toast.makeText(QuizActivity.this, "Failed to load quiz questions", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<QuizQuestionsResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    /**
     * Display current question
     */
    private void displayCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            submitQuiz();
            return;
        }

        QuizQuestionsResponse.Question question = questions.get(currentQuestionIndex);

        // Update UI
        tvQuestionNumber.setText("🤔 Question " + (currentQuestionIndex + 1) + " of " + totalQuestions);
        tvProgress.setText((currentQuestionIndex + 1) + "/" + totalQuestions);
        tvQuestionText.setText(question.getQuestionText());
        rbOption1.setText("A) " + question.getOption1());
        rbOption2.setText("B) " + question.getOption2());
        rbOption3.setText("C) " + question.getOption3());
        rbOption4.setText("D) " + question.getOption4());

        // Clear previous selection
        radioGroupOptions.clearCheck();
        btnNext.setEnabled(false);

        // Update button text
        if (currentQuestionIndex == totalQuestions - 1) {
            btnNext.setText("✅ Submit Quiz");
        } else {
            btnNext.setText("Next Question ➡️");
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

        // Store answer with question ID as key
        QuizQuestionsResponse.Question currentQuestion = questions.get(currentQuestionIndex);
        selectedAnswers.put(currentQuestion.getQuestionId(), selectedOptionIndex);

        // Move to next question or submit
        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    /**
     * Submit quiz and get adaptive decision from API
     */
    private void submitQuiz() {
        progressBar.setVisibility(View.VISIBLE);
        btnNext.setEnabled(false);

        int studentId = sessionManager.getStudentId();

        // Create submit request with student answers
        QuizSubmitRequest request = new QuizSubmitRequest(studentId, nodeId, placementLevel, selectedAnswers);

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.submitQuiz(request).enqueue(new Callback<QuizSubmitResponse>() {
            @Override
            public void onResponse(Call<QuizSubmitResponse> call, Response<QuizSubmitResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    QuizSubmitResponse result = response.body();
                    showQuizResults(result);
                } else {
                    Toast.makeText(QuizActivity.this, "Failed to submit quiz", Toast.LENGTH_SHORT).show();
                    btnNext.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<QuizSubmitResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(QuizActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                btnNext.setEnabled(true);
            }
        });
    }

    /**
     * Show quiz results and adaptive decision
     */
    private void showQuizResults(QuizSubmitResponse result) {
        Intent intent = new Intent(this, QuizResultActivity.class);
        intent.putExtra("node_id", nodeId);
        intent.putExtra("module_id", moduleId);
        intent.putExtra("lesson_number", lessonNumber);
        intent.putExtra("score_percent", result.getScorePercent());
        intent.putExtra("correct_count", result.getCorrectCount());
        intent.putExtra("total_questions", result.getTotalQuestions());
        intent.putExtra("adaptive_decision", result.getAdaptiveDecision());
        intent.putExtra("xp_awarded", result.getXpAwarded());
        intent.putExtra("placement_level", placementLevel);
        startActivity(intent);
        finish();
    }
}
