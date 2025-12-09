package com.example.literise.activities.games;



import android.Manifest;

import android.content.pm.PackageManager;

import android.media.MediaPlayer;

import android.media.MediaRecorder;

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

import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;

import androidx.recyclerview.widget.LinearLayoutManager;

import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;

import com.example.literise.activities.BaseActivity;
import com.example.literise.database.SessionManager;

import com.google.android.material.button.MaterialButton;

import com.google.android.material.card.MaterialCardView;

import java.io.File;

import java.io.IOException;

import java.util.ArrayList;

import java.util.List;



public class DialogueReadingActivity extends BaseGameActivity {



    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;



    private ImageView btnBack;

    private TextView tvProgress;

    private RecyclerView recyclerDialogue;

    private MaterialButton btnComplete;

    private DialogueAdapter adapter;

    private List<DialogueLine> dialogueLines;

    private int linesRead = 0;

    private SessionManager session;



    private MediaRecorder mediaRecorder;

    private MediaPlayer mediaPlayer;

    private int currentRecordingPosition = -1;

    private Handler recordingAnimationHandler;

    private Runnable recordingAnimationRunnable;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dialogue_reading);



        session = new SessionManager(this);

        recordingAnimationHandler = new Handler();



        initializeViews();

        setupDialogue();

        setupListeners();

        checkAudioPermission();

    }



    private void checkAudioPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,

                    new String[]{Manifest.permission.RECORD_AUDIO},

                    REQUEST_RECORD_AUDIO_PERMISSION);

        }

    }



    @Override

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,

                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "🎤 Microphone ready!", Toast.LENGTH_SHORT).show();

            } else {

                Toast.makeText(this, "Microphone permission required for voice recording",

                        Toast.LENGTH_LONG).show();

            }

        }

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

        btnBack.setOnClickListener(v -> {

            stopRecording();

            stopPlayback();

            finish();

        });

        btnComplete.setOnClickListener(v -> checkCompletion());

    }



    private void startRecording(int position) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)

                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Please grant microphone permission", Toast.LENGTH_SHORT).show();

            checkAudioPermission();

            return;

        }



        // Stop any existing recording or playback

        stopRecording();

        stopPlayback();



        DialogueLine line = dialogueLines.get(position);

        String fileName = getAudioFilePath(position);



        try {

            mediaRecorder = new MediaRecorder();

            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);

            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.setOutputFile(fileName);

            mediaRecorder.prepare();

            mediaRecorder.start();



            currentRecordingPosition = position;

            line.isRecording = true;

            line.audioFilePath = fileName;

            adapter.notifyItemChanged(position);



            // Auto-stop recording after 10 seconds

            recordingAnimationHandler.postDelayed(() -> {

                if (currentRecordingPosition == position) {

                    stopRecording();

                }

            }, 10000);



            Toast.makeText(this, "🎤 Recording...", Toast.LENGTH_SHORT).show();



        } catch (IOException e) {

            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show();

            e.printStackTrace();

        }

    }



    private void stopRecording() {

        if (mediaRecorder != null) {

            try {

                mediaRecorder.stop();

                mediaRecorder.release();

            } catch (Exception e) {

                e.printStackTrace();

            }

            mediaRecorder = null;



            if (currentRecordingPosition >= 0) {

                DialogueLine line = dialogueLines.get(currentRecordingPosition);

                line.isRecording = false;

                line.hasRecording = true;

                // Only increment if this line wasn't already recorded

                if (!line.isRead) {

                    linesRead++;

                }

                line.isRead = true;

                adapter.notifyItemChanged(currentRecordingPosition);

                updateProgress();



                Toast.makeText(this, "✅ Recording saved!", Toast.LENGTH_SHORT).show();



                currentRecordingPosition = -1;

            }

        }

    }



    private void startPlayback(int position) {

        stopPlayback();

        stopRecording();



        DialogueLine line = dialogueLines.get(position);

        if (line.audioFilePath == null || !new File(line.audioFilePath).exists()) {

            Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show();

            return;

        }



        try {

            mediaPlayer = new MediaPlayer();

            mediaPlayer.setDataSource(line.audioFilePath);

            mediaPlayer.prepare();

            mediaPlayer.start();



            line.isPlaying = true;

            adapter.notifyItemChanged(position);



            mediaPlayer.setOnCompletionListener(mp -> {

                line.isPlaying = false;

                adapter.notifyItemChanged(position);

                stopPlayback();

            });



            Toast.makeText(this, "▶️ Playing...", Toast.LENGTH_SHORT).show();



        } catch (IOException e) {

            Toast.makeText(this, "Failed to play recording", Toast.LENGTH_SHORT).show();

            e.printStackTrace();

        }

    }



    private void stopPlayback() {

        if (mediaPlayer != null) {

            try {

                if (mediaPlayer.isPlaying()) {

                    mediaPlayer.stop();

                }

                mediaPlayer.release();

            } catch (Exception e) {

                e.printStackTrace();

            }

            mediaPlayer = null;

        }

    }



    private String getAudioFilePath(int position) {

        File directory = getExternalCacheDir();

        if (directory != null) {

            return directory.getAbsolutePath() + "/dialogue_line_" + position + ".3gp";

        }

        return getCacheDir().getAbsolutePath() + "/dialogue_line_" + position + ".3gp";

    }



    private void updateProgress() {

        tvProgress.setText("Recorded: " + linesRead + "/" + dialogueLines.size());

    }



    private void checkCompletion() {

        boolean allRead = linesRead == dialogueLines.size();



        if (!allRead) {

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

                .setTitle("Not Yet! 🎤")

                .setMessage("Please record all dialogue lines before completing!")

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



        tvResultTitle.setText("EXCELLENT READING! 🎉🎤");



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



        tvResultScore.setText(totalLines + " lines recorded");

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



    @Override

    protected void onDestroy() {

        super.onDestroy();

        stopRecording();

        stopPlayback();

        if (recordingAnimationHandler != null) {

            recordingAnimationHandler.removeCallbacksAndMessages(null);

        }

    }



    // DialogueLine model class

    private static class DialogueLine {

        String speaker;

        String avatar;

        String text;

        boolean isRead;

        boolean hasRecording;

        boolean isRecording;

        boolean isPlaying;

        String audioFilePath;



        DialogueLine(String speaker, String avatar, String text) {

            this.speaker = speaker;

            this.avatar = avatar;

            this.text = text;

            this.isRead = false;

            this.hasRecording = false;

            this.isRecording = false;

            this.isPlaying = false;

            this.audioFilePath = null;

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



            // Update UI based on recording state

            if (line.isRecording) {

                // Recording state - pulsing red

                holder.cardDialogue.setCardBackgroundColor(0xFFFFEBEE);

                holder.cardDialogue.setStrokeColor(0xFFEF5350);

                holder.cardDialogue.setStrokeWidth(4);

                holder.ivCheckmark.setVisibility(View.GONE);

                holder.btnRecord.setVisibility(View.VISIBLE);

                holder.btnPlay.setVisibility(View.GONE);

                holder.btnRecord.setIconResource(R.drawable.ic_stop);

                holder.btnRecord.setText("Stop");

                holder.btnRecord.setIconTintResource(android.R.color.holo_red_dark);



                // Pulsing animation

                holder.cardDialogue.animate()

                        .alpha(0.7f)

                        .setDuration(500)

                        .withEndAction(() -> {

                            if (line.isRecording) {

                                holder.cardDialogue.animate()

                                        .alpha(1f)

                                        .setDuration(500)

                                        .withEndAction(() -> {

                                            if (line.isRecording) {

                                                notifyItemChanged(position);

                                            }

                                        });

                            }

                        });

            } else if (line.isPlaying) {

                // Playing state - blue

                holder.cardDialogue.setCardBackgroundColor(0xFFE3F2FD);

                holder.cardDialogue.setStrokeColor(0xFF2196F3);

                holder.cardDialogue.setStrokeWidth(3);

                holder.ivCheckmark.setVisibility(View.VISIBLE);

                holder.btnRecord.setVisibility(View.VISIBLE);

                holder.btnPlay.setVisibility(View.VISIBLE);

                holder.btnPlay.setIconResource(R.drawable.ic_stop);

                holder.btnPlay.setText("Stop");

            } else if (line.hasRecording) {

                // Has recording - green

                holder.cardDialogue.setCardBackgroundColor(0xFFE8F5E9);

                holder.cardDialogue.setStrokeColor(0xFF4CAF50);

                holder.cardDialogue.setStrokeWidth(3);

                holder.ivCheckmark.setVisibility(View.VISIBLE);

                holder.btnRecord.setVisibility(View.VISIBLE);

                holder.btnPlay.setVisibility(View.VISIBLE);

                holder.btnRecord.setIconResource(R.drawable.ic_mic);

                holder.btnRecord.setText("Re-record");

                holder.btnRecord.setIconTintResource(android.R.color.holo_red_dark);

                holder.btnPlay.setIconResource(R.drawable.ic_play);

                holder.btnPlay.setText("Play");

            } else {

                // No recording - white

                holder.cardDialogue.setCardBackgroundColor(0xFFFFFFFF);

                holder.cardDialogue.setStrokeColor(0xFFE0E0E0);

                holder.cardDialogue.setStrokeWidth(2);

                holder.ivCheckmark.setVisibility(View.GONE);

                holder.btnRecord.setVisibility(View.VISIBLE);

                holder.btnPlay.setVisibility(View.GONE);

                holder.btnRecord.setIconResource(R.drawable.ic_mic);

                holder.btnRecord.setText("Record");

                holder.btnRecord.setIconTintResource(android.R.color.holo_red_dark);

            }



            // Record button click

            holder.btnRecord.setOnClickListener(v -> {

                if (line.isRecording) {

                    stopRecording();

                } else {

                    startRecording(position);

                }

            });



            // Play button click

            holder.btnPlay.setOnClickListener(v -> {

                if (line.isPlaying) {

                    stopPlayback();

                    line.isPlaying = false;

                    notifyItemChanged(position);

                } else {

                    startPlayback(position);

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

            MaterialButton btnRecord;

            MaterialButton btnPlay;



            ViewHolder(View itemView) {

                super(itemView);

                cardDialogue = itemView.findViewById(R.id.cardDialogue);

                tvAvatar = itemView.findViewById(R.id.tvAvatar);

                tvSpeaker = itemView.findViewById(R.id.tvSpeaker);

                tvDialogueText = itemView.findViewById(R.id.tvDialogueText);

                ivCheckmark = itemView.findViewById(R.id.ivCheckmark);

                btnRecord = itemView.findViewById(R.id.btnRecord);

                btnPlay = itemView.findViewById(R.id.btnPlay);

            }

        }

    }

}