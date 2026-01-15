package com.example.literise.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Database helper for storing lesson progress and quiz results
 */
public class LessonDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "LiteRise.db";
    private static final int DATABASE_VERSION = 2;

    // Tables
    private static final String TABLE_LESSON_PROGRESS = "lesson_progress";
    private static final String TABLE_MODULE_PROGRESS = "module_progress";

    // Lesson Progress Columns
    private static final String KEY_LESSON_ID = "lesson_id";
    private static final String KEY_MODULE_ID = "module_id";
    private static final String KEY_IS_UNLOCKED = "is_unlocked";
    private static final String KEY_IS_COMPLETED = "is_completed";
    private static final String KEY_PRACTICE_SCORE = "practice_score";
    private static final String KEY_QUIZ_SCORE = "quiz_score";
    private static final String KEY_ATTEMPTS = "attempts";
    private static final String KEY_COMPLETED_TIMESTAMP = "completed_timestamp";
    private static final String KEY_LAST_ACCESSED = "last_accessed";

    // Module Progress Columns
    private static final String KEY_LESSONS_COMPLETED = "lessons_completed";
    private static final String KEY_TOTAL_LESSONS = "total_lessons";
    private static final String KEY_AVERAGE_SCORE = "average_score";
    private static final String KEY_MODULE_COMPLETED = "module_completed";
    private static final String KEY_ASSESSMENT_SCORE = "assessment_score";
    private static final String KEY_ASSESSMENT_PASSED = "assessment_passed";

    public LessonDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create lesson_progress table
        String CREATE_LESSON_TABLE = "CREATE TABLE " + TABLE_LESSON_PROGRESS + "("
                + KEY_LESSON_ID + " INTEGER PRIMARY KEY,"
                + KEY_MODULE_ID + " INTEGER,"
                + KEY_IS_UNLOCKED + " INTEGER DEFAULT 0,"
                + KEY_IS_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_PRACTICE_SCORE + " INTEGER DEFAULT 0,"
                + KEY_QUIZ_SCORE + " INTEGER DEFAULT 0,"
                + KEY_ATTEMPTS + " INTEGER DEFAULT 0,"
                + KEY_COMPLETED_TIMESTAMP + " INTEGER DEFAULT 0,"
                + KEY_LAST_ACCESSED + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_LESSON_TABLE);

        // Create module_progress table
        String CREATE_MODULE_TABLE = "CREATE TABLE " + TABLE_MODULE_PROGRESS + "("
                + KEY_MODULE_ID + " INTEGER PRIMARY KEY,"
                + KEY_LESSONS_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_TOTAL_LESSONS + " INTEGER DEFAULT 15,"
                + KEY_AVERAGE_SCORE + " INTEGER DEFAULT 0,"
                + KEY_MODULE_COMPLETED + " INTEGER DEFAULT 0,"
                + KEY_ASSESSMENT_SCORE + " INTEGER DEFAULT 0,"
                + KEY_ASSESSMENT_PASSED + " INTEGER DEFAULT 0"
                + ")";
        db.execSQL(CREATE_MODULE_TABLE);

        // Initialize first lesson as unlocked for each module
        for (int moduleId = 1; moduleId <= 5; moduleId++) {
            int firstLessonId = (moduleId * 100) + 1; // 101, 201, 301, etc.
            ContentValues values = new ContentValues();
            values.put(KEY_LESSON_ID, firstLessonId);
            values.put(KEY_MODULE_ID, moduleId);
            values.put(KEY_IS_UNLOCKED, 1);
            db.insert(TABLE_LESSON_PROGRESS, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LESSON_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODULE_PROGRESS);
        onCreate(db);
    }

    // ==================== LESSON PROGRESS ====================

    /**
     * Update lesson progress
     */
    public void updateLessonProgress(int lessonId, int moduleId, int practiceScore,
                                     int quizScore, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_MODULE_ID, moduleId);
        values.put(KEY_PRACTICE_SCORE, practiceScore);
        values.put(KEY_QUIZ_SCORE, quizScore);
        values.put(KEY_IS_COMPLETED, isCompleted ? 1 : 0);
        values.put(KEY_LAST_ACCESSED, System.currentTimeMillis());

        // Increment attempts
        Cursor cursor = db.query(TABLE_LESSON_PROGRESS,
                new String[]{KEY_ATTEMPTS},
                KEY_LESSON_ID + "=?",
                new String[]{String.valueOf(lessonId)},
                null, null, null);

        int attempts = 1;
        if (cursor != null && cursor.moveToFirst()) {
            attempts = cursor.getInt(0) + 1;
            cursor.close();
        }
        values.put(KEY_ATTEMPTS, attempts);

        if (isCompleted && quizScore >= 70) {
            values.put(KEY_COMPLETED_TIMESTAMP, System.currentTimeMillis());
        }

        int rowsAffected = db.update(TABLE_LESSON_PROGRESS, values,
                KEY_LESSON_ID + "=?",
                new String[]{String.valueOf(lessonId)});

        // If lesson doesn't exist, insert it
        if (rowsAffected == 0) {
            values.put(KEY_LESSON_ID, lessonId);
            values.put(KEY_IS_UNLOCKED, 1);
            db.insert(TABLE_LESSON_PROGRESS, null, values);
        }

        // If lesson passed, unlock next lesson
        if (isCompleted && quizScore >= 70) {
            unlockNextLesson(lessonId, moduleId);
        }

        db.close();
    }

    /**
     * Unlock next lesson
     */
    public void unlockNextLesson(int currentLessonId, int moduleId) {
        int nextLessonId = currentLessonId + 1;
        int maxLessonId = (moduleId * 100) + 15; // 15 lessons per module

        if (nextLessonId <= maxLessonId) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(KEY_LESSON_ID, nextLessonId);
            values.put(KEY_MODULE_ID, moduleId);
            values.put(KEY_IS_UNLOCKED, 1);

            // Check if exists
            Cursor cursor = db.query(TABLE_LESSON_PROGRESS,
                    new String[]{KEY_LESSON_ID},
                    KEY_LESSON_ID + "=?",
                    new String[]{String.valueOf(nextLessonId)},
                    null, null, null);

            if (cursor != null && cursor.getCount() == 0) {
                db.insert(TABLE_LESSON_PROGRESS, null, values);
            } else {
                db.update(TABLE_LESSON_PROGRESS,
                        values,
                        KEY_LESSON_ID + "=?",
                        new String[]{String.valueOf(nextLessonId)});
            }

            if (cursor != null) cursor.close();
            db.close();
        }
    }

    /**
     * Check if lesson is unlocked
     */
    public boolean isLessonUnlocked(int lessonId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LESSON_PROGRESS,
                new String[]{KEY_IS_UNLOCKED},
                KEY_LESSON_ID + "=?",
                new String[]{String.valueOf(lessonId)},
                null, null, null);

        boolean isUnlocked = false;
        if (cursor != null && cursor.moveToFirst()) {
            isUnlocked = cursor.getInt(0) == 1;
            cursor.close();
        }
        db.close();
        return isUnlocked;
    }

    /**
     * Get lesson progress
     */
    public int[] getLessonProgress(int lessonId) {
        // Returns [practiceScore, quizScore, attempts, isCompleted]
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_LESSON_PROGRESS,
                new String[]{KEY_PRACTICE_SCORE, KEY_QUIZ_SCORE, KEY_ATTEMPTS, KEY_IS_COMPLETED},
                KEY_LESSON_ID + "=?",
                new String[]{String.valueOf(lessonId)},
                null, null, null);

        int[] progress = new int[]{0, 0, 0, 0};
        if (cursor != null && cursor.moveToFirst()) {
            progress[0] = cursor.getInt(0); // practice score
            progress[1] = cursor.getInt(1); // quiz score
            progress[2] = cursor.getInt(2); // attempts
            progress[3] = cursor.getInt(3); // is completed
            cursor.close();
        }
        db.close();
        return progress;
    }

    // ==================== MODULE PROGRESS ====================

    /**
     * Update module progress
     */
    public void updateModuleProgress(int moduleId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Count completed lessons
        Cursor cursor = db.query(TABLE_LESSON_PROGRESS,
                new String[]{KEY_LESSON_ID, KEY_QUIZ_SCORE},
                KEY_MODULE_ID + "=? AND " + KEY_IS_COMPLETED + "=1 AND " + KEY_QUIZ_SCORE + ">=70",
                new String[]{String.valueOf(moduleId)},
                null, null, null);

        int lessonsCompleted = 0;
        int totalScore = 0;

        if (cursor != null) {
            lessonsCompleted = cursor.getCount();
            while (cursor.moveToNext()) {
                totalScore += cursor.getInt(1);
            }
            cursor.close();
        }

        int averageScore = lessonsCompleted > 0 ? totalScore / lessonsCompleted : 0;
        boolean moduleCompleted = lessonsCompleted >= 15;

        ContentValues values = new ContentValues();
        values.put(KEY_LESSONS_COMPLETED, lessonsCompleted);
        values.put(KEY_AVERAGE_SCORE, averageScore);
        values.put(KEY_MODULE_COMPLETED, moduleCompleted ? 1 : 0);

        int rowsAffected = db.update(TABLE_MODULE_PROGRESS, values,
                KEY_MODULE_ID + "=?",
                new String[]{String.valueOf(moduleId)});

        // If module doesn't exist, insert it
        if (rowsAffected == 0) {
            values.put(KEY_MODULE_ID, moduleId);
            db.insert(TABLE_MODULE_PROGRESS, null, values);
        }

        db.close();
    }

    /**
     * Save module assessment score
     */
    public void saveModuleAssessment(int moduleId, int assessmentScore) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_ASSESSMENT_SCORE, assessmentScore);
        values.put(KEY_ASSESSMENT_PASSED, assessmentScore >= 80 ? 1 : 0);

        int rowsAffected = db.update(TABLE_MODULE_PROGRESS, values,
                KEY_MODULE_ID + "=?",
                new String[]{String.valueOf(moduleId)});

        if (rowsAffected == 0) {
            values.put(KEY_MODULE_ID, moduleId);
            db.insert(TABLE_MODULE_PROGRESS, null, values);
        }

        db.close();
    }

    /**
     * Get module progress percentage
     */
    public int getModuleProgressPercentage(int moduleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MODULE_PROGRESS,
                new String[]{KEY_LESSONS_COMPLETED, KEY_TOTAL_LESSONS},
                KEY_MODULE_ID + "=?",
                new String[]{String.valueOf(moduleId)},
                null, null, null);

        int percentage = 0;
        if (cursor != null && cursor.moveToFirst()) {
            int completed = cursor.getInt(0);
            int total = cursor.getInt(1);
            percentage = (int) ((completed / (double) total) * 100);
            cursor.close();
        }
        db.close();
        return percentage;
    }

    /**
     * Check if module is completed
     */
    public boolean isModuleCompleted(int moduleId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MODULE_PROGRESS,
                new String[]{KEY_MODULE_COMPLETED},
                KEY_MODULE_ID + "=?",
                new String[]{String.valueOf(moduleId)},
                null, null, null);

        boolean isCompleted = false;
        if (cursor != null && cursor.moveToFirst()) {
            isCompleted = cursor.getInt(0) == 1;
            cursor.close();
        }
        db.close();
        return isCompleted;
    }

    /**
     * Reset all progress (for testing)
     */
    public void resetAllProgress() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LESSON_PROGRESS, null, null);
        db.delete(TABLE_MODULE_PROGRESS, null, null);
        db.close();
        onCreate(db);
    }
}