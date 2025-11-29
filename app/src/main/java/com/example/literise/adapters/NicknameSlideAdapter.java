package com.example.literise.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;

public class NicknameSlideAdapter extends RecyclerView.Adapter<NicknameSlideAdapter.NicknameSlideViewHolder> {

    private static final int[] layouts = {
            R.layout.nickname_slide_1,
            R.layout.nickname_slide_2,
            R.layout.nickname_slide_3,
            R.layout.nickname_slide_4
    };

    @NonNull
    @Override
    public NicknameSlideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        return new NicknameSlideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NicknameSlideViewHolder holder, int position) {
        // No binding needed for static slides
    }

    @Override
    public int getItemCount() {
        return layouts.length;
    }

    @Override
    public int getItemViewType(int position) {
        return layouts[position];
    }

    static class NicknameSlideViewHolder extends RecyclerView.ViewHolder {
        NicknameSlideViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
