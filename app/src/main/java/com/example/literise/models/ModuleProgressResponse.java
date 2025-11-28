package com.example.literise.models;

import java.util.List;

public class ModuleProgressResponse {
    private boolean success;
    private String message;
    private List<Module> modules;
    private int priorityModuleId;
    private StudentInfo student;

    public ModuleProgressResponse() {
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

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    public int getPriorityModuleId() {
        return priorityModuleId;
    }

    public void setPriorityModuleId(int priorityModuleId) {
        this.priorityModuleId = priorityModuleId;
    }

    public StudentInfo getStudent() {
        return student;
    }

    public void setStudent(StudentInfo student) {
        this.student = student;
    }

    // Nested class for student info
    public static class StudentInfo {
        private int studentId;
        private String nickname;
        private int totalXP;
        private int currentStreak;
        private int badgeCount;

        public StudentInfo() {
        }

        public int getStudentId() {
            return studentId;
        }

        public void setStudentId(int studentId) {
            this.studentId = studentId;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public int getTotalXP() {
            return totalXP;
        }

        public void setTotalXP(int totalXP) {
            this.totalXP = totalXP;
        }

        public int getCurrentStreak() {
            return currentStreak;
        }

        public void setCurrentStreak(int currentStreak) {
            this.currentStreak = currentStreak;
        }

        public int getBadgeCount() {
            return badgeCount;
        }

        public void setBadgeCount(int badgeCount) {
            this.badgeCount = badgeCount;
        }
    }
}
