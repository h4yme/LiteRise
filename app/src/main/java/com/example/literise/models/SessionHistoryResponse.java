package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SessionHistoryResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("StudentID")
    private int studentId;

    @SerializedName("count")
    private int count;

    @SerializedName("sessions")
    private List<SessionHistory> sessions;

    // Empty constructor
    public SessionHistoryResponse() {}

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getCount() {
        return count;
    }

    public List<SessionHistory> getSessions() {
        return sessions;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setSessions(List<SessionHistory> sessions) {
        this.sessions = sessions;
    }
}
