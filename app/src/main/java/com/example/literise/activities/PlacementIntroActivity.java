package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.views.LeoDialogueView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class PlacementIntroActivity extends AppCompatActivity {

    private RelativeLayout rootLayout;
    private MaterialButton btnStartPlacement;
    private LeoDialogueView dialogueView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placement_intro);

        rootLayout = findViewById(R.id.rootLayout);
        btnStartPlacement = findViewById(R.id.btnStartPlacement);

        // Start Leo's tutorial dialogue when button is clicked
        btnStartPlacement.setOnClickListener(v -> showLeoTutorial());
    }

    private void showLeoTutorial() {
        // Create Leo's dialogue messages
        List<LeoDialogueView.DialogueMessage> messages = new ArrayList<>();

        // Message 1: Introduction
        messages.add(new LeoDialogueView.DialogueMessage(
                "Hi! I'm Leo, your reading buddy! 👋\n\nI'm so excited to start this reading adventure with you!"
        ));

        // Message 2: Purpose of placement test
        messages.add(new LeoDialogueView.DialogueMessage(
                "Before we start, I need to know your reading level.\n\nThis helps me find the PERFECT stories just for you! 📚"
        ));

        // Message 3: What is placement test
        messages.add(new LeoDialogueView.DialogueMessage(
                "The Placement Test is like a fun game!\n\nI'll ask you questions about:\n• Listening to stories 🎧\n• Reading words 📖\n• Understanding what you read 🤔"
        ));

        // Message 4: How it works
        messages.add(new LeoDialogueView.DialogueMessage(
                "Here's how it works:\n\n1️⃣ I'll show you 20-25 questions\n2️⃣ Questions get easier or harder based on your answers\n3️⃣ Take your time - no rushing!\n4️⃣ You can skip if you're not sure\n\nIt takes about 15 minutes!"
        ));

        // Message 5: Question types
        messages.add(new LeoDialogueView.DialogueMessage(
                "You'll see 4 types of questions:\n\n🎧 Listening - Hear a story\n🔤 Words - Read and match words\n📖 Reading - Read short stories\n✏️ Grammar - Fix sentences\n\nAll questions are FUN and COLORFUL!"
        ));

        // Message 6: Encouragement
        messages.add(new LeoDialogueView.DialogueMessage(
                "Remember:\n\n✅ Do your BEST\n✅ Don't rush\n✅ It's okay to make mistakes\n✅ I'm here to help!\n\nLet's find your perfect reading level! 🌟"
        ));

        // Create and show dialogue view
        dialogueView = new LeoDialogueView(this);
        dialogueView.setDialogueMessages(messages);
        dialogueView.setDialogueCompleteListener(() -> {
            // When Leo finishes talking, start the placement test
            startPlacementTest();
        });
        dialogueView.show((ViewGroup) rootLayout);
    }

    private void startPlacementTest() {
        // Navigate to Placement Test Activity
        Intent intent = new Intent(PlacementIntroActivity.this, PlacementTestActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Don't allow back during placement intro
        // User must complete placement test
    }
}
