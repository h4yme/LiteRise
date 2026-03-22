package com.example.literise.blackbox;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.ProgressViewActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * BLACK BOX TESTS — Progress View Screen
 *
 * Verifies the My Progress screen displays XP, streak, and learning data.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProgressViewBlackBoxTest {

    @Before
    public void seedSession() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences("LiteRiseSession", Context.MODE_PRIVATE)
                .edit()
                .putInt("student_id", 1)
                .putString("fullname", "Test Student")
                .putInt("xp", 200)
                .putInt("streak", 5)
                .apply();
    }

    // ─── TC-PV-01: Progress screen launches without crash ────────────────────
    @Test
    public void progressScreen_launchesSuccessfully() {
        try (ActivityScenario<ProgressViewActivity> ignored =
                     ActivityScenario.launch(ProgressViewActivity.class)) {
        }
    }

    // ─── TC-PV-02: "My Progress" title is displayed ──────────────────────────
    @Test
    public void myProgressTitle_isDisplayed() {
        try (ActivityScenario<ProgressViewActivity> ignored =
                     ActivityScenario.launch(ProgressViewActivity.class)) {
            onView(withText("My Progress")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PV-03: "Track your learning journey" subtitle is shown ───────────
    @Test
    public void trackJourneySubtitle_isDisplayed() {
        try (ActivityScenario<ProgressViewActivity> ignored =
                     ActivityScenario.launch(ProgressViewActivity.class)) {
            onView(withText("Track your learning journey")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PV-04: Total XP stat card is displayed ───────────────────────────
    @Test
    public void totalXpStat_isDisplayed() {
        try (ActivityScenario<ProgressViewActivity> ignored =
                     ActivityScenario.launch(ProgressViewActivity.class)) {
            onView(withId(R.id.tvStatXP)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PV-05: Streak stat card is displayed ─────────────────────────────
    @Test
    public void streakStat_isDisplayed() {
        try (ActivityScenario<ProgressViewActivity> ignored =
                     ActivityScenario.launch(ProgressViewActivity.class)) {
            onView(withId(R.id.tvStatStreak)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PV-06: Scroll view is displayed ──────────────────────────────────
    @Test
    public void scrollView_isDisplayed() {
        try (ActivityScenario<ProgressViewActivity> ignored =
                     ActivityScenario.launch(ProgressViewActivity.class)) {
            onView(withId(R.id.scrollView)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PV-07: "Total XP" label is shown ────────────────────────────────
    @Test
    public void totalXpLabel_isDisplayed() {
        try (ActivityScenario<ProgressViewActivity> ignored =
                     ActivityScenario.launch(ProgressViewActivity.class)) {
            onView(withText("Total XP")).check(matches(isDisplayed()));
        }
    }
}
