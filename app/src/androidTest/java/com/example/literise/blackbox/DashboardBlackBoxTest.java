package com.example.literise.blackbox;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.DashboardActivity;
import com.example.literise.activities.SettingsActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

/**
 * BLACK BOX TESTS — Dashboard Screen
 *
 * Verifies all dashboard UI sections are visible and interactive.
 * A fake session is seeded so DashboardActivity can launch without a real login.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DashboardBlackBoxTest {

    private static final String PREFS = "LiteRiseSession";

    @Before
    public void seedSession() {
        Intents.init();
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences.Editor ed = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        ed.putInt("student_id", 1);
        ed.putString("fullname", "Test Student");
        ed.putString("email", "test@test.com");
        ed.putBoolean("has_seen_welcome", true);
        ed.putBoolean("assessment_completed", true);
        ed.putInt("xp", 100);
        ed.putInt("streak", 3);
        ed.apply();
    }

    @After
    public void cleanup() {
        Intents.release();
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply();
    }

    // ─── TC-D-01: Welcome greeting text is displayed ─────────────────────────
    @Test
    public void welcomeGreeting_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvWelcome)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-02: XP counter is displayed ────────────────────────────────────
    @Test
    public void xpCounter_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvHeaderXP)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-03: Streak counter is displayed ────────────────────────────────
    @Test
    public void streakCounter_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvStreak)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-04: Badges counter is displayed ────────────────────────────────
    @Test
    public void badgesCounter_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvBadges)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-05: Module summary text is visible ─────────────────────────────
    @Test
    public void moduleSummary_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvModuleSummary)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-06: Modules RecyclerView is displayed ──────────────────────────
    @Test
    public void modulesRecyclerView_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.rvModules)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-07: Settings icon is visible ───────────────────────────────────
    @Test
    public void settingsIcon_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.ivSettings)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-08: Tapping Settings icon navigates to SettingsActivity ────────
    @Test
    public void clickSettings_navigatesToSettingsActivity() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.ivSettings)).perform(click());
            intended(hasComponent(SettingsActivity.class.getName()));
        }
    }

    // ─── TC-D-09: Bottom navigation bar is displayed ─────────────────────────
    @Test
    public void bottomNavigation_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.customBottomNav)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-10: Leo mascot image is displayed ───────────────────────────────
    @Test
    public void leoMascot_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.ivLeoMascot)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-11: "Your Learning Path" label is shown ────────────────────────
    @Test
    public void learningPathLabel_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withText("Your Learning Path")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-D-12: XP value contains "XP" suffix ──────────────────────────────
    @Test
    public void xpValue_containsXpSuffix() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvHeaderXP))
                    .check(matches(withText(containsString("XP"))));
        }
    }

    // ─── TC-D-13: Streak value contains "Days" suffix ────────────────────────
    @Test
    public void streakValue_containsDaysSuffix() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvStreak))
                    .check(matches(withText(containsString("Day"))));
        }
    }

    // ─── TC-D-14: Motivation text is shown ───────────────────────────────────
    @Test
    public void motivationText_isDisplayed() {
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvMotivation)).check(matches(isDisplayed()));
        }
    }
}
