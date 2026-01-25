package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class NodeProgressResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("progress")
    private Progress progress;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public Progress getProgress() { return progress; }
    public String getMessage() { return message; }

    public static class Progress {
        @SerializedName("lesson_completed")
        private boolean lessonCompleted;

        @SerializedName("game_completed")
        private boolean gameCompleted;

        @SerializedName("quiz_completed")
        private boolean quizCompleted;

        @SerializedName("quiz_score")
        private double quizScore;

        @SerializedName("adaptive_decision")
        private String adaptiveDecision;

        @SerializedName("completed_at")
        private String completedAt;

        public boolean isLessonCompleted() { return lessonCompleted; }
        public boolean isGameCompleted() { return gameCompleted; }
        public boolean isQuizCompleted() { return quizCompleted; }
        public double getQuizScore() { return quizScore; }
        public String getAdaptiveDecision() { return adaptiveDecision; }
        public String getCompletedAt() { return completedAt; }
    }
}
