package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class PlacementProgressResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("student")
    private StudentInfo student;

    @SerializedName("results")
    private AssessmentResults results;

    @SerializedName("comparison")
    private ComparisonData comparison;

    @SerializedName("session_history")
    private List<SessionLog> sessionHistory;

    public boolean isSuccess() {
        return success;
    }

    public StudentInfo getStudent() {
        return student;
    }

    public AssessmentResults getResults() {
        return results;
    }

    public ComparisonData getComparison() {
        return comparison;
    }

    public List<SessionLog> getSessionHistory() {
        return sessionHistory;
    }

    public static class StudentInfo {
        @SerializedName("StudentID")
        private int studentId;

        @SerializedName("FirstName")
        private String firstName;

        @SerializedName("LastName")
        private String lastName;

        @SerializedName("Email")
        private String email;

        @SerializedName("PreAssessmentCompleted")
        private boolean preAssessmentCompleted;

        @SerializedName("PostAssessmentCompleted")
        private boolean postAssessmentCompleted;

        @SerializedName("AssessmentStatus")
        private String assessmentStatus;

        public int getStudentId() {
            return studentId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        public boolean isPreAssessmentCompleted() {
            return preAssessmentCompleted;
        }

        public boolean isPostAssessmentCompleted() {
            return postAssessmentCompleted;
        }

        public String getAssessmentStatus() {
            return assessmentStatus;
        }
    }

    public static class AssessmentResults {
        @SerializedName("pre")
        private AssessmentDetail pre;

        @SerializedName("post")
        private AssessmentDetail post;

        public AssessmentDetail getPre() {
            return pre;
        }

        public AssessmentDetail getPost() {
            return post;
        }
    }

    public static class AssessmentDetail {
        @SerializedName("ResultID")
        private int resultId;

        @SerializedName("AssessmentType")
        private String assessmentType;

        @SerializedName("CompletedDate")
        private String completedDate;

        @SerializedName("FinalTheta")
        private double finalTheta;

        @SerializedName("PlacementLevel")
        private int placementLevel;

        @SerializedName("LevelName")
        private String levelName;

        @SerializedName("AccuracyPercentage")
        private double accuracyPercentage;

        @SerializedName("TotalQuestions")
        private int totalQuestions;

        @SerializedName("CorrectAnswers")
        private int correctAnswers;

        @SerializedName("CategoryScores")
        private Map<String, Double> categoryScores;

        public int getResultId() {
            return resultId;
        }

        public String getAssessmentType() {
            return assessmentType;
        }

        public String getCompletedDate() {
            return completedDate;
        }

        public double getFinalTheta() {
            return finalTheta;
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

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public int getCorrectAnswers() {
            return correctAnswers;
        }

        public Map<String, Double> getCategoryScores() {
            return categoryScores;
        }
    }

    public static class ComparisonData {
        @SerializedName("ThetaGrowth")
        private Double thetaGrowth;

        @SerializedName("LevelGrowth")
        private Integer levelGrowth;

        @SerializedName("AccuracyGrowth")
        private Double accuracyGrowth;

        @SerializedName("ComparisonStatus")
        private String comparisonStatus;

        public Double getThetaGrowth() {
            return thetaGrowth;
        }

        public Integer getLevelGrowth() {
            return levelGrowth;
        }

        public Double getAccuracyGrowth() {
            return accuracyGrowth;
        }

        public String getComparisonStatus() {
            return comparisonStatus;
        }
    }

    public static class SessionLog {
        @SerializedName("LogID")
        private int logId;

        @SerializedName("SessionType")
        private String sessionType;

        @SerializedName("SessionTag")
        private String sessionTag;

        @SerializedName("LoggedAt")
        private String loggedAt;

        @SerializedName("DeviceInfo")
        private String deviceInfo;

        public int getLogId() {
            return logId;
        }

        public String getSessionType() {
            return sessionType;
        }

        public String getSessionTag() {
            return sessionTag;
        }

        public String getLoggedAt() {
            return loggedAt;
        }

        public String getDeviceInfo() {
            return deviceInfo;
        }
    }
}