package com.example.literise.models;

/**
 * PacingStrategy Model - Determines how lesson content is delivered
 * Adapts delivery speed, scaffolding, and examples based on placement + performance
 * Content remains the same - only delivery changes
 */
public class PacingStrategy {
    private String speed;          // SLOW, MODERATE, MODERATE_FAST, FAST
    private String scaffolding;    // MINIMAL, LOW, BALANCED, HIGH
    private String examples;       // FEW, ADEQUATE, MANY
    private int durationMinutes;
    private boolean allowReview;

    // Constructors
    public PacingStrategy() {}

    public PacingStrategy(String speed, String scaffolding, String examples,
                          int durationMinutes, boolean allowReview) {
        this.speed = speed;
        this.scaffolding = scaffolding;
        this.examples = examples;
        this.durationMinutes = durationMinutes;
        this.allowReview = allowReview;
    }

    // Utility Methods

    public boolean isSlowPace() {
        return "SLOW".equals(speed);
    }

    public boolean isFastPace() {
        return "FAST".equals(speed);
    }

    public boolean needsHighSupport() {
        return "HIGH".equals(scaffolding);
    }

    public String getStrategyDescription() {
        return String.format("%s pace, %s scaffolding, %s examples (%d min)",
                speed, scaffolding.toLowerCase(), examples.toLowerCase(), durationMinutes);
    }

    /**
     * Get beginner strategy (placement level 1)
     */
    public static PacingStrategy getBeginnerStrategy() {
        return new PacingStrategy(
                "SLOW",
                "HIGH",
                "MANY",
                15,
                true
        );
    }

    /**
     * Get intermediate strategy (placement level 2)
     */
    public static PacingStrategy getIntermediateStrategy() {
        return new PacingStrategy(
                "MODERATE",
                "BALANCED",
                "ADEQUATE",
                10,
                true
        );
    }

    /**
     * Get advanced strategy (placement level 3)
     */
    public static PacingStrategy getAdvancedStrategy() {
        return new PacingStrategy(
                "FAST",
                "MINIMAL",
                "FEW",
                7,
                false
        );
    }

    // Getters and Setters

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getScaffolding() {
        return scaffolding;
    }

    public void setScaffolding(String scaffolding) {
        this.scaffolding = scaffolding;
    }

    public String getExamples() {
        return examples;
    }

    public void setExamples(String examples) {
        this.examples = examples;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isAllowReview() {
        return allowReview;
    }

    public void setAllowReview(boolean allowReview) {
        this.allowReview = allowReview;
    }
}