package com.example.literise.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.models.BadgeItem;

import java.util.List;

public class BadgeGridAdapter extends RecyclerView.Adapter<BadgeGridAdapter.ViewHolder> {

    private final Context context;
    private final List<BadgeItem> badges;

    public BadgeGridAdapter(Context context, List<BadgeItem> badges) {
        this.context = context;
        this.badges  = badges;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_badge_grid, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        BadgeItem badge = badges.get(position);

        // Load PNG by badge_id (badge_1 … badge_15)
        int resId = context.getResources().getIdentifier(
                badge.getDrawableName(), "drawable", context.getPackageName());
        if (resId != 0) {
            h.ivBadgeIcon.setImageResource(resId);
        } else {
            h.ivBadgeIcon.setImageResource(R.drawable.ic_badge);
        }

        h.tvBadgeName.setText(badge.getBadgeName());

        if (badge.isEarned()) {
            h.flLockedOverlay.setVisibility(View.GONE);
            h.ivEarnedCheck.setVisibility(View.VISIBLE);
            h.ivBadgeIcon.setAlpha(1f);
            h.tvBadgeName.setTextColor(0xFF374151);
        } else {
            h.flLockedOverlay.setVisibility(View.VISIBLE);
            h.ivEarnedCheck.setVisibility(View.GONE);
            h.ivBadgeIcon.setAlpha(0.35f);
            h.tvBadgeName.setTextColor(0xFF9CA3AF);
        }
    }

    @Override
    public int getItemCount() { return badges == null ? 0 : badges.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBadgeIcon, ivEarnedCheck;
        FrameLayout flLockedOverlay;
        TextView tvBadgeName;

        ViewHolder(@NonNull View v) {
            super(v);
            ivBadgeIcon      = v.findViewById(R.id.ivBadgeIcon);
            ivEarnedCheck    = v.findViewById(R.id.ivEarnedCheck);
            flLockedOverlay  = v.findViewById(R.id.flLockedOverlay);
            tvBadgeName      = v.findViewById(R.id.tvBadgeName);
        }
    }
}