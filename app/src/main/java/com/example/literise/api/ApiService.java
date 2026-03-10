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
import com.example.literise.models.AdaptiveQuestionRequest;
import com.example.literise.models.AdaptiveQuestionResponse;
import com.example.literise.models.SubmitAnswerRequest;
import com.example.literise.models.SubmitAnswerResponse;
import com.example.literise.models.GameContentRequest;
import com.example.literise.models.GameContentResponse;
import java.util.List;

import java.util.Map;



import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Body;

import retrofit2.http.Headers;

import retrofit2.http.Multipart;
import retrofit2.http.POST;

import retrofit2.http.Part;
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

    // 🎯 Get Next Adaptive Question (IRT-based)
    @Headers("Content-Type: application/json")
    @POST("get_next_question.php")
    Call<AdaptiveQuestionResponse> getNextAdaptiveQuestion(@Body AdaptiveQuestionRequest request);

    // ✅ Submit Answer (IRT-based tracking)
    @Headers("Content-Type: application/json")
    @POST("submit_answer.php")
    Call<SubmitAnswerResponse> submitAnswer(@Body SubmitAnswerRequest request);

    // 🎤 Evaluate Pronunciation (Audio-based assessment — placement test)
    @Multipart
    @POST("evaluate_pronunciation.php")
    Call<okhttp3.ResponseBody> evaluatePronunciation(
            @Part("student_id") okhttp3.RequestBody studentId,
            @Part("item_id") okhttp3.RequestBody itemId,
            @Part("response_id") okhttp3.RequestBody responseId,
            @Part("session_id") okhttp3.RequestBody sessionId,
            @Part("target_word") okhttp3.RequestBody targetWord,
            @Part okhttp3.MultipartBody.Part audioFile
    );

    // 🎤 Evaluate Game Pronunciation (lightweight — in-game use, no DB recording)
    @Multipart
    @POST("evaluate_game_pronunciation.php")
    Call<okhttp3.ResponseBody> evaluateGamePronunciation(
            @Part("student_id") okhttp3.RequestBody studentId,
            @Part("target_word") okhttp3.RequestBody targetWord,
            @Part okhttp3.MultipartBody.Part audioFile
    );

    // 📚 LESSON FLOW APIs - 3-Phase Adaptive System
    @GET("get_lesson_content.php")
    Call<com.example.literise.models.LessonContentResponse> getLessonContent(
            @Query("node_id") int nodeId,
            @Query("placement_level") int placementLevel
    );

    @GET("get_quiz_questions.php")
    Call<com.example.literise.models.QuizQuestionsResponse> getQuizQuestions(
            @Query("node_id") int nodeId,
            @Query("placement_level") int placementLevel
    );

    @Headers("Content-Type: application/json")
    @POST("submit_quiz.php")
    Call<com.example.literise.models.QuizSubmitResponse> submitQuiz(@Body com.example.literise.models.QuizSubmitRequest request);

    @Headers("Content-Type: application/json")
    @POST("update_node_progress.php")
    Call<com.example.literise.models.UpdateProgressResponse> updateNodeProgress(@Body com.example.literise.models.UpdateProgressRequest request);

    @GET("get_node_progress.php")
    Call<com.example.literise.models.NodeProgressResponse> getNodeProgress(
            @Query("student_id") int studentId,
            @Query("node_id") int nodeId
    );

    @GET("get_module_ladder.php")
    Call<com.example.literise.models.ModuleLadderResponse> getModuleLadder(
            @Query("student_id") int studentId,
            @Query("module_id") int moduleId
    );

    // ✅ Check if all 65 nodes are complete (post-assessment trigger)
    @GET("check_modules_complete.php")
    Call<com.example.literise.models.CheckModulesCompleteResponse> checkModulesComplete(
            @Query("student_id") int studentId
    );

    // 📚 Tutorial progress - check if seen
    @GET("check_tutorial.php")
    Call<com.example.literise.models.TutorialStatusResponse> checkTutorial(
            @Query("student_id") int studentId,
            @Query("tutorial_key") String tutorialKey
    );

    // 📚 Tutorial progress - mark as complete
    @Headers("Content-Type: application/json")
    @POST("complete_tutorial.php")
    Call<com.example.literise.models.ResponseModel> completeTutorial(
            @Body com.example.literise.models.CompleteTutorialRequest request
    );

    // 🤖 AI-generated game content from lesson JSON (Claude API via backend)
    @Headers("Content-Type: application/json")
    @POST("generate_game_content.php")
    Call<GameContentResponse> generateGameContent(@Body GameContentRequest request);

    // 🏅 Get all badges with earned status for a student
    @GET("get_badges.php")
    Call<com.example.literise.models.BadgesResponse> getBadges(
            @Query("student_id") int studentId
    );

    // 🏅 Award badge after completing a node
    @Headers("Content-Type: application/json")
    @POST("award_badge.php")
    Call<com.example.literise.models.AwardBadgeResponse> awardBadge(
            @Body com.example.literise.models.AwardBadgeRequest request
    );
}