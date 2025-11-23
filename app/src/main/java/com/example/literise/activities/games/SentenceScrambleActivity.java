package com.example.literise.activities.games;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
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
import com.example.literise.models.ScrambleSentence;
import com.example.literise.models.ScrambleSentenceResponse;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Sentence Scramble Game Activity
 * Players drag words into correct order to form sentences
 */
public class SentenceScrambleActivity extends AppCompatActivity {

    // Views
    private TextView tvTitle, tvProgress, tvTimer, tvStreakCount, tvScore;
    private TextView tvInstruction;
    private LinearLayout wordBankContainer, answerContainer;
    private ProgressBar progressBar, timerProgress;
    private MaterialButton btnCheck, btnSkip, btnHint;
    private CardView cardStreak;
    private View streakIndicator;

    // Game State
    private List<ScrambleSentence> sentences = new ArrayList<>();
    private int currentSentenceIndex = 0;
    private int score = 0;
    private int streak = 0;
    private int maxStreak = 0;
    private int hintsUsed = 0;
    private int correctAnswers = 0;
    private int totalAttempts = 0;
    private long startTime;

    // Timer
    private CountDownTimer gameTimer;
    private static final long TOTAL_TIME = 180000; // 3 minutes
    private long timeRemaining = TOTAL_TIME;
    private boolean isTimerRunning = false;

    // Answer tracking
    private List<String> currentAnswer = new ArrayList<>();
    private List<View> placedWordViews = new ArrayList<>();

    // Animation
    private Handler handler = new Handler(Looper.getMainLooper());

    // Session
    private SessionManager session;

