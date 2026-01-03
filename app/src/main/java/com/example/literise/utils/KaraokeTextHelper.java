package com.example.literise.utils;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android:text.style.BackgroundColorSpan;
import android:text.style.ForegroundColorSpan;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for karaoke-style text highlighting
 * Highlights words one by one as they are being read
 */
public class KaraokeTextHelper {

    private TextView textView;
    private String fullText;
    private List<String> words;
    private int currentWordIndex = 0;
    private Handler handler;
    private Runnable highlightRunnable;
    private boolean isPlaying = false;
    private long wordDuration = 500; // milliseconds per word (adjustable)

    private int highlightBackgroundColor = Color.parseColor("#FFEB3B"); // Yellow
    private int highlightTextColor = Color.parseColor("#2C3E50"); // Dark text
    private int normalTextColor = Color.parseColor("#2C3E50");

    private KaraokeCallback callback;

    public interface KaraokeCallback {
        void onWordHighlighted(int wordIndex, String word);
        void onReadingComplete();
    }

    public KaraokeTextHelper(TextView textView) {
        this.textView = textView;
        this.handler = new Handler(Looper.getMainLooper());
        this.fullText = textView.getText().toString();
        this.words = splitIntoWords(fullText);
    }

    /**
     * Split text into words, preserving punctuation
     */
    private List<String> splitIntoWords(String text) {
        List<String> wordList = new ArrayList<>();
        String[] parts = text.split("\\s+");
        for (String part : parts) {
            if (!part.isEmpty()) {
                wordList.add(part);
            }
        }
        return wordList;
    }

    /**
     * Start the karaoke highlighting
     */
    public void start(KaraokeCallback callback) {
        this.callback = callback;
        this.isPlaying = true;
        this.currentWordIndex = 0;
        highlightNextWord();
    }

    /**
     * Stop the karaoke highlighting
     */
    public void stop() {
        isPlaying = false;
        if (highlightRunnable != null) {
            handler.removeCallbacks(highlightRunnable);
        }
        resetHighlight();
    }

    /**
     * Pause the karaoke highlighting
     */
    public void pause() {
        isPlaying = false;
        if (highlightRunnable != null) {
            handler.removeCallbacks(highlightRunnable);
        }
    }

    /**
     * Resume the karaoke highlighting
     */
    public void resume() {
        if (!isPlaying && currentWordIndex < words.size()) {
            isPlaying = true;
            highlightNextWord();
        }
    }

    /**
     * Reset to the beginning
     */
    public void reset() {
        stop();
        currentWordIndex = 0;
    }

    /**
     * Highlight the next word
     */
    private void highlightNextWord() {
        if (!isPlaying || currentWordIndex >= words.size()) {
            if (currentWordIndex >= words.size() && callback != null) {
                callback.onReadingComplete();
            }
            resetHighlight();
            return;
        }

        // Create spannable with highlighted current word
        SpannableString spannableString = new SpannableString(fullText);

        int startPos = 0;
        for (int i = 0; i < words.size(); i++) {
            String word = words.get(i);
            int wordStart = fullText.indexOf(word, startPos);
            int wordEnd = wordStart + word.length();

            if (i == currentWordIndex) {
                // Highlight current word
                spannableString.setSpan(
                        new BackgroundColorSpan(highlightBackgroundColor),
                        wordStart,
                        wordEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
                spannableString.setSpan(
                        new ForegroundColorSpan(highlightTextColor),
                        wordStart,
                        wordEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                if (callback != null) {
                    callback.onWordHighlighted(currentWordIndex, word);
                }
            } else if (i < currentWordIndex) {
                // Already read - keep normal color
                spannableString.setSpan(
                        new ForegroundColorSpan(normalTextColor),
                        wordStart,
                        wordEnd,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );
            }

            startPos = wordEnd;
        }

        textView.setText(spannableString);

        // Schedule next word
        currentWordIndex++;
        highlightRunnable = this::highlightNextWord;
        handler.postDelayed(highlightRunnable, wordDuration);
    }

    /**
     * Reset highlight to show normal text
     */
    private void resetHighlight() {
        textView.setText(fullText);
        textView.setTextColor(normalTextColor);
    }

    /**
     * Set the duration for each word in milliseconds
     */
    public void setWordDuration(long durationMs) {
        this.wordDuration = durationMs;
    }

    /**
     * Set reading speed (0 = slow, 1 = normal, 2 = fast)
     */
    public void setReadingSpeed(int speed) {
        switch (speed) {
            case 0: // Slow
                wordDuration = 800;
                break;
            case 1: // Normal
                wordDuration = 500;
                break;
            case 2: // Fast
                wordDuration = 300;
                break;
        }
    }

    /**
     * Calculate total reading time in milliseconds
     */
    public long getTotalDuration() {
        return words.size() * wordDuration;
    }

    /**
     * Get the number of words
     */
    public int getWordCount() {
        return words.size();
    }

    /**
     * Get current word index
     */
    public int getCurrentWordIndex() {
        return currentWordIndex;
    }

    /**
     * Check if currently playing
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Set highlight colors
     */
    public void setHighlightColors(int backgroundColor, int textColor) {
        this.highlightBackgroundColor = backgroundColor;
        this.highlightTextColor = textColor;
    }

    /**
     * Clean up resources
     */
    public void destroy() {
        stop();
        handler = null;
        callback = null;
    }
}
