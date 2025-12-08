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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.literise.R;
import com.example.literise.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
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

        // Setup drag and drop
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

        for (int i = 0; i < storyEvents.size(); i++) {
            StoryEvent event = storyEvents.get(i);
            if (event.correctOrder == i + 1) {
                correctCount++;
            } else {
                isCorrect = false;
            }
        }

        // Calculate XP based on correctness
        int xpEarned = 0;
        if (isCorrect) {
            xpEarned = 50; // Perfect score
        } else {
            // Partial credit: 5 XP per correct position
            xpEarned = correctCount * 5;
        }

        // Update session XP
        int currentXP = session.getXP();
        session.saveXP(currentXP + xpEarned);

        // Show result dialog
        showResultDialog(isCorrect, correctCount, storyEvents.size(), xpEarned);
    }

    private void showResultDialog(boolean isCorrect, int correctCount, int total, int xpEarned) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_result, null);

        ImageView ivTrophy = dialogView.findViewById(R.id.ivTrophy);
        TextView tvResultTitle = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvResultScore = dialogView.findViewById(R.id.tvResultScore);
        TextView tvResultAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);
        TextView tvResultXP = dialogView.findViewById(R.id.tvResultXP);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        // Calculate accuracy percentage
        int accuracy = (int) ((correctCount * 100.0) / total);

        if (isCorrect) {
            tvResultTitle.setText("Perfect! 🎉");
        } else if (accuracy >= 50) {
            tvResultTitle.setText("Good Job!");
        } else {
            tvResultTitle.setText("Keep Trying!");
        }

        // Set stats
        tvResultScore.setText(String.valueOf(correctCount) + "/" + total);
        tvResultAccuracy.setText(accuracy + "%");
        tvResultXP.setText("+" + xpEarned + " XP");

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
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
            CardView cardStoryEvent;
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
