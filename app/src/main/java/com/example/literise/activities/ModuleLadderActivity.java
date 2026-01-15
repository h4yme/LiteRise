package com.example.literise.activities;



import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;

import android.widget.ImageView;

import android.widget.LinearLayout;

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


    private ScrollView scrollView;
    private ImageView btnBack;

    private TextView tvModuleTitle, tvModuleSubtitle;

    private LinearLayout lessonNodesContainer;

    private MaterialButton btnStart;



    private String moduleName;
    private int moduleId;

    private int totalLessons = 15; // Total lessons per module (15 for Module 1)

    private int currentLesson = 1; // Current unlocked lesson



    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_module_ladder);



        // Get module info from intent

        moduleName = getIntent().getStringExtra("module_name");
        moduleId = getIntent().getIntExtra("module_id", 1);

        if (moduleName == null) {

            moduleName = "Reading Comprehension";

        }



        initializeViews();

        setupListeners();

        displayLessonNodes();
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

    }



    private void initializeViews() {

        btnBack = findViewById(R.id.btnBack);

        tvModuleTitle = findViewById(R.id.tvModuleTitle);

        tvModuleSubtitle = findViewById(R.id.tvModuleSubtitle);

        lessonNodesContainer = findViewById(R.id.lessonNodesContainer);

        btnStart = findViewById(R.id.btnStart);


        scrollView = findViewById(R.id.scrollView);
        // Set module title

        tvModuleTitle.setText(moduleName);

    }



    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());



        btnStart.setOnClickListener(v -> {

            // Start first unlocked lesson
            openLesson(currentLesson);

        });

    }



    private void displayLessonNodes() {

        lessonNodesContainer.removeAllViews();


        // Convert dp to pixels for positioning

        float density = getResources().getDisplayMetrics().density;

        int horizontalOffset = (int) (40 * density); // Offset from center for zigzag



        // Display nodes from bottom to top (reversed order)

        // So lesson 1 (current) is at the bottom near START button

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
                    openLesson(lessonNumber);

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
    /**

     * Opens a lesson - routes to fun game activity based on game type!

     * Calculates lesson ID based on module ID and lesson number

     */

    private void openLesson(int lessonNumber) {
        // Calculate lesson ID: Module 1 = 101-115, Module 2 = 201-215, etc.
        int lessonId = (moduleId * 100) + lessonNumber;

        // Get the lesson to check its game type
        Intent intent = null;
        String gameType = getLessonGameType(lessonId);

        // Route to the appropriate fun game activity!
        switch (gameType) {
            case "sentence_scramble":
                intent = new Intent(this, com.example.literise.activities.games.SentenceScrambleActivity.class);
                break;

            case "word_hunt":
                intent = new Intent(this, com.example.literise.activities.games.WordHuntActivity.class);
                break;

            case "timed_trail":
                intent = new Intent(this, com.example.literise.activities.games.TimedTrailActivity.class);
                break;

            case "shadow_read":
                intent = new Intent(this, com.example.literise.activities.games.DialogueReadingActivity.class);
                break;

            case "minimal_pairs":
                intent = new Intent(this, com.example.literise.activities.games.MinimalPairsActivity.class);
                break;

            case "traditional":
            default:
                // Fall back to traditional lesson activity
                intent = new Intent(this, ModuleLessonActivity.class);
                break;
        }

        if (intent != null) {
            intent.putExtra("lesson_id", lessonId);
            intent.putExtra("module_id", moduleId);
            startActivity(intent);
        }
    }

    private String getLessonGameType(int lessonId) {
        // For Module 1, get game type from content provider
        if (moduleId == 1) {
            try {
                java.lang.reflect.Method method = com.example.literise.content.Module1ContentProvider.class
                    .getDeclaredMethod("getAllLessons");
                java.util.List<?> lessons = (java.util.List<?>) method.invoke(null);

                for (Object lessonObj : lessons) {
                    com.example.literise.models.Lesson lesson = (com.example.literise.models.Lesson) lessonObj;
                    if (lesson.getLessonId() == lessonId) {
                        return lesson.getGameType();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "traditional";
    }

}