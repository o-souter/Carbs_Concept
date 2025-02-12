package com.example.carbs_concept;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FoodDetectionModel {
    private static final String MODEL_FILE = "mixed_model.tflite";
    private Interpreter interpreter;

    public FoodDetectionModel(Context context) {
        try {
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);  // Set the number of threads for performance (optional)
            interpreter = new Interpreter(loadModelFile(context), options);
            Log.d("TFLITE", "Model successfully loaded into Interpreter!");
        } catch (IOException e) {
            Log.e("TFLITE", "Error loading model: " + e.getMessage());
        }
    }

    private MappedByteBuffer loadModelFile(Context context) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public Interpreter getInterpreter() {
        return interpreter;
    }

    public void closeInterpreter() {
        if (interpreter != null) {
            interpreter.close();
            Log.d("TFLITE", "Interpreter closed.");
        }
    }
}
