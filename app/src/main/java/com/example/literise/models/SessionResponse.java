package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class SessionResponse {

    @SerializedName("SessionID")
    private int sessionId;

    @SerializedName("StudentID")
    private int studentId;

    @SerializedName("SessionType")
    private String sessionType;

    @SerializedName("InitialTheta")
    private float initialTheta;

    @SerializedName("StartTime")
    private String startTime;

    // Empty constructor
    public SessionResponse() {}

    // Getters
    public int getSessionId() {
        return sessionId;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public float getInitialTheta() {
        return initialTheta;
    }

    public String getStartTime() {
        return startTime;
    }

    // Setters
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }

    public void setInitialTheta(float initialTheta) {
        this.initialTheta = initialTheta;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
}
