package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class NodeData {
    @SerializedName("NodeID")
    private int nodeId;

    @SerializedName("NodeNumber")
    private int nodeNumber;

    @SerializedName("NodeType")
    private String nodeType;

    @SerializedName("LessonTitle")
    private String lessonTitle;

    @SerializedName("Quarter")
    private int quarter;

    @SerializedName("LessonCompleted")
    private int lessonCompleted;

    @SerializedName("GameCompleted")
    private int gameCompleted;

    @SerializedName("QuizCompleted")
    private int quizCompleted;

    // Getters
    public int getNodeId() { return nodeId; }
    public int getNodeNumber() { return nodeNumber; }
    public String getNodeType() { return nodeType; }
    public String getLessonTitle() { return lessonTitle; }
    public int getQuarter() { return quarter; }
    public boolean isLessonCompleted() { return lessonCompleted == 1; }
    public boolean isGameCompleted() { return gameCompleted == 1; }
    public boolean isQuizCompleted() { return quizCompleted == 1; }

    // Setters
    public void setNodeId(int nodeId) { this.nodeId = nodeId; }
    public void setNodeNumber(int nodeNumber) { this.nodeNumber = nodeNumber; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public void setLessonTitle(String lessonTitle) { this.lessonTitle = lessonTitle; }
    public void setQuarter(int quarter) { this.quarter = quarter; }
    public void setLessonCompleted(int lessonCompleted) { this.lessonCompleted = lessonCompleted; }
    public void setGameCompleted(int gameCompleted) { this.gameCompleted = gameCompleted; }
    public void setQuizCompleted(int quizCompleted) { this.quizCompleted = quizCompleted; }
}
