package com.example.literise.helpers;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import com.example.literise.api.ApiClient;
import com.example.literise.api.ApiService;
import com.example.literise.database.SessionManager;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class for pronunciation assessment
 * Handles audio recording and pronunciation evaluation via API
 */
public class PronunciationHelper {

    private static final String TAG = "PronunciationHelper";

    private Context context;
    private SessionManager sessionManager;
    private ApiService apiService;
    private MediaRecorder mediaRecorder;
    private File audioFile;
    private boolean isRecording = false;
    private long recordingStartTime;

    // Callbacks
    public interface PronunciationCallback {
        void onRecordingStarted();
        void onRecordingStopped(File audioFile, int durationMs);
        void onRecordingError(String error);
    }

    public interface EvaluationCallback {
        void onEvaluationSuccess(PronunciationResult result);
        void onEvaluationError(String error);
    }

    /**
     * Pronunciation evaluation result
     */
    public static class PronunciationResult {
        private String recognizedText;
        private double confidence;
        private int overallAccuracy;
        private double pronunciationScore;
        private double fluencyScore;
        private double completenessScore;
        private String feedback;
        private boolean passed;
        private int minimumAccuracy;

        // Getters
        public String getRecognizedText() { return recognizedText; }
        public double getConfidence() { return confidence; }
        public int getOverallAccuracy() { return overallAccuracy; }
        public double getPronunciationScore() { return pronunciationScore; }
        public double getFluencyScore() { return fluencyScore; }
        public double getCompletenessScore() { return completenessScore; }
        public String getFeedback() { return feedback; }
        public boolean isPassed() { return passed; }
        public int getMinimumAccuracy() { return minimumAccuracy; }

        // Setters
        public void setRecognizedText(String recognizedText) { this.recognizedText = recognizedText; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public void setOverallAccuracy(int overallAccuracy) { this.overallAccuracy = overallAccuracy; }
        public void setPronunciationScore(double pronunciationScore) { this.pronunciationScore = pronunciationScore; }
        public void setFluencyScore(double fluencyScore) { this.fluencyScore = fluencyScore; }
        public void setCompletenessScore(double completenessScore) { this.completenessScore = completenessScore; }
        public void setFeedback(String feedback) { this.feedback = feedback; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public void setMinimumAccuracy(int minimumAccuracy) { this.minimumAccuracy = minimumAccuracy; }
    }

    public PronunciationHelper(Context context) {
        this.context = context;
        this.sessionManager = new SessionManager(context);
        this.apiService = ApiClient.getClient(context).create(ApiService.class);
    }

    /**
     * Start recording audio
     */
    public void startRecording(PronunciationCallback callback) {
        if (isRecording) {
            callback.onRecordingError("Already recording");
            return;
        }

        try {
            // Create audio file
            File audioDir = new File(context.getCacheDir(), "pronunciation");
            if (!audioDir.exists()) {
                audioDir.mkdirs();
            }

            audioFile = new File(audioDir, "recording_" + System.currentTimeMillis() + ".3gp");

            // Initialize MediaRecorder
            mediaRecorder = new MediaRecorder();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            } else {
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            }

            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(audioFile.getAbsolutePath());

            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            recordingStartTime = System.currentTimeMillis();

            callback.onRecordingStarted();
            Log.d(TAG, "Recording started: " + audioFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Failed to start recording", e);
            callback.onRecordingError("Failed to start recording: " + e.getMessage());
            cleanupRecorder();
        }
    }

    /**
     * Stop recording audio
     */
    public void stopRecording(PronunciationCallback callback) {
        if (!isRecording) {
            callback.onRecordingError("Not recording");
            return;
        }

        try {
            mediaRecorder.stop();
            int duration = (int) (System.currentTimeMillis() - recordingStartTime);
            isRecording = false;

            callback.onRecordingStopped(audioFile, duration);
            Log.d(TAG, "Recording stopped. Duration: " + duration + "ms");

        } catch (Exception e) {
            Log.e(TAG, "Error stopping recording", e);
            callback.onRecordingError("Failed to stop recording: " + e.getMessage());
        } finally {
            cleanupRecorder();
        }
    }

    /**
     * Evaluate pronunciation by sending audio to API
     */
    public void evaluatePronunciation(int itemId, int responseId, String targetWord,
                                      File audioFile, EvaluationCallback callback) {
        int studentId = sessionManager.getStudentId();

        if (studentId == 0) {
            callback.onEvaluationError("Student not logged in");
            return;
        }

        if (audioFile == null || !audioFile.exists()) {
            Log.e(TAG, "Audio file not found: " + (audioFile != null ? audioFile.getAbsolutePath() : "null"));
            callback.onEvaluationError("Audio file not found");
            return;
        }

        Log.d(TAG, "Audio file exists: " + audioFile.getAbsolutePath() + ", Size: " + audioFile.length() + " bytes");

        // Prepare multipart request
        RequestBody studentIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(studentId));
        RequestBody itemIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(itemId));
        RequestBody responseIdBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(responseId));
        RequestBody targetWordBody = RequestBody.create(MediaType.parse("text/plain"), targetWord);

