package com.example.literise.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.literise.utils.Constants;

public class SessionManager {

    private static final String PREF_NAME = Constants.SESSION_PREF_NAME;
    private static final String KEY_STUDENT_ID = Constants.KEY_STUDENT_ID;
    private static final String KEY_FULLNAME = Constants.KEY_FULLNAME;
    private static final String KEY_EMAIL = Constants.KEY_EMAIL;
    private static final String KEY_SESSION_ID = Constants.KEY_SESSION_ID;
    private static final String KEY_CURRENT_ABILITY = Constants.KEY_CURRENT_ABILITY;
    private static final String KEY_GRADE_LEVEL = Constants.KEY_GRADE_LEVEL;
    private static final String KEY_TOTAL_XP = Constants.KEY_TOTAL_XP;
    private static final String KEY_CURRENT_STREAK = Constants.KEY_CURRENT_STREAK;

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * Save student login information
     */
    public void saveStudent(int studentId, String fullname, String email) {
        editor.putInt(KEY_STUDENT_ID, studentId);
        editor.putString(KEY_FULLNAME, fullname);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    /**
     * Save complete student profile
     */
    public void saveStudentProfile(int studentId, String fullname, String email,
                                    int gradeLevel, float currentAbility, int totalXP, int currentStreak) {
        editor.putInt(KEY_STUDENT_ID, studentId);
        editor.putString(KEY_FULLNAME, fullname);
        editor.putString(KEY_EMAIL, email);
        editor.putInt(KEY_GRADE_LEVEL, gradeLevel);
        editor.putFloat(KEY_CURRENT_ABILITY, currentAbility);
        editor.putInt(KEY_TOTAL_XP, totalXP);
        editor.putInt(KEY_CURRENT_STREAK, currentStreak);
        editor.apply();
    }

    /**
     * Save current test session ID
     */
    public void saveSessionId(int sessionId) {
        editor.putInt(KEY_SESSION_ID, sessionId);
        editor.apply();
    }

    /**
     * Save updated ability score
     */
    public void saveAbility(float ability) {
        editor.putFloat(KEY_CURRENT_ABILITY, ability);
        editor.apply();
    }

    /**
     * Update student XP
     */
    public void updateXP(int xp) {
        editor.putInt(KEY_TOTAL_XP, xp);
        editor.apply();
    }

    /**
     * Update streak
     */
    public void updateStreak(int streak) {
        editor.putInt(KEY_CURRENT_STREAK, streak);
        editor.apply();
    }

    // Getters
    public int getStudentId() {
        return prefs.getInt(KEY_STUDENT_ID, 0);
    }

    public String getFullname() {
        return prefs.getString(KEY_FULLNAME, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public int getSessionId() {
        return prefs.getInt(KEY_SESSION_ID, 0);
    }

    public float getCurrentAbility() {
        return prefs.getFloat(KEY_CURRENT_ABILITY, 0.0f);
    }

    public int getGradeLevel() {
        return prefs.getInt(KEY_GRADE_LEVEL, 4);
    }

    public int getTotalXP() {
        return prefs.getInt(KEY_TOTAL_XP, 0);
    }

    public int getCurrentStreak() {
        return prefs.getInt(KEY_CURRENT_STREAK, 0);
    }

    /**
     * Clear all session data (logout)
     */
    public void logout() {
        editor.clear();
        editor.apply();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return getStudentId() != 0;
    }

    /**
     * Clear only session-specific data (keep login info)
     */
    public void clearSessionData() {
        editor.remove(KEY_SESSION_ID);
        editor.apply();
    }
}
