package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.activities.games.SentenceScrambleActivity;
import com.example.literise.activities.games.WordHuntActivity;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.GameSession;
import com.example.literise.models.LessonProgressResponse;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonActivity extends AppCompatActivity {

    private static final int GAME_REQUEST_CODE = 100;
    private static final int TOTAL_GAMES_REQUIRED = 5;

    private TextView tvLessonTitle, tvLessonProgress, tvProgressPercentage;
    private TextView tvTotalXP, tvAverageAccuracy;
    private TextView tvGameTitle, tvGameDescription, tvGameReward;
    private ImageView ivBack, ivGameIcon;
    private ProgressBar progressBar, loadingProgress;
    private MaterialButton btnStartGame;
    private CardView cardNextGame;

    private GameSession gameSession;
    private SessionManager session;
    private String lessonType;
    private int lessonId = -1;

    private List<String> availableGames;
    private String currentGame;
    private int totalXPEarned = 0;
    private float totalAccuracy = 0;
    private int gamesPlayed = 0;
    private int gamesPlayedAtStart = 0;
    private boolean isLessonCompleted = false;
    private LessonProgressResponse.LessonProgress savedProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        session = new SessionManager(this);
        lessonType = getIntent().getStringExtra("lesson_type");
        if (lessonType == null) {
            lessonType = "reading";
        }

        lessonId = getIntent().getIntExtra("lesson_id", -1);

        initializeViews();
        initializeGameSession();
        setupListeners();
        loadSavedProgress();
    }

    private void initializeViews() {
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonProgress = findViewById(R.id.tvLessonProgress);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        tvTotalXP = findViewById(R.id.tvTotalXP);
        tvAverageAccuracy = findViewById(R.id.tvAverageAccuracy);
        tvGameTitle = findViewById(R.id.tvGameTitle);
        tvGameDescription = findViewById(R.id.tvGameDescription);
        tvGameReward = findViewById(R.id.tvGameReward);
        ivBack = findViewById(R.id.ivBack);
        ivGameIcon = findViewById(R.id.ivGameIcon);
        progressBar = findViewById(R.id.progressBar);
        btnStartGame = findViewById(R.id.btnStartGame);
        cardNextGame = findViewById(R.id.cardNextGame);

        // Try to find loading progress if it exists
        loadingProgress = findViewById(R.id.loadingProgress);
    }

    private void initializeGameSession() {
        gameSession = new GameSession();
        gameSession.setStudentId(session.getStudentId());
        gameSession.setLessonType(lessonType);
        gameSession.setTotalGamesRequired(TOTAL_GAMES_REQUIRED);

        String title = getLessonTitle(lessonType);
        tvLessonTitle.setText(title);

        availableGames = new ArrayList<>(Arrays.asList(
                "sentence_scramble",
                "word_hunt"
                // Other games are coming soon
        ));
        Collections.shuffle(availableGames);
    }

    private void loadSavedProgress() {
        int studentId = session.getStudentId();
        if (studentId <= 0 || lessonId <= 0) {
            updateProgressDisplay();
            selectNextGame();
            return;
        }

        if (loadingProgress != null) {
            loadingProgress.setVisibility(View.VISIBLE);
        }
        btnStartGame.setEnabled(false);

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getLessonProgress(studentId, lessonId).enqueue(new Callback<LessonProgressResponse>() {
            @Override
            public void onResponse(Call<LessonProgressResponse> call, Response<LessonProgressResponse> response) {
                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<LessonProgressResponse.LessonProgress> lessons = response.body().getLessons();
                    if (lessons != null && !lessons.isEmpty()) {
                        savedProgress = lessons.get(0);

                        gamesPlayed = savedProgress.getGamesPlayed();
                        gamesPlayedAtStart = gamesPlayed;
                        gameSession.setGamesCompleted(gamesPlayed);
                        isLessonCompleted = savedProgress.isCompleted();

                        android.util.Log.d("LessonActivity", "Loaded progress: " + gamesPlayed +
                                " games, completed=" + isLessonCompleted + " for lesson " + lessonId);

                        // If lesson is completed, show summary instead of allowing replay
                        if (isLessonCompleted) {
                            showCompletedLessonSummary();
                            return;
                        }
                    }
                }

                updateProgressDisplay();
                selectNextGame();
            }

            @Override
            public void onFailure(Call<LessonProgressResponse> call, Throwable t) {
                if (loadingProgress != null) {
                    loadingProgress.setVisibility(View.GONE);
                }
                android.util.Log.e("LessonActivity", "Failed to load progress: " + t.getMessage());

                updateProgressDisplay();
                selectNextGame();
            }
        });
    }

    private void showCompletedLessonSummary() {
        // Update UI to show completed state
        updateProgressDisplay();

        // Hide game selection, show summary
        if (cardNextGame != null) {
            cardNextGame.setVisibility(View.GONE);
        }

        // Update button to show review option
        btnStartGame.setEnabled(false);
        btnStartGame.setText("Lesson Completed");

        // Show detailed stats dialog
        if (savedProgress != null) {
            String message = String.format(
                    "🎉 You've already completed this lesson!\n\n" +
                    "📊 Your Performance:\n" +
                    "• Games Played: %d\n" +
                    "• Total XP Earned: %d\n" +
                    "• Best Score: %d\n" +
                    "• Average Accuracy: %.1f%%\n" +
                    "• Total Time: %s\n\n" +
                    "Great job! Try another lesson to keep learning.",
                    savedProgress.getGamesPlayed(),
                    savedProgress.getTotalXpEarned(),
                    savedProgress.getBestScore(),
                    savedProgress.getAverageAccuracy(),
                    savedProgress.getFormattedTotalTime()
            );

            new AlertDialog.Builder(this)
                    .setTitle("✅ Lesson Complete")
                    .setMessage(message)
                    .setPositiveButton("Back to Dashboard", (dialog, which) -> finish())
                    .setCancelable(true)
                    .show();
        }

        // Update the game info area to show stats summary
        tvGameTitle.setText("Lesson Completed!");
        tvGameDescription.setText(String.format(
                "XP: %d • Accuracy: %.1f%% • Time: %s",
                savedProgress != null ? savedProgress.getTotalXpEarned() : 0,
                savedProgress != null ? savedProgress.getAverageAccuracy() : 0,
                savedProgress != null ? savedProgress.getFormattedTotalTime() : "0:00"
        ));
        tvGameReward.setText("Try another lesson to continue learning!");
    }

    private String getLessonTitle(String type) {
        switch (type) {
            case "reading":
                return "Reading Comprehension";
            case "vocabulary":
                return "Vocabulary Building";
            case "grammar":
                return "Grammar Practice";
            case "comprehension":
                return "Reading Comprehension";
            case "fluency":
                return "Fluency Practice";
            case "review":
                return "Review & Practice";
            default:
                return "Literacy Practice";
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> {
            if (gamesPlayed > gamesPlayedAtStart) {
                showExitConfirmation();
            } else {
                finish();
            }
        });

        btnStartGame.setOnClickListener(v -> launchGame());
    }

    private void selectNextGame() {
        if (gamesPlayed >= TOTAL_GAMES_REQUIRED) {
            completeLessonAndStartPostAssessment();
            return;
        }

        // Select random game from available pool
        Random random = new Random();
        currentGame = availableGames.get(random.nextInt(availableGames.size()));

        displayGameInfo(currentGame);
    }

    private void displayGameInfo(String gameType) {
        btnStartGame.setEnabled(true);

        switch (gameType) {
            case "sentence_scramble":
                tvGameTitle.setText("Sentence Scramble");
                tvGameDescription.setText("Drag words into the correct order to form sentences");
                tvGameReward.setText("⭐ Earn up to 500 XP");
                ivGameIcon.setImageResource(R.drawable.ic_edit);
                ivGameIcon.setColorFilter(getResources().getColor(R.color.color_jade1, null));
                btnStartGame.setText("Start Game");
                break;

            case "word_hunt":
                tvGameTitle.setText("Word Hunt");
                tvGameDescription.setText("Find hidden vocabulary words in the letter grid");
                tvGameReward.setText("⭐ Earn up to 450 XP");
                ivGameIcon.setImageResource(R.drawable.ic_lightbulb);
                ivGameIcon.setColorFilter(getResources().getColor(R.color.color_sunglow, null));
                btnStartGame.setText("Start Game");
                break;

            case "timed_trail":
                tvGameTitle.setText("Timed Trail");
                tvGameDescription.setText("Race through comprehension and pronunciation challenges");
                tvGameReward.setText("⭐ Earn up to 600 XP");
                ivGameIcon.setImageResource(R.drawable.ic_timer);
                ivGameIcon.setColorFilter(getResources().getColor(R.color.color_warning, null));
                btnStartGame.setEnabled(false);
                btnStartGame.setText("Coming Soon");
                break;

            case "shadow_read":
                tvGameTitle.setText("Shadow Read");
                tvGameDescription.setText("Read along with karaoke-style guided text");
                tvGameReward.setText("⭐ Earn up to 550 XP");
                ivGameIcon.setImageResource(R.drawable.ic_book_reading);
                ivGameIcon.setColorFilter(getResources().getColor(R.color.color_jade1, null));
                btnStartGame.setEnabled(false);
                btnStartGame.setText("Coming Soon");
                break;

            case "minimal_pairs":
                tvGameTitle.setText("Minimal Pairs");
                tvGameDescription.setText("Practice distinguishing similar-sounding words");
                tvGameReward.setText("⭐ Earn up to 500 XP");
                ivGameIcon.setImageResource(R.drawable.ic_mic);
                ivGameIcon.setColorFilter(getResources().getColor(R.color.color_info_bg, null));
                btnStartGame.setEnabled(false);
                btnStartGame.setText("Coming Soon");
                break;
        }
    }

    private void launchGame() {
        Intent intent = null;

        switch (currentGame) {
            case "sentence_scramble":
                intent = new Intent(this, SentenceScrambleActivity.class);
                break;

            case "word_hunt":
                intent = new Intent(this, WordHuntActivity.class);
                break;

            case "timed_trail":
            case "shadow_read":
            case "minimal_pairs":
                CustomToast.showInfo(this, "This game is coming soon!");
                return;

            default:
                CustomToast.showInfo(this, "Unknown game type");
                return;
        }

        if (intent != null) {
            intent.putExtra("lesson_type", lessonType);

            if (lessonId > 0) {
                intent.putExtra("lesson_id", lessonId);
            }

            startActivityForResult(intent, GAME_REQUEST_CODE);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GAME_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            int xpEarned = data.getIntExtra("xp_earned", 0);
            float accuracy = data.getIntExtra("accuracy", 0);

            totalXPEarned += xpEarned;
            totalAccuracy += accuracy;
            gamesPlayed++;

            gameSession.incrementGamesCompleted();
            gameSession.setTotalXpEarned(totalXPEarned);

            CustomToast.showSuccess(this, String.format("Great job! +%d XP", xpEarned));

            updateProgressDisplay();
            selectNextGame();
        }
    }

    private void updateProgressDisplay() {
        tvLessonProgress.setText(String.format("Game %d of %d", gamesPlayed, TOTAL_GAMES_REQUIRED));

        int progress = (int) (((float) gamesPlayed / TOTAL_GAMES_REQUIRED) * 100);
        gameSession.setGamesCompleted(gamesPlayed);

        tvProgressPercentage.setText(progress + "%");
        progressBar.setProgress(progress);

        tvTotalXP.setText(String.valueOf(totalXPEarned));

        float avgAccuracy = (gamesPlayed - gamesPlayedAtStart) > 0
                ? (totalAccuracy / (gamesPlayed - gamesPlayedAtStart))
                : 0;
        tvAverageAccuracy.setText(String.format("%.0f%%", avgAccuracy));
    }

    private void completeLessonAndStartPostAssessment() {
        gameSession.setCompleted(true);

        float finalAccuracy = (gamesPlayed - gamesPlayedAtStart) > 0
                ? (totalAccuracy / (gamesPlayed - gamesPlayedAtStart))
                : 0;
        gameSession.setAccuracyPercentage(finalAccuracy);

        new AlertDialog.Builder(this)
                .setTitle("🎉 Lesson Complete!")
                .setMessage("Congratulations! You've completed all " + TOTAL_GAMES_REQUIRED + " games.\n\n" +
                        "XP Earned: " + totalXPEarned + "\n" +
                        "Average Accuracy: " + String.format("%.0f%%", finalAccuracy))
                .setPositiveButton("Continue", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showExitConfirmation() {
        int gamesCompletedThisSession = gamesPlayed - gamesPlayedAtStart;
        new AlertDialog.Builder(this)
                .setTitle("Exit Lesson?")
                .setMessage("You've completed " + gamesCompletedThisSession + " game(s) this session.\n" +
                        "Total progress: " + gamesPlayed + "/" + TOTAL_GAMES_REQUIRED + " games.\n\n" +
                        "Your progress is saved automatically.")
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setNegativeButton("Continue", null)
                .show();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (gamesPlayed > gamesPlayedAtStart) {
            showExitConfirmation();
        } else {
            super.onBackPressed();
        }
    }
}
