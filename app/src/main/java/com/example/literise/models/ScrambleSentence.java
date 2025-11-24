package com.example.literise.models;



import com.google.gson.annotations.SerializedName;



import java.util.ArrayList;

import java.util.Arrays;

import java.util.Collections;

import java.util.List;



/**

 * Model class representing a scrambled sentence for the Sentence Scramble game

 */

public class ScrambleSentence {



    @SerializedName("SentenceID")

    private int sentenceId;



    @SerializedName("CorrectSentence")

    private String correctSentence;



    @SerializedName("ScrambledWords")

    private List<String> scrambledWords;



    @SerializedName("Difficulty")

    private float difficulty;



    @SerializedName("Category")

    private String category;



    @SerializedName("GradeLevel")

    private int gradeLevel;



    // Empty constructor for Gson

    public ScrambleSentence() {}



    // Constructor for fallback sentences

    public ScrambleSentence(int sentenceId, String correctSentence, float difficulty) {

        this.sentenceId = sentenceId;

        this.correctSentence = correctSentence;

        this.difficulty = difficulty;

        this.scrambledWords = null; // Will be generated on demand

    }



    // Full constructor

    public ScrambleSentence(int sentenceId, String correctSentence, List<String> scrambledWords,

                            float difficulty, String category, int gradeLevel) {

        this.sentenceId = sentenceId;

        this.correctSentence = correctSentence;

        this.scrambledWords = scrambledWords;

        this.difficulty = difficulty;

        this.category = category;

        this.gradeLevel = gradeLevel;

    }



    // Getters

    public int getSentenceId() {

        return sentenceId;

    }



    public String getCorrectSentence() {

        return correctSentence;

    }



    /**

     * Get the scrambled words for display

     * If scrambledWords is null, generates from correctSentence

     */

    public List<String> getScrambledWords() {

        if (scrambledWords == null || scrambledWords.isEmpty()) {

            scrambledWords = generateScrambledWords();

        }

        return scrambledWords;

    }



    /**

     * Get the words in correct order

     */

    public List<String> getWords() {

        if (correctSentence == null || correctSentence.isEmpty()) {

            return new ArrayList<>();

        }

        // Remove punctuation from individual words for comparison

        String cleaned = correctSentence.replaceAll("[.!?,;:]", "");

        return Arrays.asList(cleaned.split("\\s+"));

    }



    public float getDifficulty() {

        return difficulty;

    }



    public String getCategory() {

        return category;

    }



    public int getGradeLevel() {

        return gradeLevel;

    }



    // Setters

    public void setSentenceId(int sentenceId) {

        this.sentenceId = sentenceId;

    }



    public void setCorrectSentence(String correctSentence) {

        this.correctSentence = correctSentence;

    }



    public void setScrambledWords(List<String> scrambledWords) {

        this.scrambledWords = scrambledWords;

    }



    public void setDifficulty(float difficulty) {

        this.difficulty = difficulty;

    }



    public void setCategory(String category) {

        this.category = category;

    }



    public void setGradeLevel(int gradeLevel) {

        this.gradeLevel = gradeLevel;

    }



    /**

     * Generate scrambled words from the correct sentence

     * Ensures the scrambled order is different from correct order

     */

    private List<String> generateScrambledWords() {

        if (correctSentence == null || correctSentence.isEmpty()) {

            return new ArrayList<>();

        }



        // Split and clean

        String cleaned = correctSentence.replaceAll("[.!?,;:]", "");

        List<String> words = new ArrayList<>(Arrays.asList(cleaned.split("\\s+")));

        List<String> original = new ArrayList<>(words);



        // Keep shuffling until we get a different order

        int attempts = 0;

        do {

            Collections.shuffle(words);

            attempts++;

        } while (words.equals(original) && attempts < 10 && words.size() > 1);



        return words;

    }



    /**

     * Check if the provided answer matches the correct sentence

     */

    public boolean checkAnswer(List<String> answer) {

        List<String> correctWords = getWords();



        if (answer.size() != correctWords.size()) {

            return false;

        }



        for (int i = 0; i < answer.size(); i++) {

            if (!answer.get(i).equalsIgnoreCase(correctWords.get(i))) {

                return false;

            }

        }

        return true;

    }



    /**

     * Check if the provided answer string matches the correct sentence

     */

    public boolean checkAnswer(String answer) {

        if (answer == null || correctSentence == null) {

            return false;

        }



        // Clean both strings for comparison

        String cleanedAnswer = answer.replaceAll("[.!?,;:]", "").trim().toLowerCase();

        String cleanedCorrect = correctSentence.replaceAll("[.!?,;:]", "").trim().toLowerCase();



        return cleanedAnswer.equals(cleanedCorrect);

    }



    /**

     * Get the number of words in the sentence

     */

    public int getWordCount() {

        return getWords().size();

    }

}