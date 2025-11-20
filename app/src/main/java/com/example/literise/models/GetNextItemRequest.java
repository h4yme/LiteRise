package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GetNextItemRequest {

    @SerializedName("session_id")
    private int sessionId;

    @SerializedName("current_theta")
    private double currentTheta;

    @SerializedName("items_answered")
    private List<Integer> itemsAnswered;

    public GetNextItemRequest() {}

    public GetNextItemRequest(int sessionId, double currentTheta, List<Integer> itemsAnswered) {
        this.sessionId = sessionId;
        this.currentTheta = currentTheta;
        this.itemsAnswered = itemsAnswered;
    }

    // Getters and Setters
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public double getCurrentTheta() {
        return currentTheta;
    }

    public void setCurrentTheta(double currentTheta) {
        this.currentTheta = currentTheta;
    }

    public List<Integer> getItemsAnswered() {
        return itemsAnswered;
    }

    public void setItemsAnswered(List<Integer> itemsAnswered) {
        this.itemsAnswered = itemsAnswered;
    }
}
