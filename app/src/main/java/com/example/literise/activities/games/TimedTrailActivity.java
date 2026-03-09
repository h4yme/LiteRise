package com.example.literise.activities.games;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.literise.utils.CustomToast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.GameContentRequest;
import com.example.literise.models.GameContentResponse;
import com.example.literise.models.LessonContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private android.view.View ivAvatar; // avatarContainer for movement
    private LinearLayout optionsContainer;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4;
    private CardView cardQuestion;
    private LottieAnimationView lottieCorrect, lottieComplete;

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
        applyModuleTheme();
        setupListeners();

        String lessonContent = getIntent().getStringExtra("lesson_content");
        int nodeId = getIntent().getIntExtra("node_id", -1);

        if (lessonContent != null && !lessonContent.isEmpty() && nodeId > 0) {
            generateWithAI(nodeId, lessonContent);
        } else if (nodeId > 0) {
            int placementLevel = getIntent().getIntExtra("placement_level", 2);
            ApiService fetchService = ApiClient.getClient(this).create(ApiService.class);
            fetchService.getLessonContent(nodeId, placementLevel)
                    .enqueue(new Callback<LessonContentResponse>() {
                        @Override
                        public void onResponse(Call<LessonContentResponse> call, Response<LessonContentResponse> response) {
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().getLesson() != null
                                    && response.body().getLesson().getContent() != null) {
                                generateWithAI(nodeId, response.body().getLesson().getContent());
                            } else {
                                setupQuestions();
                                startGame();
                            }
                        }
                        @Override
                        public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                            setupQuestions();
                            startGame();
                        }
                    });
        } else {
            setupQuestions();
            startGame();
        }
    }

    private void generateWithAI(int nodeId, String lessonContent) {
        ApiService apiService = ApiClient.getAiClient(this).create(ApiService.class);
        GameContentRequest request = new GameContentRequest(nodeId, "timed_trail", lessonContent);
        apiService.generateGameContent(request).enqueue(new Callback<GameContentResponse>() {
            @Override
            public void onResponse(Call<GameContentResponse> call, Response<GameContentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success
                        && response.body().content != null) {
                    try {
                        JsonArray questionsArray = response.body().content.getAsJsonArray("questions");
                        List<TrailQuestion> aiQuestions = new ArrayList<>();
                        for (int i = 0; i < questionsArray.size(); i++) {
                            JsonObject obj = questionsArray.get(i).getAsJsonObject();
                            String questionText = obj.get("question").getAsString();
                            String optA = obj.get("optionA").getAsString();
                            String optB = obj.get("optionB").getAsString();
                            String optC = obj.get("optionC").getAsString();
                            String optD = obj.get("optionD").getAsString();
                            String correctLetter = obj.get("correct").getAsString().trim().toUpperCase();
                            int correctIndex;
                            switch (correctLetter) {
                                case "B": correctIndex = 1; break;
                                case "C": correctIndex = 2; break;
                                case "D": correctIndex = 3; break;
                                default:  correctIndex = 0; break;
                            }
                            aiQuestions.add(new TrailQuestion(
                                    questionText,
                                    new String[]{optA, optB, optC, optD},
                                    correctIndex
                            ));
                        }
                        if (!aiQuestions.isEmpty()) {
                            questions = aiQuestions;
                            Collections.shuffle(questions);
                            totalQuestions = questions.size();
                            startGame();
                            return;
                        }
                    } catch (Exception e) {
                        android.util.Log.w("TimedTrail", "AI parse error: " + e.getMessage());
                    }
                } else {
                    android.util.Log.w("TimedTrail", "AI generate failed: code=" + response.code()
                            + " msg=" + (response.body() != null ? response.body().message : "null"));
                }
                setupQuestions();
                startGame();
            }

            @Override
            public void onFailure(Call<GameContentResponse> call, Throwable t) {
                android.util.Log.w("TimedTrail", "AI generate network error: " + t.getMessage());
                setupQuestions();
                startGame();
            }
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        tvTimer = findViewById(R.id.tv_timer);
        tvScore = findViewById(R.id.tv_score);
        tvStreak = findViewById(R.id.tv_streak);
        tvQuestion = findViewById(R.id.tv_question);
        tvProgress = findViewById(R.id.tv_progress);
        progressTrack = findViewById(R.id.progress_track);
        ivAvatar = findViewById(R.id.avatarContainer);
        optionsContainer = findViewById(R.id.options_container);
        btnOption1 = findViewById(R.id.btn_option_1);
        btnOption2 = findViewById(R.id.btn_option_2);
        btnOption3 = findViewById(R.id.btn_option_3);
        btnOption4 = findViewById(R.id.btn_option_4);
        cardQuestion = findViewById(R.id.card_question);
        lottieCorrect = findViewById(R.id.lottieCorrect);
        lottieComplete = findViewById(R.id.lottieComplete);

        // Initial setup
        progressTrack.setMax(100); // 100 meters to finish
        progressTrack.setProgress(0);
        tvProgress.setText("0 / 100m");
        tvScore.setText("0m");
        tvStreak.setText("Streak: 0");
    }

    private void applyModuleTheme() {
        try {
            android.content.Intent intent = getIntent();
            String colorStart = intent.getStringExtra("module_color_start");
            String colorEnd = intent.getStringExtra("module_color_end");
            if (colorStart == null || colorStart.isEmpty()) colorStart = "#3B82F6";
            if (colorEnd == null || colorEnd.isEmpty()) colorEnd = "#6366F1";
            android.view.View header = findViewById(R.id.header);
            if (header != null) {
                android.graphics.drawable.GradientDrawable grad =
                    new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        new int[]{android.graphics.Color.parseColor(colorStart),
                                  android.graphics.Color.parseColor(colorEnd)});
                grad.setCornerRadius(0f);
                header.setBackground(grad);
            }
        } catch (Exception ignored) {}
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
                CustomToast.showWarning(TimedTrailActivity.this, "Time's up! Keep going!");
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
        playLottieOnce(lottieCorrect);

        // Show distance gained
        String message = currentStreak >= 3
                ? String.format("Correct! +%dm (Streak Bonus!)", distanceGained)
                : String.format("Correct! +%dm", distanceGained);
        CustomToast.showSuccess(this, message);

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

        CustomToast.showError(this, "Wrong! Streak broken!");

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
        progressAnimator.setDuration(800);
        progressAnimator.setInterpolator(new DecelerateInterpolator());
        progressAnimator.start();

        // Move avatar horizontally along the track to match progress
        progressTrack.post(() -> {
            int trackWidth = progressTrack.getWidth();
            int avatarWidth = ivAvatar.getWidth();
            float targetX = (newProgress / 100f) * (trackWidth - avatarWidth);
            ivAvatar.animate()
                    .x(targetX)
                    .translationYBy(-30f)
                    .setDuration(400)
                    .withEndAction(() ->
                            ivAvatar.animate().translationYBy(30f).setDuration(300).start()
                    ).start();
        });
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
        int xpEarned = distanceTraveled + (accuracy / 2);

        // Mark game phase complete in StudentNodeProgress
        markGamePhaseComplete(getIntent().getIntExtra("node_id", -1));

        // Play celebration then show dialog
        if (lottieComplete != null) {
            lottieComplete.setVisibility(android.view.View.VISIBLE);
            lottieComplete.playAnimation();
            lottieComplete.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    lottieComplete.setVisibility(android.view.View.GONE);
                    showEndDialog(accuracy, xpEarned);
                }
            });
        } else {
            showEndDialog(accuracy, xpEarned);
        }
    }

    private void showEndDialog(int accuracy, int xpEarned) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_result, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvResultTitle    = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvResultSubtitle = dialogView.findViewById(R.id.tvResultSubtitle);
        TextView tvResultScore    = dialogView.findViewById(R.id.tvResultScore);
        TextView tvResultAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);
        TextView tvResultStreak   = dialogView.findViewById(R.id.tvResultStreak);
        TextView tvResultTime     = dialogView.findViewById(R.id.tvResultTime);
        TextView tvResultXP       = dialogView.findViewById(R.id.tvResultXP);
        TextView tvLabelScore     = dialogView.findViewById(R.id.tvLabelScore);
        TextView tvLabelAccuracy  = dialogView.findViewById(R.id.tvLabelAccuracy);
        TextView tvLabelStreak    = dialogView.findViewById(R.id.tvLabelStreak);
        TextView tvLabelTime      = dialogView.findViewById(R.id.tvLabelTime);
        com.google.android.material.button.MaterialButton btnFinish    = dialogView.findViewById(R.id.btnFinish);
        com.google.android.material.button.MaterialButton btnPlayAgain = dialogView.findViewById(R.id.btnPlayAgain);

        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;

        if (distanceTraveled >= 100) {
            tvResultTitle.setText("Race Complete! 🏁");
            tvResultSubtitle.setText("You crossed the finish line!");
        } else if (accuracy >= 70) {
            tvResultTitle.setText("Great Race! 🏃");
            tvResultSubtitle.setText("Solid performance on the track!");
        } else {
            tvResultTitle.setText("Race Finished! 🏁");
            tvResultSubtitle.setText("Keep practicing to go further!");
        }

        tvLabelScore.setText("Distance");
        tvLabelAccuracy.setText("Accuracy");
        tvLabelStreak.setText("Best Streak");
        tvLabelTime.setText("Time");

        tvResultScore.setText(distanceTraveled + "m");
        tvResultAccuracy.setText(accuracy + "%");
        tvResultStreak.setText(String.valueOf(maxStreak));
        tvResultTime.setText(String.format(Locale.getDefault(), "%d:%02d", timeTaken / 60, timeTaken % 60));
        tvResultXP.setText("+" + xpEarned + " XP");

        btnPlayAgain.setVisibility(android.view.View.VISIBLE);

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            android.content.Intent result = new android.content.Intent();
            result.putExtra("xp_earned", xpEarned);
            result.putExtra("accuracy", accuracy);
            setResult(RESULT_OK, result);
            finish();
        });

        btnPlayAgain.setOnClickListener(v -> {
            dialog.dismiss();
            restartGame();
        });

        dialog.show();
    }

    /** Plays a Lottie animation once, hiding it when done. */
    private void playLottieOnce(LottieAnimationView view) {
        if (view == null) return;
        view.cancelAnimation();
        view.setProgress(0f);
        view.setVisibility(android.view.View.VISIBLE);
        view.playAnimation();
        view.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(android.view.View.GONE);
                view.removeAllAnimatorListeners();
            }
        });
    }

    private void restartGame() {
        currentQuestionIndex = 0;
        correctAnswers = 0;
        currentStreak = 0;
        maxStreak = 0;
        distanceTraveled = 0;

        progressTrack.setProgress(0);
        ivAvatar.setX(0f);
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