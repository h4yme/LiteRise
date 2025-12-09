package com.example.literise.activities.games;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FillInTheBlanksActivity extends BaseGameActivity {

    private ImageView btnBack;
    private LinearLayout sentenceContainer;
    private RecyclerView recyclerWordOptions;
    private MaterialButton btnCheckAnswer;
    private WordOptionAdapter adapter;
    private List<WordOption> wordOptions;
    private List<BlankSpace> blankSpaces;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_in_the_blanks);

        session = new SessionManager(this);
        initializeViews();
        setupSentence();
        setupWordOptions();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        sentenceContainer = findViewById(R.id.sentenceContainer);
        recyclerWordOptions = findViewById(R.id.recyclerWordOptions);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
    }

    private void setupSentence() {
        blankSpaces = new ArrayList<>();

        // Example sentence: "The cat is ____ on the ____."
        // Correct words: "sitting", "mat"

        addWord("The");
        addWord("cat");
        addWord("is");
        addBlank(0, "sitting");
        addWord("on");
        addWord("the");
        addBlank(1, "mat");
        addWord(".");
    }

    private void addWord(String word) {
        TextView textView = new TextView(this);
        textView.setText(word + " ");
        textView.setTextSize(18);
        textView.setTextColor(0xFF2D3748);
        textView.setPadding(4, 4, 4, 4);
        try {
            textView.setTypeface(ResourcesCompat.getFont(this, R.font.poppins));
        } catch (Exception e) {
            // Font not found, use default
        }
        sentenceContainer.addView(textView);
    }

    private void addBlank(int index, String correctWord) {
        View blankView = LayoutInflater.from(this).inflate(R.layout.item_blank_space, sentenceContainer, false);
        TextView tvBlank = blankView.findViewById(R.id.tvBlank);

        BlankSpace blank = new BlankSpace(index, correctWord, tvBlank);
        blankSpaces.add(blank);

        tvBlank.setOnClickListener(v -> {
            if (blank.selectedWord != null) {
                // Clear the blank
                clearBlank(blank);
            }
        });

        sentenceContainer.addView(blankView);
    }

    private void setupWordOptions() {
        wordOptions = new ArrayList<>();

        // Add correct words
        wordOptions.add(new WordOption("sitting", true));
        wordOptions.add(new WordOption("mat", true));

        // Add wrong words
        wordOptions.add(new WordOption("running", false));
        wordOptions.add(new WordOption("hat", false));
        wordOptions.add(new WordOption("standing", false));
        wordOptions.add(new WordOption("cat", false));

        // Shuffle
        Collections.shuffle(wordOptions);

        adapter = new WordOptionAdapter(wordOptions);
        recyclerWordOptions.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerWordOptions.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCheckAnswer.setOnClickListener(v -> checkAnswer());
    }

    private void onWordOptionClicked(WordOption wordOption, int position) {
        if (wordOption.isUsed) {
            return; // Already used
        }

        // Find first empty blank
        for (BlankSpace blank : blankSpaces) {
            if (blank.selectedWord == null) {
                fillBlank(blank, wordOption, position);
                return;
            }
        }
    }

    private void fillBlank(BlankSpace blank, WordOption wordOption, int wordPosition) {
        // Animate word option disappearing
        View wordView = recyclerWordOptions.getLayoutManager().findViewByPosition(wordPosition);
        if (wordView != null) {
            wordView.animate()
                    .alpha(0.3f)
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(200);
        }

        // Fill the blank
        blank.selectedWord = wordOption.word;
        blank.textView.setText(wordOption.word);
        blank.textView.setBackgroundResource(R.drawable.bg_blank_filled);

        // Animate blank
        blank.textView.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(150)
                .withEndAction(() -> {
                    blank.textView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150);
                });

        wordOption.isUsed = true;
        adapter.notifyItemChanged(wordPosition);
    }

    private void clearBlank(BlankSpace blank) {
        // Find the word option to restore
        for (int i = 0; i < wordOptions.size(); i++) {
            WordOption option = wordOptions.get(i);
            if (option.word.equals(blank.selectedWord)) {
                option.isUsed = false;
                adapter.notifyItemChanged(i);

                View wordView = recyclerWordOptions.getLayoutManager().findViewByPosition(i);
                if (wordView != null) {
                    wordView.animate()
                            .alpha(1f)
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200);
                }
                break;
            }
        }

        // Clear the blank
        blank.selectedWord = null;
        blank.textView.setText("______");
        blank.textView.setBackgroundResource(R.drawable.bg_blank_space);
    }

    private void checkAnswer() {
        boolean isCorrect = true;
        int correctCount = 0;

        for (BlankSpace blank : blankSpaces) {
            if (blank.selectedWord != null && blank.selectedWord.equals(blank.correctWord)) {
                correctCount++;
                // Correct - green background
                blank.textView.setBackgroundResource(R.drawable.bg_blank_correct);

                // Bounce animation
                blank.textView.animate()
                        .scaleX(1.15f)
                        .scaleY(1.15f)
                        .setDuration(150)
                        .withEndAction(() -> {
                            blank.textView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(150);
                        });
            } else {
                isCorrect = false;
                // Wrong - red background and shake
                blank.textView.setBackgroundResource(R.drawable.bg_blank_wrong);
                shakeView(blank.textView);
            }
        }

        // Calculate XP and stars
        int xpEarned = 0;
        int stars = 0;
        int total = blankSpaces.size();

        if (isCorrect) {
            xpEarned = 50;
            stars = 3;
        } else if (correctCount >= (total * 0.75)) {
            xpEarned = correctCount * 10;
            stars = 2;
        } else if (correctCount >= (total * 0.5)) {
            xpEarned = correctCount * 10;
            stars = 1;
        } else {
            xpEarned = correctCount * 5;
            stars = 0;
        }

        // Update session XP
        int currentXP = session.getXP();
        session.saveXP(currentXP + xpEarned);

        // Make variables final for lambda
        final boolean finalIsCorrect = isCorrect;
        final int finalCorrectCount = correctCount;
        final int finalTotal = total;
        final int finalXpEarned = xpEarned;
        final int finalStars = stars;

        // Show result dialog after animations
        sentenceContainer.postDelayed(() -> {
            showResultDialog(finalIsCorrect, finalCorrectCount, finalTotal, finalXpEarned, finalStars);
        }, 800);
    }

    private void shakeView(View view) {
        view.animate()
                .translationX(-15f)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(15f)
                        .setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(-15f)
                                .setDuration(50)
                                .withEndAction(() -> view.animate()
                                        .translationX(15f)
                                        .setDuration(50)
                                        .withEndAction(() -> view.animate()
                                                .translationX(0f)
                                                .setDuration(50)))));
    }

    private void showResultDialog(boolean isCorrect, int correctCount, int total, int xpEarned, int stars) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_result, null);

        ImageView ivTrophy = dialogView.findViewById(R.id.ivTrophy);
        TextView tvResultTitle = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvResultScore = dialogView.findViewById(R.id.tvResultScore);
        TextView tvResultAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);
        TextView tvResultXP = dialogView.findViewById(R.id.tvResultXP);
        TextView tvResultStreak = dialogView.findViewById(R.id.tvResultStreak);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        int accuracy = (int) ((correctCount * 100.0) / total);

        if (isCorrect) {
            tvResultTitle.setText("PERFECT! 🎉✨");
            ivTrophy.animate()
                    .rotation(360f)
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(500)
                    .withEndAction(() -> ivTrophy.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(300));
        } else if (accuracy >= 75) {
            tvResultTitle.setText("Great Job! 🌟");
        } else if (accuracy >= 50) {
            tvResultTitle.setText("Good Effort! 👍");
        } else {
            tvResultTitle.setText("Keep Practicing! 💪");
        }

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

        tvResultScore.setText(correctCount + "/" + total);
        tvResultAccuracy.setText(accuracy + "%");
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
            if (isCorrect) {
                finish();
            } else {
                resetGame();
            }
        });

        dialog.show();
    }

    private void resetGame() {
        // Clear all blanks
        for (BlankSpace blank : blankSpaces) {
            if (blank.selectedWord != null) {
                clearBlank(blank);
            }
            blank.textView.setBackgroundResource(R.drawable.bg_blank_space);
        }

        // Reset all word options
        for (int i = 0; i < wordOptions.size(); i++) {
            wordOptions.get(i).isUsed = false;
        }
        adapter.notifyDataSetChanged();
    }

    // BlankSpace model class
    private static class BlankSpace {
        int index;
        String correctWord;
        String selectedWord;
        TextView textView;

        BlankSpace(int index, String correctWord, TextView textView) {
            this.index = index;
            this.correctWord = correctWord;
            this.textView = textView;
        }
    }

    // WordOption model class
    private static class WordOption {
        String word;
        boolean isCorrect;
        boolean isUsed;

        WordOption(String word, boolean isCorrect) {
            this.word = word;
            this.isCorrect = isCorrect;
            this.isUsed = false;
        }
    }

    // Adapter for word options
    private class WordOptionAdapter extends RecyclerView.Adapter<WordOptionAdapter.ViewHolder> {

        private List<WordOption> options;

        WordOptionAdapter(List<WordOption> options) {
            this.options = options;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_word_option, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            WordOption option = options.get(position);
            holder.tvWord.setText(option.word);

            if (option.isUsed) {
                holder.cardWordOption.setAlpha(0.3f);
                holder.cardWordOption.setCardBackgroundColor(0xFFE0E0E0);
            } else {
                holder.cardWordOption.setAlpha(1f);
                holder.cardWordOption.setCardBackgroundColor(0xFFFFFFFF);
            }

            holder.cardWordOption.setOnClickListener(v -> {
                if (!option.isUsed) {
                    // Tap animation
                    v.animate()
                            .scaleX(0.95f)
                            .scaleY(0.95f)
                            .setDuration(100)
                            .withEndAction(() -> {
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100);
                            });

                    onWordOptionClicked(option, position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return options.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardWordOption;
            TextView tvWord;

            ViewHolder(View itemView) {
                super(itemView);
                cardWordOption = itemView.findViewById(R.id.cardWordOption);
                tvWord = itemView.findViewById(R.id.tvWord);
            }
        }
    }
}
