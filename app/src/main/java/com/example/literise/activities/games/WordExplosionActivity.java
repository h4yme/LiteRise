package com.example.literise.activities.games;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Word Explosion - Bubble Popping Vocabulary Game
 * Pop bubbles by word categories to score points
 */
public class WordExplosionActivity extends AppCompatActivity {

    // UI Elements
    private FrameLayout gameContainer;
    private TextView tvScore, tvTimer, tvTargetCategory, tvCombo, tvCategoryLabel;
    private ProgressBar progressBar;
    private CardView cardTargetCategory, cardCombo;
    private MaterialButton btnPause;
    private ImageView[] heartIcons = new ImageView[3];

    // Game State
    private int score = 0;
    private int lives = 3;
    private int combo = 0;
    private int maxCombo = 0;
    private String targetCategory = "ANIMALS";
    private boolean isGameActive = false;
    private long gameTimeRemaining = 90000; // 90 seconds

    // Game Objects
    private List<WordBubble> activeBubbles = new ArrayList<>();
    private Random random = new Random();
    private CountDownTimer gameTimer;
    private CountDownTimer spawnTimer;

    // Constants
    private static final int BASE_SPAWN_INTERVAL = 1500; // 1.5 seconds
    private static final int MIN_SPAWN_INTERVAL = 800;
    private static final float BASE_RISE_SPEED = 4000f; // 4 seconds to rise
    private static final float MIN_RISE_SPEED = 2000f;

