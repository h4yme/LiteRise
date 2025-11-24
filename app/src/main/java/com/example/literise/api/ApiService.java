package com.example.literise.api;

import com.example.literise.models.GetNextItemRequest;
import com.example.literise.models.LessonProgressResponse;
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
import com.example.literise.models.TimedTrailResponse;
import com.example.literise.models.WordHuntResponse;
import com.example.literise.models.Students;
import com.example.literise.models.SubmitRequest;
import com.example.literise.models.SubmitResponseResult;
import com.example.literise.models.SubmitSingleRequest;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // 🧾 Login
    @Headers("Content-Type: application/json")
    @POST("login.php")
    Call<Students> login(@Body Students student);

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

    // 🎮 Game Endpoints

    /**
     * Get scramble sentences for Sentence Scramble game
     * @param count Number of sentences to return
     * @return ScrambleSentenceResponse with list of sentences
     */
    @Headers("Content-Type: application/json")
    @POST("get_scramble_sentences.php")
    Call<ScrambleSentenceResponse> getScrambleSentences(@Query("count") int count);

    /**
     * Get scramble sentences for a specific lesson
     * @param count Number of sentences to return
     * @param lessonId The lesson to get content for
     * @return ScrambleSentenceResponse with list of sentences
     */
    @Headers("Content-Type: application/json")
    @POST("get_scramble_sentences.php")
    Call<ScrambleSentenceResponse> getScrambleSentences(
            @Query("count") int count,
            @Query("lesson_id") int lessonId
    );

    /**
     * Save game result and update student XP
     * @param request SaveGameResultRequest with game data
     * @return SaveGameResultResponse with updated student stats
     */
    @Headers("Content-Type: application/json")
    @POST("save_game_results.php")
    Call<SaveGameResultResponse> saveGameResult(@Body SaveGameResultRequest request);

    // Word Hunt Game Endpoints

    /**
     * Get words for Word Hunt game
     * @param count Number of words to return
     * @param studentId Student ID to get grade-appropriate words
     * @return WordHuntResponse with list of words
     */
    @Headers("Content-Type: application/json")
    @POST("get_word_hunt.php")
    Call<WordHuntResponse> getWordHuntWords(
            @Query("count") int count,
            @Query("student_id") int studentId
    );

    /**
     * Get words for Word Hunt game for a specific lesson
     * @param count Number of words to return
     * @param lessonId The lesson to get content for
     * @param studentId Student ID to get grade-appropriate words
     * @return WordHuntResponse with list of words
     */
    @Headers("Content-Type: application/json")
    @POST("get_word_hunt.php")
    Call<WordHuntResponse> getWordHuntWords(
            @Query("count") int count,
            @Query("lesson_id") int lessonId,
            @Query("student_id") int studentId
    );

    // Timed Trail Game Endpoints

    /**
     * Get reading passages for Timed Trail game
     * @param count Number of questions to return
     * @return TimedTrailResponse with list of passages and questions
     */
    @Headers("Content-Type: application/json")
    @POST("get_timed_trail.php")
    Call<TimedTrailResponse> getTimedTrailQuestions(@Query("count") int count);

    /**
     * Get reading passages for Timed Trail for a specific lesson
     * @param count Number of questions to return
     * @param lessonId The lesson to get content for
     * @return TimedTrailResponse with list of passages and questions
     */
    @Headers("Content-Type: application/json")
    @POST("get_timed_trail.php")
    Call<TimedTrailResponse> getTimedTrailQuestions(
            @Query("count") int count,
            @Query("lesson_id") int lessonId
    );

    // Lesson Progress Endpoints

    /**
     * Get lesson progress for all lessons
     * @param studentId Student ID
     * @return LessonProgressResponse with progress for all lessons
     */
    @GET("get_lesson_progress.php")
    Call<LessonProgressResponse> getLessonProgress(@Query("student_id") int studentId);

    /**
     * Get lesson progress for a specific lesson
     * @param studentId Student ID
     * @param lessonId Lesson ID
     * @return LessonProgressResponse with progress for the lesson
     */
    @GET("get_lesson_progress.php")
    Call<LessonProgressResponse> getLessonProgress(
            @Query("student_id") int studentId,
            @Query("lesson_id") int lessonId
    );
}
