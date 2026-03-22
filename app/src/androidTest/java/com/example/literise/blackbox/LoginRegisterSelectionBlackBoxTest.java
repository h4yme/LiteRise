package com.example.literise.blackbox;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.literise.R;
import com.example.literise.activities.LoginActivity;
import com.example.literise.activities.LoginRegisterSelectionActivity;

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
 * BLACK BOX TESTS — Login/Register Selection Screen
 *
 * Verifies the landing screen UI and navigation to LoginActivity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginRegisterSelectionBlackBoxTest {

    @Before
    public void setup() {
        Intents.init();
    }

    @After
    public void teardown() {
        Intents.release();
    }

    // ─── TC-S-01: App title "LiteRise" is shown ──────────────────────────────
    @Test
    public void appTitle_isDisplayed() {
        try (ActivityScenario<LoginRegisterSelectionActivity> ignored =
                     ActivityScenario.launch(LoginRegisterSelectionActivity.class)) {
            onView(withText("to LiteRise!")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-S-02: "Welcome" text is visible ──────────────────────────────────
    @Test
    public void welcomeText_isDisplayed() {
        try (ActivityScenario<LoginRegisterSelectionActivity> ignored =
                     ActivityScenario.launch(LoginRegisterSelectionActivity.class)) {
            onView(withText("Welcome")).check(matches(isDisplayed()));
        }
    }

    // ─── TC-S-03: Login card is displayed ────────────────────────────────────
    @Test
    public void loginCard_isDisplayed() {
        try (ActivityScenario<LoginRegisterSelectionActivity> ignored =
                     ActivityScenario.launch(LoginRegisterSelectionActivity.class)) {
            onView(withId(R.id.cardLogin)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-S-04: Tapping Login card navigates to LoginActivity ──────────────
    @Test
    public void clickLoginCard_navigatesToLoginActivity() {
        try (ActivityScenario<LoginRegisterSelectionActivity> ignored =
                     ActivityScenario.launch(LoginRegisterSelectionActivity.class)) {
            onView(withId(R.id.cardLogin)).perform(click());
            intended(hasComponent(LoginActivity.class.getName()));
        }
    }

    // ─── TC-S-05: Tagline about adventures is shown ──────────────────────────
    @Test
    public void adventureTagline_isDisplayed() {
        try (ActivityScenario<LoginRegisterSelectionActivity> ignored =
                     ActivityScenario.launch(LoginRegisterSelectionActivity.class)) {
            onView(withText(containsString("Reading Adventures")))
                    .check(matches(isDisplayed()));
        }
    }
}
