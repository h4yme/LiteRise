package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class BadgeItem {

    @SerializedName("badge_id")
    private int badgeId;

    @SerializedName("badge_name")
    private String badgeName;

    @SerializedName("badge_description")
    private String badgeDescription;

    @SerializedName("badge_icon_url")
    private String badgeIconUrl;

    @SerializedName("xp_reward")
    private int xpReward;

    @SerializedName("badge_category")
    private String badgeCategory;

    @SerializedName("earned")
    private boolean earned;

    @SerializedName("date_earned")
    private String dateEarned;

    // Getters
    public int getBadgeId()          { return badgeId; }
    public String getBadgeName()      { return badgeName; }
    public String getBadgeDescription() { return badgeDescription; }
    public String getBadgeIconUrl()   { return badgeIconUrl; }
    public int getXpReward()          { return xpReward; }
    public String getBadgeCategory()  { return badgeCategory; }
    public boolean isEarned()         { return earned; }
    public String getDateEarned()     { return dateEarned; }

    /**
     * Returns the Android drawable resource name for this badge.
     * PNGs are named badge_1.png through badge_15.png matching badge_id.
     */
    public String getDrawableName() {
        return "badge_" + badgeId;
    }
}
