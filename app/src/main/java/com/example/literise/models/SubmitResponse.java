package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class SubmitResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("SessionID")
    private int sessionId;

    @SerializedName("FinalTheta")
    private float finalTheta;

    @SerializedName("InitialTheta")
    private float initialTheta;

    @SerializedName("ThetaChange")
    private float thetaChange;

    @SerializedName("TotalQuestions")
    private int totalQuestions;

    @SerializedName("CorrectAnswers")
    private int correctAnswers;

    @SerializedName("Accuracy")
    private float accuracy;

    @SerializedName("StandardError")
    private float standardError;

    @SerializedName("Reliability")
    private float reliability;

    @SerializedName("message")
    private String message;

    // Empty constructor
    public SubmitResponse() {}

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getSessionId() {
        return sessionId;
    }

    public float getFinalTheta() {
        return finalTheta;
    }

    public float getInitialTheta() {
        return initialTheta;
    }

    public float getThetaChange() {
        return thetaChange;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public float getStandardError() {
        return standardError;
    }

    public float getReliability() {
        return reliability;
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setFinalTheta(float finalTheta) {
        this.finalTheta = finalTheta;
    }

    public void setInitialTheta(float initialTheta) {
        this.initialTheta = initialTheta;
    }

    public void setThetaChange(float thetaChange) {
        this.thetaChange = thetaChange;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void setStandardError(float standardError) {
        this.standardError = standardError;
    }

    public void setReliability(float reliability) {
        this.reliability = reliability;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
