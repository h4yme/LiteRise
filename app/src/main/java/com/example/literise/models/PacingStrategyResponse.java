package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class PacingStrategyResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("speed")
    private String speed; // SLOW, MODERATE, FAST

    @SerializedName("scaffolding")
    private String scaffolding; // MINIMAL, LOW, BALANCED, HIGH

    @SerializedName("examples")
    private String examples; // FEW, ADEQUATE, MANY

    @SerializedName("duration_minutes")
    private int durationMinutes;

    @SerializedName("allow_review")
    private boolean allowReview;

    @SerializedName("game_difficulty")
    private String gameDifficulty; // EASY, MEDIUM, HARD

    // Getters
    public boolean isSuccess() { return success; }
    public String getSpeed() { return speed; }
    public String getScaffolding() { return scaffolding; }
    public String getExamples() { return examples; }
    public int getDurationMinutes() { return durationMinutes; }
    public boolean isAllowReview() { return allowReview; }
    public String getGameDifficulty() { return gameDifficulty; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setSpeed(String speed) { this.speed = speed; }
    public void setScaffolding(String scaffolding) { this.scaffolding = scaffolding; }
    public void setExamples(String examples) { this.examples = examples; }
    public void setDurationMinutes(int durationMinutes) { this.durationMinutes = durationMinutes; }
    public void setAllowReview(boolean allowReview) { this.allowReview = allowReview; }
    public void setGameDifficulty(String gameDifficulty) { this.gameDifficulty = gameDifficulty; }
}
