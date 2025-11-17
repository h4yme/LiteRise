package com.example.literise.utils;

import com.example.literise.models.Question;
import com.example.literise.models.ResponseModel;

import java.util.List;

/**
 * LiteRise - Item Response Theory (IRT) Calculator for Android
 * 3-Parameter Logistic (3PL) Model Implementation
 *
 * P(θ) = c + (1 - c) / (1 + e^(-a(θ - b)))
 *
 * Where:
 * - θ (theta) = Student ability
 * - a = Discrimination parameter
 * - b = Difficulty parameter
 * - c = Guessing parameter
 */
public class IRTCalculator {

    /**
     * Calculate probability of correct response using 3PL model
     *
     * @param theta Student ability
     * @param a Discrimination parameter
     * @param b Difficulty parameter
     * @param c Guessing parameter
     * @return Probability (0-1)
     */
    public static double calculateProbability(double theta, double a, double b, double c) {
        double exponent = -a * (theta - b);
        return c + ((1 - c) / (1 + Math.exp(exponent)));
    }

    /**
     * Calculate information function I(θ) for an item
     * Measures how much information an item provides at ability level θ
     *
     * @param theta Student ability
     * @param a Discrimination parameter
     * @param b Difficulty parameter
     * @param c Guessing parameter
     * @return Information value
     */
    public static double itemInformation(double theta, double a, double b, double c) {
        double p = calculateProbability(theta, a, b, c);
        double q = 1 - p;

        // I(θ) = a² * Q(θ)² / P(θ)
        // Where Q(θ) = (P(θ) - c) / (1 - c)
        double qTheta = (p - c) / (1 - c);
        return (a * a * qTheta * qTheta) / p;
    }

    /**
     * Update student ability using Maximum Likelihood Estimation (MLE)
     * Uses Newton-Raphson method for theta estimation
     *
     * @param currentTheta Current ability estimate
     * @param responses List of student responses
     * @param questions List of questions
     * @return Updated theta
     */
    public static double updateAbility(double currentTheta, List<ResponseModel> responses, List<Question> questions) {
        return updateAbility(currentTheta, responses, questions, Constants.MAX_IRT_ITERATIONS, Constants.THETA_TOLERANCE);
    }

    /**
     * Update student ability using Maximum Likelihood Estimation (MLE)
     * Uses Newton-Raphson method for theta estimation
     *
     * @param currentTheta Current ability estimate
     * @param responses List of student responses
     * @param questions List of questions
     * @param maxIterations Maximum iterations
     * @param tolerance Convergence tolerance
     * @return Updated theta
     */
    public static double updateAbility(double currentTheta, List<ResponseModel> responses,
                                       List<Question> questions, int maxIterations, double tolerance) {
        double theta = currentTheta;

        for (int iter = 0; iter < maxIterations; iter++) {
            double firstDerivative = 0.0;  // L'(θ)
            double secondDerivative = 0.0; // L''(θ)

            for (int i = 0; i < responses.size() && i < questions.size(); i++) {
                ResponseModel response = responses.get(i);
                Question question = questions.get(i);

                double a = question.getDiscrimination();
                double b = question.getDifficulty();
                double c = question.getGuessing();
                int u = response.isCorrect() ? 1 : 0; // 1 if correct, 0 if incorrect

                double p = calculateProbability(theta, a, b, c);
                double q = 1 - p;

                // First derivative components
                double pPrime = a * p * q / (1 - c);
                firstDerivative += (u - p) * pPrime / (p * q);

                // Second derivative components
                double pDoublePrime = a * a * p * q * (q - p) / ((1 - c) * (1 - c));
                secondDerivative += (pDoublePrime * (u - p) - pPrime * pPrime * (1 - 2 * p)) / (p * q);
            }

            // Newton-Raphson update
            if (secondDerivative == 0) {
                break;
            }

            double thetaNew = theta - (firstDerivative / secondDerivative);

            // Check convergence
            if (Math.abs(thetaNew - theta) < tolerance) {
                theta = thetaNew;
                break;
            }

            theta = thetaNew;

            // Constrain theta to reasonable range
            theta = Math.max(Constants.MIN_THETA, Math.min(Constants.MAX_THETA, theta));
        }

        return Math.round(theta * 10000.0) / 10000.0; // Round to 4 decimal places
    }

    /**
     * Calculate standard error of ability estimate
     *
     * @param theta Ability estimate
     * @param questions List of administered questions
     * @return Standard error
     */
    public static double calculateStandardError(double theta, List<Question> questions) {
        double totalInformation = 0.0;

        for (Question question : questions) {
            double a = question.getDiscrimination();
            double b = question.getDifficulty();
            double c = question.getGuessing();

            totalInformation += itemInformation(theta, a, b, c);
        }

        return totalInformation > 0 ? Math.round((1 / Math.sqrt(totalInformation)) * 10000.0) / 10000.0 : 9999;
    }

