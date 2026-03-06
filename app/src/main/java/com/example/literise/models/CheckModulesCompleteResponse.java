package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class CheckModulesCompleteResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("all_complete")
    private boolean allComplete;

    @SerializedName("completed_count")
    private int completedCount;

    @SerializedName("total_count")
    private int totalCount;

    @SerializedName("post_assessment_done")
    private boolean postAssessmentDone;

    @SerializedName("should_trigger_post_assessment")
    private boolean shouldTriggerPostAssessment;

    public boolean isSuccess() { return success; }
    public boolean isAllComplete() { return allComplete; }
    public int getCompletedCount() { return completedCount; }
    public int getTotalCount() { return totalCount; }
    public boolean isPostAssessmentDone() { return postAssessmentDone; }
    public boolean isShouldTriggerPostAssessment() { return shouldTriggerPostAssessment; }
}