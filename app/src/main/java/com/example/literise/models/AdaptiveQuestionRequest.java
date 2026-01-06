package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class AdaptiveQuestionRequest {

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("session_id")
    private int sessionId;

    @SerializedName("current_theta")
    private double currentTheta;

    @SerializedName("assessment_type")
    private String assessmentType;

    @SerializedName("category")
    private String category; // Optional

    public AdaptiveQuestionRequest(int studentId, int sessionId, double currentTheta, String assessmentType) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.currentTheta = currentTheta;
        this.assessmentType = assessmentType;
    }

    public AdaptiveQuestionRequest(int studentId, int sessionId, double currentTheta, String assessmentType, String category) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.currentTheta = currentTheta;
        this.assessmentType = assessmentType;
        this.category = category;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public double getCurrentTheta() {
        return currentTheta;
    }

    public void setCurrentTheta(double currentTheta) {
        this.currentTheta = currentTheta;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
