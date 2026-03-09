package com.example.literise.models;

/**
 * Represents a Key Stage 1 learning module
 * Modules are ordered based on student's placement test performance
 */
public class LearningModule {
    private int moduleId;
    private String title;
    private String subtitle;
    private String domain; // Key Stage 1 domain
    private int level; // Student's current level in this module
    private int totalLevels; // Total levels available
    private boolean isLocked;
    private double performanceScore; // 0-1, from placement test (lower = needs more focus)
    private int priorityOrder; // Display order (1 = highest priority)
    private String gradientStart; // Gradient color start
    private String gradientEnd; // Gradient color end
    private String iconResName; // Icon resource name
    private int totalLessons; // Total lessons in this module
    private int completedLessons; // Lessons completed by the student

    public LearningModule(int moduleId, String title, String subtitle, String domain,
                          double performanceScore, String gradientStart, String gradientEnd) {
        this.moduleId = moduleId;
        this.title = title;
        this.subtitle = subtitle;
        this.domain = domain;
        this.level = 1;
        this.totalLevels = 5;
        this.isLocked = false;
        this.performanceScore = performanceScore;
        this.gradientStart = gradientStart;
        this.gradientEnd = gradientEnd;
        this.completedLessons = 0;
        this.totalLessons = 10; // default, overridden per module
    }

    // Getters
    public int getModuleId() { return moduleId; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getDomain() { return domain; }
    public int getLevel() { return level; }
    public int getTotalLevels() { return totalLevels; }
    public boolean isLocked() { return isLocked; }
    public double getPerformanceScore() { return performanceScore; }
    public int getPriorityOrder() { return priorityOrder; }
    public String getGradientStart() { return gradientStart; }
    public String getGradientEnd() { return gradientEnd; }
    public String getIconResName() { return iconResName; }

    // Getters (continued)
    public int getTotalLessons() { return totalLessons; }
    public int getCompletedLessons() { return completedLessons; }

    // Setters
    public void setLevel(int level) { this.level = level; }
    public void setLocked(boolean locked) { isLocked = locked; }
    public void setPriorityOrder(int order) { this.priorityOrder = order; }
    public void setIconResName(String iconResName) { this.iconResName = iconResName; }
    public void setTotalLessons(int totalLessons) { this.totalLessons = totalLessons; }
    public void setCompletedLessons(int completedLessons) { this.completedLessons = completedLessons; }

    /**
     * Get lesson progress percentage for this module (0–100)
     */
    public int getProgressPercentage() {
        if (totalLessons <= 0) return 0;
        return (int) ((completedLessons / (double) totalLessons) * 100);
    }

    /**
     * Get performance category
     */
    public String getPerformanceCategory() {
        if (performanceScore < 0.50) return "Needs Improvement";
        if (performanceScore < 0.70) return "Developing";
        if (performanceScore < 0.85) return "Good";
        return "Excellent";
    }

    /**
     * Get display subtitle with level info
     */
    public String getDisplaySubtitle() {
        if (isLocked) {
            return "Locked - Complete prerequisites";
        }
        return subtitle + " • Level " + level;
    }
}