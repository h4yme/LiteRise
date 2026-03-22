package com.example.literise.blackbox;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.BadgesActivity;
import com.example.literise.activities.DashboardActivity;
import com.example.literise.activities.LoginActivity;
import com.example.literise.activities.LoginRegisterSelectionActivity;
import com.example.literise.activities.ProgressViewActivity;
import com.example.literise.activities.ProfileViewActivity;

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

/**
 * BLACK BOX TESTS — Navigation Flows
 *
 * Verifies end-to-end navigation between screens matches the app flowchart:
 *   Selection → Login → Dashboard → (Badges | Progress | Profile)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NavigationFlowBlackBoxTest {

    private static final String PREFS = "LiteRiseSession";

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void teardown() {
        Intents.release();
        InstrumentationRegistry.getInstrumentation()
                .getTargetContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    private void seedLoggedInSession() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt("student_id", 1)
                .putString("fullname", "Test Student")
                .putString("email", "test@example.com")
                .putBoolean("has_seen_welcome", true)
                .putBoolean("assessment_completed", true)
                .putInt("xp", 100)
                .putInt("streak", 3)
                .apply();
    }

    // ─── TC-NAV-01: Selection screen → Login screen ───────────────────────────
    @Test
    public void selectionToLogin_navigationWorks() {
        try (ActivityScenario<LoginRegisterSelectionActivity> ignored =
                     ActivityScenario.launch(LoginRegisterSelectionActivity.class)) {
            onView(withId(R.id.cardLogin)).perform(click());
            intended(hasComponent(LoginActivity.class.getName()));
        }
    }

    // ─── TC-NAV-02: Dashboard bottom nav — Badges tab navigates to Badges ─────
    @Test
    public void dashboardBottomNav_badgesTabNavigates() {
        seedLoggedInSession();
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            // Click badges nav item
            onView(withId(R.id.navBadges)).perform(click());
            intended(hasComponent(BadgesActivity.class.getName()));
        }
    }

    // ─── TC-NAV-03: Dashboard bottom nav — Progress tab navigates ─────────────
    @Test
    public void dashboardBottomNav_progressTabNavigates() {
        seedLoggedInSession();
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.navProgress)).perform(click());
            intended(hasComponent(ProgressViewActivity.class.getName()));
        }
    }

    // ─── TC-NAV-04: Dashboard bottom nav — Profile tab navigates ──────────────
    @Test
    public void dashboardBottomNav_profileTabNavigates() {
        seedLoggedInSession();
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.navProfile)).perform(click());
            intended(hasComponent(ProfileViewActivity.class.getName()));
        }
    }

    // ─── TC-NAV-05: Login screen shows LRN and PIN fields ────────────────────
    @Test
    public void loginScreen_hasBothInputFields() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLrn)).check(matches(isDisplayed()));
            onView(withId(R.id.etLoginCode)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-NAV-06: Dashboard is the landing screen when session is complete ──
    @Test
    public void completedSession_landsDashboard() {
        seedLoggedInSession();
        try (ActivityScenario<DashboardActivity> ignored =
                     ActivityScenario.launch(DashboardActivity.class)) {
            onView(withId(R.id.tvWelcome)).check(matches(isDisplayed()));
        }
    }
}
