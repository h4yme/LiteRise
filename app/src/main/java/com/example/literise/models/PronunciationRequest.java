package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class PronunciationRequest {

    @SerializedName("item_id")
    private int itemId;

    @SerializedName("expected_word")
    private String expectedWord;

    @SerializedName("recognized_text")
    private String recognizedText;

    @SerializedName("confidence")
    private float confidence;

    public PronunciationRequest(int itemId, String expectedWord, String recognizedText, float confidence) {
        this.itemId = itemId;
        this.expectedWord = expectedWord;
        this.recognizedText = recognizedText;
        this.confidence = confidence;
    }

    // Getters and Setters
    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getExpectedWord() {
        return expectedWord;
    }

    public void setExpectedWord(String expectedWord) {
        this.expectedWord = expectedWord;
    }

    public String getRecognizedText() {
        return recognizedText;
    }

    public void setRecognizedText(String recognizedText) {
        this.recognizedText = recognizedText;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
