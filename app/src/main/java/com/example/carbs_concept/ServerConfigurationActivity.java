package com.example.carbs_concept;

import android.graphics.Color;
import android.os.Bundle;
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
    private String ip;
    private String port;
    private String address;
    private EditText etIP;
    private EditText etPort;
    private Button btnConfigure;
    private TextView tvConnectTestFeedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_server_configuration);


        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        btnConfigure = findViewById(R.id.btnConfigure);
        tvConnectTestFeedback = findViewById(R.id.tvConnectTestFeedback);
        btnConfigure.setOnClickListener(v -> {
            testServerConnection();
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void testServerConnection() {

        tvConnectTestFeedback.setText("Testing connection...");
        String result = probeServer(etIP.getText().toString(), etPort.getText().toString());
        if (result.contains("Error")) {
            tvConnectTestFeedback.setText("Connection failed: " + result);
        }
        else {
            tvConnectTestFeedback.setText(result);
        }


    }

    public static String probeServer(String ip, String port) {
        OkHttpClient client = new OkHttpClient();
        String address = "http://" + ip + ":" + port + "/test";
        Log.d("Server Probe", "Probing server at " + address);
        Request request = new Request.Builder().url(address).build();
        final String[] output = {""};
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("OkHTTP Server Test", "Failed: " + e.getMessage());
//                runOnUiThread(() -> output = "none");
                output[0] = "Error: " + e.getMessage();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String response_str = response.body().string();
                    output[0] = response_str;
                    Log.d("OkHTTP Server Test", "Success"+ response_str);
//                    runOnUiThread(() -> tvConnectTestFeedback.setText(response_str));
                }
                else {
                    Log.e("OkHTTP Server Test", "Error: "+response.code());
//                    runOnUiThread(() -> tvConnectTestFeedback.setText("Request Error: "+response.code()));
                    output[0] = "Error: " + response.code();
                }
            }
        });
        Log.d("probeServer", output[0]);
        return output[0];
    }
}