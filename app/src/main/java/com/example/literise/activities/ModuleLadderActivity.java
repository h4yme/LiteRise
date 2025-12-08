package com.example.literise.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.literise.R;
import com.google.android.material.button.MaterialButton;

public class ModuleLadderActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvModuleTitle, tvModuleSubtitle;
    private LinearLayout lessonNodesContainer;
    private ScrollView scrollView;
    private MaterialButton btnStart;

    private String moduleName;
    private int totalLessons = 10; // Total lessons per module
    private int currentLesson = 4; // Current unlocked lesson (set to 4 for testing all games)

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

        // Scroll to bottom to show lesson 1 first
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvModuleTitle = findViewById(R.id.tvModuleTitle);
        tvModuleSubtitle = findViewById(R.id.tvModuleSubtitle);
        lessonNodesContainer = findViewById(R.id.lessonNodesContainer);
        scrollView = findViewById(R.id.scrollView);
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
        int horizontalOffset = (int) (40 * density); // Reduced offset for narrower zigzag

        // Display nodes from lesson 10 to 1 (top to bottom)
        // So lesson 1 ends up at the bottom near START button
        for (int i = totalLessons; i >= 1; i--) {
            // Create a wrapper FrameLayout for horizontal positioning
            FrameLayout wrapper = new FrameLayout(this);
            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            wrapper.setLayoutParams(wrapperParams);

            // Inflate the node view
            View nodeView = LayoutInflater.from(this).inflate(
                    R.layout.item_lesson_node,
                    wrapper,
                    false
            );

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

            // Zigzag pattern - calculate horizontal position
            int position = (i - 1) % 4; // Pattern repeats every 4 nodes
            int leftMargin;

            switch (position) {
                case 0: // Center
                    leftMargin = 0; // Will be centered by gravity
                    break;
                case 1: // Right
                    leftMargin = horizontalOffset;
                    break;
                case 2: // Center
                    leftMargin = 0;
                    break;
                case 3: // Left
                    leftMargin = -horizontalOffset;
                    break;
                default:
                    leftMargin = 0;
                    break;
            }

            // Position node within wrapper using FrameLayout params
            FrameLayout.LayoutParams nodeParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            nodeParams.gravity = android.view.Gravity.CENTER_HORIZONTAL;
            nodeParams.leftMargin = leftMargin;
            nodeParams.rightMargin = -leftMargin; // Compensate
            nodeView.setLayoutParams(nodeParams);

            wrapper.addView(nodeView);

            final int lessonNumber = i;
            wrapper.setOnClickListener(v -> {
                if (lessonNumber <= currentLesson) {
                    // Can play this lesson
                    android.widget.Toast.makeText(this,
                        "Opening Lesson " + lessonNumber,
                        android.widget.Toast.LENGTH_SHORT).show();

                    // Alternate between games for testing
                    Intent intent;
                    if (lessonNumber == 1) {
                        // Lesson 1: Story Sequencing
                        intent = new Intent(this, StorySequencingActivity.class);
                    } else if (lessonNumber == 2) {
                        // Lesson 2: Fill in the Blanks
                        intent = new Intent(this, FillInTheBlanksActivity.class);
                    } else if (lessonNumber == 3) {
                        // Lesson 3: Picture Match
                        intent = new Intent(this, PictureMatchActivity.class);
                    } else if (lessonNumber == 4) {
                        // Lesson 4: Dialogue Reading
                        intent = new Intent(this, DialogueReadingActivity.class);
                    } else {
                        // Other lessons: Story Sequencing
                        intent = new Intent(this, StorySequencingActivity.class);
                    }
                    startActivity(intent);
                } else {
                    // Locked lesson
                    android.widget.Toast.makeText(this,
                        "Complete previous lessons to unlock",
                        android.widget.Toast.LENGTH_SHORT).show();
                }
            });

            lessonNodesContainer.addView(wrapper);
        }
    }
}
