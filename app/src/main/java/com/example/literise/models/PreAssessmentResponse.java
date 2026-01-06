package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PreAssessmentResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("count")
    private int count;

    @SerializedName("items")
    private List<AssessmentItem> items;

    public boolean isSuccess() {
        return success;
    }

    public int getCount() {
        return count;
    }

    public List<AssessmentItem> getItems() {
        return items;
    }

    public static class AssessmentItem {
        @SerializedName("ItemID")
        private int itemId;

        @SerializedName("ItemText")
        private String itemText;

        @SerializedName("QuestionText")
        private String questionText;

        @SerializedName("PassageText")
        private String passageText;

        @SerializedName("ItemType")
        private String itemType;

        @SerializedName("DifficultyLevel")
        private String difficultyLevel;

        @SerializedName("DifficultyParam")
        private double difficultyParam;

        @SerializedName("DiscriminationParam")
        private double discriminationParam;

        @SerializedName("GuessingParam")
        private double guessingParam;

        @SerializedName("AnswerChoices")
        private List<String> answerChoices;

        @SerializedName("OptionA")
        private String optionA;

        @SerializedName("OptionB")
        private String optionB;

        @SerializedName("OptionC")
        private String optionC;

        @SerializedName("OptionD")
        private String optionD;

        @SerializedName("CorrectOption")
        private String correctOption;

        @SerializedName("CorrectAnswer")
        private String correctAnswer;

        @SerializedName("Phonetic")
        private String phonetic;

        @SerializedName("Definition")
        private String definition;

        @SerializedName("ImageURL")
        private String imageUrl;

        @SerializedName("AudioURL")
        private String audioUrl;

        public int getItemId() {
            return itemId;
        }

        public String getItemText() {
            return itemText;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getPassageText() {
            return passageText;
        }

        public String getItemType() {
            return itemType;
        }

        public String getDifficultyLevel() {
            return difficultyLevel;
        }

        public double getDifficultyParam() {
            return difficultyParam;
        }

        public double getDiscriminationParam() {
            return discriminationParam;
        }

        public double getGuessingParam() {
            return guessingParam;
        }

        public List<String> getAnswerChoices() {
            return answerChoices;
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

        public String getCorrectOption() {
            return correctOption;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public String getPhonetic() {
            return phonetic;
        }

        public String getDefinition() {
            return definition;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public String getAudioUrl() {
            return audioUrl;
        }
    }
}