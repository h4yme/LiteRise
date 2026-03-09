package com.example.literise.activities.games;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Minimal Pairs — Pronunciation Challenge Game
 *
 * Students listen to a target word via TTS, record themselves saying it,
 * then Google Cloud Speech-to-Text (evaluate_game_pronunciation.php)
 * scores the pronunciation accuracy (≥ 65 % = pass).
 *
 * Flow per pair:
 *   1. Word displayed + auto-played via TTS
 *   2. Tap 🎤 Speak → records up to 5 s (auto-stop)
 *   3. Audio sent to evaluate_game_pronunciation.php
 *   4. Accuracy % shown; pass → Next unlocks; fail → retry
 */
public class MinimalPairsActivity extends BaseGameActivity {

    private static final int REQUEST_RECORD_AUDIO = 100;
    private static final int MAX_RECORD_MS = 5000;

    // ── UI ──────────────────────────────────────────────────────────────────
    private TextView tvProgress, tvScore, tvStreak, tvTargetWord, tvContrastWord,
            tvInstruction, tvFeedback, tvAccuracy, tvRecognized;
    private MaterialCardView cardWord;
    private MaterialButton btnListen, btnSpeak, btnNext, btnShowHelp;
    private ProgressBar pbRecording, pbAccuracy;
    private View waveformView;

    // ── Speech ───────────────────────────────────────────────────────────────
    private TextToSpeech tts;
    private PronunciationHelper pronunciationHelper;
    private final Handler autoStopHandler = new Handler();
    private Runnable autoStopRunnable;
    private boolean isRecording = false;

    // ── State ────────────────────────────────────────────────────────────────
    private int currentPairIndex = 0;
    private int correctCount = 0;
    private int totalAccuracy = 0;
    private int currentStreak = 0;
    private int totalPairs = 0;
    private List<MinimalPair> minimalPairs;
    private SessionManager session;

    // ── Lifecycle ────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minimal_pairs);

        session = new SessionManager(this);
        pronunciationHelper = new PronunciationHelper(this);

        initViews();
        applyModuleTheme();
        setupTts();
        setupListeners();
        checkMicPermission();

        String lessonContent = getIntent().getStringExtra("lesson_content");
        int nodeId = getIntent().getIntExtra("node_id", -1);

