package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ModuleLadderResponse {
    @SerializedName("nodes")
    private List<NodeData> nodes;

    @SerializedName("supplementalNodes")
    private List<SupplementalNodeData> supplementalNodes;

    @SerializedName("currentNodeId")
    private int currentNodeId;

    @SerializedName("placementLevel")
    private int placementLevel;

    @SerializedName("success")
    private boolean success;

    public List<NodeData> getNodes() { return nodes; }
    public List<SupplementalNodeData> getSupplementalNodes() { return supplementalNodes; }
    public int getCurrentNodeId() { return currentNodeId; }
    public int getPlacementLevel() { return placementLevel; }
    public boolean isSuccess() { return success; }

    public void setNodes(List<NodeData> nodes) { this.nodes = nodes; }
    public void setSupplementalNodes(List<SupplementalNodeData> supplementalNodes) {
        this.supplementalNodes = supplementalNodes;
    }
    public void setCurrentNodeId(int currentNodeId) { this.currentNodeId = currentNodeId; }
    public void setPlacementLevel(int placementLevel) { this.placementLevel = placementLevel; }
    public void setSuccess(boolean success) { this.success = success; }
}
