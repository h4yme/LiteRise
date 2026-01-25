package com.example.literise.activities;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.models.LessonContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ENHANCED Phonics Ninja - Ultimate Word Slicing Experience
 * - API-based adaptive content
 * - Stunning visual effects with trails and particles
 * - 45-second intense gameplay
 * - Immersive feedback and animations
 */
public class PhonicsNinjaActivity extends AppCompatActivity {

    // UI Elements
    private FrameLayout gameContainer;
    private TextView tvScore, tvTimer, tvTargetPattern, tvCombo, tvMultiplier;
    private ProgressBar progressBar;
    private CardView cardTargetPattern, cardCombo;
    private MaterialButton btnPause;
    private ImageView[] heartIcons = new ImageView[3];

    // Trail effect
    private List<TrailParticle> trailParticles = new ArrayList<>();

    // Game State
    private int score = 0;
    private int lives = 3;
    private int combo = 0;
    private int maxCombo = 0;
    private int level = 1;
    private String targetPattern = "CVCC";
    private boolean isGameActive = false;
    private long gameTimeRemaining = 45000; // 45 SECONDS!
    private int scoreMultiplier = 1;

    // Game Objects
    private List<FallingWord> activeWords = new ArrayList<>();
    private Random random = new Random();
    private CountDownTimer gameTimer;
    private CountDownTimer spawnTimer;

    // API Data
    private int nodeId;
    private int studentId;
    private int placementLevel = 1;
    private Map<String, List<String>> wordsByCategory = new HashMap<>();
    private boolean isLoadingContent = true;

