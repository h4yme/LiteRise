package com.example.literise.api;

import com.example.literise.models.Students;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("login.php")
    Call<Students> login(@Body Students student);
}
