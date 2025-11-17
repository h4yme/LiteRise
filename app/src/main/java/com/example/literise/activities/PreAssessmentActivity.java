package com.example.literise.activities;

import android.annotation.SuppressLint;
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
import com.example.literise.models.PreAssessmentResponse;
import com.example.literise.models.Question;
import com.example.literise.models.ResponseModel;
import com.example.literise.models.SubmitRequest; // ✅ new wrapper class

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_pre_assessment);

        session = new SessionManager(this);
        initializeViews();
        loadQuestions();
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

    private void loadQuestions() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.getPreAssessmentItems().enqueue(new Callback<PreAssessmentResponse>() {
            @Override
            public void onResponse(Call<PreAssessmentResponse> call, Response<PreAssessmentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    questionList = response.body().getItems();
                    if (questionList != null && !questionList.isEmpty()) {
                        showQuestion();
                    } else {
                        Toast.makeText(PreAssessmentActivity.this, "No questions available", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PreAssessmentActivity.this, "Failed to load questions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PreAssessmentResponse> call, Throwable t) {
                Toast.makeText(PreAssessmentActivity.this, "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showQuestion() {
        if (currentIndex >= questionList.size()) {
            submitResponses();
            return;
        }

        Question q = questionList.get(currentIndex);
        tvTitle.setText("Placement Test");
        tvQuestion.setText(q.getQuestionText());
        tvPassageText.setText(q.getPassageText() == null ? "" : q.getPassageText());

        btnOptionA.setText("a) " + q.getOptionA());
        btnOptionB.setText("b) " + q.getOptionB());
        btnOptionC.setText("c) " + q.getOptionC());
        btnOptionD.setText("d) " + q.getOptionD());

        tvProgress.setText((currentIndex + 1) + "/" + questionList.size() + " Questions");
        progressBar.setProgress((int) (((float) (currentIndex + 1) / questionList.size()) * 100));

        enableOptions();
    }

    private void selectAnswer(String choice) {
        Question q = questionList.get(currentIndex);
        ResponseModel response = new ResponseModel();

        response.setItemId(q.getItemId()); // ✅ corrected
        response.setSelectedOption(choice);
        response.setCorrect(q.getCorrectOption().equalsIgnoreCase(choice));

        responses.add(response);
        disableOptions();
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

    private void goToNextQuestion() {
        if (currentIndex < questionList.size() - 1) {
            currentIndex++;
            showQuestion();
        } else {
            submitResponses();
        }
    }

    private void submitResponses() {
        int studentId = session.getStudentId();
        SubmitRequest submitRequest = new SubmitRequest(studentId, responses);

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        apiService.submitResponses(submitRequest).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(PreAssessmentActivity.this, "Assessment complete!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PreAssessmentActivity.this, "Failed to submit responses", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
