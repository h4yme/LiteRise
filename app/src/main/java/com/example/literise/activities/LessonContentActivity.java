package com.example.literise.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.literise.R;
import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.LessonContentResponse;
import com.example.literise.models.UpdateProgressRequest;
import com.example.literise.models.UpdateProgressResponse;
import com.google.android.material.button.MaterialButton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * PHASE 1: Lesson Content Display Activity
 *
 * Displays lesson content with adaptive pacing based on placement level:
 * - BEGINNER (Level 1): SLOW pacing, HIGH scaffolding
 * - INTERMEDIATE (Level 2): MODERATE pacing, BALANCED scaffolding
 * - ADVANCED (Level 3): FAST pacing, MINIMAL scaffolding
 */
public class LessonContentActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView tvLessonTitle;
    private TextView tvLessonNumber;
    private TextView tvLessonContent;
    private TextView tvScaffolding;
    private ProgressBar progressBar;
    private MaterialButton btnComplete;

    private int nodeId;
    private int lessonNumber;
    private int placementLevel;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_content);

        sessionManager = new SessionManager(this);

        nodeId = getIntent().getIntExtra("node_id", 1);
        lessonNumber = getIntent().getIntExtra("lesson_number", 1);
        placementLevel = convertPlacementLevelToInt(sessionManager.getPlacementLevel());

        initializeViews();
        setupListeners();
        loadLessonContent();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        tvLessonTitle = findViewById(R.id.tvLessonTitle);
        tvLessonNumber = findViewById(R.id.tvLessonNumber);
        tvLessonContent = findViewById(R.id.tvLessonContent);
        tvScaffolding = findViewById(R.id.tvScaffolding);
        progressBar = findViewById(R.id.progressBar);
        btnComplete = findViewById(R.id.btnComplete);

        tvLessonNumber.setText("📖 Lesson " + lessonNumber);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnComplete.setOnClickListener(v -> markLessonCompleted());
    }

    private void loadLessonContent() {
        progressBar.setVisibility(View.VISIBLE);
        btnComplete.setEnabled(false);

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.getLessonContent(nodeId, placementLevel).enqueue(new Callback<LessonContentResponse>() {
            @Override
            public void onResponse(Call<LessonContentResponse> call, Response<LessonContentResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    displayLessonContent(response.body());
                    btnComplete.setEnabled(true);
                } else {
                    Toast.makeText(LessonContentActivity.this, "Failed to load lesson", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<LessonContentResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LessonContentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void displayLessonContent(LessonContentResponse data) {
        tvLessonTitle.setText(data.getLesson().getTitle());
        tvLessonContent.setText(data.getLesson().getContent());
        tvScaffolding.setText(data.getPacing().getDescription());
    }

    private void markLessonCompleted() {
        progressBar.setVisibility(View.VISIBLE);
        btnComplete.setEnabled(false);

        int studentId = sessionManager.getStudentId();
        UpdateProgressRequest request = new UpdateProgressRequest(studentId, nodeId, "lesson");

        ApiService apiService = ApiClient.getClient(this).create(ApiService.class);
        apiService.updateNodeProgress(request).enqueue(new Callback<UpdateProgressResponse>() {
            @Override
            public void onResponse(Call<UpdateProgressResponse> call, Response<UpdateProgressResponse> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(LessonContentActivity.this, "✅ Lesson Complete! Moving to Game...", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(LessonContentActivity.this, "Failed to save progress", Toast.LENGTH_SHORT).show();
                    btnComplete.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<UpdateProgressResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(LessonContentActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                btnComplete.setEnabled(true);
            }
        });
    }

    /**
     * Convert placement level string to integer for API calls
     */
    private int convertPlacementLevelToInt(String levelString) {
        if (levelString == null) return 2; // Default to intermediate

        if (levelString.contains("2") || levelString.toLowerCase().contains("beginner")) {
            return 1;
        } else if (levelString.contains("4") || levelString.toLowerCase().contains("advanced")) {
            return 3;
        } else {
            return 2; // Grade 3 or intermediate
        }
    }
}