package com.example.literise.activities.games;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.GameContentRequest;
import com.example.literise.models.GameContentResponse;
import com.example.literise.models.LessonContentResponse;
import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Minimal Pairs - Pronunciation Challenge Game
 *
 * Students listen to a target word, repeat it aloud, and the app checks
 * if they pronounced it correctly (distinguishing from similar-sounding words).
 *
 * Features:
 * - Text-to-speech for word playback
 * - Speech recognition for pronunciation checking
 * - Minimal pairs (similar-sounding words)
 * - Visual feedback for correct/incorrect pronunciation
 * - Mouth shape guides for pronunciation help
 * - Score tracking and streak bonuses
 */
public class MinimalPairsActivity extends BaseGameActivity {

    // Permissions
    private static final int REQUEST_RECORD_AUDIO = 100;

    // UI Components
    private ImageView btnBack, ivMouthShape, ivListenIcon;
    private TextView tvScore, tvStreak, tvTargetWord, tvFeedback, tvProgress, tvInstruction;
    private CardView cardWord;
    private MaterialButton btnListen, btnSpeak, btnNext, btnShowHelp;

    // Speech Components
    private TextToSpeech tts;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;

    // Game State
    private int currentPairIndex = 0;
    private int correctCount = 0;
    private int currentStreak = 0;
    private int totalPairs = 0;
    private boolean isListening = false;

