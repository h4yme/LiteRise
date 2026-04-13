package com.example.literise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.models.LeaderboardEntry;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<LeaderboardEntry> entries;
    private final int currentStudentId;

    public LeaderboardAdapter(List<LeaderboardEntry> entries, int currentStudentId) {
        this.entries = entries;
        this.currentStudentId = currentStudentId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        LeaderboardEntry entry = entries.get(position);
        Context ctx = h.itemView.getContext();

        h.tvRank.setText(String.valueOf(entry.getRank()));
        h.tvName.setText(entry.getName());
        h.tvGrade.setText(entry.getGrade() != null ? "Grade " + entry.getGrade() : "");
        h.tvValue.setText(String.valueOf(entry.getValue()));

        // Style top 3 ranks
        int rank = entry.getRank();
        if (rank == 1) {
            h.tvRank.setTextColor(0xFFFFD700); // gold
            h.ivMedal.setVisibility(View.VISIBLE);
            h.ivMedal.setImageResource(R.drawable.ic_trophy);
            h.ivMedal.setColorFilter(0xFFFFD700);
            h.itemView.setBackgroundResource(R.drawable.bg_leaderboard_gold);
        } else if (rank == 2) {
            h.tvRank.setTextColor(0xFFC0C0C0); // silver
            h.ivMedal.setVisibility(View.VISIBLE);
            h.ivMedal.setImageResource(R.drawable.ic_trophy);
            h.ivMedal.setColorFilter(0xFFC0C0C0);
            h.itemView.setBackgroundResource(R.drawable.bg_leaderboard_silver);
        } else if (rank == 3) {
            h.tvRank.setTextColor(0xFFCD7F32); // bronze
            h.ivMedal.setVisibility(View.VISIBLE);
            h.ivMedal.setImageResource(R.drawable.ic_trophy);
            h.ivMedal.setColorFilter(0xFFCD7F32);
            h.itemView.setBackgroundResource(R.drawable.bg_leaderboard_bronze);
        } else {
            h.tvRank.setTextColor(0xFF6B7280);
            h.ivMedal.setVisibility(View.GONE);
            h.itemView.setBackgroundResource(R.drawable.bg_leaderboard_item);
        }

        // Highlight current user
        if (entry.getStudentId() == currentStudentId) {
            h.tvName.setTextColor(0xFF7C3AED);
            h.tvName.setText(entry.getName() + " (You)");
        } else {
            h.tvName.setTextColor(0xFF111827);
        }
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvGrade, tvValue;
        ImageView ivMedal;

        ViewHolder(@NonNull View v) {
            super(v);
            tvRank  = v.findViewById(R.id.tvRank);
            tvName  = v.findViewById(R.id.tvName);
            tvGrade = v.findViewById(R.id.tvGrade);
            tvValue = v.findViewById(R.id.tvValue);
            ivMedal = v.findViewById(R.id.ivMedal);
        }
    }
}
