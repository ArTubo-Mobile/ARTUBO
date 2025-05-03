package com.asucare.app.classes;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class ScanResult {
    private String userId;
    private String diagnosis;
    private float confidence;
    private String imageUrl; // Can be null since we're not uploading images
    private long timestamp;

    // Store raw prediction values for all classes
    private float healthyScore;
    private float mosaicScore;
    private float redRotScore;
    private float rustScore;
    private float yellowScore;

    // Required empty constructor for Firebase
    public ScanResult() {
    }

    public ScanResult(String userId, String diagnosis, float confidence, String imageUrl,
                      long timestamp, float healthyScore, float mosaicScore, float redRotScore,
                      float rustScore, float yellowScore) {
        this.userId = userId;
        this.diagnosis = diagnosis;
        this.confidence = confidence;
        this.imageUrl = imageUrl; // Can be null
        this.timestamp = timestamp;
        this.healthyScore = healthyScore;
        this.mosaicScore = mosaicScore;
        this.redRotScore = redRotScore;
        this.rustScore = rustScore;
        this.yellowScore = yellowScore;
    }

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getHealthyScore() {
        return healthyScore;
    }

    public void setHealthyScore(float healthyScore) {
        this.healthyScore = healthyScore;
    }

    public float getMosaicScore() {
        return mosaicScore;
    }

    public void setMosaicScore(float mosaicScore) {
        this.mosaicScore = mosaicScore;
    }

    public float getRedRotScore() {
        return redRotScore;
    }

    public void setRedRotScore(float redRotScore) {
        this.redRotScore = redRotScore;
    }

    public float getRustScore() {
        return rustScore;
    }

    public void setRustScore(float rustScore) {
        this.rustScore = rustScore;
    }

    public float getYellowScore() {
        return yellowScore;
    }

    public void setYellowScore(float yellowScore) {
        this.yellowScore = yellowScore;
    }

    // Helper method to format date for display
    @Exclude
    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }
}