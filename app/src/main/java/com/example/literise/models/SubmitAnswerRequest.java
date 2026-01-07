package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class SubmitAnswerRequest {

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("item_id")
    private int itemId;

    @SerializedName("session_id")
    private int sessionId;

    @SerializedName("assessment_type")
    private String assessmentType;

    @SerializedName("selected_answer")
    private String selectedAnswer;

    @SerializedName("is_correct")
    private boolean isCorrect;

    @SerializedName("student_theta")
    private double studentTheta;

    @SerializedName("response_time")
    private Integer responseTime; // Nullable

    @SerializedName("question_number")
    private int questionNumber;

    @SerializedName("device_info")
    private String deviceInfo;

    @SerializedName("interaction_data")
    private String interactionData; // JSON string

    public SubmitAnswerRequest(int studentId, int itemId, int sessionId, String assessmentType,
                               String selectedAnswer, boolean isCorrect, double studentTheta,
                               int questionNumber, String deviceInfo) {
        this.studentId = studentId;
        this.itemId = itemId;
        this.sessionId = sessionId;
        this.assessmentType = assessmentType;
        this.selectedAnswer = selectedAnswer;
        this.isCorrect = isCorrect;
        this.studentTheta = studentTheta;
        this.questionNumber = questionNumber;
        this.deviceInfo = deviceInfo;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public double getStudentTheta() {
        return studentTheta;
    }

    public void setStudentTheta(double studentTheta) {
        this.studentTheta = studentTheta;
    }

    public Integer getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Integer responseTime) {
        this.responseTime = responseTime;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getInteractionData() {
        return interactionData;
    }

    public void setInteractionData(String interactionData) {
        this.interactionData = interactionData;
    }
}