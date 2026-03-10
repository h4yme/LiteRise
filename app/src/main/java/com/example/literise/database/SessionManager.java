package com.example.literise.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.literise.utils.AppConfig;



public class SessionManager {



    private static final String PREF_NAME = "LiteRiseSession";

    private static final String KEY_STUDENT_ID = "student_id";

    private static final String KEY_FULLNAME = "fullname";

    private static final String KEY_EMAIL = "email";

    private static final String KEY_TOKEN = "token";



    private static final String KEY_ABILITY = "current_ability";



    private static final String KEY_XP = "total_xp";

    private static final String KEY_STREAK = "current_streak";

    private static final String KEY_HAS_SEEN_WELCOME = "has_seen_welcome";

    private static final String KEY_NICKNAME = "nickname";
    private static final String KEY_ASSESSMENT_COMPLETED = "assessment_completed";
    private static final String KEY_ASSESSMENT_STARTED = "assessment_started";

    // Placement test results (5 categories)
    private static final String KEY_PLACEMENT_LEVEL = "placement_level";
    private static final String KEY_CAT1_PHONICS_WORD_STUDY = "cat1_phonics_word_study";
    private static final String KEY_CAT2_VOCABULARY_WORD_KNOWLEDGE = "cat2_vocabulary_word_knowledge";
    private static final String KEY_CAT3_GRAMMAR_AWARENESS = "cat3_grammar_awareness";
    private static final String KEY_CAT4_COMPREHENDING_TEXT = "cat4_comprehending_text";
    private static final String KEY_CAT5_CREATING_COMPOSING = "cat5_creating_composing";
    private static final String KEY_PRE_THETA = "pre_assessment_theta";
    private static final String KEY_PRE_LEVEL = "pre_assessment_level";
    private static final String KEY_PRE_ACCURACY = "pre_assessment_accuracy";

    // Post-assessment certificate data
    private static final String KEY_POST_COMPLETED   = "post_assessment_completed";
    private static final String KEY_POST_THETA       = "post_assessment_theta_value";
    private static final String KEY_POST_LEVEL_NAME  = "post_assessment_level_name";
    private static final String KEY_POST_ACCURACY    = "post_assessment_accuracy_value";


    private SharedPreferences prefs;

    private SharedPreferences.Editor editor;

    private Context context;



    public SessionManager(Context context) {

        this.context = context;

        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        editor = prefs.edit();





    }



    /**

     * Setup demo user for offline mode

     */

    public void setupDemoUser() {

        editor.putInt(KEY_STUDENT_ID, AppConfig.DEMO_STUDENT_ID);

        editor.putString(KEY_FULLNAME, AppConfig.DEMO_STUDENT_NAME);

        editor.putString(KEY_EMAIL, AppConfig.DEMO_EMAIL);

        editor.putString(KEY_TOKEN, "demo_token");

        editor.apply();
    }

    public void saveStudent(int studentId, String fullname, String email) {
        editor.putInt(KEY_STUDENT_ID, studentId);
        editor.putString(KEY_FULLNAME, fullname);
        editor.putString(KEY_EMAIL, email);
        editor.apply();
    }

    public int getStudentId() {
        return prefs.getInt(KEY_STUDENT_ID, 0);
    }

    public String getFullname() {
        return prefs.getString(KEY_FULLNAME, null);
    }


    public void setHasSeenWelcome(boolean hasSeen) {

        editor.putBoolean(KEY_HAS_SEEN_WELCOME, hasSeen);

        editor.apply();

    }




    public boolean hasSeenWelcome() {

        return prefs.getBoolean(KEY_HAS_SEEN_WELCOME, false);

    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return getStudentId() != 0;
    }

    public void saveToken(String token) {

        editor.putString(KEY_TOKEN, token);

        editor.apply();

    }



    public String getToken() {

        return prefs.getString(KEY_TOKEN, null);

    }



    public void saveAbility(float ability) {

        editor.putFloat(KEY_ABILITY, ability);

        editor.apply();

    }



    public float getAbility() {

        return prefs.getFloat(KEY_ABILITY, 0.0f);

    }



    public void saveXP(int xp) {

        editor.putInt(KEY_XP, xp);

        editor.apply();

    }



    public int getXP() {

        return prefs.getInt(KEY_XP, 0);

    }

    public void updateTotalXP(int totalXP) {

        saveXP(totalXP);

    }

    public void saveStreak(int streak) {

        editor.putInt(KEY_STREAK, streak);

        editor.apply();

    }

