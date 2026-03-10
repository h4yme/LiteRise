package com.example.literise.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.literise.R;
import com.example.literise.models.BadgeItem;
import com.google.android.material.button.MaterialButton;

import java.util.List;

/**
 * Shows a modal dialog celebrating a newly earned badge.
 * If multiple badges were earned, shows them one after another.
 */
public class BadgeEarnedDialog {

    public static void show(Context context, List<BadgeItem> newBadges, Runnable onAllDismissed) {
        if (newBadges == null || newBadges.isEmpty()) {
            if (onAllDismissed != null) onAllDismissed.run();
            return;
        }
        showOne(context, newBadges, 0, onAllDismissed);
    }

    private static void showOne(Context context, List<BadgeItem> badges,
                                int index, Runnable onAllDismissed) {
        if (index >= badges.size()) {
            if (onAllDismissed != null) onAllDismissed.run();
            return;
        }

        BadgeItem badge = badges.get(index);

        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_badge_earned, null);
        dialog.setContentView(view);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.getWindow().setLayout(
                    (int)(context.getResources().getDisplayMetrics().widthPixels * 0.85),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        ImageView ivBadge        = view.findViewById(R.id.ivDialogBadge);
        TextView tvName          = view.findViewById(R.id.tvDialogBadgeName);
        TextView tvDesc          = view.findViewById(R.id.tvDialogBadgeDesc);
        LinearLayout llXp        = view.findViewById(R.id.llXpReward);
        TextView tvXp            = view.findViewById(R.id.tvDialogXP);
        MaterialButton btnClose  = view.findViewById(R.id.btnDialogClose);

        // Set badge PNG
        int resId = context.getResources().getIdentifier(
                "badge_" + badge.getBadgeId(), "drawable", context.getPackageName());
        if (resId != 0) ivBadge.setImageResource(resId);
        else            ivBadge.setImageResource(R.drawable.ic_badge);

        tvName.setText(badge.getBadgeName());
        tvDesc.setText(badge.getBadgeDescription());

        if (badge.getXpReward() > 0) {
            llXp.setVisibility(View.VISIBLE);
            tvXp.setText("+" + badge.getXpReward() + " XP");
        } else {
            llXp.setVisibility(View.GONE);
        }

        boolean isLast = (index == badges.size() - 1);
        btnClose.setText(isLast ? "Awesome!" : "Next Badge ›");
        btnClose.setOnClickListener(v -> {
            dialog.dismiss();
            showOne(context, badges, index + 1, onAllDismissed);
        });

        dialog.show();
    }
}