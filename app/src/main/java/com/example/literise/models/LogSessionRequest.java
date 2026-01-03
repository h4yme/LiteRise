package com.example.literise.models;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class LogSessionRequest {

    @SerializedName("student_id")
    private int studentId;

    @SerializedName("session_type")
    private String sessionType;

    @SerializedName("session_tag")
    private String sessionTag;

    @SerializedName("device_info")
    private String deviceInfo;

    @SerializedName("additional_data")
    private Map<String, Object> additionalData;

    public LogSessionRequest(int studentId, String sessionType, String sessionTag, String deviceInfo) {
        this.studentId = studentId;
        this.sessionType = sessionType;
        this.sessionTag = sessionTag;
        this.deviceInfo = deviceInfo;
        this.additionalData = new HashMap<>();
    }

    public void addAdditionalData(String key, Object value) {
        additionalData.put(key, value);
    }

    public int getStudentId() {
        return studentId;
    }

    public String getSessionType() {
        return sessionType;
    }

    public String getSessionTag() {
        return sessionTag;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }
}
