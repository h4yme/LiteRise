package com.example.literise.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import com.example.literise.R;
import com.example.literise.database.QuestionBankHelper;
import com.example.literise.database.SessionManager;
import com.example.literise.helpers.AdaptiveQuestionHelper;
import com.example.literise.helpers.PronunciationHelper;
import com.example.literise.models.AdaptiveQuestionResponse;
import com.example.literise.models.PlacementQuestion;
import com.example.literise.models.SubmitAnswerResponse;
import com.example.literise.utils.IRTEngine;
import com.example.literise.utils.KaraokeTextHelper;
import com.example.literise.utils.SessionLogger;
import com.example.literise.utils.SoundEffectsHelper;
import com.example.literise.utils.SpeechRecognitionHelper;
import com.example.literise.utils.TextToSpeechHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

public class PlacementTestActivity extends AppCompatActivity {

    private static final String TAG = "PlacementTest";

    // UI Components
    private ImageView btnBack;
    private ProgressBar progressBar;
    private TextView btnSkip;
    private TextView tvQuestionType;
    private FrameLayout questionContainer;
    private MaterialCardView leoHintContainer;
    private TextView tvLeoHint;
    private MaterialButton btnContinue, btnRetry;

    // IRT Engine and Question Bank
    private IRTEngine irtEngine;
    private QuestionBankHelper questionBankHelper;
    private AdaptiveQuestionHelper adaptiveHelper;
    private PlacementQuestion currentQuestion;
    private List<PlacementQuestion> categoryQuestions;
    private int currentSessionId;
    private long questionStartTime;

    // Speech Recognition
    private SpeechRecognitionHelper speechRecognitionHelper;

    // Pronunciation Assessment
    private PronunciationHelper pronunciationHelper;

    // Karaoke Reading
    private KaraokeTextHelper karaokeTextHelper;
    private TextToSpeechHelper textToSpeechHelper;

    // Sound Effects
    private SoundEffectsHelper soundEffectsHelper;

    // Session Manager
    private SessionManager sessionManager;

