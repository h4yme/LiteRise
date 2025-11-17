package com.example.literise.api;

import com.example.literise.models.CreateSessionRequest;
import com.example.literise.models.Question;
import com.example.literise.models.SessionResponse;
import com.example.literise.models.Students;
import com.example.literise.models.SubmitRequest;
import com.example.literise.models.SubmitResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    // Login
    @Headers("Content-Type: application/json")
    @POST("login.php")
    Call<Students> login(@Body Students student);

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

    // Update student ability
    @Headers("Content-Type: application/json")
    @POST("update_ability.php")
    Call<Void> updateAbility(@Body Students student);
}
