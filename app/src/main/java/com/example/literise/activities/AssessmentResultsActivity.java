package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android:view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.literise.R;
import com.example.literise.database.SessionManager;
import com.example.literise.models.Module;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AssessmentResultsActivity extends AppCompatActivity {

    private TextView tvTitle, tvSubtitle, tvScore, tvAccuracy;
    private LinearLayout moduleListContainer;
    private MaterialButton btnStartLearning;
    private SessionManager sessionManager;

    private int correctAnswers;
    private int totalQuestions;
    private double accuracy;
    private List<Module> modulesInOrder = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assessment_results);

        sessionManager = new SessionManager(this);

        // Get assessment results from intent
        correctAnswers = getIntent().getIntExtra("correct_answers", 0);
        totalQuestions = getIntent().getIntExtra("total_questions", 10);
        accuracy = getIntent().getDoubleExtra("accuracy", 0.0);

        initializeViews();
        setupAnimations();
        displayResults();
        determineModuleUnlockOrder();
        displayModules();
        setupListeners();
    }

    private void initializeViews() {
        tvTitle = findViewById(R.id.tvTitle);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvScore = findViewById(R.id.tvScore);
        tvAccuracy = findViewById(R.id.tvAccuracy);
        moduleListContainer = findViewById(R.id.moduleListContainer);
        btnStartLearning = findViewById(R.id.btnStartLearning);
    }

    private void setupAnimations() {
        View rootView = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1000);
        rootView.startAnimation(fadeIn);
    }

    private void displayResults() {
        tvScore.setText(String.format("%d/%d", correctAnswers, totalQuestions));
        tvAccuracy.setText(String.format("%.0f%% Correct", accuracy));
    }

    private void determineModuleUnlockOrder() {
        // TODO: In production, this should be calculated by the API based on assessment results
        // For now, we'll create a default module order
        // The API should analyze which areas are weakest and order modules accordingly

        // Create default modules (these would come from API)
        modulesInOrder.add(new Module(1, "Reading Comprehension", "Master the art of understanding texts", "book", 1, "blue", 12));
        modulesInOrder.add(new Module(2, "Phonics", "Learn letter sounds and patterns", "sound", 2, "orange", 10));
        modulesInOrder.add(new Module(3, "Vocabulary", "Build your word knowledge", "lightbulb", 3, "purple", 15));
        modulesInOrder.add(new Module(4, "Spelling", "Perfect your spelling skills", "pencil", 4, "green", 10));
        modulesInOrder.add(new Module(5, "Fluency", "Read smoothly and naturally", "rocket", 5, "pink", 8));

        // Mark first module as priority and unlocked
        if (!modulesInOrder.isEmpty()) {
            modulesInOrder.get(0).setPriority(true);
            modulesInOrder.get(0).setLocked(false);

            // Save priority module to session (for API integration later)
            // sessionManager.savePriorityModule(modulesInOrder.get(0).getModuleId());
        }
    }

    private void displayModules() {
        moduleListContainer.removeAllViews();

        for (int i = 0; i < modulesInOrder.size(); i++) {
            Module module = modulesInOrder.get(i);
            View moduleItem = createModuleItem(module, i + 1);
            moduleListContainer.addView(moduleItem);
        }
    }

    private View createModuleItem(Module module, int order) {
        View itemView = LayoutInflater.from(this).inflate(
                R.layout.item_assessment_module, moduleListContainer, false);

        TextView tvModuleOrder = itemView.findViewById(R.id.tvModuleOrder);
        TextView tvModuleName = itemView.findViewById(R.id.tvModuleName);
        TextView tvModuleStatus = itemView.findViewById(R.id.tvModuleStatus);
        TextView tvPriorityBadge = itemView.findViewById(R.id.tvPriorityBadge);

        tvModuleOrder.setText(String.valueOf(order));
        tvModuleName.setText(module.getModuleName());

        if (module.isPriority()) {
            tvModuleStatus.setText("Start here!");
            tvModuleStatus.setTextColor(getResources().getColor(R.color.color_success, null));
            tvPriorityBadge.setVisibility(View.VISIBLE);
        } else if (order == 2) {
            tvModuleStatus.setText("Next up");
            tvModuleStatus.setTextColor(getResources().getColor(R.color.primary_blue, null));
        } else {
            tvModuleStatus.setText("Coming soon");
            tvModuleStatus.setTextColor(getResources().getColor(R.color.gray_medium, null));
        }

        // Set color for order badge based on module gradient
        int backgroundColor;
        switch (module.getColorGradient()) {
            case "blue":
                backgroundColor = getResources().getColor(R.color.primary_blue, null);
                break;
            case "orange":
                backgroundColor = getResources().getColor(R.color.color_sunglow, null);
                break;
            case "purple":
                backgroundColor = getResources().getColor(R.color.color_info_bg, null);
                break;
            case "green":
                backgroundColor = getResources().getColor(R.color.color_success, null);
                break;
            case "pink":
                backgroundColor = getResources().getColor(R.color.color_warning, null);
                break;
            default:
                backgroundColor = getResources().getColor(R.color.primary_blue, null);
        }
        // Note: Can't directly set circular background color, would need to create drawable variants

        return itemView;
    }

    private void setupListeners() {
        btnStartLearning.setOnClickListener(v -> {
            // Button press animation
            v.animate()
                    .scaleX(0.95f)
                    .scaleY(0.95f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                        navigateToDashboard();
                    })
                    .start();
        });
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(AssessmentResultsActivity.this, DashboardActivity.class);
        // Clear back stack so user can't go back to assessment
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to assessment - go to dashboard instead
        navigateToDashboard();
    }
}
