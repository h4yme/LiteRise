package com.example.literise.models;

/**
 * Represents a badge/achievement that can be earned
 */
public class Badge {
    public static final String BADGE_FIRST_STEPS = "first_steps";
    public static final String BADGE_MODULE_MASTER = "module_master";
    public static final String BADGE_SPEED_READER = "speed_reader";
    public static final String BADGE_WORD_WIZARD = "word_wizard";
    public static final String BADGE_GRAMMAR_GURU = "grammar_guru";
    public static final String BADGE_STORY_STAR = "story_star";
    public static final String BADGE_PERFECT_SCORE = "perfect_score";
    public static final String BADGE_DEDICATED_LEARNER = "dedicated_learner";
    public static final String BADGE_CHAMPION = "champion";
    public static final String BADGE_GROWTH_MINDSET = "growth_mindset";

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
     * Get all available badges
     */
    public static Badge[] getAllBadges() {
        return new Badge[] {
            new Badge(BADGE_FIRST_STEPS, "First Steps",
                "Complete your first lesson", "ic_first_steps", "Complete 1 lesson"),
            new Badge(BADGE_MODULE_MASTER, "Module Master",
                "Complete an entire module", "ic_module_master", "Complete any module"),
            new Badge(BADGE_SPEED_READER, "Speed Reader",
                "Read at 100 words per minute", "ic_speed_reader", "Achieve 100 WPM"),
            new Badge(BADGE_WORD_WIZARD, "Word Wizard",
                "Master 500 vocabulary words", "ic_word_wizard", "Learn 500 words"),
            new Badge(BADGE_GRAMMAR_GURU, "Grammar Guru",
                "Perfect score on grammar assessment", "ic_grammar_guru", "100% on Grammar test"),
            new Badge(BADGE_STORY_STAR, "Story Star",
                "Write 10 complete stories", "ic_story_star", "Write 10 stories"),
            new Badge(BADGE_PERFECT_SCORE, "Perfect Score",
                "Get 100% on any assessment", "ic_perfect_score", "Score 100% on test"),
            new Badge(BADGE_DEDICATED_LEARNER, "Dedicated Learner",
                "Maintain a 7-day streak", "ic_streak", "7-day streak"),
            new Badge(BADGE_CHAMPION, "Champion",
                "Complete all 5 modules", "ic_champion", "Complete all modules"),
            new Badge(BADGE_GROWTH_MINDSET, "Growth Mindset",
                "Improve 50% from pre-test", "ic_growth", "50% improvement")
        };
    }
}
