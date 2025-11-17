package com.example.literise.models;



import com.google.gson.annotations.SerializedName;



public class PronunciationResponse {



    @SerializedName("success")

    private boolean success;



    @SerializedName("score")

    private int score;



    @SerializedName("feedback")

    private String feedback;



    @SerializedName("is_correct")

    private boolean isCorrect;



    @SerializedName("expected")

    private String expected;



    @SerializedName("recognized")

    private String recognized;



    @SerializedName("confidence")

    private float confidence;



    // Getters and Setters

    public boolean isSuccess() {

        return success;

    }



    public void setSuccess(boolean success) {

        this.success = success;

    }



    public int getScore() {

        return score;

    }



    public void setScore(int score) {

        this.score = score;

    }



    public String getFeedback() {

        return feedback;

    }



    public void setFeedback(String feedback) {

        this.feedback = feedback;

    }



    public boolean isCorrect() {

        return isCorrect;

    }



    public void setCorrect(boolean correct) {

        isCorrect = correct;

    }



    public String getExpected() {

        return expected;

    }



    public void setExpected(String expected) {

        this.expected = expected;

    }



    public String getRecognized() {

        return recognized;

    }



    public void setRecognized(String recognized) {

        this.recognized = recognized;

    }



    public float getConfidence() {

        return confidence;

    }



    public void setConfidence(float confidence) {

        this.confidence = confidence;

    }

}