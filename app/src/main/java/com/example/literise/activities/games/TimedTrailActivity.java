package com.example.literise.activities.games;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Timed Trail - Race track comprehension game
 *
 * Students race an avatar forward by answering comprehension questions correctly.
 * The faster and more accurate they are, the further they progress on the track!
 *
 * Features:
 * - Race track visualization with animated avatar
 * - Multiple choice comprehension questions
 * - Time pressure for urgency
 * - Distance traveled = score
 * - Streak bonuses for consecutive correct answers
 */
public class TimedTrailActivity extends BaseGameActivity {

    // UI Components
    private ImageView btnBack;
    private TextView tvTimer, tvScore, tvStreak, tvQuestion, tvProgress;
    private ProgressBar progressTrack;
    private ImageView ivAvatar;
    private LinearLayout optionsContainer;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4;
    private CardView cardQuestion;

    // Game State
    private int currentQuestionIndex = 0;
    private int totalQuestions = 10;
    private int correctAnswers = 0;
    private int currentStreak = 0;
    private int maxStreak = 0;
    private int distanceTraveled = 0; // Score based on distance
    private long startTime;
    private CountDownTimer questionTimer;
    private SessionManager session;

    // Game Configuration
    private static final int TIME_PER_QUESTION = 30000; // 30 seconds per question
    private static final int TOTAL_GAME_TIME = 300000; // 5 minutes total
    private static final int DISTANCE_PER_CORRECT = 10; // Meters per correct answer
    private static final int STREAK_BONUS = 5; // Extra distance for streaks

