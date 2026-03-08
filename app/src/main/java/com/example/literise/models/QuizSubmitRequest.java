package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class QuizSubmitRequest {
    @SerializedName("student_id")
    private int studentId;

    @SerializedName("node_id")
    private int nodeId;

    @SerializedName("placement_level")
    private int placementLevel;

    @SerializedName("answers")
    private Map<Integer, String> answers; // questionId → answer text

    public QuizSubmitRequest(int studentId, int nodeId, int placementLevel, Map<Integer, String> answers) {
        this.studentId = studentId;
        this.nodeId = nodeId;
        this.placementLevel = placementLevel;
        this.answers = answers;
    }
}