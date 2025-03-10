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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    private TextView textStatus;
    private ImageButton btnBackToCamera;
    private String ipForBackend;
    private String portForBackend;

    private RecyclerView rvFoodList;
    private FoodAdapter foodAdapter;
    private List<IndividualFoodItem> foodItems;
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

        BitmapFactory.Options bmOptions= new BitmapFactory.Options();
        imageBitmap = rotateBitmap(BitmapFactory.decodeFile(imagePath, bmOptions), 90);
        segmentedImgView = findViewById(R.id.segmentedImgView);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        textStatus = findViewById(R.id.textStatus);
        btnBackToCamera = findViewById(R.id.btnBackToCamera);
        rvFoodList = findViewById(R.id.rvFoodList);
        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
        btnBackToCamera.setOnClickListener(v -> {
            Intent backToCamera = new Intent(this, MainActivity.class);
            startActivity(backToCamera);
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textStatus.setText("Configuring Connection with flask backend...");
        uploadData(imagePath, pointCloudPath);

    }


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
                    runOnUiThread(() -> processAnalysisResponse());
                }
                else {
                    Log.e("OkHTTP Image Upload", "Error: "+response.code());
                    runOnUiThread(() -> textStatus.setText("Request Error: "+response.code()));
                }
            }
        });
    }


    private void processAnalysisResponse() {
        //Check if a previous response file exists
        File response_folder = new File(getFilesDir(), "response_data");
        if (!response_folder.exists()) {
            response_folder.mkdirs();
        }

//        saveAndExtractZip(responseZipStream)
    }

    private void updateRvFoodlist() {
        //Configure and set up the Recycler view with the food items recieved from backend
        foodItems = new ArrayList<>();
        foodItems.add(new IndividualFoodItem(R.drawable.ic_launcher_foreground, "Delicious Burger"));
        foodItems.add(new IndividualFoodItem(R.drawable.ic_launcher_foreground, "Cheesy Pizza"));
        foodItems.add(new IndividualFoodItem(R.drawable.ic_launcher_foreground, "Fresh Sushi"));

        foodAdapter = new FoodAdapter(foodItems);
        rvFoodList.setAdapter(foodAdapter);
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

