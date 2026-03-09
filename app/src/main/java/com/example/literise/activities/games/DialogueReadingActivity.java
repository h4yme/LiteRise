package com.example.literise.activities.games;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.helpers.PronunciationHelper;
import com.example.literise.models.GameContentRequest;
import com.example.literise.models.GameContentResponse;
import com.example.literise.models.LessonContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Dialogue Reading Activity
 *
 * Students record themselves reading each dialogue line.
 * After each recording, the audio is sent to evaluate_game_pronunciation.php
 * (Google Cloud Speech-to-Text) and an accuracy badge is shown per line.
 * Final XP is weighted by average pronunciation accuracy.
 */
public class DialogueReadingActivity extends BaseGameActivity {

    private static final int REQUEST_RECORD_AUDIO = 200;
    private static final int AUTO_STOP_MS = 8000;

    // ── UI ────────────────────────────────────────────────────────────────────
    private TextView tvProgress, tvScore, tvOverallAccuracy;
    private ProgressBar pbOverallAccuracy;
    private RecyclerView recyclerDialogue;
    private MaterialButton btnComplete;
    private DialogueAdapter adapter;

    // ── Data ──────────────────────────────────────────────────────────────────
    private List<DialogueLine> dialogueLines;
    private int linesRead = 0;
    private int totalAccuracySum = 0;
    private SessionManager session;

