package com.example.literise.api;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HTTP Interceptor to automatically add JWT token to API requests
 */
public class AuthInterceptor implements Interceptor {

    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();

        // Get token from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("LiteRisePrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);

        // If token exists, add to Authorization header
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        // Proceed without token if not available
        return chain.proceed(originalRequest);
    }
}
