package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class Question {

    @SerializedName("ItemID")
    private int itemId;

    @SerializedName("PassageText")
    private String passageText;

    @SerializedName("QuestionText")
    private String questionText;

    @SerializedName("OptionA")
    private String optionA;

    @SerializedName("OptionB")
    private String optionB;

    @SerializedName("OptionC")
    private String optionC;

    @SerializedName("OptionD")
    private String optionD;

    @SerializedName("CorrectOption")
    private String correctOption;

    @SerializedName("Difficulty")
    private float difficulty;

    @SerializedName("Discrimination")
    private float discrimination;

    // ✅ Empty constructor (required for Retrofit / GSON)
    public Question() {}

    // ✅ Full constructor (optional if you want to build manually)
    public Question(int itemId, String passageText, String questionText,
                    String optionA, String optionB, String optionC, String optionD,
                    String correctOption, float difficulty, float discrimination) {
        this.itemId = itemId;
        this.passageText = passageText;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.difficulty = difficulty;
        this.discrimination = discrimination;
    }

    // ✅ Getters
    public int getItemId() { return itemId; }
    public String getPassageText() { return passageText; }
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getOptionD() { return optionD; }
    public String getCorrectOption() { return correctOption; }
    public float getDifficulty() { return difficulty; }
    public float getDiscrimination() { return discrimination; }

    // ✅ Setters (useful when building mock questions or local test data)
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setPassageText(String passageText) { this.passageText = passageText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }
    public void setDifficulty(float difficulty) { this.difficulty = difficulty; }
    public void setDiscrimination(float discrimination) { this.discrimination = discrimination; }
}
