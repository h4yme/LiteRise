package com.example.literise.models;



import com.google.gson.annotations.SerializedName;



import java.util.List;



/**

 * Response model for save game result API

 * Contains updated student stats and any badges unlocked

 */

public class SaveGameResultResponse {



    @SerializedName("success")

    private boolean success;



    @SerializedName("message")

    private String message;



    @SerializedName("game_result_id")

    private int gameResultId;



    @SerializedName("student")

    private StudentStats student;



    @SerializedName("badges_unlocked")

    private List<Badge> badgesUnlocked;



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



    public int getGameResultId() {

        return gameResultId;

    }



    public void setGameResultId(int gameResultId) {

        this.gameResultId = gameResultId;

    }



    public StudentStats getStudent() {

        return student;

    }



    public void setStudent(StudentStats student) {

        this.student = student;

    }



    public List<Badge> getBadgesUnlocked() {

        return badgesUnlocked;

    }



    public void setBadgesUnlocked(List<Badge> badgesUnlocked) {

        this.badgesUnlocked = badgesUnlocked;

    }



    /**

     * Inner class for student statistics

     */

    public static class StudentStats {

        @SerializedName("TotalXP")

        private int totalXP;



        @SerializedName("CurrentStreak")

        private int currentStreak;



        @SerializedName("LongestStreak")

        private int longestStreak;



        public int getTotalXP() {

            return totalXP;

        }



        public void setTotalXP(int totalXP) {

            this.totalXP = totalXP;

        }



        public int getCurrentStreak() {

            return currentStreak;

        }



        public void setCurrentStreak(int currentStreak) {

            this.currentStreak = currentStreak;

        }



        public int getLongestStreak() {

            return longestStreak;

        }



        public void setLongestStreak(int longestStreak) {

            this.longestStreak = longestStreak;

        }

    }



    /**

     * Inner class for badge information

     */

    public static class Badge {

        @SerializedName("BadgeID")

        private int badgeId;



        @SerializedName("BadgeName")

        private String badgeName;



        @SerializedName("BadgeDescription")

        private String badgeDescription;



        @SerializedName("BadgeIconURL")

        private String badgeIconURL;



        @SerializedName("XPReward")

        private int xpReward;



        @SerializedName("BadgeCategory")

        private String badgeCategory;



        public int getBadgeId() {

            return badgeId;

        }



        public void setBadgeId(int badgeId) {

            this.badgeId = badgeId;

        }



        public String getBadgeName() {

            return badgeName;

        }



        public void setBadgeName(String badgeName) {

            this.badgeName = badgeName;

        }



        public String getBadgeDescription() {

            return badgeDescription;

        }



        public void setBadgeDescription(String badgeDescription) {

            this.badgeDescription = badgeDescription;

        }



        public String getBadgeIconURL() {

            return badgeIconURL;

        }



        public void setBadgeIconURL(String badgeIconURL) {

            this.badgeIconURL = badgeIconURL;

        }



        public int getXpReward() {

            return xpReward;

        }



        public void setXpReward(int xpReward) {

            this.xpReward = xpReward;

        }



        public String getBadgeCategory() {

            return badgeCategory;

        }



        public void setBadgeCategory(String badgeCategory) {

            this.badgeCategory = badgeCategory;

        }

    }

}