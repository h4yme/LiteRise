package com.example.literise.activities.games;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GestureDetectorCompat;

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
 * Synonym Sprint - Subway Surfers Style Runner
 * Swipe left/right to collect synonyms and avoid antonyms
 */
public class SynonymSprintActivity extends AppCompatActivity {

    // UI Elements
    private FrameLayout gameContainer;
    private TextView tvScore, tvDistance, tvLives, tvTargetWord, tvCombo;
    private ProgressBar progressBar;
    private CardView cardTargetWord, cardCombo;
    private MaterialButton btnPause;
    private ImageView[] heartIcons = new ImageView[3];
    private View characterView;
    private LottieAnimationView lottieCorrect, lottieComplete;

    // Game State
    private int score = 0;
    private int distance = 0;
    private int lives = 3;
    private int combo = 0;
    private int maxCombo = 0;
    private String targetWord = "happy";
    private boolean isGameActive = false;
    private long gameTimeElapsed = 0;

    // Lane System (Subway Surfers style)
    private int currentLane = 1; // 0 = left, 1 = center, 2 = right
    private int[] laneXPositions = new int[3];
    private static final int CHARACTER_Y = 900;

    // Game Objects
    private List<RunningWord> activeWords = new ArrayList<>();
    private Random random = new Random();
    private CountDownTimer gameTimer;
    private CountDownTimer spawnTimer;
    private GestureDetectorCompat gestureDetector;

    // Constants — grade-3-friendly (slower words, more time between spawns)
    private static final int GAME_DURATION = 150000;      // 2.5 minutes (more time)
    private static final int BASE_SPAWN_INTERVAL = 3000;  // 3 s between words
    private static final int WORD_SPEED = 6;              // px per frame (was 15)