    // Word Categories
    private Map<String, CategoryData> categoryDatabase;
    private List<String> availableCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_explosion);

        initializeCategoryDatabase();
        initializeViews();
        setupListeners();
        startGame();
    }

    private void initializeCategoryDatabase() {
        categoryDatabase = new HashMap<>();
        availableCategories = new ArrayList<>();

        // Animals
        categoryDatabase.put("ANIMALS", new CategoryData(
                Arrays.asList("dog", "cat", "bird", "fish", "lion", "tiger", "elephant", "monkey"),
                "#FF6B6B"
        ));
        availableCategories.add("ANIMALS");

        // Colors
        categoryDatabase.put("COLORS", new CategoryData(
                Arrays.asList("red", "blue", "green", "yellow", "purple", "orange", "pink", "brown"),
                "#4ECDC4"
        ));
        availableCategories.add("COLORS");

        // Foods
        categoryDatabase.put("FOODS", new CategoryData(
                Arrays.asList("apple", "banana", "pizza", "rice", "bread", "milk", "cake", "egg"),
                "#FFD93D"
        ));
        availableCategories.add("FOODS");

        // Emotions
        categoryDatabase.put("EMOTIONS", new CategoryData(
                Arrays.asList("happy", "sad", "angry", "excited", "scared", "proud", "calm", "worried"),
                "#A770EF"
        ));
        availableCategories.add("EMOTIONS");

        // Actions
        categoryDatabase.put("ACTIONS", new CategoryData(
                Arrays.asList("run", "jump", "swim", "fly", "walk", "dance", "sing", "read"),
                "#667EEA"
        ));
        availableCategories.add("ACTIONS");

        // Nature
        categoryDatabase.put("NATURE", new CategoryData(
                Arrays.asList("tree", "flower", "sun", "moon", "star", "cloud", "rain", "wind"),
                "#44A08D"
        ));
        availableCategories.add("NATURE");
    }

    private void initializeViews() {
        gameContainer = findViewById(R.id.gameContainer);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvTargetCategory = findViewById(R.id.tvTargetCategory);
        tvCombo = findViewById(R.id.tvCombo);
        tvCategoryLabel = findViewById(R.id.tvCategoryLabel);
        progressBar = findViewById(R.id.progressBar);
        cardTargetCategory = findViewById(R.id.cardTargetCategory);
        cardCombo = findViewById(R.id.cardCombo);
        btnPause = findViewById(R.id.btnPause);
        heartIcons[0] = findViewById(R.id.heart1);
        heartIcons[1] = findViewById(R.id.heart2);
        heartIcons[2] = findViewById(R.id.heart3);

        // Set initial target category
        setNewTargetCategory();
        cardCombo.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnPause.setOnClickListener(v -> pauseGame());
    }

    private void startGame() {
        isGameActive = true;
        score = 0;
        lives = 3;
        combo = 0;
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

        // Start spawning bubbles
        startBubbleSpawning();
    }

    private void startBubbleSpawning() {
        int spawnInterval = BASE_SPAWN_INTERVAL;

        spawnTimer = new CountDownTimer(Long.MAX_VALUE, spawnInterval) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isGameActive) {
                    spawnRandomBubble();
                }
            }

            @Override
            public void onFinish() {}
        }.start();
    }

    private void spawnRandomBubble() {
        // 60% target category, 40% other categories
        String category;
        if (random.nextInt(100) < 60) {
            category = targetCategory;
        } else {
            // Pick random other category
            List<String> otherCategories = new ArrayList<>(availableCategories);
            otherCategories.remove(targetCategory);
            category = otherCategories.get(random.nextInt(otherCategories.size()));
        }

        CategoryData data = categoryDatabase.get(category);
        String word = data.words.get(random.nextInt(data.words.size()));

        WordBubble bubble = new WordBubble(word, category, data.color);
        activeBubbles.add(bubble);
        gameContainer.addView(bubble.cardView);
        bubble.startRising();
    }

    private void onBubbleTapped(WordBubble bubble) {
        if (bubble.isPopped) return;

        bubble.isPopped = true;
        boolean isCorrectCategory = bubble.category.equals(targetCategory);

        if (isCorrectCategory) {
            onCorrectPop(bubble);
        } else {
            onWrongPop(bubble);
        }

        bubble.pop();
    }

    private void onCorrectPop(WordBubble bubble) {
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

        // Change category every 5 correct pops
        if (combo > 0 && combo % 5 == 0) {
            setNewTargetCategory();
        }

        updateUI();
    }

    private void onWrongPop(WordBubble bubble) {
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

    private void setNewTargetCategory() {
        // Pick a random category different from current
        String newCategory = availableCategories.get(random.nextInt(availableCategories.size()));
        while (newCategory.equals(targetCategory) && availableCategories.size() > 1) {
            newCategory = availableCategories.get(random.nextInt(availableCategories.size()));
        }
        targetCategory = newCategory;
        tvTargetCategory.setText(targetCategory);

        // Update category label color
        CategoryData data = categoryDatabase.get(targetCategory);
        cardTargetCategory.setCardBackgroundColor(android.graphics.Color.parseColor(data.color));
    }

    private void updateUI() {
        tvScore.setText(String.valueOf(score));
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

        // Clear all bubbles
        for (WordBubble bubble : activeBubbles) {
            gameContainer.removeView(bubble.cardView);
        }
        activeBubbles.clear();

        // Show results
        String message = String.format("Game Over!\nScore: %d\nMax Combo: %dx", score, maxCombo);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) gameTimer.cancel();
        if (spawnTimer != null) spawnTimer.cancel();
    }

    /**
     * Word Bubble Class
     */
    class WordBubble {
        CardView cardView;
        TextView textView;
        String word;
        String category;
        String color;
        boolean isPopped = false;
        ValueAnimator riseAnimator;

        WordBubble(String word, String category, String color) {
            this.word = word;
            this.category = category;
            this.color = color;

            // Create CardView bubble
            cardView = new CardView(WordExplosionActivity.this);
            cardView.setCardElevation(8);
            cardView.setRadius(50);
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor(color));

            // Create TextView
            textView = new TextView(WordExplosionActivity.this);
            textView.setText(word);
            textView.setTextSize(16);
            textView.setTextColor(0xFFFFFFFF);
            textView.setPadding(30, 20, 30, 20);
            textView.setGravity(android.view.Gravity.CENTER);

            cardView.addView(textView);

            // Random X position
            int screenWidth = gameContainer.getWidth();
            int bubbleWidth = 150;
            int randomX = random.nextInt(Math.max(1, screenWidth - bubbleWidth));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    bubbleWidth,
                    bubbleWidth
            );
            cardView.setLayoutParams(params);
            cardView.setX(randomX);
            cardView.setY(gameContainer.getHeight() + 100); // Start below screen

            // Click listener
            cardView.setOnClickListener(v -> onBubbleTapped(WordBubble.this));
        }

        void startRising() {
            int screenHeight = gameContainer.getHeight();
            float riseSpeed = BASE_RISE_SPEED;

            riseAnimator = ValueAnimator.ofFloat(screenHeight + 100, -200);
            riseAnimator.setDuration((long) riseSpeed);
            riseAnimator.addUpdateListener(animation -> {
                if (cardView.getParent() != null) {
                    float y = (float) animation.getAnimatedValue();
                    cardView.setY(y);

                    // Slight wobble effect
                    float wobble = (float) Math.sin(y / 50) * 10;
                    cardView.setRotation(wobble);
                }
            });
            riseAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (!isPopped) {
                        // Bubble escaped - no penalty for now
                    }
                    gameContainer.removeView(cardView);
                    activeBubbles.remove(WordBubble.this);
                }
            });
            riseAnimator.start();
        }

        void pop() {
            if (riseAnimator != null) riseAnimator.cancel();

            // Create particle effect
            createParticles();

            // Pop animation
            cardView.animate()
                    .scaleX(2f)
                    .scaleY(2f)
                    .alpha(0f)
                    .setDuration(300)
                    .withEndAction(() -> {
                        if (cardView.getParent() != null) {
                            gameContainer.removeView(cardView);
                        }
                    });
        }

        void createParticles() {
            // Create 8 small particles flying outward
            for (int i = 0; i < 8; i++) {
                View particle = new View(WordExplosionActivity.this);
                particle.setBackgroundColor(android.graphics.Color.parseColor(color));
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(20, 20);
                particle.setLayoutParams(params);
                particle.setX(cardView.getX() + 65);
                particle.setY(cardView.getY() + 65);
                gameContainer.addView(particle);

                // Calculate outward direction
                double angle = (i * 45) * Math.PI / 180;
                float targetX = (float) (cardView.getX() + Math.cos(angle) * 200);
                float targetY = (float) (cardView.getY() + Math.sin(angle) * 200);

                particle.animate()
                        .x(targetX)
                        .y(targetY)
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction(() -> gameContainer.removeView(particle));
            }
        }
    }

    /**
     * Category Data Class
     */
    class CategoryData {
        List<String> words;
        String color;

        CategoryData(List<String> words, String color) {
            this.words = words;
            this.color = color;
        }
    }
}
