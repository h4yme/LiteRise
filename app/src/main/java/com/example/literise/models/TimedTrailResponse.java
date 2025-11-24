package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * API Response for Timed Trail game data
 */
public class TimedTrailResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("questions")
    private List<TimedTrailQuestion> questions;

    @SerializedName("lesson_id")
    private int lessonId;

    @SerializedName("total_time_limit")
    private int totalTimeLimit; // Total time for all questions in seconds

    public TimedTrailResponse() {}

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<TimedTrailQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<TimedTrailQuestion> questions) {
        this.questions = questions;
    }

    public int getLessonId() {
        return lessonId;
    }

    public void setLessonId(int lessonId) {
        this.lessonId = lessonId;
    }

    public int getTotalTimeLimit() {
        return totalTimeLimit > 0 ? totalTimeLimit : 180; // Default 3 minutes
    }

    public void setTotalTimeLimit(int totalTimeLimit) {
        this.totalTimeLimit = totalTimeLimit;
    }
}
