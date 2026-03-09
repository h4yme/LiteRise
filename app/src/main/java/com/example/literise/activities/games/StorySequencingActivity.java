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

import androidx.cardview.widget.CardView;

import androidx.recyclerview.widget.ItemTouchHelper;

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



public class StorySequencingActivity extends BaseGameActivity {



    private ImageView btnBack;

    private TextView tvStoryTitle, tvTimer, tvCorrectCount;

    private RecyclerView recyclerStoryEvents;

    private MaterialButton btnCheckAnswer, btnHint, btnShuffle;

    private StoryEventAdapter adapter;

    private List<StoryEvent> storyEvents;

    private SessionManager session;

    private Handler timerHandler = new Handler();

    private long startTime;

    private int hintsUsed = 0;

    private LottieAnimationView lottieCorrect, lottieComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_sequencing);

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
                                setupStoryData();
                                setupRecyclerView();
                                startTimer();
                            }
                        }
                        @Override
                        public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                            setupStoryData();
                            setupRecyclerView();
                            startTimer();
                        }
                    });
        } else {
            setupStoryData();
            setupRecyclerView();
            startTimer();
        }

    }

    private void generateWithAI(int nodeId, String lessonContent) {
        ApiService apiService = ApiClient.getAiClient(this).create(ApiService.class);
        GameContentRequest request = new GameContentRequest(nodeId, "story_sequencing", lessonContent);
        apiService.generateGameContent(request).enqueue(new Callback<GameContentResponse>() {
            @Override
            public void onResponse(Call<GameContentResponse> call, Response<GameContentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success
                        && response.body().content != null) {
                    try {
                        String storyTitle = response.body().content.has("title")
                                ? response.body().content.get("title").getAsString() : null;
                        JsonArray eventsArray = response.body().content.getAsJsonArray("events");
                        List<StoryEvent> aiEvents = new ArrayList<>();
                        for (int i = 0; i < eventsArray.size(); i++) {
                            JsonObject obj = eventsArray.get(i).getAsJsonObject();
                            int order = obj.get("order").getAsInt();
                            String text = obj.get("text").getAsString();
                            aiEvents.add(new StoryEvent(order, text));
                        }
                        if (!aiEvents.isEmpty()) {
                            storyEvents = aiEvents;
                            if (storyTitle != null && !storyTitle.isEmpty() && tvStoryTitle != null) {
                                tvStoryTitle.setText(storyTitle);
                            }
                            Collections.shuffle(storyEvents);
                            for (int i = 0; i < storyEvents.size(); i++) {
                                storyEvents.get(i).displayNumber = i + 1;
                            }
                            setupRecyclerView();
                            startTimer();
                            return;
                        }
                    } catch (Exception e) {
                        android.util.Log.w("StorySequencing", "AI parse error: " + e.getMessage());
                    }
                } else {
                    android.util.Log.w("StorySequencing", "AI generate failed: code=" + response.code()
                            + " msg=" + (response.body() != null ? response.body().message : "null"));
                }
                setupStoryData();
                setupRecyclerView();
                startTimer();
            }

            @Override
            public void onFailure(Call<GameContentResponse> call, Throwable t) {
                android.util.Log.w("StorySequencing", "AI generate network error: " + t.getMessage());
                setupStoryData();
                setupRecyclerView();
                startTimer();
            }
        });
    }



    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        tvStoryTitle = findViewById(R.id.tvStoryTitle);

        tvTimer = findViewById(R.id.tvTimer);

        tvCorrectCount = findViewById(R.id.tvCorrectCount);

        recyclerStoryEvents = findViewById(R.id.recyclerStoryEvents);

        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);

        btnHint = findViewById(R.id.btnHint);

        btnShuffle = findViewById(R.id.btnShuffle);

        lottieCorrect = findViewById(R.id.lottieCorrect);

        lottieComplete = findViewById(R.id.lottieComplete);

    }

    private void applyModuleTheme() {
        try {
            android.content.Intent intent = getIntent();
            String colorStart = intent.getStringExtra("module_color_start");
            String colorEnd = intent.getStringExtra("module_color_end");
            if (colorStart == null || colorStart.isEmpty()) colorStart = "#FFD93D";
            if (colorEnd == null || colorEnd.isEmpty()) colorEnd = "#FFA93D";
            if (btnCheckAnswer != null) {
                btnCheckAnswer.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor(colorStart)));
                btnCheckAnswer.setTextColor(android.graphics.Color.parseColor("#1F2937"));
            }
            if (btnHint != null) {
                btnHint.setStrokeColor(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor(colorStart)));
                btnHint.setTextColor(android.graphics.Color.parseColor(colorStart));
                btnHint.setIconTint(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor(colorStart)));
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

    private void setupStoryData() {
        storyEvents = new ArrayList<>();

        // Create story events with correct order
        storyEvents.add(new StoryEvent(1, "Emma was walking home from school."));
        storyEvents.add(new StoryEvent(2, "She heard a soft whimpering sound from behind a bench."));
        storyEvents.add(new StoryEvent(3, "Emma found a small brown puppy hiding there."));
        storyEvents.add(new StoryEvent(4, "She picked up the puppy and checked for a collar."));
        storyEvents.add(new StoryEvent(5, "There was a tag with a phone number on it."));
        storyEvents.add(new StoryEvent(6, "Emma called the number and reached the puppy's owner."));
        storyEvents.add(new StoryEvent(7, "The grateful owner came to pick up the lost puppy."));
        storyEvents.add(new StoryEvent(8, "Emma felt happy that she could help reunite them."));

        // Shuffle the events to randomize
        Collections.shuffle(storyEvents);

        // Update display numbers after shuffle
        for (int i = 0; i < storyEvents.size(); i++) {
            storyEvents.get(i).displayNumber = i + 1;
        }
    }

    private void setupRecyclerView() {
        adapter = new StoryEventAdapter(storyEvents);
        recyclerStoryEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerStoryEvents.setAdapter(adapter);

        // Setup drag and drop with animations
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                Collections.swap(storyEvents, fromPosition, toPosition);
                adapter.notifyItemMoved(fromPosition, toPosition);

                // Update display numbers
                for (int i = 0; i < storyEvents.size(); i++) {
                    storyEvents.get(i).displayNumber = i + 1;
                }
                adapter.notifyDataSetChanged();
                updateRealTimeFeedback();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Not used
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG && viewHolder != null) {
                    // Animate when dragging starts - card scales up and glows
                    viewHolder.itemView.animate()
                            .scaleX(1.05f)
                            .scaleY(1.05f)
                            .alpha(0.9f)
                            .setDuration(100);
                    viewHolder.itemView.setElevation(16f);
                }
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                // Animate when dragging ends - return to normal
                viewHolder.itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(100);
                viewHolder.itemView.setElevation(4f);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerStoryEvents);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {

            timerHandler.removeCallbacksAndMessages(null);

            finish();

        });

        btnCheckAnswer.setOnClickListener(v -> checkAnswer());

        btnHint.setOnClickListener(v -> showHint());

        btnShuffle.setOnClickListener(v -> shuffleEvents());

    }



    private void updateRealTimeFeedback() {

        int correctCount = 0;

        for (int i = 0; i < storyEvents.size(); i++) {

            StoryEvent event = storyEvents.get(i);

            RecyclerView.ViewHolder holder = recyclerStoryEvents.findViewHolderForAdapterPosition(i);



            if (event.correctOrder == i + 1) {

                correctCount++;

                // Correct position - subtle green hint

                if (holder != null) {

                    MaterialCardView card = holder.itemView.findViewById(R.id.cardStoryEvent);

                    card.setCardBackgroundColor(0xFFF1F8F4); // Very light green

                    card.setStrokeColor(0xFFC8E6C9); // Light green border

                    card.setStrokeWidth(2);

                }

            } else {

                // Wrong position - neutral

                if (holder != null) {

                    MaterialCardView card = holder.itemView.findViewById(R.id.cardStoryEvent);

                    card.setCardBackgroundColor(0xFFFFFFFF); // White

                    card.setStrokeWidth(0);

                }

            }

        }

        tvCorrectCount.setText(correctCount + "/" + storyEvents.size());

    }



    private void showHint() {

        if (hintsUsed >= 3) {

            Toast.makeText(this, "No more hints available!", Toast.LENGTH_SHORT).show();

            return;

        }



        // Find first wrong position

        for (int i = 0; i < storyEvents.size(); i++) {

            StoryEvent event = storyEvents.get(i);

            if (event.correctOrder != i + 1) {

                // Show hint for this event

                RecyclerView.ViewHolder holder = recyclerStoryEvents.findViewHolderForAdapterPosition(i);

                if (holder != null) {

                    MaterialCardView card = holder.itemView.findViewById(R.id.cardStoryEvent);



                    // Flash animation to draw attention

                    card.setCardBackgroundColor(0xFFFFF9C4); // Light yellow

                    card.setStrokeColor(0xFFFFEB3B); // Yellow border

                    card.setStrokeWidth(4);



                    holder.itemView.animate()

                            .scaleX(1.1f)

                            .scaleY(1.1f)

                            .setDuration(200)

                            .withEndAction(() -> {

                                holder.itemView.animate()

                                        .scaleX(1f)

                                        .scaleY(1f)

                                        .setDuration(200);

                            });



                    Toast.makeText(this, "Event #" + event.correctOrder + " should be at position " + event.correctOrder,

                            Toast.LENGTH_LONG).show();

                    hintsUsed++;

                    btnHint.setText("💡 Hint (" + (3 - hintsUsed) + ")");

                    break;

                }

            }

        }

    }



    private void shuffleEvents() {

        // Reset card backgrounds

        for (int i = 0; i < storyEvents.size(); i++) {

            RecyclerView.ViewHolder holder = recyclerStoryEvents.findViewHolderForAdapterPosition(i);

            if (holder != null) {

                MaterialCardView card = holder.itemView.findViewById(R.id.cardStoryEvent);

                card.setCardBackgroundColor(0xFFFFFFFF); // White

                card.setStrokeWidth(0);

            }

        }



        // Shuffle events

        Collections.shuffle(storyEvents);

        for (int i = 0; i < storyEvents.size(); i++) {

            storyEvents.get(i).displayNumber = i + 1;

        }

        adapter.notifyDataSetChanged();

        updateRealTimeFeedback();



        Toast.makeText(this, "Events shuffled!", Toast.LENGTH_SHORT).show();
    }

    private void checkAnswer() {
        boolean isCorrect = true;
        int correctCount = 0;
        timerHandler.removeCallbacksAndMessages(null);

        long elapsedMillis = System.currentTimeMillis() - startTime;

        int elapsedSeconds = (int) (elapsedMillis / 1000);
        // Show visual feedback for each card
        for (int i = 0; i < storyEvents.size(); i++) {
            StoryEvent event = storyEvents.get(i);
            RecyclerView.ViewHolder holder = recyclerStoryEvents.findViewHolderForAdapterPosition(i);

            if (event.correctOrder == i + 1) {
                correctCount++;
                // Correct - green border and bounce animation
                if (holder != null) {
                    MaterialCardView card = holder.itemView.findViewById(R.id.cardStoryEvent);
                    card.setCardBackgroundColor(0xFFE8F5E9); // Light green
                    card.setStrokeColor(0xFF4CAF50); // Green border
                    card.setStrokeWidth(4);

                    // Bounce animation
                    holder.itemView.animate()
                            .scaleX(1.1f)
                            .scaleY(1.1f)
                            .setDuration(150)
                            .withEndAction(() -> {
                                holder.itemView.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(150);
                            });
                }
            } else {
                isCorrect = false;
                // Wrong - red border and shake animation
                if (holder != null) {
                    MaterialCardView card = holder.itemView.findViewById(R.id.cardStoryEvent);
                    card.setCardBackgroundColor(0xFFFFEBEE); // Light red
                    card.setStrokeColor(0xFFF44336); // Red border
                    card.setStrokeWidth(4);

                    // Shake animation
                    shakeView(holder.itemView);
                }
            }
        }

        // Fire Lottie based on result
        if (isCorrect) {
            if (lottieComplete != null) {
                lottieComplete.setVisibility(View.VISIBLE);
                lottieComplete.playAnimation();
            }
        } else if (correctCount >= storyEvents.size() / 2 && lottieCorrect != null) {
            playLottieOnce(lottieCorrect);
        }

        // Calculate XP based on correctness with star rating, time bonus, and hint penalty

        int xpEarned = 0;

        int stars = 0;

        if (isCorrect) {

            xpEarned = 50; // Perfect score

            stars = 3;

            // Time bonus: faster = more points

            if (elapsedSeconds < 60) {

                xpEarned += 20; // Speed bonus

            } else if (elapsedSeconds < 120) {

                xpEarned += 10; // Good time

            }

        } else if (correctCount >= 6) {

            xpEarned = correctCount * 5;

            stars = 2;

        } else if (correctCount >= 4) {

            xpEarned = correctCount * 5;

            stars = 1;

        } else {

            xpEarned = correctCount * 5;

            stars = 0;

        }



        // Hint penalty

        xpEarned = Math.max(0, xpEarned - (hintsUsed * 5));

        // Update session XP
        int currentXP = session.getXP();
        session.saveXP(currentXP + xpEarned);

        // Make variables final for lambda

        final boolean finalIsCorrect = isCorrect;

        final int finalCorrectCount = correctCount;

        final int finalTotal = storyEvents.size();

        final int finalXpEarned = xpEarned;

        final int finalStars = stars;



        // Show result dialog after animation

        recyclerStoryEvents.postDelayed(() -> {

            showResultDialog(finalIsCorrect, finalCorrectCount, finalTotal, finalXpEarned, finalStars);
        }, 800);
    }

    private void shakeView(View view) {
        // Vigorous shake animation for wrong answers
        view.animate()
                .translationX(-25f)
                .setDuration(50)
                .withEndAction(() -> view.animate()
                        .translationX(25f)
                        .setDuration(50)
                        .withEndAction(() -> view.animate()
                                .translationX(-25f)
                                .setDuration(50)
                                .withEndAction(() -> view.animate()
                                        .translationX(25f)
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

        // Calculate accuracy percentage
        int accuracy = (int) ((correctCount * 100.0) / total);

        // Set title and celebration based on performance
        if (isCorrect) {
            tvResultTitle.setText("PERFECT! 🎉✨");
            // Animate trophy - spin and scale up
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

        // Set stats with fade-in animations
        tvResultScore.setText(String.valueOf(correctCount) + "/" + total);
        tvResultAccuracy.setText(accuracy + "%");
        tvResultXP.setText("+" + xpEarned + " XP");

        // Animate stats - staggered fade-in
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
                markGamePhaseComplete(getIntent().getIntExtra("node_id", -1));
                android.content.Intent result = new android.content.Intent();
                result.putExtra("xp_earned", xpEarned);
                result.putExtra("accuracy", accuracy);
                setResult(RESULT_OK, result);
                finish(); // Go back to module ladder if perfect
            } else {
                // Reset game for retry
                resetGame();
            }
        });

        dialog.show();
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

    private void resetGame() {
        // Reset all card colors to default
        for (int i = 0; i < storyEvents.size(); i++) {
            RecyclerView.ViewHolder holder = recyclerStoryEvents.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                MaterialCardView card = holder.itemView.findViewById(R.id.cardStoryEvent);
                card.setCardBackgroundColor(0xFFFFFFFF); // White
                card.setStrokeWidth(0);
            }
        }

        // Shuffle events for new attempt
        Collections.shuffle(storyEvents);
        for (int i = 0; i < storyEvents.size(); i++) {
            storyEvents.get(i).displayNumber = i + 1;
        }
        adapter.notifyDataSetChanged();

        // Scroll to top for fresh start
        recyclerStoryEvents.smoothScrollToPosition(0);
    }

    // StoryEvent model class
    private static class StoryEvent {
        int correctOrder;
        int displayNumber;
        String text;

        StoryEvent(int correctOrder, String text) {
            this.correctOrder = correctOrder;
            this.displayNumber = correctOrder;
            this.text = text;
        }
    }

    // Adapter for RecyclerView
    private static class StoryEventAdapter extends RecyclerView.Adapter<StoryEventAdapter.ViewHolder> {

        private List<StoryEvent> events;

        StoryEventAdapter(List<StoryEvent> events) {
            this.events = events;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_story_event, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            StoryEvent event = events.get(position);
            holder.tvEventNumber.setText(String.valueOf(event.displayNumber));
            holder.tvEventText.setText(event.text);
        }

        @Override
        public int getItemCount() {
            return events.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            MaterialCardView cardStoryEvent;
            ImageView ivDragHandle;
            TextView tvEventNumber;
            TextView tvEventText;

            ViewHolder(View itemView) {
                super(itemView);
                cardStoryEvent = itemView.findViewById(R.id.cardStoryEvent);
                ivDragHandle = itemView.findViewById(R.id.ivDragHandle);
                tvEventNumber = itemView.findViewById(R.id.tvEventNumber);
                tvEventText = itemView.findViewById(R.id.tvEventText);
            }
        }
    }
}