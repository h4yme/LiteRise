package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class StudentData {

    @SerializedName("StudentID")
    private int studentID;

    @SerializedName("Nickname")
    private String nickname;

    @SerializedName("FirstName")
    private String firstName;

    @SerializedName("LastName")
    private String lastName;

    @SerializedName("FullName")
    private String fullName;

    @SerializedName("Email")
    private String email;

    @SerializedName("Birthday")
    private String birthday;

    @SerializedName("Gender")
    private String gender;

    @SerializedName("GradeLevel")
    private int gradeLevel;

    @SerializedName("SchoolID")
    private Integer schoolID;

    @SerializedName("Section")
    private String section;

    @SerializedName("CurrentAbility")
    private double currentAbility;

    @SerializedName("AbilityScore")
    private double abilityScore;

    @SerializedName("TotalXP")
    private int totalXP;

    @SerializedName("XP")
    private int xp;

    @SerializedName("CurrentStreak")
    private int currentStreak;

    @SerializedName("LongestStreak")
    private int longestStreak;

    @SerializedName("DateCreated")
    private String dateCreated;

    @SerializedName("IsActive")
    private boolean isActive;

    // Getters
    public int getStudentID() { return studentID; }
    public String getNickname() { return nickname; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getBirthday() { return birthday; }
    public String getGender() { return gender; }
    public int getGradeLevel() { return gradeLevel; }
    public Integer getSchoolID() { return schoolID; }
    public String getSection() { return section; }
    public double getCurrentAbility() { return currentAbility; }
    public double getAbilityScore() { return abilityScore; }
    public int getTotalXP() { return totalXP; }
    public int getXp() { return xp; }
    public int getCurrentStreak() { return currentStreak; }
    public int getLongestStreak() { return longestStreak; }
    public String getDateCreated() { return dateCreated; }
    public boolean isActive() { return isActive; }

    // Setters
    public void setStudentID(int studentID) { this.studentID = studentID; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public void setGender(String gender) { this.gender = gender; }
    public void setGradeLevel(int gradeLevel) { this.gradeLevel = gradeLevel; }
    public void setSchoolID(Integer schoolID) { this.schoolID = schoolID; }
    public void setSection(String section) { this.section = section; }
    public void setCurrentAbility(double currentAbility) { this.currentAbility = currentAbility; }
    public void setAbilityScore(double abilityScore) { this.abilityScore = abilityScore; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }
    public void setXp(int xp) { this.xp = xp; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public void setLongestStreak(int longestStreak) { this.longestStreak = longestStreak; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    public void setActive(boolean active) { isActive = active; }
}
