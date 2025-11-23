package com.example.literise.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.GetNextItemRequest;
import com.example.literise.models.NextItemResponse;
import com.example.literise.models.PronunciationRequest;
import com.example.literise.models.PronunciationResponse;
import com.example.literise.models.Question;
import com.example.literise.models.SingleResponseResult;
import com.example.literise.models.SubmitSingleRequest;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Adaptive PreAssessment Activity using Computer Adaptive Testing (CAT)
 * Questions adapt in real-time based on student performance
 */
public class AdaptivePreAssessmentActivity extends AppCompatActivity {

    private TextView tvTitle, tvProgress, tvPassage, tvPassageText, tvQuestion;
    private TextView tvItemTypeBadge, tvPronunciationWord, tvPronunciationGuide, tvMicStatus;
    private Button btnOptionA, btnOptionB, btnOptionC, btnOptionD;
    private MaterialButton btnContinue;
    private CardView cardPassage, cardPronunciation, cardQuestion, cardMicButton;
    private ImageView ivMic;
    private ProgressBar progressBar;
    private View gridOptions, containerScrambledWords;

    // Adaptive testing state
    private Question currentQuestion = null;
    private List<Integer> itemsAnswered = new ArrayList<>();
    private double currentTheta = 0.0;
    private int sessionId = 0;
    private long questionStartTime = 0;
    private SessionManager session;
    private String selectedAnswer = null;

