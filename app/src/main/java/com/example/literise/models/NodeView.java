package com.example.literise.models;

public class NodeView {
    private int nodeId;          // Database node ID
    private int nodeNumber;      // Display number (1-13)
    private String title;        // Lesson title
    private float x;             // X position as percentage (0-100)
    private float y;             // Y position as percentage (0-100)
    private NodeState state;
    private int quarter;         // Quarter number (1-4, or 0 for final assessment)
    private boolean isFinalAssessment;

    public enum NodeState {
        LOCKED,    // Not yet accessible
        UNLOCKED,  // Available to start
        CURRENT,   // Currently working on
        COMPLETED, // Finished
        MASTERED   // Completed with high score
    }

    public NodeView(int nodeId, int nodeNumber, String title, NodeState state, float x, float y, int quarter, boolean isFinalAssessment) {
        this.nodeId = nodeId;
        this.nodeNumber = nodeNumber;
        this.title = title;
        this.state = state;
        this.x = x;
        this.y = y;
        this.quarter = quarter;
        this.isFinalAssessment = isFinalAssessment;
    }

    // Getters
    public int getNodeId() {
        return nodeId;
    }

    public int getNodeNumber() {
        return nodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public NodeState getState() {
        return state;
    }

    public int getQuarter() {
        return quarter;
    }

    public boolean isFinalAssessment() {
        return isFinalAssessment;
    }

    // Setters
    public void setState(NodeState state) {
        this.state = state;
    }
}
