package com.example.literise.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.literise.models.Badge;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages gamification features: XP, levels, badges, streaks
 */
public class GamificationManager {
    private static final String PREF_NAME = "GamificationPrefs";
    private static final String KEY_TOTAL_XP = "total_xp";
    private static final String KEY_CURRENT_LEVEL = "current_level";
    private static final String KEY_EARNED_BADGES = "earned_badges";
    private static final String KEY_LESSONS_COMPLETED = "lessons_completed";
    private static final String KEY_MODULES_COMPLETED = "modules_completed";
    private static final String KEY_PERFECT_SCORES = "perfect_scores";
    private static final String KEY_STORIES_WRITTEN = "stories_written";
    private static final String KEY_WORDS_MASTERED = "words_mastered";
    private static final String KEY_LAST_ACTIVITY_DATE = "last_activity_date";
    private static final String KEY_CURRENT_STREAK = "current_streak";
    private static final String KEY_LONGEST_STREAK = "longest_streak";

    private SharedPreferences prefs;
    private Context context;

    // Level XP thresholds
    private static final int[] LEVEL_THRESHOLDS = {
            0,      // Level 1: 0-100 XP
            100,    // Level 2: 101-250 XP
            250,    // Level 3: 251-500 XP
            500,    // Level 4: 501-1000 XP
            1000,   // Level 5: 1001-2000 XP
            2000    // Level 6: 2001+ XP
    };

    public GamificationManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // ==================== XP SYSTEM ====================

    /**
     * Get total XP earned
     */
    public int getTotalXP() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    /**
     * Add XP and return if level up occurred
     */
    public boolean addXP(int xp) {
        int currentXP = getTotalXP();
        int currentLevel = getCurrentLevel();

        int newXP = currentXP + xp;
        prefs.edit().putInt(KEY_TOTAL_XP, newXP).apply();

        int newLevel = calculateLevel(newXP);
        if (newLevel > currentLevel) {
            prefs.edit().putInt(KEY_CURRENT_LEVEL, newLevel).apply();
            return true; // Level up!
        }
        return false;
    }

    /**
     * Calculate level from XP
     */
    private int calculateLevel(int xp) {
        for (int i = LEVEL_THRESHOLDS.length - 1; i >= 0; i--) {
            if (xp >= LEVEL_THRESHOLDS[i]) {
                return i + 1;
            }
        }
        return 1;
    }

    /**
     * Get current level
     */
    public int getCurrentLevel() {
        int xp = getTotalXP();
        return calculateLevel(xp);
    }

    /**
     * Get XP needed for next level
     */
    public int getXPForNextLevel() {
        int currentLevel = getCurrentLevel();
        if (currentLevel >= LEVEL_THRESHOLDS.length) {
            return 0; // Max level reached
        }
        return LEVEL_THRESHOLDS[currentLevel];
    }

    /**
     * Get progress to next level (0-100)
     */
    public int getLevelProgress() {
        int currentLevel = getCurrentLevel();
        if (currentLevel >= LEVEL_THRESHOLDS.length) {
            return 100; // Max level
        }

        int currentXP = getTotalXP();
        int currentLevelXP = LEVEL_THRESHOLDS[currentLevel - 1];
        int nextLevelXP = LEVEL_THRESHOLDS[currentLevel];

        int xpInCurrentLevel = currentXP - currentLevelXP;
        int xpNeededForLevel = nextLevelXP - currentLevelXP;

        return (int) ((xpInCurrentLevel / (double) xpNeededForLevel) * 100);
    }

    // ==================== BADGE SYSTEM ====================

    /**
     * Check and award badge if earned
     */
    public boolean checkAndAwardBadge(String badgeId) {
        if (isBadgeEarned(badgeId)) {
            return false; // Already earned
        }

        boolean shouldAward = false;

        switch (badgeId) {
            case Badge.BADGE_FIRST_STEPS:
                shouldAward = getLessonsCompleted() >= 1;
                break;
            case Badge.BADGE_MODULE_MASTER:
                shouldAward = getModulesCompleted() >= 1;
                break;
            case Badge.BADGE_PERFECT_SCORE:
                shouldAward = getPerfectScores() >= 1;
                break;
            case Badge.BADGE_STORY_STAR:
                shouldAward = getStoriesWritten() >= 10;
                break;
            case Badge.BADGE_WORD_WIZARD:
                shouldAward = getWordsMastered() >= 500;
                break;
            case Badge.BADGE_DEDICATED_LEARNER:
                shouldAward = getCurrentStreak() >= 7;
                break;
            case Badge.BADGE_CHAMPION:
                shouldAward = getModulesCompleted() >= 5;
                break;
        }

        if (shouldAward) {
            awardBadge(badgeId);
            return true;
        }
        return false;
    }

    /**
     * Award a badge
     */
    private void awardBadge(String badgeId) {
        Set<String> earnedBadges = getEarnedBadges();
        earnedBadges.add(badgeId);
        prefs.edit().putStringSet(KEY_EARNED_BADGES, earnedBadges).apply();

        // Award bonus XP
        addXP(25); // Bonus XP for earning badge
    }

    /**
     * Check if badge is earned
     */
    public boolean isBadgeEarned(String badgeId) {
        return getEarnedBadges().contains(badgeId);
    }

