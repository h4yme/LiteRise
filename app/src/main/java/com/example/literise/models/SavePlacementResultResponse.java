package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class SavePlacementResultResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private PlacementResult result;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public PlacementResult getResult() {
        return result;
    }

    public static class PlacementResult {
        @SerializedName("ResultID")
        private int resultId;

        @SerializedName("StudentID")
        private int studentId;

        @SerializedName("AssessmentType")
        private String assessmentType;

        @SerializedName("PlacementLevel")
        private int placementLevel;

        @SerializedName("LevelName")
        private String levelName;

        @SerializedName("AccuracyPercentage")
        private double accuracyPercentage;

        @SerializedName("CompletedDate")
        private String completedDate;

        public int getResultId() {
            return resultId;
        }

        public int getStudentId() {
            return studentId;
        }

        public String getAssessmentType() {
            return assessmentType;
        }

        public int getPlacementLevel() {
            return placementLevel;
        }

        public String getLevelName() {
            return levelName;
        }

        public double getAccuracyPercentage() {
            return accuracyPercentage;
        }

        public String getCompletedDate() {
            return completedDate;
        }
    }
}
