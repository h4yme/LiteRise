package com.example.literise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;

public class IntroSlideAdapter extends RecyclerView.Adapter<IntroSlideAdapter.IntroViewHolder> {

    private final int[] layouts = {
            R.layout.intro_slide_1,
            R.layout.intro_slide_2,
            R.layout.intro_slide_3
    };

    @NonNull
    @Override
    public IntroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layouts[viewType], parent, false);
        return new IntroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IntroViewHolder holder, int position) {
        // Slides are static, no binding needed
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class IntroViewHolder extends RecyclerView.ViewHolder {
        IntroViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
