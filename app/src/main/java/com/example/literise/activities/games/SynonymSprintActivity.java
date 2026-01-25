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

import com.example.literise.R;
import com.google.android.material.button.MaterialButton;

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

    // Constants
    private static final int GAME_DURATION = 120000; // 2 minutes
    private static final int BASE_SPAWN_INTERVAL = 1500; // 1.5 seconds
    private static final int WORD_SPEED = 15; // pixels per frame

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
        setupListeners();

        // Wait for layout to calculate lane positions
        gameContainer.post(() -> {
            calculateLanePositions();
            positionCharacterInLane(currentLane, false);
            startGame();
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

        // Set target word
        tvTargetWord.setText(targetWord.toUpperCase());
        cardCombo.setVisibility(View.GONE);
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
            cardCombo.animate().scaleX(1.2f).scaleY(1.2f).setDuration(100)
                    .withEndAction(() -> cardCombo.animate().scaleX(1f).scaleY(1f).setDuration(100));
        }

        // Animate word collection
        word.textView.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> gameContainer.removeView(word.textView));

        updateUI();
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

        // Shake screen effect
        gameContainer.animate()
                .translationX(-20f).setDuration(50)
                .withEndAction(() -> gameContainer.animate().translationX(20f).setDuration(50)
                        .withEndAction(() -> gameContainer.animate().translationX(0f).setDuration(50)));

        // Animate word explosion
        word.textView.animate()
                .scaleX(2f)
                .scaleY(2f)
                .alpha(0f)
                .rotation(360f)
                .setDuration(300)
                .withEndAction(() -> gameContainer.removeView(word.textView));

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
            gameContainer.removeView(word.textView);
        }
        activeWords.clear();

        // Show results
        String message = String.format("Game Over!\nScore: %d\nDistance: %dm\nMax Streak: %dx",
                score, distance, maxCombo);
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

            // Create TextView
            textView = new TextView(SynonymSprintActivity.this);
            textView.setText(word);
            textView.setTextSize(18);
            textView.setTextColor(0xFFFFFFFF);
            textView.setPadding(24, 16, 24, 16);
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
