package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizSubmitRequest {
    @SerializedName("student_id")
    private int studentId;

    @SerializedName("node_id")
    private int nodeId;

    @SerializedName("quiz_score")
    private int quizScore;

    @SerializedName("attempt_count")
    private int attemptCount;

    @SerializedName("recent_scores")
    private List<Integer> recentScores;

    @SerializedName("time_spent")
    private int timeSpent;

    public QuizSubmitRequest(int studentId, int nodeId, int quizScore,
                             int attemptCount, List<Integer> recentScores, int timeSpent) {
        this.studentId = studentId;
        this.nodeId = nodeId;
        this.quizScore = quizScore;
        this.attemptCount = attemptCount;
        this.recentScores = recentScores;
        this.timeSpent = timeSpent;
    }

    // Getters
    public int getStudentId() { return studentId; }
    public int getNodeId() { return nodeId; }
    public int getQuizScore() { return quizScore; }
    public int getAttemptCount() { return attemptCount; }
    public List<Integer> getRecentScores() { return recentScores; }
    public int getTimeSpent() { return timeSpent; }

    // Setters
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public void setNodeId(int nodeId) { this.nodeId = nodeId; }
    public void setQuizScore(int quizScore) { this.quizScore = quizScore; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    public void setRecentScores(List<Integer> recentScores) { this.recentScores = recentScores; }
    public void setTimeSpent(int timeSpent) { this.timeSpent = timeSpent; }
}
