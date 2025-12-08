package com.example.literise.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.literise.R;
import com.google.android.material.button.MaterialButton;

public class ModuleLadderActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvModuleTitle, tvModuleSubtitle;
    private RelativeLayout lessonNodesContainer;
    private MaterialButton btnStart;

    private String moduleName;
    private int totalLessons = 10; // Total lessons per module
    private int currentLesson = 1; // Current unlocked lesson

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_ladder);

        // Get module name from intent
        moduleName = getIntent().getStringExtra("module_name");
        if (moduleName == null) {
            moduleName = "Reading Comprehension";
        }

        initializeViews();
        setupListeners();
        displayLessonNodes();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvModuleTitle = findViewById(R.id.tvModuleTitle);
        tvModuleSubtitle = findViewById(R.id.tvModuleSubtitle);
        lessonNodesContainer = findViewById(R.id.lessonNodesContainer);
        btnStart = findViewById(R.id.btnStart);

        // Set module title
        tvModuleTitle.setText(moduleName);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnStart.setOnClickListener(v -> {
            // TODO: Start first unlocked lesson
            android.widget.Toast.makeText(this,
                "Starting Lesson " + currentLesson,
                android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private void displayLessonNodes() {
        lessonNodesContainer.removeAllViews();

        // Convert dp to pixels for positioning
        float density = getResources().getDisplayMetrics().density;
        int horizontalOffset = (int) (70 * density); // Offset from center for zigzag

        // Get screen width to calculate center position
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int nodeWidth = (int) (80 * density); // Node is 80dp wide
        int centerPosition = (screenWidth - nodeWidth) / 2;

        View previousNode = null;

        for (int i = 1; i <= totalLessons; i++) {
            View nodeView = LayoutInflater.from(this).inflate(
                    R.layout.item_lesson_node,
                    lessonNodesContainer,
                    false
            );

            // Set a unique ID for each node
            nodeView.setId(View.generateViewId());

            ImageView ivNodeBackground = nodeView.findViewById(R.id.ivNodeBackground);
            ImageView ivNodeIcon = nodeView.findViewById(R.id.ivNodeIcon);
            TextView tvLessonNumber = nodeView.findViewById(R.id.tvLessonNumber);

            tvLessonNumber.setText(String.valueOf(i));

            if (i < currentLesson) {
                // Completed lesson - gold
                ivNodeBackground.setImageResource(R.drawable.bg_lesson_node_completed);
                ivNodeIcon.setImageResource(R.drawable.ic_star);
                ivNodeIcon.setColorFilter(0xFFFFFFFF); // White star
            } else if (i == currentLesson) {
                // Current unlocked lesson - white
                ivNodeBackground.setImageResource(R.drawable.bg_lesson_node_unlocked);
                ivNodeIcon.setImageResource(R.drawable.ic_play);
                ivNodeIcon.setColorFilter(0xFF7C3AED); // Purple play icon
            } else {
                // Locked lesson - translucent white
                ivNodeBackground.setImageResource(R.drawable.bg_lesson_node_locked);
                ivNodeIcon.setImageResource(R.drawable.ic_lock);
                ivNodeIcon.setColorFilter(0xFF9D68F5); // Light purple lock
            }

            // Create zigzag pattern with absolute positioning
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );

            // Position below previous node
            if (previousNode != null) {
                params.addRule(RelativeLayout.BELOW, previousNode.getId());
                params.topMargin = 0; // No extra margin, spacing is in item layout
            }

            // Zigzag pattern - calculate horizontal position
            int position = (i - 1) % 4; // Pattern repeats every 4 nodes
            int leftMargin;

            switch (position) {
                case 0: // Center
                    leftMargin = centerPosition;
                    break;
                case 1: // Right
                    leftMargin = centerPosition + horizontalOffset;
                    break;
                case 2: // Center
                    leftMargin = centerPosition;
                    break;
                case 3: // Left
                    leftMargin = centerPosition - horizontalOffset;
                    break;
                default:
                    leftMargin = centerPosition;
                    break;
            }

            params.leftMargin = leftMargin;

            nodeView.setLayoutParams(params);

            final int lessonNumber = i;
            nodeView.setOnClickListener(v -> {
                if (lessonNumber <= currentLesson) {
                    // Can play this lesson
                    android.widget.Toast.makeText(this,
                        "Opening Lesson " + lessonNumber,
                        android.widget.Toast.LENGTH_SHORT).show();
                    // TODO: Navigate to lesson activity
                } else {
                    // Locked lesson
                    android.widget.Toast.makeText(this,
                        "Complete previous lessons to unlock",
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            });

            lessonNodesContainer.addView(nodeView);
            previousNode = nodeView;
        }
    }
}
