package com.example.literise.api;

import com.example.literise.models.GetNextItemRequest;

import com.example.literise.models.NextItemResponse;

import com.example.literise.models.PreAssessmentResponse;

import com.example.literise.models.PronunciationRequest;

import com.example.literise.models.PronunciationResponse;

import com.example.literise.models.Question;

import com.example.literise.models.ResponseModel;

import com.example.literise.models.SaveGameResultRequest;
import com.example.literise.models.SaveGameResultResponse;
import com.example.literise.models.ScrambleSentenceResponse;

import com.example.literise.models.SingleResponseResult;

import com.example.literise.models.Students;

import com.example.literise.models.SubmitRequest;

import com.example.literise.models.SubmitResponseResult;

import com.example.literise.models.SubmitSingleRequest;

import com.example.literise.models.WordHuntResponse;
import com.example.literise.models.LessonProgressResponse;
import com.example.literise.models.RegisterRequest;
import com.example.literise.models.RegisterResponse;
import com.example.literise.models.ForgotPasswordRequest;
import com.example.literise.models.ForgotPasswordResponse;
import com.example.literise.models.VerifyOTPRequest;
import com.example.literise.models.VerifyOTPResponse;
import com.example.literise.models.ResetPasswordRequest;
import com.example.literise.models.ResetPasswordResponse;
import com.example.literise.models.SavePlacementResultRequest;
import com.example.literise.models.SavePlacementResultResponse;
import com.example.literise.models.PlacementProgressResponse;
import com.example.literise.models.LogSessionRequest;
import com.example.literise.models.LogSessionResponse;
import java.util.List;

import java.util.Map;



import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;

import retrofit2.http.Headers;

import retrofit2.http.POST;

import retrofit2.http.Query;

public interface ApiService {

    // 🧾 Login
    @Headers("Content-Type: application/json")
    @POST("login.php")
    Call<Students> login(@Body Students student);

    // 📝 Register
    @Headers("Content-Type: application/json")
    @POST("register.php")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    // 🔑 Forgot Password - Request OTP
    @Headers("Content-Type: application/json")
    @POST("forgot_password.php")
    Call<ForgotPasswordResponse> forgotPassword(@Body ForgotPasswordRequest request);

    // 🔍 Verify OTP
    @Headers("Content-Type: application/json")
    @POST("verify_otp.php")
    Call<VerifyOTPResponse> verifyOTP(@Body VerifyOTPRequest request);

    // 🔄 Reset Password
    @Headers("Content-Type: application/json")
    @POST("reset_password.php")
    Call<ResetPasswordResponse> resetPassword(@Body ResetPasswordRequest request);

    @Headers("Content-Type: application/json")
    @POST("get_preassessment_items.php")
    Call<PreAssessmentResponse> getPreAssessmentItems();

    @Headers("Content-Type: application/json")
    @POST("submit_responses.php")
    Call<SubmitResponseResult> submitResponses(@Body SubmitRequest request);

    @Headers("Content-Type: application/json")

    @POST("check_pronunciation.php")

    Call<PronunciationResponse> checkPronunciation(@Body PronunciationRequest request);

    // ⚙️ Update student ability (calls SP_UpdateStudentAbility)
    @Headers("Content-Type: application/json")
    @POST("update_ability.php")
    Call<Void> updateAbility(@Body Students student);

    @Headers("Content-Type: application/json")
    @POST("get_next_item.php")
    Call<NextItemResponse> getNextItem(@Body GetNextItemRequest request);
    @Headers("Content-Type: application/json")
    @POST("submit_single_response.php")
    Call<SingleResponseResult> submitSingleResponse(@Body SubmitSingleRequest request);

    @Headers("Content-Type: application/json")
    @POST("get_scramble_sentences.php")
    Call<ScrambleSentenceResponse> getScrambleSentences(@Query("count") int count);

    @Headers("Content-Type: application/json")
    @POST("get_scramble_sentences.php")
    Call<ScrambleSentenceResponse> getScrambleSentences(
            @Query("count") int count,
            @Query("lesson_id") int lessonId
    );

    @Headers("Content-Type: application/json")
    @POST("save_game_results.php")
    Call<SaveGameResultResponse> saveGameResult(@Body SaveGameResultRequest request);


    @Headers("Content-Type: application/json")
    @POST("get_word_hunt.php")
    Call<WordHuntResponse> getWordHuntWords(
            @Query("count") int count,
            @Query("student_id") int studentId

    );

    @Headers("Content-Type: application/json")
    @POST("get_word_hunt.php")
    Call<WordHuntResponse> getWordHuntWords(

            @Query("count") int count,

            @Query("lesson_id") int lessonId,

            @Query("student_id") int studentId);


    @GET("get_lesson_progress.php")

    Call<LessonProgressResponse> getLessonProgress(@Query("student_id") int studentId);

    @GET("get_lesson_progress.php")

    Call<LessonProgressResponse> getLessonProgress(

            @Query("student_id") int studentId,

            @Query("lesson_id") int lessonId

    );

    // 📝 Save Nickname

    @Headers("Content-Type: application/json")

    @POST("save_nickname.php")

    Call<ResponseModel> saveNickname(@Body Students student);

    // 📊 Save Placement Result
    @Headers("Content-Type: application/json")
    @POST("save_placement_result.php")
    Call<SavePlacementResultResponse> savePlacementResult(@Body SavePlacementResultRequest request);

    // 📈 Get Placement Progress
    @GET("get_placement_progress.php")
    Call<PlacementProgressResponse> getPlacementProgress(@Query("student_id") int studentId);

    // 📝 Log Session
    @Headers("Content-Type: application/json")
    @POST("log_session.php")
    Call<LogSessionResponse> logSession(@Body LogSessionRequest request);
}