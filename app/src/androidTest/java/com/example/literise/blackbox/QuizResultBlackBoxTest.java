package com.example.literise.blackbox;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.literise.R;
import com.example.literise.activities.QuizResultActivity;

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
 * BLACK BOX TESTS — Quiz Result Screen
 *
 * Verifies that all result components render correctly for different score scenarios.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class QuizResultBlackBoxTest {

    /** Build an intent carrying quiz result data to QuizResultActivity */
    private Intent buildResultIntent(int score, int correct, int total, int xp) {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(), QuizResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("correct_count", correct);
        intent.putExtra("total_count", total);
        intent.putExtra("xp_earned", xp);
        intent.putExtra("node_id", 1);
        intent.putExtra("module_id", 1);
        return intent;
    }

    // ─── TC-QR-01: Result screen launches with passing score ──────────────────
    @Test
    public void resultScreen_launchesWithPassingScore() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
        }
    }

    // ─── TC-QR-02: Result title is displayed ─────────────────────────────────
    @Test
    public void resultTitle_isDisplayed() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.tvResultTitle)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-QR-03: Score percentage view is displayed ────────────────────────
    @Test
    public void scorePercentage_isDisplayed() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.tvScore)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-QR-04: Score contains a "%" sign ─────────────────────────────────
    @Test
    public void scoreContainsPercentSign() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.tvScore))
                    .check(matches(withText(containsString("%"))));
        }
    }

    // ─── TC-QR-05: Correct count view is displayed ───────────────────────────
    @Test
    public void correctCount_isDisplayed() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.tvCorrectCount)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-QR-06: Correct count contains "/" separator ──────────────────────
    @Test
    public void correctCount_containsSlash() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.tvCorrectCount))
                    .check(matches(withText(containsString("/"))));
        }
    }

    // ─── TC-QR-07: XP awarded text is displayed ──────────────────────────────
    @Test
    public void xpAwarded_isDisplayed() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.tvXpAwarded)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-QR-08: Adaptive decision card is displayed ───────────────────────
    @Test
    public void adaptiveDecision_isDisplayed() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.tvAdaptiveDecision)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-QR-09: Continue button is displayed and enabled ──────────────────
    @Test
    public void continueButton_isDisplayedAndEnabled() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.btnContinue))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ─── TC-QR-10: Leo result icon is displayed ──────────────────────────────
    @Test
    public void leoResultIcon_isDisplayed() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.ivResultIcon)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-QR-11: Next steps label is displayed ─────────────────────────────
    @Test
    public void nextSteps_isDisplayed() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(90, 9, 10, 50))) {
            onView(withId(R.id.tvNextSteps)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-QR-12: Result screen with failing score (< 70%) shows retake ─────
    @Test
    public void failingScore_showsRetakeButton() {
        try (ActivityScenario<QuizResultActivity> ignored =
                     ActivityScenario.launch(buildResultIntent(50, 5, 10, 10))) {
            onView(withId(R.id.btnRetakeQuiz)).check(matches(isDisplayed()));
        }
    }
}
