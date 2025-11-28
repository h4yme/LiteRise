package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class ModuleLadderStep {

    @SerializedName("StepID")
    private int stepId;

    @SerializedName("ModuleID")
    private int moduleId;

    @SerializedName("StepNumber")
    private int stepNumber;

    @SerializedName("StepName")
    private String stepName;

    @SerializedName("StepType")
    private String stepType; // 'game', 'lesson', 'practice', 'test'

    @SerializedName("ActivityType")
    private String activityType; // 'word_hunt', 'scramble', 'timed_trail', etc.

    @SerializedName("DifficultyLevel")
    private int difficultyLevel; // 1-5

    @SerializedName("XPReward")
    private int xpReward;

    @SerializedName("RequiredAccuracy")
    private float requiredAccuracy;

    @SerializedName("IsActive")
    private boolean isActive;

    // Student progress fields
    @SerializedName("IsCompleted")
    private boolean isCompleted;

    @SerializedName("IsPassed")
    private boolean isPassed;

    @SerializedName("BestScore")
    private float bestScore;

    @SerializedName("BestAccuracy")
    private float bestAccuracy;

    @SerializedName("Attempts")
    private int attempts;

    @SerializedName("IsLocked")
    private boolean isLocked;

    // Getters and Setters
    public int getStepId() { return stepId; }
    public void setStepId(int stepId) { this.stepId = stepId; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public int getStepNumber() { return stepNumber; }
    public void setStepNumber(int stepNumber) { this.stepNumber = stepNumber; }

    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }

    public String getStepType() { return stepType; }
    public void setStepType(String stepType) { this.stepType = stepType; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }

    public int getXpReward() { return xpReward; }
    public void setXpReward(int xpReward) { this.xpReward = xpReward; }

    public float getRequiredAccuracy() { return requiredAccuracy; }
    public void setRequiredAccuracy(float requiredAccuracy) { this.requiredAccuracy = requiredAccuracy; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public boolean isPassed() { return isPassed; }
    public void setPassed(boolean passed) { isPassed = passed; }

    public float getBestScore() { return bestScore; }
    public void setBestScore(float bestScore) { this.bestScore = bestScore; }

    public float getBestAccuracy() { return bestAccuracy; }
    public void setBestAccuracy(float bestAccuracy) { this.bestAccuracy = bestAccuracy; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }
}
