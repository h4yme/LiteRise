package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class Module {

    @SerializedName("ModuleID")
    private int moduleId;

    @SerializedName("ModuleName")
    private String moduleName;

    @SerializedName("ModuleKey")
    private String moduleKey;

    @SerializedName("ModuleDescription")
    private String moduleDescription;

    @SerializedName("IconName")
    private String iconName;

    @SerializedName("ColorCode")
    private String colorCode;

    @SerializedName("OrderIndex")
    private int orderIndex;

    @SerializedName("RequiredScore")
    private float requiredScore;

    @SerializedName("IsActive")
    private boolean isActive;

    // Progress fields (from join)
    @SerializedName("CurrentStepNumber")
    private int currentStepNumber;

    @SerializedName("CompletedSteps")
    private int completedSteps;

    @SerializedName("TotalSteps")
    private int totalSteps;

    @SerializedName("ModuleScore")
    private float moduleScore;

    @SerializedName("IsCompleted")
    private boolean isCompleted;

    @SerializedName("IsLocked")
    private boolean isLocked;

    // Getters and Setters
    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getModuleKey() { return moduleKey; }
    public void setModuleKey(String moduleKey) { this.moduleKey = moduleKey; }

    public String getModuleDescription() { return moduleDescription; }
    public void setModuleDescription(String moduleDescription) { this.moduleDescription = moduleDescription; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public float getRequiredScore() { return requiredScore; }
    public void setRequiredScore(float requiredScore) { this.requiredScore = requiredScore; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getCurrentStepNumber() { return currentStepNumber; }
    public void setCurrentStepNumber(int currentStepNumber) { this.currentStepNumber = currentStepNumber; }

    public int getCompletedSteps() { return completedSteps; }
    public void setCompletedSteps(int completedSteps) { this.completedSteps = completedSteps; }

    public int getTotalSteps() { return totalSteps; }
    public void setTotalSteps(int totalSteps) { this.totalSteps = totalSteps; }

    public float getModuleScore() { return moduleScore; }
    public void setModuleScore(float moduleScore) { this.moduleScore = moduleScore; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public int getProgressPercentage() {
        if (totalSteps == 0) return 0;
        return (int) ((completedSteps * 100.0) / totalSteps);
    }
}
