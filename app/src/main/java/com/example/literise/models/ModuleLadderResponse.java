package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModuleLadderResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("nodes")
    private List<NodeData> nodes;

    @SerializedName("currentNodeId")
    private Integer currentNodeId;  // Changed to Integer to handle null

    @SerializedName("placementLevel")
    private Integer placementLevel;

    @SerializedName("message")
    private String message;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public List<NodeData> getNodes() {
        return nodes;
    }

    public int getCurrentNodeId() {
        return currentNodeId != null ? currentNodeId : 0;
    }

    public Integer getPlacementLevel() {
        return placementLevel;
    }

    public String getMessage() {
        return message;
    }
}