    // Question tracking
    private int currentQuestionNumber = 1;
    private int totalQuestions = 25;
    private int currentCategory = 1;
    private int previousCategory = 0;
    private String selectedAnswer = "";
    private String selectedAnswerLetter = ""; // A, B, C, or D
    private int questionsPerCategory = 5; // 5 questions per category (5 categories × 5 = 25)
    private long startTime;
    private boolean answerAlreadySubmitted = false; // For pronunciation questions
    private static final int PERMISSION_REQUEST_RECORD_AUDIO = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_test);

        startTime = System.currentTimeMillis();

        // Initialize Session Manager
        sessionManager = new SessionManager(this);

        // Initialize IRT Engine and Question Bank
        irtEngine = new IRTEngine();
        questionBankHelper = new QuestionBankHelper(this);

        // Initialize Adaptive Question Helper for API-based question selection
        currentSessionId = (int) (System.currentTimeMillis() / 1000);
        String assessmentType = sessionManager.hasCompletedAssessment() ? "PostAssessment" : "PreAssessment";
        adaptiveHelper = new AdaptiveQuestionHelper(this, currentSessionId, assessmentType);

        // Initialize Sound Effects
        soundEffectsHelper = new SoundEffectsHelper(this);
        soundEffectsHelper.startBackgroundMusic();

        initViews();
        setupListeners();
        loadNextQuestion();

        // Log assessment start
        logAssessmentStart();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        btnSkip = findViewById(R.id.btnSkip);
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
            // For pronunciation questions, answer is already submitted
            if (answerAlreadySubmitted) {
                Log.d(TAG, "=== CONTINUE CLICKED - PRONUNCIATION ANSWER ===");
                Log.d(TAG, "Answer already submitted via pronunciation API");
                Log.d(TAG, "QuestionID: " + (currentQuestion != null ? currentQuestion.getQuestionId() : "null"));
                Log.d(TAG, "QuestionNumber: " + currentQuestionNumber);
                Log.d(TAG, "SelectedAnswer: " + selectedAnswer);
                Log.d(TAG, "SelectedAnswerLetter: " + selectedAnswerLetter);
                Log.d(TAG, "Current Theta: " + irtEngine.getTheta());
                Log.d(TAG, "Skipping checkAnswer() - moving to next question");
                Log.d(TAG, "===========================================");

                // Increment question number
                currentQuestionNumber++;
                // Load next question
                loadNextQuestion();
                return;
            }

            // For regular questions, check if answer selected
            if (selectedAnswer != null && !selectedAnswer.isEmpty()) {
                Log.d(TAG, "=== CONTINUE CLICKED - REGULAR ANSWER ===");
                Log.d(TAG, "Calling checkAnswer() to submit via submit_answer API");
                Log.d(TAG, "========================================");
                // Check answer and proceed
                checkAnswer();
            }
        });

        btnRetry.setOnClickListener(v -> {
            // Reload current question
            selectedAnswer = "";
            selectedAnswerLetter = "";
            displayCurrentQuestion();
        });
    }

    private void loadNextQuestion() {
        // Update category based on question number
        updateCurrentCategory();

        // Check if category changed - show transition screen
        if (previousCategory != 0 && currentCategory != previousCategory) {
            showCategoryTransition();
            return;
        }

        // Get category name for API request
        String categoryName = getCategoryName(currentCategory);

        // Fetch next adaptive question from API
        adaptiveHelper.getNextQuestion(categoryName, new AdaptiveQuestionHelper.QuestionCallback() {
            @Override
            public void onSuccess(AdaptiveQuestionResponse response) {
                runOnUiThread(() -> {
                    if (response.getQuestion() != null) {
                        // Convert API response to PlacementQuestion format
                        currentQuestion = convertToPlacementQuestion(response.getQuestion());

                        // Track when question is displayed (for response time)
                        questionStartTime = System.currentTimeMillis();

                        // Display the question
                        displayCurrentQuestion();
                    } else {
                        // No more questions available
                        Toast.makeText(PlacementTestActivity.this,
                                "No more questions available", Toast.LENGTH_SHORT).show();
                        showResults();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Log error
                    Toast.makeText(PlacementTestActivity.this,
                            "Error loading question: " + error, Toast.LENGTH_SHORT).show();

                    // Fallback: try to use local question bank
                    categoryQuestions = questionBankHelper.getQuestionsByCategory(currentCategory);
                    currentQuestion = irtEngine.selectNextQuestion(categoryQuestions);

                    if (currentQuestion != null) {
                        questionStartTime = System.currentTimeMillis();
                        displayCurrentQuestion();
                    } else {
                        // No fallback available either
                        if (currentCategory < 5) {
                            currentCategory++;
                            loadNextQuestion();
                        } else {
                            showResults();
                        }
                    }
                });
            }
        });
    }

    private void showCategoryTransition() {
        // Play transition sound
        soundEffectsHelper.playTransition();

        // Create dialog
        Dialog dialog = new Dialog(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_transition, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(false);

        // Make dialog background transparent to show rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Get views
        TextView tvCategoryIcon = dialogView.findViewById(R.id.tvCategoryIcon);
        TextView tvCategoryTitle = dialogView.findViewById(R.id.tvCategoryTitle);
        TextView tvCategoryName = dialogView.findViewById(R.id.tvCategoryName);
        TextView tvLeoMessage = dialogView.findViewById(R.id.tvLeoMessage);
        MaterialButton btnContinue = dialogView.findViewById(R.id.btnContinue);

        // Set category info
        String icon = "";
        String title = "";
        String name = "";
        String message = "";

        switch (currentCategory) {
            case 1:
                icon = "🔤";
                title = "Ready?";
                name = "Category 1: Phonics and Word Study";
                message = "Let's start with letter sounds and words! You've got this! 🌟";
                break;
            case 2:
                icon = "📚";
                title = "Great Job!";
                name = "Category 2: Vocabulary and Word Knowledge";
                message = "Now let's explore word meanings! You're doing amazing! 🌟";
                break;
            case 3:
                icon = "✏️";
                title = "Awesome Work!";
                name = "Category 3: Grammar Awareness";
                message = "Time to learn about grammar and sentence structure! 💪";
                break;
            case 4:
                icon = "📖";
                title = "Keep Going!";
                name = "Category 4: Comprehending Text";
                message = "Let's read and understand stories together! 📚";
                break;
            case 5:
                icon = "✍️";
                title = "Final Round!";
                name = "Category 5: Creating and Composing";
                message = "Last category! Show me your writing skills! You're almost done! 🎉";
                break;
        }

        tvCategoryIcon.setText(icon);
        tvCategoryTitle.setText(title);
        tvCategoryName.setText(name);
        tvLeoMessage.setText(message);

        // Speak Leo's message
        final String finalMessage = message;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (textToSpeechHelper != null && textToSpeechHelper.isInitialized()) {
                textToSpeechHelper.speak(finalMessage, null);
            }
        }, 500);

        // Continue button
        btnContinue.setOnClickListener(v -> {
            dialog.dismiss();
            // Continue with next question after transition
            loadNextQuestion();
        });

        // Show dialog with fade animation
        dialog.show();
    }

    private void displayCurrentQuestion() {
        // Fade out current question
        questionContainer.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> {
                    // Update progress
                    updateProgress();

                    // Load question based on type
                    loadQuestionView();

                    // Show Leo hint
                    showLeoHint();

                    // Reset buttons
                    btnContinue.setEnabled(false);
                    btnRetry.setVisibility(View.GONE);
                    selectedAnswer = "";
                    selectedAnswerLetter = "";
                    answerAlreadySubmitted = false; // Reset for new question

                    // Fade in new question
                    questionContainer.animate()
                            .alpha(1f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

    private void updateProgress() {
        int progress = (int) ((currentQuestionNumber / (float) totalQuestions) * 100);
        progressBar.setProgress(progress);
    }

    private void updateCurrentCategory() {
        previousCategory = currentCategory;

        if (currentQuestionNumber <= questionsPerCategory) {
            currentCategory = 1;
        } else if (currentQuestionNumber <= questionsPerCategory * 2) {
            currentCategory = 2;
        } else if (currentQuestionNumber <= questionsPerCategory * 3) {
            currentCategory = 3;
        } else if (currentQuestionNumber <= questionsPerCategory * 4) {
            currentCategory = 4;
        } else {
            currentCategory = 5;
        }
    }

    /**
     * Convert category number to category name for API
     */
    private String getCategoryName(int categoryNumber) {
        switch (categoryNumber) {
            case 1:
                return "Phonics and Word Study";
            case 2:
                return "Vocabulary and Word Knowledge";
            case 3:
                return "Grammar Awareness and Grammatical Structures";
            case 4:
                return "Comprehending and Analyzing Text";
            case 5:
                return "Creating and Composing Text";
            default:
                return "Phonics and Word Study";
        }
    }

    /**
     * Convert category name from API to category number
     */
    private int getCategoryNumber(String categoryName) {
        if (categoryName == null) return 1;

        switch (categoryName) {
            case "Phonics and Word Study":
                return 1;
            case "Vocabulary and Word Knowledge":
                return 2;
            case "Grammar Awareness and Grammatical Structures":
                return 3;
            case "Comprehending and Analyzing Text":
                return 4;
            case "Creating and Composing Text":
                return 5;
            default:
                return 1;
        }
    }

    /**
     * Convert API question response to PlacementQuestion format
     */
    private PlacementQuestion convertToPlacementQuestion(AdaptiveQuestionResponse.QuestionData apiQuestion) {
        PlacementQuestion question = new PlacementQuestion();

        // Set basic fields
        question.setQuestionId(apiQuestion.getItemId());
        question.setCategory(getCategoryNumber(apiQuestion.getCategory())); // Convert string to int
        question.setSubcategory(apiQuestion.getSubcategory() != null ? apiQuestion.getSubcategory() : "");
        question.setQuestionText(apiQuestion.getQuestionText());
        question.setQuestionType(apiQuestion.getQuestionType());
        question.setReadingPassage(apiQuestion.getReadingPassage()); // Set reading passage for comprehension questions
        question.setDifficulty(apiQuestion.getDifficulty());

        // Set answer options
        List<String> options = new ArrayList<>();
        if (apiQuestion.getOptionA() != null) {
            options.add(apiQuestion.getOptionA());
        }
        if (apiQuestion.getOptionB() != null) {
            options.add(apiQuestion.getOptionB());
        }
        if (apiQuestion.getOptionC() != null) {
            options.add(apiQuestion.getOptionC());
        }
        if (apiQuestion.getOptionD() != null) {
            options.add(apiQuestion.getOptionD());
        }
        question.setOptions(options);

        // Note: CorrectAnswer is NOT sent by API (hidden server-side)
        // We'll get correctness feedback when we submit the answer

        // Generate a simple Leo hint based on difficulty
        String hint;
        if (apiQuestion.getDifficulty() < -0.5) {
            hint = "This one is nice and easy! Take your time! 🌟";
        } else if (apiQuestion.getDifficulty() < 0.5) {
            hint = "You can do this! Think carefully! 💡";
        } else if (apiQuestion.getDifficulty() < 1.5) {
            hint = "This is a bit challenging - you've got this! 💪";
        } else {
            hint = "Wow, a tough one! Do your best! 🎯";
        }
        question.setLeoHint(hint);

        return question;
    }

    private void loadQuestionView() {
        if (currentQuestion == null) return;

        // Switch based on question type
        String questionType = currentQuestion.getQuestionType();

        if ("pronunciation".equalsIgnoreCase(questionType) || "pronunciation_reading".equalsIgnoreCase(questionType)) {
            loadPronunciationQuestion();
        } else if ("reading".equalsIgnoreCase(questionType) || "reading_comprehension".equalsIgnoreCase(questionType)) {
            loadReadingQuestion();
        } else {
            // Default to multiple choice
            loadMultipleChoiceQuestion();
        }
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

    private void loadPronunciationQuestion() {
        // Check for microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    PERMISSION_REQUEST_RECORD_AUDIO);
            return;
        }

        // Inflate pronunciation question layout
        questionContainer.removeAllViews();
        View questionView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_question_pronunciation, questionContainer, false);
        questionContainer.addView(questionView);

        // Set question type
        tvQuestionType.setText("🎤 Pronunciation Practice");

        // Initialize pronunciation helper
        if (pronunciationHelper == null) {
            pronunciationHelper = new PronunciationHelper(this);
        }

        // Get UI elements
        TextView tvWordToPronounce = questionView.findViewById(R.id.tvWordToPronounce);
        TextView tvPassageToRead = questionView.findViewById(R.id.tvPassageToRead);
        TextView tvRecordingStatus = questionView.findViewById(R.id.tvRecordingStatus);
        FloatingActionButton btnMicrophone = questionView.findViewById(R.id.btnMicrophone);
        LinearLayout waveformContainer = questionView.findViewById(R.id.waveformContainer);
        MaterialButton btnPlayback = questionView.findViewById(R.id.btnPlayback);
        MaterialCardView feedbackCard = questionView.findViewById(R.id.feedbackCard);
        TextView tvFeedbackIcon = questionView.findViewById(R.id.tvFeedbackIcon);
        TextView tvFeedbackText = questionView.findViewById(R.id.tvFeedbackText);
        TextView tvScore = questionView.findViewById(R.id.tvScore);

        // Check if this is a reading passage question or single word
        String readingPassage = currentQuestion.getReadingPassage();
        String wordToPronounce;

        if (readingPassage != null && !readingPassage.isEmpty()) {
            // This is a pronunciation_reading question - show passage
            tvPassageToRead.setText(readingPassage);
            tvPassageToRead.setVisibility(View.VISIBLE);
            tvWordToPronounce.setVisibility(View.GONE);
            wordToPronounce = readingPassage;
        } else {
            // Single word pronunciation - extract word from question text
            String questionText = currentQuestion.getQuestionText();
            wordToPronounce = questionText.contains(":")
                    ? questionText.substring(questionText.indexOf(":") + 1).trim()
                    : questionText;
            tvWordToPronounce.setText(wordToPronounce);
            tvWordToPronounce.setVisibility(View.VISIBLE);
            tvPassageToRead.setVisibility(View.GONE);
        }

        // Track if currently recording
        final boolean[] isRecording = {false};

        // Define the recording callback that will handle evaluation
        PronunciationHelper.PronunciationCallback recordingCallback = new PronunciationHelper.PronunciationCallback() {
            @Override
            public void onRecordingStarted() {
                runOnUiThread(() -> {
                    tvRecordingStatus.setText("Recording... Speak now!");
                });
            }

            @Override
            public void onRecordingStopped(java.io.File audioFile, int durationMs) {
                android.util.Log.d("PlacementTest", "onRecordingStopped called - File: " + audioFile.getAbsolutePath() + ", Duration: " + durationMs);

                runOnUiThread(() -> {
                    // Stop waveform animation
                    waveformContainer.clearAnimation();
                    waveformContainer.setVisibility(View.GONE);
                    tvRecordingStatus.setText("Evaluating...");
                    tvRecordingStatus.setTextColor(getColor(R.color.text_secondary));
                    btnMicrophone.setBackgroundTintList(getColorStateList(R.color.text_secondary));
                });

                // Evaluate pronunciation via API
                int itemId = currentQuestion.getQuestionId();
                int responseId = (int) (System.currentTimeMillis() / 1000); // Temporary response ID

                Log.d(TAG, "╔════════════════════════════════════════════════════════════");
                Log.d(TAG, "║ CALLING PRONUNCIATION EVALUATION API");
                Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                Log.d(TAG, "║ ItemID: " + itemId);
                Log.d(TAG, "║ SessionID: " + currentSessionId);
                Log.d(TAG, "║ ResponseID (temp): " + responseId);
                Log.d(TAG, "║ QuestionNumber: " + currentQuestionNumber);
                Log.d(TAG, "║ Expected Word: " + wordToPronounce);
                Log.d(TAG, "║ Audio File: " + audioFile.getAbsolutePath());
                Log.d(TAG, "║ Audio Duration: " + durationMs + "ms");
                Log.d(TAG, "╚════════════════════════════════════════════════════════════");

                pronunciationHelper.evaluatePronunciation(
                        itemId,
                        responseId,
                        currentSessionId, // Pass the actual placement test session ID
                        wordToPronounce.toLowerCase(),
                        audioFile,
                        new PronunciationHelper.EvaluationCallback() {
                            @Override
                            public void onEvaluationSuccess(PronunciationHelper.PronunciationResult result) {
                                runOnUiThread(() -> {
                                    isRecording[0] = false;

                                    // Calculate response time
                                    int responseTime = 0;
                                    if (questionStartTime > 0) {
                                        responseTime = (int) ((System.currentTimeMillis() - questionStartTime) / 1000);
                                    }

                                    Log.d(TAG, "╔════════════════════════════════════════════════════════════");
                                    Log.d(TAG, "║ PRONUNCIATION EVALUATION SUCCESS");
                                    Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                                    Log.d(TAG, "║ QuestionID: " + currentQuestion.getQuestionId());
                                    Log.d(TAG, "║ SessionID: " + currentSessionId);
                                    Log.d(TAG, "║ QuestionNumber: " + currentQuestionNumber);
                                    Log.d(TAG, "║ Expected Word: " + wordToPronounce);
                                    Log.d(TAG, "║ Response Time: " + responseTime + "s");
                                    Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                                    Log.d(TAG, "║ API RESPONSE DATA:");
                                    Log.d(TAG, "║ - Overall Accuracy: " + result.getOverallAccuracy() + "%");
                                    Log.d(TAG, "║ - Recognized Text: " + result.getRecognizedText());
                                    Log.d(TAG, "║ - Passed Threshold: " + result.isPassed());
                                    Log.d(TAG, "║ - Feedback: " + result.getFeedback());
                                    Log.d(TAG, "║ - Response ID: " + result.getResponseId());
                                    Log.d(TAG, "╠════════════════════════════════════════════════════════════");

                                    // Show feedback
                                    feedbackCard.setVisibility(View.VISIBLE);
                                    int accuracy = result.getOverallAccuracy();
                                    tvScore.setText(accuracy + "% Accuracy");

                                    // Use isPassed() from API result - it uses the item's MinimumAccuracy threshold
                                    boolean isCorrect = result.isPassed();

                                    Log.d(TAG, "║ EVALUATION RESULT:");
                                    Log.d(TAG, "║ - isCorrect (passed): " + isCorrect);
                                    Log.d(TAG, "║ - Accuracy: " + accuracy + "%");

                                    if (accuracy >= 85) {
                                        // Excellent pronunciation (85%+)
                                        tvFeedbackIcon.setText("🎉");
                                        tvFeedbackText.setText(result.getFeedback());
                                        feedbackCard.setCardBackgroundColor(getColor(R.color.success_light));
                                        tvFeedbackText.setTextColor(getColor(R.color.success_green));
                                        tvScore.setTextColor(getColor(R.color.success_green));
                                        soundEffectsHelper.playSuccess();
                                        Log.d(TAG, "║ - Feedback Level: EXCELLENT (85%+)");
                                    } else if (isCorrect) {
                                        // Good pronunciation (passed threshold but < 85%)
                                        tvFeedbackIcon.setText("👍");
                                        tvFeedbackText.setText(result.getFeedback());
                                        feedbackCard.setCardBackgroundColor(getColor(R.color.warning_light));
                                        tvFeedbackText.setTextColor(getColor(R.color.warning_orange));
                                        tvScore.setTextColor(getColor(R.color.warning_orange));
                                        soundEffectsHelper.playSuccess();
                                        Log.d(TAG, "║ - Feedback Level: GOOD (passed but < 85%)");
                                    } else {
                                        // Needs improvement (below threshold)
                                        tvFeedbackIcon.setText("🔄");
                                        tvFeedbackText.setText(result.getFeedback());
                                        feedbackCard.setCardBackgroundColor(getColor(R.color.error_light));
                                        tvFeedbackText.setTextColor(getColor(R.color.error_red));
                                        tvScore.setTextColor(getColor(R.color.error_red));
                                        soundEffectsHelper.playError();
                                        Log.d(TAG, "║ - Feedback Level: NEEDS IMPROVEMENT");
                                    }

                                    // Reset button
                                    tvRecordingStatus.setText("Tap to try again");
                                    tvRecordingStatus.setTextColor(getColor(R.color.text_secondary));
                                    btnMicrophone.setBackgroundTintList(getColorStateList(R.color.success_green));

                                    // Note: evaluate_pronunciation.php already created the StudentResponse
                                    // record and stored pronunciation metrics. We don't need to call
                                    // submit_answer again. Just update local IRT and enable continue.

                                    selectedAnswer = result.getRecognizedText();
                                    selectedAnswerLetter = accuracy + "% - " + result.getRecognizedText();

                                    Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                                    Log.d(TAG, "║ STORING ANSWER DATA:");
                                    Log.d(TAG, "║ - selectedAnswer: " + selectedAnswer);
                                    Log.d(TAG, "║ - selectedAnswerLetter: " + selectedAnswerLetter);

                                    // Update IRT engine locally with result
                                    double thetaBefore = irtEngine.getTheta();
                                    irtEngine.updateTheta(currentQuestion, isCorrect);
                                    double thetaAfter = irtEngine.getTheta();

                                    Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                                    Log.d(TAG, "║ IRT ENGINE UPDATE:");
                                    Log.d(TAG, "║ - Theta BEFORE: " + thetaBefore);
                                    Log.d(TAG, "║ - Theta AFTER: " + thetaAfter);
                                    Log.d(TAG, "║ - Delta: " + (thetaAfter - thetaBefore));

                                    // Mark that answer has been submitted
                                    answerAlreadySubmitted = true;

                                    Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                                    Log.d(TAG, "║ SUBMISSION STATUS:");
                                    Log.d(TAG, "║ - answerAlreadySubmitted: " + answerAlreadySubmitted);
                                    Log.d(TAG, "║ - StudentResponse record created by evaluate_pronunciation.php");
                                    Log.d(TAG, "║ - No additional submit_answer API call needed");
                                    Log.d(TAG, "║ - Continue button enabled");
                                    Log.d(TAG, "╚════════════════════════════════════════════════════════════");

                                    // Enable continue button
                                    btnContinue.setEnabled(true);
                                    android.view.animation.Animation bounceAnim = android.view.animation.AnimationUtils.loadAnimation(
                                            PlacementTestActivity.this, R.anim.bounce);
                                    btnContinue.startAnimation(bounceAnim);
                                });
                            }

                            @Override
                            public void onEvaluationError(String error) {
                                runOnUiThread(() -> {
                                    Log.e(TAG, "╔════════════════════════════════════════════════════════════");
                                    Log.e(TAG, "║ PRONUNCIATION EVALUATION ERROR");
                                    Log.e(TAG, "╠════════════════════════════════════════════════════════════");
                                    Log.e(TAG, "║ QuestionID: " + currentQuestion.getQuestionId());
                                    Log.e(TAG, "║ SessionID: " + currentSessionId);
                                    Log.e(TAG, "║ QuestionNumber: " + currentQuestionNumber);
                                    Log.e(TAG, "║ Error: " + error);
                                    Log.e(TAG, "║ IMPORTANT: StudentResponse NOT created - pronunciation failed!");
                                    Log.e(TAG, "║ User can retry recording by tapping microphone again");
                                    Log.e(TAG, "╚════════════════════════════════════════════════════════════");

                                    isRecording[0] = false;
                                    waveformContainer.clearAnimation();
                                    waveformContainer.setVisibility(View.GONE);
                                    tvRecordingStatus.setText("Error: " + error);
                                    tvRecordingStatus.setTextColor(getColor(R.color.error_red));
                                    btnMicrophone.setBackgroundTintList(getColorStateList(R.color.success_green));
                                    Toast.makeText(PlacementTestActivity.this,
                                            "Pronunciation evaluation failed: " + error,
                                            Toast.LENGTH_LONG).show();
                                });
                            }
                        }
                );
            }

            @Override
            public void onRecordingError(String error) {
                runOnUiThread(() -> {
                    isRecording[0] = false;
                    waveformContainer.clearAnimation();
                    waveformContainer.setVisibility(View.GONE);
                    tvRecordingStatus.setText(error);
                    tvRecordingStatus.setTextColor(getColor(R.color.error_red));
                    btnMicrophone.setBackgroundTintList(getColorStateList(R.color.success_green));
                });
            }
        };

        // Microphone button click listener
        btnMicrophone.setOnClickListener(v -> {
            if (!isRecording[0]) {
                // Start recording
                isRecording[0] = true;
                tvRecordingStatus.setText("Recording... Say the word now!");
                tvRecordingStatus.setTextColor(getColor(R.color.error_red));
                btnMicrophone.setBackgroundTintList(getColorStateList(R.color.error_red));
                waveformContainer.setVisibility(View.VISIBLE);
                feedbackCard.setVisibility(View.GONE);

                // Start animated waveform
                animateWaveform(waveformContainer);

                // Start audio recording
                pronunciationHelper.startRecording(recordingCallback);

                // Auto-stop recording after 5 seconds
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (isRecording[0]) {
                        pronunciationHelper.stopRecording(recordingCallback);
                    }
                }, 5000);

            } else {
                // Stop recording manually
                isRecording[0] = false;
                pronunciationHelper.stopRecording(recordingCallback);
            }
        });

        // Hide Leo hint for pronunciation questions
        leoHintContainer.setVisibility(View.GONE);
    }

    private void animateWaveform(LinearLayout waveformContainer) {
        // Animate each wave bar
        View wave1 = waveformContainer.findViewById(R.id.wave1);
        View wave2 = waveformContainer.findViewById(R.id.wave2);
        View wave3 = waveformContainer.findViewById(R.id.wave3);
        View wave4 = waveformContainer.findViewById(R.id.wave4);
        View wave5 = waveformContainer.findViewById(R.id.wave5);

        animateWaveBar(wave1, 200, 0);
        animateWaveBar(wave2, 200, 100);
        animateWaveBar(wave3, 200, 200);
        animateWaveBar(wave4, 200, 300);
        animateWaveBar(wave5, 200, 400);
    }

    private void animateWaveBar(View bar, long duration, long startOffset) {
        android.view.animation.Animation scaleAnim = new android.view.animation.ScaleAnimation(
                1f, 1f, 0.5f, 1.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f,
                android.view.animation.Animation.RELATIVE_TO_SELF, 0.5f);
        scaleAnim.setDuration(duration);
        scaleAnim.setStartOffset(startOffset);
        scaleAnim.setRepeatCount(android.view.animation.Animation.INFINITE);
        scaleAnim.setRepeatMode(android.view.animation.Animation.REVERSE);
        bar.startAnimation(scaleAnim);
    }

    private void loadReadingQuestion() {
        // Inflate reading question layout
        questionContainer.removeAllViews();
        View questionView = LayoutInflater.from(this)
                .inflate(R.layout.fragment_question_reading, questionContainer, false);
        questionContainer.addView(questionView);

        // Set question type
        tvQuestionType.setText("📖 Reading Comprehension");

        // Initialize Text-to-Speech
        if (textToSpeechHelper == null) {
            textToSpeechHelper = new TextToSpeechHelper(this);
        }

        // Get UI elements
        MaterialCardView readingPassageCard = questionView.findViewById(R.id.readingPassageCard);
        MaterialCardView controlPanelCard = questionView.findViewById(R.id.controlPanelCard);
        TextView tvReadingText = questionView.findViewById(R.id.tvReadingText);
        TextView tvReadingStatus = questionView.findViewById(R.id.tvReadingStatus);
        FloatingActionButton btnPlay = questionView.findViewById(R.id.btnPlay);
        FloatingActionButton btnStop = questionView.findViewById(R.id.btnStop);
        SeekBar seekBarSpeed = questionView.findViewById(R.id.seekBarSpeed);
        MaterialCardView comprehensionCard = questionView.findViewById(R.id.comprehensionCard);
        TextView tvComprehensionQuestion = questionView.findViewById(R.id.tvComprehensionQuestion);
        MaterialButton btnAnswer1 = questionView.findViewById(R.id.btnAnswer1);
        MaterialButton btnAnswer2 = questionView.findViewById(R.id.btnAnswer2);
        MaterialButton btnAnswer3 = questionView.findViewById(R.id.btnAnswer3);
        MaterialButton btnAnswer4 = questionView.findViewById(R.id.btnAnswer4);

        // Set reading text
        String readingText = currentQuestion.getReadingPassage();
        if (readingText == null || readingText.isEmpty()) {
            readingText = currentQuestion.getQuestionText();
        }
        tvReadingText.setText(readingText);

        // Initialize karaoke helper
        karaokeTextHelper = new KaraokeTextHelper(tvReadingText);
        karaokeTextHelper.setReadingSpeed(1); // Normal speed by default

        // Play button listener
        final String finalReadingText = readingText;
        btnPlay.setOnClickListener(v -> {
            if (karaokeTextHelper.isPlaying()) {
                // Pause
                karaokeTextHelper.pause();
                textToSpeechHelper.stop();
                btnPlay.setImageResource(R.drawable.ic_play);
                tvReadingStatus.setText("Paused - Tap play to continue");
            } else {
                // Start or resume
                btnPlay.setImageResource(R.drawable.ic_stop);
                tvReadingStatus.setText("Reading... Follow along!");
                tvReadingStatus.setTextColor(getColor(R.color.primary_blue));

                karaokeTextHelper.start(new KaraokeTextHelper.KaraokeCallback() {
                    @Override
                    public void onWordHighlighted(int wordIndex, String word) {
                        // Word is highlighted, TTS reads it
                        if (wordIndex == 0) {
                            // Start TTS for entire passage
                            textToSpeechHelper.speak(finalReadingText, null);
                        }
                    }

                    @Override
                    public void onReadingComplete() {
                        runOnUiThread(() -> {
                            btnPlay.setImageResource(R.drawable.ic_play);
                            tvReadingStatus.setText("Great job reading!");
                            tvReadingStatus.setTextColor(getColor(R.color.success_green));

                            // Show comprehension question
                            if (currentQuestion.getOptions() != null && !currentQuestion.getOptions().isEmpty()) {
                                // Hide reading passage and controls to make room for all 4 answer buttons
                                readingPassageCard.setVisibility(View.GONE);
                                controlPanelCard.setVisibility(View.GONE);

                                comprehensionCard.setVisibility(View.VISIBLE);
                                tvComprehensionQuestion.setText(currentQuestion.getQuestionText());

                                List<String> options = currentQuestion.getOptions();
                                android.util.Log.d("PlacementTest", "Reading question has " + options.size() + " options");
                                if (options.size() >= 4) {
                                    btnAnswer1.setText(options.get(0));
                                    btnAnswer1.setVisibility(View.VISIBLE);
                                    btnAnswer2.setText(options.get(1));
                                    btnAnswer2.setVisibility(View.VISIBLE);
                                    btnAnswer3.setText(options.get(2));
                                    btnAnswer3.setVisibility(View.VISIBLE);
                                    btnAnswer4.setText(options.get(3));
                                    btnAnswer4.setVisibility(View.VISIBLE);
                                } else if (options.size() >= 3) {
                                    btnAnswer1.setText(options.get(0));
                                    btnAnswer1.setVisibility(View.VISIBLE);
                                    btnAnswer2.setText(options.get(1));
                                    btnAnswer2.setVisibility(View.VISIBLE);
                                    btnAnswer3.setText(options.get(2));
                                    btnAnswer3.setVisibility(View.VISIBLE);
                                    btnAnswer4.setVisibility(View.GONE);
                                } else if (options.size() >= 2) {
                                    btnAnswer1.setText(options.get(0));
                                    btnAnswer1.setVisibility(View.VISIBLE);
                                    btnAnswer2.setText(options.get(1));
                                    btnAnswer2.setVisibility(View.VISIBLE);
                                    btnAnswer3.setVisibility(View.GONE);
                                    btnAnswer4.setVisibility(View.GONE);
                                }

                                // Scroll to show comprehension card
                                questionView.postDelayed(() -> {
                                    comprehensionCard.requestFocus();
                                }, 300);
                            } else {
                                // No comprehension question, enable continue
                                selectedAnswer = "completed";
                                btnContinue.setEnabled(true);
                                android.view.animation.Animation bounceAnim = android.view.animation.AnimationUtils.loadAnimation(
                                        PlacementTestActivity.this, R.anim.bounce);
                                btnContinue.startAnimation(bounceAnim);
                            }
                        });
                    }
                });
            }
        });

        // Stop button listener
        btnStop.setOnClickListener(v -> {
            karaokeTextHelper.stop();
            textToSpeechHelper.stop();
            btnPlay.setImageResource(R.drawable.ic_play);
            tvReadingStatus.setText("Tap play to start reading!");
            tvReadingStatus.setTextColor(getColor(R.color.text_secondary));

            // Show reading passage and controls again
            readingPassageCard.setVisibility(View.VISIBLE);
            controlPanelCard.setVisibility(View.VISIBLE);
            comprehensionCard.setVisibility(View.GONE);
        });

        // Speed slider listener
        seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && karaokeTextHelper != null) {
                    karaokeTextHelper.setReadingSpeed(progress);

                    // Also adjust TTS speed
                    float ttsSpeed = 0.7f + (progress * 0.3f); // 0.7 to 1.3
                    // Speed will be applied on next read
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Comprehension answer buttons
        View.OnClickListener answerClickListener = v -> {
            // Reset all button colors
            btnAnswer1.setBackgroundTintList(getColorStateList(android.R.color.white));
            btnAnswer1.setTextColor(getColor(R.color.text_primary));
            btnAnswer2.setBackgroundTintList(getColorStateList(android.R.color.white));
            btnAnswer2.setTextColor(getColor(R.color.text_primary));
            btnAnswer3.setBackgroundTintList(getColorStateList(android.R.color.white));
            btnAnswer3.setTextColor(getColor(R.color.text_primary));
            btnAnswer4.setBackgroundTintList(getColorStateList(android.R.color.white));
            btnAnswer4.setTextColor(getColor(R.color.text_primary));

            // Highlight selected
            MaterialButton selectedBtn = (MaterialButton) v;
            selectedBtn.setBackgroundTintList(getColorStateList(R.color.primary_blue));
            selectedBtn.setTextColor(getColor(android.R.color.white));

            // Play pop animation
            android.view.animation.Animation popAnim = android.view.animation.AnimationUtils.loadAnimation(
                    this, R.anim.option_pop);
            v.startAnimation(popAnim);

            // Store answer text and letter
            selectedAnswer = selectedBtn.getText().toString();

            // Determine which button was clicked and set the letter
            if (v.getId() == R.id.btnAnswer1) {
                selectedAnswerLetter = "A";
            } else if (v.getId() == R.id.btnAnswer2) {
                selectedAnswerLetter = "B";
            } else if (v.getId() == R.id.btnAnswer3) {
                selectedAnswerLetter = "C";
            } else if (v.getId() == R.id.btnAnswer4) {
                selectedAnswerLetter = "D";
            }

            // Enable continue button
            btnContinue.setEnabled(true);
            android.view.animation.Animation bounceAnim = android.view.animation.AnimationUtils.loadAnimation(
                    this, R.anim.bounce);
            btnContinue.startAnimation(bounceAnim);
        };

        btnAnswer1.setOnClickListener(answerClickListener);
        btnAnswer2.setOnClickListener(answerClickListener);
        btnAnswer3.setOnClickListener(answerClickListener);
        btnAnswer4.setOnClickListener(answerClickListener);

        // Hide Leo hint for reading questions
        leoHintContainer.setVisibility(View.GONE);
    }

    private String getQuestionTypeText(String subcategory) {
        switch (subcategory) {
            case "Vocabulary":
                return "Correct Word";
            case "Phonological":
                return "Sound Recognition";
            case "Phonics":
                return "Letter Sounds";
            case "Word Study":
                return "Word Families";
            case "Narrative":
                return "Story Comprehension";
            case "Informational":
                return "Reading for Information";
            case "Grammar":
                return "Grammar Skills";
            case "Sentence Construction":
                return "Build Sentences";
            default:
                return "Answer the Question";
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
        MaterialCardView optionA = questionView.findViewById(R.id.optionA);
        MaterialCardView optionB = questionView.findViewById(R.id.optionB);
        MaterialCardView optionC = questionView.findViewById(R.id.optionC);
        MaterialCardView optionD = questionView.findViewById(R.id.optionD);

        ImageView checkA = questionView.findViewById(R.id.ivCheckA);
        ImageView checkB = questionView.findViewById(R.id.ivCheckB);
        ImageView checkC = questionView.findViewById(R.id.ivCheckC);
        ImageView checkD = questionView.findViewById(R.id.ivCheckD);

        View.OnClickListener optionClickListener = v -> {
            // Safety check: ensure options list has enough items
            List<String> options = currentQuestion.getOptions();
            if (options == null || options.size() < 4) {
                Toast.makeText(this, "Error: Question options not loaded properly", Toast.LENGTH_SHORT).show();
                return;
            }

            // Play pop sound
            soundEffectsHelper.playPop();

            // Hide all checkmarks and reset strokes
            checkA.setVisibility(View.GONE);
            checkB.setVisibility(View.GONE);
            checkC.setVisibility(View.GONE);
            checkD.setVisibility(View.GONE);

            optionA.setStrokeColor(getColor(R.color.option_stroke));
            optionB.setStrokeColor(getColor(R.color.option_stroke));
            optionC.setStrokeColor(getColor(R.color.option_stroke));
            optionD.setStrokeColor(getColor(R.color.option_stroke));

            // Show checkmark for selected option and store both text and letter
            if (v.getId() == R.id.optionA) {
                selectedAnswer = options.get(0);
                selectedAnswerLetter = "A";
                checkA.setVisibility(View.VISIBLE);
                optionA.setStrokeColor(getColor(R.color.success_green));
            } else if (v.getId() == R.id.optionB) {
                selectedAnswer = options.get(1);
                selectedAnswerLetter = "B";
                checkB.setVisibility(View.VISIBLE);
                optionB.setStrokeColor(getColor(R.color.success_green));
            } else if (v.getId() == R.id.optionC) {
                selectedAnswer = options.get(2);
                selectedAnswerLetter = "C";
                checkC.setVisibility(View.VISIBLE);
                optionC.setStrokeColor(getColor(R.color.success_green));
            } else if (v.getId() == R.id.optionD) {
                selectedAnswer = options.get(3);
                selectedAnswerLetter = "D";
                checkD.setVisibility(View.VISIBLE);
                optionD.setStrokeColor(getColor(R.color.success_green));
            }

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

        // Calculate response time in seconds
        int responseTime = 0;
        if (questionStartTime > 0) {
            responseTime = (int) ((System.currentTimeMillis() - questionStartTime) / 1000);
        }

        // Send the letter (A/B/C/D) to API, not the text
        // If skipped (empty), send empty string
        final String finalSelectedAnswerLetter = selectedAnswerLetter.isEmpty() ? "" : selectedAnswerLetter;

        Log.d(TAG, "╔════════════════════════════════════════════════════════════");
        Log.d(TAG, "║ SUBMITTING REGULAR ANSWER TO API");
        Log.d(TAG, "╠════════════════════════════════════════════════════════════");
        Log.d(TAG, "║ QuestionID: " + currentQuestion.getQuestionId());
        Log.d(TAG, "║ SessionID: " + currentSessionId);
        Log.d(TAG, "║ QuestionNumber: " + currentQuestionNumber + " of " + totalQuestions);
        Log.d(TAG, "║ QuestionType: " + currentQuestion.getQuestionType());
        Log.d(TAG, "║ Category: " + currentQuestion.getCategory());
        Log.d(TAG, "║ Subcategory: " + currentQuestion.getSubcategory());
        Log.d(TAG, "║ Response Time: " + responseTime + "s");
        Log.d(TAG, "╠════════════════════════════════════════════════════════════");
        Log.d(TAG, "║ ANSWER DATA:");
        Log.d(TAG, "║ - Selected Letter: " + finalSelectedAnswerLetter);
        Log.d(TAG, "║ - Selected Text: " + selectedAnswer);
        Log.d(TAG, "║ - Correct Answer: " + currentQuestion.getCorrectAnswer());
        Log.d(TAG, "║ - Skipped: " + finalSelectedAnswerLetter.isEmpty());
        Log.d(TAG, "╠════════════════════════════════════════════════════════════");
        Log.d(TAG, "║ CURRENT IRT STATE:");
        Log.d(TAG, "║ - Current Theta: " + irtEngine.getTheta());
        Log.d(TAG, "║ - Question Difficulty: " + currentQuestion.getDifficulty());
        Log.d(TAG, "║ - Question Discrimination: " + currentQuestion.getDiscrimination());
        Log.d(TAG, "╚════════════════════════════════════════════════════════════");

        // Submit answer to API (send letter, not text)
        final int finalResponseTime = responseTime;
        adaptiveHelper.submitAnswer(
                currentQuestion.getQuestionId(),
                finalSelectedAnswerLetter, // Send letter (A/B/C/D)
                false, // We don't know if correct yet - API will tell us
                finalResponseTime,
                new AdaptiveQuestionHelper.AnswerCallback() {
                    @Override
                    public void onSuccess(SubmitAnswerResponse response) {
                        runOnUiThread(() -> {
                            // Get correctness from server response
                            boolean isCorrect = response.isCorrect();

                            Log.d(TAG, "╔════════════════════════════════════════════════════════════");
                            Log.d(TAG, "║ SUBMIT ANSWER API - SUCCESS");
                            Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                            Log.d(TAG, "║ API RESPONSE:");
                            Log.d(TAG, "║ - isCorrect: " + isCorrect);
                            Log.d(TAG, "║ - Message: " + response.getMessage());

                            // Update IRT engine with result (for local tracking)
                            double thetaBefore = irtEngine.getTheta();
                            irtEngine.updateTheta(currentQuestion, isCorrect);
                            double thetaAfterLocal = irtEngine.getTheta();

                            // Sync theta from API to local IRTEngine for accurate placement calculation
                            if (response.getFeedback() != null) {
                                double apiTheta = response.getFeedback().getNewThetaEstimate();
                                Log.d(TAG, "║ - API Theta: " + apiTheta);
                                irtEngine.setTheta(apiTheta);
                            }

                            double thetaFinal = irtEngine.getTheta();

                            Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                            Log.d(TAG, "║ IRT ENGINE UPDATE:");
                            Log.d(TAG, "║ - Theta BEFORE: " + thetaBefore);
                            Log.d(TAG, "║ - Theta AFTER (local): " + thetaAfterLocal);
                            Log.d(TAG, "║ - Theta FINAL (from API): " + thetaFinal);
                            Log.d(TAG, "║ - Delta: " + (thetaFinal - thetaBefore));
                            Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                            Log.d(TAG, "║ CATEGORY PROGRESS:");
                            int[] categoryScores = irtEngine.getCategoryScores();
                            Log.d(TAG, "║ - Category 1 (Phonics): " + categoryScores[0] + "%");
                            Log.d(TAG, "║ - Category 2 (Vocabulary): " + categoryScores[1] + "%");
                            Log.d(TAG, "║ - Category 3 (Grammar): " + categoryScores[2] + "%");
                            Log.d(TAG, "║ - Category 4 (Comprehending): " + categoryScores[3] + "%");
                            Log.d(TAG, "║ - Category 5 (Creating): " + categoryScores[4] + "%");
                            Log.d(TAG, "╠════════════════════════════════════════════════════════════");
                            Log.d(TAG, "║ PROGRESS:");
                            Log.d(TAG, "║ - Completed: " + currentQuestionNumber + " of " + totalQuestions);
                            Log.d(TAG, "║ - Next Question: " + (currentQuestionNumber + 1));

                            // Play appropriate sound effect
                            if (isCorrect) {
                                soundEffectsHelper.playSuccess();
                                Log.d(TAG, "║ - Sound: SUCCESS");
                            } else if (!finalSelectedAnswerLetter.isEmpty()) {
                                soundEffectsHelper.playError();
                                Log.d(TAG, "║ - Sound: ERROR");
                            } else {
                                Log.d(TAG, "║ - Sound: NONE (skipped)");
                            }

                            // Move to next question
                            currentQuestionNumber++;

                            if (currentQuestionNumber > totalQuestions) {
                                // Test complete - show results with celebration
                                Log.d(TAG, "║ - Status: TEST COMPLETE! 🎉");
                                Log.d(TAG, "╚════════════════════════════════════════════════════════════");
                                soundEffectsHelper.playCelebration();
                                showResults();
                            } else {
                                // Play chime for question completion
                                Log.d(TAG, "║ - Status: Moving to next question");
                                Log.d(TAG, "╚════════════════════════════════════════════════════════════");
                                soundEffectsHelper.playChime();
                                loadNextQuestion();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Log.e(TAG, "╔════════════════════════════════════════════════════════════");
                            Log.e(TAG, "║ SUBMIT ANSWER API - ERROR");
                            Log.e(TAG, "╠════════════════════════════════════════════════════════════");
                            Log.e(TAG, "║ QuestionID: " + currentQuestion.getQuestionId());
                            Log.e(TAG, "║ SessionID: " + currentSessionId);
                            Log.e(TAG, "║ QuestionNumber: " + currentQuestionNumber + " of " + totalQuestions);
                            Log.e(TAG, "║ Error: " + error);
                            Log.e(TAG, "╠════════════════════════════════════════════════════════════");
                            Log.e(TAG, "║ FALLBACK: Using local IRT engine to check answer");

                            // Log error but continue
                            Toast.makeText(PlacementTestActivity.this,
                                    "Error submitting answer: " + error, Toast.LENGTH_SHORT).show();

                            // Fallback: use local IRT engine to check answer
                            boolean isCorrect = false;
                            if (!finalSelectedAnswerLetter.isEmpty() && currentQuestion.getCorrectAnswer() != null) {
                                isCorrect = finalSelectedAnswerLetter.equalsIgnoreCase(currentQuestion.getCorrectAnswer());
                            }

                            Log.e(TAG, "║ - Local Check Result: " + (isCorrect ? "CORRECT" : "INCORRECT"));
                            Log.e(TAG, "║ - Selected: " + finalSelectedAnswerLetter);
                            Log.e(TAG, "║ - Correct Answer: " + currentQuestion.getCorrectAnswer());

                            // Update IRT engine with result
                            double thetaBefore = irtEngine.getTheta();
                            irtEngine.updateTheta(currentQuestion, isCorrect);
                            double thetaAfter = irtEngine.getTheta();

                            Log.e(TAG, "╠════════════════════════════════════════════════════════════");
                            Log.e(TAG, "║ IRT ENGINE UPDATE (LOCAL ONLY):");
                            Log.e(TAG, "║ - Theta BEFORE: " + thetaBefore);
                            Log.e(TAG, "║ - Theta AFTER: " + thetaAfter);
                            Log.e(TAG, "║ - Delta: " + (thetaAfter - thetaBefore));
                            Log.e(TAG, "╠════════════════════════════════════════════════════════════");
                            Log.e(TAG, "║ WARNING: Answer NOT recorded in database!");
                            Log.e(TAG, "║ StudentResponse record NOT created due to API error.");

                            // Play appropriate sound effect
                            if (isCorrect) {
                                soundEffectsHelper.playSuccess();
                                Log.e(TAG, "║ - Sound: SUCCESS");
                            } else if (!finalSelectedAnswerLetter.isEmpty()) {
                                soundEffectsHelper.playError();
                                Log.e(TAG, "║ - Sound: ERROR");
                            } else {
                                Log.e(TAG, "║ - Sound: NONE (skipped)");
                            }

                            // Move to next question
                            currentQuestionNumber++;

                            if (currentQuestionNumber > totalQuestions) {
                                Log.e(TAG, "║ - Status: TEST COMPLETE (with errors)");
                                Log.e(TAG, "╚════════════════════════════════════════════════════════════");
                                soundEffectsHelper.playCelebration();
                                showResults();
                            } else {
                                Log.e(TAG, "║ - Status: Moving to next question");
                                Log.e(TAG, "╚════════════════════════════════════════════════════════════");
                                soundEffectsHelper.playChime();
                                loadNextQuestion();
                            }
                        });
                    }
                }
        );
    }

    private void showResults() {
        // Navigate to PlacementResultActivity with IRT results
        int placementLevel = irtEngine.calculatePlacementLevel();
        String levelName = irtEngine.getPlacementLevelName();
        double accuracy = irtEngine.getAccuracyPercentage();
        int totalAnswered = irtEngine.getTotalAnswered();
        int totalCorrect = irtEngine.getTotalCorrect();
        int[] categoryScores = irtEngine.getCategoryScores();

        // Get final theta from API (adaptiveHelper has the accurate theta from server)
        double finalTheta = adaptiveHelper.getCurrentTheta();

        // Save placement results to SessionManager for dashboard
        sessionManager.savePlacementLevel(levelName);

        // Save category scores to session manager (as percentages)
        if (categoryScores.length >= 5) {
            sessionManager.saveCategoryScore("Cat1_PhonicsWordStudy", categoryScores[0]);
            sessionManager.saveCategoryScore("Cat2_VocabularyWordKnowledge", categoryScores[1]);
            sessionManager.saveCategoryScore("Cat3_GrammarAwareness", categoryScores[2]);
            sessionManager.saveCategoryScore("Cat4_ComprehendingText", categoryScores[3]);
            sessionManager.saveCategoryScore("Cat5_CreatingComposing", categoryScores[4]);
        }

        // Create intent with results
        Intent intent = new Intent(PlacementTestActivity.this, PlacementResultActivity.class);
        intent.putExtra("placement_level", placementLevel);
        intent.putExtra("level_name", levelName);
        intent.putExtra("accuracy", accuracy);
        intent.putExtra("total_answered", totalAnswered);
        intent.putExtra("total_correct", totalCorrect);
        intent.putExtra("category_scores", categoryScores);
        intent.putExtra("final_theta", finalTheta);
        intent.putExtra("start_time", startTime);

        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void logAssessmentStart() {
        int studentId = sessionManager.getStudentId();
        if (studentId == 0) {
            return;
        }

        // Mark assessment as started (for resume tracking)
        sessionManager.setAssessmentStarted(true);

        // Determine assessment type based on whether student has completed assessment before
        String assessmentType = sessionManager.hasCompletedAssessment() ? "PostAssessment" : "PreAssessment";

        // Log assessment start
        SessionLogger.logAssessmentStart(this, studentId, assessmentType);
    }

    @Override
    public void onBackPressed() {
        // Show confirmation dialog before exiting
        // For now, just finish
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Pause background music when activity is paused
        if (soundEffectsHelper != null) {
            soundEffectsHelper.pauseBackgroundMusic();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume background music when activity resumes
        if (soundEffectsHelper != null) {
            soundEffectsHelper.resumeBackgroundMusic();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, reload pronunciation question
                loadPronunciationQuestion();
            } else {
                // Permission denied, show message and move to next question
                Toast.makeText(this, "Microphone permission is required for pronunciation questions",
                        Toast.LENGTH_LONG).show();
                // Auto-advance to next question after short delay
                new Handler(Looper.getMainLooper()).postDelayed(this::loadNextQuestion, 2000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionBankHelper != null) {
            questionBankHelper.close();
        }
        if (speechRecognitionHelper != null) {
            speechRecognitionHelper.destroy();
        }
        if (karaokeTextHelper != null) {
            karaokeTextHelper.destroy();
        }
        if (textToSpeechHelper != null) {
            textToSpeechHelper.shutdown();
        }
        if (soundEffectsHelper != null) {
            soundEffectsHelper.release();
        }
    }
}