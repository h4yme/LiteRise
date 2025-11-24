package com.example.literise.models;



import com.google.gson.annotations.SerializedName;

import java.util.List;



/**

 * API Response for Word Hunt game data

 */

public class WordHuntResponse {



    @SerializedName("success")

    private boolean success;



    @SerializedName("message")

    private String message;



    @SerializedName("words")

    private List<WordHuntWord> words;



    @SerializedName("grid_size")

    private int gridSize;



    @SerializedName("lesson_id")

    private int lessonId;


    @SerializedName("student_grade")

    private int studentGrade;



    @SerializedName("student_id")

    private int studentId;



    @SerializedName("words_count")

    private int wordsCount;

    public WordHuntResponse() {}



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



    public List<WordHuntWord> getWords() {

        return words;

    }



    public void setWords(List<WordHuntWord> words) {

        this.words = words;

    }



    public int getGridSize() {

        return gridSize;

    }



    public void setGridSize(int gridSize) {

        this.gridSize = gridSize;

    }



    public int getLessonId() {

        return lessonId;

    }



    public void setLessonId(int lessonId) {

        this.lessonId = lessonId;

    }

    public int getStudentGrade() {

        return studentGrade;

    }



    public void setStudentGrade(int studentGrade) {

        this.studentGrade = studentGrade;

    }



    public int getStudentId() {

        return studentId;

    }



    public void setStudentId(int studentId) {

        this.studentId = studentId;

    }



    public int getWordsCount() {

        return wordsCount;

    }



    public void setWordsCount(int wordsCount) {

        this.wordsCount = wordsCount;

    }

}
