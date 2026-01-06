package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class SubmitAnswerResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("response_id")
    private int responseId;

    @SerializedName("is_correct")
    private boolean isCorrect;

    @SerializedName("feedback")
    private FeedbackData feedback;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getResponseId() {
        return responseId;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public FeedbackData getFeedback() {
        return feedback;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    // Inner class for feedback data
    public static class FeedbackData {
        @SerializedName("message")
        private String message;

        @SerializedName("expected_probability")
        private double expectedProbability;

        @SerializedName("new_theta_estimate")
        private double newThetaEstimate;

        // Getters
        public String getMessage() {
            return message;
        }

        public double getExpectedProbability() {
            return expectedProbability;
        }

        public double getNewThetaEstimate() {
            return newThetaEstimate;
        }
    }
}
