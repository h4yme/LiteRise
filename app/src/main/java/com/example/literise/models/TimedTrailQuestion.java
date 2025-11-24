package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Model for a Timed Trail reading passage with comprehension question
 */
public class TimedTrailQuestion {

    @SerializedName("question_id")
    private int questionId;

    @SerializedName("passage")
    private String passage;

    @SerializedName("question")
    private String question;

    @SerializedName("choices")
    private List<String> choices;

    @SerializedName("correct_answer")
    private int correctAnswer; // Index of correct choice (0-based)

    @SerializedName("difficulty")
    private float difficulty;

    @SerializedName("category")
    private String category;

    @SerializedName("time_limit")
    private int timeLimit; // Seconds allowed for this question

    public TimedTrailQuestion() {}

    public TimedTrailQuestion(int questionId, String passage, String question,
                               List<String> choices, int correctAnswer, float difficulty) {
        this.questionId = questionId;
        this.passage = passage;
        this.question = question;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
        this.difficulty = difficulty;
        this.timeLimit = 30; // Default 30 seconds
    }

    // Getters and Setters
    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public String getPassage() {
        return passage;
    }

    public void setPassage(String passage) {
        this.passage = passage;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(int correctAnswer) {
        this.correctAnswer = correctAnswer;
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

    public int getTimeLimit() {
        return timeLimit > 0 ? timeLimit : 30;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    /**
     * Check if the given answer index is correct
     */
    public boolean isCorrect(int answerIndex) {
        return answerIndex == correctAnswer;
    }

    /**
     * Get word count of the passage for WPM calculation
     */
    public int getWordCount() {
        if (passage == null || passage.isEmpty()) return 0;
        return passage.trim().split("\\s+").length;
    }
}
