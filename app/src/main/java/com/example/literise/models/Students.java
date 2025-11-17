package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class Students {

    @SerializedName("StudentID")
    private int student_id;

    @SerializedName("FullName")
    private String fullname;

    @SerializedName("GradeLevel")
    private String grade_level;

    @SerializedName("XP")
    private int xp;

    @SerializedName("AbilityScore")
    private float ability_score;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("token")
    private String token;

    // Getters and Setters
    public int getStudent_id() { return student_id; }
    public void setStudent_id(int student_id) { this.student_id = student_id; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getGrade_level() { return grade_level; }
    public void setGrade_level(String grade_level) { this.grade_level = grade_level; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public float getAbility_score() { return ability_score; }
    public void setAbility_score(float ability_score) { this.ability_score = ability_score; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }
}

