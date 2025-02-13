package com.example.carbs_concept;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    private ImageView segmentedImgView;
    private Bitmap imageBitmap;
    private ProgressBar progressBar;
    private String url = "http://192.168.1.168:5000";
    private TextView textStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analysis);
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        BitmapFactory.Options bmOptions= new BitmapFactory.Options();
        imageBitmap = rotateBitmap(BitmapFactory.decodeFile(imagePath, bmOptions), 90);
        segmentedImgView = findViewById(R.id.segmentedImgView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        textStatus = findViewById(R.id.textStatus);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textStatus.setText("Configuring Connection with flask backend...");
        uploadImage(imagePath);
    }

    private void uploadImage(String imagePath) {
        OkHttpClient client = new OkHttpClient();

        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Log.e("OkHTTP Image Upload", "File not found at " + imagePath);
            textStatus.setText("Unable to find file.");
            return;
        }
        //Create request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
                .build();

        //Create request itself
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
                    Log.d("OkHTTP Image Upload", "Success"+ response.body().string());
                    runOnUiThread(() -> textStatus.setText("Image successfully sent to flask server!"));
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

