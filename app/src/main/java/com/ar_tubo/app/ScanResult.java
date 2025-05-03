package com.ar_tubo.app;

/**
 * Model class for scan results
 */
public class ScanResult {
    private String id;
    private String diseaseDetected;
    private String confidenceLevel;
    private String timestamp;
    private String plantType;

    // Default constructor required for Firebase
    public ScanResult() {
    }

    public ScanResult(String id, String diseaseDetected, String confidenceLevel, String timestamp, String plantType) {
        this.id = id;
        this.diseaseDetected = diseaseDetected;
        this.confidenceLevel = confidenceLevel;
        this.timestamp = timestamp;
        this.plantType = plantType;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDiseaseDetected() {
        return diseaseDetected;
    }

    public void setDiseaseDetected(String diseaseDetected) {
        this.diseaseDetected = diseaseDetected;
    }

    public String getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(String confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPlantType() {
        return plantType;
    }

    public void setPlantType(String plantType) {
        this.plantType = plantType;
    }
}