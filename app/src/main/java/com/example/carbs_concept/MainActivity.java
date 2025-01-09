package com.example.carbs_concept;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.view.PreviewView;

//OpenCV
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.ArucoDetector;

import android.Manifest;
public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;

    private ImageCapture imageCapture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //Confirm OpenCV initialised properly
        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed!");
        } else {
            Log.d("OpenCV", "OpenCV initialized successfully.");
        }

        //Request camera permission

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }



        //Setup widgets
        PreviewView previewView = findViewById(R.id.previewView);
        Button captureButton = findViewById(R.id.captureButton);
        //Set up the GUI
        ProcessCameraProvider.getInstance(this).addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(this).get();
                //Select the back camera of phone
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

                imageCapture = new ImageCapture.Builder().build();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
                //When button clicked, capture image
                captureButton.setOnClickListener(v -> captureImage());


            }
            catch (Exception e) {
                Log.e("CameraX", "Camera initialization failed: ", e);
            }
        }, ContextCompat.getMainExecutor(this));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void captureImage() {
        File file = new File(getFilesDir(), "captured_image.jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d("CameraX", "Saved image: " + file.getAbsolutePath());
                        processImage(file.getAbsolutePath());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e("CameraX", "Image capture failed: " + exception.getMessage());
                    }
                }
        );
    }

    private void processImage(String imagePath) {
        //Load image with OpenCV
        Mat image = Imgcodecs.imread(imagePath);

        //Set up ARUco detector
        ArucoDetector arucoDetector = new ArucoDetector();

        // Create Mat objects for marker corners and IDs
        List<Mat> markerCorners = new ArrayList<>();
        Mat markerIds = new Mat();

        //Detect markers
        arucoDetector.detectMarkers(image, markerCorners, markerIds);

        if (!markerIds.empty()) {
            Log.d("ARUco", "Markers detected: " + markerIds.dump());
            Log.d("ARUco", "Markers detected: " + markerIds.dump());
            calculateScale(markerCorners);
        }
        else {
            Log.d("ARUco", "No Markers detected.");
        }
    }

    private void calculateScale(List<Mat> markerCorners) {
        Log.d("ARUco", "Not implemented yet.");
    }
}