package com.example.literise.activities.games;



import android.os.Bundle;

import android.os.Handler;

import android.view.LayoutInflater;

import android.view.View;

import android.view.ViewGroup;

import android.widget.ImageView;

import android.widget.TextView;

import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;

import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.literise.R;

import com.example.literise.api.ApiClient;

import com.example.literise.api.ApiService;

import com.example.literise.database.SessionManager;

import com.example.literise.models.GameContentRequest;

import com.example.literise.models.GameContentResponse;
import com.example.literise.models.LessonContentResponse;

import com.google.android.material.button.MaterialButton;

import com.google.android.material.card.MaterialCardView;

import com.google.gson.JsonArray;

import com.google.gson.JsonObject;

import java.util.ArrayList;

import java.util.Collections;

import java.util.List;

import retrofit2.Call;

import retrofit2.Callback;

import retrofit2.Response;



public class PictureMatchActivity extends BaseGameActivity {



    private ImageView btnBack;

    private TextView tvMatchCounter, tvTimer, tvCombo;

    private RecyclerView recyclerPictures, recyclerWords;

    private MaterialButton btnCheckAnswer;

    private PictureAdapter pictureAdapter;

    private WordAdapter wordAdapter;

    private List<MatchItem> pictures;

    private List<MatchItem> words;

    private MatchItem selectedPicture;

    private int matchedCount = 0;

    private int comboCount = 0;

    private SessionManager session;

    private LottieAnimationView lottieCorrect, lottieComplete;

    private Handler timerHandler = new Handler();