    // Constants
    private static final int BASE_SPAWN_INTERVAL = 1200; // Faster spawning!
    private static final int MIN_SPAWN_INTERVAL = 600;
    private static final float BASE_FALL_SPEED = 2500f; // Faster falling!
    private static final float MIN_FALL_SPEED = 1200f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonics_ninja);

        // Get intent data
        nodeId = getIntent().getIntExtra("node_id", 1);
        studentId = getIntent().getIntExtra("student_id", 1);
        targetPattern = getIntent().getStringExtra("target_pattern");
        if (targetPattern == null) targetPattern = "CVCC";

        initializeViews();
        setupListeners();
        loadAdaptiveContent(); // Load from API!
    }

    private void loadAdaptiveContent() {
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        Call<LessonContentResponse> call = apiService.getLessonContent(nodeId, placementLevel);
        call.enqueue(new Callback<LessonContentResponse>() {
            @Override
            public void onResponse(Call<LessonContentResponse> call, Response<LessonContentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    parseContentJSON(response.body().getContentJSON());
                } else {
                    loadFallbackContent();
                }
                isLoadingContent = false;
                startGame();
            }

            @Override
            public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                loadFallbackContent();
                isLoadingContent = false;
                startGame();
            }
        });
    }

    private void parseContentJSON(String contentJSON) {
        try {
            Gson gson = new Gson();
            Map<String, Object> content = gson.fromJson(contentJSON,
                    new TypeToken<Map<String, Object>>(){}.getType());

            // Parse phonics words from content
            if (content.containsKey("phonics_words")) {
                Map<String, List<String>> phonicsData = (Map<String, List<String>>) content.get("phonics_words");
                wordsByCategory.putAll(phonicsData);
            }
        } catch (Exception e) {
            loadFallbackContent();
        }
    }

    private void loadFallbackContent() {
        // Fallback word lists
        wordsByCategory.put("CVCC", Arrays.asList("jump", "bent", "melt", "lamp", "tent", "pump", "wind", "hand", "send", "fast", "list"));
        wordsByCategory.put("CCVC", Arrays.asList("trip", "clap", "flag", "stop", "from", "glad", "drip", "crop", "snap", "slip"));
        wordsByCategory.put("correct", wordsByCategory.getOrDefault(targetPattern, wordsByCategory.get("CVCC")));
        wordsByCategory.put("wrong", Arrays.asList("cat", "dog", "run", "sit", "hot", "big", "yes", "not", "can", "top"));
    }

    private void initializeViews() {
        gameContainer = findViewById(R.id.gameContainer);
        tvScore = findViewById(R.id.tvScore);
        tvTimer = findViewById(R.id.tvTimer);
        tvTargetPattern = findViewById(R.id.tvTargetPattern);
        tvCombo = findViewById(R.id.tvCombo);
        tvMultiplier = findViewById(R.id.tvMultiplier);
        progressBar = findViewById(R.id.progressBar);
        cardTargetPattern = findViewById(R.id.cardTargetPattern);
        cardCombo = findViewById(R.id.cardCombo);
        btnPause = findViewById(R.id.btnPause);
        heartIcons[0] = findViewById(R.id.heart1);
        heartIcons[1] = findViewById(R.id.heart2);
        heartIcons[2] = findViewById(R.id.heart3);

        tvTargetPattern.setText(getPatternDisplayName(targetPattern));
        cardCombo.setVisibility(View.GONE);
    }

    private void setupListeners() {
        btnPause.setOnClickListener(v -> pauseGame());

        // Enhanced touch handling with trail effects
        gameContainer.setOnTouchListener((v, event) -> {
            if (!isGameActive) return false;

            float x = event.getX();
            float y = event.getY();

            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                checkWordSlice(x, y);
                createSlashTrail(x, y); // Awesome trail effect!
            }
            return true;
        });
    }

    private void startGame() {
        if (isLoadingContent) return;

        isGameActive = true;
        score = 0;
        lives = 3;
        combo = 0;
        level = 1;
        scoreMultiplier = 1;
        updateUI();

        // 45-second game timer with visual feedback
        gameTimer = new CountDownTimer(gameTimeRemaining, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                gameTimeRemaining = millisUntilFinished;
                int secondsRemaining = (int) (millisUntilFinished / 1000);
                tvTimer.setText(secondsRemaining + "s");
                progressBar.setProgress((int) ((millisUntilFinished * 100) / 45000));

                // Warning effect when time < 10s
                if (secondsRemaining <= 10 && secondsRemaining % 2 == 0) {
                    tvTimer.setTextColor(Color.parseColor("#FF6B6B"));
                    tvTimer.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100)
                            .withEndAction(() -> tvTimer.animate().scaleX(1f).scaleY(1f).setDuration(100));
                }
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();

        startWordSpawning();
        startTrailAnimation();
    }

    private void startWordSpawning() {
        int currentSpeed = Math.max(MIN_SPAWN_INTERVAL, BASE_SPAWN_INTERVAL - (level * 80));

        spawnTimer = new CountDownTimer(Long.MAX_VALUE, currentSpeed) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (isGameActive) {
                    spawnRandomWord();
                    // Increase difficulty every 10 seconds
                    if (combo > 0 && combo % 10 == 0) {
                        level = Math.min(5, combo / 10 + 1);
                    }
                }
            }

            @Override
            public void onFinish() {}
        }.start();
    }

    private void spawnRandomWord() {
        List<String> correctWords = wordsByCategory.getOrDefault("correct",
                wordsByCategory.get(targetPattern));
        List<String> wrongWords = wordsByCategory.get("wrong");

        String word;
        boolean isCorrect;

        // 65% correct, 35% wrong for balanced challenge
        if (random.nextInt(100) < 65) {
            word = correctWords.get(random.nextInt(correctWords.size()));
            isCorrect = true;
        } else {
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

            float wordX = word.textView.getX() + word.textView.getWidth() / 2f;
            float wordY = word.textView.getY() + word.textView.getHeight() / 2f;
            float distance = (float) Math.sqrt(Math.pow(touchX - wordX, 2) + Math.pow(touchY - wordY, 2));

            if (distance < 120) { // Hit detection
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

    private void createSlashTrail(float x, float y) {
        TrailParticle particle = new TrailParticle(x, y);
        trailParticles.add(particle);
        gameContainer.addView(particle.view);
        particle.animate();
    }

    private void startTrailAnimation() {
        // Continuous trail cleanup
        final Runnable trailCleanup = new Runnable() {
            @Override
            public void run() {
                if (isGameActive) {
                    List<TrailParticle> toRemove = new ArrayList<>();
                    for (TrailParticle particle : trailParticles) {
                        if (particle.view.getAlpha() <= 0.1f) {
                            gameContainer.removeView(particle.view);
                            toRemove.add(particle);
                        }
                    }
                    trailParticles.removeAll(toRemove);
                    gameContainer.postDelayed(this, 50);
                }
            }
        };
        gameContainer.post(trailCleanup);
    }

    private void onCorrectSlice(FallingWord word) {
        combo++;
        maxCombo = Math.max(maxCombo, combo);

        // Dynamic score multiplier
        scoreMultiplier = 1 + (combo / 5);
        int points = 10 * scoreMultiplier;
        score += points;

        // Visual feedback for multiplier
        if (scoreMultiplier > 1) {
            tvMultiplier.setText(scoreMultiplier + "x");
            tvMultiplier.setVisibility(View.VISIBLE);
            tvMultiplier.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100)
                    .withEndAction(() -> tvMultiplier.animate().scaleX(1f).scaleY(1f));
        }

        // Combo card with glow effect
        if (combo >= 3) {
            cardCombo.setVisibility(View.VISIBLE);
            tvCombo.setText(combo + "x COMBO!");
            cardCombo.setCardBackgroundColor(getComboColor(combo));
            cardCombo.animate().scaleX(1.3f).scaleY(1.3f).rotation(360f).setDuration(200)
                    .withEndAction(() -> cardCombo.animate().scaleX(1f).scaleY(1f).rotation(0f).setDuration(200));
        }

        // Particle burst
        createParticleBurst(word.textView.getX() + 50, word.textView.getY() + 50, "#4ECDC4");

        updateUI();
    }

    private void onWrongSlice(FallingWord word) {
        lives--;
        combo = 0;
        scoreMultiplier = 1;
        cardCombo.setVisibility(View.GONE);
        tvMultiplier.setVisibility(View.GONE);

        if (lives >= 0 && lives < 3) {
            heartIcons[lives].setVisibility(View.INVISIBLE);
            heartIcons[lives].animate().scaleX(2f).scaleY(2f).alpha(0f).setDuration(300);
        }

        // Screen shake
        gameContainer.animate().translationX(-30f).setDuration(50)
                .withEndAction(() -> gameContainer.animate().translationX(30f).setDuration(50)
                        .withEndAction(() -> gameContainer.animate().translationX(0f).setDuration(50)));

        // Red particle burst
        createParticleBurst(word.textView.getX() + 50, word.textView.getY() + 50, "#FF6B6B");

        updateUI();

        if (lives <= 0) {
            endGame();
        }
    }

    private void createParticleBurst(float x, float y, String color) {
        for (int i = 0; i < 12; i++) {
            View particle = new View(this);
            particle.setBackgroundColor(Color.parseColor(color));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(15, 15);
            particle.setLayoutParams(params);
            particle.setX(x);
            particle.setY(y);
            gameContainer.addView(particle);

            double angle = (i * 30) * Math.PI / 180;
            float targetX = (float) (x + Math.cos(angle) * 150);
            float targetY = (float) (y + Math.sin(angle) * 150);

            particle.animate()
                    .x(targetX)
                    .y(targetY)
                    .alpha(0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .setDuration(400)
                    .withEndAction(() -> gameContainer.removeView(particle));
        }
    }

    private int getComboColor(int combo) {
        if (combo >= 15) return Color.parseColor("#FF6B6B"); // Red - Insane!
        if (combo >= 10) return Color.parseColor("#FFD93D"); // Yellow - Amazing!
        if (combo >= 5) return Color.parseColor("#4ECDC4"); // Teal - Great!
        return Color.parseColor("#A770EF"); // Purple - Good!
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

        for (FallingWord word : activeWords) {
            gameContainer.removeView(word.textView);
        }
        activeWords.clear();

        String message = String.format("🥷 NINJA MASTER!\n\n💯 Score: %d\n🔥 Max Combo: %dx\n⭐ Multiplier: %dx",
                score, maxCombo, scoreMultiplier);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private String getPatternDisplayName(String pattern) {
        switch (pattern) {
            case "CVCC": return "CVCC (jump, bent)";
            case "CCVC": return "CCVC (trip, clap)";
            case "LONG_A": return "Long A (cake, rain)";
            case "LONG_E": return "Long E (tree, bean)";
            case "BLENDS": return "Blends (stop, from)";
            case "DIGRAPHS": return "Digraphs (shop, chip)";
            default: return pattern;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (gameTimer != null) gameTimer.cancel();
        if (spawnTimer != null) spawnTimer.cancel();
    }

    // Trail Particle Class
    class TrailParticle {
        View view;

        TrailParticle(float x, float y) {
            view = new View(PhonicsNinjaActivity.this);
            view.setBackgroundResource(R.drawable.slash_trail);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(60, 60);
            view.setLayoutParams(params);
            view.setX(x - 30);
            view.setY(y - 30);
            view.setAlpha(0.8f);
        }

        void animate() {
            view.animate()
                    .alpha(0f)
                    .scaleX(1.5f)
                    .scaleY(1.5f)
                    .setDuration(300);
        }
    }

    // Falling Word Class
    class FallingWord {
        TextView textView;
        String word;
        boolean isCorrect;
        boolean isSliced = false;
        ValueAnimator fallAnimator;

        FallingWord(String word, boolean isCorrect) {
            this.word = word;
            this.isCorrect = isCorrect;

            textView = new TextView(PhonicsNinjaActivity.this);
            textView.setText(word);
            textView.setTextSize(22);
            textView.setTextColor(0xFFFFFFFF);
            textView.setPadding(35, 25, 35, 25);
            textView.setBackgroundResource(isCorrect ? R.drawable.word_card_correct : R.drawable.word_card_wrong);
            textView.setElevation(12);
            textView.setShadowLayer(10, 0, 0, isCorrect ? Color.parseColor("#4ECDC4") : Color.parseColor("#FF6B6B"));

            int screenWidth = gameContainer.getWidth();
            int randomX = random.nextInt(Math.max(1, screenWidth - 200));

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textView.setLayoutParams(params);
            textView.setX(randomX);
            textView.setY(-100);
        }

        void startFalling() {
            int screenHeight = gameContainer.getHeight();
            float fallSpeed = Math.max(MIN_FALL_SPEED, BASE_FALL_SPEED - (level * 150));

            fallAnimator = ValueAnimator.ofFloat(-100, screenHeight + 100);
            fallAnimator.setDuration((long) fallSpeed);
            fallAnimator.addUpdateListener(animation -> {
                if (textView.getParent() != null) {
                    float y = (float) animation.getAnimatedValue();
                    textView.setY(y);
                    textView.setRotation(y / 12f);
                }
            });
            fallAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    if (!isSliced && isCorrect) {
                        lives--;
                        combo = 0;
                        cardCombo.setVisibility(View.GONE);
                        if (lives >= 0 && lives < 3) {
                            heartIcons[lives].setVisibility(View.INVISIBLE);
                        }
                        updateUI();
                        if (lives <= 0) endGame();
                    }
                    gameContainer.removeView(textView);
                    activeWords.remove(FallingWord.this);
                }
            });
            fallAnimator.start();
        }

        void explode() {
            if (fallAnimator != null) fallAnimator.cancel();

            textView.animate()
                    .scaleX(2.5f)
                    .scaleY(2.5f)
                    .alpha(0f)
                    .rotation(720f)
                    .setDuration(400)
                    .withEndAction(() -> {
                        if (textView.getParent() != null) {
                            gameContainer.removeView(textView);
                        }
                    });
        }
    }
}
