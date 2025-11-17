package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.example.literise.models.GameSession;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LessonActivity extends AppCompatActivity {

    private static final int GAME_REQUEST_CODE = 100;
    private static final int TOTAL_GAMES_REQUIRED = 5;

    private TextView tvLessonTitle, tvLessonProgress, tvProgressPercentage;
    private TextView tvTotalXP, tvAverageAccuracy;
    private TextView tvGameTitle, tvGameDescription, tvGameReward;
    private ImageView ivBack, ivGameIcon;
    private ProgressBar progressBar;
    private MaterialButton btnStartGame;
    private CardView cardNextGame;

    private GameSession gameSession;
    private SessionManager session;
    private String lessonType;

    private List<String> availableGames;
    private String currentGame;
    private int totalXPEarned = 0;
    private float totalAccuracy = 0;
    private int gamesPlayed = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        session = new SessionManager(this);
        lessonType = getIntent().getStringExtra("lesson_type");
        if (lessonType == null) {
            lessonType = "reading";
        }

        initializeViews();
        initializeGameSession();
        setupListeners();
        selectNextGame();
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
    }

    private void initializeGameSession() {
        gameSession = new GameSession();
        gameSession.setStudentId(session.getStudentId());
        gameSession.setLessonType(lessonType);
        gameSession.setTotalGamesRequired(TOTAL_GAMES_REQUIRED);

        // Set lesson title
        String title = getLessonTitle(lessonType);
        tvLessonTitle.setText(title);

        // Initialize available games pool (randomized)
        availableGames = new ArrayList<>(Arrays.asList(
                "sentence_scramble",
                "sentence_scramble", // More weight to implemented game
                "timed_trail",       // Coming soon
                "word_hunt",         // Coming soon
                "shadow_read",       // Coming soon
                "minimal_pairs"      // Coming soon
        ));
        Collections.shuffle(availableGames);

        updateProgressDisplay();
    }

    private String getLessonTitle(String type) {
        switch (type) {
            case "reading":
                return "Reading Comprehension";
            case "vocabulary":
                return "Vocabulary Building";
            case "grammar":
                return "Grammar Practice";
            default:
                return "Literacy Practice";
        }
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> {
            if (gamesPlayed > 0) {
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
        switch (gameType) {
            case "sentence_scramble":
                tvGameTitle.setText("Sentence Scramble");
                tvGameDescription.setText("Drag words into the correct order to form sentences");
                tvGameReward.setText("⭐ Earn up to 500 XP");
                ivGameIcon.setImageResource(R.drawable.ic_edit);
                ivGameIcon.setColorFilter(getResources().getColor(R.color.color_jade1, null));
                btnStartGame.setEnabled(true);
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

            case "word_hunt":
                tvGameTitle.setText("Word Hunt");
                tvGameDescription.setText("Find hidden words in the letter grid");
                tvGameReward.setText("⭐ Earn up to 450 XP");
                ivGameIcon.setImageResource(R.drawable.ic_lightbulb);
                ivGameIcon.setColorFilter(getResources().getColor(R.color.color_sunglow, null));
                btnStartGame.setEnabled(false);
                btnStartGame.setText("Coming Soon");
                break;

            case "shadow_read":
                tvGameTitle.setText("Shadow Read");
                tvGameDescription.setText("Read along with karaoke-style guided text");
                tvGameReward.setText("⭐ Earn up to 550 XP");
                ivGameIcon.setImageResource(R.drawable.ic_book);
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
                intent.putExtra("lesson_type", lessonType);
                break;

            // Add other game activities when implemented
            case "timed_trail":
            case "word_hunt":
            case "shadow_read":
            case "minimal_pairs":
                CustomToast.showInfo(this, "This game is coming soon!");
                return;
        }

        if (intent != null) {
            startActivityForResult(intent, GAME_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GAME_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Get game results
            int xpEarned = data.getIntExtra("xp_earned", 0);
            float accuracy = data.getFloatExtra("accuracy", 0);

            // Update totals
            totalXPEarned += xpEarned;
            totalAccuracy += accuracy;
            gamesPlayed++;

            // Update game session
            gameSession.incrementGamesCompleted();
            gameSession.setTotalXpEarned(totalXPEarned);

            // Show quick feedback
            CustomToast.showSuccess(this, String.format("Great job! +%d XP", xpEarned));

            // Update display
            updateProgressDisplay();

            // Select next game
            selectNextGame();
        }
    }

    private void updateProgressDisplay() {
        tvLessonProgress.setText(String.format("Game %d of %d", gamesPlayed, TOTAL_GAMES_REQUIRED));

        int progress = gameSession.getProgress();
        tvProgressPercentage.setText(progress + "%");
        progressBar.setProgress(progress);

        tvTotalXP.setText(String.valueOf(totalXPEarned));

        float avgAccuracy = gamesPlayed > 0 ? (totalAccuracy / gamesPlayed) : 0;
        tvAverageAccuracy.setText(String.format("%.0f%%", avgAccuracy));
    }

    private void completeLessonAndStartPostAssessment() {
        // Mark lesson as complete
        gameSession.setCompleted(true);

        float finalAccuracy = gamesPlayed > 0 ? (totalAccuracy / gamesPlayed) : 0;
        gameSession.setAccuracyPercentage(finalAccuracy);

        // Show completion dialog
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_lesson_complete, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        // Set data in dialog
        TextView tvDialogXP = dialogView.findViewById(R.id.tvDialogTotalXP);
        TextView tvDialogAccuracy = dialogView.findViewById(R.id.tvDialogAccuracy);
        MaterialButton btnContinue = dialogView.findViewById(R.id.btnDialogContinue);

        tvDialogXP.setText(String.valueOf(totalXPEarned));
        tvDialogAccuracy.setText(String.format("%.1f%%", finalAccuracy));

        btnContinue.setOnClickListener(v -> {
            dialog.dismiss();
            // TODO: Launch Post Assessment
            CustomToast.showSuccess(this, "Lesson Complete! Post-assessment coming soon.");
            finish();
        });

        dialog.show();
    }

    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Exit Lesson?")
                .setMessage("You've completed " + gamesPlayed + " out of " + TOTAL_GAMES_REQUIRED + " games. Your progress will be saved.")
                .setPositiveButton("Exit", (dialog, which) -> finish())
                .setNegativeButton("Continue", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (gamesPlayed > 0) {
            showExitConfirmation();
        } else {
            super.onBackPressed();
        }
    }
}
