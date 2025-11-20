package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class GameResult {

    @SerializedName("game_type")
    private String gameType; // "sentence_scramble", "timed_trail", etc.

    @SerializedName("score")
    private int score;

    @SerializedName("xp_earned")
    private int xpEarned;

    @SerializedName("accuracy")
    private float accuracy;

    @SerializedName("time_spent")
    private int timeSpent; // seconds

    @SerializedName("streak")
    private int streak;

    @SerializedName("is_perfect")
    private boolean isPerfect;

    public GameResult() {}

    public GameResult(String gameType, int score, int xpEarned, float accuracy, int timeSpent) {
        this.gameType = gameType;
        this.score = score;
        this.xpEarned = xpEarned;
        this.accuracy = accuracy;
        this.timeSpent = timeSpent;
        this.isPerfect = accuracy >= 100.0f;
    }

    // Getters and Setters
    public String getGameType() {
        return gameType;
    }

    public void setGameType(String gameType) {
        this.gameType = gameType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public int getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(int timeSpent) {
        this.timeSpent = timeSpent;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public boolean isPerfect() {
        return isPerfect;
    }

    public void setPerfect(boolean perfect) {
        isPerfect = perfect;
    }
}
