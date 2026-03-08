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

    // Rotating palette for word chips
    private static final int[] CHIP_COLORS = {
            0xFFE91E63, 0xFF9C27B0, 0xFF3F51B5, 0xFF2196F3,
            0xFF009688, 0xFF4CAF50, 0xFFFF9800, 0xFFF44336,
    };

    // Phonics tile colors (Orton-Gillingham convention)
    private static final int TILE_VOWEL     = 0xFFD32F2F; // red  – vowels
    private static final int TILE_CONSONANT = 0xFF1565C0; // blue – consonants

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
        btnBack           = findViewById(R.id.btnBack);
        tvLessonTitle     = findViewById(R.id.tvLessonTitle);
        tvLessonNumber    = findViewById(R.id.tvLessonNumber);
        tvScaffolding     = findViewById(R.id.tvScaffolding);
        llContentSections = findViewById(R.id.llContentSections);
        progressBar       = findViewById(R.id.progressBar);
        btnComplete       = findViewById(R.id.btnComplete);

        tvLessonNumber.setText("📖 Lesson " + lessonNumber);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> { setResult(RESULT_CANCELED); finish(); });
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
                    Toast.makeText(LessonContentActivity.this,
                            "Failed to load lesson. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LessonContentActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayLessonContent(LessonContentResponse data) {
        tvLessonTitle.setText(data.getLesson().getTitle());
        tvScaffolding.setText(data.getPacing().getDescription());
        buildContent(data.getLesson().getObjective(), data.getLesson().getContent());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Content builder
    // ─────────────────────────────────────────────────────────────────────────

    private void buildContent(String objective, String rawJson) {
        llContentSections.removeAllViews();

        // "Read with a grown-up" hint banner
        addHintBanner("👨‍👩‍👧  Ask a grown-up to read this with you!");

        // Objective
        if (objective != null && !objective.trim().isEmpty()) {
            LinearLayout c = makeModernCard("🎯", "WHAT YOU WILL LEARN", 0xFF1565C0);
            addBodyText(c, objective.trim(), 17);
        }

        if (rawJson == null || rawJson.trim().isEmpty()) return;

        try {
            JSONObject json = new JSONObject(rawJson);

            // Rule
            String rule = json.optString("rule", null);
            if (rule == null) rule = json.optString("instruction", null);
            if (rule != null && !rule.isEmpty()) {
                addRuleCard(rule);
            }

            // Key words
            String[] wordFields = {"keyWords", "words", "themeWords", "sightWords",
                    "practiceWords", "verbList", "adjectives", "mathWords", "scienceWords"};
            for (String field : wordFields) {
                if (json.has(field)) {
                    Object val = json.get(field);
                    if (val instanceof JSONArray) {
                        JSONArray arr = (JSONArray) val;
                        if (arr.length() > 0 && arr.get(0) instanceof JSONArray) {
                            addPairsCard("📖", "TODAY'S WORDS", arr);
                        } else {
                            addWordChipsCard("📖", "TODAY'S WORDS", arr);
                        }
                    }
                    break;
                }
            }

            // Patterns
            if (json.has("patterns")) {
                addWordChipsCard("🔤", "PATTERNS", json.getJSONArray("patterns"));
            }

            // Clusters
            if (json.has("clusters")) {
                addClustersCard(json.getJSONObject("clusters"));
            }

            // Examples with phonics tiles
            if (json.has("examples")) {
                addExamplesCard(json.get("examples"));
            }

            // Sentences
            String[] sentenceFields = {"contextSentences", "sentencePractice", "sentenceFrames"};
            for (String field : sentenceFields) {
                if (json.has(field)) {
                    addSentencesCard(json.getJSONArray(field));
                    break;
                }
            }

            // Tip
            String tip = json.optString("tip", null);
            if (tip == null) tip = json.optString("memoryTrick", null);
            if (tip == null) tip = json.optString("selfCheck", null);
            if (tip == null) tip = json.optString("trick", null);
            if (tip != null && !tip.isEmpty()) {
                addTipCard(tip);
            }

        } catch (Exception e) {
            LinearLayout c = makeModernCard("📖", "LESSON", 0xFF555555);
            addBodyText(c, rawJson, 17);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Section card builders
    // ─────────────────────────────────────────────────────────────────────────

    /** Thin hint banner at the very top (not a full card). */
    private void addHintBanner(String text) {
        LinearLayout banner = new LinearLayout(this);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER);
        GradientDrawable bg = roundRect(0xFFEDE7F6, dp(12));
        banner.setBackground(bg);
        banner.setPadding(dp(16), dp(10), dp(16), dp(10));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(18));
        banner.setLayoutParams(lp);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        tv.setTextColor(0xFF4A148C);
        tv.setTypeface(null, Typeface.ITALIC);
        tv.setGravity(Gravity.CENTER);
        banner.addView(tv);
        llContentSections.addView(banner);
    }

    /** Rule card: extra-large text so the rule stands out clearly. */
    private void addRuleCard(String rule) {
        LinearLayout c = makeModernCard("📏", "THE RULE", 0xFFBF360C);
        addBodyText(c, rule, 19);
    }

    /** Tip / memory trick card with star accent. */
    private void addTipCard(String tip) {
        LinearLayout c = makeModernCard("⭐", "REMEMBER THIS!", 0xFFE65100);

        // Yellow highlight box for the tip text
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(roundRect(0xFFFFF9C4, dp(12)));
        box.setPadding(dp(14), dp(12), dp(14), dp(12));
        addBodyText(box, tip, 17);
        c.addView(box);
    }

    /** Word chips — 2 per row so each word is large and readable. */
    private void addWordChipsCard(String emoji, String label, JSONArray words) throws Exception {
        LinearLayout c = makeModernCard(emoji, label, 0xFF6A1B9A);

        final int PER_ROW = 2;
        LinearLayout row = null;
        for (int i = 0; i < words.length(); i++) {
            if (i % PER_ROW == 0) {
                row = makeHRow(dp(10));
                c.addView(row);
            }
            row.addView(makeWordChip(words.getString(i), CHIP_COLORS[i % CHIP_COLORS.length]));
        }
        // Pad last row
        if (row != null && words.length() % PER_ROW != 0) {
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(spacer);
        }
    }

    /** Synonym/antonym pair chips. */
    private void addPairsCard(String emoji, String label, JSONArray pairs) throws Exception {
        LinearLayout c = makeModernCard(emoji, label, 0xFF6A1B9A);
        for (int i = 0; i < pairs.length(); i++) {
            JSONArray pair = pairs.getJSONArray(i);
            LinearLayout row = makeHRow(dp(10));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(makeWordChip(pair.getString(0), CHIP_COLORS[i * 2 % CHIP_COLORS.length]));
            TextView arrow = new TextView(this);
            arrow.setText("  ↔  ");
            arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            arrow.setTextColor(0xFF777777);
            row.addView(arrow);
            row.addView(makeWordChip(pair.getString(1), CHIP_COLORS[(i * 2 + 1) % CHIP_COLORS.length]));
            c.addView(row);
        }
    }

    /** Clusters (e.g. bl/cl → "black, clap"). */
    private void addClustersCard(JSONObject clusters) throws Exception {
        LinearLayout c = makeModernCard("🔤", "CLUSTERS", 0xFF00695C);
        java.util.Iterator<String> keys = clusters.keys();
        int ci = 0;
        while (keys.hasNext()) {
            String k = keys.next();
            JSONArray arr = clusters.getJSONArray(k);
            LinearLayout row = makeHRow(dp(10));
            row.setGravity(Gravity.CENTER_VERTICAL);
            TextView keyTv = new TextView(this);
            keyTv.setText(k.toUpperCase() + ":");
            keyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            keyTv.setTypeface(null, Typeface.BOLD);
            keyTv.setTextColor(0xFF555555);
            keyTv.setMinWidth(dp(52));
            row.addView(keyTv);
            for (int j = 0; j < arr.length(); j++) {
                row.addView(makeWordChip(arr.getString(j), CHIP_COLORS[ci++ % CHIP_COLORS.length]));
            }
            c.addView(row);
        }
    }

    /**
     * Examples card — the key learning section.
     *
     * For each example we show:
     *  1. The full word in a large colored bubble
     *  2. If a phonics breakdown is available (e.g. "H+a+n+d"), individual
     *     letter tiles with consonants = blue, vowels = red.
     *  3. A "Say it!" prompt so kids know to speak each tile aloud.
     */
    private void addExamplesCard(Object examples) throws Exception {
        LinearLayout c = makeModernCard("💡", "SOUND IT OUT", 0xFF2E7D32);

        // Instruction hint
        TextView hint = new TextView(this);
        hint.setText("👉  Point to each tile and say its sound!");
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        hint.setTextColor(0xFF555555);
        hint.setTypeface(null, Typeface.ITALIC);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        hp.setMargins(0, 0, 0, dp(16));
        hint.setLayoutParams(hp);
        c.addView(hint);

        // Legend: blue = consonant, red = vowel
        c.addView(makeTileLegend());

        if (examples instanceof JSONArray) {
            JSONArray arr = (JSONArray) examples;
            for (int i = 0; i < arr.length(); i++) {
                String word = "", breakdown = null;
                if (arr.get(i) instanceof JSONObject) {
                    JSONObject obj = arr.getJSONObject(i);
                    word      = obj.optString("word", "");
                    breakdown = obj.optString("division", null);
                    if (breakdown == null) breakdown = obj.optString("vowelSound", null);
                } else {
                    String raw = arr.getString(i);
                    if (raw.contains(" - ")) {
                        String[] parts = raw.split(" - ", 2);
                        word      = parts[0].trim();
                        breakdown = parts[1].trim();
                    } else {
                        word = raw;
                    }
                }
                c.addView(makeExampleBlock(word, breakdown, i));
            }
        } else if (examples instanceof JSONObject) {
            JSONObject exObj = (JSONObject) examples;
            java.util.Iterator<String> keys = exObj.keys();
            int i = 0;
            while (keys.hasNext()) {
                String k = keys.next();
                JSONArray sub = exObj.getJSONArray(k);
                TextView subLabel = new TextView(this);
                subLabel.setText(capitalize(k));
                subLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                subLabel.setTypeface(null, Typeface.BOLD);
                subLabel.setTextColor(0xFF777777);
                subLabel.setPadding(0, dp(4), 0, dp(6));
                c.addView(subLabel);
                for (int j = 0; j < sub.length(); j++) {
                    String raw = sub.getString(j);
                    String w = raw, bd = null;
                    if (raw.contains(" - ")) {
                        String[] p = raw.split(" - ", 2);
                        w = p[0].trim(); bd = p[1].trim();
                    }
                    c.addView(makeExampleBlock(w, bd, i++));
                }
            }
        }
    }

    /** Practice sentences — numbered list. */
    private void addSentencesCard(JSONArray sentences) throws Exception {
        LinearLayout c = makeModernCard("📝", "PRACTICE SENTENCES", 0xFF283593);
        for (int i = 0; i < sentences.length(); i++) {
            LinearLayout row = makeHRow(dp(12));
            row.setGravity(Gravity.TOP);

            // Number bubble
            TextView num = makeCircleBadge(String.valueOf(i + 1),
                    CHIP_COLORS[i % CHIP_COLORS.length], dp(30), 13);
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(dp(30), dp(30));
            np.setMargins(0, dp(2), dp(12), 0);
            num.setLayoutParams(np);
            row.addView(num);

            TextView tv = new TextView(this);
            tv.setText(sentences.getString(i));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            tv.setTextColor(0xFF333333);
            tv.setLineSpacing(0f, 1.45f);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tv);
            c.addView(row);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Phonics example block
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a self-contained card for one example word:
     *   - Large word bubble
     *   - Phonics letter tiles (consonant=blue, vowel=red) if breakdown present
     */
    private View makeExampleBlock(String word, String breakdown, int index) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackground(roundRect(0xFFF1F8E9, dp(14)));
        box.setPadding(dp(12), dp(16), dp(12), dp(16));
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, 0, dp(14));
        box.setLayoutParams(bp);

        // Large word
        TextView wordTv = new TextView(this);
        wordTv.setText(word);
        wordTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        wordTv.setTypeface(null, Typeface.BOLD);
        wordTv.setTextColor(CHIP_COLORS[index % CHIP_COLORS.length]);
        wordTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wp.setMargins(0, 0, 0, dp(12));
        wordTv.setLayoutParams(wp);
        box.addView(wordTv);

        // Phonics tiles if breakdown available
        if (breakdown != null && (breakdown.contains("+") || hasSingleCharSplit(breakdown))) {
            box.addView(makeLetterTiles(breakdown));

            // "Say it!" label
            TextView sayIt = new TextView(this);
            sayIt.setText("Say it out loud! 🔊");
            sayIt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            sayIt.setTextColor(0xFF888888);
            sayIt.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            sp.setMargins(0, dp(8), 0, 0);
            sayIt.setLayoutParams(sp);
            box.addView(sayIt);
        }

        return box;
    }

    /**
     * Renders a row of letter tiles from a phonics breakdown string.
     * e.g. "H+a+n+d" → [H blue][a red][n blue][d blue]
     *      "Ti-ger"  → [Ti blue][ger blue]   (syllable split)
     */
    private LinearLayout makeLetterTiles(String breakdown) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        // Split on '+' (letter-level) or '-' (syllable-level)
        String[] segments = breakdown.split("[+\\-]");
        for (String seg : segments) {
            seg = seg.trim();
            if (seg.isEmpty()) continue;

            boolean isVowel = isVowelSegment(seg);
            int tileColor   = isVowel ? TILE_VOWEL : TILE_CONSONANT;

            TextView tile = new TextView(this);
            tile.setText(seg.toUpperCase());
            tile.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
            tile.setTypeface(null, Typeface.BOLD);
            tile.setTextColor(Color.WHITE);
            tile.setGravity(Gravity.CENTER);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(tileColor);
            bg.setCornerRadius(dp(8));
            tile.setBackground(bg);

            int w = dp(44 + Math.max(0, (seg.length() - 1) * 10));
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(w, dp(50));
            tp.setMargins(dp(3), 0, dp(3), 0);
            tile.setLayoutParams(tp);
            row.addView(tile);
        }
        return row;
    }

    /** Vowel if the segment's first character is a, e, i, o, or u (any case). */
    private boolean isVowelSegment(String seg) {
        if (seg.isEmpty()) return false;
        return "aeiouAEIOU".indexOf(seg.charAt(0)) >= 0;
    }

    /** True if the string contains '-' (syllable split) with single-char segments. */
    private boolean hasSingleCharSplit(String s) {
        if (!s.contains("-")) return false;
        for (String part : s.split("-")) {
            if (!part.trim().isEmpty()) return true;
        }
        return false;
    }

    /** Small legend row: 🟦 = consonant   🟥 = vowel */
    private View makeTileLegend() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(14));
        row.setLayoutParams(lp);

        row.addView(makeTileSwatch(TILE_CONSONANT));
        row.addView(makeLegendLabel("= consonant    "));
        row.addView(makeTileSwatch(TILE_VOWEL));
        row.addView(makeLegendLabel("= vowel"));
        return row;
    }

    private View makeTileSwatch(int color) {
        View v = new View(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(4));
        v.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(18), dp(18));
        lp.setMargins(0, 0, dp(5), 0);
        v.setLayoutParams(lp);
        return v;
    }

    private TextView makeLegendLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTextColor(0xFF777777);
        return tv;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Modern card factory (colored header band + white body)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a modern card with a colored header band.
     * Returns the white content LinearLayout for adding children.
     */
    private LinearLayout makeModernCard(String emoji, String label, int headerColor) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 0, 0, dp(18));
        card.setLayoutParams(cp);
        card.setRadius(dp(20));
        card.setCardElevation(dp(6));
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);

        // ── Colored header band ──────────────────────────────────────────
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setBackgroundColor(headerColor);
        header.setPadding(dp(18), dp(14), dp(18), dp(14));

        TextView emojiTv = new TextView(this);
        emojiTv.setText(emoji);
        emojiTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        header.addView(emojiTv);

        TextView labelTv = new TextView(this);
        labelTv.setText("  " + label);
        labelTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        labelTv.setTypeface(null, Typeface.BOLD);
        labelTv.setTextColor(Color.WHITE);
        labelTv.setLetterSpacing(0.08f);
        header.addView(labelTv);

        // ── White content area ───────────────────────────────────────────
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(20));

        wrapper.addView(header);
        wrapper.addView(content);
        card.addView(wrapper);
        llContentSections.addView(card);
        return content;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // View helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void addBodyText(LinearLayout parent, String text, int spSize) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, spSize);
        tv.setTextColor(0xFF333333);
        tv.setLineSpacing(0f, 1.5f);
        parent.addView(tv);
    }

    private TextView makeWordChip(String word, int color) {
        TextView chip = new TextView(this);
        chip.setText(word);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 19);
        chip.setTypeface(null, Typeface.BOLD);
        chip.setTextColor(Color.WHITE);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(8), dp(14), dp(8), dp(14));

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(24));
        chip.setBackground(bg);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(5), 0, dp(5), 0);
        chip.setLayoutParams(lp);
        return chip;
    }

    private TextView makeCircleBadge(String text, int color, int sizePx, int spSize) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, spSize);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setShape(GradientDrawable.OVAL);
        tv.setBackground(bg);
        tv.setLayoutParams(new LinearLayout.LayoutParams(sizePx, sizePx));
        return tv;
    }

    /** Horizontal LinearLayout with a bottom margin. */
    private LinearLayout makeHRow(int bottomMarginPx) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, bottomMarginPx);
        row.setLayoutParams(lp);
        return row;
    }

    private GradientDrawable roundRect(int color, int radiusPx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radiusPx);
        return d;
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
                    Toast.makeText(LessonContentActivity.this,
                            "✅ Lesson Complete! Moving to Game...", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(LessonContentActivity.this,
                            "Failed to save progress", Toast.LENGTH_SHORT).show();
                    btnComplete.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<UpdateProgressResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LessonContentActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnComplete.setEnabled(true);
            }
        });
    }

    private int convertPlacementLevelToInt(String levelString) {
        if (levelString == null) return 2;
        if (levelString.contains("2") || levelString.toLowerCase().contains("beginner")) return 1;
        if (levelString.contains("4") || levelString.toLowerCase().contains("advanced")) return 3;
        return 2;
    }
}
