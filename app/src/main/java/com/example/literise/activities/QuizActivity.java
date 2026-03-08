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
    private Map<Integer, String> selectedAnswers = new HashMap<>(); // questionId → answer text
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
        placementLevel = convertPlacementLevelToInt(sessionManager.getPlacementLevel());

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

        // Show only options that have content
        setOption(rbOption1, question.getOptionA(), "A");
        setOption(rbOption2, question.getOptionB(), "B");
        setOption(rbOption3, question.getOptionC(), "C");
        setOption(rbOption4, question.getOptionD(), "D");

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
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        QuizQuestionsResponse.Question currentQuestion = questions.get(currentQuestionIndex);

        // Store answer text (matches CorrectAnswer in DB)
        String answerText = "";
        if (selectedId == R.id.rbOption1) answerText = currentQuestion.getOptionA();
        else if (selectedId == R.id.rbOption2) answerText = currentQuestion.getOptionB();
        else if (selectedId == R.id.rbOption3) answerText = currentQuestion.getOptionC();
        else if (selectedId == R.id.rbOption4) answerText = currentQuestion.getOptionD();

        selectedAnswers.put(currentQuestion.getQuestionId(), answerText);

        currentQuestionIndex++;
        displayCurrentQuestion();
    }

    private void setOption(RadioButton rb, String text, String label) {
        if (text != null && !text.isEmpty()) {
            rb.setText(label + ") " + text);
            rb.setVisibility(View.VISIBLE);
        } else {
            rb.setVisibility(View.GONE);
        }
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
        intent.putExtra("score_percent", result.getResult().getScorePercent());
        intent.putExtra("correct_count", result.getResult().getCorrectCount());
        intent.putExtra("total_questions", result.getResult().getTotalQuestions());
        intent.putExtra("adaptive_decision", result.getResult().getAdaptiveDecision());
        intent.putExtra("xp_awarded", result.getResult().getXpAwarded());
        intent.putExtra("placement_level", placementLevel);
        startActivity(intent);
        finish();
    }

    /**
     * Convert placement level string to integer for API calls
     */
    private int convertPlacementLevelToInt(String levelString) {
        if (levelString == null) return 2; // Default to intermediate

        if (levelString.contains("2") || levelString.toLowerCase().contains("beginner")) {
            return 1;
        } else if (levelString.contains("4") || levelString.toLowerCase().contains("advanced")) {
            return 3;
        } else {
            return 2; // Grade 3 or intermediate
        }
    }
}