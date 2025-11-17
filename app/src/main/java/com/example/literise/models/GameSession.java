package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class GameSession {

    @SerializedName("session_id")
    private int sessionId;

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("lesson_type")
    private String lessonType; // "reading", "vocabulary", "grammar"

    @SerializedName("games_completed")
    private int gamesCompleted;

    @SerializedName("total_games_required")
    private int totalGamesRequired;

    @SerializedName("total_xp_earned")
    private int totalXpEarned;

    @SerializedName("accuracy_percentage")
    private float accuracyPercentage;

    @SerializedName("is_completed")
    private boolean isCompleted;

    public GameSession() {
        this.gamesCompleted = 0;
        this.totalGamesRequired = 5; // 5 random games per lesson
        this.totalXpEarned = 0;
        this.accuracyPercentage = 0;
        this.isCompleted = false;
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getLessonType() {
        return lessonType;
    }

    public void setLessonType(String lessonType) {
        this.lessonType = lessonType;
    }

    public int getGamesCompleted() {
        return gamesCompleted;
    }

    public void setGamesCompleted(int gamesCompleted) {
        this.gamesCompleted = gamesCompleted;
    }

    public int getTotalGamesRequired() {
        return totalGamesRequired;
    }

    public void setTotalGamesRequired(int totalGamesRequired) {
        this.totalGamesRequired = totalGamesRequired;
    }

    public int getTotalXpEarned() {
        return totalXpEarned;
    }

    public void setTotalXpEarned(int totalXpEarned) {
        this.totalXpEarned = totalXpEarned;
    }

    public float getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(float accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public void incrementGamesCompleted() {
        this.gamesCompleted++;
        if (this.gamesCompleted >= this.totalGamesRequired) {
            this.isCompleted = true;
        }
    }

    public int getProgress() {
        return (gamesCompleted * 100) / totalGamesRequired;
    }
}