        RequestBody audioRequestBody = RequestBody.create(MediaType.parse("audio/3gp"), audioFile);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData(
                "audio_file",
                audioFile.getName(),
                audioRequestBody
        );

        Log.d(TAG, "Evaluating pronunciation - StudentID: " + studentId + ", Item: " + itemId + ", Target: " + targetWord);
        Log.d(TAG, "Making API call to evaluate_pronunciation.php...");

        // Make API call
        Call<ResponseBody> call = apiService.evaluatePronunciation(
                studentIdBody,
                itemIdBody,
                responseIdBody,
                targetWordBody,
                audioPart
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "API Response received - Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "Pronunciation API Response: " + jsonResponse);

                        JSONObject json = new JSONObject(jsonResponse);

                        if (json.getBoolean("success")) {
                            JSONObject resultObj = json.getJSONObject("pronunciation_result");

                            PronunciationResult result = new PronunciationResult();
                            result.setRecognizedText(resultObj.getString("recognized_text"));
                            result.setConfidence(resultObj.getDouble("confidence"));
                            result.setOverallAccuracy(resultObj.getInt("overall_accuracy"));
                            result.setPronunciationScore(resultObj.getDouble("pronunciation_score"));
                            result.setFluencyScore(resultObj.getDouble("fluency_score"));
                            result.setCompletenessScore(resultObj.getDouble("completeness_score"));
                            result.setFeedback(resultObj.getString("feedback"));
                            result.setPassed(resultObj.getBoolean("passed"));
                            result.setMinimumAccuracy(resultObj.getInt("minimum_accuracy"));

                            Log.d(TAG, "Pronunciation evaluation successful - Accuracy: " + result.getOverallAccuracy() + "%");
                            callback.onEvaluationSuccess(result);
                        } else {
                            String error = json.optString("error", "Evaluation failed");
                            Log.e(TAG, "API returned error: " + error);
                            callback.onEvaluationError(error);
                        }

                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing pronunciation response", e);
                        callback.onEvaluationError("Failed to parse response: " + e.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "API error " + response.code() + ": " + errorBody);
                        callback.onEvaluationError("API error: " + response.code() + " - " + errorBody);
                    } catch (IOException e) {
                        Log.e(TAG, "API error: " + response.code());
                        callback.onEvaluationError("API error: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Pronunciation evaluation failed", t);
                callback.onEvaluationError("Network error: " + t.getMessage());
            }
        });
    }

    /**
     * Check if currently recording
     */
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * Get current audio file
     */
    public File getAudioFile() {
        return audioFile;
    }

    /**
     * Clean up recorder resources
     */
    private void cleanupRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing media recorder", e);
            }
            mediaRecorder = null;
        }
    }

    /**
     * Release resources
     */
    public void release() {
        cleanupRecorder();
        isRecording = false;

        // Clean up old audio files
        File audioDir = new File(context.getCacheDir(), "pronunciation");
        if (audioDir.exists() && audioDir.isDirectory()) {
            File[] files = audioDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        file.delete();
                    }
                }
            }
        }
    }
}