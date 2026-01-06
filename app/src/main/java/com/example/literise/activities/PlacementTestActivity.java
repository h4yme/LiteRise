package com.example.literise.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
    private int questionsPerCategory = 6; // Approximate
    private long startTime;
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
                        if (currentCategory < 4) {
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
                icon = "📚";
                title = "Ready?";
                name = "Category 1: Oral Language";
                message = "Let's start with some fun questions about listening and speaking! You've got this! 🌟";
                break;
            case 2:
                icon = "🔤";
                title = "Great Job!";
                name = "Category 2: Word Knowledge";
                message = "Now let's test your word knowledge! You're doing amazing! 🌟";
                break;
            case 3:
                icon = "📖";
                title = "Awesome Work!";
                name = "Category 3: Reading Comprehension";
                message = "Time for some fun stories! Let's see how well you understand what you read! 📚";
                break;
            case 4:
                icon = "✏️";
                title = "Almost There!";
                name = "Category 4: Language Structure";
                message = "Last category! Let's work on grammar and sentences! You're doing wonderfully! 💪";
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
        } else {
            currentCategory = 4;
        }
    }

    /**
     * Convert category number to category name for API
     */
    private String getCategoryName(int categoryNumber) {
        switch (categoryNumber) {
            case 1:
                return "Oral Language";
            case 2:
                return "Word Knowledge";
            case 3:
                return "Reading Comprehension";
            case 4:
                return "Language Structure";
            default:
                return "Oral Language";
        }
    }

    /**
     * Convert API question response to PlacementQuestion format
     */
    private PlacementQuestion convertToPlacementQuestion(AdaptiveQuestionResponse.QuestionData apiQuestion) {
        PlacementQuestion question = new PlacementQuestion();

        // Set basic fields
        question.setQuestionId(apiQuestion.getItemId());
        question.setCategory(apiQuestion.getCategory());
        question.setSubcategory(apiQuestion.getSubcategory() != null ? apiQuestion.getSubcategory() : "");
        question.setQuestionText(apiQuestion.getQuestionText());
        question.setQuestionType(apiQuestion.getQuestionType());
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

        if ("pronunciation".equalsIgnoreCase(questionType)) {
            loadPronunciationQuestion();
        } else if ("reading".equalsIgnoreCase(questionType)) {
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

        // Initialize speech recognition
        if (speechRecognitionHelper == null) {
            speechRecognitionHelper = new SpeechRecognitionHelper(this);
        }

        // Get UI elements
        TextView tvWordToPronounce = questionView.findViewById(R.id.tvWordToPronounce);
        TextView tvRecordingStatus = questionView.findViewById(R.id.tvRecordingStatus);
        FloatingActionButton btnMicrophone = questionView.findViewById(R.id.btnMicrophone);
        LinearLayout waveformContainer = questionView.findViewById(R.id.waveformContainer);
        MaterialButton btnPlayback = questionView.findViewById(R.id.btnPlayback);
        MaterialCardView feedbackCard = questionView.findViewById(R.id.feedbackCard);
        TextView tvFeedbackIcon = questionView.findViewById(R.id.tvFeedbackIcon);
        TextView tvFeedbackText = questionView.findViewById(R.id.tvFeedbackText);
        TextView tvScore = questionView.findViewById(R.id.tvScore);

        // Set the word to pronounce
        String wordToPronounce = currentQuestion.getQuestionText();
        tvWordToPronounce.setText(wordToPronounce);

        // Microphone button click listener
        btnMicrophone.setOnClickListener(v -> {
            if (!speechRecognitionHelper.isListening()) {
                // Start recording
                tvRecordingStatus.setText("Listening... Say the word now!");
                tvRecordingStatus.setTextColor(getColor(R.color.error_red));
                btnMicrophone.setBackgroundTintList(getColorStateList(R.color.error_red));
                waveformContainer.setVisibility(View.VISIBLE);
                feedbackCard.setVisibility(View.GONE);

                // Start animated waveform
                animateWaveform(waveformContainer);

                // Start speech recognition
                speechRecognitionHelper.startListening(new SpeechRecognitionHelper.RecognitionCallback() {
                    @Override
                    public void onRecognitionReady() {
                        runOnUiThread(() -> {
                            tvRecordingStatus.setText("Listening... Speak now!");
                        });
                    }

                    @Override
                    public void onRecognitionStarted() {
                        // Already handled in onRecognitionReady
                    }

                    @Override
                    public void onRecognitionResult(String recognizedText, float confidence) {
                        runOnUiThread(() -> {
                            // Stop waveform animation
                            waveformContainer.clearAnimation();
                            waveformContainer.setVisibility(View.GONE);

                            // Calculate pronunciation accuracy
                            int accuracy = SpeechRecognitionHelper.calculatePronunciationAccuracy(
                                    wordToPronounce, recognizedText);

                            // Show feedback
                            feedbackCard.setVisibility(View.VISIBLE);
                            tvScore.setText(accuracy + "% Match");

                            if (accuracy >= 85) {
                                tvFeedbackIcon.setText("🎉");
                                tvFeedbackText.setText("Excellent! You said it perfectly!");
                                feedbackCard.setCardBackgroundColor(getColor(R.color.success_light));
                                tvFeedbackText.setTextColor(getColor(R.color.success_green));
                                tvScore.setTextColor(getColor(R.color.success_green));
                                selectedAnswer = currentQuestion.getCorrectAnswer(); // Mark as correct
                            } else if (accuracy >= 65) {
                                tvFeedbackIcon.setText("👍");
                                tvFeedbackText.setText("Good try! Almost there!");
                                feedbackCard.setCardBackgroundColor(getColor(R.color.warning_light));
                                tvFeedbackText.setTextColor(getColor(R.color.warning_orange));
                                tvScore.setTextColor(getColor(R.color.warning_orange));
                                selectedAnswer = recognizedText; // Partial credit
                            } else {
                                tvFeedbackIcon.setText("🔄");
                                tvFeedbackText.setText("Try again! You can do it!");
                                feedbackCard.setCardBackgroundColor(getColor(R.color.error_light));
                                tvFeedbackText.setTextColor(getColor(R.color.error_red));
                                tvScore.setTextColor(getColor(R.color.error_red));
                                selectedAnswer = recognizedText; // Incorrect
                            }

                            // Reset button
                            tvRecordingStatus.setText("Tap to try again");
                            tvRecordingStatus.setTextColor(getColor(R.color.text_secondary));
                            btnMicrophone.setBackgroundTintList(getColorStateList(R.color.success_green));

                            // Enable continue button
                            btnContinue.setEnabled(true);
                            android.view.animation.Animation bounceAnim = android.view.animation.AnimationUtils.loadAnimation(
                                    PlacementTestActivity.this, R.anim.bounce);
                            btnContinue.startAnimation(bounceAnim);
                        });
                    }

                    @Override
                    public void onRecognitionError(String error) {
                        runOnUiThread(() -> {
                            waveformContainer.clearAnimation();
                            waveformContainer.setVisibility(View.GONE);
                            tvRecordingStatus.setText(error);
                            tvRecordingStatus.setTextColor(getColor(R.color.error_red));
                            btnMicrophone.setBackgroundTintList(getColorStateList(R.color.success_green));
                        });
                    }

                    @Override
                    public void onRecognitionEnded() {
                        runOnUiThread(() -> {
                            waveformContainer.clearAnimation();
                            waveformContainer.setVisibility(View.GONE);
                        });
                    }
                });
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
                                comprehensionCard.setVisibility(View.VISIBLE);
                                tvComprehensionQuestion.setText(currentQuestion.getQuestionText());

                                List<String> options = currentQuestion.getOptions();
                                if (options.size() >= 3) {
                                    btnAnswer1.setText(options.get(0));
                                    btnAnswer2.setText(options.get(1));
                                    btnAnswer3.setText(options.get(2));
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

            // Highlight selected
            MaterialButton selectedBtn = (MaterialButton) v;
            selectedBtn.setBackgroundTintList(getColorStateList(R.color.primary_blue));
            selectedBtn.setTextColor(getColor(android.R.color.white));

            // Play pop animation
            android.view.animation.Animation popAnim = android.view.animation.AnimationUtils.loadAnimation(
                    this, R.anim.option_pop);
            v.startAnimation(popAnim);

            // Store answer
            selectedAnswer = selectedBtn.getText().toString();

            // Enable continue button
            btnContinue.setEnabled(true);
            android.view.animation.Animation bounceAnim = android.view.animation.AnimationUtils.loadAnimation(
                    this, R.anim.bounce);
            btnContinue.startAnimation(bounceAnim);
        };

        btnAnswer1.setOnClickListener(answerClickListener);
        btnAnswer2.setOnClickListener(answerClickListener);
        btnAnswer3.setOnClickListener(answerClickListener);

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

            // Show checkmark for selected option
            if (v.getId() == R.id.optionA) {
                selectedAnswer = options.get(0);
                checkA.setVisibility(View.VISIBLE);
                optionA.setStrokeColor(getColor(R.color.success_green));
            } else if (v.getId() == R.id.optionB) {
                selectedAnswer = options.get(1);
                checkB.setVisibility(View.VISIBLE);
                optionB.setStrokeColor(getColor(R.color.success_green));
            } else if (v.getId() == R.id.optionC) {
                selectedAnswer = options.get(2);
                checkC.setVisibility(View.VISIBLE);
                optionC.setStrokeColor(getColor(R.color.success_green));
            } else if (v.getId() == R.id.optionD) {
                selectedAnswer = options.get(3);
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

        // If no answer selected, treat as incorrect
        final String finalSelectedAnswer = selectedAnswer.isEmpty() ? "" : selectedAnswer;

        // Submit answer to API
        final int finalResponseTime = responseTime;
        adaptiveHelper.submitAnswer(
                currentQuestion.getQuestionId(),
                finalSelectedAnswer,
                false, // We don't know if correct yet - API will tell us
                finalResponseTime,
                new AdaptiveQuestionHelper.AnswerCallback() {
                    @Override
                    public void onSuccess(SubmitAnswerResponse response) {
                        runOnUiThread(() -> {
                            // Get correctness from server response
                            boolean isCorrect = response.isCorrect();

                            // Play appropriate sound effect
                            if (isCorrect) {
                                soundEffectsHelper.playSuccess();
                            } else if (!finalSelectedAnswer.isEmpty()) {
                                soundEffectsHelper.playError();
                            }

                            // Update IRT engine with result (for local tracking)
                            irtEngine.updateTheta(currentQuestion, isCorrect);

                            // Move to next question
                            currentQuestionNumber++;

                            if (currentQuestionNumber > totalQuestions) {
                                // Test complete - show results with celebration
                                soundEffectsHelper.playCelebration();
                                showResults();
                            } else {
                                // Play chime for question completion
                                soundEffectsHelper.playChime();
                                loadNextQuestion();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            // Log error but continue
                            Toast.makeText(PlacementTestActivity.this,
                                    "Error submitting answer: " + error, Toast.LENGTH_SHORT).show();

                            // Fallback: use local IRT engine to check answer
                            boolean isCorrect = false;
                            if (!finalSelectedAnswer.isEmpty() && currentQuestion.getCorrectAnswer() != null) {
                                isCorrect = finalSelectedAnswer.equalsIgnoreCase(currentQuestion.getCorrectAnswer());
                            }

                            // Play appropriate sound effect
                            if (isCorrect) {
                                soundEffectsHelper.playSuccess();
                            } else if (!finalSelectedAnswer.isEmpty()) {
                                soundEffectsHelper.playError();
                            }

                            // Update IRT engine with result
                            irtEngine.updateTheta(currentQuestion, isCorrect);

                            // Move to next question
                            currentQuestionNumber++;

                            if (currentQuestionNumber > totalQuestions) {
                                soundEffectsHelper.playCelebration();
                                showResults();
                            } else {
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
        double finalTheta = irtEngine.getTheta();

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
