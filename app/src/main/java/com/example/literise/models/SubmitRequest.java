package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SubmitRequest {

    @SerializedName("StudentID")
    private int studentId;

    @SerializedName("Responses")
    private List<ResponseModel> responses;

    public SubmitRequest(int studentId, List<ResponseModel> responses) {
        this.studentId = studentId;
        this.responses = responses;
    }

    public int getStudentId() { return studentId; }
    public List<ResponseModel> getResponses() { return responses; }
}