    // Minimal Pairs Data
    private List<MinimalPair> minimalPairs;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minimal_pairs);

        session = new SessionManager(this);

        initializeViews();
        applyModuleTheme();
        setupTextToSpeech();
        setupSpeechRecognition();
        setupListeners();
        checkMicrophonePermission();

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
                                setupMinimalPairs();
                            }
                        }
                        @Override
                        public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                            setupMinimalPairs();
                        }
                    });
        } else {
            setupMinimalPairs();
        }
    }

    private void generateWithAI(int nodeId, String lessonContent) {
        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        GameContentRequest request = new GameContentRequest(nodeId, "minimal_pairs", lessonContent);
        apiService.generateGameContent(request).enqueue(new Callback<GameContentResponse>() {
            @Override
            public void onResponse(Call<GameContentResponse> call, Response<GameContentResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success
                        && response.body().content != null) {
                    try {
                        JsonArray pairsArray = response.body().content.getAsJsonArray("pairs");
                        List<MinimalPair> aiPairs = new ArrayList<>();
                        for (int i = 0; i < pairsArray.size(); i++) {
                            JsonObject obj = pairsArray.get(i).getAsJsonObject();
                            String targetWord = obj.get("targetWord").getAsString();
                            String contrastWord = obj.get("contrastWord").getAsString();
                            String hint = obj.has("hint") ? obj.get("hint").getAsString() : "";
                            aiPairs.add(new MinimalPair(targetWord, contrastWord, hint, R.drawable.ic_mouth_i));
                        }
                        if (!aiPairs.isEmpty()) {
                            minimalPairs = aiPairs;
                            Collections.shuffle(minimalPairs);
                            totalPairs = minimalPairs.size();
                            loadCurrentPair();
                            return;
                        }
                    } catch (Exception ignored) {}
                }
                setupMinimalPairs();
            }

            @Override
            public void onFailure(Call<GameContentResponse> call, Throwable t) {
                setupMinimalPairs();
            }
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        ivMouthShape = findViewById(R.id.iv_mouth_shape);
        ivListenIcon = findViewById(R.id.iv_listen_icon);
        tvScore = findViewById(R.id.tv_score);
        tvStreak = findViewById(R.id.tv_streak);
        tvTargetWord = findViewById(R.id.tv_target_word);
        tvFeedback = findViewById(R.id.tv_feedback);
        tvProgress = findViewById(R.id.tv_progress);
        tvInstruction = findViewById(R.id.tv_instruction);
        cardWord = findViewById(R.id.card_word);
        btnListen = findViewById(R.id.btn_listen);
        btnSpeak = findViewById(R.id.btn_speak);
        btnNext = findViewById(R.id.btn_next);
        btnShowHelp = findViewById(R.id.btn_show_help);

        // Initial state
        btnNext.setVisibility(View.GONE);
        tvFeedback.setVisibility(View.GONE);
        ivMouthShape.setVisibility(View.GONE);
    }

    private void applyModuleTheme() {
        try {
            android.content.Intent intent = getIntent();
            String colorStart = intent.getStringExtra("module_color_start");
            String colorEnd = intent.getStringExtra("module_color_end");
            if (colorStart == null || colorStart.isEmpty()) colorStart = "#FF6B6B";
            if (colorEnd == null || colorEnd.isEmpty()) colorEnd = "#FF8E53";
            android.view.View header = findViewById(R.id.header);
            if (header != null) {
                android.graphics.drawable.GradientDrawable grad =
                    new android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
                        new int[]{android.graphics.Color.parseColor(colorStart),
                                  android.graphics.Color.parseColor(colorEnd)});
                grad.setCornerRadius(0f);
                header.setBackground(grad);
            }
        } catch (Exception ignored) {}
    }

    private void setupMinimalPairs() {
        minimalPairs = new ArrayList<>();

        // Common minimal pairs for pronunciation practice
        minimalPairs.add(new MinimalPair("ship", "sheep", "Ship has a short 'i' sound", R.drawable.ic_mouth_i));
        minimalPairs.add(new MinimalPair("bit", "beat", "Bit has a short 'i', beat has long 'ee'", R.drawable.ic_mouth_i));
        minimalPairs.add(new MinimalPair("cat", "cut", "Cat has 'a' sound, cut has 'u' sound", R.drawable.ic_mouth_a));
        minimalPairs.add(new MinimalPair("pen", "pan", "Pen has 'e' sound, pan has 'a' sound", R.drawable.ic_mouth_e));
        minimalPairs.add(new MinimalPair("sit", "seat", "Sit is short, seat is long", R.drawable.ic_mouth_i));
        minimalPairs.add(new MinimalPair("fill", "feel", "Fill is short, feel is long", R.drawable.ic_mouth_i));
        minimalPairs.add(new MinimalPair("bad", "bed", "Bad has 'a', bed has 'e'", R.drawable.ic_mouth_a));
        minimalPairs.add(new MinimalPair("hat", "hut", "Hat has 'a', hut has 'u'", R.drawable.ic_mouth_a));
        minimalPairs.add(new MinimalPair("thin", "think", "Thin is shorter than think", R.drawable.ic_mouth_th));
        minimalPairs.add(new MinimalPair("lock", "look", "Lock has 'o', look has 'oo'", R.drawable.ic_mouth_o));

        Collections.shuffle(minimalPairs);
        totalPairs = minimalPairs.size();

        loadCurrentPair();
    }

    private void setupTextToSpeech() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(0.8f); // Slower for clarity
            } else {
                Toast.makeText(this, "Text-to-speech not available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpeechRecognition() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
            speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US);
            speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);

            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    isListening = true;
                    tvInstruction.setText("Listening... Say the word!");
                    ivListenIcon.setVisibility(View.VISIBLE);
                }

                @Override
                public void onBeginningOfSpeech() {
                    tvInstruction.setText("Listening...");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Could animate the listening icon based on volume
                }

                @Override
                public void onBufferReceived(byte[] buffer) {}

                @Override
                public void onEndOfSpeech() {
                    isListening = false;
                    tvInstruction.setText("Processing...");
                    ivListenIcon.setVisibility(View.GONE);
                }

                @Override
                public void onError(int error) {
                    isListening = false;
                    ivListenIcon.setVisibility(View.GONE);
                    String errorMessage = "Error listening. Try again!";
                    switch (error) {
                        case SpeechRecognizer.ERROR_NO_MATCH:
                            errorMessage = "No speech detected. Try again!";
                            break;
                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            errorMessage = "Timeout. Try speaking louder!";
                            break;
                    }
                    tvInstruction.setText(errorMessage);
                    btnSpeak.setEnabled(true);
                }

                @Override
                public void onResults(Bundle results) {
                    isListening = false;
                    ivListenIcon.setVisibility(View.GONE);
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (matches != null && !matches.isEmpty()) {
                        checkPronunciation(matches);
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {}

                @Override
                public void onEvent(int eventType, Bundle params) {}
            });
        } else {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_LONG).show();
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Quit Practice?")
                    .setMessage("Your progress will be lost!")
                    .setPositiveButton("Keep Practicing", null)
                    .setNegativeButton("Quit", (d, w) -> finish())
                    .show();
        });

        btnListen.setOnClickListener(v -> speakWord());

        btnSpeak.setOnClickListener(v -> {
            if (!isListening) {
                startListening();
            }
        });

        btnNext.setOnClickListener(v -> {
            currentPairIndex++;
            if (currentPairIndex < totalPairs) {
                loadCurrentPair();
            } else {
                endGame();
            }
        });

        btnShowHelp.setOnClickListener(v -> showPronunciationHelp());
    }

    private void checkMicrophonePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone ready!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void loadCurrentPair() {
        if (currentPairIndex >= totalPairs) return;

        MinimalPair pair = minimalPairs.get(currentPairIndex);

        tvTargetWord.setText(pair.targetWord.toUpperCase());
        tvProgress.setText((currentPairIndex + 1) + " / " + totalPairs);
        tvInstruction.setText("Tap 'Listen' to hear the word");

        btnNext.setVisibility(View.GONE);
        btnSpeak.setEnabled(true);
        tvFeedback.setVisibility(View.GONE);
        ivMouthShape.setVisibility(View.GONE);

        // Animate card
        cardWord.setAlpha(0f);
        cardWord.animate().alpha(1f).setDuration(500).start();

        // Auto-play the word
        new Handler().postDelayed(this::speakWord, 500);
    }

    private void speakWord() {
        MinimalPair pair = minimalPairs.get(currentPairIndex);
        if (tts != null) {
            tts.speak(pair.targetWord, TextToSpeech.QUEUE_FLUSH, null, null);
            btnListen.setEnabled(false);
            new Handler().postDelayed(() -> btnListen.setEnabled(true), 1500);
        }
    }

    private void startListening() {
        if (speechRecognizer != null) {
            btnSpeak.setEnabled(false);
            speechRecognizer.startListening(speechRecognizerIntent);
        }
    }

    private void checkPronunciation(ArrayList<String> recognizedWords) {
        MinimalPair pair = minimalPairs.get(currentPairIndex);
        String targetWord = pair.targetWord.toLowerCase();
        String contrastWord = pair.contrastWord.toLowerCase();

        boolean foundTarget = false;
        boolean foundContrast = false;
        String recognizedWord = "";

        // Check all recognized results
        for (String word : recognizedWords) {
            recognizedWord = word.toLowerCase();
            if (recognizedWord.contains(targetWord)) {
                foundTarget = true;
                break;
            }
            if (recognizedWord.contains(contrastWord)) {
                foundContrast = true;
                break;
            }
        }

        if (foundTarget) {
            handleCorrectPronunciation();
        } else if (foundContrast) {
            handleContrastWordDetected(pair.contrastWord);
        } else {
            handleIncorrectPronunciation(recognizedWord);
        }
    }

    private void handleCorrectPronunciation() {
        correctCount++;
        currentStreak++;

        tvFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setText("Perfect! You said it correctly!");
        tvFeedback.setTextColor(getResources().getColor(R.color.color_success, null));

        tvScore.setText("Score: " + correctCount);
        tvStreak.setText("Streak: " + currentStreak);

        btnNext.setVisibility(View.VISIBLE);
        btnSpeak.setEnabled(false);

        Toast.makeText(this, "Correct! +10 XP", Toast.LENGTH_SHORT).show();
    }

    private void handleContrastWordDetected(String contrastWord) {
        currentStreak = 0;
        tvStreak.setText("Streak: 0");

        tvFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setText("Oops! You said '" + contrastWord + "' instead. Try again!");
        tvFeedback.setTextColor(getResources().getColor(R.color.color_warning, null));

        btnSpeak.setEnabled(true);
        tvInstruction.setText("Listen carefully and try again");
    }

    private void handleIncorrectPronunciation(String recognized) {
        currentStreak = 0;
        tvStreak.setText("Streak: 0");

        tvFeedback.setVisibility(View.VISIBLE);
        tvFeedback.setText("Not quite. I heard: '" + recognized + "'. Try again!");
        tvFeedback.setTextColor(getResources().getColor(R.color.color_error, null));

        btnSpeak.setEnabled(true);
        tvInstruction.setText("Tap the help button for pronunciation tips");
    }

    private void showPronunciationHelp() {
        MinimalPair pair = minimalPairs.get(currentPairIndex);

        ivMouthShape.setVisibility(View.VISIBLE);
        ivMouthShape.setImageResource(pair.mouthShapeResource);

        new AlertDialog.Builder(this)
                .setTitle("Pronunciation Help")
                .setMessage(
                        "Target Word: " + pair.targetWord + "\n" +
                                "Similar Word: " + pair.contrastWord + "\n\n" +
                                "Tip: " + pair.hint
                )
                .setPositiveButton("Got it!", null)
                .show();
    }

    private void endGame() {
        int accuracy = (totalPairs > 0) ? (correctCount * 100 / totalPairs) : 0;
        int xpEarned = correctCount * 10;

        new AlertDialog.Builder(this)
                .setTitle("Pronunciation Practice Complete!")
                .setMessage(
                        "Correct Pronunciations: " + correctCount + " / " + totalPairs + "\n" +
                                "Accuracy: " + accuracy + "%\n\n" +
                                "XP Earned: +" + xpEarned
                )
                .setPositiveButton("Finish", (d, w) -> finish())
                .setNegativeButton("Practice Again", (d, w) -> restartGame())
                .setCancelable(false)
                .show();
    }

    private void restartGame() {
        currentPairIndex = 0;
        correctCount = 0;
        currentStreak = 0;

        tvScore.setText("Score: 0");
        tvStreak.setText("Streak: 0");

        Collections.shuffle(minimalPairs);
        loadCurrentPair();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    /**
     * Data class for minimal pairs
     */
    private static class MinimalPair {
        String targetWord;        // Word to pronounce correctly
        String contrastWord;      // Similar-sounding word to avoid
        String hint;              // Pronunciation tip
        int mouthShapeResource;   // Mouth shape illustration

        MinimalPair(String targetWord, String contrastWord, String hint, int mouthShapeResource) {
            this.targetWord = targetWord;
            this.contrastWord = contrastWord;
            this.hint = hint;
            this.mouthShapeResource = mouthShapeResource;
        }
    }
}