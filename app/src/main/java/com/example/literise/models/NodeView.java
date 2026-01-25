package com.example.literise.models;

public class NodeView {
    private int nodeNumber;
    private float x; // X position as percentage (0-100)
    private float y; // Y position as percentage (0-100)
    private NodeState state;
    private boolean isFinalAssessment;

    public enum NodeState {
        LOCKED,    // Not yet accessible
        UNLOCKED,  // Available to start
        CURRENT,   // Currently working on
        COMPLETED, // Finished
        MASTERED   // Completed with high score
    }

    public NodeView(int nodeNumber, float x, float y, NodeState state, boolean isFinalAssessment) {
        this.nodeNumber = nodeNumber;
        this.x = x;
        this.y = y;
        this.state = state;
        this.isFinalAssessment = isFinalAssessment;
    }

    // Getters
    public int getNodeNumber() {
        return nodeNumber;
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

    public boolean isFinalAssessment() {
        return isFinalAssessment;
    }

    // Setters
    public void setState(NodeState state) {
        this.state = state;
    }
}
