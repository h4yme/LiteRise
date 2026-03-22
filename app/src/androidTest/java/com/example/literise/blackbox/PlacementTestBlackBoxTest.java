package com.example.literise.blackbox;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.PlacementIntroActivity;
import com.example.literise.activities.PlacementTestActivity;

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
 * BLACK BOX TESTS — Placement Test & Placement Intro Screens
 *
 * Verifies the pre-assessment placement UI is complete and correct.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class PlacementTestBlackBoxTest {

    private static final String PREFS = "LiteRiseSession";

    @Before
    public void seedSession() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences.Editor ed = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        ed.putInt("student_id", 1);
        ed.putString("fullname", "Test Student");
        ed.putBoolean("has_seen_welcome", true);
        ed.putBoolean("assessment_completed", false);
        ed.apply();
    }

    // ─── TC-PI-01: Placement intro activity launches without crash ────────────
    @Test
    public void placementIntro_launchesSuccessfully() {
        try (ActivityScenario<PlacementIntroActivity> ignored =
                     ActivityScenario.launch(PlacementIntroActivity.class)) {
            // If activity launches without exception, the test passes
        }
    }

    // ─── TC-PT-01: Placement test activity launches without crash ─────────────
    @Test
    public void placementTest_launchesSuccessfully() {
        try (ActivityScenario<PlacementTestActivity> ignored =
                     ActivityScenario.launch(PlacementTestActivity.class)) {
            // Activity should launch
        }
    }

    // ─── TC-PT-02: Placement test shows progress indicator ───────────────────
    @Test
    public void placementTest_showsProgressIndicator() {
        try (ActivityScenario<PlacementTestActivity> ignored =
                     ActivityScenario.launch(PlacementTestActivity.class)) {
            onView(withId(R.id.tvProgress)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PT-03: Placement test shows question number ──────────────────────
    @Test
    public void placementTest_questionProgressContainsSlash() {
        try (ActivityScenario<PlacementTestActivity> ignored =
                     ActivityScenario.launch(PlacementTestActivity.class)) {
            onView(withId(R.id.tvProgress))
                    .check(matches(withText(containsString("/"))));
        }
    }
}
