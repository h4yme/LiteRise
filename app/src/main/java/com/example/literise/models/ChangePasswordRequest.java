package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class ChangePasswordRequest {

    @SerializedName("StudentID")
    private int studentId;

    @SerializedName("OldPassword")
    private String oldPassword;

    @SerializedName("NewPassword")
    private String newPassword;

    public ChangePasswordRequest(int studentId, String oldPassword, String newPassword) {
        this.studentId = studentId;
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    // Getters
    public int getStudentId() {
        return studentId;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    // Setters
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
