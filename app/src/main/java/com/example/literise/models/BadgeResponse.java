package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class BadgeResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("badges")
    private List<Badge> badges;

    @SerializedName("StudentID")
    private int studentId;

    // Empty constructor
    public BadgeResponse() {}

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<Badge> getBadges() {
        return badges;
    }

    public int getStudentId() {
        return studentId;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setBadges(List<Badge> badges) {
        this.badges = badges;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }
}