    // Speech recognition
    private SpeechRecognizer speechRecognizer;
    private boolean isRecording = false;
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1;
    private int pronunciationScore = 0;
    private String lastPartialResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_pre_assessment);

        session = new SessionManager(this);
        initializeViews();

        // Get initial theta from student's current ability
        currentTheta = session.getAbility();

        // Load first adaptive question
        loadNextAdaptiveQuestion();
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
        btnContinue.setOnClickListener(v -> submitCurrentAnswer());

        cardMicButton.setOnClickListener(v -> recordPronunciation());
    }

    private void loadNextAdaptiveQuestion() {
        progressBar.setVisibility(View.VISIBLE);

        GetNextItemRequest request = new GetNextItemRequest(
                sessionId,
                currentTheta,
                itemsAnswered
        );

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getNextItem(request).enqueue(new Callback<NextItemResponse>() {
            @Override
            public void onResponse(Call<NextItemResponse> call, Response<NextItemResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    NextItemResponse result = response.body();

                    if (result.isAssessmentComplete()) {
                        // Assessment is complete
                        finishAssessment(result);
                    } else {
                        // Display next question
                        currentQuestion = result.getItem();
                        if (result.getSessionId() > 0) {
                            sessionId = result.getSessionId();
                        }
                        showQuestion(result);
                        questionStartTime = SystemClock.elapsedRealtime();
                    }
                } else {
                    Toast.makeText(AdaptivePreAssessmentActivity.this,
                            "Failed to load next question", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<NextItemResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdaptivePreAssessmentActivity.this,
                        "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void showQuestion(NextItemResponse response) {
        if (currentQuestion == null) return;

        String itemType = currentQuestion.getItemType() != null ? currentQuestion.getItemType() : "";

        tvTitle.setText("Placement Test");
        int currentQuestionNumber = itemsAnswered.size() + 1;
        int totalQuestions = 20;

        tvProgress.setText(String.format("Question %d of %d", currentQuestionNumber, totalQuestions));
        tvItemTypeBadge.setText(itemType);

        // Update progress bar
        progressBar.setProgress((int) (((float) currentQuestionNumber / totalQuestions) * 100));

        // Reset state
        selectedAnswer = null;
        btnContinue.setEnabled(false);
        clearSelections();
        resetButtonStates();

        // Hide all sections by default
        cardPassage.setVisibility(View.GONE);
        cardPronunciation.setVisibility(View.GONE);
        cardQuestion.setVisibility(View.VISIBLE);
        gridOptions.setVisibility(View.VISIBLE);
        containerScrambledWords.setVisibility(View.GONE);

        // Handle different question types
        if ("Syntax".equalsIgnoreCase(itemType)) {
            handleSyntaxQuestion(currentQuestion);
        } else if ("Pronunciation".equalsIgnoreCase(itemType)) {
            // Check if pronunciation item is MCQ (has options) or speak-type
            if (currentQuestion.hasOptions() || currentQuestion.isMCQ()) {
                // MCQ pronunciation question
                handleMultipleChoiceQuestion(currentQuestion);
            } else {
                // Speak-type pronunciation (student speaks the word)
                handlePronunciationQuestion(currentQuestion);
            }
        } else if ("Spelling".equalsIgnoreCase(itemType) || "Grammar".equalsIgnoreCase(itemType)) {
            handleMultipleChoiceQuestion(currentQuestion);
        } else {
            handleMultipleChoiceQuestion(currentQuestion);
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
        // Show pronunciation card, hide MCQ options
        cardPronunciation.setVisibility(View.VISIBLE);
        cardQuestion.setVisibility(View.GONE);
        gridOptions.setVisibility(View.GONE);
        tvMicStatus.setText("Tap to record");

        tvPronunciationWord.setText(q.getItemText() != null ? q.getItemText() : "");
        tvPronunciationGuide.setText(q.getPassageText() != null ? "/" + q.getPassageText() + "/" : "");

        // Make the word clickable to show definition
        if (q.getDefinition() != null && !q.getDefinition().isEmpty()) {
            tvPronunciationWord.setClickable(true);
            tvPronunciationWord.setOnClickListener(v -> showDefinitionDialog(q));
            tvPronunciationWord.setPaintFlags(tvPronunciationWord.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        } else {
            tvPronunciationWord.setClickable(false);
            tvPronunciationWord.setOnClickListener(null);
            tvPronunciationWord.setPaintFlags(tvPronunciationWord.getPaintFlags() & ~android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        }

        // Reset mic button color
        cardMicButton.setCardBackgroundColor(getResources().getColor(R.color.color_jade1, null));
    }

    private void showDefinitionDialog(Question q) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_word_definition, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvWord = dialogView.findViewById(R.id.tvDialogWord);
        TextView tvPhonetic = dialogView.findViewById(R.id.tvDialogPhonetic);
        TextView tvDefinition = dialogView.findViewById(R.id.tvDialogDefinition);
        View layoutPhonetic = dialogView.findViewById(R.id.layoutPhonetic);
        View layoutDefinition = dialogView.findViewById(R.id.layoutDefinition);
        MaterialButton btnClose = dialogView.findViewById(R.id.btnDialogClose);

        tvWord.setText(q.getItemText());

        if (q.getPhonetic() != null && !q.getPhonetic().isEmpty()) {
            tvPhonetic.setText(q.getPhonetic());
            layoutPhonetic.setVisibility(View.VISIBLE);
        } else {
            layoutPhonetic.setVisibility(View.GONE);
        }

        if (q.getDefinition() != null && !q.getDefinition().isEmpty()) {
            tvDefinition.setText(q.getDefinition());
            layoutDefinition.setVisibility(View.VISIBLE);
        } else {
            layoutDefinition.setVisibility(View.GONE);
        }

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void handleMultipleChoiceQuestion(Question q) {
        tvQuestion.setText(q.getQuestionText());
        setOptionsVisibility(q);
    }

    private void setOptionsVisibility(Question q) {
        btnOptionA.setText(q.getOptionA() != null ? q.getOptionA() : "");
        btnOptionB.setText(q.getOptionB() != null ? q.getOptionB() : "");
        btnOptionC.setText(q.getOptionC() != null ? q.getOptionC() : "");
        btnOptionD.setText(q.getOptionD() != null ? q.getOptionD() : "");

        btnOptionA.setVisibility(!q.getOptionA().isEmpty() ? View.VISIBLE : View.GONE);
        btnOptionB.setVisibility(!q.getOptionB().isEmpty() ? View.VISIBLE : View.GONE);
        btnOptionC.setVisibility(!q.getOptionC().isEmpty() ? View.VISIBLE : View.GONE);
        btnOptionD.setVisibility(!q.getOptionD().isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void selectAnswer(String option, Button selectedButton) {
        selectedAnswer = option;
        clearSelections();
        selectedButton.setSelected(true);
        btnContinue.setEnabled(true);
    }

    private void resetButtonStates() {
        btnOptionA.setSelected(false);
        btnOptionB.setSelected(false);
        btnOptionC.setSelected(false);
        btnOptionD.setSelected(false);
    }

    private void clearSelections() {
        btnOptionA.setSelected(false);
        btnOptionB.setSelected(false);
        btnOptionC.setSelected(false);
        btnOptionD.setSelected(false);
    }

    private void enableOptions() {
        btnOptionA.setEnabled(true);
        btnOptionB.setEnabled(true);
        btnOptionC.setEnabled(true);
        btnOptionD.setEnabled(true);
    }

    private void disableOptions() {
        btnOptionA.setEnabled(false);
        btnOptionB.setEnabled(false);
        btnOptionC.setEnabled(false);
        btnOptionD.setEnabled(false);
    }

    private void submitCurrentAnswer() {
        if (selectedAnswer == null || currentQuestion == null) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }

        btnContinue.setEnabled(false);
        disableOptions();
        progressBar.setVisibility(View.VISIBLE);

        long timeSpent = (SystemClock.elapsedRealtime() - questionStartTime) / 1000;

        final int isCorrect;
        boolean isSpeakPronunciation = "Pronunciation".equalsIgnoreCase(currentQuestion.getItemType())
                && !currentQuestion.hasOptions() && !currentQuestion.isMCQ();

        if (isSpeakPronunciation) {
            isCorrect = (pronunciationScore >= 70) ? 1 : 0;
        } else {
            isCorrect = selectedAnswer.equals(currentQuestion.getCorrectOption()) ? 1 : 0;
        }

        SubmitSingleRequest request = new SubmitSingleRequest(
                sessionId,
                currentQuestion.getItemId(),
                selectedAnswer,
                isCorrect,
                (int) timeSpent
        );

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.submitSingleResponse(request).enqueue(new Callback<SingleResponseResult>() {
            @Override
            public void onResponse(Call<SingleResponseResult> call, Response<SingleResponseResult> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    SingleResponseResult result = response.body();

                    currentTheta = result.getNewTheta();
                    itemsAnswered.add(currentQuestion.getItemId());

                    if (isCorrect == 1) {
                        CustomToast.showSuccess(AdaptivePreAssessmentActivity.this, "Correct!");
                    } else {
                        CustomToast.showError(AdaptivePreAssessmentActivity.this, "Incorrect");
                    }

                    loadNextAdaptiveQuestion();
                } else {
                    CustomToast.showError(AdaptivePreAssessmentActivity.this, "Failed to submit answer");
                    btnContinue.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<SingleResponseResult> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                CustomToast.showError(AdaptivePreAssessmentActivity.this, "Connection error");
                btnContinue.setEnabled(true);
            }
        });
    }

    private void finishAssessment(NextItemResponse result) {
        if (result.getFinalTheta() != null) {
            session.saveAbility(result.getFinalTheta().floatValue());
        }
        showFinalResultsDialog(result);
    }

    private void showFinalResultsDialog(NextItemResponse result) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_assessment_complete, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        TextView tvScore = dialogView.findViewById(R.id.tvDialogScore);
        TextView tvAccuracy = dialogView.findViewById(R.id.tvDialogAccuracy);
        TextView tvAbility = dialogView.findViewById(R.id.tvDialogAbility);
        TextView tvClassification = dialogView.findViewById(R.id.tvDialogClassification);
        TextView tvFeedback = dialogView.findViewById(R.id.tvDialogFeedback);
        TextView tvPrecision = dialogView.findViewById(R.id.tvDialogPrecision);
        CardView cardScore = dialogView.findViewById(R.id.cardScore);
        View layoutAbility = dialogView.findViewById(R.id.layoutAbility);
        MaterialButton btnContinueDialog = dialogView.findViewById(R.id.btnDialogContinue);

        if (result.getCorrectAnswers() != null && result.getTotalItems() != null) {
            tvScore.setText(String.format("%d/%d", result.getCorrectAnswers(), result.getTotalItems()));

            if (result.getAccuracy() != null) {
                tvAccuracy.setText(String.format("%.1f%%", result.getAccuracy()));
            } else {
                tvAccuracy.setVisibility(View.GONE);
            }
            cardScore.setVisibility(View.VISIBLE);
        } else {
            tvScore.setText(String.valueOf(result.getItemsCompleted()));
            tvAccuracy.setText("items completed");
            cardScore.setVisibility(View.VISIBLE);
        }

        if (result.getFinalTheta() != null) {
            tvAbility.setText(String.format("%.2f", result.getFinalTheta()));

            String classification = classifyAbility(result.getFinalTheta());
            tvClassification.setText(classification);

            String feedback = getFeedbackForClassification(classification);
            tvFeedback.setText(feedback);

            layoutAbility.setVisibility(View.VISIBLE);
        } else {
            layoutAbility.setVisibility(View.GONE);
        }

        if (result.getSem() != null) {
            tvPrecision.setText(String.format("Precision: %.2f (lower is better)", result.getSem()));
            tvPrecision.setVisibility(View.VISIBLE);
        } else {
            tvPrecision.setVisibility(View.GONE);
        }

        btnContinueDialog.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(AdaptivePreAssessmentActivity.this, DashboardActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });

        dialog.show();
    }

    private String classifyAbility(double theta) {
        if (theta < -1.0) return "Below Basic";
        else if (theta < 0.5) return "Basic";
        else if (theta < 1.5) return "Proficient";
        else return "Advanced";
    }

    private String getFeedbackForClassification(String classification) {
        switch (classification) {
            case "Advanced":
                return "Excellent work! You've demonstrated advanced literacy skills.";
            case "Proficient":
                return "Great job! You have proficient literacy skills.";
            case "Basic":
                return "Good effort! Keep practicing to improve your skills.";
            case "Below Basic":
                return "You're making progress! Let's work on building your foundation.";
            default:
                return "Assessment complete! Keep learning and practicing.";
        }
    }

    // Speech Recognition Methods
    private void recordPronunciation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
            return;
        }

        if (isRecording) {
            stopRecording();
            return;
        }

        startRecording();
    }

    private void startRecording() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
            return;
        }

        isRecording = true;
        lastPartialResult = null;
        tvMicStatus.setText("Listening...");
        cardMicButton.setCardBackgroundColor(getResources().getColor(R.color.color_warning, null));

        // Always destroy and recreate to avoid stale state issues
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new PronunciationRecognitionListener());

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        // Use explicit English - more reliable than device locale
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);
        // Longer silence timeout for single words
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
        // Very short minimum for single words
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 100);
        // Online recognition - requires internet but more accurate
        intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        android.util.Log.d("SpeechRecognition", "Starting speech recognition...");
        speechRecognizer.startListening(intent);
    }

    private void stopRecording() {
        isRecording = false;
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
        tvMicStatus.setText("Tap to record");
        cardMicButton.setCardBackgroundColor(getResources().getColor(R.color.color_jade1, null));
    }

    private class PronunciationRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            tvMicStatus.setText("Speak now...");
        }

        @Override
        public void onBeginningOfSpeech() {
            tvMicStatus.setText("Listening...");
        }

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {
            tvMicStatus.setText("Processing...");
            isRecording = false;
        }

        @Override
        public void onError(int error) {
            isRecording = false;
            cardMicButton.setCardBackgroundColor(getResources().getColor(R.color.color_jade1, null));

            // Check if we have a partial result to use as fallback
            if (error == SpeechRecognizer.ERROR_NO_MATCH && lastPartialResult != null && !lastPartialResult.trim().isEmpty()) {
                android.util.Log.d("SpeechRecognition", "ERROR_NO_MATCH but using partial result: " + lastPartialResult);
                tvMicStatus.setText("Processing: " + lastPartialResult);
                validatePronunciation(lastPartialResult.trim(), 0.8f);
                return;
            }

            String message = "Recognition error";

            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "Audio error - check microphone";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "Client error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "Microphone permission required";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "No match found. Please try again.";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "Recognizer busy - please wait";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "Server error";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "Speech timeout. Please try again.";
                    break;
            }

            tvMicStatus.setText(message);
            Toast.makeText(AdaptivePreAssessmentActivity.this, message, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onResults(Bundle results) {
            isRecording = false;
            cardMicButton.setCardBackgroundColor(getResources().getColor(R.color.color_jade1, null));

            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            float[] confidenceScores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

            android.util.Log.d("SpeechRecognition", "onResults called");
            android.util.Log.d("SpeechRecognition", "Matches: " + (matches != null ? matches.size() : "null"));
            if (matches != null && !matches.isEmpty()) {
                android.util.Log.d("SpeechRecognition", "First match: " + matches.get(0));
            }

            if (matches != null && !matches.isEmpty() && matches.get(0) != null && !matches.get(0).trim().isEmpty()) {
                String recognizedText = matches.get(0).trim();
                float confidence = (confidenceScores != null && confidenceScores.length > 0)
                        ? confidenceScores[0] : 0.0f;

                android.util.Log.d("SpeechRecognition", "Confidence: " + confidence);
                validatePronunciation(recognizedText, confidence);
            } else if (lastPartialResult != null && !lastPartialResult.trim().isEmpty()) {
                android.util.Log.d("SpeechRecognition", "Empty results but using partial: " + lastPartialResult);
                tvMicStatus.setText("Processing: " + lastPartialResult);
                validatePronunciation(lastPartialResult.trim(), 0.8f);
            } else {
                tvMicStatus.setText("No clear speech - try again");
                Toast.makeText(AdaptivePreAssessmentActivity.this,
                        "Couldn't hear you clearly. Tap the microphone and speak louder.",
                        Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty() && matches.get(0) != null && !matches.get(0).trim().isEmpty()) {
                lastPartialResult = matches.get(0);
                tvMicStatus.setText("Heard: " + matches.get(0));
                android.util.Log.d("SpeechRecognition", "Partial result: " + matches.get(0));
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {}
    }

    private void validatePronunciation(String recognizedText, float confidence) {
        if (currentQuestion == null) return;

        String expectedWord = currentQuestion.getItemText();
        tvMicStatus.setText("Validating...");

        PronunciationRequest request = new PronunciationRequest(
                currentQuestion.getItemId(),
                expectedWord,
                recognizedText,
                confidence
        );

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.checkPronunciation(request).enqueue(new Callback<PronunciationResponse>() {
            @Override
            public void onResponse(Call<PronunciationResponse> call, Response<PronunciationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PronunciationResponse result = response.body();
                    pronunciationScore = result.getScore();

                    String statusText = String.format(Locale.getDefault(),
                            "Score: %d%% - %s",
                            result.getScore(),
                            result.getFeedback());
                    tvMicStatus.setText(statusText);

                    selectedAnswer = recognizedText;
                    btnContinue.setEnabled(true);

                    Toast.makeText(AdaptivePreAssessmentActivity.this,
                            result.getFeedback(),
                            Toast.LENGTH_LONG).show();
                } else {
                    tvMicStatus.setText("Validation failed");
                    Toast.makeText(AdaptivePreAssessmentActivity.this,
                            "Failed to validate pronunciation",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PronunciationResponse> call, Throwable t) {
                tvMicStatus.setText("Connection error");
                Toast.makeText(AdaptivePreAssessmentActivity.this,
                        "Connection error: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recordPronunciation();
            } else {
                Toast.makeText(this, "Microphone permission required for pronunciation", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }
}
