package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class SingleResponseResult {

    @SerializedName("success")
    private boolean success;

    @SerializedName("is_correct")
    private boolean isCorrect;

    @SerializedName("new_theta")
    private double newTheta;

    @SerializedName("previous_theta")
    private double previousTheta;

    @SerializedName("theta_change")
    private double thetaChange;

    @SerializedName("classification")
    private String classification;

    @SerializedName("standard_error")
    private double standardError;

    @SerializedName("feedback")
    private String feedback;

    @SerializedName("total_responses")
    private int totalResponses;

    public SingleResponseResult() {}

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public double getNewTheta() {
        return newTheta;
    }

    public void setNewTheta(double newTheta) {
        this.newTheta = newTheta;
    }

    public double getPreviousTheta() {
        return previousTheta;
    }

    public void setPreviousTheta(double previousTheta) {
        this.previousTheta = previousTheta;
    }

    public double getThetaChange() {
        return thetaChange;
    }

    public void setThetaChange(double thetaChange) {
        this.thetaChange = thetaChange;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public double getStandardError() {
        return standardError;
    }

    public void setStandardError(double standardError) {
        this.standardError = standardError;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getTotalResponses() {
        return totalResponses;
    }

    public void setTotalResponses(int totalResponses) {
        this.totalResponses = totalResponses;
    }
}
