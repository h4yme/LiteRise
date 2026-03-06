package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class CompleteTutorialRequest {

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("tutorial_key")
    private String tutorialKey;

    public CompleteTutorialRequest(int studentId, String tutorialKey) {
        this.studentId = studentId;
        this.tutorialKey = tutorialKey;
    }

    public int getStudentId() { return studentId; }
    public String getTutorialKey() { return tutorialKey; }
}
