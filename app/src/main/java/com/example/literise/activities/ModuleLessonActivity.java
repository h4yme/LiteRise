package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.content.Module1ContentProvider;
import com.example.literise.database.LessonDatabase;
import com.example.literise.models.Badge;
import com.example.literise.models.Lesson;
import com.example.literise.models.Question;
import com.example.literise.utils.GamificationManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

/**
 * Activity for displaying structured lesson content with practice and quiz
 * Part of the new MATATAG-aligned gamified learning system
 */
public class ModuleLessonActivity extends BaseActivity {

    // Intent extras
    public static final String EXTRA_LESSON_ID = "lesson_id";
    public static final String EXTRA_MODULE_ID = "module_id";

    // Views
    private ImageView ivBack;
    private TextView tvLessonTitle, tvLessonDescription, tvXpReward, tvTier;
    private ProgressBar progressBar;
    private TextView tvProgress;

    // Tab navigation
    private CardView tabContent, tabPractice, tabQuiz;
    private TextView tvTabContent, tvTabPractice, tvTabQuiz;
    private View indicatorContent, indicatorPractice, indicatorQuiz;

    // Content views
    private ScrollView scrollContent;
    private TextView tvLessonContent;
    private MaterialButton btnStartPractice;

    // Practice views
    private ScrollView scrollPractice;
    private LinearLayout practiceContainer;
    private MaterialButton btnCheckPractice;

    // Quiz views
    private ScrollView scrollQuiz;
    private LinearLayout quizContainer;
    private MaterialButton btnSubmitQuiz;

    // Navigation
    private MaterialButton btnRetry;

    // Data
    private Lesson currentLesson;
    private int lessonId;
    private int moduleId;
    private LessonDatabase database;
    private GamificationManager gamificationManager;
    private Markwon markwon;

    // State
    private enum LessonTab { CONTENT, PRACTICE, QUIZ }
    private LessonTab currentTab = LessonTab.CONTENT;

    private List<RadioGroup> practiceRadioGroups = new ArrayList<>();
    private List<String> practiceAnswers = new ArrayList<>();

    private List<RadioGroup> quizRadioGroups = new ArrayList<>();
    private List<String> quizAnswers = new ArrayList<>();