    /**
     * Select next best item using Maximum Information criterion
     *
     * @param theta Current ability estimate
     * @param availableQuestions Pool of unused questions
     * @return Best question to administer (or null if none available)
     */
    public static Question selectNextItem(double theta, List<Question> availableQuestions) {
        if (availableQuestions == null || availableQuestions.isEmpty()) {
            return null;
        }

        double maxInformation = -1;
        Question bestQuestion = null;

        for (Question question : availableQuestions) {
            double a = question.getDiscrimination();
            double b = question.getDifficulty();
            double c = question.getGuessing();

            double information = itemInformation(theta, a, b, c);

            if (information > maxInformation) {
                maxInformation = information;
                bestQuestion = question;
            }
        }

        return bestQuestion;
    }

    /**
     * Calculate test reliability (similar to Cronbach's Alpha for IRT)
     *
     * @param questions List of administered questions
     * @param theta Ability estimate
     * @return Reliability coefficient (0-1)
     */
    public static double calculateReliability(List<Question> questions, double theta) {
        double totalInformation = 0.0;

        for (Question question : questions) {
            double a = question.getDiscrimination();
            double b = question.getDifficulty();
            double c = question.getGuessing();
            totalInformation += itemInformation(theta, a, b, c);
        }

        double reliability = totalInformation / (1 + totalInformation);
        return Math.round(reliability * 10000.0) / 10000.0;
    }

    /**
     * Estimate ability level category based on theta value
     *
     * @param theta Ability estimate
     * @return Ability level string (Beginner, Intermediate, Advanced, Expert)
     */
    public static String getAbilityLevel(double theta) {
        if (theta < Constants.ABILITY_THRESHOLD_BEGINNER) {
            return "Beginner";
        } else if (theta < Constants.ABILITY_THRESHOLD_INTERMEDIATE) {
            return "Developing";
        } else if (theta < Constants.ABILITY_THRESHOLD_ADVANCED) {
            return "Intermediate";
        } else if (theta < Constants.ABILITY_THRESHOLD_EXPERT) {
            return "Advanced";
        } else {
            return "Expert";
        }
    }

    /**
     * Calculate expected score percentage based on theta
     *
     * @param theta Ability estimate
     * @param questions List of questions
     * @return Expected percentage score (0-100)
     */
    public static double getExpectedScore(double theta, List<Question> questions) {
        if (questions == null || questions.isEmpty()) {
            return 0;
        }

        double totalProbability = 0.0;

        for (Question question : questions) {
            double a = question.getDiscrimination();
            double b = question.getDifficulty();
            double c = question.getGuessing();
            totalProbability += calculateProbability(theta, a, b, c);
        }

        double percentage = (totalProbability / questions.size()) * 100;
        return Math.round(percentage * 100.0) / 100.0;
    }

    /**
     * Convert raw score to approximate theta (rough estimation)
     * Useful for initial ability estimation
     *
     * @param correctCount Number of correct answers
     * @param totalCount Total number of questions
     * @return Approximate theta value
     */
    public static double rawScoreToTheta(int correctCount, int totalCount) {
        if (totalCount == 0) {
            return Constants.INITIAL_THETA;
        }

        double proportion = (double) correctCount / totalCount;

        // Handle edge cases
        if (proportion >= 0.99) {
            proportion = 0.99;
        } else if (proportion <= 0.01) {
            proportion = 0.01;
        }

        // Simple logit transformation
        double theta = Math.log(proportion / (1 - proportion));

        // Constrain to reasonable range
        return Math.max(Constants.MIN_THETA, Math.min(Constants.MAX_THETA, theta));
    }

    /**
     * Check if assessment has enough items for reliable ability estimation
     *
     * @param itemCount Number of items
     * @return true if sufficient, false otherwise
     */
    public static boolean isSufficientItems(int itemCount) {
        return itemCount >= 10; // Minimum 10 items for reliable IRT estimation
    }

    /**
     * Calculate growth/change in ability
     *
     * @param initialTheta Initial ability
     * @param finalTheta Final ability
     * @return Growth description string
     */
    public static String getGrowthDescription(double initialTheta, double finalTheta) {
        double change = finalTheta - initialTheta;

        if (Math.abs(change) < 0.1) {
            return "No significant change";
        } else if (change > 0.5) {
            return "Significant improvement!";
        } else if (change > 0.2) {
            return "Good progress!";
        } else if (change > 0) {
            return "Slight improvement";
        } else if (change > -0.2) {
            return "Slight decline";
        } else {
            return "Needs more practice";
        }
    }
}
