package com.example.literise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.activities.WelcomeOnboardingActivity;

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

    private final int[] images = {
            R.drawable.onboarding_slide_1,  // Placeholder - replace with your vectors
            R.drawable.onboarding_slide_2,  // Placeholder - replace with your vectors
            R.drawable.onboarding_slide_3   // Placeholder - replace with your vectors
    };

    public OnboardingSlideAdapter(WelcomeOnboardingActivity activity) {
        // Constructor
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
        holder.image.setImageResource(images[position]);
    }

    @Override
    public int getItemCount() {
        return SLIDE_COUNT;
    }

    static class SlideViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView description;

        SlideViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.ivSlideImage);
            title = itemView.findViewById(R.id.tvSlideTitle);
            description = itemView.findViewById(R.id.tvSlideDescription);
        }
    }
}