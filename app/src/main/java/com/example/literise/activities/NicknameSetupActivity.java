package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android:view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.literise.R;
import com.example.literise.adapters.NicknameSlideAdapter;
import com.example.literise.database.SessionManager;
import com.example.literise.utils.CustomToast;
import com.google.android.material.button.MaterialButton;

public class NicknameSetupActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialButton btnNext;
    private View indicator1, indicator2, indicator3, indicator4;
    private SessionManager sessionManager;
    private String nickname = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nickname_setup);

        sessionManager = new SessionManager(this);
        initializeViews();
        setupViewPager();
        setupAnimations();
        setupListeners();
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.viewPager);
        btnNext = findViewById(R.id.btnNext);
        indicator1 = findViewById(R.id.indicator1);
        indicator2 = findViewById(R.id.indicator2);
        indicator3 = findViewById(R.id.indicator3);
        indicator4 = findViewById(R.id.indicator4);
    }

    private void setupViewPager() {
        NicknameSlideAdapter adapter = new NicknameSlideAdapter();
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
                updateButtonText(position);
            }
        });
    }

    private void setupAnimations() {
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(800);
        rootView.startAnimation(fadeIn);
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        handleNextClick();
                    })
                    .start();
        });
    }

    private void handleNextClick() {
        int currentItem = viewPager.getCurrentItem();

        if (currentItem == 2) {
            // On slide 3 (nickname input), validate and save nickname
            View currentSlide = viewPager.findViewWithTag("android:switcher:" + R.id.viewPager + ":" + currentItem);
            if (currentSlide != null) {
                EditText etNickname = currentSlide.findViewById(R.id.etNickname);
                if (etNickname != null) {
                    nickname = etNickname.getText().toString().trim();

                    if (nickname.isEmpty()) {
                        CustomToast.showWarning(this, "Please enter a nickname");
                        return;
                    }

                    if (!nickname.matches("[a-zA-Z0-9 ]+")) {
                        CustomToast.showWarning(this, "Please use only letters and numbers");
                        return;
                    }

                    sessionManager.saveNickname(nickname);
                }
            }

            // Move to congratulations slide and update greeting
            viewPager.setCurrentItem(3, true);
            viewPager.postDelayed(() -> updateGreeting(), 300);

        } else if (currentItem < 3) {
            // Go to next slide
            viewPager.setCurrentItem(currentItem + 1, true);
        } else {
            // Last slide - proceed to placement test
            proceedToPlacementTest();
        }
    }

    private void updateGreeting() {
        View currentSlide = viewPager.findViewWithTag("android:switcher:" + R.id.viewPager + ":3");
        if (currentSlide != null) {
            TextView tvGreeting = currentSlide.findViewById(R.id.tvGreeting);
            if (tvGreeting != null && !nickname.isEmpty()) {
                tvGreeting.setText("Hi " + nickname + "! I'm so\nexcited to learn\nwith you!");
            }
        }
    }

    private void updateIndicators(int position) {
        indicator1.setBackgroundResource(R.drawable.indicator_inactive);
        indicator2.setBackgroundResource(R.drawable.indicator_inactive);
        indicator3.setBackgroundResource(R.drawable.indicator_inactive);
        indicator4.setBackgroundResource(R.drawable.indicator_inactive);

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
        }
    }

    private void updateButtonText(int position) {
        if (position == 3) {
            btnNext.setText("Let's Start!");
        } else {
            btnNext.setText("Next");
        }
    }

    private void proceedToPlacementTest() {
        sessionManager.setHasSeenWelcome(true);

        Intent intent = new Intent(NicknameSetupActivity.this, AdaptivePreAssessmentActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        int currentItem = viewPager.getCurrentItem();
        if (currentItem > 0) {
            viewPager.setCurrentItem(currentItem - 1, true);
        } else {
            super.onBackPressed();
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }
}
