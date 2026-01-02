package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("student")
    private StudentData student;

    @SerializedName("token")
    private String token;

    @SerializedName("error")
    private String error;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public StudentData getStudent() { return student; }
    public void setStudent(StudentData student) { this.student = student; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    // Convenience method for student ID
    public int getStudentId() {
        return student != null ? student.getStudentID() : 0;
    }
}