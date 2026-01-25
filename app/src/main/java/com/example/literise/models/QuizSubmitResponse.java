package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizSubmitResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("result")
    private Result result;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public Result getResult() { return result; }
    public String getMessage() { return message; }

    public static class Result {
        @SerializedName("score_percent")
        private double scorePercent;

        @SerializedName("correct_count")
        private int correctCount;

        @SerializedName("total_questions")
        private int totalQuestions;

        @SerializedName("adaptive_decision")
        private String adaptiveDecision;

        @SerializedName("xp_awarded")
        private int xpAwarded;

        @SerializedName("unlocked_nodes")
        private List<UnlockedNode> unlockedNodes;

        public double getScorePercent() { return scorePercent; }
        public int getCorrectCount() { return correctCount; }
        public int getTotalQuestions() { return totalQuestions; }
        public String getAdaptiveDecision() { return adaptiveDecision; }
        public int getXpAwarded() { return xpAwarded; }
        public List<UnlockedNode> getUnlockedNodes() { return unlockedNodes; }
    }

    public static class UnlockedNode {
        @SerializedName("type")
        private String type;

        @SerializedName("node_id")
        private int nodeId;

        @SerializedName("title")
        private String title;

        @SerializedName("mandatory")
        private boolean mandatory;

        public String getType() { return type; }
        public int getNodeId() { return nodeId; }
        public String getTitle() { return title; }
        public boolean isMandatory() { return mandatory; }
    }
}
