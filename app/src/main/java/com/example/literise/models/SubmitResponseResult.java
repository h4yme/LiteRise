package com.example.literise.models;



import com.google.gson.annotations.SerializedName;



public class SubmitResponseResult {



    @SerializedName("success")

    private boolean success;



    @SerializedName("message")

    private String message;



    @SerializedName("session_id")

    private int sessionId;



    @SerializedName("total_responses")

    private int totalResponses;



    @SerializedName("correct_answers")

    private int correctAnswers;



    @SerializedName("accuracy")

    private double accuracy;



    @SerializedName("ability")

    private AbilityResult ability;



    public static class AbilityResult {

        @SerializedName("initial_theta")

        private double initialTheta;



        @SerializedName("final_theta")

        private double finalTheta;



        @SerializedName("change")

        private double change;



        @SerializedName("classification")

        private String classification;



        @SerializedName("standard_error")

        private double standardError;



        // Getters

        public double getInitialTheta() { return initialTheta; }

        public double getFinalTheta() { return finalTheta; }

        public double getChange() { return change; }

        public String getClassification() { return classification; }

        public double getStandardError() { return standardError; }



        // Setters

        public void setInitialTheta(double initialTheta) { this.initialTheta = initialTheta; }

        public void setFinalTheta(double finalTheta) { this.finalTheta = finalTheta; }

        public void setChange(double change) { this.change = change; }

        public void setClassification(String classification) { this.classification = classification; }

        public void setStandardError(double standardError) { this.standardError = standardError; }

    }



    // Getters

    public boolean isSuccess() { return success; }

    public String getMessage() { return message; }

    public int getSessionId() { return sessionId; }

    public int getTotalResponses() { return totalResponses; }

    public int getCorrectAnswers() { return correctAnswers; }

    public double getAccuracy() { return accuracy; }

    public AbilityResult getAbility() { return ability; }



    // Setters

    public void setSuccess(boolean success) { this.success = success; }

    public void setMessage(String message) { this.message = message; }

    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    public void setTotalResponses(int totalResponses) { this.totalResponses = totalResponses; }

    public void setCorrectAnswers(int correctAnswers) { this.correctAnswers = correctAnswers; }

    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public void setAbility(AbilityResult ability) { this.ability = ability; }

}