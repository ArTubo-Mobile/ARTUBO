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
        currentReadingReference = database.getReference("/current_reading");
        scanResultsReference = database.getReference("/scan_results");
    }

    /**
     * Get current plant reading data from Firebase
     * @param callback Callback to handle the plant data
     */
    public void getCurrentPlantReading(PlantDataCallback callback) {
        currentReadingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getChildrenCount() > 0) {
                    // Get the first child as we only need one reading
                    DataSnapshot firstChild = snapshot.getChildren().iterator().next();
                    Plant plantData = firstChild.getValue(Plant.class);
                    callback.onDataLoaded(plantData);
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
     * Get a specific scan result by user ID and scan ID
     *
     * @param callback Callback to handle the scan result
     */
    public void getScanResult(String userId, ScanResultCallback callback) {
        if (userId == null ) {
            callback.onError("Invalid user ID or scan ID");
            return;
        }

        scanResultsReference.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // Parse your scan result data here based on your data structure
                            String diseaseDetected = snapshot.child("diagnosis").getValue(String.class);
                            Double confidenceResult = snapshot.child("confidence").getValue(Double.class);

                            callback.onResultLoaded(diseaseDetected, confidenceResult);
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
        void onResultLoaded(String diseaseDetected, Double confidenceResult);
        void onResultNotFound();
        void onError(String errorMessage);
    }
}