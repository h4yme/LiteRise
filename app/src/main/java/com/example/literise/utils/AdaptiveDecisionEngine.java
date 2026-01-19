package com.example.literise.utils;

import com.example.literise.models.*;
import com.example.literise.database.SessionManager;
import android.content.Context;
import android.util.Log;

import java.util.*;

/**
 * AdaptiveDecisionEngine - Performance-based adaptive learning logic
 *
 * Uses: placement level + quiz scores + attempt counts + score trends
 * Does NOT use IRT theta - simpler, more transparent, curriculum-aligned
 *
 * Key Decisions:
 * 1. Quiz performance → Supplemental/Intervention/Enrichment nodes
 * 2. Placement + recent performance → Lesson pacing strategy
 * 3. Placement + quiz score → Game difficulty
 */
public class AdaptiveDecisionEngine {
    private static final String TAG = "AdaptiveDecisionEngine";
    private Context context;
    private SessionManager sessionManager;

    // Thresholds
    private static final int PASS_THRESHOLD = 70;
    private static final int BORDERLINE_THRESHOLD = 80;
    private static final int MASTERY_THRESHOLD = 90;
    private static final int SCORE_TREND_DELTA = 10; // Points change to be considered trend

    public AdaptiveDecisionEngine(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
    }

    /**
     * Main decision logic after quiz completion
     * Returns adaptive decision based on performance metrics
     */
    public AdaptiveDecision evaluateQuizPerformance(
        int placementLevel,
        int quizScore,
        int attemptCount,
        List<Integer> recentScores, // Last 3 quiz scores across any nodes
        int categoryScore // Placement test category score for this skill
    ) {

        Log.d(TAG, "Evaluating quiz performance:");
        Log.d(TAG, "  Placement Level: " + placementLevel);
        Log.d(TAG, "  Quiz Score: " + quizScore + "%");
        Log.d(TAG, "  Attempt Count: " + attemptCount);
        Log.d(TAG, "  Recent Scores: " + recentScores);
        Log.d(TAG, "  Category Score: " + categoryScore + "%");

        // Calculate score trend
        AdaptiveDecision.ScoreTrend trend = calculateScoreTrend(recentScores);
        Log.d(TAG, "  Score Trend: " + trend);

        // INTERVENTION: Failed quiz (< 70%)
        if (quizScore < PASS_THRESHOLD) {
            String reason;
            if (attemptCount == 1) {
                reason = String.format("Quiz failed (%d%%) on first attempt - adding intervention node", quizScore);
            } else {
                reason = String.format("Multiple attempts (%d%% after %d tries) - intensive support needed",
                    quizScore, attemptCount);
            }

            Log.d(TAG, "Decision: ADD_INTERVENTION - " + reason);
            return new AdaptiveDecision(
                AdaptiveDecision.DecisionType.ADD_INTERVENTION,
                reason, quizScore, attemptCount, placementLevel, trend
            );
        }

        // SUPPLEMENTAL: Borderline pass (70-79%)
        if (quizScore >= PASS_THRESHOLD && quizScore < BORDERLINE_THRESHOLD) {

            // More conservative for BEGINNERS
            if (placementLevel == 1) {
                String reason = String.format("Beginner with borderline pass (%d%%) - adding support", quizScore);
                Log.d(TAG, "Decision: ADD_SUPPLEMENTAL - " + reason);
                return new AdaptiveDecision(
                    AdaptiveDecision.DecisionType.ADD_SUPPLEMENTAL,
                    reason, quizScore, attemptCount, placementLevel, trend
                );
            }

            // INTERMEDIATE: Only if declining or weak in this category
            if (placementLevel == 2) {
                if (trend == AdaptiveDecision.ScoreTrend.DECLINING) {
                    String reason = String.format("Intermediate with declining scores (%d%%) - preventive support", quizScore);
                    Log.d(TAG, "Decision: ADD_SUPPLEMENTAL - " + reason);
                    return new AdaptiveDecision(
                        AdaptiveDecision.DecisionType.ADD_SUPPLEMENTAL,
                        reason, quizScore, attemptCount, placementLevel, trend
                    );
                }

                // Check if this skill category was weak in placement
                if (categoryScore < 60) {
                    String reason = String.format("Weak placement category (%d%%), borderline quiz (%d%%) - support needed",
                        categoryScore, quizScore);
                    Log.d(TAG, "Decision: ADD_SUPPLEMENTAL - " + reason);
                    return new AdaptiveDecision(
                        AdaptiveDecision.DecisionType.ADD_SUPPLEMENTAL,
                        reason, quizScore, attemptCount, placementLevel, trend
                    );
                }
            }
        }

        // WARNING: Declining trend even with passing scores
        if (quizScore >= PASS_THRESHOLD && trend == AdaptiveDecision.ScoreTrend.DECLINING) {
            double avgRecent = calculateAverageScore(recentScores);
            if (quizScore < avgRecent - SCORE_TREND_DELTA) {
                String reason = String.format("Significant score drop (%.0f%% → %d%%) - early intervention",
                    avgRecent, quizScore);
                Log.d(TAG, "Decision: ADD_SUPPLEMENTAL - " + reason);
                return new AdaptiveDecision(
                    AdaptiveDecision.DecisionType.ADD_SUPPLEMENTAL,
                    reason, quizScore, attemptCount, placementLevel, trend
                );
            }
        }

        // ENRICHMENT: High performance (90%+)
        if (quizScore >= MASTERY_THRESHOLD) {
            if (placementLevel == 3) { // Advanced
                String reason = String.format("Advanced learner with mastery (%d%%) - enrichment available", quizScore);
                Log.d(TAG, "Decision: OFFER_ENRICHMENT - " + reason);
                return new AdaptiveDecision(
                    AdaptiveDecision.DecisionType.OFFER_ENRICHMENT,
                    reason, quizScore, attemptCount, placementLevel, trend
                );
            }
        }

        // PROCEED: Normal progression
        String reason = String.format("On track (%d%%, attempt %d) - proceed to next lesson", quizScore, attemptCount);
        Log.d(TAG, "Decision: PROCEED - " + reason);
        return new AdaptiveDecision(
            AdaptiveDecision.DecisionType.PROCEED,
            reason, quizScore, attemptCount, placementLevel, trend
        );
    }

