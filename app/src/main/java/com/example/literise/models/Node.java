package com.example.literise.models;

/**
 * Node Model - Represents a curriculum node in the adaptive module system
 * Nodes are curriculum-locked: 12 core lessons + 1 final assessment per module
 * Flow: LESSON → GAME → QUIZ (3 phases per core lesson node)
 */
public class Node {
    private int nodeId;
    private int moduleId;
    private NodeType nodeType;
    private int nodeNumber; // 1-13
    private Integer quarter; // 1-4 (null for final assessment)

    // Curriculum Content (Fixed)
    private String lessonTitle;
    private String learningObjectives;
    private String contentJSON;
    private String skillCategory;
    private int estimatedDuration; // Minutes
    private int xpReward;

    // Student-Specific State
    private NodeState state; // Current state for this student

    // Performance Tracking (No Theta - Performance-Based)
    private int attemptCount;
    private int bestQuizScore;
    private int latestQuizScore;
    private float averageQuizScore;

    // Phase Completion (LESSON → GAME → QUIZ)
    private boolean lessonCompleted;
    private boolean gameCompleted;
    private boolean quizCompleted;

    // Timestamps
    private String unlockedDate;
    private String completedDate;
    private String lastAttemptDate;

    public enum NodeType {
        CORE_LESSON,        // Standard curriculum lesson (nodes 1-12)
        FINAL_ASSESSMENT    // Module exit exam (node 13)
    }

    public enum NodeState {
        LOCKED,         // Not yet available
        UNLOCKED,       // Available to attempt
        IN_PROGRESS,    // Started but not completed
        COMPLETED,      // Passed (quiz >= 70%)
        MASTERED        // Exceptional (quiz >= 90%)
    }

    // Constructors
    public Node() {}

    public Node(int nodeId, int moduleId, NodeType nodeType, int nodeNumber,
                Integer quarter, String lessonTitle) {
        this.nodeId = nodeId;
        this.moduleId = moduleId;
        this.nodeType = nodeType;
        this.nodeNumber = nodeNumber;
        this.quarter = quarter;
        this.lessonTitle = lessonTitle;
        this.state = NodeState.LOCKED;
    }

    // Utility Methods

    public boolean isAvailable() {
        return state == NodeState.UNLOCKED || state == NodeState.IN_PROGRESS;
    }

    public boolean isPassed() {
        return state == NodeState.COMPLETED || state == NodeState.MASTERED;
    }

    public boolean isFinalAssessment() {
        return nodeType == NodeType.FINAL_ASSESSMENT;
    }

    public int getProgressPercentage() {
        int total = 3; // LESSON + GAME + QUIZ
        int completed = 0;
        if (lessonCompleted) completed++;
        if (gameCompleted) completed++;
        if (quizCompleted) completed++;
        return (completed * 100) / total;
    }

    public boolean isReadyForQuiz() {
        return lessonCompleted && gameCompleted && !quizCompleted;
    }

    public String getPhaseStatus() {
        if (!lessonCompleted) return "LESSON";
        if (!gameCompleted) return "GAME";
        if (!quizCompleted) return "QUIZ";
        return "COMPLETED";
    }

    // Getters and Setters

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public void setNodeNumber(int nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    public Integer getQuarter() {
        return quarter;
    }

    public void setQuarter(Integer quarter) {
        this.quarter = quarter;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public void setLessonTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public String getLearningObjectives() {
        return learningObjectives;
    }

    public void setLearningObjectives(String learningObjectives) {
        this.learningObjectives = learningObjectives;
    }

    public String getContentJSON() {
        return contentJSON;
    }

    public void setContentJSON(String contentJSON) {
        this.contentJSON = contentJSON;
    }

    public String getSkillCategory() {
        return skillCategory;
    }

    public void setSkillCategory(String skillCategory) {
        this.skillCategory = skillCategory;
    }

    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(int estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public NodeState getState() {
        return state;
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getBestQuizScore() {
        return bestQuizScore;
    }

    public void setBestQuizScore(int bestQuizScore) {
        this.bestQuizScore = bestQuizScore;
    }

    public int getLatestQuizScore() {
        return latestQuizScore;
    }

    public void setLatestQuizScore(int latestQuizScore) {
        this.latestQuizScore = latestQuizScore;
    }

    public float getAverageQuizScore() {
        return averageQuizScore;
    }

    public void setAverageQuizScore(float averageQuizScore) {
        this.averageQuizScore = averageQuizScore;
    }

    public boolean isLessonCompleted() {
        return lessonCompleted;
    }

    public void setLessonCompleted(boolean lessonCompleted) {
        this.lessonCompleted = lessonCompleted;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public void setGameCompleted(boolean gameCompleted) {
        this.gameCompleted = gameCompleted;
    }

    public boolean isQuizCompleted() {
        return quizCompleted;
    }

    public void setQuizCompleted(boolean quizCompleted) {
        this.quizCompleted = quizCompleted;
    }

    public String getUnlockedDate() {
        return unlockedDate;
    }

    public void setUnlockedDate(String unlockedDate) {
        this.unlockedDate = unlockedDate;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }

    public String getLastAttemptDate() {
        return lastAttemptDate;
    }

    public void setLastAttemptDate(String lastAttemptDate) {
        this.lastAttemptDate = lastAttemptDate;
    }
}