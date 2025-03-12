package com.example.carbs_concept;

import static java.lang.Double.parseDouble;

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
import androidx.camera.core.processing.SurfaceProcessorNode;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
    private TextView txtCarbBreakdown;
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
//        progressBar.setVisibility(View.INVISIBLE);
        textStatus = findViewById(R.id.textStatus);
        btnBackToCamera = findViewById(R.id.btnBackToCamera);
        rvFoodList = findViewById(R.id.rvFoodList);
        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
        txtCarbBreakdown = findViewById(R.id.txtCarbBreakdown);
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
        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();
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
        textStatus.setText("Processing...");
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
//                    String response_str = response.body().string();
                    Log.d("OkHTTP Upload", "Success");
//                    runOnUiThread(() -> textStatus.setText("Image successfully sent to flask server! \nPress back to take another."));
                    InputStream responseZipStream = response.body().byteStream();
//                    runOnUiThread(() -> textStatus.setText(response_str));
//                    runOnUiThread(() -> textStatus.setTextColor(Color.GREEN));
                    runOnUiThread(() -> processAnalysisResponse(responseZipStream));
                }
                else {
                    Log.e("OkHTTP Image Upload", "Error: "+response.code());
                    runOnUiThread(() -> textStatus.setText("Request Error: "+response.code()));
                }
            }
        });
    }


    private void processAnalysisResponse(InputStream zipInputStream) {
        //Check if a previous response file exists
        File response_folder = new File(getFilesDir(), "response_data");
        if (!response_folder.exists()) {
            response_folder.mkdirs();
        }


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> saveAndExtractZip(zipInputStream, response_folder));
        executor.shutdown();
//        saveAndExtractZip(zipInputStream, response_folder);
    }


    private void saveAndExtractZip(InputStream zipInputStream, File outputDir) {
        try {
            File zipFile = new File(outputDir, "response.zip");
            FileOutputStream fos = new FileOutputStream(zipFile);
            byte[] buffer = new byte[1024];
            int length;

            while ((length = zipInputStream.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }

            fos.close();
            zipInputStream.close();

            unzip(zipFile, outputDir);
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("ZIP Processing", "Error saving/extracting ZIP: " + e.getMessage());
        }
    }

    private void unzip(File zipFile, File outputDir) {
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry entry;
            byte[] buffer = new byte[1024];
            File mainImgFile = null;
            File foodDataFile = null;
            while ((entry = zis.getNextEntry()) != null) {
                File extractedFile = new File(outputDir, entry.getName());

                FileOutputStream fos = new FileOutputStream(extractedFile);

                int length;
                while ((length = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }
                fos.close();
                zis.closeEntry();

                if (entry.getName().equals("foodData.txt")) {
//                    processTextFile(extractedFile);
                    foodDataFile = extractedFile;
                }
                else if(entry.getName().equals("mainImg.png")) {
                    mainImgFile = extractedFile;
                }
            }
            zis.close();
            Log.d("Zip Processing", "Zip extracted Successfully");

            updateResultsGUI(foodDataFile, mainImgFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("ZIP Processing", "Error extracting ZIP: " + e.getMessage());
        }
    }


    private void updateResultsGUI(File foodDataFile, File mainImgFile) {
        runOnUiThread(() -> {
            //Update the main display image
            Bitmap bitmap = BitmapFactory.decodeFile(mainImgFile.getAbsolutePath());
            if (bitmap != null) {
                segmentedImgView.setImageBitmap(bitmap);
            }
            else {
                Log.e("GUI Update", "Unable to display returned mainImg");
            }

            //Update recycler view and get carbs total
            Double totalCarbs = getFoodItemsFromFile(foodDataFile);
//            for (FoodItem)

            txtCarbBreakdown.setText("Food breakdown: \nTotal Carbs: " + totalCarbs + "g");
            //Finally, remove the loading progressbar to show that processing is complete
            progressBar.setVisibility(View.INVISIBLE);
            textStatus.setText("Successfully processed food data");
        });
    }

    private Double getFoodItemsFromFile(File foodDataFile) {
//        ArrayList foodItems = new ArrayList<IndividualFoodItem>();
        double totalCarbs = 0;
        HashMap<String, Double> map = new HashMap<String, Double>();
        String line;
        int linesToSkip = 1;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(foodDataFile));

            for (int i = 0; i < linesToSkip; i++) {
                if (reader.readLine() == null) {
                    Log.e("getFoodItemsFromFile", "File has fewer than " + linesToSkip + " lines.");
                    return null; // Exit early if the file is too short
                }
            }

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":", 2);
                if (parts.length >= 2) {
                    String key = parts[0];
                    Double value = parseDouble(parts[1]);
                    map.put(key, value);
                    totalCarbs += value;
                }
            }
        }
        catch (IOException e) {
            Log.e("getFoodItemsFromFile", "Unable to read food data file");
        }

        updateRvFoodlist(map);


        return totalCarbs;
    }

    private void updateRvFoodlist(HashMap<String, Double> foodsAndInfo) {
        //Configure and set up the Recycler view with the food items recieved from backend
        foodItems = new ArrayList<>();
        if (!foodsAndInfo.isEmpty()) {
            for (Map.Entry<String, Double> entry : foodsAndInfo.entrySet()) {
                String foodName = entry.getKey();
                Double carbCount = entry.getValue();
                foodItems.add(new IndividualFoodItem(R.drawable.ic_launcher_foreground, foodName, carbCount));
            }
        }
        else {
            foodItems.add(new IndividualFoodItem(android.R.drawable.ic_menu_report_image, "No recognisable food items found", 0));
        } //Reference:	@android:drawable/ic_menu_report_image


        foodAdapter = new FoodAdapter(foodItems);
        rvFoodList.setAdapter(foodAdapter);
    }

    private void processTextFile(File textFile) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(textFile));
            StringBuilder text = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line).append("\n");
            }
            br.close();

            // Display extracted food data
            String extractedText = text.toString();
            Log.d("Food Data", extractedText);
            runOnUiThread(() -> textStatus.setText(extractedText));

        } catch (IOException e) {
            e.printStackTrace();
        }
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

