package com.example.literise.activities.games;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.SaveGameResultRequest;
import com.example.literise.models.SaveGameResultResponse;
import com.example.literise.models.TimedTrailQuestion;
import com.example.literise.models.TimedTrailResponse;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Timed Trail Game Activity
 * Speed reading with comprehension questions
 * Tracks reading speed (WPM) and comprehension accuracy
 */
public class TimedTrailActivity extends AppCompatActivity {

    // Views
    private TextView tvTitle, tvProgress, tvTimer, tvScore, tvWPM;
    private TextView tvPassage, tvQuestion;
    private LinearLayout choicesContainer;
    private ProgressBar progressBar, timerProgress, loadingProgress;
    private MaterialButton btnSkip, btnNext;
    private CardView cardPassage, cardQuestion;

    // Game State
    private List<TimedTrailQuestion> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int correctAnswers = 0;
    private int selectedAnswer = -1;
    private long startTime;
    private long questionStartTime;
    private int totalWordsRead = 0;
    private long totalReadingTime = 0; // in milliseconds

    // Timer
    private CountDownTimer questionTimer;
    private int questionTimeLimit = 30; // Default 30 seconds per question
    private long questionTimeRemaining;

    // Animation
    private Handler handler = new Handler(Looper.getMainLooper());

    // Session
    private SessionManager session;
    private Integer lessonId;
    private Integer sessionId;

    // Constants
    private static final int POINTS_PER_CORRECT = 100;
    private static final int TIME_BONUS_THRESHOLD = 15; // seconds
    private static final int TIME_BONUS = 50;
    private static final int SPEED_BONUS = 25; // For fast WPM

