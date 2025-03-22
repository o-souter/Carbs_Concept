package com.example.carbs_concept;

import android.content.Intent;
import android.media.Image;
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
import com.google.ar.core.exceptions.NotYetAvailableException;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Locale;
import android.media.Image;
import java.nio.ByteBuffer;
import com.google.ar.core.Camera;
import com.google.ar.core.Frame;
import android.util.Log;

public class PointCloudCaptureActivity extends AppCompatActivity {

    private static final int MAX_RETRIES = 5;
    private static final int RETRY_DELAY_MS = 1000;
    private ArFragment arFragment;
    private int retryCount = 0;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isCapturing = false;
    private Button btnStartCapture;
    private static final long CAPTURE_TIMEOUT_MS = 1;  //15000// Duration in milliseconds to wait for a suitable point cloud (adjust as needed)
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

        Image image = null;
        try {
            image = frame.acquireCameraImage();
        } catch (NotYetAvailableException e) {
            throw new RuntimeException(e);
        }
        if (image == null) {
            Log.e("PointCloud", "Could not acquire camera image");
            return;
        }

        int[] rgbImage = convertImageToRGB(image);
        while (points.hasRemaining()) {
            float x = points.get();
            float y = points.get();
            float z = points.get();
            float confidence = points.get();  // Optional: if you need it

            // Project (x, y, z) to 2D image coordinates
            int[] uv = new int[2];
            if (projectToImageCoords(x, y, z, frame, image.getWidth(), image.getHeight(), uv)) {
                int color = rgbImage[uv[1] * image.getWidth() + uv[0]];  // Get color from image
                int red = (color >> 16) & 0xFF;
                int green = (color >> 8) & 0xFF;
                int blue = color & 0xFF;

                allPointData.append(x).append(" ").append(y).append(" ").append(z)
                        .append(" ").append(red).append(" ").append(green).append(" ").append(blue).append("\n");
            } else {
                allPointData.append(x).append(" ").append(y).append(" ").append(z).append("\n");  // No color if projection fails
            }

            capturedPointCount++;  // Increment the captured points count
        }
        pointCloud.release();
        image.close();

        // If a sufficient number of points have been captured or if timeout is reached, stop the capture
        if (capturedPointCount >= MIN_POINTS_THRESHOLD || (System.currentTimeMillis() - captureStartTime >= CAPTURE_TIMEOUT_MS)) {
            Log.d("PointCloud", "Captured " + capturedPointCount + " points.");
            stopCapture();  // Stop the capture
        }

    }

    private int[] convertImageToRGB(Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int[] rgbPixels = new int[width * height];

        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();  // Luminance (Y)
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();  // Chrominance (U)
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();  // Chrominance (V)

        int rowStride = image.getPlanes()[0].getRowStride();
        int pixelStride = image.getPlanes()[1].getPixelStride();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int yValue = yBuffer.get(y * rowStride + x) & 0xFF;
                int uvIndex = (y / 2) * (rowStride / 2) + (x / 2) * pixelStride;
                int uValue = uBuffer.get(uvIndex) & 0xFF;
                int vValue = vBuffer.get(uvIndex) & 0xFF;

                // Convert YUV to RGB
                int r = (int) (yValue + 1.402 * (vValue - 128));
                int g = (int) (yValue - 0.344136 * (uValue - 128) - 0.714136 * (vValue - 128));
                int b = (int) (yValue + 1.772 * (uValue - 128));

                // Clamp to 0-255
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                // Store pixel in ARGB format
                rgbPixels[y * width + x] = (0xFF << 24) | (r << 16) | (g << 8) | b;
            }
        }

        return rgbPixels;
    }

    private boolean projectToImageCoords(float x, float y, float z, Frame frame, int imageWidth, int imageHeight, int[] uv) {
        Camera camera = frame.getCamera();

        float[] projMatrix = new float[16];
        camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100f);  // Get projection matrix

        float[] viewMatrix = new float[16];
        camera.getViewMatrix(viewMatrix, 0);

        float[] worldPoint = {x, y, z, 1};  // Convert to homogeneous coordinates
        float[] screenPoint = new float[4];

        // Multiply view matrix
        multiplyMatrixAndVector(viewMatrix, worldPoint, screenPoint);

        // Multiply projection matrix
        multiplyMatrixAndVector(projMatrix, screenPoint, screenPoint);

        if (screenPoint[3] == 0) return false;  // Avoid division by zero

        float ndcX = screenPoint[0] / screenPoint[3];  // Normalize device coordinates
        float ndcY = screenPoint[1] / screenPoint[3];

        // Convert to pixel coordinates
        uv[0] = (int) ((ndcX * 0.5f + 0.5f) * imageWidth);
        uv[1] = (int) ((-ndcY * 0.5f + 0.5f) * imageHeight);

        // Ensure points are within bounds
        if (uv[0] < 0 || uv[0] >= imageWidth || uv[1] < 0 || uv[1] >= imageHeight) {
            return false;
        }

        return true;
    }

    // Helper function to multiply a 4x4 matrix by a 4D vector
    private void multiplyMatrixAndVector(float[] matrix, float[] vector, float[] result) {
        for (int i = 0; i < 4; i++) {
            result[i] = matrix[i * 4] * vector[0] +
                    matrix[i * 4 + 1] * vector[1] +
                    matrix[i * 4 + 2] * vector[2] +
                    matrix[i * 4 + 3] * vector[3];
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

