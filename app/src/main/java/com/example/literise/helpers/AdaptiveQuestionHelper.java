package com.example.literise.helpers;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;
import com.example.literise.models.AdaptiveQuestionRequest;
import com.example.literise.models.AdaptiveQuestionResponse;
import com.example.literise.models.SubmitAnswerRequest;
import com.example.literise.models.SubmitAnswerResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Helper class for managing adaptive question selection using IRT-based API
 */
public class AdaptiveQuestionHelper {

    private static final String TAG = "AdaptiveQuestionHelper";

    private Context context;
    private SessionManager sessionManager;
    private ApiService apiService;
    private int sessionId;
    private String assessmentType;
    private double currentTheta; // Current ability estimate
    private int questionsAnswered;
    private String currentCategory; // For category-specific testing

    public AdaptiveQuestionHelper(Context context, int sessionId, String assessmentType) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getClient(context).create(ApiService.class);
        this.sessionId = sessionId;
        this.assessmentType = assessmentType;
        this.currentTheta = 0.0; // Start at average ability
        this.questionsAnswered = 0;
    }

    /**
     * Fetch the next adaptive question from the API
     */
    public void getNextQuestion(QuestionCallback callback) {
        getNextQuestion(null, callback);
    }

    /**
     * Fetch the next adaptive question with optional category filter
     */
    public void getNextQuestion(String category, QuestionCallback callback) {
        int studentId = sessionManager.getStudentId();

        AdaptiveQuestionRequest request;
        if (category != null && !category.isEmpty()) {
            request = new AdaptiveQuestionRequest(studentId, sessionId, currentTheta, assessmentType, category);
        } else {
            request = new AdaptiveQuestionRequest(studentId, sessionId, currentTheta, assessmentType);
        }

        Log.d(TAG, "Requesting next question - Theta: " + currentTheta + ", Category: " + category);

        apiService.getNextAdaptiveQuestion(request).enqueue(new Callback<AdaptiveQuestionResponse>() {
            @Override
            public void onResponse(Call<AdaptiveQuestionResponse> call, Response<AdaptiveQuestionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AdaptiveQuestionResponse apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getQuestion() != null) {
                        Log.d(TAG, "Received question - Difficulty: " + apiResponse.getQuestion().getDifficulty());
                        callback.onSuccess(apiResponse);
                    } else {
                        String error = apiResponse.getError() != null ? apiResponse.getError() : "No question available";
                        Log.e(TAG, "API returned error: " + error);
                        callback.onError(error);
                    }
                } else {
                    Log.e(TAG, "API request failed: " + response.code());
                    callback.onError("Failed to fetch question: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<AdaptiveQuestionResponse> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Submit a student's answer to the API
     */
    public void submitAnswer(int itemId, String selectedAnswer, boolean isCorrect,
                            int responseTime, AnswerCallback callback) {
        int studentId = sessionManager.getStudentId();
        questionsAnswered++;

        String deviceInfo = getDeviceInfo();

        SubmitAnswerRequest request = new SubmitAnswerRequest(
                studentId, itemId, sessionId, assessmentType,
                selectedAnswer, isCorrect, currentTheta,
                questionsAnswered, deviceInfo
        );

        if (responseTime > 0) {
            request.setResponseTime(responseTime);
        }

        Log.d(TAG, "Submitting answer - ItemID: " + itemId + ", Correct: " + isCorrect);

        apiService.submitAnswer(request).enqueue(new Callback<SubmitAnswerResponse>() {
            @Override
            public void onResponse(Call<SubmitAnswerResponse> call, Response<SubmitAnswerResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    SubmitAnswerResponse apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getFeedback() != null) {
                        // Update theta with server's estimate
                        currentTheta = apiResponse.getFeedback().getNewThetaEstimate();
                        Log.d(TAG, "Answer submitted - New Theta: " + currentTheta);
                        callback.onSuccess(apiResponse);
                    } else {
                        String error = apiResponse.getError() != null ? apiResponse.getError() : "Failed to submit";
                        Log.e(TAG, "API returned error: " + error);
                        callback.onError(error);
                    }
                } else {
                    Log.e(TAG, "API request failed: " + response.code());
                    callback.onError("Failed to submit answer: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<SubmitAnswerResponse> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Get current ability estimate (theta)
     */
    public double getCurrentTheta() {
        return currentTheta;
    }

    /**
     * Get number of questions answered
     */
    public int getQuestionsAnswered() {
        return questionsAnswered;
    }

    /**
     * Set current category for filtering
     */
    public void setCurrentCategory(String category) {
        this.currentCategory = category;
    }

    /**
     * Get device information for analytics
     */
    private String getDeviceInfo() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String androidVersion = "Android " + Build.VERSION.RELEASE;
        return manufacturer + " " + model + ", " + androidVersion;
    }

    /**
     * Callback interface for question fetching
     */
    public interface QuestionCallback {
        void onSuccess(AdaptiveQuestionResponse response);
        void onError(String error);
    }

    /**
     * Callback interface for answer submission
     */
    public interface AnswerCallback {
        void onSuccess(SubmitAnswerResponse response);
        void onError(String error);
    }
}
