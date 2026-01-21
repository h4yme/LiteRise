package com.example.literise.models;

/**
 * AdaptiveDecision Model - Represents performance-based adaptive decisions
 * Uses quiz scores, attempt counts, and placement level (NO theta)
 * Determines if supplemental/intervention/enrichment nodes should appear
 */
public class AdaptiveDecision {
    private int decisionId;
    private int studentId;
    private int nodeId;

    private DecisionType decisionType;
    private String reason;

    // Context (Simple Performance Metrics - No Theta)
    private int quizScore;
    private int attemptCount;
    private int placementLevel; // 1=Beginner, 2=Intermediate, 3=Advanced
    private ScoreTrend recentScoresTrend;

    private String timestamp;

    public enum DecisionType {
        PROCEED,              // Continue to next lesson normally
        ADD_SUPPLEMENTAL,     // Add support node (borderline pass)
        ADD_INTERVENTION,     // Add intensive help (failed quiz)
        OFFER_ENRICHMENT,     // Add optional challenge (mastery shown)
        RETRY_REQUIRED        // Must retry quiz
    }

    public enum ScoreTrend {
        IMPROVING,   // Recent scores going up
        DECLINING,   // Recent scores going down
        STABLE       // Recent scores consistent
    }

    // Constructors
    public AdaptiveDecision() {}

    public AdaptiveDecision(DecisionType decisionType, String reason) {
        this.decisionType = decisionType;
        this.reason = reason;
    }

    public AdaptiveDecision(DecisionType decisionType, String reason,
                            int quizScore, int attemptCount,
                            int placementLevel, ScoreTrend trend) {
        this.decisionType = decisionType;
        this.reason = reason;
        this.quizScore = quizScore;
        this.attemptCount = attemptCount;
        this.placementLevel = placementLevel;
        this.recentScoresTrend = trend;
    }

    // Utility Methods

    public boolean needsSupport() {
        return decisionType == DecisionType.ADD_SUPPLEMENTAL ||
                decisionType == DecisionType.ADD_INTERVENTION;
    }

    public boolean canProceed() {
        return decisionType == DecisionType.PROCEED ||
                decisionType == DecisionType.OFFER_ENRICHMENT;
    }

    public String getDecisionSummary() {
        return String.format("%s: %s (Score: %d%%, Attempt: %d)",
                decisionType.name(), reason, quizScore, attemptCount);
    }

    public String getPlacementLevelName() {
        switch (placementLevel) {
            case 1: return "Beginner";
            case 2: return "Intermediate";
            case 3: return "Advanced";
            default: return "Unknown";
        }
    }

    // Getters and Setters

    public int getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(int decisionId) {
        this.decisionId = decisionId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getQuizScore() {
        return quizScore;
    }

    public void setQuizScore(int quizScore) {
        this.quizScore = quizScore;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(int attemptCount) {
        this.attemptCount = attemptCount;
    }

    public int getPlacementLevel() {
        return placementLevel;
    }

    public void setPlacementLevel(int placementLevel) {
        this.placementLevel = placementLevel;
    }

    public ScoreTrend getRecentScoresTrend() {
        return recentScoresTrend;
    }

    public void setRecentScoresTrend(ScoreTrend recentScoresTrend) {
        this.recentScoresTrend = recentScoresTrend;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}