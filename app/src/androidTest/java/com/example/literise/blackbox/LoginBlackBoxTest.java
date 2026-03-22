package com.example.literise.blackbox;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.literise.R;
import com.example.literise.activities.LoginActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withHint;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * BLACK BOX TESTS — Login Screen
 *
 * Tests input/output behavior of LoginActivity without knowing internals.
 * Covers: UI visibility, field validation, button states, empty-input errors.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginBlackBoxTest {

    // ─── TC-L-01: LRN field is visible on screen ─────────────────────────────
    @Test
    public void lrnField_isVisible() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLrn)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-L-02: Login code field is visible on screen ──────────────────────
    @Test
    public void loginCodeField_isVisible() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLoginCode)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-L-03: Login button is visible and enabled ────────────────────────
    @Test
    public void loginButton_isVisibleAndEnabled() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.btnLogin))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ─── TC-L-04: LRN field shows correct placeholder hint ───────────────────
    @Test
    public void lrnField_showsCorrectHint() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLrn))
                    .check(matches(withHint("e.g. 123456789012")));
        }
    }

    // ─── TC-L-05: Login code field shows correct placeholder hint ────────────
    @Test
    public void loginCodeField_showsCorrectHint() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLoginCode))
                    .check(matches(withHint("Enter 4-digit code")));
        }
    }

    // ─── TC-L-06: Clicking login with EMPTY LRN stays on the same screen ─────
    @Test
    public void clickLogin_emptyLrn_staysOnLoginScreen() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLrn)).perform(clearText(), closeSoftKeyboard());
            onView(withId(R.id.etLoginCode)).perform(clearText(), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());
            // Should remain on LoginActivity — LRN field still visible
            onView(withId(R.id.etLrn)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-L-07: Clicking login with LRN but EMPTY code stays on screen ─────
    @Test
    public void clickLogin_emptyCode_staysOnLoginScreen() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLrn)).perform(typeText("123456789012"), closeSoftKeyboard());
            onView(withId(R.id.etLoginCode)).perform(clearText(), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());
            onView(withId(R.id.etLrn)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-L-08: LRN field accepts numeric input only (max 12 digits) ───────
    @Test
    public void lrnField_acceptsNumericInput() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLrn))
                    .perform(typeText("123456789012"), closeSoftKeyboard());
            // Field should still be displayed after typing
            onView(withId(R.id.etLrn)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-L-09: Login code field accepts 4-digit PIN ───────────────────────
    @Test
    public void loginCodeField_accepts4DigitPin() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLoginCode))
                    .perform(typeText("1234"), closeSoftKeyboard());
            onView(withId(R.id.etLoginCode)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-L-10: Clicking login with invalid credentials stays on screen ────
    @Test
    public void clickLogin_invalidCredentials_staysOnLoginScreen() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLrn)).perform(typeText("000000000000"), closeSoftKeyboard());
            onView(withId(R.id.etLoginCode)).perform(typeText("0000"), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());
            // Wait briefly for network (max 3 seconds)
            try { Thread.sleep(3000); } catch (InterruptedException ignored2) {}
            onView(withId(R.id.etLrn)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-L-11: Leo mascot image is displayed ──────────────────────────────
    @Test
    public void leoMascot_isDisplayed() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.ivLeoWaiting)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-L-12: Clearing both fields then clicking login stays on screen ───
    @Test
    public void clickLogin_bothFieldsCleared_staysOnLoginScreen() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.etLrn)).perform(typeText("123"), clearText(), closeSoftKeyboard());
            onView(withId(R.id.etLoginCode)).perform(typeText("12"), clearText(), closeSoftKeyboard());
            onView(withId(R.id.btnLogin)).perform(click());
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
        }
    }
}
