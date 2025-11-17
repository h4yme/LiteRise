package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class SessionHistory {

    @SerializedName("SessionID")
    private int sessionId;

    @SerializedName("SessionType")
    private String sessionType;

    @SerializedName("StartTime")
    private String startTime;

    @SerializedName("EndTime")
    private String endTime;

    @SerializedName("InitialTheta")
    private float initialTheta;

    @SerializedName("FinalTheta")
    private float finalTheta;

    @SerializedName("ThetaChange")
    private float thetaChange;

    @SerializedName("TotalQuestions")
    private int totalQuestions;

    @SerializedName("CorrectAnswers")
    private int correctAnswers;

    @SerializedName("Accuracy")
    private float accuracy;

    @SerializedName("IsCompleted")
    private boolean isCompleted;

    // Empty constructor
    public SessionHistory() {}

    // Getters
    public int getSessionId() {
        return sessionId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public float getInitialTheta() {
        return initialTheta;
    }

    public float getFinalTheta() {
        return finalTheta;
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

    public boolean isCompleted() {
        return isCompleted;
    }

    // Setters
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setInitialTheta(float initialTheta) {
        this.initialTheta = initialTheta;
    }

    public void setFinalTheta(float finalTheta) {
        this.finalTheta = finalTheta;
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

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