    // Questions
    private List<TrailQuestion> questions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timed_trail);

        session = new SessionManager(this);

        initializeViews();
        setupQuestions();
        setupListeners();
        startGame();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTimer = findViewById(R.id.tv_timer);
        tvScore = findViewById(R.id.tv_score);
        tvStreak = findViewById(R.id.tv_streak);
        tvQuestion = findViewById(R.id.tv_question);
        tvProgress = findViewById(R.id.tv_progress);
        progressTrack = findViewById(R.id.progress_track);
        ivAvatar = findViewById(R.id.iv_avatar);
        optionsContainer = findViewById(R.id.options_container);
        btnOption1 = findViewById(R.id.btn_option_1);
        btnOption2 = findViewById(R.id.btn_option_2);
        btnOption3 = findViewById(R.id.btn_option_3);
        btnOption4 = findViewById(R.id.btn_option_4);
        cardQuestion = findViewById(R.id.card_question);

        // Initial setup
        progressTrack.setMax(100); // 100 meters to finish
        progressTrack.setProgress(0);
        tvProgress.setText("0 / 100m");
        tvScore.setText("0m");
        tvStreak.setText("Streak: 0");
    }

    private void setupQuestions() {
        questions = new ArrayList<>();

        // Sample comprehension questions (these would come from lesson content)
        questions.add(new TrailQuestion(
                "What is a sight word?",
                new String[]{"A word you recognize instantly", "A word that's hard to read", "A word with pictures", "A word in a book"},
                0
        ));

        questions.add(new TrailQuestion(
                "Which word rhymes with 'cat'?",
                new String[]{"car", "bat", "cut", "cot"},
                1
        ));

        questions.add(new TrailQuestion(
                "What is a CVC word?",
                new String[]{"Consonant-Vowel-Consonant", "Capital-Vowel-Capital", "Cat-Van-Car", "Cute-Voice-Call"},
                0
        ));

        questions.add(new TrailQuestion(
                "Which is a digraph?",
                new String[]{"st", "sh", "bl", "tr"},
                1
        ));

        questions.add(new TrailQuestion(
                "How many syllables in 'basket'?",
                new String[]{"1", "2", "3", "4"},
                1
        ));

        questions.add(new TrailQuestion(
                "Which is a compound word?",
                new String[]{"running", "sunshine", "walked", "quickly"},
                1
        ));

        questions.add(new TrailQuestion(
                "What sound does 'ch' make?",
                new String[]{"k sound", "ch sound", "s sound", "t sound"},
                1
        ));

        questions.add(new TrailQuestion(
                "Which word has a long vowel?",
                new String[]{"cat", "cake", "cap", "can"},
                1
        ));

        questions.add(new TrailQuestion(
                "What is blending?",
                new String[]{"Mixing colors", "Combining sounds", "Writing words", "Reading fast"},
                1
        ));

        questions.add(new TrailQuestion(
                "Which is a word family?",
                new String[]{"cat, bat, mat", "cat, dog, bird", "red, blue, green", "run, jump, walk"},
                0
        ));

        // Shuffle for variety
        Collections.shuffle(questions);
        totalQuestions = questions.size();
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Quit Race?")
                    .setMessage("Your progress will be lost!")
                    .setPositiveButton("Keep Racing", null)
                    .setNegativeButton("Quit", (d, w) -> finish())
                    .show();
        });

        btnOption1.setOnClickListener(v -> checkAnswer(0));
        btnOption2.setOnClickListener(v -> checkAnswer(1));
        btnOption3.setOnClickListener(v -> checkAnswer(2));
        btnOption4.setOnClickListener(v -> checkAnswer(3));
    }

    private void startGame() {
        startTime = System.currentTimeMillis();
        loadQuestion();
        startQuestionTimer();
    }

    private void loadQuestion() {
        if (currentQuestionIndex >= totalQuestions) {
            endGame();
            return;
        }

        TrailQuestion question = questions.get(currentQuestionIndex);

        // Update UI
        tvQuestion.setText(question.question);
        tvProgress.setText((currentQuestionIndex + 1) + " / " + totalQuestions);

        // Set options
        btnOption1.setText(question.options[0]);
        btnOption2.setText(question.options[1]);
        btnOption3.setText(question.options[2]);
        btnOption4.setText(question.options[3]);

        // Reset button states
        resetButtonStates();

        // Animate question card
        cardQuestion.setAlpha(0f);
        cardQuestion.animate().alpha(1f).setDuration(300).start();
    }

    private void startQuestionTimer() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        questionTimer = new CountDownTimer(TIME_PER_QUESTION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.format(Locale.getDefault(), "%d s", secondsLeft));

                // Warning color when time is low
                if (secondsLeft <= 5) {
                    tvTimer.setTextColor(getResources().getColor(R.color.color_error, null));
                } else {
                    tvTimer.setTextColor(getResources().getColor(R.color.text_primary, null));
                }
            }

            @Override
            public void onFinish() {
                // Time's up - no distance gained, streak broken
                Toast.makeText(TimedTrailActivity.this, "Time's up! Keep going!", Toast.LENGTH_SHORT).show();
                currentStreak = 0;
                updateStreakDisplay();
                currentQuestionIndex++;
                loadQuestion();
                startQuestionTimer();
            }
        }.start();
    }

    private void checkAnswer(int selectedIndex) {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        TrailQuestion question = questions.get(currentQuestionIndex);
        boolean isCorrect = (selectedIndex == question.correctAnswerIndex);

        if (isCorrect) {
            handleCorrectAnswer(selectedIndex);
        } else {
            handleWrongAnswer(selectedIndex, question.correctAnswerIndex);
        }
    }

    private void handleCorrectAnswer(int selectedIndex) {
        correctAnswers++;
        currentStreak++;
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }

        // Calculate distance with streak bonus
        int distanceGained = DISTANCE_PER_CORRECT;
        if (currentStreak >= 3) {
            distanceGained += STREAK_BONUS;
        }

        distanceTraveled += distanceGained;

        // Visual feedback
        highlightCorrectButton(selectedIndex);
        moveAvatarForward(distanceGained);
        updateScoreDisplay();
        updateStreakDisplay();

        // Show distance gained
        String message = currentStreak >= 3
                ? String.format("Correct! +%dm (Streak Bonus!)", distanceGained)
                : String.format("Correct! +%dm", distanceGained);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Next question after delay
        cardQuestion.postDelayed(() -> {
            currentQuestionIndex++;
            loadQuestion();
            startQuestionTimer();
        }, 1500);
    }

    private void handleWrongAnswer(int selectedIndex, int correctIndex) {
        currentStreak = 0; // Break streak

        // Visual feedback
        highlightWrongButton(selectedIndex);
        highlightCorrectButton(correctIndex);
        updateStreakDisplay();

        Toast.makeText(this, "Wrong! Streak broken!", Toast.LENGTH_SHORT).show();

        // Next question after delay
        cardQuestion.postDelayed(() -> {
            currentQuestionIndex++;
            loadQuestion();
            startQuestionTimer();
        }, 2000);
    }

    private void moveAvatarForward(int distance) {
        // Update progress bar
        int newProgress = Math.min(distanceTraveled, 100);
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressTrack, "progress", newProgress);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();

        // Animate avatar jumping forward
        ivAvatar.animate()
                .translationYBy(-50f)
                .setDuration(250)
                .withEndAction(() ->
                        ivAvatar.animate().translationYBy(50f).setDuration(250).start()
                ).start();
    }

    private void updateScoreDisplay() {
        tvScore.setText(distanceTraveled + "m");
        tvProgress.setText(distanceTraveled + " / 100m");
    }

    private void updateStreakDisplay() {
        tvStreak.setText("Streak: " + currentStreak);
        if (currentStreak >= 3) {
            tvStreak.setTextColor(getResources().getColor(R.color.color_success, null));
        } else {
            tvStreak.setTextColor(getResources().getColor(R.color.text_secondary, null));
        }
    }

    private void highlightCorrectButton(int index) {
        MaterialButton btn = getButtonByIndex(index);
        btn.setBackgroundTintList(getResources().getColorStateList(R.color.color_success, null));
        btn.setIconResource(android.R.drawable.checkbox_on_background);
    }

    private void highlightWrongButton(int index) {
        MaterialButton btn = getButtonByIndex(index);
        btn.setBackgroundTintList(getResources().getColorStateList(R.color.color_error, null));
        btn.setIconResource(android.R.drawable.ic_delete);

        // Shake animation
        btn.animate().translationX(25f).setDuration(100)
                .withEndAction(() -> btn.animate().translationX(-25f).setDuration(100)
                        .withEndAction(() -> btn.animate().translationX(0f).setDuration(100).start())
                        .start())
                .start();
    }

    private void resetButtonStates() {
        btnOption1.setBackgroundTintList(getResources().getColorStateList(R.color.purple_600, null));
        btnOption2.setBackgroundTintList(getResources().getColorStateList(R.color.purple_600, null));
        btnOption3.setBackgroundTintList(getResources().getColorStateList(R.color.purple_600, null));
        btnOption4.setBackgroundTintList(getResources().getColorStateList(R.color.purple_600, null));

        btnOption1.setIcon(null);
        btnOption2.setIcon(null);
        btnOption3.setIcon(null);
        btnOption4.setIcon(null);

        btnOption1.setTranslationX(0f);
        btnOption2.setTranslationX(0f);
        btnOption3.setTranslationX(0f);
        btnOption4.setTranslationX(0f);
    }

    private MaterialButton getButtonByIndex(int index) {
        switch (index) {
            case 0: return btnOption1;
            case 1: return btnOption2;
            case 2: return btnOption3;
            case 3: return btnOption4;
            default: return btnOption1;
        }
    }

    private void endGame() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;
        int accuracy = (totalQuestions > 0) ? (correctAnswers * 100 / totalQuestions) : 0;

        // Calculate XP based on distance and accuracy
        int xpEarned = distanceTraveled + (accuracy / 2);

        new AlertDialog.Builder(this)
                .setTitle("Race Complete!")
                .setMessage(
                        "Distance Traveled: " + distanceTraveled + "m\n" +
                                "Correct Answers: " + correctAnswers + " / " + totalQuestions + "\n" +
                                "Accuracy: " + accuracy + "%\n" +
                                "Max Streak: " + maxStreak + "\n" +
                                "Time: " + timeTaken + "s\n\n" +
                                "XP Earned: +" + xpEarned
                )
                .setPositiveButton("Finish", (d, w) -> finish())
                .setNegativeButton("Play Again", (d, w) -> restartGame())
                .setCancelable(false)
                .show();
    }

    private void restartGame() {
        currentQuestionIndex = 0;
        correctAnswers = 0;
        currentStreak = 0;
        maxStreak = 0;
        distanceTraveled = 0;

        progressTrack.setProgress(0);
        tvScore.setText("0m");
        tvStreak.setText("Streak: 0");
        tvProgress.setText("0 / 100m");

        Collections.shuffle(questions);
        startGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) {
            questionTimer.cancel();
        }
    }

    /**
     * Data class for trail questions
     */
    private static class TrailQuestion {
        String question;
        String[] options;
        int correctAnswerIndex;

        TrailQuestion(String question, String[] options, int correctAnswerIndex) {
            this.question = question;
            this.options = options;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }
}