    // Choice views for tracking
    private List<View> choiceViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timed_trail);

        session = new SessionManager(this);
        startTime = System.currentTimeMillis();

        // Get lesson and session info from intent
        Intent intent = getIntent();
        if (intent != null) {
            lessonId = intent.hasExtra("lesson_id") ? intent.getIntExtra("lesson_id", -1) : null;
            sessionId = intent.hasExtra("session_id") ? intent.getIntExtra("session_id", -1) : null;
            if (lessonId != null && lessonId == -1) lessonId = null;
            if (sessionId != null && sessionId == -1) sessionId = null;
        }

        initializeViews();
        setupListeners();
        loadQuestions();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvProgress = findViewById(R.id.tvProgress);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvWPM = findViewById(R.id.tvWPM);

        tvPassage = findViewById(R.id.tvPassage);
        tvQuestion = findViewById(R.id.tvQuestion);
        choicesContainer = findViewById(R.id.choicesContainer);

        progressBar = findViewById(R.id.progressBar);
        timerProgress = findViewById(R.id.timerProgress);
        loadingProgress = findViewById(R.id.loadingProgress);

        btnSkip = findViewById(R.id.btnSkip);
        btnNext = findViewById(R.id.btnNext);

        cardPassage = findViewById(R.id.cardPassage);
        cardQuestion = findViewById(R.id.cardQuestion);
    }

    private void setupListeners() {
        findViewById(R.id.ivClose).setOnClickListener(v -> showExitConfirmation());
        btnSkip.setOnClickListener(v -> skipQuestion());
        btnNext.setOnClickListener(v -> nextQuestion());
    }

    private void loadQuestions() {
        loadingProgress.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);

        Call<TimedTrailResponse> call;
        if (lessonId != null) {
            call = apiService.getTimedTrailQuestions(10, lessonId);
        } else {
            call = apiService.getTimedTrailQuestions(10);
        }

        call.enqueue(new Callback<TimedTrailResponse>() {
            @Override
            public void onResponse(Call<TimedTrailResponse> call, Response<TimedTrailResponse> response) {
                loadingProgress.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    questions = response.body().getQuestions();
                    if (questions != null && !questions.isEmpty()) {
                        startGame();
                    } else {
                        loadFallbackQuestions();
                        startGame();
                    }
                } else {
                    loadFallbackQuestions();
                    startGame();
                }
            }

            @Override
            public void onFailure(Call<TimedTrailResponse> call, Throwable t) {
                loadingProgress.setVisibility(View.GONE);
                loadFallbackQuestions();
                startGame();
            }
        });
    }

    private void loadFallbackQuestions() {
        questions = new ArrayList<>();

        // Grade 4-6 appropriate reading passages with comprehension questions
        questions.add(new TimedTrailQuestion(1,
                "The sun was setting behind the mountains, painting the sky in shades of orange and pink. Maria watched from her window, thinking about her grandmother who lived far away. She missed their walks together in the garden.",
                "What was Maria doing?",
                Arrays.asList("Playing in the garden", "Watching the sunset", "Visiting her grandmother", "Painting a picture"),
                1, 0.5f));

        questions.add(new TimedTrailQuestion(2,
                "Scientists have discovered that dolphins are among the smartest animals on Earth. They can recognize themselves in mirrors, solve complex problems, and even communicate with each other using a variety of sounds.",
                "What makes dolphins special according to the passage?",
                Arrays.asList("They can swim fast", "They are very intelligent", "They live in the ocean", "They eat fish"),
                1, 0.6f));

        questions.add(new TimedTrailQuestion(3,
                "The library was quiet except for the soft rustle of turning pages. Jake found a book about space exploration and couldn't put it down. He learned that astronauts train for years before going to space.",
                "What did Jake learn from the book?",
                Arrays.asList("Libraries are quiet", "Books are interesting", "Astronauts train for years", "Space is far away"),
                2, 0.5f));

        questions.add(new TimedTrailQuestion(4,
                "Recycling helps protect our environment by reducing waste. When we recycle paper, plastic, and metal, these materials can be made into new products instead of going to landfills. Every person can make a difference.",
                "What is the main idea of this passage?",
                Arrays.asList("Landfills are bad", "Recycling helps the environment", "Paper can be recycled", "Everyone should work"),
                1, 0.6f));

        questions.add(new TimedTrailQuestion(5,
                "The ancient Egyptians built the pyramids over 4,000 years ago. These massive structures were tombs for pharaohs. Workers moved huge stone blocks without modern machines, showing incredible skill and determination.",
                "Why were the pyramids built?",
                Arrays.asList("As homes for workers", "As tombs for pharaohs", "As places to store food", "As temples for worship"),
                1, 0.7f));

        questions.add(new TimedTrailQuestion(6,
                "Plants need three things to grow: sunlight, water, and nutrients from the soil. Through photosynthesis, plants use sunlight to turn water and carbon dioxide into food. This process also produces oxygen that we breathe.",
                "What do plants produce through photosynthesis?",
                Arrays.asList("Carbon dioxide", "Sunlight", "Oxygen and food", "Water and soil"),
                2, 0.6f));

        questions.add(new TimedTrailQuestion(7,
                "The weather forecast predicted rain, but Sarah forgot her umbrella. By the time she reached school, her clothes were soaked. She wished she had listened to the morning news more carefully.",
                "What happened to Sarah?",
                Arrays.asList("She watched the news", "She got wet in the rain", "She brought an umbrella", "She stayed home"),
                1, 0.4f));

        questions.add(new TimedTrailQuestion(8,
                "Bees play a vital role in our ecosystem. They pollinate flowers and crops, helping plants reproduce. Without bees, many fruits and vegetables we eat would not exist. Protecting bees is important for our food supply.",
                "Why are bees important?",
                Arrays.asList("They make honey", "They pollinate plants we need for food", "They fly quickly", "They live in hives"),
                1, 0.7f));

        questions.add(new TimedTrailQuestion(9,
                "The Wright Brothers made history in 1903 when they flew the first airplane at Kitty Hawk. Their flight lasted only 12 seconds, but it changed transportation forever. Today, millions of people travel by plane every day.",
                "How long was the Wright Brothers' first flight?",
                Arrays.asList("12 minutes", "12 hours", "12 seconds", "12 days"),
                2, 0.5f));

        questions.add(new TimedTrailQuestion(10,
                "Volcanoes form when hot melted rock called magma rises from deep inside the Earth. When a volcano erupts, lava flows out and ash fills the sky. While dangerous, volcanic soil is very fertile for growing crops.",
                "What is magma?",
                Arrays.asList("Cold rock", "Hot melted rock", "Volcanic ash", "Fertile soil"),
                1, 0.6f));
    }

    private void startGame() {
        displayCurrentQuestion();
        updateProgress();
    }

    private void displayCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            endGame();
            return;
        }

        TimedTrailQuestion question = questions.get(currentQuestionIndex);
        questionStartTime = System.currentTimeMillis();
        selectedAnswer = -1;
        btnNext.setEnabled(false);

        // Display passage
        tvPassage.setText(question.getPassage());

        // Display question
        tvQuestion.setText(question.getQuestion());

        // Display choices
        displayChoices(question);

        // Start timer for this question
        startQuestionTimer(question.getTimeLimit());

        // Animate entry
        animateQuestionEntry();
    }

    private void displayChoices(TimedTrailQuestion question) {
        choicesContainer.removeAllViews();
        choiceViews.clear();

        List<String> choices = question.getChoices();
        if (choices == null) return;

        for (int i = 0; i < choices.size(); i++) {
            View choiceView = createChoiceView(choices.get(i), i);
            choicesContainer.addView(choiceView);
            choiceViews.add(choiceView);
        }
    }

    private View createChoiceView(String choiceText, int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_choice_button, choicesContainer, false);

        TextView tvChoice = view.findViewById(R.id.tvChoice);
        CardView cardChoice = view.findViewById(R.id.cardChoice);

        String label = String.valueOf((char) ('A' + index));
        tvChoice.setText(label + ". " + choiceText);

        view.setTag(index);
        view.setOnClickListener(v -> selectChoice(index));

        return view;
    }

    private void selectChoice(int index) {
        selectedAnswer = index;
        btnNext.setEnabled(true);

        // Update visual selection
        for (int i = 0; i < choiceViews.size(); i++) {
            View view = choiceViews.get(i);
            CardView card = view.findViewById(R.id.cardChoice);
            TextView text = view.findViewById(R.id.tvChoice);

            if (i == index) {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_jade1));
                text.setTextColor(Color.WHITE);
            } else {
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.white));
                text.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            }
        }

        playSound(SOUND_SELECT);
    }

    private void startQuestionTimer(int seconds) {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        questionTimeLimit = seconds;
        timerProgress.setMax(seconds);
        timerProgress.setProgress(seconds);

        questionTimer = new CountDownTimer(seconds * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                questionTimeRemaining = millisUntilFinished;
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format(Locale.getDefault(), "0:%02d", secondsLeft));
                timerProgress.setProgress(secondsLeft);

                // Warning color when low time
                if (secondsLeft <= 10) {
                    tvTimer.setTextColor(ContextCompat.getColor(TimedTrailActivity.this, R.color.color_error));
                } else {
                    tvTimer.setTextColor(ContextCompat.getColor(TimedTrailActivity.this, R.color.color_warning));
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0:00");
                // Auto-submit or skip when time runs out
                if (selectedAnswer >= 0) {
                    nextQuestion();
                } else {
                    skipQuestion();
                }
            }
        }.start();
    }

    private void nextQuestion() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        if (currentQuestionIndex >= questions.size()) {
            endGame();
            return;
        }

        TimedTrailQuestion question = questions.get(currentQuestionIndex);
        long readingTime = System.currentTimeMillis() - questionStartTime;
        totalReadingTime += readingTime;
        totalWordsRead += question.getWordCount();

        // Check answer
        boolean isCorrect = selectedAnswer == question.getCorrectAnswer();

        if (isCorrect) {
            handleCorrectAnswer(readingTime);
        } else {
            handleWrongAnswer(question.getCorrectAnswer());
        }
    }

    private void handleCorrectAnswer(long readingTime) {
        correctAnswers++;

        // Calculate points
        int points = POINTS_PER_CORRECT;

        // Time bonus - answered quickly
        int secondsTaken = (int) (readingTime / 1000);
        if (secondsTaken < TIME_BONUS_THRESHOLD) {
            points += TIME_BONUS;
        }

        // Speed bonus - good WPM
        int wpm = calculateWPM();
        if (wpm > 150) {
            points += SPEED_BONUS;
        }

        score += points;

        // Show correct feedback
        showAnswerFeedback(true, selectedAnswer);

        CustomToast.showSuccess(this, "Correct! +" + points + " XP");
        playSound(SOUND_CORRECT);

        // Move to next after delay
        handler.postDelayed(() -> {
            currentQuestionIndex++;
            updateProgress();
            updateWPM();
            displayCurrentQuestion();
        }, 1500);
    }

    private void handleWrongAnswer(int correctIndex) {
        // Show wrong and correct feedback
        showAnswerFeedback(false, correctIndex);

        CustomToast.showError(this, "Not quite! The correct answer was shown.");
        playSound(SOUND_WRONG);

        // Move to next after delay
        handler.postDelayed(() -> {
            currentQuestionIndex++;
            updateProgress();
            updateWPM();
            displayCurrentQuestion();
        }, 2000);
    }

    private void showAnswerFeedback(boolean wasCorrect, int correctIndex) {
        for (int i = 0; i < choiceViews.size(); i++) {
            View view = choiceViews.get(i);
            CardView card = view.findViewById(R.id.cardChoice);
            TextView text = view.findViewById(R.id.tvChoice);

            if (i == correctIndex) {
                // Show correct answer in green
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_success));
                text.setTextColor(Color.WHITE);
            } else if (i == selectedAnswer && !wasCorrect) {
                // Show wrong selection in red
                card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_error));
                text.setTextColor(Color.WHITE);
            }
        }
    }

    private void skipQuestion() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        currentQuestionIndex++;
        updateProgress();

        if (currentQuestionIndex >= questions.size()) {
            endGame();
        } else {
            displayCurrentQuestion();
        }
    }

    private int calculateWPM() {
        if (totalReadingTime <= 0 || totalWordsRead <= 0) return 0;
        // WPM = (words / time in minutes)
        float minutes = totalReadingTime / 60000f;
        if (minutes <= 0) return 0;
        return (int) (totalWordsRead / minutes);
    }

    private void updateWPM() {
        tvWPM.setText(String.valueOf(calculateWPM()));
    }

    private void updateProgress() {
        int progress = (int) (((float) currentQuestionIndex / questions.size()) * 100);
        progressBar.setProgress(progress);
        tvProgress.setText(String.format(Locale.getDefault(), "Question %d/%d",
                Math.min(currentQuestionIndex + 1, questions.size()), questions.size()));
        tvScore.setText(String.valueOf(score));
    }

    private void animateQuestionEntry() {
        cardPassage.setAlpha(0f);
        cardPassage.setTranslationY(30f);
        cardPassage.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(300)
                .start();

        cardQuestion.setAlpha(0f);
        cardQuestion.animate()
                .alpha(1f)
                .setStartDelay(200)
                .setDuration(300)
                .start();
    }

    private void endGame() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        float accuracy = questions.size() > 0 ? ((float) correctAnswers / questions.size()) * 100 : 0;
        int finalWPM = calculateWPM();

        saveGameResults(accuracy, totalTime, finalWPM);
        showResultDialog(accuracy, totalTime, finalWPM);
    }

    private void saveGameResults(float accuracy, long totalTime, int wpm) {
        int studentId = session.getStudentId();
        if (studentId <= 0) return;

        int timeInSeconds = (int) (totalTime / 1000);

        SaveGameResultRequest request = new SaveGameResultRequest.Builder(studentId, "TimedTrail", score)
                .sessionId(sessionId)
                .lessonId(lessonId)
                .accuracyPercentage(accuracy)
                .timeCompleted(timeInSeconds)
                .xpEarned(score)
                .streakAchieved(correctAnswers)
                .build();

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.saveGameResult(request).enqueue(new Callback<SaveGameResultResponse>() {
            @Override
            public void onResponse(Call<SaveGameResultResponse> call, Response<SaveGameResultResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    SaveGameResultResponse.StudentStats stats = response.body().getStudent();
                    if (stats != null) {
                        session.updateTotalXP(stats.getTotalXP());
                    }
                }
            }

            @Override
            public void onFailure(Call<SaveGameResultResponse> call, Throwable t) {
                android.util.Log.e("TimedTrail", "Failed to save game result: " + t.getMessage());
            }
        });
    }

    private void showResultDialog(float accuracy, long totalTime, int wpm) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_result, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvResultTitle = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvResultScore = dialogView.findViewById(R.id.tvResultScore);
        TextView tvResultAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);
        TextView tvResultStreak = dialogView.findViewById(R.id.tvResultStreak);
        TextView tvResultTime = dialogView.findViewById(R.id.tvResultTime);
        TextView tvResultXP = dialogView.findViewById(R.id.tvResultXP);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        // Determine title based on performance
        String title;
        if (accuracy >= 90 && wpm >= 150) {
            title = "Speed Reader!";
        } else if (accuracy >= 80) {
            title = "Great Job!";
        } else if (accuracy >= 60) {
            title = "Good Effort!";
        } else {
            title = "Keep Practicing!";
        }
        tvResultTitle.setText(title);

        tvResultScore.setText(String.valueOf(score));
        tvResultAccuracy.setText(String.format(Locale.getDefault(), "%.0f%%", accuracy));
        tvResultStreak.setText(wpm + " WPM"); // Show WPM instead of streak

        int minutes = (int) (totalTime / 60000);
        int seconds = (int) ((totalTime % 60000) / 1000);
        tvResultTime.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));

        tvResultXP.setText("+" + score + " XP");

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            finishWithResult(accuracy);
        });

        dialog.show();
    }

    private void finishWithResult(float accuracy) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("xp_earned", score);
        resultIntent.putExtra("accuracy", (int) accuracy);
        resultIntent.putExtra("correct_count", correctAnswers);
        resultIntent.putExtra("total_count", questions.size());
        resultIntent.putExtra("wpm", calculateWPM());
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Game?")
                .setMessage("Your progress will be lost. Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    if (questionTimer != null) questionTimer.cancel();
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    private static final int SOUND_SELECT = 1;
    private static final int SOUND_CORRECT = 2;
    private static final int SOUND_WRONG = 3;

    private void playSound(int soundType) {
        try {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            switch (soundType) {
                case SOUND_SELECT:
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
                    break;
                case SOUND_CORRECT:
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150);
                    break;
                case SOUND_WRONG:
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 150);
                    break;
            }
            handler.postDelayed(toneGenerator::release, 200);
        } catch (Exception e) {
            // Ignore sound errors
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) {
            questionTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