    /**
     * Select game difficulty based on placement + recent performance
     * NO theta needed
     */
    public String selectGameDifficulty(
        int placementLevel,
        int currentQuizScore, // Latest quiz score for this node (0 if not taken)
        int gameAttemptCount
    ) {

        Log.d(TAG, "Selecting game difficulty:");
        Log.d(TAG, "  Placement Level: " + placementLevel);
        Log.d(TAG, "  Current Quiz Score: " + currentQuizScore + "%");
        Log.d(TAG, "  Game Attempt Count: " + gameAttemptCount);

        // Base difficulty on placement level
        if (placementLevel == 1) { // Beginner
            Log.d(TAG, "  Selected: EASY (Beginner)");
            return "EASY";
        }

        if (placementLevel == 3) { // Advanced
            // Unless struggling in this specific lesson
            if (currentQuizScore > 0 && currentQuizScore < BORDERLINE_THRESHOLD && gameAttemptCount > 0) {
                Log.d(TAG, "  Selected: MEDIUM (Advanced but struggling)");
                return "MEDIUM"; // Reduce challenge
            }
            Log.d(TAG, "  Selected: HARD (Advanced)");
            return "HARD";
        }

        // INTERMEDIATE (placementLevel == 2): Adaptive based on quiz performance
        if (currentQuizScore >= 85) {
            Log.d(TAG, "  Selected: HARD (Intermediate, high score)");
            return "HARD";
        } else if (currentQuizScore >= 75 || currentQuizScore == 0) {
            Log.d(TAG, "  Selected: MEDIUM (Intermediate, moderate score or not taken)");
            return "MEDIUM";
        } else {
            Log.d(TAG, "  Selected: EASY (Intermediate, low score)");
            return "EASY";
        }
    }

