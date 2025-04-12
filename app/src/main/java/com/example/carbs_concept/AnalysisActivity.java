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
import androidx.annotation.NonNull;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
    private String imagePath;
//    private String pointCloudPath;
    private ImageView segmentedImgView;
    private ProgressBar progressBar;
    private TextView textStatus;
    private ImageButton btnBackToCamera;
    private String backendUrl;
//    private String portForBackend;
    private TextView txtCarbBreakdown;
    private RecyclerView rvFoodList;
    private FoodAdapter foodAdapter;
    private List<IndividualFoodItem> foodItems;

    private File foodDataFile;
    private File mainImgFile;

    private Map<String, List<Double>> foodsAndInfo;

    private TextView tvMarkerStatus;

    private int responseTimeOut = 600;
    private boolean alertRead;
    private static final DecimalFormat df = new DecimalFormat("0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_analysis);
        Intent intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
//        pointCloudPath = intent.getStringExtra("pointCloudPath");
        backendUrl = intent.getStringExtra("correctServerAddress");
        alertRead = intent.getBooleanExtra("alertRead", false);

        segmentedImgView = findViewById(R.id.segmentedImgView);
        progressBar = findViewById(R.id.progressBar);
        textStatus = findViewById(R.id.textStatus);
        btnBackToCamera = findViewById(R.id.btnBackToCamera);
        rvFoodList = findViewById(R.id.rvFoodList);
        rvFoodList.setLayoutManager(new LinearLayoutManager(this));
        txtCarbBreakdown = findViewById(R.id.txtCarbBreakdown);
        tvMarkerStatus = findViewById(R.id.tvMarkerStatus);
        btnBackToCamera.setOnClickListener(v -> {
            Intent backToCamera = new Intent(this, MainActivity.class);
            backToCamera.putExtra("correctBackendAddress", backendUrl);
            backToCamera.putExtra("alertRead", true);
            startActivity(backToCamera);
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        textStatus.setText(R.string.configuring_connection_with_flask_backend);
        uploadData(imagePath);

    }

    private void uploadData(String imagePath) {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();
//        OkHttpClient client = new OkHttpClient();
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(responseTimeOut, TimeUnit.SECONDS)
                .build();
        File imageFile = new File(imagePath);
//        File pointCloudFile = new File(pointCloudPath);
        if (!imageFile.exists()){
            Log.e("OkHTTP Image Upload", "Files for upload not found.");
            textStatus.setText(R.string.error_unable_to_upload_file);
            textStatus.setTextColor(Color.RED);
            return;
        }
        //Create request body
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg")))
//                .addFormDataPart("pointcloud", pointCloudFile.getName(),
//                        RequestBody.create(pointCloudFile, MediaType.parse("text/plain")))
                .build();

        //Create request itself
        String url = "http://" + backendUrl;
        Request request = new Request.Builder()
                .url(url+"/upload-data")
                .post(requestBody)
                .build();

        //Execute request in background
        textStatus.setText(R.string.processing);
        Log.d("OkHTTP Image Upload", "Creating image upload request to: " + request.url());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.e("OkHTTP Image Upload", "Failed: " + e.getMessage());
                runOnUiThread(() -> textStatus.setText("Request Failed: " + e.getMessage()));
            }
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (response.isSuccessful()) {
                    Log.d("OkHTTP Upload", "Success");

                    InputStream responseZipStream = response.body().byteStream();

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
        //Check if a previous response file exists and delete if so
//        File response_folder = new File(getFilesDir(), "response_data");

        File directory = new File(getFilesDir(), "response_data");
        // Check if the directory exists
        if (directory.exists()) {
            deleteDirectory(directory); // Recursively delete contents
        }

        // Recreate the directory
        if (directory.mkdirs()) {
            Log.d("processAnalysisResponse", "Directory created: " + directory.getAbsolutePath());
        } else {
            Log.e("processAnalysisResponse", "Failed to create directory: " + directory.getAbsolutePath());
        }


        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> saveAndExtractZip(zipInputStream, directory));
        executor.shutdown();
//        saveAndExtractZip(zipInputStream, response_folder);
    }

    private static void deleteDirectory(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteDirectory(child); // Recursively delete files/subdirectories
                }
            }
        }
        file.delete(); // Delete the file or empty folder
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
            mainImgFile = null;
            foodDataFile = null;
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

            updateResultsGUI();//foodDataFile, mainImgFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            Log.e("ZIP Processing", "Error extracting ZIP: " + e.getMessage());
        }
    }

    private void updateResultsGUI(){//File foodDataFile, File mainImgFile) {
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
            ArrayList<Double> totals = getFoodDataFromFile();//foodDataFile);
            boolean markerUsed = getMarkerStatusFromFile();
//            for (FoodItem)
            if (markerUsed) {
                tvMarkerStatus.setText("");
                tvMarkerStatus.setTextColor(Color.BLACK);
            }
            else {
                tvMarkerStatus.setText(R.string.warning_marker_not_found);
                tvMarkerStatus.setTextColor(Color.RED);
            }
            txtCarbBreakdown.setText("Total Carbs: " + df.format(totals.get(0)) + "g" +"\nTotal Volume: " + df.format(totals.get(1)) + "cm³" + "\nTotal Weight: " + df.format(totals.get(2)) + "g");
            //Finally, remove the loading progressbar to show that processing is complete
            progressBar.setVisibility(View.INVISIBLE);
            textStatus.setText(R.string.successfully_processed_food_data);
        });
    }

    public void updateResultsSection(List<IndividualFoodItem> foodList) {
        //Recalculate totals after items removed
        double totalCarbs = 0;
        double totalVolume = 0;
        double totalWeight = 0;

        for (IndividualFoodItem foodItem : foodList) {
            totalCarbs += foodItem.getGramsCarbs();
            totalVolume += foodItem.getEstimatedVolume();
            totalWeight += foodItem.getEstimatedWeight();
        }
        updateTxtBreakdown("Total Carbs: " + df.format(totalCarbs) + "g" +"\nTotal Volume: " + df.format(totalVolume) + "cm³" + "\nTotal Weight: " + df.format(totalWeight) + "g");
    }

    private void updateTxtBreakdown(String text) {
        txtCarbBreakdown.setText(text);
    }

    private boolean getMarkerStatusFromFile() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(foodDataFile));
            String line = reader.readLine(); //Read the first line
            if (line.contains("True")) {
                return true;
            }
            else {
                return false;
            }

        }
        catch (IOException e) {
            Log.e("getMarkerStatusFromFile", "Unable to read food data file");
            return false;
        }
    }

    private ArrayList<Double> getFoodDataFromFile(){
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
        foodsAndInfo = foodDataMap;
        updateRvFoodlist();

        ArrayList<Double> totalsList = new ArrayList<Double>();
        totalsList.add(totalCarbs);
        totalsList.add(totalVolume);
        totalsList.add(totalWeight);
        return totalsList;
    }

    public void updateRvFoodlist(){
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
                File detectionImg = new File(getFilesDir() + "/response_data/", "detection_" + foodName + ".png");
                String detectionImgPath = detectionImg.exists() ? detectionImg.getAbsolutePath() : null;
                foodItems.add(new IndividualFoodItem(detectionImgPath, foodName, carbCount, estimatedWeight, estimatedVolume, confidence, true));
                Log.d("UpdateRvFoodList", "Added food item: " + detectionImgPath);
                img_idx += 1;
            }
        }
        else {
            foodItems.add(new IndividualFoodItem(null, "No recognisable food items found. Please try capturing again!", 0.0, 0.0, 0.0, 0.0, false));
        } //Reference:	@android:drawable/ic_menu_report_image


        foodAdapter = new FoodAdapter(foodItems);

        // Register an AdapterDataObserver
        foodAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                // This will be called when items are removed
                Log.d("RecyclerView", "Items removed from position " + positionStart + ", count: " + itemCount);
                updateResultsSection(foodAdapter.getFoodItems());
                // You can handle the removal here, like updating UI or performing any other actions
            }
        });



        rvFoodList.setAdapter(foodAdapter);
    }

//    private void processTextFile(File textFile) {
//        try {
//            BufferedReader br = new BufferedReader(new FileReader(textFile));
//            StringBuilder text = new StringBuilder();
//            String line;
//            while ((line = br.readLine()) != null) {
//                text.append(line).append("\n");
//            }
//            br.close();
//
//            // Display extracted food data
//            String extractedText = text.toString();
//            Log.d("Food Data", extractedText);
//            runOnUiThread(() -> textStatus.setText(extractedText));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private Bitmap rotateBitmap(Bitmap original, float degrees) {
//        int width = original.getWidth();
//        int height = original.getHeight();
//
//        Matrix matrix = new Matrix();
//        matrix.preRotate(degrees);
//
//        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, true);
//        Canvas canvas = new Canvas(rotatedBitmap);
//        canvas.drawBitmap(original, 5.0f, 0.0f, null);
//
//        return rotatedBitmap;
//    }
}

