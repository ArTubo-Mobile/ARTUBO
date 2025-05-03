package com.asucare.app.services;

import androidx.annotation.NonNull;

import com.asucare.app.classes.Plant;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * Service class that handles all database operations related to plant data
 */
public class PlantDataService {
    private final DatabaseReference currentReadingReference;
    private final DatabaseReference scanResultsReference;

    public PlantDataService() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        currentReadingReference = database.getReference("/plant_details");
        scanResultsReference = database.getReference("/scan_results");
    }

    /**
     * Get current plant reading data from Firebase
     * @param arduino_Id The Arduino ID to fetch plant data for
     * @param callback Callback to handle the plant data
     */
    public void getCurrentPlantReading(String arduino_Id, PlantDataCallback callback) {
        currentReadingReference.child(arduino_Id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    try {
                        // Create a new Plant object and manually set the properties
                        Plant plantData = new Plant();

                        // Check for each field individually and set if exists
                        if (snapshot.child("temperature").exists()) {
                            plantData.setTemperature(snapshot.child("temperature").getValue(String.class));
                        }

                        if (snapshot.child("humidity").exists()) {
                            plantData.setHumidity(snapshot.child("humidity").getValue(String.class));
                        }

                        if (snapshot.child("soil_moisture").exists()) {
                            plantData.setSoil_moisture(snapshot.child("soil_moisture").getValue(String.class));
                        }

                        callback.onDataLoaded(plantData);
                    } catch (Exception e) {
                        callback.onError("Error parsing plant data: " + e.getMessage());
                    }
                } else {
                    callback.onDataNotFound();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }

    /**
     * Get a specific scan result by user ID
     *
     * @param userId User ID to fetch scan results for
     * @param callback Callback to handle the scan result
     */
    public void getScanResult(String userId, ScanResultCallback callback) {
        if (userId == null) {
            callback.onError("Invalid user ID");
            return;
        }

        scanResultsReference.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            try {
                                // Parse scan result data
                                String diseaseDetected = snapshot.child("diagnosis").getValue(String.class);
                                Double confidenceResult = null;

                                // Handle possible type mismatch for confidence value
                                if (snapshot.child("confidence").exists()) {
                                    Object confidenceObj = snapshot.child("confidence").getValue();
                                    if (confidenceObj instanceof Double) {
                                        confidenceResult = (Double) confidenceObj;
                                    } else if (confidenceObj instanceof Long) {
                                        confidenceResult = ((Long) confidenceObj).doubleValue();
                                    } else if (confidenceObj instanceof String) {
                                        try {
                                            confidenceResult = Double.parseDouble((String) confidenceObj);
                                        } catch (NumberFormatException e) {
                                            confidenceResult = 0.0;
                                        }
                                    }
                                }

                                String imagePath = snapshot.child("imagePath").getValue(String.class);
                                callback.onResultLoaded(diseaseDetected, confidenceResult, imagePath);
                            } catch (Exception e) {
                                callback.onError("Error parsing scan result: " + e.getMessage());
                            }
                        } else {
                            callback.onResultNotFound();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onError(error.getMessage());
                    }
                });
    }

    // Callback interfaces
    public interface PlantDataCallback {
        void onDataLoaded(Plant plantData);
        void onDataNotFound();
        void onError(String errorMessage);
    }

    public interface ScanResultCallback {
        void onResultLoaded(String diseaseDetected, Double confidenceResult, String imagePath);
        void onResultNotFound();
        void onError(String errorMessage);
    }
}