package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;



public class Question {



    @SerializedName("ItemID")

    private int itemId;



    @SerializedName("ItemText")

    private String itemText;



    @SerializedName("ItemType")

    private String itemType;



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



    @SerializedName("CorrectAnswer")

    private String correctAnswer;



    @SerializedName("ScrambledWords")

    private List<String> scrambledWords;



    @SerializedName("ImageURL")

    private String imageURL;



    @SerializedName("AudioURL")

    private String audioURL;
    @SerializedName("Difficulty")
    private float difficulty;

    @SerializedName("Phonetic")
    private String phonetic;

    @SerializedName("Definition")
    private String definition;
    @SerializedName("Discrimination")
    private float discrimination;

    @SerializedName("IsMCQ")
    private boolean isMCQ;

    @SerializedName("PronunciationSubtype")
    private String pronunciationSubtype;

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
    public String getItemText() { return itemText; }

    public String getItemType() { return itemType; }

    public String getPassageText() { return passageText; }

    public String getQuestionText() { return questionText; }

    public String getOptionA() { return optionA; }

    public String getOptionB() { return optionB; }

    public String getOptionC() { return optionC; }

    public String getOptionD() { return optionD; }

    public String getCorrectOption() { return correctOption; }

    public String getCorrectAnswer() { return correctAnswer; }

    public List<String> getScrambledWords() { return scrambledWords; }

    public String getImageURL() { return imageURL; }

    public String getAudioURL() { return audioURL; }
    public float getDifficulty() { return difficulty; }
    public float getDiscrimination() { return discrimination; }
    public String getPhonetic() { return phonetic; }

    public String getDefinition() { return definition; }

    // ✅ Setters (useful when building mock questions or local test data)
    public void setItemId(int itemId) { this.itemId = itemId; }
    public void setItemText(String itemText) { this.itemText = itemText; }

    public void setItemType(String itemType) { this.itemType = itemType; }

    public void setPassageText(String passageText) { this.passageText = passageText; }

    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public void setOptionA(String optionA) { this.optionA = optionA; }

    public void setOptionB(String optionB) { this.optionB = optionB; }

    public void setOptionC(String optionC) { this.optionC = optionC; }

    public void setOptionD(String optionD) { this.optionD = optionD; }

    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }

    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public void setScrambledWords(List<String> scrambledWords) { this.scrambledWords = scrambledWords; }

    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public void setAudioURL(String audioURL) { this.audioURL = audioURL; }
    public void setDifficulty(float difficulty) { this.difficulty = difficulty; }
    public void setDiscrimination(float discrimination) { this.discrimination = discrimination; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public void setDefinition(String definition) { this.definition = definition; }

    public boolean isMCQ() { return isMCQ; }

    public void setMCQ(boolean MCQ) { isMCQ = MCQ; }



    public String getPronunciationSubtype() { return pronunciationSubtype; }

    public void setPronunciationSubtype(String pronunciationSubtype) { this.pronunciationSubtype = pronunciationSubtype; }



    // Helper method to check if this is a speak-type pronunciation item

    public boolean isSpeakPronunciation() {

        return "Pronunciation".equalsIgnoreCase(itemType) && "Speak".equalsIgnoreCase(pronunciationSubtype);

    }



    // Helper method to check if this item has MCQ options

    public boolean hasOptions() {

        return isMCQ || (optionA != null && !optionA.isEmpty());

    }
}
