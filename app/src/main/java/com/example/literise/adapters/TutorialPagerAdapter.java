package com.example.literise.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.literise.fragments.TutorialFragment;

public class TutorialPagerAdapter extends FragmentStateAdapter {

    private static final String[][] TUTORIAL_DATA = {
            {"🦁", "Welcome to LiteRise!", "I'm Leo, your reading buddy! I'll help you learn to read in a fun way!"},
            {"📚", "Learn and Play!", "We'll play fun games and earn exciting rewards as you learn!"},
            {"🎤", "Practice Reading!", "You'll practice reading out loud and I'll help you improve!"},
            {"🏆", "Ready to Start?", "Let's find your reading level first so I can give you the perfect lessons!"}
    };

    public TutorialPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String[] data = TUTORIAL_DATA[position];
        return TutorialFragment.newInstance(data[0], data[1], data[2]);
    }

    @Override
    public int getItemCount() {
        return TUTORIAL_DATA.length;
    }
}
