package com.example.literise.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.models.LogSessionRequest;
import com.example.literise.models.LogSessionResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SessionLogger {

    private static final String TAG = "SessionLogger";

    public static void logLogin(Context context, int studentId) {
        logSession(context, studentId, "Login", "app_launch");
    }

    public static void logLogout(Context context, int studentId) {
        logSession(context, studentId, "Logout", "user_logout");
    }

    public static void logAssessmentStart(Context context, int studentId, String assessmentType) {
        String tag = assessmentType.toLowerCase() + "_assessment";
        logSession(context, studentId, "AssessmentStart", tag);
    }

    public static void logAssessmentComplete(Context context, int studentId, String assessmentType) {
        String tag = assessmentType.toLowerCase() + "_assessment";
        logSession(context, studentId, "AssessmentComplete", tag);
    }

    public static void logLessonStart(Context context, int studentId, String lessonTag) {
        logSession(context, studentId, "LessonStart", lessonTag);
    }

    public static void logLessonComplete(Context context, int studentId, String lessonTag) {
        logSession(context, studentId, "LessonComplete", lessonTag);
    }

    public static void logGameStart(Context context, int studentId, String gameTag) {
        logSession(context, studentId, "GameStart", gameTag);
    }

    public static void logGameComplete(Context context, int studentId, String gameTag) {
        logSession(context, studentId, "GameComplete", gameTag);
    }

    private static void logSession(Context context, int studentId, String sessionType, String sessionTag) {
        // Skip API call in demo mode
        if (com.example.literise.utils.AppConfig.DEMO_MODE) {
            Log.d(TAG, "Demo mode: Skipping session log - " + sessionType + " - " + sessionTag);
            return;
        }

        String deviceInfo = getDeviceInfo();

        LogSessionRequest request = new LogSessionRequest(studentId, sessionType, sessionTag, deviceInfo);
        request.addAdditionalData("app_version", getAppVersion(context));
        request.addAdditionalData("sdk_version", Build.VERSION.SDK_INT);

        ApiService apiService = ApiClient.getClient(context).create(ApiService.class);
        Call<LogSessionResponse> call = apiService.logSession(request);

        call.enqueue(new Callback<LogSessionResponse>() {
            @Override
            public void onResponse(Call<LogSessionResponse> call, Response<LogSessionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Session logged successfully: " + sessionType + " - " + sessionTag);
                } else {
                    Log.e(TAG, "Failed to log session: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LogSessionResponse> call, Throwable t) {
                Log.e(TAG, "Error logging session: " + t.getMessage());
            }
        });
    }

    private static String getDeviceInfo() {
        return Build.MANUFACTURER + " " + Build.MODEL + ", Android " + Build.VERSION.RELEASE;
    }

    private static String getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return "1.0";
        }
    }
}
