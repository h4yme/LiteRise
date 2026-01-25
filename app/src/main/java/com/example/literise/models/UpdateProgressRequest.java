package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class UpdateProgressRequest {
    @SerializedName("student_id")
    private int studentId;

    @SerializedName("node_id")
    private int nodeId;

    @SerializedName("phase")
    private String phase;

    public UpdateProgressRequest(int studentId, int nodeId, String phase) {
        this.studentId = studentId;
        this.nodeId = nodeId;
        this.phase = phase;
    }
}