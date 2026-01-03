package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class LogSessionResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("log_id")
    private int logId;

    @SerializedName("session_type")
    private String sessionType;

    @SerializedName("logged_at")
    private String loggedAt;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public int getLogId() {
        return logId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getLoggedAt() {
        return loggedAt;
    }
}