package com.example.literise.models;

public class ModuleLadderStep {
    private int stepId;
    private int moduleId;
    private int stepNumber;
    private String stepName;
    private String activityType; // "word_hunt", "sentence_scramble", "picture_match", "fill_blanks", etc.
    private String description;
    private boolean isLocked;
    private boolean isCompleted;
    private int starsEarned; // 0-3 stars
    private int xpReward;
    private int bestScore; // Highest score achieved (0-100)
    private String status; // "locked", "unlocked", "in_progress", "completed"

    public ModuleLadderStep() {
    }

    public ModuleLadderStep(int stepId, int moduleId, int stepNumber, String stepName,
                            String activityType, String description, int xpReward) {
        this.stepId = stepId;
        this.moduleId = moduleId;
        this.stepNumber = stepNumber;
        this.stepName = stepName;
        this.activityType = activityType;
        this.description = description;
        this.xpReward = xpReward;
        this.isLocked = true;
        this.isCompleted = false;
        this.starsEarned = 0;
        this.bestScore = 0;
        this.status = "locked";
    }

    // Getters and setters
    public int getStepId() {
        return stepId;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public int getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
        updateStatus();
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
        updateStatus();
    }

    public int getStarsEarned() {
        return starsEarned;
    }

    public void setStarsEarned(int starsEarned) {
        this.starsEarned = Math.max(0, Math.min(3, starsEarned)); // Clamp between 0-3
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public int getBestScore() {
        return bestScore;
    }

    public void setBestScore(int bestScore) {
        this.bestScore = Math.max(0, Math.min(100, bestScore)); // Clamp between 0-100
        updateStarsFromScore();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private void updateStatus() {
        if (isLocked) {
            status = "locked";
        } else if (isCompleted) {
            status = "completed";
        } else {
            status = "unlocked";
        }
    }

    private void updateStarsFromScore() {
        if (bestScore >= 90) {
            starsEarned = 3;
        } else if (bestScore >= 70) {
            starsEarned = 2;
        } else if (bestScore >= 60) {
            starsEarned = 1;
        } else {
            starsEarned = 0;
        }
    }

    public String getButtonText() {
        if (isLocked) {
            return "🔒 LOCKED";
        } else if (isCompleted) {
            return "✓ COMPLETED";
        } else if (bestScore > 0) {
            return "RETRY";
        } else {
            return "START";
        }
    }

    public String getActivityIcon() {
        switch (activityType) {
            case "word_hunt":
                return "🔍";
            case "sentence_scramble":
                return "🔤";
            case "picture_match":
                return "🖼️";
            case "fill_blanks":
                return "✏️";
            case "story_sequencing":
                return "📖";
            case "synonym_antonym":
                return "🔄";
            case "dialogue_reading":
                return "💬";
            case "module_test":
                return "📝";
            default:
                return "📚";
        }
    }
}
