package com.example.literise.models;



import com.google.gson.annotations.SerializedName;

import java.util.List;



public class PreAssessmentResponse {



    @SerializedName("success")

    private boolean success;



    @SerializedName("count")

    private int count;



    @SerializedName("items")

    private List<Question> items;



    public boolean isSuccess() {

        return success;

    }



    public int getCount() {

        return count;

    }



    public List<Question> getItems() {

        return items;

    }

}