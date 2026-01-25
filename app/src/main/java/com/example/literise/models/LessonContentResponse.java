package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class LessonContentResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("lesson")
    private Lesson lesson;

    @SerializedName("pacing")
    private Pacing pacing;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public Lesson getLesson() { return lesson; }
    public Pacing getPacing() { return pacing; }
    public String getMessage() { return message; }

    public static class Lesson {
        @SerializedName("node_id")
        private int nodeId;

        @SerializedName("node_number")
        private int nodeNumber;

        @SerializedName("title")
        private String title;

        @SerializedName("objective")
        private String objective;

        @SerializedName("content")
        private String content;

        @SerializedName("module_id")
        private int moduleId;

        @SerializedName("module_name")
        private String moduleName;

        @SerializedName("quarter")
        private int quarter;

        @SerializedName("is_final_assessment")
        private boolean isFinalAssessment;

        public int getNodeId() { return nodeId; }
        public int getNodeNumber() { return nodeNumber; }
        public String getTitle() { return title; }
        public String getObjective() { return objective; }
        public String getContent() { return content; }
        public int getModuleId() { return moduleId; }
        public String getModuleName() { return moduleName; }
        public int getQuarter() { return quarter; }
        public boolean isFinalAssessment() { return isFinalAssessment; }
    }

    public static class Pacing {
        @SerializedName("speed")
        private String speed;

        @SerializedName("scaffolding")
        private String scaffolding;

        @SerializedName("description")
        private String description;

        public String getSpeed() { return speed; }
        public String getScaffolding() { return scaffolding; }
        public String getDescription() { return description; }
    }
}
