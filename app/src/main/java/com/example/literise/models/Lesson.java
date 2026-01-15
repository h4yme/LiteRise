package com.example.literise.models;

import java.util.List;

/**
 * Represents a single lesson within a module
 * Each lesson has content, practice exercises, and a quiz
 */
public class Lesson {
    // Game types
    public static final String GAME_SENTENCE_SCRAMBLE = "sentence_scramble";
    public static final String GAME_TIMED_TRAIL = "timed_trail";
    public static final String GAME_WORD_HUNT = "word_hunt";
    public static final String GAME_SHADOW_READ = "shadow_read";
    public static final String GAME_MINIMAL_PAIRS = "minimal_pairs";
    public static final String GAME_TRADITIONAL = "traditional";

    private int lessonId;
    private int moduleId;
    private int lessonNumber; // 1-15
    private String title;
    private String tier; // "Foundation", "Intermediate", "Advanced"
    private String description;
    private String gameType; // Type of game for this lesson
    private String content; // Main teaching content (can be HTML formatted)
    private List<String> learningObjectives;
    private List<Question> practiceQuestions; // 10 practice questions
    private List<Question> quizQuestions; // 5-10 quiz questions
    private int xpReward; // XP earned for completing
    private int estimatedMinutes; // Time to complete

    // Progress tracking
    private boolean isUnlocked;
    private boolean isCompleted;
    private int practiceScore; // 0-100
    private int quizScore; // 0-100
    private int attempts;
    private long completedTimestamp;

    public Lesson(int lessonId, int moduleId, int lessonNumber, String title,
                  String tier, String description) {
        this.lessonId = lessonId;
        this.moduleId = moduleId;
        this.lessonNumber = lessonNumber;
        this.title = title;
        this.tier = tier;
        this.description = description;
        this.gameType = GAME_TRADITIONAL; // Default
        this.xpReward = 10; // Default
        this.isUnlocked = false;
        this.isCompleted = false;
        this.attempts = 0;
    }

    // Getters
    public int getLessonId() { return lessonId; }
    public int getModuleId() { return moduleId; }
    public int getLessonNumber() { return lessonNumber; }
    public String getTitle() { return title; }
    public String getTier() { return tier; }
    public String getDescription() { return description; }
    public String getGameType() { return gameType; }
    public String getContent() { return content; }
    public List<String> getLearningObjectives() { return learningObjectives; }
    public List<Question> getPracticeQuestions() { return practiceQuestions; }
    public List<Question> getQuizQuestions() { return quizQuestions; }
    public int getXpReward() { return xpReward; }
    public int getEstimatedMinutes() { return estimatedMinutes; }
    public boolean isUnlocked() { return isUnlocked; }
    public boolean isCompleted() { return isCompleted; }
    public int getPracticeScore() { return practiceScore; }
    public int getQuizScore() { return quizScore; }
    public int getAttempts() { return attempts; }
    public long getCompletedTimestamp() { return completedTimestamp; }

    // Setters
    public void setGameType(String gameType) { this.gameType = gameType; }
    public void setContent(String content) { this.content = content; }
    public void setLearningObjectives(List<String> objectives) { this.learningObjectives = objectives; }
    public void setPracticeQuestions(List<Question> questions) { this.practiceQuestions = questions; }
    public void setQuizQuestions(List<Question> questions) { this.quizQuestions = questions; }
    public void setXpReward(int xp) { this.xpReward = xp; }
    public void setEstimatedMinutes(int minutes) { this.estimatedMinutes = minutes; }
    public void setUnlocked(boolean unlocked) { this.isUnlocked = unlocked; }
    public void setCompleted(boolean completed) { this.isCompleted = completed; }
    public void setPracticeScore(int score) { this.practiceScore = score; }
    public void setQuizScore(int score) { this.quizScore = score; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public void setCompletedTimestamp(long timestamp) { this.completedTimestamp = timestamp; }

    /**
     * Check if lesson is passed (quiz score >= 70%)
     */
    public boolean isPassed() {
        return quizScore >= 70;
    }

    /**
     * Get display status text
     */
    public String getStatusText() {
        if (!isUnlocked) return "Locked";
        if (isCompleted && isPassed()) return "Completed";
        if (isCompleted) return "Needs Review";
        if (attempts > 0) return "In Progress";
        return "Start";
    }

    /**
     * Get tier color for UI
     */
    public String getTierColor() {
        switch (tier) {
            case "Foundation": return "#60A5FA"; // Blue
            case "Intermediate": return "#FBBF24"; // Yellow
            case "Advanced": return "#F472B6"; // Pink
            default: return "#9CA3AF"; // Gray
        }
    }
}
