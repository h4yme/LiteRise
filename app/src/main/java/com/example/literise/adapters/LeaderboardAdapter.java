package com.example.literise.adapters;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.models.LeaderboardEntry;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private static final int[] AVATAR_COLORS = {
            0xFF7C3AED, 0xFF2563EB, 0xFF059669, 0xFFDC2626,
            0xFFD97706, 0xFF7C3AED, 0xFF0891B2, 0xFF9333EA,
    };

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

        h.tvRank.setText(String.valueOf(entry.getRank()));
        h.tvValue.setText(String.valueOf(entry.getValue()));

        // Name — highlight current user
        boolean isMe = entry.getStudentId() == currentStudentId;
        String displayName = isMe ? entry.getName() + " (You)" : entry.getName();
        h.tvName.setText(displayName);
        h.tvName.setTextColor(isMe ? 0xFF7C3AED : 0xFF1F1F2E);

        // Grade
        h.tvGrade.setText(entry.getGrade() > 0 ? "Grade " + entry.getGrade() : "");

        // Avatar initial + color
        String initial = entry.getName() != null && !entry.getName().isEmpty()
                ? String.valueOf(entry.getName().charAt(0)).toUpperCase() : "?";
        h.tvAvatarInitial.setText(initial);
        int color = avatarColor(entry.getName());
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(color);
        h.avatarFrame.setBackground(bg);

        // Current user card highlight
        if (isMe) {
            h.itemView.setAlpha(1f);
            ((androidx.cardview.widget.CardView) h.itemView)
                    .setCardBackgroundColor(0xFFF5F3FF);
        } else {
            h.itemView.setAlpha(1f);
            ((androidx.cardview.widget.CardView) h.itemView)
                    .setCardBackgroundColor(0xFFFFFFFF);
        }
    }

    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }

    private int avatarColor(String name) {
        if (name == null || name.isEmpty()) return AVATAR_COLORS[0];
        return AVATAR_COLORS[Math.abs(name.charAt(0)) % AVATAR_COLORS.length];
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvGrade, tvValue, tvAvatarInitial;
        FrameLayout avatarFrame;

        ViewHolder(@NonNull View v) {
            super(v);
            tvRank         = v.findViewById(R.id.tvRank);
            tvName         = v.findViewById(R.id.tvName);
            tvGrade        = v.findViewById(R.id.tvGrade);
            tvValue        = v.findViewById(R.id.tvValue);
            tvAvatarInitial = v.findViewById(R.id.tvAvatarInitial);
            avatarFrame    = v.findViewById(R.id.ivMedal);
        }
    }
}
