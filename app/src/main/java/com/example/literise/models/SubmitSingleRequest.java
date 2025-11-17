package com.example.literise.models;



import com.google.gson.annotations.SerializedName;



public class SubmitSingleRequest {



    @SerializedName("session_id")

    private int sessionId;



    @SerializedName("item_id")

    private int itemId;



    @SerializedName("selected_option")

    private String selectedOption;



    @SerializedName("is_correct")

    private int isCorrect;



    @SerializedName("time_spent")

    private int timeSpent;



    public SubmitSingleRequest() {}



    public SubmitSingleRequest(int sessionId, int itemId, String selectedOption, int isCorrect, int timeSpent) {

        this.sessionId = sessionId;

        this.itemId = itemId;

        this.selectedOption = selectedOption;

        this.isCorrect = isCorrect;

        this.timeSpent = timeSpent;

    }



    // Getters and Setters

    public int getSessionId() {

        return sessionId;

    }



    public void setSessionId(int sessionId) {

        this.sessionId = sessionId;

    }



    public int getItemId() {

        return itemId;

    }



    public void setItemId(int itemId) {

        this.itemId = itemId;

    }



    public String getSelectedOption() {

        return selectedOption;

    }



    public void setSelectedOption(String selectedOption) {

        this.selectedOption = selectedOption;

    }



    public int getIsCorrect() {

        return isCorrect;

    }



    public void setIsCorrect(int isCorrect) {

        this.isCorrect = isCorrect;

    }



    public int getTimeSpent() {

        return timeSpent;

    }



    public void setTimeSpent(int timeSpent) {

        this.timeSpent = timeSpent;

    }

}