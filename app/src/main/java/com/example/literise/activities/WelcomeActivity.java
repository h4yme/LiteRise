package com.example.literise.activities;



import android.content.Intent;

import android.media.MediaPlayer;

import android.os.Bundle;

import android.view.View;

import android.view.animation.AlphaAnimation;

import android.widget.TextView;



import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import androidx.viewpager2.widget.ViewPager2;



import com.example.literise.R;

import com.example.literise.adapters.IntroSlideAdapter;

import com.example.literise.database.SessionManager;

import com.google.android.material.button.MaterialButton;
import com.example.literise.utils.ZoomOutPageTransformer;


public class WelcomeActivity extends AppCompatActivity {



    private ViewPager2 viewPager;

    private MaterialButton btnNext;

    private TextView tvSkip;

    private View indicator1, indicator2, indicator3, indicator4, indicator5;

    private MediaPlayer soundPlayer;

    private SessionManager sessionManager;



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_welcome);

        // Initialize SessionManager

        sessionManager = new SessionManager(this);

        // Initialize views

        viewPager = findViewById(R.id.viewPager);

        btnNext = findViewById(R.id.btnNext);

        tvSkip = findViewById(R.id.tvSkip);

        indicator1 = findViewById(R.id.indicator1);

        indicator2 = findViewById(R.id.indicator2);

        indicator3 = findViewById(R.id.indicator3);

        indicator4 = findViewById(R.id.indicator4);

        indicator5 = findViewById(R.id.indicator5);

        // Set up ViewPager2

        IntroSlideAdapter adapter = new IntroSlideAdapter();

        viewPager.setAdapter(adapter);

        // Add smooth page transformer for zoom effect

        viewPager.setPageTransformer(new ZoomOutPageTransformer());

        // Fade-in animation

        View rootView = findViewById(android.R.id.content);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);

        fadeIn.setDuration(800);

        rootView.startAnimation(fadeIn);



        // ViewPager page change listener

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {

            @Override

            public void onPageSelected(int position) {

                super.onPageSelected(position);

                updateIndicators(position);



                // Update button text on last slide

                if (position == 4) {

                    btnNext.setText("Let's Start! 🚀");

                    tvSkip.setVisibility(View.INVISIBLE);

                } else {

                    btnNext.setText("Next");

                    tvSkip.setVisibility(View.VISIBLE);

                }

            }

        });



        // Next button click

        btnNext.setOnClickListener(v -> {

            playClickSound();



            // Button press animation

            v.animate()

                    .scaleX(0.95f)

                    .scaleY(0.95f)

                    .setDuration(100)

                    .withEndAction(() -> {

                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();



                        int currentItem = viewPager.getCurrentItem();

                        if (currentItem < 4) {

                            // Go to next slide

                            viewPager.setCurrentItem(currentItem + 1, true);

                        } else {

                            // Last slide - proceed to nickname setup

                            goToNicknameSetup();

                        }

                    }).start();

        });



        // Skip button click

        tvSkip.setOnClickListener(v -> {

            playClickSound();

            goToNicknameSetup();

        });

    }



    private void updateIndicators(int position) {

        // Reset all indicators

        indicator1.setBackgroundResource(R.drawable.indicator_inactive);

        indicator2.setBackgroundResource(R.drawable.indicator_inactive);

        indicator3.setBackgroundResource(R.drawable.indicator_inactive);



        indicator4.setBackgroundResource(R.drawable.indicator_inactive);

        indicator5.setBackgroundResource(R.drawable.indicator_inactive);



        // Set active indicator

        switch (position) {

            case 0:

                indicator1.setBackgroundResource(R.drawable.indicator_active);

                break;

            case 1:

                indicator2.setBackgroundResource(R.drawable.indicator_active);

                break;

            case 2:

                indicator3.setBackgroundResource(R.drawable.indicator_active);

                break;

            case 3:

                indicator4.setBackgroundResource(R.drawable.indicator_active);

                break;

            case 4:

                indicator5.setBackgroundResource(R.drawable.indicator_active);

                break;

        }

    }



    private void playClickSound() {

        try {

            if (soundPlayer != null) {

                soundPlayer.release();

            }

            soundPlayer = MediaPlayer.create(this, R.raw.sound_button_click);

            soundPlayer.setOnCompletionListener(MediaPlayer::release);

            soundPlayer.start();

        } catch (Exception e) {

            // Sound file might not exist yet - ignore

        }

    }



    private void goToNicknameSetup() {

        Intent intent = new Intent(WelcomeActivity.this, NicknameSetupActivity.class);

        startActivity(intent);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

        finish();

    }



    @Override

    protected void onDestroy() {

        super.onDestroy();

        if (soundPlayer != null) {

            soundPlayer.release();

            soundPlayer = null;

        }

    }

}