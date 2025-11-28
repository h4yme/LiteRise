package com.example.literise.models;

public class Module {
    private int moduleId;
    private String moduleName;
    private String description;
    private String iconName;
    private int displayOrder;
    private String colorGradient; // e.g., "blue", "orange", "purple", "green", "pink"
    private int totalSteps;
    private boolean isLocked;
    private int completedSteps;
    private double progressPercent;
    private boolean isPriority; // True if this is the priority module from assessment

    public Module() {
    }

    public Module(int moduleId, String moduleName, String description, String iconName,
                  int displayOrder, String colorGradient, int totalSteps) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.description = description;
        this.iconName = iconName;
        this.displayOrder = displayOrder;
        this.colorGradient = colorGradient;
        this.totalSteps = totalSteps;
        this.isLocked = true;
        this.completedSteps = 0;
        this.progressPercent = 0.0;
        this.isPriority = false;
    }

    // Getters and setters
    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(String colorGradient) {
        this.colorGradient = colorGradient;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public int getCompletedSteps() {
        return completedSteps;
    }

    public void setCompletedSteps(int completedSteps) {
        this.completedSteps = completedSteps;
        updateProgressPercent();
    }

    public double getProgressPercent() {
        return progressPercent;
    }

    public void setProgressPercent(double progressPercent) {
        this.progressPercent = progressPercent;
    }

    public boolean isPriority() {
        return isPriority;
    }

    public void setPriority(boolean priority) {
        isPriority = priority;
    }

    private void updateProgressPercent() {
        if (totalSteps > 0) {
            this.progressPercent = ((double) completedSteps / totalSteps) * 100.0;
        }
    }

    public boolean isComplete() {
        return completedSteps >= totalSteps && totalSteps > 0;
    }

    public String getProgressText() {
        return completedSteps + "/" + totalSteps + " steps completed";
    }
}
