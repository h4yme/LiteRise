package com.example.literise.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.literise.R;
import com.example.literise.models.LearningModule;

import java.util.List;

/**
 * Adapter for displaying learning modules in dashboard
 * Shows modules ordered by priority based on placement test results
 */
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

        // Set gradient background
        setGradientBackground(holder.gradientBackground, module.getGradientStart(), module.getGradientEnd());

        // Set module title
        holder.tvModuleTitle.setText(module.getTitle());

        // Set level indicator
        if (module.isLocked()) {
            holder.tvLevel.setText("Coming Soon");
            holder.ivIcon.setImageResource(R.drawable.ic_lock);
        } else {
            holder.tvLevel.setText("Level " + module.getLevel());
            holder.ivIcon.setImageResource(R.drawable.ic_play);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && !module.isLocked()) {
                listener.onModuleClick(module);
            }
        });

        // Dim locked modules
        holder.itemView.setAlpha(module.isLocked() ? 0.6f : 1.0f);
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    /**
     * Set gradient background programmatically
     */
    private void setGradientBackground(FrameLayout layout, String colorStart, String colorEnd) {
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR, // Top-left to bottom-right
                new int[] {
                        Color.parseColor(colorStart),
                        Color.parseColor(colorEnd)
                }
        );
        gradient.setCornerRadius(20 * context.getResources().getDisplayMetrics().density);
        layout.setBackground(gradient);
    }

    static class ModuleViewHolder extends RecyclerView.ViewHolder {
        FrameLayout gradientBackground;
        TextView tvLevel;
        TextView tvModuleTitle;
        ImageView ivIcon;
        ImageView ivLandscape;

        public ModuleViewHolder(@NonNull View itemView) {
            super(itemView);
            gradientBackground = itemView.findViewById(R.id.gradientBackground);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvModuleTitle = itemView.findViewById(R.id.tvModuleTitle);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivLandscape = itemView.findViewById(R.id.ivLandscape);
        }
    }
}