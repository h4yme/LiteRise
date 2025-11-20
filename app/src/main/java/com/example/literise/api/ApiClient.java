package com.example.literise.api;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    // TODO: Change this to your XAMPP server IP address
    // For local testing: http://10.0.2.2/api/ (Android emulator)
    // For physical device: http://YOUR_PC_IP/api/ (e.g., http://192.168.1.100/api/)
    private static final String BASE_URL = "http://10.0.2.2/api/";
    private static Retrofit retrofit;

    public static Retrofit getClient(Context context) {
        if (retrofit == null) {

            // Logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Auth interceptor to add JWT token to requests
            AuthInterceptor authInterceptor = new AuthInterceptor(context);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor) // Add auth interceptor first
                    .addInterceptor(logging) // Then logging
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    // Backward compatibility - for calls without context (will not have auth)
    public static Retrofit getClient() {
        return getClient(null);
    }
}
