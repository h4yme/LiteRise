package com.example.literise.models;

public class LeaderboardEntry {
    private int rank;
    private int studentId;
    private String nickname;
    private int totalXP;
    private int badgeCount;
    private boolean isCurrentUser;

    public LeaderboardEntry() {
    }

    public LeaderboardEntry(int rank, int studentId, String nickname, int totalXP, int badgeCount) {
        this.rank = rank;
        this.studentId = studentId;
        this.nickname = nickname;
        this.totalXP = totalXP;
        this.badgeCount = badgeCount;
        this.isCurrentUser = false;
    }

    // Getters and setters
    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getTotalXP() {
        return totalXP;
    }

    public void setTotalXP(int totalXP) {
        this.totalXP = totalXP;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        isCurrentUser = currentUser;
    }

    public String getRankEmoji() {
        switch (rank) {
            case 1:
                return "🥇";
            case 2:
                return "🥈";
            case 3:
                return "🥉";
            default:
                return String.valueOf(rank);
        }
    }
}
