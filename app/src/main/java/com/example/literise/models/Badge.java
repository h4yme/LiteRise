package com.example.literise.models;

public class Badge {
    private int badgeId;
    private String badgeName;
    private String description;
    private String iconName;
    private String unlockCriteria; // e.g., "complete_phonics_module", "6_steps_comprehension", "50_vocab_words"
    private boolean isUnlocked;
    private String unlockedDate;
    private String category; // "module_master", "step_warrior", "word_collector", "streak_keeper"

    public Badge() {
    }

    public Badge(int badgeId, String badgeName, String description, String iconName,
                 String unlockCriteria, String category) {
        this.badgeId = badgeId;
        this.badgeName = badgeName;
        this.description = description;
        this.iconName = iconName;
        this.unlockCriteria = unlockCriteria;
        this.category = category;
        this.isUnlocked = false;
    }

    // Getters and setters
    public int getBadgeId() {
        return badgeId;
    }

    public void setBadgeId(int badgeId) {
        this.badgeId = badgeId;
    }

    public String getBadgeName() {
        return badgeName;
    }

    public void setBadgeName(String badgeName) {
        this.badgeName = badgeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getUnlockCriteria() {
        return unlockCriteria;
    }

    public void setUnlockCriteria(String unlockCriteria) {
        this.unlockCriteria = unlockCriteria;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        isUnlocked = unlocked;
    }

    public String getUnlockedDate() {
        return unlockedDate;
    }

    public void setUnlockedDate(String unlockedDate) {
        this.unlockedDate = unlockedDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBadgeEmoji() {
        switch (category) {
            case "module_master":
                return "🏆";
            case "step_warrior":
                return "⭐";
            case "word_collector":
                return "📚";
            case "streak_keeper":
                return "🔥";
            default:
                return "🎖️";
        }
    }
}
