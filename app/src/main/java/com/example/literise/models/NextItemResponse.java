package com.example.literise.models;



import com.google.gson.annotations.SerializedName;



public class NextItemResponse {



    @SerializedName("success")

    private boolean success;



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



    public NextItemResponse() {}



    // Getters and Setters

    public boolean isSuccess() {

        return success;

    }



    public void setSuccess(boolean success) {

        this.success = success;

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

}