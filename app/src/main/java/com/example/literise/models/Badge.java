package com.example.literise.models;

/**
 * Represents a badge/achievement that can be earned
 */
public class Badge {
    public static final String BADGE_FIRST_STEPS        = "first_steps";
    public static final String BADGE_PHONICS_PIONEER    = "badge_module1";  // Module 1: Phonics and Word Study
    public static final String BADGE_WORD_SCHOLAR       = "badge_module2";  // Module 2: Vocabulary and Word Knowledge
    public static final String BADGE_GRAMMAR_GUARDIAN   = "badge_module3";  // Module 3: Grammar Awareness and Grammatical Structures
    public static final String BADGE_TEXT_EXPLORER      = "badge_module4";  // Module 4: Comprehending and Analyzing Texts
    public static final String BADGE_CREATIVE_COMPOSER  = "badge_module5";  // Module 5: Creating and Composing Text
    public static final String BADGE_PERFECT_SCORE      = "perfect_score";
    public static final String BADGE_DEDICATED_LEARNER  = "dedicated_learner";
    public static final String BADGE_CHAMPION           = "champion";
    public static final String BADGE_GROWTH_MINDSET     = "growth_mindset";

    private String badgeId;
    private String title;
    private String description;
    private String iconName; // Resource name for icon
    private String requirement; // What's needed to earn it
    private int xpBonus; // Bonus XP for earning this badge
    private boolean isEarned;
    private long earnedTimestamp;

    public Badge(String badgeId, String title, String description, String iconName, String requirement) {
        this.badgeId = badgeId;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.requirement = requirement;
        this.xpBonus = 25; // Default bonus XP
        this.isEarned = false;
    }

    // Getters
    public String getBadgeId() { return badgeId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getIconName() { return iconName; }
    public String getRequirement() { return requirement; }
    public int getXpBonus() { return xpBonus; }
    public boolean isEarned() { return isEarned; }
    public long getEarnedTimestamp() { return earnedTimestamp; }

    // Setters
    public void setXpBonus(int xp) { this.xpBonus = xp; }
    public void setEarned(boolean earned) { this.isEarned = earned; }
    public void setEarnedTimestamp(long timestamp) { this.earnedTimestamp = timestamp; }

    /**
     * Get badge by ID
     */
    public static Badge getBadgeById(String badgeId) {
        Badge[] allBadges = getAllBadges();
        for (Badge badge : allBadges) {
            if (badge.getBadgeId().equals(badgeId)) {
                return badge;
            }
        }
        return null;
    }

    /**
     * Get all available badges
     */
    public static Badge[] getAllBadges() {
        return new Badge[] {
                // Module completion badges — match badge_seed.sql UnlockCondition values
                new Badge(BADGE_PHONICS_PIONEER,   "Phonics Pioneer",
                        "Completed Module 1: Phonics and Word Study",
                        "badge_module1", "Complete all lessons in Module 1"),
                new Badge(BADGE_WORD_SCHOLAR,      "Word Scholar",
                        "Completed Module 2: Vocabulary and Word Knowledge",
                        "badge_module2", "Complete all lessons in Module 2"),
                new Badge(BADGE_GRAMMAR_GUARDIAN,  "Grammar Guardian",
                        "Completed Module 3: Grammar Awareness and Grammatical Structures",
                        "badge_module3", "Complete all lessons in Module 3"),
                new Badge(BADGE_TEXT_EXPLORER,     "Text Explorer",
                        "Completed Module 4: Comprehending and Analyzing Texts",
                        "badge_module4", "Complete all lessons in Module 4"),
                new Badge(BADGE_CREATIVE_COMPOSER, "Creative Composer",
                        "Completed Module 5: Creating and Composing Text",
                        "badge_module5", "Complete all lessons in Module 5"),
                // Achievement badges
                new Badge(BADGE_FIRST_STEPS,       "First Steps",
                        "Complete your first lesson", "ic_first_steps", "Complete 1 lesson"),
                new Badge(BADGE_PERFECT_SCORE,     "Perfect Score",
                        "Get 100% on any assessment", "ic_perfect_score", "Score 100% on test"),
                new Badge(BADGE_DEDICATED_LEARNER, "Dedicated Learner",
                        "Maintain a 7-day streak", "ic_streak", "7-day streak"),
                new Badge(BADGE_CHAMPION,          "Champion",
                        "Complete all 5 modules", "ic_champion", "Complete all modules"),
                new Badge(BADGE_GROWTH_MINDSET,    "Growth Mindset",
                        "Improve 50% from pre-test", "ic_growth", "50% improvement")
        };
    }
}