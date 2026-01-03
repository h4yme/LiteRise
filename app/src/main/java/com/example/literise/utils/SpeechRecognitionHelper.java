package com.example.literise.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class SpeechRecognitionHelper {

    private static final String TAG = "SpeechRecognitionHelper";

    private Context context;
    private SpeechRecognizer speechRecognizer;
    private RecognitionCallback callback;
    private boolean isListening = false;

    public interface RecognitionCallback {
        void onRecognitionReady();
        void onRecognitionStarted();
        void onRecognitionResult(String recognizedText, float confidence);
        void onRecognitionError(String error);
        void onRecognitionEnded();
    }

    public SpeechRecognitionHelper(Context context) {
        this.context = context;
        initializeSpeechRecognizer();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(new RecognitionListener() {
                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(TAG, "Ready for speech");
                    isListening = true;
                    if (callback != null) {
                        callback.onRecognitionReady();
                    }
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech");
                    if (callback != null) {
                        callback.onRecognitionStarted();
                    }
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // Can be used for waveform visualization
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // Not used
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(TAG, "End of speech");
                    isListening = false;
                }

                @Override
                public void onError(int error) {
                    Log.e(TAG, "Recognition error: " + error);
                    isListening = false;
                    String errorMessage = getErrorMessage(error);
                    if (callback != null) {
                        callback.onRecognitionError(errorMessage);
                    }
                }

                @Override
                public void onResults(Bundle results) {
                    ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    float[] confidenceScores = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);

                    if (matches != null && !matches.isEmpty()) {
                        String recognizedText = matches.get(0);
                        float confidence = (confidenceScores != null && confidenceScores.length > 0)
                                ? confidenceScores[0] : 0.0f;

                        Log.d(TAG, "Recognized: " + recognizedText + " (confidence: " + confidence + ")");

                        if (callback != null) {
                            callback.onRecognitionResult(recognizedText, confidence);
                        }
                    }

                    if (callback != null) {
                        callback.onRecognitionEnded();
                    }
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // Can be used for real-time feedback
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    // Not used
                }
            });
        }
    }

    public void startListening(RecognitionCallback callback) {
        this.callback = callback;

        if (speechRecognizer == null) {
            initializeSpeechRecognizer();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer.startListening(intent);
    }

    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }

    public boolean isListening() {
        return isListening;
    }

    /**
     * Calculate pronunciation accuracy by comparing recognized text with expected word
     * @param expectedWord The word the user should pronounce
     * @param recognizedText The text recognized from speech
     * @return Accuracy score from 0 to 100
     */
    public static int calculatePronunciationAccuracy(String expectedWord, String recognizedText) {
        if (expectedWord == null || recognizedText == null) {
            return 0;
        }

        String expected = expectedWord.toLowerCase().trim();
        String recognized = recognizedText.toLowerCase().trim();

        // Exact match
        if (expected.equals(recognized)) {
            return 100;
        }

        // Check if recognized text contains the expected word
        if (recognized.contains(expected)) {
            return 90;
        }

        // Calculate Levenshtein distance for similarity
        int distance = levenshteinDistance(expected, recognized);
        int maxLength = Math.max(expected.length(), recognized.length());

        if (maxLength == 0) {
            return 100;
        }

        // Convert distance to accuracy percentage
        int accuracy = (int) (((maxLength - distance) / (double) maxLength) * 100);
        return Math.max(0, Math.min(100, accuracy));
    }

    /**
     * Calculate Levenshtein distance between two strings
     */
    private static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(Math.min(
                                    dp[i - 1][j] + 1,      // deletion
                                    dp[i][j - 1] + 1),     // insertion
                            dp[i - 1][j - 1] + cost // substitution
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Microphone permission required";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech detected. Please try again.";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Speech recognizer busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech detected. Please try again.";
            default:
                return "Unknown error occurred";
        }
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        callback = null;
    }
}