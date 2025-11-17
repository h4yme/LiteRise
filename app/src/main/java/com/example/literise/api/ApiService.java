package com.example.literise.api;


import com.example.literise.models.GetNextItemRequest;
import com.example.literise.models.NextItemResponse;
import com.example.literise.models.PreAssessmentResponse;
import com.example.literise.models.PronunciationRequest;
import com.example.literise.models.PronunciationResponse;
import com.example.literise.models.Question;
import com.example.literise.models.ResponseModel;
import com.example.literise.models.SingleResponseResult;
import com.example.literise.models.Students;
import com.example.literise.models.SubmitRequest;
import com.example.literise.models.SubmitResponseResult;
import com.example.literise.models.SubmitSingleRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

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

    // 🎯 Adaptive Testing Endpoints
    @Headers("Content-Type: application/json")
    @POST("get_next_item.php")
    Call<NextItemResponse> getNextItem(@Body GetNextItemRequest request);

    @Headers("Content-Type: application/json")
    @POST("submit_single_response.php")
    Call<SingleResponseResult> submitSingleResponse(@Body SubmitSingleRequest request);
}
