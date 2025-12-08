package com.example.literise.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.literise.R;
import com.example.literise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.List;

public class DialogueReadingActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvProgress;
    private RecyclerView recyclerDialogue;
    private MaterialButton btnComplete;
    private DialogueAdapter adapter;
    private List<DialogueLine> dialogueLines;
    private int linesRead = 0;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue_reading);

        session = new SessionManager(this);
        initializeViews();
        setupDialogue();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvProgress = findViewById(R.id.tvProgress);
        recyclerDialogue = findViewById(R.id.recyclerDialogue);
        btnComplete = findViewById(R.id.btnComplete);
    }

    private void setupDialogue() {
        dialogueLines = new ArrayList<>();

        // Simple dialogue: Meeting a friend
        addDialogueLine("Anna", "👧", "Hello! How are you today?");
        addDialogueLine("Ben", "👦", "Hi Anna! I'm doing great, thanks!");
        addDialogueLine("Anna", "👧", "Would you like to play with me?");
        addDialogueLine("Ben", "👦", "Yes! That sounds fun!");
        addDialogueLine("Anna", "👧", "Great! Let's go to the park!");
        addDialogueLine("Ben", "👦", "Okay! I'll get my ball.");

        adapter = new DialogueAdapter(dialogueLines);
        recyclerDialogue.setLayoutManager(new LinearLayoutManager(this));
        recyclerDialogue.setAdapter(adapter);

        updateProgress();
    }

    private void addDialogueLine(String speaker, String avatar, String text) {
        dialogueLines.add(new DialogueLine(speaker, avatar, text));
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnComplete.setOnClickListener(v -> checkCompletion());
    }

    private void onDialogueLineClicked(DialogueLine line, int position) {
        if (line.isRead) {
            return; // Already read
        }

        // Mark as read
        line.isRead = true;
        linesRead++;
        adapter.notifyItemChanged(position);
        updateProgress();

        // Animate the card
        RecyclerView.ViewHolder holder = recyclerDialogue.findViewHolderForAdapterPosition(position);
        if (holder != null) {
            View itemView = holder.itemView;
            itemView.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(150)
                    .withEndAction(() -> {
                        itemView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150);
                    });
        }

        // Auto-scroll to next line
        if (position < dialogueLines.size() - 1) {
            recyclerDialogue.smoothScrollToPosition(position + 1);
        }
    }

    private void updateProgress() {
        tvProgress.setText("Read: " + linesRead + "/" + dialogueLines.size());
    }

    private void checkCompletion() {
        boolean allRead = linesRead == dialogueLines.size();

        if (!allRead) {
            // Show hint to read all lines
            showHintDialog();
            return;
        }

        // Calculate XP and stars
        int xpEarned = 50;
        int stars = 3;

        // Update session XP
        int currentXP = session.getXP();
        session.saveXP(currentXP + xpEarned);

        showResultDialog(true, dialogueLines.size(), xpEarned, stars);
    }

    private void showHintDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Not Yet! 📖")
                .setMessage("Please tap and read all dialogue lines before completing.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showResultDialog(boolean isComplete, int totalLines, int xpEarned, int stars) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_result, null);

        ImageView ivTrophy = dialogView.findViewById(R.id.ivTrophy);
        TextView tvResultTitle = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvResultScore = dialogView.findViewById(R.id.tvResultScore);
        TextView tvResultAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);
        TextView tvResultXP = dialogView.findViewById(R.id.tvResultXP);
        TextView tvResultStreak = dialogView.findViewById(R.id.tvResultStreak);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        tvResultTitle.setText("EXCELLENT READING! 🎉📚");

        // Animate trophy
        ivTrophy.animate()
                .rotation(360f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .withEndAction(() -> ivTrophy.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(300));

        // Show star rating
        String starDisplay = "";
        for (int i = 0; i < 3; i++) {
            if (i < stars) {
                starDisplay += "⭐";
            } else {
                starDisplay += "☆";
            }
        }
        tvResultStreak.setText(starDisplay);

        tvResultScore.setText(totalLines + " lines read");
        tvResultAccuracy.setText("100%");
        tvResultXP.setText("+" + xpEarned + " XP");

        // Animate stats
        tvResultScore.setAlpha(0f);
        tvResultAccuracy.setAlpha(0f);
        tvResultXP.setAlpha(0f);

        tvResultScore.animate().alpha(1f).setDuration(400).setStartDelay(200);
        tvResultAccuracy.animate().alpha(1f).setDuration(400).setStartDelay(400);
        tvResultXP.animate().alpha(1f).setDuration(400).setStartDelay(600);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    // DialogueLine model class
    private static class DialogueLine {
        String speaker;
        String avatar;
        String text;
        boolean isRead;

        DialogueLine(String speaker, String avatar, String text) {
            this.speaker = speaker;
            this.avatar = avatar;
            this.text = text;
            this.isRead = false;
        }
    }

    // Dialogue Adapter
    private class DialogueAdapter extends RecyclerView.Adapter<DialogueAdapter.ViewHolder> {
        private List<DialogueLine> lines;

        DialogueAdapter(List<DialogueLine> lines) {
            this.lines = lines;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dialogue_line, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DialogueLine line = lines.get(position);

            holder.tvSpeaker.setText(line.speaker);
            holder.tvAvatar.setText(line.avatar);
            holder.tvDialogueText.setText(line.text);

            if (line.isRead) {
                // Read state - green background
                holder.cardDialogue.setCardBackgroundColor(0xFFE8F5E9);
                holder.cardDialogue.setStrokeColor(0xFF4CAF50);
                holder.cardDialogue.setStrokeWidth(3);
                holder.ivCheckmark.setVisibility(View.VISIBLE);
            } else {
                // Unread state - white background
                holder.cardDialogue.setCardBackgroundColor(0xFFFFFFFF);
                holder.cardDialogue.setStrokeColor(0xFFE0E0E0);
                holder.cardDialogue.setStrokeWidth(2);
                holder.ivCheckmark.setVisibility(View.GONE);
            }

            holder.cardDialogue.setOnClickListener(v -> {
                if (!line.isRead) {
                    // Tap animation
                    v.animate()
                            .scaleX(0.98f)
                            .scaleY(0.98f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100);
                            });

                    onDialogueLineClicked(line, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return lines.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardDialogue;
            TextView tvAvatar;
            TextView tvSpeaker;
            TextView tvDialogueText;
            ImageView ivCheckmark;

            ViewHolder(View itemView) {
                super(itemView);
                cardDialogue = itemView.findViewById(R.id.cardDialogue);
                tvAvatar = itemView.findViewById(R.id.tvAvatar);
                tvSpeaker = itemView.findViewById(R.id.tvSpeaker);
                tvDialogueText = itemView.findViewById(R.id.tvDialogueText);
                ivCheckmark = itemView.findViewById(R.id.ivCheckmark);
            }
        }
    }
}