    /**
     * Determine lesson pacing strategy
     * Based on placement level + recent performance average
     */
    public PacingStrategy selectLessonPacing(
        int placementLevel,
        List<Integer> recentScores // Last 3 quiz scores
    ) {

        double avgRecentScore = calculateAverageScore(recentScores);

        Log.d(TAG, "Selecting lesson pacing:");
        Log.d(TAG, "  Placement Level: " + placementLevel);
        Log.d(TAG, "  Average Recent Score: " + avgRecentScore + "%");

        if (placementLevel == 1) { // Beginner
            Log.d(TAG, "  Selected: SLOW pacing (Beginner)");
            return new PacingStrategy(
                "SLOW",           // Speed
                "HIGH",           // Scaffolding
                "MANY",           // Examples
                15,               // Duration (minutes)
                true              // Allow review
            );
        }

        if (placementLevel == 3) { // Advanced
            // But slow down if recent performance dropped
            if (avgRecentScore < 75) {
                Log.d(TAG, "  Selected: MODERATE pacing (Advanced but struggling)");
                return new PacingStrategy("MODERATE", "BALANCED", "ADEQUATE", 10, true);
            }
            Log.d(TAG, "  Selected: FAST pacing (Advanced)");
            return new PacingStrategy("FAST", "MINIMAL", "FEW", 7, false);
        }

        // INTERMEDIATE: Adaptive to recent performance
        if (avgRecentScore >= 85) {
            Log.d(TAG, "  Selected: MODERATE_FAST pacing (Intermediate, high performance)");
            return new PacingStrategy("MODERATE_FAST", "LOW", "ADEQUATE", 8, false);
        } else if (avgRecentScore >= 70) {
            Log.d(TAG, "  Selected: MODERATE pacing (Intermediate, on track)");
            return new PacingStrategy("MODERATE", "BALANCED", "ADEQUATE", 10, true);
        } else {
            Log.d(TAG, "  Selected: SLOW pacing (Intermediate, needs support)");
            return new PacingStrategy("SLOW", "HIGH", "MANY", 12, true);
        }
    }

    /**
     * Get category score from placement test for a given skill
     */
    public int getCategoryScoreForSkill(String skillCategory) {
        // Map skill category to placement category score
        if (skillCategory == null) return 50; // Default

        if (skillCategory.toLowerCase().contains("phonic")) {
            return sessionManager.getCategoryScore("Cat1_PhonicsWordStudy");
        } else if (skillCategory.toLowerCase().contains("vocabular")) {
            return sessionManager.getCategoryScore("Cat2_VocabularyWordKnowledge");
        } else if (skillCategory.toLowerCase().contains("grammar")) {
            return sessionManager.getCategoryScore("Cat3_GrammarAwareness");
        } else if (skillCategory.toLowerCase().contains("comprehend") ||
                   skillCategory.toLowerCase().contains("analyz")) {
            return sessionManager.getCategoryScore("Cat4_ComprehendingText");
        } else if (skillCategory.toLowerCase().contains("creat") ||
                   skillCategory.toLowerCase().contains("compos") ||
                   skillCategory.toLowerCase().contains("writ")) {
            return sessionManager.getCategoryScore("Cat5_CreatingComposing");
        }

        return 50; // Default if no match
    }

    // =====================================================
    // HELPER METHODS
    // =====================================================

    private AdaptiveDecision.ScoreTrend calculateScoreTrend(List<Integer> recentScores) {
        if (recentScores == null || recentScores.size() < 2) {
            return AdaptiveDecision.ScoreTrend.STABLE;
        }

        int firstScore = recentScores.get(0);
        int lastScore = recentScores.get(recentScores.size() - 1);
        int delta = lastScore - firstScore;

        if (delta >= SCORE_TREND_DELTA) return AdaptiveDecision.ScoreTrend.IMPROVING;
        if (delta <= -SCORE_TREND_DELTA) return AdaptiveDecision.ScoreTrend.DECLINING;
        return AdaptiveDecision.ScoreTrend.STABLE;
    }

    private double calculateAverageScore(List<Integer> scores) {
        if (scores == null || scores.isEmpty()) return 0;

        int sum = 0;
        for (int score : scores) {
            sum += score;
        }
        return (double) sum / scores.size();
    }
}