    // Word data
    private Map<String, WordData> wordDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synonym_sprint);

        // Get target word from intent (optional)
        targetWord = getIntent().getStringExtra("target_word");
        if (targetWord == null) targetWord = "happy";

        initializeWordDatabase();
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
                                gameContainer.post(() -> {
                                    calculateLanePositions();
                                    positionCharacterInLane(currentLane, false);
                                    startGame();
                                });
                            }
                        }
                        @Override
                        public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                            gameContainer.post(() -> {
                                calculateLanePositions();
                                positionCharacterInLane(currentLane, false);
                                startGame();
                            });
                        }
                    });
        } else {
            // Wait for layout to calculate lane positions
            gameContainer.post(() -> {
                calculateLanePositions();
                positionCharacterInLane(currentLane, false);
                startGame();
            });
        }
    }

    private void generateWithAI(int nodeId, String lessonContent) {
        ApiService apiService = ApiClient.getAiClient(this).create(ApiService.class);
        GameContentRequest request = new GameContentRequest(nodeId, "synonym_sprint", lessonContent);
        apiService.generateGameContent(request).enqueue(new Callback<GameContentResponse>() {
            @Override
            public void onResponse(Call<GameContentResponse> call, Response<GameContentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success
                        && response.body().content != null) {
                    try {
                        JsonArray groupsArray = response.body().content.getAsJsonArray("groups");
                        Map<String, WordData> aiDatabase = new HashMap<>();
                        String firstWord = null;
                        for (int i = 0; i < groupsArray.size(); i++) {
                            JsonObject obj = groupsArray.get(i).getAsJsonObject();
                            String word = obj.get("targetWord").getAsString();
                            JsonArray synonymsArr = obj.getAsJsonArray("synonyms");
                            JsonArray antonymsArr = obj.getAsJsonArray("antonyms");
                            List<String> synonyms = new ArrayList<>();
                            List<String> antonyms = new ArrayList<>();
                            for (int j = 0; j < synonymsArr.size(); j++) {
                                synonyms.add(synonymsArr.get(j).getAsString());
                            }
                            for (int j = 0; j < antonymsArr.size(); j++) {
                                antonyms.add(antonymsArr.get(j).getAsString());
                            }
                            aiDatabase.put(word, new WordData(synonyms, antonyms));
                            if (firstWord == null) firstWord = word;
                        }
                        if (!aiDatabase.isEmpty() && firstWord != null) {
                            wordDatabase = aiDatabase;
                            targetWord = firstWord;
                            tvTargetWord.setText(targetWord.toUpperCase());
                        }
                    } catch (Exception e) {
                        android.util.Log.w("SynonymSprint", "AI parse error: " + e.getMessage());
                    }
                } else {
                    android.util.Log.w("SynonymSprint", "AI generate failed: code=" + response.code()
                            + " msg=" + (response.body() != null ? response.body().message : "null"));
                }
                gameContainer.post(() -> {
                    calculateLanePositions();
                    positionCharacterInLane(currentLane, false);
                    startGame();
                });
            }

            @Override
            public void onFailure(Call<GameContentResponse> call, Throwable t) {
                android.util.Log.w("SynonymSprint", "AI generate network error: " + t.getMessage());
                gameContainer.post(() -> {
                    calculateLanePositions();
                    positionCharacterInLane(currentLane, false);
                    startGame();
                });
            }
        });
    }

    private void calculateLanePositions() {
        int screenWidth = gameContainer.getWidth();
        int laneWidth = screenWidth / 3;

        // Calculate center of each lane
        laneXPositions[0] = laneWidth / 2 - 40; // Left lane (centered)
        laneXPositions[1] = screenWidth / 2 - 40; // Center lane
        laneXPositions[2] = screenWidth - laneWidth / 2 - 40; // Right lane
    }

    private void initializeWordDatabase() {
        wordDatabase = new HashMap<>();

        // Happy group
        wordDatabase.put("happy", new WordData(
                Arrays.asList("joyful", "glad", "cheerful", "pleased", "delighted", "excited"),
                Arrays.asList("sad", "unhappy", "gloomy", "miserable", "angry")
        ));

        // Fast group
        wordDatabase.put("fast", new WordData(
                Arrays.asList("quick", "rapid", "swift", "speedy", "hasty"),
                Arrays.asList("slow", "sluggish", "lazy", "gradual")
        ));

        // Big group
        wordDatabase.put("big", new WordData(
                Arrays.asList("large", "huge", "giant", "enormous", "massive"),
                Arrays.asList("small", "tiny", "little", "miniature")
        ));

        // Smart group
        wordDatabase.put("smart", new WordData(
                Arrays.asList("clever", "bright", "intelligent", "wise", "brilliant"),
                Arrays.asList("dumb", "foolish", "stupid", "silly")
        ));

        // Beautiful group
        wordDatabase.put("beautiful", new WordData(
                Arrays.asList("pretty", "lovely", "gorgeous", "stunning", "attractive"),
                Arrays.asList("ugly", "hideous", "plain", "unattractive")
        ));
    }

    private void initializeViews() {
        gameContainer = findViewById(R.id.gameContainer);
        tvScore = findViewById(R.id.tvScore);
        tvDistance = findViewById(R.id.tvDistance);
        tvLives = findViewById(R.id.tvLives);
        tvTargetWord = findViewById(R.id.tvTargetWord);
        tvCombo = findViewById(R.id.tvCombo);
        progressBar = findViewById(R.id.progressBar);
        cardTargetWord = findViewById(R.id.cardTargetWord);
        cardCombo = findViewById(R.id.cardCombo);
        btnPause = findViewById(R.id.btnPause);
        heartIcons[0] = findViewById(R.id.heart1);
        heartIcons[1] = findViewById(R.id.heart2);
        heartIcons[2] = findViewById(R.id.heart3);
        characterView = findViewById(R.id.characterView);
        lottieCorrect = findViewById(R.id.lottieCorrect);
        lottieComplete = findViewById(R.id.lottieComplete);

        // Set target word
        tvTargetWord.setText(targetWord.toUpperCase());
        cardCombo.setVisibility(View.GONE);
    }

    private void applyModuleTheme() {
        try {
            android.content.Intent intent = getIntent();
            String colorStart = intent.getStringExtra("module_color_start");
            String colorEnd = intent.getStringExtra("module_color_end");
            if (colorStart == null || colorStart.isEmpty()) colorStart = "#4ECDC4";
            if (colorEnd == null || colorEnd.isEmpty()) colorEnd = "#44A08D";
            if (cardTargetWord != null) {
                android.graphics.drawable.GradientDrawable grad =
                    new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        new int[]{android.graphics.Color.parseColor(colorStart),
                                  android.graphics.Color.parseColor(colorEnd)});
                float r = 20 * getResources().getDisplayMetrics().density;
                grad.setCornerRadius(r);
                cardTargetWord.setBackground(grad);
            }
            if (progressBar != null) {
                progressBar.setProgressTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorStart)));
            }
        } catch (Exception ignored) {}
    }

    private void setupListeners() {
        btnPause.setOnClickListener(v -> pauseGame());

        // Setup gesture detector for swipes
        gestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (!isGameActive) return false;

                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            // Swipe right
                            moveRight();
                        } else {
                            // Swipe left
                            moveLeft();
                        }
                        return true;
                    }
                }
                return false;
            }
        });

        gameContainer.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void moveLeft() {
        if (currentLane > 0) {
            currentLane--;
            positionCharacterInLane(currentLane, true);
        }
    }

    private void moveRight() {
        if (currentLane < 2) {
            currentLane++;
            positionCharacterInLane(currentLane, true);
        }
    }

    private void positionCharacterInLane(int lane, boolean animate) {
        float targetX = laneXPositions[lane];

        if (animate) {
            characterView.animate()
                    .x(targetX)
                    .setDuration(150)
                    .start();
        } else {
            characterView.setX(targetX);
            characterView.setY(CHARACTER_Y);
        }
    }

    private void startGame() {
        isGameActive = true;
        score = 0;
        distance = 0;
        lives = 3;
        combo = 0;
        updateUI();

        // Start game timer
        gameTimer = new CountDownTimer(GAME_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                gameTimeElapsed = GAME_DURATION - millisUntilFinished;
                distance += 5;
                tvDistance.setText(distance + "m");
                progressBar.setProgress((int) ((gameTimeElapsed * 100) / GAME_DURATION));
            }

            @Override
            public void onFinish() {
                endGame();
            }
        }.start();

        // Start game loop
        startGameLoop();
        startWordSpawning();
    }

    private void startGameLoop() {
        final Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                if (isGameActive) {
                    updateGame();
                    gameContainer.postDelayed(this, 16); // ~60 FPS
                }
            }
        };
        gameContainer.post(gameLoop);
    }

    private void updateGame() {
        // Move all words toward player
        List<RunningWord> wordsToRemove = new ArrayList<>();

        for (RunningWord word : activeWords) {
            word.move();

            // Check if word reached the player
            if (!word.isCollected && word.textView.getY() >= CHARACTER_Y - 50) {
                // Check if player is in the same lane
                if (word.lane == currentLane) {
                    word.isCollected = true;
                    if (word.isSynonym) {
                        onSynonymCollected(word);
                    } else {
                        onAntonymHit(word);
                    }
                }

                // Remove word if it passed the player
                if (word.textView.getY() > CHARACTER_Y + 200) {
                    if (word.isSynonym && !word.isCollected) {
                        // Missed a synonym
                        combo = 0;
                        cardCombo.setVisibility(View.GONE);
                    }
                    wordsToRemove.add(word);
                    gameContainer.removeView(word.textView);
                }
            }
        }

        activeWords.removeAll(wordsToRemove);
    }

    private void startWordSpawning() {
        spawnTimer = new CountDownTimer(Long.MAX_VALUE, BASE_SPAWN_INTERVAL) {
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
        WordData data = wordDatabase.get(targetWord);
        if (data == null) return;

        String word;
        boolean isSynonym;

        // 70% synonyms, 30% antonyms
        if (random.nextInt(100) < 70) {
            word = data.synonyms.get(random.nextInt(data.synonyms.size()));
            isSynonym = true;
        } else {
            word = data.antonyms.get(random.nextInt(data.antonyms.size()));
            isSynonym = false;
        }

        // Random lane
        int lane = random.nextInt(3);

        RunningWord runningWord = new RunningWord(word, isSynonym, lane);
        activeWords.add(runningWord);
        gameContainer.addView(runningWord.textView);
    }

    private void onSynonymCollected(RunningWord word) {
        // Increase combo
        combo++;
        maxCombo = Math.max(maxCombo, combo);

        // Calculate score with combo multiplier
        int points = 10 * Math.max(1, combo / 3);
        score += points;

        // Show combo card if combo >= 3
        if (combo >= 3) {
            cardCombo.setVisibility(View.VISIBLE);
            tvCombo.setText(combo + "x STREAK!");
            cardCombo.animate().scaleX(1.2f).scaleY(1.2f).setDuration(180)
                    .withEndAction(() -> cardCombo.animate().scaleX(1f).scaleY(1f).setDuration(180));
        }

        // Lottie sparkle
        playLottieOnce(lottieCorrect);

        // Animate word collection — slightly slower so child sees it
        word.textView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(350)
                .withEndAction(() -> {
                    if (word.textView.getParent() != null) gameContainer.removeView(word.textView);
                });

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

    private void onAntonymHit(RunningWord word) {
        // Lose a life
        lives--;
        combo = 0; // Reset combo
        cardCombo.setVisibility(View.GONE);

        // Update hearts
        if (lives >= 0 && lives < 3) {
            heartIcons[lives].setVisibility(View.INVISIBLE);
        }

        // Grade-3 friendly screen shake (gentler — 3 bounces, 80ms each)
        gameContainer.animate()
                .translationX(-18f).setDuration(80)
                .withEndAction(() -> gameContainer.animate().translationX(18f).setDuration(80)
                .withEndAction(() -> gameContainer.animate().translationX(-10f).setDuration(80)
                .withEndAction(() -> gameContainer.animate().translationX(0f).setDuration(80))));

        // Animate word explosion — slower so child sees the feedback
        word.textView.animate()
                .scaleX(2.2f)
                .scaleY(2.2f)
                .alpha(0f)
                .rotation(360f)
                .setDuration(600)
                .withEndAction(() -> {
                    if (word.textView.getParent() != null) gameContainer.removeView(word.textView);
                });

        updateUI();

        if (lives <= 0) {
            endGame();
        }
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
        for (RunningWord word : activeWords) {
            if (word.textView.getParent() != null) gameContainer.removeView(word.textView);
        }
        activeWords.clear();

        // Mark game phase complete in StudentNodeProgress
        markGamePhaseComplete(getIntent().getIntExtra("node_id", -1));

        // Lottie celebration then finish
        if (lottieComplete != null) {
            lottieComplete.setVisibility(View.VISIBLE);
            lottieComplete.playAnimation();
            lottieComplete.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    String message = String.format("Great job!\nScore: %d\nDistance: %dm\nBest Streak: %dx",
                            score, distance, maxCombo);
                    Toast.makeText(SynonymSprintActivity.this, message, Toast.LENGTH_LONG).show();
                    android.content.Intent result = new android.content.Intent();
                    result.putExtra("xp_earned", score);
                    setResult(RESULT_OK, result);
                    finish();
                }
            });
        } else {
            String message = String.format("Great job!\nScore: %d\nDistance: %dm",
                    score, distance);
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            android.content.Intent result = new android.content.Intent();
            result.putExtra("xp_earned", score);
            setResult(RESULT_OK, result);
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
     * Running Word Class - Now moves from top to bottom
     */
    class RunningWord {
        TextView textView;
        String word;
        boolean isSynonym;
        boolean isCollected = false;
        int lane;

        RunningWord(String word, boolean isSynonym, int lane) {
            this.word = word;
            this.isSynonym = isSynonym;
            this.lane = lane;

            // Create TextView — larger text for grade 3 readability
            textView = new TextView(SynonymSprintActivity.this);
            textView.setText(word);
            textView.setTextSize(22);
            textView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            textView.setTextColor(0xFFFFFFFF);
            textView.setPadding(32, 20, 32, 20);
            textView.setGravity(android.view.Gravity.CENTER);
            textView.setBackgroundResource(isSynonym ?
                    R.drawable.word_card_synonym : R.drawable.word_card_antonym);

            // Position in specific lane at top of screen
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            textView.setLayoutParams(params);
            textView.setX(laneXPositions[lane]);
            textView.setY(-100); // Start above screen
        }

        void move() {
            textView.setY(textView.getY() + WORD_SPEED);
        }
    }

    /**
     * Word Data Class
     */
    class WordData {
        List<String> synonyms;
        List<String> antonyms;

        WordData(List<String> synonyms, List<String> antonyms) {
            this.synonyms = synonyms;
            this.antonyms = antonyms;
        }
    }
}
