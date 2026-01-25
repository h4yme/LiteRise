package com.example.literise.activities;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Phonics Ninja - Word Slicing Action Game
 * Slice falling words that match the target phonics pattern
 */
public class PhonicsNinjaActivity extends AppCompatActivity {

    // UI Elements
    private FrameLayout gameContainer;
    private TextView tvScore, tvLives, tvTimer, tvTargetPattern, tvCombo;
    private ProgressBar progressBar;
    private CardView cardTargetPattern, cardCombo;
    private MaterialButton btnPause;
    private ImageView[] heartIcons = new ImageView[3];

    // Game State
    private int score = 0;
    private int lives = 3;
    private int level = 1;
    private int combo = 0;
    private int maxCombo = 0;
    private String targetPattern = "CVCC"; // Default pattern
    private boolean isGameActive = false;
    private long gameTimeRemaining = 90000; // 90 seconds

    // Game Objects
    private List<FallingWord> activeWords = new ArrayList<>();
    private Random random = new Random();
    private CountDownTimer gameTimer;
    private CountDownTimer spawnTimer;

    // Constants
    private static final int BASE_SPAWN_INTERVAL = 2000; // 2 seconds
    private static final int MIN_SPAWN_INTERVAL = 800;
    private static final float BASE_FALL_SPEED = 3000f; // 3 seconds to fall
    private static final float MIN_FALL_SPEED = 1500f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonics_ninja);

        // Get extras
        targetPattern = getIntent().getStringExtra("target_pattern");
        if (targetPattern == null) targetPattern = "CVCC";

        initializeViews();
        setupListeners();
        startGame();
    }

    private void initializeViews() {
        gameContainer = findViewById(R.id.gameContainer);
        tvScore = findViewById(R.id.tvScore);
        tvLives = findViewById(R.id.tvLives);
        tvTimer = findViewById(R.id.tvTimer);
        tvTargetPattern = findViewById(R.id.tvTargetPattern);
        tvCombo = findViewById(R.id.tvCombo);
        progressBar = findViewById(R.id.progressBar);
        cardTargetPattern = findViewById(R.id.cardTargetPattern);
        cardCombo = findViewById(R.id.cardCombo);
        btnPause = findViewById(R.id.btnPause);
        heartIcons[0] = findViewById(R.id.heart1);
        heartIcons[1] = findViewById(R.id.heart2);
        heartIcons[2] = findViewById(R.id.heart3);

        // Set target pattern
        tvTargetPattern.setText(getPatternDisplayName(targetPattern));
        cardCombo.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnPause.setOnClickListener(v -> pauseGame());

        gameContainer.setOnTouchListener((v, event) -> {
            if (!isGameActive) return false;

            if (event.getAction() == MotionEvent.ACTION_MOVE ||
                event.getAction() == MotionEvent.ACTION_DOWN) {
                float x = event.getX();
                float y = event.getY();
                checkWordSlice(x, y);

                // Create slash effect
                createSlashEffect(x, y);
            }
            return true;
        });
    }

    private void startGame() {
        isGameActive = true;
        score = 0;
        lives = 3;
        combo = 0;
        level = 1;
        updateUI();

        // Start game timer
        gameTimer = new CountDownTimer(gameTimeRemaining, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                gameTimeRemaining = millisUntilFinished;
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                tvTimer.setText(secondsRemaining + "s");
                progressBar.setProgress((int) (millisUntilFinished / 900));
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();

        // Start spawning words
        startWordSpawning();
    }

    private void startWordSpawning() {
        int spawnInterval = Math.max(MIN_SPAWN_INTERVAL, BASE_SPAWN_INTERVAL - (level * 100));

        spawnTimer = new CountDownTimer(Long.MAX_VALUE, spawnInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isGameActive) {
                    spawnRandomWord();
                }
            }

            @Override
            public void onFinish() {}
        }.start();
    }

    private void spawnRandomWord() {
        // 60% correct words, 40% wrong words
        String[] patternData = getPatternData(targetPattern);
        String word;
        boolean isCorrect;

        if (random.nextInt(100) < 60) {
            // Spawn correct word
            word = patternData[1 + random.nextInt(patternData.length - 1)];
            isCorrect = true;
        } else {
            // Spawn wrong word
            List<String> wrongWords = getWrongWords(targetPattern);
            word = wrongWords.get(random.nextInt(wrongWords.size()));
            isCorrect = false;
        }

        FallingWord fallingWord = new FallingWord(word, isCorrect);
        activeWords.add(fallingWord);
        gameContainer.addView(fallingWord.textView);
        fallingWord.startFalling();
    }

    private void checkWordSlice(float touchX, float touchY) {
        List<FallingWord> wordsToRemove = new ArrayList<>();

        for (FallingWord word : activeWords) {
            if (word.isSliced) continue;

            // Check if touch is near word
            float wordX = word.textView.getX() + word.textView.getWidth() / 2f;
            float wordY = word.textView.getY() + word.textView.getHeight() / 2f;
            float distance = (float) Math.sqrt(Math.pow(touchX - wordX, 2) + Math.pow(touchY - wordY, 2));

            if (distance < 150) { // Touch radius
                word.isSliced = true;
                if (word.isCorrect) {
                    onCorrectSlice(word);
                } else {
                    onWrongSlice(word);
                }
                word.explode();
                wordsToRemove.add(word);
            }
        }

        activeWords.removeAll(wordsToRemove);
    }

    private void onCorrectSlice(FallingWord word) {
        // Increase combo
        combo++;
        maxCombo = Math.max(maxCombo, combo);

        // Calculate score with combo multiplier
        int points = 10 * Math.max(1, combo / 3);
        score += points;

        // Show combo card if combo >= 3
        if (combo >= 3) {
            cardCombo.setVisibility(View.VISIBLE);
            tvCombo.setText(combo + "x COMBO!");
            cardCombo.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100)
                .withEndAction(() -> cardCombo.animate().scaleX(1f).scaleY(1f).setDuration(100));
        }

        updateUI();
    }

    private void onWrongSlice(FallingWord word) {
        // Lose a life
        lives--;
        combo = 0; // Reset combo
        cardCombo.setVisibility(View.GONE);

        // Update hearts
        if (lives >= 0 && lives < 3) {
            heartIcons[lives].setVisibility(View.INVISIBLE);
        }

        // Shake screen effect
        gameContainer.animate()
            .translationX(-20f).setDuration(50)
            .withEndAction(() -> gameContainer.animate().translationX(20f).setDuration(50)
                .withEndAction(() -> gameContainer.animate().translationX(0f).setDuration(50)));

        updateUI();

        if (lives <= 0) {
            endGame();
        }
    }

    private void createSlashEffect(float x, float y) {
        View slashEffect = new View(this);
        slashEffect.setBackgroundResource(R.drawable.slash_effect);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(100, 100);
        slashEffect.setLayoutParams(params);
        slashEffect.setX(x - 50);
        slashEffect.setY(y - 50);
        slashEffect.setAlpha(0.8f);
        gameContainer.addView(slashEffect);

        slashEffect.animate()
            .alpha(0f)
            .scaleX(2f)
            .scaleY(2f)
            .setDuration(300)
            .withEndAction(() -> gameContainer.removeView(slashEffect));
    }

    private void updateUI() {
        tvScore.setText(String.valueOf(score));
        tvLives.setText(String.valueOf(lives));
    }

    private void pauseGame() {
        isGameActive = false;
        if (gameTimer != null) gameTimer.cancel();
        if (spawnTimer != null) spawnTimer.cancel();

        Toast.makeText(this, "Game Paused", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void endGame() {
        isGameActive = false;
        if (gameTimer != null) gameTimer.cancel();
        if (spawnTimer != null) spawnTimer.cancel();

        // Clear all words
        for (FallingWord word : activeWords) {
            gameContainer.removeView(word.textView);
        }
        activeWords.clear();

        // Show results
        String message = String.format("Game Over!\nScore: %d\nMax Combo: %dx", score, maxCombo);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        finish();
    }

    private String getPatternDisplayName(String pattern) {
        switch (pattern) {
            case "CVCC": return "CVCC Words (e.g., jump, bent)";
            case "CCVC": return "CCVC Words (e.g., trip, clap)";
            case "LONG_A": return "Long A Sound (e.g., cake, rain)";
            case "LONG_E": return "Long E Sound (e.g., tree, bean)";
            case "BLENDS": return "Consonant Blends (e.g., stop, from)";
            case "DIGRAPHS": return "Digraphs (e.g., shop, chip)";
            default: return pattern;
        }
    }

    private String[] getPatternData(String pattern) {
        switch (pattern) {
            case "CVCC":
                return new String[]{"CVCC", "jump", "bent", "melt", "lamp", "tent", "pump", "wind", "hand", "send"};
            case "CCVC":
                return new String[]{"CCVC", "trip", "clap", "flag", "stop", "from", "glad", "drip", "crop", "snap"};
            case "LONG_A":
                return new String[]{"Long A", "cake", "rain", "play", "day", "make", "tail", "train", "way", "lake"};
            case "LONG_E":
                return new String[]{"Long E", "tree", "bean", "see", "tea", "bee", "read", "meat", "team", "seed"};
            case "BLENDS":
                return new String[]{"Blends", "stop", "from", "glad", "trip", "clap", "flag", "snap", "drip", "crop"};
            case "DIGRAPHS":
                return new String[]{"Digraphs", "shop", "chip", "that", "when", "ship", "thin", "chat", "whip", "chop"};
            default:
                return new String[]{"Pattern", "word", "test", "game", "play"};
        }
    }

    private List<String> getWrongWords(String pattern) {
        List<String> wrongWords = new ArrayList<>();
        // Add words that DON'T match the pattern
        wrongWords.add("cat");
        wrongWords.add("dog");
        wrongWords.add("run");
        wrongWords.add("sit");
        wrongWords.add("hot");
        wrongWords.add("big");
        wrongWords.add("yes");
        wrongWords.add("not");
        wrongWords.add("can");
        wrongWords.add("top");
        return wrongWords;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) gameTimer.cancel();
        if (spawnTimer != null) spawnTimer.cancel();
    }

    /**
     * Falling Word Class
     */
    class FallingWord {
        TextView textView;
        String word;
        boolean isCorrect;
        boolean isSliced = false;
        ValueAnimator fallAnimator;

        FallingWord(String word, boolean isCorrect) {
            this.word = word;
            this.isCorrect = isCorrect;

            // Create TextView
            textView = new TextView(PhonicsNinjaActivity.this);
            textView.setText(word);
            textView.setTextSize(24);
            textView.setTextColor(0xFFFFFFFF);
            textView.setPadding(30, 20, 30, 20);
            textView.setBackgroundResource(isCorrect ? R.drawable.word_card_correct : R.drawable.word_card_wrong);

            // Random X position
            int screenWidth = gameContainer.getWidth();
            int wordWidth = 200;
            int randomX = random.nextInt(Math.max(1, screenWidth - wordWidth));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textView.setLayoutParams(params);
            textView.setX(randomX);
            textView.setY(-100); // Start above screen
        }

        void startFalling() {
            int screenHeight = gameContainer.getHeight();
            float fallSpeed = Math.max(MIN_FALL_SPEED, BASE_FALL_SPEED - (level * 100));

            fallAnimator = ValueAnimator.ofFloat(-100, screenHeight + 100);
            fallAnimator.setDuration((long) fallSpeed);
            fallAnimator.addUpdateListener(animation -> {
                if (textView.getParent() != null) {
                    float y = (float) animation.getAnimatedValue();
                    textView.setY(y);
                    textView.setRotation(y / 10f); // Slight rotation
                }
            });
            fallAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (!isSliced && isCorrect) {
                        // Missed a correct word
                        lives--;
                        combo = 0;
                        cardCombo.setVisibility(View.GONE);
                        if (lives >= 0 && lives < 3) {
                            heartIcons[lives].setVisibility(View.INVISIBLE);
                        }
                        updateUI();
                        if (lives <= 0) {
                            endGame();
                        }
                    }
                    gameContainer.removeView(textView);
                    activeWords.remove(FallingWord.this);
                }
            });
            fallAnimator.start();
        }

        void explode() {
            if (fallAnimator != null) fallAnimator.cancel();

            // Explosion animation
            textView.animate()
                .scaleX(2f)
                .scaleY(2f)
                .alpha(0f)
                .rotation(360f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (textView.getParent() != null) {
                        gameContainer.removeView(textView);
                    }
                });
        }
    }
}