    private int practiceScore = 0;
    private int quizScore = 0;
    private boolean practiceCompleted = false;
    private boolean quizCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_lesson);

        // Get lesson data from intent
        lessonId = getIntent().getIntExtra(EXTRA_LESSON_ID, -1);
        moduleId = getIntent().getIntExtra(EXTRA_MODULE_ID, -1);

        if (lessonId == -1 || moduleId == -1) {
            Toast.makeText(this, "Error loading lesson", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize
        database = new LessonDatabase(this);
        gamificationManager = new GamificationManager(this);
        markwon = Markwon.create(this);

        initializeViews();
        loadLesson();
        setupTabs();
        showTab(LessonTab.CONTENT);
    }

    private void initializeViews() {
        // Header
        ivBack = findViewById(R.id.ivBack);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonDescription = findViewById(R.id.tvLessonDescription);
        tvXpReward = findViewById(R.id.tvXpReward);
        tvTier = findViewById(R.id.tvTier);
        progressBar = findViewById(R.id.progressBar);
        tvProgress = findViewById(R.id.tvProgress);

        // Tabs
        tabContent = findViewById(R.id.tabContent);
        tabPractice = findViewById(R.id.tabPractice);
        tabQuiz = findViewById(R.id.tabQuiz);

        tvTabContent = findViewById(R.id.tvTabContent);
        tvTabPractice = findViewById(R.id.tvTabPractice);
        tvTabQuiz = findViewById(R.id.tvTabQuiz);

        indicatorContent = findViewById(R.id.indicatorContent);
        indicatorPractice = findViewById(R.id.indicatorPractice);
        indicatorQuiz = findViewById(R.id.indicatorQuiz);

        // Content section
        scrollContent = findViewById(R.id.scrollContent);
        tvLessonContent = findViewById(R.id.tvLessonContent);
        btnStartPractice = findViewById(R.id.btnStartPractice);

        // Practice section
        scrollPractice = findViewById(R.id.scrollPractice);
        practiceContainer = findViewById(R.id.practiceContainer);
        btnCheckPractice = findViewById(R.id.btnCheckPractice);

        // Quiz section
        scrollQuiz = findViewById(R.id.scrollQuiz);
        quizContainer = findViewById(R.id.quizContainer);
        btnSubmitQuiz = findViewById(R.id.btnSubmitQuiz);

        // Navigation
        btnRetry = findViewById(R.id.btnRetry);

        // Click listeners
        ivBack.setOnClickListener(v -> onBackPressed());
        btnStartPractice.setOnClickListener(v -> showTab(LessonTab.PRACTICE));
        btnCheckPractice.setOnClickListener(v -> checkPracticeAnswers());
        btnSubmitQuiz.setOnClickListener(v -> submitQuiz());
        btnRetry.setOnClickListener(v -> retryQuiz());
    }

    private void setupTabs() {
        tabContent.setOnClickListener(v -> showTab(LessonTab.CONTENT));
        tabPractice.setOnClickListener(v -> {
            if (practiceCompleted || currentTab == LessonTab.PRACTICE) {
                showTab(LessonTab.PRACTICE);
            } else {
                Toast.makeText(this, "Please read the content first", Toast.LENGTH_SHORT).show();
            }
        });
        tabQuiz.setOnClickListener(v -> {
            if (practiceCompleted) {
                showTab(LessonTab.QUIZ);
            } else {
                Toast.makeText(this, "Please complete practice first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLesson() {
        // Get lesson from content provider
        List<Lesson> allLessons = Module1ContentProvider.getAllLessons();

        for (Lesson lesson : allLessons) {
            if (lesson.getLessonId() == lessonId) {
                currentLesson = lesson;
                break;
            }
        }

        if (currentLesson == null) {
            Toast.makeText(this, "Lesson not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display lesson info
        tvLessonTitle.setText(currentLesson.getTitle());
        tvLessonDescription.setText(currentLesson.getDescription());
        tvXpReward.setText("+" + currentLesson.getXpReward() + " XP");
        tvTier.setText(currentLesson.getTier());

        // Load lesson content
        if (currentLesson.getContent() != null && !currentLesson.getContent().isEmpty()) {
            markwon.setMarkdown(tvLessonContent, currentLesson.getContent());
        }

        // Build practice questions
        buildQuestions(practiceContainer, currentLesson.getPracticeQuestions(), practiceRadioGroups, practiceAnswers);

        // Build quiz questions
        buildQuestions(quizContainer, currentLesson.getQuizQuestions(), quizRadioGroups, quizAnswers);

        // Update progress
        updateProgress();
    }

    private void buildQuestions(LinearLayout container, List<Question> questions,
                                List<RadioGroup> radioGroups, List<String> correctAnswers) {
        container.removeAllViews();
        radioGroups.clear();
        correctAnswers.clear();

        if (questions == null || questions.isEmpty()) {
            TextView tvNoQuestions = new TextView(this);
            tvNoQuestions.setText("Questions coming soon!");
            tvNoQuestions.setPadding(32, 32, 32, 32);
            tvNoQuestions.setTextSize(16);
            tvNoQuestions.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            container.addView(tvNoQuestions);
            return;
        }

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);

            // Question card
            CardView cardView = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 24);
            cardView.setLayoutParams(cardParams);
            cardView.setCardElevation(4);
            cardView.setRadius(16);
            cardView.setContentPadding(24, 20, 24, 20);

            LinearLayout questionLayout = new LinearLayout(this);
            questionLayout.setOrientation(LinearLayout.VERTICAL);

            // Question text
            TextView tvQuestion = new TextView(this);
            tvQuestion.setText("Q" + (i + 1) + ": " + question.getQuestionText());
            tvQuestion.setTextSize(15);
            tvQuestion.setTextColor(getResources().getColor(android.R.color.black));
            tvQuestion.setPadding(0, 0, 0, 16);
            tvQuestion.setLineSpacing(0, 1.2f);
            questionLayout.addView(tvQuestion);

            // Radio group for options
            RadioGroup radioGroup = new RadioGroup(this);
            radioGroup.setOrientation(RadioGroup.VERTICAL);

            // Add options
            if (question.getOptionA() != null) {
                RadioButton rbA = createRadioButton("A", question.getOptionA());
                radioGroup.addView(rbA);
            }

            if (question.getOptionB() != null) {
                RadioButton rbB = createRadioButton("B", question.getOptionB());
                radioGroup.addView(rbB);
            }

            if (question.getOptionC() != null) {
                RadioButton rbC = createRadioButton("C", question.getOptionC());
                radioGroup.addView(rbC);
            }

            if (question.getOptionD() != null) {
                RadioButton rbD = createRadioButton("D", question.getOptionD());
                radioGroup.addView(rbD);
            }

            questionLayout.addView(radioGroup);
            cardView.addView(questionLayout);
            container.addView(cardView);

            radioGroups.add(radioGroup);
            correctAnswers.add(question.getCorrectOption());
        }
    }

    private RadioButton createRadioButton(String tag, String text) {
        RadioButton rb = new RadioButton(this);
        rb.setText(text);
        rb.setTag(tag);
        rb.setPadding(16, 12, 16, 12);
        rb.setTextSize(14);
        return rb;
    }

    private void showTab(LessonTab tab) {
        currentTab = tab;

        // Hide all sections
        scrollContent.setVisibility(View.GONE);
        scrollPractice.setVisibility(View.GONE);
        scrollQuiz.setVisibility(View.GONE);

        // Reset tab styles
        resetTabStyle(tabContent, tvTabContent, indicatorContent);
        resetTabStyle(tabPractice, tvTabPractice, indicatorPractice);
        resetTabStyle(tabQuiz, tvTabQuiz, indicatorQuiz);

        // Show selected tab
        switch (tab) {
            case CONTENT:
                scrollContent.setVisibility(View.VISIBLE);
                setActiveTabStyle(tabContent, tvTabContent, indicatorContent);
                break;

            case PRACTICE:
                scrollPractice.setVisibility(View.VISIBLE);
                setActiveTabStyle(tabPractice, tvTabPractice, indicatorPractice);
                break;

            case QUIZ:
                scrollQuiz.setVisibility(View.VISIBLE);
                setActiveTabStyle(tabQuiz, tvTabQuiz, indicatorQuiz);
                btnRetry.setVisibility(quizCompleted && quizScore < 70 ? View.VISIBLE : View.GONE);
                break;
        }

        updateProgress();
    }

    private void setActiveTabStyle(CardView card, TextView text, View indicator) {
        card.setCardBackgroundColor(getResources().getColor(R.color.purple_600));
        text.setTextColor(getResources().getColor(R.color.text_primary));
        indicator.setVisibility(View.VISIBLE);
    }

    private void resetTabStyle(CardView card, TextView text, View indicator) {
        card.setCardBackgroundColor(getResources().getColor(android.R.color.white));
        text.setTextColor(getResources().getColor(android.R.color.darker_gray));
        indicator.setVisibility(View.INVISIBLE);
    }

    private void checkPracticeAnswers() {
        int correct = 0;
        int total = practiceRadioGroups.size();

        if (total == 0) {
            practiceCompleted = true;
            showTab(LessonTab.QUIZ);
            return;
        }

        for (int i = 0; i < total; i++) {
            RadioGroup radioGroup = practiceRadioGroups.get(i);
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedButton = findViewById(selectedId);
            String selectedAnswer = (String) selectedButton.getTag();
            String correctAnswer = practiceAnswers.get(i);

            if (selectedAnswer.equals(correctAnswer)) {
                correct++;
                selectedButton.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                selectedButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

                // Highlight correct answer
                for (int j = 0; j < radioGroup.getChildCount(); j++) {
                    RadioButton rb = (RadioButton) radioGroup.getChildAt(j);
                    if (rb.getTag().equals(correctAnswer)) {
                        rb.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                    }
                }
            }

            // Disable radio group
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                radioGroup.getChildAt(j).setEnabled(false);
            }
        }

        practiceScore = (int) ((correct / (float) total) * 100);
        practiceCompleted = true;

        btnCheckPractice.setVisibility(View.GONE);

        // Show results
        showPracticeResults(correct, total, practiceScore);
    }

    private void submitQuiz() {
        int correct = 0;
        int total = quizRadioGroups.size();

        if (total == 0) {
            Toast.makeText(this, "No quiz questions available yet", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < total; i++) {
            RadioGroup radioGroup = quizRadioGroups.get(i);
            int selectedId = radioGroup.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(this, "Please answer all questions", Toast.LENGTH_SHORT).show();
                return;
            }

            RadioButton selectedButton = findViewById(selectedId);
            String selectedAnswer = (String) selectedButton.getTag();
            String correctAnswer = quizAnswers.get(i);

            if (selectedAnswer.equals(correctAnswer)) {
                correct++;
            }

            // Disable radio group
            for (int j = 0; j < radioGroup.getChildCount(); j++) {
                radioGroup.getChildAt(j).setEnabled(false);
            }
        }

        quizScore = (int) ((correct / (float) total) * 100);
        quizCompleted = true;

        btnSubmitQuiz.setVisibility(View.GONE);

        // Save progress
        database.updateLessonProgress(lessonId, moduleId, practiceScore, quizScore, quizScore >= 70);

        // Show results with gamification
        showQuizResults(correct, total, quizScore);

        // Update button visibility
        if (quizScore >= 70) {
            database.unlockNextLesson(lessonId, moduleId);
        } else {
            btnRetry.setVisibility(View.VISIBLE);
        }

        updateProgress();
    }

    private void showPracticeResults(int correct, int total, int percentage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Practice Complete!");
        builder.setMessage("You got " + correct + " out of " + total + " correct!\n\n" +
                "Score: " + percentage + "%\n\n" +
                "Great practice! Now let's try the quiz to earn XP.");
        builder.setPositiveButton("Start Quiz", (dialog, which) -> {
            dialog.dismiss();
            showTab(LessonTab.QUIZ);
        });
        builder.show();
    }

    private void showQuizResults(int correct, int total, int percentage) {
        boolean passed = percentage >= 70;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(passed ? "Quiz Passed!" : "Keep Trying!");

        StringBuilder message = new StringBuilder();
        message.append("You got ").append(correct).append(" out of ").append(total).append(" correct!\n\n");
        message.append("Score: ").append(percentage).append("%\n\n");

        if (passed) {
            // Award XP
            boolean leveledUp = gamificationManager.addXP(currentLesson.getXpReward());
            int currentLevel = gamificationManager.getCurrentLevel();
            int currentXP = gamificationManager.getCurrentXP();

            message.append("+").append(currentLesson.getXpReward()).append(" XP earned!\n");
            message.append("Current XP: ").append(currentXP).append("\n");
            message.append("Level: ").append(currentLevel).append("\n");

            if (leveledUp) {
                message.append("\nLEVEL UP! You reached Level ").append(currentLevel).append("!\n");
            }

            // Check for badges
            List<Badge> newBadges = gamificationManager.checkAndAwardBadges();
            if (!newBadges.isEmpty()) {
                message.append("\nNew Badge");
                if (newBadges.size() > 1) message.append("s");
                message.append(" Earned:\n");
                for (Badge badge : newBadges) {
                    message.append("• ").append(badge.getTitle()).append("\n");
                }
            }

            // Update streak
            gamificationManager.updateStreak();

            message.append("\nNext lesson unlocked!");
        } else {
            message.append("You need 70% to pass.\n\n");
            message.append("Don't worry! Review the content and try again.");
        }

        builder.setMessage(message.toString());
        builder.setPositiveButton(passed ? "Complete" : "Retry", (dialog, which) -> {
            dialog.dismiss();
            if (passed) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void retryQuiz() {
        // Reset quiz
        quizCompleted = false;
        quizScore = 0;

        // Re-enable all radio buttons
        for (RadioGroup radioGroup : quizRadioGroups) {
            radioGroup.clearCheck();
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                RadioButton rb = (RadioButton) radioGroup.getChildAt(i);
                rb.setEnabled(true);
                rb.setTextColor(getResources().getColor(android.R.color.black));
            }
        }

        btnSubmitQuiz.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.GONE);

        // Scroll to top
        scrollQuiz.smoothScrollTo(0, 0);
    }

    private void updateProgress() {
        int progress = 0;

        if (currentTab == LessonTab.CONTENT) {
            progress = 33;
        } else if (currentTab == LessonTab.PRACTICE) {
            progress = practiceCompleted ? 66 : 50;
        } else if (currentTab == LessonTab.QUIZ) {
            progress = quizCompleted ? 100 : 80;
        }

        progressBar.setProgress(progress);
        tvProgress.setText(progress + "%");
    }

    @Override
    public void onBackPressed() {
        if (quizCompleted && quizScore >= 70) {
            finish();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit Lesson?")
                    .setMessage("Your progress will be saved, but you'll need to complete the quiz to unlock the next lesson.")
                    .setPositiveButton("Exit", (dialog, which) -> {
                        super.onBackPressed();
                    })
                    .setNegativeButton("Stay", null)
                    .show();
        }
    }
}