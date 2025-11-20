package com.example.literise.activities;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.example.literise.models.GameResult;
import com.example.literise.models.ScrambledSentence;
import com.example.literise.utils.CustomToast;
import com.example.literise.utils.GameDataManager;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class SentenceScrambleActivity extends AppCompatActivity {

    private TextView tvProgress, tvTimer, tvScore, tvStreak;
    private FlexboxLayout wordsContainer, answerContainer;
    private MaterialButton btnSubmit, btnClear;
    private ImageView ivBack;

    private List<ScrambledSentence> sentences;
    private int currentIndex = 0;
    private int score = 0;
    private int streak = 0;
    private int totalCorrect = 0;
    private int totalQuestions = 5;
    private long startTime;
    private CountDownTimer questionTimer;
    private static final int QUESTION_TIME_LIMIT = 60; // seconds

    private SessionManager session;
    private String lessonType = "reading"; // Default lesson type

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sentence_scramble);

        session = new SessionManager(this);

        // Get lesson type from intent
        lessonType = getIntent().getStringExtra("lesson_type");
        if (lessonType == null) {
            lessonType = "reading";
        }

        initializeViews();
        loadSentences();
        displayCurrentSentence();
        setupListeners();
    }

    private void initializeViews() {
        tvProgress = findViewById(R.id.tvProgress);
        tvTimer = findViewById(R.id.tvTimer);
        tvScore = findViewById(R.id.tvScore);
        tvStreak = findViewById(R.id.tvStreak);
        wordsContainer = findViewById(R.id.wordsContainer);
        answerContainer = findViewById(R.id.answerContainer);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnClear = findViewById(R.id.btnClear);
        ivBack = findViewById(R.id.ivBack);

        startTime = System.currentTimeMillis();
    }

    private void loadSentences() {
        // Load 5 sentences of mixed difficulty
        sentences = GameDataManager.getMixedScrambledSentences(totalQuestions);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());

        btnClear.setOnClickListener(v -> clearAnswer());

        btnSubmit.setOnClickListener(v -> checkAnswer());

        // Set up drag and drop for answer container
        answerContainer.setOnDragListener(new AnswerDropListener());
    }

    private void displayCurrentSentence() {
        if (currentIndex >= sentences.size()) {
            finishGame();
            return;
        }

        ScrambledSentence sentence = sentences.get(currentIndex);

        // Update progress
        tvProgress.setText(String.format("Question %d of %d", currentIndex + 1, totalQuestions));

        // Clear containers
        wordsContainer.removeAllViews();
        answerContainer.removeAllViews();

        // Add scrambled words as draggable chips
        for (String word : sentence.getScrambledWords()) {
            View wordChip = createWordChip(word, true);
            wordsContainer.addView(wordChip);
        }

        // Start question timer
        startQuestionTimer();
    }

    private View createWordChip(String word, boolean isDraggable) {
        View chipView = LayoutInflater.from(this).inflate(R.layout.item_word_chip, wordsContainer, false);
        TextView tvWord = chipView.findViewById(R.id.tvWord);
        CardView cardChip = chipView.findViewById(R.id.cardChip);

        tvWord.setText(word);
        chipView.setTag(word);

        if (isDraggable) {
            chipView.setOnTouchListener(new WordTouchListener());
        }

        // Allow chips in answer container to be removed
        chipView.setOnClickListener(v -> {
            if (answerContainer.indexOfChild(v) >= 0) {
                removeFromAnswer(v);
            }
        });

        return chipView;
    }

    private class WordTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("word", v.getTag().toString());
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                v.startDragAndDrop(data, shadowBuilder, v, 0);
                v.setVisibility(View.INVISIBLE);
                return true;
            }
            return false;
        }
    }

    private class AnswerDropListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    v.setBackgroundColor(getResources().getColor(R.color.color_old_lace, null));
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    v.setBackground(getDrawable(R.drawable.background_dashed_border));
                    return true;

                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    String word = event.getClipData().getItemAt(0).getText().toString();

                    // Add word to answer
                    View answerChip = createWordChip(word, false);
                    answerContainer.addView(answerChip);

                    // Remove from words container
                    wordsContainer.removeView(draggedView);

                    v.setBackground(getDrawable(R.drawable.background_dashed_border));
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    View view = (View) event.getLocalState();
                    view.setVisibility(View.VISIBLE);
                    v.setBackground(getDrawable(R.drawable.background_dashed_border));
                    return true;

                default:
                    break;
            }
            return false;
        }
    }

    private void removeFromAnswer(View chipView) {
        String word = chipView.getTag().toString();
        answerContainer.removeView(chipView);

        // Add back to words container
        View wordChip = createWordChip(word, true);
        wordsContainer.addView(wordChip);
    }

    private void clearAnswer() {
        // Move all answer chips back to words container
        while (answerContainer.getChildCount() > 0) {
            View chip = answerContainer.getChildAt(0);
            removeFromAnswer(chip);
        }
    }

    private void checkAnswer() {
        if (answerContainer.getChildCount() == 0) {
            CustomToast.showWarning(this, "Please arrange the words first");
            return;
        }

        // Stop timer
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        // Get user's answer
        List<String> userAnswer = new ArrayList<>();
        for (int i = 0; i < answerContainer.getChildCount(); i++) {
            View chip = answerContainer.getChildAt(i);
            userAnswer.add(chip.getTag().toString());
        }

        // Check if correct
        ScrambledSentence sentence = sentences.get(currentIndex);
        boolean isCorrect = sentence.checkAnswer(userAnswer);

        if (isCorrect) {
            // Calculate score with time bonus
            int timeLeft = Integer.parseInt(tvTimer.getText().toString());
            int basePoints = sentence.getXpReward();
            int timeBonus = timeLeft * 2; // 2 points per second remaining
            int streakBonus = streak * 10; // 10 points per streak
            int questionScore = basePoints + timeBonus + streakBonus;

            score += questionScore;
            streak++;
            totalCorrect++;

            // Animate answer container - green
            animateAnswerContainer(true);

            // Show feedback
            CustomToast.showSuccess(this, String.format("✓ Correct! +%d points", questionScore));

            tvScore.setText(String.valueOf(score));
            tvStreak.setText(String.valueOf(streak));

            // Move to next question after delay
            answerContainer.postDelayed(() -> {
                currentIndex++;
                displayCurrentSentence();
            }, 1500);

        } else {
            // Wrong answer
            streak = 0;
            tvStreak.setText("0");

            // Animate answer container - red
            animateAnswerContainer(false);

            CustomToast.showError(this, "✗ Incorrect! Try again");

            // Allow retry
        }
    }

    private void animateAnswerContainer(boolean isCorrect) {
        int color = isCorrect ?
                getResources().getColor(R.color.color_success_bg, null) :
                getResources().getColor(R.color.color_error_bg, null);

        answerContainer.setBackgroundColor(color);
        answerContainer.postDelayed(() -> {
            answerContainer.setBackground(getDrawable(R.drawable.background_dashed_border));
        }, 500);
    }

    private void startQuestionTimer() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        questionTimer = new CountDownTimer(QUESTION_TIME_LIMIT * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) (millisUntilFinished / 1000);
                tvTimer.setText(String.valueOf(secondsLeft));

                // Change color when time is running out
                if (secondsLeft <= 10) {
                    tvTimer.setTextColor(getResources().getColor(R.color.color_error_bg, null));
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("0");
                CustomToast.showWarning(SentenceScrambleActivity.this, "Time's up!");
                // Skip to next question
                currentIndex++;
                displayCurrentSentence();
            }
        }.start();
    }

    private void finishGame() {
        if (questionTimer != null) {
            questionTimer.cancel();
        }

        // Calculate total time spent
        long timeSpent = (System.currentTimeMillis() - startTime) / 1000;

        // Calculate accuracy
        float accuracy = (totalCorrect * 100.0f) / totalQuestions;

        // Create game result
        GameResult result = new GameResult(
                "sentence_scramble",
                score,
                score, // XP earned = score
                accuracy,
                (int) timeSpent
        );

        // Save XP to session
        int currentXP = session.getXP();
        session.saveXP(currentXP + score);

        // Show results and return to lesson activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("game_type", "sentence_scramble");
        resultIntent.putExtra("score", score);
        resultIntent.putExtra("xp_earned", score);
        resultIntent.putExtra("accuracy", accuracy);
        resultIntent.putExtra("time_spent", (int) timeSpent);
        setResult(RESULT_OK, resultIntent);

        CustomToast.showSuccess(this, String.format("Game Complete! +%d XP", score));

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (questionTimer != null) {
            questionTimer.cancel();
        }
    }
}
