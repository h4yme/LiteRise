package com.example.literise.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScrambledSentence {

    private int id;
    private String correctSentence;
    private List<String> words;
    private List<String> scrambledWords;
    private String difficulty; // "easy", "medium", "hard"

    public ScrambledSentence(int id, String correctSentence, String difficulty) {
        this.id = id;
        this.correctSentence = correctSentence;
        this.difficulty = difficulty;

        // Split sentence into words
        this.words = new ArrayList<>();
        String[] parts = correctSentence.split("\\s+");
        for (String part : parts) {
            words.add(part);
        }

        // Create scrambled version
        this.scrambledWords = new ArrayList<>(words);
        Collections.shuffle(scrambledWords);
    }

    public boolean checkAnswer(List<String> userAnswer) {
        if (userAnswer.size() != words.size()) {
            return false;
        }

        for (int i = 0; i < words.size(); i++) {
            if (!words.get(i).equals(userAnswer.get(i))) {
                return false;
            }
        }

        return true;
    }

    public int getXpReward() {
        switch (difficulty.toLowerCase()) {
            case "easy":
                return 50;
            case "medium":
                return 75;
            case "hard":
                return 100;
            default:
                return 50;
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getCorrectSentence() {
        return correctSentence;
    }

    public List<String> getWords() {
        return words;
    }

    public List<String> getScrambledWords() {
        return scrambledWords;
    }

    public String getDifficulty() {
        return difficulty;
    }
}
