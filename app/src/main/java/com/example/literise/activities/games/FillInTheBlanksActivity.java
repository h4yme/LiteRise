package com.example.literise.activities.games;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.SaveGameResultRequest;
import com.example.literise.models.SaveGameResultResponse;
import com.example.literise.utils.DemoDataProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FillInTheBlanksActivity extends BaseGameActivity {

    // UI – Header
    private ImageView btnBack;
    private TextView tvModuleLabel;
    private TextView tvProgress;
    private TextView tvScore;
    private ProgressBar progressBar;

    // UI – Timer
    private TextView tvTimer;
    private ProgressBar timerProgress;

    // UI – Question
    private TextView tvQuestionBefore;
    private TextView tvBlank;
    private TextView tvQuestionAfter;

    // UI – Word Options
    private RecyclerView recyclerWordOptions;
    private MaterialButton btnCheckAnswer;

    // Game State
    private List<DemoDataProvider.FillQuestion> questions;
    private int currentIndex = 0;
    private int correctAnswers = 0;
    private int score = 0;
    private String selectedWord = null;
    private boolean isAnswerLocked = false;
    private CountDownTimer countDownTimer;
    private static final long TOTAL_TIME = 180000; // 3 min
    private long gameStartTime;

    // Adapter
    private WordOptionAdapter adapter;
    private List<String> currentOptions;

    // Session & API
    private SessionManager session;
    private ApiService apiService;
    private int nodeId;
    private String moduleDomain;

    // Lottie
    private LottieAnimationView lottieCorrect, lottieComplete;

    // Module theming
    private String colorStart = "#7C3AED";
    private String colorEnd = "#4F46E5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_in_the_blanks);

        session = new SessionManager(this);
        apiService = ApiClient.getClient(this).create(ApiService.class);

        nodeId = getIntent().getIntExtra("node_id", 0);
        moduleDomain = getIntent().getStringExtra("module_domain");
        String moduleName = getIntent().getStringExtra("module_name");
        String cs = getIntent().getStringExtra("module_color_start");
        String ce = getIntent().getStringExtra("module_color_end");
        if (cs != null && !cs.isEmpty()) colorStart = cs;
        if (ce != null && !ce.isEmpty()) colorEnd = ce;

        initViews();
        applyModuleTheme(moduleName);
        loadQuestions();
        startTimer();
        gameStartTime = System.currentTimeMillis();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tvModuleLabel = findViewById(R.id.tvModuleLabel);
        tvProgress = findViewById(R.id.tvProgress);
        tvScore = findViewById(R.id.tvScore);
        progressBar = findViewById(R.id.progressBar);
        tvTimer = findViewById(R.id.tvTimer);
        timerProgress = findViewById(R.id.timerProgress);
        tvQuestionBefore = findViewById(R.id.tvQuestionBefore);
        tvBlank = findViewById(R.id.tvBlank);
        tvQuestionAfter = findViewById(R.id.tvQuestionAfter);
        recyclerWordOptions = findViewById(R.id.recyclerWordOptions);
        btnCheckAnswer = findViewById(R.id.btnCheckAnswer);
        lottieCorrect = findViewById(R.id.lottieCorrect);
        lottieComplete = findViewById(R.id.lottieComplete);

        btnBack.setOnClickListener(v -> {
            if (countDownTimer != null) countDownTimer.cancel();
            setResult(RESULT_CANCELED);
            finish();
        });
        btnCheckAnswer.setOnClickListener(v -> checkAnswer());
        btnCheckAnswer.setEnabled(false);
    }

    private void applyModuleTheme(String moduleName) {
        // Header gradient
        View headerCard = findViewById(R.id.headerCard);
        if (headerCard != null) {
            applyGradient(headerCard, colorStart, colorEnd, 0f);
        }
        // Word bank gradient
        View wordBankCard = findViewById(R.id.wordBankCard);
        if (wordBankCard != null) {
            applyGradient(wordBankCard, colorStart, colorEnd, 40f);
        }
        // Timer tint
        if (timerProgress != null) {
            try {
                timerProgress.setProgressTintList(
                        android.content.res.ColorStateList.valueOf(Color.parseColor(colorStart)));
            } catch (Exception ignored) {}
        }
        // Module label
        if (tvModuleLabel != null && moduleName != null) {
            tvModuleLabel.setText(moduleName);
        }
    }

    private void applyGradient(View view, String start, String end, float radius) {
        try {
            GradientDrawable g = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    new int[]{Color.parseColor(start), Color.parseColor(end)});
            g.setCornerRadius(radius * getResources().getDisplayMetrics().density);
            view.setBackground(g);
        } catch (Exception ignored) {}
    }

    private void loadQuestions() {
        // Try to generate questions from the lesson the student just completed
        try {
            String lessonContent = getIntent().getStringExtra("lesson_content");
            if (lessonContent != null) {
                List<DemoDataProvider.FillQuestion> lessonQs = generateQuestionsFromLesson(lessonContent);
                if (lessonQs != null && !lessonQs.isEmpty()) {
                    questions = lessonQs;
                    loadQuestion(0);
                    return;
                }
            }
        } catch (Exception ignored) {}

        // Fall back to domain-based demo questions
        questions = DemoDataProvider.getFillQuestions(moduleDomain, 10);
        if (questions == null || questions.isEmpty()) {
            questions = DemoDataProvider.getFillQuestions("creating", 10);
        }
        loadQuestion(0);
    }

    /**
     * Generates fill-in-the-blank questions from lesson JSON.
     *
     * Branch A — phonics/rule lessons with an "examples" array
     *   e.g. VCV: ["Ti-ger","Pa-per","Ba-sin",...] → syllable-splitting questions
     *
     * Branch B — vocabulary lessons with a word list + practice sentences
     */
    private List<DemoDataProvider.FillQuestion> generateQuestionsFromLesson(String lessonJson) throws Exception {
        org.json.JSONObject obj = new org.json.JSONObject(lessonJson);

        // ── BRANCH A: phonics lessons with "examples" array ───────────────────
        if (obj.has("examples")) {
            org.json.JSONArray exArr = obj.getJSONArray("examples");
            String ruleHint = obj.optString("rule", "");
            // Trim long rule to one readable clause
            if (ruleHint.contains(".")) ruleHint = ruleHint.substring(0, ruleHint.indexOf(".")).trim();
            if (ruleHint.length() > 60) ruleHint = ruleHint.substring(0, 60).trim();

            List<DemoDataProvider.FillQuestion> result = new ArrayList<>();
            for (int i = 0; i < exArr.length(); i++) {
                String hyphenated = exArr.getString(i).trim(); // e.g. "Ti-ger"
                if (!hyphenated.contains("-")) continue;
                String baseWord = hyphenated.replace("-", "").toLowerCase(); // "tiger"
                String correct  = hyphenated;                                // "Ti-ger"

                // Wrong splits: shift hyphen left/right
                List<String> wrongs = buildWrongSplits(hyphenated);
                // Pad with sibling examples if still short
                for (int j = 0; j < exArr.length() && wrongs.size() < 3; j++) {
                    String other = exArr.getString(j).trim();
                    if (!other.equalsIgnoreCase(hyphenated) && !wrongs.contains(other))
                        wrongs.add(other);
                }
                while (wrongs.size() < 3) wrongs.add("---");

                String before = "Divide '" + baseWord + "':";
                String after  = ruleHint.isEmpty() ? "" : "  (" + ruleHint + ")";
                result.add(new DemoDataProvider.FillQuestion(
                        before, after, correct,
                        new String[]{wrongs.get(0), wrongs.get(1), wrongs.get(2)},
                        moduleDomain != null ? moduleDomain : "phonics"
                ));
            }
            if (!result.isEmpty()) return result;
        }

        // ── BRANCH B: vocabulary lessons with a word list + sentences ─────────
        // Collect all lesson words for blanks + distractors
        List<String> lessonWords = new ArrayList<>();
        for (String f : new String[]{"keyWords","words","themeWords","sightWords",
                "practiceWords","verbList","adjectives","mathWords","scienceWords"}) {
            if (obj.has(f)) {
                org.json.JSONArray a = obj.getJSONArray(f);
                for (int i = 0; i < a.length(); i++) lessonWords.add(a.getString(i).trim().toLowerCase());
                break;
            }
        }

        // Get practice sentences
        org.json.JSONArray sentArr = null;
        for (String f : new String[]{"contextSentences","sentencePractice","sentenceFrames"}) {
            if (obj.has(f)) { sentArr = obj.getJSONArray(f); break; }
        }
        if (sentArr == null || sentArr.length() == 0) return null;

        List<String> fillers = Arrays.asList("the","and","but","run","cat","big","red","play");
        List<DemoDataProvider.FillQuestion> result = new ArrayList<>();

        for (int i = 0; i < sentArr.length(); i++) {
            String sentence = sentArr.getString(i).trim();
            // Remove trailing punctuation to avoid matching issues
            String clean = sentence.replaceAll("[.!?]$", "").trim();
            String[] parts = clean.split("\\s+");

            // Find the best word to blank: prefer a lesson word found in the sentence
            int blankIdx = -1;
            String answer = null;
            for (int j = 0; j < parts.length; j++) {
                String norm = parts[j].toLowerCase().replaceAll("[^a-z]", "");
                if (lessonWords.contains(norm) && norm.length() > 2) {
                    blankIdx = j; answer = norm; break;
                }
            }
            // If no lesson word matched, pick the longest word (most content-rich)
            if (blankIdx < 0) {
                int maxLen = 0;
                for (int j = 0; j < parts.length; j++) {
                    String norm = parts[j].replaceAll("[^a-zA-Z]", "");
                    if (norm.length() > maxLen && norm.length() > 2) {
                        maxLen = norm.length(); blankIdx = j; answer = norm.toLowerCase();
                    }
                }
            }
            if (blankIdx < 0 || answer == null) continue;

            String before = blankIdx > 0
                    ? String.join(" ", Arrays.copyOfRange(parts, 0, blankIdx)) + " "
                    : "";
            String after = blankIdx < parts.length - 1
                    ? " " + String.join(" ", Arrays.copyOfRange(parts, blankIdx + 1, parts.length))
                    : "";

            // Build 3 wrong options from other lesson words, pad with fillers if needed
            List<String> pool = new ArrayList<>(lessonWords);
            pool.remove(answer);
            java.util.Collections.shuffle(pool);
            List<String> wrongs = new ArrayList<>();
            for (String w : pool) { if (!w.equals(answer) && wrongs.size() < 3) wrongs.add(w); }
            for (String f : fillers) { if (wrongs.size() >= 3) break; if (!f.equals(answer)) wrongs.add(f); }
            while (wrongs.size() < 3) wrongs.add("---");

            result.add(new DemoDataProvider.FillQuestion(
                    before, after, answer,
                    new String[]{wrongs.get(0), wrongs.get(1), wrongs.get(2)},
                    moduleDomain != null ? moduleDomain : "lesson"
            ));
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * Given a correctly hyphenated example like "Ti-ger", produces up to 3
     * wrong splits by shifting the hyphen position left and right.
     * e.g. "Ti-ger" → ["Tig-er", "T-iger", "Tige-r"]
     */
    private List<String> buildWrongSplits(String hyphenated) {
        List<String> wrongs = new ArrayList<>();
        int hyphenPos = hyphenated.indexOf('-');
        String raw = hyphenated.replace("-", ""); // letters only, original case
        int len = raw.length();
        // rawPos = index in raw[] where hyphen currently sits (= chars before hyphen)
        int rawPos = hyphenPos; // chars before the '-' in the original
        for (int offset = -2; offset <= 2; offset++) {
            if (offset == 0) continue;
            int newRawPos = rawPos + offset;
            if (newRawPos < 1 || newRawPos >= len) continue;
            String wrong = raw.substring(0, newRawPos) + "-" + raw.substring(newRawPos);
            if (!wrong.equalsIgnoreCase(hyphenated) && !wrongs.contains(wrong)) {
                wrongs.add(wrong);
                if (wrongs.size() == 3) break;
            }
        }
        return wrongs;
    }

    private void loadQuestion(int index) {
        if (index >= questions.size()) { endGame(); return; }
        currentIndex = index;
        selectedWord = null;
        isAnswerLocked = false;
        btnCheckAnswer.setEnabled(false);

        DemoDataProvider.FillQuestion q = questions.get(index);

        // Progress
        tvProgress.setText((index + 1) + "/" + questions.size());
        progressBar.setMax(questions.size());
        progressBar.setProgress(index + 1);

        // Sentence parts
        tvQuestionBefore.setText(q.beforeBlank);
        tvBlank.setText("______");
        try { tvBlank.setBackgroundResource(R.drawable.bg_blank_space); } catch (Exception ignored) {}
        tvQuestionAfter.setText(q.afterBlank);

        // Shuffled word options
        currentOptions = new ArrayList<>(Arrays.asList(q.options));
        Collections.shuffle(currentOptions);

        if (adapter == null) {
            adapter = new WordOptionAdapter(currentOptions);
            recyclerWordOptions.setLayoutManager(new GridLayoutManager(this, 2));
            recyclerWordOptions.setAdapter(adapter);
        } else {
            adapter.setOptions(currentOptions);
        }

        // Slide-in animation
        View questionCard = findViewById(R.id.questionCard);
        if (questionCard != null) {
            questionCard.setAlpha(0f);
            questionCard.setTranslationY(24f);
            questionCard.animate().alpha(1f).translationY(0f).setDuration(280).start();
        }
    }

    private void onWordSelected(String word) {
        if (isAnswerLocked) return;
        selectedWord = word;
        tvBlank.setText(word);
        try { tvBlank.setBackgroundResource(R.drawable.bg_blank_filled); } catch (Exception ignored) {}
        tvBlank.animate().scaleX(1.15f).scaleY(1.15f).setDuration(120)
                .withEndAction(() -> tvBlank.animate().scaleX(1f).scaleY(1f).setDuration(120).start()).start();
        btnCheckAnswer.setEnabled(true);
        adapter.setSelected(word);
    }

    private void checkAnswer() {
        if (selectedWord == null || isAnswerLocked) return;
        isAnswerLocked = true;
        btnCheckAnswer.setEnabled(false);

        DemoDataProvider.FillQuestion q = questions.get(currentIndex);
        boolean correct = selectedWord.trim().equalsIgnoreCase(q.correctAnswer.trim());

        if (correct) {
            correctAnswers++;
            score += 10;
            tvScore.setText(String.valueOf(score));
            try { tvBlank.setBackgroundResource(R.drawable.bg_blank_correct); } catch (Exception ignored) {}
            tvBlank.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200)
                    .withEndAction(() -> tvBlank.animate().scaleX(1f).scaleY(1f).setDuration(200).start()).start();
            playLottieOnce(lottieCorrect);
        } else {
            try { tvBlank.setBackgroundResource(R.drawable.bg_blank_wrong); } catch (Exception ignored) {}
            tvBlank.setText(q.correctAnswer);
            shakeView(tvBlank);
        }

        // Auto-advance
        tvBlank.postDelayed(() -> {
            int next = currentIndex + 1;
            if (next < questions.size()) loadQuestion(next);
            else endGame();
        }, 1200);
    }

    private void startTimer() {
        timerProgress.setMax((int)(TOTAL_TIME / 1000));
        timerProgress.setProgress((int)(TOTAL_TIME / 1000));
        countDownTimer = new CountDownTimer(TOTAL_TIME, 1000) {
            @Override public void onTick(long ms) {
                long secs = ms / 1000;
                tvTimer.setText(String.format(Locale.getDefault(), "%d:%02d", secs / 60, secs % 60));
                timerProgress.setProgress((int) secs);
                if (secs <= 30) tvTimer.setTextColor(Color.parseColor("#EF4444"));
            }
            @Override public void onFinish() { endGame(); }
        }.start();
    }

    private void endGame() {
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
        int total = questions.size();
        int accuracy = total > 0 ? (int)((correctAnswers * 100.0) / total) : 0;
        int timeUsed = (int)((System.currentTimeMillis() - gameStartTime) / 1000);
        saveGameResult(score, accuracy, timeUsed);

        // Celebrate then show dialog
        if (lottieComplete != null && accuracy >= 70) {
            lottieComplete.setVisibility(View.VISIBLE);
            lottieComplete.playAnimation();
            lottieComplete.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
                @Override public void onAnimationEnd(android.animation.Animator animation) {
                    lottieComplete.setVisibility(View.GONE);
                    showResultDialog(correctAnswers, total, accuracy, score);
                }
            });
        } else {
            showResultDialog(correctAnswers, total, accuracy, score);
        }
    }

    /** Plays a Lottie animation once, hiding it when done. */
    private void playLottieOnce(LottieAnimationView view) {
        if (view == null) return;
        view.cancelAnimation();
        view.setProgress(0f);
        view.setVisibility(View.VISIBLE);
        view.playAnimation();
        view.addAnimatorListener(new android.animation.AnimatorListenerAdapter() {
            @Override public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
                view.removeAllAnimatorListeners();
            }
        });
    }

    private void saveGameResult(int xpEarned, int accuracy, int timeSeconds) {
        int studentId = session.getStudentId();
        SaveGameResultRequest request = new SaveGameResultRequest.Builder(
                studentId, "FillInTheBlanks", xpEarned)
                .lessonId(nodeId).accuracyPercentage((float) accuracy)
                .timeCompleted(timeSeconds).xpEarned(xpEarned).streakAchieved(correctAnswers).build();
        apiService.saveGameResult(request).enqueue(new Callback<SaveGameResultResponse>() {
            @Override public void onResponse(@NonNull Call<SaveGameResultResponse> call,
                                             @NonNull Response<SaveGameResultResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    SaveGameResultResponse.StudentStats stats = response.body().getStudent();
                    if (stats != null) session.updateTotalXP(stats.getTotalXP());
                }
            }
            @Override public void onFailure(@NonNull Call<SaveGameResultResponse> call, @NonNull Throwable t) {
                session.saveXP(session.getXP() + xpEarned);
            }
        });
    }

    private void showResultDialog(int correct, int total, int accuracy, int xpEarned) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_game_result, null);

        ImageView ivTrophy = dialogView.findViewById(R.id.ivTrophy);
        TextView tvResultTitle = dialogView.findViewById(R.id.tvResultTitle);
        TextView tvResultScore = dialogView.findViewById(R.id.tvResultScore);
        TextView tvResultAccuracy = dialogView.findViewById(R.id.tvResultAccuracy);
        TextView tvResultXP = dialogView.findViewById(R.id.tvResultXP);
        TextView tvResultStreak = dialogView.findViewById(R.id.tvResultStreak);
        MaterialButton btnFinish = dialogView.findViewById(R.id.btnFinish);

        if (accuracy == 100) {
            tvResultTitle.setText("PERFECT! 🎉");
            ivTrophy.animate().rotation(360f).scaleX(1.2f).scaleY(1.2f).setDuration(600)
                    .withEndAction(() -> ivTrophy.animate().scaleX(1f).scaleY(1f).setDuration(300).start()).start();
        } else if (accuracy >= 70) {
            tvResultTitle.setText("Great Job! 🌟");
        } else if (accuracy >= 50) {
            tvResultTitle.setText("Good Effort! 👍");
        } else {
            tvResultTitle.setText("Keep Practicing! 💪");
        }

        tvResultStreak.setText(accuracy >= 90 ? "⭐⭐⭐" : accuracy >= 70 ? "⭐⭐" : accuracy >= 50 ? "⭐" : "");
        tvResultScore.setText(correct + "/" + total);
        tvResultAccuracy.setText(accuracy + "%");
        tvResultXP.setText("+" + xpEarned + " XP");

        for (TextView tv : new TextView[]{tvResultScore, tvResultAccuracy, tvResultXP}) tv.setAlpha(0f);
        tvResultScore.animate().alpha(1f).setStartDelay(200).setDuration(400).start();
        tvResultAccuracy.animate().alpha(1f).setStartDelay(400).setDuration(400).start();
        tvResultXP.animate().alpha(1f).setStartDelay(600).setDuration(400).start();

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        btnFinish.setOnClickListener(v -> {
            dialog.dismiss();
            Intent result = new Intent();
            result.putExtra("xp_earned", xpEarned);
            result.putExtra("accuracy", accuracy);
            result.putExtra("correct_count", correct);
            result.putExtra("total_count", total);
            setResult(RESULT_OK, result);
            finish();
        });
        dialog.show();
    }

    private void shakeView(View view) {
        view.animate().translationX(-12f).setDuration(60)
                .withEndAction(() -> view.animate().translationX(12f).setDuration(60)
                        .withEndAction(() -> view.animate().translationX(-12f).setDuration(60)
                                .withEndAction(() -> view.animate().translationX(12f).setDuration(60)
                                        .withEndAction(() -> view.animate().translationX(0f).setDuration(60).start())
                                        .start()).start()).start()).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    // ─── Word Option Adapter ──────────────────────────────────────────────────

    private class WordOptionAdapter extends RecyclerView.Adapter<WordOptionAdapter.VH> {
        private List<String> opts;
        private String selectedOpt = null;

        WordOptionAdapter(List<String> opts) { this.opts = new ArrayList<>(opts); }

        void setOptions(List<String> opts) { this.opts = new ArrayList<>(opts); selectedOpt = null; notifyDataSetChanged(); }
        void setSelected(String w) { selectedOpt = w; notifyDataSetChanged(); }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word_option, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            String word = opts.get(position);
            holder.tvWord.setText(word);
            boolean sel = word.equals(selectedOpt);
            if (sel) {
                try { holder.cardWordOption.setCardBackgroundColor(Color.parseColor(colorStart)); } catch (Exception e) {
                    holder.cardWordOption.setCardBackgroundColor(0xFF7C3AED);
                }
                holder.tvWord.setTextColor(Color.WHITE);
            } else {
                holder.cardWordOption.setCardBackgroundColor(Color.WHITE);
                holder.tvWord.setTextColor(Color.parseColor("#1F2937"));
            }
            holder.cardWordOption.setOnClickListener(v -> {
                if (!isAnswerLocked) {
                    v.animate().scaleX(0.92f).scaleY(0.92f).setDuration(80)
                            .withEndAction(() -> v.animate().scaleX(1f).scaleY(1f).setDuration(80).start()).start();
                    onWordSelected(word);
                }
            });
        }

        @Override public int getItemCount() { return opts.size(); }

        class VH extends RecyclerView.ViewHolder {
            MaterialCardView cardWordOption;
            TextView tvWord;
            VH(View v) { super(v); cardWordOption = v.findViewById(R.id.cardWordOption); tvWord = v.findViewById(R.id.tvWord); }
        }
    }
}