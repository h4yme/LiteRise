package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {

    @SerializedName("rank")
    private int rank;

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("name")
    private String name;

    @SerializedName("grade")
    private int grade;

    @SerializedName("value")
    private int value;

    @SerializedName("label")
    private String label;

    public int getRank()       { return rank; }
    public int getStudentId()  { return studentId; }
    public String getName()    { return name; }
    public int getGrade()      { return grade; }
    public int getValue()      { return value; }
    public String getLabel()   { return label; }
}