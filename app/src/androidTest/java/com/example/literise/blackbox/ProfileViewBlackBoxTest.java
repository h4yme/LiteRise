package com.example.literise.blackbox;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.ProfileViewActivity;
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
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * BLACK BOX TESTS — Profile View Screen
 *
 * Verifies profile information and navigation options are visible.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileViewBlackBoxTest {

    @Before
    public void setup() {
        Intents.init();
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences("LiteRiseSession", Context.MODE_PRIVATE)
                .edit()
                .putInt("student_id", 1)
                .putString("fullname", "Test Student")
                .putString("email", "test@example.com")
                .putInt("xp", 300)
                .putInt("streak", 7)
                .apply();
    }

    @After
    public void teardown() {
        Intents.release();
    }

    // ─── TC-PR-01: Profile screen launches without crash ─────────────────────
    @Test
    public void profileScreen_launchesSuccessfully() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
        }
    }

    // ─── TC-PR-02: Profile name is displayed ─────────────────────────────────
    @Test
    public void profileName_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.tvProfileName)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-03: Profile level is displayed ────────────────────────────────
    @Test
    public void profileLevel_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.tvProfileLevel)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-04: XP stat is displayed ──────────────────────────────────────
    @Test
    public void xpStat_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.tvProfileXP)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-05: Streak stat is displayed ──────────────────────────────────
    @Test
    public void streakStat_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.tvProfileStreak)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-06: Badges stat is displayed ──────────────────────────────────
    @Test
    public void badgesStat_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.tvProfileBadges)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-07: Full name detail row is displayed ─────────────────────────
    @Test
    public void fullNameDetail_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.tvProfileNameDetail)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-08: Email detail row is displayed ─────────────────────────────
    @Test
    public void emailDetail_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.tvProfileEmail)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-09: Placement level row is displayed ───────────────────────────
    @Test
    public void placementLevel_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.tvProfilePlacementLevel)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-10: Settings card is displayed ────────────────────────────────
    @Test
    public void settingsCard_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.cardSettings)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-11: Logout card is displayed and enabled ──────────────────────
    @Test
    public void logoutCard_isDisplayedAndEnabled() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.cardLogOut))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ─── TC-PR-12: Settings icon button is visible ───────────────────────────
    @Test
    public void settingsIconBtn_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.ivSettingsBtn)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-PR-13: Tapping Settings card navigates to SettingsActivity ───────
    @Test
    public void clickSettingsCard_navigatesToSettingsActivity() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.cardSettings)).perform(click());
            intended(hasComponent(SettingsActivity.class.getName()));
        }
    }

    // ─── TC-PR-14: Bottom navigation is displayed ────────────────────────────
    @Test
    public void bottomNavigation_isDisplayed() {
        try (ActivityScenario<ProfileViewActivity> ignored =
                     ActivityScenario.launch(ProfileViewActivity.class)) {
            onView(withId(R.id.customBottomNav)).check(matches(isDisplayed()));
        }
    }
}
