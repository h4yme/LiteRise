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

import com.airbnb.lottie.LottieAnimationView;
import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.models.GameContentRequest;
import com.example.literise.models.GameContentResponse;
import com.example.literise.models.LessonContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private LottieAnimationView lottieCorrect, lottieComplete;

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

    // Constants — grade-3-friendly (slower, more time to react)
    private static final int BASE_SPAWN_INTERVAL = 2500; // 2.5 seconds between bubbles
    private static final int MIN_SPAWN_INTERVAL = 1500;  // never faster than 1.5 s
    private static final float BASE_RISE_SPEED = 7000f;  // 7 seconds to rise
    private static final float MIN_RISE_SPEED   = 4000f; // never faster than 4 s

    // Word Categories
    private Map<String, CategoryData> categoryDatabase;
    private List<String> availableCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_explosion);

        initializeCategoryDatabase();
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
                                startGame();
                            }
                        }
                        @Override
                        public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                            startGame();
                        }
                    });
        } else {
            startGame();
        }
    }

    private void generateWithAI(int nodeId, String lessonContent) {
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.generateGameContent(new GameContentRequest(nodeId, "word_explosion", lessonContent))
                .enqueue(new Callback<GameContentResponse>() {
            @Override
            public void onResponse(Call<GameContentResponse> call, Response<GameContentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success
                        && response.body().content != null) {
                    try {
                        JsonArray cats = response.body().content.getAsJsonArray("categories");
                        for (int i = 0; i < cats.size(); i++) {
                            JsonObject obj = cats.get(i).getAsJsonObject();
                            String name  = obj.get("name").getAsString().toUpperCase();
                            String color = obj.has("color") ? obj.get("color").getAsString() : "#7C3AED";
                            JsonArray wordsArr = obj.getAsJsonArray("words");
                            List<String> wordList = new ArrayList<>();
                            for (int j = 0; j < wordsArr.size(); j++) {
                                wordList.add(wordsArr.get(j).getAsString().toLowerCase());
                            }
                            if (!wordList.isEmpty()) {
                                categoryDatabase.put(name, new CategoryData(wordList, color));
                                if (!availableCategories.contains(name)) {
                                    availableCategories.add(0, name);
                                }
                            }
                        }
                        // Set first AI category as target
                        if (cats.size() > 0) {
                            String firstName = cats.get(0).getAsJsonObject().get("name").getAsString().toUpperCase();
                            targetCategory = firstName;
                        }
                    } catch (Exception ignored) {}
                }
                startGame();
            }

            @Override
            public void onFailure(Call<GameContentResponse> call, Throwable t) {
                startGame();
            }
        });
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
        lottieCorrect = findViewById(R.id.lottieCorrect);
        lottieComplete = findViewById(R.id.lottieComplete);

        // Set initial target category
        setNewTargetCategory();
        cardCombo.setVisibility(View.GONE);
    }

    private void applyModuleTheme() {
        try {
            android.content.Intent intent = getIntent();
            String colorStart = intent.getStringExtra("module_color_start");
            String colorEnd = intent.getStringExtra("module_color_end");
            if (colorStart == null || colorStart.isEmpty()) colorStart = "#4ECDC4";
            if (colorEnd == null || colorEnd.isEmpty()) colorEnd = "#44A08D";
            if (cardTargetCategory != null) {
                android.graphics.drawable.GradientDrawable grad =
                    new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        new int[]{android.graphics.Color.parseColor(colorStart),
                                  android.graphics.Color.parseColor(colorEnd)});
                float r = 20 * getResources().getDisplayMetrics().density;
                grad.setCornerRadius(r);
                cardTargetCategory.setBackground(grad);
            }
            if (progressBar != null) {
                progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorStart)));
            }
        } catch (Exception ignored) {}
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
            cardCombo.animate().scaleX(1.2f).scaleY(1.2f).setDuration(180)
                    .withEndAction(() -> cardCombo.animate().scaleX(1f).scaleY(1f).setDuration(180));
        }

        // Lottie sparkle on every correct pop
        playLottieOnce(lottieCorrect);

        // Change category every 5 correct pops
        if (combo > 0 && combo % 5 == 0) {
            setNewTargetCategory();
        }

        updateUI();
    }

    /** Plays a Lottie animation once, hiding it when done. */
    private void playLottieOnce(LottieAnimationView view) {
        if (view == null) return;
        view.cancelAnimation();
        view.setProgress(0f);
        view.setVisibility(View.VISIBLE);
        view.playAnimation();
        view.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
                view.removeAllAnimatorListeners();
            }
        });
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

        // Lottie celebration then finish
        if (lottieComplete != null) {
            lottieComplete.setVisibility(View.VISIBLE);
            lottieComplete.playAnimation();
            lottieComplete.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    String message = String.format("Great job!\nScore: %d\nBest Combo: %dx", score, maxCombo);
                    Toast.makeText(WordExplosionActivity.this, message, Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } else {
            String message = String.format("Great job!\nScore: %d\nBest Combo: %dx", score, maxCombo);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            finish();
        }
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

            // Create CardView bubble — bigger and rounder for grade 3
            cardView = new CardView(WordExplosionActivity.this);
            cardView.setCardElevation(12);
            cardView.setRadius(56);
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor(color));

            // Create TextView — larger font so kids can read easily
            textView = new TextView(WordExplosionActivity.this);
            textView.setText(word);
            textView.setTextSize(19);
            android.graphics.Typeface bold = android.graphics.Typeface.DEFAULT_BOLD;
            textView.setTypeface(bold);
            textView.setTextColor(0xFFFFFFFF);
            textView.setPadding(24, 20, 24, 20);
            textView.setGravity(android.view.Gravity.CENTER);

            cardView.addView(textView);

            // Random X position
            int screenWidth = gameContainer.getWidth();
            int bubbleWidth = 180; // larger bubbles
            int randomX = random.nextInt(Math.max(1, screenWidth - bubbleWidth));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    bubbleWidth,
                    bubbleWidth
            );
            // noinspection deprecation — using px intentionally here
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

            // Grade-3 friendly: slower, more satisfying pop
            cardView.animate()
                    .scaleX(2.5f)
                    .scaleY(2.5f)
                    .alpha(0f)
                    .setDuration(700)
                    .withEndAction(() -> {
                        if (cardView.getParent() != null) {
                            gameContainer.removeView(cardView);
                        }
                    });
        }

        void createParticles() {
            // 10 larger, slower particles so kids can see the explosion clearly
            int bubbleCenterX = (int) (cardView.getX() + 85);
            int bubbleCenterY = (int) (cardView.getY() + 85);
            for (int i = 0; i < 10; i++) {
                View particle = new View(WordExplosionActivity.this);
                particle.setBackgroundColor(android.graphics.Color.parseColor(color));
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(36, 36);
                particle.setLayoutParams(params);
                particle.setX(bubbleCenterX);
                particle.setY(bubbleCenterY);
                gameContainer.addView(particle);

                // Spread at 36° intervals for 10 particles
                double angle = (i * 36) * Math.PI / 180;
                float targetX = (float) (bubbleCenterX + Math.cos(angle) * 280);
                float targetY = (float) (bubbleCenterY + Math.sin(angle) * 280);

                particle.animate()
                        .x(targetX)
                        .y(targetY)
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .setDuration(900)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .withEndAction(() -> {
                            if (particle.getParent() != null) gameContainer.removeView(particle);
                        });
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
