package com.example.carbs_concept;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;
import java.util.List;

public class AnalysisActivity extends AppCompatActivity {
    private String imagePath;
    private ImageView segmentedImgView;
    private Bitmap imageBitmap;
    private ProgressBar progressBar;
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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            sendImageForAnalysis();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendImageForAnalysis() throws IOException {
        progressBar.setVisibility(View.VISIBLE);
//        segmentedImgView.setImageBitmap(imageBitmap);
        //FoodDetectionModel.
        FoodDetectionModel foodmodel = new FoodDetectionModel(getApplicationContext());//, "yolov2-food100.tflite");
        YoloInference yoloInference = new YoloInference(foodmodel.getInterpreter());

        float[][][] output = yoloInference.runModel(imageBitmap);

        List<YoloDetectionResult> results = YoloPostProcessor.processOutput(output);
        Bitmap resultImage = OverlayDrawer.drawDetections(imageBitmap, results);
        segmentedImgView.setImageBitmap(resultImage);


        progressBar.setVisibility(View.VISIBLE);
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

