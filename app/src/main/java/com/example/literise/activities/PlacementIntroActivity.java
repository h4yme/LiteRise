package com.example.literise.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.example.literise.R;
import com.example.literise.utils.LocationHelper;
import com.google.android.material.button.MaterialButton;

public class PlacementIntroActivity extends AppCompatActivity {

    private static final int STEP_COUNT = 4;
    private static final int REQUEST_LOCATION_PERMISSION = 2001;
    /** How long (ms) to wait for a fresh GPS/network fix before giving up. */
    private static final long LOCATION_TIMEOUT_MS = 10_000;

    private ImageView ivLeo;
    private TextView tvStepTitle;
    private TextView tvStepDescription;
    private MaterialButton btnNext;
    private TextView tvSkip;
    private View[] dots;

    private ObjectAnimator floatAnimator;
    private MediaPlayer voicePlayer;
    private int currentStep = 0;

    /** Cached intent extra so we can pass it through the permission callback. */
    private String assessmentType;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Handler locationTimeoutHandler;
    private boolean locationResultDelivered = false;

    private static final String[] TITLES = {
            "Hi! I'm Leo! \uD83D\uDC4B",
            "A Quick Reading Check \uD83D\uDCDA",
            "What to Expect \uD83C\uDFAF",
            "You're All Set! \uD83C\uDF1F"
    };

    private static final String[] DESCRIPTIONS = {
            "I'm your reading buddy here at LiteRise! Together we'll go on an amazing English adventure built just for you.",
            "I'll ask you 25 fun questions to find your perfect reading level. Don't worry — it's not a real test! Think of it as a reading adventure.",
            "We'll explore phonics, vocabulary, grammar, reading, and writing! Questions adjust as we go — I'll make sure it's just right for you.",
            "Take your time, trust yourself, and have fun! I'll be right here cheering you on every single step of the way. Ready to rise?"
    };

    private static final int[] LEO_EXPRESSIONS = {
            R.drawable.leo_wave,      // Step 1
            R.drawable.leo_thinking,  // Step 2
            R.drawable.leo_explain,   // Step 3
            R.drawable.leo_cheer      // Step 4
    };

    // One MP3 per step — place all 4 in res/raw/
    private static final int[] STEP_VOICEOVERS = {
            R.raw.step1,      // Step 1 ✅ already have
            R.raw.step2,  // Step 2 ✅ already have
            R.raw.step3,       // Step 3 — add your mp3
            R.raw.step4         // Step 4 — add your mp3
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assessmentType = getIntent().getStringExtra("assessment_type");

        // ── Location check must pass before the assessment is accessible ──────
        checkLocationBeforeProceeding();
    }

    // ─── Location gate ────────────────────────────────────────────────────────

