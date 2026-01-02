package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

public class VerifyOTPRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("otp_code")
    private String otpCode;

    public VerifyOTPRequest(String email, String otpCode) {
        this.email = email;
        this.otpCode = otpCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }
}