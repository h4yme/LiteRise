package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class TutorialStatusResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("seen")
    private boolean seen;

    @SerializedName("tutorial_key")
    private String tutorialKey;

    @SerializedName("seen_at")
    private String seenAt;

    public boolean isSuccess() { return success; }
    public boolean isSeen() { return seen; }
    public String getTutorialKey() { return tutorialKey; }
    public String getSeenAt() { return seenAt; }
}