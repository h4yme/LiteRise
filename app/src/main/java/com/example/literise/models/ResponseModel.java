package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class ResponseModel {

    @SerializedName("SessionID")
    private int sessionId;

    @SerializedName("ItemID")
    private int itemId;

    @SerializedName("SelectedOption")
    private String selectedOption;

    @SerializedName("Correct")
    private boolean correct;

    @SerializedName("TimeTakenSec")
    private float timeTakenSec;

    // ✅ Empty constructor (required for Retrofit and Gson)
    public ResponseModel() {}

    // Optional convenience constructor
    public ResponseModel(int sessionId, int itemId, String selectedOption, boolean correct, float timeTakenSec) {
        this.sessionId = sessionId;
        this.itemId = itemId;
        this.selectedOption = selectedOption;
        this.correct = correct;
        this.timeTakenSec = timeTakenSec;
    }

    // Getters
    public int getSessionId() { return sessionId; }
    public int getItemId() { return itemId; }
    public String getSelectedOption() { return selectedOption; }
    public boolean isCorrect() { return correct; }
    public float getTimeTakenSec() { return timeTakenSec; }

    // ✅ Setters
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setSelectedOption(String selectedOption) { this.selectedOption = selectedOption; }
    public void setCorrect(boolean correct) { this.correct = correct; }
    public void setTimeTakenSec(float timeTakenSec) { this.timeTakenSec = timeTakenSec; }
}
