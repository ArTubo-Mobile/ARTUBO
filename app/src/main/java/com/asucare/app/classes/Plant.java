package com.asucare.app.classes;

/**
 * Model class for Plant data
 */
public class Plant {
    private String temperature;
    private String humidity;
    private String soil_moisture;

    // Default constructor required for Firebase
    public Plant() {
        // Default constructor required for calls to DataSnapshot.getValue(Plant.class)
    }

    // Constructor with all fields
    public Plant(String temperature, String humidity, String soil_moisture) {
        this.temperature = temperature;
        this.humidity = humidity;
        this.soil_moisture = soil_moisture;
    }

    // Getters and Setters
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getSoil_moisture() {
        return soil_moisture;
    }

    public void setSoil_moisture(String soil_moisture) {
        this.soil_moisture = soil_moisture;
    }
}