package com.ar_tubo.app;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
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
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ExecutionException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
public class ScannerActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private Button captureButton;
    private TextView resultTextView;
    private Interpreter tfliteInterpreter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // Initialize UI components
        previewView = findViewById(R.id.preview_view);
        captureButton = findViewById(R.id.capture_button);
        resultTextView = findViewById(R.id.result_text);

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

            resultTextView.setText("Model loaded successfully");
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
        if (imageCapture == null) {
            return;
        }

        // Create output file for the captured image
        File photoFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "CaneSense_" + System.currentTimeMillis() + ".jpg");

        // Create output options object
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Set up image capture listener
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        // Process the captured image with TensorFlow Lite
                        processCapturedImage(photoFile);
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        // Show error message
                        Toast.makeText(ScannerActivity.this,
                                "Error capturing image: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        exception.printStackTrace();
                    }
                });
    }

    private void processCapturedImage(File imageFile) {
        try {
            // Load and resize the image
            Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

            // Import these at the top of your file:
            // import org.tensorflow.lite.support.image.TensorImage;
            // import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
            // import org.tensorflow.lite.DataType;

            // Process input image
            TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
            tensorImage.load(resizedBitmap);

            // Create output buffer based on model's output tensor shape
            int[] outputShape = tfliteInterpreter.getOutputTensor(0).shape();
            TensorBuffer outputBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32);

            // Run inference
            tfliteInterpreter.run(tensorImage.getBuffer(), outputBuffer.getBuffer());

            // Get output as float array
            float[] outputArray = outputBuffer.getFloatArray();

            // Log all values
            StringBuilder valuesStr = new StringBuilder("Output values: ");
            for (float val : outputArray) {
                valuesStr.append(val).append(", ");
            }
            Log.d("MODEL_OUTPUT", valuesStr.toString());

            // Process results as before
            float maxProb = 0;
            int maxIndex = 0;
            for (int i = 0; i < Math.min(3, outputArray.length); i++) {
                if (outputArray[i] > maxProb) {
                    maxProb = outputArray[i];
                    maxIndex = i;
                }
            }

            String[] classes = {"Healthy", "Mosaic", "RedRot", "Rust", "Yellow"};
            String className = (maxIndex < classes.length) ? classes[maxIndex] : "Unknown";

            runOnUiThread(() -> {
                resultTextView.setText("Diagnosis: " + className );
            });

        } catch (Exception e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                resultTextView.setText("Error: " + e.getMessage());
            });
        }
    }

    private float[][][][] preprocessImage(Bitmap bitmap) {
        // Initialize the input array (adjust dimensions based on your model)
        float[][][][] input = new float[1][224][224][3];

        // Normalize pixel values to [0, 1]
        for (int x = 0; x < 224; x++) {
            for (int y = 0; y < 224; y++) {
                int pixel = bitmap.getPixel(x, y);
                input[0][y][x][0] = Color.red(pixel) / 255.0f;
                input[0][y][x][1] = Color.green(pixel) / 255.0f;
                input[0][y][x][2] = Color.blue(pixel) / 255.0f;
            }
        }

        return input;
    }

    private String postprocessResults(float[][] output) {
        // Find the class with the highest probability
        float maxProb = 0;
        int maxIndex = 0;

        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i];
                maxIndex = i;
            }
        }

        // Get class name (replace with your actual class names)
        String[] classes = {"Healthy", "Diseased", "Pest Infested"};
        String className = (maxIndex < classes.length) ? classes[maxIndex] : "Unknown";

        // Format the result string
        return "Diagnosis: " + className + "\nConfidence: " + (maxProb * 100) + "%";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the TensorFlow Lite interpreter
        if (tfliteInterpreter != null) {
            tfliteInterpreter.close();
        }
    }
}