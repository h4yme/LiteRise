package com.example.literise.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

public class TextToSpeechHelper {
    
    private TextToSpeech tts;
    private Context context;
    private boolean isInitialized = false;
    private SpeechListener speechListener;

    public interface SpeechListener {
        void onSpeechStart();
        void onSpeechDone();
        void onSpeechError();
    }

    public TextToSpeechHelper(Context context) {
        this.context = context;
        initializeTTS();
    }

    private void initializeTTS() {
        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.US);
                
                if (result == TextToSpeech.LANG_MISSING_DATA || 
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Language not supported
                    isInitialized = false;
                } else {
                    isInitialized = true;
                    
                    // Configure Leo's voice characteristics
                    configureLeoVoice();
                }
            } else {
                isInitialized = false;
            }
        });

        // Set utterance progress listener
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (speechListener != null) {
                    speechListener.onSpeechStart();
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (speechListener != null) {
                    speechListener.onSpeechDone();
                }
            }

            @Override
            public void onError(String utteranceId) {
                if (speechListener != null) {
                    speechListener.onSpeechError();
                }
            }
        });
    }

    private void configureLeoVoice() {
        // Make Leo's voice friendly and kid-appropriate
        tts.setPitch(1.1f);  // Slightly higher pitch for friendliness
        tts.setSpeechRate(0.9f);  // Slightly slower for clarity
    }

    public void speak(String text) {
        speak(text, null);
    }

    public void speak(String text, SpeechListener listener) {
        this.speechListener = listener;
        
        if (isInitialized && text != null && !text.isEmpty()) {
            HashMap<String, String> params = new HashMap<>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "LeoSpeech");
            
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    public void stop() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    public void shutdown() {
        stop();
        if (tts != null) {
            tts.shutdown();
        }
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }
}
