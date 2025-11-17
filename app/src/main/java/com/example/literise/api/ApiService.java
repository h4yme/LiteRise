package com.example.literise.api;

import com.example.literise.models.BadgeResponse;
import com.example.literise.models.ChangePasswordRequest;
import com.example.literise.models.CreateSessionRequest;
import com.example.literise.models.LeaderboardResponse;
import com.example.literise.models.Question;
import com.example.literise.models.SessionHistoryResponse;
import com.example.literise.models.SessionResponse;
import com.example.literise.models.Students;
import com.example.literise.models.SubmitRequest;
import com.example.literise.models.SubmitResponse;
import com.example.literise.models.Teacher;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // ========== AUTHENTICATION ==========

    // Student Login
    @Headers("Content-Type: application/json")
    @POST("login.php")
    Call<Students> login(@Body Students student);

    // Teacher Login
    @Headers("Content-Type: application/json")
    @POST("teacher_login.php")
    Call<Teacher> teacherLogin(@Body Teacher teacher);

    // ========== ASSESSMENT ==========

    // Create Test Session
    @Headers("Content-Type: application/json")
    @POST("create_session.php")
    Call<SessionResponse> createSession(@Body CreateSessionRequest request);

    // Get Pre-Assessment Items
    @Headers("Content-Type: application/json")
    @POST("get_preassessment_items.php")
    Call<List<Question>> getPreAssessmentItems();

    // Submit Responses
    @Headers("Content-Type: application/json")
    @POST("submit_responses.php")
    Call<SubmitResponse> submitResponses(@Body SubmitRequest request);

    // Update student ability (manual)
    @Headers("Content-Type: application/json")
    @POST("update_ability.php")
    Call<Void> updateAbility(@Body Students student);

    // ========== BADGES & LEADERBOARD ==========

    // Get all available badges
    @GET("get_badges.php")
    Call<BadgeResponse> getAllBadges();

    // Get student's earned badges
    @GET("get_student_badges.php")
    Call<BadgeResponse> getStudentBadges(@Query("StudentID") int studentId);

    // Get leaderboard
    @GET("get_leaderboard.php")
    Call<LeaderboardResponse> getLeaderboard(
            @Query("GradeLevel") int gradeLevel,
            @Query("Limit") int limit
    );

    // ========== STUDENT PROFILE ==========

    // Change password
    @Headers("Content-Type: application/json")
    @POST("change_password.php")
    Call<Map<String, Object>> changePassword(@Body ChangePasswordRequest request);

    // Update profile
    @Headers("Content-Type: application/json")
    @POST("update_profile.php")
    Call<Map<String, Object>> updateProfile(@Body Map<String, Object> profileData);

    // ========== PROGRESS & HISTORY ==========

    // Get session history
    @GET("get_session_history.php")
    Call<SessionHistoryResponse> getSessionHistory(
            @Query("StudentID") int studentId,
            @Query("Limit") int limit
    );

    // Get student progress
    @GET("get_student_progress.php")
    Call<Map<String, Object>> getStudentProgress(@Query("StudentID") int studentId);
}
