package com.example.literise.blackbox;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.SettingsActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * BLACK BOX TESTS — Settings Screen
 *
 * Verifies all settings options are visible to the user.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsBlackBoxTest {

    @Before
    public void seedSession() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences("LiteRiseSession", Context.MODE_PRIVATE)
                .edit()
                .putInt("student_id", 1)
                .putString("fullname", "Test Student")
                .apply();
    }

    // ─── TC-ST-01: Settings screen launches without crash ─────────────────────
    @Test
    public void settingsScreen_launchesSuccessfully() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            // Launch without crash
        }
    }

    // ─── TC-ST-02: "Settings" title is shown ─────────────────────────────────
    @Test
    public void settingsTitle_isDisplayed() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText("Settings")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-ST-03: ACCOUNT section label is shown ────────────────────────────
    @Test
    public void accountSectionLabel_isDisplayed() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText("ACCOUNT")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-ST-04: Display Name option is visible ────────────────────────────
    @Test
    public void displayNameOption_isDisplayed() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText("Display Name")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-ST-05: Change Password option is visible ─────────────────────────
    @Test
    public void changePasswordOption_isDisplayed() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText("Change Password")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-ST-06: LEARNING section label is shown ───────────────────────────
    @Test
    public void learningSectionLabel_isDisplayed() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withText("LEARNING")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-ST-07: Back button is visible ────────────────────────────────────
    @Test
    public void backButton_isDisplayed() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withId(R.id.btnBack)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-ST-08: Change Name card is visible ───────────────────────────────
    @Test
    public void changeNameCard_isDisplayed() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withId(R.id.cardChangeName)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-ST-09: Change Password card is visible ───────────────────────────
    @Test
    public void changePasswordCard_isDisplayed() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withId(R.id.cardChangePassword)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-ST-10: Current display name view is visible ──────────────────────
    @Test
    public void currentDisplayName_isVisible() {
        try (ActivityScenario<SettingsActivity> ignored =
                     ActivityScenario.launch(SettingsActivity.class)) {
            onView(withId(R.id.tvCurrentDisplayName)).check(matches(isDisplayed()));
        }
    }
}
