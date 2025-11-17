package com.example.literise.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.PreAssessmentResponse;
import com.example.literise.models.Question;
import com.example.literise.models.ResponseModel;
import com.example.literise.models.SubmitRequest;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PreAssessmentActivity extends AppCompatActivity {

    private TextView tvTitle, tvProgress, tvPassage, tvPassageText, tvQuestion;
    private TextView tvItemTypeBadge, tvPronunciationWord, tvPronunciationGuide, tvMicStatus;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    private MaterialButton btnContinue;
    private CardView cardPassage, cardPronunciation, cardQuestion, cardMicButton;
    private ImageView ivMic;
    private ProgressBar progressBar;
    private View gridOptions, containerScrambledWords;

    private List<Question> questionList = new ArrayList<>();
    private List<ResponseModel> responses = new ArrayList<>();
    private int currentIndex = 0;
    private SessionManager session;
    private String selectedAnswer = null;

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
        tvItemTypeBadge = findViewById(R.id.tvItemTypeBadge);
        tvPronunciationWord = findViewById(R.id.tvPronunciationWord);
        tvPronunciationGuide = findViewById(R.id.tvPronunciationGuide);
        tvMicStatus = findViewById(R.id.tvMicStatus);

        btnOptionA = findViewById(R.id.btnOptionA);
        btnOptionB = findViewById(R.id.btnOptionB);
        btnOptionC = findViewById(R.id.btnOptionC);
        btnOptionD = findViewById(R.id.btnOptionD);
        btnContinue = findViewById(R.id.btnContinue);

        cardPassage = findViewById(R.id.cardPassage);
        cardPronunciation = findViewById(R.id.cardPronunciation);
        cardQuestion = findViewById(R.id.cardQuestion);
        cardMicButton = findViewById(R.id.cardMicButton);
        ivMic = findViewById(R.id.ivMic);
        progressBar = findViewById(R.id.progressBar);

        gridOptions = findViewById(R.id.gridOptions);
        containerScrambledWords = findViewById(R.id.containerScrambledWords);

        btnOptionA.setOnClickListener(v -> selectAnswer("A", btnOptionA));
        btnOptionB.setOnClickListener(v -> selectAnswer("B", btnOptionB));
        btnOptionC.setOnClickListener(v -> selectAnswer("C", btnOptionC));
        btnOptionD.setOnClickListener(v -> selectAnswer("D", btnOptionD));
        btnContinue.setOnClickListener(v -> goToNextQuestion());

        cardMicButton.setOnClickListener(v -> recordPronunciation());
    }

    private void loadQuestions() {
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
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
        String itemType = q.getItemType() != null ? q.getItemType() : "";

        // Update header
        tvTitle.setText("Placement Test");
        tvProgress.setText("Question " + (currentIndex + 1) + " of " + questionList.size());
        tvItemTypeBadge.setText(itemType);

        // Update progress bar
        progressBar.setProgress((int) (((float) (currentIndex + 1) / questionList.size()) * 100));

        // Reset selection state
        selectedAnswer = null;
        btnContinue.setEnabled(false);
        clearSelections();

        // Hide all sections by default
        cardPassage.setVisibility(View.GONE);
        cardPronunciation.setVisibility(View.GONE);
        gridOptions.setVisibility(View.VISIBLE);
        containerScrambledWords.setVisibility(View.GONE);

        // Handle different question types
        if ("Syntax".equalsIgnoreCase(itemType)) {
            handleSyntaxQuestion(q);
        } else if ("Pronunciation".equalsIgnoreCase(itemType)) {
            handlePronunciationQuestion(q);
        } else if ("Spelling".equalsIgnoreCase(itemType) || "Grammar".equalsIgnoreCase(itemType)) {
            handleMultipleChoiceQuestion(q);
        } else {
            // Default to multiple choice
            handleMultipleChoiceQuestion(q);
        }

        enableOptions();
    }

    private void handleSyntaxQuestion(Question q) {
        tvQuestion.setText("Arrange these words to form a correct sentence:");

        // Show scrambled words if available
        if (q.getScrambledWords() != null && !q.getScrambledWords().isEmpty()) {
            String scrambledText = String.join(" | ", q.getScrambledWords());
            tvPassageText.setText(scrambledText);
            cardPassage.setVisibility(View.VISIBLE);
        }

        // Show answer options
        setOptionsVisibility(q);
    }

    private void handlePronunciationQuestion(Question q) {
        // Show pronunciation card
        cardPronunciation.setVisibility(View.VISIBLE);
        cardQuestion.setVisibility(View.GONE);

        tvPronunciationWord.setText(q.getItemText() != null ? q.getItemText() : "");
        tvPronunciationGuide.setText(q.getPassageText() != null ? "/" + q.getPassageText() + "/" : "");

        // Auto-enable continue for pronunciation (after recording)
        // For now, we'll treat mic tap as completion
    }

    private void handleMultipleChoiceQuestion(Question q) {
        tvQuestion.setText(q.getQuestionText());
        setOptionsVisibility(q);
    }

    private void setOptionsVisibility(Question q) {
        btnOptionA.setText("a) " + (q.getOptionA() != null ? q.getOptionA() : ""));
        btnOptionB.setText("b) " + (q.getOptionB() != null ? q.getOptionB() : ""));
        btnOptionC.setText("c) " + (q.getOptionC() != null ? q.getOptionC() : ""));

        if (q.getOptionD() != null && !q.getOptionD().isEmpty()) {
            btnOptionD.setVisibility(View.VISIBLE);
            btnOptionD.setText("d) " + q.getOptionD());
        } else {
            btnOptionD.setVisibility(View.GONE);
        }

        btnOptionA.setVisibility(View.VISIBLE);
        btnOptionB.setVisibility(View.VISIBLE);
        btnOptionC.setVisibility(View.VISIBLE);
    }

    private void selectAnswer(String choice, Button selectedButton) {
        Question q = questionList.get(currentIndex);

        // Clear previous selections
        clearSelections();

        // Mark current selection
        selectedButton.setSelected(true);
        selectedAnswer = choice;

        // Create response
        ResponseModel response = new ResponseModel();
        response.setItemId(q.getItemId());
        response.setSelectedOption(choice);
        response.setCorrect(q.getCorrectOption() != null && q.getCorrectOption().equalsIgnoreCase(choice));

        // Update or add response
        if (currentIndex < responses.size()) {
            responses.set(currentIndex, response);
        } else {
            responses.add(response);
        }

        // Enable continue button
        btnContinue.setEnabled(true);
    }

    private void clearSelections() {
        btnOptionA.setSelected(false);
        btnOptionB.setSelected(false);
        btnOptionC.setSelected(false);
        btnOptionD.setSelected(false);
    }

    private void recordPronunciation() {
        // TODO: Implement pronunciation recording
        // For now, just mark as recorded and enable continue
        tvMicStatus.setText("Recorded ✓");
        btnContinue.setEnabled(true);

        // Create a placeholder response for pronunciation
        Question q = questionList.get(currentIndex);
        ResponseModel response = new ResponseModel();
        response.setItemId(q.getItemId());
        response.setSelectedOption("RECORDED"); // Placeholder
        response.setCorrect(true); // For now, assume correct

        if (currentIndex < responses.size()) {
            responses.set(currentIndex, response);
        } else {
            responses.add(response);
        }

        Toast.makeText(this, "Pronunciation recorded", Toast.LENGTH_SHORT).show();
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
        if (selectedAnswer == null && cardPronunciation.getVisibility() != View.VISIBLE) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

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

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
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