    /**
     * Entry point: verify permission → get location → check school area.
     * Only calls {@link #proceedAfterLocationCheck()} when the user is
     * within an allowed school.
     */
    private void checkLocationBeforeProceeding() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            fetchLocationAndCheck();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndCheck();
            } else {
                showLocationDeniedDialog();
            }
        }
    }

    /**
     * Attempts to get the device's current location. Tries the last-known
     * location first (instant); if unavailable, requests a fresh fix with a
     * {@link #LOCATION_TIMEOUT_MS} timeout.
     */
    private void fetchLocationAndCheck() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // 1. Try last-known from GPS, then from network
        Location best = null;
        for (String provider : new String[]{LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER}) {
            if (locationManager.isProviderEnabled(provider)) {
                try {
                    Location loc = locationManager.getLastKnownLocation(provider);
                    if (loc != null && (best == null || loc.getAccuracy() < best.getAccuracy())) {
                        best = loc;
                    }
                } catch (SecurityException ignored) { }
            }
        }

        if (best != null) {
            evaluateLocation(best);
            return;
        }

        // 2. No last-known — request a fresh fix
        locationTimeoutHandler = new Handler(Looper.getMainLooper());
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (!locationResultDelivered) {
                    locationResultDelivered = true;
                    stopLocationUpdates();
                    evaluateLocation(location);
                }
            }
            @Override public void onProviderDisabled(@NonNull String provider) { }
            @Override public void onProviderEnabled(@NonNull String provider) { }
        };

        boolean requested = false;
        for (String provider : new String[]{LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER}) {
            if (locationManager.isProviderEnabled(provider)) {
                try {
                    locationManager.requestLocationUpdates(
                            provider, 0, 0, locationListener, Looper.getMainLooper());
                    requested = true;
                } catch (SecurityException ignored) { }
            }
        }

        if (!requested) {
            // No provider available
            showLocationUnavailableDialog();
            return;
        }

        // Timeout — stop waiting after LOCATION_TIMEOUT_MS
        locationTimeoutHandler.postDelayed(() -> {
            if (!locationResultDelivered) {
                locationResultDelivered = true;
                stopLocationUpdates();
                showLocationUnavailableDialog();
            }
        }, LOCATION_TIMEOUT_MS);
    }

    private void stopLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        if (locationTimeoutHandler != null) {
            locationTimeoutHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Checks whether the obtained location is inside an allowed school zone.
     */
    private void evaluateLocation(Location location) {
        if (LocationHelper.isWithinAllowedArea(location.getLatitude(), location.getLongitude())) {
            proceedAfterLocationCheck();
        } else {
            showOutOfAreaDialog();
        }
    }

    /** Shows a dialog when the user is outside both school areas. */
    private void showOutOfAreaDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Assessment Unavailable")
                .setMessage("You must be physically present at Holy Spirit Elementary School "
                        + "or Dona Juana Elementary School to take this assessment.")
                .setPositiveButton("OK", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    /** Shows a dialog when location permission was denied. */
    private void showLocationDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Permission Required")
                .setMessage("Location access is needed to verify that you are at an authorized "
                        + "school before taking the assessment. Please grant location permission.")
                .setPositiveButton("OK", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    /** Shows a dialog when no location provider is available. */
    private void showLocationUnavailableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Location Unavailable")
                .setMessage("Unable to determine your location. Please enable GPS or Wi-Fi "
                        + "location services and try again.")
                .setPositiveButton("OK", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    // ─── Post-location-check flow ─────────────────────────────────────────────

    /**
     * Called only when the user is confirmed to be within an allowed school.
     * Resumes the normal activity flow.
     */
    private void proceedAfterLocationCheck() {
        // Skip intro for post-assessment — go straight to the test
        if ("POST".equals(assessmentType)) {
            startPlacementTest();
            return;
        }

        setContentView(R.layout.activity_placement_intro);

        ivLeo             = findViewById(R.id.ivLeo);
        tvStepTitle       = findViewById(R.id.tvStepTitle);
        tvStepDescription = findViewById(R.id.tvStepDescription);
        btnNext           = findViewById(R.id.btnNext);
        tvSkip            = findViewById(R.id.tvSkip);

        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4)
        };

        startLeoFloat();
        applyStep(0);

        btnNext.setOnClickListener(v -> {
            if (currentStep < STEP_COUNT - 1) {
                currentStep++;
                animateToNextStep();
            } else {
                startPlacementTest();
            }
        });

        tvSkip.setOnClickListener(v -> startPlacementTest());
    }

    // ─── Voiceover ────────────────────────────────────────────────────────────

    private void playStepVoiceover(int step) {
        stopVoiceover();
        try {
            voicePlayer = MediaPlayer.create(this, STEP_VOICEOVERS[step]);
            if (voicePlayer != null) {
                voicePlayer.setOnCompletionListener(mp -> stopVoiceover());
                voicePlayer.start();
            }
        } catch (Exception e) {
            // Missing file or error — silently skip so UI still works
        }
    }

    private void stopVoiceover() {
        if (voicePlayer != null) {
            if (voicePlayer.isPlaying()) voicePlayer.stop();
            voicePlayer.release();
            voicePlayer = null;
        }
    }

    // ─── Leo floating animation ───────────────────────────────────────────────

    private void startLeoFloat() {
        float density = getResources().getDisplayMetrics().density;
        floatAnimator = ObjectAnimator.ofFloat(ivLeo, "translationY",
                -12 * density, 12 * density);
        floatAnimator.setDuration(1800);
        floatAnimator.setRepeatCount(ValueAnimator.INFINITE);
        floatAnimator.setRepeatMode(ValueAnimator.REVERSE);
        floatAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        floatAnimator.start();
    }

    // ─── Step transitions ─────────────────────────────────────────────────────

    private void applyStep(int step) {
        ivLeo.setImageResource(LEO_EXPRESSIONS[step]);
        tvStepTitle.setText(TITLES[step]);
        tvStepDescription.setText(DESCRIPTIONS[step]);
        btnNext.setText(step == STEP_COUNT - 1 ? "Start Test! \uD83D\uDE80" : "Next \u2192");
        updateDots(step);
        playStepVoiceover(step);  // ← plays voiceover for this step
    }

    private void animateToNextStep() {
        tvStepTitle.animate().alpha(0f).setDuration(140).withEndAction(() -> {
            tvStepTitle.setText(TITLES[currentStep]);
            tvStepTitle.animate().alpha(1f).setDuration(200).start();
        }).start();

        tvStepDescription.animate().alpha(0f).setDuration(140).withEndAction(() -> {
            tvStepDescription.setText(DESCRIPTIONS[currentStep]);
            tvStepDescription.animate().alpha(1f).setDuration(200).start();
        }).start();

        ivLeo.animate()
                .scaleX(0.88f).scaleY(0.88f)
                .setDuration(130)
                .withEndAction(() -> {
                    ivLeo.setImageResource(LEO_EXPRESSIONS[currentStep]);
                    ivLeo.animate()
                            .scaleX(1f).scaleY(1f)
                            .setDuration(220)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                }).start();

        btnNext.setText(currentStep == STEP_COUNT - 1 ? "Start Test! \uD83D\uDE80" : "Next \u2192");
        updateDots(currentStep);
        playStepVoiceover(currentStep);  // ← stops old, plays new
    }

    // ─── Dot indicators ───────────────────────────────────────────────────────

    private void updateDots(int activeStep) {
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < dots.length; i++) {
            boolean isActive = (i == activeStep);
            int targetPx = (int) ((isActive ? 32 : 10) * density);
            dots[i].setBackgroundResource(isActive
                    ? R.drawable.indicator_dot_active
                    : R.drawable.indicator_dot_placement_inactive);

            final View dot = dots[i];
            ValueAnimator anim = ValueAnimator.ofInt(dot.getLayoutParams().width, targetPx);
            anim.setDuration(250);
            anim.addUpdateListener(va -> {
                ViewGroup.LayoutParams params = dot.getLayoutParams();
                params.width = (int) va.getAnimatedValue();
                dot.setLayoutParams(params);
            });
            anim.start();
        }
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    private void startPlacementTest() {
        stopVoiceover();
        Intent intent = new Intent(PlacementIntroActivity.this, PlacementTestActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    protected void onPause() {
        super.onPause();
        if (voicePlayer != null && voicePlayer.isPlaying()) voicePlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (voicePlayer != null && !voicePlayer.isPlaying()) voicePlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        if (floatAnimator != null) floatAnimator.cancel();
        stopVoiceover();
    }

    @Override
    public void onBackPressed() {
        // Block back — user must complete or skip walkthrough
    }
}
