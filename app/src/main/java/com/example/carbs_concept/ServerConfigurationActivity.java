package com.example.carbs_concept;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ServerConfigurationActivity extends AppCompatActivity {
    private EditText etBackendAddress;
//    private EditText etPort;
    private Button btnConfigure;
    private TextView tvConnectTestFeedback;
    private Button btnAddAzureBackend;
    public String backendIP = "carbs-processing-backend-f5amajgfbue8bxbq.ukwest-01.azurewebsites.net";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_server_configuration);


        etBackendAddress = findViewById(R.id.etBackendAddress);
//        etPort = findViewById(R.id.etPort);
        btnConfigure = findViewById(R.id.btnConfigure);
        tvConnectTestFeedback = findViewById(R.id.tvConnectTestFeedback);
        btnAddAzureBackend = findViewById(R.id.btnAddAzureBackend);
        btnConfigure.setOnClickListener(v -> {
            testServerConnection(etBackendAddress.getText().toString());//, etPort.getText().toString());
        });
        btnAddAzureBackend.setOnClickListener(v -> {
            etBackendAddress.setText(backendIP);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void testServerConnection(String address) {

        tvConnectTestFeedback.setText("Testing connection...");
        if (address == "") {
            tvConnectTestFeedback.setText("Error: Invalid server address");
            tvConnectTestFeedback.setTextColor(Color.RED);
            return;
        }

        probeServerAndWaitForResponse(address, result -> {
            if (result.contains("Error")) {
                runOnUiThread(() -> {
                    tvConnectTestFeedback.setText("Connection failed: " + result);
                    tvConnectTestFeedback.setTextColor(Color.RED);
                });
            }
            else {
                runOnUiThread(() -> {
                    tvConnectTestFeedback.setText(result);
                    tvConnectTestFeedback.setTextColor(Color.GREEN);

                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("correctBackendAddress", address);
//                    intent.putExtra("correctPort", port);
                    Log.d("Connection to Flask backend", "Correct server address found at " + address);
                    startActivity(intent);
                });
            }
        });



    }

    public interface ServerCallBack {
        void onResult(String result);
    }


    public static void probeServerAndWaitForResponse(String partialAddress, ServerCallBack callBack) {
        OkHttpClient client = new OkHttpClient();
        partialAddress = partialAddress.replace("http://", "");
        partialAddress = partialAddress.replace("https://", "");
        String address = "http://" + partialAddress + "/test";
        Log.d("Server Probe", "Probing server at " + address);
        Request request;
        Handler mainHandler = new Handler(Looper.getMainLooper()); // Get UI thread handler

        try {
            request = new Request.Builder().url(address).build();
        } catch (java.lang.IllegalArgumentException e) {
            return;
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("OkHTTP Server Test", "Failed: " + e.getMessage());

                mainHandler.post(() -> callBack.onResult("Error: " + e.getMessage())); // Run callback on UI thread
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String response_str = response.body().string();
                    Log.d("OkHTTP Server Test", "Success: " + response_str);

                    mainHandler.post(() -> callBack.onResult(response_str)); // Run callback on UI thread
                } else {
                    Log.e("OkHTTP Server Test", "Error: " + response.code());

                    mainHandler.post(() -> callBack.onResult("Error: " + response.code())); // Run callback on UI thread
                }
            }
        });
    }
//        OkHttpClient client = new OkHttpClient();
//        String address = "http://" + partialAddress + "/test";
//        Log.d("Server Probe", "Probing server at " + address);
//        Request request;
//        try {
//            request = new Request.Builder().url(address).build();
//        }
//        catch (java.lang.IllegalArgumentException e) {
//
//            return;
//        }
//
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                e.printStackTrace();
//                Log.e("OkHTTP Server Test", "Failed: " + e.getMessage());
//                callBack.onResult("Error: " + e.getMessage());
//            }
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                if (response.isSuccessful()) {
//                    String response_str = response.body().string();
//                    Log.d("OkHTTP Server Test", "Success: "+ response_str);
//                    callBack.onResult(response_str);
//                }
//                else {
//                    Log.e("OkHTTP Server Test", "Error: "+response.code());
//                    callBack.onResult("Error: " + response.code());
//                }
//            }
//        });


}