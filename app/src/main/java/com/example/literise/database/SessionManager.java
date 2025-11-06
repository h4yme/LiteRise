package com.example.literise.database;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "LiteRiseSession";
    private static final String KEY_STUDENT_ID = "student_id";
    private static final String KEY_FULLNAME = "fullname";
    private static final String KEY_EMAIL = "email";

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
}
