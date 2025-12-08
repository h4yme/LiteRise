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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.literise.R;
import com.example.literise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorySequencingActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvStoryTitle;
    private RecyclerView recyclerStoryEvents;
    private MaterialButton btnCheckAnswer;
    private StoryEventAdapter adapter;
    private List<StoryEvent> storyEvents;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_sequencing);

        session = new SessionManager(this);
        initializeViews();
        setupStoryData();
        setupRecyclerView();
        setupListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvStoryTitle = findViewById(R.id.tvStoryTitle);
        recyclerStoryEvents = findViewById(R.id.recyclerStoryEvents);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
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
                    // Animate when dragging starts
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
                // Animate when dragging ends
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
        btnBack.setOnClickListener(v -> finish());

        btnCheckAnswer.setOnClickListener(v -> checkAnswer());
    }

    private void checkAnswer() {
        boolean isCorrect = true;
        int correctCount = 0;

        // Show visual feedback for each card
        for (int i = 0; i < storyEvents.size(); i++) {
            StoryEvent event = storyEvents.get(i);
            RecyclerView.ViewHolder holder = recyclerStoryEvents.findViewHolderForAdapterPosition(i);

            if (event.correctOrder == i + 1) {
                correctCount++;
                // Correct - green border and bounce
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
                // Wrong - red border and shake
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

        // Calculate XP based on correctness
        int xpEarned = 0;
        int stars = 0;
        if (isCorrect) {
            xpEarned = 50; // Perfect score
            stars = 3;
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

        // Update session XP
        int currentXP = session.getXP();
        session.saveXP(currentXP + xpEarned);

        // Show result dialog after animation
        recyclerStoryEvents.postDelayed(() -> {
            showResultDialog(isCorrect, correctCount, storyEvents.size(), xpEarned, stars);
        }, 800);
    }

    private void shakeView(View view) {
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

        // Set stats with animations
        tvResultScore.setText(String.valueOf(correctCount) + "/" + total);
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
                finish(); // Go back if perfect
            } else {
                // Reset for retry
                resetGame();
            }
        });

        dialog.show();
    }

    private void resetGame() {
        // Reset all card colors
        for (int i = 0; i < storyEvents.size(); i++) {
            RecyclerView.ViewHolder holder = recyclerStoryEvents.findViewHolderForAdapterPosition(i);
            if (holder != null) {
                MaterialCardView card = holder.itemView.findViewById(R.id.cardStoryEvent);
                card.setCardBackgroundColor(0xFFFFFFFF); // White
                card.setStrokeWidth(0);
            }
        }

        // Shuffle and update
        Collections.shuffle(storyEvents);
        for (int i = 0; i < storyEvents.size(); i++) {
            storyEvents.get(i).displayNumber = i + 1;
        }
        adapter.notifyDataSetChanged();

        // Scroll to top for new attempt
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
