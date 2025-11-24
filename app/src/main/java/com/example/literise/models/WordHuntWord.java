package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

/**
 * Model for a Word Hunt vocabulary word
 */
public class WordHuntWord {

    @SerializedName("word_id")
    private int wordId;

    @SerializedName("word")
    private String word;

    @SerializedName("definition")
    private String definition;

    @SerializedName("example_sentence")
    private String exampleSentence;

    @SerializedName("difficulty")
    private float difficulty;

    @SerializedName("category")
    private String category;

    @SerializedName("grade_level")
    private int gradeLevel;

    // Transient fields for game state
    private transient boolean found = false;
    private transient int startRow = -1;
    private transient int startCol = -1;
    private transient int endRow = -1;
    private transient int endCol = -1;
    private transient boolean horizontal = true;

    public WordHuntWord() {}

    public WordHuntWord(int wordId, String word, String definition, float difficulty) {
        this.wordId = wordId;
        this.word = word;
        this.definition = definition;
        this.difficulty = difficulty;
    }

    // Getters and Setters
    public int getWordId() {
        return wordId;
    }

    public void setWordId(int wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(float difficulty) {
        this.difficulty = difficulty;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(int gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    // Game state methods
    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getStartCol() {
        return startCol;
    }

    public void setStartCol(int startCol) {
        this.startCol = startCol;
    }

    public int getEndRow() {
        return endRow;
    }

    public void setEndRow(int endRow) {
        this.endRow = endRow;
    }

    public int getEndCol() {
        return endCol;
    }

    public void setEndCol(int endCol) {
        this.endCol = endCol;
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public void setHorizontal(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public void setPosition(int startRow, int startCol, int endRow, int endCol, boolean horizontal) {
        this.startRow = startRow;
        this.startCol = startCol;
        this.endRow = endRow;
        this.endCol = endCol;
        this.horizontal = horizontal;
    }
}
