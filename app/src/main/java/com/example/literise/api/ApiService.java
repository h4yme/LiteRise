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

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
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
}