    // Constants
    private static final int POINTS_PER_CORRECT = 100;
    private static final int STREAK_BONUS = 50;
    private static final int TIME_BONUS_MULTIPLIER = 2;
    private static final int HINT_PENALTY = 25;
    private static final int TOTAL_SENTENCES = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_scramble);

        session = new SessionManager(this);
        startTime = System.currentTimeMillis();

        initializeViews();
        setupListeners();
        loadSentences();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvProgress = findViewById(R.id.tvProgress);
        tvTimer = findViewById(R.id.tvTimer);
        tvStreakCount = findViewById(R.id.tvStreakCount);
        tvScore = findViewById(R.id.tvScore);
        tvInstruction = findViewById(R.id.tvInstruction);

        wordBankContainer = findViewById(R.id.wordBankContainer);
        answerContainer = findViewById(R.id.answerContainer);

        progressBar = findViewById(R.id.progressBar);
        timerProgress = findViewById(R.id.timerProgress);

        btnCheck = findViewById(R.id.btnCheck);
        btnSkip = findViewById(R.id.btnSkip);
        btnHint = findViewById(R.id.btnHint);

        cardStreak = findViewById(R.id.cardStreak);
        streakIndicator = findViewById(R.id.streakIndicator);

        // Setup drop zone
        setupDropZone();
    }

    private void setupListeners() {
        btnCheck.setOnClickListener(v -> checkAnswer());
        btnSkip.setOnClickListener(v -> skipSentence());
        btnHint.setOnClickListener(v -> useHint());

        findViewById(R.id.ivClose).setOnClickListener(v -> showExitConfirmation());
    }

    private void setupDropZone() {
        answerContainer.setOnDragListener((v, event) -> {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    answerContainer.setBackgroundResource(R.drawable.drop_zone_active);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    answerContainer.setBackgroundResource(R.drawable.drop_zone_background);
                    return true;

                case DragEvent.ACTION_DROP:
                    answerContainer.setBackgroundResource(R.drawable.drop_zone_background);
                    View draggedView = (View) event.getLocalState();
                    if (draggedView != null) {
                        String word = (String) draggedView.getTag();
                        addWordToAnswer(word, draggedView);
                    }
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    answerContainer.setBackgroundResource(R.drawable.drop_zone_background);
                    return true;

                default:
                    return false;
            }
        });
    }

    private void loadSentences() {
        progressBar.setVisibility(View.VISIBLE);

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getScrambleSentences(TOTAL_SENTENCES).enqueue(new Callback<ScrambleSentenceResponse>() {
            @Override
            public void onResponse(Call<ScrambleSentenceResponse> call, Response<ScrambleSentenceResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    sentences = response.body().getSentences();
                    if (sentences != null && !sentences.isEmpty()) {
                        startGame();
                    } else {
                        // Use fallback sentences if API returns empty
                        loadFallbackSentences();
                        startGame();
                    }
                } else {
                    loadFallbackSentences();
                    startGame();
                }
            }

            @Override
            public void onFailure(Call<ScrambleSentenceResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                loadFallbackSentences();
                startGame();
            }
        });
    }

    private void loadFallbackSentences() {
        sentences = new ArrayList<>();

        // Grade 4-6 appropriate sentences
        sentences.add(new ScrambleSentence(1, "The quick brown fox jumps over the lazy dog", 1.0f));
        sentences.add(new ScrambleSentence(2, "Maria finished her homework diligently", 0.8f));
        sentences.add(new ScrambleSentence(3, "The students are reading their books quietly", 1.2f));
        sentences.add(new ScrambleSentence(4, "She goes to school every morning", 0.6f));
        sentences.add(new ScrambleSentence(5, "The beautiful butterfly landed on the flower", 1.0f));
        sentences.add(new ScrambleSentence(6, "My family and I visited the museum yesterday", 1.4f));
        sentences.add(new ScrambleSentence(7, "The teacher explained the lesson clearly", 0.9f));
        sentences.add(new ScrambleSentence(8, "Reading books helps improve vocabulary skills", 1.3f));
        sentences.add(new ScrambleSentence(9, "The children played happily in the park", 0.7f));
        sentences.add(new ScrambleSentence(10, "Learning new words makes reading more enjoyable", 1.5f));
    }

    private void startGame() {
        startTimer();
        displayCurrentSentence();
        updateProgress();
    }

    private void startTimer() {
        timerProgress.setMax((int) (TOTAL_TIME / 1000));
        timerProgress.setProgress((int) (TOTAL_TIME / 1000));

        gameTimer = new CountDownTimer(TOTAL_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeRemaining = millisUntilFinished;
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "%d:%02d", minutes, seconds));
                timerProgress.setProgress(seconds + (minutes * 60));

                // Warning color when low time
                if (millisUntilFinished < 30000) {
                    tvTimer.setTextColor(ContextCompat.getColor(SentenceScrambleActivity.this, R.color.color_error));
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0:00");
                endGame();
            }
        }.start();
        isTimerRunning = true;
    }

    private void displayCurrentSentence() {
        if (currentSentenceIndex >= sentences.size()) {
            endGame();
            return;
        }

        // Clear previous state
        wordBankContainer.removeAllViews();
        answerContainer.removeAllViews();
        currentAnswer.clear();
        placedWordViews.clear();
        btnCheck.setEnabled(false);

        ScrambleSentence sentence = sentences.get(currentSentenceIndex);
        List<String> words = sentence.getScrambledWords();

        // Create word chips for word bank
        for (String word : words) {
            View wordView = createWordChip(word);
            wordBankContainer.addView(wordView);
        }

        // Update instruction based on difficulty
        float difficulty = sentence.getDifficulty();
        if (difficulty < 0.8f) {
            tvInstruction.setText("Arrange these words to form a sentence:");
        } else if (difficulty < 1.2f) {
            tvInstruction.setText("Put the words in the correct order:");
        } else {
            tvInstruction.setText("Challenge! Arrange this complex sentence:");
        }

        // Animate entry
        animateWordBankEntry();
    }

    private View createWordChip(String word) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View wordView = inflater.inflate(R.layout.item_draggable_word, wordBankContainer, false);

        TextView tvWord = wordView.findViewById(R.id.tvWord);
        CardView cardWord = wordView.findViewById(R.id.cardWord);

        tvWord.setText(word);
        wordView.setTag(word);

        // Setup drag
        wordView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("word", word);
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(data, shadowBuilder, v, 0);
                return true;
            }
            return false;
        });

        // Also allow click to add
        wordView.setOnClickListener(v -> {
            addWordToAnswer(word, v);
        });

        return wordView;
    }

    private void addWordToAnswer(String word, View sourceView) {
        // Check if word is already in answer
        if (currentAnswer.contains(word)) {
            return;
        }

        currentAnswer.add(word);

        // Create placed word view
        View placedView = createPlacedWordView(word);
        answerContainer.addView(placedView);
        placedWordViews.add(placedView);

        // Hide source view
        sourceView.setVisibility(View.INVISIBLE);
        sourceView.setTag(R.id.tag_placed, true);

        // Animate
        placedView.setScaleX(0f);
        placedView.setScaleY(0f);
        placedView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Enable check button if we have words
        btnCheck.setEnabled(currentAnswer.size() > 0);

        // Play sound
        playSound(SOUND_POP);
    }

    private View createPlacedWordView(String word) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View wordView = inflater.inflate(R.layout.item_draggable_word, answerContainer, false);

        TextView tvWord = wordView.findViewById(R.id.tvWord);
        CardView cardWord = wordView.findViewById(R.id.cardWord);

        tvWord.setText(word);
        wordView.setTag(word);

        // Different background for placed words
        cardWord.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_jade1));
        tvWord.setTextColor(Color.WHITE);

        // Click to remove
        wordView.setOnClickListener(v -> removeWordFromAnswer(word, wordView));

        return wordView;
    }

    private void removeWordFromAnswer(String word, View placedView) {
        currentAnswer.remove(word);
        placedWordViews.remove(placedView);

        // Animate removal
        placedView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(150)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        answerContainer.removeView(placedView);
                    }
                })
                .start();

        // Show word in bank again
        for (int i = 0; i < wordBankContainer.getChildCount(); i++) {
            View child = wordBankContainer.getChildAt(i);
            if (word.equals(child.getTag())) {
                child.setVisibility(View.VISIBLE);
                child.setTag(R.id.tag_placed, false);
                break;
            }
        }

        btnCheck.setEnabled(currentAnswer.size() > 0);
        playSound(SOUND_POP);
    }

    private void checkAnswer() {
        if (currentSentenceIndex >= sentences.size()) return;

        totalAttempts++;
        ScrambleSentence sentence = sentences.get(currentSentenceIndex);
        String userAnswer = String.join(" ", currentAnswer);
        String correctAnswer = sentence.getCorrectSentence();

        boolean isCorrect = userAnswer.equalsIgnoreCase(correctAnswer);

        if (isCorrect) {
            handleCorrectAnswer();
        } else {
            handleWrongAnswer();
        }
    }

    private void handleCorrectAnswer() {
        correctAnswers++;
        streak++;
        if (streak > maxStreak) maxStreak = streak;

        // Calculate score
        int points = POINTS_PER_CORRECT;
        if (streak > 1) {
            points += STREAK_BONUS * (streak - 1);
        }
        // Time bonus - faster = more points
        long timeTaken = TOTAL_TIME - timeRemaining;
        if (timeTaken < 15000) { // Under 15 seconds
            points += POINTS_PER_CORRECT * TIME_BONUS_MULTIPLIER;
        }

        score += points;

        // Update UI
        updateScore();
        updateStreak();

        // Animate correct feedback
        animateCorrectFeedback();

        // Show feedback
        CustomToast.showSuccess(this, "Correct! +" + points + " XP");

        // Move to next after delay
        handler.postDelayed(() -> {
            currentSentenceIndex++;
            updateProgress();
            displayCurrentSentence();
        }, 1500);
    }

    private void handleWrongAnswer() {
        streak = 0;
        updateStreak();

        // Animate wrong feedback
        animateWrongFeedback();

        // Shake animation on answer container
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        answerContainer.startAnimation(shake);

        CustomToast.showError(this, "Not quite! Try again");
    }

    private void animateCorrectFeedback() {
        // Flash green on all placed words
        for (View wordView : placedWordViews) {
            CardView card = wordView.findViewById(R.id.cardWord);
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_success));

            // Pulse animation
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(wordView, "scaleX", 1f, 1.1f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(wordView, "scaleY", 1f, 1.1f, 1f);
            scaleX.setDuration(300);
            scaleY.setDuration(300);
            scaleX.start();
            scaleY.start();
        }

        playSound(SOUND_CORRECT);
    }

    private void animateWrongFeedback() {
        // Flash red on placed words
        for (View wordView : placedWordViews) {
            CardView card = wordView.findViewById(R.id.cardWord);
            int originalColor = ContextCompat.getColor(this, R.color.color_jade1);

            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_error));

            // Revert after delay
            handler.postDelayed(() -> {
                card.setCardBackgroundColor(originalColor);
            }, 500);
        }

        playSound(SOUND_WRONG);
    }

    private void skipSentence() {
        streak = 0;
        updateStreak();

        currentSentenceIndex++;
        updateProgress();

        if (currentSentenceIndex >= sentences.size()) {
            endGame();
        } else {
            displayCurrentSentence();
        }
    }

    private void useHint() {
        if (currentSentenceIndex >= sentences.size()) return;

        ScrambleSentence sentence = sentences.get(currentSentenceIndex);
        List<String> correctWords = sentence.getWords();

        // Find first word that should be placed next
        int nextPosition = currentAnswer.size();
        if (nextPosition < correctWords.size()) {
            String nextWord = correctWords.get(nextPosition);

            // Find and highlight the word in the bank
            for (int i = 0; i < wordBankContainer.getChildCount(); i++) {
                View child = wordBankContainer.getChildAt(i);
                if (nextWord.equalsIgnoreCase((String) child.getTag()) &&
                    child.getVisibility() == View.VISIBLE) {

                    // Highlight with animation
                    CardView card = child.findViewById(R.id.cardWord);
                    int originalColor = ((CardView) child.findViewById(R.id.cardWord))
                            .getCardBackgroundColor().getDefaultColor();

                    // Pulse animation
                    ObjectAnimator colorAnim = ObjectAnimator.ofArgb(
                            card, "cardBackgroundColor",
                            originalColor,
                            ContextCompat.getColor(this, R.color.color_sunglow),
                            originalColor
                    );
                    colorAnim.setDuration(1000);
                    colorAnim.setRepeatCount(2);
                    colorAnim.start();

                    hintsUsed++;
                    score = Math.max(0, score - HINT_PENALTY);
                    updateScore();

                    CustomToast.showInfo(this, "Hint: Look for \"" + nextWord + "\"");
                    break;
                }
            }
        }
    }

    private void updateProgress() {
        int progress = (int) (((float) currentSentenceIndex / sentences.size()) * 100);
        progressBar.setProgress(progress);
        tvProgress.setText(String.format(Locale.getDefault(), "%d/%d",
                currentSentenceIndex + 1, sentences.size()));
    }

    private void updateScore() {
        tvScore.setText(String.valueOf(score));
    }

    private void updateStreak() {
        tvStreakCount.setText(String.valueOf(streak));

        if (streak >= 3) {
            cardStreak.setVisibility(View.VISIBLE);
            cardStreak.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_sunglow));

            // Fire animation
            streakIndicator.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction(() -> streakIndicator.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start())
                    .start();
        } else if (streak > 0) {
            cardStreak.setVisibility(View.VISIBLE);
            cardStreak.setCardBackgroundColor(ContextCompat.getColor(this, R.color.color_jade1));
        } else {
            cardStreak.setVisibility(View.GONE);
        }
    }

    private void animateWordBankEntry() {
        for (int i = 0; i < wordBankContainer.getChildCount(); i++) {
            View child = wordBankContainer.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(50f);

            child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(300)
                    .setStartDelay(i * 50L)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void endGame() {
        if (gameTimer != null) {
            gameTimer.cancel();
        }

        long totalTime = System.currentTimeMillis() - startTime;
        float accuracy = totalAttempts > 0 ? ((float) correctAnswers / totalAttempts) * 100 : 0;

        showResultDialog(accuracy, totalTime);
    }

    private void showResultDialog(float accuracy, long totalTime) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_result, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        // Setup dialog views
        TextView tvResultTitle = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvResultScore = dialogView.findViewById(R.id.tvResultScore);
        TextView tvResultAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);
        TextView tvResultStreak = dialogView.findViewById(R.id.tvResultStreak);
        TextView tvResultTime = dialogView.findViewById(R.id.tvResultTime);
        TextView tvResultXP = dialogView.findViewById(R.id.tvResultXP);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        // Set values
        String title;
        if (accuracy >= 90) {
            title = "Syntax Master!";
        } else if (accuracy >= 70) {
            title = "Great Job!";
        } else if (accuracy >= 50) {
            title = "Good Effort!";
        } else {
            title = "Keep Practicing!";
        }
        tvResultTitle.setText(title);

        tvResultScore.setText(String.valueOf(score));
        tvResultAccuracy.setText(String.format(Locale.getDefault(), "%.0f%%", accuracy));
        tvResultStreak.setText(String.valueOf(maxStreak));

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
        resultIntent.putExtra("total_count", sentences.size());
        resultIntent.putExtra("max_streak", maxStreak);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Game?")
                .setMessage("Your progress will be lost. Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    if (gameTimer != null) gameTimer.cancel();
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton("Continue", null)
                .show();
    }

    private static final int SOUND_POP = 1;
    private static final int SOUND_CORRECT = 2;
    private static final int SOUND_WRONG = 3;

    private void playSound(int soundType) {
        try {
            ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            switch (soundType) {
                case SOUND_POP:
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
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }
}
