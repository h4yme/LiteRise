package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class CreateSessionRequest {

    @SerializedName("StudentID")
    private int studentId;

    @SerializedName("SessionType")
    private String sessionType;

    public CreateSessionRequest(int studentId, String sessionType) {
        this.studentId = studentId;
        this.sessionType = sessionType;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public void setSessionType(String sessionType) {
        this.sessionType = sessionType;
    }
}
