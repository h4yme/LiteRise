package com.example.literise.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.CreateSessionRequest;
import com.example.literise.models.Question;
import com.example.literise.models.ResponseModel;
import com.example.literise.models.SessionResponse;
import com.example.literise.models.SubmitRequest;
import com.example.literise.models.SubmitResponse;
import com.example.literise.utils.Constants;
import com.example.literise.utils.CustomToast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreAssessmentActivity extends AppCompatActivity {

    private TextView tvTitle, tvProgress, tvPassage, tvPassageText, tvQuestion, tvContinue;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    private ImageView ivMic;
    private ProgressBar progressBar;

    private List<Question> questionList = new ArrayList<>();
    private List<ResponseModel> responses = new ArrayList<>();
    private int currentIndex = 0;
    private SessionManager session;
    private int sessionId = 0;
    private long questionStartTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_pre_assessment);

        session = new SessionManager(this);
        initializeViews();

        // Create session first, then load questions
        createTestSession();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvProgress = findViewById(R.id.tvProgress);
        tvPassage = findViewById(R.id.tvPassage);
        tvPassageText = findViewById(R.id.tvPassageText);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvContinue = findViewById(R.id.tvContinue);

        btnOptionA = findViewById(R.id.btnOptionA);
        btnOptionB = findViewById(R.id.btnOptionB);
        btnOptionC = findViewById(R.id.btnOptionC);
        btnOptionD = findViewById(R.id.btnOptionD);
        ivMic = findViewById(R.id.ivMic);
        progressBar = findViewById(R.id.progressBar);

        btnOptionA.setOnClickListener(v -> selectAnswer("A"));
        btnOptionB.setOnClickListener(v -> selectAnswer("B"));
        btnOptionC.setOnClickListener(v -> selectAnswer("C"));
        btnOptionD.setOnClickListener(v -> selectAnswer("D"));
        tvContinue.setOnClickListener(v -> goToNextQuestion());
    }

    /**
     * Create a test session before starting the assessment
     */
    private void createTestSession() {
        int studentId = session.getStudentId();
        CreateSessionRequest request = new CreateSessionRequest(studentId, Constants.SESSION_PRE_ASSESSMENT);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.createSession(request).enqueue(new Callback<SessionResponse>() {
            @Override
            public void onResponse(Call<SessionResponse> call, Response<SessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SessionResponse sessionResponse = response.body();
                    sessionId = sessionResponse.getSessionId();

                    // Save session ID in SharedPreferences
                    session.saveSessionId(sessionId);

                    CustomToast.showSuccess(PreAssessmentActivity.this,
                            "Session created! Starting assessment...");

                    // Now load questions
                    loadQuestions();
                } else {
                    CustomToast.showError(PreAssessmentActivity.this,
                            "Failed to create session. Please try again.");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SessionResponse> call, Throwable t) {
                CustomToast.showError(PreAssessmentActivity.this,
                        "Connection error: " + t.getMessage());
                finish();
            }
        });
    }

    /**
     * Load pre-assessment questions from API
     */
    private void loadQuestions() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getPreAssessmentItems().enqueue(new Callback<List<Question>>() {
            @Override
            public void onResponse(Call<List<Question>> call, Response<List<Question>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    questionList = response.body();
                    if (questionList.isEmpty()) {
                        CustomToast.showWarning(PreAssessmentActivity.this,
                                "No questions available");
                        finish();
                    } else {
                        showQuestion();
                    }
                } else {
                    CustomToast.showError(PreAssessmentActivity.this,
                            "Failed to load questions");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Question>> call, Throwable t) {
                CustomToast.showError(PreAssessmentActivity.this,
                        "Connection error: " + t.getMessage());
                finish();
            }
        });
    }

    /**
     * Display current question
     */
    @SuppressLint("SetTextI18n")
    private void showQuestion() {
        if (currentIndex >= questionList.size()) {
            submitResponses();
            return;
        }

        Question q = questionList.get(currentIndex);

        // Start timer for this question
        questionStartTime = System.currentTimeMillis();

        tvTitle.setText("Placement Test");
        tvQuestion.setText(q.getQuestionText());
        tvPassageText.setText(q.getPassageText() == null ? "" : q.getPassageText());

        btnOptionA.setText("a) " + q.getOptionA());
        btnOptionB.setText("b) " + q.getOptionB());
        btnOptionC.setText("c) " + q.getOptionC());
        btnOptionD.setText("d) " + q.getOptionD());

        // Hide empty options
        btnOptionA.setVisibility(q.getOptionA().isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        btnOptionB.setVisibility(q.getOptionB().isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        btnOptionC.setVisibility(q.getOptionC().isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);
        btnOptionD.setVisibility(q.getOptionD().isEmpty() ? android.view.View.GONE : android.view.View.VISIBLE);

        tvProgress.setText((currentIndex + 1) + "/" + questionList.size() + " Questions");
        progressBar.setProgress((int) (((float) (currentIndex + 1) / questionList.size()) * 100));

        enableOptions();
    }

    /**
     * Handle answer selection
     */
    private void selectAnswer(String choice) {
        Question q = questionList.get(currentIndex);

        // Calculate time spent
        long timeSpentMs = System.currentTimeMillis() - questionStartTime;
        float timeSpentSec = timeSpentMs / 1000f;

        ResponseModel response = new ResponseModel();
        response.setItemId(q.getItemId());
        response.setSelectedOption(choice);
        response.setCorrect(q.getCorrectOption().equalsIgnoreCase(choice));
        response.setTimeTakenSec(timeSpentSec);

        responses.add(response);
        disableOptions();

        // Show feedback (optional)
        if (response.isCorrect()) {
            CustomToast.showSuccess(this, "Correct!");
        } else {
            CustomToast.showInfo(this, "Let's move on...");
        }
    }

    private void disableOptions() {
        btnOptionA.setEnabled(false);
        btnOptionB.setEnabled(false);
        btnOptionC.setEnabled(false);
        btnOptionD.setEnabled(false);
    }

    private void enableOptions() {
        btnOptionA.setEnabled(true);
        btnOptionB.setEnabled(true);
        btnOptionC.setEnabled(true);
        btnOptionD.setEnabled(true);
    }

    /**
     * Move to next question
     */
    private void goToNextQuestion() {
        if (responses.size() <= currentIndex) {
            CustomToast.showWarning(this, "Please select an answer first");
            return;
        }

        if (currentIndex < questionList.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            submitResponses();
        }
    }

    /**
     * Submit all responses to server with IRT calculation
     */
    private void submitResponses() {
        if (responses.isEmpty()) {
            CustomToast.showWarning(this, "No responses to submit");
            finish();
            return;
        }

        int studentId = session.getStudentId();
        SubmitRequest submitRequest = new SubmitRequest(studentId, sessionId, responses);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.submitResponses(submitRequest).enqueue(new Callback<SubmitResponse>() {
            @Override
            public void onResponse(Call<SubmitResponse> call, Response<SubmitResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubmitResponse result = response.body();

                    // Update session with new ability
                    session.saveAbility(result.getFinalTheta());

                    // Show results
                    showResults(result);
                } else {
                    CustomToast.showError(PreAssessmentActivity.this,
                            "Failed to submit responses");
                    finish();
                }
            }

            @Override
            public void onFailure(Call<SubmitResponse> call, Throwable t) {
                CustomToast.showError(PreAssessmentActivity.this,
                        "Connection error: " + t.getMessage());
                finish();
            }
        });
    }

    /**
     * Display assessment results
     */
    private void showResults(SubmitResponse result) {
        // For now, show a toast with results
        // TODO: Create ResultsActivity to show detailed results
        String message = String.format(
                "Assessment Complete!\n" +
                        "Accuracy: %.1f%%\n" +
                        "Ability Score: %.2f\n" +
                        "Correct: %d/%d",
                result.getAccuracy(),
                result.getFinalTheta(),
                result.getCorrectAnswers(),
                result.getTotalQuestions()
        );

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Navigate to MainActivity
        Intent intent = new Intent(PreAssessmentActivity.this, com.example.literise.MainActivity.class);
        intent.putExtra("assessment_complete", true);
        intent.putExtra("final_theta", result.getFinalTheta());
        intent.putExtra("accuracy", result.getAccuracy());
        startActivity(intent);
        finish();
    }
}
