package com.example.literise.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.literise.R;
import com.example.literise.fragments.TutorialFragment;

public class TutorialPagerAdapter extends FragmentStateAdapter {

    private static final int[] TUTORIAL_IMAGES = {
            R.drawable.leo_welcome_1,
            R.drawable.leo_welcome_2,
            R.drawable.leo_welcome_3,
            R.drawable.leo_welcome_4
    };

    private static final String[] TUTORIAL_TITLES = {
            "Welcome to LiteRise!",
            "Learn and Play!",
            "Practice Reading!",
            "Ready to Start?"
    };

    private static final String[] TUTORIAL_DESCRIPTIONS = {
            "I'm Leo, your reading buddy! I'll help you learn to read in a fun way!",
            "We'll play fun games and earn exciting rewards as you learn!",
            "You'll practice reading out loud and I'll help you improve!",
            "Let's find your reading level first so I can give you the perfect lessons!"
    };

    public TutorialPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return TutorialFragment.newInstance(
                TUTORIAL_IMAGES[position],
                TUTORIAL_TITLES[position],
                TUTORIAL_DESCRIPTIONS[position]
        );
    }

    @Override
    public int getItemCount() {
        return TUTORIAL_IMAGES.length;
    }
}
