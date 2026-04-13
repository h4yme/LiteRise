package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class LrnLoginRequest {

    @SerializedName("lrn")
    private String lrn;

    @SerializedName("login_code")
    private String loginCode;

    public LrnLoginRequest(String lrn, String loginCode) {
        this.lrn       = lrn;
        this.loginCode = loginCode;
    }

    public String getLrn()       { return lrn; }
    public String getLoginCode() { return loginCode; }
}