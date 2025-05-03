package com.ar_tubo.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.ar_tubo.app.classes.ScanResult;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.res.AssetFileDescriptor;
import android.net.Uri;

public class ScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button captureButton;
    private TextView resultTextView;
    private ImageView capturedImageView;
    private ProgressBar analysisProgressBar;
    private Interpreter tfliteInterpreter;

    // Firebase references
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference scanResultsRef;

    private boolean imageCaptured = false;

    // To store analyzed results between steps
    private AnalysisResult analyzedResult;

    // Thread pool for background tasks
    private ExecutorService executor;

    private Button saveButton;
    private Button retakeButton;
    private File lastCapturedFile; // Save path for image

    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Initialize UI components
        previewView = findViewById(R.id.preview_view);
        captureButton = findViewById(R.id.capture_button);
        resultTextView = findViewById(R.id.result_text);
        capturedImageView = findViewById(R.id.captured_image_view);
        analysisProgressBar = findViewById(R.id.analysis_progress_bar);
        saveButton = findViewById(R.id.save_button);
        retakeButton = findViewById(R.id.retake_button);
        backButton = findViewById(R.id.backButton);

        // Hide progress bar, image view, and action buttons initially
        analysisProgressBar.setVisibility(View.GONE);
        capturedImageView.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        retakeButton.setVisibility(View.GONE);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        scanResultsRef = FirebaseDatabase.getInstance().getReference("/scan_results");

        // Initialize executor
        executor = Executors.newSingleThreadExecutor();

        // Initialize TensorFlow Lite model
        initTensorFlowModel();

        // Set up the camera
        setupCamera();

        // Set up the capture button
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        // Check if user is logged in
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to use this feature", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set up save button to save analysis results to Firebase
        saveButton.setOnClickListener(v -> {
            if (lastCapturedFile != null && analyzedResult != null) {
                // Show progress bar
                analysisProgressBar.setVisibility(View.VISIBLE);
                resultTextView.setText("Saving to your account...");

                // Disable buttons during saving
                saveButton.setEnabled(false);
                retakeButton.setEnabled(false);

                // Save the already analyzed results to Firebase
                executor.execute(() -> saveResultToFirebase(analyzedResult));
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Dashboard.class);
                startActivity(intent);
            }
        });

        // Set up retake button to reset the UI
        retakeButton.setOnClickListener(v -> resetScannerUI());
    }

    private void initTensorFlowModel() {
        try {
            // Load the model from assets folder
            AssetFileDescriptor fileDescriptor = getAssets().openFd("CaneSense_v1.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();
            MappedByteBuffer tfliteModel = fileChannel.map(
                    FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

            // Initialize TensorFlow Lite interpreter
            Interpreter.Options options = new Interpreter.Options();
            tfliteInterpreter = new Interpreter(tfliteModel, options);

            fileDescriptor.close();
            inputStream.close();

            resultTextView.setText("Ready to scan");
        } catch (IOException e) {
            resultTextView.setText("Error loading model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                // Camera provider is now guaranteed to be available
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Set up the preview use case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Set up the image capture use case
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                // Select back camera as default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind any existing use cases before binding new ones
                cameraProvider.unbindAll();

                // Bind use cases to camera
                Camera camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                // Handle any errors
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void captureImage() {
        if (imageCapture == null || currentUser == null) {
            return;
        }

        // Disable the capture button during processing
        captureButton.setEnabled(false);

        // Create timestamp for unique filename
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String filename = "CaneSense_" + timestamp + ".jpg";

        File appDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CaneSense");
        if (!appDir.exists()) appDir.mkdirs();

        File photoFile = new File(appDir, filename);

        // Create output options object
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Set up image capture listener
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Show the captured image
                        Bitmap capturedBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                        // Add image to gallery
                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(photoFile);
                        mediaScanIntent.setData(contentUri);
                        sendBroadcast(mediaScanIntent);

                        imageCaptured = true;
                        lastCapturedFile = photoFile;

                        runOnUiThread(() -> {
                            capturedImageView.setImageBitmap(capturedBitmap);
                            capturedImageView.setVisibility(View.VISIBLE);
                            captureButton.setVisibility(View.GONE);
                            saveButton.setVisibility(View.VISIBLE);
                            retakeButton.setVisibility(View.VISIBLE);
                            analysisProgressBar.setVisibility(View.VISIBLE);
                            resultTextView.setText("Analyzing image...");
                        });

                        // Automatically analyze the image after capture
                        executor.execute(() -> {
                            // Analyze the image (without saving to Firebase)
                            analyzeImage(photoFile);
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Show error message
                        Toast.makeText(ScannerActivity.this,
                                "Error capturing image: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        captureButton.setEnabled(true);
                        exception.printStackTrace();
                    }
                });
    }

    private void resetScannerUI() {
        capturedImageView.setVisibility(View.GONE);
        captureButton.setVisibility(View.VISIBLE);
        captureButton.setEnabled(true);
        saveButton.setVisibility(View.GONE);
        retakeButton.setVisibility(View.GONE);
        analysisProgressBar.setVisibility(View.GONE);
        resultTextView.setText("Ready to scan");
        lastCapturedFile = null;
        analyzedResult = null;
        imageCaptured = false;
    }

    // Class to hold analysis results
    private static class AnalysisResult {
        String diagnosis;
        float confidence;
        float[] rawPredictions;

        AnalysisResult(String diagnosis, float confidence, float[] rawPredictions) {
            this.diagnosis = diagnosis;
            this.confidence = confidence;
            this.rawPredictions = rawPredictions;
        }
    }

    private void analyzeImage(File imageFile) {
        try {
            // Load the image
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

            // Create a ByteBuffer for input
            ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4); // 4 bytes per float
            inputBuffer.order(ByteOrder.nativeOrder());

            // Process input (normalize)
            for (int y = 0; y < 224; y++) {
                for (int x = 0; x < 224; x++) {
                    int pixel = resizedBitmap.getPixel(x, y);

                    // Extract and normalize RGB values (0-255 → 0-1)
                    inputBuffer.putFloat(Color.red(pixel) / 255.0f);
                    inputBuffer.putFloat(Color.green(pixel) / 255.0f);
                    inputBuffer.putFloat(Color.blue(pixel) / 255.0f);
                }
            }

            // Reset position to read from the beginning
            inputBuffer.rewind();

            // Create output buffer
            float[][] outputBuffer = new float[1][5]; // 5 classes

            // Run inference
            tfliteInterpreter.run(inputBuffer, outputBuffer);

            // Log raw outputs for debugging
            StringBuilder valuesStr = new StringBuilder();
            for (float val : outputBuffer[0]) {
                valuesStr.append(val).append(", ");
            }
            Log.d("MODEL_OUTPUT", valuesStr.toString());

            // Find the class with highest probability
            float maxProb = 0;
            int maxIndex = 0;
            for (int i = 0; i < outputBuffer[0].length; i++) {
                if (outputBuffer[0][i] > maxProb) {
                    maxProb = outputBuffer[0][i];
                    maxIndex = i;
                }
            }

            // Get class name
            String[] classes = {"Healthy", "Mosaic", "RedRot", "Rust", "Yellow"};
            String className = (maxIndex < classes.length) ? classes[maxIndex] : "Unknown";

            final float confidence = maxProb * 100;
            final String diagnosis = className;

            // Store the analysis result
            analyzedResult = new AnalysisResult(diagnosis, confidence, outputBuffer[0]);

            // Update UI with results
            runOnUiThread(() -> {
                String formattedConfidence = String.format(Locale.US, "%.2f%%", confidence);
                resultTextView.setText("Diagnosis: " + diagnosis +
                        "\nConfidence: " + formattedConfidence +
                        "\nClick Save to store in your account");
                analysisProgressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                retakeButton.setEnabled(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                resultTextView.setText("Error analyzing image: " + e.getMessage());
                analysisProgressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                retakeButton.setEnabled(true);
            });
        }
    }

    private void saveResultToFirebase(AnalysisResult result) {
        if (currentUser == null) {
            runOnUiThread(() -> {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                analysisProgressBar.setVisibility(View.GONE);
                saveButton.setEnabled(true);
                retakeButton.setEnabled(true);
            });
            return;
        }

        try {
            // Create scan result object (no image URL)
            ScanResult scanResult = new ScanResult(
                    currentUser.getUid(),
                    result.diagnosis,
                    result.confidence,
                    null, // No image URL
                    System.currentTimeMillis(),
                    result.rawPredictions[0], // Healthy
                    result.rawPredictions[1], // Mosaic
                    result.rawPredictions[2], // RedRot
                    result.rawPredictions[3], // Rust
                    result.rawPredictions[4]  // Yellow
            );

            // Save to user's scan history with the unique ID
            scanResultsRef.child(currentUser.getUid())
                    .setValue(scanResult)
                    .addOnSuccessListener(aVoid -> {
                        // Update UI
                        runOnUiThread(() -> {
                            // Format confidence to 2 decimal places
                            String formattedConfidence = String.format(Locale.US, "%.2f%%", result.confidence);

                            resultTextView.setText("Diagnosis: " + result.diagnosis +
                                    "\nConfidence: " + formattedConfidence +
                                    "\nSaved to your account!");
                            analysisProgressBar.setVisibility(View.GONE);

                            // Re-enable buttons
                            saveButton.setEnabled(true);
                            retakeButton.setEnabled(true);

                            Toast.makeText(ScannerActivity.this,
                                    "Scan saved successfully",
                                    Toast.LENGTH_SHORT).show();
                        });
                    })
                    .addOnFailureListener(e -> {
                        runOnUiThread(() -> {
                            resultTextView.setText("Diagnosis: " + result.diagnosis +
                                    "\nConfidence: " + result.confidence + "%" +
                                    "\nFailed to save to database");
                            analysisProgressBar.setVisibility(View.GONE);

                            // Re-enable buttons
                            saveButton.setEnabled(true);
                            retakeButton.setEnabled(true);
                        });
                    });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                resultTextView.setText("Error saving result: " + e.getMessage());
                analysisProgressBar.setVisibility(View.GONE);

                // Re-enable buttons
                saveButton.setEnabled(true);
                retakeButton.setEnabled(true);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the TensorFlow Lite interpreter
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
        // Shutdown the executor
        if (executor != null) {
            executor.shutdown();
        }
    }
}