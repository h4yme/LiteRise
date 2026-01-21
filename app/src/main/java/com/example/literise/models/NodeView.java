package com.example.literise.models;

import com.example.literise.R;

public class NodeView {
    private int nodeId;
    private int nodeNumber;
    private String title;
    private NodeState state;
    private float x, y; // Position percentages
    private int quarter;
    private boolean isFinalAssessment;
    private int progressPercentage;

    public enum NodeState {
        LOCKED, UNLOCKED, CURRENT, COMPLETED, MASTERED
    }

    public NodeView(int nodeId, int nodeNumber, String title, NodeState state,
                    float x, float y, int quarter, boolean isFinalAssessment) {
        this.nodeId = nodeId;
        this.nodeNumber = nodeNumber;
        this.title = title;
        this.state = state;
        this.x = x;
        this.y = y;
        this.quarter = quarter;
        this.isFinalAssessment = isFinalAssessment;
        this.progressPercentage = 0;
    }

    public int getDrawableResource() {
        switch (state) {
            case LOCKED:
                return R.drawable.node_locked;
            case UNLOCKED:
                return R.drawable.node_unlocked;
            case CURRENT:
                return R.drawable.node_current;
            case COMPLETED:
                return R.drawable.node_completed;
            case MASTERED:
                return R.drawable.node_mastered;
            default:
                return R.drawable.node_locked;
        }
    }

    // Getters
    public int getNodeId() { return nodeId; }
    public int getNodeNumber() { return nodeNumber; }
    public String getTitle() { return title; }
    public NodeState getState() { return state; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int getQuarter() { return quarter; }
    public boolean isFinalAssessment() { return isFinalAssessment; }
    public int getProgressPercentage() { return progressPercentage; }

    // Setters
    public void setState(NodeState state) { this.state = state; }
    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }
}
