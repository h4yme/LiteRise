package com.example.literise.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.models.LearningModule;

import java.util.List;

public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder> {

    private Context context;
    private List<LearningModule> modules;
    private OnModuleClickListener listener;

    public interface OnModuleClickListener {
        void onModuleClick(LearningModule module);
    }

    public ModuleAdapter(Context context, List<LearningModule> modules, OnModuleClickListener listener) {
        this.context = context;
        this.modules = modules;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_learning_module, parent, false);
        return new ModuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        LearningModule module = modules.get(position);

        // ── Gradient background ────────────────────────────────────────────
        setGradientBackground(holder.gradientBackground,
                module.getGradientStart(), module.getGradientEnd());

        // ── Module title & subtitle ────────────────────────────────────────
        holder.tvModuleTitle.setText(module.getTitle());
        holder.tvSubtitle.setText(module.getSubtitle());

        // ── Domain badge (icon + label) ────────────────────────────────────
        holder.tvDomain.setText(module.getDomain());
        holder.ivDomainIcon.setImageResource(getDomainIcon(module.getDomain()));

        // ── Priority badge ─────────────────────────────────────────────────
        holder.tvPriority.setText("#" + module.getPriorityOrder());

        // ── Play / lock icon ───────────────────────────────────────────────
        holder.ivIcon.setImageResource(module.isLocked()
                ? R.drawable.ic_lock : R.drawable.ic_play);

        // ── Progress bar & percentage ──────────────────────────────────────
        int progressPct = module.getProgressPercentage();
        holder.progressBar.setProgress(progressPct);
        holder.tvProgress.setText(progressPct + "%");

        // ── Lesson count ───────────────────────────────────────────────────
        holder.tvLessonCount.setText(module.getTotalLessons() + " Lessons");

        // ── Locked overlay ─────────────────────────────────────────────────
        holder.lockOverlay.setVisibility(module.isLocked() ? View.VISIBLE : View.GONE);

        // ── Click ─────────────────────────────────────────────────────────
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && !module.isLocked()) {
                listener.onModuleClick(module);
            }
        });

        // Dim locked cards slightly
        holder.itemView.setAlpha(module.isLocked() ? 0.75f : 1.0f);
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private void setGradientBackground(FrameLayout layout, String colorStart, String colorEnd) {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.parseColor(colorStart), Color.parseColor(colorEnd)}
        );
        float radius = 20 * context.getResources().getDisplayMetrics().density;
        gradient.setCornerRadius(radius);
        layout.setBackground(gradient);
    }

    private int getDomainIcon(String domain) {
        if (domain == null) return R.drawable.ic_book;
        switch (domain) {
            case "Phonics":       return R.drawable.ic_mic;
            case "Vocabulary":    return R.drawable.ic_book_reading;
            case "Grammar":       return R.drawable.ic_edit;
            case "Comprehension": return R.drawable.ic_lightbulb;
            case "Writing":       return R.drawable.ic_edit;
            default:              return R.drawable.ic_book;
        }
    }

    // ── ViewHolder ─────────────────────────────────────────────────────────

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        FrameLayout gradientBackground;
        LinearLayout domainBadge;
        ImageView ivDomainIcon;
        TextView tvDomain;
        TextView tvPriority;
        ImageView ivIcon;
        TextView tvModuleTitle;
        TextView tvSubtitle;
        ProgressBar progressBar;
        TextView tvProgress;
        TextView tvLessonCount;
        LinearLayout lockOverlay;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            gradientBackground = itemView.findViewById(R.id.gradientBackground);
            domainBadge        = itemView.findViewById(R.id.domainBadge);
            ivDomainIcon       = itemView.findViewById(R.id.ivDomainIcon);
            tvDomain           = itemView.findViewById(R.id.tvDomain);
            tvPriority         = itemView.findViewById(R.id.tvPriority);
            ivIcon             = itemView.findViewById(R.id.ivIcon);
            tvModuleTitle      = itemView.findViewById(R.id.tvModuleTitle);
            tvSubtitle         = itemView.findViewById(R.id.tvSubtitle);
            progressBar        = itemView.findViewById(R.id.progressBar);
            tvProgress         = itemView.findViewById(R.id.tvProgress);
            tvLessonCount      = itemView.findViewById(R.id.tvLessonCount);
            lockOverlay        = itemView.findViewById(R.id.lockOverlay);
        }
    }
}