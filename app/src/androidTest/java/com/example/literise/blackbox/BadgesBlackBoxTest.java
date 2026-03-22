package com.example.literise.blackbox;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.BadgesActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * BLACK BOX TESTS — Badges Screen
 *
 * Verifies all badge screen UI components are visible.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class BadgesBlackBoxTest {

    @Before
    public void seedSession() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences("LiteRiseSession", Context.MODE_PRIVATE)
                .edit()
                .putInt("student_id", 1)
                .putString("fullname", "Test Student")
                .apply();
    }

    // ─── TC-B-01: Badges screen launches without crash ────────────────────────
    @Test
    public void badgesScreen_launchesSuccessfully() {
        try (ActivityScenario<BadgesActivity> ignored =
                     ActivityScenario.launch(BadgesActivity.class)) {
        }
    }

    // ─── TC-B-02: "My Badges" title is displayed ─────────────────────────────
    @Test
    public void myBadgesTitle_isDisplayed() {
        try (ActivityScenario<BadgesActivity> ignored =
                     ActivityScenario.launch(BadgesActivity.class)) {
            onView(withText("My Badges")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-B-03: "Collect them all!" subtitle is displayed ──────────────────
    @Test
    public void collectThemAllSubtitle_isDisplayed() {
        try (ActivityScenario<BadgesActivity> ignored =
                     ActivityScenario.launch(BadgesActivity.class)) {
            onView(withText("Collect them all!")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-B-04: Badge count view is displayed ───────────────────────────────
    @Test
    public void badgeCount_isDisplayed() {
        try (ActivityScenario<BadgesActivity> ignored =
                     ActivityScenario.launch(BadgesActivity.class)) {
            onView(withId(R.id.tvBadgeCount)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-B-05: Badge count has correct format (X / 15) ────────────────────
    @Test
    public void badgeCount_containsSlash15() {
        try (ActivityScenario<BadgesActivity> ignored =
                     ActivityScenario.launch(BadgesActivity.class)) {
            onView(withId(R.id.tvBadgeCount))
                    .check(matches(withText(containsString("/ 15"))));
        }
    }

    // ─── TC-B-06: Progress bar for badges is displayed ───────────────────────
    @Test
    public void badgesProgressBar_isDisplayed() {
        try (ActivityScenario<BadgesActivity> ignored =
                     ActivityScenario.launch(BadgesActivity.class)) {
            onView(withId(R.id.progressBadges)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-B-07: Scroll view is displayed ───────────────────────────────────
    @Test
    public void scrollView_isDisplayed() {
        try (ActivityScenario<BadgesActivity> ignored =
                     ActivityScenario.launch(BadgesActivity.class)) {
            onView(withId(R.id.scrollView)).check(matches(isDisplayed()));
        }
    }
}
