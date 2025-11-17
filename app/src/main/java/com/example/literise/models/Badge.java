package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class Badge {

    @SerializedName("BadgeID")
    private int badgeId;

    @SerializedName("BadgeName")
    private String badgeName;

    @SerializedName("Description")
    private String description;

    @SerializedName("Icon")
    private String icon;

    @SerializedName("XPRequired")
    private int xpRequired;

    @SerializedName("CreatedDate")
    private String createdDate;

    @SerializedName("UnlockedDate")
    private String unlockedDate;

    @SerializedName("UnlockID")
    private int unlockId;

    // Empty constructor
    public Badge() {}

    // Getters
    public int getBadgeId() {
        return badgeId;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public int getXpRequired() {
        return xpRequired;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getUnlockedDate() {
        return unlockedDate;
    }

    public int getUnlockId() {
        return unlockId;
    }

    // Setters
    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setXpRequired(int xpRequired) {
        this.xpRequired = xpRequired;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public void setUnlockedDate(String unlockedDate) {
        this.unlockedDate = unlockedDate;
    }

    public void setUnlockId(int unlockId) {
        this.unlockId = unlockId;
    }
}
