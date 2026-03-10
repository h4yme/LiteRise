package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BadgesResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("earned_count")
    private int earnedCount;

    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("badges")
    private List<BadgeItem> badges;

    public boolean isSuccess()           { return success; }
    public int getEarnedCount()          { return earnedCount; }
    public int getTotalCount()           { return totalCount; }
    public List<BadgeItem> getBadges()   { return badges; }
}