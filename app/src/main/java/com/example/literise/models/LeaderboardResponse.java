package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LeaderboardResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("filter")
    private String filter;

    @SerializedName("filter_label")
    private String filterLabel;

    @SerializedName("leaderboard")
    private List<LeaderboardEntry> leaderboard;

    public boolean isSuccess()                      { return success; }
    public String getFilter()                       { return filter; }
    public String getFilterLabel()                  { return filterLabel; }
    public List<LeaderboardEntry> getLeaderboard()  { return leaderboard; }
}
