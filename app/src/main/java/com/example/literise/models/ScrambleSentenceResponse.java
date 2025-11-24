package com.example.literise.models;



import com.google.gson.annotations.SerializedName;



import java.util.List;



/**

 * Response model for the get_scramble_sentences API endpoint

 */

public class ScrambleSentenceResponse {



    @SerializedName("success")

    private boolean success;



    @SerializedName("sentences")

    private List<ScrambleSentence> sentences;



    @SerializedName("total")

    private int total;



    @SerializedName("message")

    private String message;



    @SerializedName("error")

    private String error;



    // Empty constructor

    public ScrambleSentenceResponse() {}



    // Getters

    public boolean isSuccess() {

        return success;

    }



    public List<ScrambleSentence> getSentences() {

        return sentences;

    }



    public int getTotal() {

        return total;

    }



    public String getMessage() {

        return message;

    }



    public String getError() {

        return error;

    }



    // Setters

    public void setSuccess(boolean success) {

        this.success = success;

    }



    public void setSentences(List<ScrambleSentence> sentences) {

        this.sentences = sentences;

    }



    public void setTotal(int total) {

        this.total = total;

    }



    public void setMessage(String message) {

        this.message = message;

    }



    public void setError(String error) {

        this.error = error;

    }

}