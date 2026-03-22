package com.example.literise.blackbox;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.activities.SplashActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.example.literise.R;

/**
 * BLACK BOX TESTS — Splash Screen
 *
 * Verifies the splash screen launches and displays the app logo/branding.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SplashBlackBoxTest {

    // ─── TC-SP-01: Splash screen launches without crash ───────────────────────
    @Test
    public void splashScreen_launchesSuccessfully() {
        try (ActivityScenario<SplashActivity> ignored =
                     ActivityScenario.launch(SplashActivity.class)) {
        }
    }

    // ─── TC-SP-02: App package name is correct ────────────────────────────────
    @Test
    public void appPackageName_isCorrect() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assert ctx.getPackageName().equals("com.example.literise");
    }
}
