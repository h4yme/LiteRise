package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("gender")
    private String gender;

    @SerializedName("school_id")
    private int schoolId;

    @SerializedName("grade_level")
    private String gradeLevel;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    // Constructor
    public RegisterRequest(String nickname, String firstName, String lastName,
                           String birthday, String gender, int schoolId,
                           String gradeLevel, String email, String password) {
        this.nickname = nickname;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthday = birthday;
        this.gender = gender;
        this.schoolId = schoolId;
        this.gradeLevel = gradeLevel;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public int getSchoolId() { return schoolId; }
    public void setSchoolId(int schoolId) { this.schoolId = schoolId; }

    public String getGradeLevel() { return gradeLevel; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}