package com.example.literise.utils;

import com.example.literise.models.PlacementQuestion;

import java.util.ArrayList;
import java.util.List;

public class IRTEngine {

    private double theta; // Student ability estimate
    private List<PlacementQuestion> answeredQuestions;
    private List<Boolean> answerResults;

    // Constants
    private static final double INITIAL_THETA = 0.0;
    private static final double THETA_MIN = -3.0;
    private static final double THETA_MAX = 3.0;
    private static final double LEARNING_RATE = 0.3;

    public IRTEngine() {
        this.theta = INITIAL_THETA;
        this.answeredQuestions = new ArrayList<>();
        this.answerResults = new ArrayList<>();
    }

    /**
     * Select the next best question using Maximum Information criterion
     */
    public PlacementQuestion selectNextQuestion(List<PlacementQuestion> availableQuestions) {
        if (availableQuestions == null || availableQuestions.isEmpty()) {
            return null;
        }

        PlacementQuestion bestQuestion = null;
        double maxInformation = 0.0;

        for (PlacementQuestion question : availableQuestions) {
            // Skip already answered questions
            if (isQuestionAnswered(question)) {
                continue;
            }

            // Calculate information for this question at current theta
            double information = calculateInformation(question, theta);

            if (information > maxInformation) {
                maxInformation = information;
                bestQuestion = question;
            }
        }

        // If no unanswered questions, return first available
        if (bestQuestion == null && !availableQuestions.isEmpty()) {
            bestQuestion = availableQuestions.get(0);
        }

        return bestQuestion;
    }

    /**
     * Calculate Fisher Information for a question at given theta
     * Information = a^2 * P(θ) * Q(θ)
     */
    private double calculateInformation(PlacementQuestion question, double theta) {
        double a = question.getDiscrimination();
        double b = question.getDifficulty();

        // Calculate probability of correct answer
        double p = 1.0 / (1.0 + Math.exp(-a * (theta - b)));
        double q = 1.0 - p;

        // Fisher Information
        return a * a * p * q;
    }

    /**
     * Update theta estimate after answering a question
     * Uses simple weighted update based on answer correctness
     */
    public void updateTheta(PlacementQuestion question, boolean isCorrect) {
        // Store the question and result
        answeredQuestions.add(question);
        answerResults.add(isCorrect);

        double a = question.getDiscrimination();
        double b = question.getDifficulty();

        // Calculate expected probability of correct answer
        double expectedP = question.calculateProbability(theta);

        // Calculate error
        double error = (isCorrect ? 1.0 : 0.0) - expectedP;

        // Update theta using gradient ascent
        // Δθ = learning_rate * a * error
        double deltaTheta = LEARNING_RATE * a * error;

        // Update theta with bounds checking
        theta = Math.max(THETA_MIN, Math.min(THETA_MAX, theta + deltaTheta));
    }

    /**
     * Check if a question has already been answered
     */
    private boolean isQuestionAnswered(PlacementQuestion question) {
        for (PlacementQuestion answered : answeredQuestions) {
            if (answered.getQuestionId() == question.getQuestionId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Calculate final placement level based on theta
     */
    public int calculatePlacementLevel() {
        // Map theta to reading levels 1-4
        if (theta < -1.0) {
            return 1; // Beginner
        } else if (theta < 0.0) {
            return 2; // Early Reader
        } else if (theta < 1.0) {
            return 3; // Developing Reader
        } else {
            return 4; // Fluent Reader
        }
    }

    /**
     * Get placement level name
     */
    public String getPlacementLevelName() {
        int level = calculatePlacementLevel();
        switch (level) {
            case 1: return "Beginner Reader";
            case 2: return "Early Reader";
            case 3: return "Developing Reader";
            case 4: return "Fluent Reader";
            default: return "Unknown";
        }
    }

    /**
     * Calculate accuracy percentage
     */
    public double getAccuracyPercentage() {
        if (answerResults.isEmpty()) return 0.0;

        int correct = 0;
        for (boolean result : answerResults) {
            if (result) correct++;
        }

        return (correct * 100.0) / answerResults.size();
    }

    /**
     * Get category performance breakdown (5 categories)
     */
    public int[] getCategoryScores() {
        int[] scores = new int[5]; // 5 categories
        int[] counts = new int[5];

        for (int i = 0; i < answeredQuestions.size(); i++) {
            PlacementQuestion q = answeredQuestions.get(i);
            boolean correct = answerResults.get(i);
            int category = q.getCategory() - 1; // 0-indexed

            if (category >= 0 && category < 5) {
                if (correct) scores[category]++;
                counts[category]++;
            }
        }

        // Convert to percentages
        for (int i = 0; i < 5; i++) {
            if (counts[i] > 0) {
                scores[i] = (scores[i] * 100) / counts[i];
            }
        }

        return scores;
    }

    // Getters
    public double getTheta() {
        return theta;
    }

    /**
     * Set theta directly (used when syncing with API's theta estimate)
     */
    public void setTheta(double theta) {
        this.theta = Math.max(THETA_MIN, Math.min(THETA_MAX, theta));
    }

    public int getTotalAnswered() {
        return answeredQuestions.size();
    }

    public int getTotalCorrect() {
        int correct = 0;
        for (boolean result : answerResults) {
            if (result) correct++;
        }
        return correct;
    }

    public List<PlacementQuestion> getAnsweredQuestions() {
        return answeredQuestions;
    }

    public List<Boolean> getAnswerResults() {
        return answerResults;
    }

    /**
     * Reset the engine for a new test
     */
    public void reset() {
        theta = INITIAL_THETA;
        answeredQuestions.clear();
        answerResults.clear();
    }
}