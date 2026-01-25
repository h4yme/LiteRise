package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class UpdateProgressResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
}
