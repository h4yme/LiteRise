package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {

    @SerializedName("Rank")
    private int rank;

    @SerializedName("StudentID")
    private int studentId;

    @SerializedName("FullName")
    private String fullName;

    @SerializedName("FirstName")
    private String firstName;

    @SerializedName("GradeLevel")
    private int gradeLevel;

    @SerializedName("Section")
    private String section;

    @SerializedName("TotalXP")
    private int totalXP;

    @SerializedName("CurrentStreak")
    private int currentStreak;

    @SerializedName("LongestStreak")
    private int longestStreak;

    @SerializedName("AbilityScore")
    private float abilityScore;

    // Empty constructor
    public LeaderboardEntry() {}

    // Getters
    public int getRank() {
        return rank;
    }

    public int getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public String getSection() {
        return section;
    }

    public int getTotalXP() {
        return totalXP;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public float getAbilityScore() {
        return abilityScore;
    }

    // Setters
    public void setRank(int rank) {
        this.rank = rank;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setGradeLevel(int gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setTotalXP(int totalXP) {
        this.totalXP = totalXP;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public void setAbilityScore(float abilityScore) {
        this.abilityScore = abilityScore;
    }
}
