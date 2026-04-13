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

        // Rank badge
        h.tvRank.setText(String.valueOf(entry.getRank()));
        styleRankBadge(h.tvRank, entry.getRank());

        // Score value
        h.tvValue.setText(String.valueOf(entry.getValue()));

        // Short unit label below score
        h.tvLabel.setText(shortUnit(entry.getLabel()));

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

        // Card highlight for current user
        ((androidx.cardview.widget.CardView) h.itemView)
                .setCardBackgroundColor(isMe ? 0xFFF5F3FF : 0xFFFFFFFF);
    }

    /** Maps full label to compact unit string shown below the score. */
    private String shortUnit(String label) {
        if (label == null) return "";
        if (label.contains("XP"))       return "XP";
        if (label.contains("Streak"))   return "day streak";
        if (label.contains("Lesson"))   return "lessons";
        if (label.contains("Badge"))    return "badges";
        return label;
    }

    /** Colors the rank badge differently for top-tier positions. */
    private void styleRankBadge(TextView tv, int rank) {
        if (rank <= 6) {
            // Top 6: vibrant purple badge
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(0xFF7C3AED);
            tv.setBackground(gd);
            tv.setTextColor(0xFFFFFFFF);
        } else if (rank <= 10) {
            // Rank 7-10: lighter purple badge
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(0xFFEDE9FE);
            tv.setBackground(gd);
            tv.setTextColor(0xFF6D28D9);
        } else {
            // Rank 11+: soft gray badge
            GradientDrawable gd = new GradientDrawable();
            gd.setShape(GradientDrawable.OVAL);
            gd.setColor(0xFFF3F4F6);
            tv.setBackground(gd);
            tv.setTextColor(0xFF9CA3AF);
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
        TextView tvRank, tvName, tvGrade, tvValue, tvLabel, tvAvatarInitial;
        FrameLayout avatarFrame;

        ViewHolder(@NonNull View v) {
            super(v);
            tvRank          = v.findViewById(R.id.tvRank);
            tvName          = v.findViewById(R.id.tvName);
            tvGrade         = v.findViewById(R.id.tvGrade);
            tvValue         = v.findViewById(R.id.tvValue);
            tvLabel         = v.findViewById(R.id.tvLabel);
            tvAvatarInitial = v.findViewById(R.id.tvAvatarInitial);
            avatarFrame     = v.findViewById(R.id.ivMedal);
        }
    }
}
