package com.example.literise.utils;

/**
 * LiteRise Application Constants
 * Centralized configuration for the app
 */
public class Constants {

    // ==================== API Configuration ====================
    public static final String BASE_URL = "http://10.248.215.210/api/";

    // API Endpoints
    public static final String ENDPOINT_LOGIN = "login.php";
    public static final String ENDPOINT_CREATE_SESSION = "create_session.php";
    public static final String ENDPOINT_GET_ASSESSMENT = "get_preassessment_items.php";
    public static final String ENDPOINT_SUBMIT_RESPONSES = "submit_responses.php";
    public static final String ENDPOINT_UPDATE_ABILITY = "update_ability.php";
    public static final String ENDPOINT_GET_PROGRESS = "get_student_progress.php";
    public static final String ENDPOINT_GET_LESSONS = "get_lessons.php";
    public static final String ENDPOINT_GET_GAME_DATA = "get_game_data.php";
    public static final String ENDPOINT_SAVE_GAME_RESULT = "save_game_result.php";

    // ==================== Assessment Configuration ====================
    public static final int PRE_ASSESSMENT_ITEM_COUNT = 20;
    public static final int POST_ASSESSMENT_ITEM_COUNT = 20;
    public static final int LESSON_ITEM_COUNT = 10;

    // IRT Parameters
    public static final double INITIAL_THETA = 0.0;
    public static final double MIN_THETA = -4.0;
    public static final double MAX_THETA = 4.0;
    public static final double THETA_TOLERANCE = 0.001;
    public static final int MAX_IRT_ITERATIONS = 20;

    // ==================== Session Configuration ====================
    public static final String SESSION_PREF_NAME = "LiteRiseSession";
    public static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds

    // Session Keys
    public static final String KEY_STUDENT_ID = "student_id";
    public static final String KEY_FULLNAME = "fullname";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_GRADE_LEVEL = "grade_level";
    public static final String KEY_CURRENT_ABILITY = "current_ability";
    public static final String KEY_TOTAL_XP = "total_xp";
    public static final String KEY_CURRENT_STREAK = "current_streak";
    public static final String KEY_SESSION_ID = "session_id";
    public static final String KEY_LAST_ACTIVITY = "last_activity";

    // ==================== Game Configuration ====================
    public static final String GAME_SENTENCE_SCRAMBLE = "SentenceScramble";
    public static final String GAME_TIMED_TRAIL = "TimedTrail";

    public static final int GAME_DEFAULT_ITEMS = 10;
    public static final int GAME_TIME_LIMIT_SECONDS = 60;

    // XP Rewards
    public static final int XP_CORRECT_ANSWER = 10;
    public static final int XP_ACCURACY_BONUS_90 = 50;
    public static final int XP_ACCURACY_BONUS_75 = 25;
    public static final int XP_SPEED_BONUS_FAST = 30;
    public static final int XP_SPEED_BONUS_MEDIUM = 15;

    // ==================== UI Configuration ====================
    public static final int SPLASH_DURATION = 2000; // 2 seconds

    // Toast Display Duration
    public static final int TOAST_SHORT = 2000;
    public static final int TOAST_LONG = 3500;

    // Animation Duration
    public static final int ANIMATION_FADE_DURATION = 300;

    // ==================== Item Types ====================
    public static final String ITEM_TYPE_READING = "Reading";
    public static final String ITEM_TYPE_GRAMMAR = "Grammar";
    public static final String ITEM_TYPE_PRONUNCIATION = "Pronunciation";
    public static final String ITEM_TYPE_SPELLING = "Spelling";
    public static final String ITEM_TYPE_SYNTAX = "Syntax";

    // ==================== Difficulty Levels ====================
    public static final String DIFFICULTY_EASY = "Easy";
    public static final String DIFFICULTY_MEDIUM = "Medium";
    public static final String DIFFICULTY_HARD = "Hard";

    // ==================== Session Types ====================
    public static final String SESSION_PRE_ASSESSMENT = "PreAssessment";
    public static final String SESSION_LESSON = "Lesson";
    public static final String SESSION_POST_ASSESSMENT = "PostAssessment";
    public static final String SESSION_GAME = "Game";

    // ==================== Completion Status ====================
    public static final String STATUS_NOT_STARTED = "NotStarted";
    public static final String STATUS_IN_PROGRESS = "InProgress";
    public static final String STATUS_COMPLETED = "Completed";

    // ==================== Network Configuration ====================
    public static final int NETWORK_TIMEOUT_SECONDS = 30;
    public static final int MAX_RETRY_ATTEMPTS = 3;

    // ==================== Badge Categories ====================
    public static final String BADGE_ACHIEVEMENT = "Achievement";
    public static final String BADGE_FLUENCY = "Fluency";
    public static final String BADGE_PRONUNCIATION = "Pronunciation";
    public static final String BADGE_VOCABULARY = "Vocabulary";
    public static final String BADGE_SPEED = "Speed";
    public static final String BADGE_STREAK = "Streak";

    // ==================== Validation ====================
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    // ==================== Achievement Thresholds ====================
    public static final int STREAK_THRESHOLD_BRONZE = 5;
    public static final int STREAK_THRESHOLD_SILVER = 10;
    public static final int STREAK_THRESHOLD_GOLD = 20;

    public static final double ABILITY_THRESHOLD_BEGINNER = -1.0;
    public static final double ABILITY_THRESHOLD_INTERMEDIATE = 0.5;
    public static final double ABILITY_THRESHOLD_ADVANCED = 1.5;
    public static final double ABILITY_THRESHOLD_EXPERT = 2.5;

    // ==================== Error Messages ====================
    public static final String ERROR_NETWORK = "Network error. Please check your connection.";
    public static final String ERROR_SERVER = "Server error. Please try again later.";
    public static final String ERROR_INVALID_CREDENTIALS = "Invalid email or password.";
    public static final String ERROR_SESSION_EXPIRED = "Your session has expired. Please login again.";
    public static final String ERROR_NO_DATA = "No data available.";

    // ==================== Success Messages ====================
    public static final String SUCCESS_LOGIN = "Login successful!";
    public static final String SUCCESS_ASSESSMENT_COMPLETE = "Assessment completed successfully!";
    public static final String SUCCESS_GAME_COMPLETE = "Great job! Keep it up!";
    public static final String SUCCESS_LESSON_COMPLETE = "Lesson completed!";
}