    public int getStreak() {

        return prefs.getInt(KEY_STREAK, 0);

    }

    public String getFullName() {

        return prefs.getString(KEY_FULLNAME, null);

    }

    public void saveNickname(String nickname) {

        editor.putString(KEY_NICKNAME, nickname);

        editor.apply();

    }

    public String getNickname() {

        return prefs.getString(KEY_NICKNAME, null);

    }

    public void setAssessmentCompleted(boolean completed) {



        editor.putBoolean(KEY_ASSESSMENT_COMPLETED, completed);



        editor.apply();



    }



    public boolean hasCompletedAssessment() {



        return prefs.getBoolean(KEY_ASSESSMENT_COMPLETED, false);



    }

    public void setAssessmentStarted(boolean started) {
        editor.putBoolean(KEY_ASSESSMENT_STARTED, started);
        editor.apply();
    }

    public boolean hasStartedAssessment() {
        return prefs.getBoolean(KEY_ASSESSMENT_STARTED, false);
    }

    /**
     * Save placement test results
     */
    public void savePlacementLevel(String level) {
        editor.putString(KEY_PLACEMENT_LEVEL, level);
        editor.apply();
    }

    public String getPlacementLevel() {
        return prefs.getString(KEY_PLACEMENT_LEVEL, "Grade 3");
    }

    /**
     * Save category score from placement test (as percentage 0-100)
     * @param categoryKey Category key (e.g., "Cat1_PhonicsWordStudy")
     * @param score Score as percentage (0-100)
     */
    public void saveCategoryScore(String categoryKey, int score) {
        String key = getCategoryKey(categoryKey);
        if (key != null) {
            editor.putInt(key, score);
            editor.apply();
        }
    }

    /**
     * Get category score
     * @param categoryKey Category key
     * @return Score as percentage (0-100), or 0 if not found
     */
    public int getCategoryScore(String categoryKey) {
        String key = getCategoryKey(categoryKey);
        if (key != null) {
            return prefs.getInt(key, 0);
        }
        return 0;
    }

    public void savePreTheta(double theta) {
        editor.putFloat(KEY_PRE_THETA, (float) theta);
        editor.apply();
    }

    public double getPreTheta() {
        return prefs.getFloat(KEY_PRE_THETA, 0.0f);
    }

    public void savePreLevel(String level) {
        editor.putString(KEY_PRE_LEVEL, level);
        editor.apply();
    }

    public String getPreLevel() {
        return prefs.getString(KEY_PRE_LEVEL, "");
    }

    public void savePreAccuracy(double accuracy) {
        editor.putFloat(KEY_PRE_ACCURACY, (float) accuracy);
        editor.apply();
    }

    public double getPreAccuracy() {
        return prefs.getFloat(KEY_PRE_ACCURACY, 0.0f);
    }

    public void savePostAssessmentResult(double theta, String levelName, double accuracy) {
        editor.putBoolean(KEY_POST_COMPLETED, true);
        editor.putFloat(KEY_POST_THETA, (float) theta);
        editor.putString(KEY_POST_LEVEL_NAME, levelName);
        editor.putFloat(KEY_POST_ACCURACY, (float) accuracy);
        editor.apply();
    }

    public boolean hasCompletedPostAssessment() {
        return prefs.getBoolean(KEY_POST_COMPLETED, false);
    }

    public double getPostTheta() {
        return prefs.getFloat(KEY_POST_THETA, 0.0f);
    }

    public String getPostLevelName() {
        return prefs.getString(KEY_POST_LEVEL_NAME, "");
    }

    public double getPostAccuracy() {
        return prefs.getFloat(KEY_POST_ACCURACY, 0.0f);
    }

    /**
     * Map category keys to SharedPreferences keys
     */
    private String getCategoryKey(String categoryKey) {
        switch (categoryKey) {
            case "Cat1_PhonicsWordStudy":
                return KEY_CAT1_PHONICS_WORD_STUDY;
            case "Cat2_VocabularyWordKnowledge":
                return KEY_CAT2_VOCABULARY_WORD_KNOWLEDGE;
            case "Cat3_GrammarAwareness":
                return KEY_CAT3_GRAMMAR_AWARENESS;
            case "Cat4_ComprehendingText":
                return KEY_CAT4_COMPREHENDING_TEXT;
            case "Cat5_CreatingComposing":
                return KEY_CAT5_CREATING_COMPOSING;
            default:
                return null;
        }
    }
}