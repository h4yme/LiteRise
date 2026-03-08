package com.example.literise.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.LessonContentResponse;
import com.example.literise.models.UpdateProgressRequest;
import com.example.literise.models.UpdateProgressResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PHASE 1: Lesson Content Display Activity
 *
 * Displays lesson content with adaptive pacing based on placement level:
 * - BEGINNER (Level 1): SLOW pacing, HIGH scaffolding
 * - INTERMEDIATE (Level 2): MODERATE pacing, BALANCED scaffolding
 * - ADVANCED (Level 3): FAST pacing, MINIMAL scaffolding
 */
public class LessonContentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvLessonTitle;
    private TextView tvLessonNumber;
    private TextView tvLessonContent;
    private TextView tvScaffolding;
    private ProgressBar progressBar;
    private MaterialButton btnComplete;

    private int nodeId;
    private int lessonNumber;
    private int placementLevel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_content);

        sessionManager = new SessionManager(this);

        nodeId = getIntent().getIntExtra("node_id", 1);
        lessonNumber = getIntent().getIntExtra("lesson_number", 1);
        placementLevel = convertPlacementLevelToInt(sessionManager.getPlacementLevel());

        initializeViews();
        setupListeners();
        loadLessonContent();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonNumber = findViewById(R.id.tvLessonNumber);
        tvLessonContent = findViewById(R.id.tvLessonContent);
        tvScaffolding = findViewById(R.id.tvScaffolding);
        progressBar = findViewById(R.id.progressBar);
        btnComplete = findViewById(R.id.btnComplete);

        tvLessonNumber.setText("📖 Lesson " + lessonNumber);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
        btnComplete.setOnClickListener(v -> markLessonCompleted());
    }

    private void loadLessonContent() {
        progressBar.setVisibility(View.VISIBLE);
        btnComplete.setEnabled(false);

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getLessonContent(nodeId, placementLevel).enqueue(new Callback<LessonContentResponse>() {
            @Override
            public void onResponse(Call<LessonContentResponse> call, Response<LessonContentResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    displayLessonContent(response.body());
                    btnComplete.setEnabled(true);
                } else {
                    Toast.makeText(LessonContentActivity.this, "Failed to load lesson. Please try again.", Toast.LENGTH_LONG).show();
                    btnComplete.setEnabled(false);
                }
            }

            @Override
            public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LessonContentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                btnComplete.setEnabled(false);
            }
        });
    }

    private void displayLessonContent(LessonContentResponse data) {
        tvLessonTitle.setText(data.getLesson().getTitle());
        tvLessonContent.setText(formatLessonContent(
                data.getLesson().getObjective(),
                data.getLesson().getContent()));
        tvScaffolding.setText(data.getPacing().getDescription());
    }

    /**
     * Parses the ContentJSON string from the API into human-readable sections.
     * Handles all domain shapes: phonics, vocabulary, grammar, comprehension, writing.
     */
    private String formatLessonContent(String objective, String rawJson) {
        StringBuilder sb = new StringBuilder();

        // Always show the learning objective first
        if (objective != null && !objective.trim().isEmpty()) {
            sb.append("🎯 WHAT TO LEARN\n").append(objective.trim()).append("\n\n");
        }

        if (rawJson == null || rawJson.trim().isEmpty()) return sb.toString().trim();

        try {
            JSONObject json = new JSONObject(rawJson);

            // ── RULE / INSTRUCTION ──────────────────────────────────────
            String rule = json.optString("rule", null);
            if (rule == null) rule = json.optString("instruction", null);
            if (rule != null && !rule.isEmpty()) {
                sb.append("📏 RULE\n").append(rule).append("\n\n");
            }

            // ── KEY WORDS (all possible field names) ────────────────────
            String[] wordFields = {"keyWords", "words", "themeWords", "sightWords",
                    "practiceWords", "verbList", "adjectives", "mathWords",
                    "scienceWords", "antonymPairs", "synonymPairs"};
            for (String field : wordFields) {
                if (json.has(field)) {
                    sb.append("📖 KEY WORDS\n");
                    appendArrayOrPairs(sb, json.get(field));
                    sb.append("\n");
                    break;
                }
            }

            // ── PATTERNS ────────────────────────────────────────────────
            if (json.has("patterns")) {
                sb.append("🔤 PATTERNS\n");
                appendJsonArray(sb, json.getJSONArray("patterns"));
                sb.append("\n");
            }
            if (json.has("clusters")) {
                sb.append("🔤 CLUSTERS\n");
                JSONObject clusters = json.getJSONObject("clusters");
                java.util.Iterator<String> keys = clusters.keys();
                while (keys.hasNext()) {
                    String k = keys.next();
                    sb.append("  ").append(k.toUpperCase()).append(": ");
                    sb.append(jsonArrayToLine(clusters.getJSONArray(k))).append("\n");
                }
                sb.append("\n");
            }

            // ── EXAMPLES ────────────────────────────────────────────────
            if (json.has("examples")) {
                Object ex = json.get("examples");
                sb.append("💡 EXAMPLES\n");
                if (ex instanceof JSONArray) {
                    JSONArray arr = (JSONArray) ex;
                    if (arr.length() > 0 && arr.get(0) instanceof JSONObject) {
                        // Array of objects e.g. [{word, division, vowelSound}]
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject item = arr.getJSONObject(i);
                            String word = item.optString("word", "");
                            String div  = item.optString("division", "");
                            String sound = item.optString("vowelSound", "");
                            sb.append("  • ").append(word);
                            if (!div.isEmpty()) sb.append(" → ").append(div);
                            if (!sound.isEmpty()) sb.append("  (").append(sound).append(")");
                            sb.append("\n");
                        }
                    } else {
                        appendJsonArray(sb, arr);
                    }
                } else if (ex instanceof JSONObject) {
                    // Object with sub-arrays e.g. {math: [...], science: [...]}
                    JSONObject exObj = (JSONObject) ex;
                    java.util.Iterator<String> keys = exObj.keys();
                    while (keys.hasNext()) {
                        String k = keys.next();
                        sb.append("  ").append(capitalize(k)).append(": ");
                        sb.append(jsonArrayToLine(exObj.getJSONArray(k))).append("\n");
                    }
                }
                sb.append("\n");
            }

            // ── CONTEXT SENTENCES ───────────────────────────────────────
            String[] sentenceFields = {"contextSentences", "sentencePractice", "sentenceFrames"};
            for (String field : sentenceFields) {
                if (json.has(field)) {
                    sb.append("📝 SENTENCES\n");
                    appendJsonArray(sb, json.getJSONArray(field));
                    sb.append("\n");
                    break;
                }
            }

            // ── TIP ─────────────────────────────────────────────────────
            String tip = json.optString("tip", null);
            if (tip == null) tip = json.optString("memoryTrick", null);
            if (tip == null) tip = json.optString("selfCheck", null);
            if (tip == null) tip = json.optString("trick", null);
            if (tip != null && !tip.isEmpty()) {
                sb.append("⭐ TIP\n").append(tip).append("\n\n");
            }

            // ── CURRICULUM CODE ──────────────────────────────────────────
            String code = json.optString("curriculumCode", null);
            if (code != null && !code.isEmpty()) {
                sb.append("📌 ").append(code);
            }

        } catch (Exception e) {
            // Not valid JSON — show as-is (plain text lessons)
            sb.append(rawJson);
        }

        return sb.toString().trim();
    }

    private void appendJsonArray(StringBuilder sb, JSONArray arr) throws Exception {
        for (int i = 0; i < arr.length(); i++) {
            sb.append("  • ").append(arr.getString(i)).append("\n");
        }
    }

    private String jsonArrayToLine(JSONArray arr) throws Exception {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < arr.length(); i++) {
            if (i > 0) line.append(", ");
            line.append(arr.getString(i));
        }
        return line.toString();
    }

    private void appendArrayOrPairs(StringBuilder sb, Object value) throws Exception {
        if (value instanceof JSONArray) {
            JSONArray arr = (JSONArray) value;
            if (arr.length() > 0 && arr.get(0) instanceof JSONArray) {
                // Synonym/antonym pairs [[a,b],[c,d]]
                for (int i = 0; i < arr.length(); i++) {
                    JSONArray pair = arr.getJSONArray(i);
                    sb.append("  • ").append(pair.getString(0))
                      .append(" ↔ ").append(pair.getString(1)).append("\n");
                }
            } else {
                appendJsonArray(sb, arr);
            }
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private void markLessonCompleted() {
        progressBar.setVisibility(View.VISIBLE);
        btnComplete.setEnabled(false);

        int studentId = sessionManager.getStudentId();
        UpdateProgressRequest request = new UpdateProgressRequest(studentId, nodeId, "lesson");

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.updateNodeProgress(request).enqueue(new Callback<UpdateProgressResponse>() {
            @Override
            public void onResponse(Call<UpdateProgressResponse> call, Response<UpdateProgressResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(LessonContentActivity.this, "✅ Lesson Complete! Moving to Game...", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(LessonContentActivity.this, "Failed to save progress", Toast.LENGTH_SHORT).show();
                    btnComplete.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<UpdateProgressResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LessonContentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnComplete.setEnabled(true);
            }
        });
    }

    /**
     * Convert placement level string to integer for API calls
     */
    private int convertPlacementLevelToInt(String levelString) {
        if (levelString == null) return 2; // Default to intermediate

        if (levelString.contains("2") || levelString.toLowerCase().contains("beginner")) {
            return 1;
        } else if (levelString.contains("4") || levelString.toLowerCase().contains("advanced")) {
            return 3;
        } else {
            return 2; // Grade 3 or intermediate
        }
    }
}