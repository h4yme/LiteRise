package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class SavePlacementResultRequest {

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("session_id")
    private int sessionId;

    @SerializedName("assessment_type")
    private String assessmentType;

    @SerializedName("final_theta")
    private double finalTheta;

    @SerializedName("placement_level")
    private int placementLevel;

    @SerializedName("level_name")
    private String levelName;

    @SerializedName("total_questions")
    private int totalQuestions;

    @SerializedName("correct_answers")
    private int correctAnswers;

    @SerializedName("accuracy_percentage")
    private double accuracyPercentage;

    @SerializedName("category_scores")
    private Map<String, Double> categoryScores;

    @SerializedName("category_theta")
    private Map<String, Double> categoryTheta;

    @SerializedName("time_spent_seconds")
    private Integer timeSpentSeconds;

    @SerializedName("device_info")
    private String deviceInfo;

    @SerializedName("app_version")
    private String appVersion;

    public SavePlacementResultRequest(int studentId, int sessionId, String assessmentType,
                                     double finalTheta, int placementLevel, String levelName,
                                     int totalQuestions, int correctAnswers, double accuracyPercentage) {
        this.studentId = studentId;
        this.sessionId = sessionId;
        this.assessmentType = assessmentType;
        this.finalTheta = finalTheta;
        this.placementLevel = placementLevel;
        this.levelName = levelName;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.accuracyPercentage = accuracyPercentage;
        this.categoryScores = new HashMap<>();
        this.categoryTheta = new HashMap<>();
    }

    // Getters and setters
    public void setCategoryScore(int category, double score) {
        categoryScores.put("category" + category, score);
    }

    public void setCategoryTheta(int category, double theta) {
        categoryTheta.put("category" + category, theta);
    }

    public void setTimeSpentSeconds(int timeSpentSeconds) {
        this.timeSpentSeconds = timeSpentSeconds;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
