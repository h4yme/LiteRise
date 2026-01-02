package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class VerifyOTPResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("valid")
    private boolean valid;

    @SerializedName("message")
    private String message;

    @SerializedName("note")
    private String note;

    @SerializedName("error")
    private String error;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
