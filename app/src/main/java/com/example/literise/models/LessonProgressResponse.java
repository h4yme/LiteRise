package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Response model for lesson progress API
 */
public class LessonProgressResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("student")
    private StudentInfo student;

    @SerializedName("lessons")
    private List<LessonProgress> lessons;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public StudentInfo getStudent() {
        return student;
    }

    public List<LessonProgress> getLessons() {
        return lessons;
    }

    public static class StudentInfo {
        @SerializedName("total_xp")
        private int totalXP;

        @SerializedName("current_streak")
        private int currentStreak;

        @SerializedName("longest_streak")
        private int longestStreak;

        @SerializedName("grade_level")
        private int gradeLevel;

        public int getTotalXP() {
            return totalXP;
        }

        public int getCurrentStreak() {
            return currentStreak;
        }

        public int getLongestStreak() {
            return longestStreak;
        }

        public int getGradeLevel() {
            return gradeLevel;
        }
    }

    public static class LessonProgress {
        @SerializedName("lesson_id")
        private int lessonId;

        @SerializedName("lesson_title")
        private String lessonTitle;

        @SerializedName("lesson_type")
        private String lessonType;

        @SerializedName("completion_status")
        private String completionStatus;

        @SerializedName("score")
        private float score;

        @SerializedName("games_played")
        private int gamesPlayed;

        @SerializedName("progress_percent")
        private int progressPercent;

        @SerializedName("last_attempt")
        private String lastAttempt;

        public int getLessonId() {
            return lessonId;
        }

        public String getLessonTitle() {
            return lessonTitle;
        }

        public String getLessonType() {
            return lessonType;
        }

        public String getCompletionStatus() {
            return completionStatus;
        }

        public float getScore() {
            return score;
        }

        public int getGamesPlayed() {
            return gamesPlayed;
        }

        public int getProgressPercent() {
            return progressPercent;
        }

        public String getLastAttempt() {
            return lastAttempt;
        }

        public boolean isCompleted() {
            return "Completed".equals(completionStatus);
        }

        public boolean isInProgress() {
            return "InProgress".equals(completionStatus);
        }

        public boolean isNotStarted() {
            return completionStatus == null || "NotStarted".equals(completionStatus);
        }
    }
}
