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
    private boolean isSupplemental;
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
        isSupplemental   = getIntent().getBooleanExtra("is_supplemental", false);
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
        btnBack.setOnClickListener(v -> cancelLesson());
        btnComplete.setOnClickListener(v -> markLessonCompleted());
    }

    @Override
    public void onBackPressed() {
        cancelLesson();
    }

    private void cancelLesson() {
        setResult(RESULT_CANCELED);
        finish();
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

            // ── Rule / instruction ────────────────────────────────────────────
            String rule = json.optString("rule", null);
            if (rule == null) rule = json.optString("instruction", null);
            if (rule != null && !rule.isEmpty()) addRuleSection(rule);

            // ── Strategy (comprehension / writing lessons) ────────────────────
            String strategy = json.optString("strategy", null);
            if (strategy != null && !strategy.isEmpty()) {
                addTipSection(strategy);
            }

            // ── Single definition string (verbs, adjectives lessons) ──────────
            String definition = json.optString("definition", null);
            if (definition != null && !definition.isEmpty()) {
                addTextSection("📖", "DEFINITION", PILL_BLUE_BG, PILL_BLUE_TEXT, definition);
            }

            // ── Word chip sections — each field gets its own card ─────────────
            // (no break: mathWords AND scienceWords both shown for lesson 203)
            String[][] wordFields = {
                    {"keyWords",     "📖", "TODAY'S WORDS"},
                    {"words",        "📖", "TODAY'S WORDS"},
                    {"themeWords",   "🌏", "THEME WORDS"},
                    {"sightWords",   "👁️", "SIGHT WORDS"},
                    {"practiceWords","✏️", "PRACTICE WORDS"},
                    {"verbList",     "🏃", "ACTION WORDS (VERBS)"},
                    {"adjectives",   "🎨", "DESCRIBING WORDS (ADJECTIVES)"},
                    {"mathWords",    "🔢", "MATH WORDS"},
                    {"scienceWords", "🔬", "SCIENCE WORDS"},
                    {"nationalThemeWords", "🇵🇭", "NATIONAL THEME WORDS"},
                    {"themes",       "🌟", "THEMES"},
                    {"vocabulary",   "📚", "VOCABULARY"},
                    {"questionWords","❓", "QUESTION WORDS"},
                    {"markers",      "🔗", "SEQUENCE MARKERS"},
                    {"clueWords",    "🔍", "CLUE WORDS"},
                    {"sentenceStarters", "✏️", "SENTENCE STARTERS"},
                    {"connectionTypes",  "🔗", "CONNECTION TYPES"},
                    {"reflectionStarters","💭","REFLECTION STARTERS"},
                    {"questionStarters", "❓", "QUESTION STARTERS"},
            };
            for (String[] entry : wordFields) {
                String field = entry[0], emoji = entry[1], label = entry[2];
                if (!json.has(field)) continue;
                Object val = json.get(field);
                if (!(val instanceof JSONArray)) continue;
                JSONArray arr = (JSONArray) val;
                if (arr.length() == 0) continue;
                if (arr.get(0) instanceof JSONArray) {
                    // nested array → pairs (e.g. commonErrors)
                    addNamedPairsSection(emoji, label, PILL_BLUE_BG, PILL_BLUE_TEXT, arr, "  →  ");
                } else {
                    addWordChipsSection(emoji, label, PILL_BLUE_BG, PILL_BLUE_TEXT, arr);
                }
            }

            // ── Phonics-specific word chip sections ───────────────────────────
            if (json.has("vcvExamples")) {
                addWordChipsSection("🔵", "VCV EXAMPLES (Long Vowel)",
                        PILL_INDIGO_BG, PILL_INDIGO_TEXT, json.getJSONArray("vcvExamples"));
            }
            if (json.has("vccvExamples")) {
                addWordChipsSection("🟣", "VCCV EXAMPLES (Short Vowel)",
                        PILL_PURPLE_BG, PILL_PURPLE_TEXT, json.getJSONArray("vccvExamples"));
            }
            if (json.has("diphthongs")) {
                addWordChipsSection("🔊", "DIPHTHONGS",
                        PILL_GREEN_BG, PILL_GREEN_TEXT, json.getJSONArray("diphthongs"));
            }
            // Antonyms as "near-far" string pairs (lesson 108)
            if (json.has("antonyms") && json.get("antonyms") instanceof JSONArray) {
                addStringPairsSection(json.getJSONArray("antonyms"));
            }

            // ── Patterns ──────────────────────────────────────────────────────
            if (json.has("patterns")) {
                addWordChipsSection("🔤", "PATTERNS",
                        PILL_GREEN_BG, PILL_GREEN_TEXT, json.getJSONArray("patterns"));
            }

            // ── Clusters ──────────────────────────────────────────────────────
            if (json.has("clusters")) {
                addClustersSection("🔤", "CLUSTERS",
                        PILL_GREEN_BG, PILL_GREEN_TEXT, json.getJSONObject("clusters"));
            }

            // ── Word families (lesson 210) ────────────────────────────────────
            if (json.has("wordFamilies")) {
                addClustersSection("📚", "WORD FAMILIES",
                        PILL_INDIGO_BG, PILL_INDIGO_TEXT, json.getJSONObject("wordFamilies"));
            }

            // ── Noun types & gender (lesson 204) ─────────────────────────────
            if (json.has("nounTypes")) {
                addClustersSection("🏷️", "NOUN TYPES",
                        PILL_BLUE_BG, PILL_BLUE_TEXT, json.getJSONObject("nounTypes"));
            }
            if (json.has("gender")) {
                addClustersSection("👥", "GENDER OF NOUNS",
                        PILL_ROSE_BG, PILL_ROSE_TEXT, json.getJSONObject("gender"));
            }

            // ── Definitions JSONObject (key → description) ────────────────────
            if (json.has("definitions") && json.get("definitions") instanceof JSONObject) {
                addDefinitionsSection("📖", "DEFINITIONS",
                        PILL_BLUE_BG, PILL_BLUE_TEXT, json.getJSONObject("definitions"));
            }
            if (json.has("suffixes") && json.get("suffixes") instanceof JSONObject) {
                addDefinitionsSection("📝", "SUFFIXES",
                        PILL_PURPLE_BG, PILL_PURPLE_TEXT, json.getJSONObject("suffixes"));
            }

            // ── Conjunction / marker / signal word definitions ─────────────────
            // Only render as definitions if they are JSONObjects (key → string)
            String[][] defObjectFields = {
                    {"conjunctions",       "🔗", "CONJUNCTIONS"},
                    {"signalWords",        "📍", "SIGNAL WORDS"},
                    {"demonstrativeRules", "👆", "DEMONSTRATIVE PRONOUNS"},
                    {"sentenceTypes",      "📋", "SENTENCE TYPES"},
                    {"pitchTypes",         "🎵", "PITCH / INTONATION"},
                    {"comparisonForms",    "⚖️", "COMPARISON FORMS"},
                    {"questionWords",      "❓", "QUESTION WORDS"},
            };
            for (String[] entry : defObjectFields) {
                String field = entry[0], emoji = entry[1], label = entry[2];
                if (json.has(field) && json.get(field) instanceof JSONObject) {
                    JSONObject obj = json.getJSONObject(field);
                    // Only show as definitions if values are Strings (not nested objects)
                    if (obj.length() > 0) {
                        Object firstVal = obj.opt(obj.keys().next());
                        if (firstVal instanceof String) {
                            addDefinitionsSection(emoji, label,
                                    PILL_INDIGO_BG, PILL_INDIGO_TEXT, obj);
                        }
                    }
                }
            }

            // ── Sentence types with sub-structure (lessons 304, 305) ──────────
            // types: {declarative: {definition, endMark, examples}, ...}
            if (json.has("types") && json.get("types") instanceof JSONObject) {
                addTypesSection(json.getJSONObject("types"));
            }

            // ── Synonym / antonym pairs ───────────────────────────────────────
            if (json.has("synonymPairs")) {
                addNamedPairsSection("🔗", "SYNONYMS (same meaning)",
                        PILL_GREEN_BG, PILL_GREEN_TEXT,
                        json.getJSONArray("synonymPairs"), "  ≈  ");
            }
            if (json.has("antonymPairs")) {
                addNamedPairsSection("⚡", "ANTONYMS (opposite meaning)",
                        PILL_ROSE_BG, PILL_ROSE_TEXT,
                        json.getJSONArray("antonymPairs"), "  ↔  ");
            }

            // ── Root word examples [word, root] pairs (lesson 212) ────────────
            if (json.has("rootWordExamples")) {
                addNamedPairsSection("🌱", "ROOT WORDS",
                        PILL_GREEN_BG, PILL_GREEN_TEXT,
                        json.getJSONArray("rootWordExamples"), " → ");
            }

            // ── Common spelling errors (lesson 211) ──────────────────────────
            if (json.has("commonErrors")) {
                addCommonErrorsSection(json.getJSONArray("commonErrors"));
            }

            // ── Examples with phonics tiles ───────────────────────────────────
            if (json.has("examples")) addExamplesSection(json.get("examples"));

            // ── Practice sentences ────────────────────────────────────────────
            for (String field : new String[]{
                    "contextSentences","sentencePractice","sentenceFrames","sentenceExamples"}) {
                if (json.has(field) && json.get(field) instanceof JSONArray) {
                    addSentencesSection(json.getJSONArray(field));
                    break;
                }
            }

            // ── Bullet list sections ──────────────────────────────────────────
            String[][] bulletFields = {
                    {"skills",    "✅", "SKILLS"},
                    {"checklist", "☑️", "CHECKLIST"},
                    {"rules",     "📏", "RULES"},
                    {"tips",      "⭐", "TIPS"},
            };
            for (String[] entry : bulletFields) {
                String field = entry[0], emoji = entry[1], label = entry[2];
                if (json.has(field) && json.get(field) instanceof JSONArray) {
                    addBulletListSection(emoji, label,
                            PILL_GREEN_BG, PILL_GREEN_TEXT, json.getJSONArray(field));
                }
            }

            // ── Reading passage (comprehension lessons 401-412) ───────────────
            Object practiceVal = json.opt("practice");
            if (practiceVal instanceof String) {
                String practice = (String) practiceVal;
                if (!practice.isEmpty()) addPassageSection(practice);
            }

            // ── Example string (writing lessons) ─────────────────────────────
            String example = json.optString("example", null);
            if (example != null && !example.isEmpty()) {
                addTextSection("📝", "EXAMPLE", PILL_ORANGE_BG, PILL_ORANGE_TEXT, example);
            }

            // ── Tip (always last) ─────────────────────────────────────────────
            for (String field : new String[]{"tip","memoryTrick","selfCheck","trick","usage"}) {
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

    /** Simple text section (goal, definition, example string, etc.). */
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

    /** Tip / strategy section — highlighted in amber. */
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

    /**
     * Reading passage card — used for comprehension lessons (401-412).
     * Shows a story/passage that students read before answering questions.
     */
    private void addPassageSection(String passage) {
        LinearLayout body = makeCard();
        body.addView(makePill("📖  READ THIS", PILL_INDIGO_BG, PILL_INDIGO_TEXT));
        body.addView(vSpace(12));

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = roundRect(PILL_INDIGO_BG, dp(12));
        bg.setStroke(dp(2), 0xFFC7D2FE);
        box.setBackground(bg);
        box.setPadding(dp(18), dp(16), dp(18), dp(16));

        TextView tv = makeBodyText(passage, 17);
        tv.setLineSpacing(dp(4), 1.5f);
        box.addView(tv);
        body.addView(box);

        // Instruction for self-paced reading
        TextView inst = makeBodyText("Read the passage carefully before answering.", 13);
        inst.setTextColor(TEXT_SECONDARY);
        inst.setTypeface(fPoppinsRegular, Typeface.ITALIC);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ip.setMargins(0, dp(10), 0, 0);
        inst.setLayoutParams(ip);
        body.addView(inst);
    }

    /** Bullet list section (skills, checklist, rules, tips arrays). */
    private void addBulletListSection(String emoji, String label,
                                      int pillBg, int pillText, JSONArray items) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill(emoji + "  " + label, pillBg, pillText));
        body.addView(vSpace(12));

        for (int i = 0; i < items.length(); i++) {
            String item = items.getString(i);
            LinearLayout row = makeHRow(dp(8));
            row.setGravity(Gravity.TOP);

            TextView bullet = new TextView(this);
            bullet.setText("•");
            bullet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            bullet.setTypeface(fPoppinsSemi);
            bullet.setTextColor(pillText);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    dp(20), LinearLayout.LayoutParams.WRAP_CONTENT);
            bp.setMargins(0, dp(1), dp(8), 0);
            bullet.setLayoutParams(bp);
            row.addView(bullet);

            TextView tv = makeBodyText(item, 15);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(tv);
            body.addView(row);
        }
    }

    /** Definitions section — renders a JSONObject as key: description pairs. */
    private void addDefinitionsSection(String emoji, String label,
                                       int pillBg, int pillText, JSONObject defs) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill(emoji + "  " + label, pillBg, pillText));
        body.addView(vSpace(12));

        java.util.Iterator<String> keys = defs.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object val = defs.opt(key);
            if (!(val instanceof String)) continue;

            LinearLayout row = makeHRow(dp(10));
            row.setBackground(roundRect(0xFFF9F9F9, dp(10)));
            row.setPadding(dp(12), dp(10), dp(12), dp(10));
            row.setGravity(Gravity.TOP);

            TextView keyTv = new TextView(this);
            keyTv.setText(capitalize(key));
            keyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            keyTv.setTypeface(fPoppinsSemi);
            keyTv.setTextColor(pillText);
            LinearLayout.LayoutParams kp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            kp.setMargins(0, 0, dp(8), 0);
            keyTv.setLayoutParams(kp);
            row.addView(keyTv);

            TextView sep = new TextView(this);
            sep.setText("—");
            sep.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            sep.setTypeface(fPoppinsRegular);
            sep.setTextColor(TEXT_SECONDARY);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            sp.setMargins(0, 0, dp(8), 0);
            sep.setLayoutParams(sp);
            row.addView(sep);

            TextView valTv = makeBodyText((String) val, 14);
            valTv.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(valTv);
            body.addView(row);
        }
    }

    /**
     * Types section for lesson 304/305 — each type (declarative, interrogative, etc.)
     * shows its definition, end mark, and example sentences.
     */
    private void addTypesSection(JSONObject types) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill("📋  SENTENCE TYPES", PILL_INDIGO_BG, PILL_INDIGO_TEXT));
        body.addView(vSpace(14));

        int ci = 0;
        java.util.Iterator<String> keys = types.keys();
        while (keys.hasNext()) {
            String typeName = keys.next();
            Object val = types.opt(typeName);
            if (!(val instanceof JSONObject)) continue;
            JSONObject typeObj = (JSONObject) val;

            // Type header
            LinearLayout typeHeader = new LinearLayout(this);
            typeHeader.setOrientation(LinearLayout.HORIZONTAL);
            typeHeader.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            hp.setMargins(0, ci > 0 ? dp(14) : 0, 0, dp(6));
            typeHeader.setLayoutParams(hp);

            TextView nameChip = new TextView(this);
            nameChip.setText(capitalize(typeName));
            nameChip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            nameChip.setTypeface(fPoppinsSemi);
            nameChip.setTextColor(Color.WHITE);
            nameChip.setPadding(dp(10), dp(5), dp(10), dp(5));
            nameChip.setBackground(roundRect(CHIP_COLORS[ci % CHIP_COLORS.length], dp(10)));
            LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            np.setMargins(0, 0, dp(8), 0);
            nameChip.setLayoutParams(np);
            typeHeader.addView(nameChip);

            String endMark = typeObj.optString("endMark", "");
            if (!endMark.isEmpty()) {
                TextView endTv = new TextView(this);
                endTv.setText("ends with: " + endMark);
                endTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
                endTv.setTypeface(fPoppinsRegular);
                endTv.setTextColor(TEXT_SECONDARY);
                typeHeader.addView(endTv);
            }
            body.addView(typeHeader);

            String defText = typeObj.optString("definition", "");
            if (!defText.isEmpty()) {
                TextView defTv = makeBodyText(defText, 14);
                defTv.setTextColor(TEXT_SECONDARY);
                LinearLayout.LayoutParams dp_lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                dp_lp.setMargins(0, 0, 0, dp(6));
                defTv.setLayoutParams(dp_lp);
                body.addView(defTv);
            }

            JSONArray examples = typeObj.optJSONArray("examples");
            if (examples != null) {
                for (int i = 0; i < examples.length(); i++) {
                    LinearLayout exRow = makeHRow(dp(4));
                    exRow.setGravity(Gravity.CENTER_VERTICAL);
                    exRow.setBackground(roundRect(0xFFF9F9F9, dp(8)));
                    exRow.setPadding(dp(10), dp(8), dp(10), dp(8));

                    TextView bullet = new TextView(this);
                    bullet.setText("▸  ");
                    bullet.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    bullet.setTypeface(fPoppinsSemi);
                    bullet.setTextColor(CHIP_COLORS[ci % CHIP_COLORS.length]);
                    exRow.addView(bullet);

                    TextView exTv = makeBodyText(examples.getString(i), 15);
                    exTv.setLayoutParams(new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    exRow.addView(exTv);
                    body.addView(exRow);
                }
            }
            ci++;
        }
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

    /**
     * Named synonym/antonym/root-word pairs with a custom connector string.
     * Each inner array: [word1, word2]
     */
    private void addNamedPairsSection(String emoji, String label,
                                      int pillBg, int pillText,
                                      JSONArray pairs, String connector) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill(emoji + "  " + label, pillBg, pillText));
        body.addView(vSpace(14));

        for (int i = 0; i < pairs.length(); i++) {
            JSONArray pair = pairs.getJSONArray(i);
            if (pair.length() < 2) continue;
            LinearLayout row = makeHRow(dp(10));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(makeWordChip(pair.getString(0), CHIP_COLORS[i * 2 % CHIP_COLORS.length]));
            TextView conn = new TextView(this);
            conn.setText(connector);
            conn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            conn.setTypeface(fPoppinsRegular);
            conn.setTextColor(TEXT_SECONDARY);
            conn.setGravity(Gravity.CENTER);
            conn.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.addView(conn);
            row.addView(makeWordChip(pair.getString(1), CHIP_COLORS[(i * 2 + 1) % CHIP_COLORS.length]));
            body.addView(row);
        }
    }

    /**
     * Antonyms as "near-far" strings (lesson 108).
     * Each element: "near-far" → shown as two word chips with ↔.
     */
    private void addStringPairsSection(JSONArray antonyms) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill("↔  ANTONYMS", PILL_ROSE_BG, PILL_ROSE_TEXT));
        body.addView(vSpace(14));

        for (int i = 0; i < antonyms.length(); i++) {
            String raw = antonyms.getString(i);
            String[] parts = raw.split("-", 2);
            if (parts.length < 2) continue;
            LinearLayout row = makeHRow(dp(10));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(makeWordChip(parts[0].trim(), CHIP_COLORS[i * 2 % CHIP_COLORS.length]));
            TextView conn = new TextView(this);
            conn.setText("  ↔  ");
            conn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            conn.setTypeface(fPoppinsRegular);
            conn.setTextColor(TEXT_SECONDARY);
            conn.setGravity(Gravity.CENTER);
            row.addView(conn);
            row.addView(makeWordChip(parts[1].trim(), CHIP_COLORS[(i * 2 + 1) % CHIP_COLORS.length]));
            body.addView(row);
        }
    }

    /**
     * Common spelling errors (lesson 211).
     * Each inner array: [correct, wrong1, wrong2]
     * Shows the correct spelling highlighted green, wrong ones in red-ish.
     */
    private void addCommonErrorsSection(JSONArray errors) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill("✏️  CORRECT SPELLING", PILL_GREEN_BG, PILL_GREEN_TEXT));
        body.addView(vSpace(14));

        for (int i = 0; i < errors.length(); i++) {
            JSONArray row_arr = errors.getJSONArray(i);
            if (row_arr.length() == 0) continue;

            LinearLayout row = makeHRow(dp(10));
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackground(roundRect(0xFFF9F9F9, dp(10)));
            row.setPadding(dp(10), dp(8), dp(10), dp(8));

            // First item is correct spelling
            TextView correct = new TextView(this);
            correct.setText("✓ " + row_arr.getString(0));
            correct.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            correct.setTypeface(fVisbyBold);
            correct.setTextColor(0xFF15803D);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            correct.setLayoutParams(cp);
            row.addView(correct);

            // Remaining items are wrong spellings
            StringBuilder wrongs = new StringBuilder("✗ ");
            for (int j = 1; j < row_arr.length(); j++) {
                if (j > 1) wrongs.append(", ");
                wrongs.append(row_arr.getString(j));
            }
            TextView wrong = new TextView(this);
            wrong.setText(wrongs.toString());
            wrong.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            wrong.setTypeface(fPoppinsRegular);
            wrong.setTextColor(0xFFBE123C);
            wrong.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            row.addView(wrong);
            body.addView(row);
        }
    }

    /** Clusters (e.g. bl/cl → list of words). */
    private void addClustersSection(String emoji, String label,
                                    int pillBg, int pillText,
                                    JSONObject clusters) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill(emoji + "  " + label, pillBg, pillText));
        body.addView(vSpace(14));

        java.util.Iterator<String> keys = clusters.keys();
        int ci = 0;
        while (keys.hasNext()) {
            String k = keys.next();
            Object val = clusters.opt(k);
            if (!(val instanceof JSONArray)) continue;
            JSONArray arr = (JSONArray) val;

            LinearLayout row = makeHRow(dp(8));
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView keyTv = new TextView(this);
            keyTv.setText(k.toUpperCase());
            keyTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
            keyTv.setTypeface(fPoppinsSemi);
            keyTv.setTextColor(pillText);
            keyTv.setMinWidth(dp(52));
            row.addView(keyTv);

            for (int j = 0; j < arr.length(); j++) {
                row.addView(makeWordChip(arr.getString(j), CHIP_COLORS[ci++ % CHIP_COLORS.length]));
            }
            body.addView(row);
        }
    }

    /**
     * Examples section — the core self-learning section.
     *
     * Handles:
     *  - JSONArray of Strings: "Hand - H+a+n+d"
     *  - JSONArray of Objects: {word, division} or {sentence, subject, predicate}
     *                          or {compound, simple} (grammar lessons)
     *  - JSONObject of sub-arrays: {math: [...], science: [...]}
     *  - JSONArray of pairs: [[scrambled, correct]] (lesson 302)
     */
    private void addExamplesSection(Object examples) throws Exception {
        if (examples instanceof JSONArray) {
            JSONArray arr = (JSONArray) examples;
            if (arr.length() == 0) return;

            // Check if it's an array of arrays (word-order pairs for lesson 302)
            if (arr.get(0) instanceof JSONArray) {
                addNamedPairsSection("🔀", "WORD ORDER EXAMPLES",
                        PILL_ORANGE_BG, PILL_ORANGE_TEXT, arr, "  →  ");
                return;
            }

            // Check type of first element if it's an object
            if (arr.get(0) instanceof JSONObject) {
                JSONObject first = arr.getJSONObject(0);
                if (first.has("sentence") || first.has("compound")) {
                    // Grammar structured examples (303, 306, 309, 312)
                    addGrammarExamplesSection(arr);
                    return;
                }
            }

            // Standard phonics examples (string or {word, division} objects)
            LinearLayout body = makeCard();
            body.addView(makePill("💡  SOUND IT OUT", PILL_INDIGO_BG, PILL_INDIGO_TEXT));
            body.addView(vSpace(6));
            body.addView(makeTileLegend());
            body.addView(vSpace(10));

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
            // Sub-categorised examples: {math: [...], science: [...]} or {correct: [...], incorrect: [...]}
            JSONObject exObj = (JSONObject) examples;
            LinearLayout body = makeCard();
            body.addView(makePill("💡  EXAMPLES", PILL_INDIGO_BG, PILL_INDIGO_TEXT));
            body.addView(vSpace(10));

            java.util.Iterator<String> keys = exObj.keys();
            int i = 0;
            while (keys.hasNext()) {
                String k = keys.next();
                Object subVal = exObj.opt(k);
                if (!(subVal instanceof JSONArray)) continue;
                JSONArray sub = (JSONArray) subVal;

                // Sub-category label
                TextView subLabel = new TextView(this);
                subLabel.setText(capitalize(k) + ":");
                subLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                subLabel.setTypeface(fPoppinsSemi);
                subLabel.setTextColor(CHIP_COLORS[i % CHIP_COLORS.length]);
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

    /**
     * Grammar structured examples — handles:
     * - {sentence, subject, predicate} (lesson 303)
     * - {compound, simple:[]} (lesson 306)
     * - {compound, part1:{subject,verb}, part2:{subject,verb}} (lesson 309)
     * - {sentence, pitch} (lesson 312)
     */
    private void addGrammarExamplesSection(JSONArray arr) throws Exception {
        LinearLayout body = makeCard();
        body.addView(makePill("💡  EXAMPLES", PILL_INDIGO_BG, PILL_INDIGO_TEXT));
        body.addView(vSpace(12));

        for (int i = 0; i < arr.length(); i++) {
            if (!(arr.get(i) instanceof JSONObject)) continue;
            JSONObject obj = arr.getJSONObject(i);

            LinearLayout box = new LinearLayout(this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setBackground(roundRect(0xFFF9F9F9, dp(12)));
            box.setPadding(dp(14), dp(12), dp(14), dp(12));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            bp.setMargins(0, 0, 0, dp(10));
            box.setLayoutParams(bp);

            // Main sentence / compound
            String mainText = obj.optString("sentence",
                    obj.optString("compound", ""));
            if (!mainText.isEmpty()) {
                TextView tv = makeBodyText(mainText, 17);
                tv.setTypeface(fVisbyBold);
                tv.setTextColor(CHIP_COLORS[i % CHIP_COLORS.length]);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 0, 0, dp(6));
                tv.setLayoutParams(lp);
                box.addView(tv);
            }

            // Subject / predicate (lesson 303)
            String subject   = obj.optString("subject", "");
            String predicate = obj.optString("predicate", "");
            if (!subject.isEmpty() || !predicate.isEmpty()) {
                TextView detail = makeBodyText(
                        "Subject: " + subject + "   |   Predicate: " + predicate, 13);
                detail.setTextColor(TEXT_SECONDARY);
                box.addView(detail);
            }

            // Simple sentences (lesson 306 compound)
            JSONArray simpleArr = obj.optJSONArray("simple");
            if (simpleArr != null) {
                StringBuilder sb = new StringBuilder("Simple: ");
                for (int j = 0; j < simpleArr.length(); j++) {
                    if (j > 0) sb.append(" + ");
                    sb.append(simpleArr.getString(j));
                }
                TextView detail = makeBodyText(sb.toString(), 13);
                detail.setTextColor(TEXT_SECONDARY);
                box.addView(detail);
            }

            // Part1/Part2 subjects and verbs (lesson 309)
            JSONObject p1 = obj.optJSONObject("part1");
            JSONObject p2 = obj.optJSONObject("part2");
            if (p1 != null || p2 != null) {
                String detail1 = p1 != null ?
                        p1.optString("subject","") + " " + p1.optString("verb","") : "";
                String detail2 = p2 != null ?
                        p2.optString("subject","") + " " + p2.optString("verb","") : "";
                TextView detail = makeBodyText("Part 1: " + detail1 + "   |   Part 2: " + detail2, 13);
                detail.setTextColor(TEXT_SECONDARY);
                box.addView(detail);
            }

            // Pitch (lesson 312)
            String pitch = obj.optString("pitch", "");
            if (!pitch.isEmpty()) {
                TextView pitchTv = makeBodyText("Pitch: " + pitch, 13);
                pitchTv.setTextColor(TEXT_SECONDARY);
                box.addView(pitchTv);
            }

            body.addView(box);
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
                            isSupplemental ? "Intervention complete! Returning to map..." : "Lesson complete! Moving to game...",
                            Toast.LENGTH_SHORT).show();
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
