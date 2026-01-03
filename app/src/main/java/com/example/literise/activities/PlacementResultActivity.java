package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.views.LeoDialogueView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PlacementResultActivity extends AppCompatActivity {

    private RelativeLayout rootLayout;
    private TextView tvLevelName, tvLevelNumber;
    private TextView tvAccuracy, tvQuestionsAnswered;
    private TextView tvCategory1Score, tvCategory2Score, tvCategory3Score, tvCategory4Score;
    private MaterialButton btnContinueToDashboard;

    // Placement results data
    private int placementLevel;
    private String levelName;
    private double accuracy;
    private int totalAnswered;
    private int totalCorrect;
    private int[] categoryScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_result);

        initViews();
        getResultsFromIntent();
        displayResults();
        
        // Show Leo's congratulation dialogue after a brief delay
        new Handler().postDelayed(this::showLeoCongratulation, 1000);

        setupListeners();
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        tvLevelName = findViewById(R.id.tvLevelName);
        tvLevelNumber = findViewById(R.id.tvLevelNumber);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        tvQuestionsAnswered = findViewById(R.id.tvQuestionsAnswered);
        tvCategory1Score = findViewById(R.id.tvCategory1Score);
        tvCategory2Score = findViewById(R.id.tvCategory2Score);
        tvCategory3Score = findViewById(R.id.tvCategory3Score);
        tvCategory4Score = findViewById(R.id.tvCategory4Score);
        btnContinueToDashboard = findViewById(R.id.btnContinueToDashboard);
    }

    private void getResultsFromIntent() {
        Intent intent = getIntent();
        placementLevel = intent.getIntExtra("placement_level", 3);
        levelName = intent.getStringExtra("level_name");
        accuracy = intent.getDoubleExtra("accuracy", 0.0);
        totalAnswered = intent.getIntExtra("total_answered", 0);
        totalCorrect = intent.getIntExtra("total_correct", 0);
        categoryScores = intent.getIntArrayExtra("category_scores");

        // Default values if not provided
        if (levelName == null || levelName.isEmpty()) {
            levelName = getLevelNameFromLevel(placementLevel);
        }
        if (categoryScores == null) {
            categoryScores = new int[]{0, 0, 0, 0};
        }
    }

    private String getLevelNameFromLevel(int level) {
        switch (level) {
            case 1: return "Beginner Reader";
            case 2: return "Early Reader";
            case 3: return "Developing Reader";
            case 4: return "Fluent Reader";
            default: return "Developing Reader";
        }
    }

    private void displayResults() {
        // Display reading level
        tvLevelName.setText(levelName);
        tvLevelNumber.setText("Level " + placementLevel);

        // Display accuracy
        tvAccuracy.setText(String.format("%.0f%%", accuracy));

        // Display questions answered
        tvQuestionsAnswered.setText(totalAnswered + "/" + 25);

        // Display category scores
        tvCategory1Score.setText(categoryScores[0] + "%");
        tvCategory2Score.setText(categoryScores[1] + "%");
        tvCategory3Score.setText(categoryScores[2] + "%");
        tvCategory4Score.setText(categoryScores[3] + "%");
    }

    private void showLeoCongratulation() {
        List<LeoDialogueView.DialogueMessage> messages = new ArrayList<>();

        // Congratulation message based on level
        String congratsMessage = getCongratsMessage(placementLevel);
        messages.add(new LeoDialogueView.DialogueMessage(congratsMessage));

        // Encouraging message
        String encouragementMessage = getEncouragementMessage(placementLevel);
        messages.add(new LeoDialogueView.DialogueMessage(encouragementMessage));

        // Next steps message
        messages.add(new LeoDialogueView.DialogueMessage(
                "Now, let's start your reading adventure!\n\n" +
                "I've picked the perfect stories for your level. " +
                "You'll get better and better with each story you read! 📚✨"
        ));

        // Create and show dialogue view
        LeoDialogueView dialogueView = new LeoDialogueView(this);
        dialogueView.setDialogueMessages(messages);
        dialogueView.setDialogueCompleteListener(() -> {
            // Dialogue complete - user can now proceed
        });
        dialogueView.show((ViewGroup) rootLayout);
    }

    private String getCongratsMessage(int level) {
        switch (level) {
            case 1:
                return "🌟 Wonderful job!\n\n" +
                       "You're a Beginner Reader! " +
                       "You're just starting your reading journey, and that's amazing! " +
                       "Every great reader starts right where you are! 🎉";
            case 2:
                return "🌟 Fantastic work!\n\n" +
                       "You're an Early Reader! " +
                       "You're building your reading skills beautifully! " +
                       "Keep up the great work! 🎉";
            case 3:
                return "🌟 Excellent job!\n\n" +
                       "You're a Developing Reader! " +
                       "You're making wonderful progress in your reading! " +
                       "You're doing so well! 🎉";
            case 4:
                return "🌟 Outstanding!\n\n" +
                       "You're a Fluent Reader! " +
                       "Wow! You're reading really well! " +
                       "I'm so proud of you! 🎉";
            default:
                return "🌟 Great job completing the test! 🎉";
        }
    }

    private String getEncouragementMessage(int level) {
        switch (level) {
            case 1:
                return "I'll be with you every step of the way! 💪\n\n" +
                       "We'll start with fun, simple stories that are just right for you. " +
                       "Remember, every reader was once a beginner!";
            case 2:
                return "You're growing as a reader! 💪\n\n" +
                       "I've selected stories that will challenge you just the right amount. " +
                       "Keep reading, and you'll be amazed at how much you'll learn!";
            case 3:
                return "You're becoming a strong reader! 💪\n\n" +
                       "The stories I've picked will help you become even better! " +
                       "You're on your way to becoming a fluent reader!";
            case 4:
                return "You're an amazing reader! 💪\n\n" +
                       "I've found exciting stories that will keep challenging you! " +
                       "Keep reading and exploring new books!";
            default:
                return "Let's keep learning together! 💪";
        }
    }

    private void setupListeners() {
        btnContinueToDashboard.setOnClickListener(v -> {
            // Navigate to Dashboard
            Intent intent = new Intent(PlacementResultActivity.this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        // Don't allow back - user must continue to dashboard
    }
}
