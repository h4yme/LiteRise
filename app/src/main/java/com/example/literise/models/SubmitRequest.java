package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SubmitRequest {

    @SerializedName("StudentID")
    private int studentId;

    @SerializedName("SessionID")
    private int sessionId;

    @SerializedName("Responses")
    private List<ResponseModel> responses;

    public SubmitRequest(int studentId, int sessionId, List<ResponseModel> responses) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.responses = responses;
    }

    public int getStudentId() { return studentId; }
    public int getSessionId() { return sessionId; }
    public List<ResponseModel> getResponses() { return responses; }

    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public void setResponses(List<ResponseModel> responses) { this.responses = responses; }
}
