package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class NextItemResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("session_id")
    private int sessionId;

    @SerializedName("item")
    private Question item;

    @SerializedName("current_theta")
    private double currentTheta;

    @SerializedName("items_completed")
    private int itemsCompleted;

    @SerializedName("items_remaining")
    private int itemsRemaining;

    @SerializedName("assessment_complete")
    private boolean assessmentComplete;

    @SerializedName("progress_percentage")
    private double progressPercentage;

    @SerializedName("message")
    private String message;

    @SerializedName("final_theta")
    private Double finalTheta;

    @SerializedName("sem")
    private Double sem;

    @SerializedName("total_items")
    private Integer totalItems;

    @SerializedName("correct_answers")
    private Integer correctAnswers;

    @SerializedName("accuracy")
    private Double accuracy;

    public NextItemResponse() {}

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public Question getItem() {
        return item;
    }

    public void setItem(Question item) {
        this.item = item;
    }

    public double getCurrentTheta() {
        return currentTheta;
    }

    public void setCurrentTheta(double currentTheta) {
        this.currentTheta = currentTheta;
    }

    public int getItemsCompleted() {
        return itemsCompleted;
    }

    public void setItemsCompleted(int itemsCompleted) {
        this.itemsCompleted = itemsCompleted;
    }

    public int getItemsRemaining() {
        return itemsRemaining;
    }

    public void setItemsRemaining(int itemsRemaining) {
        this.itemsRemaining = itemsRemaining;
    }

    public boolean isAssessmentComplete() {
        return assessmentComplete;
    }

    public void setAssessmentComplete(boolean assessmentComplete) {
        this.assessmentComplete = assessmentComplete;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getFinalTheta() {
        return finalTheta;
    }

    public void setFinalTheta(Double finalTheta) {
        this.finalTheta = finalTheta;
    }

    public Double getSem() {
        return sem;
    }

    public void setSem(Double sem) {
        this.sem = sem;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }
}
