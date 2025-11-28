package com.example.literise.models;

import java.util.List;

public class ModuleLadderResponse {
    private boolean success;
    private String message;
    private Module module;
    private List<ModuleLadderStep> steps;
    private boolean moduleTestUnlocked;

    public ModuleLadderResponse() {
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public List<ModuleLadderStep> getSteps() {
        return steps;
    }

    public void setSteps(List<ModuleLadderStep> steps) {
        this.steps = steps;
    }

    public boolean isModuleTestUnlocked() {
        return moduleTestUnlocked;
    }

    public void setModuleTestUnlocked(boolean moduleTestUnlocked) {
        this.moduleTestUnlocked = moduleTestUnlocked;
    }
}
