package com.example.carbs_concept;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnalysisActivity extends AppCompatActivity {
    private String imagePath;
    private String pointCloudPath;
    private ImageView segmentedImgView;
    private Bitmap imageBitmap;
    private ProgressBar progressBar;
//    private String url = "http://192.168.1.168:5000";
    private TextView textStatus;
    private ImageButton btnBackToCamera;
    private Session arSession;
    private String ipForBackend;
    private String portForBackend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analysis);
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        ipForBackend = intent.getStringExtra("correctIP");
        portForBackend = intent.getStringExtra("correctPort");

//        pointCloudPath = intent.getStringExtra("pointCloudPath");
        BitmapFactory.Options bmOptions= new BitmapFactory.Options();
        imageBitmap = rotateBitmap(BitmapFactory.decodeFile(imagePath, bmOptions), 90);
        segmentedImgView = findViewById(R.id.segmentedImgView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        textStatus = findViewById(R.id.textStatus);
        btnBackToCamera = findViewById(R.id.btnBackToCamera);

        btnBackToCamera.setOnClickListener(v -> {
            Intent backToCamera = new Intent(this, MainActivity.class);
            startActivity(backToCamera);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        textStatus.setText("Capturing point cloud. Please hold camera still!");
//        try {
//            arSession = new Session(this);
//            arSession.configure(new Config(arSession));
//        }
//        catch (Exception e) {
//            Log.e("ARCore", "Failed to create AR Session");
//        }
        //        //Confirm ARCOre setup correctly
//        try {
//            arSession = new Session(this);
//        } catch (UnavailableException e) {
//            Log.e("ARCore", "ARCore session could not be created", e);
//        }
//        String pointCloudPath = getPointCloudFile();
//        File pointCloudFile = new File(pointCloudPath);
//        if (!pointCloudFile.exists()) {
//            textStatus.setText("Unable to capture point cloud file");
//            textStatus.setTextColor(Color.RED);
//        }
//        else {
//
//        }
        textStatus.setText("Configuring Connection with flask backend...");
        uploadImage(imagePath, pointCloudPath);

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (arSession != null) {
//            try {
//                arSession.resume();
//            } catch (CameraNotAvailableException e) {
//                Log.e("ARCore", "Camera not available");;
//            }
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (arSession != null) {
//            arSession.pause();
//        }
//    }
    private String getPointCloudFile() {
        if (arSession != null) {
            try {
                arSession.resume();
                if (arSession == null) {
                    Log.e("ARCore", "Session is null");
                } else {
                    Log.d("ARCore", "Session is active, updating frame...");
                }
                Frame frame = arSession.update();
                PointCloud pointCloud = frame.acquirePointCloud();
                String filePath = savePointCloudToFile(pointCloud);
                pointCloud.release();
                return filePath;
            } catch (Exception e) {
                Log.e("ARCore", "Error getting point cloud", e);
                textStatus.setText("Error capturing point cloud");
                textStatus.setTextColor(Color.RED);
                if (arSession == null) {
                    Log.e("ARCore", "Session is null");
                }
                return null;
            }
        }
        return null;
    }

    private String savePointCloudToFile(PointCloud pointCloud) {
        File file = new File(getFilesDir(), "pointcloud.txt"); // Internal storage

        try (FileWriter writer = new FileWriter(file, true)) {
            FloatBuffer points = pointCloud.getPoints();
            while (points.hasRemaining()) {
                float x = points.get();       // X coordinate
                float y = points.get();       // Y coordinate
                float z = points.get();       // Z coordinate
                float confidence = points.get(); // Confidence value

                writer.write(x + "," + y + "," + z + "," + confidence + "\n");
                return file.getAbsolutePath();
            }
            Log.d("ARCore", "Point cloud saved: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("ARCore", "Error saving point cloud", e);
            return null;
        }
        return null;
    }



    private void uploadImage(String imagePath, String pointCloudPath) {
        OkHttpClient client = new OkHttpClient();

        File imageFile = new File(imagePath);
//        File pointCloudFile = new File(pointCloudPath);
//        if (!imageFile.exists() | !pointCloudFile.exists()) {
//            Log.e("OkHTTP Image Upload", "Files for upload not found.");
//            textStatus.setText("Unable to find file:\n" +
//                    "Image exists: " + imageFile.exists() + "\n" +
//                    "PointCloud exists: " + pointCloudFile.exists());
//            textStatus.setTextColor(Color.RED);
//            return;
//        }
        //Create request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
//                .addFormDataPart("pointcloud", pointCloudFile.getName(),
//                        RequestBody.create(pointCloudFile, MediaType.parse("text/plain")))
                .build();

        //Create request itself
        String url = "http://" + ipForBackend + ":" + portForBackend;
        Request request = new Request.Builder()
                .url(url+"/"+"upload-image")
                .post(requestBody)
                .build();

        //Execute request in background
        textStatus.setText("Sending Request...");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("OkHTTP Image Upload", "Failed: " + e.getMessage());
                runOnUiThread(() -> textStatus.setText("Request Failed: " + e.getMessage()));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String response_str = response.body().string();
                    Log.d("OkHTTP Image Upload", "Success"+ response_str);
//                    runOnUiThread(() -> textStatus.setText("Image successfully sent to flask server! \nPress back to take another."));

                    runOnUiThread(() -> textStatus.setText(response_str));
                    runOnUiThread(() -> textStatus.setTextColor(Color.GREEN));
                }
                else {
                    Log.e("OkHTTP Image Upload", "Error: "+response.code());
                    runOnUiThread(() -> textStatus.setText("Request Error: "+response.code()));
                }
            }
        });
    }

    private Bitmap rotateBitmap(Bitmap original, float degrees) {
        int width = original.getWidth();
        int height = original.getHeight();

        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);

        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
        Canvas canvas = new Canvas(rotatedBitmap);
        canvas.drawBitmap(original, 5.0f, 0.0f, null);

        return rotatedBitmap;
    }
}