    // ── Recording ─────────────────────────────────────────────────────────────
    private PronunciationHelper pronunciationHelper;
    private MediaPlayer mediaPlayer;
    private int currentRecordingPosition = -1;
    private final Handler autoStopHandler = new Handler();
    private Runnable autoStopRunnable;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialogue_reading);

        session = new SessionManager(this);
        pronunciationHelper = new PronunciationHelper(this);

        initViews();
        applyModuleTheme();
        setupListeners();
        checkAudioPermission();

        String lessonContent = getIntent().getStringExtra("lesson_content");
        int nodeId = getIntent().getIntExtra("node_id", -1);

        if (lessonContent != null && !lessonContent.isEmpty() && nodeId > 0) {
            generateWithAI(nodeId, lessonContent);
        } else if (nodeId > 0) {
            int level = getIntent().getIntExtra("placement_level", 2);
            ApiClient.getClient(this).create(ApiService.class)
                    .getLessonContent(nodeId, level)
                    .enqueue(new Callback<LessonContentResponse>() {
                        @Override public void onResponse(Call<LessonContentResponse> c,
                                                         Response<LessonContentResponse> r) {
                            if (r.isSuccessful() && r.body() != null
                                    && r.body().getLesson() != null
                                    && r.body().getLesson().getContent() != null)
                                generateWithAI(nodeId, r.body().getLesson().getContent());
                            else setupDefaultDialogue();
                        }
                        @Override public void onFailure(Call<LessonContentResponse> c, Throwable t) {
                            setupDefaultDialogue();
                        }
                    });
        } else {
            setupDefaultDialogue();
        }
    }

    // ── View init ─────────────────────────────────────────────────────────────

    private void initViews() {
        tvProgress         = findViewById(R.id.tvProgress);
        tvScore            = findViewById(R.id.tvScore);
        tvOverallAccuracy  = findViewById(R.id.tvOverallAccuracy);
        pbOverallAccuracy  = findViewById(R.id.pbOverallAccuracy);
        recyclerDialogue   = findViewById(R.id.recyclerDialogue);
        btnComplete        = findViewById(R.id.btnComplete);

        tvOverallAccuracy.setVisibility(View.GONE);
        pbOverallAccuracy.setVisibility(View.GONE);
    }

    private void applyModuleTheme() {
        try {
            String c = getIntent().getStringExtra("module_color_start");
            if (c == null || c.isEmpty()) c = "#11B067";
            btnComplete.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor(c)));
        } catch (Exception ignored) {}
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners() {
        findViewById(R.id.btnBack).setOnClickListener(v -> {
            stopPlayback();
            finish();
        });
        btnComplete.setOnClickListener(v -> checkCompletion());
    }

    // ── Permission ────────────────────────────────────────────────────────────

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] p, @NonNull int[] r) {
        super.onRequestPermissionsResult(code, p, r);
        if (code == REQUEST_RECORD_AUDIO && r.length > 0
                && r[0] == PackageManager.PERMISSION_GRANTED)
            Toast.makeText(this, "🎤 Microphone ready!", Toast.LENGTH_SHORT).show();
    }

    // ── AI content ────────────────────────────────────────────────────────────

    private void generateWithAI(int nodeId, String lessonContent) {
        ApiClient.getAiClient(this).create(ApiService.class)
                .generateGameContent(new GameContentRequest(nodeId, "dialogue_reading", lessonContent))
                .enqueue(new Callback<GameContentResponse>() {
                    @Override public void onResponse(Call<GameContentResponse> c,
                                                     Response<GameContentResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().success
                                && r.body().content != null) {
                            try {
                                JsonArray arr = r.body().content.getAsJsonArray("lines");
                                List<DialogueLine> list = new ArrayList<>();
                                for (int i = 0; i < arr.size(); i++) {
                                    JsonObject o = arr.get(i).getAsJsonObject();
                                    list.add(new DialogueLine(
                                            o.get("speaker").getAsString(),
                                            o.has("avatar") ? o.get("avatar").getAsString() : "🙂",
                                            o.get("text").getAsString()));
                                }
                                if (!list.isEmpty()) {
                                    setupDialogueWith(list);
                                    return;
                                }
                            } catch (Exception e) {
                                android.util.Log.w("DialogueReading", "AI parse: " + e.getMessage());
                            }
                        }
                        setupDefaultDialogue();
                    }
                    @Override public void onFailure(Call<GameContentResponse> c, Throwable t) {
                        setupDefaultDialogue();
                    }
                });
    }

    private void setupDefaultDialogue() {
        List<DialogueLine> lines = new ArrayList<>();
        lines.add(new DialogueLine("Anna", "👧", "Hello! How are you today?"));
        lines.add(new DialogueLine("Ben",  "👦", "Hi Anna! I'm doing great, thanks!"));
        lines.add(new DialogueLine("Anna", "👧", "Would you like to play with me?"));
        lines.add(new DialogueLine("Ben",  "👦", "Yes! That sounds fun!"));
        lines.add(new DialogueLine("Anna", "👧", "Great! Let's go to the park!"));
        lines.add(new DialogueLine("Ben",  "👦", "Okay! I'll get my ball."));
        setupDialogueWith(lines);
    }

    private void setupDialogueWith(List<DialogueLine> lines) {
        dialogueLines = lines;
        adapter = new DialogueAdapter(dialogueLines);
        recyclerDialogue.setLayoutManager(new LinearLayoutManager(this));
        recyclerDialogue.setAdapter(adapter);
        updateProgress();
    }

    // ── Recording ─────────────────────────────────────────────────────────────

    private void startRecording(int position) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            checkAudioPermission();
            return;
        }

        stopPlayback();

        // If already recording another line, stop it first
        if (pronunciationHelper.isRecording()) {
            pronunciationHelper.stopRecording(new PronunciationHelper.PronunciationCallback() {
                @Override public void onRecordingStarted() {}
                @Override public void onRecordingStopped(File f, int ms) {
                    runOnUiThread(() -> doStartRecording(position));
                }
                @Override public void onRecordingError(String e) {
                    runOnUiThread(() -> doStartRecording(position));
                }
            });
        } else {
            doStartRecording(position);
        }
    }

    private void doStartRecording(int position) {
        DialogueLine line = dialogueLines.get(position);

        pronunciationHelper.startRecording(new PronunciationHelper.PronunciationCallback() {
            @Override public void onRecordingStarted() {
                runOnUiThread(() -> {
                    currentRecordingPosition = position;
                    line.state = DialogueLine.State.RECORDING;
                    adapter.notifyItemChanged(position);
                    Toast.makeText(DialogueReadingActivity.this,
                            "🎤 Recording…", Toast.LENGTH_SHORT).show();
                });
            }
            @Override public void onRecordingStopped(File f, int ms) { /* auto-stop handles this */ }
            @Override public void onRecordingError(String err) {
                runOnUiThread(() -> {
                    line.state = DialogueLine.State.IDLE;
                    adapter.notifyItemChanged(position);
                    Toast.makeText(DialogueReadingActivity.this,
                            "Recording failed. Try again.", Toast.LENGTH_SHORT).show();
                });
            }
        });

        autoStopRunnable = () -> stopRecordingAt(position);
        autoStopHandler.postDelayed(autoStopRunnable, AUTO_STOP_MS);
    }

    private void stopRecordingAt(int position) {
        autoStopHandler.removeCallbacks(autoStopRunnable);
        if (!pronunciationHelper.isRecording()) return;

        DialogueLine line = dialogueLines.get(position);
        String targetText = line.text;

        pronunciationHelper.stopRecording(new PronunciationHelper.PronunciationCallback() {
            @Override public void onRecordingStarted() {}
            @Override public void onRecordingStopped(File audioFile, int durationMs) {
                runOnUiThread(() -> {
                    line.audioFilePath = audioFile.getAbsolutePath();
                    line.state = DialogueLine.State.EVALUATING;
                    adapter.notifyItemChanged(position);
                    currentRecordingPosition = -1;
                    evaluateLine(position, audioFile, targetText);
                });
            }
            @Override public void onRecordingError(String err) {
                runOnUiThread(() -> {
                    line.state = DialogueLine.State.IDLE;
                    adapter.notifyItemChanged(position);
                });
            }
        });
    }

    // ── Evaluation ────────────────────────────────────────────────────────────

    private void evaluateLine(int position, File audioFile, String targetText) {
        DialogueLine line = dialogueLines.get(position);

        RequestBody sId = RequestBody.create(MediaType.parse("text/plain"),
                String.valueOf(session.getStudentId()));
        RequestBody tw  = RequestBody.create(MediaType.parse("text/plain"), targetText);
        RequestBody ab  = RequestBody.create(MediaType.parse("audio/3gp"), audioFile);
        MultipartBody.Part ap =
                MultipartBody.Part.createFormData("audio_file", audioFile.getName(), ab);

        ApiClient.getClient(this).create(ApiService.class)
                .evaluateGamePronunciation(sId, tw, ap)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                        try {
                            String json = r.body() != null ? r.body().string() : "{}";
                            JSONObject obj = new JSONObject(json);
                            if (obj.optBoolean("success", false)) {
                                int acc = obj.optInt("accuracy", 0);
                                runOnUiThread(() -> onLineEvaluated(position, acc));
                            } else {
                                // API error — still mark as read with 0 accuracy
                                runOnUiThread(() -> onLineEvaluated(position, 0));
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> onLineEvaluated(position, 0));
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> c, Throwable t) {
                        // Network error — mark as read with 0 accuracy so game can continue
                        runOnUiThread(() -> onLineEvaluated(position, 0));
                    }
                });
    }

    private void onLineEvaluated(int position, int accuracy) {
        DialogueLine line = dialogueLines.get(position);

        if (!line.isRead) {
            linesRead++;
            totalAccuracySum += accuracy;
        } else {
            // Re-record: replace the old accuracy contribution
            totalAccuracySum = totalAccuracySum - line.accuracy + accuracy;
        }

        line.accuracy  = accuracy;
        line.isRead    = true;
        line.state     = DialogueLine.State.DONE;
        adapter.notifyItemChanged(position);

        updateProgress();
        updateOverallAccuracy();
    }

    // ── Progress ──────────────────────────────────────────────────────────────

    private void updateProgress() {
        if (dialogueLines == null) return;
        tvProgress.setText(linesRead + " / " + dialogueLines.size() + " lines read");
    }

    private void updateOverallAccuracy() {
        if (linesRead == 0) return;
        int avg = totalAccuracySum / linesRead;
        tvOverallAccuracy.setVisibility(View.VISIBLE);
        pbOverallAccuracy.setVisibility(View.VISIBLE);
        pbOverallAccuracy.setProgress(avg);
        tvOverallAccuracy.setText("Avg accuracy: " + avg + "%");
    }

    // ── Playback ──────────────────────────────────────────────────────────────

    private void startPlayback(int position) {
        stopPlayback();
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
        } catch (IOException e) {
            Toast.makeText(this, "Failed to play recording", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayback() {
        if (mediaPlayer != null) {
            try { if (mediaPlayer.isPlaying()) mediaPlayer.stop(); mediaPlayer.release(); }
            catch (Exception ignored) {}
            mediaPlayer = null;
        }
    }

    // ── Completion ────────────────────────────────────────────────────────────

    private void checkCompletion() {
        if (linesRead < dialogueLines.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Not Yet! 🎤")
                    .setMessage("Please record all " + dialogueLines.size() + " dialogue lines first!")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        int avgAccuracy = totalAccuracySum / dialogueLines.size();
        int xpEarned    = 30 + (avgAccuracy / 2); // 30–80 XP based on avg accuracy

        // Stars based on avg accuracy
        int stars = avgAccuracy >= 85 ? 3 : avgAccuracy >= 65 ? 2 : 1;

        session.saveXP(session.getXP() + xpEarned);
        showResultDialog(avgAccuracy, xpEarned, stars);
    }

    private void showResultDialog(int avgAccuracy, int xpEarned, int stars) {
        android.view.View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_game_result, null);

        TextView tvTitle    = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvScore    = dialogView.findViewById(R.id.tvResultScore);
        TextView tvAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);
        TextView tvXP       = dialogView.findViewById(R.id.tvResultXP);
        TextView tvStreak   = dialogView.findViewById(R.id.tvResultStreak);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        tvTitle.setText("Reading Complete! 🎉");
        tvScore.setText(dialogueLines.size() + " lines recorded");
        tvAccuracy.setText("Avg: " + avgAccuracy + "%");
        tvXP.setText("+" + xpEarned + " XP");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(i < stars ? "⭐" : "☆");
        tvStreak.setText(sb.toString());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            markGamePhaseComplete(getIntent().getIntExtra("node_id", -1));
            android.content.Intent result = new android.content.Intent();
            result.putExtra("xp_earned", xpEarned);
            result.putExtra("accuracy", avgAccuracy);
            setResult(RESULT_OK, result);
            finish();
        });

        dialog.show();
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoStopHandler.removeCallbacksAndMessages(null);
        stopPlayback();
        if (pronunciationHelper != null) pronunciationHelper.release();
    }

    // ── DialogueLine model ────────────────────────────────────────────────────

    private static class DialogueLine {
        enum State { IDLE, RECORDING, EVALUATING, DONE }

        String speaker, avatar, text;
        State state = State.IDLE;
        boolean isRead = false;
        boolean isPlaying = false;
        String audioFilePath = null;
        int accuracy = 0;

        DialogueLine(String speaker, String avatar, String text) {
            this.speaker = speaker;
            this.avatar  = avatar;
            this.text    = text;
        }
    }

    // ── Adapter ───────────────────────────────────────────────────────────────

    private class DialogueAdapter extends RecyclerView.Adapter<DialogueAdapter.VH> {
        private final List<DialogueLine> lines;

        DialogueAdapter(List<DialogueLine> lines) { this.lines = lines; }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dialogue_line, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            DialogueLine line = lines.get(pos);

            h.tvAvatar.setText(line.avatar);
            h.tvSpeaker.setText(line.speaker);
            h.tvText.setText(line.text);

            // Accuracy badge
            if (line.isRead) {
                h.tvAccuracy.setVisibility(View.VISIBLE);
                h.tvAccuracy.setText(line.accuracy + "%");
                int color = line.accuracy >= 75 ? 0xFF11B067
                          : line.accuracy >= 50 ? 0xFFF59E0B
                          : 0xFFEF4444;
                h.tvAccuracy.setTextColor(color);
                h.tvAccuracy.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(
                                line.accuracy >= 75 ? 0xFFE8FFF3
                              : line.accuracy >= 50 ? 0xFFFFFBEB
                              : 0xFFFFEBEB));
            } else {
                h.tvAccuracy.setVisibility(View.GONE);
            }

            // State-driven card appearance + buttons
            switch (line.state) {
                case RECORDING:
                    h.card.setStrokeColor(0xFFEF4444);
                    h.card.setStrokeWidth(3);
                    h.card.setCardBackgroundColor(0xFFFFEBEB);
                    h.btnRecord.setText("⏹  Stop");
                    h.btnRecord.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFFEF4444));
                    h.btnPlay.setVisibility(View.GONE);
                    h.tvStatus.setText("Recording…");
                    h.tvStatus.setVisibility(View.VISIBLE);
                    break;

                case EVALUATING:
                    h.card.setStrokeColor(0xFFF59E0B);
                    h.card.setStrokeWidth(2);
                    h.card.setCardBackgroundColor(0xFFFFFBEB);
                    h.btnRecord.setText("Evaluating…");
                    h.btnRecord.setEnabled(false);
                    h.btnPlay.setVisibility(View.GONE);
                    h.tvStatus.setText("Checking pronunciation…");
                    h.tvStatus.setVisibility(View.VISIBLE);
                    break;

                case DONE:
                    int strokeColor = line.accuracy >= 65 ? 0xFF11B067 : 0xFFEF4444;
                    int bgColor     = line.accuracy >= 65 ? 0xFFE8FFF3 : 0xFFFFEBEB;
                    h.card.setStrokeColor(strokeColor);
                    h.card.setStrokeWidth(2);
                    h.card.setCardBackgroundColor(bgColor);
                    h.btnRecord.setEnabled(true);
                    h.btnRecord.setText("🎤 Re-record");
                    h.btnRecord.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFF7C3AED));
                    h.btnPlay.setVisibility(View.VISIBLE);
                    h.btnPlay.setText(line.isPlaying ? "⏹ Stop" : "▶ Play");
                    h.tvStatus.setVisibility(View.GONE);
                    break;

                default: // IDLE
                    h.card.setStrokeColor(0xFFE5E7EB);
                    h.card.setStrokeWidth(1);
                    h.card.setCardBackgroundColor(0xFFFFFFFF);
                    h.btnRecord.setEnabled(true);
                    h.btnRecord.setText("🎤 Record");
                    h.btnRecord.setBackgroundTintList(
                            android.content.res.ColorStateList.valueOf(0xFF7C3AED));
                    h.btnPlay.setVisibility(View.GONE);
                    h.tvStatus.setVisibility(View.GONE);
                    break;
            }

            h.btnRecord.setOnClickListener(v -> {
                int adapterPos = h.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_ID) return;
                DialogueLine dl = lines.get(adapterPos);
                if (dl.state == DialogueLine.State.RECORDING) {
                    stopRecordingAt(adapterPos);
                } else if (dl.state != DialogueLine.State.EVALUATING) {
                    startRecording(adapterPos);
                }
            });

            h.btnPlay.setOnClickListener(v -> {
                int adapterPos = h.getAdapterPosition();
                if (adapterPos == RecyclerView.NO_ID) return;
                DialogueLine dl = lines.get(adapterPos);
                if (dl.isPlaying) {
                    stopPlayback();
                    dl.isPlaying = false;
                    notifyItemChanged(adapterPos);
                } else {
                    startPlayback(adapterPos);
                }
            });
        }

        @Override public int getItemCount() { return lines.size(); }

        class VH extends RecyclerView.ViewHolder {
            MaterialCardView card;
            TextView tvAvatar, tvSpeaker, tvText, tvAccuracy, tvStatus;
            MaterialButton btnRecord, btnPlay;

            VH(View v) {
                super(v);
                card       = v.findViewById(R.id.cardDialogue);
                tvAvatar   = v.findViewById(R.id.tvAvatar);
                tvSpeaker  = v.findViewById(R.id.tvSpeaker);
                tvText     = v.findViewById(R.id.tvDialogueText);
                tvAccuracy = v.findViewById(R.id.tvAccuracyBadge);
                tvStatus   = v.findViewById(R.id.tvStatus);
                btnRecord  = v.findViewById(R.id.btnRecord);
                btnPlay    = v.findViewById(R.id.btnPlay);
            }
        }
    }
}
