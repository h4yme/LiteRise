package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AwardBadgeResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("new_badges")
    private List<BadgeItem> newBadges;

    @SerializedName("module_completed")
    private boolean moduleCompleted;

    @SerializedName("module_id")
    private Integer moduleId;

    public boolean isSuccess()               { return success; }
    public List<BadgeItem> getNewBadges()    { return newBadges; }
    public boolean isModuleCompleted()       { return moduleCompleted; }
    public Integer getModuleId()             { return moduleId; }
}