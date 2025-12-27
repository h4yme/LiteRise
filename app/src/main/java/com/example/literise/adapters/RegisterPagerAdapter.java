package com.example.literise.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.literise.fragments.RegisterStep1Fragment;
import com.example.literise.fragments.RegisterStep2Fragment;
import com.example.literise.fragments.RegisterStep3Fragment;
import com.example.literise.fragments.RegisterStep4Fragment;

public class RegisterPagerAdapter extends FragmentStateAdapter {

    public RegisterPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RegisterStep1Fragment();
            case 1:
                return new RegisterStep2Fragment();
            case 2:
                return new RegisterStep3Fragment();
            case 3:
                return new RegisterStep4Fragment();
            default:
                return new RegisterStep1Fragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // 4 steps
    }
}