        if (lessonContent != null && !lessonContent.isEmpty() && nodeId > 0) {
            generateWithAI(nodeId, lessonContent);
        } else if (nodeId > 0) {
            int level = getIntent().getIntExtra("placement_level", 2);
            ApiClient.getClient(this).create(ApiService.class)
                    .getLessonContent(nodeId, level)
                    .enqueue(new Callback<LessonContentResponse>() {
                        @Override
                        public void onResponse(Call<LessonContentResponse> c,
                                               Response<LessonContentResponse> r) {
                            if (r.isSuccessful() && r.body() != null
                                    && r.body().getLesson() != null
                                    && r.body().getLesson().getContent() != null)
                                generateWithAI(nodeId, r.body().getLesson().getContent());
                            else setupDefaultPairs();
                        }
                        @Override
                        public void onFailure(Call<LessonContentResponse> c, Throwable t) {
                            setupDefaultPairs();
                        }
                    });
        } else {
            setupDefaultPairs();
        }
    }

    // ── View init ────────────────────────────────────────────────────────────

    private void initViews() {
        tvProgress    = findViewById(R.id.tv_progress);
        tvScore       = findViewById(R.id.tv_score);
        tvStreak      = findViewById(R.id.tv_streak);
        tvTargetWord  = findViewById(R.id.tv_target_word);
        tvContrastWord = findViewById(R.id.tv_contrast_word);
        tvInstruction = findViewById(R.id.tv_instruction);
        tvFeedback    = findViewById(R.id.tv_feedback);
        tvAccuracy    = findViewById(R.id.tv_accuracy);
        tvRecognized  = findViewById(R.id.tv_recognized);
        cardWord      = findViewById(R.id.card_word);
        btnListen     = findViewById(R.id.btn_listen);
        btnSpeak      = findViewById(R.id.btn_speak);
        btnNext       = findViewById(R.id.btn_next);
        btnShowHelp   = findViewById(R.id.btn_show_help);
        pbRecording   = findViewById(R.id.pb_recording);
        pbAccuracy    = findViewById(R.id.pb_accuracy);
        waveformView  = findViewById(R.id.waveform_view);

        btnNext.setVisibility(View.GONE);
        tvFeedback.setVisibility(View.GONE);
        tvAccuracy.setVisibility(View.GONE);
        tvRecognized.setVisibility(View.GONE);
        pbAccuracy.setVisibility(View.GONE);
        waveformView.setVisibility(View.GONE);
        pbRecording.setVisibility(View.GONE);
    }

    private void applyModuleTheme() {
        try {
            String c = getIntent().getStringExtra("module_color_start");
            if (c == null || c.isEmpty()) c = "#7C3AED";
            btnNext.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor(c)));
        } catch (Exception ignored) {}
    }

    // ── TTS ──────────────────────────────────────────────────────────────────

    private void setupTts() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.8f);
            }
        });
    }

    private void speakWord(String word) {
        if (tts != null) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            btnListen.setEnabled(false);
            new Handler().postDelayed(() -> btnListen.setEnabled(true), 1500);
        }
    }

    // ── Listeners ────────────────────────────────────────────────────────────

    private void setupListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle("Quit Practice?")
                        .setMessage("Your progress will be lost!")
                        .setPositiveButton("Keep Practicing", null)
                        .setNegativeButton("Quit", (d, w) -> finish())
                        .show());

        btnListen.setOnClickListener(v -> {
            if (currentPairIndex < totalPairs)
                speakWord(minimalPairs.get(currentPairIndex).targetWord);
        });

        btnSpeak.setOnClickListener(v -> {
            if (!isRecording) startRecording();
            else stopAndEvaluate();
        });

        btnNext.setOnClickListener(v -> {
            currentPairIndex++;
            if (currentPairIndex < totalPairs) loadCurrentPair();
            else endGame();
        });

        btnShowHelp.setOnClickListener(v -> showHelp());
    }

    // ── Permission ───────────────────────────────────────────────────────────

    private void checkMicPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] p, @NonNull int[] r) {
        super.onRequestPermissionsResult(code, p, r);
        if (code == REQUEST_RECORD_AUDIO
                && (r.length == 0 || r[0] != PackageManager.PERMISSION_GRANTED)) {
            android.widget.Toast.makeText(this, "Microphone permission required",
                    android.widget.Toast.LENGTH_LONG).show();
            finish();
        }
    }

    // ── AI generation ────────────────────────────────────────────────────────

    private void generateWithAI(int nodeId, String lessonContent) {
        ApiClient.getAiClient(this).create(ApiService.class)
                .generateGameContent(new GameContentRequest(nodeId, "minimal_pairs", lessonContent))
                .enqueue(new Callback<GameContentResponse>() {
                    @Override
                    public void onResponse(Call<GameContentResponse> c,
                                           Response<GameContentResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().success
                                && r.body().content != null) {
                            try {
                                JsonArray arr = r.body().content.getAsJsonArray("pairs");
                                List<MinimalPair> list = new ArrayList<>();
                                for (int i = 0; i < arr.size(); i++) {
                                    JsonObject o = arr.get(i).getAsJsonObject();
                                    list.add(new MinimalPair(
                                            o.get("targetWord").getAsString(),
                                            o.get("contrastWord").getAsString(),
                                            o.has("hint") ? o.get("hint").getAsString() : ""));
                                }
                                if (!list.isEmpty()) {
                                    minimalPairs = list;
                                    Collections.shuffle(minimalPairs);
                                    totalPairs = minimalPairs.size();
                                    loadCurrentPair();
                                    return;
                                }
                            } catch (Exception e) {
                                android.util.Log.w("MinimalPairs", "AI parse: " + e.getMessage());
                            }
                        }
                        setupDefaultPairs();
                    }
                    @Override
                    public void onFailure(Call<GameContentResponse> c, Throwable t) {
                        setupDefaultPairs();
                    }
                });
    }

    private void setupDefaultPairs() {
        minimalPairs = new ArrayList<>();
        minimalPairs.add(new MinimalPair("ship",  "sheep", "Ship has a short 'i' sound"));
        minimalPairs.add(new MinimalPair("bit",   "beat",  "Bit is short; beat has long 'ee'"));
        minimalPairs.add(new MinimalPair("cat",   "cut",   "Cat has 'a'; cut has 'u'"));
        minimalPairs.add(new MinimalPair("pen",   "pan",   "Pen has 'e'; pan has 'a'"));
        minimalPairs.add(new MinimalPair("sit",   "seat",  "Sit is short; seat is long"));
        minimalPairs.add(new MinimalPair("fill",  "feel",  "Fill is short; feel is long"));
        minimalPairs.add(new MinimalPair("bad",   "bed",   "Bad has 'a'; bed has 'e'"));
        minimalPairs.add(new MinimalPair("hat",   "hut",   "Hat has 'a'; hut has 'u'"));
        Collections.shuffle(minimalPairs);
        totalPairs = minimalPairs.size();
        loadCurrentPair();
    }

    // ── Game flow ────────────────────────────────────────────────────────────

    private void loadCurrentPair() {
        if (currentPairIndex >= totalPairs) return;
        MinimalPair pair = minimalPairs.get(currentPairIndex);

        tvTargetWord.setText(pair.targetWord.toUpperCase());
        tvContrastWord.setText("Not: " + pair.contrastWord);
        tvProgress.setText((currentPairIndex + 1) + " / " + totalPairs);
        tvInstruction.setText("Tap Listen, then Speak the word");

        btnNext.setVisibility(View.GONE);
        btnSpeak.setEnabled(true);
        btnSpeak.setText("🎤  Speak");
        tvFeedback.setVisibility(View.GONE);
        tvAccuracy.setVisibility(View.GONE);
        tvRecognized.setVisibility(View.GONE);
        pbAccuracy.setVisibility(View.GONE);
        waveformView.setVisibility(View.GONE);
        pbRecording.setVisibility(View.GONE);
        pbRecording.setProgress(0);

        cardWord.setAlpha(0f);
        cardWord.setTranslationY(40f);
        cardWord.animate().alpha(1f).translationY(0f).setDuration(350).start();

        new Handler().postDelayed(() -> speakWord(pair.targetWord), 400);
    }

    // ── Recording ────────────────────────────────────────────────────────────

    private void startRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            checkMicPermission();
            return;
        }

        pronunciationHelper.startRecording(new PronunciationHelper.PronunciationCallback() {
            @Override public void onRecordingStarted() {
                runOnUiThread(() -> {
                    isRecording = true;
                    btnSpeak.setText("⏹  Stop");
                    tvInstruction.setText("Recording… speak now!");
                    waveformView.setVisibility(View.VISIBLE);
                    pbRecording.setVisibility(View.VISIBLE);
                    pbRecording.setProgress(0);
                    ObjectAnimator.ofInt(pbRecording, "progress", 0, 100)
                            .setDuration(MAX_RECORD_MS).start();
                });
            }
            @Override public void onRecordingStopped(File f, int ms) { /* handled by stopAndEvaluate */ }
            @Override public void onRecordingError(String err) {
                runOnUiThread(() -> {
                    isRecording = false;
                    btnSpeak.setText("🎤  Speak");
                    tvInstruction.setText("Recording failed. Try again.");
                });
            }
        });

        autoStopRunnable = this::stopAndEvaluate;
        autoStopHandler.postDelayed(autoStopRunnable, MAX_RECORD_MS);
    }

    private void stopAndEvaluate() {
        autoStopHandler.removeCallbacks(autoStopRunnable);
        if (!pronunciationHelper.isRecording()) return;
        String targetWord = minimalPairs.get(currentPairIndex).targetWord;

        pronunciationHelper.stopRecording(new PronunciationHelper.PronunciationCallback() {
            @Override public void onRecordingStarted() {}
            @Override public void onRecordingStopped(File audioFile, int durationMs) {
                runOnUiThread(() -> {
                    isRecording = false;
                    btnSpeak.setEnabled(false);
                    waveformView.setVisibility(View.GONE);
                    pbRecording.setVisibility(View.GONE);
                    tvInstruction.setText("Evaluating…");
                    callEvaluateApi(audioFile, targetWord);
                });
            }
            @Override public void onRecordingError(String err) {
                runOnUiThread(() -> {
                    isRecording = false;
                    btnSpeak.setText("🎤  Speak");
                    btnSpeak.setEnabled(true);
                    tvInstruction.setText("Try again.");
                });
            }
        });
    }

    // ── API call ─────────────────────────────────────────────────────────────

    private void callEvaluateApi(File audioFile, String targetWord) {
        RequestBody sId = RequestBody.create(MediaType.parse("text/plain"),
                String.valueOf(session.getStudentId()));
        RequestBody tw  = RequestBody.create(MediaType.parse("text/plain"), targetWord);
        RequestBody ab  = RequestBody.create(MediaType.parse("audio/3gp"), audioFile);
        MultipartBody.Part ap = MultipartBody.Part.createFormData("audio_file", audioFile.getName(), ab);

        ApiClient.getClient(this).create(ApiService.class)
                .evaluateGamePronunciation(sId, tw, ap)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> c, Response<ResponseBody> r) {
                        try {
                            String json = r.body() != null ? r.body().string() : "{}";
                            JSONObject obj = new JSONObject(json);
                            if (obj.optBoolean("success", false)) {
                                int acc     = obj.optInt("accuracy", 0);
                                boolean ok  = obj.optBoolean("passed", false);
                                String fb   = obj.optString("feedback", "");
                                String rec  = obj.optString("recognized_text", "");
                                runOnUiThread(() -> showResult(acc, ok, fb, rec));
                            } else {
                                runOnUiThread(() -> showEvalError());
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> showEvalError());
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> c, Throwable t) {
                        runOnUiThread(() -> showEvalError());
                    }
                });
    }

    private void showResult(int accuracy, boolean passed, String feedback, String recognized) {
        pbAccuracy.setVisibility(View.VISIBLE);
        ObjectAnimator.ofInt(pbAccuracy, "progress", 0, accuracy).setDuration(600).start();

        tvAccuracy.setVisibility(View.VISIBLE);
        tvAccuracy.setText(accuracy + "%");
        tvAccuracy.setTextColor(passed ? 0xFF11B067 : 0xFFEF4444);

        if (!recognized.isEmpty()) {
            tvRecognized.setVisibility(View.VISIBLE);
            tvRecognized.setText("I heard: \"" + recognized + "\"");
        }

        tvFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setText(feedback);
        tvFeedback.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                passed ? 0xFFE8FFF3 : 0xFFFFEBEB));
        tvFeedback.setTextColor(passed ? 0xFF0B6E3A : 0xFFBF1F1F);

        btnSpeak.setEnabled(true);
        btnSpeak.setText("🎤  Speak");

        if (passed) {
            correctCount++;
            currentStreak++;
            totalAccuracy += accuracy;
            tvScore.setText("✓ " + correctCount);
            tvStreak.setText("🔥 " + currentStreak);
            tvInstruction.setText("Well done! Tap Next to continue.");
            btnNext.setVisibility(View.VISIBLE);
            btnSpeak.setEnabled(false);
        } else {
            currentStreak = 0;
            tvStreak.setText("🔥 0");
            tvInstruction.setText("Try again — tap Speak!");
        }
    }

    private void showEvalError() {
        btnSpeak.setEnabled(true);
        btnSpeak.setText("🎤  Speak");
        tvInstruction.setText("Could not evaluate. Check connection and try again.");
    }

    // ── End game ─────────────────────────────────────────────────────────────

    private void endGame() {
        int accuracy = totalPairs > 0 ? (correctCount * 100 / totalPairs) : 0;
        int avgAcc   = correctCount > 0 ? (totalAccuracy / correctCount) : 0;
        int xpEarned = correctCount * 10 + (avgAcc / 10);

        markGamePhaseComplete(getIntent().getIntExtra("node_id", -1));

        new AlertDialog.Builder(this)
                .setTitle("Pronunciation Practice Complete! 🎤")
                .setMessage(
                        "Correct: " + correctCount + " / " + totalPairs
                        + "\nAccuracy: " + accuracy + "%"
                        + "\nAvg pronunciation: " + avgAcc + "%"
                        + "\n\nXP Earned: +" + xpEarned)
                .setPositiveButton("Finish", (d, w) -> {
                    android.content.Intent result = new android.content.Intent();
                    result.putExtra("xp_earned", xpEarned);
                    result.putExtra("accuracy", accuracy);
                    setResult(RESULT_OK, result);
                    finish();
                })
                .setNegativeButton("Practice Again", (d, w) -> restartGame())
                .setCancelable(false)
                .show();
    }

    private void restartGame() {
        currentPairIndex = 0;
        correctCount = 0;
        totalAccuracy = 0;
        currentStreak = 0;
        tvScore.setText("✓ 0");
        tvStreak.setText("🔥 0");
        Collections.shuffle(minimalPairs);
        loadCurrentPair();
    }

    private void showHelp() {
        if (currentPairIndex >= totalPairs) return;
        MinimalPair pair = minimalPairs.get(currentPairIndex);
        new AlertDialog.Builder(this)
                .setTitle("💡 Pronunciation Help")
                .setMessage("Target: " + pair.targetWord.toUpperCase()
                        + "\nNot: " + pair.contrastWord
                        + "\n\nTip: " + pair.hint)
                .setPositiveButton("Got it!", null)
                .show();
    }

    // ── Cleanup ──────────────────────────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoStopHandler.removeCallbacksAndMessages(null);
        if (tts != null) { tts.stop(); tts.shutdown(); }
        if (pronunciationHelper != null) pronunciationHelper.release();
    }

    // ── Data class ───────────────────────────────────────────────────────────

    private static class MinimalPair {
        final String targetWord, contrastWord, hint;
        MinimalPair(String t, String c, String h) {
            targetWord = t; contrastWord = c; hint = h;
        }
    }
}
