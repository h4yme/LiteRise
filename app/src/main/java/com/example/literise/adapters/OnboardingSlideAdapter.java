package com.example.literise.adapters;

import android.content.Context;
import android.graphics.Color;
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
            "Discover Stories from Islamic History",
            "Fun Quizzes After Each Story",
            "Grow with Rewards"
    };

    private final String[] descriptions = {
            "Listen to beautiful audio stories of Prophets and heroes of Islam – made just for kids!",
            "Unlock quizzes after listening to test what you've learned – smart and exciting!",
            "Earn points, unlock badges, and see your learning journey come to life!"
    };

    // Lottie raw resource IDs — replace JSONs in res/raw/ with actual LottieFiles animations
    private final int[] lottieRawRes = {
            R.raw.onboarding_lottie_1,
            R.raw.onboarding_lottie_2,
            R.raw.onboarding_lottie_3
    };

    // Per-slide background colors for the top section
    private final int[] bgColors = {
            Color.parseColor("#FFF3E0"),  // Warm orange tint  — Stories
            Color.parseColor("#F3E5F5"),  // Soft purple tint  — Quiz
            Color.parseColor("#E8F5E9")   // Soft green tint   — Rewards
    };

    public OnboardingSlideAdapter(Context context) {
        // context reserved for future use
    }

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
        holder.topSection.setBackgroundColor(bgColors[position]);
        holder.lottieView.setAnimation(lottieRawRes[position]);
        holder.lottieView.playAnimation();
    }

    @Override
    public void onViewRecycled(@NonNull SlideViewHolder holder) {
        super.onViewRecycled(holder);
        holder.lottieView.cancelAnimation();
    }

    @Override
    public int getItemCount() {
        return SLIDE_COUNT;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        LottieAnimationView lottieView;
        TextView title;
        TextView description;
        View topSection;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            lottieView = itemView.findViewById(R.id.lottieAnimation);
            title = itemView.findViewById(R.id.tvSlideTitle);
            description = itemView.findViewById(R.id.tvSlideDescription);
            topSection = itemView.findViewById(R.id.topSection);
        }
    }
}
