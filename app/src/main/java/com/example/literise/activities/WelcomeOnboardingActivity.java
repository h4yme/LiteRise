package com.example.literise.activities;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.literise.R;
import com.example.literise.adapters.OnboardingSlideAdapter;
import com.example.literise.database.SessionManager;
import com.google.android.material.button.MaterialButton;

public class WelcomeOnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private View indicator1, indicator2, indicator3;
    private MaterialButton btnContinue;
    private TextView tvSkip;
    private ImageButton btnSound;
    private OnboardingSlideAdapter adapter;

    private MediaPlayer mediaPlayer;
    private boolean isMusicOn = true;

    private static final int INDICATOR_ACTIVE_DP = 32;
    private static final int INDICATOR_INACTIVE_DP = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_onboarding);

        viewPager    = findViewById(R.id.viewPager);
        indicator1   = findViewById(R.id.indicator1);
        indicator2   = findViewById(R.id.indicator2);
        indicator3   = findViewById(R.id.indicator3);
        btnContinue  = findViewById(R.id.btnContinue);
        tvSkip       = findViewById(R.id.tvSkip);
        btnSound     = findViewById(R.id.btnSound);

        adapter = new OnboardingSlideAdapter(this);
        viewPager.setAdapter(adapter);

        startBackgroundMusic();

        tvSkip.setOnClickListener(v -> finishOnboarding());

        btnContinue.setOnClickListener(v -> {
            int current = viewPager.getCurrentItem();
            if (current < adapter.getItemCount() - 1) {
                viewPager.setCurrentItem(current + 1);
            } else {
                finishOnboarding();
            }
        });

        btnSound.setOnClickListener(v -> toggleMusic());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                btnContinue.setText(position == adapter.getItemCount() - 1 ? "Get Started!" : "Continue");
                updateIndicators(position);
            }
        });
    }

    // ─── Music ───────────────────────────────────────────────────────────────

    private void startBackgroundMusic() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.intro_music);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0.45f, 0.45f);
                mediaPlayer.start();
                isMusicOn = true;
                btnSound.setImageResource(R.drawable.ic_volume_up);
            }
        } catch (Exception e) {
            // Music is not critical — continue silently
        }
    }

    private void toggleMusic() {
        if (mediaPlayer == null) return;
        if (isMusicOn) {
            mediaPlayer.pause();
            isMusicOn = false;
            btnSound.setImageResource(R.drawable.ic_volume_off);
            btnSound.setBackgroundResource(R.drawable.bg_circle_gray);
        } else {
            mediaPlayer.start();
            isMusicOn = true;
            btnSound.setImageResource(R.drawable.ic_volume_up);
            btnSound.setBackgroundResource(R.drawable.bg_circle_orange);
        }
    }

    // ─── Indicators ──────────────────────────────────────────────────────────

    private void updateIndicators(int position) {
        animateIndicator(indicator1, position == 0);
        animateIndicator(indicator2, position == 1);
        animateIndicator(indicator3, position == 2);

        indicator1.setBackgroundResource(position == 0 ? R.drawable.indicator_dot_active : R.drawable.indicator_dot_inactive);
        indicator2.setBackgroundResource(position == 1 ? R.drawable.indicator_dot_active : R.drawable.indicator_dot_inactive);
        indicator3.setBackgroundResource(position == 2 ? R.drawable.indicator_dot_active : R.drawable.indicator_dot_inactive);
    }

    private void animateIndicator(View indicator, boolean isActive) {
        float density = getResources().getDisplayMetrics().density;
        int targetPx  = (int) ((isActive ? INDICATOR_ACTIVE_DP : INDICATOR_INACTIVE_DP) * density);
        int currentPx = indicator.getLayoutParams().width;
        if (currentPx == targetPx) return;

        ValueAnimator anim = ValueAnimator.ofInt(currentPx, targetPx);
        anim.setDuration(250);
        anim.addUpdateListener(va -> {
            ViewGroup.LayoutParams params = indicator.getLayoutParams();
            params.width = (int) va.getAnimatedValue();
            indicator.setLayoutParams(params);
        });
        anim.start();
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    private void finishOnboarding() {
        new SessionManager(this).setHasSeenWelcome(true);
        Intent intent = new Intent(WelcomeOnboardingActivity.this, PlacementIntroActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && isMusicOn && !mediaPlayer.isPlaying()) mediaPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
