package com.example.literise.blackbox;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.QuizActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * BLACK BOX TESTS — Quiz Screen
 *
 * Verifies all quiz UI elements appear correctly when launched with required extras.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class QuizBlackBoxTest {

    @Before
    public void seedSession() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences("LiteRiseSession", Context.MODE_PRIVATE)
                .edit()
                .putInt("student_id", 1)
                .putString("fullname", "Test Student")
                .apply();
    }

    private Intent buildQuizIntent() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(), QuizActivity.class);
        intent.putExtra("node_id", 1);
        intent.putExtra("module_id", 1);
        intent.putExtra("lesson_title", "Test Lesson");
        return intent;
    }

    // ─── TC-Q-01: Quiz screen launches without crash ──────────────────────────
    @Test
    public void quizScreen_launchesSuccessfully() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
        }
    }

    // ─── TC-Q-02: Quiz title "Quiz Time!" is displayed ────────────────────────
    @Test
    public void quizTitle_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.tvQuizTitle)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-03: Progress indicator is displayed ─────────────────────────────
    @Test
    public void progressIndicator_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.tvProgress)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-04: Progress indicator contains "/" ─────────────────────────────
    @Test
    public void progressIndicator_containsSlash() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.tvProgress))
                    .check(matches(withText(containsString("/"))));
        }
    }

    // ─── TC-Q-05: Question number is displayed ───────────────────────────────
    @Test
    public void questionNumber_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.tvQuestionNumber)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-06: Question text area is displayed ────────────────────────────
    @Test
    public void questionText_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.tvQuestionText)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-07: Options container is displayed ─────────────────────────────
    @Test
    public void optionsContainer_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.llOptionsContainer)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-08: Option 1 card is displayed ─────────────────────────────────
    @Test
    public void option1Card_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.cardOption1)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-09: Option 2 card is displayed ─────────────────────────────────
    @Test
    public void option2Card_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.cardOption2)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-10: Option 3 card is displayed ─────────────────────────────────
    @Test
    public void option3Card_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.cardOption3)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-11: Option 4 card is displayed ─────────────────────────────────
    @Test
    public void option4Card_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.cardOption4)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-12: Next button is displayed and enabled ───────────────────────
    @Test
    public void nextButton_isDisplayedAndEnabled() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.btnNext))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ─── TC-Q-13: Progress bar is displayed ──────────────────────────────────
    @Test
    public void progressBar_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.progressBar)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-Q-14: Back button in quiz header is displayed ────────────────────
    @Test
    public void backButton_isDisplayed() {
        try (ActivityScenario<QuizActivity> ignored =
                     ActivityScenario.launch(buildQuizIntent())) {
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        }
    }
}
