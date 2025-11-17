package com.example.literise.database;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "LiteRiseSession";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_TOKEN = "token";

    private static final String KEY_ABILITY = "current_ability";

    private static final String KEY_XP = "total_xp";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
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

    public String getFullName() {

        return prefs.getString(KEY_FULLNAME, null);

    }
}