    private long startTime;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_picture_match);



        session = new SessionManager(this);

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
                                setupMatchData();
                                setupRecyclerViews();
                                startTimer();
                            }
                        }
                        @Override
                        public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                            setupMatchData();
                            setupRecyclerViews();
                            startTimer();
                        }
                    });
        } else {
            setupMatchData();
            setupRecyclerViews();
            startTimer();
        }

    }

    private void generateWithAI(int nodeId, String lessonContent) {
        ApiService apiService = ApiClient.getAiClient(this).create(ApiService.class);
        GameContentRequest request = new GameContentRequest(nodeId, "picture_match", lessonContent);
        apiService.generateGameContent(request).enqueue(new Callback<GameContentResponse>() {
            @Override
            public void onResponse(Call<GameContentResponse> call, Response<GameContentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success
                        && response.body().content != null) {
                    try {
                        JsonArray itemsArray = response.body().content.getAsJsonArray("items");
                        List<MatchItem> aiPictures = new ArrayList<>();
                        List<MatchItem> aiWords = new ArrayList<>();
                        for (int i = 0; i < itemsArray.size(); i++) {
                            JsonObject obj = itemsArray.get(i).getAsJsonObject();
                            String word = obj.get("word").getAsString();
                            String emoji = obj.has("emoji") ? obj.get("emoji").getAsString() : "❓";
                            int id = i + 1;
                            aiPictures.add(new MatchItem(id, emoji, word));
                            aiWords.add(new MatchItem(id, emoji, word));
                        }
                        if (!aiPictures.isEmpty()) {
                            pictures = aiPictures;
                            words = aiWords;
                            Collections.shuffle(words);
                            setupRecyclerViews();
                            startTimer();
                            return;
                        }
                    } catch (Exception e) {
                        android.util.Log.w("PictureMatch", "AI parse error: " + e.getMessage());
                    }
                } else {
                    android.util.Log.w("PictureMatch", "AI generate failed: code=" + response.code()
                            + " msg=" + (response.body() != null ? response.body().message : "null"));
                }
                setupMatchData();
                setupRecyclerViews();
                startTimer();
            }

            @Override
            public void onFailure(Call<GameContentResponse> call, Throwable t) {
                android.util.Log.w("PictureMatch", "AI generate network error: " + t.getMessage());
                setupMatchData();
                setupRecyclerViews();
                startTimer();
            }
        });
    }



    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        tvMatchCounter = findViewById(R.id.tvMatchCounter);

        tvTimer = findViewById(R.id.tvTimer);

        tvCombo = findViewById(R.id.tvCombo);

        recyclerPictures = findViewById(R.id.recyclerPictures);

        recyclerWords = findViewById(R.id.recyclerWords);

        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);

        lottieCorrect = findViewById(R.id.lottieCorrect);

        lottieComplete = findViewById(R.id.lottieComplete);

    }

    private void applyModuleTheme() {
        try {
            android.content.Intent intent = getIntent();
            String colorStart = intent.getStringExtra("module_color_start");
            String colorEnd = intent.getStringExtra("module_color_end");
            if (colorStart == null || colorStart.isEmpty()) colorStart = "#4ECDC4";
            if (colorEnd == null || colorEnd.isEmpty()) colorEnd = "#44A08D";
            // Apply to check button as accent
            if (btnCheckAnswer != null) {
                btnCheckAnswer.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorStart)));
            }
        } catch (Exception ignored) {}
    }

    private void startTimer() {

        startTime = System.currentTimeMillis();

        timerHandler.post(new Runnable() {

            @Override

            public void run() {

                long elapsedMillis = System.currentTimeMillis() - startTime;

                int seconds = (int) (elapsedMillis / 1000);

                int minutes = seconds / 60;

                seconds = seconds % 60;

                tvTimer.setText(String.format("%d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 100);

            }

        });

    }



    private void setupMatchData() {

        pictures = new ArrayList<>();

        words = new ArrayList<>();



        // Create 4 picture-word pairs

        addMatchPair(1, "🐱", "Cat");

        addMatchPair(2, "🐕", "Dog");

        addMatchPair(3, "🌸", "Flower");

        addMatchPair(4, "🌞", "Sun");



        // Shuffle words to randomize

        Collections.shuffle(words);

    }



    private void addMatchPair(int id, String emoji, String word) {

        pictures.add(new MatchItem(id, emoji, word));

        words.add(new MatchItem(id, emoji, word));

    }



    private void setupRecyclerViews() {

        // Pictures

        pictureAdapter = new PictureAdapter(pictures);

        recyclerPictures.setLayoutManager(new LinearLayoutManager(this));

        recyclerPictures.setAdapter(pictureAdapter);



        // Words

        wordAdapter = new WordAdapter(words);

        recyclerWords.setLayoutManager(new LinearLayoutManager(this));

        recyclerWords.setAdapter(wordAdapter);

    }



    private void setupListeners() {

        btnBack.setOnClickListener(v -> {

            timerHandler.removeCallbacksAndMessages(null);

            finish();

        });

        btnCheckAnswer.setOnClickListener(v -> checkAnswers());

    }



    private void onPictureClicked(MatchItem picture, int position) {

        if (picture.isMatched) {

            return; // Already matched

        }



        // Deselect previous

        if (selectedPicture != null) {

            selectedPicture.isSelected = false;

        }



        // Select this picture

        selectedPicture = picture;

        picture.isSelected = true;

        pictureAdapter.notifyDataSetChanged();

    }



    private void onWordClicked(MatchItem word, int position) {

        if (word.isMatched || selectedPicture == null) {

            return;

        }



        // Check if IDs match

        if (selectedPicture.id == word.id) {

            // Correct match!

            comboCount++;

            tvCombo.setText(comboCount + "x");



            // Show combo message for streaks

            if (comboCount >= 3) {

                Toast.makeText(this, "🔥 " + comboCount + " Combo! Amazing!", Toast.LENGTH_SHORT).show();

            } else if (comboCount >= 2) {

                Toast.makeText(this, "✨ " + comboCount + " Combo!", Toast.LENGTH_SHORT).show();

            }



            makeMatch(selectedPicture, word, position);

        } else {

            // Wrong match - reset combo

            comboCount = 0;

            tvCombo.setText("0x");

            Toast.makeText(this, "❌ Wrong match! Combo reset.", Toast.LENGTH_SHORT).show();



            // Shake both

            shakeWord(position);

            shakePicture();

        }

    }



    private void makeMatch(MatchItem picture, MatchItem word, int wordPosition) {

        picture.isMatched = true;

        picture.isSelected = false;

        word.isMatched = true;

        matchedCount++;



        // Update counter

        tvMatchCounter.setText("Matches: " + matchedCount + "/" + pictures.size());



        // Animate both cards

        View pictureView = recyclerPictures.findViewHolderForAdapterPosition(getPicturePosition(picture)).itemView;

        View wordView = recyclerWords.findViewHolderForAdapterPosition(wordPosition).itemView;



        animateMatch(pictureView);

        animateMatch(wordView);

        // Lottie sparkle on each match
        playLottieOnce(lottieCorrect);

        selectedPicture = null;

        pictureAdapter.notifyDataSetChanged();

        wordAdapter.notifyDataSetChanged();

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



    private int getPicturePosition(MatchItem picture) {

        return pictures.indexOf(picture);

    }



    private void animateMatch(View view) {

        view.animate()

                .scaleX(1.1f)

                .scaleY(1.1f)

                .setDuration(150)

                .withEndAction(() -> {

                    view.animate()

                            .scaleX(1f)

                            .scaleY(1f)

                            .setDuration(150);

                });

    }



    private void shakeWord(int position) {

        RecyclerView.ViewHolder holder = recyclerWords.findViewHolderForAdapterPosition(position);

        if (holder != null) {

            shakeView(holder.itemView);

        }

    }



    private void shakePicture() {

        if (selectedPicture != null) {

            int position = getPicturePosition(selectedPicture);

            RecyclerView.ViewHolder holder = recyclerPictures.findViewHolderForAdapterPosition(position);

            if (holder != null) {

                shakeView(holder.itemView);

            }

        }

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



    private void checkAnswers() {

        // Stop timer

        timerHandler.removeCallbacksAndMessages(null);

        long elapsedMillis = System.currentTimeMillis() - startTime;

        int elapsedSeconds = (int) (elapsedMillis / 1000);



        boolean allMatched = matchedCount == pictures.size();

        int correctCount = matchedCount;

        int total = pictures.size();



        // Calculate XP and stars with time bonus and combo bonus

        int xpEarned = 0;

        int stars = 0;



        if (allMatched) {

            // Lottie full celebration for perfect match
            if (lottieComplete != null) {
                lottieComplete.setVisibility(View.VISIBLE);
                lottieComplete.playAnimation();
            }

            xpEarned = 50;

            stars = 3;

            // Time bonus: faster = more points

            if (elapsedSeconds < 30) {

                xpEarned += 25; // Speed demon!

            } else if (elapsedSeconds < 60) {

                xpEarned += 15; // Fast

            } else if (elapsedSeconds < 90) {

                xpEarned += 5; // Good time

            }

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



        // Combo bonus: reward no mistakes

        if (comboCount == total) {

            xpEarned += 20; // Perfect combo!

            Toast.makeText(this, "🎉 PERFECT COMBO! +20 XP Bonus!", Toast.LENGTH_LONG).show();

        }



        // Update session XP

        int currentXP = session.getXP();

        session.saveXP(currentXP + xpEarned);



        showResultDialog(allMatched, correctCount, total, xpEarned, stars);

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

        matchedCount = 0;

        comboCount = 0;

        tvMatchCounter.setText("Matches: 0/" + pictures.size());

        tvCombo.setText("0x");

        selectedPicture = null;



        for (MatchItem item : pictures) {

            item.isMatched = false;

            item.isSelected = false;

        }

        for (MatchItem item : words) {

            item.isMatched = false;

        }



        Collections.shuffle(words);

        pictureAdapter.notifyDataSetChanged();

        wordAdapter.notifyDataSetChanged();



        // Restart timer

        startTimer();

    }



    // MatchItem model class

    private static class MatchItem {

        int id;

        String emoji;

        String word;

        boolean isMatched;

        boolean isSelected;



        MatchItem(int id, String emoji, String word) {

            this.id = id;

            this.emoji = emoji;

            this.word = word;

            this.isMatched = false;

            this.isSelected = false;

        }

    }



    // Picture Adapter

    private class PictureAdapter extends RecyclerView.Adapter<PictureAdapter.ViewHolder> {

        private List<MatchItem> items;



        PictureAdapter(List<MatchItem> items) {

            this.items = items;

        }



        @NonNull

        @Override

        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())

                    .inflate(R.layout.item_picture_card, parent, false);

            return new ViewHolder(view);

        }



        @Override

        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            MatchItem item = items.get(position);

            holder.tvPictureEmoji.setText(item.emoji);



            if (item.isMatched) {

                holder.cardPicture.setCardBackgroundColor(0xFFE8F5E9);

                holder.cardPicture.setStrokeColor(0xFF4CAF50);

                holder.cardPicture.setStrokeWidth(4);

            } else if (item.isSelected) {

                holder.cardPicture.setCardBackgroundColor(0xFFF5F3FF);

                holder.cardPicture.setStrokeColor(0xFF7C3AED);

                holder.cardPicture.setStrokeWidth(4);

            } else {

                holder.cardPicture.setCardBackgroundColor(0xFFFFFFFF);

                holder.cardPicture.setStrokeWidth(0);

            }



            holder.cardPicture.setOnClickListener(v -> {

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

                onPictureClicked(item, position);

            });

        }



        @Override

        public int getItemCount() {

            return items.size();

        }



        class ViewHolder extends RecyclerView.ViewHolder {

            MaterialCardView cardPicture;

            TextView tvPictureEmoji;



            ViewHolder(View itemView) {

                super(itemView);

                cardPicture = itemView.findViewById(R.id.cardPicture);

                tvPictureEmoji = itemView.findViewById(R.id.tvPictureEmoji);

            }

        }

    }



    // Word Adapter

    private class WordAdapter extends RecyclerView.Adapter<WordAdapter.ViewHolder> {

        private List<MatchItem> items;



        WordAdapter(List<MatchItem> items) {

            this.items = items;

        }



        @NonNull

        @Override

        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext())

                    .inflate(R.layout.item_word_card, parent, false);

            return new ViewHolder(view);

        }



        @Override

        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

            MatchItem item = items.get(position);

            holder.tvWord.setText(item.word);



            if (item.isMatched) {

                holder.cardWord.setCardBackgroundColor(0xFFE8F5E9);

                holder.cardWord.setStrokeColor(0xFF4CAF50);

                holder.cardWord.setStrokeWidth(4);

            } else {

                holder.cardWord.setCardBackgroundColor(0xFFFFFFFF);

                holder.cardWord.setStrokeWidth(0);

            }



            holder.cardWord.setOnClickListener(v -> {

                if (!item.isMatched) {

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

                    onWordClicked(item, position);

                }

            });

        }



        @Override

        public int getItemCount() {

            return items.size();

        }



        class ViewHolder extends RecyclerView.ViewHolder {

            MaterialCardView cardWord;

            TextView tvWord;



            ViewHolder(View itemView) {

                super(itemView);

                cardWord = itemView.findViewById(R.id.cardWord);

                tvWord = itemView.findViewById(R.id.tvWord);

            }

        }

    }

}