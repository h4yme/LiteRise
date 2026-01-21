package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class SupplementalNodeData {
    @SerializedName("SupplementalNodeID")
    private int id;

    @SerializedName("NodeType")
    private String nodeType;

    @SerializedName("Title")
    private String title;

    @SerializedName("IsVisible")
    private int isVisible;

    public int getId() { return id; }
    public String getNodeType() { return nodeType; }
    public String getTitle() { return title; }
    public boolean isVisible() { return isVisible == 1; }

    public void setId(int id) { this.id = id; }
    public void setNodeType(String nodeType) { this.nodeType = nodeType; }
    public void setTitle(String title) { this.title = title; }
    public void setIsVisible(int isVisible) { this.isVisible = isVisible; }
}
