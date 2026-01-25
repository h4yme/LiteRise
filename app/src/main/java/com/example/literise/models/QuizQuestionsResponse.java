package com.example.literise.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizQuestionsResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("quiz")
    private Quiz quiz;

    @SerializedName("message")
    private String message;

    public boolean isSuccess() { return success; }
    public Quiz getQuiz() { return quiz; }
    public String getMessage() { return message; }

    public static class Quiz {
        @SerializedName("node_id")
        private int nodeId;

        @SerializedName("total_questions")
        private int totalQuestions;

        @SerializedName("questions")
        private List<Question> questions;

        public int getNodeId() { return nodeId; }
        public int getTotalQuestions() { return totalQuestions; }
        public List<Question> getQuestions() { return questions; }
    }

    public static class Question {
        @SerializedName("question_id")
        private int questionId;

        @SerializedName("question_text")
        private String questionText;

        @SerializedName("option_a")
        private String optionA;

        @SerializedName("option_b")
        private String optionB;

        @SerializedName("option_c")
        private String optionC;

        @SerializedName("option_d")
        private String optionD;

        public int getQuestionId() { return questionId; }
        public String getQuestionText() { return questionText; }
        public String getOptionA() { return optionA; }
        public String getOptionB() { return optionB; }
        public String getOptionC() { return optionC; }
        public String getOptionD() { return optionD; }
    }
}