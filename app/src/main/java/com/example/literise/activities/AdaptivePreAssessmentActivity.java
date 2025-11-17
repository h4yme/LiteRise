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

import com.example.literise.models.SubmitRequest;

import com.example.literise.models.SubmitSingleRequest;

import com.example.literise.models.SubmitResponseResult;

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



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_layout_pre_assessment);



        session = new SessionManager(this);

        initializeViews();



        // Get initial theta from student's current ability

        currentTheta = session.getAbility(); // Assumes SessionManager has getAbility()



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

                        sessionId = request.getSessionId(); // Update if session was created

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



        // Update header with progress

        tvTitle.setText("Adaptive Assessment");

        tvProgress.setText("Question " + response.getItemsCompleted() +

                " • ~" + response.getItemsRemaining() + " remaining");

        tvItemTypeBadge.setText(itemType);



        // Reset state

        selectedAnswer = null;

        btnContinue.setEnabled(false);

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

            handlePronunciationQuestion(currentQuestion);

        } else if ("Spelling".equalsIgnoreCase(itemType) || "Grammar".equalsIgnoreCase(itemType)) {

            handleMultipleChoiceQuestion(currentQuestion);

        } else {

            handleMultipleChoiceQuestion(currentQuestion);

        }

    }



    private void handleSyntaxQuestion(Question q) {

        tvQuestion.setText(q.getQuestionText() != null ? q.getQuestionText() :

                "Arrange the words to form a correct sentence:");



        // Show scrambled words if available

        if (q.getScrambledWords() != null && !q.getScrambledWords().isEmpty()) {

            containerScrambledWords.setVisibility(View.VISIBLE);

            // Display scrambled words (implementation depends on your layout)

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



        // Reset mic status

        tvMicStatus.setText("Tap to record");

        cardMicButton.setCardBackgroundColor(getResources().getColor(R.color.color_jade1, null));

    }



    private void showDefinitionDialog(Question q) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(q.getItemText());



        StringBuilder message = new StringBuilder();



        if (q.getPhonetic() != null && !q.getPhonetic().isEmpty()) {

            message.append("Pronunciation: ").append(q.getPhonetic()).append("\n\n");

        }



        if (q.getDefinition() != null && !q.getDefinition().isEmpty()) {

            message.append("Definition:\n").append(q.getDefinition());

        }



        builder.setMessage(message.toString());

        builder.setPositiveButton("Close", (dialog, which) -> dialog.dismiss());



        AlertDialog dialog = builder.create();

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

        resetButtonStates();

        selectedButton.setBackgroundColor(getResources().getColor(R.color.color_jade1, null));

        btnContinue.setEnabled(true);

    }



    private void resetButtonStates() {

        int defaultColor = getResources().getColor(R.color.color_grey, null);

        btnOptionA.setBackgroundColor(defaultColor);

        btnOptionB.setBackgroundColor(defaultColor);

        btnOptionC.setBackgroundColor(defaultColor);

        btnOptionD.setBackgroundColor(defaultColor);

    }



    private void submitCurrentAnswer() {

        if (selectedAnswer == null || currentQuestion == null) {

            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();

            return;

        }



        // Disable continue button during submission

        btnContinue.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);



        // Calculate time spent

        long timeSpent = (SystemClock.elapsedRealtime() - questionStartTime) / 1000; // seconds



        // Determine if answer is correct

        int isCorrect = selectedAnswer.equals(currentQuestion.getCorrectOption()) ? 1 : 0;



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



                    // Update theta

                    currentTheta = result.getNewTheta();



                    // Add to answered items

                    itemsAnswered.add(currentQuestion.getItemId());



                    // Show feedback

                    Toast.makeText(AdaptivePreAssessmentActivity.this,

                            result.getFeedback(), Toast.LENGTH_SHORT).show();



                    // Load next question

                    loadNextAdaptiveQuestion();

                } else {

                    Toast.makeText(AdaptivePreAssessmentActivity.this,

                            "Failed to submit answer", Toast.LENGTH_SHORT).show();

                    btnContinue.setEnabled(true);

                }

            }



            @Override

            public void onFailure(Call<SingleResponseResult> call, Throwable t) {

                progressBar.setVisibility(View.GONE);

                Toast.makeText(AdaptivePreAssessmentActivity.this,

                        "Connection error: " + t.getMessage(), Toast.LENGTH_LONG).show();

                btnContinue.setEnabled(true);

            }

        });

    }



    private void finishAssessment(NextItemResponse result) {

        if (result.getFinalTheta() != null) {

            session.saveAbility(result.getFinalTheta().floatValue());

        }



        // Show final results

        showFinalResultsDialog(result);

    }



    private void showFinalResultsDialog(NextItemResponse result) {

        StringBuilder message = new StringBuilder();

        message.append(String.format("Questions Completed: %d\n\n",

                result.getItemsCompleted()));



        if (result.getFinalTheta() != null) {

            message.append(String.format("Ability Level: %.2f\n", result.getFinalTheta()));



            // Classify ability

            String classification = classifyAbility(result.getFinalTheta());

            message.append(String.format("Classification: %s\n\n", classification));

            message.append(getFeedbackForClassification(classification));

        }



        if (result.getSem() != null) {

            message.append(String.format("\n\nPrecision: %.2f (lower is better)", result.getSem()));

        }



        new AlertDialog.Builder(this)

                .setTitle("Assessment Complete!")

                .setMessage(message.toString())

                .setPositiveButton("Continue", (dialog, which) -> {

                    dialog.dismiss();

                    finish();

                })

                .setCancelable(false)

                .show();

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



    // Pronunciation recording methods (same as original PreAssessmentActivity)

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

        tvMicStatus.setText("Listening...");

        cardMicButton.setCardBackgroundColor(getResources().getColor(R.color.color_warning, null));



        if (speechRecognizer == null) {

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

            speechRecognizer.setRecognitionListener(new PronunciationRecognitionListener());

        }



        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,

                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);



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



            String message = "Recognition error";

            switch (error) {

                case SpeechRecognizer.ERROR_AUDIO:

                    message = "Audio error";

                    break;

                case SpeechRecognizer.ERROR_CLIENT:

                    message = "Client error";

                    break;

                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:

                    message = "Insufficient permissions";

                    break;

                case SpeechRecognizer.ERROR_NETWORK:

                    message = "Network error";

                    break;

                case SpeechRecognizer.ERROR_NO_MATCH:

                    message = "No match found. Please try again.";

                    break;

                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:

                    message = "Recognizer busy";

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



            if (matches != null && !matches.isEmpty()) {

                String recognizedText = matches.get(0);

                float confidence = (confidenceScores != null && confidenceScores.length > 0)

                        ? confidenceScores[0] : 0.0f;



                validatePronunciation(recognizedText, confidence);

            } else {

                tvMicStatus.setText("No speech detected");

                Toast.makeText(AdaptivePreAssessmentActivity.this, "No speech detected", Toast.LENGTH_SHORT).show();

            }

        }



        @Override

        public void onPartialResults(Bundle partialResults) {}



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



                    // Enable continue button and set selectedAnswer to the recognized text

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