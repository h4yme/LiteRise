package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModuleLadderResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("nodes")
    private List<NodeData> nodes;

    @SerializedName("current_node_id")
    private int currentNodeId;

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
        return currentNodeId;
    }

    public String getMessage() {
        return message;
    }
}
