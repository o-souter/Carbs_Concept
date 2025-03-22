package com.example.carbs_concept;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HelpActivity extends AppCompatActivity {

    private Button btnBackToCapture;
    private Button btnPrintMarkers;
    private String ipForBackend;
    private String portForBackend;
    private String url;
    private ListView lvFoodClasses;
    private TextView tvFlaskBackend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_help);
        Intent intent = getIntent();
        ipForBackend = intent.getStringExtra("correctIP");
        portForBackend = intent.getStringExtra("correctPort");
        url = "http://" + ipForBackend + ":" + portForBackend;
        btnBackToCapture = findViewById(R.id.btnBackToCapture);
        btnPrintMarkers = findViewById(R.id.btnPrintMarkers);
        lvFoodClasses = findViewById(R.id.lvFoodClasses);
        tvFlaskBackend = findViewById(R.id.tvFlaskBackend);
        tvFlaskBackend.setText("Processing backend address: " + url);

        btnBackToCapture.setOnClickListener(v -> {
            Intent goBack = new Intent(this, MainActivity.class);
            startActivity(goBack);
        });

        btnPrintMarkers.setOnClickListener(v -> {
            printMarkers(this);
        });


        requestFoodClasses();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void printMarkers(Context context) {
        File pdfFile = copyPdfToCache(context, "MarkerDoc.pdf");
        if (pdfFile == null) {
            return;
        }
        // Get URI for file
        Uri uri = FileProvider.getUriForFile(
                context,
                "com.example.carbs_concept.provider",
                pdfFile
        );
        // Open PDF with an intent
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);

    }

    private static File copyPdfToCache(Context context, String fileName) {
        try {
            File outputFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            try (InputStream inputStream = context.getAssets().open(fileName);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            }
            return outputFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void requestFoodClasses() {
        OkHttpClient client = new OkHttpClient();
        HttpUrl requestUrl = HttpUrl.parse(url + "/get-food-classes");
        Request request = new Request.Builder()
                .url(requestUrl)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                runOnUiThread(() -> {
//                    lvFoodClasses.add
//                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) return;

                // Read responseBody in background thread
                ResponseBody responseBody = response.body();
                if (responseBody != null) {
                    String responseString = responseBody.string(); // NOW it's on background thread

                    // Process words
                    List<String> foodList = beautifyList(Arrays.asList(responseString.split("\\s+")));

                    // Update UI on the main thread
                    runOnUiThread(() -> {
                        updateFoodClassList(foodList);
                    });
                }
            }
        });
    }

    private List<String> beautifyList(List<String> wordList) {
        ArrayList newList = new ArrayList<String>();
        for (int i = 0; i < wordList.size(); i++) {
            String word = wordList.get(i);
            word = word.substring(0, 1).toUpperCase() + word.substring(1);
            word = word.replace("-", " ");
            newList.add(word);
        }

        return newList;
    }

    private void updateFoodClassList(List<String> foodList) {
        // Update UI on the main thread
        runOnUiThread(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    HelpActivity.this,
                    android.R.layout.simple_list_item_1,
                    foodList
            );
            lvFoodClasses.setAdapter(adapter);
        });

    }
}