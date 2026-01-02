package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class ForgotPasswordResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("email")
    private String email;

    @SerializedName("expires_in_minutes")
    private int expiresInMinutes;

    @SerializedName("note")
    private String note;

    @SerializedName("debug_otp")
    private String debugOtp;

    @SerializedName("error")
    private String error;

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public void setExpiresInMinutes(int expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getDebugOtp() {
        return debugOtp;
    }

    public void setDebugOtp(String debugOtp) {
        this.debugOtp = debugOtp;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}