package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class NodeData {
    @SerializedName("NodeID")
    private int nodeId;

    @SerializedName("NodeNumber")
    private int nodeNumber;

    @SerializedName("LessonTitle")
    private String lessonTitle;

    @SerializedName("NodeType")
    private String nodeType;  // "CORE_LESSON" or "FINAL_ASSESSMENT"

    @SerializedName("Quarter")
    private String quarter;  // Changed to String since API returns "1", "2", "3", "4", or null

    @SerializedName("LessonCompleted")
    private String lessonCompleted;  // Changed to String since API returns "0" or "1"

    @SerializedName("GameCompleted")
    private String gameCompleted;  // Changed to String since API returns "0" or "1"

    @SerializedName("QuizCompleted")
    private String quizCompleted;  // Changed to String since API returns "0" or "1"

    // Getters
    public int getNodeId() {
        return nodeId;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public String getLessonTitle() {
        return lessonTitle;
    }

    public String getNodeType() {
        return nodeType;
    }

    public int getQuarter() {
        // Parse string to int, handle null for final assessment
        try {
            return quarter != null ? Integer.parseInt(quarter) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public boolean isLessonCompleted() {
        return "1".equals(lessonCompleted);
    }

    public boolean isGameCompleted() {
        return "1".equals(gameCompleted);
    }

    public boolean isQuizCompleted() {
        return "1".equals(quizCompleted);
    }
}