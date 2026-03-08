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

    @SerializedName("Nickname")
    private String nickname;

    @SerializedName("PreAssessmentCompleted")
    private boolean preAssessmentCompleted;

    @SerializedName("AssessmentStatus")
    private String assessmentStatus;

    @SerializedName("Cat1_PhonicsWordStudy")
    private int cat1PhonicsWordStudy;

    @SerializedName("Cat2_VocabularyWordKnowledge")
    private int cat2VocabularyWordKnowledge;

    @SerializedName("Cat3_GrammarAwareness")
    private int cat3GrammarAwareness;

    @SerializedName("Cat4_ComprehendingText")
    private int cat4ComprehendingText;

    @SerializedName("Cat5_CreatingComposing")
    private int cat5CreatingComposing;

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

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public boolean isPreAssessmentCompleted() { return preAssessmentCompleted; }
    public void setPreAssessmentCompleted(boolean preAssessmentCompleted) { this.preAssessmentCompleted = preAssessmentCompleted; }

    public String getAssessmentStatus() { return assessmentStatus; }
    public void setAssessmentStatus(String assessmentStatus) { this.assessmentStatus = assessmentStatus; }

    public int getCat1PhonicsWordStudy() { return cat1PhonicsWordStudy; }
    public int getCat2VocabularyWordKnowledge() { return cat2VocabularyWordKnowledge; }
    public int getCat3GrammarAwareness() { return cat3GrammarAwareness; }
    public int getCat4ComprehendingText() { return cat4ComprehendingText; }
    public int getCat5CreatingComposing() { return cat5CreatingComposing; }
}