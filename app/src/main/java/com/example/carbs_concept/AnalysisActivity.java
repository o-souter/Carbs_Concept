package com.example.carbs_concept;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AnalysisActivity extends AppCompatActivity {
    private static final String TAG = "AnalysisActivity";
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
    private ArFragment arFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analysis);
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        pointCloudPath = intent.getStringExtra("pointCloudPath");
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


//        //Set up Pointcloud capture
//        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arFragment);
//
//        if (arFragment != null) {
////            arFragment.getArSceneView().getScene().addOnUpdateListener(frameTime -> capturePointCloud());
//            capturePointCloud();
//        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textStatus.setText("Configuring Connection with flask backend...");
        uploadData(imagePath, pointCloudPath);

    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (arFragment != null && arFragment.getArSceneView() != null) {
//            arFragment.getArSceneView().pause();
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (arFragment != null && arFragment.getArSceneView() != null) {
//            try {
//                arFragment.getArSceneView().resume();
//            } catch (CameraNotAvailableException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//    private void capturePointCloud() {
//        if (arFragment == null || arFragment.getArSceneView() == null) {
//            Log.e(TAG, "ArFragment or SceneView is null");
//            return;
//        }
//
//        ArSceneView sceneView = arFragment.getArSceneView();
//        if (sceneView.getSession() == null) {
//            Log.e(TAG, "ARCore session is not initialized yet.");
//            return;
//        }
//
//        Frame frame = sceneView.getArFrame();
//        if (frame == null) {
//            Log.e(TAG, "Frame is null, ARCore may not be tracking yet.");
//            return;
//        }
//
//        PointCloud pointCloud = frame.acquirePointCloud();
//        if (pointCloud == null) {
//            Log.e(TAG, "No PointCloud data available.");
//            return;
//        }
//
//        FloatBuffer buffer = pointCloud.getPoints();
//        if (buffer == null || buffer.remaining() < 4) {
//            Log.e(TAG, "Point cloud is empty, skipping save.");
//            pointCloud.release();
//            return;
//        }
//
//        savePointCloudToFile(pointCloud);
//        pointCloud.release();
//    }


//    private void savePointCloudToFile(PointCloud pointCloud) {
//        FloatBuffer buffer = pointCloud.getPoints();
//        StringBuilder sb = new StringBuilder();
//
//        while (buffer.hasRemaining()) {
//            float x = buffer.get();
//            float y = buffer.get();
//            float z = buffer.get();
//            float confidence = buffer.get();
//
//            sb.append(x).append(",").append(y).append(",").append(z).append(",").append(confidence).append("\n");
//        }
//
//        if (sb.length() == 0) {
//            Log.e(TAG, "Skipping save: Point cloud is empty.");
//            return;
//        }
//
//        File file = new File(getFilesDir(), "pointcloud.txt");
//        try (FileOutputStream fos = new FileOutputStream(file, true)) { // 'true' enables appending
//            fos.write(sb.toString().getBytes());
//            Log.d(TAG, "Point cloud saved internally at: " + file.getAbsolutePath());
//        } catch (IOException e) {
//            Log.e(TAG, "Error saving point cloud internally", e);
//        }
//    }

    private void uploadData(String imagePath, String pointCloudPath) {
        OkHttpClient client = new OkHttpClient();

        File imageFile = new File(imagePath);
        File pointCloudFile = new File(pointCloudPath);
        if (!imageFile.exists() | !pointCloudFile.exists()) {
            Log.e("OkHTTP Image Upload", "Files for upload not found.");
            textStatus.setText("Unable to find file:\n" +
                    "Image exists: " + imageFile.exists() + "\n" +
                    "PointCloud exists: " + pointCloudFile.exists());
            textStatus.setTextColor(Color.RED);
            return;
        }
        //Create request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .addFormDataPart("pointcloud", pointCloudFile.getName(),
                        RequestBody.create(pointCloudFile, MediaType.parse("text/plain")))
                .build();

        //Create request itself
        String url = "http://" + ipForBackend + ":" + portForBackend;
        Request request = new Request.Builder()
                .url(url+"/"+"upload-data")
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

