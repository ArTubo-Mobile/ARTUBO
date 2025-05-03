package com.asucare.app.classes;

public class Plant {
    String humidity;
    String soil_moisture;
    String temperature;
    String disease_detected;

    String plant_detected;

    public Plant() {
    }

    public Plant(String humidity, String soil_moisture, String temperature, String disease_detected, String plant_detected) {
        this.humidity = humidity;
        this.soil_moisture = soil_moisture;
        this.temperature = temperature;
        this.disease_detected = disease_detected;
        this.plant_detected = plant_detected;
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

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getDisease_detected() {
        return disease_detected;
    }

    public void setDisease_detected(String disease_detected) {
        this.disease_detected = disease_detected;
    }

    public String getPlant_detected() {
        return plant_detected;
    }

    public void setPlant_detected(String plant_detected) {
        this.plant_detected = plant_detected;
    }
}
