package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class LrnLoginRequest {

    @SerializedName("lrn")
    private String lrn;

    public LrnLoginRequest(String lrn) {
        this.lrn = lrn;
    }

    public String getLrn() { return lrn; }
}
