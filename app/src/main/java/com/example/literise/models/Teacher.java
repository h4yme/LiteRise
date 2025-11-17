package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class Teacher {

    @SerializedName("TeacherID")
    private int teacherId;

    @SerializedName("FullName")
    private String fullName;

    @SerializedName("FirstName")
    private String firstName;

    @SerializedName("LastName")
    private String lastName;

    @SerializedName("email")
    private String email;

    @SerializedName("Department")
    private String department;

    @SerializedName("StudentCount")
    private int studentCount;

    // For login request
    @SerializedName("password")
    private String password;

    // Empty constructor
    public Teacher() {}

    // Constructor for login
    public Teacher(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters
    public int getTeacherId() {
        return teacherId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public int getStudentCount() {
        return studentCount;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setStudentCount(int studentCount) {
        this.studentCount = studentCount;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
