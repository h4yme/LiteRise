package com.example.literise.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlacementQuestion {
    private int questionId;
    private int category;           // 1-4 (Oral, Word, Reading, Grammar)
    private String subcategory;     // e.g., "Listening", "Vocabulary"
    private String questionType;    // "multiple_choice", "listening", "recording", "image_selection"
    private String questionText;
    private String audioUrl;        // For listening questions
    private String imageUrl;        // For image-based questions
    private List<String> options;   // Answer options
    private String correctAnswer;
    private double difficulty;      // b parameter (-3 to 3)
    private double discrimination;  // a parameter (0.5 to 2.5)
    private String leoHint;         // Leo's hint for this question

    public PlacementQuestion() {
        this.options = new ArrayList<>();
    }

    public PlacementQuestion(int questionId, int category, String subcategory, 
                           String questionType, String questionText, String audioUrl,
                           String imageUrl, List<String> options, String correctAnswer,
                           double difficulty, double discrimination, String leoHint) {
        this.questionId = questionId;
        this.category = category;
        this.subcategory = subcategory;
        this.questionType = questionType;
        this.questionText = questionText;
        this.audioUrl = audioUrl;
        this.imageUrl = imageUrl;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.difficulty = difficulty;
        this.discrimination = discrimination;
        this.leoHint = leoHint;
    }

    // Getters and Setters
    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public int getCategory() { return category; }
    public void setCategory(int category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public double getDifficulty() { return difficulty; }
    public void setDifficulty(double difficulty) { this.difficulty = difficulty; }

    public double getDiscrimination() { return discrimination; }
    public void setDiscrimination(double discrimination) { this.discrimination = discrimination; }

    public String getLeoHint() { return leoHint; }
    public void setLeoHint(String leoHint) { this.leoHint = leoHint; }

    // Helper method to convert options list to JSON string
    public String getOptionsAsJson() {
        JSONArray jsonArray = new JSONArray(options);
        return jsonArray.toString();
    }

    // Helper method to parse options from JSON string
    public static List<String> parseOptionsFromJson(String json) {
        List<String> options = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                options.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return options;
    }

    // Calculate probability of correct answer using 2PL IRT model
    public double calculateProbability(double theta) {
        // 2PL IRT Model: P(θ) = 1 / (1 + e^(-a(θ - b)))
        // a = discrimination, b = difficulty, θ = ability
        return 1.0 / (1.0 + Math.exp(-discrimination * (theta - difficulty)));
    }
}
