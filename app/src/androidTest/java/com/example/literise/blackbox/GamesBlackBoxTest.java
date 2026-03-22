package com.example.literise.blackbox;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.literise.R;
import com.example.literise.activities.games.SentenceScrambleActivity;
import com.example.literise.activities.games.FillInTheBlanksActivity;
import com.example.literise.activities.games.PictureMatchActivity;
import com.example.literise.activities.games.StorySequencingActivity;
import com.example.literise.activities.games.WordHuntActivity;
import com.example.literise.activities.games.TimedTrailActivity;
import com.example.literise.activities.games.MinimalPairsActivity;
import com.example.literise.activities.games.SynonymSprintActivity;
import com.example.literise.activities.games.WordExplosionActivity;
import com.example.literise.activities.games.DialogueReadingActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * BLACK BOX TESTS — Game Screens (10 game types)
 *
 * Verifies each game launches and shows its core UI elements.
 * Tests input controls (buttons, close button) are present.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class GamesBlackBoxTest {

    private static final String PREFS = "LiteRiseSession";

    @Before
    public void seedSession() {
        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit()
                .putInt("student_id", 1)
                .putString("fullname", "Test Student")
                .apply();
    }

    private Intent baseGameIntent(Class<?> activityClass) {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), activityClass);
        i.putExtra("node_id", 1);
        i.putExtra("module_id", 1);
        i.putExtra("lesson_id", 1);
        return i;
    }

    // ═══════════════════════════════════════════
    //  GAME 1 — Sentence Scramble
    // ═══════════════════════════════════════════

    // ─── TC-G1-01: Sentence Scramble launches without crash ───────────────────
    @Test
    public void sentenceScramble_launchesSuccessfully() {
        try (ActivityScenario<SentenceScrambleActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SentenceScrambleActivity.class))) {
        }
    }

    // ─── TC-G1-02: Sentence Scramble close button is visible ─────────────────
    @Test
    public void sentenceScramble_closeButtonVisible() {
        try (ActivityScenario<SentenceScrambleActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SentenceScrambleActivity.class))) {
            onView(withId(R.id.ivClose)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-G1-03: Sentence Scramble progress indicator visible ──────────────
    @Test
    public void sentenceScramble_progressVisible() {
        try (ActivityScenario<SentenceScrambleActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SentenceScrambleActivity.class))) {
            onView(withId(R.id.tvProgress)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-G1-04: Sentence Scramble answer zone is visible ──────────────────
    @Test
    public void sentenceScramble_answerZoneVisible() {
        try (ActivityScenario<SentenceScrambleActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SentenceScrambleActivity.class))) {
            onView(withId(R.id.cardAnswerZone)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-G1-05: Sentence Scramble word bank is visible ────────────────────
    @Test
    public void sentenceScramble_wordBankVisible() {
        try (ActivityScenario<SentenceScrambleActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SentenceScrambleActivity.class))) {
            onView(withId(R.id.cardWordBank)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-G1-06: Sentence Scramble Check button is visible and enabled ──────
    @Test
    public void sentenceScramble_checkButtonEnabled() {
        try (ActivityScenario<SentenceScrambleActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SentenceScrambleActivity.class))) {
            onView(withId(R.id.btnCheck))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ─── TC-G1-07: Sentence Scramble Hint button is visible ──────────────────
    @Test
    public void sentenceScramble_hintButtonVisible() {
        try (ActivityScenario<SentenceScrambleActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SentenceScrambleActivity.class))) {
            onView(withId(R.id.btnHint)).check(matches(isDisplayed()));
        }
    }

    // ─── TC-G1-08: Sentence Scramble timer is visible ────────────────────────
    @Test
    public void sentenceScramble_timerVisible() {
        try (ActivityScenario<SentenceScrambleActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SentenceScrambleActivity.class))) {
            onView(withId(R.id.tvTimer)).check(matches(isDisplayed()));
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 2 — Fill in the Blanks
    // ═══════════════════════════════════════════

    // ─── TC-G2-01: Fill in Blanks launches without crash ─────────────────────
    @Test
    public void fillInBlanks_launchesSuccessfully() {
        try (ActivityScenario<FillInTheBlanksActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(FillInTheBlanksActivity.class))) {
        }
    }

    // ─── TC-G2-02: Fill in Blanks close button is visible ────────────────────
    @Test
    public void fillInBlanks_closeButtonVisible() {
        try (ActivityScenario<FillInTheBlanksActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(FillInTheBlanksActivity.class))) {
            onView(withId(R.id.ivClose)).check(matches(isDisplayed()));
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 3 — Picture Match
    // ═══════════════════════════════════════════

    // ─── TC-G3-01: Picture Match launches without crash ──────────────────────
    @Test
    public void pictureMatch_launchesSuccessfully() {
        try (ActivityScenario<PictureMatchActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(PictureMatchActivity.class))) {
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 4 — Story Sequencing
    // ═══════════════════════════════════════════

    // ─── TC-G4-01: Story Sequencing launches without crash ───────────────────
    @Test
    public void storySequencing_launchesSuccessfully() {
        try (ActivityScenario<StorySequencingActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(StorySequencingActivity.class))) {
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 5 — Word Hunt
    // ═══════════════════════════════════════════

    // ─── TC-G5-01: Word Hunt launches without crash ───────────────────────────
    @Test
    public void wordHunt_launchesSuccessfully() {
        try (ActivityScenario<WordHuntActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(WordHuntActivity.class))) {
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 6 — Timed Trail
    // ═══════════════════════════════════════════

    // ─── TC-G6-01: Timed Trail launches without crash ────────────────────────
    @Test
    public void timedTrail_launchesSuccessfully() {
        try (ActivityScenario<TimedTrailActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(TimedTrailActivity.class))) {
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 7 — Minimal Pairs
    // ═══════════════════════════════════════════

    // ─── TC-G7-01: Minimal Pairs launches without crash ──────────────────────
    @Test
    public void minimalPairs_launchesSuccessfully() {
        try (ActivityScenario<MinimalPairsActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(MinimalPairsActivity.class))) {
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 8 — Synonym Sprint
    // ═══════════════════════════════════════════

    // ─── TC-G8-01: Synonym Sprint launches without crash ─────────────────────
    @Test
    public void synonymSprint_launchesSuccessfully() {
        try (ActivityScenario<SynonymSprintActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(SynonymSprintActivity.class))) {
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 9 — Word Explosion
    // ═══════════════════════════════════════════

    // ─── TC-G9-01: Word Explosion launches without crash ─────────────────────
    @Test
    public void wordExplosion_launchesSuccessfully() {
        try (ActivityScenario<WordExplosionActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(WordExplosionActivity.class))) {
        }
    }

    // ═══════════════════════════════════════════
    //  GAME 10 — Dialogue Reading
    // ═══════════════════════════════════════════

    // ─── TC-G10-01: Dialogue Reading launches without crash ──────────────────
    @Test
    public void dialogueReading_launchesSuccessfully() {
        try (ActivityScenario<DialogueReadingActivity> ignored =
                     ActivityScenario.launch(
                             baseGameIntent(DialogueReadingActivity.class))) {
        }
    }
}
