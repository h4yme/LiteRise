package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LeaderboardResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("leaderboard")
    private List<LeaderboardEntry> leaderboard;

    // Empty constructor
    public LeaderboardResponse() {}

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setLeaderboard(List<LeaderboardEntry> leaderboard) {
        this.leaderboard = leaderboard;
    }
}
