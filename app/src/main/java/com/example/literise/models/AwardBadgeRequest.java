package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class AwardBadgeRequest {

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("node_id")
    private int nodeId;

    public AwardBadgeRequest(int studentId, int nodeId) {
        this.studentId = studentId;
        this.nodeId    = nodeId;
    }
}