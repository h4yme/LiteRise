package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.utils.TextToSpeechHelper;
import com.google.android.material.button.MaterialButton;

public class CategoryTransitionActivity extends AppCompatActivity {

    private TextView tvCategoryIcon, tvCategoryTitle, tvCategoryName, tvLeoMessage;
    private MaterialButton btnContinue;
    private TextToSpeechHelper ttsHelper;

    private int categoryNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_transition);

        initViews();
        getCategoryFromIntent();
        displayCategoryInfo();
        setupListeners();

        // Speak Leo's message
        new Handler().postDelayed(this::speakMessage, 500);
    }

    private void initViews() {
        tvCategoryIcon = findViewById(R.id.tvCategoryIcon);
        tvCategoryTitle = findViewById(R.id.tvCategoryTitle);
        tvCategoryName = findViewById(R.id.tvCategoryName);
        tvLeoMessage = findViewById(R.id.tvLeoMessage);
        btnContinue = findViewById(R.id.btnContinue);

        // Initialize TTS
        ttsHelper = new TextToSpeechHelper(this);
    }

    private void getCategoryFromIntent() {
        Intent intent = getIntent();
        categoryNumber = intent.getIntExtra("category_number", 2);
    }

    private void displayCategoryInfo() {
        String icon = "";
        String title = "";
        String name = "";
        String message = "";

        switch (categoryNumber) {
            case 1:
                icon = "📚";
                title = "Ready?";
                name = "Category 1: Oral Language";
                message = "Let's start with some fun questions about listening and speaking! You've got this! 🌟";
                break;
            case 2:
                icon = "🔤";
                title = "Great Job!";
                name = "Category 2: Word Knowledge";
                message = "Now let's test your word knowledge! You're doing amazing! 🌟";
                break;
            case 3:
                icon = "📖";
                title = "Awesome Work!";
                name = "Category 3: Reading Comprehension";
                message = "Time for some fun stories! Let's see how well you understand what you read! 📚";
                break;
            case 4:
                icon = "✏️";
                title = "Almost There!";
                name = "Category 4: Language Structure";
                message = "Last category! Let's work on grammar and sentences! You're doing wonderfully! 💪";
                break;
        }

        tvCategoryIcon.setText(icon);
        tvCategoryTitle.setText(title);
        tvCategoryName.setText(name);
        tvLeoMessage.setText(message);
    }

    private void speakMessage() {
        if (ttsHelper != null && ttsHelper.isInitialized()) {
            ttsHelper.speak(tvLeoMessage.getText().toString());
        }
    }

    private void setupListeners() {
        btnContinue.setOnClickListener(v -> {
            // Return to placement test
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ttsHelper != null) {
            ttsHelper.shutdown();
        }
    }

    @Override
    public void onBackPressed() {
        // Don't allow back during transition
    }
}
