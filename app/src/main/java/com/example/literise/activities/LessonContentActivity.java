package com.example.literise.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
    private TextView tvScaffolding;
    private LinearLayout llContentSections;
    private ProgressBar progressBar;
    private MaterialButton btnComplete;

    private int nodeId;
    private int lessonNumber;
    private int placementLevel;
    private SessionManager sessionManager;

    // Chip colors cycling through a fun kid-friendly palette
    private static final int[] CHIP_COLORS = {
            0xFFE91E63, // pink
            0xFF9C27B0, // purple
            0xFF3F51B5, // indigo
            0xFF2196F3, // blue
            0xFF009688, // teal
            0xFF4CAF50, // green
            0xFFFF9800, // orange
            0xFFF44336, // red
    };

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
        tvScaffolding = findViewById(R.id.tvScaffolding);
        llContentSections = findViewById(R.id.llContentSections);
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
        tvScaffolding.setText(data.getPacing().getDescription());
        buildKidFriendlyContent(data.getLesson().getObjective(), data.getLesson().getContent());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Kid-friendly content builder
    // ─────────────────────────────────────────────────────────────────────────

    private void buildKidFriendlyContent(String objective, String rawJson) {
        llContentSections.removeAllViews();

        // Learning objective
        if (objective != null && !objective.trim().isEmpty()) {
            addTextCard("🎯", "TODAY'S GOAL", objective.trim(), 0xFFE3F2FD, 0xFF1565C0);
        }

        if (rawJson == null || rawJson.trim().isEmpty()) return;

        try {
            JSONObject json = new JSONObject(rawJson);

            // Rule / instruction
            String rule = json.optString("rule", null);
            if (rule == null) rule = json.optString("instruction", null);
            if (rule != null && !rule.isEmpty()) {
                addTextCard("📏", "THE RULE", rule, 0xFFFFF3E0, 0xFFBF360C);
            }

            // Key words as colorful chips
            String[] wordFields = {"keyWords", "words", "themeWords", "sightWords",
                    "practiceWords", "verbList", "adjectives", "mathWords", "scienceWords"};
            for (String field : wordFields) {
                if (json.has(field)) {
                    Object val = json.get(field);
                    if (val instanceof JSONArray) {
                        JSONArray arr = (JSONArray) val;
                        if (arr.length() > 0 && arr.get(0) instanceof JSONArray) {
                            addPairsChipsCard("📖", "TODAY'S WORDS", arr);
                        } else {
                            addWordChipsCard("📖", "TODAY'S WORDS", arr);
                        }
                    }
                    break;
                }
            }

            // Patterns as chips
            if (json.has("patterns")) {
                addWordChipsCard("🔤", "PATTERNS", json.getJSONArray("patterns"));
            }

            // Clusters
            if (json.has("clusters")) {
                addClustersCard(json.getJSONObject("clusters"));
            }

            // Examples
            if (json.has("examples")) {
                addExamplesCard(json.get("examples"));
            }

            // Context sentences / practice
            String[] sentenceFields = {"contextSentences", "sentencePractice", "sentenceFrames"};
            for (String field : sentenceFields) {
                if (json.has(field)) {
                    addSentencesCard("📝", "PRACTICE SENTENCES", json.getJSONArray(field));
                    break;
                }
            }

            // Tip
            String tip = json.optString("tip", null);
            if (tip == null) tip = json.optString("memoryTrick", null);
            if (tip == null) tip = json.optString("selfCheck", null);
            if (tip == null) tip = json.optString("trick", null);
            if (tip != null && !tip.isEmpty()) {
                addTextCard("⭐", "REMEMBER THIS!", tip, 0xFFFFFDE7, 0xFFF57F17);
            }

        } catch (Exception e) {
            // Not valid JSON — plain text fallback
            addTextCard("📖", "LESSON", rawJson, 0xFFFFFFFF, 0xFF333333);
        }
    }

    // ── Plain text section card ──────────────────────────────────────────────

    private void addTextCard(String emoji, String label, String body, int bgColor, int labelColor) {
        CardView card = makeCard(bgColor);
        LinearLayout inner = makeCardInner(card);

        inner.addView(makeSectionHeader(emoji + "  " + label, labelColor));

        TextView tv = new TextView(this);
        tv.setText(body);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tv.setTextColor(0xFF333333);
        tv.setLineSpacing(0f, 1.45f);
        inner.addView(tv);
    }

    // ── Word chips card ──────────────────────────────────────────────────────

    private void addWordChipsCard(String emoji, String label, JSONArray words) throws Exception {
        CardView card = makeCard(0xFFF3E5F5);
        LinearLayout inner = makeCardInner(card);

        inner.addView(makeSectionHeader(emoji + "  " + label, 0xFF6A1B9A));

        // Chips in rows of 3
        final int PER_ROW = 3;
        LinearLayout row = null;
        for (int i = 0; i < words.length(); i++) {
            if (i % PER_ROW == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rp.setMargins(0, 0, 0, dp(10));
                row.setLayoutParams(rp);
                inner.addView(row);
            }
            row.addView(makeWordChip(words.getString(i), CHIP_COLORS[i % CHIP_COLORS.length]));
        }

        // Pad remaining cells so chips align left
        if (words.length() % PER_ROW != 0 && row != null) {
            int spare = PER_ROW - (words.length() % PER_ROW);
            for (int i = 0; i < spare; i++) {
                View spacer = new View(this);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                row.addView(spacer);
            }
        }
    }

    // ── Synonym / antonym pairs card ─────────────────────────────────────────

    private void addPairsChipsCard(String emoji, String label, JSONArray pairs) throws Exception {
        CardView card = makeCard(0xFFF3E5F5);
        LinearLayout inner = makeCardInner(card);
        inner.addView(makeSectionHeader(emoji + "  " + label, 0xFF6A1B9A));

        for (int i = 0; i < pairs.length(); i++) {
            JSONArray pair = pairs.getJSONArray(i);
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, dp(10));
            row.setLayoutParams(rp);

            row.addView(makeWordChip(pair.getString(0), CHIP_COLORS[i * 2 % CHIP_COLORS.length]));
            TextView arrow = new TextView(this);
            arrow.setText("  ↔  ");
            arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            arrow.setTextColor(0xFF555555);
            row.addView(arrow);
            row.addView(makeWordChip(pair.getString(1), CHIP_COLORS[(i * 2 + 1) % CHIP_COLORS.length]));
            inner.addView(row);
        }
    }

    // ── Examples card ────────────────────────────────────────────────────────

    private void addExamplesCard(Object examples) throws Exception {
        CardView card = makeCard(0xFFE8F5E9);
        LinearLayout inner = makeCardInner(card);
        inner.addView(makeSectionHeader("💡  LOOK AT THESE", 0xFF2E7D32));

        if (examples instanceof JSONArray) {
            JSONArray arr = (JSONArray) examples;
            for (int i = 0; i < arr.length(); i++) {
                if (arr.get(i) instanceof JSONObject) {
                    // Object shape: {word, division, vowelSound}
                    JSONObject item = arr.getJSONObject(i);
                    String word  = item.optString("word", "");
                    String div   = item.optString("division", "");
                    String sound = item.optString("vowelSound", "");
                    StringBuilder line = new StringBuilder(word);
                    if (!div.isEmpty())   line.append("  →  ").append(div);
                    if (!sound.isEmpty()) line.append("  (").append(sound).append(")");
                    inner.addView(makeExampleRow(line.toString(), i));
                } else {
                    inner.addView(makeExampleRow(arr.getString(i), i));
                }
            }
        } else if (examples instanceof JSONObject) {
            JSONObject exObj = (JSONObject) examples;
            java.util.Iterator<String> keys = exObj.keys();
            int i = 0;
            while (keys.hasNext()) {
                String k = keys.next();
                JSONArray sub = exObj.getJSONArray(k);
                TextView subHeader = new TextView(this);
                subHeader.setText(capitalize(k));
                subHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                subHeader.setTypeface(null, Typeface.BOLD);
                subHeader.setTextColor(0xFF555555);
                subHeader.setPadding(0, dp(4), 0, dp(4));
                inner.addView(subHeader);
                for (int j = 0; j < sub.length(); j++) {
                    inner.addView(makeExampleRow(sub.getString(j), i++));
                }
            }
        }
    }

    // ── Sentences card ───────────────────────────────────────────────────────

    private void addSentencesCard(String emoji, String label, JSONArray sentences) throws Exception {
        CardView card = makeCard(0xFFE8EAF6);
        LinearLayout inner = makeCardInner(card);
        inner.addView(makeSectionHeader(emoji + "  " + label, 0xFF283593));

        for (int i = 0; i < sentences.length(); i++) {
            TextView tv = new TextView(this);
            tv.setText((i + 1) + ".  " + sentences.getString(i));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            tv.setTextColor(0xFF333333);
            tv.setLineSpacing(0f, 1.4f);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(10));
            tv.setLayoutParams(lp);
            inner.addView(tv);
        }
    }

    // ── Clusters card ────────────────────────────────────────────────────────

    private void addClustersCard(JSONObject clusters) throws Exception {
        CardView card = makeCard(0xFFF3E5F5);
        LinearLayout inner = makeCardInner(card);
        inner.addView(makeSectionHeader("🔤  CLUSTERS", 0xFF6A1B9A));

        java.util.Iterator<String> keys = clusters.keys();
        int colorIdx = 0;
        while (keys.hasNext()) {
            String k = keys.next();
            JSONArray arr = clusters.getJSONArray(k);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            rp.setMargins(0, 0, 0, dp(10));
            row.setLayoutParams(rp);

            TextView keyTv = new TextView(this);
            keyTv.setText(k.toUpperCase() + ":");
            keyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            keyTv.setTypeface(null, Typeface.BOLD);
            keyTv.setTextColor(0xFF555555);
            keyTv.setMinWidth(dp(56));
            row.addView(keyTv);

            for (int j = 0; j < arr.length(); j++) {
                row.addView(makeWordChip(arr.getString(j), CHIP_COLORS[colorIdx++ % CHIP_COLORS.length]));
            }
            inner.addView(row);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // View factory helpers
    // ─────────────────────────────────────────────────────────────────────────

    private CardView makeCard(int bgColor) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(cp);
        card.setRadius(dp(18));
        card.setCardElevation(dp(4));
        card.setCardBackgroundColor(bgColor);
        return card;
    }

    /** Creates the inner LinearLayout, adds it to the card, and returns it. */
    private LinearLayout makeCardInner(CardView card) {
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(20), dp(16), dp(20), dp(18));
        card.addView(inner);
        llContentSections.addView(card);
        return inner;
    }

    private TextView makeSectionHeader(String text, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(color);
        tv.setAllCaps(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(12));
        tv.setLayoutParams(lp);
        return tv;
    }

    private TextView makeWordChip(String word, int color) {
        TextView chip = new TextView(this);
        chip.setText(word);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setTextColor(Color.WHITE);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(10), dp(12), dp(10), dp(12));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(24));
        chip.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        chip.setLayoutParams(lp);
        return chip;
    }

    private View makeExampleRow(String text, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        rp.setMargins(0, 0, 0, dp(10));
        row.setLayoutParams(rp);

        // Colored left dot / number bubble
        TextView num = new TextView(this);
        num.setText(String.valueOf(index + 1));
        num.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        num.setTypeface(null, Typeface.BOLD);
        num.setTextColor(Color.WHITE);
        num.setGravity(Gravity.CENTER);
        num.setPadding(dp(6), dp(6), dp(6), dp(6));
        int bubbleColor = CHIP_COLORS[index % CHIP_COLORS.length];
        GradientDrawable bubble = new GradientDrawable();
        bubble.setColor(bubbleColor);
        bubble.setShape(GradientDrawable.OVAL);
        num.setBackground(bubble);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(dp(32), dp(32));
        np.setMargins(0, 0, dp(12), 0);
        np.gravity = Gravity.TOP;
        num.setLayoutParams(np);
        row.addView(num);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        tv.setTextColor(0xFF333333);
        tv.setLineSpacing(0f, 1.35f);
        tv.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tv);
        return row;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Utilities
    // ─────────────────────────────────────────────────────────────────────────

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ─────────────────────────────────────────────────────────────────────────

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
