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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
            ArrayList<Double> totals = getFoodDataFromFile(foodDataFile);
//            for (FoodItem)

            txtCarbBreakdown.setText("Total Carbs: " + totals.get(0) + "g" +"\nTotal Volume: " + totals.get(1) + "cm^3" + "\nTotal Weight: " + totals.get(2) + "g");
            //Finally, remove the loading progressbar to show that processing is complete
            progressBar.setVisibility(View.INVISIBLE);
            textStatus.setText("Successfully processed food data");
        });
    }

    private ArrayList<Double> getFoodDataFromFile(File foodDataFile) {
//        ArrayList foodItems = new ArrayList<IndividualFoodItem>();
        Map<String, List<Double>> foodDataMap = new HashMap<>();
        double totalCarbs = 0.0;
        double totalVolume = 0.0;
        double totalWeight = 0.0;
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
                // Remove parentheses and split by comma
                String[] parts = line.split(":", 2);;
                if (parts.length == 2) {
                    String foodName = parts[0].trim();
                    String valuesString = parts[1].replaceAll("[()]", "").trim();
                    String[] values = valuesString.split(",\\s*");

                    if (values.length == 4) {
                        try {
                            double carbohydrateCount = Double.parseDouble(values[0]);
                            double estimatedVolume = Double.parseDouble(values[2]);
                            double estimatedWeight = Double.parseDouble(values[3]);
                            List<Double> foodValues = Arrays.asList(
                                    carbohydrateCount,  // carbohydrateCount
                                    Double.parseDouble(values[1]),  // detectionConfidence
                                    estimatedVolume,  // estimatedVolume
                                    estimatedWeight   // estimatedWeight
                            );
                            foodDataMap.put(foodName, foodValues);
                            totalCarbs += carbohydrateCount;
                            totalVolume += estimatedVolume;
                            totalWeight += estimatedWeight;
                        } catch (NumberFormatException e) {
                            Log.e("getFoodDataFromFile", "Error parsing values for " + foodName + ": " + line);
                        }
                    } else {
                        Log.e("getFoodDataFromFile", "Unexpected number of values for " + foodName + ": " + line);
                    }
                } else {
                    Log.e("getFoodDataFromFile", "Invalid format: " + line);
                }
            }
        }
        catch (IOException e) {
            Log.e("getFoodItemsFromFile", "Unable to read food data file");
        }

        updateRvFoodlist(foodDataMap);

        ArrayList<Double> totalsList = new ArrayList<Double>();
        totalsList.add(totalCarbs);
        totalsList.add(totalVolume);
        totalsList.add(totalWeight);
        return totalsList;
    }

    private void updateRvFoodlist(Map<String, List<Double>> foodsAndInfo) {
        //Configure and set up the Recycler view with the food items received from backend
        foodItems = new ArrayList<>();
        if (!foodsAndInfo.isEmpty()) {
            int img_idx = 0;
            for (Map.Entry<String, List<Double>> entry : foodsAndInfo.entrySet()) {
                String foodName = entry.getKey();
                List<Double> counts = entry.getValue();
                Double carbCount = counts.get(0);
                Double confidence = counts.get(1);
                Double estimatedVolume = counts.get(2);
                Double estimatedWeight = counts.get(3);
                File detectionImg = new File(getFilesDir() + "/response_data/", "detection_" + img_idx + ".png");
                String detectionImgPath = detectionImg.exists() ? detectionImg.getAbsolutePath() : null;
                foodItems.add(new IndividualFoodItem(detectionImgPath, foodName, carbCount, estimatedWeight, estimatedVolume, confidence));
                Log.d("UpdateRvFoodList", "Added food item: " + detectionImgPath);

            }
        }
        else {
            foodItems.add(new IndividualFoodItem(null, "No recognisable food items found. Please try capturing again!", 0.0, 0.0, 0.0, 0.0));
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

