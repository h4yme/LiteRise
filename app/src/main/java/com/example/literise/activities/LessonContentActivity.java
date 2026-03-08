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
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.card.MaterialCardView;

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
 * Lesson Content Activity
 *
 * Self-paced lesson display. Design matches the app dashboard:
 *  - White MaterialCardView sections (0dp elevation, no stroke)
 *  - Flat soft-pastel label pills (matching stat badges in dashboard)
 *  - No gradients on content elements
 *  - Phonics letter tiles for active self-learning (consonants = purple, vowels = red)
 */
public class LessonContentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvLessonTitle, tvLessonNumber, tvScaffolding;
    private LinearLayout llContentSections;
    private ProgressBar progressBar;
    private MaterialButton btnComplete;

    private int nodeId, lessonNumber, placementLevel;
    private SessionManager sessionManager;
    private String cachedLessonContent; // forwarded to games so they use lesson words/sentences

    // ── Custom fonts ─────────────────────────────────────────────────────────
    private Typeface fVisbyBold;      // headings, chips, tiles
    private Typeface fPoppinsSemi;    // pills, labels, badges
    private Typeface fPoppinsRegular; // body text, instructions

    // ── Palette (matches dashboard/colors.xml) ───────────────────────────────
    // Section label pill: pastel bg + dark accent text
    private static final int PILL_PURPLE_BG   = 0xFFF3E8FF;
    private static final int PILL_PURPLE_TEXT = 0xFF7C3AED;
    private static final int PILL_ORANGE_BG   = 0xFFFFF4ED;
    private static final int PILL_ORANGE_TEXT = 0xFFEA580C;
    private static final int PILL_BLUE_BG     = 0xFFEFF6FF;
    private static final int PILL_BLUE_TEXT   = 0xFF1D4ED8;
    private static final int PILL_GREEN_BG    = 0xFFF0FDF4;
    private static final int PILL_GREEN_TEXT  = 0xFF15803D;
    private static final int PILL_INDIGO_BG   = 0xFFEEF2FF;
    private static final int PILL_INDIGO_TEXT = 0xFF4338CA;
    private static final int PILL_AMBER_BG    = 0xFFFFFBEB;
    private static final int PILL_AMBER_TEXT  = 0xFF92400E;
    private static final int PILL_ROSE_BG     = 0xFFFFF1F2;
    private static final int PILL_ROSE_TEXT   = 0xFFBE123C;

    // Word chip flat colours (all from app palette, no gradients)
    private static final int[] CHIP_COLORS = {
            0xFF7C3AED, 0xFF1D4ED8, 0xFF15803D, 0xFFEA580C,
            0xFF0E7490, 0xFFBE123C, 0xFF92400E, 0xFF4338CA,
    };

    // Phonics tile colours (Orton-Gillingham convention)
    private static final int TILE_VOWEL     = 0xFFEF4444; // red  – vowels
    private static final int TILE_CONSONANT = 0xFF7C3AED; // purple – consonants

    // Body + secondary text
    private static final int TEXT_PRIMARY   = 0xFF2D3436;
    private static final int TEXT_SECONDARY = 0xFF636E72;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_content);

        sessionManager   = new SessionManager(this);
        nodeId           = getIntent().getIntExtra("node_id", 1);
        lessonNumber     = getIntent().getIntExtra("lesson_number", 1);
        placementLevel   = convertPlacementLevelToInt(sessionManager.getPlacementLevel());

        fVisbyBold      = ResourcesCompat.getFont(this, R.font.visby_bold);
        fPoppinsSemi    = ResourcesCompat.getFont(this, R.font.poppins_semibold);
        fPoppinsRegular = ResourcesCompat.getFont(this, R.font.poppins_regular);

        initViews();
        setupListeners();
        loadLessonContent();
    }

    private void initViews() {
        btnBack           = findViewById(R.id.btnBack);
        tvLessonTitle     = findViewById(R.id.tvLessonTitle);
        tvLessonNumber    = findViewById(R.id.tvLessonNumber);
        tvScaffolding     = findViewById(R.id.tvScaffolding);
        llContentSections = findViewById(R.id.llContentSections);
        progressBar       = findViewById(R.id.progressBar);
        btnComplete       = findViewById(R.id.btnComplete);

        tvLessonNumber.setText("Lesson " + lessonNumber);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> { setResult(RESULT_CANCELED); finish(); });
        btnComplete.setOnClickListener(v -> markLessonCompleted());
    }

    // ── API ───────────────────────────────────────────────────────────────────

    private void loadLessonContent() {
        progressBar.setVisibility(View.VISIBLE);
        btnComplete.setEnabled(false);

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getLessonContent(nodeId, placementLevel).enqueue(new Callback<LessonContentResponse>() {
            @Override
            public void onResponse(Call<LessonContentResponse> call,
                                   Response<LessonContentResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
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
        cachedLessonContent = data.getLesson().getContent(); // cache for forwarding to games
        buildContent(data.getLesson().getObjective(), cachedLessonContent);
    }

    // ── Content builder ───────────────────────────────────────────────────────

    private void buildContent(String objective, String rawJson) {
        llContentSections.removeAllViews();

        if (objective != null && !objective.trim().isEmpty()) {
            addTextSection("🎯", "LEARNING GOAL",
                    PILL_PURPLE_BG, PILL_PURPLE_TEXT, objective.trim());
        }

        if (rawJson == null || rawJson.trim().isEmpty()) return;

        try {
            JSONObject json = new JSONObject(rawJson);

            // Rule / instruction
            String rule = json.optString("rule", null);
            if (rule == null) rule = json.optString("instruction", null);
            if (rule != null && !rule.isEmpty()) addRuleSection(rule);

            // Key words (various field names)
            for (String field : new String[]{
                    "keyWords","words","themeWords","sightWords",
                    "practiceWords","verbList","adjectives","mathWords","scienceWords"}) {
                if (json.has(field)) {
                    Object val = json.get(field);
                    if (val instanceof JSONArray) {
                        JSONArray arr = (JSONArray) val;
                        if (arr.length() > 0 && arr.get(0) instanceof JSONArray)
                            addPairsSection(arr);
                        else
                            addWordChipsSection("📖", "TODAY'S WORDS",
                                    PILL_BLUE_BG, PILL_BLUE_TEXT, arr);
                    }
                    break;
                }
            }

            // Patterns
            if (json.has("patterns")) {
                addWordChipsSection("🔤", "PATTERNS",
                        PILL_GREEN_BG, PILL_GREEN_TEXT, json.getJSONArray("patterns"));
            }

            // Clusters
            if (json.has("clusters")) addClustersSection(json.getJSONObject("clusters"));

            // Examples with phonics tiles
            if (json.has("examples")) addExamplesSection(json.get("examples"));

            // Practice sentences
            for (String field : new String[]{"contextSentences","sentencePractice","sentenceFrames"}) {
                if (json.has(field)) {
                    addSentencesSection(json.getJSONArray(field));
                    break;
                }
            }

            // Tip
            for (String field : new String[]{"tip","memoryTrick","selfCheck","trick"}) {
                String tip = json.optString(field, null);
                if (tip != null && !tip.isEmpty()) {
                    addTipSection(tip);
                    break;
                }
            }

        } catch (Exception e) {
            addTextSection("📖", "LESSON",
                    PILL_PURPLE_BG, PILL_PURPLE_TEXT, rawJson);
        }
    }

    // ── Section builders ──────────────────────────────────────────────────────

    /** Simple text section (goal, etc.). */
    private void addTextSection(String emoji, String label,
                                int pillBg, int pillText, String text) {
        LinearLayout body = makeCard();
        body.addView(makePill(emoji + "  " + label, pillBg, pillText));
        body.addView(vSpace(12));
        body.addView(makeBodyText(text, 16));
    }

    /** Rule section — rule text is larger and boxed. */
    private void addRuleSection(String rule) {
        LinearLayout body = makeCard();
        body.addView(makePill("📏  THE RULE", PILL_ORANGE_BG, PILL_ORANGE_TEXT));
        body.addView(vSpace(14));

        // Boxed rule text
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = roundRect(PILL_ORANGE_BG, dp(12));
        bg.setStroke(dp(2), 0xFFFFD4B2);
        box.setBackground(bg);
        box.setPadding(dp(16), dp(14), dp(16), dp(14));
        TextView tv = makeBodyText(rule, 18);
        tv.setTypeface(fVisbyBold);
        tv.setTextColor(TEXT_PRIMARY);
        box.addView(tv);
        body.addView(box);
    }

    /** Tip section — highlighted in amber. */
    private void addTipSection(String tip) {
        LinearLayout body = makeCard();
        body.addView(makePill("⭐  REMEMBER THIS", PILL_AMBER_BG, PILL_AMBER_TEXT));
        body.addView(vSpace(12));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = roundRect(PILL_AMBER_BG, dp(12));
        bg.setStroke(dp(2), 0xFFFFD980);
        box.setBackground(bg);
        box.setPadding(dp(16), dp(12), dp(16), dp(12));
        box.addView(makeBodyText(tip, 16));
        body.addView(box);
    }

    /** Word chips displayed 2-per-row for readability. */
    private void addWordChipsSection(String emoji, String label,
                                     int pillBg, int pillText, JSONArray words) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill(emoji + "  " + label, pillBg, pillText));
        body.addView(vSpace(14));

        LinearLayout row = null;
        for (int i = 0; i < words.length(); i++) {
            if (i % 2 == 0) {
                row = makeHRow(dp(10));
                body.addView(row);
            }
            row.addView(makeWordChip(words.getString(i), CHIP_COLORS[i % CHIP_COLORS.length]));
        }
        // Balance last row if odd word count
        if (row != null && words.length() % 2 != 0) {
            View spacer = new View(this);
            spacer.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(spacer);
        }
    }

    /** Synonym/antonym pairs. */
    private void addPairsSection(JSONArray pairs) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill("📖  TODAY'S WORDS", PILL_BLUE_BG, PILL_BLUE_TEXT));
        body.addView(vSpace(14));

        for (int i = 0; i < pairs.length(); i++) {
            JSONArray pair = pairs.getJSONArray(i);
            LinearLayout row = makeHRow(dp(10));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(makeWordChip(pair.getString(0), CHIP_COLORS[i * 2 % CHIP_COLORS.length]));
            TextView arrow = new TextView(this);
            arrow.setText("  ↔  ");
            arrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            arrow.setTypeface(fPoppinsRegular);
            arrow.setTextColor(TEXT_SECONDARY);
            row.addView(arrow);
            row.addView(makeWordChip(pair.getString(1), CHIP_COLORS[(i * 2 + 1) % CHIP_COLORS.length]));
            body.addView(row);
        }
    }

    /** Clusters (e.g. bl/cl → list of words). */
    private void addClustersSection(JSONObject clusters) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill("🔤  CLUSTERS", PILL_GREEN_BG, PILL_GREEN_TEXT));
        body.addView(vSpace(14));

        java.util.Iterator<String> keys = clusters.keys();
        int ci = 0;
        while (keys.hasNext()) {
            String k = keys.next();
            JSONArray arr = clusters.getJSONArray(k);
            LinearLayout row = makeHRow(dp(8));
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView keyTv = new TextView(this);
            keyTv.setText(k.toUpperCase());
            keyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            keyTv.setTypeface(fPoppinsSemi);
            keyTv.setTextColor(PILL_GREEN_TEXT);
            keyTv.setMinWidth(dp(40));
            row.addView(keyTv);

            for (int j = 0; j < arr.length(); j++)
                row.addView(makeWordChip(arr.getString(j), CHIP_COLORS[ci++ % CHIP_COLORS.length]));
            body.addView(row);
        }
    }

    /**
     * Examples section — the core self-learning section.
     *
     * For each example we show:
     *   1. The full word (large, coloured)
     *   2. Phonics letter tiles (consonant = purple, vowel = red)
     *      — matches Orton-Gillingham; students point to each tile and say its sound
     *   3. A self-check prompt: "Cover the tiles. Can you read it?"
     */
    private void addExamplesSection(Object examples) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill("💡  SOUND IT OUT", PILL_INDIGO_BG, PILL_INDIGO_TEXT));
        body.addView(vSpace(6));

        // Tile legend
        body.addView(makeTileLegend());
        body.addView(vSpace(10));

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
                        String[] p = raw.split(" - ", 2);
                        word = p[0].trim(); breakdown = p[1].trim();
                    } else {
                        word = raw;
                    }
                }
                body.addView(makeExampleBlock(word, breakdown, i));
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
                subLabel.setTypeface(fPoppinsSemi);
                subLabel.setTextColor(TEXT_SECONDARY);
                subLabel.setPadding(0, dp(4), 0, dp(6));
                body.addView(subLabel);

                for (int j = 0; j < sub.length(); j++) {
                    String raw = sub.getString(j), w = raw, bd = null;
                    if (raw.contains(" - ")) {
                        String[] p = raw.split(" - ", 2);
                        w = p[0].trim(); bd = p[1].trim();
                    }
                    body.addView(makeExampleBlock(w, bd, i++));
                }
            }
        }
    }

    /** Practice sentences — numbered, each in its own box. */
    private void addSentencesSection(JSONArray sentences) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill("✏️  PRACTICE READING", PILL_ROSE_BG, PILL_ROSE_TEXT));
        body.addView(vSpace(6));

        // Self-paced instruction
        TextView inst = makeBodyText("Read each sentence out loud 3 times.", 13);
        inst.setTextColor(TEXT_SECONDARY);
        // poppins_regular already set by makeBodyText; keep as italic style
        inst.setTypeface(fPoppinsRegular, Typeface.ITALIC);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ip.setMargins(0, dp(4), 0, dp(14));
        inst.setLayoutParams(ip);
        body.addView(inst);

        for (int i = 0; i < sentences.length(); i++) {
            LinearLayout row = makeHRow(dp(10));
            row.setGravity(Gravity.TOP);
            row.setBackground(roundRect(0xFFF9F9F9, dp(12)));
            row.setPadding(dp(14), dp(12), dp(14), dp(12));

            // Number badge
            TextView num = makeCircleBadge(String.valueOf(i + 1),
                    CHIP_COLORS[i % CHIP_COLORS.length], dp(28), 12);
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(dp(28), dp(28));
            np.setMargins(0, dp(1), dp(12), 0);
            num.setLayoutParams(np);
            row.addView(num);

            TextView tv = makeBodyText(sentences.getString(i), 16);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tv);
            body.addView(row);
        }
    }

    // ── Example block (word + phonics tiles) ──────────────────────────────────

    private View makeExampleBlock(String word, String breakdown, int index) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setBackground(roundRect(0xFFF9F9F9, dp(14)));
        box.setPadding(dp(12), dp(16), dp(12), dp(16));

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        bp.setMargins(0, 0, 0, dp(12));
        box.setLayoutParams(bp);

        // Large word
        TextView wordTv = new TextView(this);
        wordTv.setText(word);
        wordTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        wordTv.setTypeface(fVisbyBold);
        wordTv.setTextColor(CHIP_COLORS[index % CHIP_COLORS.length]);
        wordTv.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wp.setMargins(0, 0, 0, dp(14));
        wordTv.setLayoutParams(wp);
        box.addView(wordTv);

        // Phonics tiles
        boolean hasTiles = breakdown != null
                && (breakdown.contains("+") || breakdown.contains("-"));
        if (hasTiles) {
            box.addView(makeLetterTiles(breakdown));

            // Self-check prompt
            TextView check = new TextView(this);
            check.setText("Cover the tiles. Can you read the word?");
            check.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            check.setTextColor(TEXT_SECONDARY);
            check.setTypeface(fPoppinsRegular, Typeface.ITALIC);
            check.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            cp.setMargins(0, dp(10), 0, 0);
            check.setLayoutParams(cp);
            box.addView(check);
        }

        return box;
    }

    /**
     * Phonics letter tiles.
     * Parses "H+a+n+d" or "Ti-ger" → individual tiles.
     * Consonants = purple (#7C3AED), vowels = red (#EF4444).
     */
    private LinearLayout makeLetterTiles(String breakdown) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);

        for (String seg : breakdown.split("[+\\-]")) {
            seg = seg.trim();
            if (seg.isEmpty()) continue;

            boolean isVowel = "aeiouAEIOU".indexOf(seg.charAt(0)) >= 0;
            int tileColor   = isVowel ? TILE_VOWEL : TILE_CONSONANT;

            TextView tile = new TextView(this);
            tile.setText(seg.toUpperCase());
            tile.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            tile.setTypeface(fVisbyBold);
            tile.setTextColor(Color.WHITE);
            tile.setGravity(Gravity.CENTER);

            GradientDrawable bg = new GradientDrawable();
            bg.setColor(tileColor);
            bg.setCornerRadius(dp(8));
            tile.setBackground(bg);

            int w = dp(40 + Math.max(0, (seg.length() - 1) * 10));
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(w, dp(46));
            tp.setMargins(dp(3), 0, dp(3), 0);
            tile.setLayoutParams(tp);
            row.addView(tile);
        }
        return row;
    }

    /** Small legend: purple swatch = consonant, red swatch = vowel. */
    private View makeTileLegend() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(8), 0, 0);
        row.setLayoutParams(lp);

        row.addView(makeSwatch(TILE_CONSONANT));
        row.addView(legendLabel(" consonant    "));
        row.addView(makeSwatch(TILE_VOWEL));
        row.addView(legendLabel(" vowel"));
        return row;
    }

    private View makeSwatch(int color) {
        View v = new View(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setCornerRadius(dp(4));
        v.setBackground(bg);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(14), dp(14)));
        return v;
    }

    private TextView legendLabel(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        tv.setTypeface(fPoppinsRegular);
        tv.setTextColor(TEXT_SECONDARY);
        return tv;
    }

    // ── Card factory ──────────────────────────────────────────────────────────

    /**
     * Creates a white MaterialCardView (0dp elevation, 20dp radius — matches
     * dashboard design) and returns its content LinearLayout.
     */
    private LinearLayout makeCard() {
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(cp);
        card.setRadius(dp(20));
        card.setCardElevation(0);
        card.setStrokeWidth(0);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(20));
        card.addView(content);
        llContentSections.addView(card);
        return content;
    }

    // ── View helpers ──────────────────────────────────────────────────────────

    /**
     * Section label pill — matches the stat badge style in the dashboard
     * (soft pastel bg, dark accent text, 12dp corners).
     */
    private TextView makePill(String text, int bgColor, int textColor) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTypeface(fPoppinsSemi);
        tv.setTextColor(textColor);
        tv.setLetterSpacing(0.04f);
        tv.setPadding(dp(10), dp(6), dp(10), dp(6));
        tv.setBackground(roundRect(bgColor, dp(12)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        return tv;
    }

    private TextView makeBodyText(String text, int spSize) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, spSize);
        tv.setTypeface(fPoppinsRegular);
        tv.setTextColor(TEXT_PRIMARY);
        tv.setLineSpacing(0f, 1.5f);
        return tv;
    }

    private TextView makeWordChip(String word, int color) {
        TextView chip = new TextView(this);
        chip.setText(word);
        chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        chip.setTypeface(fVisbyBold);
        chip.setTextColor(Color.WHITE);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(8), dp(12), dp(8), dp(12));
        chip.setBackground(roundRect(color, dp(16)));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(dp(4), 0, dp(4), 0);
        chip.setLayoutParams(lp);
        return chip;
    }

    private TextView makeCircleBadge(String text, int color, int sizePx, int spSize) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, spSize);
        tv.setTypeface(fPoppinsSemi);
        tv.setTextColor(Color.WHITE);
        tv.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(color);
        bg.setShape(GradientDrawable.OVAL);
        tv.setBackground(bg);
        tv.setLayoutParams(new LinearLayout.LayoutParams(sizePx, sizePx));
        return tv;
    }

    private LinearLayout makeHRow(int bottomMarginPx) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, bottomMarginPx);
        row.setLayoutParams(lp);
        return row;
    }

    /** Transparent vertical spacer. */
    private View vSpace(int dpValue) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(dpValue)));
        return v;
    }

    private GradientDrawable roundRect(int color, int radiusPx) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radiusPx);
        return d;
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    private int dp(int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    // ── Complete lesson ───────────────────────────────────────────────────────

    private void markLessonCompleted() {
        progressBar.setVisibility(View.VISIBLE);
        btnComplete.setEnabled(false);

        UpdateProgressRequest request = new UpdateProgressRequest(
                sessionManager.getStudentId(), nodeId, "lesson");

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.updateNodeProgress(request).enqueue(new Callback<UpdateProgressResponse>() {
            @Override
            public void onResponse(Call<UpdateProgressResponse> call,
                                   Response<UpdateProgressResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null
                        && response.body().isSuccess()) {
                    Toast.makeText(LessonContentActivity.this,
                            "Lesson complete! Moving to game...", Toast.LENGTH_SHORT).show();
                    android.content.Intent resultData = new android.content.Intent();
                    if (cachedLessonContent != null) {
                        resultData.putExtra("lesson_content", cachedLessonContent);
                    }
                    setResult(RESULT_OK, resultData);
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