package com.asucare.app;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.asucare.app.classes.Plant;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.asucare.app.services.PlantDataService;
import com.asucare.app.services.UserDataService;
import com.asucare.app.classes.User;
import android.widget.Toast;


public class ScannerDetails extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private PlantDataService plantDataService;

    private UserDataService userDataService;

    private ImageView imgBack, imgPlantDetected;
    private TextView tvPlantDetectedLabel, tvPlantDetectedSublabel;
    private TextView tvHumidity, tvTemperature, tvSoilMoisture, tvDiseaseDetected;
    private ProgressBar progressBarTemp, progressBarHum, progressBarSoilMoisture;

    private String diseaseDetected = "";
    private String objectDetectionConfidence = "";
    private String firebaseUid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_details);
        userDataService = new UserDataService();


        // Initialize status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor("#EDECE5"));
        }

        // Change navigation button color
        changeNavigationButtonColor("#F2F2ED");

        // Initialize Firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser != null) {
            firebaseUid = mUser.getUid();
        }

        // Initialize PlantDataService
        plantDataService = new PlantDataService();

        // Initialize UI components
        initViews();

        // Back button functionality
        imgBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Dashboard.class);
            startActivity(intent);
        });

        // Fetch current plant data
        getUserDataAndFetchPlantData(firebaseUid);
    }

    private void initViews() {
        imgBack = findViewById(R.id.imageView_back);
        imgPlantDetected = findViewById(R.id.imageView_plant_detected);
        tvPlantDetectedLabel = findViewById(R.id.textView_plant_detected_label);
        tvPlantDetectedSublabel = findViewById(R.id.textView_plant_detected_sublabel);
        tvHumidity = findViewById(R.id.textView_humidity_status);
        tvTemperature = findViewById(R.id.textView_temperature_status);
        tvSoilMoisture = findViewById(R.id.textView_soil_moisture_status);
        tvDiseaseDetected = findViewById(R.id.textView_disease_detected_label);
        progressBarTemp = findViewById(R.id.progressBar_temp);
        progressBarHum = findViewById(R.id.progressBar_humidity);
        progressBarSoilMoisture = findViewById(R.id.progressBar_soilmoisture);
    }

    private void getUserDataAndFetchPlantData(String firebaseID) {
        userDataService.getUserByFirebaseUid(firebaseID, new UserDataService.UserCallback() {
            @Override
            public void onUserLoaded(User user) {
                runOnUiThread(() ->
                        Toast.makeText(ScannerDetails.this, user.getArduino_uid(), Toast.LENGTH_SHORT).show()
                );
                if (user != null && user.getArduino_uid() != null && !user.getArduino_uid().isEmpty()) {
                    // Pass the arduino_id to fetch plant data
                    String arduino_id = user.getArduino_uid();
                    fetchCurrentPlantData(arduino_id);
                    fetchLatestScan(firebaseID);
                } else {
                    runOnUiThread(() ->
                            Toast.makeText(ScannerDetails.this, "No Arduino ID found for this user", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onUserNotFound() {
                runOnUiThread(() ->
                        Toast.makeText(ScannerDetails.this, "User data not found", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(String errorMessage) {
                runOnUiThread(() ->
                        Toast.makeText(ScannerDetails.this, "Error loading user data: " + errorMessage, Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void updateScanResultUI() {
        if (diseaseDetected.equals("Healthy")) {
            imgPlantDetected.setImageResource(R.drawable.bgplantdetected2_tubo);
            tvPlantDetectedLabel.setText("Sugarcane");
            tvPlantDetectedSublabel.setText("Saccharum officinarum");
            tvDiseaseDetected.setText("No Disease Detected");
            tvDiseaseDetected.setText(diseaseDetected + " (" + objectDetectionConfidence + "%)");
        } else if (diseaseDetected.toLowerCase().equals("no sugarcane found")) {
            tvPlantDetectedLabel.setText("No result");
            tvPlantDetectedSublabel.setText("Object detected is not a sugarcane");
            imgPlantDetected.setImageResource(R.drawable.nosugarcanedetected1);
            tvDiseaseDetected.setText(diseaseDetected);
        } else {
            imgPlantDetected.setImageResource(R.drawable.bgplantdetected2_tubo);
            tvPlantDetectedLabel.setText("Sugarcane");
            tvPlantDetectedSublabel.setText("Saccharum officinarum");
            tvDiseaseDetected.setText(diseaseDetected + " (" + objectDetectionConfidence + "%)");
        }
    }

    private void fetchCurrentPlantData(String arduino_id) {
        plantDataService.getCurrentPlantReading(arduino_id, new PlantDataService.PlantDataCallback() {
            @Override
            public void onDataLoaded(Plant plantData) {
                if (plantData != null) {
                    runOnUiThread(() -> updateEnvironmentData(plantData));
                }
            }

            @Override
            public void onDataNotFound() {
                // Handle case where no data is found
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error case
            }
        });
    }

    private void fetchLatestScan(String userId) {
        plantDataService.getScanResult(userId, new PlantDataService.ScanResultCallback() {
            @Override
            public void onResultLoaded(String result, Double confidenceResult) {
                if (result != null && confidenceResult != null) {
                    diseaseDetected = result;
                    objectDetectionConfidence = String.format("%.2f", confidenceResult); // 2 decimal places
                    // Update UI with scan result
                    updateScanResultUI();
                }
            }

            @Override
            public void onResultNotFound() {
                // Handle case where no data is found
            }

            @Override
            public void onError(String errorMessage) {
                // Handle error case
            }
        });
    }

    private void updateEnvironmentData(Plant plantData) {
        // Temperature
        String tempValue = plantData.getTemperature();
        String tempStatus = "";

        if (Double.parseDouble(tempValue) >= 20 && Double.parseDouble(tempValue) <= 30) {
            tempStatus = "Normal";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarTemp.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
            }
        } else {
            tempStatus = "Critical";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarTemp.setProgressTintList(getColorStateList(R.color.progress_tint_critical));
            }
        }

        int tempProgressValue = Integer.parseInt(String.valueOf(Math.round(Double.parseDouble(tempValue))));
        updateProgress(tempProgressValue, progressBarTemp);
        tvTemperature.setText(tempValue + " ℃ - " + tempStatus);

        // Humidity
        String humValue = plantData.getHumidity();
        String humStatus = "";

        if (Double.parseDouble(humValue) >= 60 && Double.parseDouble(humValue) <= 80) {
            humStatus = "Normal";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarHum.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
            }
        } else {
            humStatus = "Critical";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarHum.setProgressTintList(getColorStateList(R.color.progress_tint_critical));
            }
        }

        int humProgressValue = Integer.parseInt(String.valueOf(Math.round(Double.parseDouble(humValue))));
        updateProgress(humProgressValue, progressBarHum);
        tvHumidity.setText(humValue + " - " + humStatus);

        // Soil Moisture
        String soilValue = plantData.getSoil_moisture();
        String soilStatus = "";

        if (Double.parseDouble(soilValue) >= 700) {
            soilStatus = "Dry";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarSoilMoisture.setProgressTintList(getColorStateList(R. color.progress_tint_critical));
            }
        } else if (Double.parseDouble(soilValue) >= 300 && Double.parseDouble(soilValue) <= 700) {
            soilStatus = "Moist";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarSoilMoisture.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
            }
        } else if (Double.parseDouble(soilValue) >= 1 && Double.parseDouble(soilValue) <= 100) {
            soilStatus = "Normal";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarSoilMoisture.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
            }
        } else if (Double.parseDouble(soilValue) >= 100 && Double.parseDouble(soilValue) <= 300) {
            soilStatus = "Wet";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                progressBarSoilMoisture.setProgressTintList(getColorStateList(R.color.progress_tint_normal));
            }
        }

        int soilProgressValue = Math.round((int) Double.parseDouble(soilValue) / 7);
        updateProgress(soilProgressValue, progressBarSoilMoisture);
        tvSoilMoisture.setText(soilValue + "% - " + soilStatus);
    }

    private void updateProgress(int progress, ProgressBar progressBar) {
        ObjectAnimator.ofInt(progressBar, "progress", progress)
                .setDuration(500)
                .start();
    }

    private void changeNavigationButtonColor(String color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.parseColor(color));
        }
    }
}