    /**
     * Get all earned badge IDs
     */
    public Set<String> getEarnedBadges() {
        return prefs.getStringSet(KEY_EARNED_BADGES, new HashSet<>());
    }

    /**
     * Get total badges earned
     */
    public int getTotalBadgesEarned() {
        return getEarnedBadges().size();
    }

    /**
     * Get all badges with earned status
     */
    public List<Badge> getAllBadgesWithStatus() {
        Badge[] allBadges = Badge.getAllBadges();
        Set<String> earnedIds = getEarnedBadges();
        List<Badge> badges = new ArrayList<>();

        for (Badge badge : allBadges) {
            if (earnedIds.contains(badge.getBadgeId())) {
                badge.setEarned(true);
            }
            badges.add(badge);
        }
        return badges;
    }

    // ==================== PROGRESS TRACKING ====================

    /**
     * Increment lessons completed
     */
    public void incrementLessonsCompleted() {
        int count = getLessonsCompleted();
        prefs.edit().putInt(KEY_LESSONS_COMPLETED, count + 1).apply();
        checkAndAwardBadge(Badge.BADGE_FIRST_STEPS);
    }

    public int getLessonsCompleted() {
        return prefs.getInt(KEY_LESSONS_COMPLETED, 0);
    }

    /**
     * Increment modules completed
     */
    public void incrementModulesCompleted() {
        int count = getModulesCompleted();
        prefs.edit().putInt(KEY_MODULES_COMPLETED, count + 1).apply();
        checkAndAwardBadge(Badge.BADGE_MODULE_MASTER);
        checkAndAwardBadge(Badge.BADGE_CHAMPION);
    }

    public int getModulesCompleted() {
        return prefs.getInt(KEY_MODULES_COMPLETED, 0);
    }

    /**
     * Increment perfect scores
     */
    public void incrementPerfectScores() {
        int count = getPerfectScores();
        prefs.edit().putInt(KEY_PERFECT_SCORES, count + 1).apply();
        checkAndAwardBadge(Badge.BADGE_PERFECT_SCORE);
    }

    public int getPerfectScores() {
        return prefs.getInt(KEY_PERFECT_SCORES, 0);
    }

    /**
     * Increment stories written
     */
    public void incrementStoriesWritten() {
        int count = getStoriesWritten();
        prefs.edit().putInt(KEY_STORIES_WRITTEN, count + 1).apply();
        checkAndAwardBadge(Badge.BADGE_STORY_STAR);
    }

    public int getStoriesWritten() {
        return prefs.getInt(KEY_STORIES_WRITTEN, 0);
    }

    /**
     * Set words mastered
     */
    public void setWordsMastered(int count) {
        prefs.edit().putInt(KEY_WORDS_MASTERED, count).apply();
        checkAndAwardBadge(Badge.BADGE_WORD_WIZARD);
    }

    public int getWordsMastered() {
        return prefs.getInt(KEY_WORDS_MASTERED, 0);
    }

    // ==================== STREAK SYSTEM ====================

    /**
     * Update streak on daily activity
     */
    public void updateStreak() {
        String today = getCurrentDate();
        String lastActivity = prefs.getString(KEY_LAST_ACTIVITY_DATE, "");

        if (lastActivity.equals(today)) {
            // Already logged today
            return;
        }

        String yesterday = getYesterdayDate();
        int currentStreak = getCurrentStreak();

        if (lastActivity.equals(yesterday)) {
            // Consecutive day - increment streak
            currentStreak++;
        } else if (!lastActivity.isEmpty()) {
            // Streak broken - reset to 1
            currentStreak = 1;
        } else {
            // First time
            currentStreak = 1;
        }

        prefs.edit()
                .putString(KEY_LAST_ACTIVITY_DATE, today)
                .putInt(KEY_CURRENT_STREAK, currentStreak)
                .apply();

        // Update longest streak
        int longestStreak = getLongestStreak();
        if (currentStreak > longestStreak) {
            prefs.edit().putInt(KEY_LONGEST_STREAK, currentStreak).apply();
        }

        checkAndAwardBadge(Badge.BADGE_DEDICATED_LEARNER);
    }

    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    public int getLongestStreak() {
        return prefs.getInt(KEY_LONGEST_STREAK, 0);
    }

    private String getCurrentDate() {
        return String.valueOf(System.currentTimeMillis() / (1000 * 60 * 60 * 24));
    }

    private String getYesterdayDate() {
        return String.valueOf((System.currentTimeMillis() / (1000 * 60 * 60 * 24)) - 1);
    }

    // ==================== REWARDS ====================

    /**
     * Calculate XP reward for completing lesson
     */
    public int calculateLessonXP(int quizScore) {
        int baseXP = 10;
        if (quizScore >= 90) {
            return baseXP + 10; // Bonus for 90%+
        } else if (quizScore >= 70) {
            return baseXP + 5; // Bonus for passing
        }
        return baseXP;
    }

    /**
     * Calculate XP reward for module assessment
     */
    public int calculateModuleXP(int assessmentScore) {
        int baseXP = 50;
        if (assessmentScore == 100) {
            return baseXP + 25; // Perfect score bonus
        } else if (assessmentScore >= 90) {
            return baseXP + 10; // High score bonus
        }
        return baseXP;
    }

    /**
     * Reset all gamification data (for testing/demo)
     */
    public void resetAll() {
        prefs.edit().clear().apply();
    }
}