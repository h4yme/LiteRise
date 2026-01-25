package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class NodeData {
    @SerializedName("node_id")
    private int nodeId;

    @SerializedName("node_number")
    private int nodeNumber;

    @SerializedName("lesson_title")
    private String lessonTitle;

    @SerializedName("node_type")
    private String nodeType;  // "REGULAR_LESSON" or "FINAL_ASSESSMENT"

    @SerializedName("quarter")
    private int quarter;  // 1-4, or 0 for final assessment

    @SerializedName("lesson_completed")
    private boolean lessonCompleted;

    @SerializedName("game_completed")
    private boolean gameCompleted;

    @SerializedName("quiz_completed")
    private boolean quizCompleted;

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
        return quarter;
    }

    public boolean isLessonCompleted() {
        return lessonCompleted;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    public boolean isQuizCompleted() {
        return quizCompleted;
    }
}
