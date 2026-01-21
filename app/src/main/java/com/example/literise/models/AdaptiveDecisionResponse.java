package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class AdaptiveDecisionResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("decision")
    private String decision; // PROCEED, ADD_SUPPLEMENTAL, ADD_INTERVENTION, OFFER_ENRICHMENT, RETRY_REQUIRED

    @SerializedName("reason")
    private String reason;

    @SerializedName("next_node_id")
    private Integer nextNodeId;

    @SerializedName("supplemental_node_id")
    private Integer supplementalNodeId;

    @SerializedName("pacing_strategy")
    private String pacingStrategy; // SLOW, MODERATE, FAST

    @SerializedName("message")
    private String message;

    @SerializedName("xp_earned")
    private int xpEarned;

    // Getters
    public boolean isSuccess() { return success; }
    public String getDecision() { return decision; }
    public String getReason() { return reason; }
    public Integer getNextNodeId() { return nextNodeId; }
    public Integer getSupplementalNodeId() { return supplementalNodeId; }
    public String getPacingStrategy() { return pacingStrategy; }
    public String getMessage() { return message; }
    public int getXpEarned() { return xpEarned; }

    // Setters
    public void setSuccess(boolean success) { this.success = success; }
    public void setDecision(String decision) { this.decision = decision; }
    public void setReason(String reason) { this.reason = reason; }
    public void setNextNodeId(Integer nextNodeId) { this.nextNodeId = nextNodeId; }
    public void setSupplementalNodeId(Integer supplementalNodeId) { this.supplementalNodeId = supplementalNodeId; }
    public void setPacingStrategy(String pacingStrategy) { this.pacingStrategy = pacingStrategy; }
    public void setMessage(String message) { this.message = message; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
}
