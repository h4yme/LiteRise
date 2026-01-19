package com.example.literise.models;

/**
 * SupplementalNode Model - Represents conditional adaptive support nodes
 * These appear/disappear based on student performance
 * Never replace core curriculum - only add support
 */
public class SupplementalNode {
    private int supplementalNodeId;
    private SupplementalType nodeType;
    private int afterNodeId; // Appears after which core lesson

    // Trigger Logic (Simple Score-Based Rules)
    private String triggerLogic; // e.g., "quiz_score < 70", "quiz_score >= 70 AND quiz_score < 80 AND placement_level = 1"

    // Content
    private String title;
    private String contentJSON;
    private String skillCategory;
    private int estimatedDuration; // Minutes
    private int xpReward;

    // Student-Specific State
    private boolean isVisible; // Conditionally visible for this student
    private boolean isCompleted;
    private String triggerReason; // Why this node appeared
    private String completedDate;

    public enum SupplementalType {
        SUPPLEMENTAL,   // Borderline pass support (70-79%)
        INTERVENTION,   // Failed quiz remediation (<70%)
        ENRICHMENT      // Advanced optional content (90%+)
    }

    // Constructors
    public SupplementalNode() {}

    public SupplementalNode(int supplementalNodeId, SupplementalType nodeType,
                           int afterNodeId, String title) {
        this.supplementalNodeId = supplementalNodeId;
        this.nodeType = nodeType;
        this.afterNodeId = afterNodeId;
        this.title = title;
        this.isVisible = false;
        this.isCompleted = false;
    }

    // Utility Methods

    public boolean isRequired() {
        // Intervention nodes are required, enrichment is optional
        return nodeType == SupplementalType.INTERVENTION;
    }

    public String getTypeDescription() {
        switch (nodeType) {
            case SUPPLEMENTAL:
                return "Additional Practice";
            case INTERVENTION:
                return "Remedial Support";
            case ENRICHMENT:
                return "Challenge Activity";
            default:
                return "Supplemental";
        }
    }

    public String getIcon() {
        switch (nodeType) {
            case SUPPLEMENTAL:
                return "📚"; // Book
            case INTERVENTION:
                return "🆘"; // Help
            case ENRICHMENT:
                return "⭐"; // Star
            default:
                return "📝";
        }
    }

    // Getters and Setters

    public int getSupplementalNodeId() {
        return supplementalNodeId;
    }

    public void setSupplementalNodeId(int supplementalNodeId) {
        this.supplementalNodeId = supplementalNodeId;
    }

    public SupplementalType getNodeType() {
        return nodeType;
    }

    public void setNodeType(SupplementalType nodeType) {
        this.nodeType = nodeType;
    }

    public int getAfterNodeId() {
        return afterNodeId;
    }

    public void setAfterNodeId(int afterNodeId) {
        this.afterNodeId = afterNodeId;
    }

    public String getTriggerLogic() {
        return triggerLogic;
    }

    public void setTriggerLogic(String triggerLogic) {
        this.triggerLogic = triggerLogic;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public String getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(String triggerReason) {
        this.triggerReason = triggerReason;
    }

    public String getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(String completedDate) {
        this.completedDate = completedDate;
    }
}
