package com.example.literise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.literise.R;

public class OnboardingSlideAdapter extends RecyclerView.Adapter<OnboardingSlideAdapter.SlideViewHolder> {

    private static final int SLIDE_COUNT = 3;

    private final String[] titles = {
            "Welcome to LiteRise!",
            "Play & Learn English",
            "Rise with Every Reward"
    };

    private final String[] descriptions = {
            "Learn to read, write, and speak with Leo the Lion — fun adventures made just for kids like you!",
            "Master phonics, vocabulary, and grammar through exciting mini-games — level up with every lesson!",
            "Earn XP, collect badges, and keep your streak alive — watch your English skills soar!"
    };

    private final int[] lottieRawRes = {
            R.raw.onboarding_lottie_1,
            R.raw.onboarding_lottie_2,
            R.raw.onboarding_lottie_3
    };

    public OnboardingSlideAdapter(Context context) {}

    @NonNull
    @Override
    public SlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_onboarding_slide, parent, false);
        return new SlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SlideViewHolder holder, int position) {
        holder.title.setText(titles[position]);
        holder.description.setText(descriptions[position]);
        holder.lottieView.setAnimation(lottieRawRes[position]);
        holder.lottieView.playAnimation();
    }

    @Override
    public void onViewRecycled(@NonNull SlideViewHolder holder) {
        super.onViewRecycled(holder);
        holder.lottieView.cancelAnimation();
    }

    @Override
    public int getItemCount() { return SLIDE_COUNT; }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        LottieAnimationView lottieView;
        TextView title, description;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            lottieView  = itemView.findViewById(R.id.lottieAnimation);
            title       = itemView.findViewById(R.id.tvSlideTitle);
            description = itemView.findViewById(R.id.tvSlideDescription);
        }
    }
}
