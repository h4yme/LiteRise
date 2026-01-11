package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class AdaptiveQuestionResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("question")
    private QuestionData question;

    @SerializedName("progress")
    private ProgressData progress;

    @SerializedName("error")
    private String error;

    @SerializedName("message")
    private String message;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public QuestionData getQuestion() {
        return question;
    }

    public ProgressData getProgress() {
        return progress;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    // Inner class for question data
    public static class QuestionData {
        @SerializedName("item_id")
        private int itemId;

        @SerializedName("category")
        private String category;

        @SerializedName("subcategory")
        private String subcategory;

        @SerializedName("skill_area")
        private String skillArea;

        @SerializedName("question_text")
        private String questionText;

        @SerializedName("question_type")
        private String questionType;

        @SerializedName("reading_passage")
        private String readingPassage;

        @SerializedName("option_a")
        private String optionA;

        @SerializedName("option_b")
        private String optionB;

        @SerializedName("option_c")
        private String optionC;

        @SerializedName("option_d")
        private String optionD;

        @SerializedName("difficulty")
        private double difficulty;

        @SerializedName("discrimination")
        private double discrimination;

        @SerializedName("estimated_time")
        private int estimatedTime;

        // Getters
        public int getItemId() {
            return itemId;
        }

        public String getCategory() {
            return category;
        }

        public String getSubcategory() {
            return subcategory;
        }

        public String getSkillArea() {
            return skillArea;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getQuestionType() {
            return questionType;
        }

        public String getReadingPassage() {
            return readingPassage;
        }

        public String getOptionA() {
            return optionA;
        }

        public String getOptionB() {
            return optionB;
        }

        public String getOptionC() {
            return optionC;
        }

        public String getOptionD() {
            return optionD;
        }

        public double getDifficulty() {
            return difficulty;
        }

        public double getDiscrimination() {
            return discrimination;
        }

        public int getEstimatedTime() {
            return estimatedTime;
        }
    }

    // Inner class for progress data
    public static class ProgressData {
        @SerializedName("questions_answered")
        private int questionsAnswered;

        @SerializedName("accuracy")
        private double accuracy;

        @SerializedName("current_theta")
        private double currentTheta;

        // Getters
        public int getQuestionsAnswered() {
            return questionsAnswered;
        }

        public double getAccuracy() {
            return accuracy;
        }

        public double getCurrentTheta() {
            return currentTheta;
        }
    }
}