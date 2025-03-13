package com.example.carbs_concept;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Locale;

public class PointCloudCaptureActivity extends AppCompatActivity {

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 1000;
    private ArFragment arFragment;
    private int retryCount = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isCapturing = false;
    private Button btnStartCapture;
    private static final long CAPTURE_TIMEOUT_MS = 1000;  //15000// Duration in milliseconds to wait for a suitable point cloud (adjust as needed)
    private static final int MIN_POINTS_THRESHOLD = 1000;  //15000// Minimum number of points to consider the capture as "suitable"
    private long captureStartTime = 0;  // Tracks when capture started
    private int capturedPointCount = 0;  // Tracks number of captured points
    private StringBuilder allPointData = new StringBuilder();  // StringBuilder to accumulate points data
    private TextView txtPointCloudInfo;
    private ConstraintLayout pointCloudInfoAlert;
    //Data imported from image capture
    private String imagePath;
    private String ipForBackend;
    private String portForBackend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_point_cloud_capture);
        Intent intent = getIntent();

//        txtPointCloudInfo = findViewById(R.id.txtPointCloudInfo);
//        txtPointCloudInfo.setVisibility(View.VISIBLE);
        pointCloudInfoAlert = findViewById(R.id.alertPointCloudInfo);
        pointCloudInfoAlert.setVisibility(View.VISIBLE);
        imagePath = intent.getStringExtra("imagePath");
        ipForBackend = intent.getStringExtra("correctIP");
        portForBackend = intent.getStringExtra("correctPort");

        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
        isCapturing = true;
        btnStartCapture = findViewById(R.id.btnStartPointCloudCapture);


        btnStartCapture.setOnClickListener(view -> startCapture());

        // Listen for frame updates to capture points
        arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> {
            if (captureStartTime != 0) {
                capturePointCloud(arFragment.getArSceneView().getArFrame());
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void startCapture() {
        pointCloudInfoAlert.setVisibility(View.INVISIBLE);
        captureStartTime = System.currentTimeMillis();  // Mark the start time
        capturedPointCount = 0;  // Reset the point count
        allPointData.setLength(0);  // Clear any previous captured data

        // Update button text to indicate capturing is in progress
        btnStartCapture.setText("Capturing...");

        // Show a message indicating that capture has started
        Log.d("PointCloud", "Starting pointcloud capture...");
    }

    // Start capturing the point cloud when the AR frame is ready
    private void capturePointCloudWhenReady() {
        // Check if AR frame is available
        Frame frame = arFragment.getArSceneView().getArFrame();

        if (frame != null) {
            if (!isCapturing) {
                isCapturing = true;
            }
            capturePointCloud(frame);
        } else {
            // If the frame is not available, log an error
            Log.e("PointCloud", "AR frame is not available yet. Please try again.");
        }
    }

    // Method to capture the point cloud from the AR frame
    private void capturePointCloud(Frame frame) {
        Log.d("PointCloud", "Capturing point cloud...");

        if (frame == null) {
            Log.e("PointCloud", "capturePointCloud() called with null frame");
            return;
        }

        PointCloud pointCloud = null;
        try {
            pointCloud = frame.acquirePointCloud();
        } catch (com.google.ar.core.exceptions.DeadlineExceededException e) {
            Log.e("PointCloud", "DeadlineExceededException: ARCore took too long.", e);
            handleRetry(frame, "ARCore took too long. Retrying...");
            return;
        }
        FloatBuffer points = pointCloud.getPoints();
        while (points.hasRemaining()) {
            float x = points.get();
            float y = points.get();
            float z = points.get();
            float confidence = points.get();  // Optional: if you need it

            // Append the points to the cumulative string
            allPointData.append(x).append(" ").append(y).append(" ").append(z).append("\n");

            capturedPointCount++;  // Increment the captured points count
        }

        // If a sufficient number of points have been captured or if timeout is reached, stop the capture
        if (capturedPointCount >= MIN_POINTS_THRESHOLD || (System.currentTimeMillis() - captureStartTime >= CAPTURE_TIMEOUT_MS)) {
            Log.d("PointCloud", "Captured " + capturedPointCount + " points.");
            stopCapture();  // Stop the capture
        }

    }

    private void stopCapture() {
        // Update button text to indicate capture has stopped
        btnStartCapture.setText("Capture Complete");

        // Show a message indicating that the capture is complete
        Log.d("PointCloud", "Point Cloud Capture Complete");

        File pointCloudFile = new File(getFilesDir(), "point_cloud.xyz");

        // Check if the file already exists and delete it if necessary
        if (pointCloudFile.exists()) {
            boolean deleted = pointCloudFile.delete();
            if (deleted) {
                Log.d("PointCloud", "Existing point cloud file deleted.");
            } else {
                Log.e("PointCloud", "Failed to delete existing point cloud file.");
            }
        }
        // Save captured data to a new file
        try (FileWriter writer = new FileWriter(pointCloudFile)) {
            writer.write(allPointData.toString());
            Log.d("PointCloud", "Point cloud data saved successfully.");

            // Log the location of the saved file
            Log.d("PointCloud", "Point cloud file saved to: " + pointCloudFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e("PointCloud", "Error writing point cloud data to file.", e);
            captureStartTime = 0;
            return;
        }

        // Reset capture state
        captureStartTime = 0;  // Reset the capture start time

        Intent intent = new Intent(this, AnalysisActivity.class);
        intent.putExtra("imagePath", imagePath);
        intent.putExtra("pointCloudPath", pointCloudFile.getAbsolutePath());
        intent.putExtra("correctIP", ipForBackend);
        intent.putExtra("correctPort", portForBackend);

        startActivity(intent);
    }


    private void handleRetry(Frame frame, String logMessage) {
        Log.d("PointCloud", logMessage);
        if (retryCount < MAX_RETRIES) {
            retryCount++;
            Log.d("PointCloud", "Retrying in " + RETRY_DELAY_MS / 1000 + " second(s)... (Attempt " + retryCount + ")");
            handler.postDelayed(() -> capturePointCloud(frame), RETRY_DELAY_MS);
        } else {
            Log.e("PointCloud", "Max retries reached.");
        }
    }

}

